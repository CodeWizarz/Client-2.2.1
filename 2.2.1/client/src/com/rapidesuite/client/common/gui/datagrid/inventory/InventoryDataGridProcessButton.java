package com.rapidesuite.client.common.gui.datagrid.inventory;

import javax.swing.ImageIcon;

import org.openswing.swing.client.DataController;
import org.openswing.swing.client.GenericButton;

@SuppressWarnings("serial")
public class InventoryDataGridProcessButton extends GenericButton {
	
	private InventoryDataGridController inventoryDataGridController;
	
	public InventoryDataGridProcessButton(ImageIcon imageIcon,InventoryDataGridController inventoryDataGridController) {
		super(imageIcon);
		this.inventoryDataGridController=inventoryDataGridController;
	}
	
	protected final void executeOperation(DataController controller) {
		inventoryDataGridController.executeProcess();
	}

}
