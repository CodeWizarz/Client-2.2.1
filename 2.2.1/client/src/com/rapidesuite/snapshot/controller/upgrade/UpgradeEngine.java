package com.rapidesuite.snapshot.controller.upgrade;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.dataConversion0000.DataConversionDocument;
import com.rapidesuite.client.dataConversion0000.DataConversionType;
import com.rapidesuite.client.dataConversion0000.IfThenElseLoopType;
import com.rapidesuite.client.dataConversion0000.IfThenElseType;
import com.rapidesuite.client.dataConversion0000.IgnoreMandatoryValidationColumnType;
import com.rapidesuite.client.dataConversion0000.IgnoreMandatoryValidationColumnsType;
import com.rapidesuite.client.dataConversion0000.InventoryType;
import com.rapidesuite.client.dataConversion0000.LoopRecordsType;
import com.rapidesuite.client.dataConversion0000.RemoveTargetDuplicatesType;
import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.client.dataConversion0000.TargetType;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.snapshot.controller.upgrade.constructs.ConvertSourceTypeGeneric;
import com.rapidesuite.snapshot.controller.upgrade.constructs.IfThenElseLoopTypeConstruct;
import com.rapidesuite.snapshot.controller.upgrade.constructs.LoopRecordsTypeConstruct;
import com.rapidesuite.snapshot.controller.upgrade.constructs.SourceTypeConstruct;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.upgrade.FusionInventoryRow;

public class UpgradeEngine {

	private UpgradeController upgradeController;
	private File mappingXMLFile;
	private boolean isPostConfiguration;
	
	private List<DataRow> dataRowsConfigurationList;
	private List<DataRow> dataRowsPostConfigurationList;
	private Map<String,List<DataRow>> fusionInventoryNameToDataRowsPostImplementationSubPostList;
	private Map<String,List<DataRow>> fusionInventoryNameToDataRowsPostImplementationSubObsoleteList;
	
	private Set<String> uniqueKeysDataRowsConfigurationSet;
	private Set<String> uniqueKeysDataRowsPostConfigurationSet;
	private Set<String> uniqueKeysDataRowsPostImplementationSubPostSet;
	private Set<String> uniqueKeysDataRowsPostImplementationSubObsoleteSet;
	private Set<String> targetInventoryNameToRemoveDuplicatesSet;
	
	private Map<String,FusionInventoryRow> postImplementationFusionInventoryNameToFusionInventoryRowMap;
	private boolean isNewLoop;
	private Set<String> rscTableNameAlreadyProcessedSet;
	
	private Map<String, SourceType> sourceCodeToSourceTypeMap;
	private Map<String, Map<String, Integer>> sourceCodeToFieldPositionMap;
	private Map<String, ConvertSourceTypeGeneric> sourceCodeToConvertSourceTypeGenericMap;
	private Set<String> columnNamesConvertedSet;
	private Map<String,String[]> splitVariableTextToArrayMap;
	private FusionInventoryRow fusionInventoryRow;
	
	private boolean hasView;
	private File subFolderConfiguration;
	private File subFolderPostConfiguration;
	private Map<String,Map<String, Integer>> fusionInventoryToFieldPositionMap;
	private UpgradeWorker upgradeWorker;
	private File subFolderSubPostImplementation;
	private File subFolderSubObsoletePostImplementation;
	
	public static final String DATA_FOLDER_NAME_CONFIGURATION="CONFIGURATION";
	public static final String DATA_FOLDER_NAME_POST_CONFIGURATION="POST-CONFIGURATION";
	public static final String DATA_FOLDER_NAME_POST_IMPLEMENTATION="POST-IMPLEMENTATION";
	public static final String DATA_FOLDER_NAME_SUB_POST_IMPLEMENTATION="POST";
	public static final String DATA_FOLDER_NAME_SUB_OBSOLETE_POST_IMPLEMENTATION="OBSOLETE";
	public static final String NAVIGATION_MAPPER_FIELD_NAME="NAVIGATION_MAPPER";
	public static final String RSC_TABLES_PREFIX="RSC - ";
	
