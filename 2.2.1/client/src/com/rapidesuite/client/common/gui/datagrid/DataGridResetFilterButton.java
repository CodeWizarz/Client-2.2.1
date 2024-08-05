package com.rapidesuite.client.common.gui.datagrid;

import javax.swing.ImageIcon;

import org.openswing.swing.client.DataController;
import org.openswing.swing.client.GenericButton;

@SuppressWarnings("serial")
public class DataGridResetFilterButton extends GenericButton {
		
	private DataGridController dataGridController;
	
	public DataGridResetFilterButton(DataGridController dataGridController,ImageIcon imageIcon) {
		super(imageIcon);
		this.dataGridController=dataGridController;
	}
	
	protected final void executeOperation(DataController controller) throws Exception {
	   	dataGridController.resetFiltering();
	}
	
}
