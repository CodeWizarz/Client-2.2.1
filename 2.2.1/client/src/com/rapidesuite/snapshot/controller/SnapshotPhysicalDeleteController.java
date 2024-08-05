package com.rapidesuite.snapshot.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ControllerModalWindow;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotPhysicalDeleteWorker;
import com.rapidesuite.snapshot.view.SnapshotDeletePanel;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class SnapshotPhysicalDeleteController extends GenericController{
	TabSnapshotsPanel tabSnapshotsPanel;
	private int workersCount;
	Map<String, String> snapshotEnvironmentProperties;
	private ControllerModalWindow controllerModalWindow;
	private boolean isExecutionStopped;
	private List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList;
	private SnapshotDeletePanel snapshotDeletePanel;
	private List<Integer> snapshotIdList;
	
	public SnapshotPhysicalDeleteController(TabSnapshotsPanel tabSnapshotsPanel
			,List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList,List<Integer> snapshotIdlist) throws Exception{
		super();
		super.setSnapshotEnvironmentProperties(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel));
		workersCount = Integer.valueOf(tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getWorkersTextField().getText());
		super.setWorkersCount(workersCount);
		this.snapshotEnvironmentProperties = ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		this.snapshotInventoryGridRecordList = snapshotInventoryGridRecordList;
		this.tabSnapshotsPanel = tabSnapshotsPanel;
		this.snapshotIdList = snapshotIdlist;
		controllerModalWindow=new ControllerModalWindow(this,true);
	}
	public boolean isExecutionStopped(){
		return isExecutionStopped;
	}
	@Override
	public void preExecution() {
		try{
			try {
				super.setTasksList(snapshotInventoryGridRecordList);
				startModalWindowInThread();
				
			}
			catch(Exception e) {
				FileUtils.printStackTrace(e);
				super.stopExecution();
				throw e;
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}
	}
	
	public void startModalWindowInThread() {
		final int width=965;
		final int height=700;
		snapshotDeletePanel=new SnapshotDeletePanel(this,tabSnapshotsPanel);
		snapshotDeletePanel.setTotalSteps(snapshotInventoryGridRecordList.size());
		snapshotDeletePanel.getSnapshotIDLabel().setText(String.valueOf(snapshotIdList.size()));
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_PENDING);
			snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(0);
			snapshotInventoryGridRecord.setDownloadRemarks("");
			snapshotInventoryGridRecord.setDownloadTime("");
			snapshotInventoryGridRecord.setDownloadRawTimeInSecs(0);
			snapshotInventoryGridRecord.setDownloadStartTime(-1);
			snapshotInventoryGridRecord.setDownloadTotalRecordsCount(snapshotInventoryGridRecord.getTotalRecords());
		}
		snapshotDeletePanel.getSnapshotDownloadGridPanel().displayInventories(snapshotInventoryGridRecordList);
		Thread thread = new Thread(){
			public void run(){
				String title="Delete Window";
				
				JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindowSnap(tabSnapshotsPanel.
						getMainPanel().getSnapshotMain().getRootFrame(),title,width,height,
						snapshotDeletePanel,controllerModalWindow,false,SnapshotMain.getSharedApplicationIconPath());
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						snapshotDeletePanel.closeWindow();
					}
				});
				((java.awt.Frame)dialog.getOwner()).setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
				snapshotDeletePanel.setDialog(dialog);
				updateStatusLabel("In Progress...",false);
				dialog.setVisible(true);
			}
		};
		thread.start();				
	}	
	public void updateStatusLabel(String text,boolean isError) {
		if (isError) {
			snapshotDeletePanel.getStatusLabel().setForeground(Color.red);
		}
		snapshotDeletePanel.getStatusLabel().setText(text);
	}
	
	@Override
	public GenericWorker getImplementedWorker() {
		SnapshotPhysicalDeleteWorker snapshotPhysicalDeleteSnapshotWorker=new SnapshotPhysicalDeleteWorker(this);
		return snapshotPhysicalDeleteSnapshotWorker;
	}
	@Override
	public void postExecution() {
		updateFailedTasksCounterLabel();
		snapshotDeletePanel.getTotalRecordCountLabel().setText(UIUtils.getFormattedNumber(getTotalRecords()));
		List<SnapshotInventoryGridRecord> failedSnapshotInventoryGridRecordList = new ArrayList<SnapshotInventoryGridRecord>();
		try {
			for(SnapshotInventoryGridRecord obj : snapshotInventoryGridRecordList){
				if(UIConstants.UI_STATUS_FAILED.equals(obj.getDownloadStatus())){
					failedSnapshotInventoryGridRecordList.add(obj);
					break;
				}
			}
			//the process can delete all inventories so we can delete data from SNAPSHOT table.
			if(failedSnapshotInventoryGridRecordList.size()==0){
				ModelUtils.deleteInventoryToSnapshotData(getJDBCConnection(),snapshotIdList,null);	
				ModelUtils.deleteSnapshotDataFromSnapshotTable(getJDBCConnection(),snapshotIdList);	
				
			}
			if(super.isCancelled()) {
				updateStatusLabel("Canceled!",true);
				GUIUtils.popupInformationMessage("Operation canceled!");				
			} 
			else {
				updateStatusLabel("Completed!",false);
				GUIUtils.popupInformationMessage("Operation completed! Please review errors if any.");
			}			
			
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
			updateStatusLabel("Failed ("+e.getMessage()+")",true);
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}finally{
			snapshotDeletePanel.getActionButton().setEnabled(true);
		}	
	}
	
	@Override
	public void updateWhileExecution() {
		snapshotDeletePanel.getTotalTasksLabel().setText(UIUtils.getFormattedNumber(super.getTotalCompletedTasks())+" / "+
				UIUtils.getFormattedNumber(super.getTasksList().size()));
		updateFailedTasksCounterLabel();
		snapshotDeletePanel.getTotalRecordCountLabel().setText(UIUtils.getFormattedNumber(getTotalRecords()));
		snapshotDeletePanel.updateProgressBar(super.getTotalCompletedTasks());
		
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(getStartTime(),currentTime);
		snapshotDeletePanel.getTotalTimeLabel().setText(msg);
		
		snapshotDeletePanel.refreshGrid(snapshotInventoryGridRecordList);
	}	
	
	public void updateFailedTasksCounterLabel() {
		int totalFailedTasks=getTotalFailedTasks();
		if (totalFailedTasks>0) {
			snapshotDeletePanel.getTotalTasksErrorsLabel().setForeground(Color.red);
			snapshotDeletePanel.getTotalTasksErrorsLabel().setFont(new Font("Arial", Font.BOLD, 14));
		}
		String msg="<html>"+UIUtils.getFormattedNumber(getTotalFailedTasks())+" Errors";		
		snapshotDeletePanel.getTotalTasksErrorsLabel().setText(msg);
	}	
	public int getTotalRecords()  {
		int total=0;
		for (GenericWorker genericWorker:super.getWorkersList()) {
			if (genericWorker instanceof SnapshotPhysicalDeleteWorker) {
				SnapshotPhysicalDeleteWorker snapshotPhysicalDeleteSnapshotWorker=(SnapshotPhysicalDeleteWorker)genericWorker;
				total=total+snapshotPhysicalDeleteSnapshotWorker.getTotalDeletedRecords();
			}			
		}
		return total;
	}
	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}
/*	public int getSnapshotId() {
		return snapshotId;
	}*/
	public List<Integer> getSnapshotIdList(){
		return snapshotIdList;
	}
	public Connection getJDBCConnection() throws Exception {
		Connection connection;
		try{
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties),
					false
				);
			return connection;
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Unable to get JDBC Connection, error : "+e.getMessage());
		}
	}	
}
