/**************************************************
 * $Revision: 41605 $:
 * $Author: john.snell $:
 * $Date: 2014-06-13 11:08:20 +0700 (Fri, 13 Jun 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/PLSQLPackageValidationPanel.java $:
 * $Id: PLSQLPackageValidationPanel.java 41605 2014-06-13 04:08:20Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.Map;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;

@SuppressWarnings("serial")
public class PLSQLPackageValidationPanel extends JPanel
{

	private JLabel messageLabel;
	private boolean isPLSQLPackageValid;
	private SwiftGUIMain swiftGUIMain;

	public PLSQLPackageValidationPanel(SwiftGUIMain swiftGUIMain)
	{
		createComponents();
		this.swiftGUIMain = swiftGUIMain;
	}

	public void createComponents()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		messageLabel=GUIUtils.getLabel("",true);
		messageLabel.setFont(GUIUtils.BOLD_SYSTEM_FONT);
		messageLabel.setForeground(Color.RED);

		JPanel panel= new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(messageLabel);
		this.add(panel);
	}


	public void installPLSQLPackage(final Map<String,String> environmentProperties,
			Map<String,File> packageNameToFileMap,
            Properties replacementsProperties,
            final boolean processSynchronously) throws Exception  {
	    if ( packageNameToFileMap.size() == 0 )
	    {
	        GUIUtils.showErrorMessage(messageLabel,"No PL/SQL packages to install.");
	    }
	    else
	    {
	        GUIUtils.showInProgressMessage(messageLabel,"Please wait, installing PL/SQL package(s) (0/"+packageNameToFileMap.size()+") ...");
	        try
	        {
	        	startProcess(environmentProperties,packageNameToFileMap,processSynchronously, replacementsProperties);
	        }
	        catch(Throwable t)
	        {
	        	GUIUtils.showStandardMessage(messageLabel, "");
	        	throw t;
	        }
	    }
	}

	public void startProcess(final Map<String,String> environmentProperties,
	        Map<String,File> packageNameToFileMap,
			final boolean executeSynchronously,
			Properties replacementsProperties)  {
		try{
			PLSQLPackageValidationThread bt = new PLSQLPackageValidationThread(this, environmentProperties, packageNameToFileMap, replacementsProperties, swiftGUIMain.getPackageNameToContainsRscAuditColumnMap());
			bt.installPLSQLPackage();
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
		}
	}

	public void setPLSQLPackageValid(boolean isValid) {
		isPLSQLPackageValid=isValid;
	}

	public JLabel getMessageLabel() {
		return messageLabel;
	}

	public boolean isPLSQLPackageValid() {
		return isPLSQLPackageValid;
	}
}