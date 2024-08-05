/**************************************************************
 * $Revision: 40389 $:
 * $Author: john.snell $:
 * $Date: 2014-04-04 15:40:08 +0700 (Fri, 04 Apr 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/ftp/LogsDownloader.java $:
 * $Id: LogsDownloader.java 40389 2014-04-04 08:40:08Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.ftp;

import java.util.ArrayList;
import java.util.List;

import com.rapidesuite.client.common.util.FileUtils;

public class LogsDownloader implements Runnable
{

	private String ftpHostName;
	private String ftpUserName;
	private String ftpPassword;
	private String remoteFolder;
	private List<String> fileNamesToDownload;
	private String localFolder;
	private boolean LOGS_DOWNLOAD_COMPLETED;

	public LogsDownloader(String ftpHostName, String ftpUserName, String ftpPassword, String remoteFolder, List<String> fileNamesToDownload, String localFolder)
	{
		this.ftpHostName = ftpHostName;
		this.ftpUserName = ftpUserName;
		this.ftpPassword = ftpPassword;
		this.remoteFolder = remoteFolder;
		this.fileNamesToDownload = fileNamesToDownload;
		this.localFolder = localFolder;
		LOGS_DOWNLOAD_COMPLETED = false;
	}

	public void run()
	{
		try
		{
			downloadFiles(false);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public boolean isCompleted()
	{
		return LOGS_DOWNLOAD_COMPLETED;
	}

	public void downloadFiles(boolean showError) throws Exception
	{
		try
		{
			List<String> files = new ArrayList<String>();
			for ( int i = 0; i < fileNamesToDownload.size(); i++ )
			{
				String remoteLogFileName = fileNamesToDownload.get(i);

				int fldIndex = remoteLogFileName.toLowerCase().indexOf(".fld");
				if ( fldIndex == -1 )
				{
					FileUtils.println("DOWNLOADING FLD LOG FILES: the file '" + remoteLogFileName + "' is not a FLD type, no Logs to download.");
					continue;
				}
				files.add(remoteLogFileName);
			}
			if ( files.isEmpty() )
			{
				FileUtils.println("No log files to download.");
				return;
			}

			FileUtils.println("downloading log files...");
			try
			{
				FTPManager.downloadFiles(ftpHostName, FTPManager.FTP_PORT, ftpUserName, ftpPassword, remoteFolder, localFolder, files, true);
			}
			catch ( Exception e )
			{
				FileUtils.printStackTrace(e);
				if ( showError )
					throw e;
			}
		}
		finally
		{
			LOGS_DOWNLOAD_COMPLETED = true;
		}
	}

}
