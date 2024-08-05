package com.rapidesuite.snapshot.controller.convert;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.dataConversion0000.DataConversionDocument;
import com.rapidesuite.client.dataConversion0000.DataConversionType;
import com.rapidesuite.client.dataConversion0000.IfThenElseType;
import com.rapidesuite.client.dataConversion0000.IgnoreMandatoryValidationColumnType;
import com.rapidesuite.client.dataConversion0000.IgnoreMandatoryValidationColumnsType;
import com.rapidesuite.client.dataConversion0000.LoopRecordsType;
import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.client.dataConversion0000.TargetType;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.controller.convert.constructs.ConvertSourceTypeGeneric;
import com.rapidesuite.snapshot.controller.convert.constructs.LoopRecordsTypeConstruct;
import com.rapidesuite.snapshot.controller.convert.constructs.SourceTypeConstruct;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.convert.ConvertSourceGridRecordInformation;
import com.rapidesuite.snapshot.view.convert.ConvertTargetGridRecordInformation;

public class ConvertEngine {

	private ConvertController convertController;
	private File mappingXMLFile;
	private boolean isPostConfiguration;
	
	private Map<String,List<DataRow>> targetNameToDataRowsConfigurationMap;
	private Map<String,List<DataRow>> targetDataRowsPostConfigurationMap;
	private Map<String,List<DataRow>> targetDataRowsPostImplementationMap;
	
	private Map<String, SourceType> sourceCodeToSourceTypeMap;
	private Map<String, Map<String, Integer>> sourceCodeToFieldPositionMap;
	private Map<String, File> sourceInventoryNameToInventoryFileMap;
	private Map<String, Inventory> sourceInventoryNameToInventoryMap;
	private Map<String, ConvertSourceTypeGeneric> sourceCodeToConvertSourceTypeGenericMap;
	private Set<String> columnNamesConvertedSet;
	private Map<String,List<String[]>> inventoryNameToDataRowsMap;
	private Map<String,String[]> splitVariableTextToArrayMap;
	
	private Map<String, File> targetInventoryNameToInventoryFileMap;
	private Map<String, Inventory> targetInventoryNameToInventoryMap;
	private Map<String, Map<String, Integer>> targetNameToFieldPositionMap;
	private ConvertSourceGridRecordInformation convertSourceGridRecordInformation;
	
	private boolean hasView;
	private File subFolderConfiguration;
	private File subFolderPostConfiguration;
	private File subFolderPostImplementation;
	private int MAX_RETRY_COUNT=1000;
	
	public static final String DATA_FOLDER_NAME_CONFIGURATION="CONFIGURATION";
	public static final String DATA_FOLDER_NAME_POST_CONFIGURATION="POST-CONFIGURATION";
	public static final String DATA_FOLDER_NAME_POST_IMPLEMENTATION="POST-IMPLEMENTATION";
	
	
	public ConvertEngine(ConvertController convertController,ConvertSourceGridRecordInformation convertSourceGridRecordInformation,
			File convertedFolder,File mappingXMLFile,boolean isPostConfiguration,
			Map<String, File> sourceInventoryNameToInventoryFileMap,
			Map<String, File> targetInventoryNameToInventoryFileMap) throws Exception {
		this.mappingXMLFile=mappingXMLFile;
		this.isPostConfiguration=isPostConfiguration;
		this.convertSourceGridRecordInformation=convertSourceGridRecordInformation;
		this.sourceInventoryNameToInventoryFileMap=sourceInventoryNameToInventoryFileMap;
		this.convertController=convertController;
		this.targetInventoryNameToInventoryFileMap=targetInventoryNameToInventoryFileMap;
		sourceCodeToFieldPositionMap=new HashMap<String, Map<String, Integer>>();
		sourceInventoryNameToInventoryMap=new HashMap<String,Inventory>();
		targetInventoryNameToInventoryMap=new HashMap<String,Inventory>();
		targetNameToFieldPositionMap=new HashMap<String, Map<String, Integer>>();
		sourceCodeToSourceTypeMap=new HashMap<String,SourceType>();
		columnNamesConvertedSet=new HashSet<String>();
		sourceCodeToConvertSourceTypeGenericMap=new HashMap<String,ConvertSourceTypeGeneric>();
		inventoryNameToDataRowsMap=new HashMap<String,List<String[]>>();
		splitVariableTextToArrayMap=new HashMap<String,String[]>();
		
		targetNameToDataRowsConfigurationMap=new HashMap<String,List<DataRow>>();
		targetDataRowsPostConfigurationMap=new HashMap<String,List<DataRow>>();
		targetDataRowsPostImplementationMap=new HashMap<String,List<DataRow>>();
		
		subFolderConfiguration=new File(convertedFolder,DATA_FOLDER_NAME_CONFIGURATION);
		subFolderConfiguration.mkdirs();
		subFolderPostConfiguration=new File(convertedFolder,DATA_FOLDER_NAME_POST_CONFIGURATION);
		subFolderPostConfiguration.mkdirs();
		subFolderPostImplementation=new File(convertedFolder,DATA_FOLDER_NAME_POST_IMPLEMENTATION);
		subFolderPostImplementation.mkdirs();
	}
		
