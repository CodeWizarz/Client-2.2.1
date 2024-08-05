package com.rapidesuite.snapshot.view;

@SuppressWarnings("serial")
public class DownloadTotalsPanel extends SnapshotInventoryTotalsPanel {

	private SnapshotDownloadGridPanel snapshotDownloadGridPanel;
	
	public DownloadTotalsPanel(SnapshotDownloadGridPanel snapshotDownloadGridPanel) {
		this.snapshotDownloadGridPanel=snapshotDownloadGridPanel;
	}

	@Override
	public void updateTotalLabels() {
	
	}

	public SnapshotDownloadGridPanel getSnapshotDownloadGridPanel() {
		return snapshotDownloadGridPanel;
	}

}
