package com.rapidesuite.extract.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class TabOptionsPanel  extends JPanel{

	private ExtractMain extractMain;
	private JLabel workersLabel;
	private JLabel batchSizeLabel;
	private JTextField workersTextField;
	private JTextField batchSizeTextField;
	private JTextArea seededUsersTextArea;
	private JLabel downloadFolderPathLabel;
	private JButton downloadFolderButton;
	private JFileChooser downloadFolderFileChooser;
	private File downloadFolder;
	private JLabel templateFolderPathLabel;
	private JButton templateFolderButton;
	private JFileChooser templateFolderFileChooser;
	private File templateFolder;
	private JRadioButton excelFormatRadioButton;
	private JCheckBox showUnsupportedInventoriesCheckbox;
	private JCheckBox downloadCompletePackageCheckbox;
	private JCheckBox isStaticUseOfFunctionValuesCheckbox;
	private JButton saveOptions;
	private JButton resetOptions;
	private JRadioButton xmlFormatRadioButton;

	public static final String PREFERENCE_WORKERS_COUNT="EXTRACT_PREFERENCE_WORKERS_COUNT";
	public static final String PREFERENCE_BATCH_SIZE_COUNT="EXTRACT_PREFERENCE_BATCH_SIZE_COUNT";
	public static final String PREFERENCE_DOWNLOAD_FOLDER_LOCATION="EXTRACT_PREFERENCE_DOWNLOAD_FOLDER_LOCATION";
	public static final String PREFERENCE_TEMPLATE_FOLDER_LOCATION="EXTRACT_PREFERENCE_TEMPLATE_FOLDER_LOCATION";
	public static final String PREFERENCE_DOWNLOAD_FORMAT="EXTRACT_PREFERENCE_DOWNLOAD_FORMAT";
	public static final String PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES="EXTRACT_PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES";
	public static final String PREFERENCE_DOWNLOAD_COMPLETE_PACKAGE="EXTRACT_PREFERENCE_DOWNLOAD_COMPLETE_PACKAGE";
	public static final String PREFERENCE_SEEDED_USERS_DEFINITION="EXTRACT_PREFERENCE_SEEDED_USERS_DEFINITION";
	public static final String PREFERENCE_IS_STATIC_USE_OF_FUNCTION_VALUES="EXTRACT_PREFERENCE_IS_STATIC_USE_OF_FUNCTION_VALUES";
	
	public static final String DEFAULT_DOWNLOAD_FOLDER_NAME="extract-downloads";
	public static final String DEFAULT_TEMPLATE_FOLDER_NAME="templates";
	public static final String DEFAULT_WORKERS_COUNT="32";
	public static final String DEFAULT_BATCH_SIZE_COUNT="50000";
	public static final String DEFAULT_DOWNLOAD_FORMAT="XML";
	public static final boolean DEFAULT_DISPLAY_UNSUPPORTED_INVENTORIES=false;
	public static final boolean DEFAULT_DOWNLOAD_COMPLETE_PACKAGE=false;
	public static final boolean DEFAULT_IS_STATIC_USE_OF_FUNCTION_VALUES=true;
	
	public static final String EXPLANATION_WOKERS = "<html>Number of parallel workers to be used for extraction.";
	public static final String EXPLANATION_BATCH_SIZE = "<html>Number of records to be extracted from BI Publisher per iteration."; 
	public static final String EXPLANATION_DOWNLOAD_LOCATION = "<html>Select the location where the output files should be downloaded to.";
	public static final String EXPLANATION_TEMPLATE_LOCATION = "<html>Select the location where the extract templates will be stored and read from.";
	public static final String EXPLANATION_OUTPUT_FORMAT = "<html>Select the format of the generated output file."; 
	public static final String EXPLANATION_SHOW_ALL_INVENTORIES = "<html>Select to show all the inventories from FUSION - even the unsupported ones.";
	public static final String EXPLANATION_DOWNLOAD_COMPLETE_PACKAGE = "<html>Select to compress the output in a single archive file. <br/>If not selected output would be compressed in archive files based on the organizational level of the data.";
	public static final String EXPLANATION_CACHE_LOOKUP = "<html>Extract the look-up values from FUSION only once and cache them for the entire extraction session. <br/>Saves time, but if the look-up values are changing on the FUSION instance this option should be un-checked.";
	public static final String EXPLANATION_SEEDED_USERS = "<html>The comma seperated list of usernames and user-ids that define seeded-users. <br/>If <b>Exclude Seeded Data</b> is checked in the Selection tab, data from these users will be ignored by extract.";
	
	public static final String INFO_ICON_RESOURCE_PATH = "/images/snapshot/info_icon.png";
	public static final String INFO_ICON_HOVER_RESOURCE_PATH = "/images/snapshot/info_icon_hover.png";
	
	public static final String EXCEL_RADIO_BUTTON_TEXT="EXCEL";
	public static final String XML_RADIO_BUTTON_TEXT="XML";
	
	
	public TabOptionsPanel(ExtractMain extractMain) {
		this.extractMain=extractMain;
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 0));
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		createComponents();
	}
	
	private JButton getInfoButton(final JComponent infoForJComp, final String infoMessage) {
		ImageIcon ii=null;
		URL iconURL =null;
		
		String infoIconResourcePath = INFO_ICON_RESOURCE_PATH;
		String infoIconHoverResourcePath = INFO_ICON_HOVER_RESOURCE_PATH;
		
		iconURL = this.getClass().getResource(infoIconResourcePath);
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		final JButton infoButton = new JButton();
		infoButton.setIcon(ii);
		infoButton.setBorderPainted(false);
		infoButton.setContentAreaFilled(false);
		infoButton.setFocusPainted(false);
		infoButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource(infoIconHoverResourcePath);
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		infoButton.setRolloverIcon(new RolloverIcon(ii));
		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UIUtils.showBalloon(
						infoForJComp == null ? infoButton : infoForJComp, 
								infoMessage, true);
			}
		});
		return infoButton;
	}
	
	private void setValues() {
		Preferences pref = Preferences.userRoot();
		
		File userhomeFolder=new File(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.extract.toString());
		
		String value= pref.get(PREFERENCE_WORKERS_COUNT,DEFAULT_WORKERS_COUNT);
		workersTextField.setText(value);

		String valueBatchSize= pref.get(PREFERENCE_BATCH_SIZE_COUNT,DEFAULT_BATCH_SIZE_COUNT);
		batchSizeTextField.setText(valueBatchSize);
		
		String str= pref.get(PREFERENCE_DOWNLOAD_FORMAT,DEFAULT_DOWNLOAD_FORMAT);
		if (str.equalsIgnoreCase(EXCEL_RADIO_BUTTON_TEXT)) {
			excelFormatRadioButton.setSelected(true);
			xmlFormatRadioButton.setSelected(false);
		}
		else {
			excelFormatRadioButton.setSelected(false);
			xmlFormatRadioButton.setSelected(true);
		}

		boolean b= pref.getBoolean(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,DEFAULT_DISPLAY_UNSUPPORTED_INVENTORIES);
		showUnsupportedInventoriesCheckbox.setSelected(b);
		
		b= pref.getBoolean(PREFERENCE_DOWNLOAD_COMPLETE_PACKAGE,DEFAULT_DOWNLOAD_COMPLETE_PACKAGE);
		downloadCompletePackageCheckbox.setSelected(b);

		b= pref.getBoolean(PREFERENCE_IS_STATIC_USE_OF_FUNCTION_VALUES,DEFAULT_IS_STATIC_USE_OF_FUNCTION_VALUES);
		isStaticUseOfFunctionValuesCheckbox.setSelected(b);

		str= pref.get(PREFERENCE_SEEDED_USERS_DEFINITION,ExtractConstants.ORACLE_DEFAULT_SEEDED_USER_NAMES);
		seededUsersTextArea.setText(str);
		
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
	}

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
		workersPanel.add(getInfoButton(workersTextField, EXPLANATION_WOKERS));

		JPanel batchSizePanel=new JPanel();
		batchSizePanel.setOpaque(false);
		batchSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(batchSizePanel);
		batchSizeLabel=new JLabel("Default Extraction Batch Size:");
		batchSizeLabel.setOpaque(false);
		InjectUtils.assignArialPlainFont(batchSizeLabel,InjectMain.FONT_SIZE_NORMAL);
		batchSizeLabel.setForeground(Color.decode("#343836"));
		batchSizePanel.add(batchSizeLabel);
		batchSizeTextField=new JTextField();
		InjectUtils.assignArialPlainFont(batchSizeTextField,InjectMain.FONT_SIZE_NORMAL);
		batchSizeTextField.setForeground(Color.decode("#343836"));
		UIUtils.setDimension(batchSizeTextField,fieldsWidth,height);
		batchSizePanel.add(batchSizeTextField);
		batchSizePanel.add(getInfoButton(batchSizeTextField, EXPLANATION_BATCH_SIZE));

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

		ImageIcon ii=null;
		URL iconURL =null;

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
		downloadFolderPanel.add(getInfoButton(downloadFolderButton, EXPLANATION_DOWNLOAD_LOCATION));

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

		ii=null;
		iconURL =null;

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
		templateFolderPanel.add(getInfoButton(templateFolderButton, EXPLANATION_TEMPLATE_LOCATION));

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
		tempPanel.add(getInfoButton(null, "Select the format of the generated output file."));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		showUnsupportedInventoriesCheckbox=new JCheckBox("Display unsupported Inventories");
		showUnsupportedInventoriesCheckbox.setOpaque(false);
		InjectUtils.assignArialPlainFont(showUnsupportedInventoriesCheckbox,InjectMain.FONT_SIZE_NORMAL);
		showUnsupportedInventoriesCheckbox.setForeground(Color.decode("#343836"));
		tempPanel.add(showUnsupportedInventoriesCheckbox);
		tempPanel.add(getInfoButton(null, EXPLANATION_SHOW_ALL_INVENTORIES));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		downloadCompletePackageCheckbox=new JCheckBox("Download complete package");
		downloadCompletePackageCheckbox.setOpaque(false);
		InjectUtils.assignArialPlainFont(downloadCompletePackageCheckbox,InjectMain.FONT_SIZE_NORMAL);
		downloadCompletePackageCheckbox.setForeground(Color.decode("#343836"));
		tempPanel.add(downloadCompletePackageCheckbox);
		tempPanel.add(getInfoButton(null, EXPLANATION_DOWNLOAD_COMPLETE_PACKAGE));
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		add(tempPanel);
		isStaticUseOfFunctionValuesCheckbox=new JCheckBox("Cache Function Values (Code combinations, System profile values, Value Sets...)");
		isStaticUseOfFunctionValuesCheckbox.setOpaque(false);
		InjectUtils.assignArialPlainFont(isStaticUseOfFunctionValuesCheckbox,InjectMain.FONT_SIZE_NORMAL);
		isStaticUseOfFunctionValuesCheckbox.setForeground(Color.decode("#343836"));
		tempPanel.add(isStaticUseOfFunctionValuesCheckbox);
		tempPanel.add(getInfoButton(null, EXPLANATION_CACHE_LOOKUP));
		
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
		tempPanel.add(getInfoButton(null, EXPLANATION_SEEDED_USERS));
		add(Box.createRigidArea(new Dimension(15, 15)));


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

	protected void processActionDownloadFolder() {
		try{	
			int returnVal = downloadFolderFileChooser.showOpenDialog(extractMain.getRootFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				downloadFolder=downloadFolderFileChooser.getSelectedFile();
				downloadFolderPathLabel.setText(downloadFolder.getAbsolutePath());
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}

	protected void processActionTemplateFolder() {
		try{	
			int returnVal = templateFolderFileChooser.showOpenDialog(extractMain.getRootFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				templateFolder=templateFolderFileChooser.getSelectedFile();
				templateFolderPathLabel.setText(templateFolder.getAbsolutePath());
			}
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage(ex.getMessage());
		}
	}

	public JTextField getWorkersTextField() {
		return workersTextField;
	}

	public JTextField getBatchSizeTextField() {
		return batchSizeTextField;
	}

	public File getDownloadFolder() {
		return downloadFolder;
	}

	public File getTemplateFolder() {
		return templateFolder;
	}

	public boolean isExcelFormat() {
		return excelFormatRadioButton.isSelected();
	}

	public boolean isShowUnsupportedInventories() {
		return showUnsupportedInventoriesCheckbox.isSelected();
	}

	public boolean isDownloadCompletePackage() {
		return downloadCompletePackageCheckbox.isSelected();
	}
	
	public boolean isStaticUseOfFunctionValues() {
		return isStaticUseOfFunctionValuesCheckbox.isSelected();
	}

	public String getOracleSeededUserNames() {
		return seededUsersTextArea.getText().trim();
	}

	public void processActionSaveOptions()
	{
		Preferences pref = Preferences.userRoot();
		
		int batchSize = 0;
		try {
			batchSize = Integer.parseInt(batchSizeTextField.getText());
		} catch (Exception ex) {
			FileUtils.printStackTrace(ex);
			GUIUtils.popupErrorMessage("Default Extraction Batch Size should be a number.\n" + ex.toString());
			return;
		}
		if(batchSize <= 0) {
			GUIUtils.popupErrorMessage("Default Extraction Batch Size can not be zero or negative.");
			return;
		}

		pref.put(PREFERENCE_WORKERS_COUNT,workersTextField.getText());
		pref.put(PREFERENCE_BATCH_SIZE_COUNT,batchSizeTextField.getText());
		pref.put(PREFERENCE_DOWNLOAD_FOLDER_LOCATION,downloadFolder.getAbsolutePath());
		pref.put(PREFERENCE_TEMPLATE_FOLDER_LOCATION,templateFolder.getAbsolutePath());
		String value=EXCEL_RADIO_BUTTON_TEXT;
		if (!isExcelFormat()) {
			value=XML_RADIO_BUTTON_TEXT;
		}
		pref.put(PREFERENCE_DOWNLOAD_FORMAT,value);
		pref.putBoolean(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES,isShowUnsupportedInventories());
		pref.putBoolean(PREFERENCE_DOWNLOAD_COMPLETE_PACKAGE,isDownloadCompletePackage());
		pref.putBoolean(PREFERENCE_IS_STATIC_USE_OF_FUNCTION_VALUES,isStaticUseOfFunctionValues());
		pref.put(PREFERENCE_SEEDED_USERS_DEFINITION,seededUsersTextArea.getText());
	}

	public void processActionResetOptions()
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
		pref.remove(PREFERENCE_BATCH_SIZE_COUNT);
		pref.remove(PREFERENCE_DOWNLOAD_FOLDER_LOCATION);
		pref.remove(PREFERENCE_TEMPLATE_FOLDER_LOCATION);
		pref.remove(PREFERENCE_DOWNLOAD_FORMAT);
		pref.remove(PREFERENCE_DISPLAY_UNSUPPORTED_INVENTORIES);
		pref.remove(PREFERENCE_DOWNLOAD_COMPLETE_PACKAGE);
		pref.remove(PREFERENCE_IS_STATIC_USE_OF_FUNCTION_VALUES);
		pref.remove(PREFERENCE_SEEDED_USERS_DEFINITION);
		
		setValues();
	}

	public JTextArea getSeededUsersTextArea() {
		return seededUsersTextArea;
	}

	public JButton getSaveOptions() {
		return saveOptions;
	}

	public ExtractMain getExtractMain() {
		return extractMain;
	}

	public void unlockUI() {
		setComponentsEnabled(true);
	}
	
	public void lockUI() {
		setComponentsEnabled(false);
	}

	public void setComponentsEnabled(boolean isEnabled) {
		workersTextField.setEnabled(isEnabled);
		batchSizeTextField.setEnabled(isEnabled);
		seededUsersTextArea.setEnabled(isEnabled);
		downloadFolderButton.setEnabled(isEnabled);
		excelFormatRadioButton.setEnabled(isEnabled);
		xmlFormatRadioButton.setEnabled(isEnabled);
		showUnsupportedInventoriesCheckbox.setEnabled(isEnabled);
		downloadCompletePackageCheckbox.setEnabled(isEnabled);
		isStaticUseOfFunctionValuesCheckbox.setEnabled(isEnabled);
		saveOptions.setEnabled(isEnabled);
	}	

}
