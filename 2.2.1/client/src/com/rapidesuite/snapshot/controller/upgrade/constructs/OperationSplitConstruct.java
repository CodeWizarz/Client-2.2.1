package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.Map;
import java.util.regex.Pattern;

import com.rapidesuite.client.dataConversion0000.OperationType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;
import com.rapidesuite.snapshot.model.ModelUtils;

public class OperationSplitConstruct extends GenericConstruct{

	public OperationSplitConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(OperationType operationType, Map<String, String> variableToValueMap, String[] sourceDataRow,
			Map<String, Integer> sourceCodeToPositionMap) throws Exception {
		String outVariables=operationType.getOutVariables();
		String separator=operationType.getSeparator();
		String constantSeparator=upgradeEngine.getUpgradeController().getConstantNameToValueMap().get(separator);
		if (constantSeparator!=null) {
			separator=constantSeparator;
		}
		String sourceDataValue=OperationReplaceConstruct.getSourceValue(operationType,variableToValueMap,sourceDataRow,sourceCodeToPositionMap);
		if (outVariables==null) {
			throw new Exception("Invalid Mapping, operation SPLIT: outVariables attribute is missing");
		}
		if (separator==null) {
			throw new Exception("Invalid Mapping, operation SPLIT: separator attribute is missing");
		}
		String separatorFinal=getFinalSeparator(separator,sourceDataValue);
		
		String[] splitSourceValues=null;
		if (separatorFinal==null) {
			splitSourceValues=new String[1];
			splitSourceValues[0]=sourceDataValue;
		}
		else {
			// Search and Replace \SEPARATOR first as Oracle allows to escape the separator
			// then put it back at the end
			String escapeSeparator="\\"+separatorFinal;
			int indexOf=separator.indexOf(ModelUtils.REPLACEMENT_SEPARATOR);
			if (indexOf!=-1) {
				String escapeSeparatorQuoted = Pattern.quote(escapeSeparator);
				sourceDataValue=sourceDataValue.replaceAll(escapeSeparatorQuoted, ModelUtils.REPLACEMENT_SEPARATOR);
			}
			
			String separatorFinalQuoted = Pattern.quote(separatorFinal);
			splitSourceValues=sourceDataValue.split(separatorFinalQuoted);
			int index=0;
			for (String splitSourceValue:splitSourceValues) {
				if (indexOf!=-1) {
					splitSourceValue=splitSourceValue.replaceAll(ModelUtils.REPLACEMENT_SEPARATOR,"\\"+escapeSeparator);
				}
				splitSourceValues[index]=splitSourceValue;
				index++;
			}
		}
		String[] splitOutVariables=upgradeEngine.getSplitVariableTextToArrayMap().get(outVariables);
		if (splitOutVariables==null) {
			splitOutVariables=outVariables.split(",");
			upgradeEngine.getSplitVariableTextToArrayMap().put(outVariables, splitOutVariables);
		}
		
		if (splitSourceValues.length>splitOutVariables.length) {
			throw new Exception("Invalid Mapping, operation SPLIT: not enough Variables defined in the SPLIT operation to hold the source values.");
		}
		
		int index=0;
		for (String splitVariable:splitOutVariables) {
			String newValue="";
			if ( index < splitSourceValues.length) {
				newValue=splitSourceValues[index];
			}
			variableToValueMap.put(splitVariable,newValue);
			index++;
		}
	}
	
	private String getFinalSeparator(String separator,String sourceDataValue) {
		int indexOf=separator.indexOf(ModelUtils.REPLACEMENT_SEPARATOR);
		if (indexOf==-1) {
			return separator;
		}
		String[] separatorTextArray=separator.split(ModelUtils.REPLACEMENT_SEPARATOR);
		String separatorFinal=null;
		for (String separatorText:separatorTextArray) {
			if (separatorText.isEmpty()) {
				continue;
			}
			indexOf=sourceDataValue.indexOf(separatorText);
			if (indexOf!=-1) {
				// we stop at the first match
				separatorFinal=separatorText;
				break;
			}
		}
		return separatorFinal;
	}
	
}