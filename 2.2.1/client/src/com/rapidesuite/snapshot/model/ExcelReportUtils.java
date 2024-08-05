package com.rapidesuite.snapshot.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.rapidesuite.client.common.util.SpreadsheetWriter;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.view.GenericRecordInformation;
import com.rapidesuite.snapshot.view.RecordFrame;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;

public class ExcelReportUtils {

	public static final int EXCEL_MAX_RECORDS_TRUNCATION=10000;
	public static final String XLSX_TOC_SHEET_NAME="TOC"; 
	public static final String XLSX_STYLE_HYPERLINK="HYPERLINK";
	public static final String XLSX_STYLE_DARKGRAY_BACKGROUND="DARKGRAY_BACKGROUND";
	public static final String XLSX_STYLE_ORANGE_BOLD_BACKGROUND="ORANGE_BOLD_BACKGROUND";
	public static final String XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND="ORANGE_BOLD_CENTERED_BACKGROUND";
	public static final String XLSX_STYLE_CENTERED_BACKGROUND="CENTERED_BACKGROUND";
	public static final String XLSX_STYLE_ALTERNATE_GREY_BACKGROUND="ALTERNATE_GREY_BACKGROUND";
	public static final String XLSX_STYLE_YELLOW_BACKGROUND="YELLOW_BACKGROUND";
	public static final String XLSX_STYLE_WHITE_FONT_DARK_GREY_BACKGROUND="WHITE_FONT_DARK_GREY_BACKGROUND";
	public static final String XLSX_STYLE_WHITE_FONT_BLUE_BACKGROUND="WHITE_FONT_BLUE_BACKGROUND";
	public static final String XLSX_STYLE_BORDERED_BACKGROUND="BORDERED_BACKGROUND";
	public static final String XLSX_STYLE_LIGHT_RED_BOLD_BACKGROUND="LIGHT_RED_BOLD_BACKGROUND";
	
