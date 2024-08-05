package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.client.dataConversion0000.HasDataLoopType;
import com.rapidesuite.client.dataConversion0000.IfThenElseLoopType;
import com.rapidesuite.client.dataConversion0000.LoopRecordsType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;

public class IfThenElseLoopTypeConstruct extends GenericConstruct{

	public IfThenElseLoopTypeConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}
	
	public void process(IfThenElseLoopType ifThenElseLoopType) throws Exception {
		XmlObject[] xmlObjects=ifThenElseLoopType.selectPath("*");
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);
		XmlObject ifTypeInnerXMLObject=xmlObjectsSubList.get(0);
		
		XmlObject[] xmlObjectsTemp=ifTypeInnerXMLObject.selectPath("*");
		List<XmlObject> ifTypeXMLObjectsList=Arrays.asList(xmlObjectsTemp);
		XmlObject innerXMLObject=ifTypeXMLObjectsList.get(0);
		HasDataLoopType hasDataLoopType=(HasDataLoopType)innerXMLObject;
		String sourceCode=hasDataLoopType.getSource();
		
		List<String[]> dataRows=upgradeEngine.getSourceCodeToDataRowsMap(sourceCode);
		XmlObject xmlObject=null;
		//FileUtils.println("IfThenElseLoopTypeConstruct, sourceCode: "+sourceCode+" dataRows:"+dataRows.size());
		if (!dataRows.isEmpty()) {
			xmlObject=xmlObjectsSubList.get(1);
			//FileUtils.println("IfThenElseLoopTypeConstruct, THEN");
		}
		else {
			xmlObject=xmlObjectsSubList.get(2);
			//FileUtils.println("IfThenElseLoopTypeConstruct, ELSE");
		}
				
		XmlObject[] xmlInnerObjects=xmlObject.selectPath("*");
		List<XmlObject> xmlObjectsList=Arrays.asList(xmlInnerObjects);
		List<IfThenElseLoopType> ifThenElseLoopTypeList=new ArrayList<IfThenElseLoopType>();
		List<LoopRecordsType> loopRecordsTypeList=new ArrayList<LoopRecordsType>();
		for (XmlObject xmlObjectTemp:xmlObjectsList) {
			if (xmlObjectTemp instanceof IfThenElseLoopType) {
				ifThenElseLoopTypeList.add( (IfThenElseLoopType)xmlObjectTemp );
			}
			else
				if (xmlObjectTemp instanceof LoopRecordsType) {
					loopRecordsTypeList.add( (LoopRecordsType)xmlObjectTemp );
				}
		}
		
		for (IfThenElseLoopType ifThenElseLoopTypeTemp:ifThenElseLoopTypeList) {
			IfThenElseLoopTypeConstruct ifThenElseLoopTypeConstruct=new IfThenElseLoopTypeConstruct(upgradeEngine);
			ifThenElseLoopTypeConstruct.process(ifThenElseLoopTypeTemp);
		}
		
		for (LoopRecordsType loopRecordsType:loopRecordsTypeList) {
			LoopRecordsTypeConstruct loopRecordsTypeConstruct=new LoopRecordsTypeConstruct(upgradeEngine);
			loopRecordsTypeConstruct.process(loopRecordsType);
		}
	}
	
}
