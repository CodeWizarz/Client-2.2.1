package com.rapidesuite.client.common.gui.datagrid.inventory;

import javax.swing.ImageIcon;

import org.openswing.swing.client.DataController;
import org.openswing.swing.client.GenericButton;

@SuppressWarnings("serial")
public class InventoryDataGridImportButton extends GenericButton {
		
	private InventoryDataGridController inventoryDataGridController;
	
	public InventoryDataGridImportButton(ImageIcon imageIcon,InventoryDataGridController inventoryDataGridController) {
		super(imageIcon);
		this.inventoryDataGridController=inventoryDataGridController;
	}
	
	protected final void executeOperation(DataController controller) throws Exception {
		inventoryDataGridController.importXMLDataFile(null);
	}
	
}
