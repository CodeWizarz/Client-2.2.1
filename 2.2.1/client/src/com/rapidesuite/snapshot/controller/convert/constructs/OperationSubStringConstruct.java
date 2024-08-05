package com.rapidesuite.snapshot.controller.convert.constructs;

import java.math.BigInteger;
import java.util.Map;

import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class OperationSubStringConstruct extends GenericConstruct{

	public OperationSubStringConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> sourceCodeToPositionMap) throws Exception {
		String outVariable=operationType.getOutVariable();
		if (outVariable==null) {
			throw new Exception("Invalid Mapping, operation SUBSTRING: out-variable attribute is missing");
		}
		BigInteger startPositionInteger=operationType.getStartPosition();
		String startPositionVariable=operationType.getStartPositionVariable();
		BigInteger endPositionInteger=operationType.getEndPosition();
		String endPositionVariable=operationType.getEndPositionVariable();
		
		if (startPositionInteger==null && startPositionVariable==null) {
			throw new Exception("Invalid Mapping, operation SUBSTRING: startPosition or startPositionVariable attribute is missing");
		}
		if (endPositionInteger==null && endPositionVariable==null) {
			throw new Exception("Invalid Mapping, operation SUBSTRING: endPosition or endPositionVariable attribute is missing");
		}
		
		int startPosition=-1;
		if (startPositionInteger!=null) {
			startPosition=startPositionInteger.intValue();
		}
		else {
			String index=variableToValueMap.get(startPositionVariable);
			startPosition=Integer.valueOf(index).intValue();
		}
		if (startPosition==0) {
			throw new Exception("Invalid Mapping, operation SUBSTRING: startPosition attribute must be greater than 0");
		}
		
		int endPosition=-1;
		if (endPositionInteger!=null) {
			endPosition=endPositionInteger.intValue();
		}
		else {
			String index=variableToValueMap.get(endPositionVariable);
			endPosition=Integer.valueOf(index).intValue();
		}
		if (endPosition==0) {
			throw new Exception("Invalid Mapping, operation SUBSTRING: endPosition attribute must be greater than 0");
		}
		
		if (startPosition>endPosition) {
			throw new Exception("Invalid Mapping, operation SUBSTRING: startPosition must be less or equal to endPosition");
		}
		
		String sourceDataValue=OperationReplaceConstruct.getSourceValue(operationType,variableToValueMap,sourceDataRow,sourceCodeToPositionMap);
		//System.out.println("OperationSubStringConstruct, sourceDataValue:'"+sourceDataValue+"' startPosition:"+startPosition+" endPosition:"+endPosition);
		String newValue =null;
		if (sourceDataValue.length()<=endPosition) {
			newValue =sourceDataValue.substring(startPosition-1);
		}
		else {
			newValue =sourceDataValue.substring(startPosition-1, endPosition);
		}
		//System.out.println("OperationSubStringConstruct, outVariable:"+outVariable+" newValue:'"+newValue+"'");
		variableToValueMap.put(outVariable,newValue);
	}
		
}