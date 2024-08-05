package com.rapidesuite.inject.selenium.commands;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.erapidsuite.configurator.navigation0005.ScrollType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class ScrollTypeCommand extends Command {

	public ScrollTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(ScrollType scrollType) throws Exception {
		WebElement webElement=((SeleniumWorker)worker).getCurrentWebElement();
		ScrollType.Direction.Enum direction=scrollType.getDirection();
		processGeneric((SeleniumWorker)worker,webElement, direction);
	}
	
	public static void processGeneric(
			SeleniumWorker seleniumWorker,WebElement webElement,ScrollType.Direction.Enum direction) throws Exception {
		if (webElement==null) {
			throw new Exception("You cannot use 'Scroll' tag if no webelement selected (use 'Find' to select the scrollbar).");
		}	
		JavascriptExecutor js = (JavascriptExecutor)seleniumWorker.getWebDriver();
		
		if (direction.equals(ScrollType.Direction.DOWN)) {
			js.executeScript("arguments[0].scrollTop += arguments[1];",webElement, 100);
		}
		else
		if (direction.equals(ScrollType.Direction.TOP)) {
			// setting the scroll bar back to the top
			js.executeScript("arguments[0].scrollTop = arguments[1];",webElement, 0);
		}
		else {
			throw new Exception("Unsupported Scroll direction: '"+direction+"'");
		}
		
		//webElement.sendKeys(Keys.PAGE_DOWN);
		seleniumWorker.println("scroll, direction: '"+direction+"' sent. Waiting for Page rendering...");
		SeleniumUtils.waitForPageToFinishRendering(seleniumWorker,seleniumWorker.getWebDriverWait());
	}
	
}

