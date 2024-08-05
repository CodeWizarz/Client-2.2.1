/**************************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/SVNPatchManager.java $:
 * $Id: SVNPatchManager.java 31694 2013-03-04 06:33:20Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import noNamespace.LogDocument;
import noNamespace.Logentry;
import noNamespace.Path;
import noNamespace.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;

import com.rapidesuite.client.common.util.UtilsConstants;

public class SVNPatchManager
{

	private LogDocument logDocument;
	private String baseURL;
	private String filteredURL;

	private static final String SUBFOLDER_NAME_INVENTORY = "inventory";
	private static final String SUBFOLDER_NAME_NAVIGATION = "navigation";
	private static final String SUBFOLDER_NAME_SCENARIOS = "scenarios";
	private static final String SUBFOLDER_NAME_PROFILES = "profiles";
	private static final String SUBFOLDER_NAME_SCRIPTS = "scripts";
	private static final String SUBFOLDER_NAME_DATA = "data";

	private static final String ENTRY_INVENTORY_TYPE = "inventory";
	private static final String ENTRY_NAVIGATION_TYPE = "navigation";
	private static final String ENTRY_SCENARIO_TYPE = "scenario";
	private static final String ENTRY_PROFILE_TYPE = "profile";
	private static final String ENTRY_SCRIPT_TYPE = "script";
	private static final String ENTRY_DATA_TYPE = "data";
	private static final String ENTRY_ROOT_TYPE = "";

	private static final File LOG_FILE = new File("log.txt");
	private static final File SVN_EXPORT_COMMANDS_BAT_FILE = new File("svnExportCommands.bat");

	private static final File FOLDER_PATCH = new File("patch");
	private static final File ENTRIES_TO_DELETE_FILE = new File(FOLDER_PATCH, "delete.txt");
	private static final String DATA_MIGRATION_FILE_NAME = "data-migration.xml";
	private static final String RESPONSIBILITY_FILE_NAME = "swiftconfig-navigator.xml";

	public SVNPatchManager(File xmlFile, String baseURL, String filteredURL) throws Exception
	{
		this.baseURL = baseURL;
		this.filteredURL = filteredURL;
		logDocument = parseDocument(xmlFile);
	}

	private static LogDocument parseDocument(File file) throws XmlException, IOException
	{
		FileReader fr = null;
		try
		{
			fr = new FileReader(file);
			return LogDocument.Factory.parse(fr);
		}
		finally
		{
			IOUtils.closeQuietly(fr);
		}
	}

	public void processSVNLogEntries() throws Exception
	{
		Logentry[] logEntries = logDocument.getLog().getLogentryArray();
		List<String> allCommands = new ArrayList<String>();
		Map<String, String> entriesToDeleteMap = new HashMap<String, String>();
		Map<String, String> dataEntriesToDeleteMap = new HashMap<String, String>();

		for ( Logentry logEntry : logEntries )
		{
			String revision = logEntry.getRevision();
			Paths paths = logEntry.getPaths();
			Path[] pathArray = paths.getPathArray();
			for ( Path path : pathArray )
			{
				String url = path.getDomNode().getFirstChild().getNodeValue();

				if ( !validURL(url) )
				{
					writeToFile(LOG_FILE, "WARNING: Skipping entry, invalid URL: '" + url + "'\n", true);
					continue;
				}

				File exportFile = new File(url);
				String entryType = getEntryType(exportFile);
				if ( entryType == null )
				{
					continue;
				}
				File fullControlDataFolder = new File(FOLDER_PATCH, getFullControlDataFolder(entryType, exportFile).toString());
				fullControlDataFolder.mkdirs();
				String localFileName = fullControlDataFolder + UtilsConstants.FORWARD_SLASH + "\"" + exportFile.getName() + "\"";
				String action = path.getAction();
				if ( action.equalsIgnoreCase("D") )
				{
					if ( url.indexOf("/data/") != -1 )
					{
						dataEntriesToDeleteMap.put(exportFile.getName(), entryType);
					}
					else
					{
						System.out.println("adding entry to delete: " + exportFile.getName() + UtilsConstants.FORWARD_SLASH + entryType);
						entriesToDeleteMap.put(exportFile.getName(), entryType);
					}
					allCommands.add("del /s/q " + localFileName);
				}
				else
				{
					// not removing entry for data files as the label must be
					// set by PD.
					if ( url.indexOf("/data/") != -1 )
					{
						System.out.println("keeping entry because this is a DATA file: " + exportFile.getName());
					}
					else
					{
						System.out.println("removing entry as it was re-added: " + exportFile.getName() + UtilsConstants.FORWARD_SLASH + entryType);
						entriesToDeleteMap.remove(exportFile.getName());
					}

					// The sub path for the local file name requires slashes
					// instead of backslashes
					// else the SVN export command will fail.
					localFileName = localFileName.replaceAll("\\\\", UtilsConstants.FORWARD_SLASH);
					allCommands.add(getSVNExportCommand(url, localFileName, revision));
				}
			}
		}
		writeCommandsToFile(allCommands);
		System.out.println("Entries to delete: " + entriesToDeleteMap);
		if ( !entriesToDeleteMap.isEmpty() )
		{
			Map<String, Map<String, String>> tempMap = new HashMap<String, Map<String, String>>();
			initEntryMap(entriesToDeleteMap, tempMap);
			initEntryMap(dataEntriesToDeleteMap, tempMap);

			Iterator<String> iterator = tempMap.keySet().iterator();
			StringBuffer entriesToDelete = new StringBuffer("");
			while ( iterator.hasNext() )
			{
				String key = iterator.next();
				Map<String, String> values = tempMap.get(key);

				for ( String value : values.keySet() )
				{
					entriesToDelete.append(key).append(",").append(value).append("\n");
				}
			}
			writeToFile(ENTRIES_TO_DELETE_FILE, entriesToDelete.toString(), false);
		}
	}

	private void initEntryMap(Map<String, String> entriesToDeleteMap, Map<String, Map<String, String>> tempMap)
	{
		Iterator<String> iterator = entriesToDeleteMap.keySet().iterator();
		while ( iterator.hasNext() )
		{
			String elementName = iterator.next();
			String entryType = entriesToDeleteMap.get(elementName);

			elementName = elementName.substring(0, elementName.lastIndexOf("."));

			Map<String, String> map = tempMap.get(entryType);
			if ( map == null )
			{
				map = new TreeMap<String, String>();
				tempMap.put(entryType, map);
			}
			map.put(elementName, elementName);
		}
	}

	private boolean validURL(String url)
	{
		return (url.indexOf(filteredURL) != -1 && url.indexOf(".xml") != -1);
	}

	private File getFullControlDataFolder(String entryType, File exportFile) throws Exception
	{
		if ( entryType.isEmpty() )
		{
			return new File(".");
		}
		int indexOf = exportFile.getParent().indexOf(entryType);
		if ( indexOf == -1 )
		{
			throw new Exception("Unable to find the entry type: '" + entryType + "' in the file: '" + exportFile.getAbsolutePath() + "'");
		}
		// System.out.println("indexOf:"+indexOf+" exportFile:"+exportFile);
		String temp = exportFile.getParent().substring(indexOf);
		// System.out.println("temp:"+temp);
		temp = temp.replaceAll(" ", "_");
		File toReturn = new File(temp);
		return toReturn;
	}

	private String getEntryType(File file) throws Exception
	{
		String absolutePath = file.getAbsolutePath();
		if ( hasSubFolder(absolutePath, SUBFOLDER_NAME_INVENTORY) )
		{
			return ENTRY_INVENTORY_TYPE;
		}
		else if ( hasSubFolder(absolutePath, SUBFOLDER_NAME_NAVIGATION) )
		{
			return ENTRY_NAVIGATION_TYPE;
		}
		else if ( hasSubFolder(absolutePath, SUBFOLDER_NAME_SCENARIOS) )
		{
			return ENTRY_SCENARIO_TYPE;
		}
		else if ( hasSubFolder(absolutePath, SUBFOLDER_NAME_PROFILES) )
		{
			return ENTRY_PROFILE_TYPE;
		}
		else if ( hasSubFolder(absolutePath, SUBFOLDER_NAME_SCRIPTS) )
		{
			return ENTRY_SCRIPT_TYPE;
		}
		else if ( hasSubFolder(absolutePath, SUBFOLDER_NAME_DATA) )
		{
			return ENTRY_DATA_TYPE;
		}
		else if ( absolutePath.indexOf(DATA_MIGRATION_FILE_NAME) != -1 )
		{
			return ENTRY_ROOT_TYPE;
		}
		else if ( absolutePath.indexOf(RESPONSIBILITY_FILE_NAME) != -1 )
		{
			return ENTRY_ROOT_TYPE;
		}
		writeToFile(LOG_FILE, "WARNING: Skipping entry, cannot find the controldata type in the path: '" + absolutePath + "'\n", true);
		return null;
	}

	private boolean hasSubFolder(String url, String subFolderToFind)
	{
		return url.indexOf(UtilsConstants.FORWARD_SLASH + subFolderToFind + UtilsConstants.FORWARD_SLASH) != -1;
	}

	private String getSVNExportCommand(String url, String localFileName, String revision)
	{
		String exportCommand = "svn export --force \"" + baseURL + url + "\"@" + revision + " " + localFileName;
		return exportCommand;
	}

	private void writeCommandsToFile(List<String> commands) throws Exception
	{
		StringBuffer content = new StringBuffer("");
		for ( int i = 0; i < commands.size(); i++ )
		{
			String command = commands.get(i);
			content.append("echo '" + (i + 1) + " / " + commands.size() + " commands'\n");
			content.append(command + "\n");
		}
		writeToFile(SVN_EXPORT_COMMANDS_BAT_FILE, content.toString(), true);
	}

	private static void writeToFile(File file, String content, boolean append) throws Exception
	{
		Writer output = null;
		try
		{
			output = new BufferedWriter(new FileWriter(file, append));
			output.write(content);
		}
		finally
		{
			if ( null != output )
			{
				output.close();
			}
		}
	}

	public static void main(String[] arg)
	{
		try
		{
			File xmlFile = new File(arg[0]);
			String baseURL = arg[1];
			String filteredURL = arg[2];
			SVNPatchManager svnPatchManager = new SVNPatchManager(xmlFile, baseURL, filteredURL);
			svnPatchManager.processSVNLogEntries();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

}