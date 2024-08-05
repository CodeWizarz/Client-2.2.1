package com.rapidesuite.build.core.ssh;

import java.io.File;
import java.util.List;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.core.CoreConstants;

public class SynchronousDownloader implements Runnable
{

	private BuildMain BuildMain;
	private boolean DOWNLOAD_COMPLETED;
	private List<String> fileNamesToDownload;
	private String remoteFolder;
	private String localFolder;

	public SynchronousDownloader(BuildMain BuildMain, String remoteFolder, String localFolder, List<String> fileNamesToDownload)
	{
		this.BuildMain = BuildMain;
		this.fileNamesToDownload = fileNamesToDownload;
		this.localFolder = localFolder;
		this.remoteFolder = remoteFolder;
	}

	public void run()
	{
		try
		{
			downloadFiles();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
	}

	public void downloadFiles()
	{
		SecureShellClient client = null;
		try
		{
			if ( fileNamesToDownload == null || fileNamesToDownload.isEmpty() )
				return;

			String hostName = BuildMain.getSwiftBuildPropertiesValidationPanel().getFLDScriptsHostName();
			String port = SecureShellClient.SSH_DEFAULT_PORT;
			String userName = BuildMain.getSwiftBuildPropertiesValidationPanel().getFLDScriptsHostUserName();
			String password = BuildMain.getSwiftBuildPropertiesValidationPanel().getFLDScriptsHostPassword();
			String privateKeyFileName = BuildMain.getSwiftBuildPropertiesValidationPanel().getPrivateKeyFileName();
			
			if (BuildMain.isPasswordBasedSSHProtocol()) {
				client = new SecureShellClient.PasswordBasedSecureShellClient(hostName, port, userName, password, true);
			} else {
				client = new SecureShellClient.PrivateKeyBasedSecureShellClient(hostName, port, userName, privateKeyFileName, true);
			}			

			for ( int i = 0; i < fileNamesToDownload.size(); i++ )
			{
				String fileNameToDownload = fileNamesToDownload.get(i);
				client.cd(remoteFolder);
				client.get(remoteFolder, localFolder, fileNameToDownload);
				SwiftBuildFileUtils.createTempFileForViewer(new File(localFolder, fileNameToDownload), CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM, new File(localFolder,
						fileNameToDownload));
			}
		}
		catch ( Throwable e )
		{
			FileUtils.printStackTrace(e);
		}
		finally
		{
			DOWNLOAD_COMPLETED = true;
			if ( client != null )
			{
				client.close();
			}
		}
	}

	public boolean isCompleted()
	{
		return DOWNLOAD_COMPLETED;
	}

}