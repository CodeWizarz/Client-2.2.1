package com.rapidesuite.snapshot.controller.convert;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.dataConversionShared0000.ConstantType;
import com.rapidesuite.client.dataConversionShared0000.ConstantsType;
import com.rapidesuite.client.dataConversionShared0000.DataConversionShared;
import com.rapidesuite.client.dataConversionShared0000.DataConversionSharedDocument;
import com.rapidesuite.client.dataConversionShared0000.ValueMapType;
import com.rapidesuite.client.dataConversionShared0000.ValuesMapType;
import com.rapidesuite.snapshot.controller.GenericController;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.convert.ConvertWorker;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;
import com.rapidesuite.snapshot.view.convert.ConvertFrame;
import com.rapidesuite.snapshot.view.convert.ConvertSourceGridRecordInformation;
import com.rapidesuite.snapshot.view.convert.ConvertTargetGridRecordInformation;
import com.rapidesuite.snapshot.view.convert.ConvertTargetPanel;

public class ConvertController extends GenericController{

	private List<ConvertSourceGridRecordInformation> selectedConvertSourceGridRecordInformationList;
	private List<ConvertSourceGridRecordInformation> convertSourceGridRecordInformationToRefreshList;
	private ConvertFrame convertFrame;
	private File sourceXMLDownloadFolder;
	private File convertedFolder;
	private Map<String, File> sourceInventoryNameToDataFileMap;
	private Set<String> sourceInventoryNameSet;
	private final Object lock = new Object();
	private final Object lockSet = new Object();
	private Set<String> completedInventoryNameSet;
	private Map<String,ValueMapType[]> sharedMappingNameToValueMapArrayMap;
	private Map<String,String> constantNameToValueMap;
	public static int TARGET_FILES_DOWNLOAD_SEQUENCE_NUMBER=1;
	private Map<String, Set<String>> targetInventoryNameToMandatoryColumnNamesToIgnoreMap;
	private Map<String,ConvertTargetGridRecordInformation> targetInventoryNameToConvertTargetGridRecordInformationMap;

	public ConvertController(ConvertFrame convertFrame,int workerCount) {
		super.setSnapshotEnvironmentProperties(convertFrame.getSnapshotEnvironmentProperties());
		super.setWorkersCount(workerCount);
		this.convertFrame=convertFrame;
		sourceInventoryNameToDataFileMap=new HashMap<String, File>();
		completedInventoryNameSet=new HashSet<String>();
		sharedMappingNameToValueMapArrayMap=new HashMap<String,ValueMapType[]>();
		constantNameToValueMap=new HashMap<String,String>();
		sourceInventoryNameSet=new HashSet<String>();
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
		Date now = new Date();
		String strDate = format.format(now);		
		sourceXMLDownloadFolder=new File(convertFrame.getRootDownloadFolder(),"migrate-source-"+strDate);
		sourceXMLDownloadFolder.mkdirs();
		targetInventoryNameToMandatoryColumnNamesToIgnoreMap=new HashMap<String,Set<String>>();
		targetInventoryNameToConvertTargetGridRecordInformationMap=new HashMap<String,ConvertTargetGridRecordInformation>();
	}
	
	public Map<String, Set<String>> getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap() {
		return targetInventoryNameToMandatoryColumnNamesToIgnoreMap;
	}

	@Override
	public GenericWorker getImplementedWorker() {
		ConvertWorker convertWorker=new ConvertWorker(this);
		return convertWorker;
	}	

