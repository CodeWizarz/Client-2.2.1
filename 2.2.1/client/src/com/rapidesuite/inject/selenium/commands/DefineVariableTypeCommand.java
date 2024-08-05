package com.rapidesuite.inject.selenium.commands;

import java.util.Map;

import com.erapidsuite.configurator.navigation0005.DefineVariableType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class DefineVariableTypeCommand extends Command {

	public DefineVariableTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(DefineVariableType defineVariableType) throws Exception {	
		Map<String, DefineVariableType.Type.Enum> variableNameToTypeMap=((SeleniumWorker)worker).getVariableNameToTypeMap();
		String variableName=defineVariableType.getName();
		DefineVariableType.Type.Enum type=defineVariableType.getType();
		variableNameToTypeMap.put(variableName,type);
		worker.println("Defining variable '"+variableName+"' type: '"+type+"'");
	}
	
}