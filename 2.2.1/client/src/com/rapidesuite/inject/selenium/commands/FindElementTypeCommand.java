package com.rapidesuite.inject.selenium.commands;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.erapidsuite.configurator.navigation0005.FindElementType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class FindElementTypeCommand extends Command{

	public FindElementTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(FindElementType findElementType,String nodeValue,int maxRetries) throws Exception {
		int retriesCount=0;
		while (retriesCount<=maxRetries) {
			try{
				processGeneric(findElementType,nodeValue);
				break;
			}
			catch(org.openqa.selenium.NoSuchElementException noSuchElementException) {
				if (noSuchElementException.getMessage().toLowerCase().startsWith("Unable to locate element".toLowerCase())) {
					if (retriesCount<maxRetries) {
						worker.printStackTrace(noSuchElementException);
						worker.println("NoSuchElementException error detected...");
						worker.println("Retrying... retriesCount:"+retriesCount+" maxRetries:"+maxRetries);
						retriesCount++;
						Thread.sleep(1000);
						continue;
					}
				}
				throw noSuchElementException;
			}
		}
	}

	private void processGeneric(FindElementType findElementType,String nodeValue) throws Exception {
		WebElement webElement=null;
		WebDriver webDriver=((SeleniumWorker)worker).getWebDriver();
		com.erapidsuite.configurator.navigation0005.FindElementType.By.Enum by=findElementType.getBy();
		boolean isDebug=findElementType.getIsDebug();
		String concatenatedTextNodes=findElementType.getConcatenatedTextNodes();
	
		boolean waitIsVisible=findElementType.getWaitIsVisible();
		if (waitIsVisible) {
			if (!by.equals(FindElementType.By.XPATH)) {
				throw new Exception("'waitIsVisible' attribute is only supported with the XPATH value");
			}
			WebDriverWait webDriverWait=((SeleniumWorker)worker).getWebDriverWait();
			worker.println("Using Webdriverwait for expected visibility of element...");
			webElement = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(nodeValue)));
		}
		else {
			if (by.equals(FindElementType.By.ID)) {
				webElement =webDriver.findElement(By.id(nodeValue));
			}
			else
				if (by.equals(FindElementType.By.NAME)) {
					webElement =webDriver.findElement(By.name(nodeValue));
				}
				else
					if (by.equals(FindElementType.By.TAG_NAME)) {
						webElement =webDriver.findElement(By.tagName(nodeValue));
					}
					else
						if (by.equals(FindElementType.By.XPATH)) {
							if (isDebug) {
								SeleniumUtils.printAllMatchingElements(((SeleniumWorker)worker),webDriver,nodeValue);
							}
							if (concatenatedTextNodes!=null && !concatenatedTextNodes.isEmpty()) {
								worker.println("concatenatedTextNodes is SET so concatenating all text nodes...");
								List<WebElement> webElements =webDriver.findElements(By.xpath(nodeValue));
								for (WebElement webElementTemp:webElements) {
									String concatenatedTextNodesTemp=webElementTemp.getText();
									worker.println("found concatenated text node: '"+concatenatedTextNodesTemp+"' ...");
									if (concatenatedTextNodes.equals(concatenatedTextNodesTemp)) {
										worker.println("MATCH found!");
										webElement=webElementTemp;
										break;
									}
								}
							}
							else {
								webElement =webDriver.findElement(By.xpath(nodeValue));
							}
						}
						else
							if (by.equals(FindElementType.By.REGULAR_EXPRESSION)) {
								List<WebElement> webElements=((SeleniumWorker)worker).getCurrentWebElements();
								boolean isCaseSensitive=findElementType.getIsCaseSensitive();
								String tagName=findElementType.getTagName();
								if (webElements!=null) {
									webElement = SeleniumUtils.getElementWithText(((SeleniumWorker)worker),webDriver,webElements,tagName,nodeValue,isCaseSensitive);
								}
								else {
									webElement = SeleniumUtils.getElementWithTextGeneric(((SeleniumWorker)worker),webDriver,null,tagName,nodeValue,isCaseSensitive);
								}
								((SeleniumWorker)worker).setCurrentWebElements(null);
								if (webElement==null) {
									throw new org.openqa.selenium.NoSuchElementException("Unable to locate element by using the regular expression!");
								}
							}
							else {
								throw new Exception("UNKNOWN VALUE ("+by+") IN 'BY' ATTRIBUTE FOR TAG 'findElement'");
							}
		}
		boolean isDisplayedOnlyElement=findElementType.getIsDisplayed();
		if (isDisplayedOnlyElement) {
			if (!webElement.isDisplayed()) {
				worker.println("WEBELEMENT:"+webElement);
				worker.println("webElement.isDisplayed():"+webElement.isDisplayed());
				worker.println("webElement.isEnabled():"+webElement.isEnabled());
				worker.println("webElement.isSelected():"+webElement.isSelected());
				throw new org.openqa.selenium.NoSuchElementException("Unable to locate element, element is not displayed!");
			}
		}		
		if (worker.getScriptManager().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isWebElementHighlight()) {
			boolean isHighlightTurnedOff=findElementType.getHighlightTurnedOff();
			if (!isHighlightTurnedOff) {
				SeleniumUtils.elementHighlight(webDriver,webElement);
			}
		}
		boolean isSetFocus=findElementType.getSetFocus();
		if (isSetFocus) {
			worker.println("Setting FOCUS on webelement...");
			SeleniumUtils.setFocus(webDriver,webElement);
		}
		((SeleniumWorker)worker).setCurrentWebElement(webElement);
	}
	
}
