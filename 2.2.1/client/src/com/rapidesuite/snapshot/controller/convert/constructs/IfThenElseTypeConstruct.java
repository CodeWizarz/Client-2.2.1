package com.rapidesuite.snapshot.controller.convert.constructs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.client.dataConversion0000.IfThenElseType;
import com.rapidesuite.client.dataConversion0000.TargetType;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;

public class IfThenElseTypeConstruct extends GenericConstruct{

	public IfThenElseTypeConstruct(ConvertEngine convertEngine) {
		super(convertEngine);
	}

	public void process(IfThenElseType ifThenElseType,
			String loopRecordSourceCode,Map<String, Integer> fieldNameToPositionMap,String[] sourceDataRow,int sourceDataRowIndex,
			Map<String,String> variableToValueMap) throws Exception {
		XmlObject[] xmlObjects=ifThenElseType.selectPath("*");
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);
		if (xmlObjectsSubList.size()!=3) {
			throw new Exception("IfThenElseType is not well formed - it should be like <IF></IF><THEN></THEN><ELSE></ELSE>");
		}
		XmlObject ifTypeInnerXMLObject=xmlObjectsSubList.get(0);
		
		IfTypeConstruct ifTypeConstruct=new IfTypeConstruct(convertEngine);
		boolean isSuccess=ifTypeConstruct.process(ifTypeInnerXMLObject,loopRecordSourceCode,fieldNameToPositionMap,sourceDataRow,variableToValueMap);
		XmlObject xmlObject=null;
		if (isSuccess) {
			xmlObject=xmlObjectsSubList.get(1);
		}
		else {
			xmlObject=xmlObjectsSubList.get(2);
		}
		
		XmlObject[] xmlInnerObjects=xmlObject.selectPath("*");
		List<XmlObject> xmlObjectsList=Arrays.asList(xmlInnerObjects);
		List<IfThenElseType> ifThenElseTypeList=new ArrayList<IfThenElseType>();
		List<TargetType> targetTypeList=new ArrayList<TargetType>();
		for (XmlObject xmlObjectTemp:xmlObjectsList) {
			if (xmlObjectTemp instanceof IfThenElseType) {
				ifThenElseTypeList.add( (IfThenElseType)xmlObjectTemp );
			}
			else
				if (xmlObjectTemp instanceof TargetType) {
					targetTypeList.add( (TargetType)xmlObjectTemp );
				}
				else {
					throw new Exception("Unsupported xmlObject: '"+xmlObjectTemp.getClass()+"'");
				}
		}			
		IfThenElseType[] ifThenElseTypeArray=ifThenElseTypeList.toArray(new IfThenElseType[ifThenElseTypeList.size()]);
		TargetType[] targetTypeArray=targetTypeList.toArray(new TargetType[targetTypeList.size()]);
		LoopRecordsTypeConstruct.genericProcessing(convertEngine,ifThenElseTypeArray,targetTypeArray,
				loopRecordSourceCode,fieldNameToPositionMap,sourceDataRow,sourceDataRowIndex,variableToValueMap);
	}
	
}
