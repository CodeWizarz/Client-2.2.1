package com.rapidesuite.snapshot.controller.upgrade;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.dataConversion0000.ColumnType;
import com.rapidesuite.client.dataConversion0000.ColumnsType;
import com.rapidesuite.client.dataConversion0000.DataConversionDocument;
import com.rapidesuite.client.dataConversion0000.DataConversionType;
import com.rapidesuite.client.dataConversion0000.ElseType;
import com.rapidesuite.client.dataConversion0000.FieldDefinitionType;
import com.rapidesuite.client.dataConversion0000.FieldValueType;
import com.rapidesuite.client.dataConversion0000.FieldValuesType;
import com.rapidesuite.client.dataConversion0000.FieldsDefinitionType;
import com.rapidesuite.client.dataConversion0000.FromLeftSourceType;
import com.rapidesuite.client.dataConversion0000.FromRightSourceType;
import com.rapidesuite.client.dataConversion0000.FromType;
import com.rapidesuite.client.dataConversion0000.IfThenElseType;
import com.rapidesuite.client.dataConversion0000.LoopRecordsType;
import com.rapidesuite.client.dataConversion0000.MapType;
import com.rapidesuite.client.dataConversion0000.MappingsType;
import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.client.dataConversion0000.OperationsType;
import com.rapidesuite.client.dataConversion0000.RowType;
import com.rapidesuite.client.dataConversion0000.RowsType;
import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.client.dataConversion0000.SourceType.Type.Enum;
import com.rapidesuite.client.dataConversion0000.TargetType;
import com.rapidesuite.client.dataConversion0000.ThenType;
import com.rapidesuite.client.dataConversion0000.ValueType;
import com.rapidesuite.client.dataConversion0000.ViewType;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.NavigatorNodePath;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;
import com.rapidesuite.snapshot.view.upgrade.FusionInventoryRow;

public class UpgradeFusionReportWorker  extends GenericWorker {
		
	private TreeMap<String, String> variableNameDefaultValuesMap;
	private FusionInventoryRow fusionInventoryRow;
	private UpgradeFusionReportController upgradeFusionReportController;
	private Map<String, Map<String, String>> fusionInventoryNameToColumnNameDefaultValueMap;

	public UpgradeFusionReportWorker(UpgradeFusionReportController upgradeFusionReportController) {
		super(upgradeFusionReportController,true);
	}

	@Override
	public void execute(Object task) {
		fusionInventoryRow=(FusionInventoryRow)task;
		upgradeFusionReportController=(UpgradeFusionReportController)super.getGenericController();
		try {
			fusionInventoryRow.setReportStatus(UIConstants.UI_STATUS_PROCESSING);
			fusionInventoryRow.setDownloadStartTime(System.currentTimeMillis());
			//FileUtils.println("fusionInventoryRow: "+fusionInventoryRow.getInventoryName());
			execute();
		
			if (upgradeFusionReportController.isCancelled()) {
				fusionInventoryRow.setReportStatus(UIConstants.UI_STATUS_CANCELLED);
			} 
			else {
				fusionInventoryRow.setReportStatus(UIConstants.UI_STATUS_COMPLETED);
			}
		}
		catch(Throwable e) {
			FileUtils.printStackTrace(e);
			FileUtils.println("e.getMessage(): "+e.getMessage());
			fusionInventoryRow.setReportRemarks(e.getMessage());
			fusionInventoryRow.setReportStatus(UIConstants.UI_STATUS_FAILED);
			super.setTotalFailedTasks(super.getTotalFailedTasks()+1);
		}
		finally  {
			setDownloadTime(fusionInventoryRow);
		}
	}

	public static void setDownloadTime(FusionInventoryRow fusionInventoryRow) {
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(fusionInventoryRow.getDownloadStartTime(),currentTime);
		fusionInventoryRow.setDownloadTime(msg);
		fusionInventoryRow.setDownloadRawTimeInSecs(UIUtils.getRawTimeInSecs(fusionInventoryRow.getDownloadStartTime()));
	}
	
	/*
	 * - Get all the operations of type 'condition' and store the variables with their text values
	 * - Iterate all target tags and associate the variables with the Fusion column names. 
	 */
	private void execute() throws Exception {	
		UpgradeFusionReportController upgradeFusionReportController=(UpgradeFusionReportController)super.getGenericController();
		File mappingFile=fusionInventoryRow.getFusionInventoryInformation().getMappingFile();
		if (mappingFile==null) {
			fusionInventoryRow.setReportRemarks("No mapping file!");
		}
		else {
			//FileUtils.println("mappingFile:"+mappingFile.getAbsolutePath());

			DataConversionDocument dataConversionDocument=ModelUtils.getDataConversionDocument(mappingFile);
			DataConversionType dataConversionType=dataConversionDocument.getDataConversion();
			
			fusionInventoryNameToColumnNameDefaultValueMap=upgradeFusionReportController.getFusionInventoryNameToColumnNameDefaultValueMap();
			
			processOperationsType(dataConversionType);
			processSourceRowsType(dataConversionType);
		}
	}
	