	public void convert() throws Exception {
		FileUtils.println("@@@@@@@@@ CONVERT, convertSourceGridRecordInformation: "+
				convertSourceGridRecordInformation.getInventoryName()+" isPostConfiguration:"+isPostConfiguration);
		DataConversionDocument dataConversionDocument=ModelUtils.getDataConversionDocument(mappingXMLFile);
		DataConversionType dataConversionType=dataConversionDocument.getDataConversion();
		
		SourceType[] sourceTypeArray=dataConversionType.getSourceArray();
		for (SourceType sourceType:sourceTypeArray) {
			SourceTypeConstruct sourceTypeConstruct=new SourceTypeConstruct(this);
			sourceTypeConstruct.process(sourceType);
		}
		
		// Analyzing metadata like ignoring mandatory columns for validation
		LoopRecordsType[] loopRecordsTypeArray=dataConversionType.getLoopRecordsArray();
		for (LoopRecordsType loopRecordsType:loopRecordsTypeArray) {
			IfThenElseType[] ifThenElseTypeArray=loopRecordsType.getIfThenElseArray();
			if (ifThenElseTypeArray!=null) {
				for (IfThenElseType ifThenElseType:ifThenElseTypeArray) {
					XmlObject[] xmlObjects=ifThenElseType.selectPath("*");
					analyzeIfMetadata(xmlObjects);
				}
			}
			
			TargetType[] targetTypeArray=loopRecordsType.getTargetArray();
			for (TargetType targetType:targetTypeArray) {
				analyzeTargetTypeMetadata(targetType);
			}
		}
		
		loopRecordsTypeArray=dataConversionType.getLoopRecordsArray();
		for (LoopRecordsType loopRecordsType:loopRecordsTypeArray) {
			LoopRecordsTypeConstruct loopRecordsTypeConstruct=new LoopRecordsTypeConstruct(this);
			loopRecordsTypeConstruct.process(loopRecordsType);
		}

		Set<String> allTargetNameSet=new HashSet<String>();
		Iterator<String> iterator=targetNameToDataRowsConfigurationMap.keySet().iterator();
		while (iterator.hasNext()) {
			String targetName=iterator.next();
			allTargetNameSet.add(targetName);
		}
		iterator=targetDataRowsPostConfigurationMap.keySet().iterator();
		while (iterator.hasNext()) {
			String targetName=iterator.next();
			allTargetNameSet.add(targetName);
		}
		iterator=targetDataRowsPostImplementationMap.keySet().iterator();
		while (iterator.hasNext()) {
			String targetName=iterator.next();
			allTargetNameSet.add(targetName);
		}
		
		iterator=allTargetNameSet.iterator();
		while (iterator.hasNext()) {
			String targetName=iterator.next();
			exportTargetDataToXML(targetName);
		}
	}
	
