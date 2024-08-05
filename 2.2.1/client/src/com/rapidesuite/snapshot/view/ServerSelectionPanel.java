package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.SnapshotController;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotDropSwingWorker;
import com.rapidesuite.snapshot.model.UserInformation;

@SuppressWarnings("serial")
public class ServerSelectionPanel extends JPanel{

	private TabSnapshotsPanel tabSnapshotsPanel;
	private JLabel serverNameLabel;
	private JComboBox<String> serverSelectionComboBox;
	private ComboboxToolTipRenderer serverSelectionRenderer;
	ArrayList<String> serverSelectionToolTips;
	private JLabel connectionStatusLabel;
	private JLabel connectionStatusValueLabel;
	private JLabel plsqlPackageVersionLabel;
	private JLabel plsqlPackageVersionValueLabel;
	private JLabel serverVersionLabel;
	private JLabel serverVersionStatusValueLabel;
	private JButton serverAddButton;
	private JButton serverEditButton;
	private JButton serverDeleteButton;
	private JButton serverConnectButton;
	private JButton schemaResetButton;
	private String currentConnectionOracleRelease;
	private Map<Integer, String> oracleSeededUserIdToUserNameMap;
	
	private boolean isConnected;
	private ImageIcon connectImageIcon;
	private ImageIcon disconnectImageIcon;
	private ImageIcon connectImageIconRollOver;
	private ImageIcon disconnectImageIconRollOver;
	private UserInformation selectedUser;
	private boolean isConnectedAndInstalledPLSQLCompletely;

