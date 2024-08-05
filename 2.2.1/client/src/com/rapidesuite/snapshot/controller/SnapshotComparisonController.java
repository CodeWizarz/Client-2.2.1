package com.rapidesuite.snapshot.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.model.ControllerModalWindow;
import com.rapidesuite.snapshot.model.FormInformation;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.OperatingUnit;
import com.rapidesuite.snapshot.model.SnapshotComparisonWorker;
import com.rapidesuite.snapshot.view.SnapshotComparisonAnalysisFrame;
import com.rapidesuite.snapshot.view.SnapshotInventoryDetailsGridPanel;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;

public class SnapshotComparisonController extends ViewerAndComparisionAbstractController{
	
	private SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame;
	private ControllerModalWindow controllerModalWindow;
	private List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList;
	private int comparisonId;
	
	public SnapshotComparisonController(SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame) {
		super.setSnapshotEnvironmentProperties(snapshotComparisonAnalysisFrame.getSnapshotEnvironmentProperties());
		String workersCountStr=snapshotComparisonAnalysisFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getWorkersTextField().getText();
		int workersCount=Integer.valueOf(workersCountStr);
		super.setWorkersCount(workersCount);
		this.snapshotComparisonAnalysisFrame=snapshotComparisonAnalysisFrame;
		controllerModalWindow=new ControllerModalWindow(this,true);
	}
	
	@Override
	public GenericWorker getImplementedWorker() {
		SnapshotComparisonWorker snapshotComparisonWorker=new SnapshotComparisonWorker(this);
		return snapshotComparisonWorker;
	}

	@Override
	public void postExecution() {
		controllerModalWindow.updateExecutionLabels("");
		snapshotComparisonAnalysisFrame.getSnapshotComparisonAnalysisGridPanel().displayInventories(snapshotInventoryGridRecordList);
		snapshotComparisonAnalysisFrame.getSnapshotComparisonAnalysisGridPanel().getFilteringTable().setColumnFilterValue(
				SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_FILTERING_RESULT,UIConstants.FILTERED_IN_PREFIX);
		snapshotComparisonAnalysisFrame.getSnapshotComparisonAnalysisGridPanel().getFilteringTable().applyFiltering();
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
				ModelUtils.startModalWindowInThread(snapshotComparisonAnalysisFrame.getTabSnapshotsPanel().
						getMainPanel().getSnapshotMain().getRootFrame(),controllerModalWindow);
				
				controllerModalWindow.updateExecutionLabels("Retrieving Inventories list...");
				connection=DatabaseUtils.getJDBCConnection(
						ModelUtils.getJDBCString(getSnapshotEnvironmentProperties()),
						ModelUtils.getDBUserName(getSnapshotEnvironmentProperties()),
						ModelUtils.getDBPassword(getSnapshotEnvironmentProperties()));
						
				comparisonId=ModelUtils.getNextSequenceIdConnection(connection,"COMPARISON_ID");
				
				snapshotInventoryGridRecordList= ModelUtils.getComparisonSnapshotInventoryGridRecordsList(
						connection,snapshotComparisonAnalysisFrame.getSnapshotGridRecordsInDateOrder());
				if (snapshotInventoryGridRecordList.isEmpty()) {
					GUIUtils.popupInformationMessage("No common Inventories were found accross the selected snapshots!");
				}
				int gridIndex=0;
				TOTAL_STEPS_COUNTER=snapshotInventoryGridRecordList.size();
				controllerModalWindow.setTotalSteps(TOTAL_STEPS_COUNTER);
				
				String connectionOracleRelease=snapshotComparisonAnalysisFrame.getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
				Map<String, FormInformation> inventoryNameToFormInformationMap=snapshotComparisonAnalysisFrame.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
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
				
				controllerModalWindow.updateExecutionLabels("Computing Operating Units condition...");
				initMaps(connection);
			}
			catch(Exception e) {
				FileUtils.printStackTrace(e);
				super.stopExecution();
				connection.rollback();
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

	public SnapshotComparisonAnalysisFrame getSnapshotComparisonAnalysisFrame() {
		return snapshotComparisonAnalysisFrame;
	}
	
	public StringBuffer getOperatingUnitWhereClauseFiltering(FormInformation formInformation, boolean isBGIncluded) throws Exception, ClassNotFoundException, SQLException {
		List<OperatingUnit> selectedOperatingUnits=snapshotComparisonAnalysisFrame.getSnapshotComparisonFiltersPanel().getFilterOperatingUnitPanel().getSelectedOperatingUnits();
		StringBuffer operatingUnitWhereClauseFiltering=getOperatingUnitWhereClauseFiltering(selectedOperatingUnits, formInformation, 
				snapshotComparisonAnalysisFrame.getSnapshotComparisonFiltersPanel().getFilterLevelSelectionPanel().getSelectionLevel());
		
		return getOperatingUnitWhereClauseFiltering(operatingUnitWhereClauseFiltering,isBGIncluded);
	}

	public int getComparisonId() {
		return comparisonId;
	}
	
}