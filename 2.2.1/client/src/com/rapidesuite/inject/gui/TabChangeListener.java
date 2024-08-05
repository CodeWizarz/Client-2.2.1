package com.rapidesuite.inject.gui;

import java.awt.Color;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TabChangeListener implements ChangeListener {

	private JTabbedPane jtp;

	public TabChangeListener(JTabbedPane jtp ) {
		this.jtp=jtp;
	}

	public void stateChanged(ChangeEvent ce) {
		int iSelTab = jtp.getSelectedIndex();
		Color unSelectedBackground = jtp.getBackgroundAt(iSelTab);
		for(int i=0; i<jtp.getTabCount(); i++) {
			jtp.getTabComponentAt(i).setBackground(unSelectedBackground);
			jtp.setBackgroundAt(i,unSelectedBackground);
		}
		jtp.getTabComponentAt(iSelTab).setBackground(Color.decode("#047FC0"));
		jtp.setBackgroundAt(iSelTab,Color.decode("#047FC0"));
	}

}


