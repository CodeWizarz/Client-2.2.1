/**************************************************
 * $Revision: 46653 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-03-18 17:28:18 +0700 (Wed, 18 Mar 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/gui/environment/EnableDiagnosticsButtonAction.java $:
 * $Id: EnableDiagnosticsButtonAction.java 46653 2015-03-18 10:28:18Z olivier.deruelle $:
 */
package com.rapidesuite.build.gui.environment;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.gui.panels.EnableDiagnosticsPanel;
import com.rapidesuite.build.gui.panels.EnableDiagnosticsPanel.ECB_STATUS;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.utility.CoreUtil;

public class EnableDiagnosticsButtonAction extends SwiftBuildEnvironmentValidationButtonAction
{
    private EnableDiagnosticsPanel enableDiagnosticsPanel;

	public EnableDiagnosticsButtonAction(EnableDiagnosticsPanel enableDiagnosticsPanel)
	{
		super(enableDiagnosticsPanel);
		this.enableDiagnosticsPanel = enableDiagnosticsPanel;
	}

	public boolean beforeExecuteAction(EnvironmentValidationPanel environmentValidationPanel)
	{
		return true;
	}

	public void afterExecuteAction(EnvironmentValidationPanel environmentValidationPanel,NextButtonWrapper nextButtonWrapper)
	{
        enableDiagnosticsPanel.getEnableButton().setText(EnableDiagnosticsPanel.BUTTON_TEXT_ENABLE);
        enableDiagnosticsPanel.setEnableButtonEnableStatus();
	}


