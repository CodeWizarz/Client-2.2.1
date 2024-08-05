package com.rapidesuite.snapshot.controller.convert.constructs;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.dataConversion0000.IfThenElseType;
import com.rapidesuite.client.dataConversion0000.LoopRecordsType;
import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.client.dataConversion0000.OperationsType;
import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.client.dataConversion0000.TargetType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.view.convert.ConvertSourceGridRecordInformation;

public class LoopRecordsTypeConstruct  extends GenericConstruct{

	public LoopRecordsTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(LoopRecordsType loopRecordsType) throws Exception {
		String sourceCode=loopRecordsType.getSourceCode();
		BigInteger startRecord=loopRecordsType.getStartRecord();
		BigInteger endRecord=loopRecordsType.getEndRecord();
		
		List<String[]> sourceDataRows=convertEngine.getSourceCodeToDataRowsMap(sourceCode);
		Map<String, Integer> fieldNameToPositionMap=convertEngine.getFieldNameToPositionMap(sourceCode);
		
		int sourceDataRowIndex=0;
		ConvertSourceGridRecordInformation convertSourceGridRecordInformation=convertEngine.getConvertSourceGridRecordInformation();
		SourceType sourceType=convertEngine.getSourceCodeToSourceTypeMap().get(sourceCode);
		if (convertSourceGridRecordInformation.getInventoryName().equalsIgnoreCase(sourceType.getName())) {
			convertSourceGridRecordInformation.setDataRows(sourceDataRows);
		}
		Map<String,String> variableToValueMap=new HashMap<String,String>();
		int index=0;
		int EXPORT_BATCH_SIZE=50000;
		int counter=0;
		
		int startSourceRecordIndex=0;
		int endSourceRecordIndex=sourceDataRows.size();
		if (startRecord!=null) {
			startSourceRecordIndex=startRecord.intValue()-1; // starting at 0
		}
		if (endRecord!=null) {
			endSourceRecordIndex=endRecord.intValue();
		}
		
		for (int i=startSourceRecordIndex;i<endSourceRecordIndex;i++) {
			String[] sourceDataRow=sourceDataRows.get(i);
			if (index % 1000==0) {
				//System.out.println(index+" / "+sourceLeftDataRows.size());
				convertSourceGridRecordInformation.setRemarks("Converting ("+Utils.formatNumberWithComma(index)+
						" / "+Utils.formatNumberWithComma(sourceDataRows.size())+")");
			}
			index++;
			
			IfThenElseType[] ifThenElseTypeArray=loopRecordsType.getIfThenElseArray();
			TargetType[] targetTypeArray=loopRecordsType.getTargetArray();
			
			OperationsType operationsType=loopRecordsType.getOperations();
			if (operationsType!=null) {
				OperationType[] operationTypeArray=operationsType.getOperationArray();
				processOperations(convertEngine,operationTypeArray,sourceDataRow,variableToValueMap,fieldNameToPositionMap,sourceCode);
			}
			genericProcessing(convertEngine,ifThenElseTypeArray,targetTypeArray,
					sourceCode,fieldNameToPositionMap,sourceDataRow,sourceDataRowIndex,variableToValueMap);
			
			sourceDataRowIndex++;
			
			counter++;
			if (counter % EXPORT_BATCH_SIZE == 0 ) {
				for (TargetType targetType:targetTypeArray) {
					String targetName=targetType.getName();
					convertEngine.exportTargetDataToXML(targetName);
				}
			}
		}
	}
	
	public static void genericProcessing(ConvertEngine convertEngine,IfThenElseType[] ifThenElseTypeArray,TargetType[] targetTypeArray,
			String loopRecordSourceCode,Map<String, Integer> fieldNameToPositionMap,String[] sourceDataRow,int sourceDataRowIndex,
			Map<String,String> variableToValueMap) throws Exception {
		if (ifThenElseTypeArray!=null) {
			for (IfThenElseType ifThenElseType:ifThenElseTypeArray) {
				IfThenElseTypeConstruct ifThenElseTypeConstruct=new IfThenElseTypeConstruct(convertEngine);
				ifThenElseTypeConstruct.process(ifThenElseType,loopRecordSourceCode,fieldNameToPositionMap,sourceDataRow,
						sourceDataRowIndex,variableToValueMap);
			}
		}
				
		for (TargetType targetType:targetTypeArray) {
			TargetTypeConstruct targetTypeConstruct=new TargetTypeConstruct(convertEngine);
			DataRow	targetDataRow=targetTypeConstruct.process(loopRecordSourceCode,fieldNameToPositionMap,targetType,sourceDataRow,variableToValueMap);
			convertEngine.storeData(targetType,targetDataRow);
		}
	}
	
	public static void processOperations(ConvertEngine convertEngine,OperationType[] operationTypeArray,String[] sourceDataRow,
			Map<String,String> variableToValueMap,Map<String, Integer> fieldNameToPositionMap,String sourceCode) throws Exception {
		for (OperationType operationType:operationTypeArray) {
			com.rapidesuite.client.dataConversion0000.OperationType.Type.Enum type=operationType.getType();
			if (type.equals(OperationType.Type.REPLACE)) {
				OperationReplaceConstruct operationReplaceConstruct=new OperationReplaceConstruct(convertEngine);
				operationReplaceConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.DECODE)) {
				OperationDecodeConstruct operationDecodeConstruct=new OperationDecodeConstruct(convertEngine);
				operationDecodeConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.SUBSTRING)) {
				OperationSubStringConstruct operationSubStringConstruct=new OperationSubStringConstruct(convertEngine);
				operationSubStringConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.MATH_ADDITION)) {
				OperationMathAdditionConstruct operationMathAdditionConstruct=new OperationMathAdditionConstruct(convertEngine);
				operationMathAdditionConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.SPLIT)) {
				OperationSplitConstruct operationSplitConstruct=new OperationSplitConstruct(convertEngine);
				operationSplitConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.CONDITION)) {
				OperationConditionConstruct operationConditionConstruct=new OperationConditionConstruct(convertEngine);
				operationConditionConstruct.process(operationType,sourceCode,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.CONCATENATE)) {
				OperationConcatenateConstruct operationConcatenateConstruct=new OperationConcatenateConstruct(convertEngine);
				operationConcatenateConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.FIRST_INDEX_OF)) {
				OperationIndexOfConstruct operationIndexOfConstruct=new OperationIndexOfConstruct(convertEngine,true);
				operationIndexOfConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.LAST_INDEX_OF)) {
				OperationIndexOfConstruct operationIndexOfConstruct=new OperationIndexOfConstruct(convertEngine,false);
				operationIndexOfConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.LENGTH)) {
				OperationLengthConstruct operationLengthConstruct=new OperationLengthConstruct(convertEngine);
				operationLengthConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.DFF_FIELD_NAME)) {
				OperationDFFfieldNameConstruct operationDFFfieldNameConstruct=new OperationDFFfieldNameConstruct(convertEngine);
				operationDFFfieldNameConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
			else
			if (type.equals(OperationType.Type.UPPER)) {
				OperationUpperCaseConstruct operationUpperCaseConstruct=new OperationUpperCaseConstruct(convertEngine);
				operationUpperCaseConstruct.process(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
			}
		}
	}
	
}