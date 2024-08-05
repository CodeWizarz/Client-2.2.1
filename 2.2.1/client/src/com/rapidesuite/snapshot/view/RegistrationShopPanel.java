package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.DesktopApi;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.ProductKeyInformation;

@SuppressWarnings("serial")
public class RegistrationShopPanel extends RegistrationPanel {

	private JTextArea licenseInfoTextArea;
	private JTextArea productKeyTextArea;
	private JButton saveButton;
	private JButton closeButton;
	private JButton watchVideoButton;
	private boolean isViewScriptButton;
	private String registrationURL;
	
	public RegistrationShopPanel(String productName,boolean isViewScriptButton,String registrationURL) {
		super(productName, ModelUtils.getEncLicInformation());
		this.registrationURL=registrationURL;
		this.isViewScriptButton=isViewScriptButton;
		createComponents();
	}
	
	public void createComponents(){
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		northPanel.add(tempPanel,BorderLayout.EAST);
		
		JPanel tempLabelPanel=new JPanel();
		tempLabelPanel.setOpaque(false);
		tempLabelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		tempPanel.add(tempLabelPanel);
		JLabel label=new JLabel("<html><b>Learn how to activate "+displayProductName+"</b>");
		label.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(label,14);
		label.setOpaque(false);
		tempLabelPanel.add(label);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_watch_video.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		watchVideoButton = new JButton();
		watchVideoButton.setIcon(ii);
		watchVideoButton.setBorderPainted(false);
		watchVideoButton.setContentAreaFilled(false);
		watchVideoButton.setFocusPainted(false);
		watchVideoButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_watch_video_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		watchVideoButton.setRolloverIcon(new RolloverIcon(ii));
		tempLabelPanel=new JPanel();
		tempLabelPanel.setOpaque(false);
		tempLabelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		tempPanel.add(tempLabelPanel);
		tempLabelPanel.add(watchVideoButton);
		watchVideoButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ev) {
				try	{
	        		URI uri = URI.create(registrationURL);
	        		DesktopApi.browse( uri );
	        	}
	        	catch (Exception e)	{
	        		FileUtils.printStackTrace(e);
	        		GUIUtils.popupErrorMessage(e.getMessage());
	        	}
			}

		});
			
		int widthEmptyBordersPanels=20;
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);
		label=new JLabel("<html>In order to activate "+displayProductName+" you will need to copy the following <b>PRODUCT ID</b>"+
		" and once you click on the URL below<br/> <b>\"Registration website\"</b>, paste it in the text area.<br/> Then from there, copy the <b>PRODUCT KEY</b> and paste"+
		" it in the text area underneath. Finally click <b>'Save'</b>.");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);
		offlineRegistrationPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		
		int rows=8;
		int columns=70;
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);
		label=new JLabel("<html><b>PRODUCT ID:</b>");
		InjectUtils.assignArialPlainFont(label,14);
		label.setForeground(Color.decode("#343836"));
		label.setOpaque(false);
		tempPanel.add(label);
				
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);
		licenseInfoTextArea=new JTextArea(rows,columns);
		InjectUtils.assignArialPlainFont(licenseInfoTextArea,InjectMain.FONT_SIZE_NORMAL);
		licenseInfoTextArea.setForeground(Color.decode("#343836"));
		licenseInfoTextArea.setEditable(false);
		licenseInfoTextArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(  );
		scrollPane.setViewportView(licenseInfoTextArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		tempPanel.add(scrollPane);
		licenseInfoTextArea.setText(encLicenseInformation);
		offlineRegistrationPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_copy_cliboard.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton copyButton = new JButton();
		copyButton.setIcon(ii);
		copyButton.setBorderPainted(false);
		copyButton.setContentAreaFilled(false);
		copyButton.setFocusPainted(false);
		copyButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_copy_cliboard_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		copyButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(copyButton);
		copyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection stringSelection = new StringSelection(licenseInfoTextArea.getText());
				clipboard.setContents(stringSelection, stringSelection);
				licenseInfoTextArea.requestFocusInWindow();
				licenseInfoTextArea.selectAll();
			}

		});
		offlineRegistrationPanel.add(Box.createRigidArea(new Dimension(5, 10)));
	
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);
		iconURL = this.getClass().getResource("/images/snapshot/button_register.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton registerButton = new JButton();
		registerButton.setIcon(ii);
		registerButton.setBorderPainted(false);
		registerButton.setContentAreaFilled(false);
		registerButton.setFocusPainted(false);
		registerButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_register_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		registerButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(registerButton);
		registerButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
	        	try	{
	        		URI uri = URI.create(SnapshotMain.REGISTRATION_URL);
	        		DesktopApi.browse( uri );
	        	}
	        	catch (Exception e)	{
	        		FileUtils.printStackTrace(e);
	        		GUIUtils.popupErrorMessage(e.getMessage());
	        	}
			}

		});
		offlineRegistrationPanel.add(Box.createRigidArea(new Dimension(15, 10)));	
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);
		label=new JLabel("<html><b>PRODUCT KEY:</b>");
		InjectUtils.assignArialPlainFont(label,14);
		label.setForeground(Color.decode("#343836"));
		label.setOpaque(false);
		tempPanel.add(label);
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);
		productKeyTextArea=new JTextArea(rows,columns);
		InjectUtils.assignArialPlainFont(productKeyTextArea,InjectMain.FONT_SIZE_NORMAL);
		productKeyTextArea.setForeground(Color.decode("#343836"));
		JPopupMenu menu = new JPopupMenu();
		JMenuItem anItem= new JMenuItem("Paste");
		menu.add(anItem);
		anItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable clipData = clipboard.getContents(clipboard);
				if (clipData != null) {
					try {
						if (clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							String s = (String)(clipData.getTransferData(
									DataFlavor.stringFlavor));
							productKeyTextArea.setText(s);
						}
					} 
					catch (UnsupportedFlavorException ex) {
						FileUtils.printStackTrace(ex);
					} 
					catch (IOException ex) {
						FileUtils.printStackTrace(ex);
					}
				}
				
			}
		}
				);
		productKeyTextArea.setComponentPopupMenu( menu );
		
		productKeyTextArea.setLineWrap(true);
		scrollPane = new JScrollPane(  );
		scrollPane.setViewportView(productKeyTextArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		tempPanel.add(scrollPane);
		offlineRegistrationPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_save.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveButton = new JButton();
		saveButton.setIcon(ii);
		saveButton.setBorderPainted(false);
		saveButton.setContentAreaFilled(false);
		saveButton.setFocusPainted(false);
		saveButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveButton.setRolloverIcon(new RolloverIcon(ii));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionSave();
			}
		}
				);
		tempPanel.add(saveButton);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 10)));

		if (isViewScriptButton){
			iconURL = this.getClass().getResource("/images/snapshot/button_view_script.png");
			try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
			JButton viewScriptButton = new JButton();
			viewScriptButton.setIcon(ii);
			viewScriptButton.setBorderPainted(false);
			viewScriptButton.setContentAreaFilled(false);
			viewScriptButton.setFocusPainted(false);
			viewScriptButton.setRolloverEnabled(true);
			iconURL = this.getClass().getResource("/images/snapshot/button_view_script_rollover.png");
			try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
			viewScriptButton.setRolloverIcon(new RolloverIcon(ii));
			tempPanel.add(viewScriptButton);
			viewScriptButton.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					UIUtils.viewScript();
				}
			});
			tempPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		}

		iconURL = this.getClass().getResource("/images/snapshot/button_close.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		closeButton = new JButton();
		closeButton.setIcon(ii);
		closeButton.setBorderPainted(false);
		closeButton.setContentAreaFilled(false);
		closeButton.setFocusPainted(false);
		closeButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_close_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		closeButton.setRolloverIcon(new RolloverIcon(ii));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionClose();
			}
		}
				);
		tempPanel.add(closeButton);
		
		offlineRegistrationPanel.add(Box.createRigidArea(new Dimension(5, 10)));
	}
	
	protected void processActionSave() {
		if (productKeyTextArea.getText().trim().isEmpty()) {
			GUIUtils.popupErrorMessage("You must paste the Product Key into the Text area!");
			return;
		}
		super.processActionSave(productKeyTextArea.getText());
	}
	
	public void showPopup(ProductKeyInformation productKeyInformation) {
		licenseInfoTextArea.select(0,0);
		super.showPopup(productKeyInformation);
	}
	
}