	@Override
	public void preExecution() {
		try{
			String msg=Utils.getExecutionTime(super.getStartTime(),System.currentTimeMillis());
			convertFrame.getExecutionTimeLabel().setText("Execution time: "+msg);
			
			selectedConvertSourceGridRecordInformationList=convertFrame.getConvertMainPanel().getConvertSourcePanel().getSelectedConvertSourceGridRecordInformationList();
			super.setTasksList(selectedConvertSourceGridRecordInformationList);
			convertSourceGridRecordInformationToRefreshList = new ArrayList<ConvertSourceGridRecordInformation>(selectedConvertSourceGridRecordInformationList);
			for (ConvertSourceGridRecordInformation convertSourceGridRecordInformation:selectedConvertSourceGridRecordInformationList) {
				convertSourceGridRecordInformation.setStatus(UIConstants.UI_STATUS_PENDING);
				sourceInventoryNameSet.add(convertSourceGridRecordInformation.getInventoryName());
			}
			List<ConvertTargetGridRecordInformation> convertTargetGridRecordInformationList=convertFrame.getConvertMainPanel().getConvertTargetPanel().getConvertTargetGridRecordInformationList();
			for (ConvertTargetGridRecordInformation convertTargetGridRecordInformation:convertTargetGridRecordInformationList) {
				targetInventoryNameToConvertTargetGridRecordInformationMap.put(convertTargetGridRecordInformation.getInventoryName(),
						convertTargetGridRecordInformation);
			}			
			
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
			Date now = new Date();
			String strDate = format.format(now);
			convertedFolder=new File(convertFrame.getRootDownloadFolder(),"conversion-download-"+strDate);
			convertedFolder.mkdirs();
			
			convertFrame.setConvertedFolderLabel(convertedFolder);
			
			String sharedFileName="value_maps.xml";
			File mappingFolder=convertFrame.getMappingFolder();
			File sharedMappingsFolder=new File(mappingFolder,"SHARED");
			File sharedMappingsFile=new File(sharedMappingsFolder,sharedFileName);
			if (sharedMappingsFile.exists()) {
				DataConversionSharedDocument dataConversionSharedDocument=ModelUtils.getDataConversionSharedDocument(sharedMappingsFile);
				DataConversionShared dataConversionSharedType=dataConversionSharedDocument.getDataConversionShared();
				ValuesMapType[] valuesMapTypeArray=dataConversionSharedType.getValuesMapArray();
				if (valuesMapTypeArray!=null) {
					for (ValuesMapType valuesMapType:valuesMapTypeArray) {
						String name=valuesMapType.getName();
						if (sharedMappingNameToValueMapArrayMap.get(name)!=null) {
							throw new Exception("Invalid Shared mapping file '"+sharedFileName+"' - the value map name: '"+name+"' already exists!");
						}
						ValueMapType[] valueMapArray=valuesMapType.getValueMapArray();
						sharedMappingNameToValueMapArrayMap.put(name, valueMapArray);
					}
				}
			}
			
			String constantsFileName="constants.xml";
			File constantsFile=new File(sharedMappingsFolder,constantsFileName);
			if (constantsFile.exists()) {
				DataConversionSharedDocument dataConversionSharedDocument=ModelUtils.getDataConversionSharedDocument(constantsFile);
				DataConversionShared dataConversionSharedType=dataConversionSharedDocument.getDataConversionShared();
				ConstantsType constantsType=dataConversionSharedType.getConstants();
				if (constantsType!=null) {
					ConstantType[] constantsTypeArray=constantsType.getConstantArray();
					for (ConstantType constantType:constantsTypeArray) {
						String name=constantType.getName();
						if (sharedMappingNameToValueMapArrayMap.get(name)!=null) {
							throw new Exception("Invalid Shared Constants file '"+constantsFileName+"' - the constant name: '"+name+"' already exists!");
						}
						String value=constantType.getValue();
						constantNameToValueMap.put(name, value);
					}
				}
			}			
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}
	}
	
