package com.rapidesuite.inject.selenium.commands;

import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.erapidsuite.configurator.navigation0005.BlockToExecuteIfFoundType;
import com.erapidsuite.configurator.navigation0005.BlockToExecuteIfNotFoundType;
import com.erapidsuite.configurator.navigation0005.GridFindByColumnType;
import com.erapidsuite.configurator.navigation0005.GridFindByRowType;
import com.erapidsuite.configurator.navigation0005.ScrollType;
import com.erapidsuite.configurator.navigation0005.TemplateGridFindRowType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;

public class TemplateGridFindRowTypeCommand extends TemplateMain{

	private WebDriver webDriver;
	private WebElement gridSectionWebElement;
	private TemplateGridFindRowType templateGridFindRowType;
	
	public TemplateGridFindRowTypeCommand(SeleniumWorker seleniumWorker) {
		super(seleniumWorker);
		webDriver=((SeleniumWorker)worker).getWebDriver();
	}

	public void process(TemplateGridFindRowType templateGridFindRowType) throws Exception {
		this.templateGridFindRowType=templateGridFindRowType;
		String gridTitle=templateGridFindRowType.getGridTitle();
		gridSectionWebElement=TemplateUtils.getGridSectionWebElement(webDriver,worker,gridTitle);
		/*if (sectionTitle==null || sectionTitle.isEmpty()) {
			TemplateGridSearchTypeCommand templateGridSearchTypeCommand=((SeleniumWorker)worker).getTemplateGridSearchTypeCommand();
			if (templateGridSearchTypeCommand==null) {
				throw new Exception("You cannot use the template 'TemplateGridEditNewRowType' without selecting a GRID first!");
			}
			gridSectionWebElement=templateGridSearchTypeCommand.getGridSectionWebElement();
			if (gridSectionWebElement==null) {
				throw new Exception("You cannot use the template 'TemplateGridEditNewRowType' without a GRID section!");
			}
		}
		else {
			gridSectionWebElement=TemplateGridSearchTypeCommand.getSectionWebElement(((SeleniumWorker)worker).getWebDriver(),worker,sectionTitle);
		}
				
		// the form will set focus on that row
		String xpath="//table[@summary='"+sectionTitle+"']//tr[contains(@class,'Selected')]";		
		((SeleniumWorker)worker).setRelativeWebElementReferenceInUse(true);
		((SeleniumWorker)worker).setRelativeWebElementXPATH(xpath);
				
		XmlObject[] xmlObjects=templateGridEditSelectedRowType.selectPath("*");
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);
		worker.processNavigationXMLObjects(xmlObjectsSubList);
		
		//REQUERY THE TR FOR EVERY OPERATIONS!!!! OTHERWISE STALE EXCEPTION
		//OR USE THE xpath ABOVE AS A PREFIX TO ALL OPERATIONS UNDERNEATH INSTEAD OF STORING THE TR
		((SeleniumWorker)worker).setRelativeWebElementReferenceInUse(false);
		((SeleniumWorker)worker).setRelativeWebElementXPATH(null);
		*/
		worker.println("Find and Click on TR tag or scroll down...");
		findAndClickTRTagOrScrollDown();
	}
	
	private void findAndClickTRTagOrScrollDown() throws Exception {
		boolean hasFoundTR=false;
		while (true) {
			try{
				WebElement trWebElement = findTRTag();
				trWebElement.click();
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
		String name=null;
		if (hasFoundTR) {
			BlockToExecuteIfFoundType blockToExecuteIfFoundType=templateGridFindRowType.getBlockToExecuteIfFound();
			name=blockToExecuteIfFoundType.getName();
		}
		else {
			BlockToExecuteIfNotFoundType blockToExecuteIfNotFound=templateGridFindRowType.getBlockToExecuteIfNotFound();
			name=blockToExecuteIfNotFound.getName();
		}
		worker.println("Executing block name: '"+name+"'");
		executeBlock(name);
	}
	
	private void executeBlock(String blockToExecute) throws Exception {
		List<XmlObject> xmlObjectsSubList=worker.getBlockNameToCommandsMap().get(blockToExecute);
		if (xmlObjectsSubList==null) {
			throw new Exception("Cannot find the block name to execute: '"+blockToExecute+"'");
		}
		worker.processNavigationXMLObjects(xmlObjectsSubList);
	}
	
	private WebElement findTRTag() throws Exception {
		GridFindByRowType gridFindByRowType=templateGridFindRowType.getGridFindByRow();
		if (gridFindByRowType!=null) {
			boolean isBlankRow=gridFindByRowType.getIsBlankRow();
			if (isBlankRow) {
				StringBuffer xpathBuffer=new StringBuffer("");
				xpathBuffer.append("(");
				if (gridSectionWebElement!=null) {
					xpathBuffer.append(".");
				}
				xpathBuffer.append("(//input[(not(@value) or @value='') and (not(@title) or @title='')])[1]");
			}
			throw new Exception("Unsupported command 'GridFindByRow'");
		}
		else {
			GridFindByColumnType[] gridFindByColumnTypeArray=templateGridFindRowType.getGridFindByColumnArray();
			//String xpath="(//tr[.//td//text()='FAI1'])[1]";
			StringBuffer xpathBuffer=new StringBuffer("");
			xpathBuffer.append("(");
			if (gridSectionWebElement!=null) {
				xpathBuffer.append(".");
			}
			xpathBuffer.append("//tr[");
			int index=0;
			for (GridFindByColumnType gridFindByColumnType:gridFindByColumnTypeArray) {
				
				//gridFindByColumnType
				
				//int position=gridColumnType.getPosition().intValue();
				String value=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(((SeleniumWorker)worker).
						getBatchInjectionTracker(),gridFindByColumnType,null,true);
				value=value.trim();
				xpathBuffer.append(".//td//text()='"+value+"' ");
				if ( (index+1)<gridFindByColumnTypeArray.length) {
					xpathBuffer.append(" and ");
				}
				index++;
			}
			xpathBuffer.append("])[1]");
			String xpath=xpathBuffer.toString();
			WebElement webElement=TemplateUtils.getWebElement(webDriver,worker,gridSectionWebElement,xpath,false,true,true);
			return webElement;
		}
	}
	
	private WebElement getScrollBarWebElement() {
		try{
			String xpath="//div[contains(@id,'scroller')]";
			WebElement webElement=TemplateUtils.getWebElement(webDriver,worker,gridSectionWebElement,xpath);
			return webElement;
		}
		catch(org.openqa.selenium.NoSuchElementException exception) {
			worker.println("NoSuchElementException...");
		}
		return null;
	}
	
}
