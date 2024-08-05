package com.rapidesuite.snapshot.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.snapshot.view.SnapshotPackageSelectionPanel;

public class PackagesUnzipSwingWorker extends SnapshotSwingWorker {

	private SnapshotPackageSelectionPanel snapshotMainPackageSelectionPanel;
	private List<File> packagesList;
	private String prefixMsg;
	
	public PackagesUnzipSwingWorker(SnapshotPackageSelectionPanel snapshotMainPackageSelectionPanel,List<File> packagesList) {
		this.snapshotMainPackageSelectionPanel=snapshotMainPackageSelectionPanel;
		this.packagesList=packagesList;
	}

	@Override
	protected Void doInBackground() throws Exception {
		int index=0;
		for (File file:packagesList) {
			index++;
			prefixMsg="Unzipping "+index+" / "+packagesList.size()+" package(s)...";
			super.updateExecutionLabels(prefixMsg);
			unpackFrom7zArchiveFile(file);
		}
		
		return null;
	}

	private void unpackFrom7zArchiveFile(File file) {
		RandomAccessFile randomAccessFile = null;
		ISevenZipInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
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
			File outputFolder=new File(file.getParent());
			String extractPath=outputFolder.getPath();
			Snapshot7zExtractCallback ex=new Snapshot7zExtractCallback(this,inArchive, extractPath,itemsToExtract.size(),prefixMsg);
			inArchive.extract(items, false,ex);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
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
	
	public static class Snapshot7zExtractCallback implements IArchiveExtractCallback {

		private long itemsTotal;
		private final ISevenZipInArchive inArchive;
		private final String extractPath;
		private PackagesUnzipSwingWorker snapshotPackageUnpackSwingWorker;
		private String prefixMsg;
		
		public Snapshot7zExtractCallback(PackagesUnzipSwingWorker snapshotPackageUnpackSwingWorker,
				ISevenZipInArchive inArchive, String extractPath,int itemsTotal,String prefixMsg) {
			this.snapshotPackageUnpackSwingWorker=snapshotPackageUnpackSwingWorker;
			this.inArchive = inArchive;
			this.extractPath = extractPath;
			this.itemsTotal=itemsTotal;
			this.prefixMsg=prefixMsg;
		}

		public ISequentialOutStream getStream(final int index,
				ExtractAskMode extractAskMode) throws SevenZipException {
			try {
				snapshotPackageUnpackSwingWorker.updateExecutionLabels(prefixMsg+" Files processed: "+
						 Utils.formatNumberWithComma(index)+" / "+ Utils.formatNumberWithComma((int)itemsTotal));
			} catch (Exception e1) {
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
			}
		}

		public void setCompleted(long completeValue) throws SevenZipException {
			//System.out.println("completeValue:"+completeValue);
		}

		public void setTotal(long total) throws SevenZipException {
			//System.out.println("total:"+total);
		}
	}

	public SnapshotPackageSelectionPanel getSnapshotMainPackageSelectionPanel() {
		return snapshotMainPackageSelectionPanel;
	}
	
}
