package com.rapidesuite.inject.selenium.commands;

import java.util.Map;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.DefineVariableType;
import com.erapidsuite.configurator.navigation0005.SetVariableType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class SetVariableTypeCommand extends Command {

	public SetVariableTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(SetVariableType setVariableType) throws Exception {	
		Map<String, Object> variableNameToValueMap=((SeleniumWorker)worker).getVariableNameToValueMap();
		Map<String, DefineVariableType.Type.Enum> variableNameToTypeMap=((SeleniumWorker)worker).getVariableNameToTypeMap();
		String variableName=setVariableType.getName();
		boolean isAppend=setVariableType.getAppend();
		
		DefineVariableType.Type.Enum type=variableNameToTypeMap.get(variableName);
		if (type==DefineVariableType.Type.TEXT) {
			XmlObject setVariableXMLObject=(XmlObject)setVariableType;
			String setVariableXMLObjectNodeValue=((SimpleValue)setVariableXMLObject).getStringValue();
			String newValue="";
			setVariableXMLObjectNodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(worker.getBatchInjectionTracker(),
					setVariableXMLObject,setVariableXMLObjectNodeValue,false);
			if (isAppend) {
				String value=(String)variableNameToValueMap.get(variableName);
				if (value==null) {
					value="";
				}
				newValue=value+setVariableXMLObjectNodeValue;
			}
			else {
				newValue=setVariableXMLObjectNodeValue;
			}
			variableNameToValueMap.put(variableName, newValue);
			worker.println("Setting the value of the variable '"+variableName+"' to '"+newValue+"'");
		}
	}
	
}
