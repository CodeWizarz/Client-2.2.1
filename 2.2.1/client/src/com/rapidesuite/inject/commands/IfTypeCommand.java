package com.rapidesuite.inject.commands;

import java.util.Arrays;
import java.util.List;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.ComparisonsType;
import com.erapidsuite.configurator.navigation0005.EvaluateXPATHBooleanType;
import com.erapidsuite.configurator.navigation0005.FindElementType;
import com.erapidsuite.configurator.navigation0005.FindElementsType;
import com.erapidsuite.configurator.navigation0005.IsEndOfScrollType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.ScriptManager;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.selenium.SeleniumWorker;
import com.rapidesuite.inject.selenium.commands.ComparisonsTypeCommand;
import com.rapidesuite.inject.selenium.commands.EvaluateXPATHBooleanCommand;
import com.rapidesuite.inject.selenium.commands.FindElementTypeCommand;
import com.rapidesuite.inject.selenium.commands.FindElementsTypeCommand;
import com.rapidesuite.inject.selenium.commands.IsEndOfScrollTypeCommand;

public class IfTypeCommand extends Command{

	public IfTypeCommand(Worker worker) {
		super(worker);
	}

	public boolean process(XmlObject ifTypeInnerXMLObject) throws Exception {
		XmlObject[] xmlObjectsTemp=ifTypeInnerXMLObject.selectPath("*");
		List<XmlObject> ifTypeXMLObjectsList=Arrays.asList(xmlObjectsTemp);
		if (ifTypeXMLObjectsList.size()!=1) {
			throw new Exception("IfType must contain only one element: either <findElement> or <findElements>");
		}
		XmlObject innerXMLObject=ifTypeXMLObjectsList.get(0);
		String nodeValue=((SimpleValue)innerXMLObject).getStringValue();
		
		if (worker instanceof SeleniumWorker) {
			String taskName=((SeleniumWorker)worker).getTaskName();
			nodeValue=nodeValue.replace(ScriptManager.KEY_TASK_NAME,taskName);
		}
		
		try {
			if (innerXMLObject instanceof FindElementType) {
				FindElementType findElementType=(FindElementType)innerXMLObject;
				FindElementTypeCommand findElementTypeCommand=new FindElementTypeCommand(((SeleniumWorker)worker));
				nodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(worker.getBatchInjectionTracker(),innerXMLObject,nodeValue,true);
				worker.println("IfTypeCommand, nodeValue:'"+nodeValue+"'");
				int maxRetries=0;
				findElementTypeCommand.process(findElementType,nodeValue,maxRetries);
			}
			else
			if (innerXMLObject instanceof FindElementsType) {
				FindElementsType findElementsType=(FindElementsType)innerXMLObject;
				FindElementsTypeCommand findElementsTypeCommand=new FindElementsTypeCommand(((SeleniumWorker)worker));
				nodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(worker.getBatchInjectionTracker(),innerXMLObject,nodeValue,true);
				worker.println("IfTypeCommand, nodeValue:'"+nodeValue+"'");
				findElementsTypeCommand.process(findElementsType,nodeValue);
			}
			else
			if (innerXMLObject instanceof ComparisonsType) {
				ComparisonsType comparisonsType=(ComparisonsType)innerXMLObject;
				ComparisonsTypeCommand comparisonsTypeCommand=new ComparisonsTypeCommand(((SeleniumWorker)worker));
				return comparisonsTypeCommand.process(comparisonsType);
			}
			else
			if (innerXMLObject instanceof IsEndOfScrollType) {
				IsEndOfScrollTypeCommand isEndOfScrollTypeCommand=new IsEndOfScrollTypeCommand(((SeleniumWorker)worker));
				return isEndOfScrollTypeCommand.process();
			}
			else
			if (innerXMLObject instanceof EvaluateXPATHBooleanType) {
				EvaluateXPATHBooleanCommand evaluateXPATHBooleanCommand=new EvaluateXPATHBooleanCommand(((SeleniumWorker)worker));
				nodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(worker.getBatchInjectionTracker(),innerXMLObject,nodeValue,true);
				worker.println("IfTypeCommand, EvaluateXPATHBooleanType nodeValue:'"+nodeValue+"'");
				return evaluateXPATHBooleanCommand.process(nodeValue);
			}
			else {
				throw new Exception("'IF' commands must contain only one of those: 'FindElement', 'FindElements' or 'Comparisons'");
			}
		}
		catch(org.openqa.selenium.TimeoutException timeoutException) {
			worker.printStackTrace(timeoutException);
			return false;
		}
		catch(org.openqa.selenium.NoSuchElementException noSuchElementException) {
			worker.printStackTrace(noSuchElementException);
			if (noSuchElementException.getMessage().toLowerCase().startsWith("Unable to locate element".toLowerCase())) {
				return false;
			}
			worker.println("IfTypeCommand(), UNEXPECTED ERROR DETECTED SO PROPAGATING EXCEPTION!");
			throw noSuchElementException;
		}
		return true;
	}
	
}
