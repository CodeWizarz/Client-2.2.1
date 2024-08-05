package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.ComparisonType;
import com.rapidesuite.client.dataConversion0000.ComparisonsType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class ComparisonsTypeConstruct  extends GenericConstruct {

	public ComparisonsTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public boolean process(ComparisonsType comparisonsType,String sourceCode,Map<String, Integer> fieldNameToPositionMap,
			String[] sourceDataRow, Map<String, String> variableToValueMap) throws Exception {
		ComparisonsType.Separator.Enum separator=comparisonsType.getSeparator();
		ComparisonType[] comparisonTypeArray=comparisonsType.getComparisonArray();
		for (ComparisonType comparisonType:comparisonTypeArray) {
			ComparisonTypeConstruct comparisonTypeConstruct=new ComparisonTypeConstruct(convertEngine);
			boolean isSuccess=comparisonTypeConstruct.process(comparisonType,sourceCode,fieldNameToPositionMap,sourceDataRow,variableToValueMap);
			if ( separator==ComparisonsType.Separator.OR) {
				if (isSuccess) {
					return true;
				}
			}
			else {
				if (!isSuccess) {
					return false;
				}
			}
		}
		if ( separator==ComparisonsType.Separator.OR) {
			return false;
		}
		else {
			return true;
		}
	}
	
}
