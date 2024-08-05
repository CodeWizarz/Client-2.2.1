package com.rapidesuite.split.xlsx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.monitorjbl.xlsx.StreamingReader;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.DataFactory;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.snapshot.model.DataRow;

public class SplitXLSXMain {

	// XML 1.0
	// #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
	public static String xml10pattern = "[^"
	                    + "\u0009\r\n"
	                    + "\u0020-\uD7FF"
	                    + "\uE000-\uFFFD"
	                    + "\ud800\udc00-\udbff\udfff"
	                    + "]";
	
	// XML 1.1
	// [#x1-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
	public static String xml11pattern = "[^"
	                    + "\u0001-\uD7FF"
	                    + "\uE000-\uFFFD"
	                    + "\ud800\udc00-\udbff\udfff"
	                    + "]+";
	
	static long startTime = 0;
	static String startTimeDateText = null; 
	
	public static String output_main_folder_name = "split_output";
//	public static String folder_seperator = "\\";
	
	public static String output_folder_name = null;

	public static void main(String[] args) {
		
		startTime = System.currentTimeMillis();
        startTimeDateText = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss").format(new Date(startTime));
                
        File inputFolder = new File(args[0]);
        if (!inputFolder.exists()) {
			System.err.println("Input Folder doesn't exists. " + args[0]);
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			System.exit(0);        	
        }
		File[] xlsxInputFiles = inputFolder.listFiles((new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xlsx");
			}
		}));

		String splitCountStr = args[1];
		if(splitCountStr == null || splitCountStr.isEmpty()) {
			System.err.println(
					"Please define a split-count number in parameter number 2.");
			System.exit(0);
		}
		int splitCount = 0;
		try {
			splitCount = Integer.parseInt(splitCountStr);
		} catch (Exception ex) {
			System.err.println(
					"Please define a split-count number in parameter number 2.");
			System.exit(0);
		}
		
		String labelPrefix = args[2];
		if(labelPrefix == null || labelPrefix.isEmpty()) {
			System.err.println(
					"Please define a labelPrefix in parameter number 3.");
			System.exit(0);
		}
		
		for (File file : xlsxInputFiles) {
			System.out.println("splitting " + file.getName());
			try {
				splitFile(file, splitCount, labelPrefix);
			} catch (Exception ex) {
				System.err.println("Failed to split " + file.getName());
				ex.printStackTrace();
			}
		}
		
		System.out.println((System.currentTimeMillis() - startTime) + " ms...");
		System.out.println("split completed, fetch your zipped files from " + output_main_folder_name);
	}

	@SuppressWarnings("deprecation")
	public static void splitFile(File file, int splitCount, String labelPrefix) throws Exception {

		String fileName = file.getName();
		String fileNameWOExt = fileName.substring(0, fileName.lastIndexOf("."));
		String filenameExt = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

		if (!filenameExt.equalsIgnoreCase("xlsx")) {
			String message ="This library will ONLY work with XLSX files. The older XLS format is not capable of being streamed."; 
			System.err.println(message);
			throw new Exception(message);
		}

		try {
			InputStream is = new FileInputStream(file);
			Workbook workbook = StreamingReader.builder().rowCacheSize(100)
					.bufferSize(4096)
					.sheetIndex(0)
					.open(is);
			
//			Sheet sheet = workbook.getSheet("Sheet0");
			int labelCount = 1;
			
			int sheetCount = workbook.getNumberOfSheets();
			for(int i = 0; i<sheetCount; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				if(sheet.getSheetName().equals(CoreConstants.NAVIGATION_MAPPERS)) {
					System.out.println("Skipping Sheet " + CoreConstants.NAVIGATION_MAPPERS);
					continue;
				}
				System.out.println("Processing Sheet " + sheet.getSheetName());
				
				List<String> fields = new ArrayList<String>();
				List<DataRow> dataRows = new ArrayList<DataRow>();

				int outputRowCollectionCount = 0;

				int cellCount = 0;
				for (Row r : sheet) {
					if(r.getRowNum() == 0) {
						cellCount = r.getLastCellNum()-5;
						for (int j=0;j<cellCount;j++) {
							Cell c = r.getCell(j);
						//for (Cell c : r) {
							fields.add(c.getStringCellValue());
						}
						continue;
					}
					List<String> values = new ArrayList<String>();
					for (int j=0;j<cellCount;j++) {
						Cell c = r.getCell(j);
						String val = "";
						// catch the wrong datatype that are not supported by CONFIGURATOR.
						if (c != null && c.getCellType() != 0 && c.getCellType() != 1 && c.getCellType() != 3) {
							System.out.println("ERROR:: Wrong data FORMAT at Row: " + (c.getRowIndex()+1) + " - Column: " + (c.getColumnIndex()+1) + ". This data is useless in configurator. Continuing to parse the complete file to find other errors. Fix the errors and split again.");
						}
						try {
						val = c.getStringCellValue();
						// escape the data value
						val = StringEscapeUtils.escapeXml(val);

						// catch invalid UNICODE Characters.
		                String replacedXML10Data = val.replaceAll(xml10pattern , "");
		                String replacedXML11Data = val.replaceAll(xml11pattern , "");
		                if(!replacedXML10Data.equals(val) || !replacedXML11Data.equals(val)) {
		                	System.out.println("ERROR:: Wrong data CHARACTER at Row: " + (c.getRowIndex()+1) + " - Column: " + (c.getColumnIndex()+1) + ". This data can not be imported in configurator. Continuing to parse the complete file to find other errors. Fix the errors and split again.");
		                	System.out.println("Original value: '" + val + "'" + "replacedXML10Data value: '" + replacedXML10Data + "'" + "replacedXML11Data value: '" + replacedXML11Data + "'");
		                }
						} catch (Exception ex) {
							// do nothing
						}
						values.add(val);
					}
					DataRow dataRow = new DataRow();
					dataRow.setDataValues(values.toArray(new String[0]));
					dataRow.setLabel(labelPrefix + labelCount);
					dataRows.add(dataRow);
					
					outputRowCollectionCount++;
					
					// write the batch to file.
					if((outputRowCollectionCount % splitCount) == 0) {
						finalDumpToFile(filenameExt, fileNameWOExt, fields, labelCount, dataRows, labelPrefix + labelCount);
						dataRows = new ArrayList<DataRow>();
						labelCount++;
					}
				}
				
				// write the left overs as a batch to file.
				if(dataRows != null && dataRows.size() > 0) {
					finalDumpToFile(filenameExt, fileNameWOExt, fields, labelCount, dataRows, labelPrefix + labelCount);
					dataRows = new ArrayList<DataRow>();
					labelCount++;				
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			FileUtils.zipFolder(new File(output_folder_name), new File(output_folder_name+".zip"));
		} catch (Exception e) {
			System.err.println("Error while zipping the file");
			throw e;
		}	
	}
	
	public static void finalDumpToFile(String filenameExt, String fileNameWOExt, List<String> fields, int fileNumber, List<DataRow> dataRows, String labelPrefix) throws Exception {
		File mainOutputFolder = new File(output_main_folder_name);
		if (!mainOutputFolder.exists()) {
			mainOutputFolder.mkdirs();
		}
		output_folder_name = fileNameWOExt + "-" + startTimeDateText;
		File InventoryFolder = new File(mainOutputFolder, fileNameWOExt + "-" + startTimeDateText);
		if (!InventoryFolder.exists()) {
			InventoryFolder.mkdirs();
		}
		
		String outputFileName =fileNameWOExt +  "." + fileNumber + ".xml";
		File outputFile = new File(InventoryFolder, outputFileName);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile,false), "UTF8"),32768);
		writeFileHeader(out, filenameExt, fields, true);
		writeDataRowsConversion(out,dataRows,labelPrefix);
		writeDataFileFooter(out);
		
		IOUtils.closeQuietly(out);
		
		System.out.println(outputFileName + " created.");
	}
	
	public static void writeFileHeader(
			BufferedWriter out,
			String tableName,
			List<String> inventoryFields,
			boolean hasAuditColumns) throws IOException
	{
		StringBuffer str=new StringBuffer("");
		str.append("<?xml version=\"1.0\" encoding=\"").append(com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING).append("\"?>\n");
		str.append("<data name=\"").append(tableName).append("\" xmlns=\"http://data0000.configurator.erapidsuite.com\">\n");
		str.append("<m/>\n");
		str.append("<h>\n");
		for ( String field : inventoryFields )
		{
			str.append("<c>").append(StringEscapeUtils.escapeXml10(field)).append("</c>\n");
		}
		if(!inventoryFields.contains("RSC Data Label"))
			str.append("<c>").append(StringEscapeUtils.escapeXml10("RSC Data Label")).append("</c>\n");
		if(!inventoryFields.contains("Navigation Filter"))
			str.append("<c>").append(StringEscapeUtils.escapeXml10("Navigation Filter")).append("</c>\n");
		if (hasAuditColumns) {
			if(!inventoryFields.contains("RSC last updated by name")) {
				str.append("<c>").append(StringEscapeUtils.escapeXml10("RSC last updated by name")).append("</c>\n");
			}
			if(!inventoryFields.contains("RSC last update date")) {
				str.append("<c>").append(StringEscapeUtils.escapeXml10("RSC last update date")).append("</c>\n");
			}
		}
		str.append("</h>\n");
		out.write(str.toString());
	}

	
	public static void writeDataRowsConversion(BufferedWriter out,List<DataRow> dataRows,String label) throws IOException	{
		for ( DataRow dataRow:dataRows ){
			writeDataRowConversion(out,dataRow,label);
		}
	}
	
	private static void writeDataRowConversion(BufferedWriter out,DataRow dataRow,String label) throws IOException {
		StringBuffer str=new StringBuffer("");
		str.append("<r>\n");
		for ( String value : dataRow.getDataValues() ) {
			if ( value == null || value.isEmpty()){
				str.append("<c/>\n");
			}
			else{
				// Performance optimization: escapeXml10 is very expensive (Jprofiler)
				// those are already done because the data is already written to xml from DB before conversion
				// str.append("<c>").append(StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(value))).append("</c>\n");
				str.append("<c>").append(value).append("</c>\n");
			}
		}
		String labelDataRow=dataRow.getLabel();
		if (labelDataRow!=null && !labelDataRow.isEmpty()) {
			str.append("<c>").append(labelDataRow).append("</c>\n");
		}
		else {
			str.append("<c>").append(label).append("</c>\n");
		}
		str.append("<c/>\n");
		String userName=dataRow.getRscLastUpdatedByName();
		str.append("<c>").append(userName).append("</c>\n");
		str.append("<c>").append(dataRow.getRscLastUpdateDate()).append("</c>\n");
		str.append("</r>\n");
		
		out.write(str.toString());
	}
	
	public static void writeDataFileFooter(BufferedWriter out) throws Exception
	{
		StringBuffer str=new StringBuffer("");
		str.append(DataFactory.DATA_FILE_FOOTER).append("\n");
		out.write(str.toString());
	}

}
