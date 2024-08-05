package com.rapidesuite.snapshot.view.convert;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.dataConversion0000.ColumnType;
import com.rapidesuite.client.dataConversion0000.ColumnsType;
import com.rapidesuite.client.dataConversion0000.DataConversionDocument;
import com.rapidesuite.client.dataConversion0000.DataConversionType;
import com.rapidesuite.client.dataConversion0000.FromLeftSourceType;
import com.rapidesuite.client.dataConversion0000.FromRightSourceType;
import com.rapidesuite.client.dataConversion0000.FromType;
import com.rapidesuite.client.dataConversion0000.IfThenElseType;
import com.rapidesuite.client.dataConversion0000.LoopRecordsType;
import com.rapidesuite.client.dataConversion0000.MapType;
import com.rapidesuite.client.dataConversion0000.MappingsType;
import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.client.dataConversion0000.OperationsType;
import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.client.dataConversion0000.TargetType;
import com.rapidesuite.client.dataConversion0000.ViewType;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.model.ModelUtils;

public class MappingAnalyzer {
	
	private List<ConvertSourceGridRecordInformation> convertSourceGridRecordInformationList;
	private Map<String, File> inventoryNameToFileMap;
	private Map<String, File> sourceInventoryNameToMappingFileMap;
	private MappingAnalyzerWorker mappingAnalyzerWorker;
	private ArrayList<MappingAnalyzerResult> mappingAnalyzerResultList;
	private Map<String, Set<String>> sourceInventoryNameToTargetInventoryNamesMap;
	
	public MappingAnalyzer(MappingAnalyzerWorker mappingAnalyzerWorker,
			List<ConvertSourceGridRecordInformation> convertSourceGridRecordInformationList,
			Map<String, File> inventoryNameToFileMap, Map<String, File> sourceInventoryNameToMappingFileMap) {
		this.mappingAnalyzerWorker=mappingAnalyzerWorker;
		this.convertSourceGridRecordInformationList=convertSourceGridRecordInformationList;
		this.inventoryNameToFileMap=inventoryNameToFileMap;
		this.sourceInventoryNameToMappingFileMap=sourceInventoryNameToMappingFileMap;
		sourceInventoryNameToTargetInventoryNamesMap=new HashMap<String, Set<String>>();
	}
	
