/**************************************************
 * $Revision: 41874 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-06-25 17:20:14 +0700 (Wed, 25 Jun 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/EBSEnvironmentLoadButtonAction.java $:
 * $Id: EBSEnvironmentLoadButtonAction.java 41874 2014-06-25 10:20:14Z fajrian.yunus $:
*/
package com.rapidesuite.client.common.gui;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;


public class EBSEnvironmentLoadButtonAction extends EnvironmentValidationButtonAbstractAction
{

	public boolean beforeExecuteAction(EnvironmentValidationPanel environmentValidationPanel) {
		environmentValidationPanel.clearValueFromComponents();
		return true;
	}

	public void executeAction(EnvironmentValidationPanel environmentValidationPanel) {
		try{
			environmentValidationPanel.loadEnvironment(FileUtils.loadBwe(
					environmentValidationPanel.getLoadedFile()));
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(),"Unexpected error when loading file: "+e.getMessage());
		}
	}

	public void afterExecuteAction(EnvironmentValidationPanel environmentValidationPanel, NextButtonWrapper nextButtonWrapper) {
	    nextButtonWrapper.setNextButtonIsEnabled(!Config.isEnvironmentValidationMandatory());
	}

}