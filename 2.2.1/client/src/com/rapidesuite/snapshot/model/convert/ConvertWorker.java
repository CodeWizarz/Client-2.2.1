package com.rapidesuite.snapshot.model.convert;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.controller.convert.ConvertController;
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;
import com.rapidesuite.snapshot.view.convert.ConvertSourceGridRecordInformation;
import com.rapidesuite.snapshot.view.convert.ConvertTargetGridRecordInformation;
import com.rapidesuite.snapshot.view.convert.ConvertTargetPanel;
import com.rapidesuite.snapshot.view.convert.MappingAnalyzerWorker;

public class ConvertWorker extends GenericWorker {
	
	public ConvertWorker(ConvertController convertController) {
		super(convertController,true);
	}

	@Override
	public void execute(Object task) {
		ConvertSourceGridRecordInformation convertSourceGridRecordInformation=(ConvertSourceGridRecordInformation)task;
		ConvertController convertController=(ConvertController)super.getGenericController();
		try {
			convertSourceGridRecordInformation.setStartTime(System.currentTimeMillis());
			if (convertController.isExecutionStopped()) {
				convertSourceGridRecordInformation.setStatus(UIConstants.UI_STATUS_CANCELLED);
				return;
			}
			convertSourceGridRecordInformation.setStatus(UIConstants.UI_STATUS_PROCESSING);
			
			performConversion(convertController,convertSourceGridRecordInformation);
			
			if (!convertSourceGridRecordInformation.getStatus().equals(UIConstants.UI_STATUS_WARNING)) {
				convertSourceGridRecordInformation.setStatus(UIConstants.UI_STATUS_COMPLETED);
			}
		}
		catch (Throwable e) {
			FileUtils.printStackTrace(e);
			super.setTotalFailedTasks(super.getTotalFailedTasks()+1);
			convertSourceGridRecordInformation.setRemarks(e.getMessage());
			convertSourceGridRecordInformation.setStatus(UIConstants.UI_STATUS_FAILED);
		}
		finally {
			UIUtils.setTime(convertSourceGridRecordInformation);
			convertController.setSourceInventoryCompleted(convertSourceGridRecordInformation.getInventoryName());
		}
	}
	
