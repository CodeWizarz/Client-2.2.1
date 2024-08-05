package com.rapidesuite.inject.webservices;

import java.util.List;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.BlockType;
import com.erapidsuite.configurator.navigation0005.ExecuteBlockType;
import com.erapidsuite.configurator.navigation0005.FusionWebServiceNavigationType;
import com.erapidsuite.configurator.navigation0005.IfThenElseType;
import com.erapidsuite.configurator.navigation0005.InvokeType;
import com.erapidsuite.configurator.navigation0005.Navigation;
import com.erapidsuite.configurator.navigation0005.NavigationDocument;
import com.erapidsuite.configurator.navigation0005.RepeatType;
import com.rapidesuite.inject.BatchInjectionTracker;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.commands.BlockTypeCommand;
import com.rapidesuite.inject.commands.ExecuteBlockTypeCommand;
import com.rapidesuite.inject.commands.IfThenElseTypeCommand;
import com.rapidesuite.inject.commands.RepeatTypeCommand;
import com.rapidesuite.inject.gui.ScriptsGrid;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.webservices.commands.InvokeTypeCommand;

public class FusionWebServiceWorker  extends Worker {
	
	private FusionWebServiceNavigationType fusionWebServiceNavigation;
	
	public FusionWebServiceWorker(BatchInjectionTracker batchInjectionTracker,int workerId) {
		super(batchInjectionTracker,workerId);
	}

	@Override
	public void stopExecution(boolean isQuitBrowser)
			throws InterruptedException {
		println("##############################");
		println("WORKER stop execution called...");
		println("##############################");
		isStopped=true;
		isStopped=true;
		batchInjectionTracker.setCompleted(true);
		if (!batchInjectionTracker.getScriptGridTracker().getStatus().equals(ScriptsGrid.STATUS_FAILED)) {
			batchInjectionTracker.getScriptGridTracker().setRemarks("Manual Stop!");
			batchInjectionTracker.getScriptGridTracker().setStatus(ScriptsGrid.STATUS_FAILED);
		}
	}

	@Override
	public void startExecution() {
		try{
			batchInjectionTracker.setStarted(true);
			if (batchInjectionTracker.getScriptGridTracker().getStatus().equals(ScriptsGrid.STATUS_PENDING)) {
				batchInjectionTracker.getScriptGridTracker().setRemarks("");
				batchInjectionTracker.getScriptGridTracker().setStatus(ScriptsGrid.STATUS_PROCESSING);
			}
			
			NavigationDocument fusionCurrentScriptNavigationDocument=InjectUtils.getFusionNavigationDocument(
					batchInjectionTracker.getScriptGridTracker().getInjectMain(),
					batchInjectionTracker.getScriptGridTracker().getInjectMain().getApplicationInfoPanel().getInjectionPackage(),
					batchInjectionTracker.getScriptGridTracker().getScript());	
			Navigation navigation=fusionCurrentScriptNavigationDocument.getNavigation();	
			fusionWebServiceNavigation=navigation.getFusionWebServiceNavigation();

			RepeatType[] repeatTypeArray=fusionWebServiceNavigation.getRepeatArray();
			if (repeatTypeArray==null || repeatTypeArray.length==0) {
				throw new Exception("Invalid navigation, it must contain at least one repeating group.");
			}
			
			List<XmlObject> xmlObjectsList=InjectUtils.parseFusionWebServiceNavigationDocument(fusionCurrentScriptNavigationDocument);
			processNavigationXMLObjects(xmlObjectsList);

			if (!batchInjectionTracker.isError()) {
				println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				println("!!!!!! WORKER COMPLETED WITHOUT ERRORS !!!!!!");
				println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}
		catch(Throwable e){
			printStackTrace(e);
			batchInjectionTracker.setError(true);
			if (!batchInjectionTracker.getScriptGridTracker().getStatus().equals(ScriptsGrid.STATUS_FAILED)) {
				if (isStopped()) {
					batchInjectionTracker.getScriptGridTracker().setRemarks("Manual Stop!");
				}
				else {
					batchInjectionTracker.getScriptGridTracker().setRemarks("Click 'View' to see the errors");
				}
				batchInjectionTracker.getScriptGridTracker().setStatus(ScriptsGrid.STATUS_FAILED);
			}
			
			updateAllRemainingRecordsToFailed(e.getMessage());
		}
		finally {
			batchInjectionTracker.setCompleted(true);
			batchInjectionTracker.getScriptGridTracker().incrementCompletedBatchCount();
			batchInjectionTracker.getScriptGridTracker().getInjectMain().getScriptManager().unreserveWorker(batchInjectionTracker);
		}
	}

	public void processNavigationXMLObjects(List<XmlObject> xmlObjectsList) throws Exception {
		for (XmlObject xmlObject:xmlObjectsList) {
			if (isStopped) {
				break;
			}

			String text=xmlObject.toString();			
			String nodeValue=((SimpleValue)xmlObject).getStringValue();
			String tagName=xmlObject.getDomNode().getNodeName();
			String fullCommand=text.replaceAll("xml-fragment", tagName);
			fullCommand=SeleniumUtils.formatCommand(fullCommand);

			println("###################################################################");
			println("###################################################################");
			println("Executing TAG NAME: "+tagName);

			if ( !( (xmlObject instanceof BlockType) ||
					(xmlObject instanceof RepeatType) ||
					(xmlObject instanceof IfThenElseType)||
					(xmlObject instanceof InvokeType)
					)){
				String tmp=nodeValue.replaceAll("\n","");
				println("nodeValue: '"+tmp+"'");
			}
			else {
				println("not printing nodeValue for this tag name");
			}

			if (xmlObject instanceof IfThenElseType) {
				IfThenElseType ifThenElseType=(IfThenElseType)xmlObject;
				IfThenElseTypeCommand ifThenElseTypeCommand=new IfThenElseTypeCommand(this);
				ifThenElseTypeCommand.process(ifThenElseType);
			}
			else
				if (xmlObject instanceof BlockType) {
					BlockType blockType=(BlockType)xmlObject;
					BlockTypeCommand blockTypeCommand=new BlockTypeCommand(this);
					blockTypeCommand.process(blockType);
				}
				else
					if (xmlObject instanceof ExecuteBlockType) {
						println(fullCommand,true);
						ExecuteBlockType executeBlockType=(ExecuteBlockType)xmlObject;
						ExecuteBlockTypeCommand executeBlockTypeCommand=new ExecuteBlockTypeCommand(this);
						executeBlockTypeCommand.process(executeBlockType);
					}
					else
						if (xmlObject instanceof RepeatType) {
							RepeatType repeatType=(RepeatType)xmlObject;
							println(SeleniumUtils.getRepeatTypeFriendlyXML(repeatType),true);
							RepeatTypeCommand repeatTypeCommand=new RepeatTypeCommand(this,true,false);			
							repeatTypeCommand.process(repeatType);
						}
						else
							if (xmlObject instanceof InvokeType) {
								InvokeType invokeType=(InvokeType)xmlObject;
								InvokeTypeCommand invokeTypeCommand=new InvokeTypeCommand(this);			
								invokeTypeCommand.process(invokeType);
							}
						else
						{
							println(fullCommand,true);
							throw new Exception("UNKNOWN TAG: "+tagName);
						}
		}
	}

	public FusionWebServiceNavigationType getFusionWebServiceNavigation() {
		return fusionWebServiceNavigation;
	}

}
