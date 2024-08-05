package com.rapidesuite.inject.gui;

import java.io.File;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;

public class InjectionPackageLoadDialog extends SnapshotSwingWorker {

	private ApplicationInfoPanel applicationInfoPanel;
	private File injectionPackage;
		
	public InjectionPackageLoadDialog(ApplicationInfoPanel applicationInfoPanel, File injectionPackage) {
		super(true);
		this.applicationInfoPanel=applicationInfoPanel;
		this.injectionPackage=injectionPackage;
		super.setTotalSteps(2);
	}

	@Override
	protected Void doInBackground() throws Exception {
		processAction();
		return null;
	}
	
	private void processAction() {
		try{		
			applicationInfoPanel.loadInjectionPackageFile(this,injectionPackage);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}

}