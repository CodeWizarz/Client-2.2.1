package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.UserInformation;

@SuppressWarnings("serial")
public class TabOptionsPanel extends JPanel{

	private MainPanel mainPanel;
	private JLabel workersLabel;
	private JTextField workersTextField;
	private JLabel defaultSnapshotNameLabel;
	private JTextField defaultSnapshotNameTextField;
	private JTextArea seededUsersTextArea;
	private JLabel defaultServerInformationFolderLabel;
	private JLabel defaultServerInformationFolderPathLabel;
	private JButton defaultServerInformationFolderButton;
	private JFileChooser defaultServerInformationFolderFileChooser;
	private File serverConnectionsFolder;
	private JLabel downloadFolderPathLabel;
	private JButton downloadFolderButton;
	private JFileChooser downloadFolderFileChooser;
	private File downloadFolder;
	private JLabel templateFolderPathLabel;
	private JButton templateFolderButton;
	private JFileChooser templateFolderFileChooser;
	private File templateFolder;
	private JRadioButton excelFormatRadioButton;
	private JRadioButton xmlFormatRadioButton; 
	private JCheckBox showUnsupportedInventoriesCheckbox;
	private JCheckBox showTotalsDetailsCheckbox;
	private JCheckBox showHelpCheckbox;
	private JCheckBox reportsVerticalWayCheckbox;
	private JButton saveOptions;
	private JButton resetOptions;
	private JRadioButton softDeleteRadioButton;
	private JRadioButton physicalDeleteRadioButton; 	
	private JCheckBox clearSoftDeletedSnapshotsCheckbox;
	

	
	public static final String PREFERENCE_WORKERS_COUNT="PREFERENCE_WORKERS_COUNT";
	public static final String PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION="PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION";
	public static final String PREFERENCE_DOWNLOAD_FOLDER_LOCATION="PREFERENCE_DOWNLOAD_FOLDER_LOCATION";
	public static final String PREFERENCE_SNAPSHOT_PREFIX_NAME="PREFERENCE_SNAPSHOT_PREFIX_NAME";
	public static final String PREFERENCE_DOWNLOAD_FORMAT="PREFERENCE_DOWNLOAD_FORMAT";
	public static final String PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES="PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES";
	public static final String PREFERENCE_DISPLAY_TOTAL_DETAILS="PREFERENCE_DISPLAY_TOTAL_DETAILS";
	public static final String PREFERENCE_SEEDED_USERS_DEFINITION="PREFERENCE_SEEDED_USERS_DEFINITION";
	public static final String PREFERENCE_SHOW_HELPER_BALLOONS="PREFERENCE_SHOW_HELPER_BALLOONS";
	public static final String PREFERENCE_REPORT_VERTICAL_WAY="PREFERENCE_REPORT_VERTICAL_WAY";
	public static final String PREFERENCE_TEMPLATE_FOLDER_LOCATION="PREFERENCE_TEMPLATE_FOLDER_LOCATION";
	public static final String PREFERENCE_SNAPSHOT_DELETE_OPTION="PREFERENCE_SNAPSHOT_DELETE_OPTION";
	public static final String PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION="PREFERENCE_SNAPSHOT_CLEAR_OUT_OPTION";	
	
	public static final String SHORT_PREFERENCE_WORKERS_COUNT="PREF_WORKERS_CNT";
	public static final String SHORT_PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION="PREF_SERVER_CON_FLD_LOC";
	public static final String SHORT_PREFERENCE_DOWNLOAD_FOLDER_LOCATION="PREF_DOWNLOAD_FLD_LOC";
	public static final String SHORT_PREFERENCE_SNAPSHOT_PREFIX_NAME="PREF_SNAPSHOT_PREFIX_NAME";
	public static final String SHORT_PREFERENCE_DOWNLOAD_FORMAT="PREF_DOWNLOAD_FORMAT";
	public static final String SHORT_PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES="PREF_DISPLAY_UNSUPPORTED_INV";
	public static final String SHORT_PREFERENCE_DISPLAY_TOTAL_DETAILS="PREFE_DISPLAY_TOTAL_DETAILS";
	public static final String SHORT_PREFERENCE_SEEDED_USERS_DEFINITION="PREF_SEEDED_USERS_DEF";
	public static final String SHORT_PREFERENCE_SHOW_HELPER_BALLOONS="PREF_SHOW_HELPER_BALLOONS";
	public static final String SHORT_PREFERENCE_REPORT_VERTICAL_WAY="PREF_REPORT_VERTICAL_WAY";
	public static final String SHORT_PREFERENCE_TEMPLATE_FOLDER_LOCATION="PREF_TEMPLATE_FLD_LOC";
	public static final String SHORT_PREFERENCE_SNAPSHOT_DELETE_OPTION="PREF_SNAPSHOT_DELETE";
	public static final String SHORT_PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION="PREF_SNAPSHOT_CLEAR_OUT";	
	
	public static final String DEFAULT_WORKERS_COUNT_VALUE="16";	
	public static final String DEFAULT_DOWNLOAD_FOLDER_NAME="snapshot-downloads";
	public static final String DEFAULT_TEMPLATE_FOLDER_NAME="templates";
	public static final String DEFAULT_SERVER_CONNECTIONS_FOLDER_NAME="snapshot-servers";
	public static final String DEFAULT_SNAPSHOT_PREFIX_NAME="SNAPSHOT";
	public static final String DEFAULT_DOWNLOAD_FORMAT="EXCEL";
	public static final boolean DEFAULT_DISPLAY_UNSUPPORTED_INVENTORIES=false;
	public static final boolean DEFAULT_DISPLAY_TOTAL_DETAILS=false;
	public static final boolean DEFAULT_SHOW_HELPER_BALLOONS=true;
	public static final boolean DEFAULT_REPORT_VERTICAL_WAY=true;
	public static final String DEFAULT_SNAPSHOT_DELETE_OPTION="Soft delete";
	public static final boolean DEFAULT_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION=false;	
	
	public static final String EXCEL_RADIO_BUTTON_TEXT="EXCEL";
	public static final String XML_RADIO_BUTTON_TEXT="XML";
	
	public static final String SOFT_DELETE_RADIO_BUTTON_TEXT="Soft delete";
	public static final String PHYSICAL_DELETE_BUTTON_TEXT="Physical delete";
	
	public static final String DEFAULT_USERLOGIN_NAME = "#DEFAULT#";
	public static final String DEFAULT_SCHEMA_NAME = "#DEFAULT#";	
	public static final String DEFAULT_PREFERENCE_NAME = "#DEFAULT#";
	public static final int PREFERENCE_NAME_MAXIMUM_LENGTH = 80;

	public TabOptionsPanel(MainPanel mainPanel) {
		this.mainPanel=mainPanel;
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 0));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		createComponents();
	}
	
