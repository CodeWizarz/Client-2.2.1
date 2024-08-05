package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.ProductKeyInformation;

@SuppressWarnings("serial")
public class RegistrationPanel extends JPanel{

	private JLabel productKeyStatusLabel;
	private JLabel productKeyAltStatusLabel;
	protected String displayProductName;
	private JDialog dialog;
	private String productName;
	protected JPanel northPanel;
	protected JPanel southPanel;
	protected JPanel offlineRegistrationPanel;
	protected JPanel onlineRegistrationPanel;

	protected JRadioButton onlineRegistrationRadioButton;
	protected JRadioButton offlineRegistrationRadioButton;
	
	protected JTextField activationCodeTextField;

	protected JButton onlineCloseButton;
	protected JButton onlineActivateButton;
	
	String encLicenseInformation;
	
	public static final String VALID_PRODUCT_KEY_STATUS = "VALID";
	public static final String WARNING_PRODUCT_KEY_STATUS = "WARNING";
	public static final String ERROR_PRODUCT_KEY_STATUS = "ERROR";
	public static final String LIC_FILE_NAME="license.enc";
	public static final String EXTENSION=".txt";
	
	public static String ACTIVATION_COMMAND_PARAMNAME = "rapidactivator";
	public static String ACTIVATION_COMMAND_PARAMVALUE = "activate";
	public static String ACTIVATION_CODE_PARAMNAME = "code";
	public static String ENC_LICENSE_INFORMATION_PARAMNAME = "pid";

	public RegistrationPanel(String productName, String encLicenseInformation) {
		this.productName=productName;
		this.encLicenseInformation = encLicenseInformation;
		this.displayProductName="RAPID"+Character.toUpperCase(productName.charAt(0)) + productName.substring(1);
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		createComponentsGeneric();
	}
	
	private void createComponentsGeneric(){
		northPanel=new JPanel();
		northPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BorderLayout());
		northPanel.setMaximumSize(new Dimension(920, 100));
		add(northPanel);
		
		JPanel tempPanel = new JPanel();
		add(tempPanel);
		/*ButtonGroup radioGroup = new ButtonGroup();
		onlineRegistrationRadioButton = new JRadioButton("Online Registration", true);
		onlineRegistrationRadioButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onlineRegistrationPanel.setVisible(true);
				offlineRegistrationPanel.setVisible(false);
			}
		});
		radioGroup.add(onlineRegistrationRadioButton);
		tempPanel.add(onlineRegistrationRadioButton);
		offlineRegistrationRadioButton = new JRadioButton("Offline Registration", false);
		offlineRegistrationRadioButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onlineRegistrationPanel.setVisible(false);
				offlineRegistrationPanel.setVisible(true);
			}
		});
		radioGroup.add(offlineRegistrationRadioButton);
		tempPanel.add(offlineRegistrationRadioButton);*/
		
