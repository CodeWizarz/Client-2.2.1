package com.rapidesuite.snapshot.view.upgrade;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.SevenZipUtils;
import com.rapidesuite.client.dataConversion0000.DataConversionDocument;
import com.rapidesuite.client.dataConversion0000.DataConversionType;
import com.rapidesuite.client.dataConversion0000.IfThenElseLoopType;
import com.rapidesuite.client.dataConversion0000.IfThenElseType;
import com.rapidesuite.client.dataConversion0000.LoopRecordsType;
import com.rapidesuite.client.dataConversion0000.SourceType;
import com.rapidesuite.client.dataConversion0000.TargetType;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.NavigatorNodePath;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;
import com.rapidesuite.snapshot.view.SnapshotPackageSelectionPanel;
import com.rapidesuite.snapshot.view.SnapshotsActionsPanel;

public class FusionScenarioPackageWorker extends SnapshotSwingWorker {

	private UpgradeFrame upgradeFrame;
	private File tempFolder;
	private int step;
	private Map<String, Set<File>> ebsInventoryNameToFusionMappingFileMap;
	private Map<String, Set<String>> targetNamePostImplToFusionInventoryNamesMap;
	private Map<String, String> fusionInventoryNameToNavigationNameMap;
	
	public FusionScenarioPackageWorker(UpgradeFrame upgradeFrame) {
		super(true);
		this.upgradeFrame=upgradeFrame;
		targetNamePostImplToFusionInventoryNamesMap=new HashMap<String, Set<String>>();
		fusionInventoryNameToNavigationNameMap=new HashMap<String, String>();
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();		
		return null;
	}
	
