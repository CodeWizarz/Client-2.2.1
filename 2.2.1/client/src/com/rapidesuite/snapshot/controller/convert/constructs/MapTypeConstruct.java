package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.MapType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;
import com.rapidesuite.snapshot.model.DataRow;

public class MapTypeConstruct extends GenericConstruct{

	public static final String RSC_DATA_LABEL_COLUMN="RSC Data Label";
	
	public MapTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(MapType mapType,String loopRecordSourceCode,Map<String, Integer> sourceCodeToPositionMap,String[] sourceDataRow,
			Map<String, Integer> targetNameToPositionMap,String[] targetDataValues, Map<String, String> variableToValueMap, DataRow targetDataRow) throws Exception {
		com.rapidesuite.client.dataConversion0000.MapType.SourceType.Enum mapSourceType=mapType.getSourceType();
		if (mapSourceType.equals(MapType.SourceType.COLUMN)) {
			MapColumnTypeConstruct mapColumnTypeConstruct=new MapColumnTypeConstruct(convertEngine);
			mapColumnTypeConstruct.process(mapType,loopRecordSourceCode,sourceCodeToPositionMap,
					sourceDataRow,	targetNameToPositionMap,targetDataValues,targetDataRow);
		}
		else 
		if (mapSourceType.equals(MapType.SourceType.VARIABLE)) {
			MapVariableTypeConstruct mapVariableTypeConstruct=new MapVariableTypeConstruct(convertEngine);
			mapVariableTypeConstruct.process(mapType,variableToValueMap,targetNameToPositionMap,targetDataValues,targetDataRow);
		}
	}
	
}