package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.Map;
import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class OperationIndexOfConstruct extends GenericConstruct{
	
	private boolean isFirstIndexOf;
	
	public OperationIndexOfConstruct(ConvertEngine convertEngine,boolean isFirstIndexOf) {
		super(convertEngine);
		this.isFirstIndexOf=isFirstIndexOf;
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> fieldNameToPositionMap) throws Exception {
		String inVariable=operationType.getOutVariable();
		String inColumn=operationType.getInColumn();
		String outVariable=operationType.getOutVariable();
		String textToSearch=operationType.getTextToSearch();
		if (inVariable==null && inColumn==null) {
			throw new Exception("Invalid Mapping, operation INDEXOF: in-variable or in-column attribute must be present");
		}
		if (outVariable==null) {
			throw new Exception("Invalid Mapping, operation INDEXOF: out-variable attribute is missing");
		}
		if (textToSearch==null) {
			throw new Exception("Invalid Mapping, operation INDEXOF: text-to-search attribute is missing");
		}
		
		String sourceDataValue=OperationReplaceConstruct.getSourceValue(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
		String textToSearchValue=convertEngine.getConvertController().getConstantNameToValueMap().get(textToSearch);
		if (textToSearchValue==null) {
			textToSearchValue=textToSearch;
		}
		
		String output=null;
		int indexOf=-1;
		if (isFirstIndexOf) {
			indexOf=sourceDataValue.indexOf(textToSearchValue);
		}
		else {
			indexOf=sourceDataValue.lastIndexOf(textToSearchValue);
		}
		output=""+(indexOf+1); // returns either 0 if no match or the position starting at 1. 
		
		//System.out.println("OperationIndexOfConstruct, outVariable:"+outVariable+" output:"+output);
		variableToValueMap.put(outVariable,output);
	}
	
}