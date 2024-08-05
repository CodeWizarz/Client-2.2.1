package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.client.dataConversion0000.OperationType.ConcatenateDirection.Enum;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;

public class OperationConcatenateConstruct extends GenericConstruct{

	public OperationConcatenateConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> sourceCodeToPositionMap) throws Exception {
		String outVariable=operationType.getOutVariable();
		if (outVariable==null) {
			throw new Exception("Invalid Mapping - Operation Concatenate: outVariable attribute is missing");
		}
		Enum concatenateDirection=operationType.getConcatenateDirection();
		if (concatenateDirection==null) {
			throw new Exception("Invalid Mapping - Operation Concatenate: concatenate-direction attribute is missing");
		}
		String inColumn=operationType.getInColumn();
		String inVariable=operationType.getInVariable();
		if (inColumn==null && inVariable==null) {
			throw new Exception("Invalid Mapping - Operation Concatenate: either the 'in-column' or 'in-variable' attribute is missing");
		}
		String concatenateText=operationType.getConcatenateText();
		String concatenateColumn=operationType.getConcatenateColumn();
		String concatenateVariable=operationType.getConcatenateVariable();
		if (concatenateText==null && concatenateColumn==null && concatenateVariable==null) {
			throw new Exception("Invalid Mapping - Operation Concatenate: either the 'concatenate-text' or 'concatenate-column' or 'concatenate-variable' attribute is missing");
		}
				
		String sourceDataValue=OperationReplaceConstruct.getSourceValue(operationType,variableToValueMap,sourceDataRow,sourceCodeToPositionMap);
		if (sourceDataValue==null) {
			sourceDataValue="";
		}
		
		String targetValue="";
		if (concatenateText!=null) {
			if (concatenateDirection.equals(OperationType.ConcatenateDirection.BEFORE)) {
				targetValue=concatenateText+sourceDataValue;
			}
			else {
				targetValue=sourceDataValue+concatenateText;
			}
		}
		else {
			if (concatenateColumn!=null) {
				Integer concatenateColumnIndex=sourceCodeToPositionMap.get(concatenateColumn);
				if (concatenateColumnIndex==null) {
					throw new Exception("Invalid Mapping - Operation Concatenate: cannot find the concatenate-column '"+concatenateColumn+"'");
				}
				String concatenateColumnDataValue=sourceDataRow[concatenateColumnIndex];
				if (concatenateDirection.equals(OperationType.ConcatenateDirection.BEFORE)) {
					targetValue=concatenateColumnDataValue+sourceDataValue;
				}
				else {
					targetValue=sourceDataValue+concatenateColumnDataValue;
				}
			}
			else {
				String variableValue=variableToValueMap.get(concatenateVariable);
				if (variableValue==null) {
					throw new Exception("Invalid Mapping - Operation Concatenate: cannot find the value for the concatenate-variable: '"+concatenateColumn+"'");
				}
				if (concatenateDirection.equals(OperationType.ConcatenateDirection.BEFORE)) {
					targetValue=variableValue+sourceDataValue;
				}
				else {
					targetValue=sourceDataValue+variableValue;
				}
			}
			
		}		
		variableToValueMap.put(outVariable,targetValue);
	}
	
}