package com.rapidesuite.snapshot.controller.upgrade.constructs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.client.dataConversion0000.ComparisonsType;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;

public class IfTypeConstruct  extends GenericConstruct{

	public IfTypeConstruct(UpgradeEngine upgradeEngine) {
		super(upgradeEngine);
	}

	public boolean process(XmlObject ifTypeInnerXMLObject,String loopRecordSourceCode,Map<String, Integer> fieldNameToPositionMap,
			String[] sourceDataRow, Map<String, String> variableToValueMap) throws Exception {
		XmlObject[] xmlObjectsTemp=ifTypeInnerXMLObject.selectPath("*");
		List<XmlObject> ifTypeXMLObjectsList=Arrays.asList(xmlObjectsTemp);
		XmlObject innerXMLObject=ifTypeXMLObjectsList.get(0);

		ComparisonsType comparisonsType=(ComparisonsType)innerXMLObject;
		ComparisonsTypeConstruct comparisonsTypeConstruct=new ComparisonsTypeConstruct(upgradeEngine);
		return comparisonsTypeConstruct.process(comparisonsType,loopRecordSourceCode,fieldNameToPositionMap,sourceDataRow,variableToValueMap);
	}

}