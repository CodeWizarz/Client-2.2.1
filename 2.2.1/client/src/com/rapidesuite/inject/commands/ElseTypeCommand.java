package com.rapidesuite.inject.commands;

import java.util.Arrays;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.IfThenElseType;
import com.rapidesuite.inject.Worker;

public class ElseTypeCommand extends Command{

	public ElseTypeCommand(Worker worker) {
		super(worker);
	}

	public void process(IfThenElseType ifThenElseType) throws Exception {
		XmlObject[] xmlObjects=ifThenElseType.selectPath("*");
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);
		worker.processNavigationXMLObjects(xmlObjectsSubList);
	}
	
}