	public static int writeXLSXDataRowsVerticalWay(int startingRowIndex,SpreadsheetWriter sw,Map<String, XSSFCellStyle> styles,
			Inventory inventory,String snapshotName,List<DataRow> dataRows) throws IOException {
		int rowIndex=startingRowIndex;
		int fieldNameColumnIndex=0;
		int snapshotNameColumnIndex=fieldNameColumnIndex+1;
		for (DataRow dataRow:dataRows) {
			
			sw.insertRow(rowIndex++);
			sw.createCell(fieldNameColumnIndex,RecordFrame.COLUMN_HEADING_FIELDS,styles.get(XLSX_STYLE_WHITE_FONT_BLUE_BACKGROUND).getIndex());
			sw.createCell(snapshotNameColumnIndex,snapshotName,styles.get(XLSX_STYLE_WHITE_FONT_BLUE_BACKGROUND).getIndex());
			sw.endRow();
			
			List<String> dataInventoryFields=inventory.getFieldNamesUsedForDataEntry();
			int fieldIndex=0;
			for (int dataInventoryFieldIndex=0;dataInventoryFieldIndex<dataInventoryFields.size();dataInventoryFieldIndex++) {
				sw.insertRow(rowIndex++);
				
				String fieldName=dataInventoryFields.get(dataInventoryFieldIndex);
				sw.createCell(fieldNameColumnIndex,fieldName,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
				
				String value=dataRow.getDataValues()[dataInventoryFieldIndex];
				int colIndex=fieldNameColumnIndex+1;
				if ( value == null ){
					value="";
				}
				
				if (fieldIndex%2 == 0){
					sw.createCell(colIndex,value,styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
				}
				else {
					sw.createCell(colIndex,value,styles.get(XLSX_STYLE_ALTERNATE_GREY_BACKGROUND).getIndex());
				}
				fieldIndex++;
				
				sw.endRow();
			}
			sw.insertRow(rowIndex++);
			sw.createCell(fieldNameColumnIndex,RecordFrame.FIELD_AUDIT_NAME,styles.get(XLSX_STYLE_WHITE_FONT_DARK_GREY_BACKGROUND).getIndex());
			sw.createCell(snapshotNameColumnIndex,"",styles.get(XLSX_STYLE_WHITE_FONT_DARK_GREY_BACKGROUND).getIndex());
			sw.endRow();
			
			sw.insertRow(rowIndex++);
			sw.createCell(fieldNameColumnIndex,RecordFrame.FIELD_AUDIT_LAST_UPDATED_BY,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			String value=dataRow.getRscLastUpdatedByName();
			if ( value == null ){
				value="";
			}
			sw.createCell(snapshotNameColumnIndex,value,styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
			sw.endRow();
			
			sw.insertRow(rowIndex++);
			sw.createCell(fieldNameColumnIndex,RecordFrame.FIELD_AUDIT_LAST_UPDATE_DATE,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			value=dataRow.getRscLastUpdateDate();
			if ( value == null ){
				value="";
			}
			sw.createCell(snapshotNameColumnIndex,value,styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
			sw.endRow();
			
			sw.insertRow(rowIndex++);
			sw.createCell(fieldNameColumnIndex,RecordFrame.FIELD_AUDIT_CREATED_BY,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			value=dataRow.getRscCreatedByName();
			if ( value == null ){
				value="";
			}
			sw.createCell(snapshotNameColumnIndex,value,styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
			sw.endRow();
			
			sw.insertRow(rowIndex++);
			sw.createCell(fieldNameColumnIndex,RecordFrame.FIELD_AUDIT_CREATION_DATE,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			value=dataRow.getRscCreationDate();
			if ( value == null ){
				value="";
			}
			sw.createCell(snapshotNameColumnIndex,value,styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
			sw.endRow();
			
			sw.insertRow(rowIndex++);
			sw.endRow();
		}
		return rowIndex;
	}
	
	public static int writeXLSXDataRowsHorizontalWay(int startingRowIndex,SpreadsheetWriter sw,Map<String, XSSFCellStyle> styles,
			Inventory inventory,List<DataRow> dataRows,boolean isFirstBatch) throws IOException {
		int rowIndex=startingRowIndex;
		int startingExcelColIndex=0; // first column is index 0
		int colIndex=startingExcelColIndex;
		List<String> dataInventoryFields=inventory.getFieldNamesUsedForDataEntry();
		
		if (isFirstBatch) {
			sw.insertRow(rowIndex++);
			sw.createCell(colIndex++,"Row #",styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			for (int dataInventoryFieldIndex=0;dataInventoryFieldIndex<dataInventoryFields.size();dataInventoryFieldIndex++) {
				String fieldName=dataInventoryFields.get(dataInventoryFieldIndex);
				sw.createCell(colIndex++,fieldName,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			}
			sw.createCell(colIndex++,RecordFrame.FIELD_AUDIT_LAST_UPDATED_BY,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			sw.createCell(colIndex++,RecordFrame.FIELD_AUDIT_LAST_UPDATE_DATE,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			sw.createCell(colIndex++,RecordFrame.FIELD_AUDIT_CREATED_BY,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			sw.createCell(colIndex++,RecordFrame.FIELD_AUDIT_CREATION_DATE,styles.get(XLSX_STYLE_DARKGRAY_BACKGROUND).getIndex());
			sw.endRow();
		}
		
		int rowCountIndex=rowIndex-6; // all the headers rows to remove for now hardcoded
		for (DataRow dataRow:dataRows) {
			sw.insertRow(rowIndex++);
			
			short styleIndex=-1;
			if (rowCountIndex%2 == 0){
				styleIndex=styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex();
			}
			else {
				styleIndex=styles.get(XLSX_STYLE_ALTERNATE_GREY_BACKGROUND).getIndex();
			}
			rowCountIndex++;
			
			colIndex=startingExcelColIndex;
			sw.createCell(colIndex++,""+rowCountIndex,styleIndex);
			for (int dataInventoryFieldIndex=0;dataInventoryFieldIndex<dataInventoryFields.size();dataInventoryFieldIndex++) {				
				String value=dataRow.getDataValues()[dataInventoryFieldIndex];
				if ( value == null ){
					value="";
				}
				sw.createCell(colIndex++,value,styleIndex);
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
	
	public static void createExcelTemplate(File genTempFolder,ExcelReportTemplate excelReportTemplate,
			List<SnapshotInventoryGridRecord> selectedRecordsList)throws Exception {
		genTempFolder.mkdirs();
		File templateFile=new File(genTempFolder,"template.xlsx");
		excelReportTemplate.setTemplateFile(templateFile);
		XSSFWorkbook workbook = new XSSFWorkbook();
		Map<String, XSSFCellStyle> styles = createStyles(workbook);
		excelReportTemplate.setStyles(styles);
					
		generateExcelTOC(genTempFolder,excelReportTemplate,workbook);
		generateExcelSheets(genTempFolder,excelReportTemplate,workbook,selectedRecordsList);
		
		// Use this line to debug when the excel output file shows corruption
		// usually there is some pb with the template
		//templateFile=new File("D:/MYTEST.xlsx");
				
		//save the template
		FileOutputStream os = new FileOutputStream(templateFile.getAbsolutePath());
		workbook.write(os);
		os.close();
	}
	
	public static void generateExcelTOC(File typeTempFolder,ExcelReportTemplate excelReportTemplate,XSSFWorkbook workbook)throws Exception {
		XSSFSheet sheet = workbook.createSheet(XLSX_TOC_SHEET_NAME);
		ExcelTab tocTab=new ExcelTab();
		excelReportTemplate.setTocTab(tocTab);

		createExcelWithImage(workbook.getClass(),workbook,sheet);

		File tocFile=new File(typeTempFolder,XLSX_TOC_SHEET_NAME+".xml");
		tocTab.setSheetFile(tocFile);
		String sheetRefName = sheet.getPackagePart().getPartName().getName().substring(1);
		tocTab.setSheetRefName(sheetRefName);
	}
	
	@SuppressWarnings("rawtypes")
	public static void createExcelWithImage(Class classRef) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("My Sample Excel");
		createExcelWithImage(classRef,workbook,sheet);
		
		//Write the Excel file
		FileOutputStream fileOut = null;
		fileOut = new FileOutputStream("output.xlsx");
		workbook.write(fileOut);
		fileOut.close();
	}
	
	@SuppressWarnings({ "unused", "rawtypes" })
	public static void createExcelWithImage(Class classRef,XSSFWorkbook workbook,Sheet sheet) throws Exception {		
		//FileInputStream obtains input bytes from the image file
		//InputStream inputStream = new FileInputStream("D:/RES-REPOSITORY/rapidesuite/programs/trunk/client/images/Logo_rapid.png");
		//byte[] bytes = IOUtils.toByteArray(inputStream);
		
		BufferedImage buffImage =  ImageIO.read(classRef.getResourceAsStream("/images/logo_rapid4cloud.png"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( buffImage, "png", baos );
		baos.flush();
		byte[] bytes = baos.toByteArray();
		
		//Get the contents of an InputStream as a byte[].
		
		//Adds a picture to the workbook
		int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		
		//close the input stream
		baos.close();
		//inputStream.close();
		
		//Returns an object that handles instantiating concrete classes
		CreationHelper helper = workbook.getCreationHelper();
		//Creates the top-level drawing patriarch.
		Drawing drawing = sheet.createDrawingPatriarch();

		//Create an anchor that is attached to the worksheet
		ClientAnchor anchor = helper.createClientAnchor();

		//create an anchor with upper left cell _and_ bottom right cell
		anchor.setCol1(0); //Column A
		anchor.setRow1(0); //Row 0
		anchor.setCol2(1); //Column b
		anchor.setRow2(1); //Row 1

		//Creates a picture
		Picture pict = drawing.createPicture(anchor, pictureIdx);

		//Reset the image to the original size
		//pict.resize(); //don't do that. Let the anchor resize the image!

		//Create the Cell B3
		Cell cell = sheet.createRow(0).createCell(0);

		//set width to n character widths = count characters * 256
		int widthUnits = 30*256;
		sheet.setColumnWidth(0, widthUnits);

		//set height to n points in twips = n * 20
		short heightUnits = 40*20;
		cell.getRow().setHeight(heightUnits);
	}
	
	private static void generateExcelSheets(File typeTempFolder,ExcelReportTemplate excelReportTemplate,XSSFWorkbook workbook,
			List<SnapshotInventoryGridRecord> selectedRecordsList)throws Exception {
		int sheetIndex=1;
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:selectedRecordsList) {
			ExcelTab dataExcelTab=new ExcelTab();
			
			File dataTabFile=new File(typeTempFolder,snapshotInventoryGridRecord.getInventoryName()+".xml");
			String sheetName="I"+sheetIndex;
			dataExcelTab.setSheetIndex(sheetIndex);
			
			sheetIndex++;
			XSSFSheet sheet = workbook.createSheet(sheetName);
			String sheetRefName = sheet.getPackagePart().getPartName().getName().substring(1);
			dataExcelTab.setSnapshotInventoryGridRecord(snapshotInventoryGridRecord);
			dataExcelTab.setSheetFile(dataTabFile);
			dataExcelTab.setSheetRefName(sheetRefName);
			excelReportTemplate.addDataTab(dataExcelTab);
		}
	}
	
	public static void createExcelTOCSheet(ExcelReportTemplate excelReportTemplate,boolean isHideEmptyInventory,
			String inventoryLabelOverride,
			String recordsGeneratedLabelOverride,Map<String, Set<File>> ebsInventoryNameToFusionMappingFileMap) throws Exception {
		SpreadsheetWriter sw = null;
		Writer writer = null;
		try
		{	
			Map<String, XSSFCellStyle> styles=excelReportTemplate.getStyles();
			
			ExcelTab excelTOCTab=excelReportTemplate.getTocTab();
			File sheetFile=excelTOCTab.getSheetFile();
			writer = new OutputStreamWriter(new FileOutputStream(sheetFile), com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
			sw = new SpreadsheetWriter(writer);
			// int width=30*256; // X characters : X*256
			String formatColumnsWidth="<cols>"+
					"<col min=\"1\" max=\"1\" width=\"30.0\" customWidth=\"true\"/>"+ // logo
					"<col min=\"2\" max=\"2\" width=\"10.0\" customWidth=\"true\"/>"+ // seq #
					"<col min=\"3\" max=\"3\" width=\"10.0\" customWidth=\"true\"/>"+ // index no #
					"<col min=\"4\" max=\"4\" width=\"70.0\" customWidth=\"true\"/>"+ // EBS inventory name
					"<col min=\"5\" max=\"5\" width=\"25.0\" customWidth=\"true\"/>"+ // Configured values not migrated count
					"<col min=\"6\" max=\"6\" width=\"25.0\" customWidth=\"true\"/>"; // Remarks
			if (ebsInventoryNameToFusionMappingFileMap!=null) {
				formatColumnsWidth=formatColumnsWidth+"<col min=\"7\" max=\"7\" width=\"80.0\" customWidth=\"true\"/>"; // Fusion inventory
			}
			formatColumnsWidth=formatColumnsWidth+"</cols>";
			sw.beginSheet(formatColumnsWidth);
			
			int STARTING_ROW_INDEX=2;
			int STARTING_COLUMN_INDEX=1;
			
			int rowIndex=STARTING_ROW_INDEX;
			
			// LOGO
			writer.write("<row r=\"1\" ht=\"50.0\" customHeight=\"true\"><c r=\"A1\"/></row>\n");
			
			sw.insertRow(rowIndex++);
			sw.createCell(STARTING_COLUMN_INDEX,"Seq #",styles.get(XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND).getIndex());
			sw.createCell(STARTING_COLUMN_INDEX+1,"Index No.",styles.get(XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND).getIndex());
			if (recordsGeneratedLabelOverride!=null) {
				sw.createCell(STARTING_COLUMN_INDEX+2,inventoryLabelOverride,styles.get(XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND).getIndex());
			}
			else {
				sw.createCell(STARTING_COLUMN_INDEX+2,"Inventory Name",styles.get(XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND).getIndex());
			}
			if (recordsGeneratedLabelOverride!=null) {
				sw.createCell(STARTING_COLUMN_INDEX+3,recordsGeneratedLabelOverride,styles.get(XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND).getIndex());

			}
			else {
				sw.createCell(STARTING_COLUMN_INDEX+3,"Records generated",styles.get(XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND).getIndex());
			}
			sw.createCell(STARTING_COLUMN_INDEX+4,"Remarks",styles.get(XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND).getIndex());
			if (ebsInventoryNameToFusionMappingFileMap!=null) {
				sw.createCell(STARTING_COLUMN_INDEX+5,"Fusion Inventory Name",styles.get(XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND).getIndex());
			}
			sw.endRow();
			
			int sheetInventoryRowIndex=rowIndex+1;
			int rowCount=1;
			List<ExcelTab> dataTabList=excelReportTemplate.getDataTabList();
			boolean hasAtLeastOneHyperlink=false;
			for (ExcelTab excelTab:dataTabList) {
				SnapshotInventoryGridRecord snapshotInventoryGridRecord=excelTab.getSnapshotInventoryGridRecord();
				int generatedRecordsCount=excelTab.getGeneratedRecordsCount();
				
				if (isHideEmptyInventory && generatedRecordsCount==0) {
					continue;
				}
				
				sw.insertRow(rowIndex++);
				sw.createCell(STARTING_COLUMN_INDEX,""+(rowCount++),styles.get(XLSX_STYLE_CENTERED_BACKGROUND).getIndex());
				int excelTabIndexStr = 0;
				excelTabIndexStr = excelTab.getSheetIndex();
				sw.createCell(STARTING_COLUMN_INDEX+1,"I"+excelTabIndexStr,styles.get(XLSX_STYLE_CENTERED_BACKGROUND).getIndex());
				if (generatedRecordsCount>0) {
					sw.createCell(STARTING_COLUMN_INDEX+2,snapshotInventoryGridRecord.getInventoryName(),styles.get(XLSX_STYLE_HYPERLINK).getIndex());
					hasAtLeastOneHyperlink=true;
				}
				else {
					sw.createCell(STARTING_COLUMN_INDEX+2,snapshotInventoryGridRecord.getInventoryName(),styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
				}
				sw.createCell(STARTING_COLUMN_INDEX+3,""+generatedRecordsCount,styles.get(XLSX_STYLE_CENTERED_BACKGROUND).getIndex());
				if (excelTab.isTruncated()) {
					sw.createCell(STARTING_COLUMN_INDEX+4,"Truncated! (Max records reached)",styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
				}
				else {
					sw.createCell(STARTING_COLUMN_INDEX+4,"",styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
				}
				if (ebsInventoryNameToFusionMappingFileMap!=null) {
					Set<File> fusionSet=ebsInventoryNameToFusionMappingFileMap.get(snapshotInventoryGridRecord.getInventoryName());
					String fusionSetStr="";
					if (fusionSet!=null) {
						Iterator<File> iterator=fusionSet.iterator();
						StringBuffer listBuffer=new StringBuffer("");
						while (iterator.hasNext()) {
							File file=iterator.next();
							String name=file.getName().replace(".xml","");
							listBuffer.append(name);
							if (iterator.hasNext()) {
								listBuffer.append("\n");
							}
						}
						fusionSetStr=listBuffer.toString();
					}
					sw.createCell(STARTING_COLUMN_INDEX+5,fusionSetStr,	styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
				}
				sw.endRow();
			}
			sw.endTabSheet();
			
			if (hasAtLeastOneHyperlink) {
				int dataTabIndex=1;
				int tocRowIndex=sheetInventoryRowIndex;
				writer.write("<hyperlinks>\n");
				for (ExcelTab excelTab:dataTabList) {
					int generatedRecordsCount=excelTab.getGeneratedRecordsCount();
					if (generatedRecordsCount>0) {
						String sheetName="I"+dataTabIndex;
						String refCell="D"+tocRowIndex;
						tocRowIndex++;
						writer.write("<hyperlink location=\"'"+sheetName+"'!A1\" ref=\""+refCell+"\"/>\n");
					}
					dataTabIndex++;
				}
				writer.write("</hyperlinks>\n");
			}
			
			// RAPID LOGO
			writer.write("<drawing r:id=\"rId1\"/>\n");
			
			sw.endWorkSheet();
		}
		finally
		{
			if (writer!=null) {
				writer.close();
			}
		}
	}
	
	public static void populateExcelDataSheets(ExcelReportTemplate excelReportTemplate,File reportFile,JLabel statusLabel,
			boolean isReplaceWorkbook,boolean isSkipZeroRecordsGeneratedEntries) throws Exception {
		FileOutputStream out=null;
		try{			
			out = new FileOutputStream(reportFile);
			substituteXMLFile(excelReportTemplate, out,statusLabel,isReplaceWorkbook,isSkipZeroRecordsGeneratedEntries);
		}
		finally
		{
			if (out!=null) {
				out.close();
			}
		}
	}
	
	private static void substituteXMLFile(ExcelReportTemplate excelReportTemplate, OutputStream out, JLabel statusLabel,boolean isReplaceWorkbook,
			boolean isSkipZeroRecordsGeneratedEntries) throws IOException {
		ZipFile templateFile = new ZipFile(excelReportTemplate.getTemplateFile());

		Set<String> excelInUseSheetSet = new HashSet<String>();
		List<ExcelTab> dataTabList=excelReportTemplate.getDataTabList();
		for (ExcelTab excelTab:dataTabList) {
			// we don't replace the sheet if there was an error because the xlxs will become corrupted
			// if it is missing a sheetX.xml
			if (!excelTab.isError()) {
				excelInUseSheetSet.add(excelTab.getSheetRefName());
			}
		}
		excelInUseSheetSet.add(excelReportTemplate.getTocTab().getSheetRefName());
		String WORKBOOK_ENTRY="xl/workbook.xml";
		if (isReplaceWorkbook) {
			excelInUseSheetSet.add(WORKBOOK_ENTRY);
		}
		
		ZipOutputStream zos = new ZipOutputStream(out);

		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) templateFile.entries();
		while (en.hasMoreElements()) {
			ZipEntry ze = en.nextElement();
			if( ! excelInUseSheetSet.contains(ze.getName())){
				zos.putNextEntry(new ZipEntry(ze.getName()));
				InputStream is = templateFile.getInputStream(ze);
				copyStream(is, zos);
				is.close();
			}
		}
		templateFile.close();
		
		InputStream is=null;
		zos.putNextEntry(new ZipEntry(excelReportTemplate.getTocTab().getSheetRefName()));
		is = new FileInputStream(excelReportTemplate.getTocTab().getSheetFile());
		copyStream(is, zos);
		is.close();
		
		if (isReplaceWorkbook) {
			File file=new File(excelReportTemplate.getTemplateFile().getParentFile(),"workbook.xml");
			zos.putNextEntry(new ZipEntry(WORKBOOK_ENTRY));
			is = new FileInputStream(file);
			copyStream(is, zos);
			is.close();
		}
		
		int index=0;
		for (ExcelTab excelTab:dataTabList) {		
			String entry=excelTab.getSheetRefName();
			File xmlFile=excelTab.getSheetFile();
			if (index % 10 == 0) {
				statusLabel.setText("Please wait, generating Excel archive... ("+(index+1)+"/"+dataTabList.size()+" files)");
			}
			index++;
			
			if (!xmlFile.exists()) {
				continue;
			}
			if (isSkipZeroRecordsGeneratedEntries && excelTab.getGeneratedRecordsCount()==0) {
				continue;
			}
			zos.putNextEntry(new ZipEntry(entry));
			is = new FileInputStream(xmlFile);
			copyStream(is, zos);
			is.close();
		}
		zos.close();
	}
	
	private static void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] chunk = new byte[1024];
		int count;
		while ((count = in.read(chunk)) >=0 ) {
			out.write(chunk,0,count);
		}
	}
	
	private static Map<String, XSSFCellStyle> createStyles(XSSFWorkbook wb){
		Map<String, XSSFCellStyle> styles = new HashMap<String, XSSFCellStyle>();

        XSSFCellStyle style=createHyperLinkStyle(wb);
        styles.put(XLSX_STYLE_HYPERLINK, style);
        
        style=createDarkGrayStyle(wb);
        styles.put(XLSX_STYLE_DARKGRAY_BACKGROUND, style);
        
        style=createOrangeBoldStyle(wb);
        styles.put(XLSX_STYLE_ORANGE_BOLD_BACKGROUND, style);
        
        style=createOrangeBoldCenteredStyle(wb);
        styles.put(XLSX_STYLE_ORANGE_BOLD_CENTERED_BACKGROUND, style);
        
        style=createAlternateGreyStyle(wb);
        styles.put(XLSX_STYLE_ALTERNATE_GREY_BACKGROUND, style);
        
        style=createYellowStyle(wb);
        styles.put(XLSX_STYLE_YELLOW_BACKGROUND, style);
        
        style=createWhiteFontDarkGreyStyle(wb);
        styles.put(XLSX_STYLE_WHITE_FONT_DARK_GREY_BACKGROUND, style);
        
        style=createWhiteFontBlueStyle(wb);
        styles.put(XLSX_STYLE_WHITE_FONT_BLUE_BACKGROUND, style);
        
        style=createCenteredStyle(wb);
        styles.put(XLSX_STYLE_CENTERED_BACKGROUND, style);
        
        style=createBorderedStyle(wb);
        styles.put(XLSX_STYLE_BORDERED_BACKGROUND, style);
        
        style=createLightRedBoldStyle(wb);
        styles.put(XLSX_STYLE_LIGHT_RED_BOLD_BACKGROUND, style);
        
		return styles;
	}
	
	private static XSSFCellStyle createLightRedBoldStyle(XSSFWorkbook wb) {
		XSSFCellStyle style = wb.createCellStyle();
		XSSFFont headerFont = wb.createFont();
		headerFont.setBold(true);
		XSSFColor color =new XSSFColor(new Color(255,213,213));
		style.setFillForegroundColor(color);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFont(headerFont);
		addBorders(style);
		return style;
	}

	private static XSSFCellStyle createHyperLinkStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        addBorders(style);
        return style;
	}
	
	private static XSSFCellStyle createDarkGrayStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
		XSSFColor color =new XSSFColor(Color.LIGHT_GRAY);
		style.setFillForegroundColor(color);
		style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		addBorders(style);
        return style;
	}
	
	private static XSSFCellStyle createOrangeBoldStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
		XSSFFont headerFont = wb.createFont();
		headerFont.setBold(true);
		XSSFColor	color =new XSSFColor(Color.decode("#FABF8F"));
		style.setFillForegroundColor(color);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFont(headerFont);
		addBorders(style);
		return style;
	}
	
	private static XSSFCellStyle createOrangeBoldCenteredStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
		XSSFFont headerFont = wb.createFont();
		headerFont.setBold(true);
		XSSFColor	color =new XSSFColor(Color.decode("#FABF8F"));
		style.setFillForegroundColor(color);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFont(headerFont);
		style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		addBorders(style);
		return style;
	}
	
	private static XSSFCellStyle createAlternateGreyStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
		XSSFColor	color =new XSSFColor(Color.decode("#dbdbdb"));
		style.setFillForegroundColor(color);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		addBorders(style);
		return style;
	}
	
	private static XSSFCellStyle createYellowStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
		XSSFColor	color =new XSSFColor(Color.decode("#fbf468"));
		style.setFillForegroundColor(color);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		addBorders(style);
		return style;
	}
	
	private static XSSFCellStyle createWhiteFontDarkGreyStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
		XSSFColor	color =new XSSFColor(Color.decode("#4a4f4e"));
		style.setFillForegroundColor(color);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		XSSFFont headerFont = wb.createFont();
		headerFont.setColor(HSSFColor.WHITE.index);
		style.setFont(headerFont);
		addBorders(style);
		return style;
	}
    
	private static XSSFCellStyle createWhiteFontBlueStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
		XSSFColor	color =new XSSFColor(Color.decode("#047FC0"));
		style.setFillForegroundColor(color);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		XSSFFont headerFont = wb.createFont();
		headerFont.setColor(HSSFColor.WHITE.index);
		style.setFont(headerFont);
		addBorders(style);
		return style;
	}
	
	private static XSSFCellStyle createCenteredStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		addBorders(style);
		return style;
	}
	
	private static XSSFCellStyle createBorderedStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
		addBorders(style);
		return style;
	}	
  	
	private static void addBorders(XSSFCellStyle style){
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
	}
	
	public static void createExcelDataCells(SpreadsheetWriter sw,Map<String, XSSFCellStyle> styles,
			List<SnapshotGridRecord> snapshotGridRecords,Map<Integer, DataRow> snapshotIdToDataRowMap,String fieldName,int fieldIndex,
			int startingExcelColumnIndex,int defaultStyleIndex) throws IOException {
		String previousCellValue=null;
		int excelColumnIndex=startingExcelColumnIndex+1;
		for (SnapshotGridRecord snapshotGridRecord:snapshotGridRecords) {
			int snapshotId=snapshotGridRecord.getSnapshotId();
			DataRow dataRow=snapshotIdToDataRowMap.get(snapshotId);
			String value=getValue(fieldName,fieldIndex,dataRow);
			
			int styleIndex=defaultStyleIndex;
			if (previousCellValue!=null) {
				if (!value.equals(previousCellValue) ) {
					styleIndex=styles.get(XLSX_STYLE_YELLOW_BACKGROUND).getIndex();
				}
			}
			sw.createCell(excelColumnIndex++,value,styleIndex);
			previousCellValue=value;
			if (previousCellValue==null) {
				previousCellValue="";
			}
		}
	}
	
	private static String getValue(String columnName,int dataInventoryFieldIndex,DataRow dataRow) {
		String value=null;
		if (dataRow==null) {
			// This means that the record was either added or PK updated.
			value="";
		}
		else {
			if (columnName==null) {
				value=dataRow.getDataValues()[dataInventoryFieldIndex];
			}
			else
				if (columnName.equalsIgnoreCase(RecordFrame.FIELD_AUDIT_LAST_UPDATED_BY)) {
					value=dataRow.getRscLastUpdatedByName();
				}
				else
					if (columnName.equalsIgnoreCase(RecordFrame.FIELD_AUDIT_LAST_UPDATE_DATE)) {
						value=dataRow.getRscLastUpdateDate();
					}
					else
						if (columnName.equalsIgnoreCase(RecordFrame.FIELD_AUDIT_CREATED_BY)) {
							value=dataRow.getRscCreatedByName();
						}
						else
							if (columnName.equalsIgnoreCase(RecordFrame.FIELD_AUDIT_CREATION_DATE)) {
								value=dataRow.getRscCreationDate();
							}
			if (value==null) {
				value=""; 
			}
		}
		return value;
	}
	
	public static void closeAndDeleteExcelFile(ExcelTab excelTab) throws IOException {
		if (excelTab.getSpreadsheetWriter().getWriter()!=null) {
			excelTab.getSpreadsheetWriter().getWriter().close();
		}
		excelTab.getSheetFile().delete();
	}
	
	public static void endSheetAndCloseExcelFile(ExcelTab excelTab) throws IOException {
		excelTab.getSpreadsheetWriter().endSheet();
		if (excelTab.getSpreadsheetWriter().getWriter()!=null) {
			excelTab.getSpreadsheetWriter().getWriter().close();
		}
	}
	
	public static void initExcelTab(ExcelTab excelTab,
			File folder,int inventoryColumnsCount,boolean isVerticalWay,
			Map<String, XSSFCellStyle> styles,GenericRecordInformation genericRecordInformation) throws IOException {
		String sheetFileName=genericRecordInformation.getInventoryName()+".xml";
		File sheetFile=new File(folder,sheetFileName);
		Writer	writer = new OutputStreamWriter(new FileOutputStream(sheetFile), com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
		SpreadsheetWriter spreadsheetWriter = new SpreadsheetWriter(writer);
		String formatColumnsWidth=getXLSXFormat(inventoryColumnsCount,isVerticalWay);
		spreadsheetWriter.beginSheet(formatColumnsWidth);
		int excelRowIndex=addExcelTabInventoryHeader(0,spreadsheetWriter,styles,genericRecordInformation);
		excelTab.setExcelRowIndex(excelRowIndex);
		excelTab.setSpreadsheetWriter(spreadsheetWriter);
		excelTab.setSheetFile(sheetFile);		
	}
	
	private static String getXLSXFormat(int inventoryColumnsCount,boolean isVerticalWay) {
		// int width=30*256; // X characters : X*256
		String formatColumnsWidth=null;
		if (isVerticalWay) {
			formatColumnsWidth="<cols>"+
					"<col min=\"1\" max=\"1\" width=\"30.0\" customWidth=\"true\"/>"+
					"<col min=\"2\" max=\"2\" width=\"30.0\" customWidth=\"true\"/>"+
					"</cols>";
		}
		else {
			formatColumnsWidth="<cols>"+
					"<col min=\"1\" max=\"1\" width=\"25.0\" customWidth=\"true\"/>";
			for (int i=0;i<inventoryColumnsCount+5;i++ ) {
				int excelColIndex=i+2;
				formatColumnsWidth=formatColumnsWidth+"<col min=\""+excelColIndex+"\" max=\""+excelColIndex+"\" width=\"20.0\" customWidth=\"true\"/>";
			}
			formatColumnsWidth=formatColumnsWidth+"</cols>";
		}
		return formatColumnsWidth;
	}
	
	public static int addExcelTabInventoryHeader(int startingExcelRowIndex,SpreadsheetWriter sw,Map<String, XSSFCellStyle> styles,GenericRecordInformation genericRecordInformation) throws IOException {
		int excelRowIndex=startingExcelRowIndex;
		Inventory inventory=genericRecordInformation.getInventory();
		
		sw.insertRow(excelRowIndex++);
		sw.createCell(0,"Inventory:",styles.get(XLSX_STYLE_ORANGE_BOLD_BACKGROUND).getIndex());
		sw.createCell(1,inventory.getName(),styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
		sw.endRow();
		
		sw.insertRow(excelRowIndex++);
		sw.createCell(0,"Form name:",styles.get(XLSX_STYLE_ORANGE_BOLD_BACKGROUND).getIndex());
		sw.createCell(1,genericRecordInformation.getFormInformation().getFormName(),styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
		sw.endRow();
		
		sw.insertRow(excelRowIndex++);
		sw.createCell(0,"Application names:",styles.get(XLSX_STYLE_ORANGE_BOLD_BACKGROUND).getIndex());
		sw.createCell(1,genericRecordInformation.getFormInformation().getFormattedApplicationNames(),styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
		sw.endRow();
		
		sw.insertRow(excelRowIndex++);
		sw.createCell(0,"Form paths:",styles.get(XLSX_STYLE_ORANGE_BOLD_BACKGROUND).getIndex());
		sw.createCell(1,genericRecordInformation.getFormInformation().getFormattedFormPaths(),styles.get(XLSX_STYLE_BORDERED_BACKGROUND).getIndex());
		sw.endRow();
		
		sw.insertRow(excelRowIndex++);
		sw.endRow();
		
		return excelRowIndex;
	}	
	
}
