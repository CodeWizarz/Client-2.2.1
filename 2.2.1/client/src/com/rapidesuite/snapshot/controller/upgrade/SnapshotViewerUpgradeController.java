package com.rapidesuite.snapshot.controller.upgrade;

import javax.swing.JFrame;

import com.rapidesuite.snapshot.controller.SnapshotViewerController;
import com.rapidesuite.snapshot.view.FiltersPanel;
import com.rapidesuite.snapshot.view.SnapshotGridRecord;
import com.rapidesuite.snapshot.view.SnapshotInventoryDetailsGridPanel;
import com.rapidesuite.snapshot.view.TabSnapshotsPanel;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.upgrade.UpgradeFrame;

public class SnapshotViewerUpgradeController  extends SnapshotViewerController{

	private UpgradeFrame upgradeFrame;
	
	public SnapshotViewerUpgradeController(UpgradeFrame upgradeFrame,TabSnapshotsPanel tabSnapshotsPanel,
			SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel, JFrame parentFrame, SnapshotGridRecord snapshotGridRecord,
			FiltersPanel filtersPanel) {
		super(tabSnapshotsPanel, snapshotInventoryDetailsGridPanel, parentFrame, snapshotGridRecord, filtersPanel);
		this.upgradeFrame=upgradeFrame;
	}

	@Override
	public void postExecution() {
		snapshotInventoryDetailsGridPanel.getFilteringTable().setColumnFilterValue(
				SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_FILTERING_RESULT,UIConstants.FILTERED_IN_PREFIX);
		snapshotInventoryDetailsGridPanel.displayInventories(snapshotInventoryGridRecordList);
		upgradeFrame.getTabUpgradeMainPanel().selectEBSInventoriesRelatedToSelectedFusionInventories(true);
	}
	
}
