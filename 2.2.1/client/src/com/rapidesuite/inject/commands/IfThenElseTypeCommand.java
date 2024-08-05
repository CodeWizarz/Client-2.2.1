package com.rapidesuite.inject.commands;

import java.util.Arrays;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.IfThenElseType;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.selenium.SeleniumUtils;

public class IfThenElseTypeCommand extends Command{

	public IfThenElseTypeCommand(Worker worker) {
		super(worker);
	}

	public void process(IfThenElseType ifThenElseType) throws Exception {
		XmlObject[] xmlObjects=ifThenElseType.selectPath("*");
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);
		if (xmlObjectsSubList.size()!=3) {
			throw new Exception("IfThenElseType is not well formed - it should be like <IF></IF><THEN></THEN><ELSE></ELSE>");
		}
		XmlObject ifTypeInnerXMLObject=xmlObjectsSubList.get(0);
		
		String text=SeleniumUtils.formatCommand(ifTypeInnerXMLObject.toString());
		worker.println("<if>"+text+"</if>",true);
		
		IfTypeCommand ifTypeCommand=new IfTypeCommand(worker);
		boolean isSuccess=ifTypeCommand.process(ifTypeInnerXMLObject);
		worker.println("@@@@ RESULT OF CONDITION: "+isSuccess);
		if (isSuccess) {
			worker.println("@@@@ EXECUTING THE 'THEN' STATEMENT @@@@");
			XmlObject thenTypeXMLObject=xmlObjectsSubList.get(1);
			XmlObject[] thenXMLObjects=thenTypeXMLObject.selectPath("*");
			List<XmlObject> thenXMLObjectsList=Arrays.asList(thenXMLObjects);
			worker.processNavigationXMLObjects(thenXMLObjectsList);
		}
		else {
			worker.println("@@@@ EXECUTING THE 'ELSE' STATEMENT @@@@");
			XmlObject elseTypeXMLObject=xmlObjectsSubList.get(2);
			XmlObject[] elseXMLObjects=elseTypeXMLObject.selectPath("*");
			List<XmlObject> elseXMLObjectsList=Arrays.asList(elseXMLObjects);
			worker.processNavigationXMLObjects(elseXMLObjectsList);
		}
	}
	
}
