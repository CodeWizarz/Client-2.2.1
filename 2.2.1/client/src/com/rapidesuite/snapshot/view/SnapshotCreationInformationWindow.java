package com.rapidesuite.snapshot.view;

@SuppressWarnings("serial")
public class SnapshotCreationInformationWindow extends SnapshotCreationInformationPanel {

	private SnapshotCreationGenericFrame snapshotCreationGenericFrame;

	public SnapshotCreationInformationWindow(SnapshotCreationGenericFrame snapshotCreationGenericFrame,String defaultSnapshotName) {
		super(defaultSnapshotName);
		this.snapshotCreationGenericFrame=snapshotCreationGenericFrame;
	}

	@Override
	public void processActionSubmit(SnapshotCreationInformationPanel snapshotCreationInformationPanel) {
		snapshotCreationGenericFrame.processAction(snapshotCreationInformationPanel);
	}

}
