/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/TextTreeNodeFinder.java $:
 * $Id: TextTreeNodeFinder.java 31694 2013-03-04 06:33:20Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class TextTreeNodeFinder extends TextTreeGenericNodeFinder
{

	private JTree tree;
	private List<TextTreeNode> treeNodesMatchingCriteria;
	private int currentSearchItemIndex;
	private String previousTextToFind;
	
	public TextTreeNodeFinder(JTree tree,List<String> list){
		super(list);
		this.tree=tree;
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
	
	private void showNode(TextTreeNode textTreeNode) {
		TreeNode[] nodes =((DefaultTreeModel) tree.getModel()).getPathToRoot(textTreeNode);
		TreePath path = new TreePath(nodes);
		tree.scrollPathToVisible(path);
		tree.setSelectionPath(path);
	}

	@SuppressWarnings("rawtypes")
	private List<TextTreeNode> findMatchingStringTreeNodes(String nodeNameToFind) {
		Enumeration e = ((TextTreeNode)tree.getModel().getRoot()).breadthFirstEnumeration();
		List<TextTreeNode> toReturn=new ArrayList<TextTreeNode>();
		while (e.hasMoreElements()) {
			InventoryTreeNode treeNode  = (InventoryTreeNode) e.nextElement();
			String treeNodeName=treeNode.getName();
			if (treeNodeName.toLowerCase().indexOf(nodeNameToFind.toLowerCase())!=-1) {
				toReturn.add(treeNode);
			}
		}
		return toReturn;
	}
	
}