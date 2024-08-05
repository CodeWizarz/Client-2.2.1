package com.rapidesuite.inject.selenium.commands;

import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.erapidsuite.configurator.navigation0005.ScrollType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateGridSearchTypeCommand extends TemplateMain{

	private WebDriver webDriver;
	private WebElement sectionWebElement;
	private String blockToExecuteIfFound;
	private String blockToExecuteIfNotFound;

	public TemplateGridSearchTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
		webDriver=((SeleniumWorker)worker).getWebDriver();
	}
/*
	public void process(TemplateGridSearchType templateGridSearchType) throws Exception {
		String sectionTitle=templateGridSearchType.getSectionTitle();
		blockToExecuteIfFound=templateGridSearchType.getBlockToExecuteIfFound();
		blockToExecuteIfNotFound=templateGridSearchType.getBlockToExecuteIfNotFound();
		
		sectionWebElement=null;
		if (sectionTitle!=null && !sectionTitle.isEmpty()) {
			sectionWebElement=getSectionWebElement(sectionTitle);
			// No highlight because it shrinks the section causing an "not visible" error
			//SeleniumUtils.elementHighlight(webDriver,sectionWebElement);
		}		
		
		GridSearchType gridSearch=templateGridSearchType.getGridSearch();
		boolean hasSearchBar=gridSearch.getHasSearchBar();
		GridColumnType[] gridColumnTypeArray=gridSearch.getGridColumnArray();

		if (hasSearchBar) {
			worker.println("Find and Fill-in Search Bar...");
			findAndFillSearchBar(gridColumnTypeArray);
			boolean hasNoDataInGrid=hasNoDataInGrid();
			if (hasNoDataInGrid) {
				worker.println("Execute Block Not found...");
				executeBlock(blockToExecuteIfNotFound);
			}
			else {
				worker.println("Find and Click on TR tag...");
				findAndClickOnTRTag(gridColumnTypeArray);
				worker.println("Execute Block found...");
				executeBlock(blockToExecuteIfFound);
			}
		}
		else {
			worker.println("Find and Click on TR tag or scroll down...");
			findAndClickTRTagOrScrollDown(gridColumnTypeArray);
		}
	}
	
	private boolean hasNoDataInGrid() {
		try{
			String xpath="//div[text()='No data to display.']";
			getWebElement(xpath);
			return true;
		}
		catch(org.openqa.selenium.NoSuchElementException exception) {
			worker.println("NoSuchElementException...");
			return false;
		}
	}
	
	private WebElement getWebElement(String xpath,boolean isForceUseDriver,boolean isLocalReferenceAlreadyAddedInXPATH,boolean isDisplayedOnly) {
		WebElement webElement=null;
		if (sectionWebElement!=null && !isForceUseDriver) {
			if (!isLocalReferenceAlreadyAddedInXPATH) {
				xpath="."+xpath;
			}
			worker.println("Searching with xpath: '"+xpath+"' using section");
			webElement=sectionWebElement.findElement(By.xpath(xpath));
		}
		else {
			worker.println("Searching with xpath: '"+xpath+"' using driver");
			webElement=webDriver.findElement(By.xpath(xpath));
		}
		if (webElement!=null) {
			if (isDisplayedOnly && !webElement.isDisplayed()) {
				throw new NoSuchElementException("Element found but not displayed!");
			}
		}
		return webElement;
	}
	
	private WebElement getWebElement(String xpath) {
		return getWebElement(xpath,false,false,true);
	}
	
	private void findAndFillSearchBar(GridColumnType[] gridColumnTypeArray) throws Exception {
		
		try{
			String xpath="//a[@title='Clear All']";
			getWebElement(xpath);
		}
		catch(org.openqa.selenium.NoSuchElementException exception) {
			worker.println("NoSuchElementException...");
			// We need to enable it
			String xpath="//a[text()='View']";
			WebElement webElement=getWebElement(xpath);
			webElement.click();
			SeleniumUtils.waitForPageToFinishRendering((SeleniumWorker)worker,((SeleniumWorker)worker).getWebDriverWait());
			xpath="//td[text()='Query By Example']";	
			webElement=getWebElement(xpath,true,false,true);
			webElement.click();
			SeleniumUtils.waitForPageToFinishRendering((SeleniumWorker)worker,((SeleniumWorker)worker).getWebDriverWait());
		}
		
		WebElement returnInputWebElement=null;
		for (GridColumnType gridColumnType:gridColumnTypeArray) {
			int position=gridColumnType.getPosition().intValue();
			
			String value=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(((SeleniumWorker)worker).
					getBatchInjectionTracker(),gridColumnType,null,true);
			value=value.trim();
			worker.println("position: "+position+" value: '"+value+"'");
			
			StringBuffer xpathBuffer=new StringBuffer("");
			xpathBuffer.append("(");
			if (sectionWebElement!=null) {
				xpathBuffer.append(".");
			}
			xpathBuffer.append("//table[contains(@summary,'This table contains column headers')]//th//input)[").append(position).append("]");
			WebElement inputWebElement=getWebElement(xpathBuffer.toString(),false,true,true);
			if (returnInputWebElement==null) {
				returnInputWebElement=inputWebElement;
			}
			worker.println("Found!");
			SeleniumUtils.elementHighlight(webDriver,inputWebElement);
			
			inputWebElement.sendKeys(value);
			worker.println("sendkeys: '"+value+"'");
		}
		returnInputWebElement.sendKeys(Keys.RETURN);
		SeleniumUtils.waitForPageToFinishRendering(((SeleniumWorker)worker),((SeleniumWorker)worker).getWebDriverWait());
	}
	
	private void executeBlock(String blockToExecute) throws Exception {
		List<XmlObject> xmlObjectsSubList=worker.getBlockNameToCommandsMap().get(blockToExecute);
		if (xmlObjectsSubList==null) {
			throw new Exception("Cannot find the block name to execute: '"+blockToExecute+"'");
		}
		worker.processNavigationXMLObjects(xmlObjectsSubList);
	}
	
	private WebElement findAndClickOnTRTag(GridColumnType[] gridColumnTypeArray) throws Exception {
		WebElement webElement=findTRTag(gridColumnTypeArray);
		webElement.click();
		return webElement;
	}
	
	private void findAndClickTRTagOrScrollDown(GridColumnType[] gridColumnTypeArray) throws Exception {
		boolean hasFoundTR=false;
		while (true) {
			try{
				WebElement webElement=findTRTag(gridColumnTypeArray);
				webElement.click();
				hasFoundTR=true;
				break;
			}
			catch(org.openqa.selenium.NoSuchElementException exception) {
				worker.println("NoSuchElementException...");
				WebElement scrollBarWebElement=getScrollBarWebElement();
				if (scrollBarWebElement==null) {
					// the grid does not have any scrollbar (yet) maybe only few records showing.
					// so let's treat it as end of scroll.
					worker.println("No scroll bar detected!");
					hasFoundTR=false;
					break;
				}
				boolean isEndOfScrollTypeCommand=IsEndOfScrollTypeCommand.processGeneric(((SeleniumWorker)worker),scrollBarWebElement);
				if (isEndOfScrollTypeCommand) {
					worker.println("End of scroll detected!");
					hasFoundTR=false;
					break;
				}
				worker.println("Scrolling down...");
				ScrollTypeCommand.processGeneric((SeleniumWorker)worker,scrollBarWebElement,ScrollType.Direction.DOWN);
			}
			Thread.sleep(1500);
		}
		if (hasFoundTR) {
			worker.println("Executing block name: '"+blockToExecuteIfFound+"'");
			executeBlock(blockToExecuteIfFound);
		}
		else {
			worker.println("Executing block name: '"+blockToExecuteIfNotFound+"'");
			executeBlock(blockToExecuteIfNotFound);
		}
	}
	
	private WebElement getScrollBarWebElement() {
		try{
			String xpath="//div[contains(@id,'scroller')]";
			WebElement webElement=getWebElement(xpath);
			return webElement;
		}
		catch(org.openqa.selenium.NoSuchElementException exception) {
			worker.println("NoSuchElementException...");
		}
		return null;
	}

	private WebElement findTRTag(GridColumnType[] gridColumnTypeArray) throws Exception {
		//String xpath="(//tr[.//td//text()='FAI1'])[1]";
		StringBuffer xpathBuffer=new StringBuffer("");
		xpathBuffer.append("(");
		if (sectionWebElement!=null) {
			xpathBuffer.append(".");
		}
		xpathBuffer.append("//tr[");
		int index=0;
		for (GridColumnType gridColumnType:gridColumnTypeArray) {
			//int position=gridColumnType.getPosition().intValue();
			String value=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(((SeleniumWorker)worker).
					getBatchInjectionTracker(),gridColumnType,null,true);
			value=value.trim();
			xpathBuffer.append(".//td//text()='"+value+"' ");
			if ( (index+1)<gridColumnTypeArray.length) {
				xpathBuffer.append(" and ");
			}
			index++;
		}
		xpathBuffer.append("])[1]");
		String xpath=xpathBuffer.toString();
		WebElement webElement=getWebElement(xpath,false,true,true);
		return webElement;
	}

	private WebElement getSectionWebElement(String sectionTitle) throws Exception {
		WebElement webElement=getGenericSectionWebElement(sectionTitle,"h1");
		if (webElement==null) {
			webElement=getGenericSectionWebElement(sectionTitle,"h2");
		}
		if (webElement==null) {
			webElement=getGenericSectionWebElement(sectionTitle,"h3");
		}
		if (webElement==null) {
			throw new Exception("Unable to find the heading section '"+sectionTitle+"'");
		}
		return webElement;
	}
	
	private WebElement getGenericSectionWebElement(String sectionTitle,String tagName) {
		
		String xpath1="//"+tagName+"[text()='"+sectionTitle+"']";
		String xpath2="//table[contains(@summary,'This table contains column headers')]";
		String xpath=xpath1+"/ancestor::*[count(. |"+xpath2+"/ancestor::*) = count("+xpath2+"/ancestor::*)][1]";
		
		worker.println("Searching for section "+tagName+" with xpath: '"+xpath+"'");
		try{
			WebElement webElement=webDriver.findElement(By.xpath(xpath));
			return webElement;
		}
		catch(org.openqa.selenium.NoSuchElementException exception) {
			worker.println("NoSuchElementException...");
		}
		return null;
	}
	*/
}