package com.rapidesuite.snapshot.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.model.ControllerModalWindow;
import com.rapidesuite.snapshot.model.FormInformation;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.OperatingUnit;
import com.rapidesuite.snapshot.model.SnapshotImportSwingWorker;
import com.rapidesuite.snapshot.model.SnapshotViewerWorker;
import com.rapidesuite.snapshot.model.TotalsObject;
import com.rapidesuite.snapshot.view.FilterDatePanel;
import com.rapidesuite.snapshot.view.FiltersPanel;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryDetailsGridPanel;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;
import com.rapidesuite.snapshot.view.UIConstants;

public class SnapshotViewerController extends ViewerAndComparisionAbstractController{
	
	private TabSnapshotsPanel tabSnapshotsPanel;
	private ControllerModalWindow controllerModalWindow;
	protected List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList;
	protected SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel;
	private JFrame parentFrame;
	private SnapshotGridRecord snapshotGridRecord;
	private FiltersPanel filtersPanel;
	private Map<Integer,TotalsObject> tableIdToTotalsObjectMap;

	public SnapshotViewerController(TabSnapshotsPanel tabSnapshotsPanel,
			SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel,
			JFrame parentFrame,
			SnapshotGridRecord snapshotGridRecord,
			FiltersPanel filtersPanel) {
		super.setSnapshotEnvironmentProperties(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel));
		String workersCountStr=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getWorkersTextField().getText();
		int workersCount=Integer.valueOf(workersCountStr);
		super.setWorkersCount(workersCount);
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		controllerModalWindow=new ControllerModalWindow(this,true);
		this.snapshotInventoryDetailsGridPanel=snapshotInventoryDetailsGridPanel;
		this.parentFrame=parentFrame;
		this.snapshotGridRecord=snapshotGridRecord;
		this.filtersPanel=filtersPanel;
		tableIdToTotalsObjectMap=new HashMap<Integer,TotalsObject>();
	}
	
	public Map<Integer, TotalsObject> getTableIdToTotalsObjectMap() {
		return tableIdToTotalsObjectMap;
	}
	
	@Override
	public GenericWorker getImplementedWorker() {
		SnapshotViewerWorker snapshotViewerWorker=new SnapshotViewerWorker(this);
		return snapshotViewerWorker;
	}

	@Override
	public void postExecution() {
		snapshotInventoryDetailsGridPanel.getFilteringTable().setColumnFilterValue(
				SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_FILTERING_RESULT,UIConstants.FILTERED_IN_PREFIX);
		snapshotInventoryDetailsGridPanel.displayInventories(snapshotInventoryGridRecordList);
	}
	
	@Override
	public void updateWhileExecution() {
		controllerModalWindow.updateExecutionLabels("Analyzing tables: "+super.getTotalCompletedTasks()+" / "+super.getTasksList().size()+" ...");
		controllerModalWindow.getProgressBar().setValue(super.getTotalCompletedTasks());
	}
	

	@Override
	public void preExecution() {
		try{
			Connection connection=null;
			try {
				ModelUtils.startModalWindowInThread(parentFrame,controllerModalWindow);
				
				controllerModalWindow.updateExecutionLabels("Retrieving Inventories list...");
				
				connection=DatabaseUtils.getJDBCConnection(
						ModelUtils.getJDBCString(getSnapshotEnvironmentProperties()),
						ModelUtils.getDBUserName(getSnapshotEnvironmentProperties()),
						ModelUtils.getDBPassword(getSnapshotEnvironmentProperties()));
				
				snapshotInventoryGridRecordList= ModelUtils.getSnapshotInventoryGridRecordsList(
						connection,snapshotGridRecord.getSnapshotId(),null);
				snapshotInventoryGridRecordList = getListableSnapshotInventoryGridRecord(snapshotInventoryGridRecordList);
				int gridIndex=0;
	
				TOTAL_STEPS_COUNTER=snapshotInventoryGridRecordList.size();
				controllerModalWindow.setTotalSteps(TOTAL_STEPS_COUNTER);
								
				controllerModalWindow.updateExecutionLabels("Retrieving Inventories form information...");
				
				String connectionOracleRelease=tabSnapshotsPanel.getServerSelectionPanel().getCurrentConnectionOracleRelease();
				Map<String, FormInformation> inventoryNameToFormInformationMap=tabSnapshotsPanel.getSnapshotPackageSelectionPanel().
						getInventoryNameToFormInformation(connectionOracleRelease);
				for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
					snapshotInventoryGridRecord.setDefaultSelected(true);
					snapshotInventoryGridRecord.setGridIndex(gridIndex);
					FormInformation formInformation=inventoryNameToFormInformationMap.get(snapshotInventoryGridRecord.getInventoryName());
					if (formInformation!=null) {
						snapshotInventoryGridRecord.setFormInformation(formInformation);
					}			
					gridIndex++;
				}
				super.setTasksList(snapshotInventoryGridRecordList);	
					
				controllerModalWindow.updateExecutionLabels("Retrieving Operating Units filtering information...");
				if (snapshotGridRecord!=null && snapshotGridRecord.getMode().equals(SnapshotImportSwingWorker.IMPORT_MODE)) {
					initImportMaps(connection,snapshotGridRecord.getSnapshotId());
				}
				else {
					initMaps(connection);
				}
			}
			catch(Exception e) {
				FileUtils.printStackTrace(e);
				super.stopExecution();
				if (connection!=null) connection.rollback();
				throw e;
			}
			finally{
				DirectConnectDao.closeQuietly(connection);
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}
	}
	
	public List<SnapshotInventoryGridRecord> getListableSnapshotInventoryGridRecord( List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList){
		List<SnapshotInventoryGridRecord> toReturn = new ArrayList<SnapshotInventoryGridRecord>();
		for(SnapshotInventoryGridRecord obj : snapshotInventoryGridRecordList){
			if(obj.isListable()){
				toReturn.add(obj);
			}
		}
		return toReturn;
		
	}
	
	protected void initImportMaps(Connection connection, int snapshotId) throws Exception {
		operatingUnitIdToInventoryOrganizationsList=ModelUtils.getOperatingUnitIdToInventoryOrganizationsListImport(connection,snapshotId);
		operatingUnitToLedgersMap = ModelUtils.getOperatingUnitToLedgersMapImport(connection,snapshotId);
		operatingUnitToLegalEntityMap = ModelUtils.getOperatingUnitIdToLegalEntityListImport(connection,snapshotId);
		operatingUnitToBusinessGroupMap = ModelUtils.getOperatingUnitIdToBusinessGroupListImport(connection,snapshotId);
	}

	public StringBuffer getOperatingUnitWhereClauseFiltering(FormInformation formInformation, boolean isBGIncluded) throws Exception, ClassNotFoundException, SQLException {
		List<OperatingUnit> selectedOperatingUnits=filtersPanel.getFilterOperatingUnitPanel().getSelectedOperatingUnits();
		StringBuffer operatingUnitWhereClauseFiltering=getOperatingUnitWhereClauseFiltering(selectedOperatingUnits, formInformation, 
				filtersPanel.getFilterLevelSelectionPanel().getSelectionLevel());
		
		return getOperatingUnitWhereClauseFiltering(operatingUnitWhereClauseFiltering,isBGIncluded);
	}

	public String getFilteringDatesWhereClauseFiltering() {
		boolean isCreationDateEnabled=filtersPanel.getFilterDatePanel().isCreationDateEnabled();
		boolean isLastUpdateDateEnabled=filtersPanel.getFilterDatePanel().isLastUpdateDateEnabled();
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		String dbDateFormat ="DD-MON-YYYY HH24:MI:SS";
		
		StringBuffer toRetun=new StringBuffer("");
		if (isCreationDateEnabled) {
			toRetun.append(" and RSC_CREATION_DATE ");
			Date fromDate=filtersPanel.getFilterDatePanel().getFromCreationDate();
			String formattedFromDate=format.format(fromDate);
			Date toDate=filtersPanel.getFilterDatePanel().getToCreationDate();
			String formattedToDate=format.format(toDate);
			String creationDateComparator=filtersPanel.getFilterDatePanel().getCreationDateComparator();
			if (creationDateComparator.equalsIgnoreCase(FilterDatePanel.COMPARISON_BETWEEN)) {
				toRetun.append(" BETWEEN ").append("to_date('").append(formattedFromDate).append("','").append(dbDateFormat).append("') ").
				append(" AND to_date('").append(formattedToDate).append("','").append(dbDateFormat).append("') ");
			}
			else {
				toRetun.append(creationDateComparator).append(" to_date('").append(formattedFromDate).append("','").append(dbDateFormat).append("') ");
			}
		}
		
		if (isLastUpdateDateEnabled) {
			toRetun.append(" and RSC_LAST_UPDATE_DATE ");
			Date fromDate=filtersPanel.getFilterDatePanel().getFromLastUpdateDate();
			String formattedFromDate=format.format(fromDate);
			Date toDate=filtersPanel.getFilterDatePanel().getToLastUpdateDate();
			String formattedToDate=format.format(toDate);
			String creationDateComparator=filtersPanel.getFilterDatePanel().getLastUpdateDateComparator();
			if (creationDateComparator.equalsIgnoreCase(FilterDatePanel.COMPARISON_BETWEEN)) {
				toRetun.append(" BETWEEN ").append("to_date('").append(formattedFromDate).append("','").append(dbDateFormat).append("') ").
				append(" AND to_date('").append(formattedToDate).append("','").append(dbDateFormat).append("') ");
			}
			else {
				toRetun.append(creationDateComparator).append(" to_date('").append(formattedFromDate).append("','").append(dbDateFormat).append("') ");
			}
		}
		
		return toRetun.toString();
	}

	public String getCreatedByUsersWhereClauseFiltering() {
		StringBuffer toRetun=new StringBuffer("");
		
		if (filtersPanel.getFilterUserPanel().isCreatedByUserNameEnabled()) {
			List<Integer> createdByUserIdList=filtersPanel.getFilterUserPanel().getCreatedBySelectedUserIds();
			if (!createdByUserIdList.isEmpty()) {
				toRetun.append(" AND rsc_created_by IN (");
				for (int i=0;i<createdByUserIdList.size();i++) {
					Integer userId=createdByUserIdList.get(i);
					toRetun.append(userId);
					if ( (i+1)<createdByUserIdList.size() ) {
						toRetun.append(",");
					}
				}
				toRetun.append(")");
			}
		}
		return toRetun.toString();
	}

	public Object getLastUpdatedByUsersWhereClauseFiltering() {
		StringBuffer toRetun=new StringBuffer("");

		if (filtersPanel.getFilterUserPanel().isLastUpdateByUserNameEnabled()) {
			List<Integer> lastUpdatedByUserIdList=filtersPanel.getFilterUserPanel().getLastUpdatedBySelectedUserIds();
			if (!lastUpdatedByUserIdList.isEmpty()) {
				toRetun.append(" AND rsc_last_updated_by IN (");
				for (int i=0;i<lastUpdatedByUserIdList.size();i++) {
					Integer userId=lastUpdatedByUserIdList.get(i);
					toRetun.append(userId);
					if ( (i+1)<lastUpdatedByUserIdList.size() ) {
						toRetun.append(",");
					}
				}
				toRetun.append(")");
			}
		}
		return toRetun.toString();
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public int getSnapshotId() {
		return snapshotGridRecord.getSnapshotId();
	}

	public FiltersPanel getFiltersPanel() {
		return filtersPanel;
	}
	
}
