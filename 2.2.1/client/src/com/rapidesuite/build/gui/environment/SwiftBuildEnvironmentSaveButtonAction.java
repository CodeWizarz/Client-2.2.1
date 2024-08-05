/**************************************************
 * $Revision: 47766 $:
 * $Author: fajrian.yunus $:
 * $Date: 2015-05-29 17:41:06 +0700 (Fri, 29 May 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/environment/SwiftBuildEnvironmentSaveButtonAction.java $:
 * $Id: SwiftBuildEnvironmentSaveButtonAction.java 47766 2015-05-29 10:41:06Z fajrian.yunus $:
 */
package com.rapidesuite.build.gui.environment;

import java.util.Map;

import com.rapidesuite.build.gui.panels.SwiftBuildPropertiesValidationPanel;
import com.rapidesuite.client.common.gui.EnvironmentValidationButtonAbstractAction;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class SwiftBuildEnvironmentSaveButtonAction extends EnvironmentValidationButtonAbstractAction
{

	protected Map<String, String> fileProperties;
	private final SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel;

	public SwiftBuildEnvironmentSaveButtonAction(SwiftBuildPropertiesValidationPanel swiftBuildPropertiesValidationPanel)
	{
		this.swiftBuildPropertiesValidationPanel = swiftBuildPropertiesValidationPanel;
	}

	public boolean beforeExecuteAction(EnvironmentValidationPanel environmentValidationPanel)
	{
		fileProperties = environmentValidationPanel.getEnvironmentPropertiesMap();
		// set the extra properties:
		fileProperties.putAll(swiftBuildPropertiesValidationPanel.getEnvironmentExtraPropertiesFrame().getExtraProperties());
		return true;
	}

	public void executeAction(EnvironmentValidationPanel environmentValidationPanel)
	{
		try
		{
			environmentValidationPanel.saveEnvironment(fileProperties);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), "Unexpected error when saving file: " + e.getMessage());
		}
	}

	public void afterExecuteAction(EnvironmentValidationPanel environmentValidationPanel, NextButtonWrapper nextButtonWrapper)
	{

	}

}