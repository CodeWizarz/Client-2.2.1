package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;

public class ServerFrame {

	private ServerSelectionPanel serverSelectionPanel;
	private JDialog dialog;
	
	private JLabel serverNameLabel;
	private JTextField serverNameTextField;
	private JLabel serverHostLabel;
	private JTextField serverHostTextField;
	private JLabel serverPortLabel;
	private JTextField serverPortTextField;
	private JLabel serverSIDLabel;
	private JTextField serverSIDTextField;
	private JLabel serverUserNameLabel;
	private JTextField serverUserNameTextField;
	private JLabel serverPasswordLabel;
	private JPasswordField serverPasswordField;
	private JLabel serviceNameLabel;
	private JTextField serviceNameTextField;
	private JLabel serviceTypeLabel;
	private JComboBox serviceTypeComboBox;
	
	public static final String CONNECTION_NAME_PROPERTY="CONNECTION_NAME";
	public static final String HOST_NAME_PROPERTY="HOST_NAME";
	public static final String PORT_NUMBER_PROPERTY="PORT_NUMBER";
	public static final String SID_PROPERTY="SID";
	public static final String USER_NAME_PROPERTY="USER_NAME";
	public static final String PASSWORD_PROPERTY="PASSWORD";
	public static final String SNAPSHOT_CREATOR_USERNAME_PROPERTY="SNAPSHOT_CREATOR_USERNAME";
	public static final String SNAPSHOT_CREATOR_PASSWORD_PROPERTY="SNAPSHOT_CREATOR_PASSWORD";
	public static final String SERVICE_TYPE_PROPERTY="SERVICE_TYPE";
	public static final String SERVICE_NAME_PROPERTY="SERVICE_NAME";