	public UpgradeEngine(UpgradeWorker upgradeWorker,FusionInventoryRow fusionInventoryRow,
			File convertedFolder,File mappingXMLFile,boolean isPostConfiguration) throws Exception {
		this.mappingXMLFile=mappingXMLFile;
		this.upgradeWorker=upgradeWorker;
		this.isPostConfiguration=isPostConfiguration;
		this.fusionInventoryRow=fusionInventoryRow;
		this.upgradeController=(UpgradeController)upgradeWorker.getGenericController();
		sourceCodeToFieldPositionMap=new HashMap<String, Map<String, Integer>>();
		sourceCodeToSourceTypeMap=new HashMap<String,SourceType>();
		columnNamesConvertedSet=new HashSet<String>();
		sourceCodeToConvertSourceTypeGenericMap=new HashMap<String,ConvertSourceTypeGeneric>();
		splitVariableTextToArrayMap=new HashMap<String,String[]>();
		postImplementationFusionInventoryNameToFusionInventoryRowMap=new HashMap<String,FusionInventoryRow>();
		rscTableNameAlreadyProcessedSet=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		
		dataRowsConfigurationList=new ArrayList<DataRow>();
		dataRowsPostConfigurationList=new ArrayList<DataRow>();
		fusionInventoryNameToDataRowsPostImplementationSubPostList=new HashMap<String,List<DataRow>>();
		fusionInventoryNameToDataRowsPostImplementationSubObsoleteList=new HashMap<String,List<DataRow>>();
		
		uniqueKeysDataRowsConfigurationSet=new HashSet<String>();
		uniqueKeysDataRowsPostConfigurationSet=new HashSet<String>();
		uniqueKeysDataRowsPostImplementationSubPostSet=new HashSet<String>();
		uniqueKeysDataRowsPostImplementationSubObsoleteSet=new HashSet<String>();
		targetInventoryNameToRemoveDuplicatesSet=new HashSet<String>();
		
		subFolderConfiguration=new File(convertedFolder,DATA_FOLDER_NAME_CONFIGURATION);
		subFolderConfiguration.mkdirs();
		subFolderPostConfiguration=new File(convertedFolder,DATA_FOLDER_NAME_POST_CONFIGURATION);
		subFolderPostConfiguration.mkdirs();
		File subFolderPostImplementation=new File(convertedFolder,DATA_FOLDER_NAME_POST_IMPLEMENTATION);
		subFolderPostImplementation.mkdirs();
		subFolderSubPostImplementation=new File(subFolderPostImplementation,DATA_FOLDER_NAME_SUB_POST_IMPLEMENTATION);
		subFolderSubPostImplementation.mkdirs();
		subFolderSubObsoletePostImplementation=new File(subFolderPostImplementation,DATA_FOLDER_NAME_SUB_OBSOLETE_POST_IMPLEMENTATION);
		subFolderSubObsoletePostImplementation.mkdirs();
		
		Inventory inventory=fusionInventoryRow.getInventory();
		fusionInventoryToFieldPositionMap=new HashMap<String,Map<String, Integer>>();
		addFieldNamePositionMap(inventory);
	}
		
	public void convert() throws Exception {
		DataConversionDocument dataConversionDocument=ModelUtils.getDataConversionDocument(mappingXMLFile);
		DataConversionType dataConversionType=dataConversionDocument.getDataConversion();

		RemoveTargetDuplicatesType removeTargetDuplicatesType=dataConversionType.getRemoveTargetDuplicates();
		if (removeTargetDuplicatesType!=null) {
			InventoryType[] inventoryTypeArray=removeTargetDuplicatesType.getInventoryArray();
			for (InventoryType inventoryType:inventoryTypeArray) {
				targetInventoryNameToRemoveDuplicatesSet.add(inventoryType.getName());
			}
		}

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
			isNewLoop=true;
			loopRecordsTypeConstruct.process(loopRecordsType);
		}
		IfThenElseLoopType[] ifThenElseLoopTypeArray=dataConversionType.getIfThenElseLoopArray();
		if (ifThenElseLoopTypeArray!=null) {
			for (IfThenElseLoopType ifThenElseLoopType:ifThenElseLoopTypeArray) {
				IfThenElseLoopTypeConstruct ifThenElseLoopTypeConstruct=new IfThenElseLoopTypeConstruct(this);
				ifThenElseLoopTypeConstruct.process(ifThenElseLoopType);
			}
		}
		
