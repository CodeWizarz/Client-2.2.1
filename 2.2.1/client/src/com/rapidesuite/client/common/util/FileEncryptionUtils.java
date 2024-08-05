/**************************************************
 * $Revision: 39031 $:
 * $Author: john.snell $:
 * $Date: 2014-02-14 13:50:12 +0700 (Fri, 14 Feb 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/util/FileEncryptionUtils.java $:
 * $Id: FileEncryptionUtils.java 39031 2014-02-14 06:50:12Z john.snell $:
 */

package com.rapidesuite.client.common.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import org.apache.commons.io.IOUtils;

import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.Encryption;

public class FileEncryptionUtils
{
    private static void encryptOrDecryptAndCopyFolder(boolean isEncryptNotDecrypt,File source,File destination, String fileExtensionsToProcess[]) throws Exception {
		if (source.isDirectory()) {
			String tmp=source.getName();
			if (tmp.indexOf("svn")!=-1) {
				System.out.println("Ignoring folder: '"+tmp+"'");
				return;
			}
			if (!destination.exists()) {
				destination.mkdir();
			}
			String[] children = source.list();
			for (int i=0; i<children.length; i++) {
				encryptOrDecryptAndCopyFolder(isEncryptNotDecrypt,new File(source, children[i]),new File(destination, children[i]), fileExtensionsToProcess);
			}
		}
		else {
		    File originalDestination = null;
		    if ( source.getAbsolutePath().equals(destination.getAbsolutePath()) )
		    { //same file; need temporary
		        originalDestination = destination;
		        destination = File.createTempFile(destination.getName(), "temp");
		    }

			if (isIncludeFileExtension(source,fileExtensionsToProcess)) {
			    if ( isEncryptNotDecrypt )
			    {
			        encryptAndCopyFile(source,destination);
			    }
			    else
			    {
			        decryptAndCopyFile(source,destination);
			    }
			}
			if ( originalDestination != null )
			{
			    org.apache.commons.io.FileUtils.copyFile(destination, originalDestination);
			}
		}
	}

    private static void decryptAndCopyFile(File sourceFile,File destinationFile) throws Exception {
        OutputStream out=null;
        CipherInputStream cin=null;
        try{
            Cipher cipher = Encryption.getDecryptedCipherInstance();
            cin = new CipherInputStream(new FileInputStream(sourceFile), cipher);
            out = new FileOutputStream(destinationFile);
            IOUtils.copy(cin, out);
        }
        finally
        {
            IOUtils.closeQuietly(cin);
            IOUtils.closeQuietly(out);
        }
    }


	private static void encryptAndCopyFile(File sourceFile,File destinationFile) throws Exception {
		InputStream in=null;
		CipherOutputStream cout=null;
		try{
			in = new FileInputStream(sourceFile);
			Cipher cipher = Encryption.getEncryptedCipherInstance();
			cout = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(destinationFile)), cipher);
			IOUtils.copy(in, cout);
		}
		finally
		{
		    IOUtils.closeQuietly(cout);
            IOUtils.closeQuietly(in);
		}
	}

	private static boolean isIncludeFileExtension(File sourceFile,String array[]){
		for (String token:array) {
			if ( CoreUtil.getFileExtension(sourceFile).equalsIgnoreCase(token)) {
				return true;
			}
		}
		return false;
	}

	final static String ENCRYPT = "encrypt";
	final static String DECRYPT = "decrypt";

	private static final void printUsageAndDie()
	{
		System.err.println("--------------------------------------------");
		System.err.println("Usage:  com.rapidesuite.client.common.util.FileEncryptionUtils <" + ENCRYPT + "|" + DECRYPT + "> <source> <destination> <include-files-type-list>");
		System.err.println("--------------------------------------------");
		System.exit(-1);
	}

	public static void main(String[] args)
	{
		if (args.length != 4){
			printUsageAndDie();
		}
		try{
		    int index = 0;
		    String encdec = args[index++];
            System.out.println("encdec: " + encdec);
		    if ( !ENCRYPT.equals(encdec) && !DECRYPT.equals(encdec) )
		    {
		        printUsageAndDie();
		    }
		    boolean isEncryptNotDecrypt = ENCRYPT.equals(encdec);

			String source=args[index++];
			System.out.println("source: '"+source+"'");

			String destination=args[index++];
			System.out.println("destination: '"+destination+"'");

			String includeFilesTypeList=args[index++];
			String array[]= includeFilesTypeList.split(",");
			System.out.println("include-files-type-list: '"+includeFilesTypeList+"'");

			FileEncryptionUtils.encryptOrDecryptAndCopyFolder(isEncryptNotDecrypt,new File(source),new File(destination), array);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}