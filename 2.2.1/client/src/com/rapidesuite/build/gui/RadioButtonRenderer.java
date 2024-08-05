package com.rapidesuite.build.gui;

import java.awt.Component;

import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.rapidesuite.client.common.util.Utils;

public class RadioButtonRenderer extends JRadioButton implements TableCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2029542281739281995L;

	public RadioButtonRenderer() {
		super();
	}	

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return Utils.getTableCellRendererComponent(this, table, value, isSelected, hasFocus, row, column, false);
	}

}