	public void executeAction(EnvironmentValidationPanel environmentValidationPanel)
	{
		try
		{
			this.environmentValidationPanel = environmentValidationPanel;
			environmentValidationPanel.setPropertiesResetStatusLabel();
			environmentValidationPanel.setValidationSuccess(false);
			enableDiagnosticsPanel.getEnableButton().setText(EnableDiagnosticsPanel.BUTTON_TEXT_STOP);

			if ( this.enableDiagnosticsPanel.getUserNames().size() > 0 )
			{
			    boolean result = validate();
	            environmentValidationPanel.setValidationSuccess(result);
			}
			else
			{
                environmentValidationPanel.setValidationSuccess(true);
			}
		}
		catch ( Throwable e )
		{
            environmentValidationPanel.setValidationSuccess(false);
			FileUtils.printStackTrace(e);
			if ( environmentValidationPanel.isValidationStarted() )
			{
				GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), "Unable to validate the environment:<br/>" + CoreUtil.getAllThrowableMessagesHTML(e));
			}
		}
		finally
		{
			if ( environmentValidationPanel.isValidationStarted() )
			{
				swiftBuildPropertiesValidationPanel.enableComponents();
				this.enableDiagnosticsPanel.getEnableButton().setText(EnableDiagnosticsPanel.BUTTON_TEXT_ENABLE);
				environmentValidationPanel.setValidationStartedFlag(false);
			}
		}
	}

    private ECB_STATUS[] lastStatusObject = null;
    
	public ECB_STATUS[] getLastStatusObject()
    {
        return lastStatusObject;
    }

    public void setLastStatusObject(ECB_STATUS[] lastStatusObject)
    {
        this.lastStatusObject = lastStatusObject;
    }

    @Override
	public boolean validate() throws Exception
	{
        Map<String,String> environmentProperties = this.swiftBuildPropertiesValidationPanel.getBuildMain().getEnvironmentProperties();

        String userName = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY);
		String password = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY);
		String privateKeyFileLocation = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_SSH_PRIVATE_KEY_LOCATION_KEY);
		String hostName = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY);
		SwiftBuildConstants.TRANSFER_PROTOCOL connectionMethod = SwiftBuildConstants.TRANSFER_PROTOCOL.valueOfAcceptsNull(environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY));
		String fldScriptsFolder = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_FOLDER_KEY);
		String fldScriptsLogFolder = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_LOG_FOLDER_KEY);
        String fldScriptsUrl = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_URL_KEY);
		String formsUserName = environmentProperties.get(SwiftBuildConstants.FLD_FORM_USER_NAME_KEY);
        String formsPassword = environmentProperties.get(SwiftBuildConstants.FLD_FORM_PASSWORD_KEY);
        String responsibility = environmentProperties.get(SwiftBuildConstants.FLD_FORM_RESPONSIBILITY_KEY);

        try
        {
            //Assert.isTrue(!StringUtils.isEmpty(formsUserName), "Environment must specify a forms user name.");
            //Assert.isTrue(!StringUtils.isEmpty(formsPassword), "Environment must specify a forms user password.");
            Assert.isTrue(!StringUtils.isEmpty(responsibility), "Please return to the first panel to enter a valid responsibility that have access to System Profile.");
        }
        catch(Throwable t)
        {
            GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), t.getMessage());
            return false;
        }

        ActionManager actionManager=this.swiftBuildPropertiesValidationPanel.getBuildMain().getInjectorsExecutionPanel().getActionManager();
        
		boolean success = true;
		String lastReturnMessage = null;
		SortedMap<String, String> restrictToTheseUsersPasswords=this.enableDiagnosticsPanel.getRestrictToTheseUsersPasswords();
		Iterator<String> iterator=restrictToTheseUsersPasswords.keySet().iterator();
	    while (iterator.hasNext() ) {
	      	String formsUserNameToEnable=iterator.next();
			String formsPasswordToEnable=restrictToTheseUsersPasswords.get(formsUserNameToEnable);
			
		    String fldScriptContent = generateFldScript(formsUserNameToEnable, formsPasswordToEnable, responsibility, formsUserNameToEnable);
		    //System.out.println("fldScriptContent:"+fldScriptContent);
		    String fldScriptFilename = Utils.getUniqueFilename("enableCheckboxes", ".fld");

		    lastReturnMessage = validateFLDFormsPlayback(
		    		actionManager,
		    		hostName,
	                userName,
	                password,
	                privateKeyFileLocation,
	                connectionMethod,
	                fldScriptsFolder,
	                fldScriptsLogFolder,
	                fldScriptsUrl,
	                formsUserName,
	                formsPassword,
	                fldScriptContent,
	                fldScriptFilename);

	        success = lastReturnMessage.equalsIgnoreCase(SwiftBuildConstants.VALIDATION_SUCCESS_VALUE);
	        if ( !success )
	        {
	            lastReturnMessage = "Failed to enable injectability for user name: '" + formsUserNameToEnable + "': " + lastReturnMessage;
	            break;
	        }
		}

		if ( success )
		{
		    getLastStatusObject()[0] = ECB_STATUS.SUCCESS;
		    ((EnableDiagnosticsPanel)this.swiftBuildPropertiesValidationPanel).refreshFormsUserList(true, getLastStatusObject());
	        GUIUtils.showSuccessMessage(environmentValidationPanel.getMessageLabel(), "Users enabled for injection.");
	        return true;
		}
		else
		{
			FileUtils.println("Enabling users for injection failed:\\"+lastReturnMessage);
			GUIUtils.showErrorMessage(environmentValidationPanel.getMessageLabel(), lastReturnMessage);
			return false;
		}
	}


	private static final String REPLACEMENT_TOKEN_USER_NAME = UtilsConstants.REPLACEMENTS_DELIMITER + "USER_NAME" + UtilsConstants.REPLACEMENTS_DELIMITER;
	private static final String REPLACEMENT_TOKEN_PASSWORD = UtilsConstants.REPLACEMENTS_DELIMITER + "PASSWORD" + UtilsConstants.REPLACEMENTS_DELIMITER;
	private static final String REPLACEMENT_TOKEN_TARGET_USER_NAME = UtilsConstants.REPLACEMENTS_DELIMITER + "TARGET_USER_NAME" + UtilsConstants.REPLACEMENTS_DELIMITER;
	private static final String REPLACEMENT_TOKEN_RESPONSIBILITY = UtilsConstants.REPLACEMENTS_DELIMITER + "RESPONSIBILITY" + UtilsConstants.REPLACEMENTS_DELIMITER;

	private String generateFldScript(String formsUserName, String formsPassword, String responsibility, String formsUserNameToEnable) throws Exception
	{
	    String template = org.apache.commons.io.FileUtils.readFileToString(SwiftBuildConstants.SOURCE_FILE_FLD_ENABLE_MENU_DIAGNOSTICS);
	    template = template.replace(REPLACEMENT_TOKEN_USER_NAME, formsUserName);
        template = template.replace(REPLACEMENT_TOKEN_PASSWORD, formsPassword);
        template = template.replace(REPLACEMENT_TOKEN_RESPONSIBILITY, responsibility);
        template = template.replace(REPLACEMENT_TOKEN_TARGET_USER_NAME, formsUserNameToEnable);

	    return template;
	}


}