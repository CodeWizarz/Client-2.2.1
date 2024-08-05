package com.rapidesuite.inject.selenium;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.BatchInjectionTracker;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.Worker;
import com.erapidsuite.configurator.navigation0005.NavigationDocument;
import com.erapidsuite.configurator.navigation0005.RepeatType;

public class SeleniumUtils {

	
	public static void openWebpage(String urlString) {
	    try {
	        Desktop.getDesktop().browse(new URL(urlString).toURI());
	    } 
	    catch (Exception e) {
	    	FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("error: "+e.getMessage());
	    }
	}

	public static void printElement(SeleniumWorker seleniumWorker,WebDriver webDriver,WebElement webElement) {
		if (webElement==null) {
			seleniumWorker.println("N/A");
			return;
		}
		try{
			seleniumWorker.println("- Tag name: "+webElement.getTagName());
			String innerHtml = (String)((JavascriptExecutor)webDriver).executeScript("return arguments[0].innerHTML;", webElement);
			seleniumWorker.println("- innerHtml:#"+innerHtml+"#");
			//String outerHtml = (String)((JavascriptExecutor)webDriver).executeScript("return arguments[0].outerHtml;", webElement);
			//seleniumWorker.println("- outerHtml:"+outerHtml);		
			ArrayList<?> attributes = (ArrayList<?>) ((JavascriptExecutor)webDriver).executeScript("var s = []; var attrs = arguments[0].attributes;"+
					"for (var l = 0; l < attrs.length; ++l) { var a = attrs[l]; s.push(a.name + ':' + a.value); } ; return s;", webElement);
			seleniumWorker.println("- attributes:"+attributes);
		}
		catch(org.openqa.selenium.StaleElementReferenceException e) {
			FileUtils.printStackTrace(e);
			seleniumWorker.println("WARNING: StaleElementReferenceException!!!! UNABLE TO PRINT THE ELEMENT");
		}
	}
	
	public static void elementHighlight(WebDriver webDriver,WebElement element) {
		//for (int i = 0; i < 2; i++) {
			JavascriptExecutor js = (JavascriptExecutor) webDriver;
			js.executeScript("arguments[0].setAttribute('style', arguments[1]);",element, "color: blue; border: 3px solid blue;");
			//js.executeScript("arguments[0].setAttribute('style', arguments[1]);",element, "");
		//}
	}
	
	public static void waitForPageToFinishRendering(final SeleniumWorker seleniumWorker,WebDriverWait wait) {
		int counter=0;
		while (true) {
			counter++;
			seleniumWorker.println("Calling waitForPageToFinishRendering, counter: "+counter);
			try {
				waitForPageToFinishRenderingGeneric(seleniumWorker,wait);
				break;
			}
			catch(TimeoutException e) {
				seleniumWorker.println("TimeoutException received from Selenium, so retrying...");
				seleniumWorker.printStackTrace(e);
			}
		}
	}
	
	public static void waitForPageToFinishRenderingGeneric(final SeleniumWorker seleniumWorker,WebDriverWait wait) {
		wait.until(new Function<WebDriver,Boolean>() {
			public Boolean apply(WebDriver d) {
				JavascriptExecutor js = (JavascriptExecutor) d;
				Boolean isReady =false;
				try{
					String jsExpression= "return "+
							"typeof AdfPage !== 'undefined' && " +
							"typeof AdfPage.PAGE !== 'undefined' && " +
							"typeof AdfPage.PAGE.isSynchronizedWithServer === 'function' && " +
							"(AdfPage.PAGE.isSynchronizedWithServer() || " +
							"(typeof AdfPage.PAGE.whyIsNotSynchronizedWithServer === 'function' && " +
							"AdfPage.PAGE.whyIsNotSynchronizedWithServer()))";
					
					Object result = js.executeScript(jsExpression);
					if (seleniumWorker!=null) {
						seleniumWorker.println("waitForPageToFinishRendering, message: '"+result+"'");
					}
					isReady =Boolean.TRUE.equals(result);
				}
				catch(org.openqa.selenium.WebDriverException e) {
					if (seleniumWorker!=null) {
						seleniumWorker.println("e.getMessage():"+e.getMessage()+"#");
					}
					if (e.getMessage()!=null &&
							(e.getMessage().toLowerCase().startsWith("AdfPage is not defined".toLowerCase()) ||
							 e.getMessage().toLowerCase().startsWith("AdfPage.PAGE is null".toLowerCase()) ||
							 e.getMessage().toLowerCase().startsWith("waiting for doc.body failed".toLowerCase())
									)	
					) {
						if (seleniumWorker!=null) {
							seleniumWorker.println("'ADFPAGE IS NOT DEFINED' DETECTED SO RETRYING...");
						}
						seleniumWorker.println("IGNORING THIS ERROR!");
					}
					else {
						throw e;
					}
				}
				return isReady;
			}
		});		
	}
			
