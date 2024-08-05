package com.rapidesuite.build.core.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.poi.util.IOUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;

public class FTPClientWrapper
{

	private static final String _FTPES = "FTPES";
	private static final String _FTP = "FTP";

	static String connect(FTPClient ftpClient, String hostName, int port, String userName, String password)
	{
		try
		{
			String protocol = _FTP;
			if ( ftpClient instanceof FTPSClient )
			{
				protocol = _FTPES;
			}

			FileUtils.println(protocol + ": connecting to the server... " + hostName + ":" + port);
			ftpClient.connect(hostName, port);
			FileUtils.println(protocol + ": connected. Trying to login...");

			int reply = ftpClient.getReplyCode();
			if ( !FTPReply.isPositiveCompletion(reply) )
			{
				FileUtils.println(protocol + ": not a positive completion reply: server refused connection." + " Reply code: '" + reply + "' . Disconnecting...");
				ftpClient.disconnect();
				FileUtils.println(protocol + ": disconnected.");
				return "Server refused connection.";
			}

			if ( ftpClient instanceof FTPSClient )
			{
				FTPSClient ftpsClient = (FTPSClient) ftpClient;
				ftpsClient.execPBSZ(0);
				ftpsClient.execPROT("P");
			}

			FileUtils.println(protocol + ": sending credentials...");
			boolean isLoggedIn = ftpClient.login(userName, password);
			if ( !isLoggedIn )
			{
				return "Authentication failed";
			}
			FileUtils.println(protocol + ": logged in.");
            FileUtils.println(protocol + ": Entering PASSIVE mode.");
			ftpClient.enterLocalPassiveMode();
			Assert.isTrue(ftpClient.getDataConnectionMode() == FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE, "Failed to set PASSIVE mode.");
	        try
	        {
                FileUtils.println(protocol + ": setting the file type to BINARY.");
	            Assert.isTrue(ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE), "Failed to set BINARY mode.");
	        }
	        catch ( IOException e )
	        {
	            throw new Error(e);
	        }

			return SwiftBuildConstants.CONNECTION_SUCCESS_MESSAGE;
		}
		catch ( UnknownHostException ue )
		{
			FileUtils.printStackTrace(ue);
			return "Unknown host";
		}
		catch ( ConnectException ce )
		{
			FileUtils.printStackTrace(ce);
			return "Connection refused. (Port not " + FTPManager.FTP_PORT + " ? ).";
		}
		catch ( SocketTimeoutException ex )
		{
			FileUtils.printStackTrace(ex);
			return "Timeout when connecting to the server. (port not " + FTPManager.FTP_PORT + " or busy?).";
		}
		catch ( SocketException ex )
		{
			FileUtils.printStackTrace(ex);
			return "Timeout when connecting to the server. Service unavailable?";
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
            return e.getMessage();
		}
	}

	static FTPClient getClient(boolean isFTPES) throws NoSuchAlgorithmException
	{
		String protocol = _FTP;
		if ( isFTPES )
		{
			protocol = _FTPES;
		}

		if ( FTPManager.socketTimeout == -1 )
		{
			FTPManager.socketTimeout = FTPManager.SOCKET_DEFAULT_TIMEOUT * 1000;
			try
			{
				FTPManager.socketTimeout = Config.getBuildFtpConnectTimeout() * 1000;
			}
			catch ( Exception e )
			{
				FileUtils.printStackTrace(e);
			}
		}

		FTPClient client = null;
		if ( isFTPES )
		{
			client = new FTPSClient(false);
		}
		else
		{
			client = new FTPClient();
		}

		if ( FTPManager.socketTimeout <= 0 )
		{
			FTPManager.socketTimeout = FTPManager.SOCKET_DEFAULT_TIMEOUT * 1000;
		}

		client.setDefaultTimeout(FTPManager.socketTimeout);
		FileUtils.println(protocol + ": setting default socket open timeout to " + client.getDefaultTimeout() + " ms");
		return client;
	}

	static void disconnectFTPClient(FTPClient ftpClient)
	{
		if ( ftpClient != null && ftpClient.isConnected() )
		{
			try
			{
				FileUtils.println("FTP: disconnecting...");
				ftpClient.disconnect();
				FileUtils.println("FTP: disconnected");
			}
			catch ( IOException ioe )
			{
				FileUtils.printStackTrace(ioe);
			}
		}
	}

	static void connectUntilRetryTimeout(String hostName, int port, String userName, String password, FTPClient ftpClient) throws Exception
	{
		int retryCount = 0;
		while ( true )
		{
			String res = connect(ftpClient, hostName, port, userName, password);

			if ( res == null || !res.equalsIgnoreCase(SwiftBuildConstants.CONNECTION_SUCCESS_MESSAGE) )
			{
				disconnectFTPClient(ftpClient);
				retryCount++;
				String tmp = "";
				if ( retryCount >= 3 )
				{
					tmp = "Unable to upload file to the FTP server.\n" + " Retry max limit reached. (Error message: " + res + " )";
					FileUtils.println(tmp);
					throw new Exception(tmp);
				}
				tmp = "Unable to upload file to the FTP server. Retrying (" + retryCount + ") waiting 3 secs ... (Error message: " + res + " )";
				FileUtils.println(tmp);
				com.rapidesuite.client.common.util.Utils.sleep(3000);
			}
			else
			{
				break;
			}
		}
	}

	static void uploadFile(String hostName,
			int port,
			String userName,
			String password,
			InputStream inputStream,
			String remoteFolder,
			String remoteName,
			boolean isFTPES) throws Exception
	{
		FTPClient ftpClient = null;
		try
		{
			String protocol = _FTP;
			if ( isFTPES )
			{
				protocol = _FTPES;
			}

			FileUtils.println(protocol + ": uploadFile() method initiated...");
			ftpClient = getClient(isFTPES);
			ftpClient.setBufferSize(1024 * 1024); //found this value somewhere in stackoverflow, and somehow it seems to work
			connectUntilRetryTimeout(hostName, port, userName, password, ftpClient);
			FileUtils.println(protocol + ": changing working directory to: '" + remoteFolder + "'");
			boolean success = ftpClient.changeWorkingDirectory(remoteFolder);
			if ( !success )
			{
				String tmp = protocol + ": changing working directory to '" + remoteFolder + "' failed.";
				FileUtils.println(tmp);
				throw new Exception(tmp);
			}
			FileUtils.println(protocol + ": changing working directory completed.");
			FileUtils.println(protocol + ": uploading file: '" + remoteName + "' ...");
			int retryCount = 0;
			if ( inputStream == null )
			{
				throw new Exception("Unable to find the file to upload in the InjectorsPackage");
			}

			while ( true )
			{
				success = ftpClient.storeFile(remoteName, inputStream);
				if ( !success )
				{
					retryCount++;
					String tmp = "";
					int reply = ftpClient.getReplyCode();
					if ( retryCount >= 3 )
					{
						tmp = protocol + ": unable to upload the file. Retry max limit reached. Reply code: " + reply;
						FileUtils.println(tmp);
						throw new Exception(tmp);
					}
					tmp = "Unable to upload the file. Retrying (" + retryCount + ") waiting 3 secs ...  Reply code: " + reply;
					FileUtils.println(tmp);
					com.rapidesuite.client.common.util.Utils.sleep(3000);
					continue;
				}
				FileUtils.println(protocol + ": uploading file completed.");
				break;
			}
		}
		finally
		{
			disconnectFTPClient(ftpClient);
		}
	}

	static void downloadFiles(String hostName,
			int port,
			String userName,
			String password,
			String remoteFolder,
			String localLogsFolder,
			List<String> files,
			boolean showError,
			boolean isFTPES) throws Exception
	{
		FTPClient ftpClient = null;
		FileOutputStream localFile = null;
		try
		{
			String protocol = _FTP;
			if ( isFTPES )
			{
				protocol = _FTPES;
			}

			FileUtils.println(protocol + ": downloadFile() method initiated...");
			ftpClient = getClient(isFTPES);
			String res = connect(ftpClient, hostName, port, userName, password);
			if ( res == null || !res.equalsIgnoreCase(SwiftBuildConstants.CONNECTION_SUCCESS_MESSAGE) )
			{
				String tmp = "Unable to download the files from the FTP server. Error message: " + res;
				FileUtils.println(tmp);
				throw new Exception(tmp);
			}

			FileUtils.println(protocol + ": changing working directory to: '" + remoteFolder + "'");
			boolean success = ftpClient.changeWorkingDirectory(remoteFolder);
			if ( !success )
			{
				String tmp = protocol + ": changing working directory failed.";
				FileUtils.println(tmp);
				throw new Exception(tmp);
			}
			FileUtils.println(protocol + ": changing working directory completed.");

			for ( int i = 0; i < files.size(); i++ )
			{
				String remoteFileName = files.get(i);
				String fullLocalFile = localLogsFolder + UtilsConstants.FORWARD_SLASH + remoteFileName;

				FileUtils.println(protocol + ": downloading file: '" + remoteFileName + "' ...");
				try
				{
					localFile = new FileOutputStream(fullLocalFile);
					success = ftpClient.retrieveFile(remoteFileName, localFile);
				}
				finally
				{
					if ( localFile != null )
					{
						localFile.close();
					}
				}
				if ( !success )
				{
					String tmp = "Unable to download the file: '" + remoteFileName + "'";
					FileUtils.println(tmp);
					if ( showError )
					{
						throw new Exception(tmp);
					}
				}
				else
				{
					SwiftBuildFileUtils.createTempFileForViewer(new File(fullLocalFile), CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM, new File(fullLocalFile));
					FileUtils.println(protocol + ": storing file completed to local: '" + fullLocalFile + "'");
				}
			}
		}
		finally
		{
			disconnectFTPClient(ftpClient);
		}
	}



	static File downloadFile(String hostName,
			int port,
			String userName,
			String password,
			String remoteFolder,
			String remoteFileName,
			boolean showError,
			boolean isFTPES) throws Exception
	{
		File tempDir = new File(Config.getTempFolder(), "ftp");
		tempDir.mkdirs();
		File file = File.createTempFile("ftp-", ".dat", tempDir);		
		return downloadFile(hostName,
				port,
				userName,
				password,
				remoteFolder,
				remoteFileName,
				showError,
				isFTPES,
				file);
	}
	
	static File downloadFile(String hostName,
			int port,
			String userName,
			String password,
			String remoteFolder,
			String remoteFileName,
			boolean showError,
			boolean isFTPES,
			File localFile) throws Exception
	{
		FTPClient ftpClient = null;
		try
		{
			String protocol = _FTP;
			if ( isFTPES )
			{
				protocol = _FTPES;
			}

			FileUtils.println(protocol + ": downloadFile() method initiated...");
			ftpClient = getClient(isFTPES);
			String res = connect(ftpClient, hostName, port, userName, password);
			if ( res == null || !res.equalsIgnoreCase(SwiftBuildConstants.CONNECTION_SUCCESS_MESSAGE) )
			{
				String tmp = "Unable to download the files from the FTP server. Error message: '" + res + "',  Reply code: " + ftpClient.getReplyCode();
				FileUtils.println(tmp);
				throw new Exception(tmp);
			}

			FileUtils.println(protocol + ": changing working directory to: '" + remoteFolder + "'");
			boolean success = ftpClient.changeWorkingDirectory(remoteFolder);
			if ( !success )
			{
				String tmp = protocol + ": changing working directory failed. Reply code: " + ftpClient.getReplyCode();
				FileUtils.println(tmp);
				throw new Exception(tmp);
			}
			FileUtils.println(protocol + ": changing working directory completed.");
			FileUtils.println(protocol + ": downloading file: '" + remoteFileName + "' ...");

            FileOutputStream fos = null;
            InputStream is = null;
			try
			{
			    fos = new FileOutputStream(localFile);
	            is = ftpClient.retrieveFileStream(remoteFileName);
	            IOUtils.copy(is, fos);
			}
			catch(Throwable t)
			{
                String tmp = "Error while downloading file.  Reply code: " + ftpClient.getReplyCode() + ", filename: '" + remoteFileName + "'";
                FileUtils.println(tmp);
                FileUtils.printStackTrace(t);
                if ( showError )
                {
                    throw new Exception(tmp, t);
                }
			}
			finally
			{
                IOUtils.closeQuietly(fos);
                IOUtils.closeQuietly(is);
			}

			return localFile;
		}
		finally
		{
			disconnectFTPClient(ftpClient);
		}
	}	

	static boolean deleteFile(String hostName, int port, String userName, String password, String remoteFolder, String remoteName, boolean isFTPES)
			throws Exception
	{
		FTPClient ftpClient = null;
		try
		{
			String protocol = _FTP;
			if ( isFTPES )
			{
				protocol = _FTPES;
			}

			FileUtils.println(protocol + ": deleteFile() method initiated...");
			ftpClient = getClient(isFTPES);

			connectUntilRetryTimeout(hostName, port, userName, password, ftpClient);

			FileUtils.println(protocol + ": changing working directory to: '" + remoteFolder + "'");
			boolean success = ftpClient.changeWorkingDirectory(remoteFolder);
			if ( !success )
			{
				String tmp = protocol + ": changing working directory failed.";
				FileUtils.println(tmp);
				return false;
			}
			FileUtils.println(protocol + ": changing working directory completed.");
			FileUtils.println(protocol + ": deleting file: '" + remoteName + "' ...");
			success = ftpClient.deleteFile(remoteName);
			if ( !success )
			{
				String tmp = protocol + ": unable to delete the script.";
				FileUtils.println(tmp);
				return false;
			}
			FileUtils.println(protocol + ": deleting file completed.");
			return true;
		}
		finally
		{
			disconnectFTPClient(ftpClient);
		}
	}

	static String validateCredentials(String hostName, int port, String userName, String password, boolean isFTPES) throws NoSuchAlgorithmException
	{
		FTPClient ftpClient = null;
		try
		{
			String protocol = _FTP;
			if ( isFTPES )
			{
				protocol = _FTPES;
			}

			FileUtils.println(protocol + ": validation...");
			ftpClient = getClient(isFTPES);
			String res = connect(ftpClient, hostName, port, userName, password);
			return res;
		}
		finally
		{
			disconnectFTPClient(ftpClient);
		}
	}

	static String validateTargetFolder(String hostName, int port, String userName, String password, String remoteFolder, boolean isListFiles, boolean isFTPES)
	{
		FTPClient ftpClient = null;
		try
		{
			String protocol = _FTP;
			if ( isFTPES )
			{
				protocol = _FTPES;
			}

			FileUtils.println(protocol + ": validating target folder: '" + remoteFolder + "'");
			ftpClient = getClient(isFTPES);
			connect(ftpClient, hostName, port, userName, password);
			FileUtils.println(protocol + ": changing working directory to: '" + remoteFolder + "'");
			boolean success = ftpClient.changeWorkingDirectory(remoteFolder);
			if ( !success )
			{
				FileUtils.println(protocol + ": changing working directory failed.");
				return "Unable to access remote folder.";
			}
			FileUtils.println(protocol + ": changing working directory completed.");

			if ( isListFiles )
			{
				FileUtils.println(protocol + ": listing files in the remote folder...");
				String[] names = ftpClient.listNames();
				int count = 0;
				if ( names != null )
				{
					count = names.length;
				}
				FileUtils.println(protocol + ": there are " + count + " files in the remote folder.");
			}

			return SwiftBuildConstants.REMOTE_FOLDER_SUCCESS_MESSAGE;
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return "Unexpected error. See log files.";
		}
		finally
		{
			disconnectFTPClient(ftpClient);
		}
	}

	static long getSize(String hostName, int port, String userName, String password, String remoteFolder, String remoteName, boolean isFTPES)
			throws Exception
	{
		FTPClient ftpClient = null;
		try
		{
			String protocol = _FTP;
			if ( isFTPES )
			{
				protocol = _FTPES;
			}

			FileUtils.println(protocol + ": getSize() method initiated...");
			ftpClient = getClient(isFTPES);

			connectUntilRetryTimeout(hostName, port, userName, password, ftpClient);

			FileUtils.println(protocol + ": changing working directory to: '" + remoteFolder + "'");
			boolean success = ftpClient.changeWorkingDirectory(remoteFolder);
			if ( !success )
			{
				String tmp = protocol + ": changing working directory failed.";
				FileUtils.println(tmp);
				return 0L;
			}
			FileUtils.println(protocol + ": changing working directory completed.");
			FileUtils.println(protocol + ": listing file: '" + remoteName + "' ...");
			FTPFile[] files = ftpClient.listFiles(remoteName);
			success = files != null && files.length == 1;
			if ( !success )
			{
				String tmp = protocol + ": unable to find the script.";
				FileUtils.println(tmp);
				return 0L;
			}
			FileUtils.println(protocol + ": listing file completed.");
			return files[0].getSize();
		}
		finally
		{
			disconnectFTPClient(ftpClient);
		}
	}
}