	public void process(boolean isSaveToExcel) throws Exception
	{
		// used for filtering unwanted inventories
		List<ConvertSourceGridRecordInformation> sourceList=new ArrayList<ConvertSourceGridRecordInformation>();
		for (ConvertSourceGridRecordInformation convertSourceGridRecordInformation:convertSourceGridRecordInformationList) {
			sourceList.add(convertSourceGridRecordInformation);
		}
		
		int totalSteps=convertSourceGridRecordInformationList.size()*2+1; // saving to excel is one step;
		int currentStep=0;
		mappingAnalyzerWorker.setTotalSteps(totalSteps);
		
		Map<String,Set<String>> inventoryNameToColumnNamesMap=new HashMap<String,Set<String>>();
		Map<String,Set<String>> inventoryNameToTargetNamesMap=new HashMap<String,Set<String>>();
		for (ConvertSourceGridRecordInformation convertSourceGridRecordInformation:sourceList) {
			String inventoryName=convertSourceGridRecordInformation.getInventoryName();
			File mappingFile=sourceInventoryNameToMappingFileMap.get(inventoryName);
			currentStep++;
			mappingAnalyzerWorker.updateStep(currentStep);
			String executionMessage="<html>Processing mapping files... Operations: "+
					Utils.formatNumberWithComma(currentStep)+" / "+Utils.formatNumberWithComma(totalSteps);
			mappingAnalyzerWorker.updateExecutionLabels(executionMessage);
			//FileUtils.println("### inventoryName:"+inventoryName+" mappingFile:"+mappingFile);
			if (mappingFile==null) {
				continue;
			}
			
			DataConversionDocument dataConversionDocument=ModelUtils.getDataConversionDocument(mappingFile);
			DataConversionType dataConversionType=dataConversionDocument.getDataConversion();
			
			SourceType[] sourceTypeArray=dataConversionType.getSourceArray();
			Map<String,String> sourceCodeToInventoryNameMap=new HashMap<String,String>();
			Set<String> sourceCodeViewTypeSet=new HashSet<String>();
			Set<String> inventoryNameSet=new HashSet<String>();
			for (SourceType sourceType:sourceTypeArray) {
				com.rapidesuite.client.dataConversion0000.SourceType.Type.Enum sourceTypeAttribute=sourceType.getType();
				String sourceCode=sourceType.getCode();
				//FileUtils.println("sourceCode:"+sourceCode);

				if (sourceTypeAttribute.equals(SourceType.Type.INVENTORY)) {
					String sourceName=sourceType.getName();
					sourceCodeToInventoryNameMap.put(sourceCode, sourceName);
					
					inventoryNameSet.add(sourceName);
				}
				else 
				if (sourceTypeAttribute.equals(SourceType.Type.VIEW)) {
					sourceCodeViewTypeSet.add(sourceCode);
				}
			}
			
			for (SourceType sourceType:sourceTypeArray) {
				com.rapidesuite.client.dataConversion0000.SourceType.Type.Enum sourceTypeAttribute=sourceType.getType();
				//FileUtils.println("sourceTypeAttribute:"+sourceTypeAttribute);
				if (sourceTypeAttribute.equals(SourceType.Type.VIEW)) {
					ViewType viewType=sourceType.getView();
					FromType fromType=viewType.getFrom();
					FromLeftSourceType fromLeftSourceType=fromType.getFromLeftSource();
					String leftSourceCode=fromLeftSourceType.getCode();
					FromRightSourceType fromRightSourceType=fromType.getFromRightSource();
					String rightSourceCode=fromRightSourceType.getCode();
					
					setColumnNamesFromView(viewType,true,leftSourceCode,sourceCodeToInventoryNameMap,inventoryNameToColumnNamesMap);
					setColumnNamesFromView(viewType,false,rightSourceCode,sourceCodeToInventoryNameMap,inventoryNameToColumnNamesMap);					
				}
			}			
			LoopRecordsType[] loopRecordsTypeArray=dataConversionType.getLoopRecordsArray();
			
			Set<String> allTargetNames=getAllTargetNames(loopRecordsTypeArray);
			for (String inventoryNameTemp:inventoryNameSet) {
				Set<String> targetNames=inventoryNameToTargetNamesMap.get(inventoryNameTemp);
				if (targetNames==null) {
					targetNames=new HashSet<String>();
					inventoryNameToTargetNamesMap.put(inventoryNameTemp,targetNames);
				}
				targetNames.addAll(allTargetNames);
			}
			for (LoopRecordsType loopRecordsType:loopRecordsTypeArray) {
				String sourceCode=loopRecordsType.getSourceCode();
				String sourceInventoryName=sourceCodeToInventoryNameMap.get(sourceCode);
				//FileUtils.println("LoopRecordsType, sourceCode: '"+sourceCode+"' sourceInventoryName: '"+sourceInventoryName+"'");
				if (sourceInventoryName==null) {
					// this is a view so we assume all the columns from the view will be used
					continue;
				}
				
				//Map<String,String> variableNameToColumnNameFromOperations=new HashMap<String,String>();
				OperationsType operationsType=loopRecordsType.getOperations();
				if (operationsType!=null) {
					//variableNameToColumnNameFromOperations=getVariableNameToColumnNameFromOperations(operationsType);
					setColumnNamesFromOperations(operationsType,sourceCode,
							sourceCodeToInventoryNameMap,
							inventoryNameToColumnNamesMap);
				}
					
				setColumnNamesFromTargets(loopRecordsType,sourceCode,sourceCodeToInventoryNameMap,
						inventoryNameToColumnNamesMap);				
			}
		}
		
		Set<String> columnNamesBlackList=new HashSet<String>();
		columnNamesBlackList.add("[]");
		columnNamesBlackList.add("[ ]");
		columnNamesBlackList.add("( )");
			
		//FileUtils.println(convertSourceGridRecordInformationList.size()+" inventories files to analyze.");
		mappingAnalyzerResultList=new ArrayList<MappingAnalyzerResult>();
		for (ConvertSourceGridRecordInformation convertSourceGridRecordInformation:convertSourceGridRecordInformationList) {
			currentStep++;
			mappingAnalyzerWorker.updateStep(currentStep);
			String executionMessage="<html>Processing inventory files... Operations: "+
					Utils.formatNumberWithComma(currentStep)+" / "+Utils.formatNumberWithComma(totalSteps);
			mappingAnalyzerWorker.updateExecutionLabels(executionMessage);
			
			String inventoryName=convertSourceGridRecordInformation.getInventoryName();
			File inventoryFile=inventoryNameToFileMap.get(inventoryName);
			//FileUtils.println("inventoryName: '"+inventoryName+"' inventoryFile: "+inventoryFile);
			if (inventoryFile==null) {
				continue;
			}
			Inventory inventory=FileUtils.getInventory(inventoryFile,inventoryName);
			
			MappingAnalyzerResult mappingAnalyzerResult=new MappingAnalyzerResult();
			mappingAnalyzerResultList.add(mappingAnalyzerResult);
			mappingAnalyzerResult.setInventoryName(inventoryName);
			Set<String> targetNames=inventoryNameToTargetNamesMap.get(inventoryName);
			if (targetNames!=null) {
				mappingAnalyzerResult.getTargetNamesSet().addAll(targetNames);
				Set<String> tempTargetNames=sourceInventoryNameToTargetInventoryNamesMap.get(inventoryName);
				if (tempTargetNames==null) {
					tempTargetNames=new HashSet<String>();
					sourceInventoryNameToTargetInventoryNamesMap.put(inventoryName,tempTargetNames);
				}
				tempTargetNames.addAll(targetNames);
			}
			Set<String> columnNamesUsedInMappingsSet=inventoryNameToColumnNamesMap.get(inventoryName);
			if (columnNamesUsedInMappingsSet==null) {
				columnNamesUsedInMappingsSet=new HashSet<String>();
			}
			if (inventoryName.toLowerCase().endsWith(" - dff")) {
				columnNamesUsedInMappingsSet.addAll(inventory.getFieldNamesUsedForDataEntry());
			}
			//FileUtils.println("inventoryName: '"+inventoryName+"' columnNamesUsedInMappingsSet: "+columnNamesUsedInMappingsSet);
			mappingAnalyzerResult.getColumnNamesUsedInMappingFileSet().addAll(columnNamesUsedInMappingsSet);
			
			List<String> fieldNamesUsedForDataEntry=inventory.getFieldNamesUsedForDataEntry();
			mappingAnalyzerResult.getAllColumnNamesUsedInDataEntry().addAll(fieldNamesUsedForDataEntry);
			for (String fieldNameUsedForDataEntry:fieldNamesUsedForDataEntry) {
				boolean hasCol=columnNamesUsedInMappingsSet.contains(fieldNameUsedForDataEntry);
				if (!hasCol && !columnNamesBlackList.contains(fieldNameUsedForDataEntry)) {
					mappingAnalyzerResult.getColumnNamesNotInAnyMappingFileSet().add(fieldNameUsedForDataEntry);
				}
				else {
					mappingAnalyzerResult.getColumnNamesUsedInMappingFileSet().add(fieldNameUsedForDataEntry);
				}
			}
		}
		if (isSaveToExcel) {
			String executionMessage="Saving to Excel...";
			mappingAnalyzerWorker.updateExecutionLabels(executionMessage);
			saveAnalysisResultsToExcel();
		}
		currentStep++;
		mappingAnalyzerWorker.updateStep(currentStep);
	}
	