	private void processSourceRowsType(DataConversionType dataConversionType) {
		Map<String, Map<String, Set<String>>> sourceCodeToFieldNameValuesMap=new HashMap<String, Map<String, Set<String>>>();
		SourceType[] sourceTypeArray=dataConversionType.getSourceArray();
		for (SourceType sourceType:sourceTypeArray) {
			Enum type=sourceType.getType();
			if (type.equals(SourceType.Type.ROWS)) {
				Map<String, Set<String>> fieldNameToValuesMap=new HashMap<String, Set<String>>();
				Map<Integer,String> fieldPositionToNameMap=new HashMap<Integer,String>();
				sourceCodeToFieldNameValuesMap.put(sourceType.getCode(),fieldNameToValuesMap);
				
				FieldsDefinitionType fieldsDefinitionType=sourceType.getFieldsDefinition();
				FieldDefinitionType[] fieldDefinitionTypeArray=fieldsDefinitionType.getFieldDefinitionArray();
				int position=0;
				for (FieldDefinitionType fieldDefinitionType:fieldDefinitionTypeArray) {
					String name=fieldDefinitionType.getName();
					Set<String> values=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
					fieldNameToValuesMap.put(name,values);
					fieldPositionToNameMap.put(position++,name);
				}
				RowsType rowsType=sourceType.getRows();
				RowType[] rowTypeArray=rowsType.getRowArray();
				for (RowType rowType:rowTypeArray) {
					FieldValuesType fieldValuesType=rowType.getFieldValues();
					FieldValueType[] fieldValueTypeArray=fieldValuesType.getFieldValueArray();
					position=0;
					for (FieldValueType fieldValueType:fieldValueTypeArray) {
						String value=fieldValueType.getValue();
						String fieldName=fieldPositionToNameMap.get(position++);
						Set<String> values=fieldNameToValuesMap.get(fieldName);
						if (values==null) {
							values=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
							fieldNameToValuesMap.put(fieldName,values);
						}
						values.add(value);
					}
				}
			}
			if (type.equals(SourceType.Type.VIEW)) {
				ViewType viewType=sourceType.getView();
				FromType fromType=viewType.getFrom();
				FromLeftSourceType fromLeftSourceType=fromType.getFromLeftSource();
				String leftSourceCode=fromLeftSourceType.getCode();
				FromRightSourceType fromRightSourceType=fromType.getFromRightSource();
				String rightSourceCode=fromRightSourceType.getCode();
				
				Map<String, Set<String>> leftFieldNameValuesMap=sourceCodeToFieldNameValuesMap.get(leftSourceCode);
				Map<String, Set<String>> rightFieldNameValuesMap=sourceCodeToFieldNameValuesMap.get(rightSourceCode);
				if (leftFieldNameValuesMap==null && rightFieldNameValuesMap==null) {
					continue;
				}
				
				ColumnsType columnsType=viewType.getColumns();
				ColumnType[] columnTypeArray=columnsType.getColumnArray();
				Map<String, Set<String>> fieldNameToValuesMap=new HashMap<String, Set<String>>();
				boolean hasJoinRow=false;
				for (ColumnType columnType:columnTypeArray) {
					String outputName=columnType.getOutputName();
					String columnName=columnType.getName();
					com.rapidesuite.client.dataConversion0000.ColumnType.Direction.Enum direction=columnType.getDirection();
					if (direction.equals(ColumnType.Direction.LEFT)) {
						if (leftFieldNameValuesMap!=null) {
							Set<String> values=leftFieldNameValuesMap.get(columnName);
							fieldNameToValuesMap.put(outputName,values);
							hasJoinRow=true;
						}
					}
					else {
						if (rightFieldNameValuesMap!=null) {
							Set<String> values=rightFieldNameValuesMap.get(columnName);
							fieldNameToValuesMap.put(outputName,values);
							hasJoinRow=true;
						}
					}
				}
				if (hasJoinRow) {
					sourceCodeToFieldNameValuesMap.put(sourceType.getCode(),fieldNameToValuesMap);
				}
			}
		}
		TreeMap<String, String> columnNameDefaultValueMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		LoopRecordsType[] loopRecordsTypeArray=dataConversionType.getLoopRecordsArray();
		for (LoopRecordsType loopRecordsType:loopRecordsTypeArray) {
			String sourceCode=loopRecordsType.getSourceCode();
			Map<String, Set<String>> fieldNameValuesMap=sourceCodeToFieldNameValuesMap.get(sourceCode);
			if (fieldNameValuesMap!=null) {
				TargetType[] targetTypeArray=loopRecordsType.getTargetArray();
				for (TargetType targetType:targetTypeArray) {
					MappingsType mappingsType=targetType.getMappings();
					MapType[] mapTypeArray=mappingsType.getMapArray();
					if (mapTypeArray==null) {
						return;
					}
					for (MapType mapType:mapTypeArray) {
						String mapSourceName=mapType.getSourceName();
						String mapTargetName=mapType.getTargetName();
						Set<String> values=fieldNameValuesMap.get(mapSourceName);
						if (values!=null && !values.isEmpty()) {
							columnNameDefaultValueMap.put(mapTargetName, values.toString().toString().replace("[","").replace("]",""));
						}
					}	
				}
			}
		}
		updateResultMap(fusionInventoryRow.getInventoryName(), columnNameDefaultValueMap);
	}
	
