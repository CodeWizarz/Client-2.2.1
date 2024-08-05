package com.rapidesuite.inject.selenium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;

public class NodeDialog extends SnapshotSwingWorker {

	private SeleniumHub seleniumHub;
	private int CURRENT_STEP_COUNTER;
	private boolean isStartNode;
	
	public NodeDialog(SeleniumHub seleniumHub,final boolean isStartNode) {
		super(true);
		this.seleniumHub=seleniumHub;
		this.isStartNode=isStartNode;
		CURRENT_STEP_COUNTER=0;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	
	private void processAction() {
		try{		
			super.updateExecutionLabels("Operation in progress, please wait...",CURRENT_STEP_COUNTER);
			
			Iterator<String> iterator=seleniumHub.getHostnameUserToSeleniumHubTableList().keySet().iterator();
			List<String> allNodesIdentifierList=new ArrayList<String>();
			Map<String, SeleniumNodeInformation> nodeIdentifierToSeleniumNodeInformationMap=new HashMap<String, SeleniumNodeInformation>();
			while (iterator.hasNext()) {
				String hostName=iterator.next();
				SeleniumHubTable seleniumHubTable=seleniumHub.getHostnameUserToSeleniumHubTableList().get(hostName);
				Map<String, SeleniumNodeInformation> displayNameToNodeInformationMap=seleniumHub.getDisplayNameToNodeInformationMap();
				nodeIdentifierToSeleniumNodeInformationMap.putAll(displayNameToNodeInformationMap);
				if (isStartNode) {
					List<String> chechedDisplayNames=seleniumHubTable.getChechedDisplayNames(SeleniumHubTable.STATUS_DOWN);
					allNodesIdentifierList.addAll(chechedDisplayNames);
				}
				else {
					List<String> chechedDisplayNames=seleniumHubTable.getChechedDisplayNames(SeleniumHubTable.STATUS_UP);
					allNodesIdentifierList.addAll(chechedDisplayNames);
				}
			}
			
			int index=0;
			super.setTotalSteps(allNodesIdentifierList.size());
			for (String nodeIdentifier:allNodesIdentifierList) {
				SeleniumNodeInformation seleniumNodeInformation=nodeIdentifierToSeleniumNodeInformationMap.get(nodeIdentifier);
				index++;
				if (isStartNode) {
					SeleniumHubTable.startNode(seleniumHub,seleniumNodeInformation);
					super.updateExecutionLabels("Starting "+index+" / "+allNodesIdentifierList.size()+" Nodes...",CURRENT_STEP_COUNTER++);
				}
				else {
					SeleniumHubTable.stopNode(seleniumHub,seleniumNodeInformation);
					super.updateExecutionLabels("Stopping "+index+" / "+allNodesIdentifierList.size()+" Nodes...",CURRENT_STEP_COUNTER++);
				}
			}
			
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}
		
}