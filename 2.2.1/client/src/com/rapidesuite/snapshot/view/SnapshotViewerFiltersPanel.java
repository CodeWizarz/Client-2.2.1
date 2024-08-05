package com.rapidesuite.snapshot.view;

@SuppressWarnings("serial")
public class SnapshotViewerFiltersPanel extends FiltersPanel {

	private SnapshotViewerFrame snapshotViewerFrame;
	
	public SnapshotViewerFiltersPanel(SnapshotViewerFrame snapshotViewerFrame, SnapshotGridRecord snapshotGridRecord) {
		super(snapshotViewerFrame,snapshotViewerFrame.getTabSnapshotsPanel(), snapshotGridRecord);
		this.snapshotViewerFrame=snapshotViewerFrame;
	}
	
	@Override
	public void mainFilteringProcess() {
		snapshotViewerFrame.startFiltering();
	}

	@Override
	public FilterOperatingUnitCommon getFilterOperatingUnitPanel() {
		return super.filterOperatingUnitPanel;
	}

}
