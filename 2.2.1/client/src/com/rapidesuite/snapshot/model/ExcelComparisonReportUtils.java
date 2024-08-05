package com.rapidesuite.snapshot.model;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.SpreadsheetWriter;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.view.RecordFrame;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIUtils;

public class ExcelComparisonReportUtils {

	private enum COMPARISON_RECORD_TYPE {ADD,DELETE,UPDATE};
	private enum COMPARISON_TYPE {ALL,ADD,DELETE,UPDATE};
	
	public static ExcelComparisonReport createExcelComparisonReport(File reportTempFolder,List<SnapshotInventoryGridRecord> selectedRecordsList)throws Exception {
		ExcelComparisonReport excelComparisonReport=new ExcelComparisonReport();
		
		ExcelReportTemplate allChangesExcelReportTemplate=excelComparisonReport.getAllChangesExcelReportTemplate();
		File genTempFolder=new File(reportTempFolder,COMPARISON_TYPE.ALL.toString());
		ExcelReportUtils.createExcelTemplate(genTempFolder,allChangesExcelReportTemplate,selectedRecordsList);
		
		ExcelReportTemplate addChangesExcelReportTemplate=excelComparisonReport.getAddChangesExcelReportTemplate();
		genTempFolder=new File(reportTempFolder,COMPARISON_TYPE.ADD.toString());
		ExcelReportUtils.createExcelTemplate(genTempFolder,addChangesExcelReportTemplate,selectedRecordsList);
		
		ExcelReportTemplate deleteChangesExcelReportTemplate=excelComparisonReport.getDeleteChangesExcelReportTemplate();
		genTempFolder=new File(reportTempFolder,COMPARISON_TYPE.DELETE.toString());
		ExcelReportUtils.createExcelTemplate(genTempFolder,deleteChangesExcelReportTemplate,selectedRecordsList);
		
		ExcelReportTemplate updateChangesExcelReportTemplate=excelComparisonReport.getUpdateChangesExcelReportTemplate();
		genTempFolder=new File(reportTempFolder,COMPARISON_TYPE.UPDATE.toString());
		ExcelReportUtils.createExcelTemplate(genTempFolder,updateChangesExcelReportTemplate,selectedRecordsList);
		
		return excelComparisonReport;
	}
	
	public static void createExcelComparisonTOCSheets(ExcelComparisonReport excelComparisonReport) throws Exception {
		ExcelReportUtils.createExcelTOCSheet(excelComparisonReport.getAllChangesExcelReportTemplate(),false,null,null,null);
		ExcelReportUtils.createExcelTOCSheet(excelComparisonReport.getAddChangesExcelReportTemplate(),true,null,null,null);
		ExcelReportUtils.createExcelTOCSheet(excelComparisonReport.getDeleteChangesExcelReportTemplate(),true,null,null,null);
		ExcelReportUtils.createExcelTOCSheet(excelComparisonReport.getUpdateChangesExcelReportTemplate(),true,null,null,null);
	}
	
	public static void populateExcelComparisonDataSheets(ExcelComparisonReport excelComparisonReport,File reportFolder,JLabel statusLabel) throws Exception {
		ExcelReportTemplate allChangesExcelReportTemplate=excelComparisonReport.getAllChangesExcelReportTemplate();
		if (allChangesExcelReportTemplate.getTotalGeneratedRecordsCount()>0) {
			String reportName=COMPARISON_TYPE.ALL.toString()+"-"+reportFolder.getName()+".xlsx";
			File reportFile=new File(reportFolder,reportName);
			ExcelReportUtils.populateExcelDataSheets(allChangesExcelReportTemplate,reportFile,statusLabel,true,true);
		}
		ExcelReportTemplate addChangesExcelReportTemplate=excelComparisonReport.getAddChangesExcelReportTemplate();
		if (addChangesExcelReportTemplate.getTotalGeneratedRecordsCount()>0) {
			String reportName=COMPARISON_TYPE.ADD.toString()+"-"+reportFolder.getName()+".xlsx";
			File reportFile=new File(reportFolder,reportName);
			ExcelReportUtils.populateExcelDataSheets(addChangesExcelReportTemplate,reportFile,statusLabel,true,true);
		}
		ExcelReportTemplate deleteChangesExcelReportTemplate=excelComparisonReport.getDeleteChangesExcelReportTemplate();
		if (deleteChangesExcelReportTemplate.getTotalGeneratedRecordsCount()>0) {
			String reportName=COMPARISON_TYPE.DELETE.toString()+"-"+reportFolder.getName()+".xlsx";
			File reportFile=new File(reportFolder,reportName);
			ExcelReportUtils.populateExcelDataSheets(deleteChangesExcelReportTemplate,reportFile,statusLabel,true,true);
		}
		ExcelReportTemplate updateChangesExcelReportTemplate=excelComparisonReport.getUpdateChangesExcelReportTemplate();
		if (updateChangesExcelReportTemplate.getTotalGeneratedRecordsCount()>0) {
			String reportName=COMPARISON_TYPE.UPDATE.toString()+"-"+reportFolder.getName()+".xlsx";
			File reportFile=new File(reportFolder,reportName);
			ExcelReportUtils.populateExcelDataSheets(updateChangesExcelReportTemplate,reportFile,statusLabel,true,true);
		}		
	}
	
