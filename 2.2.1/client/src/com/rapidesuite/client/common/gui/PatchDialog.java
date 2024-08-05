package com.rapidesuite.client.common.gui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import com.rapidesuite.client.common.util.GUIUtils;

@SuppressWarnings("serial")
public class PatchDialog extends JDialog {

    protected PatchManager patchManager;
    	
    public PatchDialog(String title,Frame frame,final PatchManager patchManager){
    	super(frame,true);
    	this.setTitle(title);
    	this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    	this.patchManager=patchManager;
    	this.addWindowListener (new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				if (patchManager.isUpdateInProgress()) {
					GUIUtils.popupInformationMessage("Patching in progress...");
		    		return;
				}
				else {
					dispose();
				}
			}
		});
    }
    
}
