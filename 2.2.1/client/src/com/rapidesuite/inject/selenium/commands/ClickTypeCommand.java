package com.rapidesuite.inject.selenium.commands;

import org.apache.xmlbeans.XmlObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.erapidsuite.configurator.navigation0005.ClickType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class ClickTypeCommand extends Command {

	public ClickTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(ClickType clickType) throws Exception {
		boolean waitForPageToFinishRendering=clickType.getWaitForPageToFinishRendering();
		boolean isCheckbox=clickType.getIsCheckbox();
		boolean isSkipBrowserTitleChange=clickType.getIsSkipBrowserTitleChange();
		
		processGeneric(worker,waitForPageToFinishRendering,isCheckbox,isSkipBrowserTitleChange,clickType);
	}
	
	public static void processGeneric(Worker worker,boolean waitForPageToFinishRendering,boolean isCheckbox,boolean isSkipBrowserTitleChange,
			XmlObject xmlObject) throws Exception {
		WebElement webElement=((SeleniumWorker)worker).getCurrentWebElement();
		if (webElement==null) {
			throw new Exception("YOU CANNOT USE 'CLICK' IF NO ELEMENT SELECTED.");
		}
		if (isCheckbox) {
			String nodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(((SeleniumWorker)worker).getBatchInjectionTracker(),xmlObject,"",true);
			worker.println("ClickTypeCommand, nodeValue: '"+nodeValue+"'");
			Boolean booleanObj = parseKBValueCheckbox(nodeValue);
			if (booleanObj==null) {
				// not set in the KB so we don't click!!!
				worker.println("ClickTypeCommand, not set in the KB so we don't click!!!");
				return;
			}
			if(webElement.isSelected()){
				if (booleanObj) {
					// already checked
					worker.println("ClickTypeCommand, already checked!!!");
					return;
				}
			}
			else {
				if (!booleanObj) {
					// not checked and the user want to uncheck it so nothing to do
					worker.println("ClickTypeCommand, not checked and the user want to uncheck it so nothing to do!!!");
					return;
				}
			}
		}
		
		int retriesCount=0;
		int maxRetries=3;
		while (retriesCount<=maxRetries) {
			try{
				webElement.click();
				break;
			}
			catch(org.openqa.selenium.ElementNotVisibleException e) {
				if (retriesCount<maxRetries) {
					worker.printStackTrace(e);
					worker.println("ElementNotVisibleException error detected...");
					worker.println("Retrying... retriesCount:"+retriesCount+" maxRetries:"+maxRetries);
					retriesCount++;
					Thread.sleep(1000);
					continue;
				}
				throw e;
			}
		}
		
		if (waitForPageToFinishRendering) {
			WebDriverWait webDriverWait=((SeleniumWorker)worker).getWebDriverWait();
			((SeleniumWorker)worker).println("Waiting for page to finish rendering...");
			SeleniumUtils.waitForPageToFinishRendering(((SeleniumWorker)worker),webDriverWait);
		}	
		((SeleniumWorker)worker).setCurrentWebElement(null);
		if ( ! isSkipBrowserTitleChange) {
			SeleniumUtils.changeBrowserTitle(((SeleniumWorker)worker).getWebDriver(),((SeleniumWorker)worker).getBatchInjectionTracker().getScriptGridTracker().getGridIndex()+1,
					((SeleniumWorker)worker));
		}
	}
	
	public static Boolean parseKBValueCheckbox(final String strBoolean) throws Exception
	{
		if ( strBoolean == null || strBoolean.isEmpty())
		{
			return null;
		}
		else if (  strBoolean.equalsIgnoreCase("yes") )
	    {
	        return true;
	    }
	    else if ( strBoolean.equalsIgnoreCase("no") )
	    {
	        return false;
	    }
	    else
	    {
	    	throw new Exception("Checkboxes values must either be 'yes' or 'no' or blank. Value found: '"+strBoolean+"'");
	    }
	}
	
}
