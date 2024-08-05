package com.rapidesuite.client.common.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTreeTable;

import com.rapidesuite.client.common.util.UtilsConstants;

@SuppressWarnings("serial")
public class ExecutionStatusTreeTable extends JXTreeTable 
{
	
	public ExecutionStatusTreeTable(TreeTableModel treeTableModel) {
		super(treeTableModel);
	}
	
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
	    Component c = super.prepareRenderer(renderer, row, column);
	    c.setForeground(Color.BLACK);
	    ExecutionStatusTreeTableNode node = (ExecutionStatusTreeTableNode) this.getPathForRow(row).getLastPathComponent();
	    if (column==ExecutionStatusPanel.STATUS_COLUMN_INDEX) {
	    	String status=(String) getTreeTableModel().getValueAt(node,ExecutionStatusPanel.STATUS_COLUMN_INDEX);	
	    	if (status==null){
	    		return c;
	    	}
	    	if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_SUCCESS)) {
	    		c.setBackground(UtilsConstants.SUCCESS_COLOR);
	    	}
	    	else
	    	if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_ERROR)) {
	    		c.setBackground(UtilsConstants.INVALID_OR_ERROR_COLOR);
		    }
	    	else
		    if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_WARNING)) {
		    	c.setBackground(UtilsConstants.WARNING_COLOR);
			}
		    else
			if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_PROCESSING)) {
			 	c.setBackground(UtilsConstants.PROCESSING_COLOR);
			}
			else
			if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_RETRY)) {
				c.setBackground(UtilsConstants.INVALID_OR_ERROR_COLOR);
			}
			else
			if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_UNPROCESSED) 
			) {
			 	c.setBackground(UtilsConstants.UNPROCESSED_COLOR);
			}
		    else
	    	if (status.startsWith(ExecutionStatusTreeTableNode.STATUS_DONE)) {
	    		if (status.toLowerCase().indexOf(ExecutionStatusTreeTableNode.STATUS_ERROR.toLowerCase())!=-1) {
	    			c.setBackground(UtilsConstants.INVALID_OR_ERROR_COLOR);
	    		}
	    		else {
	    			c.setBackground(UtilsConstants.SUCCESS_COLOR);
	    		}
	    	}
	    	else {
	    		c.setBackground(Color.white);
	    	}
	    }
	    //c.setFont(new Font("Helvetica Bold", Font.ITALIC, 22));
	    
	    return c;
	}
	 
}