/**************************************************************
 * $Revision: 52350 $:
 * $Author: olivier.deruelle $:
 * $Date: 2016-02-01 18:51:33 +0700 (Mon, 01 Feb 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/utils/InjectorsPackageUtils.java $:
 * $Id: InjectorsPackageUtils.java 52350 2016-02-01 11:51:33Z olivier.deruelle $:
 **************************************************************/
package com.rapidesuite.build.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.controller.InjectorsManager;
import com.rapidesuite.build.core.htmlplayback.HTMLRunnerConstants;
import com.rapidesuite.build.gui.frames.InjectorsPackageSplitterFrame;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.autoinjectors.navigation.fld.FLDNavigation;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.CoreConstants.INJECTOR_TYPE;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.Encryption;

public class InjectorsPackageUtils
{

	public static boolean isEncryptedInjectorsPackage(File archiveFile) throws IOException
	{
		try
		{
			new ZipFile(archiveFile);
			return false;
		}
		catch ( ZipException e )
		{
			//e.printStackTrace();
			return true;
		}
	}

	public static InputStream getInputStreamFromUnencryptedZIPFile(ZipFile zipFile, String entryName) throws Exception
	{
		ZipEntry zipEntry = zipFile.getEntry(entryName);
		return zipFile.getInputStream(zipEntry);
	}

