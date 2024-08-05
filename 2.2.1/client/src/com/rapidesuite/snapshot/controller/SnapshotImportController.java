package com.rapidesuite.snapshot.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ControllerModalWindow;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotImportWorker;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotImportPanel;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class SnapshotImportController extends GenericController{
	
	private TabSnapshotsPanel tabSnapshotsPanel;
	private ControllerModalWindow controllerModalWindow;
	private SnapshotImportPanel snapshotImportPanel;
	private File sourceFolder;
	private List<SnapshotInventoryGridRecord> selectedRecordsList;
	private SnapshotGridRecord snapshotGridRecord;
	private Set<Integer> tableIds;
	public static final int IMPORT_BATCH_SIZE=10000;
	
	public SnapshotImportController(
			Set<Integer> tableIds, File sourceFolder,
			TabSnapshotsPanel tabSnapshotsPanel,List<SnapshotInventoryGridRecord> selectedRecordsList,
			SnapshotGridRecord snapshotGridRecord) {
		super();
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.tableIds=tableIds;
		this.selectedRecordsList=selectedRecordsList;
		this.snapshotGridRecord=snapshotGridRecord;
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		super.setSnapshotEnvironmentProperties(snapshotEnvironmentProperties);
		String workersCountStr=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getWorkersTextField().getText();
		int workersCount=Integer.valueOf(workersCountStr);
		super.setWorkersCount(workersCount);
		this.sourceFolder=sourceFolder;
	}
		
	@Override
	public GenericWorker getImplementedWorker() {
		GenericWorker genericWorker=new SnapshotImportWorker(this);
		return genericWorker;
	}
	
	@Override
	public void preExecution() {
		try{
			try {
				super.setTasksList(selectedRecordsList);
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

	@Override
	public void postExecution() {
		updateFailedTasksCounterLabel();
		snapshotImportPanel.getTotalRecordCountLabel().setText(UIUtils.getFormattedNumber(getTotalRecords()));
		
		try {
			if(super.isCancelled()) {
				updateStatusLabel("Canceled!",true);
				GUIUtils.popupInformationMessage("Operation canceled!");				
			} 
			else {
				updateStatusLabel("Completed!",false);
				GUIUtils.popupInformationMessage("Operation completed! Please review errors if any.");
			}
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			updateStatusLabel("Failed ("+e.getMessage()+")",true);
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}
		finally{
			snapshotImportPanel.getActionButton().setEnabled(true);
			snapshotImportPanel.getCancelButton().setEnabled(false);
		}
	}
	
	@Override
	public void updateWhileExecution() {
		snapshotImportPanel.getTotalTasksLabel().setText(UIUtils.getFormattedNumber(super.getTotalCompletedTasks())+" / "+
				UIUtils.getFormattedNumber(super.getTasksList().size()));
		updateFailedTasksCounterLabel();
		snapshotImportPanel.getTotalRecordCountLabel().setText(UIUtils.getFormattedNumber(getTotalRecords()));
		snapshotImportPanel.updateProgressBar(super.getTotalCompletedTasks());
		
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(getStartTime(),currentTime);
		snapshotImportPanel.getTotalTimeLabel().setText(msg);
		
		snapshotImportPanel.refreshGrid(selectedRecordsList);
	}
	
	public void updateFailedTasksCounterLabel() {
		int totalFailedTasks=getTotalFailedTasks();
		if (totalFailedTasks>0) {
			snapshotImportPanel.getTotalTasksErrorsLabel().setForeground(Color.red);
			snapshotImportPanel.getTotalTasksErrorsLabel().setFont(new Font("Arial", Font.BOLD, 14));
		}
		String msg="<html>"+UIUtils.getFormattedNumber(getTotalFailedTasks())+" Errors";		
		snapshotImportPanel.getTotalTasksErrorsLabel().setText(msg);
	}
	
	public void updateStatusLabel(String text,boolean isError) {
		if (isError) {
			snapshotImportPanel.getStatusLabel().setForeground(Color.red);
		}
		snapshotImportPanel.getStatusLabel().setText(text);
	}
	
	public int getTotalRecords()  {
		int total=0;
		for (GenericWorker genericWorker:super.getWorkersList()) {
			if (genericWorker instanceof SnapshotImportWorker) {
				SnapshotImportWorker snapshotImportWorker=(SnapshotImportWorker)genericWorker;
				total=total+snapshotImportWorker.getTotalImportedRecords();
			}
		}
		return total;
	}
	
	public void startModalWindowInThread() {
		final int width=965;
		final int height=700;
				
		snapshotImportPanel=new SnapshotImportPanel(this,this.getTabSnapshotsPanel());
		snapshotImportPanel.setTotalSteps(selectedRecordsList.size());
		
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedRecordsList) {
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_PENDING);
			snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(0);
			snapshotInventoryGridRecord.setDownloadRemarks("");
			snapshotInventoryGridRecord.setDownloadTime("");
			snapshotInventoryGridRecord.setDownloadRawTimeInSecs(0);
			snapshotInventoryGridRecord.setDownloadStartTime(-1);
		}
		snapshotImportPanel.getSnapshotDownloadGridPanel().displayInventories(selectedRecordsList);

		
		Thread thread = new Thread(){
			public void run(){
				String title="Import Window";
				
				JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindowSnap(tabSnapshotsPanel.
						getMainPanel().getSnapshotMain().getRootFrame(),title,width,height,
						snapshotImportPanel,controllerModalWindow,false,SnapshotMain.getSharedApplicationIconPath());
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						snapshotImportPanel.closeWindow();
					}
				});
				((java.awt.Frame)dialog.getOwner()).setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
				snapshotImportPanel.setDialog(dialog);
				updateStatusLabel("In Progress...",false);
				dialog.setVisible(true);
			}
		};
		thread.start();				
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public SnapshotGridRecord getSnapshotGridRecord() {
		return snapshotGridRecord;
	}

	public List<SnapshotInventoryGridRecord> getSelectedRecordsList() {
		return selectedRecordsList;
	}

	public File getSourceFolder() {
		return sourceFolder;
	}

	public Set<Integer> getTableIds() {
		return tableIds;
	}

}