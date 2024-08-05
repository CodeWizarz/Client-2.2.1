package com.rapidesuite.snapshot.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.SpreadsheetWriter;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.view.GenericRecordInformation;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIUtils;

public class ExcelBR100ReportUtils {
	
	public static ExcelBR100Report createExcelBR100Report(File reportTempFolder,List<SnapshotInventoryGridRecord> selectedRecordsList)throws Exception {
		ExcelBR100Report excelBR100Report=new ExcelBR100Report();
		
		ExcelReportTemplate excelReportTemplate=excelBR100Report.getExcelReportTemplate();
		ExcelReportUtils.createExcelTemplate(reportTempFolder,excelReportTemplate,selectedRecordsList);
		
		return excelBR100Report;
	}
	
	public static void createExcelBR100TOCSheet(ExcelBR100Report excelBR100Report,boolean isHideEmptyInventory,String inventoryLabelOverride,
			String recordsGeneratedLabelOverride,Map<String, Set<File>> ebsInventoryNameToFusionMappingFileMap) throws Exception {
		ExcelReportUtils.createExcelTOCSheet(excelBR100Report.getExcelReportTemplate(),isHideEmptyInventory,inventoryLabelOverride,
				recordsGeneratedLabelOverride,ebsInventoryNameToFusionMappingFileMap);
	}
	
	public static void populateExcelBR100DataSheets(ExcelBR100Report excelBR100Report,File reportFolder,
			JLabel statusLabel,boolean isSkipZeroRecordsGeneratedEntries) throws Exception {
		ExcelReportTemplate excelReportTemplate=excelBR100Report.getExcelReportTemplate();
		String reportName=reportFolder.getName()+".xlsx";
		File reportFile=new File(reportFolder,reportName);
		ExcelReportUtils.populateExcelDataSheets(excelReportTemplate,reportFile,statusLabel,false,isSkipZeroRecordsGeneratedEntries);
	}
	
	public static int generateExcelXMLDataFileForBR100Report(ExcelBR100Report excelBR100Report,
			GenericRecordInformation genericRecordInformation,Connection connection,
			String sqlQuery,SnapshotGridRecord snapshotGridRecord,boolean isVerticalWay) 
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
			ExcelReportUtils.initExcelTab(excelTab,genTempFolder,inventoryColumnsCount,isVerticalWay,excelBR100Report.getExcelReportTemplate().getStyles(),genericRecordInformation);
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
			List<DataRow> dataRows=new ArrayList<DataRow>();
			boolean isFirstBatch=true;
			while (resultSet.next()) {
				recordNumber++;
				dataRow=ModelUtils.getDataRow(resultSet,inventoryColumnsCount,format);
				dataRows.add(dataRow);
				if ( (recordNumber % Config.getSnapshotRecordProcessingBatchSize())==0 )
				{	
					
					if (isVerticalWay) {
						excelRowIndex=ExcelReportUtils.writeXLSXDataRowsVerticalWay(excelRowIndex,spreadsheetWriter,styles,
								genericRecordInformation.getInventory(),snapshotGridRecord.getName(),dataRows);
					}
					else {
						excelRowIndex=ExcelReportUtils.writeXLSXDataRowsHorizontalWay(excelRowIndex,spreadsheetWriter,styles,genericRecordInformation.getInventory(),dataRows,isFirstBatch);
					}
					if (isFirstBatch) {
						isFirstBatch=false;
					}
					
					dataRows=new ArrayList<DataRow>();
					genericRecordInformation.setDownloadDownloadedRecordsCount(recordNumber);
					UIUtils.setDownloadTime(genericRecordInformation);
				}
				if (recordNumber>=ExcelReportUtils.EXCEL_MAX_RECORDS_TRUNCATION) {
					break;
				}
			}
			if (isVerticalWay) {
				ExcelReportUtils.writeXLSXDataRowsVerticalWay(excelRowIndex,spreadsheetWriter,styles,genericRecordInformation.getInventory(),
						snapshotGridRecord.getName(),dataRows);
			}
			else {
				ExcelReportUtils.writeXLSXDataRowsHorizontalWay(excelRowIndex,spreadsheetWriter,styles,genericRecordInformation.getInventory(),dataRows,isFirstBatch);
			}
			excelTab.setGeneratedRecordsCount(recordNumber);
			UIUtils.setDownloadTime(genericRecordInformation);
			
			ExcelReportUtils.endSheetAndCloseExcelFile(excelBR100Report.getExcelReportTemplate().getDataTab(inventoryName));
			
			return recordNumber;
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

	public static void hideEmptySheets(ExcelReportTemplate excelReportTemplate) throws IOException {
		File templateFile=excelReportTemplate.getTemplateFile();
		InputStream inp = null;
		try {
			inp = new FileInputStream(templateFile);
			XSSFWorkbook workbook = new XSSFWorkbook(inp);
			List<ExcelTab> dataTabList=excelReportTemplate.getDataTabList();
			for (ExcelTab excelTab:dataTabList) {		
				if (excelTab.getGeneratedRecordsCount()==0) {
					workbook.setSheetHidden(excelTab.getSheetIndex(), true);
				}
			}			
			//save the template
			FileOutputStream os = new FileOutputStream(templateFile.getAbsolutePath());
			workbook.write(os);
			os.close();
		} 
		finally {
			if (inp!=null) {
				inp.close();
			}
		}			
	}
	
}
