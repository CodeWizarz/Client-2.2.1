package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.snapshot.SnapshotMain;

public class ExcelFormatInformationDialog {

	protected JDialog dialog;
	private JPanel mainPanel;
	private SnapshotMain snapshotMain;
	
	public ExcelFormatInformationDialog(SnapshotMain snapshotMain) {
		this.snapshotMain=snapshotMain;
		int width=900;
		int height=190;
		mainPanel=new JPanel();
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.decode("#dbdcdf"));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 10));

		createComponents();
		dialog=UIUtils.displayOperationInProgressComplexModalWindow(snapshotMain.getRootFrame(),
				"Information Excel format",width,height,mainPanel,null,true,SnapshotMain.getSharedApplicationIconPath());
	}
	
	public void setVisible(boolean isVisible) {
		dialog.setVisible(isVisible);
	}

	public SnapshotMain getSnapshotMain() {
		return snapshotMain;
	}
	
	private void createComponents() {
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BorderLayout());
		//centerPanel.setBackground(Color.red);
		mainPanel.add(centerPanel,BorderLayout.CENTER);
		
		JLabel label=new JLabel("<html>The Excel format is only used for reporting purposes (BR100 in Excel format)."+
				"<br/><br/><b>IMPORTANT: You cannot import the data files in this format into RapidConfigurator! "+
				"You must select the XML format option otherwise.</b>");
		label.setForeground(Color.decode("#2F3436"));
		centerPanel.add(label);
	}
	
}
