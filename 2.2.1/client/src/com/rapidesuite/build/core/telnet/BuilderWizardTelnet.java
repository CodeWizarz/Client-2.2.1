/**************************************************************
 * $Revision: 32318 $:
 * $Author: john.snell $:
 * $Date: 2013-04-29 12:36:14 +0700 (Mon, 29 Apr 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/BuilderWizardTelnet.java $:
 * $Id: BuilderWizardTelnet.java 32318 2013-04-29 05:36:14Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import com.rapidesuite.build.core.action.ActionInterface;
import com.rapidesuite.build.core.action.TelnetAction;

public class BuilderWizardTelnet
{

	private Terminal terminal;
	private ScriptThread script;
	private Telnet telnet;
	private SucessThread successThread;
	private ActionInterface actionInterface;

	public BuilderWizardTelnet(String host, int port, ActionInterface actionInterface) throws Exception
	{
		telnet = new Telnet(host, port);
		telnet.connect();
		this.actionInterface = actionInterface;
		terminal = new Terminal(this, "Telnet terminal");
	}

	public void cloneTelnet() throws Exception
	{
		telnet = new Telnet(telnet.getSocket().getHost(), telnet.getSocket().getPort());
		telnet.setScript(script);
	}

	public void setGUIOutput(String output)
	{
		if ( actionInterface != null )
		{
			actionInterface.setGUIOutput(output);
		}
	}

	public void fireEvent()
	{
		if ( actionInterface != null )
		{
			actionInterface.fireActionCompletedEvent();
		}
	}

	public void stopAll()
	{
		if ( actionInterface != null )
		{
			TelnetAction telnetAction = (TelnetAction) actionInterface;
			telnetAction.stopAll();
		}
	}

	public void manualStop()
	{
		telnet.manualStop();
	}

	public Terminal getTerminal()
	{
		return terminal;
	}

	public Telnet getTelnet()
	{
		return telnet;
	}

	public void executeScript(Questions questions) throws Exception
	{
		script = new ScriptThread(telnet, this, questions);
		telnet.setScript(script);
		successThread = new SucessThread(this);
		successThread.start();
		script.start();
	}

}
