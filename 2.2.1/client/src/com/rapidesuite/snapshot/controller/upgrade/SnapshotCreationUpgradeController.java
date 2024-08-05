package com.rapidesuite.snapshot.controller.upgrade;

import com.rapidesuite.snapshot.controller.SnapshotCreationCommonController;
import com.rapidesuite.snapshot.view.upgrade.TabUpgradeMainPanel;
import com.rapidesuite.snapshot.view.upgrade.UpgradeFrame;

public class SnapshotCreationUpgradeController extends SnapshotCreationCommonController{

	private UpgradeFrame upgradeFrame;
	
	public SnapshotCreationUpgradeController(UpgradeFrame upgradeFrame,String snapshotName,String snapshotDescription) {
		super(snapshotName,snapshotDescription,
				Integer.valueOf(upgradeFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getWorkersTextField().getText()),
				upgradeFrame.getTabSnapshotsPanel(),
				upgradeFrame,
				upgradeFrame.getTabUpgradeMainPanel().getCancelButton(),
				upgradeFrame.getTabUpgradeMainPanel().getEbsExecutionTimeLabel(),
				upgradeFrame.getTabUpgradeMainPanel().getEbsExecutionStatusLabel(),
				upgradeFrame.getTabUpgradeMainPanel().getSnapshotInventoryDetailsGridPanel(),
				true,
				upgradeFrame.getTabUpgradeMainPanel().getSnapshotIdValueLabel(),
				"Your Snapshot is completed! Please review the statuses",
				TabUpgradeMainPanel.EBS_EXEC_TIME,
				TabUpgradeMainPanel.EBS_EXEC_STATUS,
				true);
				
		this.upgradeFrame=upgradeFrame;
	}
	
	public UpgradeFrame getUpgradeFrame() {
		return upgradeFrame;
	}
		
	@Override
	public void updateTotalLabels() {
		upgradeFrame.updateTotalLabels(upgradeFrame.getTabUpgradeMainPanel().getSnapshotInventoryDetailsGridPanel(),
				upgradeFrame.getTabUpgradeMainPanel().getEbsExecutionFailedRowsCountLabelValue());
	}
	
	@Override
	public void postExecution() {
		upgradeFrame.setSnapshotGridRecord(this.getSnapshotGridRecord());
		upgradeFrame.getTabUpgradeMainPanel().unlockUI();
		upgradeFrame.getTabUpgradeMainPanel().setAutoRefreshFilteringOn(false);
		
		if (upgradeFrame.getSnapshotGridRecord()!=null) {
			upgradeFrame.getTabUpgradeMainPanel().getUpgradeNameValueLabel().setText(upgradeFrame.getSnapshotGridRecord().getName());
			upgradeFrame.getTabUpgradeMainPanel().getUpgradeIdValueLabel().setText(""+upgradeFrame.getSnapshotGridRecord().getSnapshotId());
			upgradeFrame.getTabUpgradeMainPanel().getEbsFiltersPanel().setComponentsEnabled(true);
		}
		
		postExecutionGeneric(true);
	}
	
}