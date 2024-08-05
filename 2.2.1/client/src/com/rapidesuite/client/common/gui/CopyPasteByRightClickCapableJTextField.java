package com.rapidesuite.client.common.gui;

import javax.swing.JTextField;
import com.rapidesuite.client.common.util.GUIUtils;

public class CopyPasteByRightClickCapableJTextField extends JTextField {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1994356146257136082L;

	public CopyPasteByRightClickCapableJTextField() {
		super();
		GUIUtils.addCopyPasteByRightClickCapabilityToJTextField(this, true);
	}

	/**
	 * 
	 */

}
