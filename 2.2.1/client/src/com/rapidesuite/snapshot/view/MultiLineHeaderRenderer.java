package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import com.rapidesuite.inject.InjectMain;

@SuppressWarnings({ "serial", "rawtypes" })
public class MultiLineHeaderRenderer extends JList implements TableCellRenderer {

	private Color foregroundColor;
	private Color backgroundColor;
		
	public MultiLineHeaderRenderer(Color backgroundColor,Color foregroundColor) {
		this.foregroundColor=foregroundColor;
		this.backgroundColor=backgroundColor;
	}
	
	@SuppressWarnings({ "unchecked" })
	public MultiLineHeaderRenderer() {
		this(Color.decode("#047FC0"),Color.decode("#FFFFFF"));
		setOpaque(true);
		ListCellRenderer renderer = getCellRenderer();
		((JLabel) renderer).setHorizontalAlignment(JLabel.CENTER);
		setCellRenderer(renderer);
	}

	@SuppressWarnings({ "unchecked" })
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setFont(table.getFont());
		String str = (value == null) ? "" : value.toString();
		BufferedReader br = new BufferedReader(new StringReader(str));
		String line;
		Vector v = new Vector();
		try {
			while ((line = br.readLine()) != null) {
				v.addElement(line);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		setListData(v);

		setBackground(backgroundColor);
		setOpaque(true);
		setForeground(foregroundColor);
		setFont(new Font("Arial",Font.BOLD, InjectMain.FONT_SIZE_NORMAL));
		Border border = BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray);
		setBorder(border);

		return this;
	}

}
