/**************************************************
 * $Revision: 56113 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2016-06-21 18:12:00 +0700 (Tue, 21 Jun 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/utils/DataExtractionFileUtils.java $:
 * $Id: DataExtractionFileUtils.java 56113 2016-06-21 11:12:00Z jannarong.wadthong $:
 */

package com.rapidesuite.reverse.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import com.rapidesuite.client.common.Job;
import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;
import com.rapidesuite.client.common.gui.InventoryTreeNode;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.SevenZipUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.DataFactory;
import com.rapidesuite.configurator.GeneralConstants;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.reverse.DataExtractionJob;
import com.rapidesuite.reverse.DataExtractionJobManager;
import com.rapidesuite.reverse.gui.DataExtractionInventoryTreeNode;
import com.rapidesuite.reverse.gui.DataExtractionPanel;
import com.rapidesuite.reverse.gui.DataExtractionStatusTreeTableNode;
import com.rapidesuite.reverse.inventory_tree.Directory;

public class DataExtractionFileUtils
{

	public static void logQueryToExecute(Job worker,String sqlQuery)
	{
		if ( Config.getReversePrintSqlToLog() ){
			FileUtils.println("Job Id: "+worker.getJobId()+" . Executing the SQL:\n'"+sqlQuery+"'");
		}
	}

	public static File getSQLFile(File sqlFile) throws Exception {
		return FileUtils.getFileFromSQLName(sqlFile.getParentFile(), sqlFile.getName());
	}

	public static synchronized void logReverseError(Exception e,int taskId,String inventoryFileName,File sqlFile){
		FileUtils.println("ERROR: ");
    	FileUtils.println("		Task id: "+taskId);
    	FileUtils.println("		Inventory file name: "+inventoryFileName);
    	if (sqlFile!=null) {
    		FileUtils.println("		SQL file name: "+sqlFile.getAbsolutePath());
        	FileUtils.println("		SQL revision number: "+
        			FileUtils.getSubversionRevisionNumberFromSQLFile(
        			sqlFile)
				);
    	}
        FileUtils.printStackTrace(e);
	}

	public static boolean isBusinessGroupLevelQuery(String sqlQuery) throws Exception{
		return isTypeOfQuery(sqlQuery,"##BUSINESS_GROUP_NAME##") || isTypeOfQuery(sqlQuery,"##BUSINESS_GROUP_ID##");
	}

	public static boolean isOperatingUnitLevelQuery(String sqlQuery) throws Exception{
		return isTypeOfQuery(sqlQuery,"##OPERATING_UNIT_NAME##") || isTypeOfQuery(sqlQuery,"##OPERATING_UNIT_ID##")
			||	isTypeOfQuery(sqlQuery,"##BUSINESS_UNIT_NAME##") || isTypeOfQuery(sqlQuery,"##BUSINESS_UNIT_ID##")
				;
	}

	public static boolean isInstanceLevelQuery(String sqlQuery) throws Exception{
		return !isBusinessGroupLevelQuery(sqlQuery) && !isOperatingUnitLevelQuery(sqlQuery);
	}

	public static boolean isTypeOfQuery(String sqlQuery,String typeOf) throws Exception{
		if (sqlQuery==null) {
			throw new Exception("Invalid query");
		}
		return sqlQuery.toLowerCase().indexOf(typeOf.toLowerCase())!=-1;
	}

	public static File getFinalXMLDataFile(DataExtractionJob dataExtractionJob,int currentDataFileIndex)
	{
		File folder=new File(Config.getReverseOutputFolder(),dataExtractionJob.getPrefixOutputFolder().getName());
		if (currentDataFileIndex==0) {
			return new File(folder,dataExtractionJob.getInventory().getName()+".xml");
		}
		return new File(folder,dataExtractionJob.getInventory().getName()+"."+currentDataFileIndex+".xml");
	}

