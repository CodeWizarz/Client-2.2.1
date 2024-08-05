package com.rapidesuite.inject.selenium.commands;

import com.erapidsuite.configurator.navigation0005.TemplateTextAreaType;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateTextAreaTypeCommand extends TemplateMain{

	public TemplateTextAreaTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(TemplateTextAreaType templateTextAreaType,String nodeValue) throws Exception {
		String labelText=templateTextAreaType.getLabel();
		boolean isPageLoadingAfterSet=templateTextAreaType.getIsPageLoadingAfterSet();
		
		nodeValue=TemplateInputTypeCommand.inferNodeValueIfNoValueKB(worker,templateTextAreaType.getValueKBArray(),labelText,nodeValue);
		
		TemplateInputTypeCommand.processByLabelMatching((SeleniumWorker)worker,labelText,nodeValue,isPageLoadingAfterSet,
				templateTextAreaType,-1,"textarea");
	}
		
}