	public void processAction() {
		try{			
			setTotalSteps(5000);
			step=50;
			if (upgradeFrame.getTabUpgradeMainPanel().getUpgradeScenarioExplodedFolderPath()==null) {
				updateExecutionLabels("Please wait, unpacking...",step);
				File userhomeFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
				tempFolder=new File(userhomeFolder,"fusioninvzip");
				if (tempFolder.exists()) {
					FileUtils.deleteDirectory(tempFolder);
				}
				tempFolder.mkdirs();

				File fusionScenarioFile=upgradeFrame.getTabUpgradeMainPanel().getFusionScenarioFileFileChooser().getSelectedFile();
				SevenZipUtils.decompressFile(fusionScenarioFile,tempFolder);
			}
			else {
				tempFolder=new File(upgradeFrame.getTabUpgradeMainPanel().getUpgradeScenarioExplodedFolderPath());
			}
			
			// Parsing the navigator.xml if any - this is needed for some reports (Default values)
			File navigatorFile=new File(tempFolder,"navigator.xml");
			if (navigatorFile.exists()) {
				FileUtils.println("Parsing navigator file: "+navigatorFile.getAbsolutePath());
				Map<String,List<NavigatorNodePath>> functionIdToFunctionIdNavigatorNodesMap=
						SnapshotPackageSelectionPanel.getFunctionIdToFunctionIdNavigatorNodesMap(navigatorFile); 
				upgradeFrame.getTabUpgradeMainPanel().setFunctionIdToFunctionIdNavigatorNodesMap(functionIdToFunctionIdNavigatorNodesMap);
			}
			
			step+=50;
			updateExecutionLabels("Please wait, analyzing files...",step);
			File scenarioFolder=getScenarioFolder();
			FileUtils.println("Scenario Folder: "+scenarioFolder.getAbsolutePath());
			int totalFilesCount=countFiles(scenarioFolder);
			updateExecutionLabels("Please wait, analyzing files... [ 0 / "+totalFilesCount+" ]",step);
			List<FusionInventoryRow> fusionInventoryRowList=buildFusionInventoryRowList(scenarioFolder,totalFilesCount);
			init(fusionInventoryRowList);
			
			/*
			 * Loading extra RSC tables
			 */
			FileUtils.println("Loading extra RSC tables...");
			loadExtraRSCInventories(fusionInventoryRowList);
						
			upgradeFrame.getTabUpgradeMainPanel().getFusionInventoryGridPanel().displayFusionInventories(fusionInventoryRowList);
			upgradeFrame.getTabUpgradeMainPanel().displayEBSInventories();
			upgradeFrame.getTabUpgradeMainPanel().selectEBSInventoriesRelatedToSelectedFusionInventories(true);
		}
		catch(Throwable e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}
	
	private void loadExtraRSCInventories(List<FusionInventoryRow> fusionInventoryRowList) throws Exception {
		File extraRSCTablesFolder=SnapshotsActionsPanel.getExtraRSCTablesFolder();
		int gridIndex=fusionInventoryRowList.size();
		for ( File file : extraRSCTablesFolder.listFiles())
		{
			if(!file.isDirectory()){
	        	String inventoryName=file.getName().replace(".xml","");
	        	
	        	FusionInventoryRow fusionInventoryRow=new FusionInventoryRow(inventoryName);
	        	FusionInventoryInformation fusionInventoryInformation=new FusionInventoryInformation();
	        	fusionInventoryRow.setFusionInventoryInformation(fusionInventoryInformation);
	        	fusionInventoryInformation.setTempInventoryFile(file);
	        	Inventory inventory=FileUtils.getInventory(file,fusionInventoryRow.getInventoryName());
	        	fusionInventoryRow.setInventory(inventory);
	        	fusionInventoryRow.setGridIndex(gridIndex++);
	        	
	        	Set<String> fusionInventoryNames=targetNamePostImplToFusionInventoryNamesMap.get(inventoryName);
				if (fusionInventoryNames!=null) {
					Iterator<String> iterator=fusionInventoryNames.iterator();
					while (iterator.hasNext()) {
						String fusionInventoryName=iterator.next();
			        	String navigationName=fusionInventoryNameToNavigationNameMap.get(fusionInventoryName);
			        	fusionInventoryInformation.addNavigationName(navigationName);
					}
				}
	        	fusionInventoryRowList.add(fusionInventoryRow);
	        }
		}
	}

	private File getScenarioFolder() throws Exception {
		if (!tempFolder.exists()) {
			throw new Exception("File or folder does not exist: '"+tempFolder.getAbsolutePath()+"'");
		}
		boolean hasControlFolder=false;
		for ( File file : tempFolder.listFiles())
		{
			if(file.isDirectory() && file.getName().equalsIgnoreCase("control")) {
				hasControlFolder = true;
			}
		}
		if (!hasControlFolder) {
			throw new Exception("Invalid Fusion Scenario package, no control folder detected!");
		}
		
		for ( File file : tempFolder.listFiles())
		{
			if(file.isDirectory() && !file.getName().equalsIgnoreCase("control")) {
				return file;
			}
		}
		throw new Exception("Invalid Fusion Scenario package, cannot find inventory folder!");
	}

	public UpgradeFrame getUpgradeFrame() {
		return upgradeFrame;
	}
	
	public static int countFiles(File directory) {
	    int count = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isDirectory()) {
	            count += countFiles(file); 
	        }
	        count++;
	    }
	    return count;
	}
	
	int currentFileIndex=0;
	private List<FusionInventoryRow> buildFusionInventoryRowList(File folder, int totalFilesCount) throws Exception
	{
		List<FusionInventoryRow> toReturn=new ArrayList<FusionInventoryRow>();
		File[] files=folder.listFiles();
		if (files==null) {
			return toReturn;
		}
		Map<String,FusionInventoryRow> inventoryNameToFusionInventoryRowMap=new HashMap<String,FusionInventoryRow>();
		for ( File file : folder.listFiles())
		{
			if(file.isDirectory())
	        {
				List<FusionInventoryRow> list=buildFusionInventoryRowList(file,totalFilesCount);
				toReturn.addAll(list);
	        }
	        else
	        {
	        	currentFileIndex++;
	        	if (currentFileIndex % 50 ==0) {
	        		updateExecutionLabels("Please wait, parsing Inventories... [ "+currentFileIndex+" / "+totalFilesCount+" ]",step);
	        	}
	        	String inventoryName=file.getName().replace(".xml","");
	        	if (inventoryName.toLowerCase().endsWith(".sql")) {
	        		throw new Exception("Invalid inventory detected in the Fusion scenario package. File: '"+file.getAbsolutePath()+"'");
	        	}
	        	FusionInventoryRow fusionInventoryRow=new FusionInventoryRow(inventoryName);
	        	FusionInventoryInformation fusionInventoryInformation=new FusionInventoryInformation();
	        	fusionInventoryRow.setFusionInventoryInformation(fusionInventoryInformation);
	        	String encodedNavigationName=file.getParentFile().getName();
	        	int indexOf=encodedNavigationName.indexOf("-");
	        	String navigationName=encodedNavigationName;
				if (indexOf!=-1) {
					navigationName=encodedNavigationName.substring(indexOf+1);
				}
	        	fusionInventoryInformation.addNavigationName(navigationName);
	        	fusionInventoryNameToNavigationNameMap.put(inventoryName, navigationName);
	        	fusionInventoryInformation.setTempInventoryFile(file);
	        	
	        	Inventory inventory=FileUtils.getInventory(file,fusionInventoryRow.getInventoryName());
	        	inventoryNameToFusionInventoryRowMap.put(inventoryName, fusionInventoryRow);
	        	fusionInventoryRow.setInventory(inventory);
	        	//System.out.println(currentFileIndex+" inventoryName:"+inventoryName+" sequence:"+inventory.getSequence());
	        }
		}
		if (!inventoryNameToFusionInventoryRowMap.isEmpty()) {
			//System.out.println("###");
			Iterator<String> iterator=inventoryNameToFusionInventoryRowMap.keySet().iterator();
			List<FusionInventoryRow> rootList=new ArrayList<FusionInventoryRow>();
			while (iterator.hasNext()) {
				String inventoryName=iterator.next();
				FusionInventoryRow fusionInventoryRow=inventoryNameToFusionInventoryRowMap.get(inventoryName);
				
				Inventory inventory=fusionInventoryRow.getInventory();
				String parentName=inventory.getParentName();
				//System.out.println("inventoryName:"+inventoryName+" sequence:"+inventory.getSequence()+" parentName:"+parentName);
				if (parentName!=null && !parentName.isEmpty()) {
					FusionInventoryRow fusionInventoryRowParent=inventoryNameToFusionInventoryRowMap.get(parentName);
					int sequence=inventory.getSequence();
					fusionInventoryRowParent.addChild(sequence, fusionInventoryRow);
				}
				else {
					rootList.add(fusionInventoryRow);
					
				}
			}
			for (FusionInventoryRow fusionInventoryRow:rootList) {
				toReturn.add(fusionInventoryRow);
				toReturn.addAll(fusionInventoryRow.getSortedChildrenList());
			}			
		}
		
		return toReturn;
	}
		
	private void init(List<FusionInventoryRow> fusionInventoryRowList) throws Exception
	{
		File mappingFolder=upgradeFrame.getTabUpgradeMainPanel().getFusionMappingFolderFileChooser().getSelectedFile();
		Map<String, File> fusionInventoryNameToMappingFileMap=ModelUtils.getFileNameToFileMap(mappingFolder,true);
		upgradeFrame.getTabUpgradeMainPanel().setFusionInventoryNameToMappingFileMap(fusionInventoryNameToMappingFileMap);
		int index=0;
		setTotalSteps(fusionInventoryRowList.size());
		
		updateExecutionLabels("Please wait, analyzing files ( "+index+" / "+fusionInventoryRowList.size()+" )...",step);
		ebsInventoryNameToFusionMappingFileMap=new HashMap<String,Set<File>>();
		for (FusionInventoryRow fusionInventoryRow:fusionInventoryRowList)
		{
			FusionInventoryInformation fusionInventoryInformation=fusionInventoryRow.getFusionInventoryInformation();
			fusionInventoryRow.setGridIndex(index);

			index++;
			if (index % 100 == 0) {
				updateExecutionLabels("Please wait, analyzing files ( "+index+" / "+fusionInventoryRowList.size()+" )...",step+index);
			}
			
			File mappingFile=fusionInventoryNameToMappingFileMap.get(fusionInventoryRow.getInventoryName());
			if (mappingFile!=null) {
				fusionInventoryInformation.setMappingFile(mappingFile);

				DataConversionDocument dataConversionDocument=ModelUtils.getDataConversionDocument(mappingFile);
				DataConversionType dataConversionType=dataConversionDocument.getDataConversion();

				SourceType[] sourceTypeArray=dataConversionType.getSourceArray();
				Set<String> ebsInventoryNameSet=new HashSet<String>();
				fusionInventoryInformation.setEBSInventorySet(ebsInventoryNameSet);
				for (SourceType sourceType:sourceTypeArray) {
					com.rapidesuite.client.dataConversion0000.SourceType.Type.Enum sourceTypeAttribute=sourceType.getType();
					if (sourceTypeAttribute.equals(SourceType.Type.INVENTORY)) {
						String sourceName=sourceType.getName();
						ebsInventoryNameSet.add(sourceName);
						
						Set<File> files=ebsInventoryNameToFusionMappingFileMap.get(sourceName);
						if (files==null) {
							files=new HashSet<File>();
							ebsInventoryNameToFusionMappingFileMap.put(sourceName, files);
						}
						files.add(mappingFile);
					}
				}
				
				LoopRecordsType[] loopRecordsTypeArray=dataConversionType.getLoopRecordsArray();
				for (LoopRecordsType loopRecordsType:loopRecordsTypeArray) {
					analyzeLoopRecordsTypeMetadata(loopRecordsType,fusionInventoryRow);
				}
				IfThenElseLoopType[] ifThenElseLoopTypeArray=dataConversionType.getIfThenElseLoopArray();
				if (ifThenElseLoopTypeArray!=null) {
					for (IfThenElseLoopType ifThenElseLoopType:ifThenElseLoopTypeArray) {
						XmlObject[] xmlObjects=ifThenElseLoopType.selectPath("*");
						analyzeIfMetadata(xmlObjects,fusionInventoryRow);
					}
				}
				
			}
		}
	}

	public Map<String, Set<File>> getEbsInventoryNameToFusionMappingFileMap() {
		return ebsInventoryNameToFusionMappingFileMap;
	}
	
	private void analyzeLoopRecordsTypeMetadata(LoopRecordsType loopRecordsType,FusionInventoryRow fusionInventoryRow) throws Exception {
		IfThenElseType[] ifThenElseTypeArray=loopRecordsType.getIfThenElseArray();
		if (ifThenElseTypeArray!=null) {
			for (IfThenElseType ifThenElseType:ifThenElseTypeArray) {
				XmlObject[] xmlObjects=ifThenElseType.selectPath("*");
				analyzeIfMetadata(xmlObjects,fusionInventoryRow);
			}
		}
		
		TargetType[] targetTypeArray=loopRecordsType.getTargetArray();
		for (TargetType targetType:targetTypeArray) {
			analyzeTargetTypeMetadata(targetType,fusionInventoryRow);
		}
	}

	private void analyzeIfMetadata(XmlObject[] xmlObjects,FusionInventoryRow fusionInventoryRow) throws Exception {
		List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);

		XmlObject xmlObjectThen=xmlObjectsSubList.get(1);
		XmlObject xmlObjectElse=xmlObjectsSubList.get(2);

		analyzeIfThenElseMetadata(xmlObjectThen,fusionInventoryRow);
		analyzeIfThenElseMetadata(xmlObjectElse,fusionInventoryRow);
	}
	
	private void analyzeIfThenElseMetadata(XmlObject xmlObject,FusionInventoryRow fusionInventoryRow) throws Exception {
		XmlObject[] xmlInnerObjects=xmlObject.selectPath("*");
		List<XmlObject> xmlObjectsList=Arrays.asList(xmlInnerObjects);
		for (XmlObject xmlObjectTemp:xmlObjectsList) {
			if (xmlObjectTemp instanceof IfThenElseType) {
				XmlObject[] xmlInnerObjectsTemp=xmlObjectTemp.selectPath("*");
				analyzeIfMetadata(xmlInnerObjectsTemp,fusionInventoryRow);
			}
			else
				if (xmlObjectTemp instanceof TargetType) {
					analyzeTargetTypeMetadata( (TargetType)xmlObjectTemp,fusionInventoryRow);
				}
				else
					if (xmlObjectTemp instanceof LoopRecordsType) {
						analyzeLoopRecordsTypeMetadata((LoopRecordsType)xmlObjectTemp,fusionInventoryRow);
					}
		}
	}
	
	private void analyzeTargetTypeMetadata(TargetType targetType,FusionInventoryRow fusionInventoryRow) throws Exception {
		String targetName=targetType.getName();
		Boolean isPostImplementation=targetType.getIsPostImplementation();
		if (isPostImplementation!=null && isPostImplementation.booleanValue()) {
			Set<String> fusionInventoryNames=targetNamePostImplToFusionInventoryNamesMap.get(targetName);
			if (fusionInventoryNames==null) {
				fusionInventoryNames=new HashSet<String>();
				targetNamePostImplToFusionInventoryNamesMap.put(targetName,fusionInventoryNames);
			}
			fusionInventoryNames.add(fusionInventoryRow.getInventoryName());
		}
	}
	
}