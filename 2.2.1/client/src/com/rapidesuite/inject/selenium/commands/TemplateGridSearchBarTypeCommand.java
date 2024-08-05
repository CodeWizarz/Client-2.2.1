package com.rapidesuite.inject.selenium.commands;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.erapidsuite.configurator.navigation0005.GridFindByColumnType;
import com.erapidsuite.configurator.navigation0005.TemplateGridSearchBarType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateGridSearchBarTypeCommand extends TemplateMain{

	private WebDriver webDriver;
	private WebElement gridSectionWebElement;

	public TemplateGridSearchBarTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
		webDriver=((SeleniumWorker)worker).getWebDriver();
	}
	
	public void process(TemplateGridSearchBarType templateGridSearchBarType) throws Exception {
		String gridTitle=templateGridSearchBarType.getGridTitle();
		gridSectionWebElement=TemplateUtils.getGridSectionWebElement(webDriver,worker,gridTitle);
		// No highlight because it shrinks the section causing an "not visible" error
		//SeleniumUtils.elementHighlight(webDriver,sectionWebElement);	
		GridFindByColumnType[] gridColumnTypeArray=templateGridSearchBarType.getGridFindByColumnArray();

		worker.println("Find and Fill-in Search Bar...");
		findAndFillSearchBar(gridColumnTypeArray);
	}
		
	private void findAndFillSearchBar(GridFindByColumnType[] gridFindByColumnTypeArray) throws Exception {
		/*
		 * Need to enable search bar if not done yet
		 */
		try{
			String xpath="//a[@title='Clear All']";
			TemplateUtils.getWebElement(webDriver,worker,gridSectionWebElement,xpath);
		}
		catch(org.openqa.selenium.NoSuchElementException exception) {
			worker.println("NoSuchElementException...");
			// We need to enable it
			String xpath="//a[text()='View']";
			WebElement webElement=TemplateUtils.getWebElement(webDriver,worker,gridSectionWebElement,xpath);
			webElement.click();
			SeleniumUtils.waitForPageToFinishRendering((SeleniumWorker)worker,((SeleniumWorker)worker).getWebDriverWait());
			xpath="//td[text()='Query By Example']";	
			webElement=TemplateUtils.getWebElement(webDriver,worker,gridSectionWebElement,xpath,true,false,true);
			webElement.click();
			SeleniumUtils.waitForPageToFinishRendering((SeleniumWorker)worker,((SeleniumWorker)worker).getWebDriverWait());
		}
		
		WebElement returnInputWebElement=null;
		for (GridFindByColumnType gridFindByColumnType:gridFindByColumnTypeArray) {
			int position=gridFindByColumnType.getPosition().intValue();
			
			String value=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(((SeleniumWorker)worker).
					getBatchInjectionTracker(),gridFindByColumnType,null,true);
			value=value.trim();
			worker.println("position: "+position+" value: '"+value+"'");
			
			StringBuffer xpathBuffer=new StringBuffer("");
			xpathBuffer.append("(");
			if (gridSectionWebElement!=null) {
				xpathBuffer.append(".");
			}
			xpathBuffer.append("//table[contains(@summary,'This table contains column headers')]//th//input)[").append(position).append("]");
			WebElement inputWebElement=TemplateUtils.getWebElement(webDriver,worker,gridSectionWebElement,
					xpathBuffer.toString(),false,true,true);
			if (returnInputWebElement==null) {
				returnInputWebElement=inputWebElement;
			}
			worker.println("Found!");
			SeleniumUtils.elementHighlight(webDriver,inputWebElement);
			
			inputWebElement.clear();
			inputWebElement.sendKeys(value);
			worker.println("sendkeys: '"+value+"'");
		}
		returnInputWebElement.sendKeys(Keys.RETURN);
		SeleniumUtils.waitForPageToFinishRendering(((SeleniumWorker)worker),((SeleniumWorker)worker).getWebDriverWait());
	}
		
}