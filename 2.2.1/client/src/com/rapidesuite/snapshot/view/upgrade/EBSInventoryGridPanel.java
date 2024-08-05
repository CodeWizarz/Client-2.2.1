package com.rapidesuite.snapshot.view.upgrade;

import java.io.File;
import javax.swing.JFrame;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.SnapshotInventoryDetailsGridPanel;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridFrame;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class EBSInventoryGridPanel extends SnapshotInventoryDetailsGridPanel {

	private TabUpgradeMainPanel tabUpgradeMainPanel;

	public EBSInventoryGridPanel(TabUpgradeMainPanel tabUpgradeMainPanel,SnapshotInventoryGridFrame snapshotInventoryGridFrame, boolean hasSelectionColumn,
			boolean hasViewChangesColumn, boolean hasFilteringResultColumn, boolean isRefreshEveryListOnUse,
			boolean hasCleanupColumn, boolean isSnapshotCreation, boolean isShowBalloons, boolean isUpgrade,
			int topBorderSpace) {
		super(snapshotInventoryGridFrame, hasSelectionColumn, hasViewChangesColumn, hasFilteringResultColumn,
				isRefreshEveryListOnUse, hasCleanupColumn, isSnapshotCreation, isShowBalloons, isUpgrade, topBorderSpace,false);
		this.tabUpgradeMainPanel=tabUpgradeMainPanel;
	}

	public void viewChanges(int viewRow) {
		try{
			int modelRow=table.convertRowIndexToModel(viewRow);
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=super.snapshotInventoryGridRecordsList.get(modelRow);
			String status=snapshotInventoryGridRecord.getStatus();
			status=ModelUtils.getUIStatusFromDBStatus(status);
			if ( !status.equals(UIConstants.UI_STATUS_COMPLETED)) {
				GUIUtils.popupInformationMessage("Invalid Snapshot status!");
				return;
			}
			
			int	totalRecords=snapshotInventoryGridRecord.getTotalRecords();
			if (totalRecords==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Records to convert: 0");
				return;
			}
			JFrame rootFrame=tabUpgradeMainPanel.getUpgradeFrame();
			
			String inventoryName=snapshotInventoryGridRecord.getInventoryName();
			Inventory sourceInventory=snapshotInventoryGridRecord.getInventory();
			if (sourceInventory==null) {
				File sourceInventoryFile=tabUpgradeMainPanel.getEbsInventoryNameToInventoryFileMap().
						get(inventoryName);
				if (sourceInventoryFile==null) {
					throw new Exception("Unable to find the inventory file: '"+inventoryName+"'");
				}
				sourceInventory=FileUtils.getInventory(sourceInventoryFile,inventoryName);
				snapshotInventoryGridRecord.setInventory(sourceInventory);
			}
			
			int startRowNumToFetch=1;
			int endRowNumToFetch=50000;
			StringBuffer whereClauseViewData=new StringBuffer("");
			if (snapshotInventoryGridRecord.getWhereClauseFilter()!=null) {
				whereClauseViewData.append(snapshotInventoryGridRecord.getWhereClauseFilter());
			}
			
			if(tabUpgradeMainPanel.isViewDataChangesOnlyCheckBox()) {
				StringBuffer whereClauseSeededOracleUserIds=ModelUtils.getChangesOnlyWhereClause(
						tabUpgradeMainPanel.getUpgradeFrame().getTabSnapshotsPanel().getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());
				whereClauseViewData.append(whereClauseSeededOracleUserIds);
			}
			
			StringBuffer sqlQueryDataRows=ModelUtils.getSQLQueryViewerDataRows(
					tabUpgradeMainPanel.getUpgradeFrame().getSnapshotGridRecord().getSnapshotId(),
					snapshotInventoryGridRecord.getTableId(),whereClauseViewData);

			UpgradeDataGridLoader swingWorker=new  UpgradeDataGridLoader(tabUpgradeMainPanel,sourceInventory,sqlQueryDataRows,
					startRowNumToFetch,endRowNumToFetch);
			
			final int width=450;
			final int height=150;
			UIUtils.displayOperationInProgressModalWindow(tabUpgradeMainPanel.getUpgradeFrame(),
					width,height,"Loading...",swingWorker,true,InjectMain.getSharedApplicationIconPath());
					
			if (swingWorker.getDataRows()==null) {
				return;
			}
			
			UpgradeDataGrid upgradeDataGrid=new UpgradeDataGrid(rootFrame,sourceInventory,swingWorker.getDataRows(),true,null);
			if (sourceInventory.getFieldNamesUsedForDataEntry().size()<=6) {
				upgradeDataGrid.pack();
			}			
			upgradeDataGrid.setVisible(true);
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}
	
}
