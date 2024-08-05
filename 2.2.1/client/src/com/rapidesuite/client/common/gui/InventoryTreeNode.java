/**************************************************
 * $Revision: 42830 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-08-15 16:54:34 +0700 (Fri, 15 Aug 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/InventoryTreeNode.java $:
 * $Id: InventoryTreeNode.java 42830 2014-08-15 09:54:34Z fajrian.yunus $:
 */

package com.rapidesuite.client.common.gui;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.math.NumberUtils;

import com.rapidesuite.configurator.domain.Inventory;

@SuppressWarnings("serial")
public abstract class InventoryTreeNode extends TextTreeNode  {

	protected String archiveItemName;
	protected boolean isNodeToRemove;
	protected Inventory inventory;
	
	public InventoryTreeNode(String inventoryName,String nodePath) {
		super(inventoryName, nodePath);
	}

	public void setArchiveItemName(String archiveItemName) {
		this.archiveItemName = archiveItemName;
	}
	
	public void removeInventoryNodesMatchingInMap(Map<String,Boolean> inventoryNamesMap) {
		if (children == null){
			Boolean isFound=inventoryNamesMap.get(getName());
			if (isFound!=null) {
				isNodeToRemove=true;
			}
			return;
		}
		
		Enumeration<?> e = children.elements();
		List<InventoryTreeNode> nodesToRemoveList=new ArrayList<InventoryTreeNode>();
		while (e.hasMoreElements()) {
			InventoryTreeNode inventoryTreeNode = (InventoryTreeNode)e.nextElement();
			inventoryTreeNode.removeInventoryNodesMatchingInMap(inventoryNamesMap);
			
			if (inventoryTreeNode.isNodeToRemove) {
				nodesToRemoveList.add(inventoryTreeNode);
				continue;
			}
		}
		for (InventoryTreeNode inventoryTreeNode:nodesToRemoveList) {
			this.remove(inventoryTreeNode);
		}
		if (super.getChildCount()==0) {
			isNodeToRemove=true;
		}
	}
		
	public int getInventoriesCount() {
		if (children==null){
			return 0;
		}
		Enumeration<?> e = children.elements();
		int totalCount=0;
		while (e.hasMoreElements()) {
			InventoryTreeNode inventoryTreeNode = (InventoryTreeNode)e.nextElement();
			if (inventoryTreeNode.getInventory()!=null) {
				totalCount++;
			}
			totalCount=totalCount+inventoryTreeNode.getInventoriesCount();
		}
		return totalCount;
	}

	public String getArchiveItemName() {
		return archiveItemName;
	}	
	
	public Set<String> getAllNavigationNameSet() {
		TreeSet<String> toReturn=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		if (children == null){
			return toReturn;
		}
		String navName=CheckTreeUtils.getNavigationName(this.getName(),children!=null);
		if (navName!=null) {
			toReturn.add(navName);
		}
		Enumeration<?> e = children.elements();
		while (e.hasMoreElements()) {
			InventoryTreeNode treeNode = (InventoryTreeNode)e.nextElement();
			Set<String> temp= treeNode.getAllNavigationNameSet();
			toReturn.addAll(temp);
		}
		return toReturn;
	}

	public void setInventory(Inventory inventory) {
		this.inventory=inventory;
	}
	
	public void resequenceNodesPerParentChild() {
		if (children == null){
			return;		
		}
		List<TextTreeNode> children=getChildren();
		for (TextTreeNode childTreeNode:children) {
			InventoryTreeNode inventoryChildTreeNode=(InventoryTreeNode)childTreeNode;
			if (inventoryChildTreeNode.getInventory()==null) {
				inventoryChildTreeNode.resequenceNodesPerParentChild();
			}
			else {
				String parentName=inventoryChildTreeNode.getInventory().getParentName();
				if (parentName!=null && !parentName.isEmpty()){
					// this node must be moved under the parent
					InventoryTreeNode parentTreeNode=getNode(children,parentName);
					if (parentTreeNode!=null && !parentName.equalsIgnoreCase(childTreeNode.getName())){
						parentTreeNode.add(childTreeNode);
					}
				}		
			}
		}
	}
	
	private InventoryTreeNode getNode(List<TextTreeNode> children,String name) {
		for (TextTreeNode childTreeNode:children) {
			InventoryTreeNode inventoryTreeNode=(InventoryTreeNode)childTreeNode;
			if ( inventoryTreeNode.getInventory()!=null &&
					 inventoryTreeNode.getInventory().getName().equalsIgnoreCase(name)
			) {
				return  inventoryTreeNode;
			}
		}
		return null;
	}
	
	public Inventory getInventory() {
		return inventory;
	}
		
	public void copyResequencedNodesFlat(TextTreeNode targetNode) {
		if (children == null){
			return;				
		}
		List<TextTreeNode> children=getChildren();
		for (TextTreeNode childTreeNode:children) {
			InventoryTreeNode inventoryTreeNode=(InventoryTreeNode)childTreeNode;
			if (inventoryTreeNode.getInventory()==null) {
				TextTreeNode childCopy=childTreeNode.getShallowCopy();
				targetNode.add(childCopy);
				childTreeNode.copyResequencedNodesFlat(childCopy);
			}
			else {
				List<TextTreeNode> shallowCopyChildrenFlatList=childTreeNode.getShallowCopyChildrenFlatList();
				for (TextTreeNode shallowCopyChild:shallowCopyChildrenFlatList) {
					targetNode.add(shallowCopyChild);
				}
			}
		}
	}
		
}