package com.rapidesuite.snapshot.view;

import com.rapidesuite.snapshot.controller.SnapshotCreationController;

@SuppressWarnings("serial")
public abstract class SnapshotCreationGenericFrame extends SnapshotInventoryGridFrame {

	protected SnapshotCreationController snapshotCreationController;
	protected SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel;

	public SnapshotCreationGenericFrame(TabSnapshotsPanel tabSnapshotsPanel) {
		super(tabSnapshotsPanel);
	}

	public abstract void processAction(SnapshotCreationInformationPanel snapshotCreationInformationPanel);
	
	public SnapshotCreationController getSnapshotCreationController() {
		return snapshotCreationController;
	}

	public abstract void closeWindow();

	public SnapshotInventoryDetailsGridPanel getSnapshotInventoryDetailsGridPanel() {
		return snapshotInventoryDetailsGridPanel;
	}

	public void setSnapshotInventoryDetailsGridPanel(SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel) {
		this.snapshotInventoryDetailsGridPanel = snapshotInventoryDetailsGridPanel;
	}
	
}
