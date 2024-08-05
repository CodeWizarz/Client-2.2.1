package com.rapidesuite.extract.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.view.ApplicationInfoPanel;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;

public class ExtractionPackageLoadDialog extends SnapshotSwingWorker {

	private ApplicationInfoPanel applicationInfoPanel;
	private File extractionPackage;
	private int CURRENT_STEP_COUNTER;
		
	public ExtractionPackageLoadDialog(ApplicationInfoPanel applicationInfoPanel, File extractionPackage) {
		super(true);
		this.applicationInfoPanel=applicationInfoPanel;
		this.extractionPackage=extractionPackage;
		CURRENT_STEP_COUNTER=1;
		super.setTotalSteps(3);
	}

	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	
	private void processAction() {
		try{		
			super.updateExecutionLabels("Unzipping in progress, please wait...",CURRENT_STEP_COUNTER++);
			ExtractUtils.unpackExtractionPackage(extractionPackage);
			parseStructures();
			applicationInfoPanel.loadExtractionPackageFile(extractionPackage);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}
	
	private void parseStructures() {
		try{		
			super.updateExecutionLabels("Loading Inventories, please wait...",CURRENT_STEP_COUNTER++);
			
			File tempFolder = new File(Config.getTempFolder(),ExtractConstants.EXTRACT_ZIP_TEMP_FOLDER);
			tempFolder = new File(tempFolder,extractionPackage.getName());
			
			File navigatorFile=new File(tempFolder,"navigator.xml");
			Map<String,NavigatorNode> functionIdToNavigatorNode=new HashMap<String,NavigatorNode>(); 
			if (navigatorFile.exists()) {
				functionIdToNavigatorNode=ExtractUtils.getFunctionIdToNavigatorNode(navigatorFile); 
			}
			Map<String,File> inventoryNameToInventoryFileMap=null;
			SortedMap<String, SortedSet<File>> inventoryNameToSQLFileMap=null;
			/*
			 * Configurator creates different folders depending of if the export was done from a scenario
			 * or Browse Repository:
			 * if the file "tree_structure.xml" is present then use all_inventories
			 * else parse the second folder except "control" as the name of the folder is the scenario name
			 */
			File treeStructureFile= new File(tempFolder,"tree_structure.xml");
			if (treeStructureFile.exists()) {
				File inventoryRootFolder=new File(tempFolder,"all_inventories");
				inventoryNameToInventoryFileMap=ExtractUtils.getFileNameToFileMap(inventoryRootFolder,true,".xml");
			}
			else {
				File[] files=tempFolder.listFiles();
				if (files==null || files.length==0) {
					throw new Exception("Invalid Extract Package selected. Error: Unrecognized structure");
				}
				for (File file:files) {
					if (file.isDirectory() && !file.getName().equalsIgnoreCase("control")) {
						// found the scenario name folder:
						inventoryNameToInventoryFileMap=ExtractUtils.getFileNameToFileMap(file,true,".xml");
						break;
					}
				}
			}
			File controlFolder=new File(tempFolder,"control");
			inventoryNameToSQLFileMap=ExtractUtils.getInventoryToSQLMap(controlFolder,true,".sql");
			
			applicationInfoPanel.setInventoryNameToInventoryFileMap(inventoryNameToInventoryFileMap);
			applicationInfoPanel.setInventoryNameToSQLFileMap(inventoryNameToSQLFileMap);
			
			List<ExtractInventoryRecord> extractInventoryRecordList=new ArrayList<ExtractInventoryRecord>();
			Iterator<String> iterator=inventoryNameToInventoryFileMap.keySet().iterator();
			int gridIndex=0;
			boolean isShowUnsupportedInventories=applicationInfoPanel.getExtractMain().getExtractMainPanel().getTabOptionsPanel().isShowUnsupportedInventories();
			Set<String> allFunctionNamesSet=new HashSet<String>();
			applicationInfoPanel.setAllFunctionNamesSet(allFunctionNamesSet);
			while (iterator.hasNext()) {
				String inventoryName=iterator.next();
				File inventoryFile=inventoryNameToInventoryFileMap.get(inventoryName);
				File sqlFile=null;
				try {
					// This is just to show the inventory in grid, and establish the level of the form so the first one should be good
					// no need to find the one that works.
					sqlFile=inventoryNameToSQLFileMap.get(inventoryName).first();
				} catch (Exception ex) {
					FileUtils.println("couldn't load SQL(s) for Inventory: '" + inventoryName + "'");
				}
								
				if (sqlFile==null && !isShowUnsupportedInventories) {
					continue;
				}
				Inventory inventory=FileUtils.getInventory(inventoryFile,inventoryName);
				if (!inventory.isReversible()) {
					continue;
				}
				
				ExtractInventoryRecord extractInventoryRecord=new ExtractInventoryRecord(inventoryName);
				
				if (sqlFile!=null) {
					ExtractUtils.computeInformationFromSQLFile(sqlFile,extractInventoryRecord);
					allFunctionNamesSet.addAll(extractInventoryRecord.getFunctionNamesSet());
				}
				
				extractInventoryRecord.setInventory(inventory);
				extractInventoryRecord.setSelectionGridIndex(gridIndex);
				extractInventoryRecordList.add(extractInventoryRecord);
				
				NavigatorNode navigatorNode=functionIdToNavigatorNode.get(inventory.getFunctionIds());
				if (navigatorNode!=null) {
					//FileUtils.println("inventory.getFunctionIds():"+inventory.getFunctionIds());
					extractInventoryRecord.setFormName(navigatorNode.getEbsResponsibilityRecord().getDisplayText());
					List<String> fullPathList=navigatorNode.getFullPathList();
					if (fullPathList.isEmpty()) {
						// This is an orphan in the navigator - a bug from PD they didn't put the correct ID!!!
					}
					else {
						//FileUtils.println("fullPathList:"+fullPathList);
						extractInventoryRecord.setApplicationName(fullPathList.get(0));
						StringBuffer formPath=new StringBuffer("");
						int cnt=0;
						for (String item:fullPathList) {
							formPath.append(item);
							if ( (cnt+1)<fullPathList.size()) {
								formPath.append(" ->\n");
							}
							cnt++;
						}
						extractInventoryRecord.setFormPath(formPath.toString());
					}
				}
				
				gridIndex++;
			}
			applicationInfoPanel.getExtractMain().getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel()
				.loadFilterPanel();
			applicationInfoPanel.getExtractMain().getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel()
				.displayInventories(extractInventoryRecordList);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}

}