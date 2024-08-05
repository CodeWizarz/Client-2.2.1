package com.rapidesuite.client.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import com.rapidesuite.core.utility.CoreUtil;


public class SevenZipUtils
{

	public static class MyExtractCallback implements IArchiveExtractCallback {

		private long itemsTotal;
		JLabel progressLabel;
		boolean hasWarnings;
		StringBuffer warnings;
		JLabel warningsLabel;
		final ISevenZipInArchive inArchive;
		final String extractPath;

		public MyExtractCallback(ISevenZipInArchive inArchive, String extractPath,
				JLabel progressLabel,JLabel warningsLabel,int itemsTotal) {
			this.inArchive = inArchive;
			this.extractPath = extractPath;
			this.progressLabel = progressLabel;
			this.warningsLabel=warningsLabel;
			this.itemsTotal=itemsTotal;
			hasWarnings=false;
			warnings=new StringBuffer("");
		}

		public ISequentialOutStream getStream(final int index,
				ExtractAskMode extractAskMode) throws SevenZipException {
			if (progressLabel!=null) {
				GUIUtils.showInProgressMessage(progressLabel,"In progress: "+index+" / "+itemsTotal+" ...");
			}

			String filePath = inArchive.getStringProperty(index, PropID.PATH);
			final File dir = new File(extractPath);
			final File path = new File(extractPath +UtilsConstants.FORWARD_SLASH+ filePath);
			if (path.exists()) {
				path.delete();
			}
			if (extractAskMode != ExtractAskMode.EXTRACT) {
				return null;
			}
			return new ISequentialOutStream() {

				public int write(byte[] data) throws SevenZipException {
					FileOutputStream fos = null;
					try {

						if (!dir.exists()) {
							dir.mkdirs();
						}
						File parentFolder=path.getParentFile();
						if (parentFolder!=null && !parentFolder.exists()) {
							parentFolder.mkdirs();
						}
						if (!path.exists()) {
							path.createNewFile();
						}
						fos = new FileOutputStream(path, true);
						fos.write(data);
					}
					catch (IOException e) {
						FileUtils.printStackTrace(e);
						String tmp="Error: Unable to extract file: '"+path+"' from archive: "+e.getMessage();
						GUIUtils.popupErrorMessage(tmp);
					}
					finally {
						try {
							if (fos != null) {
								fos.flush();
								fos.close();
							}
						}
						catch (IOException e) {
							FileUtils.printStackTrace(e);
						}
					}
					return data.length; // Return amount of proceed data
				}
			};
		}

		public void prepareOperation(ExtractAskMode extractAskMode)
		throws SevenZipException {
			//System.out.println("prepareOperation:");
		}

		public void setOperationResult(ExtractOperationResult
				extractOperationResult) throws SevenZipException {
			if (extractOperationResult != ExtractOperationResult.OK) {
				hasWarnings=true;
				String tmp="Unable to extract file.";
				warnings.append(tmp+"<br>");
				FileUtils.println(tmp);
			}
		}

		public void setCompleted(long completeValue) throws SevenZipException {
			//System.out.println("completeValue:"+completeValue);
		}

		public void setTotal(long total) throws SevenZipException {
			//System.out.println("total:"+total);
		}

		public boolean hasWarnings() {
			return hasWarnings;
		}

		public StringBuffer getWarnings() {
			return warnings;
		}

	}

	public static void unpackFrom7zFile(File archiveFile,File destinationFolder) throws Exception {
		unpackFrom7zFile(archiveFile,null,null,destinationFolder);
	}

