package com.rapidesuite.client.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZipException;


public class SevenZipStream implements ISequentialOutStream
{

	private File temporaryFile; 

	public SevenZipStream() throws IOException {
		File tempFolder=FileUtils.getTemporaryFolder();
		temporaryFile = File.createTempFile("7zipTempFile", ".xml",tempFolder); 
		temporaryFile.deleteOnExit();
	}
	
	public int write(byte[] data) throws SevenZipException {
		FileOutputStream fos = null;
		try { 

			fos=new FileOutputStream(temporaryFile,true);
			fos.write(data);
		}
		catch (IOException e) { 
			FileUtils.printStackTrace(e);
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
		return data.length;
	}

	public File getTemporaryFile() {
		return temporaryFile;
	}

}