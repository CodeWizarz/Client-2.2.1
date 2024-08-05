package com.rapidesuite.snapshot.controller.upgrade;

import java.io.File;
import java.util.Map;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.model.GenericWorker;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.convert.ConvertTargetPanel;
import com.rapidesuite.snapshot.view.upgrade.FusionInventoryInformation;
import com.rapidesuite.snapshot.view.upgrade.FusionInventoryRow;

public class UpgradeWorker extends GenericWorker {
	
	public UpgradeWorker(UpgradeController upgradeController) {
		super(upgradeController,true);
	}

	@Override
	public void execute(Object task) {
		FusionInventoryRow fusionInventoryRow=(FusionInventoryRow)task;
		UpgradeController upgradeController=(UpgradeController)super.getGenericController();
		try {
			
			fusionInventoryRow.setStartTime(System.currentTimeMillis());
			if (upgradeController.isExecutionStopped()) {
				fusionInventoryRow.setStatus(UIConstants.UI_STATUS_CANCELLED);
				return;
			}
			fusionInventoryRow.setStatus(UIConstants.UI_STATUS_PROCESSING);
			performConversion(upgradeController,fusionInventoryRow);
			
			if (!fusionInventoryRow.getStatus().equals(UIConstants.UI_STATUS_WARNING)) {
				fusionInventoryRow.setStatus(UIConstants.UI_STATUS_COMPLETED);
			}
		}
		catch (Throwable e) {
			FileUtils.printStackTrace(e);
			super.setTotalFailedTasks(super.getTotalFailedTasks()+1);
			fusionInventoryRow.setRemarks(e.getMessage());
			fusionInventoryRow.setStatus(UIConstants.UI_STATUS_FAILED);
		}
		finally {
			UpgradeController.setTime(fusionInventoryRow);
		}
	}
	
	private void performConversion(UpgradeController upgradeController,FusionInventoryRow fusionInventoryRow) throws Exception {
		String inventoryName=fusionInventoryRow.getInventoryName();
		String inventoryNamePostConfiguration=inventoryName+" - POSTCONFIG";
		
		Map<String, File> fusionInventoryNameToMappingFileMap=upgradeController.getUpgradeFrame().getTabUpgradeMainPanel().getFusionInventoryNameToMappingFileMap();
		File configurationMappingXMLFile=fusionInventoryNameToMappingFileMap.get(inventoryName);
		File postConfigurationMappingXMLFile=fusionInventoryNameToMappingFileMap.get(inventoryNamePostConfiguration);
		if (configurationMappingXMLFile==null) {
			fusionInventoryRow.setStatus(UIConstants.UI_STATUS_WARNING);
			fusionInventoryRow.setRemarks("No mapping file!");
			return;
		}
		else {
			if (upgradeController.hasFusionInventoryTaskStartedAndStoreIfSo(inventoryName)) {
				fusionInventoryRow.setStatus(UIConstants.UI_STATUS_COMPLETED);
				fusionInventoryRow.setRemarks("Already processed in another task!");
				return;
			}
			fusionInventoryRow.setRemarks("Converting...");
			File convertedFolder=upgradeController.getConvertedFolder();
	
			FusionInventoryInformation fusionInventoryInformation=fusionInventoryRow.getFusionInventoryInformation();
			Inventory inventory=FileUtils.getInventory(fusionInventoryInformation.getTempInventoryFile(),fusionInventoryRow.getInventoryName());
			fusionInventoryRow.setInventory(inventory);
			
			UpgradeEngine upgradeEngine=new UpgradeEngine(this,fusionInventoryRow,convertedFolder,configurationMappingXMLFile,false);
			upgradeEngine.convert();
			
			if (postConfigurationMappingXMLFile!=null) {
				upgradeEngine=new UpgradeEngine(this,fusionInventoryRow,convertedFolder,
						postConfigurationMappingXMLFile,true);
				upgradeEngine.convert();
			}
			
			if (fusionInventoryRow.getTotalRecords()!=0){
				if (fusionInventoryRow.getTotalRecordsMissingRequiredColumns()>0 ||
						fusionInventoryRow.getTotalRecordsInvalidValues()>0) {
					fusionInventoryRow.setStatus(ConvertTargetPanel.STATUS_INVALID);
				}
				else {
					fusionInventoryRow.setStatus(ConvertTargetPanel.STATUS_VALID);
				}
			}
			fusionInventoryRow.setRemarks("");			
		}
	}
		
}