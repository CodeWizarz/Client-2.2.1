/**************************************************
 * $Revision: 61077 $:
 * $Author: hassan.jamil $:
 * $Date: 2017-02-20 18:37:51 +0700 (Mon, 20 Feb 2017) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/util/Utils.java $:
 * $Id: Utils.java 61077 2017-02-20 11:37:51Z hassan.jamil $:
 */

package com.rapidesuite.client.common.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.utils.JavaVersionRetriever;
import com.rapidesuite.client.common.Job;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;
import com.rapidesuite.client.common.gui.InventoryTreeNode;
import com.rapidesuite.client.common.gui.TextTreeNode;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.Encryption;
import com.rapidesuite.reverse.DataExtractionConstants;
import com.rapidesuite.reverse.utils.DataExtractionFileUtils;

public class Utils
{

	//this method can NOT have false negative, but can have false positive
	//thus, whenever in doubt, return true
	public static boolean isValidBrowserAndJvmConfiguration(final String userAgent, final String appletSentJavaVersion) {
		Assert.notNull(userAgent);
		Assert.notNull(appletSentJavaVersion);
		//IE 11 no longer has "MSIE" in its user agent
		//According to http://stackoverflow.com/questions/17907445/how-to-detect-ie11, the way to detect IE 11 is checking for "Trident"
		//However, Trident is IE's web browser engine name, which means that there is likelihood that there is other browser (or browser enabled application) which can fool this validation
		if (!userAgent.contains("MSIE") && !userAgent.contains("Firefox") && !userAgent.contains("Trident")) {
			throw new RuntimeException("only IE and Firefox are supported. Your user agent is "+userAgent);
		}
		final boolean jvmIs64Bit = is64BitJvm(appletSentJavaVersion);
		if (userAgent.contains("Firefox") && jvmIs64Bit) {
			if (userAgent.contains("x86_64") && !userAgent.contains("i686")) {
				return true;
			} else {
				return false;
			}
		} else if (userAgent.contains("MSIE") && jvmIs64Bit && !userAgent.contains("x64")) {
			return false;
		}
		return true;
	}


	public static boolean is64BitJvm(final String appletSentJavaVersion) {
		final String osarch = JavaVersionRetriever.getOsArchFromJvmInformation(appletSentJavaVersion);
		if (osarch.equalsIgnoreCase("amd64")) {
			return true;
		} else if (osarch.equalsIgnoreCase("x64")) {
			return true;
		} else if (osarch.equalsIgnoreCase("x86_64")) {
			return true;
		} else {
			return false;
		}
	}

	public static final void stopWindowsFromBeeping()
	{
		try{
			Portability.startProcess("net stop beep");
		}
		catch(Throwable t){
			FileUtils.printStackTrace(t);
		}
	}

	public static int getRandomNumber()
	{
		return getRandomNumber(10000000);
	}

	public static String getUniqueFilenameSuffix() {
	    String uuid = UUID.randomUUID().toString();
	    uuid = uuid.replaceAll("-", "");
	    BigInteger bi = new BigInteger(uuid, 16);
	    uuid = bi.toString(Character.MAX_RADIX);
	    
	    // replace + (in the GMT+07:00) with '' (so that it becomes GMT07:00 - GMT-07:00 remains the same) because R11 doesn't support + sign
	    String date = CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE_SHORT.getDateFormat().format(new Date()).replace("+", "");

		return "-" + date + "-u" + uuid;
	}

	public static String getUniqueFilename(String prefix, String fileExtension)
	{
        String toReturn = prefix + Utils.getUniqueFilenameSuffix() + fileExtension;
        return toReturn;
	}


	public static int getRandomNumber(long maxValue)
	{
		return (int)(Math.random()*maxValue);
	}

	public static void displayMemoryConsumption(String message)
	{
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		FileUtils.println(message+" # Total free memory: '"+
				(freeMemory + (maxMemory - allocatedMemory))/(1024*1024)+"' MB");
	}

	public static Map<String,String> getMap(List<String> list)
	{
		Map<String,String> toReturn=new HashMap<String,String>();
		for (String item:list){
			toReturn.put(item,item);
		}
		return toReturn;
	}

	public static Set<String> getSet(List<String> list)
	{
		Set<String> toReturn=new HashSet<String>();
		for (String item:list){
			toReturn.add(item);
		}
		return toReturn;
	}

	public static boolean hasSpecialCharacters(String name)
	{
		if ( name.indexOf("\"")!= -1 ) return true;
		if ( name.indexOf("*")!= -1 ) return true;
		if ( name.indexOf(UtilsConstants.FORWARD_SLASH)!= -1 ) return true;
		if ( name.indexOf(":")!= -1 ) return true;
		if ( name.indexOf("<")!= -1 ) return true;
		if ( name.indexOf(">")!= -1 ) return true;
		if ( name.indexOf("?")!= -1 ) return true;
		if ( name.indexOf("\\")!= -1 ) return true;
		if ( name.indexOf("|")!= -1 ) return true;
		if ( name.indexOf("'")!= -1 ) return true;

		return false;
	}