	public static ZipInputStream getZipInputStreamFromUnencryptedZIPFile(File archiveFile) throws Exception
	{
		ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(archiveFile));
		return zipInputStream;
	}

	public static ZipInputStream getZipInputStreamFromEncryptedZIPFile(File archiveFile) throws Exception
	{
		ZipInputStream zipInputStream = new ZipInputStream(new CipherInputStream(new BufferedInputStream(new FileInputStream(archiveFile)),
		        Encryption.getDecryptedCipherInstance()));
		return zipInputStream;
	}

	public static InputStream getInputStreamFromEncryptedZIPFile(File archiveFile, String entryName) throws Exception
	{
		ZipEntry zipEntry = null;
		ZipInputStream zipInputStream = new ZipInputStream(new CipherInputStream(new BufferedInputStream(new FileInputStream(archiveFile)),
		        Encryption.getDecryptedCipherInstance()));
            while ( (zipEntry = zipInputStream.getNextEntry()) != null )
            {
                if ( entryName.equalsIgnoreCase(zipEntry.getName()) )
                {
                    return zipInputStream;
                }
            }
		return null;
	}

    public static List<String> getEntryListFromEncryptedZIPFile(File archiveFile) throws Exception
    {
        List<String> toReturn = new ArrayList<String>();
        ZipEntry zipEntry = null;
        ZipInputStream zipInputStream = null;
        try
        {
                zipInputStream = new ZipInputStream(new CipherInputStream(new BufferedInputStream(new FileInputStream(archiveFile)),
                        Encryption.getDecryptedCipherInstance()));
            while ( (zipEntry = zipInputStream.getNextEntry()) != null )
            {
                toReturn.add(zipEntry.getName());
            }
        }
        finally
        {
            IOUtils.closeQuietly(zipInputStream);
        }
        return toReturn;
    }

	public static String getContentFromZIPFile(File archiveFile, String entryName) throws Exception
	{
		InputStream inputStream = null;
		StringBuffer res = new StringBuffer("");
		ZipFile zipFile = null;
		try
		{
			if ( isEncryptedInjectorsPackage(archiveFile) )
			{
				inputStream = getInputStreamFromEncryptedZIPFile(archiveFile, entryName);
			}
			else
			{
				zipFile = new ZipFile(archiveFile);
				inputStream = getInputStreamFromUnencryptedZIPFile(zipFile, entryName);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING));
			String strLine;
			while ( (strLine = br.readLine()) != null )
			{
				res.append(strLine).append("\n");
			}
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
			if ( zipFile != null )
			{
				zipFile.close();
			}
		}
		return res.toString();
	}

	public static Map<String, String> getSpecificationProperties(File archiveFile) throws Exception
	{
		ZipFile zipFile = null;
		InputStream inputStream = null;
		Map<String, String> res = new HashMap<String, String>();
		try
		{
			if ( isEncryptedInjectorsPackage(archiveFile) )
			{
				inputStream = getInputStreamFromEncryptedZIPFile(archiveFile, CoreConstants.SPECIFICATIONS_FILE_NAME);
			}
			else
			{
				zipFile = new ZipFile(archiveFile);
				inputStream = getInputStreamFromUnencryptedZIPFile(zipFile, CoreConstants.SPECIFICATIONS_FILE_NAME);
			}
			Properties properties = new Properties();
			properties.load(inputStream);
			Set<Map.Entry<Object, Object>> propertySet = properties.entrySet();
			for ( Map.Entry<Object, Object> o : propertySet )
			{
				Map.Entry<Object, Object> entry = o;
				res.put((String) entry.getKey(), (String) entry.getValue());
			}
			if ( properties.isEmpty() )
			{
				throw new Exception("Invalid Specifications file.");
			}
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
			if ( zipFile != null )
			{
				zipFile.close();
			}
		}
		return res;
	}
	
	public static boolean isIterationTrackingEnabled(File archiveFile) throws Exception
	{
		 Map<String, String> properties = getSpecificationProperties(archiveFile);
		 for (final String key : properties.keySet()) {
			 if (key.contains(CoreConstants.SPECIFICATION_RESPONSIBILITY_USER_NAME_PREFIX)) {
				 return true;
			 }
		 }
		 return false;
	}	

	public static StringBuffer getSpecificationsFileTemplate(File archiveFile) throws Exception
	{
		StringBuffer sb = new StringBuffer("");
		Map<String, String> properties = InjectorsPackageUtils.getSpecificationProperties(archiveFile);

		sb.append(CoreConstants.SPECIFICATION_GENERATED_BY + "= Automatic splitting\n");
		sb.append(CoreConstants.SPECIFICATION_INJECTOR_NAME + "=" + getProperty(properties, CoreConstants.SPECIFICATION_INJECTOR_NAME) + "\n");
		sb.append(CoreConstants.SPECIFICATION_FILTERS + "=" + getProperty(properties, CoreConstants.SPECIFICATION_FILTERS) + "\n");
		sb.append(CoreConstants.SPECIFICATION_SERVER_NAME + "=" + getProperty(properties, CoreConstants.SPECIFICATION_SERVER_NAME) + "\n");
		sb.append(CoreConstants.SPECIFICATION_PROJECT_NAME + "=" + getProperty(properties, CoreConstants.SPECIFICATION_PROJECT_NAME) + "\n");
		sb.append(CoreConstants.SPECIFICATION_GENERATION_ID).append('=').append(getProperty(properties, CoreConstants.SPECIFICATION_GENERATION_ID)).append('\n');
		sb.append(CoreConstants.SPECIFICATION_HISTORY_ID).append('=').append(getProperty(properties, CoreConstants.SPECIFICATION_HISTORY_ID)).append('\n');
		Date date = new Date();
		sb.append(CoreConstants.SPECIFICATION_GENERATED_ON + "=" + date.toString() + "\n");
		sb.trimToSize();

		return sb;
	}

	public static String getProperty(Map<String, String> properties, String propertyName)
	{
		String temp = properties.get(propertyName);
		if ( temp == null )
		{
			temp = "";
		}
		return temp;
	}

	public static void createZIPArchives(final File sourceArchiveFile,
			final int iterationBatchSize,
			final String scriptName,
			final String scriptType,
			final String destinationArchiveFilePrefix,
			final File outputFolder,
			final JLabel iterationParsingCommentLabel,
			final JProgressBar iterationParsingProgressBar,
			final JLabel encryptionCommentLabel,
			final JProgressBar encryptionProgressBar, 
			final JLabel bwpWritingCommentLabel, 
			final JProgressBar bwpWritingProgressBar, 
			final InjectorsPackageSplitterFrame.InjectorSplittingStopButton stopButton,
			final JTable injectorTable,
			final int iterationCount) throws Exception
	{
		injectorTable.setEnabled(false);
		iterationParsingProgressBar.setValue(0);
		encryptionProgressBar.setValue(0);
		bwpWritingProgressBar.setValue(0);
		iterationParsingProgressBar.setMaximum(100);
		encryptionProgressBar.setMaximum(100);
		bwpWritingProgressBar.setMaximum(100);
		iterationParsingCommentLabel.setText("Waiting to parse the injector file");
		encryptionCommentLabel.setText("Waiting to encrypt the temporary files");
		bwpWritingCommentLabel.setText("Waiting to write BWP output files");
		
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				
				InputStream inputStream = null;
				List<CipherInputStream> scriptTextStreams = new ArrayList<CipherInputStream>();
				ZipFile zipFile = null;
				try
				{
					if ( isEncryptedInjectorsPackage(sourceArchiveFile) )
					{
						inputStream = getInputStreamFromEncryptedZIPFile(sourceArchiveFile, scriptName);
					}
					else
					{
						zipFile = new ZipFile(sourceArchiveFile);
						inputStream = getInputStreamFromUnencryptedZIPFile(zipFile, scriptName);
					}				
                    InjectorsManager.splitInjectorToFiles(inputStream,
                            iterationBatchSize,
                            scriptName,
                            sourceArchiveFile.getName(),
                            null,
                            iterationParsingCommentLabel,
                            iterationParsingProgressBar,
                            encryptionCommentLabel,
                            encryptionProgressBar,
                            iterationCount,
                            stopButton,
                            scriptTextStreams,
                            null,
                            new HashMap<String,String>());

					bwpWritingProgressBar.setMaximum(scriptTextStreams.size());
					final String specification = InjectorsPackageUtils.getSpecificationsFileTemplate(sourceArchiveFile).toString() +
							CoreConstants.SPECIFICATION_INJECTOR_NAME_PREFIX + "1=" + scriptName + "\n" +
							CoreConstants.SPECIFICATION_INJECTOR_TYPE_PREFIX + "1=" + scriptType + "\n" +
							CoreConstants.SPECIFICATION_INJECTOR_COUNT + "=1";

					int fileCounter = 0;
					boolean isEncryptedInjectorsPackage = isEncryptedInjectorsPackage(sourceArchiveFile);
					for ( CipherInputStream content : scriptTextStreams )
					{
						stopButton.throwExceptionIfManuallyStopped();
						fileCounter++;
						File destinationFile = new File(outputFolder, "0" + fileCounter + "-" + destinationArchiveFilePrefix + "." + SwiftBuildConstants.INJECTORS_PACKAGE_FILE_EXTENSION);
						createInjectorsPackageArchive(specification, scriptName, scriptType, content, destinationFile, isEncryptedInjectorsPackage);
						bwpWritingCommentLabel.setText("Writing "+fileCounter+"/"+scriptTextStreams.size()+" BWP files");
						bwpWritingProgressBar.setValue(fileCounter);						
					}
					GUIUtils.popupInformationMessage("Completed.");
				}
				catch (InjectorsPackageSplitterFrame.InjectorSplittingStopButton.InjectorSplittingManualStopException e) {
					//do nothing
				}
				catch (Throwable t) {
					throw new Error(t);
				}			
				finally
				{
					IOUtils.closeQuietly(inputStream);
					if ( null != scriptTextStreams )
					{
					    for ( CipherInputStream content : scriptTextStreams )
					    {
					        IOUtils.closeQuietly(content);
					    }
					}
					IOUtils.closeQuietly(zipFile);
					stopButton.setEnabled(false);
					injectorTable.setEnabled(true);					
				}
			}
		});
		stopButton.markStart();
		t.start();
	}

	private static void createInjectorsPackageArchive(final String specification,
			final String injectorName,
			final String injectorType,
			final CipherInputStream scriptText,
			final File destinationFile,
			final boolean isInjectorsPackageEncrypted) throws Exception
	{
		FileOutputStream fileOutputStream = null;
		ZipInputStream zipInputStream = null;
		ZipOutputStream zipOutputStream = null;
		Writer out = null;
		try
		{
			fileOutputStream = new FileOutputStream(destinationFile);

			if ( isInjectorsPackageEncrypted )
			{
				CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, Encryption.getEncryptedCipherInstance());
				zipOutputStream = new ZipOutputStream(cipherOutputStream);
			}
			else
			{
				zipOutputStream = new ZipOutputStream(fileOutputStream);
			}
			ZipEntry entry = new ZipEntry(CoreConstants.SPECIFICATIONS_FILE_NAME);
			zipOutputStream.putNextEntry(entry);
			out = new OutputStreamWriter(zipOutputStream, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
			out.write(specification);
			out.flush();

			entry = new ZipEntry(injectorName);
			zipOutputStream.putNextEntry(entry);

			// Process the script file by line to cater for big files
			InputStreamReader inputStream = null;
			BufferedReader br = null;
			String strLine;
			try
			{
				inputStream = new InputStreamReader(scriptText, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
				br = new BufferedReader(inputStream);
				while ((strLine = br.readLine()) != null)
				{
					out.write(strLine);
					out.write("\n");
				}
			} finally
			{
				IOUtils.closeQuietly(br);
				IOUtils.closeQuietly(inputStream);
			}

			out.flush();
		}
		finally
		{
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(fileOutputStream);
			IOUtils.closeQuietly(zipOutputStream);
			IOUtils.closeQuietly(zipInputStream);
		}
	}

	public static List<Injector> getInjectors(File injectorsPackageFile, CoreConstants.INJECTOR_TYPE injectorTypeOnly) throws Exception
	{
		return getInjectors(injectorsPackageFile, injectorTypeOnly==null?null:Collections.singleton(injectorTypeOnly));
	}
	
	public static List<Injector> getInjectors(File injectorsPackageFile, Set<CoreConstants.INJECTOR_TYPE> injectorTypesOnly) throws Exception {
		Map<String, String> properties = getSpecificationProperties(injectorsPackageFile);
		String injectorsCount = properties.get(CoreConstants.SPECIFICATION_INJECTOR_COUNT);
		List<Injector> res = new ArrayList<Injector>();
		int injectorCount = Integer.valueOf(injectorsCount).intValue();
		for ( int i = 0; i < injectorCount; i++ )
		{
			String injectorName = properties.get(CoreConstants.SPECIFICATION_INJECTOR_NAME_PREFIX + (i + 1));
			CoreConstants.INJECTOR_TYPE injectorType = null;

			try {
				injectorType = CoreConstants.INJECTOR_TYPE.forString(properties.get(CoreConstants.SPECIFICATION_INJECTOR_TYPE_PREFIX + (i + 1)));
			} catch (IllegalArgumentException e) {
				//do nothing, this injector will be skipped
			}


			if ( injectorName == null || injectorType == null )
			{
				continue;
			}

			if ( injectorTypesOnly == null || injectorTypesOnly.contains(injectorType) )
			{
				Injector injector = new Injector(injectorName, injectorType, i);
				res.add(injector);
			}
		}
		return res;		
	}

	public static boolean hasAtLeastOneAPIInjectorType(Map<String, String> properties) throws Exception
	{
		String injectorsCount = properties.get(CoreConstants.SPECIFICATION_INJECTOR_COUNT);
		int injectorCount = Integer.valueOf(injectorsCount).intValue();
		for ( int i = 0; i < injectorCount; i++ )
		{
			CoreConstants.INJECTOR_TYPE injectorType = CoreConstants.INJECTOR_TYPE.forString(properties.get(CoreConstants.SPECIFICATION_INJECTOR_TYPE_PREFIX + (i + 1)));
			if ( injectorType == null )
			{
				continue;
			}
			if ( injectorType.equals(CoreConstants.INJECTOR_TYPE.TYPE_API) )
			{
				return true;
			}
		}
		return false;
	}

	public static List<Injector> getInjectors(File injectorsPackageFile) throws Exception
	{
		return getInjectors(injectorsPackageFile, (CoreConstants.INJECTOR_TYPE) null);
	}

	public static String getInjectorType(File injectorsPackageFile, String injectorName) throws Exception
	{
		Map<String, String> properties = getSpecificationProperties(injectorsPackageFile);
		String injectorsCount = properties.get(CoreConstants.SPECIFICATION_INJECTOR_COUNT);
		int injectorCount = Integer.valueOf(injectorsCount).intValue();
		for ( int i = 0; i < injectorCount; i++ )
		{
			String injectorNameTemp = properties.get(CoreConstants.SPECIFICATION_INJECTOR_NAME_PREFIX + (i + 1));
			String injectorType = properties.get(CoreConstants.SPECIFICATION_INJECTOR_TYPE_PREFIX + (i + 1));
			if ( injectorName != null && injectorNameTemp.trim().equalsIgnoreCase(injectorName) )
			{
				return injectorType;
			}
		}
		throw new Exception("Invalid Specifications file");
	}

	public static void encryptInjectorsPackage(File sourceArchiveFile, File targetArchiveFile) throws Exception
	{
		FileOutputStream fileOutputStream = null;
		ZipInputStream zipInputStream = null;
		ZipOutputStream zipOutputStream = null;
		try
		{
			fileOutputStream = new FileOutputStream(targetArchiveFile);
			CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, Encryption.getEncryptedCipherInstance());
			zipOutputStream = new ZipOutputStream(cipherOutputStream);

			zipInputStream = new ZipInputStream(new FileInputStream(sourceArchiveFile));
			ZipEntry zipEntry = null;
			while ( (zipEntry = zipInputStream.getNextEntry()) != null )
			{
				ZipEntry destEntry = new ZipEntry(zipEntry.getName());
				zipOutputStream.putNextEntry(destEntry);
				byte[] b = new byte[512];
				int len = 0;
				while ( (len = zipInputStream.read(b)) != -1 )
				{
					zipOutputStream.write(b, 0, len);
				}
			}
		}
		finally
		{
			IOUtils.closeQuietly(fileOutputStream);
			IOUtils.closeQuietly(zipOutputStream);
			IOUtils.closeQuietly(zipInputStream);
		}
	}
	
	public static abstract class RawOutputProcessor {	
		public abstract File processRawOutput(final InputStream rawOutputInputStream) throws Exception;
	}
	
	public static class RawLogOutputProcessor extends RawOutputProcessor {
		
		private final Injector injector;
		private final File rawLogFile;
		private final boolean iterationTrackingEnabled;
		private final boolean convertToHtml;
		
		public RawLogOutputProcessor(final Injector injector, final File rawLogFile, final boolean iterationTrackingEnabled, final boolean convertToHtml) {
			this.injector = injector;
			this.rawLogFile = rawLogFile;
			this.iterationTrackingEnabled = iterationTrackingEnabled;
			this.convertToHtml = convertToHtml;
		}

		@Override
		public File processRawOutput(InputStream rawOutputInputStream) throws Exception {
			final int dotIndex = rawLogFile.getName().indexOf('.');
			String fileName;
			if (dotIndex == -1) {
				fileName = rawLogFile.getName() + "-subset";
			} else {
				fileName = rawLogFile.getName().substring(0, dotIndex) + "-subset" + rawLogFile.getName().substring(dotIndex);
			}
			
			final File outputFile;
			if (INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) {
				fileName += ".html";
				outputFile = new File(Config.getTempFolder(), fileName);
				if (convertToHtml) {
					SwiftBuildFileUtils.generatePartialLogMainContentFldScriptAndLog(injector.getNameWithoutExtension()+" Subset", injector.getType(), rawOutputInputStream, outputFile, true, iterationTrackingEnabled, false);
				} else {
					SwiftBuildFileUtils.generatePartialLogMainContentFldScriptAndLogPlain(injector.getType(), rawOutputInputStream, outputFile);
				}
				
			} else {
				outputFile = new File(Config.getTempFolder(), fileName);
				try (OutputStream os = new FileOutputStream(outputFile)) {
					IOUtils.copy(rawOutputInputStream, os);												
				}				
			}

			return outputFile;
		}
		
	}
	
	public static class RawScriptOutputProcessor extends RawOutputProcessor {
		
		private final Injector injector;
		private final String fileNameAppendix;
		private final boolean iterationTrackingEnabled;
		private final boolean convertToHtml;
		
		public RawScriptOutputProcessor(final Injector injector, final String fileNameAppendix, final boolean iterationTrackingEnabled, final boolean convertToHtml) {
			this.injector = injector;
			this.fileNameAppendix = fileNameAppendix;
			this.iterationTrackingEnabled = iterationTrackingEnabled;
			this.convertToHtml = convertToHtml;
		}

		@Override
		public File processRawOutput(InputStream rawOutputInputStream) throws Exception {
			final File output;
			if (INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType())) {
				output = new File(Config.getTempFolder(), injector.getNameWithoutExtension()+"-"+fileNameAppendix+Utils.getUniqueFilenameSuffix()+".html");
				if (convertToHtml) {
					SwiftBuildFileUtils.generatePartialLogMainContentHtml(injector.getNameWithoutExtension()+" Subset", injector.getType(), rawOutputInputStream, output, true);
				} else {
					SwiftBuildFileUtils.generatePartialLogMainContentPlain(injector.getType(), rawOutputInputStream, output);
				}
			} else if (INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) {
				output = new File(Config.getTempFolder(), injector.getNameWithoutExtension()+"-"+fileNameAppendix+Utils.getUniqueFilenameSuffix()+".html");
				if (convertToHtml) {
					SwiftBuildFileUtils.generatePartialLogMainContentFldScriptAndLog(injector.getNameWithoutExtension()+" Subset", injector.getType(), rawOutputInputStream, output, true, iterationTrackingEnabled, true);
				} else {
					SwiftBuildFileUtils.generatePartialLogMainContentFldScriptAndLogPlain(injector.getType(), rawOutputInputStream, output);
				}
			} else {
				output = new File(Config.getTempFolder(), injector.getNameWithoutExtension()+"-"+fileNameAppendix+Utils.getUniqueFilenameSuffix()+".fld");
				try (OutputStream os = new FileOutputStream(output)) {
					IOUtils.copy(rawOutputInputStream, os);											
				}
			}											
			return output;
		}
	}
	
	public static abstract class PartitionBoundaryAppendingDecision {
		public abstract boolean appendPartitionBoundary(final Set<Integer> iterationNumbersFound);
		
		public static PartitionBoundaryAppendingDecision getDefaultInstance() {
			return new PartitionBoundaryAppendingDecision() {

				@Override
				public boolean appendPartitionBoundary(Set<Integer> iterationNumbersFound) {
					return false;
				}
			};
		}
	}
	
	public static abstract class TokenReplacementProcessor {
		public abstract InputStream replaceTokens(InputStream rawInputStream);
		
		public static TokenReplacementProcessor getDefaultInstance() {
			return new TokenReplacementProcessor() {

				@Override
				public InputStream replaceTokens(InputStream rawInputStream) {
					return rawInputStream;
				}
				
			};
		}
	}

	public static File processSpecificIterations(
			final File file, 
			final boolean isBwpFile, 
			final Injector injector, 
			final Collection<Integer> iterationsToBeIncluded, 
			final PartitionBoundaryAppendingDecision partitionBoundaryAppendingDecision, 
			final TokenReplacementProcessor tokenReplacementProcessor, 
			final RawOutputProcessor rawOutputProcessor,
			final MutableInt numberOfLines) throws Exception {
		Assert.isTrue(INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()), "Only FLD and IL type injector is accepted. This injector's type = '"+injector.getType()+"'");
		Assert.notNull(iterationsToBeIncluded, "iterationsToBeIncluded must be defined");
		Assert.notNull(partitionBoundaryAppendingDecision, "partitionBoundaryAppendingDecision must be defined");
		Assert.notNull(tokenReplacementProcessor, "tokenReplacementProcessor must be defined");
		Assert.notNull(rawOutputProcessor, "rawOutputProcessor must be defined");
		
		LinkedHashMap<Integer, Boolean> printedIterationNumbers = new LinkedHashMap<Integer, Boolean>();
		LinkedHashMap<Integer, Boolean> allIterationsFound = new LinkedHashMap<Integer, Boolean>();
		
		final File outputRaw = new File(Config.getTempFolder(), UUID.randomUUID().toString());
		try {
			class LineCountTracker {
				boolean isTruncated = false;
				int lineCounter = numberOfLines.intValue();
				
				class SourceFileReaderLoopBreakerException extends Exception {

					/**
					 * 
					 */
					private static final long serialVersionUID = -1047005373597465461L;
					
				}
				
				public void writeToFileSubjectToTheMaxLineCountConstraint(final String line) throws SourceFileReaderLoopBreakerException {
					if (lineCounter < Config.getBuildMaxLinesInPartialSourceFile()) {
						lineCounter++;
						numberOfLines.setValue(lineCounter);
						CoreUtil.writeToFile(line, true, outputRaw.getAbsoluteFile().getParentFile(), outputRaw.getName());
					} else {
						isTruncated = true;
						if (isBwpFile) {
							throw new SourceFileReaderLoopBreakerException();
						}	
					}
				}
			}
			final LineCountTracker lineCountTracker = new LineCountTracker();
			
			final int MAX_LINES_IN_MEMORY = 5;
			
			InputStream inputStream = null;
			ZipFile zipFile = null;
			try {
				if ( isBwpFile && isEncryptedInjectorsPackage(file) )
				{
					inputStream = getInputStreamFromEncryptedZIPFile(file, injector.getName());
				}
				else if (isBwpFile)
				{
					zipFile = new ZipFile(file);
					inputStream = getInputStreamFromUnencryptedZIPFile(zipFile, injector.getName());
				} else {
					inputStream = new FileInputStream(file);
				}
				
				inputStream = tokenReplacementProcessor.replaceTokens(inputStream);

				try ( BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING)) ) {
					String strLine;
					String previousLine = null;
					List<String> tempLines = new ArrayList<String>();
					boolean endOfIterationMarkerIsRead = false;				
					boolean isRecordingToFile = false;
					boolean isRecordingToMemory = true;
					boolean insideLogIterationBlock = false;
					while ( (strLine = br.readLine()) != null ) {
						Assert.isTrue(tempLines.size() <= MAX_LINES_IN_MEMORY, 
								"tempLines size is now "+tempLines.size()+". It has exceeded the maximum limit of "+MAX_LINES_IN_MEMORY+" lines");
						strLine = strLine.trim();
						strLine = SwiftBuildFileUtils.removePasswordFromInputString(strLine, injector.getType()).trim();

						if ( strLine.contains(CoreConstants.ITERATION_SEPARATOR) && 
								((isBwpFile && (INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()))) 
									||
								INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()))) {
							for (String tempLine : tempLines) {
								lineCountTracker.writeToFileSubjectToTheMaxLineCountConstraint(tempLine+"\n");						
							}
							tempLines.clear();
							isRecordingToMemory = false;
							
							int iterationSeparatorIndex = strLine.indexOf(CoreConstants.ITERATION_SEPARATOR);
							int iterationNumber = Integer.parseInt(strLine.substring(iterationSeparatorIndex+CoreConstants.ITERATION_SEPARATOR.length()).trim().split("\\s+")[0]);
							if (iterationsToBeIncluded.contains(iterationNumber)) {
								isRecordingToFile = true;
								if (!lineCountTracker.isTruncated) {
									printedIterationNumbers.put(iterationNumber, false);
								}
								allIterationsFound.put(iterationNumber, false);
							} else {
								isRecordingToFile = false;
							}
						} else if (!isBwpFile && CoreConstants.MENU_CUSTOM_DIAGNOSTICS_EXAMINE.equals(strLine) && !isRecordingToFile && !insideLogIterationBlock) {
							if (!tempLines.isEmpty() && tempLines.get(tempLines.size()-1).equals(previousLine)) {
								tempLines.remove(tempLines.size()-1);
							}
							for (String tempLine : tempLines) {
								lineCountTracker.writeToFileSubjectToTheMaxLineCountConstraint(tempLine+"\n");								
							}
							tempLines.clear();
							
							if (previousLine != null) {
								tempLines.add(previousLine);
								while(tempLines.size() > MAX_LINES_IN_MEMORY) {
									lineCountTracker.writeToFileSubjectToTheMaxLineCountConstraint(tempLines.get(0)+"\n");	
									tempLines.remove(0);
								}
							}
							isRecordingToMemory = true;
						} else if (!isBwpFile && isRecordingToMemory && strLine.contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX)) {
							int prefixIndex = strLine.indexOf(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX);
							int iterationNumber = Integer.parseInt(
									strLine.substring(prefixIndex+CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX.length()).replaceAll("[^\\d]", "")
									);
							if (iterationsToBeIncluded.contains(iterationNumber)) {
								for (String tempLine : tempLines) {
									lineCountTracker.writeToFileSubjectToTheMaxLineCountConstraint(tempLine+"\n");								
								}
								isRecordingToFile = true;
								if (!lineCountTracker.isTruncated) {
									printedIterationNumbers.put(iterationNumber, false);
								}
								allIterationsFound.put(iterationNumber, false);
							} else {
								isRecordingToFile = false;
							}
							isRecordingToMemory = false;
							tempLines.clear();
							insideLogIterationBlock = true;
						} else if (!isBwpFile && strLine.contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX)) {
							if (isRecordingToMemory) {
								for (String tempLine : tempLines) {
									lineCountTracker.writeToFileSubjectToTheMaxLineCountConstraint(tempLine+"\n");								
								}
								tempLines.clear();	
								isRecordingToFile = false;
								isRecordingToMemory = false;							
							}

							insideLogIterationBlock = false;
						} else if (isBwpFile && (SwiftBuildConstants.FLD_SCRIPT_FOOTER_SEPARATOR.equals(strLine) || HTMLRunnerConstants.WORKER_BREAK_SIGNAL.equals(strLine))) {
							isRecordingToFile = true;
						} else if (!isBwpFile && CoreConstants.MENU_MAGIC_MAGIC_QUIT.equals(strLine)) {
							if (previousLine != null) {
								lineCountTracker.writeToFileSubjectToTheMaxLineCountConstraint(previousLine+"\n");						
							}
							isRecordingToFile = true;
						}
						
						
						
						if (isRecordingToFile && strLine.contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX)) {
							endOfIterationMarkerIsRead = true;
							final int closurePrefixIndex = strLine.indexOf(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX);
							final int iterationNumber = Integer.parseInt(
									strLine.substring(closurePrefixIndex+CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX.length()).replaceAll("[^\\d]", "")
									);
							if (!lineCountTracker.isTruncated && printedIterationNumbers.containsKey(iterationNumber) && !printedIterationNumbers.get(iterationNumber)) {
								printedIterationNumbers.put(iterationNumber, true);
							}
							if (allIterationsFound.containsKey(iterationNumber) && !allIterationsFound.get(iterationNumber)) {
								allIterationsFound.put(iterationNumber, true);
							}							
							
						} 
						
						if (isRecordingToFile) {
							lineCountTracker.writeToFileSubjectToTheMaxLineCountConstraint(strLine+"\n");						
						} else if (isRecordingToMemory) {
							tempLines.add(strLine);
							while(tempLines.size() > MAX_LINES_IN_MEMORY) {
								lineCountTracker.writeToFileSubjectToTheMaxLineCountConstraint(tempLines.get(0)+"\n");								
								tempLines.remove(0);
							}							
						}
						
						if (endOfIterationMarkerIsRead &&
								((isBwpFile && 
										(((FLDNavigation.RAPID_BUILD_ITERATION_TRACKING_END).equals(strLine) && (INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType()))) 
											|| 
										(strLine.contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX) && (INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType()))))) 
									|| 
								(!isBwpFile && CoreConstants.USER_EXIT_RESPONSE_DONE_CANCEL.equals(strLine)))) {
							//put an empty line between iterations				
							endOfIterationMarkerIsRead = false;
							isRecordingToFile = false;
							lineCountTracker.writeToFileSubjectToTheMaxLineCountConstraint("\n");
						}
						
						previousLine = strLine;
					}
				}			
			} catch(LineCountTracker.SourceFileReaderLoopBreakerException e) {

			} finally {
				IOUtils.closeQuietly(inputStream);
				IOUtils.closeQuietly(zipFile);
			}	
			
			File output = null;
			
			if (!printedIterationNumbers.isEmpty() && outputRaw.exists()) {
				final boolean incompleteIterationExists = printedIterationNumbers.containsValue(false);
				
				if (partitionBoundaryAppendingDecision.appendPartitionBoundary(allIterationsFound.keySet()) && !lineCountTracker.isTruncated && !incompleteIterationExists) {
					CoreUtil.writeToFile("\n############Partition Boundary############", true, outputRaw.getAbsoluteFile().getParentFile(), outputRaw.getName());
				} else if (!lineCountTracker.isTruncated && incompleteIterationExists) {
					CoreUtil.writeToFile("\n############ERROR. An iteration is not closed############", true, outputRaw.getAbsoluteFile().getParentFile(), outputRaw.getName());
				} else if (lineCountTracker.isTruncated) {
					CoreUtil.writeToFile("\n############TRUNCATED. This file's line count has exceeded the "+Config.BUILD_MAX_LINES_IN_PARTIAL_SOURCE_FILE+" value ("+Config.getBuildMaxLinesInPartialSourceFile()+" lines)############", true, outputRaw.getAbsoluteFile().getParentFile(), outputRaw.getName());
				}
				
				try (FileInputStream fis = new FileInputStream(outputRaw)) {
					output = rawOutputProcessor.processRawOutput(fis);
				}
			}
			
			if (output != null && !output.isFile()) {
				output = null;
			}
			
			return output;			
		} finally {
			if (outputRaw.isFile()) {
				outputRaw.delete();
			}
		}

		
	}
		
}
