package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class SourceInventoryTypeConstruct extends GenericConstruct{

	public SourceInventoryTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(SourceType sourceType) throws Exception {
		Map<String, SourceType> sourceCodeToSourceTypeMap=convertEngine.getSourceCodeToSourceTypeMap();
		SourceType sourceTypeTemp=sourceCodeToSourceTypeMap.get(sourceType.getCode());
		if (sourceTypeTemp!=null){
			throw new Exception("Source code '"+sourceType.getCode()+"' is already defined.");
		}
		sourceCodeToSourceTypeMap.put(sourceType.getCode(),sourceType);
	}
	
}