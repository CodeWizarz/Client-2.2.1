package com.rapidesuite.inject.selenium.commands;

import com.erapidsuite.configurator.navigation0005.TemplateEndSectionType;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateEndSectionTypeCommand extends TemplateMain{

	public TemplateEndSectionTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	@SuppressWarnings("unused")
	public void process(TemplateEndSectionType templateEndSectionType) {
		((SeleniumWorker)worker).setRelativeWebElementReferenceInUse(false);
		((SeleniumWorker)worker).setRelativeWebElementXPATH(null);
	}
	
}
