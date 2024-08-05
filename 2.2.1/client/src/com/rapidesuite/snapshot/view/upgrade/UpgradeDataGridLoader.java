package com.rapidesuite.snapshot.view.upgrade;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;

public class UpgradeDataGridLoader extends SnapshotSwingWorker {

	private int CURRENT_STEP_COUNTER;
	private TabUpgradeMainPanel tabUpgradeMainPanel;
	private List<DataRow> dataRows;
	private Inventory sourceInventory;
	private StringBuffer sqlQueryDataRows;
	private int startRowNumToFetch;
	private int endRowNumToFetch;
	
	public UpgradeDataGridLoader(TabUpgradeMainPanel tabUpgradeMainPanel,Inventory sourceInventory,StringBuffer sqlQueryDataRows,
			int startRowNumToFetch,int endRowNumToFetch) {
		super(true);
		CURRENT_STEP_COUNTER=0;
		super.setTotalSteps(endRowNumToFetch);
		this.sourceInventory=sourceInventory;
		this.tabUpgradeMainPanel=tabUpgradeMainPanel;
		this.sqlQueryDataRows=sqlQueryDataRows;
		this.startRowNumToFetch=startRowNumToFetch;
		this.endRowNumToFetch=endRowNumToFetch;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	
	private void processAction() {
		Connection connection=null;
		try{		
			super.updateExecutionLabels("Loading data, please wait...",CURRENT_STEP_COUNTER);
		
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabUpgradeMainPanel.getUpgradeFrame().getTabSnapshotsPanel());
			connection=DatabaseUtils.getJDBCConnection(
						ModelUtils.getJDBCString(snapshotEnvironmentProperties),
						ModelUtils.getDBUserName(snapshotEnvironmentProperties),
						ModelUtils.getDBPassword(snapshotEnvironmentProperties));
			
			dataRows=ModelUtils.getDataRows(connection,sourceInventory,sqlQueryDataRows.toString(),startRowNumToFetch,endRowNumToFetch,
					this,tabUpgradeMainPanel.getUpgradeFrame().getSnapshotGridRecord());
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
			return;
		}
		finally {
			DirectConnectDao.closeQuietly(connection);	  
		}
	}

	public List<DataRow> getDataRows() {
		return dataRows;
	}

}
