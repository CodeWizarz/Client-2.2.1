/**************************************************************
 * $Revision: 40638 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-04-21 17:57:55 +0700 (Mon, 21 Apr 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/action/SQLAction.java $:
 * $Id: SQLAction.java 40638 2014-04-21 10:57:55Z fajrian.yunus $:
 **************************************************************/
package com.rapidesuite.build.core.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.CipherOutputStream;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.ManualStopException;
import com.rapidesuite.build.core.controller.ActionManager;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.core.utility.Encryption;
import com.rapidesuite.core.utility.FileType;

public class SQLAction extends AbstractAction implements Runnable
{
    private Properties replacementProperties;
	public SQLAction(Injector injector, InputStream scriptInputStream, Map<String, String> environmentProperties, ActionManager ad,
	        Properties replacementProperties)
	{
		super(injector, scriptInputStream, environmentProperties, ad);
		this.replacementProperties = replacementProperties;
	}

	private MutableBoolean terminationCheck = new MutableBoolean(false);
    public void stop()
    {
        synchronized ( this.terminationCheck )
        {
            this.terminationCheck.setValue(true);
        }
    }
    
	public static File getFullSqlLogFile(final BuildMain BuildMain, String injectorName)
	{
		return new File(SwiftBuildFileUtils.getLogFolder(BuildMain), injectorName + ".full." + SwiftBuildConstants.INJECTOR_LOG_FILE_EXTENSION);
	}    

	public void run()
	{
	    FileOutputStream fos = null;
        try
        {
            SwiftBuildFileUtils.getLogFile(this.actionManager.getBuildMain(), injector.getName()).delete();
            File injectorTempFile = File.createTempFile(injector.getName(), FileType.SQL.getFileExtension(), Config.getTempFolder());
            fos = new FileOutputStream(injectorTempFile);
            org.apache.commons.io.IOUtils.copy(this.injectorInputStream, fos);
            org.apache.commons.io.IOUtils.closeQuietly(fos);
    
    		Map<PrintWriter, Boolean> outputLogWriterToPrintSqlStatementsMap = new HashMap<PrintWriter, Boolean>();
    		try (	PrintWriter redactedLogFilePrintWriter = new PrintWriter(new FileWriter(SwiftBuildFileUtils.getLogFile(this.actionManager.getBuildMain(), injector.getName())));
    				OutputStream fullSqlLogFileOutputStream = new FileOutputStream(getFullSqlLogFile(this.actionManager.getBuildMain(), injector.getName()));
    				OutputStream fullSqlLogCipherOutputStream = new CipherOutputStream(fullSqlLogFileOutputStream,Encryption.getEncryptedCipherInstance());
    				OutputStreamWriter fullSqlLogOutputStreamWriter = new OutputStreamWriter(fullSqlLogCipherOutputStream, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
    				PrintWriter fullSqlLogOutputPrintWriter = new PrintWriter(fullSqlLogOutputStreamWriter);) {
    			
        		outputLogWriterToPrintSqlStatementsMap.put(redactedLogFilePrintWriter, false);
        		outputLogWriterToPrintSqlStatementsMap.put(fullSqlLogOutputPrintWriter, true);    	
        		
    			DatabaseUtils.executeSqlStatements(environmentProperties, injectorTempFile, replacementProperties, true, outputLogWriterToPrintSqlStatementsMap, terminationCheck, this.actionManager);
    			injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_SUCCESS_VALUE);		
    		}
        }
        catch(ManualStopException e)
        {
            FileUtils.printStackTrace(e);
            injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_PARTIAL_COMPLETE_VALUE);
        }
        catch(Throwable t)
        {
            FileUtils.printStackTrace(t);
            injector.setStatus(SwiftBuildConstants.INJECTOR_EXECUTION_ERROR_VALUE);
        }
        finally
        {
            org.apache.commons.io.IOUtils.closeQuietly(fos);
            fireActionCompletedEvent();
        }
	}

    @Override
    public void join() throws InterruptedException
    {
        // TODO Auto-generated method stub

    }

}