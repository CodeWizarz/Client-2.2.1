/**************************************************
 * $Revision: 48277 $:
 * $Author: fajrian.yunus $:
 * $Date: 2015-07-03 12:37:57 +0700 (Fri, 03 Jul 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/environment/SwiftBuildEnvironmentLoadButtonAction.java $:
 * $Id: SwiftBuildEnvironmentLoadButtonAction.java 48277 2015-07-03 05:37:57Z fajrian.yunus $:
 */
package com.rapidesuite.build.gui.environment;

import java.util.Map;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.gui.panels.SwiftBuildPropertiesValidationPanel;
import com.rapidesuite.client.common.gui.EnvironmentValidationButtonAbstractAction;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class SwiftBuildEnvironmentLoadButtonAction extends EnvironmentValidationButtonAbstractAction
{

	private Map<String, String> properties;
	private final SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel;

	public SwiftBuildEnvironmentLoadButtonAction(SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel)
	{
		this.swiftBuildPropertiesValidationPanel = swiftBuildPropertiesValidationPanel;
	}

	public boolean beforeExecuteAction(EnvironmentValidationPanel environmentValidationPanel)
	{
		try
		{
			environmentValidationPanel.clearValueFromComponents();
			properties = FileUtils.loadBwe(environmentValidationPanel.getLoadedFile());
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), "Unexpected error when loading file: " + e.getMessage());
		}
		return true;
	}

	public void executeAction(EnvironmentValidationPanel environmentValidationPanel)
	{
		try
		{
			environmentValidationPanel.loadEnvironment(properties);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), "Unexpected error when loading file: " + e.getMessage());
		}
	}

	public void afterExecuteAction(EnvironmentValidationPanel environmentValidationPanel, NextButtonWrapper nextButtonWrapper)
	{
        swiftBuildPropertiesValidationPanel.getSwiftBuildHtmlValidationPanel().getNextButton().setEnabled(!Config.isEnvironmentValidationMandatory());
        swiftBuildPropertiesValidationPanel.getBuildMain().setRunSqlScriptItemEnabled(!Config.isEnvironmentValidationMandatory());

		if ( !swiftBuildPropertiesValidationPanel.getPrivateKeyFileName().isEmpty() )
		{
			swiftBuildPropertiesValidationPanel.getTransferProtocolComboBox().setSelectedItem(SwiftBuildConstants.CONNECTION_METHOD_SFTPK_VALUE);
		}
		// set the extra properties:
		swiftBuildPropertiesValidationPanel.getEnvironmentExtraPropertiesFrame().addExtraProperties(properties);
		if (nextButtonWrapper != null) {
			swiftBuildPropertiesValidationPanel.getBuildMain().setRunSqlScriptItemEnabled(!Config.isEnvironmentValidationMandatory());
		}
		
		swiftBuildPropertiesValidationPanel.getBuildMain().getSwiftBuildHtmlValidationPanel().setFieldValues();
	}
}