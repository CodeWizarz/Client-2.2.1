package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.IOUtils;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.DesktopApi;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class RegistrationAppliancePanel extends RegistrationPanel {

	private JFileChooser downloadProductIdFileChooser;
	private JButton downloadProductIdButton;
	private JFileChooser uploadProductKeyFileChooser;
	private JButton uploadProductKeyButton;

	private JButton closeButton;
	private JFrame rootFrame;
	
	public RegistrationAppliancePanel(String productName, JFrame rootFrame) {
		super(productName, ModelUtils.getEncLicInformation());
		this.rootFrame=rootFrame;
		createComponents();
	}
	
	public void createComponents(){
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		northPanel.add(tempPanel,BorderLayout.CENTER);

		ImageIcon ii=null;
		URL iconURL =null;

		int widthEmptyBordersPanels=20;
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);

		String labelText="<html>In order to activate "+displayProductName+" you will need to:<br/>"+
		" 1. Download the <b>PRODUCT ID</b> in a text file (click on 'Download' and save to a text file)<br/>"+
		" 2. Create a Support Request (click on 'Support Website') and attach the above text file<br/>"+
		" 3. A <b>PRODUCT KEY</b> in a text file will be provided then just upload it (click on 'Upload')<br/>";
		JLabel label= new JLabel(labelText);
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);
		offlineRegistrationPanel.add(Box.createRigidArea(new Dimension(15, 10)));

		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		offlineRegistrationPanel.add(tempPanel);

		downloadProductIdFileChooser= new JFileChooser();
		FileFilter imageFilter = new FileNameExtensionFilter("Text files","txt");
		downloadProductIdFileChooser.setFileFilter(imageFilter);
		downloadProductIdFileChooser.setAcceptAllFileFilterUsed(false);
		iconURL = this.getClass().getResource("/images/inject/button_viewer_download.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		downloadProductIdButton = new JButton();
		downloadProductIdButton.setIcon(ii);
		downloadProductIdButton.setBorderPainted(false);
		downloadProductIdButton.setContentAreaFilled(false);
		downloadProductIdButton.setFocusPainted(false);
		downloadProductIdButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_viewer_download_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		downloadProductIdButton.setRolloverIcon(new RolloverIcon(ii));
		downloadProductIdButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				downloadProductId();
			}
		}
				);
		tempPanel.add(downloadProductIdButton,BorderLayout.CENTER);
		
		iconURL = this.getClass().getResource("/images/inject/button_supportwebsite.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton supportWebsiteButton = new JButton();
		supportWebsiteButton.setIcon(ii);
		supportWebsiteButton.setBorderPainted(false);
		supportWebsiteButton.setContentAreaFilled(false);
		supportWebsiteButton.setFocusPainted(false);
		supportWebsiteButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_supportwebsite_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		supportWebsiteButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(supportWebsiteButton);
		supportWebsiteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
	        	try	{
	        		URI uri = URI.create(SnapshotMain.SUPPORT_TICKET_R4C_URL);
	        		DesktopApi.browse( uri );
	        	}
	        	catch (Exception e)	{
	        		FileUtils.printStackTrace(e);
	        		GUIUtils.popupErrorMessage(e.getMessage());
	        	}
			}

		});
		offlineRegistrationPanel.add(Box.createRigidArea(new Dimension(15, 10)));	
		
		uploadProductKeyFileChooser= new JFileChooser();
		uploadProductKeyFileChooser.setFileFilter(imageFilter);
		uploadProductKeyFileChooser.setAcceptAllFileFilterUsed(false);
		iconURL = this.getClass().getResource("/images/inject/button_upload.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		uploadProductKeyButton = new JButton();
		uploadProductKeyButton.setIcon(ii);
		uploadProductKeyButton.setBorderPainted(false);
		uploadProductKeyButton.setContentAreaFilled(false);
		uploadProductKeyButton.setFocusPainted(false);
		uploadProductKeyButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_upload_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		uploadProductKeyButton.setRolloverIcon(new RolloverIcon(ii));
		uploadProductKeyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				uploadProductKey();
			}
		}
				);
		tempPanel.add(uploadProductKeyButton,BorderLayout.CENTER);
		

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
	}
	
	protected void uploadProductKey() {
		try{					
			int returnVal = uploadProductKeyFileChooser.showOpenDialog(rootFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				FileInputStream inputStream = new FileInputStream(uploadProductKeyFileChooser.getSelectedFile());
				try {
				    String text = IOUtils.toString(inputStream);
				    super.processActionSave(text);
				} finally {
				    inputStream.close();
				}
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}

	protected void downloadProductId() {
		try{	
			int status = downloadProductIdFileChooser.showSaveDialog(rootFrame);
			if (status == JFileChooser.APPROVE_OPTION) {
				File selectedFile = downloadProductIdFileChooser.getSelectedFile();

				String fileName = selectedFile.getCanonicalPath();
				if (!fileName.endsWith(EXTENSION)) {
					selectedFile = new File(fileName + EXTENSION);
				}
				ModelUtils.writeToFile(selectedFile,encLicenseInformation,false);
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}

}
