package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.Map;
import com.rapidesuite.client.dataConversion0000.MapType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;
import com.rapidesuite.snapshot.model.DataRow;

public class MapColumnTypeConstruct extends GenericConstruct{

	public MapColumnTypeConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(MapType mapType,String loopRecordSourceCode,Map<String, Integer> sourceCodeToPositionMap,
			String[] sourceDataRow,	Map<String, Integer> targetNameToPositionMap,String[] targetDataValues, DataRow targetDataRow) throws Exception {
		String mapSourceName=mapType.getSourceName();
		String mapTargetName=mapType.getTargetName();
		Integer sourceDataValueIndex=sourceCodeToPositionMap.get(mapSourceName);
		if (sourceDataValueIndex==null) {
			throw new Exception("Invalid Mapping, MAP: cannot find the column '"+mapSourceName+"' in the source code '"+loopRecordSourceCode+"'");
		}
		String sourceDataValue=sourceDataRow[sourceDataValueIndex];
		
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
		upgradeEngine.getColumnNamesConvertedSet().add(mapSourceName);
	}

}