	@Override
	public void updateWhileExecution() {
		try{
			refreshGrid();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void postExecution() {
		convertFrame.getCloseButton().setEnabled(true);
		convertFrame.getCancelButton().setEnabled(false);
		updateUI();
			
		File parentFolder=convertedFolder.getParentFile();
		File zippedFile=new File(parentFolder,convertedFolder.getName()+".zip");
		try {
			convertFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"Please wait compressing Download Folder...");
			FileUtils.zipFolder(convertedFolder, zippedFile);
			if (this.isExecutionStopped()) {
				convertFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"Cancelled");
			}
			else {
				convertFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"Completed!");
			}
				
			GUIUtils.popupInformationMessage("<html><body>Conversion completed!<br/><br/>"+
					"Please click on the 'Output Folder' link to browse the converted files.");
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			convertFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"Failed ("+e.getMessage()+")");
			GUIUtils.popupErrorMessage("Internal Error: "+e.getMessage());
		}		
	}
	
	public void updateUI() {
		String msg=Utils.getExecutionTime(super.getStartTime(),System.currentTimeMillis());
		convertFrame.getExecutionStatusLabel().setText(ConvertFrame.LABEL_EXECUTION_STATUS+"In Progress");
		
		convertFrame.getExecutionTimeLabel().setText("Execution time: "+msg);
		convertFrame.getExecutionTasksLabel().setText("Total Source Inventories: "+super.getTotalCompletedTasks()+" / "+super.getTotalTasks());
		
		int totalFailedTasks=super.getTotalFailedTasks();
		if (totalFailedTasks>0) {
			convertFrame.getFailedTasksLabel().setForeground(Color.red);
			convertFrame.getFailedTasksLabel().setBackground(Color.white);
			convertFrame.getFailedTasksLabel().setOpaque(true);
			convertFrame.getFailedTasksLabel().setFont(new Font ("Arial", Font.BOLD, 14));
		}
		convertFrame.getFailedTasksLabel().setText("Total Failed Source Inventories: "+super.getTotalFailedTasks());
		convertFrame.updateProgressBar(super.getTotalCompletedTasks());
		
		List<ConvertTargetGridRecordInformation> list=convertFrame.getConvertMainPanel().getConvertTargetPanel().getConvertTargetGridRecordInformationList();
		int totalFailedTargetInventories=0;
		for (ConvertTargetGridRecordInformation convertTargetGridRecordInformation:list) {
			String status=convertTargetGridRecordInformation.getStatus();
			if (status!=null && status.equals(ConvertTargetPanel.STATUS_INVALID) ) {
				totalFailedTargetInventories++;
			}
		}
		if (totalFailedTargetInventories>0) {
			convertFrame.getFailedTargetInventoriesLabel().setForeground(Color.red);
			convertFrame.getFailedTargetInventoriesLabel().setBackground(Color.white);
			convertFrame.getFailedTargetInventoriesLabel().setOpaque(true);
			convertFrame.getFailedTargetInventoriesLabel().setFont(new Font ("Arial", Font.BOLD, 14));
		}
		convertFrame.getFailedTargetInventoriesLabel().setText("Total Failed Target Inventories: "+totalFailedTargetInventories);
	}
	
	protected void refreshGrid() throws Exception {
		updateUI();
		
		List<ConvertSourceGridRecordInformation> revisedListToRefresh=new ArrayList<ConvertSourceGridRecordInformation>();
		List<ConvertSourceGridRecordInformation> itemsToUpdateInThisCycleList=new ArrayList<ConvertSourceGridRecordInformation>();
		for (ConvertSourceGridRecordInformation convertSourceGridRecordInformation:convertSourceGridRecordInformationToRefreshList) {
			String status=convertSourceGridRecordInformation.getStatus();
			if (status.equals(UIConstants.UI_STATUS_PENDING) ) {
				revisedListToRefresh.add(convertSourceGridRecordInformation);
				itemsToUpdateInThisCycleList.add(convertSourceGridRecordInformation);
			}
			else
			if ( status.equals(UIConstants.UI_STATUS_UNSELECTED)) {
				itemsToUpdateInThisCycleList.add(convertSourceGridRecordInformation);
			}
			else
			if ( status.equals(UIConstants.UI_STATUS_FAILED) || status.equals(UIConstants.UI_STATUS_COMPLETED) 
					|| status.equals(UIConstants.UI_STATUS_WARNING) || status.equals(UIConstants.UI_STATUS_CANCELLED)) {
				itemsToUpdateInThisCycleList.add(convertSourceGridRecordInformation);
			}
			else
			if ( status.equals(UIConstants.UI_STATUS_PROCESSING)) {
				revisedListToRefresh.add(convertSourceGridRecordInformation);
				itemsToUpdateInThisCycleList.add(convertSourceGridRecordInformation);
				UIUtils.setTime(convertSourceGridRecordInformation);
				convertSourceGridRecordInformation.setRawTimeInSecs(UIUtils.getRawTimeInSecs(convertSourceGridRecordInformation.getStartTime()));
			}
		}
		convertSourceGridRecordInformationToRefreshList=revisedListToRefresh;
	
		convertFrame.getConvertMainPanel().getConvertSourcePanel().refreshGrid(itemsToUpdateInThisCycleList);
		convertFrame.getConvertMainPanel().getConvertTargetPanel().refreshGrid();
	}

	public ConvertFrame getConvertFrame() {
		return convertFrame;
	}

	public File getConvertedFolder() {
		return convertedFolder;
	}

	public File getDataFile(String inventoryName) {
		synchronized(lock) {
			return sourceInventoryNameToDataFileMap.get(inventoryName);
		}
	}
	
	public void putDataFile(String inventoryName,File dataFile) {
		synchronized(lock) {
			sourceInventoryNameToDataFileMap.put(inventoryName,dataFile);
		}
	}

	public Map<String, File> getSourceInventoryNameToDataFileMap() {
		return sourceInventoryNameToDataFileMap;
	}
	
	public boolean isSourceInventoryCompleted(String inventoryName) {
		synchronized(lockSet) {
			return completedInventoryNameSet.contains(inventoryName);
		}
	}
	
	public void setSourceInventoryCompleted(String inventoryName) {
		synchronized(lockSet) {
			completedInventoryNameSet.add(inventoryName);
		}
	}

	public ValueMapType[] getSharedMappings(String sharedMapName) {
		return sharedMappingNameToValueMapArrayMap.get(sharedMapName);
	}

	public Map<String, String> getConstantNameToValueMap() {
		return constantNameToValueMap;
	}

	public File getSourceXMLDownloadFolder() {
		return sourceXMLDownloadFolder;
	}

	public boolean isSourceInventorySelected(String inventoryName) {
		return sourceInventoryNameSet.contains(inventoryName);
	}

	public Map<String, ConvertTargetGridRecordInformation> getTargetInventoryNameToConvertTargetGridRecordInformationMap() {
		return targetInventoryNameToConvertTargetGridRecordInformationMap;
	}

}