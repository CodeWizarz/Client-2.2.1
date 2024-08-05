package com.rapidesuite.inject.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class ButtonEditor extends DefaultCellEditor {
	
	protected JButton button;
	private JPanel panel;
	private String label;
	private boolean isPushed;
	private boolean isImage;
	
	public ButtonEditor(JCheckBox checkBox,Border border,int width,int height,Color backgroundColor) {
		super(checkBox);
		
		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setOpaque(true);
		if (backgroundColor!=null) {
			panel.setBackground(backgroundColor);
		}
		button = new JButton();
		if (border!=null) {
			panel.setBorder(border);
		}
		button.setPreferredSize(new Dimension(width, height));
		button.setMinimumSize(new Dimension(width, height));
		button.setMaximumSize(new Dimension(width, height));
		button.setSize(new Dimension(width, height));
		//panel.add(Box.createHorizontalGlue());
		panel.add(button);
		//panel.add(Box.createHorizontalGlue());
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
	}
	
	public ButtonEditor(JCheckBox checkBox,Border border,int width,int height,Color backgroundColor,boolean isImage){
		this(checkBox,border,width,height,backgroundColor);
		this.isImage=isImage;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (isSelected) {
			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
		} else {
			button.setForeground(table.getForeground());
			button.setBackground(table.getBackground());
		}
		label = (value == null) ? "" : value.toString();

		if (!isImage) {
			button.setText(label);
		}
				
		isPushed = true;
		return panel;
	}

	public Object getCellEditorValue() {
		if (isPushed) {
		}
		isPushed = false;
		return new String(label);
	}

	public boolean stopCellEditing() {
		isPushed = false;
		return super.stopCellEditing();
	}

	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}

	public JButton getButton() {
		return button;
	}
}