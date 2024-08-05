package com.rapidesuite.inject.selenium.commands;

import com.erapidsuite.configurator.navigation0005.TemplateSelectType;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateSelectTypeCommand extends TemplateMain{

	public TemplateSelectTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(TemplateSelectType templateSelectType,String nodeValue) throws Exception {
		String labelText=templateSelectType.getLabel();
		boolean isPageLoadingAfterSet=templateSelectType.getIsPageLoadingAfterSet();
		
		nodeValue=TemplateInputTypeCommand.inferNodeValueIfNoValueKB(worker,templateSelectType.getValueKBArray(),labelText,nodeValue);
		
		TemplateInputTypeCommand.processByLabelMatching((SeleniumWorker)worker,labelText,nodeValue,isPageLoadingAfterSet,
				templateSelectType,-1,"select");
	}
		
}