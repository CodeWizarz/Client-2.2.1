/**************************************************
 * $Revision: 44740 $:
 * $Author: john.snell $:
 * $Date: 2014-11-25 16:01:56 +0700 (Tue, 25 Nov 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/EBSPropertiesValidationPanel.java $:
 * $Id: EBSPropertiesValidationPanel.java 44740 2014-11-25 09:01:56Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.util.Assert;

import com.rapidesuite.client.common.gui.EnvironmentValidationButtonAbstractAction.NextButtonWrapper;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.EnvironmentPropertyConstants;

@SuppressWarnings("serial")
public abstract class EBSPropertiesValidationPanel extends JPanel implements NextButtonWrapper
{

	private SwiftGUIMain swiftGUIMain;
	private EnvironmentValidationPanel environmentValidationPanel;
	private JButton nextButton;
	private String dbEBSVersion;
	private ActionListener onNextButtonIsClicked = null;

	public EBSPropertiesValidationPanel(SwiftGUIMain swiftGUIMain)
	{
		this.swiftGUIMain=swiftGUIMain;
		createComponents();
	}

	public void createComponents()
	{
		JPanel mainPanel= new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel , BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		add(mainPanel,BorderLayout.CENTER);

		List<EnvironmentValidationProperty> environmentProperties=new ArrayList<EnvironmentValidationProperty>();
		EnvironmentValidationProperty environmentProperty=null;

		int width=250;
		int height=23;

		JTextField tf=new CopyPasteByRightClickCapableJTextField();
		GUIUtils.setComponentDimension(tf,width,height);
		environmentProperty=new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_HOST_NAME_KEY,GUIUtils.getLabel("Database hostname:",true),tf,false);
		environmentProperties.add(environmentProperty);

		environmentProperty=new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_PORT_NUMBER_KEY,GUIUtils.getLabel("Database port number:",true),new CopyPasteByRightClickCapableJTextField(),false);
		environmentProperties.add(environmentProperty);

		environmentProperty=new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_SID_KEY,GUIUtils.getLabel("Database SID:",true),new CopyPasteByRightClickCapableJTextField(),false);
		environmentProperties.add(environmentProperty);

		environmentProperty=new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_USER_NAME_KEY,GUIUtils.getLabel("Database user name:",true),new CopyPasteByRightClickCapableJTextField(),false);
		environmentProperties.add(environmentProperty);

		environmentProperty=new EnvironmentValidationProperty(EnvironmentPropertyConstants.DATABASE_PASSWORD_KEY,GUIUtils.getLabel("Database password:",true),new CopyPasteByRightClickCapableJPasswordField(),false);
		environmentProperties.add(environmentProperty);

		Map<String,String> filesExtensionAllowed=new HashMap<String,String>();
		filesExtensionAllowed.put("bwe","BWE files");
		filesExtensionAllowed.put("reverse","Environment file");

		EBSEnvironmentLoadButtonAction swiftReverseEnvironmentLoadButtonAction=
			new EBSEnvironmentLoadButtonAction();
		EBSEnvironmentSaveButtonAction swiftReverseEnvironmentSaveButtonAction=
			new EBSEnvironmentSaveButtonAction();
		EBSEnvironmentValidationButtonAction swiftReverseEnvironmentValidationButtonAction=
			new EBSEnvironmentValidationButtonAction(this);

		Dimension scrollPaneDimension=new Dimension(500,190);
		environmentValidationPanel= new EnvironmentValidationPanel(
				"Environment validation",
				environmentProperties,
				filesExtensionAllowed,
				swiftReverseEnvironmentLoadButtonAction,
				swiftReverseEnvironmentSaveButtonAction,
				swiftReverseEnvironmentValidationButtonAction,
				null,
				scrollPaneDimension,
				this,
				this.swiftGUIMain);
		//createEmptyBorder(int top, int left, int bottom, int right)
		environmentValidationPanel.setBorder(BorderFactory.createEmptyBorder(5,0,2,0));
		//environmentValidationPanel.setBackground(java.awt.Color.blue);

		mainPanel.add(environmentValidationPanel);

		JPanel loadSaveValidateButtonsPanel = new JPanel();
		loadSaveValidateButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		loadSaveValidateButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		loadSaveValidateButtonsPanel.setBorder(BorderFactory.createEmptyBorder(20,5,5,5));
		loadSaveValidateButtonsPanel.add(environmentValidationPanel.getLoadFileButton());
		loadSaveValidateButtonsPanel.add(Box.createRigidArea(new Dimension(15,5)));
		loadSaveValidateButtonsPanel.add(environmentValidationPanel.getSaveFileButton());
		loadSaveValidateButtonsPanel.add(Box.createRigidArea(new Dimension(15,5)));
		loadSaveValidateButtonsPanel.add(environmentValidationPanel.getValidateButton());

		mainPanel.add(loadSaveValidateButtonsPanel);

		nextButton=GUIUtils.getButton(this.getClass(),"Next","/images/forward16.gif");
		onNextButtonIsClicked = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				clickNextButtonActionPerformed();
			}
		};
		nextButton.addActionListener(onNextButtonIsClicked);

		JPanel southPanel= new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel , BoxLayout.X_AXIS));
		//(int top, int left, int bottom, int right)
		southPanel.setBorder(BorderFactory.createEmptyBorder(50,0,10,0));
		southPanel.add(nextButton);
		mainPanel.add(southPanel);

		nextButton.setEnabled(!Config.isEnvironmentValidationMandatory());
	}

	public void enableComponents() {
		environmentValidationPanel.enableComponents();
	}

	public EnvironmentValidationPanel getEnvironmentValidationPanel() {
		return environmentValidationPanel;
	}

	public void validateEnvironment(File file) throws Exception
	{
		environmentValidationPanel.loadEnvironment(file);
		environmentValidationPanel.actionPerformedValidationButton(false);
	}

	public void loadDefaultEnvironment(File defaultEnvironmentFileName)
	{
		try{
			if (defaultEnvironmentFileName!=null){
				environmentValidationPanel.loadEnvironment(defaultEnvironmentFileName);
			}
	         this.getNextButton().setEnabled(!Config.isEnvironmentValidationMandatory());
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(),"Error loading default file: "+e.getMessage());
		}
	}

	public JButton getNextButton() {
		return nextButton;
	}

	public SwiftGUIMain getSwiftGUIMain() {
		return swiftGUIMain;
	}	
	
	public boolean matchesTheEBSDBVersion(final String inventoriesPackageFileTargetVersion) throws Exception {
		Assert.notNull(this.dbEBSVersion, "DB EBS version must not be null");
		
		if (inventoriesPackageFileTargetVersion == null) {
			return false;
		}
		
		com.rapidesuite.client.ebsVersionConversion.PatternsDocument patternsDocument = 
				com.rapidesuite.client.ebsVersionConversion.PatternsDocument.Factory.parse(Config.getEbsVersionMappingFile());
		
		XmlOptions validateOptions = new XmlOptions();
		List<XmlError> xmlErrors = new ArrayList<XmlError>();
		validateOptions.setErrorListener(xmlErrors);
		if (!patternsDocument.validate(validateOptions)) {
			StringBuffer xmlErrorMessages = new StringBuffer();
			for (int i = 0 ; i < xmlErrors.size() ; i++) {
				if (i > 0) {
					xmlErrorMessages.append("<br/>");
				}
				xmlErrorMessages.append(xmlErrors.get(i).getMessage());
			}
			throw new XmlException(xmlErrorMessages.toString());
		}
		
		final com.rapidesuite.client.ebsVersionConversion.Pattern patterns[] = patternsDocument.getPatterns().getPatternArray();
		for (final com.rapidesuite.client.ebsVersionConversion.Pattern pattern : patterns) {
			if (inventoriesPackageFileTargetVersion.equalsIgnoreCase(pattern.getInventoriesPackageEBSDBVersion())) {
				for (final com.rapidesuite.client.ebsVersionConversion.EBSDBVersionPattern ebsDbVersionPattern : pattern.getEBSDBVersionPatternArray()) {
					if (CoreUtil.sqlLike(this.dbEBSVersion, ebsDbVersionPattern.getValue())) {
						return true;
					}
				}
			}
		}		
		
		return false;
	}
	
	public String getDbEBSVersion() {
		return this.dbEBSVersion;
	}

	public void setDbEBSVersion(String dbEBSVersion) {
		this.dbEBSVersion = dbEBSVersion;
	}

	public void clickNextButtonActionPerformed()
	{
		try{
			if (Config.isEnvironmentValidationMandatory() && getEnvironmentValidationPanel().hasChangedValues()) {
				GUIUtils.popupErrorMessage("Changes were made, you must validate the environment first.");
				setNextButtonIsEnabled(!Config.isEnvironmentValidationMandatory());
				return;
			}
			else
			{
				swiftGUIMain.switchToPanel(UtilsConstants.PANEL_PACKAGES_SELECTION);
				if (this.environmentValidationPanel.getLoadedFile() != null) {
					swiftGUIMain.setInformationLabelText(this.environmentValidationPanel.getLoadedFile().getName(), swiftGUIMain.getBweNameLabelIndex());
				}
			}
	        FileUtils.safeLogEnvironmentProperties(swiftGUIMain.getEnvironmentPropertiesMap());
		}
		catch(Exception ex) {
			FileUtils.printStackTrace(ex);
		}
	}

	public void validateDefaultEnvironment(File defaultEnvironmentFile)
	{
		try{
			if (defaultEnvironmentFile!=null){
				loadDefaultEnvironment(defaultEnvironmentFile);
				getEnvironmentValidationPanel().actionPerformedValidationButton(false);
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.showErrorMessage(getEnvironmentValidationPanel().getMessageLabel(),"Error validating default file: "+e.getMessage());
		}
	}

    @Override
    public void setNextButtonIsEnabled(boolean isEnabled)
    {
        this.getNextButton().setEnabled(isEnabled);
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

}