/**************************************************
 * $Revision: 32237 $:
 * $Author: john.snell $:
 * $Date: 2013-04-19 11:48:06 +0700 (Fri, 19 Apr 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/EBSEnvironmentSaveButtonAction.java $:
 * $Id: EBSEnvironmentSaveButtonAction.java 32237 2013-04-19 04:48:06Z john.snell $:
*/
package com.rapidesuite.client.common.gui;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;


public class EBSEnvironmentSaveButtonAction extends EnvironmentValidationButtonAbstractAction
{

	public boolean beforeExecuteAction(EnvironmentValidationPanel environmentValidationPanel) {
		return true;
	}

	public void executeAction(EnvironmentValidationPanel environmentValidationPanel) {
		try{
			environmentValidationPanel.saveEnvironment(environmentValidationPanel.getEnvironmentPropertiesMap());
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(),"Unexpected error when saving file: "+e.getMessage());
		}
	}

	public void afterExecuteAction(EnvironmentValidationPanel environmentValidationPanel, NextButtonWrapper nextButtonWrapper) {
	}

}