/**************************************************************
 * $Revision: 42119 $:
 * $Author: john.snell $:
 * $Date: 2014-07-08 16:25:39 +0700 (Tue, 08 Jul 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/action/TelnetAction.java $:
 * $Id: TelnetAction.java 42119 2014-07-08 09:25:39Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.action;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.telnet.BuilderWizardTelnet;
import com.rapidesuite.build.core.telnet.Question;
import com.rapidesuite.build.core.telnet.QuestionnaireParser;
import com.rapidesuite.build.core.telnet.Questions;
import com.rapidesuite.build.core.telnet.Response;
import com.rapidesuite.client.common.util.FileUtils;

public class TelnetAction extends AbstractAction
{

	private BuilderWizardTelnet bwTelnet;

	public TelnetAction(Injector injector, InputStream scriptInputStream, Map<String, String> environmentProperties, ActionManager ad)
	{
		super(injector, scriptInputStream, environmentProperties, ad);
	}

	public String getActionOutput()
	{
		String actionOutput = bwTelnet.getTerminal().getText();
		return actionOutput;
	}

	public void manualStop()
	{
		if ( actionManager != null )
		{
			actionManager.setOutput("TELNET MANUAL STOP INITIATED...");
		}
		bwTelnet.manualStop();
	}

	public void stopAll()
	{
		if ( actionManager != null )
		{
			actionManager.stopExecution();
		}
	}

	public void start() throws Exception
	{
		String host = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY);
		if ( host == null || host.equals("") )
		{
			throw new Exception(String.format("INJECTOR FAILURE: the '%s' property is missing.", SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY));
		}
		int portNumber = 23;
		String login = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY);
		if ( login == null || login.equals("") )
		{
			throw new Exception(String.format("INJECTOR FAILURE: the '%s' property is missing.", SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY));
		}
		String password = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY);
		if ( password == null || password.equals("") )
		{
			throw new Exception(String.format("INJECTOR FAILURE: the '%s' property is missing.", SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY));
		}
		Questions questions = null;
		try
		{
			questions = QuestionnaireParser.parseDocument(injectorInputStream);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			throw new Exception("INJECTOR FAILURE: the TELNET injector file cannot be parsed. " + "The following error occurred:" + e.getMessage());
		}
		finally
		{
			IOUtils.closeQuietly(injectorInputStream);
		}

		Question question = new Question();
		question.setPattern("login:");
		Response response = new Response();
		response.setResponse(login);
		question.setResponse(response);
		questions.getListOfQuestion().add(0, question);

		question = new Question();
		question.setPattern(":");
		response = new Response();
		response.setResponse(password);
		question.setResponse(response);
		questions.getListOfQuestion().add(1, question);

		questions.setTree();
		bwTelnet = new BuilderWizardTelnet(host, portNumber, this);
		bwTelnet.executeScript(questions);
		injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE);
	}

    @Override
    public void join() throws InterruptedException
    {
        // TODO Auto-generated method stub
        
    }

}