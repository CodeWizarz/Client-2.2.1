package com.rapidesuite.client.common;

import com.rapidesuite.client.common.gui.FileBrowserOpenAction;
import com.rapidesuite.client.common.gui.InventoriesPackageSelectionPanel;
import com.rapidesuite.client.common.util.FileUtils;

public class InventoriesPackageOpenAction implements FileBrowserOpenAction
{

	private InventoriesPackageSelectionPanel panel;
	
	public InventoriesPackageOpenAction(InventoriesPackageSelectionPanel panel) {
		this.panel=panel;
	}

	public void openFileAction() {
		try {
			panel.loadDefaultInventoriesPackageFile(panel.getInventoriesPackageFile());
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
	}


}