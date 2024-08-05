package com.rapidesuite.inject.selenium.commands;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.erapidsuite.configurator.navigation0005.WaitType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class WaitTypeCommand extends Command{

	public WaitTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(WaitType waitType,String nodeValue) {
		WebElement webElement=null;
		com.erapidsuite.configurator.navigation0005.WaitType.Type.Enum type=waitType.getType();
		com.erapidsuite.configurator.navigation0005.WaitType.By.Enum by=waitType.getBy();
		if (type.equals(WaitType.Type.PRESENCE_OF_ELEMENT_LOCATED)) {
			if (by.equals(WaitType.By.ID)) {
				WebDriverWait webDriverWait=((SeleniumWorker)worker).getWebDriverWait();
				WebDriver webDriver=((SeleniumWorker)worker).getWebDriver();
				webElement =webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id(nodeValue)));
				if (worker.getScriptManager().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isWebElementHighlight()) {
					SeleniumUtils.elementHighlight(webDriver,webElement);
				}
			}
		}
		((SeleniumWorker)worker).setCurrentWebElement(webElement);
	}
	
}
