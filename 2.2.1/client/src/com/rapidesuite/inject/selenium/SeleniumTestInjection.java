package com.rapidesuite.inject.selenium;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

public class SeleniumTestInjection {
	
	public final static String url="https://fusion01.rapidesuite.com:18614/homePage";
	//public final static String taskName="Assign Business Unit Business Function";
	public final static String taskName="Manage Remit to Addresses";

	public final static long sleepTimeInMs=2000;
	
	private WebDriver webDriver;
	private WebDriverWait webDriverWait;
	
	
	public SeleniumTestInjection() {
		
	}
	
	public static void main(String[] args)
	{
		try
		{
			SeleniumTestInjection seleniumTestInjection=new SeleniumTestInjection();
			//seleniumTestInjection.testSaveAndCloseButton();
			//seleniumTestInjection.testScrollBarBottomDetection();
			//seleniumTestInjection.testRegex();
			seleniumTestInjection.testRemoteDriverLinuxVM();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void waitForPageToFinishRendering(WebDriverWait wait) {
		wait.until(new Function<WebDriver,Boolean>() {
			public Boolean apply(WebDriver d) {
				JavascriptExecutor js = (JavascriptExecutor) d;
				Boolean isReady = (Boolean)js.executeScript("return AdfPage.PAGE.isSynchronizedWithServer()");
				return isReady;
			}
		});		
	}
		
	public void loginAndNavigateToTask() throws InterruptedException {
		System.setProperty("webdriver.firefox.bin","C:/Program Files (x86)/Mozilla Firefox 10/firefox.exe");
		FirefoxProfile fp = new FirefoxProfile();
		//fp.setPreference("webdriver.load.strategy", "unstable");
		webDriver = new FirefoxDriver(fp);
		//webDriver = new FirefoxDriver();
		//webDriver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		webDriver.manage().window().maximize();
		webDriver.get(url);
		webDriverWait = new WebDriverWait(webDriver,sleepTimeInMs);
		//webDriver.manage().timeouts().pageLoadTimeout(3, TimeUnit.SECONDS);
		//webDriver.manage().timeouts().setScriptTimeout(3, TimeUnit.SECONDS);
		
		WebElement webElement =webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("userid")));
		webElement.sendKeys("RAPID1");
		webElement =webDriver.findElement(By.xpath("//input[@name='password']"));
		webElement.sendKeys("RSCuser1!");
		webElement =webDriver.findElement(By.xpath("//button[@type='submit']"));
		System.out.println(new Date()+" "+"before click submit");
		webElement.click();	
		System.out.println(new Date()+" "+"before find image2");
		
		//webElement = getElementById(webDriver,"pt1:_UIScmil1u::icon");
		//webElement = getElementById(webDriver,"pt1:_UIScmil1u");
		//webElement = getElementById(webDriver,"pt1:_UISmmLink");
		//webElement =webDriver.findElement(By.xpath("//img[@id='pt1:_UIScmil1u::icon']"));
		webElement =webDriver.findElement(By.xpath("//img[contains(@src,'menuarrow_ena.png')]/.."));
		//List<WebElement> webElements =webDriver.findElements(By.xpath("//img[contains(@src,'menuarrow_ena.png')]/.."));
		//webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		//System.out.println(new Date()+" "+"webElements.size(): "+webElements.size());
		//webElement=webElements.get(0);
		
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		webElement =webDriver.findElement(By.xpath("//a[text()='Setup and Maintenance...']"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		webElement =webDriver.findElement(By.xpath("//a[text()='Manage Implementation Projects']"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		webElement =webDriver.findElement(By.xpath("//a[text()='Implementation Project - PD']"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		webElement =webDriver.findElement(By.xpath("//tr[td/label = 'Task']//input"));
		webElement.sendKeys(taskName);
		webElement =webDriver.findElement(By.id("pt1:USma:0:MAnt1:1:pt1:r1:1:topAppPanel:applicationsTable1:_ATTp:s38:cil1"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		webElement =webDriver.findElement(By.xpath("//a[text()='"+taskName+"']"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		webElement =webDriver.findElement(By.id("pt1:USma:0:MAnt1:1:pt1:r1:1:topAppPanel:applicationsTable1:_ATTp:table1:3:cil2"));
		//webElement =webDriver.findElement(By.xpath("//span[text()[contains(.,'"+taskName+"')]]/../../..//a[@title='Go to Task']"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		
	}

	public void testSaveAndCloseButton() throws InterruptedException {
		loginAndNavigateToTask();	
		WebElement webElement=null;
		
		webElement =webDriver.findElement(By.id("pt1:USma:0:MAnt2:1:r2:0:dynamicRegion1:0:ap1:AT1:_ATp:table1:0:enableCheckbox::content"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		
		webElement =webDriver.findElement(By.id("pt1:USma:0:MAnt2:1:r2:0:dynamicRegion1:0:ap1:primaryLedgerNameId::lovIconId"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		
		//white-space: nowrap
		webElement =webDriver.findElement(By.xpath("//td[text()='DEMOM3']/.."));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		
		webElement =webDriver.findElement(By.xpath("//a[@onclick='this.focus();return false']"));
		webElement.click();
	}
	
	private static long getIntAttribute(WebDriver webDriver,WebElement webElement,String attribute) {
		JavascriptExecutor js = (JavascriptExecutor) webDriver;
		String command="return arguments[0]."+attribute+";";
		long toReturn=(Long)js.executeScript(command,webElement);
		return toReturn;
	}
		
	public void testScrollBarBottomDetection() throws InterruptedException {
		loginAndNavigateToTask();	
		WebElement webElement=null;
		
		webElement =webDriver.findElement(By.xpath("//td[text()='Search']/../../../../../..//input"));
		//webElement.sendKeys("United States");
		webElement =webDriver.findElement(By.xpath("//td[text()='Search']/../..//a[@title='Search']"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		
		webElement =webDriver.findElement(By.xpath("//div[@id='pt1:USma:0:MAnt2:1:r2:0:dynamicRegion1:0:AP1:AT1:_ATp:table1::scroller']"));
		
		int cnt=0;
		while (cnt<10) {
			cnt++;
			
			long offsetHeight=getIntAttribute(webDriver,webElement,"offsetHeight");
			long scrollTop=getIntAttribute(webDriver,webElement,"scrollTop");
			long scrollHeight=getIntAttribute(webDriver,webElement,"scrollHeight");
			System.out.println(new Date()+" "+"BEFORE PAGE DOWN cnt: "+cnt+" offsetHeight: "+offsetHeight+" scrollTop: "+scrollTop+" scrollHeight: "+scrollHeight);
			/*
			
			*/
			webElement.sendKeys(Keys.PAGE_DOWN);
			//waitForPageToFinishRendering(webDriverWait);
			System.out.println(new Date()+" "+"wait for completion..."); 
			SeleniumUtils.waitForPageToFinishRendering(null,webDriverWait);
			 
			//Thread.sleep(2000);
			
			offsetHeight=getIntAttribute(webDriver,webElement,"offsetHeight");
			scrollTop=getIntAttribute(webDriver,webElement,"scrollTop");
			scrollHeight=getIntAttribute(webDriver,webElement,"scrollHeight");
			System.out.println(new Date()+" "+"AFTER PAGE DOWN cnt: "+cnt+" offsetHeight: "+offsetHeight+" scrollTop: "+scrollTop+" scrollHeight: "+scrollHeight);
			
			if (offsetHeight+scrollTop>=scrollHeight) {
				int cnt1=0;
				long currentScrollHeight=scrollHeight;
				while (cnt1<10) {
					cnt1++;
					scrollHeight=getIntAttribute(webDriver,webElement,"scrollHeight");
					if (currentScrollHeight != scrollHeight) {
						System.out.println(new Date()+" "+"CHANGED!!!! cnt: "+cnt+" offsetHeight: "+offsetHeight+" scrollTop: "+scrollTop+" scrollHeight: "+scrollHeight);
						break;
					}
					System.out.println(new Date()+" "+"SAME SAME so retrying until max tries cnt: "+cnt+" offsetHeight: "+offsetHeight+" scrollTop: "+scrollTop+" scrollHeight: "+scrollHeight);
				}
			}

			if (offsetHeight+scrollTop>=scrollHeight) {
				//SeleniumUtils.showAlert(webDriver,"END OF THE WORLD BY MIK DEM");
				System.out.println(new Date()+" "+"END OF THE WORLD BY MIK DEM");
				
				//Thread.sleep(2000);
				
				break;
			}
			
			//Thread.sleep(2000);
			//webElement =webDriver.findElement(By.xpath("//div[@id='pt1:USma:0:MAnt2:1:r2:0:dynamicRegion1:0:AP1:AT1:_ATp:table1::scroller']"));
		}
	}
	
	
	public void testRegex() throws InterruptedException {
		boolean isCaseSensitive=true;
		/*
		System.setProperty("webdriver.firefox.bin","C:/Program Files (x86)/Mozilla Firefox 10/firefox.exe");
		webDriver = new FirefoxDriver();
		webDriver.manage().window().maximize();
		webDriver.get(url);
		webDriverWait = new WebDriverWait(webDriver,sleepTimeInMs);
		WebElement webElement =webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("userid")));
		//getElementWithText(webDriver,"LABEL","USER ID",isCaseSensitive);
		*/
		
		System.setProperty("webdriver.firefox.bin","C:/Program Files (x86)/Mozilla Firefox 10/firefox.exe");
		webDriver = new FirefoxDriver();
		webDriver.manage().window().maximize();
		webDriver.get("file:///D:/TaskListManagerTop.htm");
		
		/*
		loginAndNavigateToTask();
		WebElement webElement =webDriver.findElement(By.xpath("//td[text()='Search']/../../../../../..//input"));
		webElement =webDriver.findElement(By.xpath("//td[text()='Search']/../..//a[@title='Search']"));
		webElement.click();
		waitForPageToFinishRendering(webDriverWait);
		*/
		WebElement  webElement =getElementWithText(webDriver,"TD","##WILDCARD##FFF##WILDCARD##",isCaseSensitive);
		//line2-##WILDCARD##-[^$[##WILDCARD##]{}')CODE
		webElement.click();
	}

	private static WebElement getElementWithText(WebDriver webDriver,String tagName,String textToFind,boolean isCaseSensitive) {
		JavascriptExecutor js = (JavascriptExecutor) webDriver;
		String command=
		"var elts=document.getElementsByTagName('"+tagName+"');\n";
		
		textToFind = textToFind.replaceAll("'", "\\\\'");
				
		command=command+"var inputText='"+textToFind+"';\n";	
		command=command+"inputText=inputText.replace(/[-\\/\\\\^$*+?.()|[\\]{}]/g, '\\\\$&');\n";
		command=command+"inputText=inputText.replace(/##WILDCARD##/g,'.*');\n";
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
		
		System.out.println("executing: "+command);
		WebElement toReturn=(WebElement)js.executeScript(command);
		System.out.println("toReturn:"+toReturn);
		if (toReturn!=null) {
			System.out.println("toReturn.getTagName():"+toReturn.getTagName());
			System.out.println("toReturn.getText():"+toReturn.getText());
		}
		return toReturn;
	}
	
	public void testRemoteDriverLinuxVM() throws InterruptedException, MalformedURLException {
		/*
		NODE: /usr/java/jre1.7.0_65/bin/java -jar selenium-server-standalone-2.46.0.jar -role wd -hub http://192.188.172.183:4444/grid/register -browser browserName=firefox,maxInstances=1
		HUB: java -jar lib/selenium-server-standalone-2.44.0.jar -role hub
		*/
						
		FirefoxProfile profile = new FirefoxProfile();
		DesiredCapabilities capabilities = DesiredCapabilities.firefox();
		capabilities.setCapability(FirefoxDriver.PROFILE, profile);
		URL urlHub = new URL("http://192.188.172.183:4444/wd/hub");
		System.out.println("before wedbridver: url:"+url);
		webDriver= new RemoteWebDriver(urlHub,capabilities);
		webDriver.manage().window().maximize();
		webDriver.get("http://www.google.com");
		
		System.out.println("after wedbridver");
	}
	
}