	public ServerSelectionPanel(TabSnapshotsPanel tabSnapshotsPanel) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BorderLayout());
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(20,100, 20, 10));
		createComponents();
	}

	private void createComponents() {
		ImageIcon ii=null;
		URL iconURL =null;
		
		JPanel serverSelectionPanel=new JPanel();
		serverSelectionPanel.setOpaque(true);
		serverSelectionPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		serverSelectionPanel.setBackground(Color.decode("#047FC0"));
		serverSelectionPanel.setLayout(new BoxLayout(serverSelectionPanel, BoxLayout.X_AXIS));
		add(serverSelectionPanel,BorderLayout.WEST);
		
		JPanel tempSub1Panel=new JPanel();
		tempSub1Panel.setOpaque(false);
		tempSub1Panel.setLayout(new BoxLayout(tempSub1Panel, BoxLayout.Y_AXIS));
		JPanel tempSub2Panel=new JPanel();
		tempSub2Panel.setOpaque(false);
		tempSub2Panel.setLayout(new BoxLayout(tempSub2Panel, BoxLayout.Y_AXIS));
		JPanel tempSub3Panel=new JPanel();
		tempSub3Panel.setOpaque(false);
		tempSub3Panel.setLayout(new BoxLayout(tempSub3Panel, BoxLayout.Y_AXIS));
		serverSelectionPanel.add(tempSub1Panel);
		serverSelectionPanel.add(tempSub2Panel);
		serverSelectionPanel.add(tempSub3Panel);
		
		int labelWidth=100;
		int labelValueWidth=225;
		int labelHeight=20;
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempSub1Panel.add(tempPanel);
		
		serverNameLabel=new JLabel("Connection:");
		serverNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		UIUtils.setDimension(serverNameLabel,110,labelHeight);
		InjectUtils.assignArialPlainFont(serverNameLabel,InjectMain.FONT_SIZE_NORMAL);
		serverNameLabel.setForeground(Color.white);
		tempPanel.add(serverNameLabel);
				
		serverSelectionComboBox=new JComboBox<String>();
		serverSelectionRenderer = new ComboboxToolTipRenderer();
		serverSelectionComboBox.setRenderer(serverSelectionRenderer);
		serverSelectionToolTips = new ArrayList<String>();
		tempPanel.add(serverSelectionComboBox);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tempSub1Panel.add(tempPanel);
		connectionStatusLabel=new JLabel("Status:");
		UIUtils.setDimension(connectionStatusLabel,labelWidth,labelHeight);
		InjectUtils.assignArialPlainFont(connectionStatusLabel,InjectMain.FONT_SIZE_NORMAL);
		connectionStatusLabel.setForeground(Color.white);
		tempPanel.add(connectionStatusLabel);
		connectionStatusValueLabel=new JLabel();
		connectionStatusValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		UIUtils.setDimension(connectionStatusValueLabel,labelValueWidth,labelHeight);
		InjectUtils.assignArialPlainFont(connectionStatusValueLabel,InjectMain.FONT_SIZE_NORMAL);
		connectionStatusValueLabel.setOpaque(true);
		connectionStatusValueLabel.setBackground(Color.white);
		tempPanel.add(connectionStatusValueLabel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tempSub1Panel.add(tempPanel);
		serverVersionLabel=new JLabel("Server version:");
		UIUtils.setDimension(serverVersionLabel,labelWidth,labelHeight);
		InjectUtils.assignArialPlainFont(serverVersionLabel,InjectMain.FONT_SIZE_NORMAL);
		serverVersionLabel.setForeground(Color.white);
		tempPanel.add(serverVersionLabel);
		serverVersionStatusValueLabel=new JLabel("");
		serverVersionStatusValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		UIUtils.setDimension(serverVersionStatusValueLabel,labelValueWidth,labelHeight);
		InjectUtils.assignArialPlainFont(serverVersionStatusValueLabel,InjectMain.FONT_SIZE_NORMAL);
		serverVersionStatusValueLabel.setOpaque(true);
		serverVersionStatusValueLabel.setBackground(Color.white);
		tempPanel.add(serverVersionStatusValueLabel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tempSub1Panel.add(tempPanel);
		plsqlPackageVersionLabel=new JLabel("PL/SQL version:");
		UIUtils.setDimension(plsqlPackageVersionLabel,labelWidth,labelHeight);
		InjectUtils.assignArialPlainFont(plsqlPackageVersionLabel,InjectMain.FONT_SIZE_NORMAL);
		plsqlPackageVersionLabel.setForeground(Color.white);
		tempPanel.add(plsqlPackageVersionLabel);
		plsqlPackageVersionValueLabel=new JLabel("");
		plsqlPackageVersionValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		UIUtils.setDimension(plsqlPackageVersionValueLabel,labelValueWidth,labelHeight);
		InjectUtils.assignArialPlainFont(plsqlPackageVersionValueLabel,InjectMain.FONT_SIZE_NORMAL);
		plsqlPackageVersionValueLabel.setOpaque(true);
		plsqlPackageVersionValueLabel.setBackground(Color.white);
		tempPanel.add(plsqlPackageVersionValueLabel);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_add.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		ii=new ImageIcon(ii.getImage().getScaledInstance(45,22,java.awt.Image.SCALE_SMOOTH));
		
		serverAddButton = new JButton();
		
		serverAddButton.setIcon(ii);
		serverAddButton.setSize(45,22);
		serverAddButton.setBorderPainted(false);
		serverAddButton.setContentAreaFilled(false);
		serverAddButton.setFocusPainted(false);
		serverAddButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_add_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		ii=new ImageIcon(ii.getImage().getScaledInstance(45,22,java.awt.Image.SCALE_SMOOTH));
		serverAddButton.setRolloverIcon(new RolloverIcon(ii));
		serverAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionServerAdd();
			}
		}
				);
		tempSub2Panel.add(serverAddButton);
		tempSub2Panel.add(Box.createRigidArea(new Dimension(5, 5)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_edit.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		
		serverEditButton = new JButton();
		
		serverEditButton.setIcon(ii);
		serverEditButton.setSize(45,22);
		serverEditButton.setBorderPainted(false);
		serverEditButton.setContentAreaFilled(false);
		serverEditButton.setFocusPainted(false);
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_edit_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		serverEditButton.setRolloverIcon(new RolloverIcon(ii));
		serverEditButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionServerEdit();
			}
		}
				);
		tempSub2Panel.add(serverEditButton);
		tempSub2Panel.add(Box.createRigidArea(new Dimension(5, 5)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_delete.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		
		serverDeleteButton = new JButton();
		
		serverDeleteButton.setIcon(ii);
		serverDeleteButton.setSize(52,22);
		serverDeleteButton.setBorderPainted(false);
		serverDeleteButton.setContentAreaFilled(false);
		serverDeleteButton.setFocusPainted(false);
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_delete_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		serverDeleteButton.setRolloverIcon(new RolloverIcon(ii));
		serverDeleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionServerDelete();
			}
		}
				);
		tempSub2Panel.add(serverDeleteButton);		
		tempSub2Panel.add(Box.createRigidArea(new Dimension(5, 5)));

		iconURL = this.getClass().getResource("/images/snapshot/button_connection_connect.png");
		try{ connectImageIcon=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_connect_rollover.png");
		try{ connectImageIconRollOver=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_disconnect.png");
		try{ disconnectImageIcon=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_disconnect_rollover.png");
		try{ disconnectImageIconRollOver=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		
		serverConnectButton = new JButton();
		
		serverConnectButton.setIcon(connectImageIcon);
		serverConnectButton.setBorderPainted(false);
		serverConnectButton.setContentAreaFilled(false);
		serverConnectButton.setFocusPainted(false);
		serverConnectButton.setEnabled(false);
		serverConnectButton.setRolloverIcon(new RolloverIcon(connectImageIconRollOver));
		serverConnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					processActionServerConnect();
				}
				catch(Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		}
				);
		tempSub3Panel.add(serverConnectButton);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_reset_connection.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		schemaResetButton = new JButton();
		schemaResetButton.setEnabled(false);
		schemaResetButton.setIcon(ii);
		schemaResetButton.setBorderPainted(false);
		schemaResetButton.setContentAreaFilled(false);
		schemaResetButton.setFocusPainted(false);
		schemaResetButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_reset_connection_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		schemaResetButton.setRolloverIcon(new RolloverIcon(ii));
		schemaResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					processActionResetConnection();
				}
				catch(Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}
		}
				);
		tempSub3Panel.add(schemaResetButton);
		
	}
	
	protected void processActionResetConnection() throws Exception {
		SnapshotsGridPanel.validateDeletePermission(tabSnapshotsPanel,
				tabSnapshotsPanel.getSnapshotsGridPanel().getSnapshotGridRecords());
		
		int response = JOptionPane.showConfirmDialog(null, 
				"<html><body>'Reset Connection' will reset the current schema connection to an initial clean state.<br/>This operation is unreversible - all your Snapshots will be lost.<br/><br/>"+
						"<b>Are you sure to continue with the reset?</b>", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			SnapshotDropSwingWorker swingWorker=new SnapshotDropSwingWorker(tabSnapshotsPanel);
			final int width=450;
			final int height=150;
			UIUtils.displayOperationInProgressModalWindow(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),width,height,"Deleting...",swingWorker,SnapshotMain.getSharedApplicationIconPath());
		}		
	}

	protected void processActionServerDelete() {
		File serverConnectionsFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getServerConnectionsFolder();
		File seFile=new File(serverConnectionsFolder,(String)serverSelectionComboBox.getSelectedItem()+SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION);

		int response = JOptionPane.showConfirmDialog(null, "The file "+seFile.getAbsolutePath()+" will be deleted. Please confirm.", "Confirm",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			seFile.delete();
			refreshServerConnections(null);
		}
	}
	
	protected void processActionServerEdit() {
		new ServerFrame(this,(String)serverSelectionComboBox.getSelectedItem());
	}
	
