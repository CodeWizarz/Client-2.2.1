/**************************************************
 * $Revision: 50236 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2015-11-06 11:56:59 +0700 (Fri, 06 Nov 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/panels/SwiftBuildPropertiesValidationPanel.java $:
 * $Id: SwiftBuildPropertiesValidationPanel.java 50236 2015-11-06 04:56:59Z jannarong.wadthong $:
 */

package com.rapidesuite.build.gui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.gui.environment.SwiftBuildEnvironmentLoadButtonAction;
import com.rapidesuite.build.gui.environment.SwiftBuildEnvironmentSaveButtonAction;
import com.rapidesuite.build.gui.environment.SwiftBuildEnvironmentValidationButtonAction;
import com.rapidesuite.build.gui.frames.EnvironmentExtraPropertiesFrame;
import com.rapidesuite.client.common.gui.CopyPasteByRightClickCapableJPasswordField;
import com.rapidesuite.client.common.gui.CopyPasteByRightClickCapableJTextField;
import com.rapidesuite.client.common.gui.EnvironmentValidationButtonAbstractAction.NextButtonWrapper;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;
import com.rapidesuite.client.common.gui.EnvironmentValidationProperty;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.utility.EnvironmentPropertyConstants;

@SuppressWarnings("serial")
public class SwiftBuildPropertiesValidationPanel extends JPanel implements NextButtonWrapper
{

	EnvironmentValidationPanel environmentValidationPanel;
	BuildMain BuildMain;
	protected JButton nextButton;
	private String ebsVersion;
	private JTextField fldScriptsSSHPrivateKeyLocationField;
	private JButton fldScriptsSSHPrivateKeyLocationButton;
	private JFileChooser fldScriptsSSHPrivateKeyLocationFileChooser;
	private JComboBox<String> transferProtocolComboBox;
	private EnvironmentExtraPropertiesFrame environmentExtraPropertiesFrame;
	private ActionListener onNextButtonIsClicked = null;
	protected JPanel mainPanel;
	protected List<EnvironmentValidationProperty> environmentProperties;
	protected Map<String, String> filesExtensionAllowed;
	private SwiftBuildEnvironmentLoadButtonAction swiftBuildEnvironmentLoadButtonAction;
	private SwiftBuildEnvironmentSaveButtonAction swiftBuildEnvironmentSaveButtonAction;
	private SwiftBuildEnvironmentValidationButtonAction swiftBuildEnvironmentValidationButtonAction;	
	protected JLabel optionalLabel;
	protected Dimension scrollPaneDimension;
	protected JPanel loadSaveValidateButtonsPanel;
	
	protected JButton previousButton;

	public SwiftBuildPropertiesValidationPanel(BuildMain BuildMain)
	{
	    this(BuildMain, true);
	}

	public SwiftBuildPropertiesValidationPanel(BuildMain BuildMain, boolean createComponents)
	{
		this.BuildMain = BuildMain;
		if ( createComponents )
		{
		    createComponents();
		}
	}

