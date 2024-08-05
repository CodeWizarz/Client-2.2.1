package com.rapidesuite.client.common.gui;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.springframework.util.Assert;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.autoinjectors.engines.APIEngine;
import com.rapidesuite.core.CoreConstants;

public class PLSQLPackageValidationThread
{

	private PLSQLPackageValidationPanel panel;
	private Map<String,String> environmentProperties;
	public static final String VALID_STATUS="valid";
	private Map<String,File> packageNameToFileMap;
	private Map<String,Boolean> packageNameToContainsRscAuditColumnMap;
	private Properties replacementsProperties;

	public PLSQLPackageValidationThread(PLSQLPackageValidationPanel panel,
	        Map<String,String> environmentProperties,
			Map<String,File> packageNameToFileMap,
			Properties replacementsProperties,
			Map<String,Boolean> packageNameToContainsRscAuditColumnMap)
	{
		this.packageNameToFileMap=packageNameToFileMap;
		this.panel=panel;
		this.environmentProperties=environmentProperties;
		this.replacementsProperties = replacementsProperties;
		this.packageNameToContainsRscAuditColumnMap = packageNameToContainsRscAuditColumnMap;
	}

	public void installPLSQLPackage() {
		try{
		    panel.setPLSQLPackageValid(false);

		    packageNameToContainsRscAuditColumnMap.clear();

		    List<String> orderedList = new ArrayList<String>();
		    orderedList.addAll(packageNameToFileMap.keySet());
		    for ( int i = 0; i < orderedList.size(); i++ )
		    {
		        if ( orderedList.get(i).equalsIgnoreCase(APIEngine.PLSQL_PACKAGE_UTILITIES) )
		        {
		            orderedList.remove(i);
		            orderedList.add(0, APIEngine.PLSQL_PACKAGE_UTILITIES);
		        }
		    }

			int counter=0;
			boolean oneOrMorePackagesAlreadyInstalledAtCorrectVersion = false;
			boolean oneOrMorePackagesInstalledWereOutOfDate = false;
			for ( String packageName : orderedList )
			{
				counter++;
				GUIUtils.showInProgressMessage(panel.getMessageLabel(),"Please wait, installing PL/SQL package: '"+
						packageName+"' ("+counter+"/"+packageNameToFileMap.size()+") ...");
				File packageFile=packageNameToFileMap.get(packageName);

		        String sourcePLSQLPackageVersion=FileUtils.getRscPLSQLPackageSourceVersion(packageFile);
                String targetPLSQLPackageVersion=DatabaseUtils.getRscPLSQLPackageTargetVersion(environmentProperties,packageName);
                String versionDescription = "Installed on Server: " + targetPLSQLPackageVersion + ", To Install: " + sourcePLSQLPackageVersion;
                String warning = "One or more PL/SQL packages not installed.";
                int packageComparisonResult = 0;
                boolean failedToParseServerPackageVersion = false;
                if ( targetPLSQLPackageVersion != null )
                {
                    try
                    {
                        getSvnRevisionFromPackageVersion(targetPLSQLPackageVersion);
                        packageComparisonResult = comparePackageVersions(sourcePLSQLPackageVersion,targetPLSQLPackageVersion);
                    }
                    catch(NumberFormatException e)
                    {
                        failedToParseServerPackageVersion=true;
                    }
                }
                if ( failedToParseServerPackageVersion )
                {
                	int result = JOptionPane.OK_OPTION;
                	if (!Config.isAutomatedRun()) {
	                    result = JOptionPane.showConfirmDialog((Component) null, "YOU ARE ATTEMPTING TO INSTALL A VERSION OF THE PL/SQL PACKAGE '" + packageName +
	                            "'\nTHAT CONTAINS AN UNRECOGNIZED VERSION STRING:" + targetPLSQLPackageVersion,
	                            "WARNING", JOptionPane.OK_CANCEL_OPTION);
					}
                    if ( result != JOptionPane.OK_OPTION )
                    {
                        GUIUtils.showWarningMessage(panel.getMessageLabel(), warning);
                        return;
                    }
                }
                else if ( targetPLSQLPackageVersion != null && packageComparisonResult < 0 )
                {
                	int result = JOptionPane.OK_OPTION;
                	if (!Config.isAutomatedRun()) {
                        result = JOptionPane.showConfirmDialog((Component) null, "YOU ARE ATTEMPTING TO INSTALL AN OLDER VERSION OF THE PL/SQL PACKAGE '" + packageName +
                                "'\nTHAN IS PRESENTLY INSTALLED.\n" + versionDescription,
                                "WARNING", JOptionPane.OK_CANCEL_OPTION);               		
                	}
                    if ( result == JOptionPane.OK_OPTION )
                    {
                    	if (!Config.isAutomatedRun()) {
                            result = JOptionPane.showConfirmDialog((Component) null, "Are you SURE you want to replace a newer package with an older package?",
                                    "WARNING", JOptionPane.OK_CANCEL_OPTION);                    		
                    	}

                        if ( result != JOptionPane.OK_OPTION )
                        {
                            GUIUtils.showWarningMessage(panel.getMessageLabel(), warning);
                            return;
                        }
                        oneOrMorePackagesInstalledWereOutOfDate = true;
                    }
                    else
                    {
                        GUIUtils.showWarningMessage(panel.getMessageLabel(), warning);
                        return;
                    }
                }
                else if ( targetPLSQLPackageVersion != null && packageComparisonResult > 0 )
                {
                	int result = JOptionPane.OK_OPTION;
                	if (!Config.isAutomatedRun()) {
	                    result = JOptionPane.showConfirmDialog((Component) null, "The version of the PL/SQL package '" +
	                            packageName + "' on the EBS server is out of date.\nYou will need to upgrade that package in order to proceed.  Press OK to upgrade the package.\n" + versionDescription,
	                            "Notice", JOptionPane.OK_CANCEL_OPTION);
                	}
                    if ( result != JOptionPane.OK_OPTION )
                    {
                        GUIUtils.showWarningMessage(panel.getMessageLabel(), warning);
                        return;
                    }
                }


                boolean containsRscAuditColumn = DatabaseUtils.isPLSQLFileContainAuditColumns(environmentProperties, packageFile, this.replacementsProperties);
                this.packageNameToContainsRscAuditColumnMap.put(packageName, containsRscAuditColumn);

                if ( targetPLSQLPackageVersion != null && packageComparisonResult == 0 && !failedToParseServerPackageVersion )
                {
                    oneOrMorePackagesAlreadyInstalledAtCorrectVersion = true;
                    continue;
                }
                
                
                try (PrintWriter printWriter = new PrintWriter(new FileWriter(FileUtils.getLogFile(), true))) {
                    boolean printSqlToLog;
                    if (CoreConstants.SHORT_APPLICATION_NAME.reverse.equals(SwiftGUIMain.getInstance().getShortApplicationName())) {
                    	printSqlToLog = Config.getReversePrintSqlToLog();
                    } else {
                    	printSqlToLog = Utils.hasAccessToInternalStaffsOnlyFeatures();
                    }
                    DatabaseUtils.executeSqlStatements(environmentProperties,packageFile,this.replacementsProperties, false, Collections.singletonMap(printWriter, printSqlToLog), null, null);                	
                }
                
                if (!DatabaseUtils.isPackageValid(packageName, environmentProperties) )
                {
                    throw new Exception("Database reports package as invalid: '" + packageName + "'");
                }
			}

			
            if ( !oneOrMorePackagesInstalledWereOutOfDate )
            {
                GUIUtils.showSuccessMessage(panel.getMessageLabel(),"Installed SQL packages." +
                        (oneOrMorePackagesAlreadyInstalledAtCorrectVersion? " (One or more packages were already installed at the correct version level.)":""));
            }
            else
            {
                GUIUtils.showWarningMessage(panel.getMessageLabel(),"Installed SQL packages, one or more of which was OUT OF DATE.");
            }

            panel.setPLSQLPackageValid(true);
		}
		catch(Throwable e) {
			FileUtils.printStackTrace(e);
			panel.setPLSQLPackageValid(false);
			throw new Error("Unable to install.<br/>", e);
		}
	}

	private static int comparePackageVersions(String one, String two)
	{
	    Integer oneRev = getSvnRevisionFromPackageVersion(one);
	    Integer twoRev = getSvnRevisionFromPackageVersion(two);
	    return oneRev.compareTo(twoRev);
	}

	/**
	 *
	 * @param packageVersion expected format: R12.1.3 - 1.3.3.0.27912
	 * or R12.1.3 -  30626
	 * @return
	 */
	private static int getSvnRevisionFromPackageVersion(String packageVersion)
	{
	    Assert.notNull(packageVersion, "packageVersion was null");
	    final String PERIOD = ".";
	    int lastIndexOfPeriod = packageVersion.lastIndexOf(PERIOD);
	    String strRev = packageVersion.substring(lastIndexOfPeriod + PERIOD.length());
	    Integer revision = null;
	    try
	    {
	        revision = Integer.parseInt(strRev);
	    }
	    catch(NumberFormatException e)
	    {
	        final String SPACE = " ";
	        int lastIndexOfSpace = packageVersion.lastIndexOf(SPACE);
	        strRev = packageVersion.substring(lastIndexOfSpace + SPACE.length());
            revision = Integer.parseInt(strRev);
	    }
	    return revision;
	}

}