	public static int generateExcelXMLDataFileForComparisonReport(ExcelComparisonReport excelComparisonReport,
			int comparisonId,SnapshotInventoryGridRecord snapshotInventoryGridRecord,
			Connection connection,int tableId,List<Integer> primaryKeysPositionList,List<SnapshotGridRecord> snapshotGridRecords,
			boolean isVerticalWay) 
	throws Exception
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String inventoryName=snapshotInventoryGridRecord.getInventoryName();
		try
		{
			int inventoryColumnsCount=snapshotInventoryGridRecord.getInventory().getFieldNamesUsedForDataEntry().size();
					
			ExcelTab allChangesExcelDataTab=excelComparisonReport.getAllChangesExcelReportTemplate().getDataTab(snapshotInventoryGridRecord.getInventoryName());
			File genTempFolder=excelComparisonReport.getAllChangesExcelReportTemplate().getTemplateFile().getParentFile();
			ExcelReportUtils.initExcelTab(allChangesExcelDataTab,genTempFolder,inventoryColumnsCount,isVerticalWay,
					excelComparisonReport.getAllChangesExcelReportTemplate().getStyles(),snapshotInventoryGridRecord);
			
			ExcelTab addChangesExcelDataTab=excelComparisonReport.getAddChangesExcelReportTemplate().getDataTab(snapshotInventoryGridRecord.getInventoryName());
			genTempFolder=excelComparisonReport.getAddChangesExcelReportTemplate().getTemplateFile().getParentFile();
			ExcelReportUtils.initExcelTab(addChangesExcelDataTab,genTempFolder,inventoryColumnsCount,isVerticalWay,
					excelComparisonReport.getAddChangesExcelReportTemplate().getStyles(),snapshotInventoryGridRecord);
			
			ExcelTab updateChangesExcelDataTab=excelComparisonReport.getUpdateChangesExcelReportTemplate().getDataTab(snapshotInventoryGridRecord.getInventoryName());
			genTempFolder=excelComparisonReport.getUpdateChangesExcelReportTemplate().getTemplateFile().getParentFile();
			ExcelReportUtils.initExcelTab(updateChangesExcelDataTab,genTempFolder,inventoryColumnsCount,isVerticalWay,
					excelComparisonReport.getUpdateChangesExcelReportTemplate().getStyles(),snapshotInventoryGridRecord);
			
			ExcelTab deleteChangesExcelDataTab=excelComparisonReport.getDeleteChangesExcelReportTemplate().getDataTab(snapshotInventoryGridRecord.getInventoryName());
			genTempFolder=excelComparisonReport.getDeleteChangesExcelReportTemplate().getTemplateFile().getParentFile();
			ExcelReportUtils.initExcelTab(deleteChangesExcelDataTab,genTempFolder,inventoryColumnsCount,isVerticalWay,
					excelComparisonReport.getDeleteChangesExcelReportTemplate().getStyles(),snapshotInventoryGridRecord);
						
			//FileUtils.println("generateExcelXMLDataFileForComparisonReport, tableId:"+tableId);
			snapshotInventoryGridRecord.setDownloadRemarks("Running query...");
			StringBuffer sqlQuery = ModelUtils.getSQLDataRowsComparisonChanges(comparisonId,tableId,primaryKeysPositionList,snapshotGridRecords);
			sqlQuery=ModelUtils.addOracleUserNamesToQuery(sqlQuery.toString(),null);
			//List<List<String>> distinctPKValuesList=ModelUtils.getComparisonDataRowsDistinctPKValues(connection,comparisonId,
			//		tableId,primaryKeysPositionList,-1,-1);
			//List<String> preparedStatementValues=new ArrayList<String>();
			//StringBuffer sqlBuffer=ModelUtils.getComparisonSQLQuery(sqlQuery,primaryKeysPositionList,distinctPKValuesList,preparedStatementValues);
			//FileUtils.println("generateExcelXMLDataFileForComparisonReport, sql:\n"+sqlQuery.toString());
			
			statement= connection.prepareStatement(sqlQuery.toString());
			//int parameterIndex=1;
			//for (String value:preparedStatementValues) {
			//	statement.setString(parameterIndex++,value);
			//}
			resultSet=statement.executeQuery();
			snapshotInventoryGridRecord.setDownloadRemarks("Fetching data...");
			
			// Map of forms record with all their baselines versions
			List<Map<Integer,DataRow>> snapshotIdToDataRowsMapList=new ArrayList<Map<Integer,DataRow>>();

			String previousCompositeKeyValue=null;
			Map<Integer,DataRow> snapshotIdToRecordMap=new HashMap<Integer,DataRow>();
			DataRow dataRow=null;
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			int recordNumber=0;
			while (resultSet.next()) {
				int snapshotId=resultSet.getInt("SNAPSHOT_ID");

				String compositeKeyValue=ModelUtils.concatenatePKValues(primaryKeysPositionList,resultSet);
				if (previousCompositeKeyValue==null || !previousCompositeKeyValue.equals(compositeKeyValue) ) {
					
					if ( (recordNumber % Config.getSnapshotRecordProcessingBatchSize())==0 )
					{
						writeComparisonXLSXDataRowsVerticalWay(
								snapshotInventoryGridRecord.getInventory(),snapshotGridRecords,snapshotIdToDataRowsMapList,excelComparisonReport);
						snapshotIdToDataRowsMapList=new ArrayList<Map<Integer,DataRow>>();
						snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(recordNumber);
						UIUtils.setDownloadTime(snapshotInventoryGridRecord);
					}
					
					// this is a new form record
					if (recordNumber>=ExcelReportUtils.EXCEL_MAX_RECORDS_TRUNCATION) {
						allChangesExcelDataTab.setTruncated(true);
						addChangesExcelDataTab.setTruncated(true);
						updateChangesExcelDataTab.setTruncated(true);
						deleteChangesExcelDataTab.setTruncated(true);
						break;
					}
					snapshotIdToRecordMap=new HashMap<Integer,DataRow>();
					snapshotIdToDataRowsMapList.add(snapshotIdToRecordMap);
					previousCompositeKeyValue=compositeKeyValue;
					recordNumber++;
				}
				
				// new baseline record
				dataRow=ModelUtils.getDataRow(resultSet,inventoryColumnsCount,format);
				snapshotIdToRecordMap.put(snapshotId, dataRow);
			}
			snapshotInventoryGridRecord.setDownloadRemarks("Writing data to XML...");
			writeComparisonXLSXDataRowsVerticalWay(
					snapshotInventoryGridRecord.getInventory(),snapshotGridRecords,snapshotIdToDataRowsMapList,excelComparisonReport);
			snapshotIdToDataRowsMapList=new ArrayList<Map<Integer,DataRow>>();
			snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(recordNumber);
			UIUtils.setDownloadTime(snapshotInventoryGridRecord);
			endSheetsAndCloseExcelComparisonDataFiles(excelComparisonReport,inventoryName);
			snapshotInventoryGridRecord.setDownloadRemarks("");
			
			return recordNumber;
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			closeAndDeleteExcelComparisonDataFiles(excelComparisonReport,inventoryName);
			throw e;
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
		}
	}
	
	private static void writeComparisonXLSXDataRowsVerticalWay(
			Inventory inventory,List<SnapshotGridRecord> snapshotGridRecords,List<Map<Integer,DataRow>> snapshotIdToDataRowsMapList,
			ExcelComparisonReport excelComparisonReport) throws Exception {
		for (Map<Integer,DataRow> snapshotIdToDataRowMap:snapshotIdToDataRowsMapList) {
			COMPARISON_RECORD_TYPE recordType=getRecordType(snapshotGridRecords,snapshotIdToDataRowMap);
			ExcelReportTemplate allChangesExcelReportTemplate=excelComparisonReport.getAllChangesExcelReportTemplate();
			
			if (snapshotGridRecords.size()==2) {
				ExcelReportTemplate excelReportTemplate=null;
				if (recordType.equals(COMPARISON_RECORD_TYPE.ADD)) {
					excelReportTemplate=excelComparisonReport.getAddChangesExcelReportTemplate();
				}
				else
					if (recordType.equals(COMPARISON_RECORD_TYPE.UPDATE)) {
						excelReportTemplate=excelComparisonReport.getUpdateChangesExcelReportTemplate();
					}
					else
						if (recordType.equals(COMPARISON_RECORD_TYPE.DELETE)) {
							excelReportTemplate=excelComparisonReport.getDeleteChangesExcelReportTemplate();
						}
				ExcelTab excelTab=excelReportTemplate.getDataTab(inventory.getName());
				excelTab.setGeneratedRecordsCount(excelTab.getGeneratedRecordsCount()+1);
				writeComparisonXLSXDataRowsVerticalWay(excelReportTemplate,inventory,snapshotGridRecords,snapshotIdToDataRowMap);
			}
			
			ExcelTab excelTab=allChangesExcelReportTemplate.getDataTab(inventory.getName());
			excelTab.setGeneratedRecordsCount(excelTab.getGeneratedRecordsCount()+1);
			writeComparisonXLSXDataRowsVerticalWay(allChangesExcelReportTemplate,inventory,snapshotGridRecords,snapshotIdToDataRowMap);
		}
	}
	
	private static void writeComparisonXLSXDataRowsVerticalWay(ExcelReportTemplate excelReportTemplate,
			Inventory inventory,List<SnapshotGridRecord> snapshotGridRecords,Map<Integer, DataRow> snapshotIdToDataRowMap) throws IOException {

		ExcelTab excelTab=excelReportTemplate.getDataTab(inventory.getName());
		SpreadsheetWriter spreadsheetWriter=excelTab.getSpreadsheetWriter();
		int excelRowIndex=excelTab.getExcelRowIndex();
		int excelColumnIndex=0;
		Map<String, XSSFCellStyle> styles=excelReportTemplate.getStyles();

		spreadsheetWriter.insertRow(excelRowIndex++);
		spreadsheetWriter.createCell(excelColumnIndex,RecordFrame.COLUMN_HEADING_FIELDS,styles.get(ExcelReportUtils.XLSX_STYLE_WHITE_FONT_BLUE_BACKGROUND).getIndex());
		for (int index=0;index<snapshotGridRecords.size();index++) {
			SnapshotGridRecord snapshotGridRecord=snapshotGridRecords.get(index);
			String snapshotName=snapshotGridRecord.getName();
			spreadsheetWriter.createCell(excelColumnIndex+index+1,snapshotName,styles.get(ExcelReportUtils.XLSX_STYLE_WHITE_FONT_BLUE_BACKGROUND).getIndex());
		}
		spreadsheetWriter.endRow();

		List<String> dataInventoryFields=inventory.getFieldNamesUsedForDataEntry();
		for (int index=0;index<dataInventoryFields.size();index++) {
			spreadsheetWriter.insertRow(excelRowIndex++);
			String fieldName=dataInventoryFields.get(index);
			spreadsheetWriter.createCell(excelColumnIndex,fieldName,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			boolean isColorChange=false;
			int styleIndex=-1;
			if (!isColorChange) {
				if (excelRowIndex%2 == 0){
					styleIndex=styles.get(ExcelReportUtils.XLSX_STYLE_BORDERED_BACKGROUND).getIndex();
				}
				else {
					styleIndex=styles.get(ExcelReportUtils.XLSX_STYLE_ALTERNATE_GREY_BACKGROUND).getIndex();
				}
			}
			ExcelReportUtils.createExcelDataCells(spreadsheetWriter,styles,snapshotGridRecords,snapshotIdToDataRowMap,null,index,excelColumnIndex,styleIndex);
			spreadsheetWriter.endRow();
		}

		spreadsheetWriter.insertRow(excelRowIndex++);
		spreadsheetWriter.createCell(excelColumnIndex,RecordFrame.FIELD_AUDIT_NAME,styles.get(ExcelReportUtils.XLSX_STYLE_WHITE_FONT_DARK_GREY_BACKGROUND).getIndex());
		for (int index=0;index<snapshotGridRecords.size();index++) {
			spreadsheetWriter.createCell(excelColumnIndex+index+1,"",styles.get(ExcelReportUtils.XLSX_STYLE_WHITE_FONT_DARK_GREY_BACKGROUND).getIndex());
		}
		spreadsheetWriter.endRow();

		spreadsheetWriter.insertRow(excelRowIndex++);
		spreadsheetWriter.createCell(excelColumnIndex,RecordFrame.FIELD_AUDIT_LAST_UPDATED_BY,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
		ExcelReportUtils.createExcelDataCells(spreadsheetWriter,styles,snapshotGridRecords,snapshotIdToDataRowMap,RecordFrame.FIELD_AUDIT_LAST_UPDATED_BY,-1,excelColumnIndex,
				styles.get(ExcelReportUtils.XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
		spreadsheetWriter.endRow();

		spreadsheetWriter.insertRow(excelRowIndex++);
		spreadsheetWriter.createCell(excelColumnIndex,RecordFrame.FIELD_AUDIT_LAST_UPDATE_DATE,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
		ExcelReportUtils.createExcelDataCells(spreadsheetWriter,styles,snapshotGridRecords,snapshotIdToDataRowMap,RecordFrame.FIELD_AUDIT_LAST_UPDATE_DATE,-1,excelColumnIndex,
				styles.get(ExcelReportUtils.XLSX_STYLE_ALTERNATE_GREY_BACKGROUND).getIndex());
		spreadsheetWriter.endRow();

		spreadsheetWriter.insertRow(excelRowIndex++);
		spreadsheetWriter.createCell(excelColumnIndex,RecordFrame.FIELD_AUDIT_CREATED_BY,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
		ExcelReportUtils.createExcelDataCells(spreadsheetWriter,styles,snapshotGridRecords,snapshotIdToDataRowMap,RecordFrame.FIELD_AUDIT_CREATED_BY,-1,excelColumnIndex,
				styles.get(ExcelReportUtils.XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
		spreadsheetWriter.endRow();

		spreadsheetWriter.insertRow(excelRowIndex++);
		spreadsheetWriter.createCell(excelColumnIndex,RecordFrame.FIELD_AUDIT_CREATION_DATE,styles.get(ExcelReportUtils.XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
		ExcelReportUtils.createExcelDataCells(spreadsheetWriter,styles,snapshotGridRecords,snapshotIdToDataRowMap,RecordFrame.FIELD_AUDIT_CREATION_DATE,-1,excelColumnIndex,
				styles.get(ExcelReportUtils.XLSX_STYLE_ALTERNATE_GREY_BACKGROUND).getIndex());
		spreadsheetWriter.endRow();

		spreadsheetWriter.insertRow(excelRowIndex++);
		spreadsheetWriter.endRow();

		excelTab.setExcelRowIndex(excelRowIndex);
	}
	
	/*
	 * Only works for 2 snapshots
	 */
	private static COMPARISON_RECORD_TYPE getRecordType(List<SnapshotGridRecord> snapshotGridRecords,Map<Integer, DataRow> snapshotIdToDataRowMap) throws Exception {		
		SnapshotGridRecord firstSnapshotGridRecord=snapshotGridRecords.get(0);
		int firstSnapshotId=firstSnapshotGridRecord.getSnapshotId();
		DataRow firstDataRow=snapshotIdToDataRowMap.get(firstSnapshotId);
		if (firstDataRow==null) {
			return COMPARISON_RECORD_TYPE.ADD;
		}
		
		SnapshotGridRecord secondSnapshotGridRecord=snapshotGridRecords.get(1);
		int secondSnapshotId=secondSnapshotGridRecord.getSnapshotId();
		DataRow secondDataRow=snapshotIdToDataRowMap.get(secondSnapshotId);
		if (secondDataRow==null) {
			return COMPARISON_RECORD_TYPE.DELETE;
		}
		return COMPARISON_RECORD_TYPE.UPDATE;
	}
	
	private static void closeAndDeleteExcelComparisonDataFiles(ExcelComparisonReport excelComparisonReport,String inventoryName) throws IOException {
		ExcelReportUtils.closeAndDeleteExcelFile(excelComparisonReport.getAllChangesExcelReportTemplate().getDataTab(inventoryName));
		ExcelReportUtils.closeAndDeleteExcelFile(excelComparisonReport.getAddChangesExcelReportTemplate().getDataTab(inventoryName));
		ExcelReportUtils.closeAndDeleteExcelFile(excelComparisonReport.getDeleteChangesExcelReportTemplate().getDataTab(inventoryName));
		ExcelReportUtils.closeAndDeleteExcelFile(excelComparisonReport.getUpdateChangesExcelReportTemplate().getDataTab(inventoryName));
	}
	
	private static void endSheetsAndCloseExcelComparisonDataFiles(ExcelComparisonReport excelComparisonReport,String inventoryName) throws IOException {
		ExcelReportUtils.endSheetAndCloseExcelFile(excelComparisonReport.getAllChangesExcelReportTemplate().getDataTab(inventoryName));
		ExcelReportUtils.endSheetAndCloseExcelFile(excelComparisonReport.getAddChangesExcelReportTemplate().getDataTab(inventoryName));
		ExcelReportUtils.endSheetAndCloseExcelFile(excelComparisonReport.getDeleteChangesExcelReportTemplate().getDataTab(inventoryName));
		ExcelReportUtils.endSheetAndCloseExcelFile(excelComparisonReport.getUpdateChangesExcelReportTemplate().getDataTab(inventoryName));
	}
	
	/*
	 * For Comparison reports, some sheets will be blank like for ADD or DELETE so we need to remove them or
	 * this will be an issue for printing.
	 * Remember that all the sheets are generated in the PRE steps of the controller so we don't know at that time
	 * which sheets are used and which aren't yet.
	 */
	public static void regenerateExcelWorkbooks(ExcelComparisonReport excelComparisonReport) throws IOException {
		ExcelReportTemplate allChangesExcelReportTemplate=excelComparisonReport.getAllChangesExcelReportTemplate();
		regenerateExcelWorkbook(allChangesExcelReportTemplate);
		ExcelReportTemplate updateChangesExcelReportTemplate=excelComparisonReport.getUpdateChangesExcelReportTemplate();
		regenerateExcelWorkbook(updateChangesExcelReportTemplate);
		ExcelReportTemplate deleteChangesExcelReportTemplate=excelComparisonReport.getDeleteChangesExcelReportTemplate();
		regenerateExcelWorkbook(deleteChangesExcelReportTemplate);
		ExcelReportTemplate addChangesExcelReportTemplate=excelComparisonReport.getAddChangesExcelReportTemplate();
		regenerateExcelWorkbook(addChangesExcelReportTemplate);
	}
	
	private static void regenerateExcelWorkbook(ExcelReportTemplate excelReportTemplate) throws IOException {
		File file=new File(excelReportTemplate.getTemplateFile().getParentFile(),"workbook.xml");

		StringBuffer content=new StringBuffer("");
		
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ");
		content.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n");
		content.append("<workbookPr date1904=\"false\"/><bookViews><workbookView activeTab=\"0\"/></bookViews><sheets>\n");
		content.append("<sheet name=\"TOC\" r:id=\"rId3\" sheetId=\"1\"/>\n");
		
		int rId=4;
		int sheetId=2;
		int sheetNameId=1;
		List<ExcelTab> dataTabList=excelReportTemplate.getDataTabList();
		for (ExcelTab excelTab:dataTabList) {
			int generatedRecordsCount=excelTab.getGeneratedRecordsCount();
			if (generatedRecordsCount>0) {
				content.append("<sheet name=\"I").append(sheetNameId).append("\" r:id=\"rId").append(rId).append("\" sheetId=\"").append(sheetId).
				append("\"/>\n");
				sheetId++;
			}
			rId++;			
			sheetNameId++;
		}
		content.append("</sheets></workbook>");
			
		ModelUtils.writeToFile(file,content.toString(),false);
	}
	
}
