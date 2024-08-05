package com.rapidesuite.snapshot.view;

@SuppressWarnings("serial")
public class SnapshotComparisonFiltersPanel extends ComparisonFiltersPanel {

	private SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame;
	
	public SnapshotComparisonFiltersPanel(SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame) {
		super(snapshotComparisonAnalysisFrame,snapshotComparisonAnalysisFrame.getTabSnapshotsPanel());
		this.snapshotComparisonAnalysisFrame=snapshotComparisonAnalysisFrame;
	}
	
	@Override
	public void mainFilteringProcess() {
		snapshotComparisonAnalysisFrame.startFiltering();
	}

}