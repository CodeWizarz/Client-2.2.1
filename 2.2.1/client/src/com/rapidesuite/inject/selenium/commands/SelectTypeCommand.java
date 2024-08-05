package com.rapidesuite.inject.selenium.commands;

import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.erapidsuite.configurator.navigation0005.SelectType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class SelectTypeCommand extends Command{

	public SelectTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(SelectType selectType,String nodeValue) throws Exception {
		WebElement webElement=((SeleniumWorker)worker).getCurrentWebElement();
		boolean isPageLoadingOnSelectValue=selectType.getIsPageLoadingOnSelectValue();
		com.erapidsuite.configurator.navigation0005.SelectType.By.Enum by=selectType.getBy();
		boolean isSelectByVisibleText=by.equals(SelectType.By.SELECT_BY_VISIBLE_TEXT);
		
		processGeneric(((SeleniumWorker)worker),webElement,nodeValue,isPageLoadingOnSelectValue,isSelectByVisibleText);
	}
	
	public static String getOptionValue(WebDriver webDriver,WebElement webElement) {
		String innerHtml = (String)((JavascriptExecutor)webDriver).executeScript("return arguments[0].innerHTML;", webElement);
		return innerHtml;
	}

	private static void printSelectElement(SeleniumWorker worker,Select selectElement) {
		worker.println("Printing SELECT html markup:");
		List<WebElement> elements=selectElement.getOptions();
		if (elements==null) {
			worker.println("No Options!");
			return;
		}
		worker.println("Options size: "+elements.size());
		for (WebElement element:elements) {
			SeleniumUtils.printElement(((SeleniumWorker)worker),((SeleniumWorker)worker).getWebDriver(),element);
		}
	}

	public static void processGeneric(SeleniumWorker worker, WebElement webElement, String nodeValue,boolean isPageLoadingAfterSet,boolean isSelectByVisibleText) throws Exception {
		if (webElement==null) {
			throw new Exception("YOU CANNOT USE 'SELECT' IF NO ELEMENT SELECTED.");
		}
		Select dropdownSelectElement = new Select(webElement);
		
		if (nodeValue.isEmpty()) {
			worker.println("BLANK VALUE detected in KB so no update.");
			return; 
		}
		if (nodeValue.equals("\"\"")) {
			worker.println("DOUBLE-QUOTES VALUE detected in KB so forcing blank.");
			nodeValue="";
		}
		
		worker.println("Selecting Value: '"+nodeValue+"'");
		if (isSelectByVisibleText) {
			try{				
				dropdownSelectElement.selectByVisibleText(nodeValue);
			}
			catch(org.openqa.selenium.NoSuchElementException noSuchElementException) {
				noSuchElementException.printStackTrace();
				printSelectElement(worker,dropdownSelectElement);
				//TODO: replace with user exception so we can separate data versus navigation issues.
				throw new Exception("UNABLE TO SELECT THE VALUE '"+nodeValue+"' FROM THE DROPDOWN LIST. PLEASE FIX YOUR DATA AND TRY AGAIN.");
			}
		}
		/*else 
		{
			//TODO: add this as an option in the XSD spec so that PD can use it
			worker.println("Selecting by Value Options...");
			List<WebElement> elements=dropdownSelectElement.getOptions();
			if (elements==null) {
				worker.println("No Options!");
				return;
			}
			worker.println("Options size: "+elements.size());
			int index=0;
			boolean hasValue=false;
			for (WebElement element:elements) {
				String optionValue=getOptionValue(((SeleniumWorker)worker).getWebDriver(),element);
				worker.println("option value: '"+optionValue+"'");
				if (optionValue!=null && optionValue.trim().equalsIgnoreCase(nodeValue)) {
					dropdownSelectElement.selectByIndex(index);
					hasValue=true;
					break;
				}
				index++;
			}
			if (!hasValue) {
				throw new Exception("UNABLE TO SELECT THE VALUE '"+nodeValue+"' FROM THE DROPDOWN LIST. PLEASE FIX YOUR DATA AND TRY AGAIN.");
			}
		}
		*/
				
		if (isPageLoadingAfterSet) {
			WebDriverWait webDriverWait=((SeleniumWorker)worker).getWebDriverWait();
			SeleniumUtils.waitForPageToFinishRendering(((SeleniumWorker)worker),webDriverWait);
			((SeleniumWorker)worker).setCurrentWebElement(null);
		}
	}
	
}
