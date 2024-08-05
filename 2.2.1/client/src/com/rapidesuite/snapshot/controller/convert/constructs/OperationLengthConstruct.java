package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class OperationLengthConstruct extends GenericConstruct{
	
	public OperationLengthConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> fieldNameToPositionMap) throws Exception {
		String inVariable=operationType.getOutVariable();
		String inColumn=operationType.getInColumn();
		String outVariable=operationType.getOutVariable();
		if (inVariable==null && inColumn==null) {
			throw new Exception("Invalid Mapping, operation LENGTH: in-variable or in-column attribute must be present");
		}
		if (outVariable==null) {
			throw new Exception("Invalid Mapping, operation LENGTH: out-variable attribute is missing");
		}
			
		String sourceDataValue=OperationReplaceConstruct.getSourceValue(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
		if (sourceDataValue==null) {
			sourceDataValue="";
		}
		int length=sourceDataValue.length();
		String output=""+length;
		//System.out.println("OperationLengthConstruct, outVariable:"+outVariable+" output:"+output);
		variableToValueMap.put(outVariable,output);
	}
	
}
