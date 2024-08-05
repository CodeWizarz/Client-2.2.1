package com.rapidesuite.inject.selenium.commands;

import com.erapidsuite.configurator.navigation0005.ErrorType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class ErrorTypeCommand extends Command{

	public ErrorTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(ErrorType errorType) throws Exception {
		String message=errorType.getMessage();
		
		String applicationKey=worker.getBatchInjectionTracker().getScriptGridTracker().getScript().getApplicationKey();
		String applicationName=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().
				getApplicationInfoPanel().getApplicationKeysToNamesMap().get(applicationKey);

		message=message.replaceAll("###APPLICATION_NAME###", applicationName);
		
		worker.println("Throwing an error message: '"+message+"'",false);
		throw new Exception(message);
	}
	
}
