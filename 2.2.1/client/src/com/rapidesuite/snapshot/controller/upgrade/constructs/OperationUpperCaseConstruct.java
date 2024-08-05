package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.Map;
import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;

public class OperationUpperCaseConstruct extends GenericConstruct{

	public OperationUpperCaseConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> fieldNameToPositionMap) throws Exception {
		String inVariable=operationType.getOutVariable();
		String inColumn=operationType.getInColumn();
		String outVariable=operationType.getOutVariable();
		if (inVariable==null && inColumn==null) {
			throw new Exception("Invalid Mapping, operation UPPER: in-variable or in-column attribute must be present");
		}
		if (outVariable==null) {
			throw new Exception("Invalid Mapping, operation UPPER: out-variable attribute is missing");
		}
				
		String sourceDataValue=OperationReplaceConstruct.getSourceValue(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
		if (sourceDataValue==null) {
			sourceDataValue="";
		}
		String output=sourceDataValue.toUpperCase();
		variableToValueMap.put(outVariable,output);
	}
	
}