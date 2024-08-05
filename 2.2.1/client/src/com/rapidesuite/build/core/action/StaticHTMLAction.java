/**************************************************************
 * $Revision: 39099 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-02-19 11:55:10 +0700 (Wed, 19 Feb 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/action/StaticHTMLAction.java $:
 * $Id: StaticHTMLAction.java 39099 2014-02-19 04:55:10Z fajrian.yunus $:
 **************************************************************/
package com.rapidesuite.build.core.action;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.ManualStopException;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.utils.CurrentBrowserTask;
import com.rapidesuite.build.utils.InjectorsPackageUtils;
import com.rapidesuite.build.utils.TaskListUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.UtilsConstants;

public class StaticHTMLAction extends AbstractAction
{

	private static final int BROWSER_WAITING_TIME_IN_MS = 200;

	public StaticHTMLAction(Injector injector, InputStream scriptInputStream, Map<String, String> environmentProperties, ActionManager actionManager)
	{
		super(injector, scriptInputStream, environmentProperties, actionManager);
	}

	public void start() throws Exception
	{
		actionManager.setOutput("After reading the static HTML page, please close the browser" + " to continue the execution.");

		File tempFolder = FileUtils.getTemporaryFolder();
		File targetFile = new File(tempFolder.getAbsolutePath() + UtilsConstants.FORWARD_SLASH + injector.getName());

		FileUtils.println("Creating file for static HTML: '" + targetFile + "'");

		String content = InjectorsPackageUtils.getContentFromZIPFile(actionManager.getBuildMain().getInjectorsPackageSelectionPanel().getInjectorsPackageFile(),
				injector.getName());
		StringBuffer modifiedHTMLContent = new StringBuffer("");
		modifiedHTMLContent.append(content);

		org.apache.commons.io.FileUtils.writeStringToFile(targetFile, modifiedHTMLContent.toString());
		modifiedHTMLContent = null;
		while (CurrentBrowserTask.isActive()) {
			com.rapidesuite.client.common.util.Utils.sleep(BROWSER_WAITING_TIME_IN_MS);
		}
		TaskListUtils.startBrowser(null, targetFile.getAbsolutePath(), true);
		//wait until the browser has started
		while (!CurrentBrowserTask.isActive()) {
			com.rapidesuite.client.common.util.Utils.sleep(BROWSER_WAITING_TIME_IN_MS);
		}
		//wait until the browser is closed
		while (CurrentBrowserTask.isActive()) {
			com.rapidesuite.client.common.util.Utils.sleep(BROWSER_WAITING_TIME_IN_MS);
		}
		if (actionManager.isExecutionStopped()) {
			throw new ManualStopException();
		} else {
			injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE);
			fireActionCompletedEvent();				
		}

	}

    @Override
    public void join() throws InterruptedException
    {
        // TODO Auto-generated method stub

    }

}