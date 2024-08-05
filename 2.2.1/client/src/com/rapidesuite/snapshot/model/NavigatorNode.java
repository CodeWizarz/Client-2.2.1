package com.rapidesuite.snapshot.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rapidesuite.configurator.domain.Project;
import com.rapidesuite.configurator.utility.XmlNavigatorParser.EbsResponsibilityRecord;

public class NavigatorNode {

	private EbsResponsibilityRecord ebsResponsibilityRecord;
	private List<NavigatorNode> children;
	private List<NavigatorNode> parents;
	private boolean isVisited;

	public NavigatorNode(EbsResponsibilityRecord ebsResponsibilityRecord) {
		super();
		this.ebsResponsibilityRecord = ebsResponsibilityRecord;
		parents=new ArrayList<NavigatorNode>();
		children=new ArrayList<NavigatorNode>();
	}

	public List<NavigatorNode> getParents() {
		return parents;
	}

	public EbsResponsibilityRecord getEbsResponsibilityRecord() {
		return ebsResponsibilityRecord;
	}

	public void setEbsResponsibilityRecord(EbsResponsibilityRecord ebsResponsibilityRecord) {
		this.ebsResponsibilityRecord = ebsResponsibilityRecord;
	}

	public List<NavigatorNode> getChildren() {
		return children;
	}

	public void addChild(NavigatorNode node) {
		children.add(node);
	}
	
	public void addParent(NavigatorNode node) {
		parents.add(node);
	}
			
	public void linkNodes(Map<String,List<NavigatorNode>> menuIdToNavigatorNodesMap) throws Exception {
		isVisited=true; // This is used to avoid duplicates linkage as a node may have many parents so it will try
		// to link the children again resulting in millions of links.
		
		String subMenuId=this.getEbsResponsibilityRecord().getSubMenuId();
		List<NavigatorNode> childNodes=menuIdToNavigatorNodesMap.get(subMenuId);
		if (childNodes!=null) {
			for (NavigatorNode childNode:childNodes) {
				childNode.addParent(this);
				this.addChild(childNode);
				if (!childNode.isVisited) {
					childNode.linkNodes(menuIdToNavigatorNodesMap);
				}
			}
		}		
	}
	
	public void populateFunctionIdToFunctionIdNavigatorNodesMap(Map<String, List<NavigatorNode>> functionIdToFunctionIdNavigatorNodesMap) {
		String functionId=ebsResponsibilityRecord.getFormFunctionId();
		if (functionId!=null && !functionId.isEmpty()) {
			List<NavigatorNode> nodes=functionIdToFunctionIdNavigatorNodesMap.get(functionId);
			if (nodes==null) {
				nodes=new ArrayList<NavigatorNode>();
				functionIdToFunctionIdNavigatorNodesMap.put(functionId,nodes);
			}
			nodes.add(this);
		}
		else {
			if (children!=null) {
				for (NavigatorNode childNode:children) {
					childNode.populateFunctionIdToFunctionIdNavigatorNodesMap(functionIdToFunctionIdNavigatorNodesMap);
				}
			}
		}
	}

	public int getFunctionCount(String functionIdParameter) {
		String functionId=ebsResponsibilityRecord.getFormFunctionId();
		if (functionId!=null && !functionId.isEmpty() && functionIdParameter.equalsIgnoreCase(functionId)) {
			return 1;
		}
		else {
			int totalCount=0;
			for (NavigatorNode childNode:children) {
				int count=childNode.getFunctionCount(functionIdParameter);
				totalCount=totalCount+count;
			}
			return totalCount;
		}
	}

	public List<NavigatorNodePath> getPaths(List<String> fullPathParameter) {
		List<NavigatorNodePath> toReturn=new ArrayList<NavigatorNodePath>();
		String functionId=ebsResponsibilityRecord.getFormFunctionId();
		
		NavigatorNodePath navigatorNodePath=new NavigatorNodePath();
		navigatorNodePath.setFunctionId(functionId);
		navigatorNodePath.addAllPath(fullPathParameter);
		navigatorNodePath.addPath(this.getEbsResponsibilityRecord().getDisplayText());
		
		if (functionId!=null && !functionId.isEmpty()) {
			navigatorNodePath.setFormName(this.getEbsResponsibilityRecord().getDisplayText());
			toReturn.add(navigatorNodePath);
		}
		else {
			List<String> fullPath=new ArrayList<String>();
			fullPath.addAll(fullPathParameter);
			
			// adding all except the Top menu "-2"
			String menuId=this.getEbsResponsibilityRecord().getMenuId();
			if ( !menuId.equalsIgnoreCase(Project.REPOSITORY_BROWSER_ROOT_MENU_ID)) {
				fullPath.add(this.getEbsResponsibilityRecord().getDisplayText());
			}
			
			for (NavigatorNode childNode:children) {
				List<NavigatorNodePath> res=childNode.getPaths(fullPath);
				toReturn.addAll(res);
			}
		}
		return toReturn;
	}
	
}