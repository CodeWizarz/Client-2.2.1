package com.rapidesuite.client.common.gui.datagrid;

import javax.swing.ImageIcon;

import org.openswing.swing.client.DataController;
import org.openswing.swing.client.GenericButton;

@SuppressWarnings("serial")
public class DataGridMassUpdateButton extends GenericButton {
		
	private DataGridController dataGridController;
	
	public DataGridMassUpdateButton(DataGridController dataGridController,ImageIcon imageIcon) {
		super(imageIcon);
		this.dataGridController=dataGridController;
	}
	
	protected final void executeOperation(DataController controller) throws Exception {
	   	dataGridController.showMassUpdateDataGridRowsFrame();
	}
	
}
