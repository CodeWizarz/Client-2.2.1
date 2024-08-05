package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.MapType;
import com.rapidesuite.client.dataConversion0000.MappingsType;
import com.rapidesuite.client.dataConversion0000.TargetType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;
import com.rapidesuite.snapshot.model.DataRow;

public class TargetTypeConstruct extends GenericConstruct{

	public TargetTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public DataRow process(String loopRecordSourceCode,Map<String, Integer> fieldNameToPositionMap,TargetType targetType,String[] sourceDataRow
			, Map<String, String> variableToValueMap) throws Exception {		
		String targetInventoryName=targetType.getName();
		Map<String, Integer> targetInventoryNameToPositionMap=convertEngine.getTargetInventoryNameToPosition(targetInventoryName);
		int totalTargetFieldCount=targetInventoryNameToPositionMap.size();
		
		DataRow	targetDataRow=new DataRow();
		String[] targetDataValues=new String[totalTargetFieldCount];
		targetDataRow.setDataValues(targetDataValues);
		targetDataRow.setRscLastUpdatedByName("");
		targetDataRow.setRscLastUpdateDate("");

		MappingsType mappingsType=targetType.getMappings();
		MapType[] mapTypeArray=mappingsType.getMapArray();
		if (mapTypeArray==null) {
			return targetDataRow;
		}
				
		for (MapType mapType:mapTypeArray) {
			MapTypeConstruct mapTypeConstruct=new MapTypeConstruct(convertEngine);
			mapTypeConstruct.process(mapType,loopRecordSourceCode,fieldNameToPositionMap,sourceDataRow,targetInventoryNameToPositionMap,targetDataValues,
					variableToValueMap,targetDataRow);
		}
		return targetDataRow;
	}
	
}