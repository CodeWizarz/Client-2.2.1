package com.rapidesuite.inject.selenium.commands;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.erapidsuite.configurator.navigation0005.SendKeysType;
import com.rapidesuite.inject.ScriptManager;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class SendKeysTypeCommand extends Command{

	public SendKeysTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(SendKeysType sendKeysType,String nodeValue) throws Exception {
		boolean isClear=sendKeysType.getIsClear();
		boolean isENTERKey=sendKeysType.getIsENTERKey();
		boolean isTABKey=sendKeysType.getIsTABKey();
		boolean waitForPageToFinishRendering=sendKeysType.getWaitForPageToFinishRendering();
		
		WebElement webElement =((SeleniumWorker)worker).getCurrentWebElement();
		processGeneric(worker,webElement,nodeValue,isClear,isENTERKey,isTABKey,waitForPageToFinishRendering);
	}
	
	public static void processGeneric(Worker worker,WebElement webElement,String nodeValue,boolean isClear,boolean isENTERKey,boolean isTABKey,
			boolean waitForPageToFinishRendering) throws Exception {
		if (webElement==null) {
			throw new Exception("YOU CANNOT USE 'SENDKEYS' IF NO ELEMENT SELECTED.");
		}
		worker.println("SendKeys value to inject: '"+nodeValue+"'");
		
		String applicationKey=worker.getBatchInjectionTracker().getScriptGridTracker().getScript().getApplicationKey();
		String userName=worker.getScriptManager().getInjectMain().getApplicationInfoPanel().getUserName(applicationKey);
		String password=worker.getScriptManager().getInjectMain().getApplicationInfoPanel().getPassword(applicationKey);
		
		nodeValue=nodeValue.replace(ScriptManager.KEY_FUSION_USER_NAME,userName);
		nodeValue=nodeValue.replace(ScriptManager.KEY_FUSION_PASSWORD,password);
		String implementationProject=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().getApplicationInfoPanel().getImplementationProject();
		nodeValue=nodeValue.replace(ScriptManager.KEY_IMPLEMENTATION_PROJECT,implementationProject);
		String taskName=((SeleniumWorker)worker).getTaskName();
		nodeValue=nodeValue.replace(ScriptManager.KEY_TASK_NAME,taskName);
		
		if (nodeValue.isEmpty()) {
			worker.println("BLANK VALUE detected in KB so no update.");
			return;
		}
		if (nodeValue.equals("\"\"")) {
			worker.println("DOUBLE-QUOTES VALUE detected in KB so forcing blank.");
			nodeValue="";
		}
		String value=webElement.getAttribute("value");
	    /*ArrayList attributes = (ArrayList) ((JavascriptExecutor)seleniumWorker.getWebDriver()).executeScript(
		      "var s = []; var attrs = arguments[0].attributes; for (var l = 0; l < attrs.length; ++l) { var a = attrs[l]; s.push(a.name + ':' + a.value); } ; return s;", webElement);
		    for (Object o : attributes) {
		      seleniumWorker.println(o);
		    }
		    */
		worker.println("The value from the DOM ELEMENT is '"+value+"'");
		if (value!=null && value.equals(nodeValue)) {
			worker.println("IDENTICAL VALUE so no update.");
		}
		else {			
			if (isClear) {
				webElement.clear();
			}
			webElement.sendKeys(nodeValue);
		}
		
		if (isENTERKey) {
			webElement.sendKeys(Keys.RETURN);
		}
		if (isTABKey) {
			webElement.sendKeys(Keys.TAB);
		}
		
		if (waitForPageToFinishRendering) {
			WebDriverWait webDriverWait=((SeleniumWorker)worker).getWebDriverWait();
			SeleniumUtils.waitForPageToFinishRendering(((SeleniumWorker)worker),webDriverWait);
			worker.println("!!!!! waitForPageToFinishRendering DETECTED so the current webelement will be RESET. !!!!!");
			((SeleniumWorker)worker).setCurrentWebElement(null);
		}
	}
	
}
