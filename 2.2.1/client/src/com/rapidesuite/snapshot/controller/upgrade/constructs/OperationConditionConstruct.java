package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.ElseType;
import com.rapidesuite.client.dataConversion0000.IfThenElseType;
import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.client.dataConversion0000.ThenType;
import com.rapidesuite.client.dataConversion0000.ValueType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;

public class OperationConditionConstruct extends GenericConstruct{

	public OperationConditionConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(OperationType operationType,String sourceCode, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> fieldNameToPositionMap) throws Exception {
		IfThenElseType ifThenElseType=operationType.getIfThenElse();
		if (ifThenElseType==null) {
			throw new Exception("Invalid Mapping, operation CONDITION: IfThenElse missing from operation");
		}

		processInternal(operationType,ifThenElseType,sourceCode, variableToValueMap, sourceDataRow,fieldNameToPositionMap);
	}
	
	public void processInternal(OperationType operationType,IfThenElseType ifThenElseType,String sourceCode, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> fieldNameToPositionMap) throws Exception {
		String newValue=null;
		IfTypeConstruct ifTypeConstruct=new IfTypeConstruct(upgradeEngine);
		boolean isSuccess=ifTypeConstruct.process(ifThenElseType.getIf(),sourceCode,fieldNameToPositionMap,sourceDataRow,variableToValueMap);
		ValueType valueType=null;
		if (isSuccess) {
			ThenType thenType=ifThenElseType.getThen();
			OperationType[] operationArray=thenType.getOperationArray();
			IfThenElseType[] ifThenElseTypeInternal=thenType.getIfThenElseArray();
			if (ifThenElseTypeInternal!=null && ifThenElseTypeInternal.length>0) {
				processInternal(operationType,ifThenElseTypeInternal[0],sourceCode,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
				return;
			}
			else 
				if (operationArray!=null && operationArray.length>0) {
					LoopRecordsTypeConstruct.processOperations(upgradeEngine,operationArray,sourceDataRow,variableToValueMap,fieldNameToPositionMap,sourceCode);
					return;
				}
				else {
					valueType=thenType.getValue();
					if (valueType==null) {
						throw new Exception("Invalid Mapping, operation CONDITION: empty THEN block");
					}
				}
		}
		else {
			ElseType elseType=ifThenElseType.getElse();
			OperationType[] operationArray=elseType.getOperationArray();
			IfThenElseType[] ifThenElseTypeInternal=elseType.getIfThenElseArray();
			if (ifThenElseTypeInternal!=null && ifThenElseTypeInternal.length>0) {
				processInternal(operationType,ifThenElseTypeInternal[0],sourceCode,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
				return;
			}
			else 
				if (operationArray!=null && operationArray.length>0) {
					LoopRecordsTypeConstruct.processOperations(upgradeEngine,operationArray,sourceDataRow,variableToValueMap,fieldNameToPositionMap,sourceCode);
					return;
				}
				else {
					valueType=elseType.getValue();
					if (valueType==null) {
						throw new Exception("Invalid Mapping, operation CONDITION: empty ELSE block");
					}
				}
		}
		
		if (valueType.getType().equals(ValueType.Type.COLUMN)) {
			Integer columnIndex=fieldNameToPositionMap.get(valueType.getName());
			if (columnIndex==null) {
				throw new Exception("Invalid Mapping, operation CONDITION: cannot find the column '"+valueType.getName()+"' in the source code '"+sourceCode+"'");
			}
			newValue=sourceDataRow[columnIndex];
		}
		else
			if (valueType.getType().equals(ValueType.Type.TEXT)) {
				String text=valueType.getText();
				if (text==null) {
					throw new Exception("Invalid Mapping, operation CONDITION: VALUE tag with no 'text' attribute");
				}
				newValue=text;
			}
			else
				if (valueType.getType().equals(ValueType.Type.VARIABLE)) {
					String tempValue=variableToValueMap.get(valueType.getName());
					if (tempValue==null) {
						throw new Exception("Invalid Mapping, operation CONDITION: cannot find the variable '"+valueType.getName()+"' in the source code '"+sourceCode+"'");
					}
					newValue=tempValue;
				}

		if (newValue==null) {
			newValue="";
		}
		String outVariable=operationType.getOutVariable();
		if (outVariable==null) {
			throw new Exception("Invalid Mapping, operation CONDITION: out-variable attribute is missing");
		}
		//System.out.println("outVariable:"+outVariable+" newValue:'"+newValue+"'");
		variableToValueMap.put(outVariable,newValue);
	}

}