	private void updateResultMap(String inventoryName,TreeMap<String, String> columnNameDefaultValueMap) {
		Map<String, String> map=fusionInventoryNameToColumnNameDefaultValueMap.get(inventoryName);
		if (map==null) {
			map=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
			fusionInventoryNameToColumnNameDefaultValueMap.put(inventoryName,map);
		}
		map.putAll(columnNameDefaultValueMap);
	}

	private void processOperationsType(DataConversionType dataConversionType) throws Exception {
		variableNameDefaultValuesMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		LoopRecordsType[] loopRecordsTypeArray=dataConversionType.getLoopRecordsArray();
		for (LoopRecordsType loopRecordsType:loopRecordsTypeArray) {
			OperationsType operationsType=loopRecordsType.getOperations();
			if (operationsType!=null) {
				OperationType[] operationTypeArray=operationsType.getOperationArray();
				for (OperationType operationType:operationTypeArray) {
					com.rapidesuite.client.dataConversion0000.OperationType.Type.Enum type=operationType.getType();
					
					if (type.equals(OperationType.Type.CONDITION)) {
						Set<String> defaultValuesSet=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
						IfThenElseType ifThenElseType=operationType.getIfThenElse();
						retrieveDefaultValuesFromConditionOperation(ifThenElseType,defaultValuesSet);
						String outVariable=operationType.getOutVariable();
						if (outVariable!=null && !defaultValuesSet.isEmpty()) {
							String defaultValuesStr=defaultValuesSet.toString().replace("[","").replace("]","");
							variableNameDefaultValuesMap.put(outVariable,defaultValuesStr );
							//FileUtils.println("outVariable:"+outVariable+" defaultValuesSet: "+defaultValuesSet.toString());
						}
					}
				}
			}
		}
		if (variableNameDefaultValuesMap.isEmpty()) {
			return;
		}
		
		TreeMap<String, String> columnNameDefaultValueMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		loopRecordsTypeArray=dataConversionType.getLoopRecordsArray();
		for (LoopRecordsType loopRecordsType:loopRecordsTypeArray) {
			IfThenElseType[] ifThenElseTypeArray=loopRecordsType.getIfThenElseArray();
			if (ifThenElseTypeArray!=null) {
				for (IfThenElseType ifThenElseType:ifThenElseTypeArray) {
					XmlObject[] xmlObjects=ifThenElseType.selectPath("*");
					analyzeIfMetadata(xmlObjects,columnNameDefaultValueMap);
				}
			}
			
			TargetType[] targetTypeArray=loopRecordsType.getTargetArray();
			for (TargetType targetType:targetTypeArray) {
				analyzeTargetTypeMetadata(targetType,columnNameDefaultValueMap);
			}
		}
		if (columnNameDefaultValueMap.isEmpty()) {
			return;
		}
		
		Inventory inventory=fusionInventoryRow.getInventory();
		fusionInventoryRow.getFusionInventoryInformation().setTaskName(UIConstants.UI_NA);
		if (inventory==null) {
			Map<String, List<NavigatorNodePath>> functionIdToFunctionIdNavigatorNodesMap=upgradeFusionReportController.getUpgradeFrame().getTabUpgradeMainPanel().getFunctionIdToFunctionIdNavigatorNodesMap();
			if (functionIdToFunctionIdNavigatorNodesMap!=null) {
				inventory=FileUtils.getInventory(fusionInventoryRow.getFusionInventoryInformation().getTempInventoryFile(),fusionInventoryRow.getInventoryName());
				String functionIds=inventory.getFunctionIds();
				if (functionIds!=null && !functionIds.isEmpty() && !functionIds.contains(",")) {
					List<NavigatorNodePath> functionIdNavigatorNodeList=functionIdToFunctionIdNavigatorNodesMap.get(functionIds);
					if (!functionIdNavigatorNodeList.isEmpty()) {
						fusionInventoryRow.getFusionInventoryInformation().setTaskName(functionIdNavigatorNodeList.get(0).getFormName());
					}
					fusionInventoryRow.setInventory(inventory);
				}
			}
		}
		updateResultMap(fusionInventoryRow.getInventoryName(), columnNameDefaultValueMap);
	}