	private void setColumnNamesFromView(ViewType viewType,boolean isLeft,String sourceCode,Map<String,String> sourceCodeToInventoryNameMap,
			Map<String,Set<String>> inventoryToColumnNamesMap) throws Exception {
		String inventoryName=sourceCodeToInventoryNameMap.get(sourceCode);
		if (inventoryName==null) {
			//ignoring From = source from another view
			//FileUtils.println("ignoring From = source from another view");
			return;
		}
		Set<String> columnNames=inventoryToColumnNamesMap.get(inventoryName);
		if (columnNames==null) {
			columnNames=new HashSet<String>();
			inventoryToColumnNamesMap.put(inventoryName, columnNames);
		}
		ColumnsType columnsType=viewType.getColumns();
		ColumnType[] columnTypeArray=columnsType.getColumnArray();
		for (ColumnType columnType:columnTypeArray) {
			com.rapidesuite.client.dataConversion0000.ColumnType.Direction.Enum direction=columnType.getDirection();
			if ( (direction.equals(ColumnType.Direction.LEFT) && !isLeft) ||
				 (direction.equals(ColumnType.Direction.RIGHT) && isLeft)
			) {
				continue;
			}
			com.rapidesuite.client.dataConversion0000.ColumnType.Type.Enum colType=columnType.getType();
			//FileUtils.println("sourceCode: '"+sourceCode+"' inventoryName: '"+inventoryName+"' columnType.getName(): '"+columnType.getName()+"' colType:"+colType);
			if (!colType.equals(ColumnType.Type.COLUMN)) {
				continue;
			}
			if (columnType.getName()==null) {
				throw new Exception("COLUMN NAME NULL");
			}

			columnNames.add(columnType.getName());
		}		
	}
	
