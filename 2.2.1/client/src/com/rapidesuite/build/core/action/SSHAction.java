package com.rapidesuite.build.core.action;

import java.io.InputStream;
import java.util.Map;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.shell.SSHShellRobot;
import com.rapidesuite.build.core.shell.SSHTerminal;
import com.rapidesuite.client.common.util.FileUtils;

public class SSHAction extends AbstractAction
{

	private SSHShellRobot robot;

	public SSHAction(Injector injector, InputStream injectorInputStream, Map<String, String> environmentProperties, ActionManager ad)
	{
		super(injector, injectorInputStream, environmentProperties, ad);
	}

	public String getActionOutput()
	{
		String actionOutput = null;
		return actionOutput;
	}

	public void setOutput(String text)
	{
		if ( actionManager != null )
		{
			actionManager.setOutput(text);
		}
	}

	public void stopAll()
	{
		if ( actionManager != null )
		{
			actionManager.getInjectorsPackageExecutionPanel().stopExecution();
			actionManager.stopExecution();
		}
	}

	public void manualStop()
	{
		FileUtils.println("SSH MANUAL STOP INITIATED...");
		if ( actionManager != null )
		{
			actionManager.setOutput("SSH MANUAL STOP INITIATED...");
		}
		if ( robot != null )
		{
			robot.setReceivedText("\nSSH MANUAL STOP INITIATED...");
			robot.disconnect();
		}
	}

	public void errorStop()
	{
		FileUtils.println("SSH ERROR...");
		if ( actionManager != null )
		{
			actionManager.setOutput("SSH ERROR...");
		}
		if ( robot != null )
		{
			robot.setReceivedText("\nSSH ERROR...");
			robot.disconnect();
		}
	}

	public void start() throws Exception
	{
		String host = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY);
		if ( host == null || host.equals("") )
			throw new Exception("Error: the '" + SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY + "' property is missing.");

		int portNumber = 22;

		String login = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY);
		if ( login == null || login.equals("") )
		{
			throw new Exception("Error: the '" + SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY + "' property is missing.");
		}
		String password = environmentProperties.get(SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY);
		if ( password == null || password.equals("") )
		{
			throw new Exception("Error: the '" + SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY + "' property is missing.");
		}
		try
		{
			actionManager.setOutput("Executing SSH injector: '" + injector.getName() + "'");
			actionManager.updateStatus("In process");
			robot = new SSHShellRobot(actionManager.getBuildMain(), host, portNumber, login, password, this);
			robot.connect();
			actionManager.setOutput("Playback commands...");
			robot.executeQuestions(injectorInputStream);
			SSHShellRobot.streamClosedAfterFinish = true;
			String tmp = robot.getTerminalText();
			tmp = tmp.substring(tmp.length() - 3);
			while ( tmp.indexOf(SSHShellRobot.prompt) == -1 )
			{
				if ( actionManager != null && actionManager.isExecutionStopped() )
				{
					manualStop();
					return;
				}
				com.rapidesuite.client.common.util.Utils.sleep(1000);
				tmp = robot.getTerminalText();
				tmp = tmp.substring(tmp.length() - 3);
			}
			actionManager.setOutput("SSH injector completed.");
			String suc = "\n*****************************************\n";
			suc = suc + "*****************************************\n";
			suc = suc + " SCRIPT COMPLETED... \n";
			suc = suc + " SESSION DISCONNECTED... \n";
			suc = suc + "*****************************************\n";
			suc = suc + "*****************************************\n";
			robot.setReceivedText(suc);
			injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE);
			robot.fireEvent();
		}
		finally
		{
			if ( robot != null )
			{
				robot.disconnect();
			}
			SSHShellRobot.streamClosedAfterFinish = false;
		}
	}

	public SSHTerminal getTerminal()
	{
		return robot.getTerminal();
	}

    @Override
    public void join() throws InterruptedException
    {
        // TODO Auto-generated method stub
        
    }

}