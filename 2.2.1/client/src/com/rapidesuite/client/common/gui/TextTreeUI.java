package com.rapidesuite.client.common.gui;

import java.awt.event.MouseEvent;

import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

public class TextTreeUI extends BasicTreeUI{ 

	private boolean isEnabled;
	
	public TextTreeUI() {
		isEnabled=true;
	}

	public void setEnabled(boolean isEnabled){ 
		this.isEnabled=isEnabled;
	} 
	
	protected void selectPathForEvent(TreePath path, MouseEvent event) {
		if (isEnabled) {
			super.selectPathForEvent(path, event);
		}
		else {
			event.consume();
		}
	}

	public boolean isEnabled() {
		return isEnabled;
	}
	
} 