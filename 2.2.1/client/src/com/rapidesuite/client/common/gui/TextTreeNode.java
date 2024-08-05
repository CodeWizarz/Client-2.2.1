/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/TextTreeNode.java $:
 * $Id: TextTreeNode.java 31694 2013-03-04 06:33:20Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public abstract class TextTreeNode extends DefaultMutableTreeNode  {

	private boolean isSetToIgnore;
	private boolean isSelected;
	protected String tooltipText;
	private String nodePath;
	
	public TextTreeNode(String name,String nodePath) {
		super(name, true);
		this.nodePath=nodePath;
	}
		
	public void pruneChildrenSetToIgnore() {
		if (children == null){
			return;
		}
		Enumeration<?> e = children.elements();
		List<TextTreeNode> nodesToRemove=new ArrayList<TextTreeNode>();
		while (e.hasMoreElements()) {
			TextTreeNode treeNode = (TextTreeNode)e.nextElement();
			if (treeNode.isSetToIgnore()) {
				nodesToRemove.add(treeNode);
			}
		}
		for (TextTreeNode treeNode:nodesToRemove) {
			this.remove(treeNode);
		}
	}
	
	public String getToolTipText() {
		if (tooltipText==null) {
			initToolTipText();
		}
		return tooltipText;
	}
	
	public abstract void initToolTipText();
	public abstract void copyResequencedNodesFlat(TextTreeNode targetNode);
	public abstract TextTreeNode getShallowCopy();
	
	public boolean isSetToIgnore() {
		return isSetToIgnore;
	}

	public void setToIgnore(boolean isSetToIgnore) {
		this.isSetToIgnore = isSetToIgnore;
	}
	
	public String getName() {
		return (String)super.getUserObject();
	}
	
	public void resetIsSelected() {
		setSelected(false); 
		if (children == null){
			return;
		}
		Enumeration<?> e = children.elements();
		while (e.hasMoreElements()) {
			TextTreeNode treeNode = (TextTreeNode)e.nextElement();
			treeNode.resetIsSelected();
		}
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public List<TextTreeNode> getFlatListSelectedLeavesNodes() {
		List<TextTreeNode> toReturn= new ArrayList<TextTreeNode>();
		if (children == null){
			if (isSelected()) {
				toReturn.add(this);
			}
			return toReturn;
		}
		Enumeration<?> e = children.elements();
		while (e.hasMoreElements()) {
			TextTreeNode treeNode = (TextTreeNode)e.nextElement();
			if (isSelected()) {
				toReturn.addAll(treeNode.getFlatListSelectedLeavesNodes());
			}
		}
		return toReturn;
	}
	
	public List<TextTreeNode>  getShallowCopyChildrenFlatList() {
		List<TextTreeNode> toReturn=new ArrayList<TextTreeNode>();
		toReturn.add(this.getShallowCopy());
		if (children == null){
			return toReturn;
		}
		Enumeration<?> e = children.elements();
		while (e.hasMoreElements()) {
			TextTreeNode treeNode = (TextTreeNode)e.nextElement();
			List<TextTreeNode> list=treeNode.getShallowCopyChildrenFlatList();
			toReturn.addAll(list);
		}
		return toReturn;
	}
	
	public List<TextTreeNode>  getAllChildrenFlatList() {
		List<TextTreeNode> toReturn=new ArrayList<TextTreeNode>();
		toReturn.add(this);
		if (children == null){
			return toReturn;
		}
		Enumeration<?> e = children.elements();
		while (e.hasMoreElements()) {
			TextTreeNode treeNode = (TextTreeNode)e.nextElement();
			List<TextTreeNode> list=treeNode.getAllChildrenFlatList();
			toReturn.addAll(list);
		}
		return toReturn;
	}
	
	public List<TextTreeNode> getChildren() {
		List<TextTreeNode> toReturn=new ArrayList<TextTreeNode>();
		if (children==null) {
			return toReturn;
		}
		Enumeration<?> e = children.elements();
		while (e.hasMoreElements()) {
			TextTreeNode treeNode = (TextTreeNode)e.nextElement();
			toReturn.add(treeNode);
		}
		return toReturn;
	}

	public String getNodePath() {
		return nodePath;
	}
	
}


