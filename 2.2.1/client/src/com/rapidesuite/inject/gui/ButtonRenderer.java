package com.rapidesuite.inject.gui;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class ButtonRenderer extends JPanel implements TableCellRenderer {

	private final JButton button;
	private boolean isImage;
	
	public ButtonRenderer(Border border,int width,int height){
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setOpaque(true);
		button = new JButton();
		if (border!=null) {
			setBorder(border);
		}
		
		button.setPreferredSize(new Dimension(width, height));
		button.setMinimumSize(new Dimension(width, height));
		button.setMaximumSize(new Dimension(width, height));
		button.setSize(new Dimension(width, height));
		//add(Box.createHorizontalGlue());
		add(button);
		//add(Box.createHorizontalGlue());
	}
	
	public ButtonRenderer(Border border,int width,int height,boolean isImage){
		this(border,width,height);
		this.isImage=isImage;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		if (!isImage) {
			button.setText(String.valueOf(value));
		}
		if (hasFocus) {
		} 
		return this;
	}

	public JButton getButton() {
		return button;
	}
	
}