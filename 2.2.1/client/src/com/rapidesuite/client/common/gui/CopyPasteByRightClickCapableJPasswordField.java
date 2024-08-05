package com.rapidesuite.client.common.gui;

import javax.swing.JPasswordField;

import com.rapidesuite.client.common.util.GUIUtils;

public class CopyPasteByRightClickCapableJPasswordField extends JPasswordField {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4198074715083541589L;
	
	public CopyPasteByRightClickCapableJPasswordField() {
		super();
		GUIUtils.addCopyPasteByRightClickCapabilityToJTextField(this, false);
	}

}
