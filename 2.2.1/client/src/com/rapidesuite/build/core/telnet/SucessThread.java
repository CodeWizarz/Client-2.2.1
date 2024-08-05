/**************************************************************
 * $Revision: 35772 $:
 * $Author: john.snell $:
 * $Date: 2013-09-02 09:30:03 +0700 (Mon, 02 Sep 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/SucessThread.java $:
 * $Id: SucessThread.java 35772 2013-09-02 02:30:03Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import com.rapidesuite.client.common.util.FileUtils;

public class SucessThread extends Thread
{

	private BuilderWizardTelnet bwt;

	public SucessThread(BuilderWizardTelnet bwt)
	{
		this.bwt = bwt;
	}

	public void run()
	{
		try
		{
			ScriptThread st = bwt.getTelnet().getScript();
			FileUtils.println("STARTING THREAD SUCCESS OBSERVER...");
			while ( !st.isCompleted() )
			{
				com.rapidesuite.client.common.util.Utils.sleep(500);
			}
			FileUtils.println("SCRIPT COMPLETED... NOTIFYING THE BUILDER WIZARD");
			bwt.fireEvent();
		}
		catch ( InterruptedException e )
		{
			FileUtils.printStackTrace(e);
		}
	}

}