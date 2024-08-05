package com.rapidesuite.extract.model;

import java.util.ArrayList;
import java.util.List;

import com.rapidesuite.configurator.utility.XmlNavigatorParser.EbsResponsibilityRecord;

public class NavigatorNode {

	private EbsResponsibilityRecord ebsResponsibilityRecord;
	private List<NavigatorNode> children;
	private NavigatorNode parent;

	public NavigatorNode(EbsResponsibilityRecord ebsResponsibilityRecord) {
		super();
		this.ebsResponsibilityRecord = ebsResponsibilityRecord;
	}

	public NavigatorNode getParent() {
		return parent;
	}

	public void setParent(NavigatorNode parent) {
		this.parent = parent;
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

	public void setChildren(List<NavigatorNode> children) {
		this.children = children;
	}

	public void addChild(NavigatorNode node) {
		if (children == null) {
			children = new ArrayList<NavigatorNode>();
		}
		children.add(node);
	}
	
	public List<String> getFullPathList() {
		NavigatorNode tempNode=this;
		List<String> toReturn=new ArrayList<String>();
		while (tempNode.getParent()!=null) {
			toReturn.add(0,tempNode.getEbsResponsibilityRecord().getDisplayText());
			tempNode=tempNode.getParent();
		}
		return toReturn;
	}

}