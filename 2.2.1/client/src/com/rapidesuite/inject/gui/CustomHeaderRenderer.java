package com.rapidesuite.inject.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
 
/**
 * http://www.codejava.net/java-se/swing/jtable-column-header-custom-renderer-examples
 *
 */
@SuppressWarnings("serial")
public class CustomHeaderRenderer extends JLabel implements TableCellRenderer {
 
    public CustomHeaderRenderer(String tooltipText,Color backgroundColor) {
        //setFont(new Font("Consolas", Font.BOLD, 14));
        //setForeground(Color.BLUE);
        setOpaque(true);
        setToolTipText(tooltipText);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEtchedBorder());
        setBackground(backgroundColor);
    }
     
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value.toString());
        return this;
    }
 
}