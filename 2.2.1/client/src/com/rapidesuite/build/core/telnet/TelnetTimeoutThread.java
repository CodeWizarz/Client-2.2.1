/**************************************************************
 * $Revision: 35772 $:
 * $Author: john.snell $:
 * $Date: 2013-09-02 09:30:03 +0700 (Mon, 02 Sep 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/TelnetTimeoutThread.java $:
 * $Id: TelnetTimeoutThread.java 35772 2013-09-02 02:30:03Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import com.rapidesuite.client.common.util.FileUtils;

public class TelnetTimeoutThread extends Thread
{

	private Telnet telnet;
	private long timeout;
	private boolean isBlocked;

	public TelnetTimeoutThread(Telnet telnet, long timeout)
	{
		this.telnet = telnet;
		this.timeout = timeout;
		isBlocked = false;
	}

	public void run()
	{
		try
		{
			try
			{
				com.rapidesuite.client.common.util.Utils.sleep(timeout);
			}
			catch ( InterruptedException e )
			{
				FileUtils.printStackTrace(e);
			}

			if ( isBlocked )
			{
				telnet.scriptBlocked();
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public boolean isBlocked()
	{
		return isBlocked;
	}

	public void setBlocked(boolean blocked)
	{
		this.isBlocked = blocked;
	}

}
