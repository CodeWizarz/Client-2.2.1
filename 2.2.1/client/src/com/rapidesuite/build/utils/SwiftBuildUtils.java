/**************************************************
 * $Revision: 38577 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-01-28 13:19:50 +0700 (Tue, 28 Jan 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/utils/SwiftBuildUtils.java $:
 * $Id: SwiftBuildUtils.java 38577 2014-01-28 06:19:50Z fajrian.yunus $:
 */

package com.rapidesuite.build.utils;


import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.client.common.gui.EnvironmentValidationPanel;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

public class SwiftBuildUtils
{
	public static String replaceSpecialCharacters(String value)
	{
		if ( value == null )
			return value;

		String tmp = value;
		tmp = tmp.replaceAll("\\\\", "\\\\\\\\");
		tmp = tmp.replaceAll("'", "\\\\'");
		tmp = tmp.replaceAll("\\\\", "\\\\\\\\");
		tmp = tmp.replaceAll("\"", "\\\\\"");

		return tmp;
	}

	public static void pause(int pause, String message, EnvironmentValidationPanel environmentValidationPanel, ActionManager actionManager)
	{
		try
		{
			if ( pause > 0 )
			{
				String fullMessage = "Pausing " + pause / 1000 + " secs " + message;
				if ( environmentValidationPanel != null )
				{
					GUIUtils.showInProgressMessage(environmentValidationPanel.getMessageLabel(), fullMessage);
				}
				else
				{
					actionManager.setOutput(fullMessage);
				}
				com.rapidesuite.client.common.util.Utils.sleep(pause);
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}	

}