package com.rapidesuite.snapshot.model;

import com.rapidesuite.snapshot.controller.GenericController;

public class ControllerModalWindow extends SnapshotSwingWorker {
	
	private GenericController genericController;

	public ControllerModalWindow(GenericController genericController) {
		this(genericController,false);
	}
	
	public ControllerModalWindow(GenericController genericController,boolean showProgressBar) {
		super(showProgressBar);
		this.genericController=genericController;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		while (!genericController.isExecutionCompleted() && !genericController.isExecutionStopped() ) {
			Thread.sleep(1000);
		}
		return null;
	}

}
