package com.rapidesuite.snapshot.controller.convert.constructs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.rapidesuite.client.dataConversion0000.ComparisonType;
import com.rapidesuite.client.dataConversion0000.LeftOperandType;
import com.rapidesuite.client.dataConversion0000.RightOperandType;
import com.rapidesuite.client.dataConversion0000.ValueType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class ComparisonTypeConstruct extends GenericConstruct {

	public ComparisonTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public boolean process(ComparisonType comparisonType,String sourceCode,Map<String, Integer> fieldNameToPositionMap,
			String[] sourceDataRow, Map<String, String> variableToValueMap) throws Exception {
		LeftOperandType leftOperandType=comparisonType.getLeftOperand();
		RightOperandType rightOperandType=comparisonType.getRightOperand();
		ComparisonType.Operator.Enum operator=comparisonType.getOperator();
		
		return processGeneric(leftOperandType,rightOperandType,operator,sourceCode,fieldNameToPositionMap,sourceDataRow,variableToValueMap);
	}
	
	public static boolean processGeneric(LeftOperandType leftOperandType,RightOperandType rightOperandType,
			ComparisonType.Operator.Enum operator,String sourceCode,Map<String, Integer> fieldNameToPositionMap,
			String[] sourceDataRow, Map<String, String> variableToValueMap) throws Exception {
		ValueType leftOperandValueType=leftOperandType.getValue();
		ValueType rightOperandValueType=rightOperandType.getValue();
				
		if (operator==ComparisonType.Operator.EQUAL || operator==ComparisonType.Operator.NOT_EQUAL) {
			// text comparison
			String leftOperandNodeValue=getOperandStringValue(sourceCode,fieldNameToPositionMap,sourceDataRow,leftOperandValueType,variableToValueMap);
			String rightOperandNodeValue=getOperandStringValue(sourceCode,fieldNameToPositionMap,sourceDataRow,rightOperandValueType,variableToValueMap);
			if (operator==ComparisonType.Operator.EQUAL) {
				return leftOperandNodeValue.equals(rightOperandNodeValue);
			}
			else {
				return !leftOperandNodeValue.equals(rightOperandNodeValue);
			}
		}
		else
		if (operator==ComparisonType.Operator.LESS_THAN || operator==ComparisonType.Operator.LESS_THAN_OR_EQUAL ||
			operator==ComparisonType.Operator.GREATER_THAN || operator==ComparisonType.Operator.GREATER_THAN_OR_EQUAL 
		) {
			if (leftOperandValueType.getType().equals(ValueType.Type.DATE) || rightOperandValueType.getType().equals(ValueType.Type.DATE)) {
				Date leftOperandNodeValue=getOperandDateValue(sourceCode,fieldNameToPositionMap,sourceDataRow,leftOperandValueType,variableToValueMap);
				Date rightOperandNodeValue=getOperandDateValue(sourceCode,fieldNameToPositionMap,sourceDataRow,rightOperandValueType,variableToValueMap);
				//System.out.println("leftOperandNodeValue:"+leftOperandNodeValue+" rightOperandNodeValue:"+rightOperandNodeValue);
				if (operator==ComparisonType.Operator.LESS_THAN ) {
					return leftOperandNodeValue.getTime()<rightOperandNodeValue.getTime();
				}
				else
				if (operator==ComparisonType.Operator.LESS_THAN_OR_EQUAL) {
					return leftOperandNodeValue.getTime()<=rightOperandNodeValue.getTime();
				}
				else
				if (operator==ComparisonType.Operator.GREATER_THAN){
					return leftOperandNodeValue.getTime()>rightOperandNodeValue.getTime();
				}
				else
				if (operator==ComparisonType.Operator.GREATER_THAN_OR_EQUAL){
					return leftOperandNodeValue.getTime()>=rightOperandNodeValue.getTime();
				}
			}
			else {
				int leftOperandNodeValue=getOperandIntegerValue(sourceCode,fieldNameToPositionMap,sourceDataRow,leftOperandValueType,variableToValueMap);
				int rightOperandNodeValue=getOperandIntegerValue(sourceCode,fieldNameToPositionMap,sourceDataRow,rightOperandValueType,variableToValueMap);
				if (operator==ComparisonType.Operator.LESS_THAN ) {
					return leftOperandNodeValue<rightOperandNodeValue;
				}
				else
				if (operator==ComparisonType.Operator.LESS_THAN_OR_EQUAL) {
					return leftOperandNodeValue<=rightOperandNodeValue;
				}
				else
				if (operator==ComparisonType.Operator.GREATER_THAN){
					return leftOperandNodeValue>rightOperandNodeValue;
				}
				else
				if (operator==ComparisonType.Operator.GREATER_THAN_OR_EQUAL){
					return leftOperandNodeValue>=rightOperandNodeValue;
				}
			}
		}
		throw new Exception("Unsupported operator: '"+operator+"'");
	}
	
	private static String getOperandStringValue(String sourceCode,Map<String, Integer> fieldNameToPositionMap,
			String[] sourceDataRow,ValueType operandValueType, Map<String, String> variableToValueMap) throws Exception {
		String toReturn=null;
		if (operandValueType.getType().equals(ValueType.Type.COLUMN)) {
			Integer operandNodeIndex=fieldNameToPositionMap.get(operandValueType.getName());
			if (operandNodeIndex==null) {
				throw new Exception("Invalid Mapping, operation COMPARISON: cannot find the column '"+operandValueType.getName()+"' in the source code '"+sourceCode+"'");
			}
			toReturn=sourceDataRow[operandNodeIndex];
		}
		else
		if (operandValueType.getType().equals(ValueType.Type.VARIABLE)) {
			String variableValue=variableToValueMap.get(operandValueType.getName());
			if (variableValue==null) {
				throw new Exception("Invalid Mapping, operation COMPARISON: cannot find the variable '"+operandValueType.getName()+"' in the source code '"+sourceCode+"'");
			}
			toReturn=variableValue;
		}
		else
			if (operandValueType.getType().equals(ValueType.Type.TEXT)) {
				toReturn=operandValueType.getText();
			}
			
		if (toReturn==null) {
			toReturn="";
		}
		return toReturn;
	}
	
	private static int getOperandIntegerValue(String sourceCode,Map<String, Integer> fieldNameToPositionMap,
			String[] sourceDataRow,ValueType operandValueType, Map<String, String> variableToValueMap) throws Exception {
		if (operandValueType.getType().equals(ValueType.Type.COLUMN)) {
			Integer operandNodeIndex=fieldNameToPositionMap.get(operandValueType.getName());
			if (operandNodeIndex==null) {
				throw new Exception("Invalid Mapping, operation COMPARISON: cannot find the column '"+operandValueType.getName()+"' in the source code '"+sourceCode+"'");
			}
			String value=sourceDataRow[operandNodeIndex];
			return getInt(value);
		}
		else
		if (operandValueType.getType().equals(ValueType.Type.VARIABLE)) {
			String variableValue=variableToValueMap.get(operandValueType.getName());
			if (variableValue==null) {
				throw new Exception("Invalid Mapping, operation COMPARISON: cannot find the variable '"+operandValueType.getName()+"' in the source code '"+sourceCode+"'");
			}
			return getInt(variableValue);
		}
		else
		if (operandValueType.getType().equals(ValueType.Type.TEXT)) {
			String value=operandValueType.getText();
			return getInt(value);
		}
		else {
			throw new Exception("Invalid Mapping, operation COMPARISON: the value tag must be a column or text");
		}
	}
	
	private static int getInt(String value) throws Exception {
		try{
			int valueInt=Integer.valueOf(value);
			return valueInt;
		}
		catch(NumberFormatException e) {
			throw new Exception("Invalid Mapping, operation COMPARISON: the value must be a number (value: '"+value+"')");
		}
	}

	private static Date getOperandDateValue(String sourceCode,Map<String, Integer> fieldNameToPositionMap,
			String[] sourceDataRow,ValueType operandValueType, Map<String, String> variableToValueMap) throws Exception {
		if (operandValueType.getType().equals(ValueType.Type.COLUMN)) {
			Integer operandNodeIndex=fieldNameToPositionMap.get(operandValueType.getName());
			if (operandNodeIndex==null) {
				throw new Exception("Invalid Mapping, operation COMPARISON: cannot find the column '"+operandValueType.getName()+"' in the source code '"+sourceCode+"'");
			}
			String dateStr=sourceDataRow[operandNodeIndex];
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
			return format.parse(dateStr);
		}
		else
		if (operandValueType.getType().equals(ValueType.Type.VARIABLE)) {
			String variableValue=variableToValueMap.get(operandValueType.getName());
			if (variableValue==null) {
				throw new Exception("Invalid Mapping, operation COMPARISON: cannot find the variable '"+operandValueType.getName()+"' in the source code '"+sourceCode+"'");
			}
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
			return format.parse(variableValue);
		}
		else
		if (operandValueType.getType().equals(ValueType.Type.DATE)) {
			boolean isCurrentDate=operandValueType.getIsCurrentDate();
			if (isCurrentDate) {
				Date todayDate=new Date();
				return todayDate;
			}
			else {
				throw new Exception("Invalid Mapping, operation COMPARISON: the value tag does not contain the attribute 'isCurrentDate'");
			}
		}
		else {
			throw new Exception("Invalid Mapping, operation COMPARISON: the value tag is not a date");
		}
	}

}
