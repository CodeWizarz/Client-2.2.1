package com.rapidesuite.snapshot.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.extract.view.UIConstants;
import com.rapidesuite.snapshot.controller.SnapshotController;
import com.rapidesuite.snapshot.controller.SnapshotPhysicalDeleteController;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;

public class SnapshotDeleteSwingWorker extends SnapshotSwingWorker {

	private TabSnapshotsPanel tabSnapshotsPanel;
	private Map<String,String> snapshotEnvironmentProperties;
	private List<SnapshotInventoryGridRecord> exportInventoryGridRecordList;
	private List<SnapshotGridRecord> snapshotGridRecordsList;
	private boolean isDeleteMoreThanOneSnapshot;
	private boolean isClearOutSoftDeleteSnapshot;
	
	public SnapshotDeleteSwingWorker(TabSnapshotsPanel tabSnapshotsPanel, List<SnapshotGridRecord> snapshotGridRecordsList,boolean isClearOutSoftDeleteSnapshot) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.snapshotGridRecordsList=snapshotGridRecordsList;
		snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		if(snapshotGridRecordsList.size()>0){
			isDeleteMoreThanOneSnapshot = true;
		}else{
			isDeleteMoreThanOneSnapshot = false;
		}
		this.isClearOutSoftDeleteSnapshot = isClearOutSoftDeleteSnapshot;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	
	protected void processAction() {
		Connection connection=null;
		try {
			String dbUserName=ModelUtils.getDBUserName(snapshotEnvironmentProperties);
			
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					dbUserName,
					ModelUtils.getDBPassword(snapshotEnvironmentProperties));

			super.updateExecutionLabels("Retrieving list of inventories...");
			List<Integer> snapshotIdlist=new ArrayList<Integer>();
			for(SnapshotGridRecord obj : snapshotGridRecordsList){
				snapshotIdlist.add(obj.getSnapshotId());
			}
			
			if(isClearOutSoftDeleteSnapshot){
				List<Integer> deletedSnapshotIdlist = ModelUtils.getListOfDeletedSnapshotId(connection);
				if(!deletedSnapshotIdlist.isEmpty() && deletedSnapshotIdlist.size()>0){
					for(int deletedSnapshotId : deletedSnapshotIdlist){
						snapshotIdlist.add(deletedSnapshotId);
					}
				}
			}

			List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList= ModelUtils.getSnapshotInventoryGridRecordsList(
					connection,snapshotIdlist);
			exportInventoryGridRecordList=new ArrayList<SnapshotInventoryGridRecord>();
			for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
				if(isDeleteMoreThanOneSnapshot){
					exportInventoryGridRecordList.add(snapshotInventoryGridRecord);
				}else{
					String uiStatus=ModelUtils.getUIStatusFromDBStatus(snapshotInventoryGridRecord.getStatus());
					if (uiStatus!=null && (uiStatus.equalsIgnoreCase(UIConstants.UI_STATUS_COMPLETED)||
							uiStatus.equalsIgnoreCase(UIConstants.UI_STATUS_WARNING))) {
						exportInventoryGridRecordList.add(snapshotInventoryGridRecord);
					}
				}
			}
			SnapshotPhysicalDeleteController snapshotPhysicalDeleteSnapshotController = new SnapshotPhysicalDeleteController(tabSnapshotsPanel,exportInventoryGridRecordList,snapshotIdlist);
			snapshotPhysicalDeleteSnapshotController.startExecution();	
			while ( !snapshotPhysicalDeleteSnapshotController.isExecutionCompleted()) {
				Thread.sleep(2000);
			}
			SnapshotController snapshotController=new SnapshotController(tabSnapshotsPanel,false);
			snapshotController.startExecution();
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
		finally {
			DirectConnectDao.closeQuietly(connection);	  
		}
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public List<SnapshotInventoryGridRecord> getSnapshotInventoryGridRecordList() {
		return exportInventoryGridRecordList;
	}
	
}