package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.Map;

import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;

public class SourceInventoryTypeConstruct extends GenericConstruct{

	public SourceInventoryTypeConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(SourceType sourceType) throws Exception {
		Map<String, SourceType> sourceCodeToSourceTypeMap=upgradeEngine.getSourceCodeToSourceTypeMap();
		SourceType sourceTypeTemp=sourceCodeToSourceTypeMap.get(sourceType.getCode());
		if (sourceTypeTemp!=null){
			throw new Exception("Source code '"+sourceType.getCode()+"' is already defined.");
		}
		sourceCodeToSourceTypeMap.put(sourceType.getCode(),sourceType);
	}
	
}