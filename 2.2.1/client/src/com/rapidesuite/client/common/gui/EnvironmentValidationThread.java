package com.rapidesuite.client.common.gui;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class EnvironmentValidationThread implements Runnable
{
	private EnvironmentValidationPanel environmentValidationPanel;

	public EnvironmentValidationThread(EnvironmentValidationPanel environmentValidationPanel)
	{
		this.environmentValidationPanel=environmentValidationPanel;
	}

	public void run() {
		synchronized(EnvironmentValidationThread.class) {
			try{
				FileUtils.safeLogEnvironmentProperties(this.environmentValidationPanel.getSwiftGuiMain().getEnvironmentPropertiesMap());
				EnvironmentValidationButtonAbstractAction validateButtonAction=environmentValidationPanel.getValidateButtonAction();
				boolean isActionToBeExecuted=validateButtonAction.beforeExecuteAction(environmentValidationPanel);
				if (isActionToBeExecuted) {
					validateButtonAction.executeAction(environmentValidationPanel);
					validateButtonAction.afterExecuteAction(environmentValidationPanel, environmentValidationPanel.getNextButtonWrapper());
				}
			}
			catch(Exception e) {
				GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(),
						"Error validating the environment: "+e.getMessage());
			}
		}
	}

}