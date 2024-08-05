package com.rapidesuite.inject.selenium.commands;

import java.util.Map;

import org.openqa.selenium.WebElement;

import com.erapidsuite.configurator.navigation0005.DefineVariableType;
import com.erapidsuite.configurator.navigation0005.ExtractType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class ExtractTypeCommand  extends Command {

	public ExtractTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(ExtractType extractType) throws Exception {	
		Map<String, DefineVariableType.Type.Enum> variableNameToTypeMap=((SeleniumWorker)worker).getVariableNameToTypeMap();
		
		String variable=extractType.getVariable();
		DefineVariableType.Type.Enum type=variableNameToTypeMap.get(variable);
		if (type==null) {
			throw new Exception("You must define the variable '"+variable+"' before extracting into it.");
		}
		WebElement webElement=((SeleniumWorker)worker).getCurrentWebElement();
		if (webElement==null) {
			throw new Exception("You cannot use 'Extract' tag if no webelement selected (Find).");
		}
		String attributeName=extractType.getAttribute();
		String attributeValue=webElement.getAttribute(attributeName);
		Map<String, Object> variableNameToValueMap=((SeleniumWorker)worker).getVariableNameToValueMap();
		variableNameToValueMap.put(variable, attributeValue);
		worker.println("Attribute name: '"+attributeName+"' attribute value: '"+attributeValue+"'");
	}
	
}
