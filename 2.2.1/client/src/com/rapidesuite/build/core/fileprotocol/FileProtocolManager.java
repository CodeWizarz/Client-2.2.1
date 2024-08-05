package com.rapidesuite.build.core.fileprotocol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.core.CoreConstants;

//Ensure that the computer where this application runs and the other computer where Oracle EBS runs see the shared directory where the FLD scripts and FLD logs are kept in fresh state (i.e ensure there is no cache)
//for example, in the Oracle EBS development environment I am using, the bash command to mount the shared directory is as following:
//	mount -t cifs -o username=<my user name>,password=<my password>,forcedirectio //<shared directory ip address>/<shared directory remote path> <shared directory local path>
//notice the "forcedirectio" option. This option disables the caching

public class FileProtocolManager {

	public static void uploadFile(
			InputStream inputStream,
			String folderPath,
			String fileName) throws Exception
	{
		File targetFile = new File(folderPath, fileName);
		deleteIfExists(targetFile);
		FileUtils.copyInputStreamToFile(inputStream, targetFile);
	}

	public static boolean deleteFile(String folderPath, String fileName) throws Exception
	{
		File toDelete = new File(folderPath, fileName);
		boolean output = toDelete.delete();
		return output;
	}

	public static void downloadFiles(String targetFolder,
			String localLogsFolder,
			List<String> files,
			boolean showError) throws Exception
	{
		final File destinationDirectory = new File(localLogsFolder);
		destinationDirectory.mkdirs();
		for (final String fileName : files) {
			downloadFile(targetFolder, fileName, showError, new File(localLogsFolder, fileName));
		}
	}	

	public static File downloadFile(String targetFolder,
			String targetFileName,
			boolean showError,
			File localFile) throws Exception
	{
		File targetFile = new File(targetFolder, targetFileName);
		deleteIfExists(localFile);
		FileUtils.copyFile(targetFile, localFile);
		return localFile;
	}
	
	public static File downloadFile(String targetFolder,
			String targetFileName,
			boolean showError) throws Exception
	{
		File tempDir = new File(Config.getTempFolder(), "file");
		tempDir.mkdirs();
		File file = File.createTempFile("file-", ".dat", tempDir);	
		return downloadFile(targetFolder, targetFileName, showError, file);
	}
	