	private Set<String> getAllTargetNames(LoopRecordsType[] loopRecordsTypeArray) {
		Set<String> targetNames=new HashSet<String>();
		for (LoopRecordsType loopRecordsType:loopRecordsTypeArray) {
			
			IfThenElseType[] ifThenElseTypeArray=loopRecordsType.getIfThenElseArray();
			if (ifThenElseTypeArray!=null) {
				for (IfThenElseType ifThenElseType:ifThenElseTypeArray) {
					Set<String> tempTargetNames=getAllTargetNames(ifThenElseType);
					targetNames.addAll(tempTargetNames);
				}
			}
			
			TargetType[] targetTypeArray=loopRecordsType.getTargetArray();
			for (TargetType targetType:targetTypeArray) {
				MappingsType mappingsType=targetType.getMappings();
				MapType[] mapTypeArray=mappingsType.getMapArray();
				if (mapTypeArray==null) {
					continue;
				}
				targetNames.add(targetType.getName());
			}}
		return targetNames;
	}
	
	private Set<String> getAllTargetNames(IfThenElseType ifThenElseType) {
		Set<String> toReturn=new HashSet<String>();
		
		XmlObject[] xmlObjects=ifThenElseType.selectPath("*");
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);
		
		XmlObject xmlObject=xmlObjectsSubList.get(1);
		XmlObject[] xmlInnerObjects=xmlObject.selectPath("*");
		List<XmlObject> xmlObjectsList=Arrays.asList(xmlInnerObjects);
		Set<String> tempTargetNames=getIfTargetNames(xmlObjectsList);
		toReturn.addAll(tempTargetNames);
		
		xmlObject=xmlObjectsSubList.get(2);
		xmlInnerObjects=xmlObject.selectPath("*");
		xmlObjectsList=Arrays.asList(xmlInnerObjects);
		tempTargetNames=getIfTargetNames(xmlObjectsList);
		toReturn.addAll(tempTargetNames);
		
