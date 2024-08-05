package com.rapidesuite.build.core.ftp;

import java.io.File;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FTPManager
{

	public static final int SOCKET_DEFAULT_TIMEOUT = 60; // in seconds
	public static int socketTimeout = -1;
	public static final int FTP_PORT = 21;
	private static boolean IS_FTPES = false;

	public static boolean isFTPES()
	{
		return IS_FTPES;
	}

	public static void setFTPES(boolean isFTPES)
	{
		IS_FTPES = isFTPES;
	}

	public static void uploadFile(String hostName,
			int port,
			String userName,
			String password,
			InputStream inputStream,
			String remoteFolder,
			String remoteName) throws Exception
	{
		FTPClientWrapper.uploadFile(hostName, port, userName, password, inputStream, remoteFolder, remoteName, isFTPES());
	}

	public static boolean deleteFile(String hostName, int port, String userName, String password, String remoteFolder, String remoteName) throws Exception
	{
		return FTPClientWrapper.deleteFile(hostName, port, userName, password, remoteFolder, remoteName, isFTPES());
	}

	public static void downloadFiles(String hostName,
			int port,
			String userName,
			String password,
			String remoteFolder,
			String localLogsFolder,
			List<String> files,
			boolean showError) throws Exception
	{
		FTPClientWrapper.downloadFiles(hostName, port, userName, password, remoteFolder, localLogsFolder, files, showError, isFTPES());
	}

	public static File downloadFile(String hostName,
			int port,
			String userName,
			String password,
			String remoteFolder,
			String remoteFileName,
			boolean showError) throws Exception
	{
		return FTPClientWrapper.downloadFile(hostName, port, userName, password, remoteFolder, remoteFileName, showError, isFTPES());
	}
	
	public static File downloadFile(String hostName,
			int port,
			String userName,
			String password,
			String remoteFolder,
			String remoteFileName,
			boolean showError,
			File localFile) throws Exception
	{
		return FTPClientWrapper.downloadFile(hostName, port, userName, password, remoteFolder, remoteFileName, showError, isFTPES(), localFile);
	}	

	public static String validateCredentials(String hostName, int port, String userName, String password) throws NoSuchAlgorithmException
	{
		return FTPClientWrapper.validateCredentials(hostName, port, userName, password, isFTPES());
	}

	public static String validateTargetFolder(String hostName, int port, String userName, String password, String remoteFolder, boolean isListFiles)
	{
		return FTPClientWrapper.validateTargetFolder(hostName, port, userName, password, remoteFolder, isListFiles, isFTPES());
	}

	public static long getSize(String hostName, int port, String userName, String password, String remoteFolder, String remoteName) throws Exception
	{
		return FTPClientWrapper.getSize(hostName, port, userName, password, remoteFolder, remoteName, isFTPES());
	}
}