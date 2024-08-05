package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

import org.apache.commons.io.IOUtils;

import com.rapid4Cloud.snapshotLogFile.StatusType;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.Encryption;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.UserInformation;

@SuppressWarnings("serial")
public class UserManagementPanel extends JPanel{

	private static final String NEW_USER = "New User";
	private JList<String> userNamesList;
	private DefaultListModel<String> listModel;
	private JTextField loginNameTextField;
	private JTextField fullNameTextField;
	private JCheckBox isUserManagerCheckBox;
	private JCheckBox isEnabledCheckBox;
	private JCheckBox hasSnapshotCreationPermissionCheckBox;
	private JPasswordField userPasswordTextField;
	private JPasswordField userPasswordVerifyTextField;
	private JCheckBox isUsingForAutomationSnapshotCheckBox;	
	
	private JCheckBox isEnableDefaultParallelWorkersFieldCheckBox;
	private JCheckBox isEnableServerConnectionsDefaultFolderButtonCheckBox;
	private JCheckBox isEnableDownloadFolderButtonCheckBox;
	private JCheckBox isEnableTemplateFolderButtonCheckBox;	
	private JCheckBox isEnableDefaultSnapshotNamePrefixFieldCheckBox;	
	private JCheckBox isEnableResetOptionButtonCheckBox;	
	private JCheckBox isEnableDeleteOptionCheckBox;
	private JCheckBox isEnableDisplayTotalDetailCheckBox;
	private final boolean DEFAULT_VALUE_FOR_ENABLE_OPTION_PANEL = false;

	private JButton resetButton;
	private JButton saveButton;
	private JButton deleteButton;
	private JDialog dialog;
	
	private TabSnapshotsPanel tabSnapshotsPanel;
	private List<UserInformation> snapshotUserInformationList;
	private JButton closeButton;
	private String selectedLoginName;
	private boolean isManagerLoggedIn;