		return toReturn;
	}
	
	private Set<String> getIfTargetNames(List<XmlObject> xmlObjectsList) {
		Set<String> toReturn=new HashSet<String>();
		
		for (XmlObject xmlObjectTemp:xmlObjectsList) {
			if (xmlObjectTemp instanceof IfThenElseType) {
				IfThenElseType ifThenElseTypeTemp=(IfThenElseType)xmlObjectTemp;
				Set<String> tempTargetNames=getAllTargetNames(ifThenElseTypeTemp);
				toReturn.addAll(tempTargetNames);
			}
			else
				if (xmlObjectTemp instanceof TargetType) {
					TargetType targetType=(TargetType)xmlObjectTemp;
					toReturn.add(targetType.getName());
				}
		}
		return toReturn;
	}

	private void setColumnNamesFromTargets(LoopRecordsType loopRecordsType,String sourceCode,
			Map<String,String> sourceCodeToInventoryNameMap,
			Map<String,Set<String>> inventoryToColumnNamesMap) throws Exception {
		
		String inventoryName=sourceCodeToInventoryNameMap.get(sourceCode);
		if (inventoryName==null) {
			throw new Exception("Internal error, cannot find inventory name for sourceCode: '"+sourceCode+"'");
		}

		Set<String> columnNames=inventoryToColumnNamesMap.get(inventoryName);
		if (columnNames==null) {
			columnNames=new HashSet<String>();
			inventoryToColumnNamesMap.put(inventoryName, columnNames);
		}
				
		TargetType[] targetTypeArray=loopRecordsType.getTargetArray();
		for (TargetType targetType:targetTypeArray) {
			MappingsType mappingsType=targetType.getMappings();
			MapType[] mapTypeArray=mappingsType.getMapArray();
			if (mapTypeArray==null) {
				continue;
			}
				
			//FileUtils.println("Inventory: '"+inventoryName+"' targetType: "+targetType.getName());
			for (MapType mapType:mapTypeArray) {
				com.rapidesuite.client.dataConversion0000.MapType.SourceType.Enum mapSourceType=mapType.getSourceType();
				if (mapSourceType.equals(MapType.SourceType.COLUMN)) {
					//FileUtils.println("Inventory: '"+inventoryName+"' COLUMN: "+mapType.getSourceName());
					if (mapType.getSourceName()==null) {
						throw new Exception("COLUMN NAME NULL");
					}
					columnNames.add(mapType.getSourceName());
				}
				/*
				else 
				if (mapSourceType.equals(MapType.SourceType.VARIABLE)) {
					String columnName=variableNameToColumnNameFromOperations.get(mapType.getSourceName());
					FileUtils.println("Inventory: '"+inventoryName+"' VARIABLE: "+mapType.getSourceName()+" columnName:"+columnName);
					if (inventoryName==null) {
						throw new Exception("Internal error, cannot find column name for sourceName: '"+mapType.getSourceName()+"'");
					}
					if (columnName==null) {
						throw new Exception("COLUMN NAME NULL");
					}
					columnNames.add(columnName);
				}
				*/
			}
		}
	}
	
	private void setColumnNamesFromOperations(OperationsType operationsType,String sourceCode,
			Map<String,String> sourceCodeToInventoryNameMap,
			Map<String,Set<String>> inventoryToColumnNamesMap) throws Exception{
		String inventoryName=sourceCodeToInventoryNameMap.get(sourceCode);
		if (inventoryName==null) {
			throw new Exception("Internal error, cannot find inventory name for sourceCode: '"+sourceCode+"'");
		}
		if (operationsType==null) {
			return;
		}
		Set<String> columnNames=inventoryToColumnNamesMap.get(inventoryName);
		if (columnNames==null) {
			columnNames=new HashSet<String>();
			inventoryToColumnNamesMap.put(inventoryName, columnNames);
		}
		OperationType[] operationTypeArray=operationsType.getOperationArray();
		for (OperationType operationType:operationTypeArray) {
			String columnName=operationType.getInColumn();
			if (columnName!=null) {
				columnNames.add(columnName);
			}
		}
	}
	/*
	private Map<String,String> getVariableNameToColumnNameFromOperations(OperationsType operationsType) {
		Map<String,String> toReturn=new HashMap<String,String>();
		if (operationsType==null) {
			return toReturn;
		}
		OperationType[] operationTypeArray=operationsType.getOperationArray();
		for (OperationType operationType:operationTypeArray) {
			String columnName=operationType.getInColumn();
			String variable=operationType.getOutVariable();
			toReturn.put(variable, columnName);
		}
		return toReturn;
	}
	*/
	protected void saveAnalysisResultsToExcel() {
		try {
			XSSFWorkbook workbook=generateWorkbook(mappingAnalyzerResultList);

			String EXCEL_FILE_EXTENSION=".xlsx";
			JFileChooser fileChooser = new JFileChooser();

			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
			Date now = new Date();
			String strDate = format.format(now);
			File file=new File("RAPIDUpgrade-analyzer-"+strDate+EXCEL_FILE_EXTENSION);

			fileChooser.setSelectedFile(file);
			fileChooser.setDialogTitle("Specify a file to save");   

			mappingAnalyzerWorker.getDialog().setVisible(false);
			int userSelection = fileChooser.showSaveDialog(null);
			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File fileToSave = fileChooser.getSelectedFile();
				int indexOf=fileToSave.getName().toLowerCase().indexOf(EXCEL_FILE_EXTENSION);
				File outputFile=fileToSave;
				if (indexOf==-1) {
					String newFileName=fileToSave.getAbsolutePath()+EXCEL_FILE_EXTENSION;
					outputFile=new File(newFileName);
				}
				FileOutputStream fileOut = new FileOutputStream(outputFile);
				workbook.write(fileOut);
				fileOut.flush();
				fileOut.close();

				Desktop.getDesktop().open(outputFile);
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete the operation: "+e.getMessage());
		}
	}
	
	public static XSSFCellStyle createExcelStandardStyleInstance(XSSFWorkbook workbook,boolean isBold,boolean hasBorder,
			boolean isWhiteFontColor,Color backgroundColor,boolean isAlignedCenter) {
		XSSFCellStyle stdStyle = workbook.createCellStyle();  
		XSSFFont font = workbook.createFont();
		if (isBold) {
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		}
		else {
			font.setBoldweight(Font.COLOR_NORMAL);
		}
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setFontHeightInPoints((short)10);
		if (isWhiteFontColor) {
			font.setColor(IndexedColors.WHITE.getIndex());
		}
		else {
			font.setColor(IndexedColors.BLACK.getIndex());
		}
		stdStyle.setFont(font);
		if (hasBorder) {
			stdStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			stdStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			stdStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			stdStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		}
		if (backgroundColor!=null) {
			stdStyle.setFillForegroundColor(new XSSFColor(backgroundColor));
			stdStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		if (isAlignedCenter) {
			stdStyle.setAlignment(CellStyle.ALIGN_CENTER);
		}
		stdStyle.setWrapText(true);
		
	    return stdStyle;
	}
	
	public XSSFWorkbook generateWorkbook(List<MappingAnalyzerResult> mappingAnalyzerResultList) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet firstSheet = workbook.createSheet("Result") ;
			    
	    XSSFCellStyle stdStyle = createExcelStandardStyleInstance(workbook,false,true,false,null,false);
	    XSSFCellStyle stdAlignedStyle = createExcelStandardStyleInstance(workbook,false,true,false,null,true);
	    XSSFCellStyle headerBlueStyle = createExcelStandardStyleInstance(workbook,true,true,true,new Color(83,141,213),true);
	    //XSSFCellStyle headerYellowStyle = createExcelStandardStyleInstance(workbook,true,true,false,Color.yellow,true);
	    XSSFCellStyle headerLightBlueStyle = createExcelStandardStyleInstance(workbook,true,true,false,new Color(218,238,243),true);
	    //XSSFCellStyle errorStyle = createExcelStandardStyleInstance(workbook,true,true,true,Color.red,false);
	    XSSFCellStyle errorAlignedStyle = createExcelStandardStyleInstance(workbook,true,true,true,new Color(255,139,139),true);
	    	    
		int startingRowIndex=1;
		int startingCellIndex=0;
		
		int rowIndex=startingRowIndex;
		int cellIndex=startingCellIndex;
				
		for (MappingAnalyzerResult mappingAnalyzerResult:mappingAnalyzerResultList) {
			cellIndex=startingCellIndex;
			XSSFRow headerRow = firstSheet.createRow(rowIndex++);
			
			XSSFCell cell = headerRow.createCell(cellIndex++);
			cell.setCellValue("Inventory name");
			cell.setCellStyle(headerBlueStyle);

			cell = headerRow.createCell(cellIndex++);
			cell.setCellStyle(headerBlueStyle);
			cell.setCellValue("Columns");

			cell = headerRow.createCell(cellIndex++);
			cell.setCellStyle(headerBlueStyle);
			cell.setCellValue("Supported");

			cell = headerRow.createCell(cellIndex++);
			cell.setCellStyle(headerBlueStyle);
			cell.setCellValue("NOT supported");
			
			cell = headerRow.createCell(cellIndex++);
			cell.setCellStyle(headerBlueStyle);
			cell.setCellValue("FUSION targets");

			cell = headerRow.createCell(cellIndex++);
			cell.setCellStyle(headerBlueStyle);
			cell.setCellValue("All Columns");
						
			for (String name:mappingAnalyzerResult.getAllColumnNamesUsedInDataEntry()) {
				cell = headerRow.createCell(cellIndex++);
				cell.setCellValue(name);
				cell.setCellStyle(headerLightBlueStyle);
			}
			
			XSSFRow row = firstSheet.createRow(rowIndex++);
			
			cellIndex=startingCellIndex;
			cell = row.createCell(cellIndex++);
			cell.setCellStyle(stdStyle);
			cell.setCellValue(mappingAnalyzerResult.getInventoryName());
			
			cell = row.createCell(cellIndex++);
			cell.setCellStyle(stdAlignedStyle);
			cell.setCellValue(mappingAnalyzerResult.getAllColumnNamesUsedInDataEntry().size());
			
			cell = row.createCell(cellIndex++);
			int cnt=mappingAnalyzerResult.getColumnNamesUsedInMappingFileSet().size();
			cell.setCellValue(cnt);
			cell.setCellStyle(stdAlignedStyle);
						
			cell = row.createCell(cellIndex++);
			cnt=mappingAnalyzerResult.getColumnNamesNotInAnyMappingFileSet().size();
			cell.setCellValue(cnt);
			if (cnt>0) {
				cell.setCellStyle(errorAlignedStyle);
			}
			else {
				cell.setCellStyle(stdAlignedStyle);
			}
			
			cell = row.createCell(cellIndex++);
			cell.setCellStyle(stdAlignedStyle);
			cell.setCellValue(mappingAnalyzerResult.getFormattedTargetNamesSet());
			
			cell = row.createCell(cellIndex++);
			cell.setCellStyle(headerBlueStyle);
			cell.setCellValue("Support");
			
			for (String name:mappingAnalyzerResult.getAllColumnNamesUsedInDataEntry()) {
				boolean isMissing=mappingAnalyzerResult.getColumnNamesNotInAnyMappingFileSet().contains(name);
				cell = row.createCell(cellIndex++);
				if (isMissing) {
					cell.setCellValue("No");
					cell.setCellStyle(errorAlignedStyle);
				}
				else {
					cell.setCellValue("Yes");
					cell.setCellStyle(stdAlignedStyle);
				}
			}
			firstSheet.createRow(rowIndex++);
		}
		
		for (int i=0;i<200;i++) {
			firstSheet.autoSizeColumn(i);
		}
		return workbook;
	}

	public ArrayList<MappingAnalyzerResult> getMappingAnalyzerResultList() {
		return mappingAnalyzerResultList;
	}

	public Map<String, Set<String>> getSourceInventoryNameToTargetInventoryNamesMap() {
		return sourceInventoryNameToTargetInventoryNamesMap;
	}
	
}
