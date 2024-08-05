package com.rapidesuite.client.common.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import com.rapidesuite.client.common.util.GUIUtils;

public class InventoriesCheckTreeCellRenderer extends TextCheckTreeCellRenderer{ 

	private TristateCheckBox checkBox;
	private JPanel panel;
	protected JLabel label;
	private ImageIcon ii;

	public InventoriesCheckTreeCellRenderer(){ 
		panel = new JPanel();
		checkBox = new TristateCheckBox();
		label = new JLabel();
		label.setFocusable(true);
		label.setOpaque(true);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(checkBox);
		panel.add(label);
		ii=GUIUtils.getImageIcon(this.getClass(),"/images/table.gif");
	}  

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,boolean hasFocus) {
		TreePath path = tree.getPathForRow(row);
		label.setText(value.toString());
		label.setOpaque(true);
		label.setForeground(Color.black);
		label.setBackground(Color.white);
		checkBox.setBackground(Color.white);
		panel.setBackground(Color.white);
		
		InventoryTreeNode inventoryTreeNode=(InventoryTreeNode)value;
		if (inventoryTreeNode.getInventory()!=null) {
			label.setIcon(ii);
			checkBox.setVisible(false);
		}
		else {
			label.setIcon(null);
			checkBox.setVisible(true);
		}
		if (selected) {
			label.setBackground(new Color(177,220,254));
		}
		TextTreeUI textTreeUI=(TextTreeUI)tree.getUI();
		if(path!=null){ 
			if(super.getCheckTreeSelectionModel().isPathSelected(path, true)) {
				checkBox.setState(Boolean.TRUE);
			}
			else{ 
				checkBox.setState(super.getCheckTreeSelectionModel().isPartiallySelected(path) ? null : Boolean.FALSE);
			}
		} 
		checkBox.setEnabled(textTreeUI.isEnabled());		
		return panel;
	}

} 