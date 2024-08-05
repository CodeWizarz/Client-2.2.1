package com.rapidesuite.inject.selenium.commands;

import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class EvaluateXPATHBooleanCommand extends Command {

	public EvaluateXPATHBooleanCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public boolean process(String xpath) throws Exception {		
		worker.println("EvaluateXPATHBooleanCommand, xpath to evaluate: '"+xpath+"'");
		boolean toReturn=SeleniumUtils.evaluateBooleanXPATHExpression(((SeleniumWorker)worker).getWebDriver(),xpath);
		worker.println("EvaluateXPATHBooleanCommand, result: '"+toReturn+"'");
		
		return toReturn;
	}
		
}