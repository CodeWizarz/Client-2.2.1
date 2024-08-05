package com.rapidesuite.inject.gui;

import java.awt.Component;

import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class PasswordCellRenderer extends JPasswordField
implements TableCellRenderer {
	
	public PasswordCellRenderer() {
		super();
		// This displays astericks in fields since it is a password.
		// It does not affect the actual value of the cell.
		//this.setText("filler123");
	}

	public Component getTableCellRendererComponent(
			JTable  arg0,
			Object value,
			boolean arg2,
			boolean arg3,
			int arg4,
			int arg5) {

		setText((String)value);
		
		return this;
	}
}