		exportTargetDataToXML();
		
		/*
		 * Update UI for all the post implementation fusion tasks
		 */
		Iterator<String> iterator=postImplementationFusionInventoryNameToFusionInventoryRowMap.keySet().iterator();
		List<FusionInventoryRow> additionalFusionInventoryRowList=new ArrayList<FusionInventoryRow>();
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			FusionInventoryRow tempFusionInventoryRow=postImplementationFusionInventoryNameToFusionInventoryRowMap.get(inventoryName);
			tempFusionInventoryRow.setStatus(UIConstants.UI_STATUS_COMPLETED);
			tempFusionInventoryRow.setRemarks("");
			additionalFusionInventoryRowList.add(tempFusionInventoryRow);
		}
		this.upgradeController.getUpgradeFrame().getTabUpgradeMainPanel().getFusionInventoryGridPanel().refreshGrid(
				additionalFusionInventoryRowList);
	}
	
	public void storeData(TargetType targetType,DataRow	targetDataRow) {
		if (isPostConfiguration) {
			Boolean isPostImplementation=targetType.getIsPostImplementation();
			if (isPostImplementation!=null && isPostImplementation.booleanValue()) {
				processPostImplementationList(targetType,targetDataRow);
			}
			else {
				dataRowsPostConfigurationList.add(targetDataRow);
			}
		}
		else {
			Boolean isPostImplementation=targetType.getIsPostImplementation();
			Boolean isPostConfiguration=targetType.getIsPostConfiguration();
			if (isPostImplementation!=null && isPostImplementation.booleanValue()) {
				processPostImplementationList(targetType,targetDataRow);
			}
			else
			if (isPostConfiguration!=null && isPostConfiguration.booleanValue()) {
				dataRowsPostConfigurationList.add(targetDataRow);
			}
			else {
				dataRowsConfigurationList.add(targetDataRow);
			}
		}
	}
	
	private void processPostImplementationList(TargetType targetType,DataRow targetDataRow) {
		Boolean isObsolete=targetType.getIsObsolete();
		List<DataRow> list=null;
		if (isObsolete!=null && isObsolete.booleanValue()) {
			list=fusionInventoryNameToDataRowsPostImplementationSubObsoleteList.get(targetType.getName());
			if (list==null) {
				list=new ArrayList<DataRow>();
				fusionInventoryNameToDataRowsPostImplementationSubObsoleteList.put(targetType.getName(), list);
			}
		}
		else {
			list=fusionInventoryNameToDataRowsPostImplementationSubPostList.get(targetType.getName());
			if (list==null) {
				list=new ArrayList<DataRow>();
				fusionInventoryNameToDataRowsPostImplementationSubPostList.put(targetType.getName(), list);
			}
		}				
		list.add(targetDataRow);
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
		validateTargetName(targetName);
		
		IgnoreMandatoryValidationColumnsType ignoreMandatoryValidationColumnsType=targetType.getIgnoreMandatoryValidationColumns();
		if (ignoreMandatoryValidationColumnsType!=null) {
			IgnoreMandatoryValidationColumnType[] ignoreMandatoryValidationColumnArray=ignoreMandatoryValidationColumnsType.getIgnoreMandatoryValidationColumnArray();
			Set<String> columnsSet=upgradeController.getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().get(targetName);
			if (columnsSet==null) {
				columnsSet=new HashSet<String>();
				upgradeController.getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().put(targetName, columnsSet);
			}
			for (IgnoreMandatoryValidationColumnType ignoreMandatoryValidationColumnType:ignoreMandatoryValidationColumnArray) {
				columnsSet.add(ignoreMandatoryValidationColumnType.getName());
			}		
		}
	}

	private void validateTargetName(String targetName) throws Exception {
		if (!targetName.equalsIgnoreCase(fusionInventoryRow.getInventoryName())
			&&	!targetName.toUpperCase().startsWith(RSC_TABLES_PREFIX) 
				) {
			throw new Exception("Invalid mapping file, the target name '"+targetName+"' must be the same as the Fusion inventory!");
		}
	}	
	
	public void exportTargetDataToXML() throws Exception {
		if (isPostConfiguration) {
			processDataRowsPostGeneric();
		}
		else {
			if (!dataRowsConfigurationList.isEmpty()) {
				
				List<DataRow> list=removeDuplicates(fusionInventoryRow.getInventory().getName(),dataRowsConfigurationList,true,false,false,false);
				
				fusionInventoryRow.setTotalRecords(fusionInventoryRow.getTotalRecords()+list.size());
				fusionInventoryRow.setTotalRecordsConfiguration(fusionInventoryRow.getTotalRecordsConfiguration()+list.size());
				ModelUtils.exportToXML(subFolderConfiguration,fusionInventoryRow.getInventory(),list);		
				validation(fusionInventoryRow,upgradeController.getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().get(
						fusionInventoryRow.getInventoryName()),
						fusionInventoryRow.getInventory(),list);
				dataRowsConfigurationList.clear();
			}
			processDataRowsPostGeneric();
		}
	}

	private List<DataRow> removeDuplicates(String targeInventoryName,List<DataRow> dataRowsList,boolean isConfigurationSet,boolean isPostConfigurationSet,
			boolean isPostImplementationSubPostSet,boolean isPostImplementationSubObsoleteSet) {
		if (!targetInventoryNameToRemoveDuplicatesSet.contains(targeInventoryName)) {
			return dataRowsList;
		}		
		List<DataRow> toReturn=new ArrayList<DataRow>();
		for (DataRow dataRow:dataRowsList) {
			String key=dataRow.getKey();
			if (isConfigurationSet) {
				boolean isPresent=uniqueKeysDataRowsConfigurationSet.contains(key);
				if (!isPresent) {
					toReturn.add(dataRow);
					uniqueKeysDataRowsConfigurationSet.add(key);
				}
			}
			else
				if (isPostConfigurationSet) {
					boolean isPresent=uniqueKeysDataRowsPostConfigurationSet.contains(key);
					if (!isPresent) {
						toReturn.add(dataRow);
						uniqueKeysDataRowsPostConfigurationSet.add(key);
					}
				}
				else
					if (isPostImplementationSubPostSet) {						
						boolean isPresent=uniqueKeysDataRowsPostImplementationSubPostSet.contains(key);
						if (!isPresent) {
							toReturn.add(dataRow);
							uniqueKeysDataRowsPostImplementationSubPostSet.add(key);
						}
					}
					else
						if (isPostImplementationSubObsoleteSet) {
							boolean isPresent=uniqueKeysDataRowsPostImplementationSubObsoleteSet.contains(key);
							if (!isPresent) {
								toReturn.add(dataRow);
								uniqueKeysDataRowsPostImplementationSubObsoleteSet.add(key);
							}
						}
		}
		return toReturn;
	}

	private void processDataRowsPostGeneric() throws Exception {
		if (!fusionInventoryNameToDataRowsPostImplementationSubPostList.isEmpty()) {
			processPostImplementationList(subFolderSubPostImplementation,fusionInventoryNameToDataRowsPostImplementationSubPostList,false);
		}
		if (!fusionInventoryNameToDataRowsPostImplementationSubObsoleteList.isEmpty()) {
			processPostImplementationList(subFolderSubObsoletePostImplementation,fusionInventoryNameToDataRowsPostImplementationSubObsoleteList,true);
		}
		processDataRowsPostConfiguratonList();
	}
	
	private void processDataRowsPostConfiguratonList() throws Exception {
		if (!dataRowsPostConfigurationList.isEmpty()) {
			boolean isDuplicateData=isDuplicateData(fusionInventoryRow.getInventory());
			if (!isDuplicateData) {
				
				List<DataRow> listNoDuplicates=
						removeDuplicates(fusionInventoryRow.getInventory().getName(),dataRowsConfigurationList,false,true,false,false);					
			
				fusionInventoryRow.setTotalRecords(fusionInventoryRow.getTotalRecords()+listNoDuplicates.size());
				fusionInventoryRow.setTotalRecordsPostConfiguration(
						fusionInventoryRow.getTotalRecordsPostConfiguration()+listNoDuplicates.size());
				ModelUtils.exportToXML(subFolderPostConfiguration,fusionInventoryRow.getInventory(),listNoDuplicates);		
				validation(fusionInventoryRow,
						upgradeController.getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().get(fusionInventoryRow.getInventoryName()),
						fusionInventoryRow.getInventory(),listNoDuplicates);
			}
			dataRowsPostConfigurationList.clear();
		}
	}
	
	private boolean isDuplicateData(Inventory inventory) {
		if (!inventory.getName().startsWith(RSC_TABLES_PREFIX)) {
			return false;
		}
		boolean hasRscTable=rscTableNameAlreadyProcessedSet.contains(inventory.getName());
		if (isNewLoop) {
			isNewLoop=false;
			if (hasRscTable) {
				return true;
			}
			rscTableNameAlreadyProcessedSet.add(inventory.getName());
		}
		return false;
	}

	private void processPostImplementationList(File targetFolder,Map<String,List<DataRow>> map, boolean isObsolete) throws Exception {
		Iterator<String> iterator=map.keySet().iterator();
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			List<DataRow> list=map.get(inventoryName);

			if (!list.isEmpty()) {
				Map<String, List<FusionInventoryRow>> inventoryNameToFusionInventoryRowsMap=upgradeController.getUpgradeFrame().getTabUpgradeMainPanel().
						getFusionInventoryGridPanel().getInventoryNameToFusionInventoryRowsMap();
				List<FusionInventoryRow> targetFusionInventoryRows=inventoryNameToFusionInventoryRowsMap.get(inventoryName);
				if (targetFusionInventoryRows==null || targetFusionInventoryRows.isEmpty()) {
					throw new Exception("Cannot find the inventory: '"+inventoryName+"'");
				}
				// pick the first one which was selected for conversion
				FusionInventoryRow targetFusionInventoryRow=null;
				for (FusionInventoryRow fusionInventoryRowTempLoop:targetFusionInventoryRows) {
					boolean isSelected=upgradeController.getUpgradeFrame().getTabUpgradeMainPanel().
					getFusionInventoryGridPanel().isFusionInventorySelected(fusionInventoryRowTempLoop.getGridIndex());
					if (isSelected) {
						targetFusionInventoryRow=fusionInventoryRowTempLoop;
						break;
					}
				}
				if (targetFusionInventoryRow==null) {
					throw new Exception("Internal error: cannot find any selected Fusion inventory in the grid for the name: '"+inventoryName+"'");
				}
				Inventory targetInventory=FileUtils.getInventory(targetFusionInventoryRow.getFusionInventoryInformation().getTempInventoryFile(),
						inventoryName);
				boolean isDuplicateData=isDuplicateData(targetInventory);
				if (!isDuplicateData) {
					postImplementationFusionInventoryNameToFusionInventoryRowMap.put(targetFusionInventoryRow.getInventoryName(),targetFusionInventoryRow);
					
					List<DataRow> listNoDuplicates=
							removeDuplicates(fusionInventoryRow.getInventory().getName(),list,false,false,!isObsolete,isObsolete);					
				
					if (isObsolete) {
						/*
						 * Many fusion tasks may write the same obsolete data into the same RSC table
						 * so we count only once to avoid duplicates count.
						 */
						if (targetFusionInventoryRow.getTotalRecordsPostImplementationObsolete()==0) {
							targetFusionInventoryRow.setTotalRecordsPostImplementationObsolete(
								targetFusionInventoryRow.getTotalRecordsPostImplementationObsolete()+listNoDuplicates.size());
							targetFusionInventoryRow.setTotalRecords(targetFusionInventoryRow.getTotalRecords()+listNoDuplicates.size());
						}
					}
					else {
						targetFusionInventoryRow.setTotalRecordsPostImplementation(
								targetFusionInventoryRow.getTotalRecordsPostImplementation()+listNoDuplicates.size());
						targetFusionInventoryRow.setTotalRecords(targetFusionInventoryRow.getTotalRecords()+listNoDuplicates.size());
					}				
									
					if (isObsolete) {
						/*
						 * Many fusion tasks may write the same obsolete data into the same RSC table
						 * so we write only once to avoid duplicates.
						 */
						File file =new File(targetFolder,targetInventory.getName()+".xml");
						if (!file.exists()) {
							ModelUtils.exportToXML(targetFolder,targetInventory,listNoDuplicates);
						}
					}
					else {
						ModelUtils.exportToXML(targetFolder,targetInventory,listNoDuplicates);
					}
					validation(targetFusionInventoryRow,
							upgradeController.getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap().get(targetInventory.getName()),
							targetInventory,listNoDuplicates);
				}				
				list.clear();
			}
		}
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
			boolean isSourceInventorySelected=upgradeController.isEBSInventorySelected(inventoryName);
			if (!isSourceInventorySelected) {
				return new ArrayList<String[]>();
			}
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=upgradeController.getEbsInventoryNameToSnapshotInventoryGridRecordMap().get(inventoryName);
			if (snapshotInventoryGridRecord==null) {
				throw new Exception("Cannot find the EBS inventory: '"+inventoryName+"'");
			}
			if (snapshotInventoryGridRecord.getStatus()==null || !ModelUtils.getUIStatusFromDBStatus(
					snapshotInventoryGridRecord.getStatus()).equals(UIConstants.UI_STATUS_COMPLETED) ) {
				String stat=(snapshotInventoryGridRecord.getStatus()==null)?"":ModelUtils.getUIStatusFromDBStatus(
						snapshotInventoryGridRecord.getStatus());
				throw new Exception("Invalid EBS inventory snapshot status: '"+inventoryName+"' status: '"+stat+"'");
			}
			File sourceInventoryFile=upgradeController.getUpgradeFrame().getTabUpgradeMainPanel().getEbsInventoryNameToInventoryFileMap().
					get(inventoryName);
			if (sourceInventoryFile==null) {
				throw new Exception("Unable to find the inventory file: '"+inventoryName+"'");
			}
			Inventory sourceInventory=FileUtils.getInventory(sourceInventoryFile,inventoryName);
			snapshotInventoryGridRecord.setInventory(sourceInventory);
			
			List<String[]> dataRows=upgradeController.downloadEBSInventoryData(
					upgradeWorker,snapshotInventoryGridRecord,fusionInventoryRow,
					upgradeController.getUpgradeFrame().getTabUpgradeMainPanel().getSnapshotGridRecord());
			//FileUtils.println("getSourceCodeToDataRowsMap, dataRows:"+dataRows.size());
			
			return dataRows;
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
	
	public Map<String, Integer> getTargetInventoryNameToPosition(String inventoryName) throws Exception {
		validateTargetName(inventoryName);
		
		Map<String, Integer> fieldNameToPositionMap=fusionInventoryToFieldPositionMap.get(inventoryName);
		if (fieldNameToPositionMap==null) {
			Map<String, List<FusionInventoryRow>> inventoryNameToFusionInventoryRowsMap=upgradeController.getUpgradeFrame().getTabUpgradeMainPanel().
					getFusionInventoryGridPanel().getInventoryNameToFusionInventoryRowsMap();
			List<FusionInventoryRow> fusionInventoryRows=inventoryNameToFusionInventoryRowsMap.get(inventoryName);
			if (fusionInventoryRows==null || fusionInventoryRows.isEmpty()) {
				throw new Exception("Cannot find the Fusion inventory for: '"+inventoryName+"'");
			}
			FusionInventoryRow fusionInventoryRowTemp=fusionInventoryRows.get(0);
			Inventory inventory=FileUtils.getInventory(fusionInventoryRowTemp.getFusionInventoryInformation().getTempInventoryFile(),
					fusionInventoryRowTemp.getInventoryName());
			addFieldNamePositionMap(inventory);
			fieldNameToPositionMap=fusionInventoryToFieldPositionMap.get(inventoryName);
		}
		return fieldNameToPositionMap;	
	}
	
	private void addFieldNamePositionMap(Inventory inventory) {
		Map<String, Integer> fieldNameToPositionMap=new HashMap<String, Integer>();
		fusionInventoryToFieldPositionMap.put(inventory.getName(), fieldNameToPositionMap);

		List<String> fieldNamesUsedForDataEntry=inventory.getFieldNamesUsedForDataEntry();
		int position=0;
		for (String fieldName:fieldNamesUsedForDataEntry) {
			fieldNameToPositionMap.put(fieldName, position);
			position++;
		}
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
				SnapshotInventoryGridRecord snapshotInventoryGridRecord=upgradeController.getEbsInventoryNameToSnapshotInventoryGridRecordMap().get(inventoryName);
				if (snapshotInventoryGridRecord==null) {
					throw new Exception("The EBS inventory '"+inventoryName+"' was not selected.");
				}
				Inventory inventory=snapshotInventoryGridRecord.getInventory();
				fieldToPositionMap=new HashMap<String, Integer>();
				List<String> fieldNamesUsedForDataEntry=inventory.getFieldNamesUsedForDataEntry();
				int position=0;
				for (String fieldName:fieldNamesUsedForDataEntry) {
					fieldToPositionMap.put(fieldName, position);
					position++;
				}
				/*
				 * <c>RSC Data Label</c>
				 * <c>Navigation Filter</c>
				 * 
				 * Adding the navigation filter as it contains DFF field names
				 */
				position++; 
				fieldToPositionMap.put(NAVIGATION_MAPPER_FIELD_NAME, position);
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

	public UpgradeController getUpgradeController() {
		return upgradeController;
	}

	public FusionInventoryRow getFusionInventoryRow() {
		return fusionInventoryRow;
	}
	
	public static void validation(FusionInventoryRow fusionInventoryRow,
			Set<String> columnNamesToIgnore,Inventory inventory,List<DataRow> dataRows)	throws Exception {
		List<Field> fieldsUsedForDataEntry=inventory.getFieldsUsedForDataEntry();	
		
		List<Integer> mandatoryFieldIndexList=new ArrayList<Integer>();
		Map<Integer,Set<String>> fieldIndexToSubstitutionsMap=new TreeMap<Integer,Set<String>>();
		int colIndex=0;
		for ( Field field : fieldsUsedForDataEntry ) {
			if (fieldsUsedForDataEntry.get(colIndex).getMandatory()) {
				if (columnNamesToIgnore!=null && columnNamesToIgnore.contains(field.getName())) {
					// to ignore
				}
				else {
					mandatoryFieldIndexList.add(colIndex);
				}
			}
			Set<String> substitutionKeysSet=ModelUtils.getSubstitutionKeysSet(field.getSubstitution());
			if (!substitutionKeysSet.isEmpty()) {
				fieldIndexToSubstitutionsMap.put(colIndex, substitutionKeysSet);
			}
			colIndex++;
		}
		
		int totalRecordsMissingDataForRequiredColumns=0;
		int totalRecordsInvalidValues=0;
		for ( DataRow dataRow:dataRows ){
			String[] dataValues=dataRow.getDataValues();			
			boolean hasMissingDataForRequiredColumns=false;
			boolean hasInvalidValues=false;
			for ( Integer mandatoryFieldIndex : mandatoryFieldIndexList ) {
				String value=dataValues[mandatoryFieldIndex];
				if ( value == null || value.isEmpty()){
					hasMissingDataForRequiredColumns=true;
					break;
				}
			}
			
			Iterator<Integer> iterator=fieldIndexToSubstitutionsMap.keySet().iterator();
			while (iterator.hasNext()) {
				Integer fieldIndex=iterator.next();
				String value=dataValues[fieldIndex];
				if (value!=null && !value.isEmpty()) {
					Set<String> substitutionKeysSet=fieldIndexToSubstitutionsMap.get(fieldIndex);
					boolean hasValue=substitutionKeysSet.contains(value);
					if (!hasValue) {
						hasInvalidValues=true;
						break;
					}
				}	
			}
			if (hasMissingDataForRequiredColumns) {
				totalRecordsMissingDataForRequiredColumns++;
			}
			if (hasInvalidValues) {
				totalRecordsInvalidValues++;
			}
		}
		fusionInventoryRow.setTotalRecordsMissingRequiredColumns(
				fusionInventoryRow.getTotalRecordsMissingRequiredColumns()+totalRecordsMissingDataForRequiredColumns);
		fusionInventoryRow.setTotalRecordsInvalidValues(
				fusionInventoryRow.getTotalRecordsInvalidValues()+totalRecordsInvalidValues);
	}

}