/*	public void userLogin() throws ClassNotFoundException, SQLException {
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		List<UserInformation> userInformationList=ModelUtils.getSnapshotUserInformationList(snapshotEnvironmentProperties);
		boolean isRequireUserLogin = true;
		if(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getCachedUser()!=null){
			try{
				if(isCachedUserInUsersTable(userInformationList,tabSnapshotsPanel.getMainPanel().getSnapshotMain().getCachedUser())){
					isRequireUserLogin = false;
				}
			}catch(Exception e){
				FileUtils.printStackTrace(e);
				isRequireUserLogin = true;
			}
		}
		if(isRequireUserLogin){
			if (!userInformationList.isEmpty()) {
				GenericLoginPanel genericLoginPanel=UserManagementPanel.showUserLoginDialog(tabSnapshotsPanel,userInformationList,"User Login");
				if (genericLoginPanel.isSuccess()) {
					selectedUser=genericLoginPanel.getSelectedUser();
					tabSnapshotsPanel.getSnapshotsGridPanel().getLoginAsLabel().setText("Logged in as: "+selectedUser.getFullName());
				}
			}
		}
	}*/
	
	public void userLogin() throws ClassNotFoundException, SQLException {
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		List<UserInformation> userInformationList=ModelUtils.getSnapshotUserInformationList(snapshotEnvironmentProperties);
		if (!userInformationList.isEmpty()) {
			GenericLoginPanel genericLoginPanel=UserManagementPanel.showUserLoginDialog(tabSnapshotsPanel,userInformationList,"User Login");
			if (genericLoginPanel.isSuccess()) {
				selectedUser=genericLoginPanel.getSelectedUser();
				tabSnapshotsPanel.getSnapshotsGridPanel().getLoginAsLabel().setText("Logged in as: "+selectedUser.getFullName());
			}
		}
	}
	
