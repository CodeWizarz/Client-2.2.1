package com.rapidesuite.snapshot.model;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.view.SnapshotCreationFrame;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;

public class InitCreationGridSwingWorker extends SnapshotSwingWorker {

	private SnapshotCreationFrame snapshotCreationFrame;
	private int CURRENT_STEP_COUNTER;
	private int delayInMs;
	
	public InitCreationGridSwingWorker(SnapshotCreationFrame snapshotCreationFrame,int delayInMs) {
		super(true);
		this.snapshotCreationFrame=snapshotCreationFrame;
		CURRENT_STEP_COUNTER=0;
		this.delayInMs=delayInMs;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	private void processAction() {
		try{
			super.updateExecutionLabels("Loading Inventories...",CURRENT_STEP_COUNTER++);
			if (delayInMs!=0) {
				Thread.sleep(delayInMs);
			}
			String connectionOracleRelease=snapshotCreationFrame.getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
			Map<String, SnapshotInventoryGridRecord> inventoryInformationMap=getInventoryInformationMap();
			Map<String, File> inventoryNameToInventoryFileMap=snapshotCreationFrame.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
					getInventoryNameToInventoryFileMap(connectionOracleRelease);
			Map<String, File> inventoryNameToReverseSQLFileMap=snapshotCreationFrame.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
					getInventoryNameToReverseSQLFileMap(connectionOracleRelease);
		
			List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList=new ArrayList<SnapshotInventoryGridRecord>();
			Iterator<String> iterator=inventoryNameToInventoryFileMap.keySet().iterator();
			int gridIndex=0;
			Set<String> moduleSet=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			super.updateExecutionLabels("Initializing Inventories...",CURRENT_STEP_COUNTER++);
			boolean isShowUnsupportedInventories=snapshotCreationFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowUnsupportedInventories();
			Map<String, FormInformation> inventoryNameToFormInformationMap=snapshotCreationFrame.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
					getInventoryNameToFormInformation(connectionOracleRelease);
			
			boolean isConversion=snapshotCreationFrame.isSnapshotForConversionSelected();
			while (iterator.hasNext()) {
				String inventoryName=iterator.next();
				File reverseSQLFile=inventoryNameToReverseSQLFileMap.get(inventoryName);	
				
				if (!isShowUnsupportedInventories) {
					if (reverseSQLFile==null) {
						continue;
					}
				}
				
				if (!isConversion && inventoryName.toLowerCase().endsWith("rapidmigrate")) {
					continue;
				}
				boolean isExecutable = true;
				if(inventoryInformationMap!=null && inventoryInformationMap.size()>0){
					SnapshotInventoryGridRecord snapshotInventoryGridRecord = inventoryInformationMap.get(inventoryName);
					if(snapshotInventoryGridRecord!=null){
						isExecutable = inventoryInformationMap.get(inventoryName).isExecutable();
					}
				}	
				if(isExecutable){
					SnapshotInventoryGridRecord snapshotInventoryGridRecord=new SnapshotInventoryGridRecord(inventoryName);
					FormInformation formInformation=inventoryNameToFormInformationMap.get(snapshotInventoryGridRecord.getInventoryName());
					if (formInformation!=null) {
						snapshotInventoryGridRecord.setFormInformation(formInformation);
					}
					Set<String> applicationNameSet=snapshotInventoryGridRecord.getFormInformation().getApplicationNameSet();
					if (applicationNameSet!=null) {
						moduleSet.addAll(applicationNameSet);
					}
					if (!snapshotCreationFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowTotalDetails()) {
						snapshotInventoryGridRecord.setDisplayTotalRecords(true);
						snapshotInventoryGridRecord.setDisplayTotalAddedRecords(false);
						snapshotInventoryGridRecord.setDisplayTotalDefaultRecords(false);
						snapshotInventoryGridRecord.setDisplayTotalUpdatedRecords(false);
					}
					snapshotInventoryGridRecord.setDefaultSelected(true);
					snapshotInventoryGridRecord.setGridIndex(gridIndex);
					snapshotInventoryGridRecordList.add(snapshotInventoryGridRecord);
					
					gridIndex++;					
				}
			}
			boolean isAutomationMode = snapshotCreationFrame.getTabSnapshotsPanel().getMainPanel().getSnapshotMain().isAutomationMode();
			if(isAutomationMode){
				int retryDisplayInventories = 5;
				while(retryDisplayInventories>0){
					try{
						snapshotCreationFrame.getSnapshotInventoryDetailsGridPanel().displayInventories(snapshotInventoryGridRecordList);
						break;
					}catch(Exception innerException){
						FileUtils.println("Retrying...display inventories process.");
						Thread.sleep(5000);
						retryDisplayInventories--;
					}
				}
			}else{
				snapshotCreationFrame.getSnapshotInventoryDetailsGridPanel().displayInventories(snapshotInventoryGridRecordList);
			}
			snapshotCreationFrame.getFilterModulePanel().setAllModules(moduleSet);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}
	private Map<String, SnapshotInventoryGridRecord> getInventoryInformationMap() throws Exception, SQLException{
		Map<String, SnapshotInventoryGridRecord> toReturn=new HashMap<String,SnapshotInventoryGridRecord>();
		Connection connection= ModelUtils.getConnection(snapshotCreationFrame.getTabSnapshotsPanel());
		connection.setAutoCommit(false);
		toReturn = ModelUtils.getInventoryToSnapshotInventoryMap(connection);
		
		return toReturn;
	}
}