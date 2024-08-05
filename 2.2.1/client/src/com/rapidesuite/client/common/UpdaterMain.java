/**************************************************
 * $Revision: 44181 $:
 * $Author: john.snell $:
 * $Date: 2014-10-27 12:30:42 +0700 (Mon, 27 Oct 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/UpdaterMain.java $:
 * $Id: UpdaterMain.java 44181 2014-10-27 05:30:42Z john.snell $:
 */

package com.rapidesuite.client.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.apache.commons.io.IOUtils;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Portability;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.AbstractConfig;
import com.rapidesuite.core.utility.CoreUtil;
import com.sun.jna.Platform;

public class UpdaterMain
{

	private String executableFileName;
	private JWindow window;
	private final File updatesFolder;
	private final File tempFolder;
	private final String cmdTextEditor;

	public UpdaterMain(String executableFileName,String updatesFolder) {
		this.executableFileName=executableFileName;
		this.updatesFolder = new File(updatesFolder);	

		Config.customInitInstance(new File(this.updatesFolder.getParentFile(), UtilsConstants.EFFECTIVE_ENGINE_PROPERTIES_FILE_NAME).getPath());
		tempFolder = Config.getTempFolder();
		cmdTextEditor = Config.getCmdTextEditor();		
		
		window = new JWindow();
		window.setSize(450,300);
		window.setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.white);
		panel.setBorder(new LineBorder(Color.black));

		Container container = window.getContentPane();
		container.add(panel);
		container.setBackground(Color.white);

		JLabel centerLabel=new JLabel();
		GUIUtils.showInProgressMessage(centerLabel,"Please wait, applying patches...");
		Font f = new Font("Dialog", Font.PLAIN, 22);
		centerLabel.setFont(f);
		centerLabel.setHorizontalAlignment( SwingConstants.CENTER );
		centerLabel.setVerticalAlignment( SwingConstants.CENTER );

		JLabel companyLabel=new JLabel("Copyright (C) 2012, Rapid e-Suite Pte Ltd. All Rights reserved.");
		companyLabel.setHorizontalAlignment( SwingConstants.CENTER );

		panel.add(centerLabel,BorderLayout.CENTER);
		panel.add(companyLabel,BorderLayout.SOUTH);
		window.setVisible(true);
		
