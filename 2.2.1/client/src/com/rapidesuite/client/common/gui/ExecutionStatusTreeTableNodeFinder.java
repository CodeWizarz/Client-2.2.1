/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/ExecutionStatusTreeTableNodeFinder.java $:
 * $Id: ExecutionStatusTreeTableNodeFinder.java 31694 2013-03-04 06:33:20Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

@SuppressWarnings("serial")
public class ExecutionStatusTreeTableNodeFinder extends TextTreeGenericNodeFinder
{

	private JXTreeTable treeTable;
	private List<ExecutionStatusTreeTableNode> treeNodesMatchingCriteria;
	private int currentSearchItemIndex;
	private String previousTextToFind;
		
	public ExecutionStatusTreeTableNodeFinder(JXTreeTable treeTable,List<String> list){
		super(list);
		this.treeTable=treeTable;
	}
	
	public void searchForNodes() {
		if (autoSuggestField.getText().trim().isEmpty()) {
			return;
		}
		if ( previousTextToFind!=null && !previousTextToFind.isEmpty() &&  previousTextToFind.equalsIgnoreCase(autoSuggestField.getText())) {
			// go to the next item if any, else go back to the start
			currentSearchItemIndex++;
			if (currentSearchItemIndex>=treeNodesMatchingCriteria.size()) {
				currentSearchItemIndex=0;
			}
			showNode(treeNodesMatchingCriteria.get(currentSearchItemIndex));
		}
		else {
			treeNodesMatchingCriteria = findMatchingStringTreeNodes(autoSuggestField.getText());
			if (!treeNodesMatchingCriteria.isEmpty()) {
				previousTextToFind=autoSuggestField.getText();
				currentSearchItemIndex=0;
				showNode(treeNodesMatchingCriteria.get(0));
			} 
		}
	}

	private void showNode(ExecutionStatusTreeTableNode executionStatusTreeTableNode) {
		TreeNode[] nodes =((DefaultTreeTableModel) treeTable.getTreeTableModel()).getPathToRoot(executionStatusTreeTableNode);
		TreePath path = new TreePath(nodes);
		treeTable.scrollPathToVisible(path);
		int row = treeTable.getRowForPath(path);
		treeTable.setRowSelectionInterval(row, row);
	}

	private List<ExecutionStatusTreeTableNode> findMatchingStringTreeNodes(String nodeNameToFind) {
		List<ExecutionStatusTreeTableNode> children = ((ExecutionStatusTreeTableNode)treeTable.getTreeTableModel().getRoot()).firstBreadthChildren();
		List<ExecutionStatusTreeTableNode> toReturn=new ArrayList<ExecutionStatusTreeTableNode>();
		for (ExecutionStatusTreeTableNode child:children){
			String treeNodeName=child.getName();
			if (treeNodeName.toLowerCase().indexOf(nodeNameToFind.toLowerCase())!=-1) {
				toReturn.add(child);
			}
		}
		return toReturn;
	}

	
}