package com.rapidesuite.inject.selenium.commands;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.erapidsuite.configurator.navigation0005.TemplateClickType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateClickTypeCommand extends Command{

	public TemplateClickTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(TemplateClickType templateClickType,String nodeValue) {
		WebDriver webDriver=((SeleniumWorker)worker).getWebDriver();
		WebDriverWait webDriverWait=((SeleniumWorker)worker).getWebDriverWait();
		
		TemplateClickType.Attribute.Enum attribute=templateClickType.getAttribute();
		TemplateClickType.Type.Enum type=templateClickType.getType();
		String xpath="N/A";
		if (type.equals(TemplateClickType.Type.BUTTON)) {
			if (attribute.equals(TemplateClickType.Attribute.TEXT_NODE)) {
				xpath="//button[text()='"+nodeValue+"']";
			}
		}
		else
			if (type.equals(TemplateClickType.Type.ANCHOR_IMAGE)) {
				if (attribute.equals(TemplateClickType.Attribute.TITLE)) {
					xpath="//img[@title='"+nodeValue+"']/..";
				}
			}
		WebElement webElement=null;
		if (((SeleniumWorker)worker).isRelativeWebElementReferenceInUse()) {
			String modifiedXpath="."+xpath;
			worker.println("Searching with xpath: '"+modifiedXpath+"'");
			webElement=((SeleniumWorker)worker).getRelativeWebElement().findElement(By.xpath(modifiedXpath));
		}
		else {
			worker.println("Searching with xpath: '"+xpath+"'");
			webElement =webDriver.findElement(By.xpath(xpath));
		}
		SeleniumUtils.elementHighlight(webDriver,webElement);
		SeleniumUtils.printElement(((SeleniumWorker)worker),webDriver,webElement);
		((SeleniumWorker)worker).setCurrentWebElement(webElement);
		webElement.click();
		SeleniumUtils.waitForPageToFinishRendering(((SeleniumWorker)worker),webDriverWait);
	}
	
}