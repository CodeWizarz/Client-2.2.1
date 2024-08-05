package com.rapidesuite.snapshot.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.rapid4Cloud.snapshotLogFile.StatusType;
import com.rapid4Cloud.snapshotLogFile.StatusType.Enum;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotCreationWorker;
import com.rapidesuite.snapshot.model.UserInformation;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryDetailsGridPanel;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public abstract class SnapshotCreationCommonController extends GenericController{

	private String snapshotName;
	private String snapshotDescription;
	private List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList;
	private List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsToRefreshList;
	private Map<String, Integer> inventoryNameToTableIdMap;
	private Set<Integer> tableIds;
	protected JFrame parentFrame;
	protected JButton cancelButton;
	private TabSnapshotsPanel tabSnapshotsPanel;
	private JLabel executionTimeLabel;
	private JLabel executionStatusLabel;
	private SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel;
	private boolean isConversion;
	private JLabel snapshotIdValueLabel;
	protected String postMessage;
	private String executionTimeLabelTitle;
	private String executionStatusLabelTitle;
	private boolean isShowTotalDetails;
	private SnapshotGridRecord snapshotGridRecord;
	
	public SnapshotCreationCommonController(String snapshotName,String snapshotDescription,
			int workersCount,
			TabSnapshotsPanel tabSnapshotsPanel,
			JFrame parentFrame,
			JButton cancelButton,
			JLabel executionTimeLabel,
			JLabel executionStatusLabel,
			SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel,
			boolean isConversion,
			JLabel snapshotIdValueLabel,
			String postMessage,
			String executionTimeLabelTitle,
			String executionStatusLabelTitle,
			boolean isShowTotalDetails) {
		super();
		super.setSnapshotEnvironmentProperties(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel));
		super.setWorkersCount(workersCount);
		this.postMessage=postMessage;
		this.parentFrame=parentFrame;
		this.snapshotName=snapshotName;
		this.snapshotDescription=snapshotDescription;
		this.cancelButton=cancelButton;
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.executionTimeLabel=executionTimeLabel;
		this.executionStatusLabel=executionStatusLabel;
		this.snapshotInventoryDetailsGridPanel=snapshotInventoryDetailsGridPanel;
		this.isConversion=isConversion;
		this.snapshotIdValueLabel=snapshotIdValueLabel;
		this.executionTimeLabelTitle=executionTimeLabelTitle;
		this.executionStatusLabelTitle=executionStatusLabelTitle;
		this.isShowTotalDetails=isShowTotalDetails;
	}	
	
	public Map<String, Integer> getInventoryNameToTableIdMap() {
		return inventoryNameToTableIdMap;
	}

	public Set<Integer> getTableIds() {
		return tableIds;
	}

	@Override
	public GenericWorker getImplementedWorker() {
		SnapshotCreationWorker snapshotCreationWorker=new SnapshotCreationWorker(this);
		return snapshotCreationWorker;
	}

	@Override
	public void postExecution() {
		if(tabSnapshotsPanel.getMainPanel().getSnapshotMain().isAutomationMode()){
			try {	
				postExecutionGeneric(false);
				postExecutionProcessForAutomationSnapshot();
			} catch (Exception e) {
				String errMsg = "Cannot continue the post execution process, error : "+e.getMessage();
				try {
					tabSnapshotsPanel.getMainPanel().getSnapshotMain().processGenericException(errMsg, e);
				} catch (Exception e1) {
					e1.printStackTrace();
				}	
			}
		}else{
			postExecutionGeneric(true);
		}
	}
	public void postExecutionProcessForAutomationSnapshot() throws Exception{
		File gridOutputFolder = tabSnapshotsPanel.getMainPanel().getSnapshotMain().getAutomationLogFolder();
		tabSnapshotsPanel.getMainPanel().getTabSnapshotsPanel().getSnapshotCreationFrame().getSnapshotCreationActionsPanel()
		.getSnapshotCreationGenericFrame().getSnapshotInventoryDetailsGridPanel()
		.saveGridToExcelInAutomationMode(gridOutputFolder);
		tabSnapshotsPanel.getMainPanel().getSnapshotMain().setProcessComplete(true);
		String message = "";
		if(StatusType.TIME_LIMIT_EXCEEDED.equals(getFinalStatusForAutomationSnapshot())){
			message = "Snapshot was cancelled because the execution time exceed a time limit, snapshot id = '"+getSnapshotGridRecord().getSnapshotId()+"' and snapshot name = '"+getSnapshotGridRecord().getName()+"'";
		}else{
			message = "Snapshot completed with snapshot id = '"+getSnapshotGridRecord().getSnapshotId()+"' and snapshot name = '"+getSnapshotGridRecord().getName()+"'";
		}
		tabSnapshotsPanel.getMainPanel().getSnapshotMain().writeToAutomationLogFile(getFinalStatusForAutomationSnapshot(),message);	
		tabSnapshotsPanel.getMainPanel().getSnapshotMain().closeSnapshotAfterLogged();
	}
	
	public StatusType.Enum getFinalStatusForAutomationSnapshot() throws Exception{
		List<SnapshotInventoryGridRecord> selectedList = tabSnapshotsPanel.getMainPanel().getTabSnapshotsPanel().getSnapshotCreationFrame().getSnapshotCreationActionsPanel()
				.getSnapshotCreationGenericFrame().getSnapshotInventoryDetailsGridPanel().getSelectedSnapshotInventoryGridRecordsList();
		boolean isTimeLimitExceeded = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isTimeLimitExceeded();
		if(isTimeLimitExceeded){
			return StatusType.TIME_LIMIT_EXCEEDED;
		}else{
			for(SnapshotInventoryGridRecord record:selectedList){
				if(!"Completed".equals(record.getStatus())){
					return StatusType.COMPLETED_WITH_ERRORS;
				}
			}
		}
		return StatusType.COMPLETED;
	}
	public void updateAutomationLog() {
		Thread t = new Thread()
		{
			public void run()
			{
				try {
					while (!isWorkerExecutionCompleted()) {
						tabSnapshotsPanel.getMainPanel().getSnapshotMain().writeToAutomationLogFile(StatusType.PROCESSING, "");
						Thread.sleep(5000);
					}
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
			}
		};
		t.start();
	}
	public void postExecutionGeneric(boolean isShowMessageDialog) {
		cancelButton.setEnabled(false);
		updateDatabaseSnapshotRecord();
		try {
			tabSnapshotsPanel.getServerSelectionPanel().unlockPanel();
			tabSnapshotsPanel.getSnapshotsActionsPanel().unlockPanel();
			refreshSnapshots();
			
			if (isShowMessageDialog) {
				JOptionPane.showMessageDialog(parentFrame,postMessage,"Information",JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}
	}
	
	public void postExecutionGenericUI(boolean isShowMessageDialog) {
		cancelButton.setEnabled(false);
		updateDatabaseSnapshotRecord();
		try {
			tabSnapshotsPanel.getServerSelectionPanel().unlockPanel();
			tabSnapshotsPanel.getSnapshotsActionsPanel().unlockPanel();
			refreshSnapshots();
			
			if (isShowMessageDialog) {
				JOptionPane.showMessageDialog(parentFrame,postMessage,"Information",JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}
	}
	
	protected void updateDatabaseSnapshotRecord() {
		try {
			String status="";
			int timeLimit =0;
			if (super.isExecutionStopped()) {
				boolean isAutomationMode = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isAutomationMode();
				boolean isTimeLimitExceeded = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isTimeLimitExceeded();
				if(isAutomationMode && isTimeLimitExceeded){
					status=ModelUtils.DB_STATUS_TIME_LIMIT_EXCEEDED;
					timeLimit= tabSnapshotsPanel.getMainPanel().getSnapshotMain().getSnapshotArgumentsDocument().getSnapshotArguments().getTimeLimit();
				}else{
					status=ModelUtils.DB_STATUS_CANCELLED;
				}
			}
			else{
				status=ModelUtils.DB_STATUS_SUCCESS;
			}
			ModelUtils.updateSnapshot(
					ModelUtils.getJDBCString(getSnapshotEnvironmentProperties()),
					ModelUtils.getDBUserName(getSnapshotEnvironmentProperties()),
					ModelUtils.getDBPassword(getSnapshotEnvironmentProperties()),
					snapshotGridRecord.getSnapshotId(),status,timeLimit);
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
	}
	
	protected void refreshSnapshots() throws Exception {
		SnapshotController snapshotController=new SnapshotController(tabSnapshotsPanel,false);
		snapshotController.startExecution();
	}

	@Override
	public void updateWhileExecution() {
		try{
			refreshGrid();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void refreshGrid() throws Exception {
		String msg=Utils.getExecutionTime(super.getStartTime(),System.currentTimeMillis());
		executionTimeLabel.setText(executionTimeLabelTitle+msg);
		
		List<SnapshotInventoryGridRecord> revisedListToRefresh=new ArrayList<SnapshotInventoryGridRecord>();
		List<SnapshotInventoryGridRecord> itemsToUpdateInThisCycleList=new ArrayList<SnapshotInventoryGridRecord>();
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordsToRefreshList) {
			String status=snapshotInventoryGridRecord.getStatus();
			if (status.equals(UIConstants.UI_STATUS_PENDING) ) {
				revisedListToRefresh.add(snapshotInventoryGridRecord);
				itemsToUpdateInThisCycleList.add(snapshotInventoryGridRecord);
			}
			else
			if ( status.equals(UIConstants.UI_STATUS_UNSELECTED)) {
				itemsToUpdateInThisCycleList.add(snapshotInventoryGridRecord);
			}
			else
			if ( status.equals(UIConstants.UI_STATUS_FAILED) || status.equals(UIConstants.UI_STATUS_COMPLETED) 
					|| status.equals(UIConstants.UI_STATUS_WARNING) || status.equals(UIConstants.UI_STATUS_CANCELLED)) {
				itemsToUpdateInThisCycleList.add(snapshotInventoryGridRecord);
			}
			else
			if ( status.equals(UIConstants.UI_STATUS_PROCESSING)) {
				revisedListToRefresh.add(snapshotInventoryGridRecord);
				itemsToUpdateInThisCycleList.add(snapshotInventoryGridRecord);
				UIUtils.setTime(snapshotInventoryGridRecord);
				snapshotInventoryGridRecord.setRawTimeInSecs(UIUtils.getRawTimeInSecs(snapshotInventoryGridRecord.getStartTime()));
			}
		}
		snapshotInventoryGridRecordsToRefreshList=revisedListToRefresh;
		executionStatusLabel.setText(executionStatusLabelTitle+
				Utils.formatNumberWithComma((snapshotInventoryGridRecordsList.size()-snapshotInventoryGridRecordsToRefreshList.size()))+" / "+
				Utils.formatNumberWithComma(snapshotInventoryGridRecordsList.size()));
		
		snapshotInventoryDetailsGridPanel.refreshGrid(itemsToUpdateInThisCycleList);
		updateTotalLabels();
	}

	public abstract void updateTotalLabels();
	
	@Override
	public void preExecution() {
		try{
			Connection connection=null;
			String templateName = "";
			boolean isAutomationMode = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isAutomationMode();
			try {
				if(isAutomationMode){
					updateAutomationLog();
				}
				String msg=Utils.getExecutionTime(super.getStartTime(),System.currentTimeMillis());
				executionTimeLabel.setText(executionTimeLabelTitle+msg);

				snapshotInventoryGridRecordsList=snapshotInventoryDetailsGridPanel.getSnapshotInventoryGridRecordsList();
				super.setTasksList(snapshotInventoryGridRecordsList);
				snapshotInventoryGridRecordsToRefreshList = new ArrayList<SnapshotInventoryGridRecord>(snapshotInventoryGridRecordsList);
				try{
					templateName = (String) tabSnapshotsPanel.getSnapshotCreationFrame().getTemplateSelectionComboBox().getSelectedItem();
				}catch(Exception e1){
					FileUtils.println("Cannot get template name.");
					FileUtils.printStackTrace(e1);
					templateName = "";
				}

				connection=DatabaseUtils.getJDBCConnection(
						ModelUtils.getJDBCString(getSnapshotEnvironmentProperties()),
						ModelUtils.getDBUserName(getSnapshotEnvironmentProperties()),
						ModelUtils.getDBPassword(getSnapshotEnvironmentProperties()));
				connection.setAutoCommit(false);

				boolean hasSnapshotsInProgress=validateNoSnapshotsInProgress(connection);
				if (hasSnapshotsInProgress && !isAutomationMode) {
					return;
				}
				
				UserInformation selectedUser=tabSnapshotsPanel.getServerSelectionPanel().getSelectedUser();
				int userId=-1;
				if (selectedUser!=null) {
					userId=selectedUser.getId();
				}
				snapshotGridRecord=ModelUtils.createSnapshot(connection,snapshotName,snapshotDescription,isConversion,userId,templateName);
				snapshotIdValueLabel.setText(""+snapshotGridRecord.getSnapshotId());
				
				ModelUtils.insertNewInventories(connection,snapshotInventoryGridRecordsList);
				inventoryNameToTableIdMap=ModelUtils.getInventoryNameToTableIdMap(connection);

				Set<String> selectedTableNamesSet=new HashSet<String>();
				for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordsList) {
					snapshotInventoryGridRecord.setStatus(UIConstants.UI_STATUS_UNSELECTED);
				}	
				
				List<SnapshotInventoryGridRecord> selectedSnapshotInventoryGridRecordsList=snapshotInventoryDetailsGridPanel.getSelectedSnapshotInventoryGridRecordsList();
				for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedSnapshotInventoryGridRecordsList) {
					selectedTableNamesSet.add(snapshotInventoryGridRecord.getInventoryName());
					snapshotInventoryGridRecord.setStatus(UIConstants.UI_STATUS_PENDING);
				}				
				ModelUtils.insertInventoryToSnapshotDBRecords(connection,snapshotGridRecord.getSnapshotId(),inventoryNameToTableIdMap,selectedTableNamesSet);				
				tableIds=ModelUtils.getTableIds(connection);
				connection.commit();
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
			boolean isAutomationMode = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isAutomationMode();
			if(isAutomationMode){
				try {
					tabSnapshotsPanel.getMainPanel().getSnapshotMain().processGenericException(ModelUtils.removeHTMLTagsFromString(e.getMessage()),e);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}else{
				GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
			}
		}
	}
	
	private boolean validateNoSnapshotsInProgress(Connection connection) throws Exception {
		List<SnapshotGridRecord> snapshotGridRecordInProgress=ModelUtils.getSnapshotsInProgress(connection);
		boolean isAutomationMode = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isAutomationMode();
		if (!snapshotGridRecordInProgress.isEmpty()) {
			List<String> list=new ArrayList<String>();
			for (SnapshotGridRecord snapshotGridRecord:snapshotGridRecordInProgress) {
				String clientHostName=snapshotGridRecord.getClientHostName();
				if (clientHostName==null) {
					clientHostName="N/A";
				}
				String record="Snapshot Name: '"+snapshotGridRecord.getName()+"' OS user name: '"+snapshotGridRecord.getOsUserName()+"' from '"+
						clientHostName+"'";
				list.add(record);
			}
			String errMsg = "Snapshot(s) in progress. Please try again later!\n List: "+list;
			if(isAutomationMode){
				tabSnapshotsPanel.getMainPanel().getSnapshotMain().processGenericException(ModelUtils.removeHTMLTagsFromString(errMsg),null);
			}else{
				GUIUtils.popupErrorMessage(errMsg);
			}
			return true;
		}
		return false;
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public boolean isSnapshotForConversionSelected() {
		return isConversion;
	}

	public boolean isShowTotalDetails() {
		return isShowTotalDetails;
	}

	public SnapshotGridRecord getSnapshotGridRecord() {
		return snapshotGridRecord;
	}
	
}