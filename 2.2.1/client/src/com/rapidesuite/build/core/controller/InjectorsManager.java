package com.rapidesuite.build.core.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.action.RobotPasteAction;
import com.rapidesuite.build.gui.frames.InjectorsPackageSplitterFrame;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.autoinjectors.navigation.fld.FLDNavigation;
import com.rapidesuite.configurator.autoinjectors.navigation.fld.templates.LoginTemplate;
import com.rapidesuite.configurator.robotPaste0001.RobotPasteDocument;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.Encryption;
import com.rapidesuite.core.utility.TempFileCleaner;

public class InjectorsManager
{	
	public static void splitInjectorToFiles(InputStream inputStream, int iterationBatchSize, String injectorName,
	        String injectorPackageName, Map<Integer,RobotPasteAction> iterationToRobotPasteMap,
	        final JLabel splittingCommentLabel,
	        final JProgressBar splittingProgressBar,
	        final JLabel encryptionCommentLabel, 
	        final JProgressBar encryptionProgressBar, 
	        final Integer totalIterationCount,
	        final InjectorsPackageSplitterFrame.InjectorSplittingStopButton stopButton,
	        final List<CipherInputStream> cipherInputStreams, 
	        final List<CipherInputStream> cipherInputStreamForTracking,
	        final Map<String, String> userNamesToPasswordFoundInInjector) throws Exception
	{
		Assert.notNull(cipherInputStreams);
	    boolean isRobotPaste = false;
        boolean inIterationLogging = false;
		injectorName = CoreUtil.getFileNameWithoutExtension(new File(injectorName));
		String dateStr = SwiftGUIMain.getStartTime();
		File directory = new File(FileUtils.getTemporaryFolder(), injectorPackageName + "-" + dateStr);
		directory.mkdirs();
        boolean insideXml = false;
        StringBuffer xml = new StringBuffer();
        RobotPasteDocument rbp = null;

		String strLine;
		StringBuffer body = new StringBuffer();
		StringBuffer headerTemp = new StringBuffer();
		String header = "";
		StringBuffer footerTemp = new StringBuffer();
		boolean isFooterDetected = false;
		boolean isFirstIterationDetected = false;
		int counter = 0;
		List<File> toReturnFiles = new ArrayList<File>();
		if (splittingProgressBar != null && totalIterationCount != null) {
			splittingProgressBar.setMaximum(totalIterationCount);
		}		
		int iterationIndex = 0;
		BufferedReader br = null;
		InputStreamReader isr = null;
		String FLD_LOGIN_PASSWORD_COMMAND_PREFIX="VALUE FNDSCSGN SIGNON PASSWORD 1 ";
		String tempUserName=null;
		try 
		{
		    isr = new InputStreamReader(inputStream, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
		    br = new BufferedReader(isr);
			//Splitting an injector can take a very long time for large injectors; protect our temp files
			TempFileCleaner.setIsDisabled(true);
			while ( (strLine = br.readLine()) != null )
			{
				if (stopButton != null) {
					stopButton.throwExceptionIfManuallyStopped();
				}
				
				if ( strLine.startsWith(LoginTemplate.FLD_LOGIN_USERNAME_COMMAND_PREFIX) ) 
				{
					tempUserName = strLine.substring(LoginTemplate.FLD_LOGIN_USERNAME_COMMAND_PREFIX.length()).trim();
				}
				else
				if ( strLine.startsWith(FLD_LOGIN_PASSWORD_COMMAND_PREFIX) ) 
				{
				    String password = strLine.substring(FLD_LOGIN_PASSWORD_COMMAND_PREFIX.length()).trim();
				    userNamesToPasswordFoundInInjector.put(tempUserName,password);
				    tempUserName=null;
				}
				
				
				if ( !isFirstIterationDetected )
				{
					if ( strLine.startsWith(CoreConstants.ITERATION_SEPARATOR) )
					{
						isFirstIterationDetected = true;
					}
					else
					{
						headerTemp.append(strLine).append("\n");
						continue;
					}
				}

				//large scripts are blowing out memory; ensure we're not wasting memory.
				if ( headerTemp != null )
				{
					header = headerTemp.toString();
					headerTemp = null;
				}

				if ( strLine.startsWith(CoreConstants.ITERATION_SEPARATOR) )
				{
					counter++;
					
					if (splittingProgressBar != null) {
						splittingProgressBar.setValue(counter);
					}
					if (splittingCommentLabel != null && totalIterationCount != null) {
						splittingCommentLabel.setText("Parsing iteration "+counter+"/"+totalIterationCount);
					}					
					
					if ( (counter >= 2 && iterationBatchSize == 1) || ((counter % iterationBatchSize == 1) && counter != 1) )
					{
						recordIteration(injectorName, iterationToRobotPasteMap, directory, rbp, body, toReturnFiles);
						// write the header for next iteration
						iterationIndex = toReturnFiles.size();
						write(header, injectorName, directory, iterationIndex);
					}
					else if (counter == 1 )
					{
						iterationIndex = toReturnFiles.size();
						write(header, injectorName, directory, iterationIndex);
					}
				}
				else if ( strLine.startsWith(SwiftBuildConstants.FLD_SCRIPT_FOOTER_SEPARATOR) )
				{
					recordIteration(injectorName, iterationToRobotPasteMap, directory, rbp, body, toReturnFiles);
					isFooterDetected = true;
				}

				if ( strLine.startsWith("<?xml") )
				{
					insideXml = true;
					xml.append(strLine).append("\n");
					continue;
				}
				if ( insideXml )
				{
					xml.append(strLine).append("\n");;
					if (strLine.startsWith("</robotPaste>") )
					{
						insideXml = false;
						rbp = RobotPasteDocument.Factory.parse(xml.toString());
						xml.setLength(0);
						FileUtils.println("RobotPaste detected - forcing iteration batch size to 1");
						isRobotPaste = true;
						iterationBatchSize = 1;
					}
					continue;
				}
				if ( strLine.trim().startsWith(FLDNavigation.RAPID_BUILD_ITERATION_TRACKING) )
				{
					inIterationLogging = !inIterationLogging;
					if ( !inIterationLogging )
					{
						//remove that last trailing line
						continue;
					}
				}
				if ( inIterationLogging && isRobotPaste )
				{
					continue;
				}


				if ( isFooterDetected )
				{
					footerTemp.append(strLine).append("\n");
				}
				else
				{
					body.append(strLine).append("\n");

					if(body.length() >= Config.getBuildSplitInjectorBufferSize()){
						iterationIndex = toReturnFiles.size();
						append(body.toString(), injectorName, directory, iterationIndex);
						body.setLength(0);
					}

				}
			}
			String footer = footerTemp.toString();
			footerTemp = null;

			if ( !isFirstIterationDetected )
			{
				// Unable to split the script because it does not contain a valid
				// header.
				// Possible if special script like set-password in HTML
				// so to be safe, we return the full script:
				iterationIndex = toReturnFiles.size();
				write(headerTemp.toString(), injectorName, directory, iterationIndex);
				File file = encrypt(injectorName, directory, iterationIndex);
				cipherInputStreams.add(new CipherInputStream(new FileInputStream(file), Encryption.getDecryptedCipherInstance()));
				if (cipherInputStreamForTracking != null) {
					cipherInputStreamForTracking.add(new CipherInputStream(new FileInputStream(file), Encryption.getDecryptedCipherInstance()));
				}
				return;
			}

			if ( !isFooterDetected )
			{
				// for last iteration without footer
				iterationIndex = toReturnFiles.size();
				append(body.toString(), injectorName, directory, iterationIndex);
				body.setLength(0);
				toReturnFiles.add(append(body.toString(), injectorName, directory, iterationIndex));
			}
			else
			{
				headerTemp = null;
				body = null;
			}
			
			if (encryptionProgressBar != null) {
				encryptionProgressBar.setMaximum(toReturnFiles.size());
			}				

			// encrypt all the files
			for ( int i = 0; i < toReturnFiles.size(); i++ )
			{
				iterationIndex = i;
				if (stopButton != null) {
					stopButton.throwExceptionIfManuallyStopped();
				}
				
				if (isFooterDetected) {
					if ( !isRobotPaste )
					{
						append(footer, injectorName, directory, iterationIndex);
					}
					toReturnFiles.set(i, append("", injectorName, directory, iterationIndex));				
				}
				
				File file = encrypt(injectorName, directory, iterationIndex);
				cipherInputStreams.add(new CipherInputStream(new FileInputStream(file), Encryption.getDecryptedCipherInstance()));
				if (cipherInputStreamForTracking != null) {
					cipherInputStreamForTracking.add(new CipherInputStream(new FileInputStream(file), Encryption.getDecryptedCipherInstance()));
				}
				
				if (encryptionCommentLabel != null) {
					encryptionCommentLabel.setText("Encrypting "+(i+1)+"/"+toReturnFiles.size()+" files");
				}
				if (encryptionProgressBar != null) {
					encryptionProgressBar.setValue(i+1);
				}				
				
			}
			return;
		}
		finally
		{
            //Manually closing streams to force-ignore BadPaddingException thrown; 
		    IOUtils.closeQuietly(br);
		    IOUtils.closeQuietly(isr);
			TempFileCleaner.setIsDisabled(false);
		}
	}

    private static void recordIteration(String injectorName,
            Map<Integer, RobotPasteAction> iterationToRobotPasteMap,
            File directory,
            RobotPasteDocument rbp,
            StringBuffer body,
            List<File> toReturn) throws Exception
    {
        int iterationIndex = toReturn.size();
        toReturn.add(append(body.toString(), injectorName, directory, iterationIndex));
        if ( null != iterationToRobotPasteMap && null != rbp )
        {
            write(CoreUtil.getXMLFromXMLBean(rbp), injectorName + ROBOT_PASTE_FILE_SUFFIX, directory, iterationIndex);
            File file = encrypt(injectorName + ROBOT_PASTE_FILE_SUFFIX, directory, iterationIndex);
            iterationToRobotPasteMap.put(iterationIndex, new RobotPasteAction(iterationIndex + 1, file));
        }
        body.setLength(0);
    }

	public static File write(String content, String fileName, File directory, int iterationIndex) throws Exception
	{
		return writeAppend(content, fileName, directory, iterationIndex, false);
	}

	public static File append(String content, String fileName, File directory, int iterationIndex) throws Exception
	{
		return writeAppend(content, fileName, directory, iterationIndex, true);
	}


	private static final String PARTITION_FILE_NAME_SUFFIX = ".fld";
	private static final String ROBOT_PASTE_FILE_SUFFIX = ".rbp";

	private static File writeAppend(String content, String fileName, File directory, int iterationIndex, boolean append) throws Exception
	{
		FileOutputStream fos = null;
		OutputStreamWriter out = null;
		File file = null;
		try
		{
			String[] nameSplit = fileName.split("-");
			nameSplit[0] = String.format("%04d", Integer.parseInt(nameSplit[0]));
			ArrayList<String> lst = new ArrayList<String>(Arrays.asList(nameSplit));
			lst.add(1, String.format("%04d", iterationIndex));
			lst.add(1, "Block");
			lst.add(0, "Step");
			fileName = StringUtils.join(lst.toArray(), '-');
			file = new File(directory, fileName + PARTITION_FILE_NAME_SUFFIX);
			if(!append){
			if(file.exists()) {
				file.delete();
			}
			final Long sleepingTime = 3000L;
			final int numberOfMaxTries = 5;
			for (int i = 0 ; i < numberOfMaxTries ; i++) {
				try {
					file.createNewFile();
					break;
				} catch (IOException e) {
					if  (i == numberOfMaxTries - 1) {
						throw e;
					} else if ("Access is denied".equalsIgnoreCase(e.getMessage().trim())) { //it occasionally happens and terminate the injection
						FileUtils.println("File creation failed for " + file.getName() + ", retrying in " + sleepingTime + " MS...");
						FileUtils.printStackTrace(e);
						com.rapidesuite.client.common.util.Utils.sleep(sleepingTime);
					} else {
						throw e;
					}

				} catch (Exception e) {
					throw e;
				}
			}
			}

			fos = new FileOutputStream(file, append);
			out = new OutputStreamWriter(fos, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
			out.write(content.toString());
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(fos);
		}

		return file;
	}
	private static File encrypt(String fileName, File directory, int iterationIndex) throws Exception
	{
        String[] nameSplit = fileName.split("-");
        nameSplit[0] = String.format("%04d", Integer.parseInt(nameSplit[0]));
        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(nameSplit));
        lst.add(1, String.format("%04d", iterationIndex));
        lst.add(1, "Block");
        lst.add(0, "Step");
        fileName = StringUtils.join(lst.toArray(), '-');
        File inputFile = new File(directory, fileName + PARTITION_FILE_NAME_SUFFIX);
        File outputFile = new File(directory, fileName + PARTITION_FILE_NAME_SUFFIX + ".temp");

		try (
		        FileInputStream fis = new FileInputStream(inputFile);
		        InputStreamReader in = new InputStreamReader(fis, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
		        BufferedReader br = new BufferedReader(in);
		        FileOutputStream fos = new FileOutputStream(outputFile);
		        CipherOutputStream cos = new CipherOutputStream(fos,Encryption.getEncryptedCipherInstance());
		        OutputStreamWriter out = new OutputStreamWriter(cos, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
		        )
		{
			String inStrLine = null;
			while ( (inStrLine = br.readLine()) != null ) {
				out.write(inStrLine);
				out.write("\n");
			}
			out.flush();
		}
		Files.move(Paths.get(outputFile.getAbsolutePath()), Paths.get(inputFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
		inputFile = new File(directory, fileName + PARTITION_FILE_NAME_SUFFIX);
		return inputFile;

	}

	public static InputStream getInputStreamAtMultipleIterationNumbers(InputStream inputStream, Set<Integer> selectedIterationNumbers, String injectorPackageName, String injectorName, int injectorIndex) throws Exception {

		InputStreamReader reader = null;
		BufferedReader buff = null;
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		BufferedWriter bw = null;
		try
		{
			String dateStr = SwiftGUIMain.getStartTime();
			File directory = new File(FileUtils.getTemporaryFolder(), injectorPackageName + "-" + dateStr);
			directory.mkdirs();

			File tempFile = new File(directory,  "Step-" + String.format("%04d", injectorIndex+1) + "-" + StringUtils.substringAfter(injectorName, "-") + Utils.getUniqueFilenameSuffix() +".fld");
			File encryptedFile = new File(directory,  "Step-" + String.format("%04d", injectorIndex+1) + "-" + StringUtils.substringAfter(injectorName, "-") + Utils.getUniqueFilenameSuffix() +".fld");

			fos = new FileOutputStream(tempFile, true);
			writer = new OutputStreamWriter(fos, CoreConstants.CHARACTER_SET_ENCODING);

			bw = new BufferedWriter(writer);

			reader = new InputStreamReader(inputStream, CoreConstants.CHARACTER_SET_ENCODING);
			buff = new BufferedReader(reader);

			String line = null;
			boolean isRecording = true;
			while ( (line = buff.readLine()) != null )
			{
				if (line.startsWith(CoreConstants.ITERATION_SEPARATOR)) {
					final int iterationNumber = Utils.retrieveIterationNumberFromIterationText(line);
					if (selectedIterationNumbers.contains(iterationNumber)) {
						isRecording = true;
					} else {
						isRecording = false;
					}
				} else if (line.startsWith(SwiftBuildConstants.FLD_SCRIPT_FOOTER_SEPARATOR)) {
					isRecording = true;
				}

				if (isRecording) {
					bw.write(line);
					bw.write("\n");
				}
			}
			bw.flush();
			IOUtils.closeQuietly(bw);
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(fos);
			return FileUtils.encryptDeleteUnencryptedFile(tempFile, encryptedFile);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(bw);
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(fos);
		}
	}

	/*
	 * Scanning the FLD file for replacement tokens and replacing them by their
	 * values from the BWE. Limitations: - It takes the full file into a String,
	 * this is reasonable if SPLIT_FLD is enabled but not if otherwise as it
	 * will result in out of memory issue. Should SPLIT_FLD be enforced for this
	 * method? - It replaces tokens only in the header (login commands) not in
	 * iterations
	 */
	public static InputStream replaceTokensInScript(InputStream scriptStream, Properties replacementTokens, String injectorNamePartition)
	{
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		BufferedWriter bw = null;
		try
		{
			DataInputStream in = new DataInputStream(scriptStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, CoreConstants.CHARACTER_SET_ENCODING));

			String dateStr = SwiftGUIMain.getStartTime();
			File directory = new File(FileUtils.getTemporaryFolder(), injectorNamePartition + "-" + dateStr);
			directory.mkdirs();

			File tempFile = new File(directory,  "token"+Utils.getUniqueFilenameSuffix() +".fld");
			File encryptedFile = new File(directory,  "token"+Utils.getUniqueFilenameSuffix() +".fld");

			fos = new FileOutputStream(tempFile, true);
			writer = new OutputStreamWriter(fos, CoreConstants.CHARACTER_SET_ENCODING);
			bw = new BufferedWriter(writer);

			String strLine;
			while ( (strLine = br.readLine()) != null )
			{
				strLine = replaceTokens(strLine, replacementTokens);
				bw.write(strLine);
				bw.write("\n");
			}
			bw.flush();
			IOUtils.closeQuietly(bw);
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(fos);

			return FileUtils.encryptDeleteUnencryptedFile(tempFile, encryptedFile);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return null;
		}
		finally
		{
			if ( scriptStream != null )
			{
				try
				{
					scriptStream.close();
				}
				catch ( Exception e )
				{
					FileUtils.printStackTrace(e);
				}
			}

			IOUtils.closeQuietly(bw);
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(fos);
		}
	}

	@SuppressWarnings("rawtypes")
	public static String replaceTokens(String source, Properties replacementTokens)
	{
		String res = source;
		if ( res == null || res.trim().equals("") )
		{
			return res;
		}

		Enumeration e = replacementTokens.keys();
		while ( e.hasMoreElements() )
		{
			String key = (String) e.nextElement();
			if ( res.indexOf(key) != -1 )
			{
				String target = replacementTokens.getProperty(key);
				res = res.replaceAll(key, java.util.regex.Matcher.quoteReplacement(target));
			}
		}
		return res;
	}

}