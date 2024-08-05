package com.rapidesuite.client.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.swing.JLabel;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.Assert;

import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;
import com.rapidesuite.client.common.gui.InventoryTreeNode;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.licenseClient0001.License;
import com.rapidesuite.client.licenseClient0001.LicenseDocument;
import com.rapidesuite.configurator.DataFactory;
import com.rapidesuite.configurator.GeneralConstants;
import com.rapidesuite.configurator.InventoryFactory;
import com.rapidesuite.configurator.domain.FLDBlock;
import com.rapidesuite.configurator.domain.HTMLInventory;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.configurator.utility.LicenseEncryptor;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.core.inventory0007.FldFormFieldData;
import com.rapidesuite.core.inventory0007.HtmlFormFieldData;
import com.rapidesuite.core.inventory0007.InventoryConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.utility.Encryption;
import com.rapidesuite.core.utility.EnvironmentPropertyConstants;
import com.sun.jna.Platform;

public class FileUtils
{

	private static File logFile;
	public static final String CLIENT_LICENCE_XML_FILE = "licenseClient.xml";
	
	// windows doesn't allow the folder-name to end with a dot or space, doesn't throw error - just removes these characters from the name, so we need to remove it too.
	// here is the method to do it.
	public static String removeTrailingInvalidCharactersForWindows(String name) {
		if(!Platform.isWindows())
			return name;
		
		while(name.endsWith(".") || name.endsWith(" ")) {
			name = name.substring(0, name.length()-1);
		}
		return name;
	}

	public static List<File> listAllFiles(File rootFolder,boolean isRecursive,String fileExtension)
			throws Exception
			{
		List<File> res=new ArrayList<File>();
		if ( rootFolder.isFile() )
		{
			res.add(rootFolder);
			return res;
		}

		if (rootFolder.getName().indexOf(".svn")!=-1) {
			return res;
		}

		File [] children = rootFolder.listFiles();
		if (children==null) {
			return res;
		}
		for ( int i=0;i<children.length;i++ )
		{
			File child=children[i];
			if ( child.isFile() )
			{
				if (fileExtension!=null) {
					boolean isCorrectFile=child.getName().toLowerCase().endsWith("."+fileExtension.toLowerCase());
					if ( isCorrectFile )
					{
						res.add(child);
					}
				}
				else {
					res.add(child);
				}
			}
			else
			{
				if (isRecursive) {
					res.addAll(listAllFiles(child,isRecursive,fileExtension));
				}
			}
		}
		return res;
			}

	public static String readContentsFromSQLFile(File file)
			throws Exception
	{
		InputStream is=null;
		try
		{
			is=getInputStreamFromSQLFile(file);
			return IOUtils.toString(is);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
	}

	public static String readContentsAsStringFromPLSQLFile(File file)
			throws Exception
	{
		InputStream is=null;
		try
		{
			is=getInputStreamFromPLSQLFile(file);
			return IOUtils.toString(is);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
	}

	public static InputStream getInputStreamFromSQLFile(File file)
			throws Exception
	{
		if (CoreUtil.isSQLFileEncrypted(file))
		{
			return CoreUtil.getUnencryptedInputStream(file);
		}
		return new FileInputStream(file);
	}

	private static InputStream getInputStreamFromPLSQLFile(File file)
			throws Exception
	{
		if (CoreUtil.isSQLFileEncrypted(file))
		{
			return CoreUtil.getUnencryptedInputStream(file);
		}
		return new FileInputStream(file);
	}
	
	public static String getMajorApplicationVersion(ClassLoader cl,String resourcePath) {
		try {
			String applicationVersion = getApplicationVersion(cl, resourcePath);
			return applicationVersion.substring(0, applicationVersion.lastIndexOf("."));
		} catch (Exception ex) {
			//FileUtils.printStackTrace(ex);
			return "N/A";
		}
	}

	public static String getApplicationVersion(ClassLoader cl,String resourcePath)
			throws Exception{
		InputStream in=null;
		try
		{
			in = cl.getResourceAsStream(resourcePath);
			StringBuilder inputStringBuilder = new StringBuilder();
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
	        String line = bufferedReader.readLine();
	        while(line != null){
	            inputStringBuilder.append(line);inputStringBuilder.append('\n');
	            line = bufferedReader.readLine();
	        }

			return inputStringBuilder.toString();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return "N/A";
		}
		finally
		{
			if (in != null) {
				in.close();
			}
		}
	}

	public static String createOutputSubFolder(String outputFolder)
	{
		File file=new File(outputFolder+UtilsConstants.FORWARD_SLASH);
		file.mkdirs();
		return file.toString();
	}


	public static String getSQLVersionFromZIPInventoriesPackage(File zipFile)
			throws Exception
	{
		InputStream inputStream=null;
		ZipInputStream zipInputStream = null;
		try
		{
			inputStream = new FileInputStream(zipFile);
			ZipEntry zipEntry;
			zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream) );

			String res=null;
			while ( (zipEntry = zipInputStream.getNextEntry()) != null )
			{
				String fileName=zipEntry.getName();
				if ( fileName.indexOf("targetVersion.txt")!=-1 )
				{
					BufferedReader br=null;
					try
					{
						InputStreamReader isr = new InputStreamReader(zipInputStream);
						br = new BufferedReader(isr);
						return br.readLine();
					}
					finally
					{
						if ( br!=null ) br.close();
					}
				}
			}
			return res;
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return null;
		}
		finally
		{
			if (zipInputStream!=null) try{ zipInputStream.close(); } catch(Exception e) {FileUtils.printStackTrace(e);}
			IOUtils.closeQuietly(inputStream);
		}
	}

	public static String getEBSTargetVersionFromInventoriesPackage(File zipFile, String fileExtension)
			throws Exception
	{
		if (fileExtension!=null && fileExtension.equalsIgnoreCase("zip")) {
			return getSQLVersionFromZIPInventoriesPackage(zipFile);
		}
		else
			if (fileExtension!=null && fileExtension.equalsIgnoreCase("7z")) {
				return getEBSTargetVersionFrom7ZIPInventoriesPackage(zipFile);
			}
			else {
				throw new Exception("Invalid Reverse archive");
			}
	}

	public static String getEBSTargetVersionFrom7ZIPInventoriesPackage(File dir)
			throws Exception
	{
		final String targetVersionFileName = "targetVersion.txt";
		File temporaryFile = SevenZipUtils.getSingleFileByName(dir, targetVersionFileName);
		Assert.notNull(temporaryFile, "Invalid file. The mandatory "+targetVersionFileName+" is missing from the inventories package file. Please ensure that you selected the correct file, and extract the file to verify that the file is not corrupted.");
		BufferedReader br=null;
		try
		{
			InputStreamReader isr = new InputStreamReader(new FileInputStream(temporaryFile));
			br = new BufferedReader(isr);
			return br.readLine();
		}
		finally
		{
			IOUtils.closeQuietly(br);
		}
	}

