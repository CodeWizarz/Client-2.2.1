package com.rapidesuite.snapshot.model;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;

public class SnapshotDropSwingWorker extends SnapshotSwingWorker {

	private TabSnapshotsPanel tabSnapshotsPanel;
	private Map<String,String> snapshotEnvironmentProperties;
	
	public SnapshotDropSwingWorker(TabSnapshotsPanel tabSnapshotsPanel) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processActionDropSnapshots();
		return null;
	}
	
	protected void processActionDropSnapshots() {
		Connection connection=null;
		try {
			String dbUserName=ModelUtils.getDBUserName(snapshotEnvironmentProperties);
			
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					dbUserName,
					ModelUtils.getDBPassword(snapshotEnvironmentProperties));

			super.updateExecutionLabels("Retrieving list of tables...");
			List<String> tableNamesList=ModelUtils.getTableNamesList(connection,dbUserName);
			
			int index=0;
			for (String tableName:tableNamesList) {
				index++;
				super.updateExecutionLabels("Dropping tables: "+index+" / "+tableNamesList.size()+" ...");
				ModelUtils.dropTable( dbUserName,connection,tableName);
			}
			super.updateExecutionLabels("Truncating core tables...");
			ModelUtils.truncateTable(connection,"INVENTORY_TO_SNAPSHOT");
			ModelUtils.truncateTable(connection,"SNAPSHOT");
			ModelUtils.truncateTable(connection,"INVENTORY");
			connection.commit();
			tabSnapshotsPanel.getSnapshotsGridPanel().resetGrid();
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to delete the snapshots. Error: "+e.getMessage());
		}
		finally {
			DirectConnectDao.closeQuietly(connection);	  
		}
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}
	
}
