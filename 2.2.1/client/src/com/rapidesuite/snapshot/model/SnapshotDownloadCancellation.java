package com.rapidesuite.snapshot.model;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.snapshot.controller.GenericController;

public class SnapshotDownloadCancellation  extends SnapshotSwingWorker {

	private GenericController genericController;
	
	public SnapshotDownloadCancellation(GenericController genericController) {
		this.genericController=genericController;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	
	private void processAction() {
		genericController.stopExecution();
		while ( !genericController.isExecutionCompleted()) {
			try {
				super.updateExecutionLabels("Please wait, cancellation in progress...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				FileUtils.printStackTrace(e);
			}
		}
	}

}