	public static void zipFiles(Set<File> srcFiles, File destZipFile) throws Exception {
		Assert.notNull(srcFiles);
		Assert.notEmpty(srcFiles);
		for (File f : srcFiles) {
			Assert.isTrue(f.exists());
		}
		ZipOutputStream zipOutputStream = null;
		try{
			FileOutputStream fileOutputStream = new FileOutputStream(destZipFile);
			zipOutputStream = new ZipOutputStream(fileOutputStream);
			for (File f : srcFiles) {
				ZipEntry ze= new ZipEntry(f.getName());
				zipOutputStream.putNextEntry(ze);
				FileInputStream in = new FileInputStream(f);
				int len;
				byte[] buffer = new byte[1024];
				while ((len = in.read(buffer)) > 0) {
					zipOutputStream.write(buffer, 0, len);
				}
				in.close();
				zipOutputStream.closeEntry();
			}
		}
		finally{
			IOUtils.closeQuietly(zipOutputStream);
		}
	}

	public static void zipFolder(File srcFolder,File destZipFile) throws Exception {
		if (!srcFolder.exists() || !hasAtLeastOneFile(srcFolder)) {
			return;
		}
		ZipOutputStream zipOutputStream = null;
		try{
			FileOutputStream fileOutputStream = new FileOutputStream(destZipFile);
			zipOutputStream = new ZipOutputStream(fileOutputStream);
			addFolderToZip("", srcFolder.getAbsolutePath(), zipOutputStream);
			zipOutputStream.flush();
		}
		finally{
			IOUtils.closeQuietly(zipOutputStream);
		}
	}

