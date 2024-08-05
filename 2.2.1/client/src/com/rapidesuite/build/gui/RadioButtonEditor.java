package com.rapidesuite.build.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class RadioButtonEditor extends DefaultCellEditor implements
		ItemListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1321320867615702581L;
	
	private final JRadioButton radioButton;

	public RadioButtonEditor(JCheckBox checkBox) {
		super(checkBox);
		this.radioButton = new JRadioButton();
		this.radioButton.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)  {
		if (value == null) {
			return null;
		}
		radioButton.addItemListener(this);
		radioButton.setSelected((Boolean) value);
		return radioButton;
	}
	
	@Override
	public Object getCellEditorValue() {
		return (Object) radioButton.isSelected();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		super.fireEditingStopped();
	}

}
