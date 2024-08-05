package com.rapidesuite.snapshot.controller.upgrade;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.SpreadsheetWriter;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.GenericController;
import com.rapidesuite.snapshot.model.ControllerModalWindow;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.model.ExcelBR100Report;
import com.rapidesuite.snapshot.model.ExcelBR100ReportUtils;
import com.rapidesuite.snapshot.model.ExcelReportTemplate;
import com.rapidesuite.snapshot.model.ExcelReportUtils;
import com.rapidesuite.snapshot.model.ExcelTab;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.DownloadExecutionPanel;
import com.rapidesuite.snapshot.view.GenericRecordInformation;
import com.rapidesuite.snapshot.view.RecordFrame;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class UpgradeMigrationReportController  extends GenericController{
	
	private TabSnapshotsPanel tabSnapshotsPanel;
	private ControllerModalWindow controllerModalWindow;
	private DownloadExecutionPanel downloadExecutionPanel;
	private File downloadFolder;
	private List<SnapshotInventoryGridRecord> selectedRecordsList;
	private SnapshotGridRecord snapshotGridRecord;
	private ExcelBR100Report excelBR100Report;
	private boolean canceled = false;
	private Map<String, Set<File>> ebsInventoryNameToFusionMappingFileMap;

	public static final int EXCEL_MAX_RECORDS_TRUNCATION=50000;
	
	public UpgradeMigrationReportController(TabSnapshotsPanel tabSnapshotsPanel,List<SnapshotInventoryGridRecord> selectedRecordsList,
			SnapshotGridRecord snapshotGridRecord,Map<String, Set<File>> ebsInventoryNameToFusionMappingFileMap) {
		super();
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.selectedRecordsList=selectedRecordsList;
		this.snapshotGridRecord=snapshotGridRecord;
		this.ebsInventoryNameToFusionMappingFileMap=ebsInventoryNameToFusionMappingFileMap;
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		super.setSnapshotEnvironmentProperties(snapshotEnvironmentProperties);
		String workersCountStr=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getWorkersTextField().getText();
		int workersCount=Integer.valueOf(workersCountStr);
		super.setWorkersCount(workersCount);
		File parentFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getDownloadFolder();
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
		Date now = new Date();
		String strDate = format.format(now);
		downloadFolder=new File(parentFolder,"upgrade-report-"+strDate);
		File reportTempFolder=new File(downloadFolder,"temp");
		reportTempFolder.mkdirs();	
	}

	@Override
	public GenericWorker getImplementedWorker() {
		GenericWorker genericWorker=null;
		genericWorker=new UpgradeMigrationReportWorker(this);
		return genericWorker;
	}
	
	@Override
	public void preExecution() {
		try{
			try {
				File reportTempFolder=new File(downloadFolder,"temp");
				excelBR100Report=ExcelBR100ReportUtils.createExcelBR100Report(reportTempFolder,selectedRecordsList);	
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
			if(canceled) {
				updateStatusLabel("Canceled!",true);
				GUIUtils.popupInformationMessage("Operation canceled!");				
			} 
			else {
				updateStatusLabel("Please wait, generating Excel archive...",false);
				ExcelBR100ReportUtils.createExcelBR100TOCSheet(excelBR100Report,true,"EBS inventory Name","Data not migrated Count",ebsInventoryNameToFusionMappingFileMap);
				ExcelBR100ReportUtils.hideEmptySheets(excelBR100Report.getExcelReportTemplate());				
				boolean isSkipZeroRecordsGeneratedEntries=false;
				ExcelBR100ReportUtils.populateExcelBR100DataSheets(excelBR100Report,downloadFolder,
						downloadExecutionPanel.getStatusLabel(),isSkipZeroRecordsGeneratedEntries);
				updateStatusLabel("Please wait deleting Temporary Folder...",false);
				File reportTempFolder=new File(downloadFolder,"temp");
				FileUtils.deleteDirectory(reportTempFolder);
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
				downloadExecutionPanel.setExportFolder(downloadFolder);
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
			if (genericWorker instanceof UpgradeMigrationReportWorker) {
				UpgradeMigrationReportWorker snapshotDownloadWorker=(UpgradeMigrationReportWorker)genericWorker;
				total=total+snapshotDownloadWorker.getTotalDownloadedRecords();
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
		executionTimeLabelText="Generation Time: ";
		totalRecordsCountLabelText="Total generated records count: ";
		outputFolderLabelText="Report location (click to open) : ";
				
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
				String title="Data Analysis Window";
				JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindowSnap(tabSnapshotsPanel.
						getMainPanel().getSnapshotMain().getRootFrame(),title,width,height,
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
	
	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
	
	public static int generateExcelXMLDataFileForUpgradeReport(ExcelBR100Report excelBR100Report,
			GenericRecordInformation genericRecordInformation,Connection connection,
			String sqlQuery,Set<String> unsupportedInventoryFieldNames,SnapshotGridRecord snapshotGridRecord) 
					throws Exception {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		SpreadsheetWriter spreadsheetWriter=null;
		String inventoryName=genericRecordInformation.getInventoryName();
		try
		{
			int inventoryColumnsCount=genericRecordInformation.getInventory().getFieldNamesUsedForDataEntry().size();

			ExcelReportTemplate excelReportTemplate=excelBR100Report.getExcelReportTemplate();
			ExcelTab excelTab=excelReportTemplate.getDataTab(inventoryName);
			File genTempFolder=excelBR100Report.getExcelReportTemplate().getTemplateFile().getParentFile();
			ExcelReportUtils.initExcelTab(excelTab,genTempFolder,inventoryColumnsCount,false,excelBR100Report.getExcelReportTemplate().getStyles(),genericRecordInformation);
			spreadsheetWriter=excelTab.getSpreadsheetWriter();
						
			int excelRowIndex=excelTab.getExcelRowIndex();
			Map<String, XSSFCellStyle> styles=excelBR100Report.getExcelReportTemplate().getStyles();
						
			StringBuffer sqlBuffer =new StringBuffer("");
			sqlBuffer.append(ModelUtils.addOracleUserNamesToQuery(sqlQuery,snapshotGridRecord));
			//System.out.println("generateExcelXMLDataFileForBR100Report: \n"+sqlBuffer.toString());
			statement= connection.prepareStatement(sqlBuffer.toString());
			resultSet=statement.executeQuery();

			DataRow dataRow=null;
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			int recordNumber=0;
			int recordsUnsupportedFieldsWithDataCount=0;
			List<DataRow> dataRows=new ArrayList<DataRow>();
			boolean isFirstBatch=true;
			while (resultSet.next()) {
				recordNumber++;
				dataRow=ModelUtils.getDataRow(resultSet,inventoryColumnsCount,format);
				if (!hasRowUnsupportedFieldsWithData(dataRow,genericRecordInformation.getInventory().getFieldNamesUsedForDataEntry(),
						unsupportedInventoryFieldNames)) {
					continue;
				}
				recordsUnsupportedFieldsWithDataCount++;
				dataRows.add(dataRow);
				if ( (recordNumber % Config.getSnapshotRecordProcessingBatchSize())==0 )
				{
					excelRowIndex=writeXLSXDataRowsHorizontalWay(excelRowIndex,spreadsheetWriter,styles,
							genericRecordInformation.getInventory(),dataRows,isFirstBatch,unsupportedInventoryFieldNames);
					
					if (isFirstBatch) {
						isFirstBatch=false;
					}
					
					dataRows=new ArrayList<DataRow>();
					genericRecordInformation.setDownloadDownloadedRecordsCount(recordNumber);
					UIUtils.setDownloadTime(genericRecordInformation);
				}
				if (recordNumber>=UpgradeMigrationReportController.EXCEL_MAX_RECORDS_TRUNCATION) {
					excelTab.setTruncated(true);
					break;
				}
			}
			writeXLSXDataRowsHorizontalWay(excelRowIndex,spreadsheetWriter,styles,
					genericRecordInformation.getInventory(),dataRows,isFirstBatch,unsupportedInventoryFieldNames);
			excelTab.setGeneratedRecordsCount(recordsUnsupportedFieldsWithDataCount);
			UIUtils.setDownloadTime(genericRecordInformation);
			
			ExcelReportUtils.endSheetAndCloseExcelFile(excelBR100Report.getExcelReportTemplate().getDataTab(inventoryName));
			
			return recordsUnsupportedFieldsWithDataCount;
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			ExcelReportUtils.closeAndDeleteExcelFile(excelBR100Report.getExcelReportTemplate().getDataTab(inventoryName));
			throw e;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	private static boolean hasRowUnsupportedFieldsWithData(DataRow dataRow,List<String> dataInventoryFields,Set<String> unsupportedInventoryFieldNames) {
		for (int i=0;i<dataInventoryFields.size();i++) {				
			String value=dataRow.getDataValues()[i];
			String fieldName=dataInventoryFields.get(i);
			if ( value!= null && !value.isEmpty() && unsupportedInventoryFieldNames.contains(fieldName) ){
				return true;
			}
		}
		return false;
	}

	public static int writeXLSXDataRowsHorizontalWay(int startingRowIndex,SpreadsheetWriter sw,Map<String, XSSFCellStyle> styles,
			Inventory inventory,List<DataRow> dataRows,boolean isFirstBatch,Set<String> unsupportedInventoryFieldNames) throws IOException {
		int rowIndex=startingRowIndex;
		int startingExcelColIndex=0; // first column is index 0
		int colIndex=startingExcelColIndex;
		List<String> dataInventoryFields=inventory.getFieldNamesUsedForDataEntry();
		
		if (isFirstBatch) {
			sw.insertRow(rowIndex++);
			sw.createCell(colIndex++,"Row #",styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			for (int dataInventoryFieldIndex=0;dataInventoryFieldIndex<dataInventoryFields.size();dataInventoryFieldIndex++) {
				String fieldName=dataInventoryFields.get(dataInventoryFieldIndex);
				sw.createCell(colIndex++,fieldName,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			}
			sw.createCell(colIndex++,RecordFrame.FIELD_AUDIT_LAST_UPDATED_BY,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			sw.createCell(colIndex++,RecordFrame.FIELD_AUDIT_LAST_UPDATE_DATE,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			sw.createCell(colIndex++,RecordFrame.FIELD_AUDIT_CREATED_BY,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			sw.createCell(colIndex++,RecordFrame.FIELD_AUDIT_CREATION_DATE,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			sw.endRow();
		}
		
		int rowCountIndex=rowIndex-6; // all the headers rows to remove for now hardcoded
		short redStyleIndex=styles.get(ExcelReportUtils.XLSX_STYLE_LIGHT_RED_BOLD_BACKGROUND).getIndex();
		for (DataRow dataRow:dataRows) {
			sw.insertRow(rowIndex++);
			
			short styleIndex=-1;
			if (rowCountIndex%2 == 0){
				styleIndex=styles.get(ExcelReportUtils.XLSX_STYLE_BORDERED_BACKGROUND).getIndex();
			}
			else {
				styleIndex=styles.get(ExcelReportUtils.XLSX_STYLE_ALTERNATE_GREY_BACKGROUND).getIndex();
			}
			rowCountIndex++;
			
			colIndex=startingExcelColIndex;
			sw.createCell(colIndex++,""+rowCountIndex,styleIndex);
			for (int dataInventoryFieldIndex=0;dataInventoryFieldIndex<dataInventoryFields.size();dataInventoryFieldIndex++) {	
				int valStyleIndex=styleIndex;
				String value=dataRow.getDataValues()[dataInventoryFieldIndex];
				if ( value == null ){
					value="";
				}
				if (!value.isEmpty()) {
					String fieldName=dataInventoryFields.get(dataInventoryFieldIndex);
					if (unsupportedInventoryFieldNames.contains(fieldName)) {
						valStyleIndex=redStyleIndex;
					}
				}
				sw.createCell(colIndex++,value,valStyleIndex);
			}
			String value=dataRow.getRscLastUpdatedByName();
			if ( value == null ){
				value="";
			}
			sw.createCell(colIndex++,value,styleIndex);
			
			value=dataRow.getRscLastUpdateDate();
			if ( value == null ){
				value="";
			}
			sw.createCell(colIndex++,value,styleIndex);
			
			value=dataRow.getRscCreatedByName();
			if ( value == null ){
				value="";
			}
			sw.createCell(colIndex++,value,styleIndex);
			
			value=dataRow.getRscCreationDate();
			if ( value == null ){
				value="";
			}
			sw.createCell(colIndex++,value,styleIndex);
						
			sw.endRow();
		}
		return rowIndex;
	}
	
	public ExcelBR100Report getExcelBR100Report() {
		return excelBR100Report;
	}	

	public Map<String, Set<File>> getEbsInventoryNameToFusionMappingFileMap() {
		return ebsInventoryNameToFusionMappingFileMap;
	}
	
}