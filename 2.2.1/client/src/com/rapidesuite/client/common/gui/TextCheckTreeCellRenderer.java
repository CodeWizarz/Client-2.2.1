package com.rapidesuite.client.common.gui;

import javax.swing.tree.TreeCellRenderer;

public abstract class TextCheckTreeCellRenderer implements TreeCellRenderer{ 

	private CheckTreeSelectionModel checkTreeSelectionModel;
	
	public TextCheckTreeCellRenderer(){ 
		super();
	}

	public void setSelectionModel(CheckTreeSelectionModel checkTreeSelectionModel) {
		this.checkTreeSelectionModel=checkTreeSelectionModel;
	}

	public CheckTreeSelectionModel getCheckTreeSelectionModel() {
		return checkTreeSelectionModel;
	}  
	
} 