	public void storeData(TargetType targetType,DataRow	targetDataRow) {
		String targetName=targetType.getName();
		if (isPostConfiguration) {
			Boolean isPostImplementation=targetType.getIsPostImplementation();
			if (isPostImplementation!=null && isPostImplementation.booleanValue()) {
				List<DataRow> targetDataRows=targetDataRowsPostImplementationMap.get(targetName);
				if (targetDataRows==null) {
					targetDataRows=new ArrayList<DataRow>();
					targetDataRowsPostImplementationMap.put(targetName, targetDataRows);
				}
				targetDataRows.add(targetDataRow);
			}
			else {
				List<DataRow> targetDataRows=targetDataRowsPostConfigurationMap.get(targetName);
				if (targetDataRows==null) {
					targetDataRows=new ArrayList<DataRow>();
					targetDataRowsPostConfigurationMap.put(targetName, targetDataRows);
				}
				targetDataRows.add(targetDataRow);
			}
		}
		else {
			List<DataRow> targetDataRows=targetNameToDataRowsConfigurationMap.get(targetName);
			if (targetDataRows==null) {
				targetDataRows=new ArrayList<DataRow>();
				targetNameToDataRowsConfigurationMap.put(targetName, targetDataRows);
			}
			targetDataRows.add(targetDataRow);
		}
	}
	
	private void analyzeIfMetadata(XmlObject[] xmlObjects) throws Exception {
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);

		XmlObject xmlObjectThen=xmlObjectsSubList.get(1);
		XmlObject xmlObjectElse=xmlObjectsSubList.get(2);

