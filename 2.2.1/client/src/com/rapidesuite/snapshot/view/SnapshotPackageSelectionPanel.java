package com.rapidesuite.snapshot.view;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.xml.sax.InputSource;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.utility.XmlNavigatorParser;
import com.rapidesuite.configurator.utility.XmlNavigatorParser.EbsResponsibilityRecord;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;
import com.rapidesuite.snapshot.model.ControllerModalWindow;
import com.rapidesuite.snapshot.model.FormInformation;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.NavigatorNode;
import com.rapidesuite.snapshot.model.NavigatorNodePath;
import com.rsc.rapidsnapshot.inventoryFormInfo0000.FormInfo;
import com.rsc.rapidsnapshot.respToApplicationMapping0000.Mapping;
import com.rsc.rapidsnapshot.respToApplicationMapping0000.Mappings;
import com.rsc.rapidsnapshot.respToApplicationMapping0000.MappingsDocument;

@SuppressWarnings("serial")
public class SnapshotPackageSelectionPanel extends JPanel{

	private TabSnapshotsPanel tabSnapshotsPanel;
	private Map<String,Map<String, File>> oracleReleaseToInventoryNameToInventoryFileMap;
	private Map<String,Map<String, File>> oracleReleaseToInventoryNameToReverseSQLFileMap;
	private Map<String,Map<String, List<File>>> oracleReleaseToInventoryNameToAlternateReverseSQLFilesMap;
	private Set<String> supportedOracleReleasesSet;
	private Map<String,Map<String,FormInformation>> oracleReleaseToInventoryNameToFormInformation;
			
	public static final File DEFAULT_PACKAGE_FOLDER=new File("snapshot-packages");
	public static final String ORACLE_RELEASE_12_1_2="12.1.2";
	public static final String ORACLE_RELEASE_12_1_3="12.1.3";

