package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.Map;
import java.util.regex.Pattern;

import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;
import com.rapidesuite.snapshot.model.ModelUtils;

public class OperationReplaceConstruct extends GenericConstruct{

	public OperationReplaceConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> fieldNameToPositionMap) throws Exception {
		String outVariable=operationType.getOutVariable();
		String textToSearch=operationType.getTextToSearch();
		String replacementText=operationType.getReplacementText();
		
		String sourceDataValue=getSourceValue(operationType,variableToValueMap,sourceDataRow,fieldNameToPositionMap);
		if (outVariable==null) {
			throw new Exception("Invalid Mapping, out-variable attribute is missing");
		}
		if (textToSearch==null) {
			throw new Exception("Invalid Mapping, text-to-search attribute is missing");
		}
		if (replacementText==null) {
			throw new Exception("Invalid Mapping, replacement-text attribute is missing");
		}
		int indexOf=textToSearch.indexOf(ModelUtils.REPLACEMENT_SEPARATOR);
		if (indexOf!=-1) {
			if (indexOf!=0) {
				throw new Exception("Invalid Mapping, text-to-search attribute must start with "+ModelUtils.REPLACEMENT_SEPARATOR);
			}
		}
		String value=convertEngine.getConvertController().getConstantNameToValueMap().get(textToSearch);
		if (value==null) {
			value=textToSearch;
		}
		String replacementTextValue=convertEngine.getConvertController().getConstantNameToValueMap().get(replacementText);
		if (replacementTextValue==null) {
			replacementTextValue=replacementText;
		}
		
		String safeToUseInReplaceAllString = Pattern.quote(value);
		//System.out.println("safeToUseInReplaceAllString:"+safeToUseInReplaceAllString);
		
		String[] splitText=safeToUseInReplaceAllString.split(ModelUtils.REPLACEMENT_SEPARATOR);
		String tempValue=sourceDataValue;
		for (String text:splitText) {
			String safeToUseInReplaceAllStringTemp = Pattern.quote(text);
			tempValue=tempValue.replaceAll(safeToUseInReplaceAllStringTemp, replacementTextValue);
		}
		//System.out.println("sourceDataValue:'"+sourceDataValue+"' textToSearch:'"+textToSearch+"' result:'"+tempValue+"'");
		variableToValueMap.put(outVariable,tempValue);
	}
	
	public static String getSourceValue(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> fieldNameToPositionMap) throws Exception {
		String inColumn=operationType.getInColumn();
		String inVariable=operationType.getInVariable();
	
		String sourceDataValue=null;
		if (inColumn!=null) {
			Integer sourceDataValueIndex=fieldNameToPositionMap.get(inColumn);
			if (sourceDataValueIndex==null) {
				throw new Exception("Invalid Mapping - cannot find the in-column '"+inColumn+"'");
			}
			sourceDataValue=sourceDataRow[sourceDataValueIndex];	
		}
		else {
			sourceDataValue=variableToValueMap.get(inVariable);
			if (sourceDataValue==null) {
				throw new Exception("Invalid Mapping - cannot find the in-variable: '"+inVariable+"'");
			}
		}
		return sourceDataValue;
	}
	
}