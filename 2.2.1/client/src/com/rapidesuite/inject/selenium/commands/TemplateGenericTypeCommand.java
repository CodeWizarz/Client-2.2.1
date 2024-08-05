package com.rapidesuite.inject.selenium.commands;

import org.apache.xmlbeans.XmlObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateGenericTypeCommand extends Command{

	public TemplateGenericTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}
	
	/*
	public static void processGeneric(Worker worker,WebDriver webDriver,String lookupSourceTag,String lookupSourceAttribute,String lookupSourceText,
			boolean skipIfKBBlank,String nodeValue,String lookupTargetAttribute,boolean isPageLoadingAfterSet,XmlObject xmlObject,
			int position) throws Exception
	{
		//worker.println("skipIfKBBlank: "+skipIfKBBlank+" nodeValue: '"+nodeValue+"'");
		if (skipIfKBBlank && nodeValue.isEmpty()) {
			worker.println("Empty value in KB so skipping this tag.");
			return;
		}		
		
		List<WebElement> lookupSourceWebElements=null;
		if (((SeleniumWorker)worker).isRelativeWebElementReferenceInUse()) {
			lookupSourceWebElements=((SeleniumWorker)worker).getRelativeWebElement().findElements(By.tagName(lookupSourceTag));
		}
		else {
			lookupSourceWebElements =webDriver.findElements(By.tagName(lookupSourceTag));
		}
		if (lookupSourceWebElements.isEmpty()) {
			throw new Exception("Unable to find any HTML elements with tag name: '"+lookupSourceTag+"'");
		}
		worker.println("Found "+lookupSourceWebElements.size()+" lookup '"+lookupSourceTag+"' source elements to analyze...");
		//worker.println("Looking for text node: '"+lookupSourceText+"'");
		int positionCounter=0;
		for (WebElement lookupSourceWebElement:lookupSourceWebElements) {
			//String concatenatedTextNodesTemp=lookupSourceWebElement.getText();
			//worker.println("###### element ########");
			//SeleniumUtils.printElement(((SeleniumWorker)worker),webDriver,lookupSourceWebElement);
			
			String innerHtml = (String)((JavascriptExecutor)webDriver).executeScript("return arguments[0].innerHTML;", lookupSourceWebElement);
			//worker.println("Source element text node: '"+innerHtml+"'");
			if (innerHtml.trim().equalsIgnoreCase(lookupSourceText)) {
				//worker.println("Matching text node!");
				
				String lookupSourceAttributeValue=lookupSourceWebElement.getAttribute(lookupSourceAttribute);
				if (lookupSourceAttributeValue==null) {
					throw new Exception("Unable to find the attribute name: '"+lookupSourceAttribute+"'");
				}
				
				if (lookupTargetAttribute.equalsIgnoreCase("ID")) {
					try{
						WebElement targetWebElement=null;
						if (((SeleniumWorker)worker).isRelativeWebElementReferenceInUse()) {
							targetWebElement=((SeleniumWorker)worker).getRelativeWebElement().findElement(By.id(lookupSourceAttributeValue));
						}
						else {
							targetWebElement =webDriver.findElement(By.id(lookupSourceAttributeValue));
						}
						if (targetWebElement==null) {
							throw new Exception("Unable to find any HTML elements with attribute name/value: '"+lookupTargetAttribute+"'/'"+lookupSourceAttributeValue+"'");
						}
						
						positionCounter++;
						if (position!=-1) {
							if (position!=positionCounter) {
								worker.println("skipping element because out of position, position found: "+positionCounter+" position to look for: "+
										position);
								continue;
							}
						}
						
						processAction(worker,webDriver,targetWebElement,nodeValue,isPageLoadingAfterSet,xmlObject);
						return;
						
					}
					catch(org.openqa.selenium.NoSuchElementException exception) {
						//FileUtils.printStackTrace(exception);
						worker.println("No target Element with ID attribute so trying another source element");
					}
				}
				else {
					// TODO: use XPATH - this is going to be slower
				}
			}
		}
		throw new Exception("Unable to find any matching HTML element on the page");
	}
	*/
	
	public static void processAction(Worker worker,WebDriver webDriver,WebElement webElement,String nodeValue,boolean isPageLoadingAfterSet
			,XmlObject xmlObject) throws Exception{
		if (worker.getScriptManager().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isWebElementHighlight()) {
			SeleniumUtils.elementHighlight(webDriver,webElement);
		}
		SeleniumUtils.setFocus(webDriver,webElement);
		((SeleniumWorker)worker).setCurrentWebElement(webElement);
		
		if (webElement.getTagName().equalsIgnoreCase("SELECT")) {
			//worker.println("SELECT processing...");
			SelectTypeCommand.processGeneric((SeleniumWorker)worker,webElement,nodeValue,isPageLoadingAfterSet,true);
		}
		else
			if (webElement.getTagName().equalsIgnoreCase("INPUT") ||
				webElement.getTagName().equalsIgnoreCase("TEXTAREA")) {
				//worker.println("INPUT processing...");
				
				String typeAttribute=webElement.getAttribute("type");
				if (typeAttribute!=null && typeAttribute.equalsIgnoreCase("checkbox")) {
					boolean isCheckbox=true;
					boolean isSkipBrowserTitleChange=true;
					
					ClickTypeCommand.processGeneric(worker,isPageLoadingAfterSet,isCheckbox,isSkipBrowserTitleChange,xmlObject);
				}
				else {
					boolean isClear=true;
					boolean isENTERKey=false;
					boolean isTABKey=false;	
					SendKeysTypeCommand.processGeneric(worker,webElement,nodeValue,isClear,isENTERKey,isTABKey,isPageLoadingAfterSet);
				}
			}
			else {
				throw new Exception("Unsupported tag name: '"+webElement.getTagName()+"'");
			}
	}
	
}