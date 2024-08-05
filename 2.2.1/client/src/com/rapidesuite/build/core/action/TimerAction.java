/**************************************************************
 * $Revision: 35772 $:
 * $Author: john.snell $:
 * $Date: 2013-09-02 09:30:03 +0700 (Mon, 02 Sep 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/action/TimerAction.java $:
 * $Id: TimerAction.java 35772 2013-09-02 02:30:03Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.action;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.client.common.util.FileUtils;

public class TimerAction extends AbstractAction implements Runnable
{

	public TimerAction(Injector injector, InputStream scriptInputStream, Map<String, String> environmentProperties, ActionManager ad)
	{
		super(injector, scriptInputStream, environmentProperties, ad);
	}

	private Thread mainThread = null;

	public void start() throws Exception
	{
	    mainThread = new Thread(this);
	    mainThread.start();
	}

	public void interrupt() {
		injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_MANUAL_STOP_VALUE);
		mainThread.interrupt();
	}

	public void run()
	{
		Properties properties = new Properties();
		long timeInSecond = 0L;
		try
		{
			properties.load(injectorInputStream);
			timeInSecond = Long.parseLong(properties.getProperty(SwiftBuildConstants.TIMER_PROPERTY_NAME, "0"));
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
		finally
		{
			IOUtils.closeQuietly(injectorInputStream);
		}

		if ( timeInSecond > 0 )
		{
			try
			{
				actionManager.setOutput(SwiftBuildConstants.WAITING_MESSAGE.replaceAll(SwiftBuildConstants.TIME_IN_SECOND_TOKEN, String.valueOf(timeInSecond)));
				com.rapidesuite.client.common.util.Utils.sleep(timeInSecond * 1000);
				actionManager.setOutput(SwiftBuildConstants.RESUMING_MESSAGE);
			}
			catch ( InterruptedException e )
			{
				FileUtils.printStackTrace(e);
			}
		}
		fireActionCompletedEvent();
	}

    @Override
    public void join() throws InterruptedException
    {
        if ( null != this.mainThread )
        {
            this.mainThread.join();
        }
    }

}