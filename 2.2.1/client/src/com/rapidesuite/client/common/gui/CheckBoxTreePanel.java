/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/CheckBoxTreePanel.java $:
 * $Id: CheckBoxTreePanel.java 31694 2013-03-04 06:33:20Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.rapidesuite.client.common.util.GUIUtils;

@SuppressWarnings("serial")
public class CheckBoxTreePanel extends TextTreePanel
{

	private CheckTreeManager checkTreeManager;
	private TextCheckTreeCellRenderer textCheckTreeCellRenderer;
	private CheckTreeSelectionModel selectionModel;
	
	public CheckBoxTreePanel(TextCheckTreeCellRenderer textCheckTreeCellRenderer){
		super();
		this.textCheckTreeCellRenderer=textCheckTreeCellRenderer;
		createComponents();
	}
			
	protected void showTree(JFrame frame,TextTreeNode treeNode,boolean isRootVisible) 
	throws Exception
	{
		super.showTree(frame,treeNode,isRootVisible);
		GUIUtils.setComponentDimension(super.getTextTreeNodeFinder().getAutoSuggestField(),350,18);
		selectionModel = new CheckTreeSelectionModel(tree.getModel()); 
		textCheckTreeCellRenderer.setSelectionModel(selectionModel);
		checkTreeManager = new CheckTreeManager(tree,textCheckTreeCellRenderer,selectionModel); 	
	}
	
	public void selectAllPaths() {
		List<TreePath> pathsToRestore=new ArrayList<TreePath>();
		TextTreeNode root = (TextTreeNode)tree.getModel().getRoot();
		TreePath path=new TreePath(root);
		pathsToRestore.add(path);
		selectPathsToRestore(pathsToRestore);
	}

	private void selectPathsToRestore(List<TreePath> pathsToRestore) {
		TreePath[] newpaths=new TreePath[pathsToRestore.size()];
		for (int i=0;i<pathsToRestore.size();i++) {
			newpaths[i]=pathsToRestore.get(i);
		}
		TreeSelectionModel treeSelectionModel =checkTreeManager.getSelectionModel();	
		treeSelectionModel.addSelectionPaths(newpaths); 
	}

	public List<TreePath> getSelectedFullTreePaths() {
		return checkTreeManager.getAllCheckedPaths();
	}
	
	public TreePath[] getSelectedPaths() {
		return checkTreeManager.getSelectionModel().getSelectionPaths();
	}
	
	public void lockAll() {
		setComponentsEnabled(false);
	}
	
	public void unlockAll() {
		setComponentsEnabled(true);
	}
	
	public void setComponentsEnabled(boolean isEnabled) {
		TextTreeUI textTreeUI=(TextTreeUI)tree.getUI();
		if (textTreeUI!=null) {
			textTreeUI.setEnabled(isEnabled);
		}
	}
		
	public List<TextTreeNode> getSelectedTextTreeNodes()
	throws Exception {
		rootTreeNode.resetIsSelected();
		List<TreePath> checkedPaths = checkTreeManager.getAllCheckedPaths();
		// list is random, sorting as they appear on the tree:
		for (TreePath checkedPath:checkedPaths) {
			Object[] paths=checkedPath.getPath();
			for (Object path:paths) {
				TextTreeNode textTreeNodeLeave=(TextTreeNode)path;
				textTreeNodeLeave.setSelected(true);
			}
		}
		TextTreeNode shallowCopyNode=rootTreeNode.getShallowCopy();
		rootTreeNode.copyResequencedNodesFlat(shallowCopyNode);
		List<TextTreeNode> selectedLeavesNodes=shallowCopyNode.getFlatListSelectedLeavesNodes();
		return selectedLeavesNodes;
	}

	public CheckTreeManager getCheckTreeManager() {
		return checkTreeManager;
	}

	public void setCheckTreeManager(CheckTreeManager checkTreeManager) {
		this.checkTreeManager = checkTreeManager;
	}

	public TextCheckTreeCellRenderer getTextCheckTreeCellRenderer() {
		return textCheckTreeCellRenderer;
	}

	public CheckTreeSelectionModel getSelectionModel() {
		return selectionModel;
	}
	
	public void restoreSelectedPaths(Map<String,String> selectedPaths) {
		checkTreeManager.restoreSelectedPaths(selectedPaths); 
	}
		
}