	public static void unpackFrom7zFile(File archiveFile,JLabel progressLabel,
			JLabel warningsLabel,File destinationFolder) throws Exception {
		RandomAccessFile randomAccessFile = null;
		ISevenZipInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(archiveFile, "r");
			inArchive = SevenZip.openInArchive(null,new RandomAccessFileInStream(randomAccessFile));

			int count = inArchive.getNumberOfItems();
			List<Integer> itemsToExtract = new ArrayList<Integer>();
			for (int i = 0; i < count; i++) {
				if (!((Boolean) inArchive.getProperty(i, PropID.IS_FOLDER))
						.booleanValue()) {
					itemsToExtract.add(Integer.valueOf(i));
				}
			}
			int[] items = new int[itemsToExtract.size()];
			int i = 0;
			for (Integer integer : itemsToExtract) {
				items[i++] = integer.intValue();
			}
			String extractPath=destinationFolder.getPath();
			MyExtractCallback ex=new MyExtractCallback(inArchive, extractPath,progressLabel,warningsLabel,itemsToExtract.size());
			inArchive.extract(items, false,ex);

			if (warningsLabel!=null && ex.hasWarnings()) {
				GUIUtils.showWarningMessage(warningsLabel,"<html>Warnings:<br>"+ex.getWarnings());
			}
		}
		catch(Exception e) {
			String tmp="Unable to extract from archive: "+e.getMessage();
			FileUtils.printStackTrace(e);
			if (warningsLabel!=null) {
				GUIUtils.showWarningMessage(warningsLabel,"<html>Error:<br>"+tmp);
			}
			else {
				throw e;
			}
		}
		finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				} catch (SevenZipException e) {
					FileUtils.println("Error closing archive: " + e);
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					FileUtils.println("Error closing file: " + e);
				}
			}
		}
	}

	public static String getContentFrom7ZIPFile(
			String inventoryFileName,
			File zipFile)
	throws Exception
	{
		InputStream fileInputStream=null;
		try{
			fileInputStream=getStreamFrom7ZIPFile(inventoryFileName,zipFile);
			return IOUtils.toString(fileInputStream);
		}
		finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}


    public static InputStream getStreamFrom7ZIPFile(
            String entryName,
            File zipFile)
    throws Exception
    {
        return getStreamFrom7ZIPFile(entryName, zipFile, true);
    }

	public static InputStream getStreamFrom7ZIPFile(
			String entryName,
			File zipFile,
			boolean decryptEncryptedStreams)
	throws Exception
	{
        File temporaryFile=SevenZipUtils.getTemporaryFileFrom7ZIPFile(entryName,zipFile);
	    try (InputStream tempStream=new FileInputStream(temporaryFile);)
	    {
	        if (decryptEncryptedStreams && CoreUtil.isXMLStreamEncrypted(tempStream)) {
	            return CoreUtil.getUnencryptedInputStream(new FileInputStream(temporaryFile));
	        }
	        return new FileInputStream(temporaryFile);
	    }
	}

	public static File getTemporaryFileFrom7ZIPFile(
			String entryName,
			File zipFile)
	throws Exception
	{
		File tempFolder = Files.createTempDirectory("7zipTempFolder_"+CoreUtil.getFileNameWithoutExtension(zipFile)).toFile();
		decompressFile(zipFile, tempFolder);
		tempFolder.deleteOnExit();
		return getSingleFileByName(tempFolder, entryName);
	}
	
	public static void decompressFile(final File inputFile, final File outputFolder)
	throws Exception
	{
		CoreUtil.decompressFile(Config.getPathTo7zip(), inputFile, outputFolder, false);
	}
	
	
	public static File getSingleFileByName(final File directory, final String fileName) {
		File[] files = directory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File f, String s) {
				return fileName.equals(s);
			}
			
		});
		
		Assert.notEmpty(files);
		Assert.isTrue(files.length==1, "Expected only one file but got "+files.length);
		return  files[0];
	}

    public static List<String> getItemPaths(File archiveFile) throws Exception  {
        ISevenZipInArchive inArchive = null;
        RandomAccessFile randomAccessFile= null;
        try{
            randomAccessFile = new RandomAccessFile(archiveFile, "r");
            inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
            List<String> toReturn=new ArrayList<String>();
            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                String itemPath=item.getPath();
                itemPath = CoreUtil.normalizePathSeparators(itemPath);
                toReturn.add(itemPath);
            }
            return toReturn;
        }
        finally {
            if (inArchive != null) {
                inArchive.close();
            }
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
    }


	public static List<String> getXmlPathsFrom7ZIPFileThatAreNotInControlFolder(File archiveFile) throws Exception	{
		List<String> toReturn=new ArrayList<String>();
		for ( String path : getItemPaths(archiveFile) )
		{
            if (!FileUtils.isZIPEntryToIgnore(false,path)){
                toReturn.add(path);
            }
		}
		return toReturn;
	}

}