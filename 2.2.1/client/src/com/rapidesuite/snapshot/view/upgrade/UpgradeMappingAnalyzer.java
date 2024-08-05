package com.rapidesuite.snapshot.view.upgrade;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;

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
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.client.common.util.FileUtils;

public class UpgradeMappingAnalyzer {
	
	private HashSet<String> columnNamesInUseSet;

	public UpgradeMappingAnalyzer() {
	}

	public void process(String inventoryName,File mappingFile) throws Exception
	{		
		DataConversionDocument dataConversionDocument=ModelUtils.getDataConversionDocument(mappingFile);
		DataConversionType dataConversionType=dataConversionDocument.getDataConversion();

		FileUtils.println("### UpgradeMappingAnalyzer, inventoryName: "+inventoryName);
		SourceType[] sourceTypeArray=dataConversionType.getSourceArray();
		Set<String> sourceCodeToReviewSet=new HashSet<String>();
		for (SourceType sourceType:sourceTypeArray) {
			com.rapidesuite.client.dataConversion0000.SourceType.Type.Enum sourceTypeAttribute=sourceType.getType();
			String sourceCode=sourceType.getCode();
			if (sourceTypeAttribute.equals(SourceType.Type.INVENTORY)) {
				String sourceName=sourceType.getName();
				if (inventoryName.equalsIgnoreCase(sourceName)) {
					sourceCodeToReviewSet.add(sourceCode);
					FileUtils.println("sourceCodeToReviewSet: "+sourceCodeToReviewSet);
				}
			}
		}
		
		columnNamesInUseSet=new HashSet<String>();
		for (SourceType sourceType:sourceTypeArray) {
			com.rapidesuite.client.dataConversion0000.SourceType.Type.Enum sourceTypeAttribute=sourceType.getType();
			if (sourceTypeAttribute.equals(SourceType.Type.VIEW)) {
				ViewType viewType=sourceType.getView();
				FromType fromType=viewType.getFrom();
				FromLeftSourceType fromLeftSourceType=fromType.getFromLeftSource();
				String leftSourceCode=fromLeftSourceType.getCode();
				FromRightSourceType fromRightSourceType=fromType.getFromRightSource();
				String rightSourceCode=fromRightSourceType.getCode();

				if (sourceCodeToReviewSet.contains(leftSourceCode)) {
					Set<String> toReturn=getColumnNamesUsedInView(viewType,true);
					columnNamesInUseSet.addAll(toReturn);
					FileUtils.println("leftSourceCode columnNamesInUseSet: "+columnNamesInUseSet);
				}
				if (sourceCodeToReviewSet.contains(rightSourceCode)) {
					Set<String> toReturn=getColumnNamesUsedInView(viewType,false);
					columnNamesInUseSet.addAll(toReturn);
					FileUtils.println("rightSourceCode columnNamesInUseSet: "+columnNamesInUseSet);
				}
			}
		}
		LoopRecordsType[] loopRecordsTypeArray=dataConversionType.getLoopRecordsArray();
		for (LoopRecordsType loopRecordsType:loopRecordsTypeArray) {
			String sourceCode=loopRecordsType.getSourceCode();
			if (sourceCodeToReviewSet.contains(sourceCode)) {
				OperationsType operationsType=loopRecordsType.getOperations();
				if (operationsType!=null) {
					Set<String> toReturn=getColumnNamesFromOperations(operationsType);
					columnNamesInUseSet.addAll(toReturn);
					FileUtils.println("operationsType columnNamesInUseSet: "+columnNamesInUseSet);
				}
				Set<String> toReturn=getColumnNamesFromTargets(loopRecordsType);
				columnNamesInUseSet.addAll(toReturn);
				FileUtils.println("getColumnNamesFromTargets columnNamesInUseSet: "+columnNamesInUseSet);
			}			
		}
	}
	
	private Set<String> getColumnNamesUsedInView(ViewType viewType,boolean isLeft) throws Exception {
		Set<String> toReturn=new HashSet<String>();
		
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

			toReturn.add(columnType.getName());
		}
		return toReturn;
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

	private Set<String> getColumnNamesFromTargets(LoopRecordsType loopRecordsType) throws Exception {
		Set<String> toReturn=new HashSet<String>();
		
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
					toReturn.add(mapType.getSourceName());
				}
			}
		}
		return toReturn;
	}
	
	private Set<String> getColumnNamesFromOperations(OperationsType operationsType) throws Exception{
		Set<String> toReturn=new HashSet<String>();
		if (operationsType==null) {
			return toReturn;
		}
		OperationType[] operationTypeArray=operationsType.getOperationArray();
		for (OperationType operationType:operationTypeArray) {
			String columnName=operationType.getInColumn();
			if (columnName!=null) {
				toReturn.add(columnName);
			}
		}
		return toReturn;
	}

	public HashSet<String> getColumnNamesInUseSet() {
		return columnNamesInUseSet;
	}
		
}