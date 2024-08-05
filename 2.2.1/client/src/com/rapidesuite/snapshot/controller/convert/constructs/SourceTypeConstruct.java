package com.rapidesuite.snapshot.controller.convert.constructs;

import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class SourceTypeConstruct extends GenericConstruct{

	public SourceTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(SourceType sourceType) throws Exception {
		com.rapidesuite.client.dataConversion0000.SourceType.Type.Enum sourceTypeAttribute=sourceType.getType();		
		if (sourceTypeAttribute.equals(SourceType.Type.INVENTORY)) {
			SourceInventoryTypeConstruct sourceInventoryTypeConstruct=new SourceInventoryTypeConstruct(convertEngine);
			sourceInventoryTypeConstruct.process(sourceType);
		}
		else 
		if (sourceTypeAttribute.equals(SourceType.Type.VIEW)) {
			convertEngine.setHasView(true);
			SourceViewTypeConstruct sourceViewTypeConstruct=new SourceViewTypeConstruct(convertEngine);
			sourceViewTypeConstruct.process(sourceType);
		}
		else 
		if (sourceTypeAttribute.equals(SourceType.Type.ROWS)) {
			SourceRowsTypeConstruct sourceRowsTypeConstruct=new SourceRowsTypeConstruct(convertEngine);
			sourceRowsTypeConstruct.process(sourceType);
		}
	}
	
}