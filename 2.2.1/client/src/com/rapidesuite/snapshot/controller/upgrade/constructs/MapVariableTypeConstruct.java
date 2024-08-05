package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.MapType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;
import com.rapidesuite.snapshot.model.DataRow;

public class MapVariableTypeConstruct extends GenericConstruct{

	public MapVariableTypeConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(MapType mapType, Map<String, String> variableToValueMap,Map<String, Integer> targetNameToPositionMap, String[] targetDataValues, DataRow targetDataRow) throws Exception {
		String mapSourceName=mapType.getSourceName();
		String mapTargetName=mapType.getTargetName();
		String sourceDataValue=variableToValueMap.get(mapSourceName);
		if (sourceDataValue==null) {
			throw new Exception("Invalid Mapping, MAP: cannot find the variable '"+mapSourceName+"'");
		}
		
		if (mapTargetName.equalsIgnoreCase(MapTypeConstruct.RSC_DATA_LABEL_COLUMN)) {
			targetDataRow.setLabel(sourceDataValue);
		}
		else {
			Integer targetDataValueIndexInteger=targetNameToPositionMap.get(mapTargetName);
			if (targetDataValueIndexInteger==null) {
				throw new Exception("Invalid Mapping, MAP: cannot find the target column '"+mapTargetName+"'");
			}
			int targetDataValueIndex=targetDataValueIndexInteger.intValue();
			targetDataValues[targetDataValueIndex]=sourceDataValue;
		}
	}

}