		analyzeIfThenElseMetadata(xmlObjectThen);
		analyzeIfThenElseMetadata(xmlObjectElse);
	}
	
	private void analyzeIfThenElseMetadata(XmlObject xmlObject) throws Exception {
		XmlObject[] xmlInnerObjects=xmlObject.selectPath("*");
		List<XmlObject> xmlObjectsList=Arrays.asList(xmlInnerObjects);
		for (XmlObject xmlObjectTemp:xmlObjectsList) {
			if (xmlObjectTemp instanceof IfThenElseType) {
				XmlObject[] xmlInnerObjectsTemp=xmlObjectTemp.selectPath("*");
				analyzeIfMetadata(xmlInnerObjectsTemp);
			}
			else
				if (xmlObjectTemp instanceof TargetType) {
					analyzeTargetTypeMetadata( (TargetType)xmlObjectTemp);
				}
		}
	}
	
	private void analyzeTargetTypeMetadata(TargetType targetType) throws Exception {
		String targetName=targetType.getName();

		IgnoreMandatoryValidationColumnsType ignoreMandatoryValidationColumnsType=targetType.getIgnoreMandatoryValidationColumns();
		if (ignoreMandatoryValidationColumnsType!=null) {
			IgnoreMandatoryValidationColumnType[] ignoreMandatoryValidationColumnArray=ignoreMandatoryValidationColumnsType.getIgnoreMandatoryValidationColumnArray();
			Set<String> columnsSet=getConvertController().getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().get(targetName);
			if (columnsSet==null) {
				columnsSet=new HashSet<String>();
				getConvertController().getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().put(targetName, columnsSet);
			}
			for (IgnoreMandatoryValidationColumnType ignoreMandatoryValidationColumnType:ignoreMandatoryValidationColumnArray) {
				columnsSet.add(ignoreMandatoryValidationColumnType.getName());
			}		
		}
	}
	
	public void exportTargetDataToXML(String targetName) throws Exception {
		Inventory targetInventory=getTargetInventory(targetName);
		
		List<DataRow> targetDataRowsConfiguration=targetNameToDataRowsConfigurationMap.get(targetName);
		List<DataRow> targetDataRowsPostConfiguration=targetDataRowsPostConfigurationMap.get(targetName);
		List<DataRow> targetDataRowsPostImplementation=targetDataRowsPostImplementationMap.get(targetName);
		ConvertTargetGridRecordInformation convertTargetGridRecordInformation=
				convertController.getTargetInventoryNameToConvertTargetGridRecordInformationMap().get(targetName);
		int totalRecords=convertTargetGridRecordInformation.getTotalRecords();
		/*
		FileUtils.println("### exportTargetDataToXML targetName: '"+targetName+"' isPostConfiguration:"+isPostConfiguration+" totalRecords:"+totalRecords+" ###");
		FileUtils.println("exportTargetDataToXML targetDataRowsConfiguration:"+targetDataRowsConfiguration);
		FileUtils.println("exportTargetDataToXML targetDataRowsPostConfiguration:"+targetDataRowsPostConfiguration);
		FileUtils.println("exportTargetDataToXML convertTargetGridRecordInformation.getTotalRecordsConfiguration():"+convertTargetGridRecordInformation.getTotalRecordsConfiguration());
		FileUtils.println("exportTargetDataToXML convertTargetGridRecordInformation.getTotalRecordsPostConfiguration():"+convertTargetGridRecordInformation.getTotalRecordsPostConfiguration());
		FileUtils.println("exportTargetDataToXML convertTargetGridRecordInformation.getTotalRecordsPostImplementation():"+convertTargetGridRecordInformation.getTotalRecordsPostImplementation());
		 */
		if (targetDataRowsConfiguration!=null) {
			totalRecords=totalRecords+targetDataRowsConfiguration.size();
			//FileUtils.println("targetDataRowsConfiguration.size():"+targetDataRowsConfiguration.size());
		}
		if (targetDataRowsPostConfiguration!=null) {
			totalRecords=totalRecords+targetDataRowsPostConfiguration.size();
			//FileUtils.println("targetDataRowsPostConfiguration.size():"+targetDataRowsPostConfiguration.size());
		}
		if (targetDataRowsPostImplementation!=null) {
			totalRecords=totalRecords+targetDataRowsPostImplementation.size();
			//FileUtils.println("targetDataRowsPostImplementation.size():"+targetDataRowsPostImplementation.size());
		}
		convertTargetGridRecordInformation.setTotalRecords(totalRecords);
		File folder=null;
		if (isPostConfiguration) {
			//FileUtils.println("exportTargetDataToXML, targetDataRowsPostImplementation:"+targetDataRowsPostImplementation);
			if (targetDataRowsPostImplementation!=null) {
				folder=subFolderPostImplementation;
				convertTargetGridRecordInformation.setTotalRecordsPostImplementation(
						convertTargetGridRecordInformation.getTotalRecordsPostImplementation()+targetDataRowsPostImplementation.size());
				ModelUtils.exportToXML(folder,targetInventory,targetDataRowsPostImplementation);		
				ModelUtils.validation(convertTargetGridRecordInformation,
						getConvertController().getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().get(targetName),targetInventory,targetDataRowsPostImplementation);
				targetDataRowsPostImplementationMap.remove(targetName);
				//FileUtils.println("exportTargetDataToXML, POST IMPLEMENTATION");
			}
			if (targetDataRowsPostConfiguration!=null) {
				folder=subFolderPostConfiguration;
				convertTargetGridRecordInformation.setTotalRecordsPostConfiguration(
						convertTargetGridRecordInformation.getTotalRecordsPostConfiguration()+targetDataRowsPostConfiguration.size());
				ModelUtils.exportToXML(folder,targetInventory,targetDataRowsPostConfiguration);		
				ModelUtils.validation(convertTargetGridRecordInformation,
						getConvertController().getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().get(targetName),targetInventory,targetDataRowsPostConfiguration);
				targetDataRowsPostConfiguration.remove(targetName);
				//FileUtils.println("exportTargetDataToXML, POST CONFIGURATIONS");
			}
		}
		else {
			folder=subFolderConfiguration;
			convertTargetGridRecordInformation.setTotalRecordsConfiguration(
					convertTargetGridRecordInformation.getTotalRecordsConfiguration()+
					targetDataRowsConfiguration.size());
			ModelUtils.exportToXML(folder,targetInventory,targetDataRowsConfiguration);		
			ModelUtils.validation(convertTargetGridRecordInformation,
					getConvertController().getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().get(targetName),targetInventory,targetDataRowsConfiguration);
			targetDataRowsConfiguration.remove(targetName);
			//FileUtils.println("exportTargetDataToXML, CONFIGURATIONS");
		}
		//FileUtils.println("after convertTargetGridRecordInformation.getTotalRecordsConfiguration():"+convertTargetGridRecordInformation.getTotalRecordsConfiguration());
		//FileUtils.println("after convertTargetGridRecordInformation.getTotalRecordsPostConfiguration():"+convertTargetGridRecordInformation.getTotalRecordsPostConfiguration());
		//FileUtils.println("after convertTargetGridRecordInformation.getTotalRecordsPostImplementation():"+convertTargetGridRecordInformation.getTotalRecordsPostImplementation());

		//FileUtils.println("exportTargetDataToXML, DONE...");
	}

	public ConvertController getConvertController() {
		return convertController;
	}

	public ConvertSourceGridRecordInformation getConvertSourceGridRecordInformation() {
		return convertSourceGridRecordInformation;
	}
	
	public List<String[]> getSourceCodeToDataRowsMap(String sourceCode) throws Exception {
		SourceType sourceType=sourceCodeToSourceTypeMap.get(sourceCode);
		
		if (sourceType==null) {
			throw new Exception("Undefined source code: '"+sourceCode+"'");
		}
		if (sourceType.getType().equals(SourceType.Type.INVENTORY)) {
			String inventoryName=sourceType.getName();
			if (inventoryName==null) {
				throw new Exception("Missing attribute 'name' in source tag for the source code: '"+sourceCode+"'");
			}
			List<String[]> dataRows=inventoryNameToDataRowsMap.get(inventoryName);
			if (dataRows!=null) {
				return dataRows;
			}
			
			int retryCount=0;
			while ( true ) {
				if (convertController.isExecutionStopped()) {
					throw new Exception("Execution stopped!");
				}
				if (retryCount==MAX_RETRY_COUNT) {
					break;
				}
				boolean isSourceInventorySelected=getConvertController().isSourceInventorySelected(inventoryName);
				if (!isSourceInventorySelected) {
					return new ArrayList<String[]>();
				}
				boolean isSourceInventoryCompleted=getConvertController().isSourceInventoryCompleted(inventoryName);
				if (isSourceInventoryCompleted || inventoryName.equalsIgnoreCase(convertSourceGridRecordInformation.getInventoryName())) {
					File dataFile=getConvertController().getDataFile(inventoryName);
					if (dataFile==null) {
						throw new Exception("Unable to find XML data file for the inventory name: '"+inventoryName+"'");
					}
					convertSourceGridRecordInformation.setRemarks("");
					List<String[]> dataRowsTemp=InjectUtils.parseXMLDataFile(dataFile);
					inventoryNameToDataRowsMap.put(inventoryName, dataRowsTemp);
					return dataRowsTemp;
				}
				Thread.sleep(3000);
				retryCount++;
				convertSourceGridRecordInformation.setRemarks("Waiting for inventory '"+inventoryName+"' to complete (Retries: "+retryCount+" / "+MAX_RETRY_COUNT+")");
			}
			throw new Exception("Timeout waiting for completion of inventory name: '"+inventoryName+"'");
		}
		else
		if (sourceType.getType().equals(SourceType.Type.VIEW) || sourceType.getType().equals(SourceType.Type.ROWS)) {
			ConvertSourceTypeGeneric convertSourceTypeGeneric=sourceCodeToConvertSourceTypeGenericMap.get(sourceCode);
			if (convertSourceTypeGeneric==null) {
				throw new Exception("Invalid mapping - unable to find "+sourceType.getType().toString()+" source code: '"+sourceCode+"'");
			}
			return convertSourceTypeGeneric.getDataRows();
		}
		else {
			throw new Exception("Unsupported source code: '"+sourceCode+"'");
		}
	}

	public Inventory getSourceInventory(String inventoryName) throws Exception {
		Inventory inventory=sourceInventoryNameToInventoryMap.get(inventoryName);
		if (inventory==null) {
			File inventoryFile=sourceInventoryNameToInventoryFileMap.get(inventoryName);
			if (inventoryFile==null) {
				throw new Exception("Invalid Mapping file: cannot find the source inventory name: '"+inventoryName+"'");
			}
			inventory=FileUtils.getInventory(inventoryFile,inventoryName);
			sourceInventoryNameToInventoryMap.put(inventoryName, inventory);
		}
		return inventory;		
	}

	public Inventory getTargetInventory(String inventoryName) throws Exception {
		Inventory inventory=targetInventoryNameToInventoryMap.get(inventoryName);
		if (inventory==null) {
			File inventoryFile=targetInventoryNameToInventoryFileMap.get(inventoryName);
			if (inventoryFile==null) {
				throw new Exception("Invalid Mapping file: cannot find the target inventory name: '"+inventoryName+"'");
			}
			inventory=FileUtils.getInventory(inventoryFile,inventoryName);
			targetInventoryNameToInventoryMap.put(inventoryName, inventory);
		}
		return inventory;		
	}
	
	public Map<String, Integer> getTargetInventoryNameToPosition(String inventoryName) throws Exception {
		Map<String, Integer> fieldPositionMap=targetNameToFieldPositionMap.get(inventoryName);
		if (fieldPositionMap==null) {
			Inventory inventory=getTargetInventory(inventoryName);
			fieldPositionMap=new HashMap<String, Integer>();
			List<String> fieldNamesUsedForDataEntry=inventory.getFieldNamesUsedForDataEntry();
			int position=0;
			for (String fieldName:fieldNamesUsedForDataEntry) {
				fieldPositionMap.put(fieldName, position);
				position++;
			}
			targetNameToFieldPositionMap.put(inventoryName, fieldPositionMap);
		}
		return fieldPositionMap;		
	}
	
	public Map<String, Integer> getFieldNameToPositionMap(String sourceCode) throws Exception {
		SourceType sourceType=sourceCodeToSourceTypeMap.get(sourceCode);
		if (sourceType==null) {
			throw new Exception("Undefined source code: '"+sourceCode+"'");
		}
		if (sourceType.getType().equals(SourceType.Type.INVENTORY)) {
			String inventoryName=sourceType.getName();
			Map<String, Integer> fieldToPositionMap=sourceCodeToFieldPositionMap.get(inventoryName);
			if (fieldToPositionMap==null) {
				Inventory inventory=getSourceInventory(inventoryName);
				fieldToPositionMap=new HashMap<String, Integer>();
				List<String> fieldNamesUsedForDataEntry=inventory.getFieldNamesUsedForDataEntry();
				int position=0;
				for (String fieldName:fieldNamesUsedForDataEntry) {
					fieldToPositionMap.put(fieldName, position);
					position++;
				}
				sourceCodeToFieldPositionMap.put(sourceCode, fieldToPositionMap);
			}
			return fieldToPositionMap;
		}
		else
		if (sourceType.getType().equals(SourceType.Type.VIEW) || sourceType.getType().equals(SourceType.Type.ROWS)) {
			ConvertSourceTypeGeneric convertSourceTypeGeneric=sourceCodeToConvertSourceTypeGenericMap.get(sourceCode);
			if (convertSourceTypeGeneric==null) {
				throw new Exception("Invalid mapping - unable to find "+sourceType.getType().toString()+" source code: '"+sourceCode+"'");
			}
			return convertSourceTypeGeneric.getColumnNameToPositionMap();
		}
		else {
			throw new Exception("Unsupported source code: '"+sourceCode+"'");
		}
	}

	public Map<String,SourceType> getSourceCodeToSourceTypeMap() {
		return sourceCodeToSourceTypeMap;
	}

	public Map<String, ConvertSourceTypeGeneric> getSourceCodeToConvertSourceTypeGenericMap() {
		return sourceCodeToConvertSourceTypeGenericMap;
	}

	public Set<String> getColumnNamesConvertedSet() {
		return columnNamesConvertedSet;
	}

	public boolean hasView() {
		return hasView;
	}

	public void setHasView(boolean hasView) {
		this.hasView = hasView;
	}

	public Map<String, String[]> getSplitVariableTextToArrayMap() {
		return splitVariableTextToArrayMap;
	}

	public File getSubFolderConfiguration() {
		return subFolderConfiguration;
	}

	public File getSubFolderPostConfiguration() {
		return subFolderPostConfiguration;
	}

	public File getSubFolderPostImplementation() {
		return subFolderPostImplementation;
	}

	public Map<String, List<DataRow>> getTargetDataRowsPostConfigurationMap() {
		return targetDataRowsPostConfigurationMap;
	}

	public Map<String, List<DataRow>> getTargetDataRowsPostImplementationMap() {
		return targetDataRowsPostImplementationMap;
	}

}
