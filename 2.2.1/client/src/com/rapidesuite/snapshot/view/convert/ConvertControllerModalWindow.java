package com.rapidesuite.snapshot.view.convert;

import com.rapidesuite.snapshot.controller.GenericController;
import com.rapidesuite.snapshot.model.ControllerModalWindow;

public class ConvertControllerModalWindow extends ControllerModalWindow {
	
	private GenericController genericController;

	public ConvertControllerModalWindow(GenericController genericController) {
		super(genericController,false);
	}
		
	@Override
	protected Void doInBackground() throws Exception {
		while (!genericController.isExecutionCompleted() ) {
			Thread.sleep(1000);
		}
		return null;
	}

}