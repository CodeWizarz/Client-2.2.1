/**************************************************
 * $Revision: 42754 $:
 * $Author: john.snell $:
 * $Date: 2014-08-11 16:57:46 +0700 (Mon, 11 Aug 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/util/PatchUtils.java $:
 * $Id: PatchUtils.java 42754 2014-08-11 09:57:46Z john.snell $:
 */

package com.rapidesuite.client.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Properties;

import javax.swing.JProgressBar;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import com.rapidesuite.configurator.domain.Update;
import com.rapidesuite.core.utility.CoreUtil;
import com.sun.jna.Platform;

public class PatchUtils
{

    public static File downloadFile(String urlText,boolean isDownloadInSwiftReverseTempFolder,
    		File downloadFolder,JProgressBar progressBar,final String userName,final String password) throws Exception {
    	FileOutputStream writer=null;
    	InputStream reader=null;
    	try{

    		Authenticator.setDefault(new Authenticator() {
    		    protected PasswordAuthentication getPasswordAuthentication() {
    		        return new PasswordAuthentication(userName, password.toCharArray());
    		    }
    		});

    		URL url = new URL(urlText);
    		File file=null;
    		String downloadFileName=new File(url.getFile()).getName();
    		if (isDownloadInSwiftReverseTempFolder) {
    			downloadFolder.mkdirs();
    			file=new File(downloadFolder,downloadFileName);
    		}
    		else {
    			file = File.createTempFile(Config.getTempFolder().getName(),downloadFileName);
    			file.deleteOnExit();
    		}
    		long startTime = System.currentTimeMillis();
    		URLConnection uc=url.openConnection();
    		// setting these timeouts ensures the client does not deadlock indefinitely
    		// when the server has problems.
    		int timeoutMs=60000;
    		uc.setConnectTimeout(timeoutMs);
    		uc.setReadTimeout(timeoutMs);

    	    int contentLength = uc.getContentLength();
    	    int size=1024*4;
    	    if (progressBar!=null) {
    	    	progressBar.setMinimum(0);
    	    	progressBar.setMaximum(contentLength);
    	    	progressBar.setValue(0);
    	    }

    		reader = url.openStream();
    		writer = new FileOutputStream(file);

    		byte[] buffer = new byte[size];
    		int totalBytesRead = 0;
    		int bytesRead = 0;
    		String expectedDownloadedInMB=convertBytesToMegaBytes(contentLength);
    		while ((bytesRead = reader.read(buffer)) > 0){
    			writer.write(buffer, 0, bytesRead);
    			buffer = new byte[size];
    			totalBytesRead += bytesRead;

    			if (progressBar!=null) {
    				progressBar.setValue(totalBytesRead);
    				String totalBytesReadInMB=convertBytesToMegaBytes(totalBytesRead);
    				String msg="Downloading...  ("+totalBytesReadInMB+" / "+expectedDownloadedInMB+" MB)";
    				progressBar.setString(msg);
    			}
    		}
    		if (progressBar!=null) {
    			long endTime = System.currentTimeMillis();
    			String executionTime=Utils.getExecutionTime(startTime,endTime);
    			String msg=expectedDownloadedInMB+" MB in " + executionTime;
    			progressBar.setString(msg);
    			progressBar.setValue(contentLength);
    		}
    		return file;
    	}
    	catch(Exception e) {
    		FileUtils.printStackTrace(e);
    		throw new Exception("Unable to download from URL: '"+urlText+"' error: "+e.getMessage());
    	}
    	finally {
    		IOUtils.closeQuietly(reader);
    		IOUtils.closeQuietly(writer);
    	}
    }

    /*
     * 1. Download the selected patch to the updates/ folder
     * 2. unpack  all files. Delete the archive.
     * 3. Start the updater.bat or updater.sh (depends on the platform)
     * 4. The main application close itself
     * 5. The updater will copy all the files to the installation folder
     * 6. The updater will start the program and close itself
     */
    public static void downloadAndApplyPatch(final Update update,JProgressBar patchProgressBar,String executableFileName,String userName,String password) throws Exception {
    	final File updatesFolder = new File(Config.getTempFolder(), UtilsConstants.UPDATES_FOLDER_NAME);
    	Assert.isTrue(!updatesFolder.getPath().contains(" "), "Updates folder path must not contain space. Please check your temp folder setting and updates folder constant.");
    	try{
    		updatesFolder.mkdir();

    		String patchDownloadURL=update.getInstallerUrl().toExternalForm();
    		File archiveFile=PatchUtils.downloadFile(patchDownloadURL,true,updatesFolder,patchProgressBar,userName,password);

    		patchProgressBar.setString("Unpacking...");
    		SevenZipUtils.unpackFrom7zFile(archiveFile,null,null,updatesFolder);
    		archiveFile.delete();

    		patchProgressBar.setString("Installing...");
    		GUIUtils.popupInformationMessage("The program must be restarted, click OK to continue.");

    		final Map<String, String> allProperties = Config.getEffectivePropertiesMap();
    		Properties effectiveProperties = new Properties();
    		for (final Map.Entry<String, String> entry : allProperties.entrySet()) {
    			effectiveProperties.setProperty(entry.getKey(), entry.getValue());
    		}
    		OutputStream propertiesFromJavaArgumentsOutputStream = null;
    		final File effectivePropertiesFile = new File(updatesFolder.getParentFile(), UtilsConstants.EFFECTIVE_ENGINE_PROPERTIES_FILE_NAME);
    		try {
    			propertiesFromJavaArgumentsOutputStream = new FileOutputStream(effectivePropertiesFile);
    			effectiveProperties.store(propertiesFromJavaArgumentsOutputStream, null);
    		} finally {
    			IOUtils.closeQuietly(propertiesFromJavaArgumentsOutputStream);
    		}


    		String command = null;
    		String updatesFolderPath = updatesFolder.getPath();
			if (!updatesFolderPath.endsWith(File.separator)) {
				updatesFolderPath += File.separator;
			}
    		if (Platform.isWindows()) {
    			command = new File(updatesFolder, "updater.bat").getPath()+" "+executableFileName+" "+updatesFolderPath;
    		} else {
    			command = "python "+new File(updatesFolder, "updater.py").getPath()+" "+executableFileName+" "+updatesFolderPath;
    		}

    		//do not consume the output of this process, that makes problem in linux (the application fails to close itself)
    		Portability.startProcess(command);
    		final File beforeRunningUpdaterFlag = new File(Config.getTempFolder(), UtilsConstants.BEFORE_RUNNING_UPDATER_FLAG_FILENAME);
			CoreUtil.writeToFile(".", false, beforeRunningUpdaterFlag.getAbsoluteFile().getParentFile(), beforeRunningUpdaterFlag.getName());
			while(beforeRunningUpdaterFlag.exists()) { //this file will be deleted by updater (see UpdaterMain.java)
				com.rapidesuite.client.common.util.Utils.sleep(UtilsConstants.UPDATER_FILE_OPERATION_WAITING_TIME_MS);
			}
    		System.exit(0);
    	}
    	catch (Exception e) {
    		// clean up, so we will not apply corrupt updates.
    		org.apache.commons.io.FileUtils.deleteDirectory(updatesFolder);
    		throw e;
    	}
    }

    public static String convertBytesToMegaBytes(double bytes){
    	long  MEGABYTE = 1024L * 1024L;
    	double res=bytes / MEGABYTE;
    	DecimalFormat df = new DecimalFormat("####.##");
		return df.format(res);
    }

}