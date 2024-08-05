package com.rapidesuite.inject.selenium.commands;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.erapidsuite.configurator.navigation0005.TemplateRadioButtonType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateRadioButtonTypeCommand extends Command{

	public TemplateRadioButtonTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(TemplateRadioButtonType templateRadioButtonType,String nodeValue) throws Exception {
		WebDriver webDriver=((SeleniumWorker)worker).getWebDriver();
		
		String label=templateRadioButtonType.getLabel();
		boolean isPageLoadingAfterSet=templateRadioButtonType.getIsPageLoadingAfterSet();
		//worker.println("TemplateFindRadioButtonAndSetTypeCommand...");
		/*
		 * Find the element with has a LEGEND tag and its text node= legend value above.
		 * Then fetch the parent so we will be at the root
		 * find the label with its text node equal to the nodevalue then take the ID and fetch the input element.
		 */
		WebElement rootWebElement=null;
		if (((SeleniumWorker)worker).isRelativeWebElementReferenceInUse()) {
			rootWebElement=((SeleniumWorker)worker).getRelativeWebElement().findElement(By.xpath(".//legend[text()='"+label+"']/.."));
		}
		else {
			rootWebElement =webDriver.findElement(By.xpath("//legend[text()='"+label+"']/.."));
		}
		SeleniumUtils.printElement(((SeleniumWorker)worker),webDriver,rootWebElement);
		
		WebElement webElementLabel =rootWebElement.findElement(By.xpath(".//label[text()='"+nodeValue+"']"));
		SeleniumUtils.printElement(((SeleniumWorker)worker),webDriver,webElementLabel);
		String attribute=webElementLabel.getAttribute("FOR");
		if (attribute==null || attribute.isEmpty()) {
			throw new Exception("Unable to find the FOR attribute");
		}
		//worker.println("attribute:"+attribute);
		
		WebElement webElementInput =rootWebElement.findElement(By.xpath(".//input[@id='"+attribute+"']"));
		SeleniumUtils.printElement(((SeleniumWorker)worker),webDriver,webElementInput);
		
		((SeleniumWorker)worker).setCurrentWebElement(webElementInput);
		webElementInput.click();
		
		if (isPageLoadingAfterSet) {
			WebDriverWait webDriverWait=((SeleniumWorker)worker).getWebDriverWait();
			((SeleniumWorker)worker).println("Waiting for page to finish rendering...");
			SeleniumUtils.waitForPageToFinishRendering(((SeleniumWorker)worker),webDriverWait);
		}	
		((SeleniumWorker)worker).setCurrentWebElement(null);
	}
	
}