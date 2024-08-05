/**************************************************
 * $Revision: 48695 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-08-04 18:12:36 +0700 (Tue, 04 Aug 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/EBSEnvironmentValidationButtonAction.java $:
 * $Id: EBSEnvironmentValidationButtonAction.java 48695 2015-08-04 11:12:36Z olivier.deruelle $:
*/
package com.rapidesuite.client.common.gui;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.utility.EnvironmentPropertyConstants;
import com.rapidesuite.reverse.ReverseMain;


public class EBSEnvironmentValidationButtonAction extends EnvironmentValidationButtonAbstractAction
{

	private final EBSPropertiesValidationPanel ebsPropertiesValidationPanel;

	public EBSEnvironmentValidationButtonAction(
			EBSPropertiesValidationPanel swiftReversePropertiesValidationPanel) {
		this.ebsPropertiesValidationPanel=swiftReversePropertiesValidationPanel;
	}

	public boolean beforeExecuteAction(EnvironmentValidationPanel environmentValidationPanel) {
		return true;
	}

	public void executeAction(EnvironmentValidationPanel environmentValidationPanel) {
		try{
			environmentValidationPanel.setValidationSuccess(false);
			Map<String, String> environmentProperties=environmentValidationPanel.getEnvironmentPropertiesMap();
			DatabaseUtils.testDatabaseConnection(environmentProperties);
			
			final String databaseUserName=environmentProperties.get(EnvironmentPropertyConstants.DATABASE_USER_NAME_KEY);
			
			String ebsVersion=DatabaseUtils.getEBSVersion(environmentProperties);

			ebsPropertiesValidationPanel.setDbEBSVersion(ebsVersion);
			ebsPropertiesValidationPanel.getSwiftGUIMain().initEnvironment();
			
			if (StringUtils.isBlank(databaseUserName)) {
				GUIUtils.showWarningMessage(environmentValidationPanel.getMessageLabel(),"Database connection established. Oracle EBS version: "+ebsVersion+UtilsConstants.DATABASE_USERNAME_STATUS.EMPTY.getMessage());
			} else if (!Utils.matchesDefaultDatabaseUsername(databaseUserName)) {
				if (ReverseMain.IS_FUSION_DB) {
					GUIUtils.showSuccessMessage(environmentValidationPanel.getMessageLabel(),"Database connection established. Oracle FUSION version: "+ebsVersion);
				}
				else {
					GUIUtils.showWarningMessage(environmentValidationPanel.getMessageLabel(),"Database connection established. Oracle EBS version: "+
							ebsVersion+UtilsConstants.DATABASE_USERNAME_STATUS.NOT_STANDARD.getMessage());
				}
			} else {
				GUIUtils.showSuccessMessage(environmentValidationPanel.getMessageLabel(),"Database connection established. Oracle EBS version: "+ebsVersion);
			}
			

			environmentValidationPanel.storeValues();

			environmentValidationPanel.setValidationSuccess(true);
			environmentValidationPanel.setPropertiesSuccessStatusLabel();
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			if (environmentValidationPanel.isValidationStarted()) {
				try {
					environmentValidationPanel.setPropertiesErrorStatusLabel();
				} catch (Exception e1) {
					FileUtils.printStackTrace(e);
				}
				GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(),"Unable to connect to the Database:<br/> "+e.getMessage());
			}
		}
		finally {
			if (environmentValidationPanel.isValidationStarted()) {
				ebsPropertiesValidationPanel.enableComponents();
				environmentValidationPanel.getValidateButton().setText("Start validation");
				environmentValidationPanel.setValidationStartedFlag(false);
			}
		}
	}

	public void afterExecuteAction(EnvironmentValidationPanel environmentValidationPanel, NextButtonWrapper nextButtonWrapper) {
	    if ( environmentValidationPanel.isValidationSuccess() )
	    {
	        nextButtonWrapper.setNextButtonIsEnabled(true);
	    }
	    else
	    {
	        nextButtonWrapper.setNextButtonIsEnabled(!Config.isEnvironmentValidationMandatory());
	    }
	}


}