	public static String getExecutionTime(long startTimeMS,long endTimeMS) {
		return getExecutionTime(endTimeMS - startTimeMS);
	}

	public static String getExecutionTime(long diff) {
		long hours = diff / (60 * 60 * 1000);
		long minutes = (diff - (hours * (60 * 60 * 1000) )) / (60 * 1000);
	    long seconds = (diff - (hours * (60 * 60 * 1000)) - (minutes * (60 * 1000) ) )/ 1000;

		String msg=seconds+" s.";
		if (minutes!=0) {
			msg=minutes+" mins, "+msg;
		}
		if (hours!=0) {
			msg=hours+" hrs, "+msg;
		}
		if (msg.startsWith("-")) {
			return "";
		}
		return  msg;
	}

	public static String stackTostring(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		}
		catch(Exception e2) {
			FileUtils.printStackTrace(e2);
			return "";
		}
	}

	public static Date getDate(String date) {
		if (date==null || date.isEmpty()) {
			return null;
		}
		Date res=null;
		DateFormat df =getDateFormat(date);
		try {
			res= df.parse(date);
		} catch (ParseException e) {
			FileUtils.printStackTrace(e);
		}
		return res;
	}

	public static String getFormattedDate(Date date)
	{
		DateFormat df=new SimpleDateFormat(UtilsConstants.JAVA_DATE_TIME_FORMAT);
		return df.format(date).toUpperCase();
	}

	private static DateFormat getDateFormat(String date) {
		DateFormat df =null;
		int indexOf=date.indexOf(":");
		if (indexOf==-1){
			df = new SimpleDateFormat(UtilsConstants.JAVA_DATE_FORMAT);
		}
		else {
			df = new SimpleDateFormat(UtilsConstants.JAVA_DATE_TIME_FORMAT);
		}
		return df;
	}

	public static String formatNumberWithComma(int number)
    throws Exception
    {
    	DecimalFormat myFormatter = new DecimalFormat("###,###,###,###");
        return myFormatter.format(number);
    }


	/**
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public static String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }
    //"com.rapidesuite.reverse.ReverseMain"
    public static void restartJavaProgram(String mainClassName) {
    	try{
    		StringBuilder cmd = new StringBuilder();
    		cmd.append("jre" + UtilsConstants.FORWARD_SLASH + "bin" + UtilsConstants.FORWARD_SLASH + "java ");
    		for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
    			cmd.append(jvmArg + " ");
    		}
    		cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
    		cmd.append(mainClassName).append(" ");
    		Portability.startProcess(cmd.toString());
    		System.exit(0);
    	}
    	catch(Exception e) {
    		GUIUtils.popupErrorMessage("Unable to restart the program, please restart it manually.");
    	}
    }

    public static final String formatNumber(int number,String pattern){
    	final DecimalFormat df = new DecimalFormat(pattern);
		return df.format(number);
	}

    public static final int getIntValue(Properties properties,String propertyName)
	{
    	String propertyValue=properties.getProperty(propertyName);
		return Integer.valueOf(propertyValue).intValue();
	}


	public static File getPrefixOutputFolder(String operatingUnitName,Long operatingUnitId) {
		boolean isInvalidName=false;
		if (operatingUnitName!=null) {
			isInvalidName=Utils.hasSpecialCharacters(operatingUnitName);
		}
		else {
			isInvalidName=true;
		}
		String operatingUnitOutputFolderName=operatingUnitName;
		if (isInvalidName) {
			operatingUnitOutputFolderName="Operating_unit_id_"+operatingUnitId;
		}
		return new File(operatingUnitOutputFolderName);
	}

	public static void displayMemory() {
		Runtime runtime = Runtime.getRuntime();
		int mb = 1024*1024;
		System.out.println("Used Memory:"+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
		System.out.println("Free Memory:"+ runtime.freeMemory() / mb);
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
	}

	public static Map<Integer,Job> convertListToMap(List<Job> list)
	{
		Map<Integer, Job> map=new HashMap<Integer, Job>();
		for (Job item:list){
			map.put(item.getJobId(),item);
		}
		return map;
	}

	public static Map<String,String> convertListOfStringsToMap(List<String> list)
	{
		Map<String,String> map=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		for (String item:list){
			map.put(item,item);
		}
		return map;
	}


	public static InventoryTreeNode buildInventoryTree(InventoryTreeNode  rootNode,List<TreePath> treePaths) {
		for (TreePath treePath:treePaths) {
			Object[] pathObjectArray=treePath.getPath();
			InventoryTreeNode[] pathInventoryTreeNodeArray=Arrays.asList(pathObjectArray).toArray(new InventoryTreeNode[pathObjectArray.length]);

			InventoryTreeNode currentNode=rootNode;
			for (InventoryTreeNode treeNode:pathInventoryTreeNodeArray) {
				String nodeName=treeNode.getName();

				boolean hasChildTreeTableNode=FileUtils.hasChildInventoryTreeNode(currentNode,nodeName);
				if (hasChildTreeTableNode) {
					currentNode=FileUtils.getChildInventoryTreeNode(currentNode,nodeName);
				}
				else {
					currentNode.add(treeNode);
					currentNode=treeNode;
				}
			}
		}
		return rootNode;
	}

	public static void convertInventoryTreeNodesToTreeTableNode(
			ExecutionStatusTreeTableNode rootTreeTableNode,
			InventoryTreeNode inventoryTreeNode,
			int taskId)
	throws Exception	{
		Object[] pathObjectArray=inventoryTreeNode.getUserObjectPath();
		String[] pathStringArray=Arrays.asList(pathObjectArray).toArray(new String[pathObjectArray.length]);

		ExecutionStatusTreeTableNode currentNode=rootTreeTableNode;
		// remove the first node as this is a hidden root
		StringBuffer nodePath=new StringBuffer();
		for (int i=1;i<pathStringArray.length;i++) {
			String nodeName=pathStringArray[i];
			boolean hasChildTreeTableNode=FileUtils.hasChildTreeTableNode(currentNode,nodeName);
			if (hasChildTreeTableNode) {
				currentNode=FileUtils.getChildTreeTableNode(currentNode,nodeName);
			}
			else {
				ExecutionStatusTreeTableNode treeNode=null;
				nodePath.append(nodeName+"###");
				if ( i==(pathStringArray.length-1)) {
					treeNode=rootTreeTableNode.createInstance(nodeName,taskId,nodePath.toString());
				}
				else {
					treeNode=rootTreeTableNode.createInstance(nodeName,-1,nodePath.toString());
				}
				currentNode.add(treeNode);
				currentNode=treeNode;
			}
		}
	}

	public static TextTreeNode buildTree(TextTreeNode rootNode,List<TreePath> treePaths) {
		for (TreePath treePath:treePaths) {
			Object[] pathObjectArray=treePath.getPath();
			TextTreeNode[] pathTextTreeNodeArray=Arrays.asList(pathObjectArray).toArray(new TextTreeNode[pathObjectArray.length]);

			TextTreeNode currentNode=rootNode;
			for (TextTreeNode treeNode:pathTextTreeNodeArray) {
				currentNode.add(treeNode);
				currentNode=treeNode;
			}
		}
		return rootNode;
	}

	public static int getControlRevision(String type, String programName) throws Exception {
		List<String> lines=DataExtractionFileUtils.readContentsFromBuildInfoFile(new File(DataExtractionConstants.BUILD_INFO_FILE_FOLDER,
				type+"." + programName.toLowerCase() + DataExtractionConstants.BUILD_INFO_FILE_SUFFIX));
		for (String line:lines) {
			int startIndex=line.indexOf("EBS#");
			int endIndex=line.indexOf("#EBS");
			String ebsVersion=line.substring(startIndex+4,endIndex);
			if (ebsVersion.equalsIgnoreCase(type)) {
				startIndex=line.indexOf("REVISION#");
				endIndex=line.indexOf("#REVISION");
				return Integer.valueOf(line.substring(startIndex+9,endIndex)).intValue();
			}
		}
		throw new Exception("Invalid build file");
	}

	//to convert text like "# ITERATION NUMBER 3 of the generation" into number 3
	public static int retrieveIterationNumberFromIterationText(String text) {
		text = text.trim();
		Assert.isTrue(text.startsWith(CoreConstants.ITERATION_SEPARATOR));
		text = text.substring(CoreConstants.ITERATION_SEPARATOR.length());
		text = text.trim();
		Assert.isTrue(text.matches("^\\d+[^\\d].*$"), "text must start with number");
		text = text.replaceAll("[^\\d].*$", "");
		Assert.isTrue(text.matches("^\\d+$"), "text must by now be a string of number");
		final int output = Integer.parseInt(text);
		return output;
	}


	private static Object sleepingBeauty = new Object();

	public static void awakenAllSleepers()
	{
	    synchronized(sleepingBeauty)
	    {
	        sleepingBeauty.notifyAll();
	    }
	}

	public static void sleep(long time) throws InterruptedException
	{
        synchronized(sleepingBeauty)
        {
            try
            {
                sleepingBeauty.wait(time);
            }
            catch(InterruptedException e)
            {
                FileUtils.print("Sleeping Beauty Interrupted Thread = " + Thread.currentThread().getId());
                throw e;
            }
        }
	}

	public static boolean matchesDefaultDatabaseUsername(final String databaseUsername) {
		return UtilsConstants.DEFAULT_DATABASE_USER_NAME.equalsIgnoreCase(databaseUsername);
	}

	public static Date getInternalStaffPermissionExpiry() {
		final File permissionFile = Config.getInternalStaffPermissionFilePath();
		if (permissionFile == null || !permissionFile.isFile()) {
			return null;
		} else {
			try {
				final String content = new String(Encryption.decrypt(org.apache.commons.io.FileUtils.readFileToByteArray(permissionFile), InternalStaffPermissionGenerator.INTERNAL_STAFF_PERMISSION_KEY), CoreConstants.CHARACTER_SET_ENCODING);
				if (content.startsWith(InternalStaffPermissionGenerator.INTERNAL_STAFF_PERMISSION_PREFIX)) {
					Date expirationTimestamp = InternalStaffPermissionGenerator.getDateFormat().parse(content.substring(InternalStaffPermissionGenerator.INTERNAL_STAFF_PERMISSION_PREFIX.length()));
					return expirationTimestamp;
				} else {
					return null;
				}
			} catch (Exception e) {
				return null;
			}
		}
	}

	public static boolean hasAccessToInternalStaffsOnlyFeatures() {
		final Date expirationTimestamp = getInternalStaffPermissionExpiry();
		return expirationTimestamp != null && expirationTimestamp.getTime() >= System.currentTimeMillis();
	}


	public static Component getTableCellRendererComponent(JToggleButton toggleButton, JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column, boolean borderPainted) {
		toggleButton.setHorizontalAlignment(SwingConstants.CENTER);

		toggleButton.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		if ( value instanceof String )
		{
			toggleButton.setSelected(false);
		}
		else
		{
			if ( isSelected )
			{
				toggleButton.setForeground(table.getSelectionForeground());
				toggleButton.setBackground(table.getSelectionBackground());
			}
			else
			{
				toggleButton.setForeground(table.getForeground());
				toggleButton.setBackground(table.getBackground());
			}
			toggleButton.setSelected((value != null && ((Boolean) value).booleanValue()));
		}
		toggleButton.setBorderPainted(borderPainted);
		return toggleButton;
	}

	public static List<String> splitAndTrimCommaSeparatedString(final String commaSeparatedElements) {
    	final String arr[] = StringUtils.split(commaSeparatedElements, ',');
    	final List<String> output = new ArrayList<String>();
    	for (final String s : arr) {
    		output.add(s.trim());
    	}
    	return output;  		
	}
	
	public static synchronized JFileChooser initializeJFileChooserWithTheLastPath(final String key) {
		final File propertiesFile = new File("file_chooser_default_path.properties");
		
		final JFileChooser jFileChooser = new JFileChooser();
		
		if (propertiesFile.exists()) {
			boolean removeKey = false;
			Properties properties = new Properties();
			try (InputStream input = new FileInputStream(propertiesFile)) {
				properties.load(input);
				String path = properties.getProperty(key);
				if (path != null) {
					File f = new File(path);
					f = f.getAbsoluteFile();
					if (f.isFile()) {
						jFileChooser.setSelectedFile(f);
					} else if (f.isDirectory()) {
						jFileChooser.setCurrentDirectory(f);
					} else if (f.getParentFile() != null && f.getParentFile().isDirectory()) {
						jFileChooser.setCurrentDirectory(f.getParentFile());
						removeKey = true;
					} else {
						removeKey = true;
					}
				}				
			} catch (Throwable t) {
				FileUtils.printStackTrace(t);
				return jFileChooser;
			}
			if (removeKey) {
				try (OutputStream output = new FileOutputStream(propertiesFile)) {
					properties.remove(key);
					properties.store(output, null);
				} catch (Throwable t) {
					FileUtils.printStackTrace(t);
					return jFileChooser;
				}					
			}
		}
		
		jFileChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
					Properties properties = new Properties();
					if (propertiesFile.exists()) {
						try (InputStream input = new FileInputStream(propertiesFile)) {
							properties.load(input);
						} catch (Throwable t) {
							FileUtils.printStackTrace(t);
							return;
						}
					}

					try (OutputStream output = new FileOutputStream(propertiesFile)) {
						properties.setProperty(key, jFileChooser.getSelectedFile().getAbsolutePath());
						properties.store(output, null);
					} catch (Throwable t) {
						FileUtils.printStackTrace(t);
						return;
					}
				}
			}
			
		});

		return jFileChooser;
	}	
}