	public static void printAllMatchingElements(SeleniumWorker seleniumWorker,WebDriver webDriver,String xpath) {
		List<WebElement> webElements=webDriver.findElements(By.xpath(xpath));
		printAllMatchingElementsGeneric(seleniumWorker,webDriver,webElements);
	}
	
	public static void printAllMatchingElements(SeleniumWorker seleniumWorker,WebDriver webDriver,WebElement webElement,String xpath) {
		List<WebElement> webElements=webElement.findElements(By.xpath(xpath));
		printAllMatchingElementsGeneric(seleniumWorker,webDriver,webElements);
	}
	
	private static void printAllMatchingElementsGeneric(SeleniumWorker seleniumWorker,WebDriver webDriver,List<WebElement> webElements) {
		seleniumWorker.println("%%%%%%%%%%%%%%%% DEBUG PRINT MATCHING ELEMENTS %%%%%%%%%%%%%%%%%%%%%%%");
		seleniumWorker.println("matching webElements size: "+webElements.size());
		for (int i=0;i<webElements.size();i++) {
			WebElement webElement=webElements.get(i);
			seleniumWorker.println("$$ MATCHING ELEMENT AT POSITION: "+(i+1)+" $$");
			printElement(seleniumWorker,webDriver,webElement);
		}
		seleniumWorker.println("%%%%%%%%%%%%%%%% DEBUG PRINT MATCHING ELEMENTS COMPLETED %%%%%%%%%%%%%%%%%%%%%%%");
	}

	public static void showAlert(WebDriver webDriver,String message) throws InterruptedException 
	{  
		JavascriptExecutor js = (JavascriptExecutor) webDriver;
		String text=StringEscapeUtils.escapeJava(message);
		text=text.replaceAll("'","\"");
		js.executeScript("alert('"+text+"');");
		//Thread.sleep(2000);
		//webDriver.switchTo().alert().accept();
	}

	public static void captureScreenShot(WebDriver webDriver,File file) throws IOException {
		if (webDriver!=null) {
			File screenshotFile=((TakesScreenshot)webDriver).getScreenshotAs(OutputType.FILE);
			org.apache.commons.io.FileUtils.copyFile(screenshotFile,file);
		}
	}

	public static void changeBrowserTitle(WebDriver webDriver,int scriptId,SeleniumWorker seleniumWorker) {
		try{
			if (webDriver!=null) {
			((JavascriptExecutor) webDriver).executeScript("var resTitle=document.getElementsByTagName('title')[0].innerHTML;"+
				" if (resTitle.indexOf(\""+SeleniumWorker.BROWSER_PREFIX+"\")==-1) document.getElementsByTagName('title')[0].innerHTML='"+
				" SCRIPT: "+scriptId+" / "+SeleniumWorker.BROWSER_PREFIX+
				" "+seleniumWorker.getWorkerId()+" - '+resTitle;");
			}
		}
		catch(Throwable e) {
			FileUtils.printStackTrace(e);
		}
	}	
	
	public static String getRepeatTypeFriendlyXML(RepeatType repeatType) {
		String inventoryName=repeatType.getInventoryName();
		return "<repeat inventoryName=\""+inventoryName+"\" </repeat>";
	}
	
	public static String formatCommand(String fullCommand) {
		fullCommand=fullCommand.replaceAll("\\n", "");
		fullCommand=fullCommand.replaceAll("\\r", "");
		fullCommand=fullCommand.replace("xmlns:xsd=\"http://xsdutility.configurator.erapidsuite.com\"", "");
		fullCommand=fullCommand.replace("xmlns:nav=\"http://navigation0005.configurator.erapidsuite.com\"", "");
		fullCommand=fullCommand.replace("xmlns=\"http://navigation0005.configurator.erapidsuite.com\"", "");
		return fullCommand;
	}
	
	public static String getAvailableRandomPortNumber() {
		while (true) {
			try{
				ServerSocket s = new ServerSocket(0);
				int port=s.getLocalPort();
				FileUtils.println("listening on port: " +port);
				s.close();
				return ""+port;
			} 
			catch (IOException ex) {
				FileUtils.printStackTrace(ex);
				FileUtils.println("Error opening random port number, retrying...");
				try {
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) {
					FileUtils.printStackTrace(e);
				}
			}
		}
	}
	
