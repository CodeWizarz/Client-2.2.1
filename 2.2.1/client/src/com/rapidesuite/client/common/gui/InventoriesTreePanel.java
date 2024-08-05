/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/InventoriesTreePanel.java $:
 * $Id: InventoriesTreePanel.java 31694 2013-03-04 06:33:20Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class InventoriesTreePanel extends CheckBoxTreePanel
{

	private ExecutionPanel executionPanel;
	private boolean isBackgroundColorEnabled;
	
	public InventoriesTreePanel(ExecutionPanel panel,boolean isBackgroundColorEnabled,TextCheckTreeCellRenderer textCheckTreeCellRenderer){
		super(textCheckTreeCellRenderer);
		this.executionPanel=panel;
		this.isBackgroundColorEnabled=isBackgroundColorEnabled;
	}
	
	public List<InventoryTreeNode> getSelectedInventoryTreeNodes()
	throws Exception {
		List<InventoryTreeNode> toReturn=new ArrayList<InventoryTreeNode>();
		List<TextTreeNode> selectedTextTreeNodes=getSelectedTextTreeNodes();
		for (TextTreeNode selectedTextTreeNode:selectedTextTreeNodes) {
			InventoryTreeNode inventoryTreeNode=(InventoryTreeNode)selectedTextTreeNode;
			String archiveItemName=inventoryTreeNode.getArchiveItemName();
			if (archiveItemName!=null) {
				toReturn.add(inventoryTreeNode);
			}
		}
		return toReturn;
	}

	public boolean isBackgroundColorEnabled() {
		return isBackgroundColorEnabled;
	}

	public ExecutionPanel getExecutionPanel() {
		return executionPanel;
	}
	
	public Set<String> getAllNavigationNamesUsedInScenarioSet() {
		return ((InventoryTreeNode)rootTreeNode).getAllNavigationNameSet();
	}
	
	public List<TreePath> getAllCheckedPaths() {
		return super.getSelectedFullTreePaths();
	}
	
}