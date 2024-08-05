package com.rapidesuite.snapshot.controller.upgrade;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JDialog;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.GenericController;
import com.rapidesuite.snapshot.model.ControllerModalWindow;
import com.rapidesuite.snapshot.model.ExcelReportUtils;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;
import com.rapidesuite.snapshot.view.convert.MappingAnalyzer;
import com.rapidesuite.snapshot.view.upgrade.FusionInventoryRow;
import com.rapidesuite.snapshot.view.upgrade.UpgradeFrame;
import com.rapidesuite.snapshot.view.upgrade.UpgradeFusionReportPanel;

public class UpgradeFusionReportController  extends GenericController{

	private ControllerModalWindow controllerModalWindow;
	private boolean canceled;
	private UpgradeFrame upgradeFrame;
	private List<FusionInventoryRow> fusionInventoryRowList;
	private File reportFolder;
	private UpgradeFusionReportPanel upgradeFusionReportPanel;
	private Map<String,Map<String,String>> fusionInventoryNameToColumnNameDefaultValueMap;
	private int totalStepsToExecute;
	
	public UpgradeFusionReportController(UpgradeFrame upgradeFrame, List<FusionInventoryRow> fusionInventoryRowList,
			int workersCount,File rootGenerationFolder) {
		super.setWorkersCount(workersCount);
		this.upgradeFrame=upgradeFrame;
		this.fusionInventoryRowList=fusionInventoryRowList;
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
		Date now = new Date();
		String strDate = format.format(now);
		reportFolder=new File(rootGenerationFolder,"upgrade-default-values-report-"+strDate);
		reportFolder.mkdirs();
		fusionInventoryNameToColumnNameDefaultValueMap=new TreeMap<String,Map<String,String>>(String.CASE_INSENSITIVE_ORDER);
	}

	@Override
	public GenericWorker getImplementedWorker() {
		GenericWorker genericWorker=null;
		genericWorker=new UpgradeFusionReportWorker(this);
		return genericWorker;
	}
	
	@Override
	public void preExecution() {
		try{
			try {
				startModalWindowInThread();
				super.setTasksList(fusionInventoryRowList);
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
		try {
			if(canceled) {
				updateStatusLabel("Canceled!",true);
				GUIUtils.popupInformationMessage("Operation canceled!");				
			} 
			else {
				saveToExcel();
				upgradeFusionReportPanel.updateProgressBar(totalStepsToExecute);
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
			if(!canceled) {
				upgradeFusionReportPanel.setExportFolder(reportFolder);
			}
			upgradeFusionReportPanel.getActionButton().setEnabled(true);
			upgradeFusionReportPanel.getCancelButton().setEnabled(false);
		}
	}
	
	@Override
	public void updateWhileExecution() {
		upgradeFusionReportPanel.getTotalTasksLabel().setText(UIUtils.getFormattedNumber(super.getTotalCompletedTasks())+" / "+
				UIUtils.getFormattedNumber(super.getTasksList().size()));
		upgradeFusionReportPanel.updateProgressBar(super.getTotalCompletedTasks());
		
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(getStartTime(),currentTime);
		upgradeFusionReportPanel.getTotalDownloadTimeLabel().setText(msg);
		
		upgradeFusionReportPanel.refreshGrid(fusionInventoryRowList);
	}
		
	public void updateStatusLabel(String text,boolean isError) {
		if (isError) {
			upgradeFusionReportPanel.getStatusLabel().setForeground(Color.red);
		}
		upgradeFusionReportPanel.getStatusLabel().setText(text);
	}
	
	public int getTotalDownloadedRecords()  {
		int total=0;
		for (GenericWorker genericWorker:super.getWorkersList()) {
			if (genericWorker instanceof UpgradeMigrationReportWorker) {
				UpgradeMigrationReportWorker snapshotDownloadWorker=(UpgradeMigrationReportWorker)genericWorker;
				total=total+snapshotDownloadWorker.getTotalDownloadedRecords();
			}
		}
		return total;
	}
	
	public void startModalWindowInThread() {
		final int width=965;
		final int height=700;
		
		String executionTimeLabelText=null;
		String outputFolderLabelText=null;
		executionTimeLabelText="Generation Time: ";
		outputFolderLabelText="Report location (click to open) : ";
				
		upgradeFusionReportPanel=new UpgradeFusionReportPanel(this,executionTimeLabelText,outputFolderLabelText);
		totalStepsToExecute=fusionInventoryRowList.size()+500; // adding a bit more so that it does not show
		// 100% while the report is being saved to excel.
		upgradeFusionReportPanel.setTotalSteps(totalStepsToExecute);
		
		for (FusionInventoryRow fusionInventoryRow:fusionInventoryRowList) {
			fusionInventoryRow.setReportStatus(UIConstants.UI_STATUS_PENDING);
			fusionInventoryRow.setReportRemarks("");
			fusionInventoryRow.setDownloadTime("");
			fusionInventoryRow.setDownloadRawTimeInSecs(0);
			fusionInventoryRow.setDownloadStartTime(-1);
		}
		upgradeFusionReportPanel.getUpgradeFusionReportGridPanel().displayInventories(fusionInventoryRowList);

		
		Thread thread = new Thread(){
			public void run(){
				String title="Default Values Report Window";
				JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindowSnap(upgradeFrame,title,width,height,
						upgradeFusionReportPanel,controllerModalWindow,false,SnapshotMain.getSharedApplicationIconPath());
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						upgradeFusionReportPanel.closeWindow();
					}
				});
				((java.awt.Frame)dialog.getOwner()).setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
				upgradeFusionReportPanel.setDialog(dialog);
				updateStatusLabel("In Progress...",false);
				dialog.setVisible(true);
			}
		};
		thread.start();				
	}
	
	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public Map<String, Map<String, String>> getFusionInventoryNameToColumnNameDefaultValueMap() {
		return fusionInventoryNameToColumnNameDefaultValueMap;
	}
	
