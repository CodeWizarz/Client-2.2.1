package com.rapidesuite.inject.selenium.commands;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.erapidsuite.configurator.navigation0005.FindElementsType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class FindElementsTypeCommand extends Command{

	public FindElementsTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(FindElementsType findElementsType,String nodeValue) throws Exception {
		WebDriver webDriver=((SeleniumWorker)worker).getWebDriver();
		com.erapidsuite.configurator.navigation0005.FindElementsType.By.Enum by=findElementsType.getBy();
		boolean isDebug=findElementsType.getIsDebug();
		BigInteger returnElementAtPosition=findElementsType.getReturnElementAtPosition();
		if (by.equals(FindElementsType.By.XPATH)) {
			if (isDebug) {
				SeleniumUtils.printAllMatchingElements(((SeleniumWorker)worker),webDriver,nodeValue);
			}
			List<WebElement> webElements=((SeleniumWorker)worker).getCurrentWebElements();
			if (webElements!=null) {
				if (returnElementAtPosition!=null) {
					WebElement webElement = findElementAtIndex(webElements,nodeValue,returnElementAtPosition.intValue());
					if (webElement!=null && worker.getScriptManager().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isWebElementHighlight()) {
						SeleniumUtils.elementHighlight(webDriver,webElement);
					}
					((SeleniumWorker)worker).setCurrentWebElement(webElement);
				}
				else {
					List<WebElement> webElementsTemp=findElements(findElementsType,webElements,nodeValue);
					((SeleniumWorker)worker).setCurrentWebElements(webElementsTemp);
				}		
			}
			else {
				if (returnElementAtPosition!=null) {
					WebElement webElement = findElementAtIndex(webDriver,nodeValue,returnElementAtPosition.intValue());
					if (webElement!=null && worker.getScriptManager().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isWebElementHighlight()) {
						SeleniumUtils.elementHighlight(webDriver,webElement);
					}
					((SeleniumWorker)worker).setCurrentWebElement(webElement);
				}
				else {
					List<WebElement> webElementsTemp=webDriver.findElements(By.xpath(nodeValue));
					printElements(findElementsType,webElementsTemp);
					((SeleniumWorker)worker).setCurrentWebElements(webElementsTemp);
				}		
			}
		}
	}
	
	public WebElement findElementAtIndex(WebDriver webDriver,String xpath,int elementIndex) throws Exception {
		List<WebElement> webElements=webDriver.findElements(By.xpath(xpath));
		worker.println("using webdriver to find webElements count: '"+webElements.size()+"'");
		return findElementAtIndex(webElements,xpath,elementIndex);
	}
		
	private static WebElement findElementAtIndex(List<WebElement> webElements,String xpath,int elementIndex) throws Exception {
		if (webElements.isEmpty()) {
			throw new NoSuchElementException("Unable to locate element for xpath: '"+xpath+"' at index: '"+elementIndex+"'");
		}
		if (elementIndex<=0) {
			throw new Exception("You cannot specify an index <= 0");
		}
		if (webElements.size()<elementIndex) {
			throw new NoSuchElementException("Unable to locate element - only "+webElements.size()+" Elements were found but index to find is '"+elementIndex+"'");
		}
		return webElements.get(elementIndex-1);
	}
	
	private List<WebElement> findElements(FindElementsType findElementsType,List<WebElement> webElements,String xpath) {
		List<WebElement> toReturn=new ArrayList<WebElement>();
		for (WebElement webElement:webElements){
			List<WebElement> webElementsTemp=webElement.findElements(By.xpath(xpath));
			toReturn.addAll(webElementsTemp);
		}
		printElements(findElementsType,toReturn);
		return toReturn;
	}
	
	private void printElements(FindElementsType findElementsType,List<WebElement> webElements) {
		worker.println("using webElements to find webElements count: '"+webElements.size()+"'");
		boolean isDebug=findElementsType.getIsDebug();
		for (WebElement webElement:webElements){
			if (isDebug) {
				worker.println("web element tagName: '"+webElement.getTagName()+"' text: '"+webElement.getText()+"'");
			}
		}
	}
	
}
