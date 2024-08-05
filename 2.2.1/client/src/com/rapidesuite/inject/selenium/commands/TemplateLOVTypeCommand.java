package com.rapidesuite.inject.selenium.commands;

import java.util.Iterator;
import java.util.TreeMap;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.erapidsuite.configurator.navigation0005.SearchLabelType;
import com.erapidsuite.configurator.navigation0005.TemplateLOVType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateLOVTypeCommand extends TemplateMain{

	private TreeMap<Integer, SearchLabelInformation> labelPositionToSearchLabelInformationMap;
	private SearchLabelInformation mainSearchLabelInformation;
	
	public TemplateLOVTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
	}

	public void process(TemplateLOVType templateLOVType) throws Exception {
		initLabelPositionToSearchLabelInformationMap(templateLOVType);
		
		/*
		 * Skip if the KB has blank values
		 */
		worker.println("Checking if KB has blank values...");
		boolean hasNoDataInKB=true;
		Iterator<Integer> iterator=labelPositionToSearchLabelInformationMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer position=iterator.next();
			SearchLabelInformation searchLabelInformation=labelPositionToSearchLabelInformationMap.get(position);
			String value=searchLabelInformation.value;
			if (value!=null && !value.isEmpty()) {
				hasNoDataInKB=false;
				break;
			}
		}
		if (hasNoDataInKB) {
			worker.println("Empty value(s) in KB so skipping this step.");
			return;
		}
		
		/*
		 * Validate if the value to update is the same as what is already set in the form (then skip)
		 */
		worker.println("Checking if KB has same value as defined in the form...");
		WebElement dropDownAnchorWebElement=findAnchorDropdownWebElement(templateLOVType);
		String dropDownAnchorIDAttributeValue=dropDownAnchorWebElement.getAttribute("id");
		String baseDropDownAnchorIDAttributeValue=dropDownAnchorIDAttributeValue.replace("::lovIconId","");
		worker.println("Searching for input with name: '"+baseDropDownAnchorIDAttributeValue+"'");
		
		WebDriver webDriver=((SeleniumWorker)worker).getWebDriver();
		WebElement inputWebElement=null;
		if (((SeleniumWorker)worker).isRelativeWebElementReferenceInUse()) {
			inputWebElement=((SeleniumWorker)worker).getRelativeWebElement().findElement(By.name(baseDropDownAnchorIDAttributeValue));
		}
		else {
			inputWebElement=webDriver.findElement(By.name(baseDropDownAnchorIDAttributeValue));
		}
		
		worker.println("Found!");
		SeleniumUtils.elementHighlight(webDriver,inputWebElement);
		
		String value=inputWebElement.getAttribute("value");
		worker.println("Comparing values, KB: '"+mainSearchLabelInformation.value+"' with FORM: '"+value+"'");
		if (value!=null && value.equals(mainSearchLabelInformation.value)) {
			worker.println("IDENTICAL VALUE so no update.");
			return;
		}				
		worker.println("Clicking on the dropdown anchor web element");
		dropDownAnchorWebElement.click();
		waitPageLoad();

		Boolean hasDropDownList=templateLOVType.getPopupDialog().getHasDropDownList();
		worker.println("hasDropDownList:"+hasDropDownList);
		if (hasDropDownList.booleanValue()) {
			// the click may not result in the popup showing (from what PD says) - looks like some Oracle bug
			// so PD needs to click a second time.
			int sleepTimeInSecs=1;
			int retryCount=0;
			int retryMaxCountBeforeClickingMainAnchor=5;
			WebElement searchAnchorWebElement=null;
			int retryErrorCount=0;
			int retryMaxBeforeError=5;
			while (true) {
				try{
					if (retryErrorCount>=retryMaxBeforeError) {
						throw new Exception("Unable to find the DropDown LOV list");
					}
					if (retryCount>=retryMaxCountBeforeClickingMainAnchor) {
						retryErrorCount++;
						worker.println("Searching retry # "+retryErrorCount+" for dropdown anchor web element");
						retryCount=0;
						dropDownAnchorWebElement=findAnchorDropdownWebElement(templateLOVType);
						worker.println("Clicking on it...");
						dropDownAnchorWebElement.click();
						waitPageLoad();
					}
					worker.println("Searching for anchor By.linkText: 'Search...'");
					searchAnchorWebElement=webDriver.findElement(By.linkText("Search..."));
					worker.println("Found!");
					SeleniumUtils.elementHighlight(webDriver,searchAnchorWebElement);

					break;
				}
				catch(org.openqa.selenium.NoSuchElementException exception) {
					retryCount++;
					worker.println("NoSuchElementException... retrying in "+sleepTimeInSecs+" secs");
					Thread.sleep(sleepTimeInSecs*1000);
				}
			}

			// find the value in the drop down window before clicking on search
			// Gain in injection time.
			String popupDivDataAfrPopupidAttributeValue=baseDropDownAnchorIDAttributeValue+"::dropdownPopup";
			String cssSelector="div[data-afr-popupid='"+popupDivDataAfrPopupidAttributeValue+"']";
			worker.println("Searching for popup div By.cssSelector: "+cssSelector);
			WebElement divPopupWebElement=webDriver.findElement(By.cssSelector(cssSelector));
			SeleniumUtils.elementHighlight(webDriver,divPopupWebElement);
			
			worker.println("Found! Now looking for one row matching the searchLabels before clicking on 'Search...'");
			String xpathData=getSearchLabelsXPATH();
			worker.println("xpath: "+xpathData);
			try{
				WebElement trWebElement=divPopupWebElement.findElement(By.xpath(xpathData));
				SeleniumUtils.elementHighlight(webDriver,trWebElement);
				worker.println("Found! clicking on it...");
				trWebElement.click();
				waitPageLoad();
				return;
			}
			catch(org.openqa.selenium.NoSuchElementException exception) {
				worker.println("NoSuchElementException... going the long way to search in the popup dialog...");
			}
			worker.println("Clicking on the Search anchor...");
			searchAnchorWebElement.click();
			waitPageLoad();
		}

		/*
		 * Fill in the search fields on the popup dialog
		 */
		String popupIdAttributeValue=baseDropDownAnchorIDAttributeValue+"::lovDialogId";
		worker.println("Searching for popup div By.id: '"+popupIdAttributeValue+"'");
		WebElement popupDialogWebElement=webDriver.findElement(By.id(popupIdAttributeValue));
		SeleniumUtils.elementHighlight(webDriver,popupDialogWebElement);
		worker.println("Found!");
		fillInSearchFieldsOnPopupDialog(popupDialogWebElement);
	}
	
	private WebElement findAnchorDropdownWebElement(TemplateLOVType templateLOVType) throws Exception {
		WebElement dropDownAnchorWebElement=null;
		WebDriver webDriver=((SeleniumWorker)worker).getWebDriver();

		Boolean byLabelTagLookup=templateLOVType.getByLabelTagLookup();
		worker.println("byLabelTagLookup: "+byLabelTagLookup);
		String dropDownAnchorXpath=null;
		if (byLabelTagLookup!=null && byLabelTagLookup) {
			dropDownAnchorXpath="//label[contains(text(), '"+templateLOVType.getLabel()+"')]/../..//a";
		}
		else {
			//String xpath="//label[contains(text(), '"+templateLOVType.getLabel()+"') and @class ]";
			dropDownAnchorXpath="//a[contains(@title,'Search:') and contains(@title,'"+templateLOVType.getLabel()+"')]";
		}
		worker.println("Searching for dropdown anchor web element with xpath: '"+dropDownAnchorXpath+"'");
		if (((SeleniumWorker)worker).isRelativeWebElementReferenceInUse()) {
			dropDownAnchorWebElement=((SeleniumWorker)worker).getRelativeWebElement().findElement(By.xpath("."+dropDownAnchorXpath));
		}
		else {
			dropDownAnchorWebElement=webDriver.findElement(By.xpath(dropDownAnchorXpath));
		}
		worker.println("Found!");
		SeleniumUtils.elementHighlight(webDriver,dropDownAnchorWebElement);
		
		return dropDownAnchorWebElement;
	}
	
	private void fillInSearchFieldsOnPopupDialog(WebElement popupDialogWebElement) throws Exception {
		WebDriver webDriver=((SeleniumWorker)worker).getWebDriver();
		Iterator<Integer> iterator=labelPositionToSearchLabelInformationMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer position=iterator.next();
			SearchLabelInformation searchLabelInformation=labelPositionToSearchLabelInformationMap.get(position);
			
			// find the input based on the label using a relative path
			String xpath=".//label[contains(text(), '"+searchLabelInformation.label+"') and @class ]";
			worker.println("Searching for label with xpath: '"+xpath+"'");
			WebElement labelWebElement=popupDialogWebElement.findElement(By.xpath(xpath));
			worker.println("Found!");
			SeleniumUtils.elementHighlight(webDriver,labelWebElement);
			
			String labelForAttributeValue=labelWebElement.getAttribute("for");
			worker.println("Searching for input with id: '"+labelForAttributeValue+"'");
			WebElement inputWebElement=popupDialogWebElement.findElement(By.id(labelForAttributeValue));
			worker.println("Found!");
			SeleniumUtils.elementHighlight(webDriver,inputWebElement);
			
			inputWebElement.sendKeys(searchLabelInformation.value);
			worker.println("sendkeys: '"+searchLabelInformation.value+"'");
		}
		// search button
		String xpath=".//button[contains(text(), 'Search')]";
		worker.println("Searching for button 'Search' with xpath: '"+xpath+"'");
		WebElement buttonWebElement=popupDialogWebElement.findElement(By.xpath(xpath));
		worker.println("Found!");
		SeleniumUtils.elementHighlight(webDriver,buttonWebElement);
		
		buttonWebElement.click();
		waitPageLoad();
		
		// look for the TR with data
		String xpathData=getSearchLabelsXPATH();
		xpathData="."+xpathData;
		worker.println("Searching for TR with xpath: '"+xpathData+"'");
		WebElement trWebElement=popupDialogWebElement.findElement(By.xpath(xpathData));
		worker.println("Found! clicking on it...");
		SeleniumUtils.elementHighlight(webDriver,trWebElement);
		
		trWebElement.click();
		
		xpath=".//button[contains(text(), 'OK')]";
		worker.println("Searching for button 'OK' with xpath: '"+xpath+"'");
		buttonWebElement=popupDialogWebElement.findElement(By.xpath(xpath));
		worker.println("Found!");
		SeleniumUtils.elementHighlight(webDriver,buttonWebElement);
		
		buttonWebElement.click();	
		waitPageLoad();
	}
	
	private void waitPageLoad() {
		WebDriverWait webDriverWait=((SeleniumWorker)worker).getWebDriverWait();
		((SeleniumWorker)worker).println("Waiting for page to finish rendering...");
		SeleniumUtils.waitForPageToFinishRendering(((SeleniumWorker)worker),webDriverWait);
	}
	
	private String getSearchLabelsXPATH() throws Exception {
		StringBuffer xpath=new StringBuffer("");
		xpath.append(".//tr[");
		Iterator<Integer> iterator=labelPositionToSearchLabelInformationMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer position=iterator.next();
			SearchLabelInformation searchLabelInformation=labelPositionToSearchLabelInformationMap.get(position);
			String value=searchLabelInformation.value;
			xpath.append("td[text()='").append(value).append("']");
			if (iterator.hasNext()) {
				xpath.append(" and ");
			}
		}
		xpath.append("]");
		
		return xpath.toString();
	}
	
	private void initLabelPositionToSearchLabelInformationMap(TemplateLOVType templateLOVType) throws Exception {
		labelPositionToSearchLabelInformationMap=new TreeMap<Integer,SearchLabelInformation>();
		SearchLabelType[] searchLabelArray=templateLOVType.getPopupDialog().getSearchLabelArray();
		boolean hasMainLabel=false;
		for (SearchLabelType searchLabel:searchLabelArray) {
			int position=searchLabel.getPosition().intValue();
			String label=searchLabel.getLabel();
			Boolean isMainLabel=searchLabel.getIsMainLabel();
			
			//worker.println(searchLabel:"+searchLabel);
			String value=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(((SeleniumWorker)worker).getBatchInjectionTracker(),
					searchLabel,null,true);
			// The above function will not trim the value so let's do it otherwise
			// it will break the popup search
			value=value.trim();
			//worker.println("label: '"+label+"' value: '"+value+"'");
			SearchLabelInformation searchLabelInformation=new SearchLabelInformation();
			searchLabelInformation.position=position;
			searchLabelInformation.value=value;
			searchLabelInformation.label=label;
			
			if (isMainLabel!=null && isMainLabel.booleanValue()) {
				mainSearchLabelInformation=searchLabelInformation;
				 hasMainLabel=true;
			}			
			labelPositionToSearchLabelInformationMap.put(position, searchLabelInformation);
		}
		if (!hasMainLabel) {
			throw new Exception("Invalid navigation, the boolean attribute 'isMainLabel' must be defined for one 'searchLabel' tag!");
		}
	}
	
	/*
	private WebElement findInputLabelWebElement(WebDriver webDriver,String labelTagToFindWithTextNodeValue, List<WebElement> labelWebElementList) {
		for (WebElement labelWebElement:labelWebElementList) {
			String innerHtml = (String)((JavascriptExecutor)webDriver).executeScript("return arguments[0].innerHTML;", labelWebElement);
			if (innerHtml.trim().equalsIgnoreCase(labelTagToFindWithTextNodeValue)) {
				// we need only the LABEL element for which the FOR attribute is linked to the ID of an actual INPUT element.
				
				String forAttributeValue=labelWebElement.getAttribute("for");
				if (forAttributeValue==null) {
					continue;
				}
				try{
					return webDriver.findElement(By.id(forAttributeValue));
				}
				catch(org.openqa.selenium.NoSuchElementException exception) {
					continue;
				}
			}
		}
		return null;
	}
	*/
	
	private class SearchLabelInformation {

		public String label;
		public String value;
		@SuppressWarnings("unused")
		public int position;
		
	}
		
}