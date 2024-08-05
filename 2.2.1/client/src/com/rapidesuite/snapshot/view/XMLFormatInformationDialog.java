package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ModelUtils;

public class XMLFormatInformationDialog {

	protected JDialog dialog;
	private JPanel mainPanel;
	private SnapshotMain snapshotMain;
	private JLabel kbProjectFileLabel;
	private File kbProjectFile;
	
	public XMLFormatInformationDialog(SnapshotMain snapshotMain) throws Exception {
		this.snapshotMain=snapshotMain;
		int width=900;
		int height=190;
		mainPanel=new JPanel();
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.decode("#dbdcdf"));
		//mainPanel.setBackground(Color.BLUE);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		createComponents();
		dialog=UIUtils.displayOperationInProgressComplexModalWindow(snapshotMain.getRootFrame(),
				"Information XML format",width,height,mainPanel,null,true,SnapshotMain.getSharedApplicationIconPath());
	}

	private void createComponents() throws Exception {
		int widthEmptyBordersPanels=0;
		
		JPanel topPanel=new JPanel();
		topPanel.setOpaque(false);
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		//topPanel.setBackground(Color.decode("#dbdcdf"));
		mainPanel.add(topPanel,BorderLayout.NORTH);

		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		centerPanel.setLayout(new BorderLayout());
		//centerPanel.setBackground(Color.red);
		mainPanel.add(centerPanel,BorderLayout.CENTER);

		JLabel label=new JLabel("<html>The XML format must be selected if you wish to import Data into RapidConfigurator."+
		"<br/><br/><b>IMPORTANT: You must build a KB project using the file below before importing your Data<br/>"+
		"(or use an existing KB project with the same revision number)</b>");
		label.setForeground(Color.decode("#2F3436"));
		topPanel.add(label);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		centerPanel.add(tempPanel);
		label=new JLabel("<html><b>KB project file: </b>");
		tempPanel.add(label);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#2F3436"));
		
		kbProjectFileLabel = new JLabel();
		kbProjectFileLabel.setForeground(Color.decode("#2F3436"));
		tempPanel.add(kbProjectFileLabel);
		
		ServerSelectionPanel serverSelectionPanel=snapshotMain.getMainPanel().getTabSnapshotsPanel().getServerSelectionPanel();
		String msg="";
		if (serverSelectionPanel.isConnected() ) {
			String currentConnectionOracleRelease=serverSelectionPanel.getCurrentConnectionOracleRelease();
			File oracleReleaseFolder=SnapshotPackageSelectionPanel.getOracleReleaseFolder(currentConnectionOracleRelease);
			
			kbProjectFile=ModelUtils.getFirst7zFile(oracleReleaseFolder);
			msg="<html><FONT color=\"#000099\"><U>"+kbProjectFile.getAbsolutePath()+"</U></FONT>";
			kbProjectFileLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			kbProjectFileLabel.addMouseListener(new MouseAdapter() {
		            @Override
		            public void mouseClicked(MouseEvent e) {
		            	openParentFolder(kbProjectFile);
		            }
		        });
		}
		else {
			 msg="Please connect RapidSnapshot to an Oracle DB prior to view the KB Project File location.";
		}	
		
		kbProjectFileLabel.setText(msg);
		kbProjectFileLabel.setHorizontalAlignment(SwingConstants.LEFT);
		kbProjectFileLabel.setOpaque(false);
	}

	public void setVisible(boolean isVisible) {
		dialog.setVisible(isVisible);
	}

	public SnapshotMain getSnapshotMain() {
		return snapshotMain;
	}
	
	public void openParentFolder(File file) {
        try {
        	if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(file.getParentFile());
			}
			else {
				SeleniumUtils.startLinuxFileBrowser(file.getParentFile());
			}
			
		} catch (IOException e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}

}