	public static String validateTargetFolder(String targetFolderStr)
	{
		final File targetFolder = new File(targetFolderStr);
				
		if (targetFolder.isFile()) {
			final String msg = "'"+ targetFolderStr +"' is a file, not a directory";
			com.rapidesuite.client.common.util.FileUtils.printStackTrace(new Throwable(msg));
			return msg;
		} else if (!targetFolder.isDirectory()) {
			final String msg = "'"+ targetFolderStr +"' does not exist";
			com.rapidesuite.client.common.util.FileUtils.printStackTrace(new Throwable(msg));
			return msg;			
		}
		
		final String starsStr = "********************************";
		
		com.rapidesuite.client.common.util.FileUtils.println(starsStr+"Validating '"+targetFolderStr+"' directory to ensure that it can be used by the '"+SwiftBuildConstants.CONNECTION_METHOD_FILE_VALUE+"' transfer protocol"+starsStr);
		
		
		final String delimiter = "---------------------------------------------------------------------------------------------------------";
		com.rapidesuite.client.common.util.FileUtils.println(delimiter);
		com.rapidesuite.client.common.util.FileUtils.println("The properties of '"+targetFolderStr+"':");
		com.rapidesuite.client.common.util.FileUtils.println("\tCan Read\t\t: "+targetFolder.canRead());
		com.rapidesuite.client.common.util.FileUtils.println("\tCan Write\t\t: "+targetFolder.canWrite());
		com.rapidesuite.client.common.util.FileUtils.println("\tCan Execute\t\t: "+targetFolder.canExecute());
		com.rapidesuite.client.common.util.FileUtils.println("\tLast Modified\t: "+
				CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE_PRECISE.getDateFormat().format(new java.util.Date(targetFolder.lastModified())));
		com.rapidesuite.client.common.util.FileUtils.println("\tIs Hidden\t\t: "+targetFolder.isHidden());
		com.rapidesuite.client.common.util.FileUtils.println("\tAbsolute Path\t: "+targetFolder.getAbsolutePath());
		com.rapidesuite.client.common.util.FileUtils.println(delimiter);
		
		File testFile = new File(targetFolderStr, UUID.randomUUID().toString() + ".test");
		final String expectedContent = UUID.randomUUID().toString();
		
		try {
			FileUtils.writeStringToFile(testFile, expectedContent, CoreConstants.CHARACTER_SET_ENCODING);
			com.rapidesuite.client.common.util.FileUtils.println("Successfully wrote a test file ('"+testFile.getName()+"') to '"+targetFolderStr+"'");
		} catch (IOException e1) {
			final String msg = "Failed to write a file to '"+targetFolderStr+"'. Please ensure that you have permission to write to this directory";
			com.rapidesuite.client.common.util.FileUtils.println(msg);
			com.rapidesuite.client.common.util.FileUtils.printStackTrace(e1);
			return msg;
		}
		
		File contents[] = targetFolder.listFiles();
		if (contents == null || !ArrayUtils.contains(contents, testFile)) {
			final String msg = "Failed to list the contents of '"+targetFolderStr+"'";
			com.rapidesuite.client.common.util.FileUtils.printStackTrace(new Throwable(msg));
			return msg;							
		} else {
			com.rapidesuite.client.common.util.FileUtils.println("Successfully listed the contents of '"+targetFolderStr+"'");		
		}		
		
		final String actualContent;
		try {
			actualContent = FileUtils.readFileToString(testFile, CoreConstants.CHARACTER_SET_ENCODING);
			com.rapidesuite.client.common.util.FileUtils.println("Successfully read the test file ('"+testFile.getName()+"') from '"+targetFolderStr+"'");
		} catch (IOException e1) {
			final String msg = "Failed to read the test file ('"+testFile.getName()+"') file from '"+targetFolderStr+"'. Please ensure that you have read permission to this directory";
			com.rapidesuite.client.common.util.FileUtils.println(msg);
			com.rapidesuite.client.common.util.FileUtils.printStackTrace(e1);
			return msg;
		}
		
		
		if (expectedContent.equals(actualContent)) {
			com.rapidesuite.client.common.util.FileUtils.println("Successfully verified the content of the test file ('"+testFile.getName()+"')");
		} else {
			final String msg = "The file content that was read is different from what was written. Something might have corrupted the test file ('"+testFile.getName()+"') or the existing cache might have interfered with the reading operation.";
			com.rapidesuite.client.common.util.FileUtils.printStackTrace(new Throwable(msg));
			return msg;			
		}
		
		
		boolean testFileWasDeleted = testFile.delete();
		if (testFileWasDeleted) {
			com.rapidesuite.client.common.util.FileUtils.println("Successfully deleted the test file ('"+testFile.getName()+"') from '"+targetFolderStr+"'");
		} else {
			final String msg = "Failed to delete the test file ('"+testFile.getName()+"') from '"+targetFolderStr+"'";
			com.rapidesuite.client.common.util.FileUtils.printStackTrace(new Throwable(msg));
			return msg;						
		}
		
		com.rapidesuite.client.common.util.FileUtils.println(starsStr+"Successfully validated '"+targetFolderStr+"' directory"+starsStr);
		
		return SwiftBuildConstants.REMOTE_FOLDER_SUCCESS_MESSAGE;
	}

	public static long getSize(String targetFolder, String targetName) throws Exception
	{
		File f = new File(targetFolder, targetName);
		final long output = f.length();
		return output;
	}
	
	private static void deleteIfExists(File file) throws IOException {
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			FileUtils.deleteDirectory(file);
		}		
	}
}