/*	private void setValues() {
		
		Preferences pref = Preferences.userRoot();
		
		File userhomeFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
		
		String value= pref.get(PREFERENCE_WORKERS_COUNT,DEFAULT_WORKERS_COUNT_VALUE);
		workersTextField.setText(value);	
		
		File defaultServerConnectionFolder=new File(userhomeFolder,DEFAULT_SERVER_CONNECTIONS_FOLDER_NAME);
		defaultServerConnectionFolder.mkdirs();
		
		value = pref.get(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION,defaultServerConnectionFolder.getAbsolutePath());
		serverConnectionsFolder=new File(value);
		serverConnectionsFolder.mkdirs();
		defaultServerInformationFolderFileChooser.setCurrentDirectory(serverConnectionsFolder);
		defaultServerInformationFolderPathLabel.setText(serverConnectionsFolder.getAbsolutePath());
		
		File defaultDownloadFolder=new File(userhomeFolder,DEFAULT_DOWNLOAD_FOLDER_NAME);
		defaultDownloadFolder.mkdirs();
		
		value = pref.get(PREFERENCE_DOWNLOAD_FOLDER_LOCATION,defaultDownloadFolder.getAbsolutePath());
		downloadFolder=new File(value);
		downloadFolder.mkdirs();
		downloadFolderFileChooser.setCurrentDirectory(downloadFolder);
		downloadFolderPathLabel.setText(downloadFolder.getAbsolutePath());
		
		File defaultTemplateFolder=new File(userhomeFolder,DEFAULT_TEMPLATE_FOLDER_NAME);
		defaultTemplateFolder.mkdirs();
		
		value = pref.get(PREFERENCE_TEMPLATE_FOLDER_LOCATION,defaultTemplateFolder.getAbsolutePath());
		templateFolder=new File(value);
		templateFolder.mkdirs();
		templateFolderFileChooser.setCurrentDirectory(templateFolder);
		templateFolderPathLabel.setText(templateFolder.getAbsolutePath());	
		
		String str= pref.get(PREFERENCE_SNAPSHOT_PREFIX_NAME,DEFAULT_SNAPSHOT_PREFIX_NAME);
		defaultSnapshotNameTextField.setText(str);
		
		str= pref.get(PREFERENCE_DOWNLOAD_FORMAT,DEFAULT_DOWNLOAD_FORMAT);
		if (str.equalsIgnoreCase(DEFAULT_DOWNLOAD_FORMAT)) {
			excelFormatRadioButton.setSelected(true);
			xmlFormatRadioButton.setSelected(false);
		}
		else {
			excelFormatRadioButton.setSelected(false);
			xmlFormatRadioButton.setSelected(true);
		}
		
		boolean isCheckClearOutCheckBox= pref.getBoolean(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION,DEFAULT_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
		clearSoftDeletedSnapshotsCheckbox.setSelected(isCheckClearOutCheckBox);	
		
		str= pref.get(PREFERENCE_SNAPSHOT_DELETE_OPTION,DEFAULT_SNAPSHOT_DELETE_OPTION);
		if (str.equalsIgnoreCase(DEFAULT_SNAPSHOT_DELETE_OPTION)) {
			softDeleteRadioButton.setSelected(true);
			physicalDeleteRadioButton.setSelected(false);
			clearSoftDeletedSnapshotsCheckbox.setVisible(false);
		}else {
			softDeleteRadioButton.setSelected(false);
			physicalDeleteRadioButton.setSelected(true);
			clearSoftDeletedSnapshotsCheckbox.setVisible(true);
		}	

		boolean b= pref.getBoolean(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,DEFAULT_DISPLAY_UNSUPPORTED_INVENTORIES);
		showUnsupportedInventoriesCheckbox.setSelected(b);

		b= pref.getBoolean(PREFERENCE_DISPLAY_TOTAL_DETAILS,DEFAULT_DISPLAY_TOTAL_DETAILS);
		showTotalsDetailsCheckbox.setSelected(b);

		b= pref.getBoolean(PREFERENCE_SHOW_HELPER_BALLOONS,DEFAULT_SHOW_HELPER_BALLOONS);
		showHelpCheckbox.setSelected(b);
		UIUtils.allBallonTipsSetVisable(b);

		b= pref.getBoolean(PREFERENCE_REPORT_VERTICAL_WAY,DEFAULT_REPORT_VERTICAL_WAY);
		reportsVerticalWayCheckbox.setSelected(b);

		str= pref.get(PREFERENCE_SEEDED_USERS_DEFINITION,ModelUtils.ORACLE_DEFAULT_SEEDED_USER_NAMES);
		seededUsersTextArea.setText(str);
		
	}*/

	private void createComponents() {
		int fieldsWidth=100;
		int height=20;
		
		JPanel workersPanel=new JPanel();
		workersPanel.setOpaque(false);
		workersPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(workersPanel);
		workersLabel=new JLabel("Default Parallel Workers:");
		workersLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(workersLabel,InjectMain.FONT_SIZE_NORMAL);
		workersLabel.setForeground(Color.decode("#343836"));
		workersPanel.add(workersLabel);
		workersTextField=new JTextField();
		InjectUtils.assignArialPlainFont(workersTextField,InjectMain.FONT_SIZE_NORMAL);
		workersTextField.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(workersTextField,fieldsWidth,height);
		workersPanel.add(workersTextField);
		
		JPanel defaultServerInformationFolderPanel=new JPanel();
		defaultServerInformationFolderPanel.setOpaque(false);
		defaultServerInformationFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(defaultServerInformationFolderPanel);
		defaultServerInformationFolderLabel=new JLabel("Server Connections Default Folder:");
		defaultServerInformationFolderLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(defaultServerInformationFolderLabel,InjectMain.FONT_SIZE_NORMAL);
		defaultServerInformationFolderLabel.setForeground(Color.decode("#343836"));
		
		defaultServerInformationFolderPanel.add(defaultServerInformationFolderLabel);
		defaultServerInformationFolderPathLabel=new JLabel("");
		defaultServerInformationFolderPathLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(defaultServerInformationFolderPathLabel,InjectMain.FONT_SIZE_NORMAL);
		defaultServerInformationFolderPathLabel.setForeground(Color.decode("#343836"));
		defaultServerInformationFolderPanel.add(defaultServerInformationFolderPathLabel);
		
		ImageIcon ii=null;
		URL iconURL =null;

		iconURL = this.getClass().getResource("/images/snapshot/button_select.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		defaultServerInformationFolderButton = new JButton();
		defaultServerInformationFolderButton.setIcon(ii);
		defaultServerInformationFolderButton.setBorderPainted(false);
		defaultServerInformationFolderButton.setContentAreaFilled(false);
		defaultServerInformationFolderButton.setFocusPainted(false);
		defaultServerInformationFolderButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_select_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		defaultServerInformationFolderButton.setRolloverIcon(new RolloverIcon(ii));
		defaultServerInformationFolderPanel.add(defaultServerInformationFolderButton);
		defaultServerInformationFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionPackageDefaultServerFolder();
			}
		}
				);
		
		JPanel downloadFolderPanel=new JPanel();
		downloadFolderPanel.setOpaque(false);
		downloadFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(downloadFolderPanel);
		JLabel label=new JLabel("Download Folder:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		
		downloadFolderPanel.add(label);
		downloadFolderPathLabel=new JLabel("");
		downloadFolderPathLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(downloadFolderPathLabel,InjectMain.FONT_SIZE_NORMAL);
		downloadFolderPathLabel.setForeground(Color.decode("#343836"));
		downloadFolderPanel.add(downloadFolderPathLabel);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_select.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		downloadFolderButton = new JButton();
		downloadFolderButton.setIcon(ii);
		downloadFolderButton.setBorderPainted(false);
		downloadFolderButton.setContentAreaFilled(false);
		downloadFolderButton.setFocusPainted(false);
		downloadFolderButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_select_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		downloadFolderButton.setRolloverIcon(new RolloverIcon(ii));
		downloadFolderPanel.add(downloadFolderButton);
		downloadFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionDownloadFolder();
			}
		}
				);
		
		JPanel templateFolderPanel=new JPanel();
		templateFolderPanel.setOpaque(false);
		templateFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(templateFolderPanel);
		label=new JLabel("Template Folder:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		
		templateFolderPanel.add(label);
		templateFolderPathLabel=new JLabel("");
		templateFolderPathLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(templateFolderPathLabel,InjectMain.FONT_SIZE_NORMAL);
		templateFolderPathLabel.setForeground(Color.decode("#343836"));
		templateFolderPanel.add(templateFolderPathLabel);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_select.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateFolderButton = new JButton();
		templateFolderButton.setIcon(ii);
		templateFolderButton.setBorderPainted(false);
		templateFolderButton.setContentAreaFilled(false);
		templateFolderButton.setFocusPainted(false);
		templateFolderButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_select_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateFolderButton.setRolloverIcon(new RolloverIcon(ii));
		templateFolderPanel.add(templateFolderButton);
		templateFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionTemplateFolder();
			}
		}
				);
		
		JPanel snapshotNamePanel=new JPanel();
		snapshotNamePanel.setOpaque(false);
		snapshotNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(snapshotNamePanel);
		defaultSnapshotNameLabel=new JLabel("Default Snapshot name prefix:");
		defaultSnapshotNameLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(defaultSnapshotNameLabel,InjectMain.FONT_SIZE_NORMAL);
		defaultSnapshotNameLabel.setForeground(Color.decode("#343836"));
		
		snapshotNamePanel.add(defaultSnapshotNameLabel);
		defaultSnapshotNameTextField=new JTextField();
		InjectUtils.assignArialPlainFont(defaultSnapshotNameTextField,InjectMain.FONT_SIZE_NORMAL);
		defaultSnapshotNameTextField.setForeground(Color.decode("#343836"));
		
		UIUtils.setDimension(defaultSnapshotNameTextField,150,height);
		snapshotNamePanel.add(defaultSnapshotNameTextField);
	
		
		
		
		
		ButtonGroup exportFormatButtonGroup = new ButtonGroup();
		excelFormatRadioButton = new JRadioButton(EXCEL_RADIO_BUTTON_TEXT);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_unselected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		excelFormatRadioButton.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_selected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		excelFormatRadioButton.setSelectedIcon(ii);
		excelFormatRadioButton.setOpaque(false);
		InjectUtils.assignArialPlainFont(excelFormatRadioButton,InjectMain.FONT_SIZE_NORMAL);
		excelFormatRadioButton.setForeground(Color.decode("#343836"));
		excelFormatRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionClickExcelFormat();
			}
		}
				);
		
		xmlFormatRadioButton = new JRadioButton(XML_RADIO_BUTTON_TEXT);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_unselected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		xmlFormatRadioButton.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_selected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		xmlFormatRadioButton.setSelectedIcon(ii);
		xmlFormatRadioButton.setOpaque(false);
		InjectUtils.assignArialPlainFont(xmlFormatRadioButton,InjectMain.FONT_SIZE_NORMAL);
		xmlFormatRadioButton.setForeground(Color.decode("#343836"));
		xmlFormatRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionClickXMLFormat();
			}
		}
				);
		
		exportFormatButtonGroup.add(excelFormatRadioButton);
		exportFormatButtonGroup.add(xmlFormatRadioButton);
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		
		label=new JLabel("Download Format: ");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		label.setOpaque(false);
		
		tempPanel.add(label);
		tempPanel.add(excelFormatRadioButton);
		tempPanel.add(xmlFormatRadioButton);
		
		
		ButtonGroup snapshotDeleteOptionButtonGroup = new ButtonGroup();
		softDeleteRadioButton = new JRadioButton(SOFT_DELETE_RADIO_BUTTON_TEXT);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_unselected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		softDeleteRadioButton.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_selected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		softDeleteRadioButton.setSelectedIcon(ii);
		softDeleteRadioButton.setOpaque(false);
		InjectUtils.assignArialPlainFont(softDeleteRadioButton,InjectMain.FONT_SIZE_NORMAL);
		softDeleteRadioButton.setForeground(Color.decode("#343836"));
		
		physicalDeleteRadioButton = new JRadioButton(PHYSICAL_DELETE_BUTTON_TEXT);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_unselected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		physicalDeleteRadioButton.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/checkbox_selected.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		physicalDeleteRadioButton.setSelectedIcon(ii);
		physicalDeleteRadioButton.setOpaque(false);
		InjectUtils.assignArialPlainFont(physicalDeleteRadioButton,InjectMain.FONT_SIZE_NORMAL);
		physicalDeleteRadioButton.setForeground(Color.decode("#343836"));
		snapshotDeleteOptionButtonGroup.add(softDeleteRadioButton);
		snapshotDeleteOptionButtonGroup.add(physicalDeleteRadioButton);
		softDeleteRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionShowClearSoftDeletedOption(false);
			}
		}
		);		
		physicalDeleteRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionShowClearSoftDeletedOption(true);
			}
		}
		);
		clearSoftDeletedSnapshotsCheckbox=new JCheckBox("Include soft-deleted snapshots");
		clearSoftDeletedSnapshotsCheckbox.setOpaque(false);
		InjectUtils.assignArialPlainFont(clearSoftDeletedSnapshotsCheckbox,InjectMain.FONT_SIZE_NORMAL);
		clearSoftDeletedSnapshotsCheckbox.setForeground(Color.decode("#343836"));
		clearSoftDeletedSnapshotsCheckbox.setVisible(false);

		
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		
		label=new JLabel("Snapshot delete option: ");
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		label.setOpaque(false);
		
		tempPanel.add(label);
		tempPanel.add(softDeleteRadioButton);
		tempPanel.add(physicalDeleteRadioButton);	
		tempPanel.add(clearSoftDeletedSnapshotsCheckbox);
		
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		showUnsupportedInventoriesCheckbox=new JCheckBox("Display unsupported Inventories");
		showUnsupportedInventoriesCheckbox.setOpaque(false);
		InjectUtils.assignArialPlainFont(showUnsupportedInventoriesCheckbox,InjectMain.FONT_SIZE_NORMAL);
		showUnsupportedInventoriesCheckbox.setForeground(Color.decode("#343836"));
		tempPanel.add(showUnsupportedInventoriesCheckbox);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		showTotalsDetailsCheckbox=new JCheckBox("Display Total Details (Default/ Added/ Updated)");
		showTotalsDetailsCheckbox.setOpaque(false);
		InjectUtils.assignArialPlainFont(showTotalsDetailsCheckbox,InjectMain.FONT_SIZE_NORMAL);
		showTotalsDetailsCheckbox.setForeground(Color.decode("#343836"));
		tempPanel.add(showTotalsDetailsCheckbox);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		showHelpCheckbox=new JCheckBox("Display Help balloons");
		showHelpCheckbox.setOpaque(false);
		InjectUtils.assignArialPlainFont(showHelpCheckbox,InjectMain.FONT_SIZE_NORMAL);
		showHelpCheckbox.setForeground(Color.decode("#343836"));
		showHelpCheckbox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				UIUtils.allBallonTipsSetVisable(showHelpCheckbox.isSelected());				
			}
		});
		tempPanel.add(showHelpCheckbox);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		reportsVerticalWayCheckbox=new JCheckBox("Display BR100 Report Data in Vertical Grid");
		reportsVerticalWayCheckbox.setOpaque(false);
		InjectUtils.assignArialPlainFont(reportsVerticalWayCheckbox,InjectMain.FONT_SIZE_NORMAL);
		reportsVerticalWayCheckbox.setForeground(Color.decode("#343836"));
		tempPanel.add(reportsVerticalWayCheckbox);
				
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		label=new JLabel("Seeded users definition:");
		label.setOpaque(false);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setForeground(Color.decode("#343836"));
		tempPanel.add(label);
		seededUsersTextArea=new JTextArea(5, 120);
		seededUsersTextArea.setLineWrap(true);
		tempPanel.add(seededUsersTextArea);
		add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_reset.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		JButton resetSeededUsersButton = new JButton();
		resetSeededUsersButton.setIcon(ii);
		resetSeededUsersButton.setBorderPainted(false);
		resetSeededUsersButton.setContentAreaFilled(false);
		resetSeededUsersButton.setFocusPainted(false);
		resetSeededUsersButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_reset_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		resetSeededUsersButton.setRolloverIcon(new RolloverIcon(ii));
		resetSeededUsersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionResetSeededUsers();
			}
		}
				);
		tempPanel.add(resetSeededUsersButton);

 
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		add(tempPanel);
		iconURL = this.getClass().getResource("/images/snapshot/button_save.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveOptions = new JButton();
		saveOptions.setIcon(ii);
		saveOptions.setBorderPainted(false);
		saveOptions.setContentAreaFilled(false);
		saveOptions.setFocusPainted(false);
		saveOptions.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveOptions.setRolloverIcon(new RolloverIcon(ii));
		saveOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionSaveOptions();
			}
		}
				);
		tempPanel.add(saveOptions);
		iconURL = this.getClass().getResource("/images/snapshot/button_reset.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		resetOptions = new JButton();
		resetOptions.setIcon(ii);
		resetOptions.setBorderPainted(false);
		resetOptions.setContentAreaFilled(false);
		resetOptions.setFocusPainted(false);
		resetOptions.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_reset_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		resetOptions.setRolloverIcon(new RolloverIcon(ii));
		resetOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionResetOptions();
			}
		}
				);
		tempPanel.add(resetOptions);
		
		add(Box.createVerticalGlue());

		defaultServerInformationFolderFileChooser= new JFileChooser();
		defaultServerInformationFolderFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		defaultServerInformationFolderFileChooser.setAcceptAllFileFilterUsed(false);
		defaultServerInformationFolderFileChooser.setDialogTitle("Choose a folder");
		
		downloadFolderFileChooser= new JFileChooser();
		downloadFolderFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		downloadFolderFileChooser.setAcceptAllFileFilterUsed(false);
		downloadFolderFileChooser.setDialogTitle("Choose a folder");
		
		templateFolderFileChooser= new JFileChooser();
		templateFolderFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		templateFolderFileChooser.setAcceptAllFileFilterUsed(false);
		templateFolderFileChooser.setDialogTitle("Choose a folder");
		
		setValues();
	}

	protected void processActionResetSeededUsers() {
		int response = JOptionPane.showConfirmDialog(null, 
				"<html><body>This will reset the seeded users definition to the default values.<br/>You will lose your current definition.<br/><br/>"+
						"<b>Are you sure to continue with the reset?</b>", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			seededUsersTextArea.setText(ModelUtils.ORACLE_DEFAULT_SEEDED_USER_NAMES);
		}
	}

	protected void processActionClickXMLFormat() {
		try{
			XMLFormatInformationDialog xmlFormatInformationDialog=new XMLFormatInformationDialog(mainPanel.getSnapshotMain());
			xmlFormatInformationDialog.setVisible(true);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}
	
	protected void processActionClickExcelFormat() {
		ExcelFormatInformationDialog excelFormatInformationDialog=new ExcelFormatInformationDialog(mainPanel.getSnapshotMain());
		excelFormatInformationDialog.setVisible(true);
	}

	protected void processActionDownloadFolder() {
		try{	
			int returnVal = downloadFolderFileChooser.showOpenDialog(mainPanel.getSnapshotMain().getRootFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				downloadFolder=downloadFolderFileChooser.getSelectedFile();
				try{
					if(downloadFolder.exists()){
						downloadFolderPathLabel.setText(downloadFolder.getAbsolutePath());
					}else{
						throw new Exception("A folder you specified does not exist in the file system! please select another one.");
					}					
				}catch(Exception e2){
					GUIUtils.popupErrorMessage(e2.getMessage());
					processActionDownloadFolder();	
				}
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}
	
	protected void processActionTemplateFolder() {
		try{	
			int returnVal = templateFolderFileChooser.showOpenDialog(mainPanel.getSnapshotMain().getRootFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				templateFolder=templateFolderFileChooser.getSelectedFile();
				try{
					if(templateFolder.exists()){
						templateFolderPathLabel.setText(templateFolder.getAbsolutePath());
					}else{
						throw new Exception("A folder you specified does not exist in the file system! please select another one.");
					}					
				}catch(Exception e2){
					GUIUtils.popupErrorMessage(e2.getMessage());
					processActionTemplateFolder();	
				}	
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}

	protected void processActionPackageDefaultServerFolder() {
		try{	
			int returnVal = defaultServerInformationFolderFileChooser.showOpenDialog(mainPanel.getSnapshotMain().getRootFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				serverConnectionsFolder=defaultServerInformationFolderFileChooser.getSelectedFile();
				try{
					if(serverConnectionsFolder.exists()){
						defaultServerInformationFolderPathLabel.setText(serverConnectionsFolder.getAbsolutePath());
					}else{
						throw new Exception("A folder you specified does not exist in the file system! please select another one.");
					}					
				}catch(Exception e2){
					GUIUtils.popupErrorMessage(e2.getMessage());
					processActionPackageDefaultServerFolder();	
				}	
		
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}
	protected void processActionShowClearSoftDeletedOption(boolean visible) {
		try{
			clearSoftDeletedSnapshotsCheckbox.setVisible(visible);
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}

	public JTextField getWorkersTextField() {
		return workersTextField;
	}

	public JTextField getDefaultSnapshotNameTextField() {
		return defaultSnapshotNameTextField;
	}
	
	public File getServerConnectionsFolder() {
		return serverConnectionsFolder;
	}
	
	public File getDownloadFolder() {
		return downloadFolder;
	}
	
	public boolean isExcelFormat() {
		return excelFormatRadioButton.isSelected();
	}
	
	public boolean isShowUnsupportedInventories() {
		return showUnsupportedInventoriesCheckbox.isSelected();
	}
	
	public boolean isShowTotalDetails() {
		return showTotalsDetailsCheckbox.isSelected();
	}
	
	public String getOracleSeededUserNames() {
		return seededUsersTextArea.getText().trim();
	}

	public void setEnabledOnComponentsToConnectFirst(boolean isEnabled) {
		seededUsersTextArea.setEnabled(isEnabled);
	}
	
	public boolean isPhysicalDelete() {
		return physicalDeleteRadioButton.isSelected();
	}
	public boolean isClearOutSoftDeleteSnapshot(){
		return clearSoftDeletedSnapshotsCheckbox.isSelected();
	}
/*	public void processActionSaveOptions(){
		Preferences pref = Preferences.userRoot();
		UserInformation selectedUser = mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getSelectedUser();
		String userLoginName = selectedUser.getLoginName();
		Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(mainPanel.getTabSnapshotsPanel());
		String schemaName = ModelUtils.getDBUserName(snapshotEnvironmentProperties);
		
		
		String prefName
		pref.put(PREFERENCE_WORKERS_COUNT,workersTextField.getText());
		pref.put(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION,serverConnectionsFolder.getAbsolutePath());
		pref.put(PREFERENCE_DOWNLOAD_FOLDER_LOCATION,downloadFolder.getAbsolutePath());
		pref.put(PREFERENCE_SNAPSHOT_PREFIX_NAME,defaultSnapshotNameTextField.getText());
		String value=EXCEL_RADIO_BUTTON_TEXT;
		if (!isExcelFormat()) {
			value=XML_RADIO_BUTTON_TEXT;
		}
		pref.put(PREFERENCE_DOWNLOAD_FORMAT,value);
		
		value = PHYSICAL_DELETE_BUTTON_TEXT;
		if(!isPhysicalDelete()){
			value = SOFT_DELETE_RADIO_BUTTON_TEXT;
		}
		pref.put(PREFERENCE_SNAPSHOT_DELETE_OPTION,value);
		
		pref.putBoolean(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,isShowUnsupportedInventories());
		pref.put(PREFERENCE_SEEDED_USERS_DEFINITION,seededUsersTextArea.getText());
		pref.putBoolean(PREFERENCE_DISPLAY_TOTAL_DETAILS,isShowTotalDetails());
		pref.putBoolean(PREFERENCE_SHOW_HELPER_BALLOONS,showHelpCheckbox.isSelected());
		pref.putBoolean(PREFERENCE_REPORT_VERTICAL_WAY,reportsVerticalWayCheckbox.isSelected());
		pref.put(PREFERENCE_TEMPLATE_FOLDER_LOCATION,templateFolder.getAbsolutePath());
		pref.putBoolean(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION,clearSoftDeletedSnapshotsCheckbox.isSelected());
		
	}*/
	
/*	public void processActionResetOptions()
	{
		int response = JOptionPane.showConfirmDialog(null, 
				"<html><body>This will reset all the options to their default values.<br/>You will lose your current setting.<br/><br/>"+
						"<b>Are you sure to continue with the reset?</b>", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
			return;
		}
		
		Preferences pref = Preferences.userRoot();
		
		pref.remove(PREFERENCE_WORKERS_COUNT);
		pref.remove(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION);
		pref.remove(PREFERENCE_DOWNLOAD_FOLDER_LOCATION);
		pref.remove(PREFERENCE_SNAPSHOT_PREFIX_NAME);
		pref.remove(PREFERENCE_DOWNLOAD_FORMAT);
		pref.remove(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES);
		pref.remove(PREFERENCE_SEEDED_USERS_DEFINITION);
		pref.remove(PREFERENCE_DISPLAY_TOTAL_DETAILS);
		pref.remove(PREFERENCE_SHOW_HELPER_BALLOONS);
		pref.remove(PREFERENCE_REPORT_VERTICAL_WAY);
		pref.remove(PREFERENCE_TEMPLATE_FOLDER_LOCATION);
		pref.remove(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
		pref.remove(PREFERENCE_SNAPSHOT_DELETE_OPTION);
		
		setValues();
	}*/

	public boolean isShowBalloons() {
		return showHelpCheckbox.isSelected();
	}
	
	public boolean isReportsVerticalWay() {
		return reportsVerticalWayCheckbox.isSelected();
	}	

	public JTextArea getSeededUsersTextArea() {
		return seededUsersTextArea;
	}

	public JCheckBox getShowTotalsDetailsCheckbox() {
		return showTotalsDetailsCheckbox;
	}

	public JButton getSaveOptions() {
		return saveOptions;
	}
	
	public File getTemplateFolder() {
		return templateFolder;
	}	
	
	public void setEnableOnWorkersTextFiled(boolean isEnabled){
		workersTextField.setEditable(isEnabled);
	} 
	public void setEnableOnDefaultServerInformationFolderButton(boolean isEnabled){
		defaultServerInformationFolderButton.setEnabled(isEnabled);
	} 	
	public void setEnableOnDownloadFolderButton(boolean isEnabled){
		downloadFolderButton.setEnabled(isEnabled);
	} 
	public void setEnableOnTemplateFolderButton(boolean isEnabled){
		templateFolderButton.setEnabled(isEnabled);
	} 
	public void setEnableOnDefaultSnapshotNameTextField(boolean isEnabled){
		defaultSnapshotNameTextField.setEditable(isEnabled);
	} 
	public void setEnableOnResetOptionsButton(boolean isEnabled){
		resetOptions.setEnabled(isEnabled);
	} 	
	public void setEnableOnDeleteOption(boolean isEnabled){
		softDeleteRadioButton.setEnabled(isEnabled);
		physicalDeleteRadioButton.setEnabled(isEnabled);
		clearSoftDeletedSnapshotsCheckbox.setEnabled(isEnabled);
	} 			
	public void setEnableOnShowTotalsDetailsCheckbox(boolean isEnabled){
		if(mainPanel.getSnapshotMain().isAlreadyClickedNewSnapshot() && isEnabled){
			//do nothing
		}else{
			showTotalsDetailsCheckbox.setEnabled(isEnabled);
		}
	}
	public String getPreferenceName(String defaultPrefName, String userId, String schemaName,String shortPrefernceName){
		/*Preference name : 
		 * 1. SCHEMA_USERNAME_PREFNAME => for a schema which has many users.
		 * 2. SCHEMA_PREFNAME => for a schema which has only one user, as a default for a schema (in case, it's a new schema)
		 * 3. PREFNAME => default value for a schema
		 * 
		 * IF 
		 * 
		 * 
		 * 
		 * */
		String toReturn = defaultPrefName;
		if(defaultPrefName!=null && userId!="" && !DEFAULT_USERLOGIN_NAME.equals(userId)//SCHEMA_USERID_PREFNAME 
				&& schemaName!="" && !DEFAULT_SCHEMA_NAME.equals(schemaName)){
			toReturn = schemaName.toUpperCase()+"_"+userId.toUpperCase()+"_"+defaultPrefName;
			if(toReturn.length()>PREFERENCE_NAME_MAXIMUM_LENGTH){
				toReturn = schemaName.toUpperCase()+"_"+userId.toUpperCase()+"_"+shortPrefernceName;
			}
		}else if(defaultPrefName!=null && userId!="" && DEFAULT_USERLOGIN_NAME.equals(userId)//SCHEMA_PREFNAME 
				&& schemaName!="" && !DEFAULT_SCHEMA_NAME.equals(schemaName)){
			toReturn = schemaName.toUpperCase()+"_"+defaultPrefName;
			if(toReturn.length()>PREFERENCE_NAME_MAXIMUM_LENGTH){
				toReturn = schemaName.toUpperCase()+"_"+shortPrefernceName;
			}
		}
		return toReturn;	
	}
	public String getUserID() throws Exception{
		String toReturn  = "";
		List<UserInformation> userInformationList = ModelUtils.getSnapshotUserInformationList(ModelUtils.getSnapshotEnvironmentProperties(mainPanel.getTabSnapshotsPanel()));
		if(!userInformationList.isEmpty()){
			UserInformation selectedUser = mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getSelectedUser();
			toReturn = String.valueOf(selectedUser.getId());
		}else{
			toReturn = DEFAULT_USERLOGIN_NAME;
		}
		return toReturn;
	}
	public String getPreferenceNameIfItExists(String preferenceName, String userId, String schemaName, boolean isResetToDefaultMode,String shortPreferenceName ) throws Exception{
		Preferences pref = Preferences.userRoot();
		String toReturn = "";
		if(!isResetToDefaultMode){
			toReturn = preferenceName;
			String prefName = getPreferenceName(preferenceName,userId,schemaName,shortPreferenceName);
			if(pref.get(prefName, null) != null){
				toReturn = prefName;
			}else{
				prefName = getPreferenceName(preferenceName,DEFAULT_USERLOGIN_NAME,schemaName,shortPreferenceName);
				if(pref.get(prefName, null) != null){
					toReturn = prefName;
				}	
			}
		}else{
			toReturn = DEFAULT_PREFERENCE_NAME;
		}
		return toReturn;
	}
	
	public void setValues() {
		setValues(false);
	}
	/*public void setValues(boolean isResetToDefaultMode) {
		try{
			//in case do not have loginName_schemaName_prefName in pref file, it takes value from default value
			//but want the value from prefName
			//priority will be 1.loginName_schemaName_prefName 2.prefName 3.default value
			
			//reset to default mode, we will take a value from default value.
			
			String schemaName =DEFAULT_SCHEMA_NAME;
			String userId = DEFAULT_USERLOGIN_NAME;
			String prefName = "";
			boolean isPrefOverrideExist = false;
			try{
				if(mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().isConnected()){
					Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(mainPanel.getTabSnapshotsPanel());
					schemaName = ModelUtils.getDBUserName(snapshotEnvironmentProperties);
					userId = getUserID();
				}
			}catch(Exception e3){
				FileUtils.println("Cannot get the schema name and user login name, error : "+e3.getMessage());
				FileUtils.printStackTrace(e3);			
			}
			
			Preferences pref = Preferences.userRoot();
			File userhomeFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
			
			UserInformation currentUserInfo =  mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getSelectedUser();
			if(currentUserInfo!=null){
				isPrefOverrideExist = currentUserInfo.isPrefOverrideExist();
			}

			//WORKER COUNT
			prefName = getPreferenceNameIfItExists(PREFERENCE_WORKERS_COUNT,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_WORKERS_COUNT);
			String value= pref.get(prefName,DEFAULT_WORKERS_COUNT_VALUE);
			workersTextField.setText(value);	
			
			
			//SERVER CONNECTION
			File defaultServerConnectionFolder=new File(userhomeFolder,DEFAULT_SERVER_CONNECTIONS_FOLDER_NAME);
			defaultServerConnectionFolder.mkdirs();
			prefName = getPreferenceNameIfItExists(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION);
			value = pref.get(prefName,defaultServerConnectionFolder.getAbsolutePath());
			serverConnectionsFolder=new File(value);
			serverConnectionsFolder.mkdirs();
			defaultServerInformationFolderFileChooser.setCurrentDirectory(serverConnectionsFolder);
			defaultServerInformationFolderPathLabel.setText(serverConnectionsFolder.getAbsolutePath());
			
			//DOWNLOAD FOLDER
			File defaultDownloadFolder=new File(userhomeFolder,DEFAULT_DOWNLOAD_FOLDER_NAME);
			defaultDownloadFolder.mkdirs();
			prefName = getPreferenceNameIfItExists(PREFERENCE_DOWNLOAD_FOLDER_LOCATION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_DOWNLOAD_FOLDER_LOCATION);
			value = pref.get(prefName,defaultDownloadFolder.getAbsolutePath());
			downloadFolder=new File(value);
			downloadFolder.mkdirs();
			downloadFolderFileChooser.setCurrentDirectory(downloadFolder);
			downloadFolderPathLabel.setText(downloadFolder.getAbsolutePath());
			
			//TEMPLATE FOLDER
			File defaultTemplateFolder=new File(userhomeFolder,DEFAULT_TEMPLATE_FOLDER_NAME);
			defaultTemplateFolder.mkdirs();
			prefName = getPreferenceNameIfItExists(PREFERENCE_TEMPLATE_FOLDER_LOCATION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_TEMPLATE_FOLDER_LOCATION);
			value = pref.get(prefName,defaultTemplateFolder.getAbsolutePath());
			templateFolder=new File(value);
			templateFolder.mkdirs();
			templateFolderFileChooser.setCurrentDirectory(templateFolder);
			templateFolderPathLabel.setText(templateFolder.getAbsolutePath());
			
			//SNAPSHOT PREFIX NAME
			prefName = getPreferenceNameIfItExists(PREFERENCE_SNAPSHOT_PREFIX_NAME,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SNAPSHOT_PREFIX_NAME);
			String str= pref.get(prefName,DEFAULT_SNAPSHOT_PREFIX_NAME);
			defaultSnapshotNameTextField.setText(str);
			
			//DOWNLOAD FORMAT
			prefName = getPreferenceNameIfItExists(PREFERENCE_DOWNLOAD_FORMAT,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_DOWNLOAD_FORMAT);
			str= pref.get(prefName,DEFAULT_DOWNLOAD_FORMAT);
			if (str.equalsIgnoreCase(DEFAULT_DOWNLOAD_FORMAT)) {
				excelFormatRadioButton.setSelected(true);
				xmlFormatRadioButton.setSelected(false);
			}
			else {
				excelFormatRadioButton.setSelected(false);
				xmlFormatRadioButton.setSelected(true);
			}
			
			//CLEAR OUT SNAPSHOT
			prefName = getPreferenceNameIfItExists(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
			boolean isCheckClearOutCheckBox= pref.getBoolean(prefName,DEFAULT_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
			clearSoftDeletedSnapshotsCheckbox.setSelected(isCheckClearOutCheckBox);	
			
			//DELETE OPTION
			prefName = getPreferenceNameIfItExists(PREFERENCE_SNAPSHOT_DELETE_OPTION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SNAPSHOT_DELETE_OPTION);
			str= pref.get(prefName,DEFAULT_SNAPSHOT_DELETE_OPTION);
			if (str.equalsIgnoreCase(DEFAULT_SNAPSHOT_DELETE_OPTION)) {
				softDeleteRadioButton.setSelected(true);
				physicalDeleteRadioButton.setSelected(false);
				clearSoftDeletedSnapshotsCheckbox.setVisible(false);
			}else {
				softDeleteRadioButton.setSelected(false);
				physicalDeleteRadioButton.setSelected(true);
				clearSoftDeletedSnapshotsCheckbox.setVisible(true);
			}
			
			prefName = getPreferenceNameIfItExists(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES);
			boolean b= pref.getBoolean(prefName,DEFAULT_DISPLAY_UNSUPPORTED_INVENTORIES);
			showUnsupportedInventoriesCheckbox.setSelected(b);

			prefName = getPreferenceNameIfItExists(PREFERENCE_DISPLAY_TOTAL_DETAILS,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_DISPLAY_TOTAL_DETAILS);
			b= pref.getBoolean(prefName,DEFAULT_DISPLAY_TOTAL_DETAILS);
			showTotalsDetailsCheckbox.setSelected(b);

			prefName = getPreferenceNameIfItExists(PREFERENCE_SHOW_HELPER_BALLOONS,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SHOW_HELPER_BALLOONS);
			b= pref.getBoolean(prefName,DEFAULT_SHOW_HELPER_BALLOONS);
			showHelpCheckbox.setSelected(b);
			UIUtils.allBallonTipsSetVisable(b);

			prefName = getPreferenceNameIfItExists(PREFERENCE_REPORT_VERTICAL_WAY,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_REPORT_VERTICAL_WAY);
			b= pref.getBoolean(prefName,DEFAULT_REPORT_VERTICAL_WAY);
			reportsVerticalWayCheckbox.setSelected(b);

			prefName = getPreferenceNameIfItExists(PREFERENCE_SEEDED_USERS_DEFINITION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SEEDED_USERS_DEFINITION);
			str= pref.get(prefName,ModelUtils.ORACLE_DEFAULT_SEEDED_USER_NAMES);
			seededUsersTextArea.setText(str);			
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
	}*/
/*	public void processActionSaveOptions(){
		try{
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(mainPanel.getTabSnapshotsPanel());
			String schemaName = ModelUtils.getDBUserName(snapshotEnvironmentProperties);
			String userId = getUserID();
			Preferences pref = Preferences.userRoot();
			
			
			String prefName = getPreferenceName(PREFERENCE_WORKERS_COUNT,userId,schemaName,SHORT_PREFERENCE_WORKERS_COUNT);
			pref.put(prefName,workersTextField.getText());
			
			prefName = getPreferenceName(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION,userId,schemaName,SHORT_PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION);
			pref.put(prefName,serverConnectionsFolder.getAbsolutePath());
			
			prefName = getPreferenceName(PREFERENCE_DOWNLOAD_FOLDER_LOCATION,userId,schemaName,SHORT_PREFERENCE_DOWNLOAD_FOLDER_LOCATION);
			pref.put(prefName,downloadFolder.getAbsolutePath());
			
			
			prefName = getPreferenceName(PREFERENCE_SNAPSHOT_PREFIX_NAME,userId,schemaName,SHORT_PREFERENCE_SNAPSHOT_PREFIX_NAME);
			pref.put(prefName,defaultSnapshotNameTextField.getText());
			
			String value=EXCEL_RADIO_BUTTON_TEXT;
			if (!isExcelFormat()) {
				value=XML_RADIO_BUTTON_TEXT;
			}
			prefName = getPreferenceName(PREFERENCE_DOWNLOAD_FORMAT,userId,schemaName,SHORT_PREFERENCE_DOWNLOAD_FORMAT);
			
			pref.put(prefName,value);
			value = PHYSICAL_DELETE_BUTTON_TEXT;
			if(!isPhysicalDelete()){
				value = SOFT_DELETE_RADIO_BUTTON_TEXT;
			}
			prefName = getPreferenceName(PREFERENCE_SNAPSHOT_DELETE_OPTION,userId,schemaName,SHORT_PREFERENCE_SNAPSHOT_DELETE_OPTION);
			pref.put(prefName,value);
			
			prefName = getPreferenceName(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,userId,schemaName,SHORT_PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES);
			pref.putBoolean(prefName,isShowUnsupportedInventories());
			
			prefName = getPreferenceName(PREFERENCE_SEEDED_USERS_DEFINITION,userId,schemaName,SHORT_PREFERENCE_SEEDED_USERS_DEFINITION);
			pref.put(prefName,seededUsersTextArea.getText());
			
			prefName = getPreferenceName(PREFERENCE_DISPLAY_TOTAL_DETAILS,userId,schemaName,SHORT_PREFERENCE_DISPLAY_TOTAL_DETAILS);
			pref.putBoolean(prefName,isShowTotalDetails());

			prefName = getPreferenceName(PREFERENCE_SHOW_HELPER_BALLOONS,userId,schemaName,SHORT_PREFERENCE_SHOW_HELPER_BALLOONS);
			pref.putBoolean(prefName,showHelpCheckbox.isSelected());
			
			prefName = getPreferenceName(PREFERENCE_REPORT_VERTICAL_WAY,userId,schemaName,SHORT_PREFERENCE_REPORT_VERTICAL_WAY);
			pref.putBoolean(prefName,reportsVerticalWayCheckbox.isSelected());
			
			prefName = getPreferenceName(PREFERENCE_TEMPLATE_FOLDER_LOCATION,userId,schemaName,SHORT_PREFERENCE_TEMPLATE_FOLDER_LOCATION);
			pref.put(prefName,templateFolder.getAbsolutePath());
			
			prefName = getPreferenceName(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION,userId,schemaName,SHORT_PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
			pref.putBoolean(prefName,clearSoftDeletedSnapshotsCheckbox.isSelected());
			
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
		
	}*/
	
	
	
	public void processActionResetOptions(){
		try{
			if(isOptionsFromOrToUserInformation()){
				processActionResetOptionsToUserInformation();
			}else{
				processActionResetOptionsToPreferenceFile();
			}
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
	}
	
	public void processActionResetOptionsToUserInformation(){
		//Reset option to default
		try{
			UserInformation currentUserInfo =  mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getSelectedUser();
			Connection connection = null;
			File userhomeFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
			if(currentUserInfo!=null){
				currentUserInfo.setPrefDefaultParallelWorkers(Integer.parseInt(DEFAULT_WORKERS_COUNT_VALUE));
				File defaultServerConnectionFolder=new File(userhomeFolder,DEFAULT_SERVER_CONNECTIONS_FOLDER_NAME);
				currentUserInfo.setPrefServerConnectionFolderLocation(defaultServerConnectionFolder.getAbsolutePath());
				File defaultDownloadFolder=new File(userhomeFolder,DEFAULT_DOWNLOAD_FOLDER_NAME);
				currentUserInfo.setPrefDownloadFolderLocation(defaultDownloadFolder.getAbsolutePath());
				File defaultTemplateFolder=new File(userhomeFolder,DEFAULT_TEMPLATE_FOLDER_NAME);
				currentUserInfo.setPrefTemplateFolderLocation(defaultTemplateFolder.getAbsolutePath());
				currentUserInfo.setPrefSnapshotPrefixName(DEFAULT_SNAPSHOT_PREFIX_NAME);
				currentUserInfo.setPrefDownloadFormat(DEFAULT_DOWNLOAD_FORMAT);
				currentUserInfo.setPrefDeleteOption(DEFAULT_SNAPSHOT_DELETE_OPTION);
				currentUserInfo.setPrefIncludeSoftDeletedSnapshot(DEFAULT_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
				currentUserInfo.setPrefDisplayUnsupportedInvOption(DEFAULT_DISPLAY_UNSUPPORTED_INVENTORIES);
				currentUserInfo.setPrefDisplayTotalDetailOption(DEFAULT_DISPLAY_TOTAL_DETAILS);
				currentUserInfo.setPrefDisplayHelperBalloon(DEFAULT_SHOW_HELPER_BALLOONS);
				currentUserInfo.setPrefDisplayBR100(DEFAULT_REPORT_VERTICAL_WAY);
				currentUserInfo.setPrefSeededUsersInfoOption(ModelUtils.ORACLE_DEFAULT_SEEDED_USER_NAMES);
				Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(mainPanel.getTabSnapshotsPanel());
				connection=DatabaseUtils.getJDBCConnectionGeneric(
						ModelUtils.getJDBCString(snapshotEnvironmentProperties),
						ModelUtils.getDBUserName(snapshotEnvironmentProperties),
						ModelUtils.getDBPassword(snapshotEnvironmentProperties),true);
				connection.setAutoCommit(false);
				ModelUtils.updateUser(connection,currentUserInfo);
				connection.commit();
				setValues(true);
			}
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
		
		
	}
	
	public void processActionResetOptionsToPreferenceFile(){
		int response = JOptionPane.showConfirmDialog(null, 
				"<html><body>This will reset all the options to their default values.<br/>You will lose your current setting.<br/><br/>"+
						"<b>Are you sure to continue with the reset?</b>", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
			return;
		}
		try {
			Preferences pref = Preferences.userRoot();
			pref.remove(PREFERENCE_WORKERS_COUNT);
			pref.remove(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION);
			pref.remove(PREFERENCE_DOWNLOAD_FOLDER_LOCATION);
			pref.remove(PREFERENCE_SNAPSHOT_PREFIX_NAME);
			pref.remove(PREFERENCE_DOWNLOAD_FORMAT);
			pref.remove(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES);	
			pref.remove(PREFERENCE_SEEDED_USERS_DEFINITION);
			pref.remove(PREFERENCE_DISPLAY_TOTAL_DETAILS);	
			pref.remove(PREFERENCE_SHOW_HELPER_BALLOONS);
			pref.remove(PREFERENCE_REPORT_VERTICAL_WAY);
			pref.remove(PREFERENCE_TEMPLATE_FOLDER_LOCATION);	
			pref.remove(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
			pref.remove(PREFERENCE_SNAPSHOT_DELETE_OPTION);	
			setValues(true);
		} catch (Exception e) {
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
	}
	public void setValues(boolean isResetToDefaultMode) {
		try{
			if(isOptionsFromOrToUserInformation()){
				setValuesByUsingUserInformation();
			}else{
				setValuesByPreferenceFile();
			}
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
	}
	
	public void setValuesByUsingUserInformation() {
		try{
			String value = "";
			String str = "";
			Preferences pref = Preferences.userRoot();
			File userhomeFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
			UserInformation currentUserInfo =  mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getSelectedUser();
			
			//WORKER COUNT
			try{
				value = String.valueOf(currentUserInfo.getPrefDefaultParallelWorkers());
			}catch(Exception e1){
				value = pref.get(PREFERENCE_WORKERS_COUNT,DEFAULT_WORKERS_COUNT_VALUE);
			}
			workersTextField.setText(value);	
			
			//SERVER CONNECTION
			File defaultServerConnectionFolder=new File(userhomeFolder,DEFAULT_SERVER_CONNECTIONS_FOLDER_NAME);
			defaultServerConnectionFolder.mkdirs();
			if(currentUserInfo.getPrefServerConnectionFolderLocation()!=null && currentUserInfo.getPrefServerConnectionFolderLocation()!=""){
				value = currentUserInfo.getPrefServerConnectionFolderLocation();
			}else{
				value = pref.get(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION,defaultServerConnectionFolder.getAbsolutePath());
			}
			serverConnectionsFolder=new File(value);
			serverConnectionsFolder.mkdirs();
			defaultServerInformationFolderFileChooser.setCurrentDirectory(serverConnectionsFolder);
			defaultServerInformationFolderPathLabel.setText(serverConnectionsFolder.getAbsolutePath());
			
			//DOWNLOAD FOLDER
			File defaultDownloadFolder=new File(userhomeFolder,DEFAULT_DOWNLOAD_FOLDER_NAME);
			defaultDownloadFolder.mkdirs();
			
			if(currentUserInfo.getPrefDownloadFolderLocation()!=null && currentUserInfo.getPrefDownloadFolderLocation()!=""){
				value = currentUserInfo.getPrefDownloadFolderLocation();
			}else{
				value = pref.get(PREFERENCE_DOWNLOAD_FOLDER_LOCATION,defaultDownloadFolder.getAbsolutePath());
			}
			downloadFolder=new File(value);
			downloadFolder.mkdirs();
			downloadFolderFileChooser.setCurrentDirectory(downloadFolder);
			downloadFolderPathLabel.setText(downloadFolder.getAbsolutePath());
			
			//TEMPLATE FOLDER
			File defaultTemplateFolder=new File(userhomeFolder,DEFAULT_TEMPLATE_FOLDER_NAME);
			defaultTemplateFolder.mkdirs();
			
			if(currentUserInfo.getPrefTemplateFolderLocation()!=null && currentUserInfo.getPrefTemplateFolderLocation()!=""){
				value = currentUserInfo.getPrefTemplateFolderLocation();
			}else{
				value = pref.get(PREFERENCE_TEMPLATE_FOLDER_LOCATION,defaultTemplateFolder.getAbsolutePath());
			}
			templateFolder=new File(value);
			templateFolder.mkdirs();
			templateFolderFileChooser.setCurrentDirectory(templateFolder);
			templateFolderPathLabel.setText(templateFolder.getAbsolutePath());
			
			
			//SNAPSHOT PREFIX NAME
			if(currentUserInfo.getPrefSnapshotPrefixName()!=null && currentUserInfo.getPrefSnapshotPrefixName()!=""){
				str = currentUserInfo.getPrefSnapshotPrefixName();
			}else{
				str= pref.get(PREFERENCE_SNAPSHOT_PREFIX_NAME,DEFAULT_SNAPSHOT_PREFIX_NAME);
			}
			defaultSnapshotNameTextField.setText(str);
			
			
			//DOWNLOAD FORMAT
			if(currentUserInfo.getPrefDownloadFormat()!=null && currentUserInfo.getPrefDownloadFormat()!=""){
				str = currentUserInfo.getPrefDownloadFormat();
			}else{
				str= pref.get(PREFERENCE_DOWNLOAD_FORMAT,DEFAULT_DOWNLOAD_FORMAT);
			}
			if (str.equalsIgnoreCase(DEFAULT_DOWNLOAD_FORMAT)) {
				excelFormatRadioButton.setSelected(true);
				xmlFormatRadioButton.setSelected(false);
			}
			else {
				excelFormatRadioButton.setSelected(false);
				xmlFormatRadioButton.setSelected(true);
			}
			
			//DELETE OPTION
			boolean isCheckClearOutCheckBox = false;
			if(currentUserInfo.getPrefDeleteOption()!=null && currentUserInfo.getPrefDeleteOption()!=""){
				str = currentUserInfo.getPrefDeleteOption();
				isCheckClearOutCheckBox = currentUserInfo.isPrefIncludeSoftDeletedSnapshot();
			}else{
				str= pref.get(PREFERENCE_SNAPSHOT_DELETE_OPTION,DEFAULT_SNAPSHOT_DELETE_OPTION);
				isCheckClearOutCheckBox= pref.getBoolean(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION,DEFAULT_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
			}
			if (str.equalsIgnoreCase(DEFAULT_SNAPSHOT_DELETE_OPTION)) {
				softDeleteRadioButton.setSelected(true);
				physicalDeleteRadioButton.setSelected(false);
				clearSoftDeletedSnapshotsCheckbox.setVisible(false);
			}else {
				softDeleteRadioButton.setSelected(false);
				physicalDeleteRadioButton.setSelected(true);
				clearSoftDeletedSnapshotsCheckbox.setVisible(true);
			}
			clearSoftDeletedSnapshotsCheckbox.setSelected(isCheckClearOutCheckBox);	

			//DISPLAY UNSUPPORTED INVENTORIES
			if(currentUserInfo.getPrefSnapshotPrefixName()!=null && currentUserInfo.getPrefSnapshotPrefixName()!=""){
				showUnsupportedInventoriesCheckbox.setSelected(currentUserInfo.isPrefDisplayUnsupportedInvOption());
				showTotalsDetailsCheckbox.setSelected(currentUserInfo.isPrefDisplayTotalDetailOption());
				showHelpCheckbox.setSelected(currentUserInfo.isPrefDisplayHelperBalloon());
				UIUtils.allBallonTipsSetVisable(currentUserInfo.isPrefDisplayHelperBalloon());
				reportsVerticalWayCheckbox.setSelected(currentUserInfo.isPrefDisplayBR100());
			}else{
				boolean b= pref.getBoolean(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,DEFAULT_DISPLAY_UNSUPPORTED_INVENTORIES);
				showUnsupportedInventoriesCheckbox.setSelected(b);

				b= pref.getBoolean(PREFERENCE_DISPLAY_TOTAL_DETAILS,DEFAULT_DISPLAY_TOTAL_DETAILS);
				showTotalsDetailsCheckbox.setSelected(b);

				b= pref.getBoolean(PREFERENCE_SHOW_HELPER_BALLOONS,DEFAULT_SHOW_HELPER_BALLOONS);
				showHelpCheckbox.setSelected(b);
				UIUtils.allBallonTipsSetVisable(b);

				b= pref.getBoolean(PREFERENCE_REPORT_VERTICAL_WAY,DEFAULT_REPORT_VERTICAL_WAY);
				reportsVerticalWayCheckbox.setSelected(b);
				
			}

			
			if(currentUserInfo.getPrefSeededUsersInfoOption()!=null){
				str = currentUserInfo.getPrefSeededUsersInfoOption();
			}else{
				str= pref.get(PREFERENCE_SEEDED_USERS_DEFINITION,ModelUtils.ORACLE_DEFAULT_SEEDED_USER_NAMES);
			}
			seededUsersTextArea.setText(str);			
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
	}
	
	/*public void setValuesByPreferenceFile(boolean isResetToDefaultMode) {
		try{
			//in case do not have loginName_schemaName_prefName in pref file, it takes value from default value
			//but want the value from prefName
			//priority will be 1.loginName_schemaName_prefName 2.prefName 3.default value
			
			//reset to default mode, we will take a value from default value.
			
			String schemaName =DEFAULT_SCHEMA_NAME;
			String userId = DEFAULT_USERLOGIN_NAME;
			String prefName = "";
			boolean isPrefOverrideExist = false;
			try{
				if(mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().isConnected()){
					Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(mainPanel.getTabSnapshotsPanel());
					schemaName = ModelUtils.getDBUserName(snapshotEnvironmentProperties);
					userId = getUserID();
				}
			}catch(Exception e3){
				FileUtils.println("Cannot get the schema name and user login name, error : "+e3.getMessage());
				FileUtils.printStackTrace(e3);			
			}
			
			Preferences pref = Preferences.userRoot();
			File userhomeFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
			
			UserInformation currentUserInfo =  mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getSelectedUser();
			if(currentUserInfo!=null){
				isPrefOverrideExist = currentUserInfo.isPrefOverrideExist();
			}

			//WORKER COUNT
			prefName = getPreferenceNameIfItExists(PREFERENCE_WORKERS_COUNT,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_WORKERS_COUNT);
			String value= pref.get(prefName,DEFAULT_WORKERS_COUNT_VALUE);
			workersTextField.setText(value);	
			
			
			//SERVER CONNECTION
			File defaultServerConnectionFolder=new File(userhomeFolder,DEFAULT_SERVER_CONNECTIONS_FOLDER_NAME);
			defaultServerConnectionFolder.mkdirs();
			prefName = getPreferenceNameIfItExists(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION);
			value = pref.get(prefName,defaultServerConnectionFolder.getAbsolutePath());
			serverConnectionsFolder=new File(value);
			serverConnectionsFolder.mkdirs();
			defaultServerInformationFolderFileChooser.setCurrentDirectory(serverConnectionsFolder);
			defaultServerInformationFolderPathLabel.setText(serverConnectionsFolder.getAbsolutePath());
			
			//DOWNLOAD FOLDER
			File defaultDownloadFolder=new File(userhomeFolder,DEFAULT_DOWNLOAD_FOLDER_NAME);
			defaultDownloadFolder.mkdirs();
			prefName = getPreferenceNameIfItExists(PREFERENCE_DOWNLOAD_FOLDER_LOCATION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_DOWNLOAD_FOLDER_LOCATION);
			value = pref.get(prefName,defaultDownloadFolder.getAbsolutePath());
			downloadFolder=new File(value);
			downloadFolder.mkdirs();
			downloadFolderFileChooser.setCurrentDirectory(downloadFolder);
			downloadFolderPathLabel.setText(downloadFolder.getAbsolutePath());
			
			//TEMPLATE FOLDER
			File defaultTemplateFolder=new File(userhomeFolder,DEFAULT_TEMPLATE_FOLDER_NAME);
			defaultTemplateFolder.mkdirs();
			prefName = getPreferenceNameIfItExists(PREFERENCE_TEMPLATE_FOLDER_LOCATION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_TEMPLATE_FOLDER_LOCATION);
			value = pref.get(prefName,defaultTemplateFolder.getAbsolutePath());
			templateFolder=new File(value);
			templateFolder.mkdirs();
			templateFolderFileChooser.setCurrentDirectory(templateFolder);
			templateFolderPathLabel.setText(templateFolder.getAbsolutePath());
			
			//SNAPSHOT PREFIX NAME
			prefName = getPreferenceNameIfItExists(PREFERENCE_SNAPSHOT_PREFIX_NAME,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SNAPSHOT_PREFIX_NAME);
			String str= pref.get(prefName,DEFAULT_SNAPSHOT_PREFIX_NAME);
			defaultSnapshotNameTextField.setText(str);
			
			//DOWNLOAD FORMAT
			prefName = getPreferenceNameIfItExists(PREFERENCE_DOWNLOAD_FORMAT,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_DOWNLOAD_FORMAT);
			str= pref.get(prefName,DEFAULT_DOWNLOAD_FORMAT);
			if (str.equalsIgnoreCase(DEFAULT_DOWNLOAD_FORMAT)) {
				excelFormatRadioButton.setSelected(true);
				xmlFormatRadioButton.setSelected(false);
			}
			else {
				excelFormatRadioButton.setSelected(false);
				xmlFormatRadioButton.setSelected(true);
			}
			
			//CLEAR OUT SNAPSHOT
			prefName = getPreferenceNameIfItExists(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
			boolean isCheckClearOutCheckBox= pref.getBoolean(prefName,DEFAULT_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
			clearSoftDeletedSnapshotsCheckbox.setSelected(isCheckClearOutCheckBox);	
			
			//DELETE OPTION
			prefName = getPreferenceNameIfItExists(PREFERENCE_SNAPSHOT_DELETE_OPTION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SNAPSHOT_DELETE_OPTION);
			str= pref.get(prefName,DEFAULT_SNAPSHOT_DELETE_OPTION);
			if (str.equalsIgnoreCase(DEFAULT_SNAPSHOT_DELETE_OPTION)) {
				softDeleteRadioButton.setSelected(true);
				physicalDeleteRadioButton.setSelected(false);
				clearSoftDeletedSnapshotsCheckbox.setVisible(false);
			}else {
				softDeleteRadioButton.setSelected(false);
				physicalDeleteRadioButton.setSelected(true);
				clearSoftDeletedSnapshotsCheckbox.setVisible(true);
			}
			
			prefName = getPreferenceNameIfItExists(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES);
			boolean b= pref.getBoolean(prefName,DEFAULT_DISPLAY_UNSUPPORTED_INVENTORIES);
			showUnsupportedInventoriesCheckbox.setSelected(b);

			prefName = getPreferenceNameIfItExists(PREFERENCE_DISPLAY_TOTAL_DETAILS,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_DISPLAY_TOTAL_DETAILS);
			b= pref.getBoolean(prefName,DEFAULT_DISPLAY_TOTAL_DETAILS);
			showTotalsDetailsCheckbox.setSelected(b);

			prefName = getPreferenceNameIfItExists(PREFERENCE_SHOW_HELPER_BALLOONS,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SHOW_HELPER_BALLOONS);
			b= pref.getBoolean(prefName,DEFAULT_SHOW_HELPER_BALLOONS);
			showHelpCheckbox.setSelected(b);
			UIUtils.allBallonTipsSetVisable(b);

			prefName = getPreferenceNameIfItExists(PREFERENCE_REPORT_VERTICAL_WAY,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_REPORT_VERTICAL_WAY);
			b= pref.getBoolean(prefName,DEFAULT_REPORT_VERTICAL_WAY);
			reportsVerticalWayCheckbox.setSelected(b);

			prefName = getPreferenceNameIfItExists(PREFERENCE_SEEDED_USERS_DEFINITION,userId,schemaName,isResetToDefaultMode,SHORT_PREFERENCE_SEEDED_USERS_DEFINITION);
			str= pref.get(prefName,ModelUtils.ORACLE_DEFAULT_SEEDED_USER_NAMES);
			seededUsersTextArea.setText(str);			
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
	}*/
	
	private void setValuesByPreferenceFile() {
		
		Preferences pref = Preferences.userRoot();
		
		File userhomeFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString());
		
		String value= pref.get(PREFERENCE_WORKERS_COUNT,DEFAULT_WORKERS_COUNT_VALUE);
		workersTextField.setText(value);	
		
		File defaultServerConnectionFolder=new File(userhomeFolder,DEFAULT_SERVER_CONNECTIONS_FOLDER_NAME);
		defaultServerConnectionFolder.mkdirs();
		
		value = pref.get(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION,defaultServerConnectionFolder.getAbsolutePath());
		serverConnectionsFolder=new File(value);
		serverConnectionsFolder.mkdirs();
		defaultServerInformationFolderFileChooser.setCurrentDirectory(serverConnectionsFolder);
		defaultServerInformationFolderPathLabel.setText(serverConnectionsFolder.getAbsolutePath());
		
		File defaultDownloadFolder=new File(userhomeFolder,DEFAULT_DOWNLOAD_FOLDER_NAME);
		defaultDownloadFolder.mkdirs();
		
		value = pref.get(PREFERENCE_DOWNLOAD_FOLDER_LOCATION,defaultDownloadFolder.getAbsolutePath());
		downloadFolder=new File(value);
		downloadFolder.mkdirs();
		downloadFolderFileChooser.setCurrentDirectory(downloadFolder);
		downloadFolderPathLabel.setText(downloadFolder.getAbsolutePath());
		
		File defaultTemplateFolder=new File(userhomeFolder,DEFAULT_TEMPLATE_FOLDER_NAME);
		defaultTemplateFolder.mkdirs();
		
		value = pref.get(PREFERENCE_TEMPLATE_FOLDER_LOCATION,defaultTemplateFolder.getAbsolutePath());
		templateFolder=new File(value);
		templateFolder.mkdirs();
		templateFolderFileChooser.setCurrentDirectory(templateFolder);
		templateFolderPathLabel.setText(templateFolder.getAbsolutePath());	
		
		String str= pref.get(PREFERENCE_SNAPSHOT_PREFIX_NAME,DEFAULT_SNAPSHOT_PREFIX_NAME);
		defaultSnapshotNameTextField.setText(str);
		
		str= pref.get(PREFERENCE_DOWNLOAD_FORMAT,DEFAULT_DOWNLOAD_FORMAT);
		if (str.equalsIgnoreCase(DEFAULT_DOWNLOAD_FORMAT)) {
			excelFormatRadioButton.setSelected(true);
			xmlFormatRadioButton.setSelected(false);
		}
		else {
			excelFormatRadioButton.setSelected(false);
			xmlFormatRadioButton.setSelected(true);
		}
		
		boolean isCheckClearOutCheckBox= pref.getBoolean(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION,DEFAULT_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION);
		clearSoftDeletedSnapshotsCheckbox.setSelected(isCheckClearOutCheckBox);	
		
		str= pref.get(PREFERENCE_SNAPSHOT_DELETE_OPTION,DEFAULT_SNAPSHOT_DELETE_OPTION);
		if (str.equalsIgnoreCase(DEFAULT_SNAPSHOT_DELETE_OPTION)) {
			softDeleteRadioButton.setSelected(true);
			physicalDeleteRadioButton.setSelected(false);
			clearSoftDeletedSnapshotsCheckbox.setVisible(false);
		}else {
			softDeleteRadioButton.setSelected(false);
			physicalDeleteRadioButton.setSelected(true);
			clearSoftDeletedSnapshotsCheckbox.setVisible(true);
		}	

		boolean b= pref.getBoolean(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,DEFAULT_DISPLAY_UNSUPPORTED_INVENTORIES);
		showUnsupportedInventoriesCheckbox.setSelected(b);

		b= pref.getBoolean(PREFERENCE_DISPLAY_TOTAL_DETAILS,DEFAULT_DISPLAY_TOTAL_DETAILS);
		showTotalsDetailsCheckbox.setSelected(b);

		b= pref.getBoolean(PREFERENCE_SHOW_HELPER_BALLOONS,DEFAULT_SHOW_HELPER_BALLOONS);
		showHelpCheckbox.setSelected(b);
		UIUtils.allBallonTipsSetVisable(b);

		b= pref.getBoolean(PREFERENCE_REPORT_VERTICAL_WAY,DEFAULT_REPORT_VERTICAL_WAY);
		reportsVerticalWayCheckbox.setSelected(b);

		str= pref.get(PREFERENCE_SEEDED_USERS_DEFINITION,ModelUtils.ORACLE_DEFAULT_SEEDED_USER_NAMES);
		seededUsersTextArea.setText(str);
		
	}
	public void processActionSaveOptions(){
		try{
			if(isOptionsFromOrToUserInformation()){
				FileUtils.println("Save options to User information");
				processActionSaveOptionsToUserInformation();
			}else{
				processActionSaveOptionsToPreferenceFile();
				FileUtils.println("Save options to Preference");
			}
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
	}
	public boolean isOptionsFromOrToUserInformation() throws Exception{
		boolean toReturn = false;
		boolean isPrefOverrideExist = false;
		try{
			if(isAtLeastOneUserExist()){
				UserInformation currentUserInfo =  mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getSelectedUser();
				if(currentUserInfo!=null){
					isPrefOverrideExist = currentUserInfo.isPrefOverrideExist();
				}
				if(isPrefOverrideExist){
					toReturn = true;
				}
			}
			return toReturn;
			
		}catch(Exception e){
			e.printStackTrace();
			String errMsg = "Unexpected error: "+e.getMessage();
			throw new Exception(errMsg);
			
		}
	}
	public void processActionSaveOptionsToUserInformation(){
		try{
			UserInformation currentUserInfo =  mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getSelectedUser();
			Connection connection = null;
			if(currentUserInfo!=null){
				String workerNo = workersTextField.getText();
				currentUserInfo.setPrefDefaultParallelWorkers(Integer.parseInt(workerNo));
				currentUserInfo.setPrefServerConnectionFolderLocation(serverConnectionsFolder.getAbsolutePath());
				currentUserInfo.setPrefDownloadFolderLocation(downloadFolder.getAbsolutePath());
				currentUserInfo.setPrefTemplateFolderLocation(templateFolder.getAbsolutePath());
				currentUserInfo.setPrefSnapshotPrefixName(defaultSnapshotNameTextField.getText());
				
				String value=EXCEL_RADIO_BUTTON_TEXT;
				if (!isExcelFormat()) {
					value=XML_RADIO_BUTTON_TEXT;
				}
				currentUserInfo.setPrefDownloadFormat(value);
				
				value = PHYSICAL_DELETE_BUTTON_TEXT;
				if(!isPhysicalDelete()){
					value = SOFT_DELETE_RADIO_BUTTON_TEXT;
				}
				currentUserInfo.setPrefDeleteOption(value);
				currentUserInfo.setPrefIncludeSoftDeletedSnapshot(clearSoftDeletedSnapshotsCheckbox.isSelected());
				currentUserInfo.setPrefDisplayUnsupportedInvOption(isShowUnsupportedInventories());
				currentUserInfo.setPrefDisplayTotalDetailOption(isShowTotalDetails());
				currentUserInfo.setPrefDisplayHelperBalloon(showHelpCheckbox.isSelected());
				currentUserInfo.setPrefDisplayBR100(reportsVerticalWayCheckbox.isSelected());
				currentUserInfo.setPrefSeededUsersInfoOption(seededUsersTextArea.getText());
				Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(mainPanel.getTabSnapshotsPanel());
				connection=DatabaseUtils.getJDBCConnectionGeneric(
						ModelUtils.getJDBCString(snapshotEnvironmentProperties),
						ModelUtils.getDBUserName(snapshotEnvironmentProperties),
						ModelUtils.getDBPassword(snapshotEnvironmentProperties),true);
				connection.setAutoCommit(false);
				ModelUtils.updateUser(connection,currentUserInfo);
				connection.commit();
			}
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
		
	}
	public void processActionSaveOptionsToPreferenceFile(){
		try{
			Preferences pref = Preferences.userRoot();
			pref.put(PREFERENCE_WORKERS_COUNT,workersTextField.getText());
			pref.put(PREFERENCE_SERVER_CONNECTIONS_FOLDER_LOCATION,serverConnectionsFolder.getAbsolutePath());
			pref.put(PREFERENCE_DOWNLOAD_FOLDER_LOCATION,downloadFolder.getAbsolutePath());
			pref.put(PREFERENCE_SNAPSHOT_PREFIX_NAME,defaultSnapshotNameTextField.getText());
			
			String value=EXCEL_RADIO_BUTTON_TEXT;
			if (!isExcelFormat()) {
				value=XML_RADIO_BUTTON_TEXT;
			}
			pref.put(PREFERENCE_DOWNLOAD_FORMAT,value);
			
			value = PHYSICAL_DELETE_BUTTON_TEXT;
			if(!isPhysicalDelete()){
				value = SOFT_DELETE_RADIO_BUTTON_TEXT;
			}
			pref.put(PREFERENCE_SNAPSHOT_DELETE_OPTION,value);
			pref.putBoolean(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,isShowUnsupportedInventories());
			pref.put(PREFERENCE_SEEDED_USERS_DEFINITION,seededUsersTextArea.getText());
			pref.putBoolean(PREFERENCE_DISPLAY_TOTAL_DETAILS,isShowTotalDetails());
			pref.putBoolean(PREFERENCE_SHOW_HELPER_BALLOONS,showHelpCheckbox.isSelected());
			pref.putBoolean(PREFERENCE_REPORT_VERTICAL_WAY,reportsVerticalWayCheckbox.isSelected());
			pref.put(PREFERENCE_TEMPLATE_FOLDER_LOCATION,templateFolder.getAbsolutePath());
			pref.putBoolean(PREFERENCE_SNAPSHOT_CLEAR_SOFT_DELETE_OPTION,clearSoftDeletedSnapshotsCheckbox.isSelected());
			
		}catch(Exception e){
			String errMsg = "Unexpected error: "+e.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;	
		}
		
	}
	public boolean isAtLeastOneUserExist(){
		List<UserInformation> snapshotUserInformationList = new ArrayList<UserInformation>();
		try{
			Connection connection = null;
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(mainPanel.getTabSnapshotsPanel());
			connection=DatabaseUtils.getJDBCConnectionGeneric(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties),true);
			snapshotUserInformationList=ModelUtils.getSnapshotUserInformationList(connection);
			if(!snapshotUserInformationList.isEmpty() && snapshotUserInformationList.size()>0){
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			FileUtils.println("Cannot get user information list,  error : "+e.getMessage());
			return false;
		}
	}
	
	
	
	
	
}