		southPanel=new JPanel();
		southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.WHITE);
		southPanel.setLayout(new BorderLayout());
		add(southPanel,BorderLayout.SOUTH);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(true);
		tempPanel.setBackground(Color.decode("#4b4f4e"));
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		southPanel.add(tempPanel);
		
		onlineRegistrationPanel=new JPanel();
		onlineRegistrationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		onlineRegistrationPanel.setLayout(new BoxLayout(onlineRegistrationPanel, BoxLayout.Y_AXIS));
		onlineRegistrationPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		onlineRegistrationPanel.setOpaque(true);
		onlineRegistrationPanel.setBackground(Color.decode("#dbdcdf"));
		tempPanel.add(onlineRegistrationPanel);
		onlineRegistrationPanel.setVisible(true);
		
		offlineRegistrationPanel=new JPanel();
		offlineRegistrationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		offlineRegistrationPanel.setLayout(new BoxLayout(offlineRegistrationPanel, BoxLayout.Y_AXIS));
		offlineRegistrationPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		offlineRegistrationPanel.setOpaque(true);
		offlineRegistrationPanel.setBackground(Color.decode("#dbdcdf"));
		tempPanel.add(offlineRegistrationPanel);
		offlineRegistrationPanel.setVisible(false);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(true);
		tempPanel.setBackground(Color.decode("#4b4f4e"));
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
		northPanel.add(tempPanel,BorderLayout.WEST);
		
		productKeyStatusLabel=new JLabel();
		productKeyStatusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		productKeyStatusLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(productKeyStatusLabel,14);
		productKeyStatusLabel.setOpaque(false);
		tempPanel.add(productKeyStatusLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		
		productKeyAltStatusLabel=new JLabel();
		productKeyAltStatusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		productKeyAltStatusLabel.setForeground(Color.white);
		InjectUtils.assignArialPlainFont(productKeyAltStatusLabel,14);
		productKeyAltStatusLabel.setOpaque(false);
		tempPanel.add(productKeyAltStatusLabel);

		int widthEmptyBordersPanels=20;
		
		ImageIcon ii=null;
		URL iconURL =null;

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		onlineRegistrationPanel.add(tempPanel);
		JLabel label=new JLabel("<html>In order to activate "+displayProductName+" you will need to enter the Product Activation code in the text field below."+
		" You can find this Product Activation code<br/>on the website under My Account > Activation Codes.<br/> After entering the Product Activation Code, click on Activate button, and wait for the product to be activated." + 
		" Once the product is activated you can click<br/>on the Close button, and continue using " + displayProductName + ".<br/>" + 
		"This method required "+displayProductName+" to be connected to the internet, if you do not have direct access to internet please go to the offline activation mode.");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);
		onlineRegistrationPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		onlineRegistrationPanel.add(tempPanel);
		label=new JLabel("<html><b>ACTIVATION CODE:</b>");
		InjectUtils.assignArialPlainFont(label,14);
		label.setForeground(Color.decode("#343836"));
		label.setOpaque(false);
		tempPanel.add(label);
				
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		onlineRegistrationPanel.add(tempPanel);
		activationCodeTextField=new JTextField(70);
		InjectUtils.assignArialPlainFont(activationCodeTextField,InjectMain.FONT_SIZE_NORMAL);
		activationCodeTextField.setForeground(Color.decode("#343836"));
		activationCodeTextField.setEditable(true);
		tempPanel.add(activationCodeTextField);
		onlineRegistrationPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		
		tempPanel=new JPanel();
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		onlineRegistrationPanel.add(tempPanel);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_activate_green.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		onlineActivateButton = new JButton();
		onlineActivateButton.setIcon(ii);
		onlineActivateButton.setBorderPainted(false);
		onlineActivateButton.setContentAreaFilled(false);
		onlineActivateButton.setFocusPainted(false);
		onlineActivateButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_activate_green_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		onlineActivateButton.setRolloverIcon(new RolloverIcon(ii));
		onlineActivateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{				
				processActionActivate();
			}
		}
				);
		tempPanel.add(onlineActivateButton);
		tempPanel.add(Box.createRigidArea(new Dimension(10, 10)));

		iconURL = this.getClass().getResource("/images/snapshot/button_close.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		onlineCloseButton = new JButton();
		onlineCloseButton.setIcon(ii);
		onlineCloseButton.setBorderPainted(false);
		onlineCloseButton.setContentAreaFilled(false);
		onlineCloseButton.setFocusPainted(false);
		onlineCloseButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_close_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		onlineCloseButton.setRolloverIcon(new RolloverIcon(ii));
		onlineCloseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionClose();
			}
		}
				);
		tempPanel.add(onlineCloseButton);
		
		onlineRegistrationPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		onlineRegistrationPanel.setVisible(false);
		offlineRegistrationPanel.setVisible(true);
	}
	
	protected void processActionClose() {
		close();
	}

	protected void processActionSave(String text) {
		File licenceFile=new File(LIC_FILE_NAME);
		ModelUtils.writeToFile(licenceFile,text,false);
		ProductKeyInformation productKeyInformation=ModelUtils.getProductKeyInformation(productName);
		updateLabels(productKeyInformation);
	}
	
	protected void processActionActivate() {
		if (activationCodeTextField.getText().trim().isEmpty()) {
			GUIUtils.popupErrorMessage("You must paste the Activation code from the website into the Text field!");
			return;
		}
		
		try {			
			ActivateProductSwingWorker activateProductSwingWorker = new ActivateProductSwingWorker(ACTIVATION_COMMAND_PARAMNAME, ACTIVATION_COMMAND_PARAMVALUE, 
					ACTIVATION_CODE_PARAMNAME, activationCodeTextField.getText().trim(), 
					ENC_LICENSE_INFORMATION_PARAMNAME, ModelUtils.getEncLicInformation());
			
			UIUtils.displayOperationInProgressModalWindow(this, 450, 150, "Product Activation", activateProductSwingWorker, SnapshotMain.getSharedApplicationIconPath());
			
			String productKey = activateProductSwingWorker.getGeneratedProductKey();
			
			if(productKey != null && !productKey.isEmpty()) {
				processActionSave(productKey);
			}
		} catch (Throwable t) {
			GUIUtils.popupErrorMessage(t.getMessage());
		}
		finally {
		}
	}

	public void setCustomWindowClosingListener(final JDialog dialog) {
		this.dialog=dialog;
		dialog.addWindowListener (new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				close();
			}
		});
	}
	
	private void close() {
		ProductKeyInformation productKeyInformation=ModelUtils.getProductKeyInformation(productName);
		String status=productKeyInformation.getStatus();
		if (status.equalsIgnoreCase(VALID_PRODUCT_KEY_STATUS)) {
			dialog.dispose();
		}
		else {
			System.exit(0);
		}
	}
	
	private void updateLabels(ProductKeyInformation productKeyInformation) {
		productKeyStatusLabel.setText("");
		productKeyAltStatusLabel.setText("");
		productKeyAltStatusLabel.setOpaque(false);
		productKeyStatusLabel.setOpaque(true);
		String status=productKeyInformation.getStatus();
		if (status.equalsIgnoreCase(VALID_PRODUCT_KEY_STATUS)) {
			String text=VALID_PRODUCT_KEY_STATUS+" : "+productKeyInformation.getValidMessage();
			productKeyStatusLabel.setText("<html><body style='width: 300px'><b>"+text+"</b>");
			productKeyStatusLabel.setBackground(Color.decode("#08bc08"));
			
			String warningMessage=productKeyInformation.getWarningMessage();
			if (warningMessage!=null) {
				text=WARNING_PRODUCT_KEY_STATUS+" : "+productKeyInformation.getWarningMessage();
				productKeyAltStatusLabel.setText("<html><b>"+text+"</b>");
				productKeyAltStatusLabel.setOpaque(true);
				productKeyAltStatusLabel.setBackground(Color.ORANGE);
			}
		}
		else
			if (status.equalsIgnoreCase(ERROR_PRODUCT_KEY_STATUS)) {
				String text=ERROR_PRODUCT_KEY_STATUS+" : "+productKeyInformation.getErrorMessage();
				productKeyStatusLabel.setText("<html><body style='width: 300px'><b>"+text+"</b>");
				productKeyStatusLabel.setBackground(Color.decode("#ee3630"));
			}
	}
	
	public void showPopup(ProductKeyInformation productKeyInformation) {
		updateLabels(productKeyInformation);
		dialog.setVisible(true);
	}

	public String getDisplayProductName() {
		return displayProductName;
	}
	
}