	public void createComponents()
	{
		setLayout(new GridBagLayout());
		mainPanel = new JPanel();
		// mainPanel.setBackground(java.awt.Color.red);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		GridBagConstraints mainPanelConsraints = new GridBagConstraints();
		mainPanelConsraints.fill = GridBagConstraints.BOTH;
		mainPanelConsraints.gridy = 0;
		mainPanelConsraints.weightx=1;
		mainPanelConsraints.weighty=0.8;
		mainPanelConsraints.anchor=GridBagConstraints.FIRST_LINE_START;
		add(mainPanel, mainPanelConsraints);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		environmentProperties = new ArrayList<EnvironmentValidationProperty>();
		EnvironmentValidationProperty environmentProperty = null;

		int width = 200;
		int height = 20;
		fldScriptsSSHPrivateKeyLocationField = new JTextField();
		fldScriptsSSHPrivateKeyLocationField.setEditable(false);
		fldScriptsSSHPrivateKeyLocationField.setBackground(Color.lightGray);
		final JTextField fldScriptsHostUserNameField = new CopyPasteByRightClickCapableJTextField();
		final JPasswordField fldScriptsHostPasswordField = new CopyPasteByRightClickCapableJPasswordField();
		final JTextField fldScriptHostNameField = new CopyPasteByRightClickCapableJTextField();
		fldScriptsSSHPrivateKeyLocationButton = GUIUtils.getButton(this.getClass(), "Choose", "/images/open16.gif");
		fldScriptsSSHPrivateKeyLocationButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					openPrivateKeyFileChooser();
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		});

		JLabel temp = GUIUtils.getLabel("Transfer protocol:", true);
		temp.setToolTipText("This is the protocol in order to transfer files to the server.");
		transferProtocolComboBox = new JComboBox();
		transferProtocolComboBox.addItemListener(new ItemListener()
		{

			public void itemStateChanged(ItemEvent evt)
			{
				if ( evt.getStateChange() == ItemEvent.SELECTED )
				{
					JComboBox cb = (JComboBox) evt.getSource();
					String connectionMethod = (String) cb.getSelectedItem();
					// Item was just selected
					if ( connectionMethod.equalsIgnoreCase(SwiftBuildConstants.CONNECTION_METHOD_SFTPK_VALUE) )
					{
						fldScriptHostNameField.setEnabled(true);
						fldScriptHostNameField.setBackground(Color.white);
						fldScriptsHostUserNameField.setEnabled(true);
						fldScriptsHostUserNameField.setBackground(Color.white);
						fldScriptsHostPasswordField.setEnabled(false);
						fldScriptsHostPasswordField.setText("");
						fldScriptsHostPasswordField.setBackground(Color.lightGray);
						fldScriptsSSHPrivateKeyLocationField.setEnabled(true);
						fldScriptsSSHPrivateKeyLocationField.setEditable(true);
						fldScriptsSSHPrivateKeyLocationField.setBackground(Color.white);
						fldScriptsSSHPrivateKeyLocationButton.setEnabled(true);
					}
					else if ( connectionMethod.equalsIgnoreCase(SwiftBuildConstants.CONNECTION_METHOD_FILE_VALUE) )
					{
						fldScriptHostNameField.setEnabled(false);
						fldScriptHostNameField.setText("");
						fldScriptHostNameField.setBackground(Color.lightGray);
						fldScriptsHostUserNameField.setEnabled(false);
						fldScriptsHostUserNameField.setText("");
						fldScriptsHostUserNameField.setBackground(Color.lightGray);
						fldScriptsHostPasswordField.setEnabled(false);
						fldScriptsHostPasswordField.setText("");
						fldScriptsHostPasswordField.setBackground(Color.lightGray);
						fldScriptsSSHPrivateKeyLocationField.setEnabled(false);
						fldScriptsSSHPrivateKeyLocationField.setEditable(false);
						fldScriptsSSHPrivateKeyLocationField.setBackground(Color.lightGray);
						fldScriptsSSHPrivateKeyLocationButton.setEnabled(false);
						fldScriptsSSHPrivateKeyLocationField.setText("");
					}					
					else
					{
						fldScriptHostNameField.setEnabled(true);
						fldScriptHostNameField.setBackground(Color.white);
						fldScriptsHostUserNameField.setEnabled(true);
						fldScriptsHostUserNameField.setBackground(Color.white);
						fldScriptsHostPasswordField.setEnabled(true);
						fldScriptsHostPasswordField.setBackground(Color.white);
						fldScriptsSSHPrivateKeyLocationField.setEnabled(false);
						fldScriptsSSHPrivateKeyLocationField.setEditable(false);
						fldScriptsSSHPrivateKeyLocationField.setBackground(Color.lightGray);
						fldScriptsSSHPrivateKeyLocationButton.setEnabled(false);
						fldScriptsSSHPrivateKeyLocationField.setText("");
					}
				}
				else if ( evt.getStateChange() == ItemEvent.DESELECTED )
				{
					// Item is no longer selected
				}
			}
		});
		GUIUtils.setComponentDimension(transferProtocolComboBox, width, height);
		transferProtocolComboBox.addItem(SwiftBuildConstants.CONNECTION_METHOD_DEFAULT_VALUE);
		transferProtocolComboBox.addItem(SwiftBuildConstants.CONNECTION_METHOD_FTP_VALUE);
		transferProtocolComboBox.addItem(SwiftBuildConstants.CONNECTION_METHOD_SFTP_VALUE);
		transferProtocolComboBox.addItem(SwiftBuildConstants.CONNECTION_METHOD_SFTPK_VALUE);
		transferProtocolComboBox.addItem(SwiftBuildConstants.CONNECTION_METHOD_FILE_VALUE);
		transferProtocolComboBox.setSelectedItem(SwiftBuildConstants.CONNECTION_METHOD_DEFAULT_VALUE);
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY, temp, transferProtocolComboBox, false);
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put(SwiftBuildConstants.CONNECTION_METHOD_SFTP_VALUE, SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP.toString());
		mappings.put(SwiftBuildConstants.CONNECTION_METHOD_FTP_VALUE, SwiftBuildConstants.TRANSFER_PROTOCOL.FTP.toString());
		mappings.put(SwiftBuildConstants.CONNECTION_METHOD_SFTPK_VALUE, SwiftBuildConstants.TRANSFER_PROTOCOL.SFTPK.toString());
		mappings.put(SwiftBuildConstants.CONNECTION_METHOD_FILE_VALUE, SwiftBuildConstants.TRANSFER_PROTOCOL.FILE.toString());
		environmentProperty.setMappings(mappings);
		environmentProperties.add(environmentProperty);
		
		temp = GUIUtils.getLabel("Hostname:", true);
		temp.setToolTipText("This is the server where the FLD scripts are located. Example: orrsctst01.serv.com or 172.120.12.1");
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY, temp, fldScriptHostNameField, true);
		environmentProperties.add(environmentProperty);

		temp = GUIUtils.getLabel("Host user name:", true);
		temp.setToolTipText("This is the username in order to connect to the server.");
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY, temp, fldScriptsHostUserNameField, true);
		environmentProperties.add(environmentProperty);

		temp = GUIUtils.getLabel("Host password:", true);
		temp.setToolTipText("This is the password in order to connect to the server.");
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY, temp, fldScriptsHostPasswordField, true);
		environmentProperties.add(environmentProperty);

		JPanel fldScriptsSSHPrivateKeyLocationPanel = new JPanel();
		GUIUtils.setComponentDimension(fldScriptsSSHPrivateKeyLocationField, 150, height);
		fldScriptsSSHPrivateKeyLocationPanel.setLayout(new BoxLayout(fldScriptsSSHPrivateKeyLocationPanel, BoxLayout.X_AXIS));
		fldScriptsSSHPrivateKeyLocationPanel.add(fldScriptsSSHPrivateKeyLocationField);
		fldScriptsSSHPrivateKeyLocationPanel.add(Box.createRigidArea(new Dimension(5, 1)));
		fldScriptsSSHPrivateKeyLocationPanel.add(fldScriptsSSHPrivateKeyLocationButton);
		temp = GUIUtils.getLabel("(*) SFTP Private key Location:", true);
		temp.setToolTipText("This is the file containing the credentials in order to connect to the server. Only relevant for the transfer protocol: 'SFTP with Private key file'.");
		GUIUtils.setComponentDimension(fldScriptsSSHPrivateKeyLocationField, width, height);
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_SCRIPTS_SSH_PRIVATE_KEY_LOCATION_KEY, temp, fldScriptsSSHPrivateKeyLocationPanel, true);
		environmentProperties.add(environmentProperty);

		temp = GUIUtils.getLabel("FLD scripts folder:", true);
		temp.setToolTipText("This is the folder located on the server where the FLD scripts must be stored in order for 'Forms playback' technology to work.");
		JTextField textField = new CopyPasteByRightClickCapableJTextField();
		GUIUtils.setComponentDimension(textField, width, height);
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_SCRIPTS_FOLDER_KEY, temp, textField, false);
		environmentProperties.add(environmentProperty);

		temp = GUIUtils.getLabel("FLD scripts Logs folder:", true);
		temp.setToolTipText("This is the folder located on the server where the FLD scripts log files are generated by 'Forms playback' technology.");
		textField = new CopyPasteByRightClickCapableJTextField();
		GUIUtils.setComponentDimension(textField, width, height);
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_SCRIPTS_LOG_FOLDER_KEY, temp, textField, false);
		environmentProperties.add(environmentProperty);

		temp = GUIUtils.getLabel("FLD URL:", true);
		temp.setToolTipText("This is the URL which starts 'Forms playback'.");
		textField = new CopyPasteByRightClickCapableJTextField();
		GUIUtils.setComponentDimension(textField, width, height);
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_SCRIPTS_URL_KEY, temp, textField, false);
		environmentProperties.add(environmentProperty);

		temp = GUIUtils.getLabel("(*) Oracle Forms user name:", true);
		temp.setToolTipText("This is the username to login to Oracle Forms.  Must have access to System Profile Options");
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_FORM_USER_NAME_KEY, temp, new CopyPasteByRightClickCapableJTextField(), true);
		environmentProperties.add(environmentProperty);

		temp = GUIUtils.getLabel("(*) Oracle Forms password:", true);
		temp.setToolTipText("This is the password to login to Oracle Forms.");
		environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_FORM_PASSWORD_KEY, temp, new CopyPasteByRightClickCapableJPasswordField(), true);
		environmentProperties.add(environmentProperty);

        temp = GUIUtils.getLabel("Oracle Responsibility:", true);
        temp.setToolTipText("This is the Oracle Responsibility Name which has access to the 'System Profile' Form");
        environmentProperty = new EnvironmentValidationProperty(SwiftBuildConstants.FLD_FORM_RESPONSIBILITY_KEY, temp, new CopyPasteByRightClickCapableJTextField(), true);
        environmentProperties.add(environmentProperty);

		environmentProperty = new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_HOST_NAME_KEY,
				GUIUtils.getLabel("Database hostname:", true),
				new CopyPasteByRightClickCapableJTextField(),
				true);
		environmentProperties.add(environmentProperty);
		environmentProperty = new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_PORT_NUMBER_KEY,
				GUIUtils.getLabel("Database port number:", true),
				new CopyPasteByRightClickCapableJTextField(),
				true);
		environmentProperties.add(environmentProperty);
		environmentProperty = new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_SID_KEY, GUIUtils.getLabel("Database SID:", true), new CopyPasteByRightClickCapableJTextField(), true);
		environmentProperties.add(environmentProperty);
		environmentProperty = new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_USER_NAME_KEY,
				GUIUtils.getLabel("Database user name:", true),
				new CopyPasteByRightClickCapableJTextField(),
				true);
		environmentProperties.add(environmentProperty);
		environmentProperty = new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_PASSWORD_KEY,
				GUIUtils.getLabel("Database password:", true),
				new CopyPasteByRightClickCapableJPasswordField(),
				true);
		environmentProperties.add(environmentProperty);

		temp = GUIUtils.getLabel("(*) Extra properties panel:", true);
		environmentExtraPropertiesFrame = new EnvironmentExtraPropertiesFrame("Extra properties panel:");
		temp.setToolTipText("This is a panel to specify extra properties such as replacement tokens.");
		JButton extraPropertiesButton = GUIUtils.getButton(this.getClass(), "...", null);
		extraPropertiesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		GUIUtils.setComponentDimension(extraPropertiesButton, 100, height);
		extraPropertiesButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					environmentExtraPropertiesFrame.setVisible(true);
				}
				catch ( Exception ex )
				{
					FileUtils.printStackTrace(ex);
				}
			}
		});
		environmentProperty = new EnvironmentValidationProperty("extra properties", temp, extraPropertiesButton, true);
		environmentProperties.add(environmentProperty);
		
		filesExtensionAllowed = new HashMap<String, String>();
		filesExtensionAllowed.put(SwiftBuildConstants.OLD_ENVIRONMENT_FILE_EXTENSION, "BWE files");
		filesExtensionAllowed.put(SwiftBuildConstants.ENVIRONMENT_FILE_EXTENSION, "Environment files");

		optionalLabel = GUIUtils.getLabel("(*) Optional", true);

		swiftBuildEnvironmentLoadButtonAction = new SwiftBuildEnvironmentLoadButtonAction(this);
		swiftBuildEnvironmentSaveButtonAction = new SwiftBuildEnvironmentSaveButtonAction(this) {
			@Override
			public boolean beforeExecuteAction(EnvironmentValidationPanel environmentValidationPanel)
			{
				if (SwiftBuildPropertiesValidationPanel.this.swiftBuildHtmlValidationPanel != null) {
					this.fileProperties = SwiftBuildPropertiesValidationPanel.this.swiftBuildHtmlValidationPanel.getEnvironmentValidationPanel().getEnvironmentPropertiesMap();
					return true;
				} else {
					return super.beforeExecuteAction(environmentValidationPanel);
				}
			}			
		};
		swiftBuildEnvironmentValidationButtonAction = new SwiftBuildEnvironmentValidationButtonAction(this);

		scrollPaneDimension = new Dimension(100, 430);

		environmentValidationPanel = new EnvironmentValidationPanel("Environment validation",
				environmentProperties,
				filesExtensionAllowed,
				swiftBuildEnvironmentLoadButtonAction,
				swiftBuildEnvironmentSaveButtonAction,
				swiftBuildEnvironmentValidationButtonAction,
				optionalLabel,
				scrollPaneDimension,
				this,
				this.BuildMain);

		// createEmptyBorder(int top, int left, int bottom, int right)
		environmentValidationPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
		mainPanel.add(environmentValidationPanel);

		nextButton = GUIUtils.getButton(this.getClass(), "Next", SwiftBuildConstants.IMAGE_NEXT);
		onNextButtonIsClicked = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//bwe selection window
				clickNextButtonActionPerformed();
			}
		};
		nextButton.addActionListener(onNextButtonIsClicked);

		fldScriptsSSHPrivateKeyLocationFileChooser = Utils.initializeJFileChooserWithTheLastPath("FLD_SCRIPTS_SSH_PRIVATE_KEY_LOCATION_FILE_CHOOSER");

		//this panel contains the load-save-validate buttons and next button
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		GridBagConstraints bottomPanelConstraints = new GridBagConstraints();
		bottomPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
		bottomPanelConstraints.gridy = 2;
		bottomPanelConstraints.weightx = 1;
		bottomPanelConstraints.weighty = 0.1;
		bottomPanelConstraints.anchor = GridBagConstraints.LAST_LINE_START;
		bottomPanelConstraints.gridheight = GridBagConstraints.REMAINDER;
		bottomPanelConstraints.insets = new Insets(0,0,0,0);
		bottomPanelConstraints.ipadx = 0;
		bottomPanelConstraints.ipady = 0;
		add(bottomPanel, bottomPanelConstraints);

		//this panel contains the load-save-validate buttons
		loadSaveValidateButtonsPanel = new JPanel();
		loadSaveValidateButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		loadSaveValidateButtonsPanel.setBorder(BorderFactory.createEmptyBorder(20,5,5,5));
		if (environmentValidationPanel.getLoadFileButton() != null) {
			loadSaveValidateButtonsPanel.add(environmentValidationPanel.getLoadFileButton());
		}
		loadSaveValidateButtonsPanel.add(Box.createRigidArea(new Dimension(15,5)));
		if (environmentValidationPanel.getSaveFileButton() != null) {
			loadSaveValidateButtonsPanel.add(environmentValidationPanel.getSaveFileButton());
		}
		loadSaveValidateButtonsPanel.add(Box.createRigidArea(new Dimension(15,5)));
		if (environmentValidationPanel.getValidateButton() != null) {
			loadSaveValidateButtonsPanel.add(environmentValidationPanel.getValidateButton());
		}
		bottomPanel.add(loadSaveValidateButtonsPanel);

		//this panel contains the "next" button
		JPanel nextButtonPanel = new JPanel();
		nextButtonPanel.setLayout(new BoxLayout(nextButtonPanel, BoxLayout.Y_AXIS));
		nextButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		bottomPanel.add(nextButtonPanel);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
		nextButtonPanel.add(panel);
		
		previousButton = GUIUtils.getButton(BuildMain.getClass(), "Previous", SwiftBuildConstants.IMAGE_BACK);
		previousButton.setEnabled(false);
		panel.add(previousButton);
		panel.add(nextButton);
		
		nextButton.setEnabled(true);
		BuildMain.setRunSqlScriptItemEnabled(!isValidationMandatory());
	}

	public EnvironmentExtraPropertiesFrame getEnvironmentExtraPropertiesFrame()
	{
		return environmentExtraPropertiesFrame;
	}

	public boolean isValidationMandatory()
	{
		try
		{
			return Config.isEnvironmentValidationMandatory();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return true;
		}
	}

	public void openPrivateKeyFileChooser() throws Exception
	{
		String privateKeyFileLocation = fldScriptsSSHPrivateKeyLocationField.getText();
		if ( privateKeyFileLocation != null && !privateKeyFileLocation.equals("") )
		{
			File file = new File(privateKeyFileLocation);
			fldScriptsSSHPrivateKeyLocationFileChooser.setSelectedFile(file);
		}
		else
		{
			fldScriptsSSHPrivateKeyLocationFileChooser.setSelectedFile(null);
		}
		int returnVal = fldScriptsSSHPrivateKeyLocationFileChooser.showDialog(this, "Open");
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			File file = fldScriptsSSHPrivateKeyLocationFileChooser.getSelectedFile();
			fldScriptsSSHPrivateKeyLocationField.setText(file.getAbsolutePath());
		}
	}

	public JComboBox getTransferProtocolComboBox()
	{
		return transferProtocolComboBox;
	}

	public void clickNextButtonActionPerformed()
	{
		BuildMain.switchToPanel(SwiftBuildConstants.PANEL_HTML_VALIDATION);
		
		if (environmentValidationPanel.getLoadedFile() != null) {
			BuildMain.setInformationLabelText(environmentValidationPanel.getLoadedFile().getName(), BuildMain.getBweNameLabelIndex());
		}
		
	}

    public void setNextButton(boolean isEnabled)
	{
		nextButton.setEnabled(isEnabled);
	}

	public void enableComponents()
	{
		environmentValidationPanel.enableComponents();
	}

	public EnvironmentValidationPanel getEnvironmentValidationPanel()
	{
		return environmentValidationPanel;
	}

	public BuildMain getBuildMain()
	{
		return BuildMain;
	}

	public boolean loadDefaultEnvironment() throws Exception
	{
		boolean propagateException = false;
		try
		{
			if (Config.getBuildTerminateAfterFailedInjection() && Config.isAutomatedRun()) {
				propagateException = true;
			}			
			File file = Config.getBuildEnvironmentFile();
			if (file == null && Config.isAutomatedRun()) {
				throw new Exception(Config.BUILD_ENVIRONMENT_FILE+" property in "+UtilsConstants.ENGINE_PROPERTIES_FILE_NAME+" file is required for automated operation");
			}
			
			if (Config.isAutomatedRun() && Config.getBuildInjectorsPackageFile() == null) {
				throw new Exception(Config.BUILD_INJECTORS_PACKAGE_FILE+" property in "+UtilsConstants.ENGINE_PROPERTIES_FILE_NAME+" file is required for automated operation");
			}
			
			if (file != null) {
				environmentValidationPanel.loadEnvironment(file);
			}

			Config.validateConfig();

			if (file != null) {
				environmentValidationPanel.getLoadEnvironmentFileButtonAction().beforeExecuteAction(environmentValidationPanel);
				environmentValidationPanel.getLoadEnvironmentFileButtonAction().executeAction(environmentValidationPanel);
				environmentValidationPanel.getLoadEnvironmentFileButtonAction().afterExecuteAction(environmentValidationPanel, null);
			}

			return true;
		}
		catch ( Exception e )
		{
			if (propagateException) {
				throw e;
			}
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), "Error loading default file: " + e.getMessage());
			return false;
		}
	}

	public JButton getNextButton()
	{
		return nextButton;
	}

	public String getEBSVersion()
	{
		return ebsVersion;
	}

	public boolean isTransferProtocolSSH()
	{
		return this.isTransferProtocolPasswordBasedSSH() || this.isTransferProtocolPrivateKeyBasedSSH();
	}
	
	public boolean isTransferProtocolPasswordBasedSSH() {
		return SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.valueOfAcceptsNull(environmentValidationPanel.getEnvironmentPropertiesMap()
				.get(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY)));
	}
	
	public boolean isTransferProtocolPrivateKeyBasedSSH() {
		return SwiftBuildConstants.TRANSFER_PROTOCOL.SFTPK.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.valueOfAcceptsNull(environmentValidationPanel.getEnvironmentPropertiesMap()
				.get(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY)));
	}	

	public boolean isTransferProtocolFTP()
	{
		return SwiftBuildConstants.TRANSFER_PROTOCOL.FTP.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.valueOfAcceptsNull(environmentValidationPanel.getEnvironmentPropertiesMap()
				.get(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY)));
	}
	
	public boolean isTransferProtocolFile() {
		return SwiftBuildConstants.TRANSFER_PROTOCOL.FILE.equals(SwiftBuildConstants.TRANSFER_PROTOCOL.valueOfAcceptsNull(environmentValidationPanel.getEnvironmentPropertiesMap()
				.get(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY)));	
	}

	public SwiftBuildConstants.TRANSFER_PROTOCOL getFLDScriptsTransferProtocol()
	{
		return SwiftBuildConstants.TRANSFER_PROTOCOL.valueOfAcceptsNull(environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY));
	}

	public String getFLDScriptsHostName()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY);
	}

	public String getFLDScriptsHostUserName()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY);
	}

	public String getFLDScriptsHostPassword()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY);
	}

	public String getPrivateKeyFileName()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_SCRIPTS_SSH_PRIVATE_KEY_LOCATION_KEY);
	}

	public String getFLDScriptsFolder()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_SCRIPTS_FOLDER_KEY);
	}

	public String getFLDScriptsLogFolder()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_SCRIPTS_LOG_FOLDER_KEY);
	}

	public String getFLDScriptsURL()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_SCRIPTS_URL_KEY);
	}

	public String getFLDFormUserName()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_FORM_USER_NAME_KEY);
	}

	public String getFLDFormPassword()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(SwiftBuildConstants.FLD_FORM_PASSWORD_KEY);
	}

	public String getDatabaseHostName()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(EnvironmentPropertyConstants.DATABASE_HOST_NAME_KEY);
	}

	public String getDatabasePortNumber()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(EnvironmentPropertyConstants.DATABASE_PORT_NUMBER_KEY);
	}

	public String getDatabaseSID()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(EnvironmentPropertyConstants.DATABASE_SID_KEY);
	}

	public String getDatabaseUserName()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(EnvironmentPropertyConstants.DATABASE_USER_NAME_KEY);
	}

	public String getDatabasePassword()
	{
		return environmentValidationPanel.getEnvironmentPropertiesMap().get(EnvironmentPropertyConstants.DATABASE_PASSWORD_KEY);
	}

    @Override
    public void setNextButtonIsEnabled(boolean isEnabled)
    {
        this.getNextButton().setEnabled(isEnabled);
        swiftBuildHtmlValidationPanel.getNextButton().setEnabled(isEnabled);
    }

	@Override
	public boolean isNextButtonEnabled() {
		return this.getNextButton().isEnabled();
	}

	@Override
	public void simulateClickingNext() {
		if (!isNextButtonEnabled()) {
			return;
		}

		if (onNextButtonIsClicked == null) {
			throw new RuntimeException("the next button action has not been fully initialized");
		} else {
			onNextButtonIsClicked.actionPerformed(null);
		}

	}
	
	private SwiftBuildHtmlValidationPanel swiftBuildHtmlValidationPanel = null;
	
	public void setSwiftBuildHtmlValidationPanel(SwiftBuildHtmlValidationPanel swiftBuildHtmlValidationPanel) {
		//this method is invoked only once
		this.swiftBuildHtmlValidationPanel = swiftBuildHtmlValidationPanel;
		this.swiftBuildHtmlValidationPanel.getNextButton().setEnabled(!isValidationMandatory());
	}
	
	public SwiftBuildHtmlValidationPanel getSwiftBuildHtmlValidationPanel() {
		return this.swiftBuildHtmlValidationPanel;
	}
	
	public JLabel getMessageLabel() {
		return this.optionalLabel;
	}
	

}