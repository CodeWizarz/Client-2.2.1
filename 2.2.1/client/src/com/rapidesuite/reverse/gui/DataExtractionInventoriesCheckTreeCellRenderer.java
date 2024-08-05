package com.rapidesuite.reverse.gui;

import java.awt.Component;

import javax.swing.JTree;

import com.rapidesuite.client.common.gui.InventoriesCheckTreeCellRenderer;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.reverse.ReverseMain;

public class DataExtractionInventoriesCheckTreeCellRenderer extends InventoriesCheckTreeCellRenderer{ 

	private DataExtractionPanel dataExtractionPanel;

	public DataExtractionInventoriesCheckTreeCellRenderer(DataExtractionPanel dataExtractionPanel){ 
		this.dataExtractionPanel=dataExtractionPanel;
	}  

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,boolean hasFocus) {
		Component toReturn=super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
		DataExtractionInventoryTreeNode dataExtractionInventoryTreeNode=(DataExtractionInventoryTreeNode)value;
		
		if (dataExtractionInventoryTreeNode.isUnreversible()) {
			label.setBackground(UtilsConstants.GREY_COLOR);
		}
		else 
		if (dataExtractionInventoryTreeNode.isInvalidNode()) {
			label.setBackground(UtilsConstants.INVALID_OR_ERROR_COLOR);
		}
		else {
			boolean isInstanceLevel=((ReverseMain)dataExtractionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().isInstanceLevel(dataExtractionInventoryTreeNode.getName());
			boolean isOULevel=((ReverseMain)dataExtractionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().isOperatingUnitLevel(dataExtractionInventoryTreeNode.getName());
			boolean isBGLevel=((ReverseMain)dataExtractionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().isBusinessGroupLevel(dataExtractionInventoryTreeNode.getName());
			boolean isOUAndBGLevels=isOULevel && isBGLevel;
			boolean isOULevelOnly=isOULevel && !isBGLevel;
			
			if (isInstanceLevel || isOUAndBGLevels || isBGLevel) {
				label.setBackground(UtilsConstants.INSTANCE_LEVEL_COLOR);
   	    	}
   	    	else
   	    	if (isOULevelOnly) {
   	    		label.setBackground(UtilsConstants.OPERATING_UNIT_SPECIFIC_COLOR);
   	    	}
		}
		return toReturn;
	}
	
} 