	private static void addFileToZip(String path, String srcFile, ZipOutputStream zip)
			throws Exception {
		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		}
		else {
			byte[] buf = new byte[1024];
			int len;
			FileInputStream in=null;
			try{
				in = new FileInputStream(srcFile);
				zip.putNextEntry(new ZipEntry(path + UtilsConstants.FORWARD_SLASH + folder.getName()));
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
			}
			finally{
				IOUtils.closeQuietly(in);
			}
		}
	}

	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
			throws Exception {
		File folder = new File(srcFolder);
		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + UtilsConstants.FORWARD_SLASH + fileName, zip);
			} else {
				addFileToZip(path + UtilsConstants.FORWARD_SLASH + folder.getName(), srcFolder + UtilsConstants.FORWARD_SLASH + fileName, zip);
			}
		}
	}

	public static boolean hasAtLeastOneFile(File folder)
	{
		if (folder.isFile()) {
			return true;
		}
		String fileList[] = folder.list();
		if (fileList==null) {
			return false;
		}
		for ( int i = 0;i<fileList.length;i++ )
		{
			boolean hasAtLeastOneFile=hasAtLeastOneFile(
					new File(folder,fileList[i]) );
			if (hasAtLeastOneFile) {
				return true;
			}
		}
		return false;
	}

	public static void createSQLFile(String tableName,String sqlStatement,String subOutputFolder)
			throws Exception
	{
		PrintStream ps = null;
		try
		{
			if ( sqlStatement==null || sqlStatement.trim().equals("") )
			{
				return;
			}

			String sqlFileName=subOutputFolder+UtilsConstants.FORWARD_SLASH+tableName+".sql";
			FileUtils.println("Creating SQL file: '"+sqlFileName+"'");
			ps = new PrintStream(new FileOutputStream (sqlFileName));
			ps.println(sqlStatement);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
		finally
		{
			IOUtils.closeQuietly(ps);
		}
	}

	public static String getSQLStatementFromFile(String inputFolder,File excelFile,String rscTableName,
			String sqlFolder)
					throws Exception
	{
		BufferedReader in=null;
		try
		{
			String path=excelFile.getPath();
			String name=excelFile.getName();
			//FileUtils.println("path:"+path);
			//FileUtils.println("name:"+name);

			//FileUtils.println("inputFolder:"+inputFolder);
			String tmp=inputFolder;
			//FileUtils.println("tmp:"+tmp);
			int indexOf=path.indexOf(tmp);
			if ( indexOf==-1 ) throw new Exception("Unable to find the Inventory file: "+path);

			String subPath=path.substring(indexOf+inputFolder.length());
			//FileUtils.println("subpath:"+subPath);

			indexOf=subPath.indexOf(name);
			subPath=subPath.substring(0,indexOf);

			String folderSQL=sqlFolder+UtilsConstants.FORWARD_SLASH+subPath;
			File folder = new File(folderSQL);
			File[] listOfFiles = folder.listFiles();
			String sqlFileName=null;
			if ( listOfFiles!=null )
			{
				for ( int i = 0; i < listOfFiles.length; i++ )
				{
					File file=listOfFiles[i];
					if ( file.isFile() )
					{
						String fileName=file.getName();
						//FileUtils.println("fileName:"+fileName);
						if ( fileName.startsWith(rscTableName+".") )
						{
							FileUtils.println("Found a file: '"+fileName+"' matching the table name: '"+
									rscTableName+"'");
							sqlFileName=file.getAbsolutePath();
							break;
						}
					}
				}
			}
			else
			{
				FileUtils.println("Cannot find the SQL file for the table: '"+rscTableName+
						"' (no files in the folder)");
				return "";
			}

			if ( sqlFileName==null )
			{
				FileUtils.println("Cannot find the SQL file for the table: '"+rscTableName+"'");
				return "";
			}
			//String sqlFileName=sqlFolder+"/"+subPath+rscTableName;
			//FileUtils.println("SQL file:"+sqlFileName);

			in = new BufferedReader(new FileReader(sqlFileName));
			String str;
			StringBuffer res=new StringBuffer("");
			while ( (str = in.readLine()) != null )
			{
				res.append(str+"\n");
			}
			return res.toString();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return "";
		}
		finally
		{
			IOUtils.closeQuietly(in);
		}
	}

	public static Inventory getInventoryFromArchive(String inventoryFileName,File archiveFile)
			throws Exception
	{
		String fileExtension=CoreUtil.getFileExtension(archiveFile);
		if (fileExtension!=null && fileExtension.equalsIgnoreCase("zip")) {
			return getInventoryFromZIPFile(inventoryFileName,archiveFile);
		}
		else
			if (fileExtension!=null && fileExtension.equalsIgnoreCase("7z")) {
				return getInventoryFrom7ZIPFile(inventoryFileName,archiveFile);
			}
			else {
				throw new Exception("Invalid Reverse archive");
			}
	}

	public static String getContentFromArchiveEntry(
			String entryName,
			File zipFile)
					throws Exception
	{
		String fileExtension=CoreUtil.getFileExtension(zipFile);
		if (fileExtension!=null && fileExtension.equalsIgnoreCase("zip")) {
			return getContentFromZIPFile(entryName,zipFile);
		}
		else
			if (fileExtension!=null && fileExtension.equalsIgnoreCase("7z")) {
				return SevenZipUtils.getContentFrom7ZIPFile(entryName,zipFile);
			}
			else {
				throw new Exception("Invalid Reverse archive");
			}
	}

	private static String getContentFromZIPFile(
			String entryName,
			File archiveFile)
					throws Exception
	{
		InputStream fileInputStream=null;
		ZipFile zipFile=null;
		try{
			zipFile=new ZipFile(archiveFile);
			fileInputStream=getStreamFromZIPFile(zipFile,entryName);
			return IOUtils.toString(fileInputStream);
		}
		finally {
			IOUtils.closeQuietly(fileInputStream);
			if (zipFile!=null) {
				zipFile.close();
			}
		}
	}

	public static InputStream getStreamFromZIPFile(ZipFile zipFile,String entryName)
			throws Exception
	{
		InputStream fileInputStream=null;
		InputStream stream=null;

		ZipEntry zipEntry=zipFile.getEntry(entryName);
		if (zipEntry==null) {
			return null;
		}
		stream=zipFile.getInputStream(zipEntry);
		if (CoreUtil.isXMLStreamEncrypted(stream)) {
			stream=zipFile.getInputStream(zipEntry);
			fileInputStream=CoreUtil.getUnencryptedInputStream(stream);
		}
		else {
			fileInputStream=zipFile.getInputStream(zipEntry);
		}
		return fileInputStream;
	}

	public static boolean hasExcelDataFile(File archiveFile) throws ZipException, IOException
	{
		ZipFile zipFile=null;
		try{
			zipFile=new ZipFile(archiveFile);
			Enumeration<? extends ZipEntry> zipEntries=zipFile.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry zipEntry=zipEntries.nextElement();
				String zipEntryName=zipEntry.getName();
				if (zipEntryName!=null && zipEntryName.indexOf("generation/data")!=-1
						&& zipEntryName.indexOf(".xls")!=-1) {
					return true;
				}
			}
			return false;
		}
		finally {
			if (zipFile!=null) {
				zipFile.close();
			}
		}
	}
	public static void savePropertiesToFile(
			File originalFile,
			File file,
			Map<String,String> props,
			boolean isEncrypted)
					throws Exception
	{
		FileOutputStream fileOutputStream=null;
		OutputStream outputStream=null;
		try{

			boolean isOriginalFileEncrypted=false;
			if (originalFile!=null) {
				isOriginalFileEncrypted=CoreUtil.isEnvironmentFileEncrypted(originalFile);
			}

			fileOutputStream=new FileOutputStream(file);

			if (isEncrypted || isOriginalFileEncrypted) {
				outputStream=new CipherOutputStream(fileOutputStream,Encryption.getEncryptedCipherInstance());
			}
			else {
				outputStream=fileOutputStream;
			}

			String line="";
			for (Iterator<Entry<String, String>> it = props.entrySet().iterator(); it.hasNext() ;)
			{
				Map.Entry<String, String> entry = it.next();
				line=entry.getKey() + "=" + entry.getValue()+"\n";
				outputStream.write(line.getBytes());
			}
		}
		finally {
			// the ordering is VERY important, otherwise some bytes will not be written out!!!!!!
			if (outputStream!=null) {
				outputStream.close();
			}
			if (fileOutputStream!=null) {
				fileOutputStream.close();
			}
		}
	}

	public static File getFileFromSQLName(File sqlFolder,String sqlFileName)
	{
		if (sqlFileName==null) {
			return null;
		}
		File file=new File(sqlFolder,sqlFileName);

		return  file;
	}

	public static String getSubversionRevisionNumberFromSQLFile(File sqlFile)
	{
		return getSubversionRevisionNumberFromSQLFile(sqlFile,UtilsConstants.SUBVERSION_REVISION_XML_KEYWORD);
	}

	public static String getSubversionRevisionNumberFromInventoryFile(File zipFile,String inventoryEntryName)
	{
		String res="N/A";
		try{
			String content=getContentFromArchiveEntry(inventoryEntryName,zipFile);
			return getRevisionNumberFromContent(content,UtilsConstants.SUBVERSION_REVISION_XML_KEYWORD);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return res;
		}
	}

	public static String getSubversionRevisionNumberFromSQLFile(File file,String token)
	{
		String res="N/A";
		try{
			if (!file.exists()) {
				return res;
			}
			String content=readContentsFromSQLFile(file);

			return getRevisionNumberFromContent(content,token);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return res;
		}
	}

	public static String getRevisionNumberFromContent(String content,String token)
	{
		String res="N/A";
		try{
			if (content!=null) {
				int startIndex=content.indexOf(token);
				if (startIndex!=-1) {
					int endIndex=content.indexOf("$:",startIndex);
					res=content.substring(
							startIndex+token.length(),
							endIndex);
				}
			}
			return res;
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return res;
		}
	}

	public static void initializeSevenZipLibrary()
			throws SevenZipNativeInitializationException
	{
		SevenZip.initSevenZipFromPlatformJAR();
		FileUtils.println("7-Zip-JBinding library was initialized");
	}

	/**
	 * "ignorable" in this sense means, not an inventory XML file, which is to say, any XML file not located in the control folder.
	 * (leaving original logic)
	 * @param isDirectory
	 * @param zipEntryName
	 * @return
	 * @throws Exception
	 */
	public static boolean isZIPEntryToIgnore(boolean isDirectory,String zipEntryName)
			throws Exception
	{
		if (isDirectory || CoreUtil.normalizePathSeparators(zipEntryName).startsWith(CoreConstants.CONTROL_OUTPUT_FOLDER_NAME + UtilsConstants.FORWARD_SLASH) ) {
			return true;
		}

		if (GeneralConstants.REVERSE_INVENTORIES_PACKAGE_TREE_STRUCTURE_DESCRIPTOR_FILE.equals(zipEntryName)) {
			return true;
		}

		String fileExtension=CoreUtil.getFileExtension(zipEntryName);
		if ( fileExtension==null || !fileExtension.equalsIgnoreCase("xml") ) {
			return true;
		}
		return false;
	}

	public static int getIndexOfSeparator(String path,boolean isFirstIndex)
			throws Exception
	{
		String temp=path;
		int indexOf=-1;
		if (isFirstIndex) {
			indexOf=temp.indexOf(UtilsConstants.FORWARD_SLASH);
		}
		else {
			indexOf=temp.lastIndexOf(UtilsConstants.FORWARD_SLASH);
		}

		if ( indexOf==-1 ) {
			if (isFirstIndex) {
				indexOf=temp.indexOf("\\");
			}
			else {
				indexOf=temp.lastIndexOf("\\");
			}
		}
		return indexOf;
	}



	public static String replaceSVNRevisionKeywords(String text)
	{
		String temp=text.replaceAll(UtilsConstants.SUBVERSION_START_REVISION_KEYWORD,"");
		temp=temp.replaceAll(UtilsConstants.SUBVERSION_END_REVISION_KEYWORD,"");
		return temp.trim();
	}

	public static List<String> readContentsFromPLSQLFile(File file)
			throws Exception
			{
		InputStream is=null;
		try
		{
			is=getInputStreamFromSQLFile(file);
			return readContentsFromInputStream(is,UtilsConstants.FORWARD_SLASH);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
			}

	public static List<String> readContentsFromInputStream(InputStream is,String lineSeparator)
			throws Exception
			{
		BufferedReader br=null;
		try
		{
			br = new BufferedReader(new InputStreamReader(is));

			String line;
			StringBuffer buffer=new StringBuffer();
			List<String> res=new ArrayList<String>();
			while ( (line = br.readLine()) != null )
			{
				if (lineSeparator==null) {
					res.add(line);
					continue;
				}
				if (line.trim().equals(lineSeparator)) {
					res.add(buffer.toString());
					buffer.setLength(0);
					continue;
				}
				buffer.append(line);
				buffer.append("\n");
			}
			String temp = buffer.toString().trim();
			if ( temp.length() > 0 )
			{
				res.add(temp);
			}
			return res;
		}
		finally
		{
			IOUtils.closeQuietly(br);
		}
			}

	public static void unpackFromZIPFile(File archiveFile,File destinationFolder,JLabel progressLabel)
			throws Exception
	{
		unpackFromZIPFile(archiveFile,destinationFolder,progressLabel,true);
	}

	@SuppressWarnings("unchecked")
	public static void unpackFromZIPFile(File archiveFile,File destinationFolder,JLabel progressLabel,boolean isFormatLabel)
			throws Exception
	{
		ZipFile zipFile=null;
		try{
			zipFile=new ZipFile(archiveFile);
			Enumeration<ZipEntry> enumeration=(Enumeration<ZipEntry>) zipFile.entries();
			int counter=0;
			while (enumeration.hasMoreElements()) {
				ZipEntry zipEntry=enumeration.nextElement();
				counter++;
				if (progressLabel!=null) {
					String text="Unpacking, in progress: "+counter+" / "+zipFile.size()+" ...";
					if (isFormatLabel) {
						GUIUtils.showInProgressMessage(progressLabel,text);
					}
					else {
						progressLabel.setText(text);
					}
				}
				if (zipEntry.isDirectory()) {
					continue;
				}
				File destFile = new File(destinationFolder,zipEntry.getName());
				File destinationParent = destFile.getParentFile();
				destinationParent.mkdirs();

				InputStream is = null;
				OutputStream os = null;
				try
				{
					is = zipFile.getInputStream(zipEntry);
					os = new BufferedOutputStream(new FileOutputStream(destFile));
					org.apache.commons.io.IOUtils.copy(is, os);
				}
				finally
				{
					IOUtils.closeQuietly(is);
					IOUtils.closeQuietly(os);
				}

			}
		}
		finally {
			if (zipFile!=null) {
				zipFile.close();
			}
		}
	}



	public static synchronized void createLogFile(File logFile) throws IOException
	{
		FileUtils.logFile=logFile;
		log(null,"Log file initialized\n",false);

		String hostname = "Unknown";

		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch(Exception e) {
			throw new Error(e);
		}

		println("Computer Name: " + hostname);
		println("IP Address: " + addr.getHostAddress());
		println("Username: " + System.getProperty("user.name"));
		println("Java Runtime Version: " + System.getProperty("java.runtime.version"));
		println("OS Name: " + System.getProperty("os.name"));
		println("OS Version: " + System.getProperty("os.version"));
		println("OS architecture: " + System.getProperty("os.arch"));
	}

	public static File getLogFolder()
	{
		return logFile.getParentFile();
	}

	public static File getLogFile()
	{
		return logFile;
	}

	public static void println(String message)
	{
		print(message+"\n");
	}

	public static void print(String message)
	{
		log(null,message,true);
	}

	public static void printStackTrace(Throwable tr)
	{
		log(tr,null,true);
	}

	private static void log(Throwable tr,String message,boolean append)
	{
		log(tr,message,append,true);
	}

	public static void printNoDate(String message)
	{
		logNoDate(null,message,true);
	}

	private static void logNoDate(Throwable tr,String message,boolean append)
	{
		log(tr,message,append,false);
	}

	private static void log(Throwable tr,String message,boolean append,boolean isDatePrinting)
	{
		log(tr, message, append, isDatePrinting, logFile);
	}

	public static void log(Throwable tr,String message,boolean append,boolean isDatePrinting, File outputFile)
	{
		PrintWriter printWriter = null;
		try {
			printWriter= new PrintWriter(new FileWriter(outputFile,append)); 
			if (printWriter!=null) {
				log(tr,message,append,isDatePrinting, printWriter);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (printWriter != null ) {
				printWriter.close();
			}
		}
	}

	public static void log(Throwable tr,String message,boolean append,boolean isDatePrinting, Set<PrintWriter> writers) {
		if (writers != null && !writers.isEmpty()) {
			for (PrintWriter writer : writers) {
				log(tr,message,append,isDatePrinting, writer);
			}
		}
	}

	public static void log(Throwable tr,String message,boolean append,boolean isDatePrinting, PrintWriter writer)
	{
		synchronized(writer)
		{
			try{
				String heading="";
				if (isDatePrinting) {
					String t=CoreConstants.DATE_FORMAT_PATTERN.STANDARD_TIMEZONE.getDateFormat().format(new Date());
					heading=t+": ";
				}
				writer.write(heading);

				if (tr!=null) {
					tr.printStackTrace(writer);
				}
				else {
					writer.write(message);
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}	

	public static void exportToXML(PrintWriter pw,Inventory inventory,List<String[]> rows,
			boolean hasAuditColumns) throws Exception
	{
		writeFileHeader(pw,inventory.getName(),inventory.getFieldsUsedForDataEntry(),hasAuditColumns);
		for (String[] row : rows ) {
			writeDataRow(pw,row);
		}
		writeDataFileFooter(pw);
	}
	
	public static void writeFileHeader(
			PrintWriter pw,
			String tableName,
			List<Field> inventoryFields,
			boolean hasAuditColumns)
	{
		writeFileHeader(pw,tableName,inventoryFields,hasAuditColumns,false);
	}

	public static void writeFileHeader(
			PrintWriter pw,
			String tableName,
			List<Field> inventoryFields,
			boolean hasAuditColumns,
			boolean isExportSnapshotInfo)
	{
		pw.println("<?xml version=\"1.0\" encoding=\"" + com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING + "\"?>");
		pw.println("<data name=\"" + tableName + "\" xmlns=\"http://data0000.configurator.erapidsuite.com\">");
		pw.println("<m/>");
		pw.println("<h>");
		for ( Field field : inventoryFields )
		{
			pw.println("<c>" + StringEscapeUtils.escapeXml10(field.getName()) + "</c>");
		}
		pw.println("<c>" + StringEscapeUtils.escapeXml10("RSC Data Label") + "</c>");
		pw.println("<c>" + StringEscapeUtils.escapeXml10("Navigation Filter") + "</c>");
		if (hasAuditColumns) {
			pw.println("<c>" + StringEscapeUtils.escapeXml10("RSC last updated by name") + "</c>");
			pw.println("<c>" + StringEscapeUtils.escapeXml10("RSC last update date") + "</c>");
		}
		if (isExportSnapshotInfo) {
			pw.println("<c>" + "RSC created by name"+ "</c>");
			pw.println("<c>" + "RSC creation date" + "</c>");
			pw.println("<c>" + "RSC OU ID" + "</c>");
			pw.println("<c>" + "RSC BG ID"  + "</c>");
			pw.println("<c>" + "RSC INVORG ID"  + "</c>");
			pw.println("<c>" + "RSC LEDGER ID" + "</c>");
			pw.println("<c>" + "RSC COA ID" + "</c>");
		}
		pw.println("</h>");
		pw.flush();
	}

	public static void writeDataRow(PrintWriter pw,String[] row)
	{
		pw.println("<r>");
		for ( String column : row )	{
			if ( column == null || column.trim().length() == 0 ){
				pw.println("<c/>");
			}
			else {
				pw.println("<c>" + StringEscapeUtils.escapeXml10(column) + "</c>");
			}
		}
		pw.println("<c/>");
		pw.println("<c/>");
		pw.println("<c/>");
		pw.println("<c/>");

		pw.println("</r>");
		pw.flush();
	}

	public static void writeDataRow(
			PrintWriter pw,
			List<Field> inventoryFields,
			String[] row,
			String label,
			boolean hasAuditColumns)
	{
		pw.println("<r>");
		int columnCounter=0;
		for ( String column : row )
		{
			columnCounter++;
			if (columnCounter==(inventoryFields.size()+1)) {
				// this is the first extra column: it should be the label but the data returns the navigation mapper
				pw.println("<c>" + StringEscapeUtils.escapeXml10(label) + "</c>");
				pw.println("<c/>");
			}
			else
				if (columnCounter>(inventoryFields.size()+1)) {
					// this is one audit column
					if (hasAuditColumns) {
						pw.println("<c>" + StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(column)) + "</c>");
					}
				}
				else
					if ( column == null || column.trim().length() == 0 ){
						pw.println("<c/>");
					}
					else{
						pw.println("<c>" + StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(column)) + "</c>");
					}
		}
		pw.println("</r>");
		pw.flush();
	}

	public static void writeDataFileFooter(PrintWriter pw) throws Exception
	{
		pw.println(DataFactory.DATA_FILE_FOOTER);
		pw.flush();
	}

	public static List<Field> getInventoryDataOnlyFields(Inventory inventory)
	{
		List<Field> tmpCols=inventory.getFields();
		List<Field> inventoryFields=new ArrayList<Field>();
		for ( int col=0; col<tmpCols.size();col++ )
		{
			Field field=tmpCols.get(col);

			FldFormFieldData fielData=field.getFldFormFieldData();
			if ( fielData!=null )
			{
				String type=fielData.getOracleFieldType();
				if (type!=null && type.equalsIgnoreCase("button")) {
					continue;
				}
			}
			else {
				HtmlFormFieldData htmlFielData=field.getHtmlFormFieldData();
				if ( htmlFielData!=null )
				{
					com.rapidesuite.core.inventory0007.RscColumnType.Enum type=htmlFielData.getRscColumnType();
					if ( type.equals(InventoryConstants.RSC_COLUMN_TYPE_CONTROL) ){
						continue;
					}
				}
			}
			inventoryFields.add(field);
		}
		return inventoryFields;
	}

	public static InputStream convertUTF8StreamToCharacterSet(
			InputStream sourceInputStream,
			String characterSet, String injectorNamePartition) throws Exception
	{
		String dateStr = SwiftGUIMain.getStartTime();
		File directory = new File(FileUtils.getTemporaryFolder(), injectorNamePartition + "-" + dateStr);
		directory.mkdirs();

		File tempFile = new File(directory,  characterSet + Utils.getUniqueFilenameSuffix() +".fld");
		File encryptedFile = new File(directory,  characterSet + Utils.getUniqueFilenameSuffix() +".fld");
		Charset charset = Charset.forName(characterSet);
		CharsetEncoder encoder = charset.newEncoder();
		CharsetDecoder decoder = charset.newDecoder();
		
		DataInputStream in = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		BufferedWriter bw = null;

		try {
			
			in = new DataInputStream(sourceInputStream);
			isr = new InputStreamReader(in,com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
			br = new BufferedReader(isr);
			fos = new FileOutputStream(tempFile, true);
			writer = new OutputStreamWriter(fos, charset);
			bw = new BufferedWriter(writer);
			
			FileUtils.println("Converting script to character set: '"+characterSet+"' ...");
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				try
				{
					ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(strLine));
					CharBuffer cbuf = decoder.decode(bbuf);
					bw.write(cbuf.toString());
					bw.write("\n");
				}
				catch(java.nio.charset.UnmappableCharacterException uce)
				{
					throw new Exception("Error converting text line from " + com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING + " to " + characterSet +
							".  Line = \"" + strLine + "\"", uce);
				}
			}
			FileUtils.println("Script converted.");
			
			bw.close();
			writer.close();
			fos.close();
			br.close();
			isr.close();
			in.close();
		}
		catch(UnsupportedCharsetException ue) {
			FileUtils.printStackTrace(ue);
			throw new Exception("Unsupported character set: '"+characterSet+"'");
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			throw e;
		}
		finally{
			IOUtils.closeQuietly(bw);
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(in);
		}
		return encryptDeleteUnencryptedFile(tempFile, encryptedFile, charset.toString());
	}

	public static File getTemporaryFolder()
	{
		if (!com.rapidesuite.client.common.util.Config.getTempFolder().exists()) {
			com.rapidesuite.client.common.util.Config.getTempFolder().mkdirs();
		}

		return com.rapidesuite.client.common.util.Config.getTempFolder();
	}

	public static void serialize(List<String[]> list,File outputFile) throws IOException
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(outputFile);
			out = new ObjectOutputStream(fos);
			out.writeObject(list);

		}
		finally{
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(fos);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String[]> unserialize(File inputFile) throws IOException, ClassNotFoundException
	{
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream(inputFile);
			in = new ObjectInputStream(fis);
			return (List<String[]>)in.readObject();
		}
		finally{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(fis);
		}
	}

	public static String convertPathToWindows(File file)
	{
		try{
			List<String> path=new ArrayList<String>();
			File tempFile=file;
			while (true) {
				File parentFile=tempFile.getParentFile();
				if (parentFile!=null) {
					String parentName=parentFile.getName();
					if (parentName.equals("")) {
						parentName=parentFile.getAbsolutePath();
					}
					if (parentName.indexOf(" ")!=-1) {
						parentName="\""+parentName+"\"";
					}
					path.add(parentName);
					tempFile=parentFile;
				}
				else {
					break;
				}
			}
			String fullPath="";
			for (int i=path.size()-1;i>=0;i--) {
				String parentName=path.get(i);
				if (parentName.indexOf("\\")==-1)
					fullPath=fullPath+parentName+UtilsConstants.FORWARD_SLASH;
				else fullPath=fullPath+parentName;
			}
			fullPath=fullPath+file.getName();
			fullPath=fullPath.replaceAll("\\\\",UtilsConstants.FORWARD_SLASH);

			return fullPath;
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return file.getAbsolutePath();
		}
	}

	public static void startSourceViewerBrowser(String editorExecutableCommand, File file) throws Exception {
		try {
			String cmd=editorExecutableCommand+" "+convertPathToWindows(file);
			FileUtils.println("Starting source viewer browser, cmd: '"+cmd+"'");
			Portability.startProcess(cmd);
		}
		catch (Exception err) {
			FileUtils.printStackTrace(err);
		}
	}

	public static void startTextEditor(String editorExecutableCommand,File file)
			throws Exception{
		try {
			String cmd=editorExecutableCommand+" "+convertPathToWindows(file);
			FileUtils.println("Starting text editor, cmd: '"+cmd+"'");
			Portability.startProcess(cmd);
		}
		catch (Exception err) {
			FileUtils.printStackTrace(err);
		}
	}

	public static File getFileInstance(File rootFolder,String fileName) throws Exception
	{
		List<File> res=getFileInstances(rootFolder,fileName);
		if (res.isEmpty()) {
			return null;
		}
		if (res.size()>1) {
			throw new Exception("Duplicate file detected: '"+fileName+"'");
		}
		return res.get(0);
	}

	public static List<File> getFileInstances(File rootFolder,String fileName)
	{
		List<File> res=new ArrayList<File>();
		if ( rootFolder.isFile() ){
			return res;
		}
		if (rootFolder.getName().indexOf(".svn")!=-1) {
			return res;
		}

		File [] children = rootFolder.listFiles();
		if (children==null) {
			return res;
		}
		for ( int i=0;i<children.length;i++ ){
			File child=children[i];
			if ( child.isFile() ) {
				if (child.getName().equalsIgnoreCase(fileName)){
					res.add(child);
				}
			}
			else {
				res.addAll(getFileInstances(child,fileName));
			}
		}
		return res;
	}


	public static final String LOG_FILE_EXTENSION = ".log";
	public static final String LOG_FILE_NAME_PREFIX = "system";

	public static void init(File rootFolder,CoreConstants.SHORT_APPLICATION_NAME shortApplicationName,boolean isDeleteFolder) throws Exception
	{
		File file=new File(rootFolder,shortApplicationName.toString());
		if (isDeleteFolder) {
			deleteDirectory(file);
		}
		file.mkdirs();
		File logFile=new File(file,shortApplicationName.toString() + "-" + SwiftGUIMain.getStartTime() + "-" + LOG_FILE_NAME_PREFIX + LOG_FILE_EXTENSION);
		FileUtils.createLogFile(logFile);
	}

	public static void deleteDirectory(File folder) {
	    try {
	    	CoreUtil.deleteDir(folder);
	    } 
	    catch (Exception e) {
	    	FileUtils.printStackTrace(e);
	    }
	}

	public static boolean hasChildTreeTableNode(ExecutionStatusTreeTableNode node,String childNodeName) {
		return getChildTreeTableNode(node,childNodeName)!=null;
	}

	public static ExecutionStatusTreeTableNode getChildTreeTableNode(ExecutionStatusTreeTableNode node,String childNodeName) {
		Enumeration<?> children=node.children();
		while (children.hasMoreElements()) {
			ExecutionStatusTreeTableNode child=(ExecutionStatusTreeTableNode) children.nextElement();
			if (child.getName().equalsIgnoreCase(childNodeName)) {
				return child;
			}
		}
		return null;
	}

	public static boolean hasChildInventoryTreeNode(InventoryTreeNode node,String childNodeName) {
		return getChildInventoryTreeNode(node,childNodeName)!=null;
	}

	@SuppressWarnings("unchecked")
	public
	static InventoryTreeNode getChildInventoryTreeNode(InventoryTreeNode node,String childNodeName) {
		List<InventoryTreeNode> children=Collections.list(node.children());
		for (InventoryTreeNode child:children) {
			if (child.getName().equalsIgnoreCase(childNodeName)) {
				return child;
			}
		}
		return null;
	}

	public static File getFile(File rootFolder,String itemPath) throws Exception	{
		File f=new File(rootFolder,itemPath);
		if (!f.exists()) {
			throw new Exception("file does not exist: "+f.getAbsolutePath());
		}
		return f;
	}

	public static Inventory getInventory(String name,InputStream inputStream)
			throws Exception
	{
		try
		{
			InventoryFactory inventoryFactory = new InventoryFactory();
			return inventoryFactory.parseInventory(inputStream, name,null, com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}

	public static Inventory getInventory(File file,String inventoryName)
			throws Exception
	{
		InputStream fileInputStream=null;
		try{
			fileInputStream=CoreUtil.getInputStreamFromXMLFile(file);
			return getInventory(inventoryName,fileInputStream);
		}
		finally {
			IOUtils.closeQuietly(fileInputStream);
		}
	}


	private static Inventory getInventoryFrom7ZIPFile(String entryName,File zipFile)
			throws Exception
	{
		InputStream fileInputStream=null;
		try{
			fileInputStream=SevenZipUtils.getStreamFrom7ZIPFile(entryName,zipFile);
			return getInventory(entryName,fileInputStream);
		}
		finally {
			IOUtils.closeQuietly(fileInputStream);
		}
	}

	public static Inventory getInventoryFromZIPFile(String entryName,File archiveFile)
			throws Exception
	{
		InputStream fileInputStream=null;
		ZipFile zipFile=null;
		try
		{
			zipFile=new ZipFile(archiveFile);
			fileInputStream=getStreamFromZIPFile(zipFile,entryName);
			return getInventory(entryName,fileInputStream);
		}
		finally
		{
			IOUtils.closeQuietly(fileInputStream);
			if (zipFile!=null) {
				zipFile.close();
			}
		}
	}

	public static boolean isControl(Inventory inventory)
	{
		// Discard inventory that is control (buttons only,...)
		if ( inventory instanceof FLDBlock ){
			FLDBlock b=(FLDBlock)inventory;
			if ( b.isControlTypeInventory() )	{
				return true;
			}
		}
		else
			if ( inventory instanceof HTMLInventory ){
				HTMLInventory b=(HTMLInventory)inventory;
				if ( b.isControlTypeInventory() ){
					return true;
				}
			}
		return false;
	}


	public static String getSubversionRevisionNumberFromXMLFile(File file)
	{
		String res="N/A";
		try{
			if (!file.exists()) {
				return res;
			}
			String content= org.apache.commons.io.FileUtils.readFileToString(file);
			return FileUtils.getRevisionNumberFromContent(content,UtilsConstants.SUBVERSION_REVISION_XML_KEYWORD);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return res;
		}
	}

	public static void createAndSaveRowsToXLSXFile(String sheetTitle,File outputFile,List<String[]> list) throws Exception
	{
		File templateFile=null;
		try{
			FileOutputStream os = null;
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet(sheetTitle);
			String sheetRef = sheet.getPackagePart().getPartName().getName();
			templateFile=new File(com.rapidesuite.client.common.util.Config.getTempFolder(),outputFile.getName());

			XSSFCellStyle cs = wb.createCellStyle();
			XSSFDataFormat fmt = wb.createDataFormat();
			cs.setDataFormat(fmt.getFormat("@"));
			try
			{
				os = new FileOutputStream(templateFile);
				wb.write(os);
			}
			finally
			{
				IOUtils.closeQuietly(os);
			}

			Writer fw = null;
			File tmp =new File(com.rapidesuite.client.common.util.Config.getTempFolder(),"sheet"+Math.random()+".xml");
			try{
				fw = new OutputStreamWriter(new FileOutputStream(tmp), com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING);
				generate(fw,list);
			}
			finally
			{
				IOUtils.closeQuietly(fw);
			}

			FileOutputStream out =null;
			try{
				out = new FileOutputStream(outputFile);
				substitute(templateFile, tmp, sheetRef.substring(1), out);
				tmp.delete();
			}
			finally
			{
				IOUtils.closeQuietly(out);
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			FileUtils.println("ERROR: "+e.getMessage()+" templateFile: '"+
			templateFile.getAbsolutePath()+"' outputFile: '"+outputFile.getAbsolutePath()+"' list: "+list.size());
			throw e;
		}
	}

	public static void generate(Writer out,List<String[]> list) throws Exception {
		SpreadsheetWriter sw = new SpreadsheetWriter(out);
		sw.beginSheet();

		for(int i =0; i< list.size();i++) {
			String[] gridRow=list.get(i);
			sw.insertRow(i);
			for(int j =0; j< gridRow.length;j++) {
				sw.createCell(j, gridRow[j]);
			}
			sw.endRow();
		}
		sw.endSheet();
	}

	/**
	 *
	 * @param zipfile the template file
	 * @param tmpfile the XML file with the sheet data
	 * @param entry the name of the sheet entry to substitute, e.g. xl/worksheets/sheet1.xml
	 * @param out the stream to write the result to
	 */
	public static void substitute(File zipfile, File tmpfile, String entry, OutputStream out) throws IOException {
		ZipFile zip = null;
		ZipOutputStream zos = null;
		try {
			zip = new ZipFile(zipfile);
			zos = new ZipOutputStream(out);
			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zip.entries();
			while (en.hasMoreElements()) {
				ZipEntry ze = en.nextElement();
				if(!ze.getName().equals(entry)){
					zos.putNextEntry(new ZipEntry(ze.getName()));
					try ( InputStream is = zip.getInputStream(ze); )
					{
						IOUtils.copy(is, zos);
					}
				}
			}
			zos.putNextEntry(new ZipEntry(entry));
			InputStream is = null;
			try
			{
				is = new FileInputStream(tmpfile);
				
				IOUtils.copy(is, zos);

				is.close();
			}
			finally {
				IOUtils.closeQuietly(is);
			}
			
			zip.close();
			zos.close();
		}
		finally {
			IOUtils.closeQuietly(zip);
			IOUtils.closeQuietly(zos);
		}
	}


	public static String getRscPLSQLPackageSourceVersion(File file)
	{
		try
		{
			if ( !file.exists() ) { throw new Exception("the file does not exist: '" + file.getAbsolutePath() + "'"); }
			String content = readContentsAsStringFromPLSQLFile(file);
			String keyword = ":='";
			int startIndex = content.indexOf(keyword);
			if ( startIndex == -1 ) { throw new Exception("Error: invalid PL/SQL file: '" + file.getAbsolutePath()
					+ "' cannot detect the version."); }
			String subContent = content.substring(startIndex + keyword.length());
			int endIndex = subContent.indexOf("'");
			String version = subContent.substring(0, endIndex);
			version = replaceSVNRevisionKeywords(version);

			return version;
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			throw new Error("Unsupported Oracle EBS version: ", e);
		}
	}

	public static void safeLogEnvironmentProperties(Map<String,String> environmentProperties)
	{
		SortedMap<String,String> safeEnvironmentProperties = CoreUtil.excludePasswordPropertiesForLogging(environmentProperties);
		for(Map.Entry<String, String> entry : safeEnvironmentProperties.entrySet()) {
			println("Environment Properties: " + entry.getKey() + "=" + entry.getValue());
		}
	}
	public static InputStream encryptDeleteUnencryptedFile(File unencryptedFile, File encryptedFile) throws Exception
	{
		return encryptDeleteUnencryptedFile(unencryptedFile, encryptedFile, CoreConstants.CHARACTER_SET_ENCODING);
	}

	public static InputStream encryptDeleteUnencryptedFile(File unencryptedFile, File encryptedFile, String sourceEncoding) throws Exception
	{
		try (
				FileInputStream fis = new FileInputStream(unencryptedFile);
				InputStreamReader reader = new InputStreamReader(fis, sourceEncoding);
				BufferedReader buff = new BufferedReader(reader);

				FileOutputStream fos = new FileOutputStream(encryptedFile);
				CipherOutputStream cos = new CipherOutputStream(fos,Encryption.getEncryptedCipherInstance());
				OutputStreamWriter writer = new OutputStreamWriter(cos, sourceEncoding);
				BufferedWriter bw = new BufferedWriter(writer);
				)
				{
			String line = null;
			while ( (line = buff.readLine()) != null )
			{
				bw.write(line);
				bw.write("\n");
			}
			bw.flush();
			IOUtils.closeQuietly(buff);
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(fis);

			if(!unencryptedFile.delete())
			{
				throw new Exception("Unable to delete file: " + unencryptedFile.getName());
			}
			return new CipherInputStream(new FileInputStream(encryptedFile), Encryption.getDecryptedCipherInstance());
				}
	}

	public static void logConfigPropertiesFileContent(String openingLine, String closingLine, SortedMap<String,String> properties) {
		if (getLogFile() == null || !getLogFile().isFile()) {
			return;
		}
		final Set<Map.Entry<String, String>> entries = properties.entrySet();
		StringBuffer contentBfr = new StringBuffer(openingLine).append(System.lineSeparator());
		for (Map.Entry<String, String> entry : entries) {
			contentBfr.append(entry.getKey()).append(" : ").append(entry.getValue()).append(System.lineSeparator());
		}
		contentBfr.append(closingLine);
		println(contentBfr.toString());
	}

	public static Map<String, String> loadBwe(File bweFile) throws Exception 
	{
		Map<String, String> rawProperties = CoreUtil.readPropertiesFromEnvironmentFile(bweFile);
		Map<String, String> toReturn = new HashMap<String, String>();
		for ( final Entry<String, String> entry : rawProperties.entrySet() )
		{
			String key = entry.getKey();
			String value = entry.getValue();

			if ("SSH_HOST".equals(key)) {
				key = SwiftBuildConstants.FLD_SCRIPTS_HOSTNAME_KEY;
			} else if ("TRANSFER_PROTOCOL".equals(key)) {
				key = SwiftBuildConstants.FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY;
				if ("SSH".equalsIgnoreCase(value)) {
					value = SwiftBuildConstants.TRANSFER_PROTOCOL.SFTP.toString();
				}
			} else if ("SSH_LOGIN".equals(key)) {
				key = SwiftBuildConstants.FLD_SCRIPTS_HOST_USER_NAME_KEY;
			} else if ("SSH_PASSWORD".equals(key)) {
				key = SwiftBuildConstants.FLD_SCRIPTS_HOST_PASSWORD_KEY;
			} else if ("SSH_PRIVATE_KEY_FILE_NAME".equals(key)) {
				key = SwiftBuildConstants.FLD_SCRIPTS_SSH_PRIVATE_KEY_LOCATION_KEY;
			} else if (EnvironmentPropertyConstants.OLD_FLD_SCRIPTS_FOLDER_KEY.equals(key)) {
				key = SwiftBuildConstants.FLD_SCRIPTS_FOLDER_KEY;
			} else if ("REMOTE_FLD_LOGS_FOLDER".equals(key)) {
				key = SwiftBuildConstants.FLD_SCRIPTS_LOG_FOLDER_KEY;
			} else if ("Full URL for FLD files".equals(key)) {
				key = SwiftBuildConstants.FLD_SCRIPTS_URL_KEY;
			} else if (EnvironmentPropertyConstants.OLD_PROPERTY_DB_HOST_NAME.equals(key)) {
				key = EnvironmentPropertyConstants.DATABASE_HOST_NAME_KEY;
			} else if ("Database Port Number".equals(key)) {
				key = EnvironmentPropertyConstants.DATABASE_PORT_NUMBER_KEY;
			} else if ("Database SID".equals(key)) {
				key = EnvironmentPropertyConstants.DATABASE_SID_KEY;
			} else if ("Database User".equals(key)) {
				key = EnvironmentPropertyConstants.DATABASE_USER_NAME_KEY;
			} else if ("Database Password".equals(key)) {
				key = EnvironmentPropertyConstants.DATABASE_PASSWORD_KEY;
			}

			toReturn.put(key, value);
		}
		return toReturn;
	}    
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }
    
	public static String getContentsOfProcCpuInfoIfPresent() throws Exception
	{
		StringBuffer toReturn=new StringBuffer("");
		
		if (isWindows()) {
			toReturn.append("#PROCESSOR_IDENTIFIER=").append(System.getenv("PROCESSOR_IDENTIFIER"));
			toReturn.append("#PROCESSOR_ARCHITECTURE=").append(System.getenv("PROCESSOR_ARCHITECTURE"));
			String val=System.getenv("PROCESSOR_ARCHITEW6432");
			if (val==null) {
				val="";
			}
			toReturn.append("#NUMBER_OF_PROCESSORS=").append(System.getenv("NUMBER_OF_PROCESSORS"));
			toReturn.append("#PROCESSOR_ARCHITEW6432=").append(val);
		}
		else
		if (isUnix()) {
			toReturn.append(LicenseEncryptor.getContentsOfProcCpuInfoIfPresent());
		}
		else throw new Exception("Invalid OS - Windows and Linux are only supported. Detected: '"+OS+"'");
		
		return toReturn.toString();
	}

	public static void validateLicenseOnLicensedMachine(LicenseDocument licenseDocument)
			throws Exception
	{
		try
		{
			StringBuffer toReturn=new StringBuffer("");
			// if the "validate_hardware" parameter is set to false, DO NOT VALIDATE
			if(StringUtils.isNotBlank(licenseDocument.getLicense().getValidateHardware()) && !licenseDocument.getLicense().getValidateHardware().equalsIgnoreCase("false")) {
				// default is true, so if the string is NULL, EMPTY or WHITESPACE ONLY this code will get executed. 
				String licensedProcCpuInfo = licenseDocument.getLicense().getProcCpuinfoContents();
				if ( licensedProcCpuInfo != null )
				{
					String currentProcCpuInfo = getContentsOfProcCpuInfoIfPresent();
					if (currentProcCpuInfo==null) {
						toReturn.append("ERROR:  Current machine lacks CPU identification, but license file requires it.\n");
					}
	
					if ( !currentProcCpuInfo.equals(licensedProcCpuInfo) )
					{
						FileUtils.println("L: '" + licensedProcCpuInfo + "'");
						FileUtils.println("C: '" + currentProcCpuInfo + "'");
						FileUtils.println("D: '" + StringUtils.difference(licensedProcCpuInfo, currentProcCpuInfo) + "'");
						FileUtils.println("I: '" + StringUtils.indexOfDifference(licensedProcCpuInfo, currentProcCpuInfo) + "'");
						toReturn.append("ERROR:  Current machine's CPU information does NOT match licensed CPU information.\n");
					}
				}
			}

			// Disable licensed hostname and port validation.
	        // 0008101: Disable Hardware validation for RAPIDConfigurator license
//			List<String> licensedHostNames = new ArrayList<String>(Arrays.asList(licenseDocument.getLicense().getHostnameArray()));
//			InetAddress addr = InetAddress.getLocalHost();
//			String hostName = addr.getHostName();
//			if (hostName==null) {
//				toReturn.append("ERROR:  Cannot retrieve the hostName from the machine.\n");
//			}
//			if ( !licensedHostNames.contains(hostName) )
//			{
//				toReturn.append("ERROR: this server is being accessed from an unlicensed host name.\n"+
//						" Licensed host names = " + licensedHostNames +", but you accessed it via host name = " + hostName+"\n");
//			}

			if (!toReturn.toString().isEmpty()) {
				throw new Exception(toReturn.toString());
			}
		}
		catch(Throwable t)
		{
			String msg="Error while validating license :: " + CoreUtil.getAllThrowableMessages(t);
			FileUtils.println(msg);
			throw new Exception(msg);
		}
		finally{
			FileUtils.println("License validation information: \n" + 
					"\n\n=============CURRENT LICENSE==================\n\n" +
					licenseDocument.toString() +
					"\n\n=============NEW LICENSE TEMPLATE===============\n\n" +
					generateTemporaryLicense().toString() +
					"\n\n=============================\n\n");
		}
	}

	public static LicenseDocument generateTemporaryLicense()
	{
		try
		{
			LicenseDocument licenseDocument = LicenseDocument.Factory.newInstance();
			License license = licenseDocument.addNewLicense();
			InetAddress ip=InetAddress.getLocalHost();
			String hostname = ip.getHostName();
			license.addHostname(hostname);
			license.setCompanyName("PLEASE SPECIFY COMPANY NAME");
			license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.build.toString().toUpperCase());
			license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.reverse.toString().toUpperCase());
			license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString().toUpperCase());
			license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.inject.toString().toUpperCase());
			license.addProduct(CoreConstants.SHORT_APPLICATION_NAME.extract.toString().toUpperCase());
			license.setExpirationDate(LicenseEncryptor.formatExpirationDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)));
			license.setValidateHardware("true");

			String currentProcCpuInfo = getContentsOfProcCpuInfoIfPresent();
			if ( null != currentProcCpuInfo )
			{
				license.setProcCpuinfoContents(currentProcCpuInfo);
			}

			return licenseDocument;
		}
		catch(Throwable t)
		{
			FileUtils.printStackTrace(t);
			return null;
		}
	}


	public static void createLicenseFile(File file,String content) throws Exception
	{
		PrintStream ps = null;
		try
		{
			ps = new PrintStream(new FileOutputStream (file, false));
			ps.println(content);
		}
		finally
		{
			IOUtils.closeQuietly(ps);
		}
	}
	
	public static File getUserHomeFolder()
	{
		File file=new File(System.getProperty("user.home"));
		file.mkdirs();
		return file;
	}
	public static void zipFolder(File srcFolder,File destZipFile, boolean isEncrypted) throws Exception {
		if (!srcFolder.exists() || !hasAtLeastOneFile(srcFolder)) {
			return;
		}
		if(isEncrypted){
			ZipOutputStream zipOutputStream = null;
			try{
				FileOutputStream fileOutputStream = new FileOutputStream(destZipFile);
				zipOutputStream = initOutputStream(fileOutputStream);
				addFolderToZip("", srcFolder.getAbsolutePath(), zipOutputStream);
				zipOutputStream.flush();
			}
			finally{
				IOUtils.closeQuietly(zipOutputStream);
			}
		}else{
			zipFolder(srcFolder,destZipFile);
		}

	}
	 public static ZipOutputStream initOutputStream(FileOutputStream zipFileTemp) throws Exception {
		 
		 final BufferedOutputStream fileOutputStream = new BufferedOutputStream(zipFileTemp);
		 ZipOutputStream zipOutputStream = null;
		 final CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, Encryption.getEncryptedCipherInstance());
		 zipOutputStream = new ZipOutputStream(cipherOutputStream);
		 zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
		 return zipOutputStream;
	
	}

}