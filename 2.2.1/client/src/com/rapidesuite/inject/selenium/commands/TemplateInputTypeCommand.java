package com.rapidesuite.inject.selenium.commands;

import java.math.BigInteger;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.erapidsuite.configurator.navigation0005.TemplateInputType;
import com.erapidsuite.configurator.navigation0005.ValueKBType;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateInputTypeCommand extends TemplateMain{

	public TemplateInputTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(TemplateInputType templateInputType,String nodeValue) throws Exception {
		String labelText=templateInputType.getLabel();
		boolean isPageLoadingAfterSet=templateInputType.getIsPageLoadingAfterSet();
		
		nodeValue=inferNodeValueIfNoValueKB(worker,templateInputType.getValueKBArray(),labelText,nodeValue);
			
		int position=-1;
		BigInteger positionBigInt=templateInputType.getPosition();
		if (positionBigInt!=null) {
			position=positionBigInt.intValue();
		}
		processByLabelMatching((SeleniumWorker)worker,labelText,nodeValue,isPageLoadingAfterSet,templateInputType,position,"input");
	}

	public static String inferNodeValueIfNoValueKB(Worker worker,ValueKBType[] valueKBTypeArray,String columnName,String nodeValue) throws Exception {
		String toReturn=nodeValue;

		worker.println("valueKBTypeArray: '"+valueKBTypeArray+"'");
		if (valueKBTypeArray==null || valueKBTypeArray.length==0) {
			InjectMain injectMain=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain();
			String inventoryName=worker.getCurrentRepeatInventoryName();
			toReturn=InjectUtils.getDataValue(injectMain,worker.getBatchInjectionTracker(),inventoryName,columnName);
			worker.println("Inferred nodeValue: '"+nodeValue+"'");
		}
		return toReturn;
	}

	public static void processByLabelMatching(SeleniumWorker seleniumWorker,
			String labelText,String nodeValue,boolean isPageLoadingAfterSet,XmlObject xmlObject,
			int position,String targetTagName) throws Exception
	{
		while (true) {
			try {
				processByLabelMatchingInternal(seleniumWorker,labelText,nodeValue,isPageLoadingAfterSet,xmlObject,position, targetTagName);
				break;
			} 
			catch (StaleElementReferenceException e) {
				seleniumWorker.println("Attempting to recover from StaleElementReferenceException ...");
				Thread.sleep(1000);
			}
		}
	}

	private static void processByLabelMatchingInternal(SeleniumWorker seleniumWorker,
			String labelText,String nodeValue,boolean isPageLoadingAfterSet,XmlObject xmlObject,
			int position,String targetTagName) throws Exception
	{
		seleniumWorker.println("Searching for element tag name '"+targetTagName+"' from source label text value: '"+labelText+"'...");
		if (position!=-1) {
			seleniumWorker.println("Position to look for: "+position);
		}
		WebDriver webDriver=seleniumWorker.getWebDriver();
		
		boolean skipIfKBBlank=true;
		/*
		 * lookupSourceText is the name of the column in the inventory
		 * so checking to skip if empty in KB
		 */
		if (skipIfKBBlank && nodeValue.isEmpty()) {
			seleniumWorker.println("Empty value in KB so skipping this command.");
			return;
		}	
		//TO FIX: THE FULL TR ROW IS RELOADED BY THE FORM CAN SEE THE HIGLIGHT DISAPPEAR!!!!
		
		String xpathLabel="//"+targetTagName+"[@id=//label[normalize-space(text())='"+labelText+"']/@for]";
		List<WebElement> webElements=null;
		if (seleniumWorker.isRelativeWebElementReferenceInUse()) {
			xpathLabel="."+xpathLabel;
			seleniumWorker.println("Searching for xpath: '"+xpathLabel+"' using section");
			webElements=seleniumWorker.getRelativeWebElement().findElements(By.xpath(xpathLabel));
		}
		else {
			seleniumWorker.println("Searching for xpath: '"+xpathLabel+"'");
			webElements =webDriver.findElements(By.xpath(xpathLabel));
		}
		if (webElements.isEmpty()) {
			throw new Exception("Unable to find any HTML elements");
		}
		seleniumWorker.println("Found "+webElements.size()+" elements!");
		
		WebElement targetWebElement=null;
		if (position!=-1) {
			if (webElements.size()<position) {
				throw new Exception("Navigation is looking for element at position "+position+" but found "+webElements.size()+" elements!");
			}
			int positionCounter=0;
			for (WebElement webElement:webElements) {
				positionCounter++;
				if (position!=positionCounter) {
					seleniumWorker.println("skipping element because out of position, position found: "+positionCounter+" position to look for: "+
							position);
					continue;
				}
				targetWebElement=webElement;
				break;
			}
		}
		else {
			if (webElements.size()>1) {
				throw new Exception("Too many elements found! "+webElements.size()+" elements");
			}
			targetWebElement=webElements.get(0);
		}
		SeleniumUtils.printElement(seleniumWorker, webDriver, targetWebElement);
		TemplateGenericTypeCommand.processAction(seleniumWorker,webDriver,targetWebElement,nodeValue,isPageLoadingAfterSet,xmlObject);
	}
	
	/*
	public static WebElement getStaleElementByXPATH(SeleniumWorker seleniumWorker,String xpath) {
	    try {
	    	if (seleniumWorker.isRelativeWebElementReferenceInUse()) {
	    		xpath="."+xpath;
				seleniumWorker.println("Searching for xpath: '"+xpath+"' using section");
				webElements=seleniumWorker.getRelativeWebElement().findElements(By.xpath(xpath));
			}
			else {
				seleniumWorker.println("Searching for xpath: '"+xpathLabel+"'");
				webElements =webDriver.findElements(By.xpath(xpathLabel));
			}
	        return driver.findElement(By.id(id));
	    } catch (StaleElementReferenceException e) {
	        System.out.println("Attempting to recover from StaleElementReferenceException ...");
	        return getStaleElemById(id);
	    }
	}
	*/
	
}