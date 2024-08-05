package com.rapidesuite.inject.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import com.rapidesuite.inject.InjectMain;

@SuppressWarnings("serial")
public class TableHeaderRenderer extends JLabel implements TableCellRenderer {

	private Color foregroundColor;
	private Color backgroundColor;
	
	public TableHeaderRenderer() {
		this(Color.decode("#047FC0"),Color.decode("#FFFFFF"));
	}
	
	public TableHeaderRenderer(Color backgroundColor,Color foregroundColor) {
		this.foregroundColor=foregroundColor;
		this.backgroundColor=backgroundColor;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int rowIndex, int vColIndex) {
		setText(value.toString());
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
		setBackground(backgroundColor);
		setOpaque(true);
		setForeground(foregroundColor);
		setFont(new Font("Arial",Font.BOLD, InjectMain.FONT_SIZE_NORMAL));
		Border border = BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray);
		setBorder(border);
		return this;
	}	
	
}
