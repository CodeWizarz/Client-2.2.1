package com.rapidesuite.inject.selenium.commands;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.rapidesuite.inject.Worker;

public class TemplateUtils {

	public static WebElement getGridSectionWebElement(WebDriver webDriver,Worker worker,String gridTitle) throws Exception {
		WebElement webElement=getGenericSectionWebElement(webDriver,worker,gridTitle,"h1");
		if (webElement==null) {
			webElement=getGenericSectionWebElement(webDriver,worker,gridTitle,"h2");
		}
		if (webElement==null) {
			webElement=getGenericSectionWebElement(webDriver,worker,gridTitle,"h3");
		}
		if (webElement==null) {
			throw new Exception("Unable to find the grid heading section '"+gridTitle+"'");
		}
		return webElement;
	}
	
	/*
	 * Get the common lowest ancestor to 2 xpaths
	 */
	public static WebElement getGenericSectionWebElement(WebDriver webDriver,Worker worker,String sectionTitle,String tagName) {
		/* http://stackoverflow.com/questions/538293/find-common-parent-using-xpath
			Use the following XPath 1.0 expression:

				$v1/ancestor::*
				   [count(. | $v2/ancestor::*) 
				   = 
				    count($v2/ancestor::*)
				   ]
				    [1]

		where $v1 and $v2 hold the two text nodes (in case you use XPath not within XSLT, you will have to replace $v1 and $v2 
		in the above expression with the XPath expressions that select each one of these two text nodes).
		Explanation:
		The above XPath 1.0 expression finds the intersection of two node-sets: the node-set of all element ancestors 
		of $v1 and the node-set of all element ancestors of $v2. This is done with the so called Kaysian method for 
		intersection (after Michael Kay, who discovered this in 2000). Using the Kaysian method for intersection,
		the intersection of two nodesets, $ns1 and $ns2 is selected by the following XPath expression:
	  		$ns1[count(. | $ns2) = count($ns2)]
		Then, from the intersection of the ancestors we must select the last element. However, because we are using 
		a reverse axis (ancestor), the required node position must be denoted as 1.
		*/
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
	
	public static WebElement getWebElement(WebDriver webDriver,Worker worker,WebElement rootWebElement,
			String xpath,boolean isForceUseDriver,boolean isLocalReferenceAlreadyAddedInXPATH,boolean isDisplayedOnly) {
		WebElement webElement=null;
		if (rootWebElement!=null && !isForceUseDriver) {
			if (!isLocalReferenceAlreadyAddedInXPATH) {
				xpath="."+xpath;
			}
			worker.println("Searching with xpath: '"+xpath+"' using rootWebElement");
			webElement=rootWebElement.findElement(By.xpath(xpath));
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
	
	public static WebElement getWebElement(WebDriver webDriver,Worker worker,WebElement rootWebElement,String xpath) {
		return getWebElement(webDriver,worker,rootWebElement,xpath,false,false,true);
	}
	
}
