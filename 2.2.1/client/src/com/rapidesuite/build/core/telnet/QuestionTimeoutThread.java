/**************************************************************
 * $Revision: 35772 $:
 * $Author: john.snell $:
 * $Date: 2013-09-02 09:30:03 +0700 (Mon, 02 Sep 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/QuestionTimeoutThread.java $:
 * $Id: QuestionTimeoutThread.java 35772 2013-09-02 02:30:03Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import com.rapidesuite.client.common.util.FileUtils;

public class QuestionTimeoutThread extends Thread
{

	private long timeout;
	public boolean BLOCKED;

	public QuestionTimeoutThread(ScriptThread st, long timeout)
	{
		this.timeout = timeout;
	}

	public void run()
	{
		try
		{
			BLOCKED = true;
			try
			{
				if ( timeout == -1 )
					return;
				com.rapidesuite.client.common.util.Utils.sleep(timeout);
			}
			catch ( InterruptedException e )
			{
				FileUtils.printStackTrace(e);
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

}