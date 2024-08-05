package com.rapidesuite.snapshot.controller.upgrade.constructs;

import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;

public class SourceTypeConstruct extends GenericConstruct{

	public SourceTypeConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public void process(SourceType sourceType) throws Exception {
		com.rapidesuite.client.dataConversion0000.SourceType.Type.Enum sourceTypeAttribute=sourceType.getType();		
		if (sourceTypeAttribute.equals(SourceType.Type.INVENTORY)) {
			SourceInventoryTypeConstruct sourceInventoryTypeConstruct=new SourceInventoryTypeConstruct(upgradeEngine);
			sourceInventoryTypeConstruct.process(sourceType);
		}
		else 
		if (sourceTypeAttribute.equals(SourceType.Type.VIEW)) {
			upgradeEngine.setHasView(true);
			SourceViewTypeConstruct sourceViewTypeConstruct=new SourceViewTypeConstruct(upgradeEngine);
			sourceViewTypeConstruct.process(sourceType);
		}
		else 
		if (sourceTypeAttribute.equals(SourceType.Type.ROWS)) {
			SourceRowsTypeConstruct sourceRowsTypeConstruct=new SourceRowsTypeConstruct(upgradeEngine);
			sourceRowsTypeConstruct.process(sourceType);
		}
	}
	
}