	public ServerFrame(ServerSelectionPanel serverSelectionPanel,String serverConnectionName) {
		this.serverSelectionPanel=serverSelectionPanel;

		dialog=new JDialog();
		dialog.setIconImage(GUIUtils.getImageIcon(this.getClass(), serverSelectionPanel.getTabSnapshotsPanel().getMainPanel().
				getSnapshotMain().getApplicationIconPath()).getImage());
		dialog.setTitle("Database Information");
		dialog.setModal(true);
		dialog.setSize(450,370);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);		
		dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));

		int labelsWidth=130;
		int fieldsWidth=220;
		int height=20;
		JPanel mainPanel=new JPanel();
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.decode("#dbdcdf"));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		dialog.add(mainPanel);

		JPanel panel=new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(panel);
		serverNameLabel=new JLabel("DB Connection name:");
		InjectUtils.assignArialPlainFont(serverNameLabel,InjectMain.FONT_SIZE_NORMAL);
		serverNameLabel.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(serverNameLabel,labelsWidth,height);
		panel.add(serverNameLabel);
		serverNameTextField=new JTextField();
		UIUtils.setDimension(serverNameTextField,fieldsWidth,height);
		panel.add(serverNameTextField);

		panel=new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(panel);
		serverHostLabel=new JLabel("DB Host name:");
		InjectUtils.assignArialPlainFont(serverHostLabel,InjectMain.FONT_SIZE_NORMAL);
		serverHostLabel.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(serverHostLabel,labelsWidth,height);
		panel.add(serverHostLabel);
		serverHostTextField=new JTextField();
		UIUtils.setDimension(serverHostTextField,fieldsWidth,height);
		panel.add(serverHostTextField);

		panel=new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(panel);
		serverPortLabel=new JLabel("DB Port number:");
		InjectUtils.assignArialPlainFont(serverPortLabel,InjectMain.FONT_SIZE_NORMAL);
		serverPortLabel.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(serverPortLabel,labelsWidth,height);
		panel.add(serverPortLabel);
		serverPortTextField=new JTextField();
		UIUtils.setDimension(serverPortTextField,fieldsWidth,height);
		panel.add(serverPortTextField);

		panel=new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(panel);
		serviceTypeLabel=new JLabel("DB Service type:");
		InjectUtils.assignArialPlainFont(serviceTypeLabel,InjectMain.FONT_SIZE_NORMAL);
		serviceTypeLabel.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(serviceTypeLabel,labelsWidth,height);
		panel.add(serviceTypeLabel);
		serviceTypeComboBox = new JComboBox();
		serviceTypeComboBox.addItem(UtilsConstants.DEFAULT_DATABASE_SERVICE_TYPE);
		serviceTypeComboBox.addItem(UtilsConstants.SID_DATABASE_SERVICE_TYPE);
		serviceTypeComboBox.addItem(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE);
		serviceTypeComboBox.setSelectedItem(UtilsConstants.DEFAULT_DATABASE_SERVICE_TYPE);
		UIUtils.setDimension(serviceTypeComboBox,fieldsWidth,height);
		panel.add(serviceTypeComboBox);
		serviceTypeComboBox.addItemListener(new ItemListener()
		{

			public void itemStateChanged(ItemEvent evt)
			{
				if ( evt.getStateChange() == ItemEvent.SELECTED )
				{
					JComboBox cb = (JComboBox) evt.getSource();
					String serviceType = (String) cb.getSelectedItem();
					// Item was just selected
					if ( serviceType.equalsIgnoreCase(UtilsConstants.SID_DATABASE_SERVICE_TYPE) ){
						serverSIDTextField.setEnabled(true);
						serverSIDTextField.setBackground(Color.white);
						serviceNameTextField.setEnabled(false);
						serviceNameTextField.setBackground(Color.lightGray);
						serviceNameTextField.setText("");

					}
					else if ( serviceType.equalsIgnoreCase(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE) ){
						serverSIDTextField.setEnabled(false);
						serverSIDTextField.setBackground(Color.lightGray);
						serverSIDTextField.setText("");
						serviceNameTextField.setEnabled(true);
						serviceNameTextField.setBackground(Color.white);
					}					
					else{
						serverSIDTextField.setEnabled(false);
						serverSIDTextField.setBackground(Color.lightGray);
						serverSIDTextField.setText("");
						serviceNameTextField.setEnabled(false);
						serviceNameTextField.setBackground(Color.lightGray);
						serviceNameTextField.setText("");
					}
				}
				else if ( evt.getStateChange() == ItemEvent.DESELECTED )
				{
					// Item is no longer selected
				}
			}
		});
		
		
		
		
		
		
		
		panel=new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(panel);
		serverSIDLabel=new JLabel("DB SID:");
		InjectUtils.assignArialPlainFont(serverSIDLabel,InjectMain.FONT_SIZE_NORMAL);
		serverSIDLabel.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(serverSIDLabel,labelsWidth,height);
		panel.add(serverSIDLabel);
		serverSIDTextField=new JTextField();
		UIUtils.setDimension(serverSIDTextField,fieldsWidth,height);
		panel.add(serverSIDTextField);
		
		
		panel=new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(panel);
		serviceNameLabel=new JLabel("DB Service name:");
		InjectUtils.assignArialPlainFont(serviceNameLabel,InjectMain.FONT_SIZE_NORMAL);
		serviceNameLabel.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(serviceNameLabel,labelsWidth,height);
		panel.add(serviceNameLabel);
		serviceNameTextField=new JTextField();
		UIUtils.setDimension(serviceNameTextField,fieldsWidth,height);
		panel.add(serviceNameTextField);
		

		panel=new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(panel);
		serverUserNameLabel=new JLabel("DB User name:");
		InjectUtils.assignArialPlainFont(serverUserNameLabel,InjectMain.FONT_SIZE_NORMAL);
		serverUserNameLabel.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(serverUserNameLabel,labelsWidth,height);
		panel.add(serverUserNameLabel);
		serverUserNameTextField=new JTextField();
		UIUtils.setDimension(serverUserNameTextField,fieldsWidth,height);
		panel.add(serverUserNameTextField);
	
		panel=new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(panel);
		serverPasswordLabel=new JLabel("DB Password:");
		InjectUtils.assignArialPlainFont(serverPasswordLabel,InjectMain.FONT_SIZE_NORMAL);
		serverPasswordLabel.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(serverPasswordLabel,labelsWidth,height);
		panel.add(serverPasswordLabel);
		serverPasswordField=new JPasswordField();
		UIUtils.setDimension(serverPasswordField,fieldsWidth,height);
		panel.add(serverPasswordField);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_save.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton saveButton = new JButton();
		saveButton.setIcon(ii);
		saveButton.setBorderPainted(false);
		saveButton.setContentAreaFilled(false);
		saveButton.setFocusPainted(false);
		saveButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveButton.setRolloverIcon(new RolloverIcon(ii));
		panel=new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout());
		mainPanel.add(panel);
		panel.add(saveButton);
		saveButton.addActionListener(new ActionListener() 
		{

			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		
		if (serverConnectionName!=null && !serverConnectionName.isEmpty()) {
			load(serverConnectionName);
		}
		
		dialog.setVisible(true);
	}


	private void save() 
	{
		File serverConnectionsFolder=serverSelectionPanel.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getServerConnectionsFolder();
		String serverConnectionName=serverNameTextField.getText();
		String serverHostName=serverHostTextField.getText();
		String serverPort=serverPortTextField.getText();
		String serverSID=serverSIDTextField.getText();
		String serverUserName=serverUserNameTextField.getText();
		String serverPasswordName=new String(serverPasswordField.getPassword());
		String serviceType = UtilsConstants.SID_DATABASE_SERVICE_TYPE;
		String serviceName = serviceNameTextField.getText();
		try{
			serviceType = serviceTypeComboBox.getSelectedItem().toString();
			if(!UtilsConstants.SID_DATABASE_SERVICE_TYPE.equalsIgnoreCase(serviceType) &&
			   !UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE.equalsIgnoreCase(serviceType)	){
				GUIUtils.popupErrorMessage("Please select DB Service type.");
				return;
			}else if(UtilsConstants.SID_DATABASE_SERVICE_TYPE.equalsIgnoreCase(serviceType)){
				if(serverSID.isEmpty() || serverSID.equals("") || serverSID==null){
					GUIUtils.popupErrorMessage("Please specify DB SID.");
					return;
				}
			}else if(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE.equalsIgnoreCase(serviceType)){
				if(serviceName.isEmpty() || serviceName.equals("") || serviceName==null){
					GUIUtils.popupErrorMessage("Please specify DB Service name.");
					return;
				}
			}
		}catch(Exception e){
			serviceType = UtilsConstants.SID_DATABASE_SERVICE_TYPE;
		}
		
		boolean isSuccess=createConnectionFile(serverConnectionsFolder,serverConnectionName,serverHostName,serverPort,serverSID,
				serverUserName,serverPasswordName,serviceType,serviceName);
		if (isSuccess) {
			serverSelectionPanel.refreshServerConnections(serverConnectionName);
			dialog.dispose();
		}
	}
	
	public static boolean createConnectionFile(File serverConnectionsFolder,String serverConnectionName,String serverHostName,String serverPort,String serverSID,
			String serverUserName,String serverPasswordName, String serviceType, String serviceName) 
	{
		// no APPS user allowed - validate and reject it instead use the script
		// save as a .se (snapshot environment) file in the default SE folder in options panel.
		// the name of the file is the "server name".ie (enforce no special characters not allowed in a file)
		// then refresh the combox list on the main panel.
		if (serverUserName.equalsIgnoreCase("APPS")) {
			GUIUtils.popupErrorMessage("You cannot use 'APPS' as the user name. Please ask your DBA to run the script in order to create a custom schema.");
			return false;
		}
		
		StringBuffer content=new StringBuffer("");
		content.append(CONNECTION_NAME_PROPERTY).append("=").append(serverConnectionName).append("\n");
		content.append(HOST_NAME_PROPERTY).append("=").append(serverHostName).append("\n");
		content.append(PORT_NUMBER_PROPERTY).append("=").append(serverPort).append("\n");
		content.append(SERVICE_TYPE_PROPERTY).append("=").append(serviceType).append("\n");
		if(serviceType.equals(UtilsConstants.SID_DATABASE_SERVICE_TYPE)){
			content.append(SID_PROPERTY).append("=").append(serverSID).append("\n");
		}else if(serviceType.equals(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE)){
			content.append(SERVICE_NAME_PROPERTY).append("=").append(serviceName).append("\n");
		}
		content.append(USER_NAME_PROPERTY).append("=").append(serverUserName).append("\n");
		content.append(PASSWORD_PROPERTY).append("=").append(serverPasswordName).append("\n");
		File seFile=new File(serverConnectionsFolder,serverConnectionName+SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION);
		try {
			InjectUtils.encryptToFile(content.toString(),seFile);
			return true;
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to create file: '"+seFile.getAbsolutePath()+"'. Error: "+e.getMessage());
		}
		return false;
	}
	
	private void load(String serverConnectionName) 
	{
		File serverConnectionsFolder=serverSelectionPanel.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().getServerConnectionsFolder();
		File seFile=new File(serverConnectionsFolder,serverConnectionName+SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION);
		Map<String,String> snapshotEnvironmentProperties=UIUtils.readSnapshotEnvironmentProperties(seFile);

		String value=snapshotEnvironmentProperties.get(CONNECTION_NAME_PROPERTY);
		if (value!=null) {
			serverNameTextField.setText(value);
		}
		value=snapshotEnvironmentProperties.get(HOST_NAME_PROPERTY);
		if (value!=null) {
			serverHostTextField.setText(value);
		}
		value=snapshotEnvironmentProperties.get(PORT_NUMBER_PROPERTY);
		if (value!=null) {
			serverPortTextField.setText(value);
		}
		value=snapshotEnvironmentProperties.get(SID_PROPERTY);
		if (value!=null) {
			serverSIDTextField.setText(value);
		}
		value=snapshotEnvironmentProperties.get(USER_NAME_PROPERTY);
		if (value!=null) {
			serverUserNameTextField.setText(value);
		}
		value=snapshotEnvironmentProperties.get(PASSWORD_PROPERTY);
		if (value!=null) {
			serverPasswordField.setText(value);
		}
		value =snapshotEnvironmentProperties.get(SERVICE_NAME_PROPERTY);
		if (value!=null) {
			serviceNameTextField.setText(value);
		}
		value =snapshotEnvironmentProperties.get(SERVICE_TYPE_PROPERTY);
		if (value!=null) {
			if(value.equalsIgnoreCase(UtilsConstants.SID_DATABASE_SERVICE_TYPE)){
				serviceTypeComboBox.setSelectedItem(UtilsConstants.SID_DATABASE_SERVICE_TYPE);
			}else if(value.equalsIgnoreCase(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE)){
				serviceTypeComboBox.setSelectedItem(UtilsConstants.SERVICE_NAME_DATABASE_SERVICE_TYPE);
			}
		}else{
			serviceTypeComboBox.setSelectedItem(UtilsConstants.SID_DATABASE_SERVICE_TYPE);
		}
		
	}

}
