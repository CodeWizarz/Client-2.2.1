package com.rapidesuite.build.gui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.rapidesuite.client.common.util.Utils;

public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer
{

	private static final long serialVersionUID = 1L;
	
	private final boolean borderPainted;
	
	public CheckBoxRenderer(final boolean borderPainted) {
		super();
		this.borderPainted = borderPainted;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		return Utils.getTableCellRendererComponent(this, table, value, isSelected, hasFocus, row, column, this.borderPainted);
	}

}