package com.rapidesuite.inject.commands;

import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.BlockType;
import com.erapidsuite.configurator.navigation0005.ExecuteBlockType;
import com.erapidsuite.configurator.navigation0005.NavigationDocument;
import com.erapidsuite.configurator.navigation0005.ParameterType;
import com.erapidsuite.configurator.navigation0005.ParametersType;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;
import com.rapidesuite.inject.selenium.commands.ParameterTypeCommand;

public class ExecuteBlockTypeCommand extends Command {

	public ExecuteBlockTypeCommand(Worker worker) {
		super(worker);
	}

	public void process(ExecuteBlockType executeBlockType) throws Exception {
		String blockName=executeBlockType.getName();
		
		if (worker instanceof SeleniumWorker) {
			blockName=SeleniumUtils.applyParameters(worker,blockName,((SeleniumWorker)worker).getParameterNameToValueMap());
			worker.println("Applying parameters on the block name : '"+blockName+"' -> new block name: '"+blockName+"'");
		}		
		
		String navigationName=executeBlockType.getNavigationName();
		if (navigationName!=null && !navigationName.isEmpty()) {
			Set<String> parsedExtraNavigationsSet=worker.getParsedExtraNavigationsSet();
			// we don't want to parse it twice
			if (! parsedExtraNavigationsSet.contains(navigationName)) {
				parsedExtraNavigationsSet.add(navigationName);
				NavigationDocument navigationDocumentTemp=SeleniumUtils.getNavigationDocument(worker.getBatchInjectionTracker(),navigationName);
				List<XmlObject> xmlObjectsListTemp=InjectUtils.parseFusionNavigationDocument(navigationDocumentTemp.getNavigation());

				boolean isFound=false;
				worker.println("Searching for block name: '"+blockName+"' in navigation: '"+navigationName+"'");
				for (XmlObject xmlObject:xmlObjectsListTemp) {
					if (xmlObject instanceof BlockType) {
						BlockType blockType=(BlockType)xmlObject;
						BlockTypeCommand blockTypeCommand=new BlockTypeCommand(worker);
						blockTypeCommand.process(blockType);

						String name=blockType.getName();
						worker.println("- block name: '"+name+"' ...");
						if (name.equalsIgnoreCase(blockName)) {
							isFound=true;
							worker.println(" FOUND! ");
						}
					}
				}
				if (!isFound) {
					throw new Exception("EXECUTEBLOCK cannot find the block defined as '"+blockName+"' in the sub navigation: '"+navigationName+"'");
				}
			}
		}
		
		ParametersType parametersType=executeBlockType.getParameters();
		if (parametersType!=null) {
			ParameterType[] parametersArray=parametersType.getParameterArray();
			ParameterTypeCommand parameterTypeCommand=new ParameterTypeCommand(((SeleniumWorker)worker));
			parameterTypeCommand.process(parametersArray,true);
		}
		
		List<XmlObject> xmlObjectsSubList=worker.getBlockNameToCommandsMap().get(blockName);
		if (xmlObjectsSubList==null) {
			throw new Exception("EXECUTEBLOCK cannot find the block defined as '"+blockName+"'");
		}
		
		if (worker instanceof SeleniumWorker) {
			boolean isRepeatUntilBreak=executeBlockType.getIsRepeatUntilBreak();
			if ( ! isRepeatUntilBreak) {
				worker.processNavigationXMLObjects(xmlObjectsSubList);
			}
			else {
				// the developer may forgot to add the attribute in the executeBlock tag
				// resulting in the <break> tag being set and breaking any other executeblock
				// so forcing it before starting any loop.
				((SeleniumWorker)worker).resetBreakDetected();
				
				int counter=0;
				while ( ! ((SeleniumWorker)worker).getIsBreakDetected() ) {
					counter++;
					if (counter==SeleniumWorker.BLOCK_REPEATER_TIMEOUT_RETRIES){
						throw new Exception("Timeout waiting for Block repeater to complete.");
					}
					try{
						worker.processNavigationXMLObjects(xmlObjectsSubList);
					}
					catch(org.openqa.selenium.StaleElementReferenceException exception) {
						worker.printStackTrace(exception);
						// IGNORES IT AS SOMETIMES A POPUP WITH PROGRESS BAR IS GETTING RELOADED EVERY
						// 5 SECONDS BY FUSION BUT BECAUSE OF SYNCHRONIZATION ISSUE WE GET THIS ERROR:
						// "Element is no longer attached to the DOM"
					}
				}
				((SeleniumWorker)worker).resetBreakDetected();
			}
			((SeleniumWorker)worker).setCurrentWebElement(null);
		}
		else {
			worker.processNavigationXMLObjects(xmlObjectsSubList);
		}
	}
	
}