/*	public boolean isCachedUserInUsersTable(List<UserInformation> userInformationList, String cachedUser) throws Exception{
		try{
			boolean toReturn = false;
			FileUtils.println("OS USER NAME : "+cachedUser);
			for(UserInformation user : userInformationList){
				if(cachedUser.equalsIgnoreCase(user.getLoginName())){
					if(user.getOsAuthLoginDate() != null && user.getLastPasswordUpdateDate() != null){
						if(user.getOsAuthLoginDate().after(user.getLastPasswordUpdateDate())){
							setSelectedUser(user);
							tabSnapshotsPanel.getSnapshotsGridPanel().getLoginAsLabel().setText("Logged in as: "+selectedUser.getFullName());
							toReturn = true;
						}
					}
				}
			}
			return toReturn;
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Cannot check if OS user '"+cachedUser+"' exists in RAPIDSnapshot database.");
		}
	}*/
	public void processActionServerConnect() throws ClassNotFoundException, SQLException {
		oracleSeededUserIdToUserNameMap=new TreeMap<Integer,String>();
		// to disconnect
		if (isConnected) {
			isConnected=false;
			selectedUser=null;
			serverConnectButton.setIcon(connectImageIcon);
			serverConnectButton.setRolloverIcon(new RolloverIcon(connectImageIconRollOver));
			connectionStatusValueLabel.setText("");
			plsqlPackageVersionValueLabel.setText("");
			serverVersionStatusValueLabel.setText("");
			serverAddButton.setEnabled(true);
			serverEditButton.setEnabled(true);
			serverDeleteButton.setEnabled(true);
			serverSelectionComboBox.setEnabled(true);
			schemaResetButton.setEnabled(false);
			tabSnapshotsPanel.getSnapshotsActionsPanel().getSnapshotButton().setEnabled(false);
			tabSnapshotsPanel.getSnapshotsActionsPanel().getDeleteButton().setEnabled(false);
			tabSnapshotsPanel.getSnapshotsActionsPanel().getImportButton().setEnabled(false);
			tabSnapshotsPanel.getSnapshotsActionsPanel().getExportButton().setEnabled(false);
			tabSnapshotsPanel.getSnapshotsActionsPanel().getComparisonButton().setEnabled(false);
			tabSnapshotsPanel.getSnapshotsActionsPanel().getSnapshotForUpgradeButton().setEnabled(false);
			tabSnapshotsPanel.getSnapshotsGridPanel().resetGrid();
			tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().setEnabledOnComponentsToConnectFirst(true);
			tabSnapshotsPanel.getSnapshotsGridPanel().resetLabels();
			setInventoryMgtMenuItemEnabled(false);
			return;
		}
		
		String serverConnectionName=(String)serverSelectionComboBox.getSelectedItem();
		if (serverConnectionName==null || serverConnectionName.isEmpty()) {
			GUIUtils.popupErrorMessage("Please select a server first!");
			return;
		}
		connectionStatusValueLabel.setText("");
		
		SnapshotController snapshotController=new SnapshotController(tabSnapshotsPanel,true);
		snapshotController.startExecution();
		//waitUntilPostExecutionCompleteAndCacheUser(snapshotController);
		
	}
	public void postConnection() {
		isConnected=true;
		serverConnectButton.setIcon(disconnectImageIcon);
		serverConnectButton.setRolloverIcon(new RolloverIcon(disconnectImageIconRollOver));
		serverAddButton.setEnabled(false);
		serverEditButton.setEnabled(false);
		serverDeleteButton.setEnabled(false);
		serverSelectionComboBox.setEnabled(false);
		schemaResetButton.setEnabled(true);
		if (currentConnectionOracleRelease!=null) {
			tabSnapshotsPanel.getSnapshotsActionsPanel().unlockPanel();
			tabSnapshotsPanel.getSnapshotsActionsPanel().getDeleteButton().setEnabled(false);
		}
		tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().setEnabledOnComponentsToConnectFirst(false);
		setInventoryMgtMenuItemEnabled(true);
		UIUtils.showBalloon(tabSnapshotsPanel.getSnapshotsActionsPanel().getSnapshotButton(),
				"<html>After connection, you can take a snapshot<br/>of your Configurations by clicking here.", 
				tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().isShowBalloons());
	}
	public void waitUntilPostExecutionCompleteAndCacheUser(SnapshotController snapshotController){
		while(!snapshotController.isExecutionCompleted()){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		checkIfCacheUserOrByPass();
	}
	public void checkIfCacheUserOrByPass(){
		try{
			//if cached user = os username
			authenticateOsUser();
		}catch(Exception e){
			FileUtils.printStackTrace(e);
		}
	}
	public UserInformation getOSUserNameInformationIfExist() throws Exception{
		try{
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
			List<UserInformation> userInformationList=ModelUtils.getSnapshotUserInformationList(snapshotEnvironmentProperties);
			String osUserName = System.getProperty("user.name");
			UserInformation toReturn = null;
			if (!userInformationList.isEmpty()){
				for(UserInformation user : userInformationList){
					if(osUserName.equalsIgnoreCase(user.getLoginName())){
						toReturn = user;
					}
				}	
			}
			return toReturn;
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Cannot get OS Username information, error : "+e.getMessage());
		}
		
	}
	public void authenticateOsUser() throws Exception {
		//do we need to check if CachedUser is null? which mean this is the first connect
		try{
			UserInformation userInformation = getOSUserNameInformationIfExist();
			if(userInformation!=null){
				FileUtils.println("OS user exists in table : "+userInformation.getLoginName());
				ModelUtils.updateUserOsAuthLogInDate(ModelUtils.getConnection(tabSnapshotsPanel),userInformation,true);
				
				userLogin();
				if(userInformation.getLoginName().equalsIgnoreCase(selectedUser.getLoginName())){
					ModelUtils.updateUserOsAuthLogInDate(ModelUtils.getConnection(tabSnapshotsPanel),selectedUser,false);
					tabSnapshotsPanel.getMainPanel().getSnapshotMain().setCachedUser(selectedUser.getLoginName());
				}else{
					FileUtils.println("Logged in but not with the OS user : "+selectedUser.getLoginName());
				}					
			}
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Cannot check if an OS username exists in USERS table, error : "+e.getMessage());
		}
	}
	
	protected void processActionServerAdd() {
		new ServerFrame(this,null);
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public void refreshServerConnections(String defaultConnectionName) {
		File serverConnectionsFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getServerConnectionsFolder();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION.toLowerCase());
			}
		};
		File[] listOfFiles = serverConnectionsFolder.listFiles(filter);
		serverSelectionComboBox.removeAllItems();
		if (listOfFiles!=null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				File file = listOfFiles[i];
				//String serverConnectionName=file.getName().toLowerCase().replace(SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION.toLowerCase(),"");
				String serverConnectionName=file.getName().replace(SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION.toLowerCase(),"");
				serverSelectionComboBox.addItem(serverConnectionName);
				serverSelectionToolTips.add(serverConnectionName);
			}
			serverSelectionRenderer.setTooltips(serverSelectionToolTips);
		}
		if (listOfFiles!=null && listOfFiles.length>0) {
			serverConnectButton.setEnabled(true);
			serverEditButton.setEnabled(true);
			serverDeleteButton.setEnabled(true);
			if (defaultConnectionName!=null) {
				serverSelectionComboBox.setSelectedItem(defaultConnectionName.toLowerCase());
			}
		}
		else {
			serverConnectButton.setEnabled(false);
			serverEditButton.setEnabled(false);
			serverDeleteButton.setEnabled(false);
		}
	}

	public JComboBox<String> getServerSelectionComboBox() {
		return serverSelectionComboBox;
	}

	public JLabel getConnectionStatusValueLabel() {
		return connectionStatusValueLabel;
	}
	
	public JLabel getPLSQLPackageVersionValueLabel() {
		return plsqlPackageVersionValueLabel;
	}
	
	public JLabel getServerVersionStatusValueLabel() {
		return serverVersionStatusValueLabel;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public String getSelectedServerConnection() {
		return (String)serverSelectionComboBox.getSelectedItem();
	}

	public String getCurrentConnectionOracleRelease() {
		return currentConnectionOracleRelease;
	}

	public void setCurrentConnectionOracleRelease(String currentConnectionOracleRelease) {
		this.currentConnectionOracleRelease = currentConnectionOracleRelease;
	}

	public void lockPanel() {
		setComponentsEnabled(false);
	}
	
	public void unlockPanel() {
		serverConnectButton.setEnabled(true);
		setInventoryMgtMenuItemEnabled(true);
	}
	
	private void setComponentsEnabled(boolean isEnabled) {
		serverConnectButton.setEnabled(isEnabled);
		serverEditButton.setEnabled(isEnabled);
		serverDeleteButton.setEnabled(isEnabled);
		serverAddButton.setEnabled(isEnabled);
		serverSelectionComboBox.setEnabled(isEnabled);
		schemaResetButton.setEnabled(isEnabled);
		setInventoryMgtMenuItemEnabled(isEnabled);
	}
	
	public Map<Integer, String> getOracleSeededUserIdToUserNameMap() {
		return oracleSeededUserIdToUserNameMap;
	}
	
	public void setOracleSeededUserIdToUserNameMap(Map<Integer, String> oracleSeededUserIdToUserNameMap) {
		this.oracleSeededUserIdToUserNameMap = oracleSeededUserIdToUserNameMap;
	}

	public UserInformation getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(UserInformation selectedUser) {
		this.selectedUser = selectedUser;
	}

	public boolean hasUserWritePermission() throws ClassNotFoundException, SQLException {
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		List<UserInformation> userInformationList=ModelUtils.getSnapshotUserInformationList(snapshotEnvironmentProperties);
		if (userInformationList.isEmpty()) {
			return true;
		}
		else {
			if (selectedUser ==null ) {
				userLogin();
			}
			if (selectedUser ==null) { 
				return false;
			}
			else {
				return selectedUser.hasSnapshotCreationPermission();
			}
		}
	}
	
	public boolean hasUserReadPermission() throws ClassNotFoundException, SQLException {
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		List<UserInformation> userInformationList=ModelUtils.getSnapshotUserInformationList(snapshotEnvironmentProperties);
		if (userInformationList.isEmpty()) {
			return true;
		}
		else {
			if (selectedUser ==null ) {
				userLogin();
			}
			if (selectedUser ==null) { 
				return false;
			}
			else {
				return selectedUser.isEnabled();
			}
		}
	}
	public boolean isConnectedAndInstalledPLSQLCompletely() {
		return isConnectedAndInstalledPLSQLCompletely;
	}

	public void setConnectedAndInstalledPLSQLCompletely(boolean isConnectedAndInstalledPLSQLCompletely) {
		this.isConnectedAndInstalledPLSQLCompletely = isConnectedAndInstalledPLSQLCompletely;
	}	
	public boolean hasUserManagerRole() throws ClassNotFoundException, SQLException {
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		List<UserInformation> userInformationList=ModelUtils.getSnapshotUserInformationList(snapshotEnvironmentProperties);
		if (userInformationList.isEmpty()) {
			return true;
		}
		else {
			if (selectedUser ==null ) {
				userLogin();
			}
			if (selectedUser ==null) { 
				return false;
			}
			else {
				return selectedUser.isUserManager();
			}
		}
	}	
	public void setInventoryMgtMenuItemEnabled(boolean isEnabled){
		tabSnapshotsPanel.getMainPanel().getInventoryMgtMenuItem().setEnabled(isEnabled);
		if(isEnabled){
			tabSnapshotsPanel.getMainPanel().getInventoryMgtMenuItem().setBackground(Color.decode("#343836"));
			tabSnapshotsPanel.getMainPanel().getInventoryMgtMenuItem().setForeground(Color.decode("#FFFFFF") );			
		}else{
			tabSnapshotsPanel.getMainPanel().getInventoryMgtMenuItem().setForeground(Color.decode("#AAAAAA") );
			tabSnapshotsPanel.getMainPanel().getInventoryMgtMenuItem().setBackground(Color.decode("#909090"));
		}		
	}	
}