	private void performConversion(ConvertController convertController,ConvertSourceGridRecordInformation convertSourceGridRecordInformation) throws Exception {
		convertSourceGridRecordInformation.setRemarks("Downloading data...");
		
		File sourceInventoryFile=convertController.getConvertFrame().getSourceInventoryNameToInventoryFileMap().
				get(convertSourceGridRecordInformation.getInventoryName());
		if (sourceInventoryFile==null) {
			throw new Exception("Unable to find the inventory file: '"+convertSourceGridRecordInformation.getInventoryName()+"'");
		}
		Inventory sourceInventory=FileUtils.getInventory(sourceInventoryFile,convertSourceGridRecordInformation.getInventoryName());
		convertSourceGridRecordInformation.setInventory(sourceInventory);		
		File sourceDataXMLFile=convertController.getConvertFrame().downloadSourceDataToXMLFile(this,convertSourceGridRecordInformation);
		if (convertController.getConvertFrame().getSnapshotId()==-1 && convertSourceGridRecordInformation.getTotalRecordsToConvert()==0) {
			List<String[]> dataRows=InjectUtils.parseXMLDataFile(sourceDataXMLFile);
			convertSourceGridRecordInformation.setTotalRecordsToConvert(dataRows.size());
		}
		convertSourceGridRecordInformation.setDataFile(sourceDataXMLFile);
		convertController.putDataFile(convertSourceGridRecordInformation.getInventoryName(),sourceDataXMLFile);
		
		String inventoryName=convertSourceGridRecordInformation.getInventoryName();
		String inventoryNamePostConfiguration=inventoryName+" - POSTCONFIG";
		
		Map<String, File> sourceInventoryNameToMappingFileMap=convertController.getConvertFrame().getSourceInventoryNameToMappingFileMap();
		File configurationMappingXMLFile=sourceInventoryNameToMappingFileMap.get(inventoryName);
		File postConfigurationMappingXMLFile=sourceInventoryNameToMappingFileMap.get(inventoryNamePostConfiguration);
		
		if (configurationMappingXMLFile==null) {
			MappingAnalyzerWorker mappingAnalyzerWorker=convertController.getConvertFrame().getMappingAnalyzerWorker();
			Map<String, Set<String>> sourceInventoryNameToTargetInventoryNamesMap=mappingAnalyzerWorker.getMappingAnalyzer().getSourceInventoryNameToTargetInventoryNamesMap();
			Set<String> targetInventoryNames=sourceInventoryNameToTargetInventoryNamesMap.get(convertSourceGridRecordInformation.getInventoryName());
			if (targetInventoryNames!=null) {
				convertSourceGridRecordInformation.setStatus(UIConstants.UI_STATUS_COMPLETED);
				convertSourceGridRecordInformation.setRemarks("Referenced in another mapping file");
				convertSourceGridRecordInformation.setSupportText("Yes");
				convertSourceGridRecordInformation.setTotalRecordsNotFullyConverted(-1);
			}
			else {
				convertSourceGridRecordInformation.setStatus(UIConstants.UI_STATUS_WARNING);
				convertSourceGridRecordInformation.setRemarks("Unsupported - No mapping file found!");
				convertSourceGridRecordInformation.setSupportText("No");
				convertSourceGridRecordInformation.setTotalRecordsNotFullyConverted(convertSourceGridRecordInformation.getTotalRecordsToConvert());
			}
			return;
		}
		else {
			convertSourceGridRecordInformation.setSupportText("Yes");
			convertSourceGridRecordInformation.setRemarks("Converting...");
			Map<String, File> targetInventoryNameToInventoryFileMap=convertController.getConvertFrame().getTargetInventoryNameToInventoryFileMap();
			File convertedFolder=convertController.getConvertedFolder();
			Map<String, File> sourceInventoryNameToInventoryFileMap=convertController.getConvertFrame().getSourceInventoryNameToInventoryFileMap();
								
			ConvertEngine convertEngine=new ConvertEngine(convertController,convertSourceGridRecordInformation,convertedFolder,
					configurationMappingXMLFile,false,
					sourceInventoryNameToInventoryFileMap,targetInventoryNameToInventoryFileMap);
			convertEngine.convert();
			
			if (postConfigurationMappingXMLFile!=null) {
				convertEngine=new ConvertEngine(convertController,convertSourceGridRecordInformation,convertedFolder,
						postConfigurationMappingXMLFile,true,
						sourceInventoryNameToInventoryFileMap,targetInventoryNameToInventoryFileMap);
				convertEngine.convert();
			}
			
			updateObjects(convertController.getTargetInventoryNameToConvertTargetGridRecordInformationMap());
			convertSourceGridRecordInformation.setRemarks("");			
		}
	}
	
	public void updateObjects(Map<String, ConvertTargetGridRecordInformation> targetInventoryNameToConvertTargetGridRecordInformationMap) {
		Iterator<String> iterator=targetInventoryNameToConvertTargetGridRecordInformationMap.keySet().iterator();
		while (iterator.hasNext()) {
			String targetInventoryName=iterator.next();
			ConvertTargetGridRecordInformation convertTargetGridRecordInformation=targetInventoryNameToConvertTargetGridRecordInformationMap.get(targetInventoryName);
			
			if (convertTargetGridRecordInformation.getTotalRecords()==0){
				continue;
			}
			if (convertTargetGridRecordInformation.getTotalRecordsMissingRequiredColumns()>0 ||
					convertTargetGridRecordInformation.getTotalRecordsInvalidValues()>0) {
				convertTargetGridRecordInformation.setStatus(ConvertTargetPanel.STATUS_INVALID);
			}
			else {
				convertTargetGridRecordInformation.setStatus(ConvertTargetPanel.STATUS_VALID);
			}
		}
	}
	
}