	public static InetAddress getCurrentIp() throws Exception {
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
			Enumeration<InetAddress> nias = ni.getInetAddresses();
			while(nias.hasMoreElements()) {
				InetAddress ia= (InetAddress) nias.nextElement();
				if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()	&& ia instanceof Inet4Address) {
					return ia;
				}
			}
		}
		throw new Exception("Unable to find local IP Address.");
	}

	public static long getIntAttribute(WebDriver webDriver,WebElement webElement,String attribute) {
		JavascriptExecutor js = (JavascriptExecutor) webDriver;
		String command="return arguments[0]."+attribute+";";
		long toReturn=(Long)js.executeScript(command,webElement);
		return toReturn;
	}
	
	public static WebElement getElementWithText(SeleniumWorker seleniumWorker,WebDriver webDriver,List<WebElement> webElements,String tagName,String textToFind,boolean isCaseSensitive) {
		for (WebElement webElement:webElements) {
			WebElement webElementFound=getElementWithTextGeneric(seleniumWorker,webDriver,webElement,tagName,textToFind,isCaseSensitive);
			if (webElementFound!=null) {
				return webElementFound;
			}
		}
		return null;
	}
	
	public static WebElement getElementWithTextGeneric(SeleniumWorker seleniumWorker,WebDriver webDriver,WebElement webElement,String tagName,String textToFind,boolean isCaseSensitive) {
		JavascriptExecutor js = (JavascriptExecutor) webDriver;
		String command="";
				
		if (webElement==null) {
			command=command+"var elts=document.getElementsByTagName('"+tagName+"');\n";
		}
		else {
			command=command+"var elts=arguments[0].getElementsByTagName('"+tagName+"');\n";
		}
		
		textToFind = textToFind.replaceAll("'", "\\\\'");
				
		command=command+"var inputText='"+textToFind+"';\n";	
		command=command+"inputText=inputText.replace(/[-\\/\\\\^$*+?.()|[\\]{}]/g, '\\\\$&');\n";
		command=command+"inputText=inputText.replace(/"+SeleniumWorker.WILDCARD_KEYWORD+"/g,'.*');\n";
		command=command+"inputText='^'+inputText+'$';\n";	
		
		command=command+"var regExpr=new RegExp(inputText";
		if (!isCaseSensitive) {		
			command=command+", 'i'";		
		}
		command=command+");\n";
				
		command=command+"for(var i=0;i<elts.length;i++) {"+
        "	var tempElt=elts[i];"+
		"   var text=tempElt.textContent;"+
		"  	if (regExpr.test(text) ) {"+
		"  		return tempElt;"+
		"  		break;"+
		"   }"+
		"}"+
		"return null;";
		
		//seleniumWorker.println("executing: "+command);
		WebElement toReturn=null;
		if (webElement==null) {
			toReturn=(WebElement)js.executeScript(command);
		}
		else {
			toReturn=(WebElement)js.executeScript(command,webElement);
		}
		seleniumWorker.println("webElement found: "+toReturn);
		if (toReturn!=null) {
			seleniumWorker.println("web element tagName: '"+toReturn.getTagName()+"' text: '"+toReturn.getText()+"'");
		}
		return toReturn;
	}

	public static void createErrorPopup(WebDriver webDriver,String header,String details) {
		if (webDriver==null) {
			return;
		}
		JavascriptExecutor js = (JavascriptExecutor) webDriver;
		
		String headerEscaped=StringEscapeUtils.escapeJava(header);
		headerEscaped=headerEscaped.replaceAll("'","\"");
		String detailsEscaped=StringEscapeUtils.escapeJava(details);
		detailsEscaped=detailsEscaped.replaceAll("'","\"");
		/*
		js.executeScript(
				"var elemDiv = document.createElement('div');"+
				"elemDiv.innerHTML = \"Click to show/ hide Message\";"+
				"elemDiv.style.cssText = 'cursor: pointer;color: white;z-index: 999999;display: block; position: absolute; left: 0px; top: 0px; border: solid black 2px; padding: 5px; background-color: red; text-align: justify; font-size: 16px; width: 240px;';"+
				"elemDiv.onclick = function() {   "+
				"	var popUpElt=document.getElementById('PopUp');"+
				"	if (popUpElt==null) {"+
				"		var elemDiv = document.createElement('div');"+
				"		elemDiv.id = \"PopUp\";"+
				//"		elemDiv.addEventListener('mouseover', function(){ document.getElementById('PopUp').style.display = 'none'  }, false);"+
				"		elemDiv.style.cssText = 'color: white;z-index: 999999;display: none; position: absolute; border: solid black 2px; padding: 10px; background-color: red; text-align: justify; font-size: 16px; width: 900px;';"+
				"		document.body.appendChild(elemDiv);"+
				"		var elemSpan = document.createElement('span');"+
				"		elemSpan.id = \"PopUpText\";"+
				"		elemDiv.appendChild(elemSpan);"+
				"		var el, x, y;"+
				"		el = document.getElementById('PopUp');"+
				"		x = 0;"+
				"		y = 50;"+
				"		el.style.left = x + \"px\";"+
				"		el.style.top = y + \"px\";"+
				"		el.style.display = \"block\";"+
				"		document.getElementById('PopUpText').innerHTML = '"+text+"';"+
				"	}"+
				"	else {"+
				"		if (popUpElt.style.display == 'none') {"+
				"			popUpElt.style.display = 'block';"+
				"		}"+
				"		else {"+
				"			popUpElt.style.display = 'none';"+
				"		}"+
				"	}"+
				"};"+
				"document.body.appendChild(elemDiv);"+
				"elemDiv.click();");
		*/
		js.executeScript(
		"var elemDivContainer = document.createElement('div');"+		
		"elemDivContainer.setAttribute(\"id\", \"container\");"+
		"elemDivContainer.style.cssText = 'min-height:100%; font-family: Verdana, Geneva, sans-serif;font-size: 13px;font-style: normal; z-index: 1;';"+
		"var elemDivBg = document.createElement('div');"+	
		"elemDivBg.setAttribute(\"id\", \"bg\");"+
		"elemDivBg.style.cssText =\"display:block; position: absolute; top: 0; left: 0; width: 100%; height: 100%;background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAVkAAAEYCAYAAAD29oUSAAAAIGNIUk0AAHomAACAhAAA+gAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAABc1JHQgCuzhzpAAAABGdBTUEAALGPC/xhBQAAAAlwSFlzAAALEwAACxMBAJqcGAAAPCBpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+Cjx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMDY3IDc5LjE1Nzc0NywgMjAxNS8wMy8zMC0yMzo0MDo0MiAgICAgICAgIj4KICAgPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICAgICAgPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIKICAgICAgICAgICAgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIgogICAgICAgICAgICB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIKICAgICAgICAgICAgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIKICAgICAgICAgICAgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIgogICAgICAgICAgICB4bWxuczpkYz0iaHR0cDovL3B1cmwub3JnL2RjL2VsZW1lbnRzLzEuMS8iCiAgICAgICAgICAgIHhtbG5zOnRpZmY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vdGlmZi8xLjAvIgogICAgICAgICAgICB4bWxuczpleGlmPSJodHRwOi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyI+CiAgICAgICAgIDx4bXA6Q3JlYXRvclRvb2w+QWRvYmUgUGhvdG9zaG9wIENDIDIwMTUgKE1hY2ludG9zaCk8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHhtcDpDcmVhdGVEYXRlPjIwMTUtMDktMTRUMTQ6NTg6NDcrMDc6MDA8L3htcDpDcmVhdGVEYXRlPgogICAgICAgICA8eG1wOk1ldGFkYXRhRGF0ZT4yMDE1LTA5LTE0VDE0OjU4OjQ3KzA3OjAwPC94bXA6TWV0YWRhdGFEYXRlPgogICAgICAgICA8eG1wOk1vZGlmeURhdGU+MjAxNS0wOS0xNFQxNDo1ODo0NyswNzowMDwveG1wOk1vZGlmeURhdGU+CiAgICAgICAgIDx4bXBNTTpJbnN0YW5jZUlEPnhtcC5paWQ6YmRjMTc3NGItMzIxNC00ZTI2LTk0NzItY2RiMjBjY2YwZDFlPC94bXBNTTpJbnN0YW5jZUlEPgogICAgICAgICA8eG1wTU06RG9jdW1lbnRJRD5hZG9iZTpkb2NpZDpwaG90b3Nob3A6YzMxZWZmZmYtOWI0NC0xMTc4LTkzMDAtZGNjZWU0YmE0Y2Q5PC94bXBNTTpEb2N1bWVudElEPgogICAgICAgICA8eG1wTU06T3JpZ2luYWxEb2N1bWVudElEPnhtcC5kaWQ6MTRmNDM4ZjItNmQ1YS00NWFiLWI0NGItMDUwMTdhMDVhYzExPC94bXBNTTpPcmlnaW5hbERvY3VtZW50SUQ+CiAgICAgICAgIDx4bXBNTTpIaXN0b3J5PgogICAgICAgICAgICA8cmRmOlNlcT4KICAgICAgICAgICAgICAgPHJkZjpsaSByZGY6cGFyc2VUeXBlPSJSZXNvdXJjZSI+CiAgICAgICAgICAgICAgICAgIDxzdEV2dDphY3Rpb24+Y3JlYXRlZDwvc3RFdnQ6YWN0aW9uPgogICAgICAgICAgICAgICAgICA8c3RFdnQ6aW5zdGFuY2VJRD54bXAuaWlkOjE0ZjQzOGYyLTZkNWEtNDVhYi1iNDRiLTA1MDE3YTA1YWMxMTwvc3RFdnQ6aW5zdGFuY2VJRD4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OndoZW4+MjAxNS0wOS0xNFQxNDo1ODo0NyswNzowMDwvc3RFdnQ6d2hlbj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OnNvZnR3YXJlQWdlbnQ+QWRvYmUgUGhvdG9zaG9wIENDIDIwMTUgKE1hY2ludG9zaCk8L3N0RXZ0OnNvZnR3YXJlQWdlbnQ+CiAgICAgICAgICAgICAgIDwvcmRmOmxpPgogICAgICAgICAgICAgICA8cmRmOmxpIHJkZjpwYXJzZVR5cGU9IlJlc291cmNlIj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OmFjdGlvbj5zYXZlZDwvc3RFdnQ6YWN0aW9uPgogICAgICAgICAgICAgICAgICA8c3RFdnQ6aW5zdGFuY2VJRD54bXAuaWlkOmJkYzE3NzRiLTMyMTQtNGUyNi05NDcyLWNkYjIwY2NmMGQxZTwvc3RFdnQ6aW5zdGFuY2VJRD4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OndoZW4+MjAxNS0wOS0xNFQxNDo1ODo0NyswNzowMDwvc3RFdnQ6d2hlbj4KICAgICAgICAgICAgICAgICAgPHN0RXZ0OnNvZnR3YXJlQWdlbnQ+QWRvYmUgUGhvdG9zaG9wIENDIDIwMTUgKE1hY2ludG9zaCk8L3N0RXZ0OnNvZnR3YXJlQWdlbnQ+CiAgICAgICAgICAgICAgICAgIDxzdEV2dDpjaGFuZ2VkPi88L3N0RXZ0OmNoYW5nZWQ+CiAgICAgICAgICAgICAgIDwvcmRmOmxpPgogICAgICAgICAgICA8L3JkZjpTZXE+CiAgICAgICAgIDwveG1wTU06SGlzdG9yeT4KICAgICAgICAgPHBob3Rvc2hvcDpEb2N1bWVudEFuY2VzdG9ycz4KICAgICAgICAgICAgPHJkZjpCYWc+CiAgICAgICAgICAgICAgIDxyZGY6bGk+YWRvYmU6ZG9jaWQ6cGhvdG9zaG9wOjU2YjBmMGUwLTk3NmYtMTE3OC1hZWY2LWRlZjcyMDNlMGNlZDwvcmRmOmxpPgogICAgICAgICAgICAgICA8cmRmOmxpPmFkb2JlOmRvY2lkOnBob3Rvc2hvcDo4ZGQxMTBmYS05NzU0LTExNzgtODgxOC05YjRkYmZlNjhhOWE8L3JkZjpsaT4KICAgICAgICAgICAgICAgPHJkZjpsaT5hZG9iZTpkb2NpZDpwaG90b3Nob3A6ZDA1NWE5Y2QtOGMzNC0xMTc4LTlkNWEtOTJjNTM1NGNlMzMzPC9yZGY6bGk+CiAgICAgICAgICAgICAgIDxyZGY6bGk+YWRvYmU6ZG9jaWQ6cGhvdG9zaG9wOmUwMTBjODU1LTk3NjktMTE3OC1hZWY2LWRlZjcyMDNlMGNlZDwvcmRmOmxpPgogICAgICAgICAgICA8L3JkZjpCYWc+CiAgICAgICAgIDwvcGhvdG9zaG9wOkRvY3VtZW50QW5jZXN0b3JzPgogICAgICAgICA8cGhvdG9zaG9wOkNvbG9yTW9kZT4zPC9waG90b3Nob3A6Q29sb3JNb2RlPgogICAgICAgICA8cGhvdG9zaG9wOklDQ1Byb2ZpbGU+c1JHQiBJRUM2MTk2Ni0yLjE8L3Bob3Rvc2hvcDpJQ0NQcm9maWxlPgogICAgICAgICA8ZGM6Zm9ybWF0PmltYWdlL3BuZzwvZGM6Zm9ybWF0PgogICAgICAgICA8dGlmZjpPcmllbnRhdGlvbj4xPC90aWZmOk9yaWVudGF0aW9uPgogICAgICAgICA8dGlmZjpYUmVzb2x1dGlvbj43MjAwMDAvMTAwMDA8L3RpZmY6WFJlc29sdXRpb24+CiAgICAgICAgIDx0aWZmOllSZXNvbHV0aW9uPjcyMDAwMC8xMDAwMDwvdGlmZjpZUmVzb2x1dGlvbj4KICAgICAgICAgPHRpZmY6UmVzb2x1dGlvblVuaXQ+MjwvdGlmZjpSZXNvbHV0aW9uVW5pdD4KICAgICAgICAgPGV4aWY6Q29sb3JTcGFjZT4xPC9leGlmOkNvbG9yU3BhY2U+CiAgICAgICAgIDxleGlmOlBpeGVsWERpbWVuc2lvbj4zNTM8L2V4aWY6UGl4ZWxYRGltZW5zaW9uPgogICAgICAgICA8ZXhpZjpQaXhlbFlEaW1lbnNpb24+MjgwPC9leGlmOlBpeGVsWURpbWVuc2lvbj4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgIDwvcmRmOlJERj4KPC94OnhtcG1ldGE+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgCjw/eHBhY2tldCBlbmQ9InciPz7MO7vpAAAAIXRFWHRDcmVhdGlvbiBUaW1lADIwMTU6MDk6MTQgMTU6MDM6MjJTM1o8AABNWUlEQVR4Xu3d+bbbxrG34diSLMnxFA+xnZx1LiJ/ntv6LvkkObblObE1WfZXT7uKgbbIvUmA41b91sIC2QTRA7rfrq5uAG/87W9/+39/aLVardZB9GbuW61Wq3UANWRbrVbrgGrItlqt1gHVkG21Wq0DqiHbarVaB1RDttVqtQ6ohmyr1WodUA3ZVqvVOqAasq1Wq3VANWRbrVbrgGrItlqtW6s333zzjbt3747N5ww+qhqyrVbr1qng+uDBgzfff//9ex9//PH9d955507+fFQ1ZFut1q1RwfWtt94acP3oo4/uf/LJJw9sAdl7fstDj6aGbKvVunhN4fruu+/eC6gOuIYF++DDDz+8L+ztt9++E5bt0a3Zhmyr1bpYrYPrp59+uoLrw4cPB1RfvHjxWxz35v379988tjXbkG21WhenCVzfeO+99+5ugutUjvc/WwYdRQ3ZVqt1MboKVxNa3AJ8r5vgSm+k7ty50z7ZVqvVuqopXP/4xz+u4Mpy/dOf/vTg7bffvpuHrlX8/w9cCvfu3Ts68xqyrVbrrFWANXHFWv3ss89egiuAbqM4R/tkW61W66rKCgVYboEPPvjg/i5wLXEVxHnuHNuabci2Wq2z1q+//voHFqh1r1YQLPGrgjW/7TGt2YZsq9U6awVkf7M3cTUCFsg5WLJgm0EHV0O21WqdvR4/fvzip59++oVVu0RcDCAbGn7eDD6oGrKtVuvs9ezZs1+fPHny4vnz5y8yaJa4GoD2mEu5GrKtVusiFID9FWzdvZVBO4u7wCoDluwvv/wy+zy7qCHbarXOXqAIsksASyzYYy/jasi2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');"+ 
		"background-repeat: repeat; z-index:100\";"+
		"elemDivContainer.appendChild(elemDivBg);"+
		"var elemDivMsgBox = document.createElement('div');"+		
		"elemDivMsgBox.setAttribute(\"id\", \"msgbox\");"+
		"elemDivMsgBox.style.cssText = 'display:block; margin: auto;position: absolute;top:75px; left: 50px;right: 50px;border:2px solid #000;padding: 20px 40px;background-color: #FFF;position: absolute;margin: 10% auto;width: 30%;z-index:200;';"+
		"var elemDivInner = document.createElement('div');"+		
		"elemDivInner.style.cssText = 'margin: 3% auto; width: 200px;padding: 5px 0px 5px 0px;text-align: center;font-size: 34px;font-weight: bold;color: #FFF;background-color:red;';"+
		"elemDivInner.innerHTML = \"ERROR!\";"+
		"elemDivMsgBox.appendChild(elemDivInner);"+
		"var elemP = document.createElement('p');"+	
		"elemP.innerHTML = '"+headerEscaped+"';"+
		"elemDivMsgBox.appendChild(elemP);"+
		"var elemP2 = document.createElement('p');"+
		"elemDivMsgBox.appendChild(elemP2);"+
		"elemP2.style.cssText = 'font-weight: bold;color:red; font-size: 16px;';"+
		"elemP2.innerHTML = 'ERROR: ';"+
		"var elemI = document.createElement('i');"+
		"elemI.innerHTML = '"+detailsEscaped+"';"+
		"elemP2.appendChild(elemI);"+
		
		"document.body.appendChild(elemDivContainer);"+
		"document.body.appendChild(elemDivMsgBox);"+
		
		"var elemDiv = document.createElement('div');"+
		"elemDiv.style.cssText = 'cursor: pointer;position: absolute;top: 75px;right: 50px;z-index: 300;';"+
		"var elemInput = document.createElement('input');"+
		"elemDiv.appendChild(elemInput);"+
		"elemInput.setAttribute(\"type\", \"image\");"+
		"elemInput.setAttribute(\"src\", \"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAQMAAAA/CAYAAADkHq2pAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAhdEVYdENyZWF0aW9uIFRpbWUAMjAxNTowOToxNCAxMTowOToyMCbP/2cAABIqSURBVHhe7Z13jBVVF8APsMBi7x27Yu+9fIoNO2rU2KNRg7EkakzEmBhLIv6jMSYiJvqHokYUsWABG4q99957b2ABWfZ8+3tnbrxe7rx98ubtgp5fctn3Zu7cN+2ePkOfEWddoFKHtrY2aR84QAYM6C9t/fpJnz59ijXzHp2dKh2zO2TWrK7W0SEdXW327M5ibTkcE4fV9W/XGJ1S94RUQFtbPxnU3i7t7QOl71yeT1Wt7esfM2bKjJl/dh3n7GKN48wdfbpu/Fbf+47jzAf0Lf46jvMfx4WB4zg1XBg4jlPDhYHjODVcGDiOU8OFgeM4NVwYOI5Tw4WB4zg1XBg4jlPDhYHjODVcGDiOU8OFgeM4NVwYOI5Tw4WB4zg1/j2PMK+5psgee4jstJPISiuJfPmlyJQpIvffL/LJJyKzZhUdHcfJ8e+wDBZcUGT11UU23lhk883t75AhIsstJ9LezttLio6O45TRu8KASdq3axfa2kT69xcZMEBk4ECbwHFjGevo06+fbRO2XWghkQ03FNllF5GttxZZbTXbZuZMkT/+MItA/f0tjtMdve8mLLqoyGKLiSyyiMgCC5iWZ/Iz6YHXeTGxf/9d5LffRH76SeSXX2wZfddZx1yDAw4Q2WQT2/bHH0UeeEDkttvMVWAbfy2Y49Sl54UBkxwtz+RfYQWR5ZcXWXllM+kXX1xkySVNKGAtdHaK/PmnyK+/ivz8s7VPP7V4AIJhmWVEtttO5H//E9lsM9vmm29EHntMZNIkkalTRT76yASBWweOU5eeFQZM8qWXFllqKZE11jC/HoGAMFh2WbMQaAgLhAaTGGGAVTBtmsj06SKffSby1Ve2DEGCNYB1gEXwwQcizz4rcu+99vfjj217x3G6pWeEAf49lgBBPibvWmuZIEAIMPmXWMJ8f4RAAE0eAn98ZlLj/2MdIBQQFFgSWAfEErAYsAYmTxZ54glzFVqdQSCDcfzxIkOH2rEh6AIIIoTTc8+JnHNOsbAOo0aJ7Ldf8aWL99831+e/yMiRIkcdZZ+xCrfZxj4HXn+9+FBw/vki48cXX0podJu5GbuMgw+27WM22KD4MA+CMGhZ69NHddAg1bXWUt13X9ULL1S9/37V995TnT5dtbNT/8Zvv6m+/77q44+rPvqo6ssvq379terMmUWHDLNnq378seo116gedJDq8svn96XqNnKk7W8jfPSR6sEH58cJbezYonPB66/n+/0X2oMPFiehi6efnnN9yogRc/ZJW0rZNimNjF3W2DYl128eaa3NJgwaZNp/++1N69G23dYi/lgCQfMTG8D0R4veeafIuHEit94q8vDDIq+9JvL999aHXY5hGTGBRx+1OMEXX1gmAYsBlyQEIasGzYUm5zcaYdVVRa67zgKdTvdstFHxoYv33is+OK2mdcKAibL22iK77Wbm0j77mIuAEJgxwwKAmPFM6K+/FnnqKYv+X3+9yIQJIg8+KPLqqxYjwFRMIW3IelwDCovoRwxh551F9t7b6g1wIVrBmWcWHxLeeMMaLkIK5+PKK4svTim4XrG79fjjxQen1VQvDND2aOf11rOKwOHDTSOuuKKlA995xyb+u++axmcS8/3uu83fxxJg8jMG8YQ4lsDYWAekCvHtsCLw51580dKMZBUOO0zk6KPNCkH4YCVQj1AVWAXxzQpXXWX7hj9Iw/JBKKVCYf31TTA65aTn5+qriw89BNcxbj39+71IayyDwYNFdtxRZM89RbbYwiY00f+XXzYt/tBDJgy++84mNbUAaABcBYQGk3rffa2QiMnF5AsTGmviww8tSIjwwLXAylhlFfutrbYyV4QxttzSxuOiVgUTOoYJf/LJxZcIXJdjjy2+RHBMc8OIEdaaFSZhHNq8SBwsxMqaXwjntCpXkOscxsRa6gGqFQZE9XENuOFxC7iwaGYyAEyOW24Rue8+EwSkCr/91lKA1AN8/rlN6AMPFDnmGIukM5nxt9H6QEYBy4JYAunDl14yQYCgwZqgEScgc7HppmaZ7LqryLrrWuqxFbBvZReLY0bwcVPz94YbRF55pVjZAIw7dqwd45gx1oilcN6wUBqFvghdrKowDo3vLK8nGPitWnipaLnfZfu4Dy13TjiWuA+uYEq8HRmVnibeP1q9c8O+3nHH36/PI49YHKvedmXE15vrHMYkbsKYxKlaSXLoc98WXFB17bVVTz5ZdeJE1V9+segpWYM7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\");"+
		"elemInput.onclick = function() {   "+
		"	if(document.getElementById('bg').style.display == 'block'){ "+
		"		document.getElementById('bg').style.display = 'none'; "+
		"		document.getElementById('msgbox').style.display = 'none'; "+
		"	} "+
		" 	else { "+
		"		document.getElementById('bg').style.display = 'block'; "+
		"		document.getElementById('msgbox').style.display = 'block'; "+
		"	} "+
		"};"+
		"document.body.appendChild(elemDiv);"+
		"");
	}
		
	public static void startLinuxFileBrowser(File folder)  {
		try {
			List<String> arguments=new ArrayList<String>();
			arguments.add("dolphin");
			arguments.add(folder.getAbsolutePath());
			
			String[] argumentsArray = arguments.toArray(new String[arguments.size()]);
			ProcessBuilder pb = new ProcessBuilder(argumentsArray);
			pb.directory(new File("."));
			pb.start();
		} 
		catch (Exception e) {
			e.printStackTrace();
			FileUtils.printStackTrace(e);
		}
	}
	
	public synchronized static RemoteWebDriver getRemoteWebDriverFactory(URL url,DesiredCapabilities capabilities) {
		/* Moving the creation into a synchronized static method in order to prevent:
		 *  0006369: RAPIDINJECT: org.openqa.selenium.WebDriverException: Unable to bind to locking port 7054 within 45000 ms
		 */
		return new RemoteWebDriver(url,capabilities);
	}
	
	public static String applyParameters(Worker worker,String text,Map<String, String> parameterNameToValueMap) {
		if (parameterNameToValueMap==null || parameterNameToValueMap.isEmpty()) {
			return text;
		}
		Iterator<String> iterator=parameterNameToValueMap.keySet().iterator();
		String toReturn=text;
		//worker.println("parameterNameToValueMap: "+parameterNameToValueMap.size());
		while (iterator.hasNext()) {
			String parameterName=iterator.next();
			//worker.println("parameterName: '"+parameterName+"'");
			if (toReturn.indexOf(parameterName)!=-1) {
				String parameterValue=parameterNameToValueMap.get(parameterName);
				worker.println("Replacing parameter name: '"+parameterName+"' by '"+parameterValue+"'");
				toReturn=toReturn.replaceAll(parameterName, parameterValue);
			}
		}
		return toReturn;
	}
	
	public static NavigationDocument getNavigationDocument(BatchInjectionTracker batchInjectionTracker,String navigationName) throws Exception {
		NavigationDocument navigationDocumentTemp=null;
		try{
			navigationDocumentTemp=InjectUtils.getExtraFusionNavigationDocument(batchInjectionTracker.getScriptGridTracker().getInjectMain(),
					batchInjectionTracker.getScriptGridTracker().getInjectMain().getApplicationInfoPanel().getInjectionPackage(),navigationName);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			throw new Exception("Invalid navigation to call: '"+navigationName+"', it is not found in the injection package. (error: "+e.getMessage()+")");
		}
		return navigationDocumentTemp;
	}
	
	public static void setFocus(WebDriver webDriver,WebElement webElement) {
		JavascriptExecutor js = (JavascriptExecutor) webDriver;
		String command="arguments[0].focus();";
		js.executeScript(command,webElement);
	}

	public static boolean evaluateBooleanXPATHExpression(WebDriver webDriver,String xpath) throws Exception {
		JavascriptExecutor js = (JavascriptExecutor) webDriver;
		//xpath="count(//a)=3";
		//xpath="count((//tr[1]//span[contains(@id,'AccountIterator')])//span[1])=3";
		Object toReturn=js.executeScript("var retBool = document.evaluate(arguments[0], document, null, XPathResult.ANY_TYPE, null );return retBool.booleanValue;",xpath);
		if (toReturn==null || !(toReturn instanceof Boolean) ) {
			throw new Exception("Internal error: invalid XPATH expression to evaluate. Please recheck the navigation!");
		}
		return ((Boolean)toReturn).booleanValue();
	}

}
