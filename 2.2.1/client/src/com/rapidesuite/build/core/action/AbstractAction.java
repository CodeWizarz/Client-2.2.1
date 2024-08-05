/**************************************************************
 * $Revision: 32318 $:
 * $Author: john.snell $:
 * $Date: 2013-04-29 12:36:14 +0700 (Mon, 29 Apr 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/action/AbstractAction.java $:
 * $Id: AbstractAction.java 32318 2013-04-29 05:36:14Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.action;

import java.io.InputStream;
import java.util.Map;

import com.rapidesuite.build.core.controller.ActionCompleteEvent;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;

public abstract class AbstractAction implements ActionInterface
{

	protected Injector injector;
	protected InputStream injectorInputStream;
	protected Map<String, String> environmentProperties;
	protected ActionManager actionManager;

	public AbstractAction(Injector injector, InputStream injectorInputStream, Map<String, String> environmentProperties, ActionManager actionManager)
	{
		this.injector = injector;
		this.injectorInputStream = injectorInputStream;
		this.environmentProperties = environmentProperties;
		this.actionManager = actionManager;
	}

	public void fireActionCompletedEvent()
	{
		ActionCompleteEvent event = new ActionCompleteEvent(this);

		if ( actionManager != null )
		{
			actionManager.fireEvent(event);
		}
	}

	public void start() throws Exception
	{
		// do nothing; expecting to be overwritten
	}

	public void setGUIOutput(String output)
	{
		if ( actionManager != null )
		{
			actionManager.setOutput(output);
		}
	}

	public ActionManager getActionManager()
	{
		return actionManager;
	}

}
