package com.rapidesuite.inject.selenium.commands;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.erapidsuite.configurator.navigation0005.TemplateStartSectionType;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateStartSectionTypeCommand extends TemplateMain{

	public TemplateStartSectionTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	@SuppressWarnings("unused")
	public void process(TemplateStartSectionType templateStartSectionType,String nodeValue) {
		WebDriver webDriver=((SeleniumWorker)worker).getWebDriver();
		
		worker.println("Searching for relative web element with xpath: '"+nodeValue+"'");
		WebElement webElement=webDriver.findElement(By.xpath(nodeValue));
		
		((SeleniumWorker)worker).setRelativeWebElementReferenceInUse(true);
		((SeleniumWorker)worker).setRelativeWebElementXPATH(nodeValue);
		((SeleniumWorker)worker).setCurrentWebElement(webElement);
	}
	
}
