package com.rapidesuite.inject.selenium.commands;

import java.util.Map;

import org.apache.xmlbeans.SimpleValue;

import com.erapidsuite.configurator.navigation0005.ParameterType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class ParameterTypeCommand extends Command {

	public ParameterTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(ParameterType[] parametersArray,boolean isExecuteBlock) throws Exception {
		Map<String,String> parameterNameToValueMap=((SeleniumWorker)worker).getParameterNameToValueMap();
		for (ParameterType parameterType:parametersArray) {
			String parameterName=parameterType.getName();
			String value=parameterNameToValueMap.get(parameterName);
			
			boolean isKBValue=false;
			String parameterValue=parameterType.getValue();
			if (parameterValue==null || parameterValue.isEmpty()) {
				isKBValue=true;
			}
			// We don't override the stored parameters especially if they are set before calling another
			// navigation.
			if (value==null || isKBValue) {
				worker.println("Adding new parameterName: '"+parameterName+"' parameterValue: '"+parameterType.getValue()+"'");
				
				if (isKBValue) {
					String nodeValue=((SimpleValue)parameterType).getStringValue();
					parameterValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(((SeleniumWorker)worker).getBatchInjectionTracker(),
							parameterType,nodeValue,true);
					worker.println("'Value' attribute is missing so analysing the text node...");
					worker.println("parameterValue: '"+parameterValue+"'");
				}
				parameterNameToValueMap.put(parameterName,parameterValue);
				worker.println("parameterName: '"+parameterName+"' parameterValue: '"+parameterValue+"'");
			}
			else {
				// We must reinitialize the parameters when executing a block because the same block may be called
				// multiple times in the REPEAT but with different parameters.
				if (isExecuteBlock) {
					parameterNameToValueMap.put(parameterName,parameterValue);
					worker.println("parameterName: '"+parameterName+"' parameterValue: '"+parameterValue+"' (Update)");
				}
				else {
					worker.println("parameter already added! skipping...");
				}
			}
		}
	}
	
}