	private static PrintWriter getPrintWriter(DataExtractionJob dataExtractionJob,boolean append,int currentDataFileIndex)
	throws Exception
	{
		File file = DataExtractionFileUtils.getFinalXMLDataFile(dataExtractionJob,currentDataFileIndex);
		return new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,append), "UTF8"));
	}

	public static void writeDataFileHeader(DataExtractionJob dataExtractionJob,PrintWriter pw) throws Exception
	{
		List<Field> inventoryFieldsDataOnly=FileUtils.getInventoryDataOnlyFields(dataExtractionJob.getInventory());
		FileUtils.writeFileHeader(pw,dataExtractionJob.getInventory().getName(),inventoryFieldsDataOnly,dataExtractionJob.isAuditColumnsIncludedInDataFile());
	}

	public static void writeDataRows(
			DataExtractionJob dataExtractionJob,
			List<String[]> rows,
			List<Field> inventoryFields,
			String labelParameter,
			boolean hasAuditColumns,
			int recordNumberBeforeProcessing,
			boolean isLastBatchToProcess) throws Exception
	{
		PrintWriter pw=null;
		int maximumRecordsPerXMLFile=((DataExtractionJobManager)dataExtractionJob.getExecutionJobManager()).getMaximumRecordsPerXMLFile();
		boolean hasDataFileSplitEnabled=(maximumRecordsPerXMLFile>0);
		try{
			int dataFileIndex=dataExtractionJob.getCurrentDataFileIndex();
			File file = DataExtractionFileUtils.getFinalXMLDataFile(dataExtractionJob,dataFileIndex);
			boolean isDataFileExist=file.exists();
			pw=getPrintWriter(dataExtractionJob,true,dataExtractionJob.getCurrentDataFileIndex());
			if (!isDataFileExist) {
				writeDataFileHeader(dataExtractionJob,pw);
			}
			else
			{
			    RandomAccessFile raf = null;
			    try
			    {
			        raf = new RandomAccessFile(file, "r");
    			    int length = DataFactory.DATA_FILE_FOOTER.length() * 4;
    			    byte[] b = new byte[length];
    			    int offset = (int)Math.max(0, raf.length() - length);
    			    if ( length + offset > raf.length() )
    			    {
    			        length = length - ((length + offset) - (int)raf.length());
    			    }
    			    raf.seek(offset);
    			    length = raf.read(b);
    			    String str = new String(b, 0, length).trim();
    			    if ( str.endsWith(DataFactory.DATA_FILE_FOOTER) )
    			    {
    			        throw new Exception("INTERNAL ERROR: Cannot append to a file that already has a footer.  isLastBatchToProcess = " +
    			                isLastBatchToProcess + ", recordNumberBeforeProcessing = " + recordNumberBeforeProcessing + ", maximumRecordsPerXMLFile = " +
    			                maximumRecordsPerXMLFile + ", raf.length() = " + raf.length());
    			    }
			    }
			    finally
			    {
			        raf.close();
			    }
			}
			String label=labelParameter;
			int labelIterator=dataExtractionJob.getCurrentLabelIterator();
			if (hasDataFileSplitEnabled && labelIterator>0) {
				label=labelParameter+labelIterator;
			}
			for (int i=0;i<rows.size();i++)
			{
				String[] row=rows.get(i);
				int rowNumber=recordNumberBeforeProcessing+i+1;

				FileUtils.writeDataRow(pw,inventoryFields,row,label,hasAuditColumns);

				if (hasDataFileSplitEnabled && (rowNumber % maximumRecordsPerXMLFile==0)) {
					// create a new data file
					FileUtils.writeDataFileFooter(pw);
					IOUtils.closeQuietly(pw);
					dataFileIndex++;
					dataExtractionJob.setCurrentLabelIterator(dataExtractionJob.getCurrentLabelIterator()+1);
					label=labelParameter+dataExtractionJob.getCurrentLabelIterator();
					pw=getPrintWriter(dataExtractionJob,true,dataFileIndex);
					dataExtractionJob.setCurrentDataFileIndex(dataFileIndex);
					writeDataFileHeader(dataExtractionJob,pw);
				}
			}
			if (isLastBatchToProcess) {
				FileUtils.writeDataFileFooter(pw);
			}
		}
		finally{
			IOUtils.closeQuietly(pw);
		}
	}


	public static SortedMap<String, SortedSet<File>>  getInventoryToSqlFileMap(List<String> xmlPathsFrom7ZIPFileThatAreNotInControlFolder, File reverseSqlFolder)
	throws Exception
	{
        Set<String> allTableNamesFromInventory = new HashSet<String>();
	    for ( String zipPath : xmlPathsFrom7ZIPFileThatAreNotInControlFolder )
	    {
	        zipPath = CoreUtil.normalizePathSeparators(zipPath);
            String filename = zipPath.substring(zipPath.lastIndexOf(UtilsConstants.FORWARD_SLASH) + 1);
            String tableName = CoreUtil.getTableName(filename);
            allTableNamesFromInventory.add(tableName);
	    }

        SortedMap<String,SortedSet<File>> toReturn=new TreeMap<String,SortedSet<File>>(String.CASE_INSENSITIVE_ORDER);
        for ( String tableName : allTableNamesFromInventory )
        {
            toReturn.put(tableName, new TreeSet<File>());
        }

        for ( File file : reverseSqlFolder.listFiles() )
        {
            String tableName = CoreUtil.getTableName(file.getName());
            SortedSet<File> fileList = toReturn.get(tableName);
            if ( fileList != null )
            {
                fileList.add(file);
            }
        }
        return toReturn;
	}

	public static InventoryTreeNode buildTree(DataExtractionPanel dataExtractionPanel,
			Map<String,Inventory> inventoriesMap,
			LinkedHashMap<String, String> displayedItemPathToActualItemPathOrderedMap,
			SortedMap<String, SortedSet<File>> inventoryToSQLFileNamesMap) throws Exception {
		return getInventoryTree(dataExtractionPanel,inventoriesMap,displayedItemPathToActualItemPathOrderedMap,inventoryToSQLFileNamesMap);
	}

	public static Map<String,Inventory> getInventoriesMap(JLabel messageLabel,File inventoriesPackageFile,List<String> itemPaths) throws Exception	{
		Map<String,Inventory> toReturn=new HashMap<String,Inventory>();

		File dest=new File(com.rapidesuite.client.common.util.Config.getTempFolder(),"invzip");
		dest.mkdirs();
		GUIUtils.showInProgressMessage(messageLabel,"Please wait, validating the inventories package... Unpacking...");
		SevenZipUtils.decompressFile(inventoriesPackageFile,dest);

		for (int i=0;i<itemPaths.size();i++) {
			String itemPath=itemPaths.get(i);
			if (i % 20 ==0) {
				GUIUtils.showInProgressMessage(messageLabel,"Please wait, validating the inventories package... parsing "+(i+1)+" / "+itemPaths.size()+" files...");
			}
			String[] splitItemPath=itemPath.split("/");
			for (int j=0;j<splitItemPath.length;j++) {
				String splitItem=splitItemPath[j];
				String inventoryName=splitItem.replaceAll(".xml","");
				if (j== (splitItemPath.length-1) && splitItem.endsWith(".xml")) {
					File file=FileUtils.getFile(dest,itemPath);
					Inventory inventory=toReturn.get(inventoryName);
					if (inventory==null) {
						inventory=FileUtils.getInventory(file,inventoryName);
						toReturn.put(inventoryName,inventory);
					}
				}
			}
		}
		GUIUtils.showInProgressMessage(messageLabel,"Please wait, validating the inventories package... Deleting temp files...");
		org.apache.commons.io.FileUtils.deleteDirectory(dest);

		return toReturn;
	}

	private static InventoryTreeNode getInventoryTree(DataExtractionPanel dataExtractionPanel,
			Map<String,Inventory> inventoriesMap, LinkedHashMap<String, String> displayedItemPathToActualItemPathOrderedMap, SortedMap<String, SortedSet<File>> inventoryToSQLFileNamesMap) throws Exception	{
		List<TreePath> treePaths=new ArrayList<TreePath>();

		final List<String> displayedPaths = new ArrayList<String>(displayedItemPathToActualItemPathOrderedMap.keySet());
		
		for (int i=0;i<displayedPaths.size();i++) {
			String itemPath=displayedPaths.get(i);
			String[] splitItemPath=itemPath.split("/");
			InventoryTreeNode[] splitInventoryTreeNodePath=new InventoryTreeNode[splitItemPath.length];
			boolean isSkip=false;
			StringBuffer nodePath=new StringBuffer();
			for (int j=0;j<splitItemPath.length;j++) {
				String splitItem=splitItemPath[j];
				String nodeName=splitItem.replaceAll(".xml","");
				// replace number followed by '-' (without quotes) (e.g. 0001-) from the node name for matching and display.
				// once it is removed, it will not be saved to the newly created template sessions.
				if (j == 2) {
					nodeName = nodeName.replaceAll("[0-9]{4}-", "");
				}
				nodePath.append(nodeName+"###");
				DataExtractionInventoryTreeNode inventoryTreeNode=new DataExtractionInventoryTreeNode(dataExtractionPanel,nodeName,nodePath.toString());
				if (j== (splitItemPath.length-1) && splitItem.endsWith(".xml")) {
					Inventory inventory=inventoriesMap.get(nodeName);
					if (inventory==null) {
						throw new Exception("Unable to find the temporary inventory: '"+nodeName+"'");
					}
					if (inventory.isControlTypeInventory()) {
						isSkip=true;
						break;
					}
					SortedSet<File> sqlFileNames=inventoryToSQLFileNamesMap.get(nodeName);
					inventoryTreeNode.setUnreversible(!inventory.isReversible());
					inventoryTreeNode.setSQLFileNames(sqlFileNames);
					inventoryTreeNode.setInventory(inventory);
					inventoryTreeNode.setArchiveItemName(itemPath);
				}
				splitInventoryTreeNodePath[j]=inventoryTreeNode;
			}
			if (!isSkip) {
				TreePath treePath=new TreePath(splitInventoryTreeNodePath);
				treePaths.add(treePath);
			}
		}
		InventoryTreeNode inventoryTreeNode=buildInventoryTree(dataExtractionPanel,treePaths);
		inventoryTreeNode.resequenceNodesPerParentChild();

		return inventoryTreeNode;
	}

	private static InventoryTreeNode buildInventoryTree(DataExtractionPanel dataExtractionPanel,List<TreePath> treePaths) {
		DataExtractionInventoryTreeNode rootNode=new DataExtractionInventoryTreeNode(dataExtractionPanel,"Root of tree",null);
		return Utils.buildInventoryTree(rootNode,treePaths);
	}

	public static DataExtractionStatusTreeTableNode getDataExecutionStatusTreeTableNode(String path) {
		return new DataExtractionStatusTreeTableNode(path,-1,path,ExecutionStatusTreeTableNode.STATUS_PENDING);
	}

	public static List<String> readContentsFromBuildInfoFile(File file)
	throws Exception
	{
		InputStream is=null;
		try
		{
			is=new FileInputStream(file);
			return FileUtils.readContentsFromInputStream(is,null);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
	}
	
	public static LinkedHashMap<String, String> getDisplayedPathToActualXmlPathMapFrom7ZIPFileThatAreNotInControlFolder(File archiveFile) throws Exception {
		final List<String> allPaths = SevenZipUtils.getItemPaths(archiveFile);
		if (allPaths.contains(GeneralConstants.REVERSE_INVENTORIES_PACKAGE_TREE_STRUCTURE_DESCRIPTOR_FILE)) {
	    	Directory rootDirectory = null;
			try (final Reader reader = new BufferedReader(new InputStreamReader(SevenZipUtils.getStreamFrom7ZIPFile(GeneralConstants.REVERSE_INVENTORIES_PACKAGE_TREE_STRUCTURE_DESCRIPTOR_FILE, archiveFile), CoreConstants.CHARACTER_SET_ENCODING))) {
		        Unmarshaller jaxbUnmarshaller = JAXBContext.newInstance(Directory.class).createUnmarshaller();
		        jaxbUnmarshaller.setEventHandler(new ValidationEventHandler() {

					@Override
					public boolean handleEvent(ValidationEvent event) {      
						return event.getSeverity() != ValidationEvent.ERROR && event.getSeverity() != ValidationEvent.FATAL_ERROR;
					}
		        	
		        });
		        JAXBElement<Directory> root = jaxbUnmarshaller.unmarshal(new StreamSource(reader), Directory.class);
		        rootDirectory = root.getValue();			
			}			
			
			final List<String> actualPaths = SevenZipUtils.getXmlPathsFrom7ZIPFileThatAreNotInControlFolder(archiveFile);
			
			return traverseInventoriesPackageStructureTree(rootDirectory, actualPaths, null);
			
		} else {
			final List<String> paths = SevenZipUtils.getXmlPathsFrom7ZIPFileThatAreNotInControlFolder(archiveFile);
			Collections.sort(paths);
			final LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
			for (final String path : paths) {
				output.put(path, path);
			}
			return output;
		}
	}
	
	private static LinkedHashMap<String, String> traverseInventoriesPackageStructureTree(final Directory node, final List<String> actualPaths, final String nodePath) {
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
		Assert.isTrue(node.getDirectory().isEmpty() ^ node.getInventory().isEmpty(), node.getName()+" has mixed inventories and directories");
		if (node.getInventory().isEmpty()) {
			final String newNodePath = (nodePath==null?"":(nodePath+UtilsConstants.FORWARD_SLASH)) + node.getName();
			for (final Directory child : node.getDirectory()) {
				output.putAll(traverseInventoriesPackageStructureTree(child, actualPaths, newNodePath));
			}			
		} else {
			for (final String child : node.getInventory()) {
				boolean foundTheActualXmlFile = false;
				final String newNodePath = (nodePath==null?"":(nodePath+UtilsConstants.FORWARD_SLASH)) + node.getName() + UtilsConstants.FORWARD_SLASH + child;
				for (final String actualPath : actualPaths) {
					if (actualPath.equals(child) || actualPath.endsWith(UtilsConstants.FORWARD_SLASH+child)) {
						output.put(newNodePath, actualPath);
						foundTheActualXmlFile = true;
						break;
					}
				}
				if (!foundTheActualXmlFile) {
					FileUtils.printStackTrace(new Exception(newNodePath+" does not have an associated XML file"));
				}
			}		
		}
		return output;
	}	
	
	public static String generateDefualtDateStringForReverse_tree_export() {
		Calendar cal = Calendar.getInstance();
		return CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE.getDateFormat().format(cal.getTime());
	}

}