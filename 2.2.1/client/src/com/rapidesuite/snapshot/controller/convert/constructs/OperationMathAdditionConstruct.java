package com.rapidesuite.snapshot.controller.convert.constructs;

import java.math.BigInteger;
import java.util.Map;

import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class OperationMathAdditionConstruct extends GenericConstruct{

	public OperationMathAdditionConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> sourceCodeToPositionMap) throws Exception {
		String outVariable=operationType.getOutVariable();
		if (outVariable==null) {
			throw new Exception("Invalid Mapping, MATH_ADDITION: outVariable attribute is missing");
		}
		BigInteger startValueBigInteger=operationType.getStartValue();
		if (startValueBigInteger==null) {
			String sourceDataValue=OperationReplaceConstruct.getSourceValue(operationType,variableToValueMap,sourceDataRow,sourceCodeToPositionMap);
			int num=-1;
			try{
				num=Integer.valueOf(sourceDataValue);
			}
			catch(NumberFormatException e) {
				throw new Exception("Invalid number, MATH_ADDITION for number: '"+sourceDataValue+"'");
			}	
			BigInteger number=operationType.getNumber();
			if (number==null) {
				throw new Exception("Invalid Mapping, MATH_ADDITION: number attribute is missing");
			}
			int newNum=num+number.intValue();
			variableToValueMap.put(outVariable,""+newNum);
		}
		else {
			int startValue=startValueBigInteger.intValue();
			BigInteger preIncrementBigInteger=operationType.getPreIncrement();
			int newNum=-1;
			if (preIncrementBigInteger==null) {
				throw new Exception("Invalid Mapping, MATH_ADDITION: post-increment attribute is missing");
			}
			int preIncrement=preIncrementBigInteger.intValue();
			String value=variableToValueMap.get(outVariable);
			if (value==null) {
				newNum=startValue+preIncrement;
			}
			else {
				int currentValue=Integer.valueOf(value);
				newNum=currentValue+preIncrement;
			}
			variableToValueMap.put(outVariable,""+newNum);
		}
		
		
	}
	
}