	protected void saveToExcel() throws Exception {
		String EXCEL_FILE_EXTENSION=".xlsx";
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
		Date now = new Date();
		String strDate = format.format(now);
		File outputFile=new File(reportFolder,"RAPIDUpgrade-default-values-report-"+strDate+EXCEL_FILE_EXTENSION);
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet firstSheet = workbook.createSheet("Default Values") ;
		XSSFCellStyle headerBlueStyle = MappingAnalyzer.createExcelStandardStyleInstance(workbook,true,true,true,new Color(83,141,213),true);
		 
		ExcelReportUtils.createExcelWithImage(this.getClass(),workbook,firstSheet);
		
		int startingRowIndex=2;
		int startingCellIndex=1;
		int rowIndex=startingRowIndex;
		int cellIndex=startingCellIndex;
		
		XSSFRow headerRow = firstSheet.createRow(rowIndex++);
		
		XSSFCell cell = headerRow.createCell(cellIndex++);
		cell.setCellValue("Row count");
		cell.setCellStyle(headerBlueStyle);
		cell = headerRow.createCell(cellIndex++);
		cell.setCellValue("Fusion task name");
		cell.setCellStyle(headerBlueStyle);
		cell = headerRow.createCell(cellIndex++);
		cell.setCellValue("Fusion inventory name");
		cell.setCellStyle(headerBlueStyle);
		cell = headerRow.createCell(cellIndex++);
		cell.setCellValue("Fusion column name");
		cell.setCellStyle(headerBlueStyle);
		cell = headerRow.createCell(cellIndex++);
		cell.setCellValue("Fusion default values");
		cell.setCellStyle(headerBlueStyle);
		
		int counter=0;
		Iterator<String> iterator=fusionInventoryNameToColumnNameDefaultValueMap.keySet().iterator();
		Map<String, List<FusionInventoryRow>> inventoryNameToFusionInventoryRowsMap=upgradeFrame.getTabUpgradeMainPanel().getFusionInventoryGridPanel().getInventoryNameToFusionInventoryRowsMap();
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			List<FusionInventoryRow> fusionInventoryRows=inventoryNameToFusionInventoryRowsMap.get(inventoryName);
			String taskName="N/A";
			if (fusionInventoryRows!=null && !fusionInventoryRows.isEmpty()) {
				FusionInventoryRow fusionInventoryRow=fusionInventoryRows.get(0);
				taskName=fusionInventoryRow.getFusionInventoryInformation().getTaskName();
			}
			Map<String, String> columnNameDefaultValueMap=fusionInventoryNameToColumnNameDefaultValueMap.get(inventoryName);
			Iterator<String> iteratorTemp=columnNameDefaultValueMap.keySet().iterator();
			while (iteratorTemp.hasNext()) {
				counter++;
				String columnName=iteratorTemp.next();
				String defaultValue=columnNameDefaultValueMap.get(columnName);
				//FileUtils.println("'"+inventoryName+"' columnName: '"+columnName+"' / default Value: '"+defaultValue+"'");
				
				XSSFRow excelRow = firstSheet.createRow(rowIndex++);
				cellIndex=startingCellIndex;
				
				// Row count
				cell = excelRow.createCell(cellIndex++);
				cell.setCellValue(""+counter);
		
				// Fusion task name
				cell = excelRow.createCell(cellIndex++);
				cell.setCellValue(taskName);
				
				// Fusion inventory name
				cell = excelRow.createCell(cellIndex++);
				cell.setCellValue(inventoryName);
				
				// Fusion column name
				cell = excelRow.createCell(cellIndex++);
				cell.setCellValue(columnName);
				
				// Fusion default value
				cell = excelRow.createCell(cellIndex++);
				cell.setCellValue(defaultValue);
			}
		}
		firstSheet.autoSizeColumn(0);
		firstSheet.autoSizeColumn(1);
		firstSheet.autoSizeColumn(2);
		firstSheet.autoSizeColumn(3);
		firstSheet.autoSizeColumn(4);
		firstSheet.autoSizeColumn(5);
		firstSheet.autoSizeColumn(6);
		firstSheet.autoSizeColumn(7);
		
		FileOutputStream fileOut = new FileOutputStream(outputFile);
		workbook.write(fileOut);
		fileOut.flush();
		fileOut.close();
	}

	public UpgradeFrame getUpgradeFrame() {
		return upgradeFrame;
	}

		
}