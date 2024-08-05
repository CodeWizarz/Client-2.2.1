package com.rapidesuite.snapshot.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ControllerModalWindow;
import com.rapidesuite.snapshot.model.ExcelBR100Report;
import com.rapidesuite.snapshot.model.ExcelBR100ReportUtils;
import com.rapidesuite.snapshot.model.ExcelComparisonReport;
import com.rapidesuite.snapshot.model.ExcelComparisonReportUtils;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotComparisonDownloadWorker;
import com.rapidesuite.snapshot.model.SnapshotViewerDownloadWorker;
import com.rapidesuite.snapshot.view.DownloadExecutionPanel;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class SnapshotDownloadController extends GenericController{
	
	private TabSnapshotsPanel tabSnapshotsPanel;
	private ControllerModalWindow controllerModalWindow;
	private DownloadExecutionPanel downloadExecutionPanel;
	private File downloadFolder;
	private List<SnapshotInventoryGridRecord> selectedRecordsList;
	private SnapshotGridRecord snapshotGridRecord;
	private boolean isDownloadFromViewer;
	private boolean isComparisonReportGeneration;
	private boolean isBR100ReportGeneration;
	private ExcelComparisonReport excelComparisonReport;
	private ExcelBR100Report excelBR100Report;
	private boolean isDownloadDataChangesOnly;
	private boolean isExport;
    private JDialog dialog;
	public SnapshotDownloadController(
			File downloadFolder,
			TabSnapshotsPanel tabSnapshotsPanel,List<SnapshotInventoryGridRecord> selectedRecordsList,
			SnapshotGridRecord snapshotGridRecord,boolean isDownloadFromViewer,boolean isComparisonReportGeneration,boolean isBR100ReportGeneration,
			boolean isDownloadDataChangesOnly,boolean isExport) {
		super();
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.isExport=isExport;
		this.isDownloadDataChangesOnly=isDownloadDataChangesOnly;
		this.isComparisonReportGeneration=isComparisonReportGeneration;
		this.isBR100ReportGeneration=isBR100ReportGeneration;
		this.selectedRecordsList=selectedRecordsList;
		this.isDownloadFromViewer=isDownloadFromViewer;
		this.snapshotGridRecord=snapshotGridRecord;
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		super.setSnapshotEnvironmentProperties(snapshotEnvironmentProperties);
		String workersCountStr=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getWorkersTextField().getText();
		int workersCount=Integer.valueOf(workersCountStr);
		super.setWorkersCount(workersCount);
		this.downloadFolder=downloadFolder;
	}	
	
	public SnapshotDownloadController(TabSnapshotsPanel tabSnapshotsPanel,List<SnapshotInventoryGridRecord> selectedRecordsList,
			SnapshotGridRecord snapshotGridRecord,boolean isDownloadFromViewer,boolean isComparisonReportGeneration,boolean isBR100ReportGeneration,
			boolean isDownloadDataChangesOnly,boolean isExport) {
		
		this( getDownloadFolder(
				tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getDownloadFolder(),
				isComparisonReportGeneration,isBR100ReportGeneration,isExport)
				,tabSnapshotsPanel,selectedRecordsList,snapshotGridRecord,isDownloadFromViewer,
				isComparisonReportGeneration,isBR100ReportGeneration,
				isDownloadDataChangesOnly,isExport);
	}	

	public static File getDownloadFolder(File parentFolder,boolean isComparisonReportGeneration,boolean isBR100ReportGeneration,
			boolean isExport) {
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
		Date now = new Date();
		String strDate = format.format(now);
		File downloadFolder=null;
		if (isComparisonReportGeneration) {
			downloadFolder=new File(parentFolder,"comparison-report-"+strDate);
			File reportTempFolder=new File(downloadFolder,"temp");
			reportTempFolder.mkdirs();
		}
		else
		if (isBR100ReportGeneration) {
			downloadFolder=new File(parentFolder,"BR100-report-"+strDate);
			File reportTempFolder=new File(downloadFolder,"temp");
			reportTempFolder.mkdirs();
		}
		else
			if (isExport) {
				downloadFolder=new File(parentFolder,"export-"+strDate);
				File reportTempFolder=new File(downloadFolder,"temp");
				reportTempFolder.mkdirs();
			}
		else {
			downloadFolder=new File(parentFolder,"download-"+strDate);
			downloadFolder.mkdirs();
		}
		return downloadFolder;
	}
	
	@Override
	public GenericWorker getImplementedWorker() {
		GenericWorker genericWorker=null;
		if (isDownloadFromViewer) {
			genericWorker=new SnapshotViewerDownloadWorker(this,isBR100ReportGeneration);
		}
		else {
			genericWorker=new SnapshotComparisonDownloadWorker(this,isComparisonReportGeneration);
		}
		return genericWorker;
	}
	
	@Override
	public void preExecution() {
		try{
			try {
				if (isBR100ReportGeneration) {
					File reportTempFolder=new File(downloadFolder,"temp");
					excelBR100Report=ExcelBR100ReportUtils.createExcelBR100Report(reportTempFolder,selectedRecordsList);
				}
				else
				if (isComparisonReportGeneration) {
					File reportTempFolder=new File(downloadFolder,"temp");
					excelComparisonReport=ExcelComparisonReportUtils.createExcelComparisonReport(reportTempFolder,selectedRecordsList);
				}			
				startModalWindowInThread();
				super.setTasksList(selectedRecordsList);
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
		downloadExecutionPanel.getTotalExportedRecordCountLabel().setText(UIUtils.getFormattedNumber(getTotalDownloadedRecords()));
		
		try {
			if(super.isCancelled()) {
				updateStatusLabel("Canceled!",true);
				GUIUtils.popupInformationMessage("Operation canceled!");				
			} else {
				if (isBR100ReportGeneration) {
					updateStatusLabel("Please wait, generating Excel archive...",false);
					ExcelBR100ReportUtils.createExcelBR100TOCSheet(excelBR100Report,false,null,null,null);
					ExcelBR100ReportUtils.populateExcelBR100DataSheets(excelBR100Report,downloadFolder,
							downloadExecutionPanel.getStatusLabel(),true);
					updateStatusLabel("Please wait deleting Temporary Folder...",false);
					File reportTempFolder=new File(downloadFolder,"temp");
					FileUtils.deleteDirectory(reportTempFolder);
				}
				else
				if (isComparisonReportGeneration) {
					updateStatusLabel("Please wait, generating Excel archive...",false);
					ExcelComparisonReportUtils.createExcelComparisonTOCSheets(excelComparisonReport);
					ExcelComparisonReportUtils.regenerateExcelWorkbooks(excelComparisonReport);
					ExcelComparisonReportUtils.populateExcelComparisonDataSheets(excelComparisonReport,downloadFolder,downloadExecutionPanel.getStatusLabel());
					updateStatusLabel("Please wait deleting Temporary Folder...",false);
					File reportTempFolder=new File(downloadFolder,"temp");
					FileUtils.deleteDirectory(reportTempFolder);
				}
				else
				if (isExport) {
						File parentFolder=downloadFolder.getParentFile();
						File zippedFile=new File(parentFolder,downloadFolder.getName()+".zip");
						updateStatusLabel("Please wait compressing Export Folder...",false);
						
						boolean isEncryptedExportedSnapshort = Config.isEncryptedExportedSnapshort();
						FileUtils.zipFolder(downloadFolder, zippedFile,isEncryptedExportedSnapshort);
						FileUtils.deleteDirectory(downloadFolder);
				}
				else {
					File parentFolder=downloadFolder.getParentFile();
					File zippedFile=new File(parentFolder,downloadFolder.getName()+".zip");
					updateStatusLabel("Please wait compressing Download Folder...",false);
					FileUtils.zipFolder(downloadFolder, zippedFile);
					FileUtils.deleteDirectory(downloadFolder);
				}
				updateStatusLabel("Completed!",false);
			    GUIUtils.popupInformationMessage(dialog, "Operation completed! Please review errors if any.");
			
			
			}
			
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			updateStatusLabel("Failed ("+e.getMessage()+")",true);
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}
		finally{
			if(!super.isCancelled()) {
				File parentFolder=downloadFolder.getParentFile();
				downloadExecutionPanel.setExportFolder(parentFolder);
			}
			downloadExecutionPanel.getActionButton().setEnabled(true);
			downloadExecutionPanel.getCancelButton().setEnabled(false);
		}
	}
	
	@Override
	public void updateWhileExecution() {
		downloadExecutionPanel.getTotalTasksLabel().setText(UIUtils.getFormattedNumber(super.getTotalCompletedTasks())+" / "+
				UIUtils.getFormattedNumber(super.getTasksList().size()));
		updateFailedTasksCounterLabel();
		downloadExecutionPanel.getTotalExportedRecordCountLabel().setText(UIUtils.getFormattedNumber(getTotalDownloadedRecords()));
		downloadExecutionPanel.updateProgressBar(super.getTotalCompletedTasks());
		
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(getStartTime(),currentTime);
		downloadExecutionPanel.getTotalDownloadTimeLabel().setText(msg);
		
		downloadExecutionPanel.refreshGrid(selectedRecordsList);
	}
	
	public void updateFailedTasksCounterLabel() {
		int totalFailedTasks=getTotalFailedTasks();
		if (totalFailedTasks>0) {
			downloadExecutionPanel.getTotalTasksErrorsLabel().setForeground(Color.red);
			downloadExecutionPanel.getTotalTasksErrorsLabel().setFont(new Font("Arial", Font.BOLD, 14));
		}
		String msg="<html><U>"+UIUtils.getFormattedNumber(getTotalFailedTasks())+" Errors</U>";		
		downloadExecutionPanel.getTotalTasksErrorsLabel().setText(msg);
	}
	
	public void updateStatusLabel(String text,boolean isError) {
		if (isError) {
			downloadExecutionPanel.getStatusLabel().setForeground(Color.red);
		}
		downloadExecutionPanel.getStatusLabel().setText(text);
	}
	
	public int getTotalDownloadedRecords()  {
		int total=0;
		for (GenericWorker genericWorker:super.getWorkersList()) {
			if (genericWorker instanceof SnapshotViewerDownloadWorker) {
				SnapshotViewerDownloadWorker snapshotDownloadWorker=(SnapshotViewerDownloadWorker)genericWorker;
				total=total+snapshotDownloadWorker.getTotalDownloadedRecords();
			}
			else 
				if (genericWorker instanceof SnapshotComparisonDownloadWorker) {
					SnapshotComparisonDownloadWorker snapshotComparisonDownloadWorker=(SnapshotComparisonDownloadWorker)genericWorker;
					total=total+snapshotComparisonDownloadWorker.getTotalDownloadedRecords();
				}
		}
		return total;
	}

	public File getJobDownloadFolder()  {
		return downloadFolder;
	}
	
	public void startModalWindowInThread() {
		final int width=965;
		final int height=700;
		
		String executionTimeLabelText=null;
		String totalRecordsCountLabelText=null;
		String outputFolderLabelText=null;
		if (isComparisonReportGeneration || isBR100ReportGeneration) {
			executionTimeLabelText="Generation Time: ";
			totalRecordsCountLabelText="Total generated records count: ";
			outputFolderLabelText="Report location (click to open) : ";
		}
		else
		if (isExport) {
			executionTimeLabelText="Export Time: ";
			totalRecordsCountLabelText="Total exported records count: ";
			outputFolderLabelText="Export Folder (click to open) : ";
		}
		else {
			executionTimeLabelText="Download Time: ";
			totalRecordsCountLabelText="Total downloaded records count: ";
			outputFolderLabelText="Download Folder (click to open) : ";
		}
		
		downloadExecutionPanel=new DownloadExecutionPanel(this,this.getTabSnapshotsPanel(),executionTimeLabelText,totalRecordsCountLabelText,outputFolderLabelText);
		downloadExecutionPanel.setTotalSteps(selectedRecordsList.size());
		
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedRecordsList) {
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_PENDING);
			snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(0);
			snapshotInventoryGridRecord.setDownloadTotalRecordsCount(0);
			snapshotInventoryGridRecord.setDownloadRemarks("");
			snapshotInventoryGridRecord.setDownloadTime("");
			snapshotInventoryGridRecord.setDownloadRawTimeInSecs(0);
			snapshotInventoryGridRecord.setDownloadStartTime(-1);
		}
		downloadExecutionPanel.getSnapshotDownloadGridPanel().displayInventories(selectedRecordsList);

		
		Thread thread = new Thread(){
			public void run(){
				String title=null;
				if (isComparisonReportGeneration ) {
					title="Comparison Report Generation Window";
				}
				else
				if (isBR100ReportGeneration ) {
					title="BR100 Report Generation Window";
				}
				else
				if (isExport) {
					title="Export Window";
				}
				else {
					title="Download Window";
				}
				
				
			    dialog=UIUtils.displayOperationInProgressComplexModalWindowSnap(tabSnapshotsPanel.getSnapshotViewerFrame(),title,width,height,
						downloadExecutionPanel,controllerModalWindow,false,SnapshotMain.getSharedApplicationIconPath());
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						downloadExecutionPanel.closeWindow();
					}
				});
				((java.awt.Frame)dialog.getOwner()).setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
				downloadExecutionPanel.setDialog(dialog);
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

	public ExcelComparisonReport getExcelComparisonReport() {
		return excelComparisonReport;
	}

	public ExcelBR100Report getExcelBR100Report() {
		return excelBR100Report;
	}

	public boolean isDownloadDataChangesOnly() {
		return isDownloadDataChangesOnly;
	}

	public boolean isExport() {
		return isExport;
	}
		
}