	private void analyzeIfMetadata(XmlObject[] xmlObjects,TreeMap<String, String> columnNameDefaultValueMap) throws Exception {
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);

		XmlObject xmlObjectThen=xmlObjectsSubList.get(1);
		XmlObject xmlObjectElse=xmlObjectsSubList.get(2);

		analyzeIfThenElseMetadata(xmlObjectThen,columnNameDefaultValueMap);
		analyzeIfThenElseMetadata(xmlObjectElse,columnNameDefaultValueMap);
	}
	
	private void analyzeIfThenElseMetadata(XmlObject xmlObject,TreeMap<String, String> columnNameDefaultValueMap) throws Exception {
		XmlObject[] xmlInnerObjects=xmlObject.selectPath("*");
		List<XmlObject> xmlObjectsList=Arrays.asList(xmlInnerObjects);
		for (XmlObject xmlObjectTemp:xmlObjectsList) {
			if (xmlObjectTemp instanceof IfThenElseType) {
				XmlObject[] xmlInnerObjectsTemp=xmlObjectTemp.selectPath("*");
				analyzeIfMetadata(xmlInnerObjectsTemp,columnNameDefaultValueMap);
			}
			else
				if (xmlObjectTemp instanceof TargetType) {
					analyzeTargetTypeMetadata( (TargetType)xmlObjectTemp,columnNameDefaultValueMap);
				}
		}
	}
	
	private void analyzeTargetTypeMetadata(TargetType targetType,TreeMap<String, String> columnNameDefaultValueMap) throws Exception {
		MappingsType mappingsType=targetType.getMappings();
		MapType[] mapTypeArray=mappingsType.getMapArray();
		if (mapTypeArray==null) {
			return;
		}
		for (MapType mapType:mapTypeArray) {
			com.rapidesuite.client.dataConversion0000.MapType.SourceType.Enum mapSourceType=mapType.getSourceType();
			if (mapSourceType.equals(MapType.SourceType.VARIABLE)) {
				String mapSourceName=mapType.getSourceName();
				String mapTargetName=mapType.getTargetName();
				String defaultValues=variableNameDefaultValuesMap.get(mapSourceName);
				//FileUtils.println("mapSourceName:"+mapSourceName+" mapTargetName:"+mapTargetName+" defaultValues:"+defaultValues);
				if (defaultValues!=null && !defaultValues.isEmpty()) {
					columnNameDefaultValueMap.put(mapTargetName, defaultValues);
				}
			}
		}	
	}
	
	private void retrieveDefaultValuesFromConditionOperation(IfThenElseType ifThenElseType,Set<String> defaultValuesSet) {
		ThenType thenType=ifThenElseType.getThen();
		IfThenElseType[] ifThenElseArrayThen=thenType.getIfThenElseArray();
		if (ifThenElseArrayThen!=null && ifThenElseArrayThen.length!=0) {
			for (IfThenElseType ifThenElseTypeElt:ifThenElseArrayThen) {
				retrieveDefaultValuesFromConditionOperation(ifThenElseTypeElt,defaultValuesSet);
			}
		}	
		ValueType valueType=thenType.getValue();
		if (valueType!=null && valueType.getType().equals(ValueType.Type.TEXT) && valueType.getIsDefaultValue() ) {
			 String defaultValue=valueType.getText();
			 if (defaultValue==null || defaultValue.isEmpty()) {
				 defaultValue="'Empty'";
			 }
			 defaultValuesSet.add(defaultValue);
		}
		
		ElseType elseType=ifThenElseType.getElse();
		IfThenElseType[] ifThenElseArrayElse=elseType.getIfThenElseArray();
		if (ifThenElseArrayElse!=null && ifThenElseArrayElse.length!=0) {
			for (IfThenElseType ifThenElseTypeElt:ifThenElseArrayElse) {
				retrieveDefaultValuesFromConditionOperation(ifThenElseTypeElt,defaultValuesSet);
			}
		}	
		valueType=elseType.getValue();
		if (valueType!=null && valueType.getType().equals(ValueType.Type.TEXT) && valueType.getIsDefaultValue() ) {
			 String defaultValue=valueType.getText();
			 if (defaultValue==null || defaultValue.isEmpty()) {
				 defaultValue="'Empty'";
			 }
			 defaultValuesSet.add(defaultValue);
		}
	}
	
}