	public UserManagementPanel(TabSnapshotsPanel tabSnapshotsPanel) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		createComponents();
		(new UpdateRunner()).execute();
	}

	public void setDialog(JDialog dialog) {
		this.dialog=dialog;
		dialog.addWindowListener (new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				close();
			}
		});
	}

	protected void close() {
		dialog.dispose();
	}

	@SuppressWarnings({ "rawtypes" })
	private void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
		add(northPanel,BorderLayout.NORTH);
		
		JLabel label=new JLabel("<html><body>This purpose of this window is to manage Users in the case where"+
		" you want to share one schema among many consultants.<br/>All users can view, compare or download snapshots"+
		" at the same time but ONLY ONE user can create snapshots.");
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		label.setForeground(Color.white);
		label.setBackground(Color.decode("#047fc0"));
		InjectUtils.assignArialPlainFont(label,12);
		label.setOpaque(true);
		northPanel.add(label);
		
		final JPanel mainPanel=new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		add(mainPanel,BorderLayout.CENTER);

		JPanel leftPanel=new JPanel();
		leftPanel.setOpaque(false);
		leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 20));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		mainPanel.add(leftPanel);

		listModel = new DefaultListModel<String>(); 
		userNamesList = new JList<String>(listModel);
		userNamesList.setSelectionBackground(Color.decode("#047fc0"));
		userNamesList.setCellRenderer(new DefaultListCellRenderer  (){
			
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component component = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
				component.setFont(component.getFont().deriveFont(Font.PLAIN));
				component.setForeground(Color.black);
				
		        if (index % 2 == 0) {
		        	component.setBackground(Color.white);
		        }
		        else {
		        	component.setBackground(Color.decode("#efefef"));
		        }
		        if (isSelected) {
		        	component.setBackground(Color.decode("#047fc0"));
		        }
		        
		        if (index!=0) {
		        	UserInformation userInformation=snapshotUserInformationList.get(index-1);
		        	if (userInformation.isUserManager()) {
		        		//component.setFont(component.getFont().deriveFont(Font.BOLD));	
					}
		        	if (!userInformation.isEnabled()) {
		        		component.setBackground(new Color(255,176,176));
					}
		        	else {
		        		component.setBackground(new Color(0,242,121));
		        	}
		        }		      	
				
		        return component;
		    }
		}
		);
		userNamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userNamesList.setVisibleRowCount(20);
		userNamesList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
                	int selectedIndex=userNamesList.getSelectedIndex();
                	if (selectedIndex==-1) {
                		selectedIndex=0;
                	}
                	editUser(selectedIndex);
                }
            }
        });
		
		JScrollPane scrollPane = new JScrollPane(userNamesList);
		Dimension d = userNamesList.getPreferredSize();
		d.width = 160;
		scrollPane.setPreferredSize(d);
		label=new JLabel("Users List:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		leftPanel.add(label);
		leftPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		leftPanel.add(scrollPane);

		JPanel rightPanel=new JPanel();
		rightPanel.setOpaque(false);
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		mainPanel.add(rightPanel);
		
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		
		int labelsWidth=180;
		int fieldsWidth=200;
		int labelsHeight=30;
		int fieldsHeight=22;
		int panelSpacing=5;
		ImageIcon ii=null;
		URL iconURL =null;
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("Login name:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,labelsHeight);
		tempPanel.add(label);
		loginNameTextField=new JTextField();
		UIUtils.setDimension(loginNameTextField,fieldsWidth,fieldsHeight);
		tempPanel.add(loginNameTextField);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("Full name:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,labelsHeight);
		tempPanel.add(label);
		fullNameTextField=new JTextField();
		UIUtils.setDimension(fullNameTextField,fieldsWidth,fieldsHeight);
		tempPanel.add(fullNameTextField);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("Password:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,labelsHeight);
		tempPanel.add(label);
		userPasswordTextField=new JPasswordField();
		UIUtils.setDimension(userPasswordTextField,fieldsWidth,fieldsHeight);
		tempPanel.add(userPasswordTextField);
		tempPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_reset.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		resetButton = new JButton();
		resetButton.setIcon(ii);
		resetButton.setBorderPainted(false);
		resetButton.setContentAreaFilled(false);
		resetButton.setFocusPainted(false);
		resetButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_reset_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		resetButton.setRolloverIcon(new RolloverIcon(ii));
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					processActionResetPassword();
				}
				catch(Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}

		});
		tempPanel.add(resetButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
		label=new JLabel("Confirm password:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(label,labelsWidth,labelsHeight);
		tempPanel.add(label);
		userPasswordVerifyTextField=new JPasswordField();
		UIUtils.setDimension(userPasswordVerifyTextField,fieldsWidth,fieldsHeight);
		tempPanel.add(userPasswordVerifyTextField);
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));
		
		JTabbedPane jtp = new JTabbedPane();
		rightPanel.add(jtp);
        JPanel generalPanel = new JPanel();
        JPanel permissionPanel = new JPanel();
        generalPanel.setOpaque(false);
        generalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));
        permissionPanel.setOpaque(false);
        permissionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        permissionPanel.setLayout(new BoxLayout(permissionPanel, BoxLayout.Y_AXIS));
        
        jtp.addTab("General", generalPanel);
        jtp.addTab("Options Permissions", permissionPanel);
        
        
        
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		generalPanel.add(tempPanel);
		isEnabledCheckBox=new JCheckBox("Enabled");
		isEnabledCheckBox.setOpaque(false);
		isEnabledCheckBox.setEnabled(true);
		InjectUtils.assignArialPlainFont(isEnabledCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isEnabledCheckBox.setForeground(Color.decode("#343836"));
		tempPanel.add(isEnabledCheckBox);
		generalPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		generalPanel.add(tempPanel);
		isUserManagerCheckBox=new JCheckBox("Manager");
		isUserManagerCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isUserManagerCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isUserManagerCheckBox.setForeground(Color.decode("#343836"));
		tempPanel.add(isUserManagerCheckBox);
		generalPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));
		isUserManagerCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isUserManagerCheckBox.isSelected()) {
					int selectedIndex=userNamesList.getSelectedIndex();
					if (selectedIndex==-1 || selectedIndex==0) {
						return;
					}
					// the manager cannot unset its own manager property
					UserInformation userInformation=snapshotUserInformationList.get(selectedIndex-1);
					if (userInformation.isUserManager()) {
						isUserManagerCheckBox.setSelected(true);
						GUIUtils.popupInformationMessage(dialog,"The manager cannot unset its managerial permission!");		
						return;
					}
				}
			}

		});        
        
        
        
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		generalPanel.add(tempPanel);
		hasSnapshotCreationPermissionCheckBox=new JCheckBox("Snapshot Creation Permission");
		hasSnapshotCreationPermissionCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(hasSnapshotCreationPermissionCheckBox,InjectMain.FONT_SIZE_NORMAL);
		hasSnapshotCreationPermissionCheckBox.setForeground(Color.decode("#343836"));
		tempPanel.add(hasSnapshotCreationPermissionCheckBox);
		generalPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		generalPanel.add(tempPanel);
		isUsingForAutomationSnapshotCheckBox=new JCheckBox("Use for Automation");
		isUsingForAutomationSnapshotCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isUsingForAutomationSnapshotCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isUsingForAutomationSnapshotCheckBox.setForeground(Color.decode("#343836"));
		tempPanel.add(isUsingForAutomationSnapshotCheckBox);	
		generalPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));
		
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new GridLayout(4, 2));
		permissionPanel.add(tempPanel);
		isEnableDefaultParallelWorkersFieldCheckBox=new JCheckBox("Enable 'Default Parallel Workers' option");
		isEnableDefaultParallelWorkersFieldCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isEnableDefaultParallelWorkersFieldCheckBox,InjectMain.FONT_SIZE_NORMAL);	
		isEnableDefaultParallelWorkersFieldCheckBox.setForeground(Color.decode("#343836"));
		tempPanel.add(isEnableDefaultParallelWorkersFieldCheckBox);
		isEnableServerConnectionsDefaultFolderButtonCheckBox=new JCheckBox("Enable 'Server Connections Default Folder' option");
		isEnableServerConnectionsDefaultFolderButtonCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isEnableServerConnectionsDefaultFolderButtonCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isEnableServerConnectionsDefaultFolderButtonCheckBox.setForeground(Color.decode("#343836"));
		tempPanel.add(isEnableServerConnectionsDefaultFolderButtonCheckBox);
		permissionPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));	
		
	
		isEnableDownloadFolderButtonCheckBox=new JCheckBox("Enable 'Download Folder' option");
		isEnableDownloadFolderButtonCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isEnableDownloadFolderButtonCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isEnableDownloadFolderButtonCheckBox.setForeground(Color.decode("#343836"));
		tempPanel.add(isEnableDownloadFolderButtonCheckBox);
		isEnableTemplateFolderButtonCheckBox=new JCheckBox("Enable 'Template Folder' option");
		isEnableTemplateFolderButtonCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isEnableTemplateFolderButtonCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isEnableTemplateFolderButtonCheckBox.setForeground(Color.decode("#343836"));
		tempPanel.add(isEnableTemplateFolderButtonCheckBox);

		isEnableDefaultSnapshotNamePrefixFieldCheckBox=new JCheckBox("Enable 'Default Snapshot name prefix' option");
		isEnableDefaultSnapshotNamePrefixFieldCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isEnableDefaultSnapshotNamePrefixFieldCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isEnableDefaultSnapshotNamePrefixFieldCheckBox.setForeground(Color.decode("#343836"));
		tempPanel.add(isEnableDefaultSnapshotNamePrefixFieldCheckBox);
		isEnableResetOptionButtonCheckBox=new JCheckBox("Enable 'Reset to Default' option");
		isEnableResetOptionButtonCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isEnableResetOptionButtonCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isEnableResetOptionButtonCheckBox.setForeground(Color.decode("#343836"));	
		tempPanel.add(isEnableResetOptionButtonCheckBox);
		permissionPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));	
		
		isEnableDeleteOptionCheckBox=new JCheckBox("Enable 'Snapshot delete' option");
		isEnableDeleteOptionCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isEnableDeleteOptionCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isEnableDeleteOptionCheckBox.setForeground(Color.decode("#343836"));	
		tempPanel.add(isEnableDeleteOptionCheckBox);
		
		isEnableDisplayTotalDetailCheckBox=new JCheckBox("Enable 'Display Total Details(Default/Added/Updated)' option");
		isEnableDisplayTotalDetailCheckBox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isEnableDisplayTotalDetailCheckBox,InjectMain.FONT_SIZE_NORMAL);
		isEnableDisplayTotalDetailCheckBox.setForeground(Color.decode("#343836"));	
		tempPanel.add(isEnableDisplayTotalDetailCheckBox);		
		permissionPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));	
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPanel.add(tempPanel);
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

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					processActionSave();
				}
				catch(Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage(ex.getMessage());
				}
			}

		});
		tempPanel.add(saveButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_delete.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		deleteButton = new JButton();
		deleteButton.setIcon(ii);
		deleteButton.setBorderPainted(false);
		deleteButton.setContentAreaFilled(false);
		deleteButton.setFocusPainted(false);
		deleteButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_connection_delete_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		deleteButton.setRolloverIcon(new RolloverIcon(ii));
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				processActionUserDeletion();
			}

		});
		//tempPanel.add(deleteButton);
		tempPanel.add(Box.createRigidArea(new Dimension(15, 10)));
						
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
				close();
			}
		}
				);
		tempPanel.add(closeButton);
			
		rightPanel.add(Box.createRigidArea(new Dimension(15, panelSpacing)));
	}

	protected void processActionResetPassword() throws Exception {
		int selectedIndex = userNamesList.getSelectedIndex();
		if (selectedIndex==-1) {
			GUIUtils.popupErrorMessage(dialog,"You must select a user on the List");
			return;
		}
		final UserInformation userInformation=snapshotUserInformationList.get(selectedIndex-1);
		GenericLoginPanel genericLoginPanel=null;
		if (userInformation.isUserManager()) {
			genericLoginPanel=showDBALoginDialog();
		}
		else {
			List<UserInformation> restrictedUserInformationList=new ArrayList<UserInformation>();
			restrictedUserInformationList.add(userInformation);
			restrictedUserInformationList.add(getManagerUserInformation());
			genericLoginPanel=showUserLoginDialog(tabSnapshotsPanel,restrictedUserInformationList,"User Login");
		}
		if (!genericLoginPanel.isSuccess()) {
			return;
		}		
		resetUserPassword(userInformation);
	}

	private void editUser(int selectedIndex) {
		isUserManagerCheckBox.setEnabled(true);
		isEnabledCheckBox.setEnabled(true);
		String userName = "";
		try {
			File seFile = getConnectionFileName();
			Map<String,String> snapshotEnvironmentProperties=UIUtils.readSnapshotEnvironmentProperties(seFile);		
			userName=snapshotEnvironmentProperties.get(ServerFrame.SNAPSHOT_CREATOR_USERNAME_PROPERTY);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (selectedIndex==0) {
			loginNameTextField.setText("");
			loginNameTextField.setEnabled(true);
			userPasswordTextField.setText("");
			userPasswordVerifyTextField.setText("");
			fullNameTextField.setText("");
			hasSnapshotCreationPermissionCheckBox.setSelected(false);
			isUserManagerCheckBox.setSelected(false);
			isEnabledCheckBox.setSelected(false);
			userPasswordTextField.setEnabled(true);
			userPasswordVerifyTextField.setEnabled(true);
			isUsingForAutomationSnapshotCheckBox.setSelected(false);
			
			setSelectedToOptionsCheckbox(DEFAULT_VALUE_FOR_ENABLE_OPTION_PANEL);
			setEnableToOptionsCheckbox(true);
			
		}
		else {
			UserInformation userInformation=snapshotUserInformationList.get(selectedIndex-1);
			loginNameTextField.setEnabled(false);
			loginNameTextField.setText(userInformation.getLoginName());
			userPasswordTextField.setText(userInformation.getPassword());
			userPasswordVerifyTextField.setText(userInformation.getPassword());
			fullNameTextField.setText(userInformation.getFullName());
			hasSnapshotCreationPermissionCheckBox.setSelected(userInformation.hasSnapshotCreationPermission());	
			if((userName!=null || userName!="") && userInformation.getLoginName().equals(userName)){
				isUsingForAutomationSnapshotCheckBox.setSelected(true);
			}else{
				isUsingForAutomationSnapshotCheckBox.setSelected(false);
			}
			isUserManagerCheckBox.setSelected(userInformation.isUserManager());
			isEnabledCheckBox.setSelected(userInformation.isEnabled());
			
			isEnableDefaultParallelWorkersFieldCheckBox.setSelected(userInformation.isEnableDefaultParallelWorkersField());
			isEnableServerConnectionsDefaultFolderButtonCheckBox.setSelected(userInformation.isEnableServerConnectionsDefaultFolderButton());
			isEnableDownloadFolderButtonCheckBox.setSelected(userInformation.isEnableDownloadFolderButton());
			isEnableTemplateFolderButtonCheckBox.setSelected(userInformation.isEnableTemplateFolderButton());
			isEnableDefaultSnapshotNamePrefixFieldCheckBox.setSelected(userInformation.isEnableDefaultSnapshotNamePrefixField());	
			isEnableResetOptionButtonCheckBox.setSelected(userInformation.isEnableResetOptionButton());
			isEnableDeleteOptionCheckBox.setSelected(userInformation.isEnableDeleteOptionCheckBox());
			isEnableDisplayTotalDetailCheckBox.setSelected(userInformation.isEnableDisplayTotalDetailOption());
			
		}
		if (snapshotUserInformationList.isEmpty()) {
			isUserManagerCheckBox.setSelected(true);
			isUserManagerCheckBox.setEnabled(false);
			isEnabledCheckBox.setSelected(true);
			isEnabledCheckBox.setEnabled(false);
			
			setSelectedToOptionsCheckbox(true);
			setEnableToOptionsCheckbox(false);
			
		}
	}

	private void processActionUserDeletion() {
		int selectedIndex = userNamesList.getSelectedIndex();
		if (selectedIndex==-1) {
			GUIUtils.popupErrorMessage(dialog,"You must select a user on the List");
			return;
		}
		final UserInformation userInformation=snapshotUserInformationList.get(selectedIndex-1);
		int response = JOptionPane.showConfirmDialog(dialog, "Are you sure to delete the user '"+userInformation.getLoginName()+"' ?", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			deleteUser(userInformation);		
		}
	}

	private void deleteUser(UserInformation userInformation) {
		Connection connection=null;
		try {			
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties),true);
			
			ModelUtils.deleteUser(connection,userInformation);
			connection.commit();
			
			GUIUtils.popupInformationMessage(dialog,"User deleted!");
			(new UpdateRunner()).execute();
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
			unlock();
		}			
	}
	
	private void processActionSave() throws Exception {
		if (loginNameTextField.isEnabled()) {
			processActionCreateUser();
		}
		else {
			processActionUpdateUser();
		}
	}
	private File getConnectionFileName() throws Exception{
		File serverConnectionsFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getServerConnectionsFolder();
		String connectionName = tabSnapshotsPanel.getServerSelectionPanel().getSelectedServerConnection().toString();
		File seFile = new File(serverConnectionsFolder,connectionName+SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION); 
		return seFile;
	}
	protected boolean saveUserFile() throws Exception {
		File seFile = getConnectionFileName(); 
		String seFileString = CoreUtil.decryptFromFile(seFile);
		StringBuffer content=new StringBuffer(seFileString);
		content.append(ServerFrame.SNAPSHOT_CREATOR_USERNAME_PROPERTY).append("=").append(loginNameTextField.getText()).append("\n");
		content.append(ServerFrame.SNAPSHOT_CREATOR_PASSWORD_PROPERTY).append("=").append(new String(userPasswordTextField.getPassword())).append("\n");	
		try {
			seFile.delete();
			CoreUtil.encryptToFile(content.toString(),seFile);
			return true;
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to create file: '"+seFile.getAbsolutePath()+"'. Error: "+e.getMessage());
			return false;
		}
	}
	protected void setDefaultFileChooserProperties(JFileChooser fileChooser,String propertyName) {
		Preferences pref = Preferences.userRoot();
		String value = pref.get(propertyName, "");
		if (value!=null && !value.isEmpty()) {
			File file=new File(value);
			fileChooser.setCurrentDirectory(file.getParentFile());
			fileChooser.setSelectedFile(file);
		}
	}
	private boolean verifyPassword() {
		String password=new String(userPasswordTextField.getPassword());
		if (password.isEmpty()) {
			GUIUtils.popupErrorMessage(dialog,"Password cannot be empty!");
			return false;
		}
		String passwordCheck=new String(userPasswordVerifyTextField.getPassword());
		if (!password.equals(passwordCheck)) {
			GUIUtils.popupErrorMessage(dialog,"Passwords do not match!");
			return false;
		}
		return true;
	}

	private void processActionCreateUser() throws Exception {
		if (loginNameTextField.getText().trim().isEmpty()) {
			GUIUtils.popupErrorMessage(dialog,"You must set the login name");
			return;
		}
		if ( !verifyPassword() ) {
			return;
		}
		for (UserInformation userInformation:snapshotUserInformationList) {
			if (userInformation.getLoginName().equalsIgnoreCase(loginNameTextField.getText())) {
				GUIUtils.popupErrorMessage(dialog,"This login name is already used!");
				return;
			}
		}
		boolean isSavingSuccess = false;
		String message = "";
		boolean isUsingForAutomationSnapshot =isUsingForAutomationSnapshotCheckBox.isSelected();
		boolean hasPermissionToCreateSnapshot = hasSnapshotCreationPermissionCheckBox.isSelected();
		if(isUsingForAutomationSnapshot && !hasPermissionToCreateSnapshot){
			GUIUtils.popupErrorMessage(dialog,"Set 'Snapshot Creation Permission' if you would like to apply this user for the automation!");
			return;	
		}
		if (!snapshotUserInformationList.isEmpty()) {
			// only the manager can create new users
			if ( ! isManagerLoggedIn) {
				List<UserInformation> restrictedUserInformationList=new ArrayList<UserInformation>();
				restrictedUserInformationList.add(getManagerUserInformation());
				GenericLoginPanel genericLoginPanel=showUserLoginDialog(tabSnapshotsPanel,restrictedUserInformationList,"Manager Login");
				if (!genericLoginPanel.isSuccess()) {
					return;
				}
				isManagerLoggedIn=true;
			}
		}
		if(isUsingForAutomationSnapshot){
			isSavingSuccess = saveUserFile();
		}
		
		createNewUser();
		if(isSavingSuccess){
			message = "User created, Snapshot Environment File updated!";
		}else{
			message = "User created!";
			}
		GUIUtils.popupInformationMessage(dialog,message);
		(new UpdateRunner()).execute();
	}
	
	private UserInformation getManagerUserInformation() throws Exception {
		for (UserInformation userInformation:snapshotUserInformationList) {
			if (userInformation.isUserManager()) {
				return userInformation;
			}
		}
		throw new Exception("Internal error: no Manager detected!"); 
	}

	public void lock() {
		setComponentsEnabled(false);
	}

	public void unlock() {
		setComponentsEnabled(true);
	}

	private void setComponentsEnabled(boolean isEnabled) {
		loginNameTextField.setEnabled(isEnabled);
		userPasswordTextField.setEnabled(isEnabled);
		userPasswordVerifyTextField.setEnabled(isEnabled);
		fullNameTextField.setEnabled(isEnabled);
		hasSnapshotCreationPermissionCheckBox.setEnabled(isEnabled);
		saveButton.setEnabled(isEnabled);
		deleteButton.setEnabled(isEnabled);
		closeButton.setEnabled(isEnabled);
	}

	private void createNewUser() {
		Connection connection=null;
		try {
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties),true);
			

			String loginName=loginNameTextField.getText();
			String password=new String(userPasswordTextField.getPassword());
			String fullName=fullNameTextField.getText();
			boolean hasSnapshotCreationPermission=hasSnapshotCreationPermissionCheckBox.isSelected();
			boolean isUserManager=isUserManagerCheckBox.isSelected();
			boolean isEnabled=isEnabledCheckBox.isSelected();
			//
			boolean isEnableDefaultParallelWorkersField = isEnableDefaultParallelWorkersFieldCheckBox.isSelected();
			boolean isEnableServerConnectionsDefaultFolderButton = isEnableServerConnectionsDefaultFolderButtonCheckBox.isSelected();
			boolean isEnableDownloadFolderButton  = isEnableDownloadFolderButtonCheckBox.isSelected();
			boolean isEnableTemplateFolderButton = isEnableTemplateFolderButtonCheckBox.isSelected();	
			boolean isEnableDefaultSnapshotNamePrefixField = isEnableDefaultSnapshotNamePrefixFieldCheckBox.isSelected();
			boolean isEnableResetOptionButton = isEnableResetOptionButtonCheckBox.isSelected();
			boolean isEnableDeleteOption = isEnableDeleteOptionCheckBox.isSelected();
			boolean isEnableDisplayTotalDetailOption = isEnableDisplayTotalDetailCheckBox.isSelected();
			
			UserInformation userInformation=new UserInformation();
			userInformation.setLoginName(loginName);
			userInformation.setFullName(fullName);
			userInformation.setPassword(password);
			userInformation.setHasSnapshotCreationPermission(hasSnapshotCreationPermission);
			userInformation.setUserManager(isUserManager);
			userInformation.setEnabled(isEnabled);
			
			userInformation.setEnableDefaultParallelWorkersField(isEnableDefaultParallelWorkersField);
			userInformation.setEnableServerConnectionsDefaultFolderButton(isEnableServerConnectionsDefaultFolderButton);
			userInformation.setEnableDownloadFolderButton(isEnableDownloadFolderButton);
			userInformation.setEnableTemplateFolderButton(isEnableTemplateFolderButton);
			userInformation.setEnableDefaultSnapshotNamePrefixField(isEnableDefaultSnapshotNamePrefixField);
			userInformation.setEnableResetOptionButton(isEnableResetOptionButton);
			userInformation.setEnableDeleteOptionCheckBox(isEnableDeleteOption);
			userInformation.setEnableDisplayTotalDetailOption(isEnableDisplayTotalDetailOption);
			
			if (hasSnapshotCreationPermission) {
				ModelUtils.resetAllUsersCreationPermission(connection);
			}
			if (isUserManager) {
				ModelUtils.resetAllUsersManagerPermission(connection);
			}
			ModelUtils.createUser(connection,userInformation);
			connection.commit();
			selectedLoginName=userInformation.getLoginName();
			
			//GUIUtils.popupInformationMessage(dialog,"User created!");
			//(new UpdateRunner()).execute();
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
			unlock();
		}
	}
	
	public static GenericLoginPanel showUserLoginDialog(TabSnapshotsPanel tabSnapshotsPanel,
			List<UserInformation> restrictedUserInformationList,String title) {
		int width=300;
		int height=250;
		GenericLoginPanel genericLoginPanel=new UserSelectionPanel(tabSnapshotsPanel,restrictedUserInformationList,false);
		JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindow(tabSnapshotsPanel.
				getMainPanel().getSnapshotMain().getRootFrame(),title,width,height,
				genericLoginPanel,null,true,SnapshotMain.getSharedApplicationIconPath());
		genericLoginPanel.setDialog(dialog);
		boolean isAutomationMode = tabSnapshotsPanel.getMainPanel().getSnapshotMain().isAutomationMode();
		if(isAutomationMode){
			UserInformation userInformation = checkIfLoginSuccess(tabSnapshotsPanel,restrictedUserInformationList);
			if(userInformation!=null){
				genericLoginPanel.isSuccess=true;
				genericLoginPanel.setSelectedUser(userInformation);
				dialog.setVisible(false);			
			}
		}else{
			dialog.setVisible(true);
		}
		return genericLoginPanel;
	}
	
	public static UserInformation checkIfLoginSuccess(TabSnapshotsPanel tabSnapshotsPanel,List<UserInformation> restrictedUserInformationList){
		try{
			String serverConnectionName = tabSnapshotsPanel.getMainPanel().getSnapshotMain().getSnapshotArgumentsDocument().getSnapshotArguments().getConnectionName();
			File serverConnectionsFolder= tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getServerConnectionsFolder();
			File seFile=new File(serverConnectionsFolder,serverConnectionName+SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION);
			String userName = "";
			String password = "";
			Map<String,String> snapshotEnvironmentProperties=UIUtils.readSnapshotEnvironmentProperties(seFile);		
			userName=snapshotEnvironmentProperties.get(ServerFrame.SNAPSHOT_CREATOR_USERNAME_PROPERTY);
			password = snapshotEnvironmentProperties.get(ServerFrame.SNAPSHOT_CREATOR_PASSWORD_PROPERTY);			
				for (UserInformation userInformation:restrictedUserInformationList) {
					if (userInformation.getLoginName().equalsIgnoreCase(userName)) {
						if (userInformation.getPassword().equals(password)) {
							FileUtils.println("Create Snapshot by user : "+userInformation.getLoginName());
							return userInformation;
						}
						else {
							String errMsg = "Wrong password in '"+seFile.getAbsolutePath()+"'";
							FileUtils.println("ERROR :"+errMsg);
							tabSnapshotsPanel.getMainPanel().getSnapshotMain().writeToAutomationLogFile(StatusType.FAILED,errMsg);
							tabSnapshotsPanel.getMainPanel().getSnapshotMain().closeSnapshotAfterLogged();
							throw new Exception(errMsg);
						}
					}
				}							
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	private GenericLoginPanel showDBALoginDialog() {
		int width=300;
		int height=250;
		GenericLoginPanel genericLoginPanel=new DBALoginPanel(this);
		JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindow(tabSnapshotsPanel.
				getMainPanel().getSnapshotMain().getRootFrame(),"SYS Login",width,height,
				genericLoginPanel,null,true,SnapshotMain.getSharedApplicationIconPath());
		genericLoginPanel.setDialog(dialog);
		dialog.setVisible(true);
		return genericLoginPanel;
	}
	private boolean checkHasAtLeastOnePermissionChange(UserInformation userInformation) throws Exception{
		boolean toReturn = false;
		if (
				(userInformation.isEnabled() && !isEnabledCheckBox.isSelected()) ||
				(userInformation.isEnableDefaultParallelWorkersField() && !isEnableDefaultParallelWorkersFieldCheckBox.isSelected()) ||
				(userInformation.isEnableServerConnectionsDefaultFolderButton() && !isEnableServerConnectionsDefaultFolderButtonCheckBox.isSelected()) ||
				(userInformation.isEnableDownloadFolderButton() && !isEnableDownloadFolderButtonCheckBox.isSelected()) ||
				(userInformation.isEnableTemplateFolderButton() && !isEnableTemplateFolderButtonCheckBox.isSelected()) ||
				(userInformation.isEnableDefaultSnapshotNamePrefixField() && !isEnableDefaultSnapshotNamePrefixFieldCheckBox.isSelected()) ||
				(userInformation.isEnableResetOptionButton() && !isEnableResetOptionButtonCheckBox.isSelected()) ||	
				(userInformation.isEnableDeleteOptionCheckBox() && !isEnableDeleteOptionCheckBox.isSelected()) ||	
				(userInformation.isEnableDisplayTotalDetailOption() && !isEnableDisplayTotalDetailCheckBox.isSelected()) ||	
				(userInformation.hasSnapshotCreationPermission() && !hasSnapshotCreationPermissionCheckBox.isSelected()) ||
				(userInformation.isUserManager() && !isUserManagerCheckBox.isSelected()) ||
				
				(!userInformation.isEnabled() && isEnabledCheckBox.isSelected()) ||
				(!userInformation.isEnableDefaultParallelWorkersField() && isEnableDefaultParallelWorkersFieldCheckBox.isSelected()) ||
				(!userInformation.isEnableServerConnectionsDefaultFolderButton() && isEnableServerConnectionsDefaultFolderButtonCheckBox.isSelected()) ||
				(!userInformation.isEnableDownloadFolderButton() && isEnableDownloadFolderButtonCheckBox.isSelected()) ||
				(!userInformation.isEnableTemplateFolderButton() && isEnableTemplateFolderButtonCheckBox.isSelected()) ||
				(!userInformation.isEnableDefaultSnapshotNamePrefixField() && isEnableDefaultSnapshotNamePrefixFieldCheckBox.isSelected()) ||
				(!userInformation.isEnableResetOptionButton() && isEnableResetOptionButtonCheckBox.isSelected()) ||	
				(!userInformation.isEnableDeleteOptionCheckBox() && isEnableDeleteOptionCheckBox.isSelected()) ||	
				(!userInformation.isEnableDisplayTotalDetailOption() && isEnableDisplayTotalDetailCheckBox.isSelected()) ||	
				(!userInformation.hasSnapshotCreationPermission() && hasSnapshotCreationPermissionCheckBox.isSelected()) ||
				(!userInformation.isUserManager() && isUserManagerCheckBox.isSelected())

				) {
			toReturn=true;
		}
		return toReturn;
		
	}
	private void processActionUpdateUser() throws Exception {
		int selectedIndex=userNamesList.getSelectedIndex();
		UserInformation userInformation=snapshotUserInformationList.get(selectedIndex-1);
		selectedLoginName=userInformation.getLoginName();
		
		boolean hasAtLeastOnePermissionChange=checkHasAtLeastOnePermissionChange(userInformation);
		boolean hasPasswordChange=false;
		boolean isUsingForAutomationSnapshot =isUsingForAutomationSnapshotCheckBox.isSelected();
		String passwordChecking=new String(userPasswordTextField.getPassword());
		if ( !userInformation.getPassword().equals(passwordChecking)) {
			 hasPasswordChange=true;
		}
		
		if (hasPasswordChange) {
			if ( userInformation.isUserManager() ) {
				// needs manager login
				if ( ! isManagerLoggedIn) {
					List<UserInformation> restrictedUserInformationList=new ArrayList<UserInformation>();
					restrictedUserInformationList.add(getManagerUserInformation());
					GenericLoginPanel genericLoginPanel=showUserLoginDialog(tabSnapshotsPanel,restrictedUserInformationList,"Manager Login");
					if (!genericLoginPanel.isSuccess()) {
						(new UpdateRunner()).execute();
						return;
					}
					isManagerLoggedIn=true;
				}
			}
			else {
				// needs manager login or user login
				List<UserInformation> restrictedUserInformationList=new ArrayList<UserInformation>();
				restrictedUserInformationList.add(userInformation);
				restrictedUserInformationList.add(getManagerUserInformation());
				GenericLoginPanel genericLoginPanel=showUserLoginDialog(tabSnapshotsPanel,restrictedUserInformationList,"User Login");
				if (!genericLoginPanel.isSuccess()) {
					(new UpdateRunner()).execute();
					return;
				}
			}
		}
		else {
			if (hasAtLeastOnePermissionChange) {
				// only the manager can update users permissions
				if ( ! isManagerLoggedIn) {
					List<UserInformation> restrictedUserInformationList=new ArrayList<UserInformation>();
					restrictedUserInformationList.add(getManagerUserInformation());
					GenericLoginPanel genericLoginPanel=showUserLoginDialog(tabSnapshotsPanel,restrictedUserInformationList,"Manager Login");
					if (!genericLoginPanel.isSuccess()) {
						(new UpdateRunner()).execute();
						return;
					}
					isManagerLoggedIn=true;
				}
			}
			else {
				// only the user or the manager can update its details
				List<UserInformation> restrictedUserInformationList=new ArrayList<UserInformation>();
				restrictedUserInformationList.add(userInformation);
				restrictedUserInformationList.add(getManagerUserInformation());
				GenericLoginPanel genericLoginPanel=showUserLoginDialog(tabSnapshotsPanel,restrictedUserInformationList,"User Login");
				if (!genericLoginPanel.isSuccess()) {
					(new UpdateRunner()).execute();
					return;
				}
			}
		}		
		
		if ( !verifyPassword() ) {
			(new UpdateRunner()).execute();
			return;
		}
		boolean isSavingSuccess = false;
		String message = "";
		if(isUsingForAutomationSnapshot){	
			isSavingSuccess = saveUserFile();
		}
		Connection connection=null;
		try {
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties),true);
			connection.setAutoCommit(false);
			
			String fullName=fullNameTextField.getText();
			boolean hasSnapshotCreationPermission=hasSnapshotCreationPermissionCheckBox.isSelected();
			boolean isUserManager=isUserManagerCheckBox.isSelected();
			boolean isEnabled=isEnabledCheckBox.isSelected();
			String password=new String(userPasswordTextField.getPassword());
			
			boolean isEnableDefaultParallelWorkersField = isEnableDefaultParallelWorkersFieldCheckBox.isSelected();
			boolean isEnableServerConnectionsDefaultFolderButton = isEnableServerConnectionsDefaultFolderButtonCheckBox.isSelected();
			boolean isEnableDownloadFolderButton  = isEnableDownloadFolderButtonCheckBox.isSelected();
			boolean isEnableTemplateFolderButton = isEnableTemplateFolderButtonCheckBox.isSelected();	
			boolean isEnableDefaultSnapshotNamePrefixField = isEnableDefaultSnapshotNamePrefixFieldCheckBox.isSelected();
			boolean isEnableResetOptionButton = isEnableResetOptionButtonCheckBox.isSelected();
			boolean isEnableDeleteOption = isEnableDeleteOptionCheckBox.isSelected();
			boolean isEnableDisplayTotalDetailOption = isEnableDisplayTotalDetailCheckBox.isSelected();
			
			userInformation.setPassword(password);
			userInformation.setFullName(fullName);
			userInformation.setUserManager(isUserManager);
			userInformation.setEnabled(isEnabled);
			userInformation.setHasSnapshotCreationPermission(hasSnapshotCreationPermission);
			
			userInformation.setEnableDefaultParallelWorkersField(isEnableDefaultParallelWorkersField);
			userInformation.setEnableServerConnectionsDefaultFolderButton(isEnableServerConnectionsDefaultFolderButton);
			userInformation.setEnableDownloadFolderButton(isEnableDownloadFolderButton);
			userInformation.setEnableTemplateFolderButton(isEnableTemplateFolderButton);
			userInformation.setEnableDefaultSnapshotNamePrefixField(isEnableDefaultSnapshotNamePrefixField);
			userInformation.setEnableResetOptionButton(isEnableResetOptionButton);
			userInformation.setEnableDeleteOptionCheckBox(isEnableDeleteOption);
			userInformation.setEnableDisplayTotalDetailOption(isEnableDisplayTotalDetailOption);
			
			if (hasSnapshotCreationPermission) {
				ModelUtils.resetAllUsersCreationPermission(connection);
			}
			if (isUserManager) {
				ModelUtils.resetAllUsersManagerPermission(connection);
			}
			ModelUtils.updateUser(connection,userInformation);
			connection.commit();
			if(isSavingSuccess){
				message = "User and Snapshot Environment File updated!";
			}else{
				message = "User updated!";
			}			
			GUIUtils.popupInformationMessage(dialog,message);
			(new UpdateRunner()).execute();
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
			unlock();
		}
	}
		
	private void displayUserNameList() throws ClassNotFoundException, SQLException {
		Connection connection=null;
		try {
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties),true);
			
			snapshotUserInformationList=ModelUtils.getSnapshotUserInformationList(connection);
			
			listModel = new DefaultListModel<String>(); 
			//listModel.removeAllElements();
			listModel.addElement(NEW_USER);
			
			int selectedUserIndex=0;
			int index=1;
			for (UserInformation userInformation:snapshotUserInformationList) {
				String loginName=userInformation.getLoginName();
				if (selectedLoginName!=null && loginName.equals(selectedLoginName)) {
					selectedUserIndex=index;
				}
				boolean hasSnapshotCreationPermission=userInformation.hasSnapshotCreationPermission();
				
				String line=loginName;
				String content="";
				if (hasSnapshotCreationPermission) {
					content="Snapshot creation";
				}
				if (userInformation.isUserManager()) {
					if (!content.isEmpty()) {
						content+=", ";
					}
					content+="Manager";
				}
				
				if (!content.isEmpty()) {
					line+=" ("+content+")";
				}
				listModel.addElement(line);
				index++;
			}
			userNamesList.setModel(listModel);
			userNamesList.setSelectedIndex(selectedUserIndex);
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
		}
	}
	
	private class UpdateRunner extends SwingWorker<List<String>, List<String>>{
		@Override
		public List<String> doInBackground() {
			try {
				displayUserNameList();
			} 
			catch (Exception e) {
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage(e.getMessage());
			}
			return null;
		}
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}
	
	private void resetUserPassword(UserInformation userInformation) {
		Connection connection=null;
		try {
			String defaultManagerPassword="manager";
			
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties),true);
			
			ModelUtils.updateUserPassword(connection,userInformation.getId(),defaultManagerPassword);
			connection.commit();
			
			GUIUtils.popupInformationMessage(dialog,"Password reset to '"+defaultManagerPassword+"'");
			(new UpdateRunner()).execute();
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
		finally{
			DirectConnectDao.closeQuietly(connection);
			unlock();
		}			
	}
	private void setSelectedToOptionsCheckbox(boolean isSelected){
		isEnableDefaultParallelWorkersFieldCheckBox.setSelected(isSelected);
		isEnableServerConnectionsDefaultFolderButtonCheckBox.setSelected(isSelected);
		isEnableDownloadFolderButtonCheckBox.setSelected(isSelected);
		isEnableTemplateFolderButtonCheckBox.setSelected(isSelected);
		isEnableDefaultSnapshotNamePrefixFieldCheckBox.setSelected(isSelected);	
		isEnableResetOptionButtonCheckBox.setSelected(isSelected);
		isEnableDeleteOptionCheckBox.setSelected(isSelected);	
		isEnableDisplayTotalDetailCheckBox.setSelected(isSelected);
	}
	private void setEnableToOptionsCheckbox(boolean isEnabled){
		isEnableDefaultParallelWorkersFieldCheckBox.setEnabled(isEnabled);
		isEnableServerConnectionsDefaultFolderButtonCheckBox.setEnabled(isEnabled);
		isEnableDownloadFolderButtonCheckBox.setEnabled(isEnabled);
		isEnableTemplateFolderButtonCheckBox.setEnabled(isEnabled);
		isEnableDefaultSnapshotNamePrefixFieldCheckBox.setEnabled(isEnabled);
		isEnableResetOptionButtonCheckBox.setEnabled(isEnabled);
		isEnableDeleteOptionCheckBox.setEnabled(isEnabled);	
		isEnableDisplayTotalDetailCheckBox.setEnabled(isEnabled);
	}	
}
