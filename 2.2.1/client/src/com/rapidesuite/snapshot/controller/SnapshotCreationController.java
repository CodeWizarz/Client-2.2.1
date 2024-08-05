package com.rapidesuite.snapshot.controller;

import com.rapidesuite.snapshot.view.SnapshotCreationFrame;

public class SnapshotCreationController extends SnapshotCreationCommonController{

	private SnapshotCreationFrame snapshotCreationFrame;
	
	public SnapshotCreationController(SnapshotCreationFrame snapshotCreationFrame,String snapshotName,String snapshotDescription) {
		super(snapshotName,snapshotDescription,
				Integer.valueOf(snapshotCreationFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getWorkersTextField().getText()),
				snapshotCreationFrame.getTabSnapshotsPanel(),
				snapshotCreationFrame,
				snapshotCreationFrame.getSnapshotCreationActionsPanel().getCancelButton(),
				snapshotCreationFrame.getExecutionStatusLabel(),
				snapshotCreationFrame.getExecutionProgressLabel(),
				snapshotCreationFrame.getSnapshotInventoryDetailsGridPanel(),
				snapshotCreationFrame.isSnapshotForConversionSelected(),
				snapshotCreationFrame.getSnapshotIdValueLabel(),
				"Your Snapshot is completed! Please review the status\n"+
			"then close the Window in order to view the details of your snapshot.",
			"Execution time: ",
			"Progress: ",
			snapshotCreationFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowTotalDetails()
			);
				
		this.snapshotCreationFrame=snapshotCreationFrame;
	}
	
	public SnapshotCreationFrame getSnapshotCreationFrame() {
		return snapshotCreationFrame;
	}

	@Override
	public void updateTotalLabels() {
		snapshotCreationFrame.updateTotalLabels(snapshotCreationFrame.getSnapshotInventoryDetailsGridPanel(),
				snapshotCreationFrame.getExecutionFailedRowsCountLabelValue());
	}
		
}