		final File logFolder = new File(Config.getLogFolder(), SwiftBuildConstants.UPDATE_LOG_FOLDER);
		if (!logFolder.exists()) {
			logFolder.mkdirs();
		}
		final String startTime = CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE.getDateFormat().format(new Date());
		logFile = new File(logFolder, "update-" + startTime + "-" + FileUtils.LOG_FILE_NAME_PREFIX + FileUtils.LOG_FILE_EXTENSION);	
		try {
			FileUtils.createLogFile(logFile);
		} catch (IOException e) {
			GUIUtils.popupErrorMessage("Failed to make log file for updater. The updater will be shut down");
			System.exit(1);
		}	
		
	}

	/*
	 * - The updater will copy all the files to the installation folder
	 * - The updater will remove the files from the update folder
	 * - The updater will start the main program and close itself
	 */
	public void update() {
		try {	
			final File beforeRunningUpdaterFlag = new File(tempFolder, UtilsConstants.BEFORE_RUNNING_UPDATER_FLAG_FILENAME);
			FileUtils.println("beforeRunningUpdaterFlag is "+beforeRunningUpdaterFlag.getAbsoluteFile().getAbsolutePath());
			while(!beforeRunningUpdaterFlag.exists()) { //this file was created by the launching application (see PatchUtils.java)
				com.rapidesuite.client.common.util.Utils.sleep(UtilsConstants.UPDATER_FILE_OPERATION_WAITING_TIME_MS);
			}
			beforeRunningUpdaterFlag.delete();
			com.rapidesuite.client.common.util.Utils.sleep(UtilsConstants.APPLICATION_EXIT_UPDATING_WAITING_TIME_MS);
			
			final File updatesFolderAbsolute = updatesFolder.getAbsoluteFile();
			final File applicationFolderAbsolute = (new File("dummy.txt")).getAbsoluteFile().getParentFile();
			
			final File backupDirectory = new File(applicationFolderAbsolute, UtilsConstants.OLD_VERSION_BACKUP_FOLDER_NAME_PREFIX+CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE_PRECISE.getDateFormat().format(new Date()));
			backupDirectory.mkdirs();
			FileUtils.println("Starting making backup at "+backupDirectory.getPath());
			for (final File oldFile : applicationFolderAbsolute.listFiles()) {
				if (!oldFile.isDirectory() || !oldFile.getName().startsWith(UtilsConstants.OLD_VERSION_BACKUP_FOLDER_NAME_PREFIX)) {
					copyDirectory(oldFile , new File(backupDirectory, oldFile.getName()));
				}
			}			
			FileUtils.println("Finished making backup at "+backupDirectory.getPath());
			
			final File newEnginePropertiesFile = new File(updatesFolderAbsolute, UtilsConstants.ENGINE_PROPERTIES_FILE_NAME);
			final File oldEnginePropertiesFile = new File(applicationFolderAbsolute, UtilsConstants.ENGINE_PROPERTIES_FILE_NAME);

			FileUtils.println("Starting merging "+UtilsConstants.ENGINE_PROPERTIES_FILE_NAME);
			copyProperties(oldEnginePropertiesFile, newEnginePropertiesFile);
			FileUtils.println("Finished merging "+UtilsConstants.ENGINE_PROPERTIES_FILE_NAME);
			
			final File newReplacementPropertiesFile = new File(updatesFolderAbsolute, UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME);
			final File oldReplacementPropertiesFile = new File(applicationFolderAbsolute, UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME);
			
			FileUtils.println("Starting merging "+UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME);
			copyProperties(oldReplacementPropertiesFile, newReplacementPropertiesFile);
			FileUtils.println("Finished merging "+UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME);					
			
			FileUtils.println("Starting copying from "+updatesFolderAbsolute.getAbsolutePath()+" to "+applicationFolderAbsolute.getAbsolutePath());
			copyDirectory(updatesFolderAbsolute,applicationFolderAbsolute);
			FileUtils.println("Finished copying from "+updatesFolderAbsolute.getAbsolutePath()+" to "+applicationFolderAbsolute.getAbsolutePath());
			if (Platform.isLinux()) {
				FileUtils.println("Starting changing files permission");
				//running chmod directly won't work, so put the chmod inside an sh file
				Portability.startProcess("bash "+new File(updatesFolderAbsolute, "update_files_permission_setter.sh").getPath());
				FileUtils.println("Finished changing files permission");
			}
			
			FileUtils.println("Starting "+executableFileName);
			final long timestampJustBeforeApplicationRelaunch = new Date().getTime();
			relaunchApplication();
			FileUtils.println("Finished "+executableFileName);
			final long updaterWaitToExitStartTimestamp = new Date().getTime();

			//timestamp file will be written by the application (see SwiftGUIMain.java)
			while(!applicationHasWrittenTimestampFile(timestampJustBeforeApplicationRelaunch)) {
				com.rapidesuite.client.common.util.Utils.sleep(UtilsConstants.UPDATER_FILE_OPERATION_WAITING_TIME_MS);
				
				final long currentTimestamp = new Date().getTime();
				if (currentTimestamp - updaterWaitToExitStartTimestamp >= UtilsConstants.MAX_WAITING_DURATION_BEFORE_UPDATER_CLOSE_MS) {
					break;
				}
			}
		}
		catch (Throwable e) {
			FileUtils.printStackTrace(e);
			if (logFile != null && JOptionPane.showConfirmDialog(null, "Are you running another instance of rapidclient? If that's the case, please close them before doing update.\n"
					+"Update failure. "+CoreUtil.getAllThrowableMessages(e)+".\n"
					+"Do you want to see log file?",
				"Update Failure",  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				Portability.startProcess(cmdTextEditor+" "+logFile.getAbsoluteFile().getAbsolutePath());
			}
		} finally {
			window.dispose();
		}
	}
	
	private boolean applicationHasWrittenTimestampFile(final long timestampJustBeforeApplicationRelaunch) {
		final File[] filesInTempFolder = tempFolder.listFiles();
		for (final File file : filesInTempFolder) {
			if (file.isFile() && file.getName().startsWith(UtilsConstants.APPLICATION_STARTED_FILENAME_PREFIX)) {
				String timestampStr = file.getName().replace(UtilsConstants.APPLICATION_STARTED_FILENAME_PREFIX, "");
				if (timestampStr.matches("^\\d+$")) {
					long timestamp = Long.parseLong(timestampStr);
					if (timestamp >= timestampJustBeforeApplicationRelaunch) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void relaunchApplication() {
		//do not consume the output of this process, that makes problem in linux (the application fails to close itself)
		if (Platform.isLinux()) {
			if (executableFileName.toLowerCase().endsWith(".sh")) {
				Portability.startProcess("bash "+executableFileName);
			} else if (executableFileName.toLowerCase().endsWith(".py")) {
				Portability.startProcess("python "+executableFileName);
			} else {
				Portability.startProcess(executableFileName);
			}
		} else {
			Portability.startProcess(executableFileName);
		}		
	}

	public void copyDirectory(File sourceLocation , File targetLocation) throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}
			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),new File(targetLocation, children[i]));
			}
		}
		else {
			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}
	
	private static File logFile = null;

	public static void main(String[] args) {
		try{
			UpdaterMain um = new UpdaterMain(args[0],args[1]);
			um.update();
		}
		finally{
			System.exit(0);
		}
	}
	
	private void copyProperties(final File oldPropertiesFile, final File newPropertiesFile) throws IOException {
		
		if (!newPropertiesFile.exists()) {
			FileUtils.println(newPropertiesFile.getPath()+" does not exist");
			return;
		}
		
		final Map<String, String> oldPropertiesMap = getPropertiesMap(oldPropertiesFile);
		final Map<String, String> newPropertiesMap = getPropertiesMap(newPropertiesFile);
		
		final Map<String, String> additionalPropertiesMap = new HashMap<String, String>();
		
		for (final Map.Entry<String, String> entry : newPropertiesMap.entrySet()) {
			if (!oldPropertiesMap.containsKey(entry.getKey())) {
				additionalPropertiesMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		CoreUtil.copyFile(oldPropertiesFile, newPropertiesFile);
		
		if (!additionalPropertiesMap.isEmpty()) {
			final Properties additionalProperties = new Properties();
			for (final Map.Entry<String, String> entry : additionalPropertiesMap.entrySet()) {
				additionalProperties.put(entry.getKey(), entry.getValue());
			}
			OutputStream os = null;
			try {
				//append the new properties
				os = new FileOutputStream(newPropertiesFile, true);
				additionalProperties.store(os, System.getProperty("line.separator")+"New Properties");
			} finally {
				IOUtils.closeQuietly(os);
			}
		}
	}
	
	private Map<String, String> getPropertiesMap(final File propertiesFile) {
		class SimpleConfig extends AbstractConfig {

			protected SimpleConfig() {
				super(propertiesFile.getPath(), 0);
				super.updateConfigProperties();
			}

			@Override
			protected void validate() {
				throw new UnsupportedOperationException();
			}
            @Override
            public void configUpdated()
            {
                //do nothing.
            }
			
			public Map<String, String> getPropertiesMap() {
				return super.getConcurrentMap();
			}

			@Override
			protected void logConfigPropertiesFileContent(String openingLine, String closingLine) {
				FileUtils.logConfigPropertiesFileContent(openingLine, closingLine, CoreUtil.excludePasswordPropertiesForLogging(this.getConcurrentMap()));
			}
		}
		
		final SimpleConfig simpleConfig = new SimpleConfig();
		final Map<String, String> output = simpleConfig.getPropertiesMap();
		return output;
	}

}