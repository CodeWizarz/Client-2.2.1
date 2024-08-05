package com.rapidesuite.snapshot.model;

import java.sql.Connection;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.SnapshotViewerFrame;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;

public class InventoryCleanupSwingWorker extends SnapshotSwingWorker {

	private TabSnapshotsPanel tabSnapshotsPanel;
	private SnapshotInventoryGridRecord snapshotInventoryGridRecord;
	
	public InventoryCleanupSwingWorker(TabSnapshotsPanel tabSnapshotsPanel,
			SnapshotInventoryGridRecord snapshotInventoryGridRecord) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		this.snapshotInventoryGridRecord=snapshotInventoryGridRecord;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	
	private void processAction() {
		Connection connection=null;
		try {
			super.updateExecutionLabels("Cleaning up Snapshots History...");
			
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel)),
					ModelUtils.getDBUserName(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel)),
					ModelUtils.getDBPassword(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel)));
			String inventoryName=snapshotInventoryGridRecord.getInventoryName();

			ModelUtils.cleanupInventory(ModelUtils.getDBUserName(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel)),connection,inventoryName);
			
			GUIUtils.popupInformationMessage("Cleanup completed!");
			
			SnapshotViewerFrame snapshotViewerFrame=tabSnapshotsPanel.getSnapshotViewerFrame();
			if (snapshotViewerFrame!=null) {
				snapshotViewerFrame.runDefaultProcess();
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
		finally {
			DirectConnectDao.closeQuietly(connection);	  
		}
	}

}