	public SnapshotPackageSelectionPanel(TabSnapshotsPanel tabSnapshotsPanel) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		oracleReleaseToInventoryNameToInventoryFileMap=new HashMap<String,Map<String, File>>();
		oracleReleaseToInventoryNameToReverseSQLFileMap=new HashMap<String,Map<String, File>>();
		oracleReleaseToInventoryNameToFormInformation=new HashMap<String,Map<String, FormInformation>>();
		oracleReleaseToInventoryNameToAlternateReverseSQLFilesMap=new HashMap<String,Map<String, List<File>>>();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setAlignmentX(Component.LEFT_ALIGNMENT);
		createComponents();
	}

	private void createComponents() {
	}
	
	public void initializeSupportedOracleReleases() throws Exception {
		supportedOracleReleasesSet=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		File[] files=DEFAULT_PACKAGE_FOLDER.listFiles();
		if (files==null) {
			return;
		}
		for (File file:files) {
			supportedOracleReleasesSet.add(file.getName());
		}
		/*
		 * Adding support for 12.1.2 which is almost identical to 12.1.3 in term of DB structure.
		 * so, we will redirect to the 12.1.3 folder.
		 */
		supportedOracleReleasesSet.add(ORACLE_RELEASE_12_1_2);
		
		/*
		 * Not supported for now: this is if we want to use a package from Configurator containing
		 * all the inventories and SQLs.
		if (!supportedOracleReleasesSet.isEmpty()) {
			new Thread( new Runnable() {
				@Override
				public void run() {
					try {
						initializeDefaultSnapshotPackageFiles();
					} 
					catch (Exception e) {
						FileUtils.printStackTrace(e);
					}
				}
			}).start();
		}
		*/
	}
	
	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}
	
	public void validateOracleRelease(String oracleRelease) throws Exception {
		boolean hasOracleRelease=supportedOracleReleasesSet.contains(oracleRelease);
		if ( !hasOracleRelease) {
			throw new Exception("Oracle release '"+oracleRelease+"' not supported");
		}
	}

	public File getPLSQLPackageFile(String oracleRelease) throws Exception {
		validateOracleRelease(oracleRelease);
		File oracleReleaseFolder=getOracleReleaseFolder(oracleRelease);
		File reverseSQLRootFolder=new File(oracleReleaseFolder,"snapshot_sql");
		File packageFile=getFirstPLSQLPackageFile(reverseSQLRootFolder);
		return packageFile;
	}
		
	public Map<String, File> getInventoryNameToInventoryFileMap(String oracleRelease) throws Exception {
		validateOracleRelease(oracleRelease);
		Map<String, File> inventoryNameToInventoryFileMap=oracleReleaseToInventoryNameToInventoryFileMap.get(oracleRelease);
		return inventoryNameToInventoryFileMap;
	}
	
	public Map<String, File> getInventoryNameToReverseSQLFileMap(String oracleRelease) throws Exception {
		validateOracleRelease(oracleRelease);
		Map<String, File> inventoryNameToReverseSQLFileMap=oracleReleaseToInventoryNameToReverseSQLFileMap.get(oracleRelease);
		return inventoryNameToReverseSQLFileMap;
	}	
	
	private File getFirstPLSQLPackageFile(File folder) {
		File[] files=folder.listFiles();
		if (files==null) {
			return null;
		}
		for (File file:files) {
			if (file.getName().endsWith("_pkg.sql")) {
				return file;
			}
		}
		return null;
	}

	public void initInventories(String currentConnectionOracleRelease,ControllerModalWindow controllerModalWindow) throws Exception {
		File oracleReleaseFolder=getOracleReleaseFolder(currentConnectionOracleRelease);
		
		File reverseSQLRootFolder=new File(oracleReleaseFolder,"snapshot_sql");
		Map<String, File> inventoryNameToReverseSQLFileMap=oracleReleaseToInventoryNameToReverseSQLFileMap.get(currentConnectionOracleRelease);
		if (inventoryNameToReverseSQLFileMap==null) {
			inventoryNameToReverseSQLFileMap=ModelUtils.getFileNameToFileMap(reverseSQLRootFolder,false);
			oracleReleaseToInventoryNameToReverseSQLFileMap.put(currentConnectionOracleRelease, inventoryNameToReverseSQLFileMap);
			initAlternateSQLQueryFilesMap(inventoryNameToReverseSQLFileMap,currentConnectionOracleRelease);
		}
		
		File inventoryRootFolder=new File(oracleReleaseFolder,"inventories");
		Map<String, File> inventoryNameToInventoryFileMap=oracleReleaseToInventoryNameToInventoryFileMap.get(currentConnectionOracleRelease);
		if (inventoryNameToInventoryFileMap==null) {
			Map<String, File> allInventoriesFileMap=ModelUtils.getFileNameToFileMap(inventoryRootFolder,true);
			inventoryNameToInventoryFileMap=new TreeMap<String, File>();
			oracleReleaseToInventoryNameToInventoryFileMap.put(currentConnectionOracleRelease, inventoryNameToInventoryFileMap);
			Map<String, FormInformation> inventoryNameToFormInformationMap=new TreeMap<String, FormInformation>();
			oracleReleaseToInventoryNameToFormInformation.put(currentConnectionOracleRelease, inventoryNameToFormInformationMap);
			
			//initInventoriesBasedOnOracleDB(allInventoriesFileMap,currentConnectionOracleRelease,inventoryNameToInventoryFileMap,inventoryNameToFormInformationMap,inventoryNameToReverseSQLFileMap,controllerModalWindow);
			initInventoriesBasedOnNavigator(allInventoriesFileMap,currentConnectionOracleRelease,inventoryNameToInventoryFileMap,inventoryNameToFormInformationMap,inventoryNameToReverseSQLFileMap,controllerModalWindow);
		}
	}
	
	private void initAlternateSQLQueryFilesMap(Map<String, File> inventoryNameToReverseSQLFileMap,String oracleRelease) {
		oracleReleaseToInventoryNameToAlternateReverseSQLFilesMap.put(oracleRelease,new HashMap<String, List<File>>() );
		Iterator<String> iterator=inventoryNameToReverseSQLFileMap.keySet().iterator();
		while (iterator.hasNext()) {
			String fileName=iterator.next();
			int indexFirstDot=fileName.indexOf(".");
			if (indexFirstDot==-1) {
				continue;
			}

			String textAfterFirstDot=fileName.substring(indexFirstDot+1);
			int indexDot=textAfterFirstDot.indexOf(".");
			if (indexDot==-1) {
				continue;
			}
			String inventoryName=fileName.substring(0, indexFirstDot);
			File file=inventoryNameToReverseSQLFileMap.get(fileName);
			Map<String, List<File>> inventoryNameToAlternateReverseSQLFilesMap=oracleReleaseToInventoryNameToAlternateReverseSQLFilesMap.get(oracleRelease);
			List<File> alternateReverseSQLFileList=inventoryNameToAlternateReverseSQLFilesMap.get(inventoryName);
			if (alternateReverseSQLFileList==null) {
				alternateReverseSQLFileList=new ArrayList<File>();
				inventoryNameToAlternateReverseSQLFilesMap.put(inventoryName, alternateReverseSQLFileList);
			}
			alternateReverseSQLFileList.add(file);
		}
	}

	public File getInventoriesFormInfoFile(String oracleRelease) throws Exception {
		validateOracleRelease(oracleRelease);
		File oracleReleaseFolder=getOracleReleaseFolder(oracleRelease);
		File file=new File(oracleReleaseFolder,"inventoriesFormInfo.xml");
		if (!file.exists()) {
			throw new Exception("Internal error: inventoriesFormInfo.xml not found");
		}
		return file;
	}
	
	public static String OU="rsc_ou_id";
	public static String INV_ORG="rsc_inv_org_id";
	public static String LEDGER="rsc_ledger_id";
	public static String BG="rsc_bg_id";
	public static String COA="rsc_coa_id";
	
	public static String REGEX_CAST_NUMBER = "cast[ ]*\\([ ]*null[ ]+as[ ]+number\\)[ ]+as[ ]+";
	public static String REGEX_NORMAL = "null[ ]+as[ ]+";
	
	public static Pattern OU_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+OU, Pattern.CASE_INSENSITIVE);
	public static Pattern OU_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+OU, Pattern.CASE_INSENSITIVE);
	
	public static Pattern INV_ORG_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+INV_ORG, Pattern.CASE_INSENSITIVE);
	public static Pattern INV_ORG_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+INV_ORG, Pattern.CASE_INSENSITIVE);
	
	public static Pattern LEDGER_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+LEDGER, Pattern.CASE_INSENSITIVE);
	public static Pattern LEDGER_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+LEDGER, Pattern.CASE_INSENSITIVE);
	
	public static Pattern BG_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+BG, Pattern.CASE_INSENSITIVE);
	public static Pattern BG_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+BG, Pattern.CASE_INSENSITIVE);
	
	public static Pattern COA_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+COA, Pattern.CASE_INSENSITIVE);
	public static Pattern COA_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+COA, Pattern.CASE_INSENSITIVE);
	
	public static void computeFormInformationFromSQLFile(File sqlFile,FormInformation formInformation) throws Exception {
		String sqlFileContent=FileUtils.readContentsFromSQLFile(sqlFile).toLowerCase();
		
		boolean hasOperatingUnitId=
				(!OU_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() &&
						!OU_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find() ) ||
						sqlFileContent.indexOf("VALIDATE_OU_NAME".toLowerCase())!=-1;
		
		boolean hasInventoryOrganizationId=
				(!INV_ORG_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() &&
						!INV_ORG_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find() ) ||
						sqlFileContent.indexOf("validate_io_name".toLowerCase())!=-1;
		
		boolean hasLedgerId=
				!LEDGER_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() &&
						!LEDGER_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find();
		
		boolean hasBusinessGroupId=
				!BG_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() &&
						!BG_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find();
		
		boolean hasCOAId=
				!COA_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() &&
						!COA_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find();
		
		// by default legalEntityFilteringColumnNumber is -1
		int legalEntityFilteringColumnNumber = -1;
		// first check if the keyword is present in the sqlFileContent because building patterns can be computationally expensive
		if(sqlFileContent.toLowerCase().indexOf(ModelUtils.LEGEL_ENTITY_FILTERING_IDENIFIER.toLowerCase()) >= 0) {
			// find the LEGEL_ENTITY_FILTERING_IDENIFIER in the SQL
			//Pattern pattern = Pattern.compile(ModelUtils.LEGEL_ENTITY_FILTERING_IDENIFIER_REGEX, Pattern.CASE_INSENSITIVE);
			Pattern pattern = Pattern.compile("--RSC_LE_FILTERING=(\\d+)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(sqlFileContent);
			String match = null;
			if (matcher.find())
			{
				match = matcher.group(0);
			}
			// if LEGEL_ENTITY_FILTERING_IDENIFIER found in the SQL
			if(match != null) {
				// find the number 
				pattern = Pattern.compile("[\\d]+");
				matcher = pattern.matcher(match);
				String match2 = null;
				if (matcher.find())
				{
					match2 = matcher.group(0);
				}
				// if number found
				if(match2 != null) {
					// set it to legalEntityFilteringColumnNumber
					legalEntityFilteringColumnNumber = Integer.parseInt(match2);
				}
			}
		}
		
		if(sqlFileContent.toLowerCase().indexOf(ModelUtils.DISABLE_OU_AND_LEVEL_FILTER_IDENIFIER.toLowerCase()) >= 0) {
			formInformation.setDisableOUAndLevelFiltering(true);
		}
		
		formInformation.setHasLedgerId(hasLedgerId);
		formInformation.setHasCOAId(hasCOAId);
		formInformation.setHasOperatingUnitId(hasOperatingUnitId);
		formInformation.setHasInventoryOrganizationId(hasInventoryOrganizationId);
		formInformation.setHasBusinessGroupId(hasBusinessGroupId);
		formInformation.setLegalEntityFilteringColumnNumber(legalEntityFilteringColumnNumber);
	}

	public Map<String, FormInformation> getInventoryNameToFormInformation(String oracleRelease) {
		return oracleReleaseToInventoryNameToFormInformation.get(oracleRelease);
	}

	public void initInventoriesBasedOnOracleDB(Map<String, File> allInventoriesFileMap,String currentConnectionOracleRelease,
			Map<String, File> inventoryNameToInventoryFileMap,
			Map<String, FormInformation> inventoryNameToFormInformationMap ,
			Map<String, File> inventoryNameToReverseSQLFileMap,
			ControllerModalWindow controllerModalWindow) throws Exception {
		int inventoriesCounter=0;
		Iterator<String> iterator=allInventoriesFileMap.keySet().iterator();
		Map<String, List<FormInfo>> inventoryNameToListFormInfoMap=ModelUtils.getInventoryNameToFormInfoMap(tabSnapshotsPanel,currentConnectionOracleRelease);
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			inventoriesCounter++;

			if (inventoriesCounter % 500 == 0) {
				controllerModalWindow.updateExecutionLabels("Initializing Inventories... ( "+
						Utils.formatNumberWithComma(inventoriesCounter)+" / "+Utils.formatNumberWithComma(allInventoriesFileMap.size())+" )");
			}

			if (inventoryName.startsWith("R11 - ") ) {
				continue;
			}
			File inventoryFile=allInventoriesFileMap.get(inventoryName);
			inventoryNameToInventoryFileMap.put(inventoryName, inventoryFile);

			File sqlFile=inventoryNameToReverseSQLFileMap.get(inventoryName);

			FormInformation formInformation=new FormInformation();
			inventoryNameToFormInformationMap.put(inventoryName,formInformation);
			if (sqlFile!=null) {
				computeFormInformationFromSQLFile(sqlFile,formInformation);
			}

			List<FormInfo> formInfoList=inventoryNameToListFormInfoMap.get(inventoryName);
			if (formInfoList==null) {
				formInformation.setFormName(UIConstants.UI_NA);
			}
			else {
				Set<String> applicationNameSet=formInformation.getApplicationNameSet();
				Set<String> fullPathSet=formInformation.getFullPathSet();
				for (FormInfo formInfo:formInfoList) {
					formInformation.setFormName(formInfo.getFormName());
					if (!formInfo.getApplicationName().isEmpty()) {
						applicationNameSet.add(formInfo.getApplicationName());
					}
					if (!formInfo.getMenuPath().isEmpty()) {
						fullPathSet.add(formInfo.getMenuPath());
					}
				}
			} 				
		}		
	}
	
	/*
	public static Map<String,List<NavigatorNode>> getFunctionIdToFunctionIdNavigatorNodesMap(File file) throws Exception {
		FileInputStream fis =null;
		Map<String,List<NavigatorNode>> functionIdToFunctionIdNavigatorNodesMap=new TreeMap<String,List<NavigatorNode>>();
		try{
			fis =new FileInputStream(file);

			final XmlNavigatorParser xmlNavigatorParser = new XmlNavigatorParser();
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final SAXParser parser = factory.newSAXParser();
			parser.parse(new InputSource(new InputStreamReader(fis, CoreConstants.CHARACTER_SET_ENCODING)), xmlNavigatorParser);

			List<EbsResponsibilityRecord> list= xmlNavigatorParser.allRecords;
			
			Map<String,NavigatorNode> subMenuIdToNavigatorNodeMap=new TreeMap<String,NavigatorNode>();
			
			//UPDATE: NO NEED TO REMOVE THE DEAD PATHS HERE AS THEY ARE ALWAYS AFTER THE VALID PATHS SO THE CODE BELOW IGNORES ALWAYS THE SECOND ENTRIES IF ANY (SEE IF1)
			// ISSUE: sub menu id is not unique => one node may have 2 or more parents!!!
			for (EbsResponsibilityRecord ebsResponsibilityRecord: list) {
				String subMenuId=ebsResponsibilityRecord.getSubMenuId();
				String functionId=ebsResponsibilityRecord.getFormFunctionId();

				NavigatorNode navigatorNode=new NavigatorNode(ebsResponsibilityRecord);	
				// Important: always check the function id first - it must take precedence over the sub menu id as 
				// some nodes have both!?!
				if (functionId!=null && !functionId.isEmpty()) {
					List<NavigatorNode> functionIdNavigatorNodes=functionIdToFunctionIdNavigatorNodesMap.get(functionId);
					if (functionIdNavigatorNodes==null) {
						functionIdNavigatorNodes=new ArrayList<NavigatorNode>();
						functionIdToFunctionIdNavigatorNodesMap.put(functionId,functionIdNavigatorNodes);
					}
					functionIdNavigatorNodes.add(navigatorNode);
				}
				else {
					NavigatorNode navigatorNodeTemp=subMenuIdToNavigatorNodeMap.get(subMenuId);
					// IF1
					if (navigatorNodeTemp!=null) {
						//throw new Exception("subMenuId: "+subMenuId+" already has a node: '"+navigatorNodeTemp.getEbsResponsibilityRecord().getDisplayText()+"'");
						//System.out.println("subMenuId: "+subMenuId+" already has a node: '"+navigatorNodeTemp.getEbsResponsibilityRecord().getDisplayText()+"'");
						continue;
					}
					subMenuIdToNavigatorNodeMap.put(subMenuId, navigatorNode);
				}
			}
			
			
			// Linking all the nodes to be a Tree
			Set<String> keySet = subMenuIdToNavigatorNodeMap.keySet();
			for (String subMenuId : keySet) {
				NavigatorNode navigatorNode = subMenuIdToNavigatorNodeMap.get(subMenuId);
				String parentId = navigatorNode.getEbsResponsibilityRecord().getMenuId();
				NavigatorNode parentNavigatorNode = subMenuIdToNavigatorNodeMap.get(parentId);
				//System.out.println("subMenuId:"+subMenuId+" parentId:"+parentId+" parentNavigatorNode:"+parentNavigatorNode);
				if (parentNavigatorNode != null) {
					navigatorNode.setParent(parentNavigatorNode);
					parentNavigatorNode.addChild(navigatorNode);
					
				}
			}
			Iterator<String> iterator = functionIdToFunctionIdNavigatorNodesMap.keySet().iterator();
			while (iterator.hasNext()) {
				String functionId=iterator.next();
				List<NavigatorNode> navigatorNodes = functionIdToFunctionIdNavigatorNodesMap.get(functionId);
				for (NavigatorNode navigatorNode:navigatorNodes) {
					String parentId = navigatorNode.getEbsResponsibilityRecord().getMenuId();
					NavigatorNode parentNavigatorNode = subMenuIdToNavigatorNodeMap.get(parentId);
										
					if (parentNavigatorNode != null) {
						navigatorNode.setParent(parentNavigatorNode);
						parentNavigatorNode.addChild(navigatorNode);
					}
				}
			}		
			
			// remove all dead paths - meaning some function ids link to branches going nowhere so
			//  if those menu ids don't go up until a menuId =-1 then discard.
			Map<String,List<NavigatorNode>> finalFunctionIdToFunctionIdNavigatorNodesMap=new TreeMap<String,List<NavigatorNode>>();
			iterator = functionIdToFunctionIdNavigatorNodesMap.keySet().iterator();
			while (iterator.hasNext()) {
				String functionId=iterator.next();
				List<NavigatorNode> navigatorNodes = functionIdToFunctionIdNavigatorNodesMap.get(functionId);
				List<NavigatorNode> finalFunctionIdNavigatorNodes=new ArrayList<NavigatorNode>();
				for (NavigatorNode navigatorNode:navigatorNodes) {
					boolean isDeadBranch = navigatorNode.isDeadBranch();
					if (!isDeadBranch) {
						finalFunctionIdNavigatorNodes.add(navigatorNode);
					}
				}
				if (!finalFunctionIdNavigatorNodes.isEmpty()) {
					finalFunctionIdToFunctionIdNavigatorNodesMap.put(functionId,finalFunctionIdNavigatorNodes);
				}
			}		
			
			return finalFunctionIdToFunctionIdNavigatorNodesMap;	
		}
		catch(Exception e) {
			e.printStackTrace();
			FileUtils.printStackTrace(e);
			return functionIdToFunctionIdNavigatorNodesMap;
		}
		finally {
			if (fis!=null) {
				fis.close();
			}
		}
	}
	*/
	
	public static Map<String,List<NavigatorNodePath>> getFunctionIdToFunctionIdNavigatorNodesMap(File file) throws Exception {
		FileInputStream fis =null;
		Map<String,List<NavigatorNodePath>> functionIdToFunctionIdNavigatorNodesMap=new TreeMap<String,List<NavigatorNodePath>>();
		try{
			fis =new FileInputStream(file);

			final XmlNavigatorParser xmlNavigatorParser = new XmlNavigatorParser();
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final SAXParser parser = factory.newSAXParser();
			parser.parse(new InputSource(new InputStreamReader(fis, CoreConstants.CHARACTER_SET_ENCODING)), xmlNavigatorParser);

			List<EbsResponsibilityRecord> list= xmlNavigatorParser.allRecords;
						
			/* 
			 * First step is to map all the nodes by menu ids and catch the root node.
			 * validate:
			 * 	 - discard duplicate nodes (meaning same menu id - sub menu id- function id) - there are about 10 in 12.2.4
			 * Note: a menu Id may have more than one parent
			 */
			Map<String,List<NavigatorNode>> menuIdToNavigatorNodesMap=new TreeMap<String,List<NavigatorNode>>();
			NavigatorNode rootNode=null;
			Set<String> keySet=new HashSet<String>();
			for (EbsResponsibilityRecord ebsResponsibilityRecord: list) {
				String menuId=ebsResponsibilityRecord.getMenuId();
				String subMenuId=ebsResponsibilityRecord.getSubMenuId();
				String functionId=ebsResponsibilityRecord.getFormFunctionId();
				String key=menuId+"-"+subMenuId+"-"+functionId;
				boolean hasKey=keySet.contains(key);
				if (hasKey) {
					//System.out.println("Duplicate key detected in the Navigator file, key: '"+key+"'");
					continue;
				}
				keySet.add(key);
				
				NavigatorNode navigatorNode=new NavigatorNode(ebsResponsibilityRecord);	
				if (menuId.equalsIgnoreCase("-2")) {
					rootNode=navigatorNode;
				}
				
				List<NavigatorNode> navigatorNodes=menuIdToNavigatorNodesMap.get(menuId);
				if (navigatorNodes==null) {
					navigatorNodes=new ArrayList<NavigatorNode>();
					menuIdToNavigatorNodesMap.put(menuId,navigatorNodes);
				}
				navigatorNodes.add(navigatorNode);
			}
			if (rootNode==null) {
				throw new Exception("Internal error: invalid Navigator file, cannot find the root node!");
			}
			//System.out.println("rootNode: "+rootNode.getEbsResponsibilityRecord().getDisplayText());			
			
			/* 
			 * Second step is to build the Tree starting from the root node as there are a lot of dead paths
			 * So, starting from the root will eliminate those automatically.
			 */
			//System.out.println(new java.util.Date()+" linking nodes...");
			rootNode.linkNodes(menuIdToNavigatorNodesMap);
			//System.out.println(new java.util.Date()+" linking nodes COMPLETED.");
			
			//String functionIdToPrint="1277";
			//System.out.println(new java.util.Date()+" printing count for functionId: "+functionIdToPrint+" ...");
			//int cnt=rootNode.getFunctionCount(functionIdToPrint);
			//System.out.println(new java.util.Date()+" count: "+cnt);
			
			//System.out.println(new java.util.Date()+" getPaths ...");
			List<NavigatorNodePath> paths=rootNode.getPaths(new ArrayList<String>());
			for (NavigatorNodePath navigatorNodePath:paths) {
				List<NavigatorNodePath> nodes=functionIdToFunctionIdNavigatorNodesMap.get(navigatorNodePath.getFunctionId());
				if (nodes==null) {
					nodes=new ArrayList<NavigatorNodePath>();
					functionIdToFunctionIdNavigatorNodesMap.put(navigatorNodePath.getFunctionId(),nodes);
				}
				nodes.add(navigatorNodePath);
			}
			//System.out.println(new java.util.Date()+" getPaths: "+paths.size()+" COMPLETED.");
			
			/*
			System.out.println(new java.util.Date()+" printing nodes for functionId: "+functionIdToPrint);
			List<NavigatorNodePath> nodes=functionIdToFunctionIdNavigatorNodesMap.get(functionIdToPrint);
			if (nodes!=null) {
				for (NavigatorNodePath navigatorNodePath:nodes) {
					List<String> fullPath=navigatorNodePath.getFullPath();
					System.out.println("fullPath: "+fullPath);
				}
				System.out.println("Found nodes: "+nodes.size());
			}
			System.out.println(new java.util.Date()+" printing nodes completed");
			*/		
			return functionIdToFunctionIdNavigatorNodesMap;	
		}
		catch(Exception e) {
			e.printStackTrace();
			FileUtils.printStackTrace(e);
			return functionIdToFunctionIdNavigatorNodesMap;
		}
		finally {
			if (fis!=null) {
				fis.close();
			}
		}
	}
	
	public void initInventoriesBasedOnNavigator(Map<String, File> allInventoriesFileMap,String currentConnectionOracleRelease,
			Map<String, File> inventoryNameToInventoryFileMap,
			Map<String, FormInformation> inventoryNameToFormInformationMap ,
			Map<String, File> inventoryNameToReverseSQLFileMap,
			ControllerModalWindow controllerModalWindow) throws Exception {
		int inventoriesCounter=0;
		Iterator<String> iterator=allInventoriesFileMap.keySet().iterator();
		

		File oracleReleaseFolder=getOracleReleaseFolder(currentConnectionOracleRelease);
		
		File respToApplicationMappingFile=new File(oracleReleaseFolder,"respToApplicationMapping.xml");
		if (!respToApplicationMappingFile.exists()) {
			throw new Exception("Internal error: respToApplicationMappingFile.xml not found");
		}
		Map<String,String> respToApplicationMappingMap=new HashMap<String,String>(); 
		if (respToApplicationMappingFile.exists()) {
			respToApplicationMappingMap=getRespToApplicationMappingMap(respToApplicationMappingFile); 
		}
		
		File navigatorFile=new File(oracleReleaseFolder,"swiftconfig-navigator.xml");
		if (!navigatorFile.exists()) {
			throw new Exception("Internal error: swiftconfig-navigator.xml not found");
		}
		Map<String,List<NavigatorNodePath>> functionIdToFunctionIdNavigatorNodesMap=new HashMap<String,List<NavigatorNodePath>>(); 
		if (navigatorFile.exists()) {
			functionIdToFunctionIdNavigatorNodesMap=getFunctionIdToFunctionIdNavigatorNodesMap(navigatorFile); 
		}
		Map<String, List<String>> inventoryNameToFunctionIdsMap=ModelUtils.getInventoryNameToFunctionIdsMap(tabSnapshotsPanel,currentConnectionOracleRelease);
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			inventoriesCounter++;

			if (inventoriesCounter % 500 == 0) {
				controllerModalWindow.updateExecutionLabels("Initializing Inventories... ( "+
						Utils.formatNumberWithComma(inventoriesCounter)+" / "+Utils.formatNumberWithComma(allInventoriesFileMap.size())+" )");
			}

			if (inventoryName.startsWith("R11 - ") ) {
				continue;
			}
			List<String> functionIds=inventoryNameToFunctionIdsMap.get(inventoryName);
			if (functionIds==null) {
				functionIds=new ArrayList<String>();
				//FileUtils.println("!!!WARNING!!! Inventory removed from list!!! Cannot find functionId for the inventory name: '"+inventoryName+"'");
				//continue;
			}
			
			File inventoryFile=allInventoriesFileMap.get(inventoryName);
			inventoryNameToInventoryFileMap.put(inventoryName, inventoryFile);

			File sqlFile=inventoryNameToReverseSQLFileMap.get(inventoryName);

			FormInformation formInformation=new FormInformation();
			inventoryNameToFormInformationMap.put(inventoryName,formInformation);
			if (sqlFile!=null) {
				computeFormInformationFromSQLFile(sqlFile,formInformation);
			}
			
			//System.out.println("functionIds:"+functionIds.size());			
			for (String functionId:functionIds) {
				List<NavigatorNodePath> functionIdNavigatorNodeList=functionIdToFunctionIdNavigatorNodesMap.get(functionId);
				//System.out.println("functionId: "+functionId);
				if (functionIdNavigatorNodeList!=null) {
					//System.out.println("functionIdNavigatorNodeList size:"+functionIdNavigatorNodeList.size());
					
					if (!functionIdNavigatorNodeList.isEmpty()) {
						formInformation.setFormName(functionIdNavigatorNodeList.get(0).getFormName());
					}
					else {
						formInformation.setFormName(UIConstants.UI_NA);
					}
					Set<String> applicationNameSet=formInformation.getApplicationNameSet();
					Set<String> fullPathSet=formInformation.getFullPathSet();

					for (NavigatorNodePath navigatorNodePath:functionIdNavigatorNodeList) {
						List<String> fullPath=navigatorNodePath.getFullPath();
						//System.out.println("fullPath:"+fullPath);
						if (!fullPath.isEmpty()) {
							String value=StringUtils.join(fullPath, " > ");
							fullPathSet.add(value);
							
							String respName=fullPath.get(0);
							String applicationName=respToApplicationMappingMap.get(respName);
							if (applicationName==null || applicationName.isEmpty()) {
								//System.out.println("respName: '"+respName+"'");
								applicationName=UIConstants.UI_NA;
							}
							applicationNameSet.add(applicationName);
						}
					}
				}
			}
		}		
	}
	
	public static MappingsDocument validateMappingsDocument(File xmlFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = InjectUtils.getXmlOptions(validationErrors);
		MappingsDocument mappingsDocument =MappingsDocument.Factory.parse(xmlFile,xmlOptions);
		InjectUtils.validateXMLBeanDocument(mappingsDocument,xmlOptions,validationErrors);
		return mappingsDocument;
	}	

	public static MappingsDocument getMappingDocument(File xmlFile) throws Exception {
		MappingsDocument mappingsDocument =validateMappingsDocument(xmlFile);
		return mappingsDocument;
	}

	private Map<String, String> getRespToApplicationMappingMap(File file) {
		Map<String,String> toReturn=new TreeMap<String,String>();
		try{
			/*
			 * SELECT 'Advanced Planning Administrator',Responsibility_Name,(Select Application_Name From Fnd_Application_Tl T1 Where Language='US' And T1.Application_Id=T.Application_Id) APPLICATION_NAME
			 *  From Fnd_Responsibility_Vl T WHERE UPPER(Responsibility_Name) = UPPER('Advanced Planning Administrator') UNION 
			 */
			MappingsDocument mappingsDocument=getMappingDocument(file);
			Mappings mappings=mappingsDocument.getMappings();
			Mapping[] mappingArray=mappings.getMappingArray();
			for (Mapping mapping:mappingArray) {
				toReturn.put(mapping.getResponsibilityName(),mapping.getApplicationName());
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return toReturn;
		}
		return toReturn;
	}

	public Map<String, List<File>> getInventoryNameToAlternateReverseSQLFilesMap(String oracleRelease) throws Exception {
		validateOracleRelease(oracleRelease);
		return oracleReleaseToInventoryNameToAlternateReverseSQLFilesMap.get(oracleRelease);
	}
	
	public static File getOracleReleaseFolder(String oracleRelease) throws Exception {
		File rootPackageFolder=DEFAULT_PACKAGE_FOLDER;
		try{
			String snapshotOverridePackageFolderPath=Config.getSnapshotOverridePackageFolderPath();
			rootPackageFolder=new File(snapshotOverridePackageFolderPath);
		}
		catch(Throwable e) {
			FileUtils.println(Config.SNAPSHOT_OVERRIDE_PACKAGE_FOLDER+" property not set in engine.properties.");
		}
		//FileUtils.println("Using snapshot package root folder: '"+rootPackageFolder.getAbsolutePath()+"'");
		if (!rootPackageFolder.exists()) {
			throw new Exception("Invalid Package root folder: '"+rootPackageFolder.getAbsolutePath()+"'");
		}
		
		File oracleReleaseFolder=null;
		if (oracleRelease.equals(ORACLE_RELEASE_12_1_2)) {
			oracleReleaseFolder=new File(rootPackageFolder,ORACLE_RELEASE_12_1_3);
		}
		else {
			oracleReleaseFolder=new File(rootPackageFolder,oracleRelease);
		}
		return oracleReleaseFolder;
	}
	
}
