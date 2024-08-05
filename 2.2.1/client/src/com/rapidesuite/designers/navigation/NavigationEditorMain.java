package com.rapidesuite.designers.navigation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;

import com.erapidsuite.configurator.navigation0005.NavigationDocument;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.NavigationFactory;
import com.rapidesuite.configurator.autoinjectors.engines.FusionEngine;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.configurator.domain.Navigation;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.fusionScripts0000.FusionScriptType;
import com.rapidesuite.core.fusionScripts0000.FusionScriptsDocument;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.ScriptManager;
import com.rapidesuite.snapshot.model.ModelUtils;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;

/*
 * Test on 
Manage Payment Terms
 */
public class NavigationEditorMain extends SwiftGUIMain {
	
	private JFrame frame;
	private NavigationTextEditorPanel navigationTextEditorPanel;
	private NavigationJWebBrowser navigationJWebBrowser;
	private Map<String, Inventory> inventoryNameToInventoryMap;
	private Map<String, String> columnNameToInventoryNameMap;
	private File rootFolder;
	private File inventoriesFolder;
	private File navigationFolder;
	private File dataFolder;
	private int inventoryId;
	private InjectMain injectMain;
	private JLabel savedLabel;
	
	public static final String DEFAULT_TASK_NAME="XXXX";
	public static final String NAVIGATION_NAME="NAVIGATION_NAME_TO_REPLACE";
	public static final String BLOCKS_COMMENT_LINE="<!--INSERT YOUR BLOCKS UNDER THIS LINE-->";
	public static final String COMMANDS_COMMENT_LINE="<!--INSERT YOUR MAIN COMMANDS UNDER THIS LINE-->";
	
	public static final int FRAME_WIDTH=1340;
	public static final int FRAME_HEIGHT=760;
	
	public NavigationEditorMain() throws Exception {
		super(FileUtils.getUserHomeFolder(),CoreConstants.SHORT_APPLICATION_NAME.inject,true);
		super.createComponents(false,null,getApplicationIconPath());
		
		frame=super.getRootFrame();
		createComponents();
		//ModelUtils.createRegistrationWindow(super.rootFrame,CoreConstants.SHORT_APPLICATION_NAME.inject.toString(),false,false,null,NavigationEditorMain.getSharedApplicationIconPath());

		rootFolder=new File("RAPIDDesigner");
		rootFolder.mkdirs();
		inventoriesFolder=new File(rootFolder,"INVENTORY");
		inventoriesFolder.mkdirs();
		navigationFolder=new File(rootFolder,"NAVIGATION");
		navigationFolder.mkdirs();
		dataFolder=new File(rootFolder,"DATA");
		dataFolder.mkdirs();
		
		loadInventories();
		initNavigation();
		navigationTextEditorPanel.listenToChanges();
		//launchBrowser();
	}
	
	private void createComponents() {
		frame.setTitle(getRootFrameTitle());
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setLayout(new BorderLayout());
				
		JPanel mainPanel = new JPanel(new BorderLayout());
		frame.getContentPane().add(mainPanel);

		JPanel northPanel=new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		mainPanel.add(northPanel,BorderLayout.NORTH);
		
		navigationTextEditorPanel=new NavigationTextEditorPanel(this);
		mainPanel.add(navigationTextEditorPanel,BorderLayout.CENTER);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setLayout(new FlowLayout());
		northPanel.add(tempPanel);
		
		JButton saveButton=new JButton("Save");
		tempPanel.add(saveButton);
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveNavigation();	
			}
		});
		
		JButton launchBrowserButton=new JButton("Browser");
		tempPanel.add(launchBrowserButton);
		launchBrowserButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				launchBrowser();	
			}
		});
		
		JButton validateButton=new JButton("Validate");
		tempPanel.add(validateButton);
		validateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				validateNavigationXML();	
			}
		});
		
		JButton injectButton=new JButton("Inject");
		tempPanel.add(injectButton);
		injectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				inject();	
			}
		});
		
		tempPanel=new JPanel();
		tempPanel.setLayout(new FlowLayout());
		northPanel.add(tempPanel);
		savedLabel=new JLabel();
		tempPanel.add(savedLabel);
		
		frame.setVisible(true);
	}
	
	/*
	private void launchAutoSaveThread() {
		Thread t = new Thread() {
		    public void run() {
		    	while (true) {
		    		saveNavigation();
		    		try {
						Thread.sleep(5000);
					}
		    		catch (InterruptedException e) {
		    			FileUtils.printStackTrace(e);
					}
		    	}
		    }
		};
		t.start();
	}
	*/
	
	
	public void close()
	{
		if (injectMain!=null) {
			injectMain.cleanup();
		}
		System.exit(0);
	}

	protected void inject() {
		try{
			saveNavigation();
			
			if (injectMain!=null) {
				injectMain.cleanup();
				injectMain.getRootFrame().setVisible(false);
				injectMain.getRootFrame().dispose();
				ScriptManager.BATCH_ID_RUNNING_NUMBER=1;
				ScriptManager.WORKER_ID_RUNNING_NUMBER=1;
				injectMain=null;
			}
			 
			String labels="any labels";
			String fusionImplProject = "N/A";
			
			File navigationFile=getFirstFileInFolder(navigationFolder);
			NavigationFactory navigationFactory = new NavigationFactory();
			InputStream fileStream=null;
			Navigation navigation=null;
			try{
				fileStream=new FileInputStream(navigationFile);
				navigation = navigationFactory.parseNavigation(fileStream);
				if (navigation==null) {
					throw new Exception("Cannot parse Navigation file: '"+navigationFile.getAbsolutePath()+"'");
				}
			}
			finally {
				IOUtils.closeQuietly(fileStream);
			}
			
			final File generationFolder =new File("DESIGNER-TEST-IP");
			generationFolder.mkdirs();
			String userName="RAPIDDesigner automated test";
			int scriptFolderId=0;
			Properties specProps = new Properties();
			boolean isProfileGeneration = false;
						
			ClientFusionEngine clientFusionEngine = new ClientFusionEngine(labels,navigation,inventoryNameToInventoryMap,dataFolder);
			clientFusionEngine.process(generationFolder,scriptFolderId,navigation,inventoryNameToInventoryMap);
			FusionScriptsDocument fusionScriptsDocument=FusionScriptsDocument.Factory.newInstance();
			FusionScriptType fusionScriptType=fusionScriptsDocument.addNewFusionScripts();
			fusionScriptType.setImplementationProject(fusionImplProject);			
			FusionEngine.generateFusionScriptType(navigation,fusionScriptType,0);
			FusionEngine.generateScriptsFile(fusionScriptsDocument,generationFolder);
			FusionEngine.generateInjectionPackageInformation(generationFolder,isProfileGeneration,specProps,fusionImplProject,labels,userName);
			
			File tempInjectionPackageFile = FusionEngine.getPackage(generationFolder,false, generationFolder);
			File injectionPackageFile = new File(generationFolder,"test.ip");
			injectionPackageFile.delete();
			tempInjectionPackageFile.renameTo(injectionPackageFile);
			FileUtils.deleteDirectory(new File(generationFolder,"fusionArchive"));
			
			String[] arguments=new String[1];
			arguments[0]=injectionPackageFile.getAbsolutePath();
			injectMain=new InjectMain(arguments);
		}
		catch(Throwable e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}		
	}

	protected void validateNavigationXML() {
		try{
			List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
			XmlOptions xmlOptions = InjectUtils.getXmlOptions(validationErrors);
			NavigationDocument fusionNavigationDocument =NavigationDocument.Factory.parse(
					navigationTextEditorPanel.getTextArea().getText(),xmlOptions);
			InjectUtils.validateXMLBeanDocument(fusionNavigationDocument,xmlOptions,validationErrors);
			GUIUtils.popupInformationMessage("Navigation is valid!");
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}

	protected void launchBrowser(){
		try{
			if (navigationJWebBrowser!=null) {
				navigationJWebBrowser.setVisible(false);
				navigationJWebBrowser.dispose();
			}

			Navigation navigation= parseNavigation();
			if (navigation==null) {
				return;
			}
			navigationJWebBrowser=new NavigationJWebBrowser(this,navigation);
			navigationJWebBrowser.setVisible(true);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
		UIUtils.setPreferredLookAndFeel();
		NativeInterface.open();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new NavigationEditorMain();
				} catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
			}
		});
		NativeInterface.runEventPump();
	}

	@Override
	public void showUpdatesFrame() {
	}

	@Override
	public void initEnvironment() throws Exception {
	}

	@Override
	public void initExecutionPanel() throws Exception {
	}

	@Override
	public Map<String, String> getEnvironmentPropertiesMap() {
		return null;
	}

	@Override
	protected void runAutomatically() {	
	}

	@Override
	protected Set<File> getFilesToAttachInTicket() {
		return null;
	}

	public String getApplicationIconPath() {
		return getSharedApplicationIconPath();
	}

	public static String getSharedApplicationIconPath() {
		return "/images/inject/inject.png";
	}

	@Override
	protected String getRootFrameTitle() {
		return "RAPIDDesigner - Navigation Editor";
	}

	public NavigationTextEditorPanel getNavigationTextEditorPanel() {
		return navigationTextEditorPanel;
	}

	public void loadInventories() throws Exception {
		inventoryNameToInventoryMap=loadInventoriesGeneric(inventoriesFolder);
	}
	
	private Map<String,Inventory> loadInventoriesGeneric(File folder) throws Exception {
		File[] files=folder.listFiles();
		Map<String,Inventory> toReturn=new TreeMap<String,Inventory>(String.CASE_INSENSITIVE_ORDER);
		if (files==null) {
			return toReturn;
		}
		for ( File file : folder.listFiles())
		{
			if(file.isDirectory())
			{
				Map<String, Inventory> map=loadInventoriesGeneric(file);
				toReturn.putAll(map);
			}
			else
			{
				String inventoryName=file.getName().replace(".xml","");
				Inventory inventory=FileUtils.getInventory(file,inventoryName);
				inventory.setId(inventoryId++);
				toReturn.put(inventoryName,inventory);
				
				// store all the columns and which inventory they belong to.
				// we will use this to auto-populate the templates with valueKB by matchin
				// the labels with those column names.
				// Note that if the column name is used in multiple inventory then it is not considered.
				columnNameToInventoryNameMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
				for (String fieldName:inventory.getFieldNamesUsedForDataEntry()) {
					String tempInventoryName=columnNameToInventoryNameMap.get(fieldName);
					if (tempInventoryName==null) {
						columnNameToInventoryNameMap.put(fieldName,inventory.getName());
					}
				}
				
			}			
		}
		return toReturn;
	}

	public Map<String, Inventory> getInventoryNameToInventoryMap() {
		return inventoryNameToInventoryMap;
	}
		
	public void saveNavigation() {
		try{
			navigationTextEditorPanel.executeIndent();
			String navigationXML=navigationTextEditorPanel.getTextArea().getText();
			Navigation navigation= parseNavigation();
			if (navigation==null) {
				return;
			}
			String navigationName=navigation.getName();
			
			File navigationFile=getFirstFileInFolder(navigationFolder);
			if (navigationFile!=null) {
				boolean isDeleted=navigationFile.delete();
				if (!isDeleted) {
					throw new Exception("Unable to save navigation, file is opened by another process: '"+
							navigationFile.getAbsolutePath()+"'");
				}
			}
			navigationFile=new File(navigationFolder,navigationName+".xml");
			ModelUtils.writeToFile(navigationFile,navigationXML,false); 
		
			savedLabel.setText(new java.util.Date()+" : Navigation saved!");
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}

	protected Navigation parseNavigation() throws Exception {
		try{
			String navigationXML=navigationTextEditorPanel.getTextArea().getText();
			InputStream stream = new ByteArrayInputStream(navigationXML.getBytes(StandardCharsets.UTF_8));
			NavigationFactory navigationFactory = new NavigationFactory();
			Navigation navigation= navigationFactory.parseNavigation(stream);
			stream.close();
			return navigation;
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: the navigation XML is invalid, please check the log file for more details");
		}
		return null;
	}

	private void initNavigation() throws Exception {
		File navigationFile=getFirstFileInFolder(navigationFolder);
		if (navigationFile!=null){
			String content=ModelUtils.readFile(navigationFile);
			navigationTextEditorPanel.getTextArea().insert(content, 0);
		}
		else {
			StringBuffer header=new StringBuffer("");
			header.append("<?xml version=\"1.0\"?>\n");
			header.append("<navigation xmlns=\"http://navigation0005.configurator.erapidsuite.com\" xmlns:xsd=\"http://xsdutility.configurator.erapidsuite.com\">\n");
			header.append("<name>"+NAVIGATION_NAME+"</name>\n");
			header.append("<svn>\n");
			header.append("<xsd:revision>$Revision: $:</xsd:revision>\n");
			header.append("<xsd:author>$Author: $:</xsd:author>\n");
			header.append("<xsd:date>$Date: $:</xsd:date>\n");
			header.append("<xsd:headURL>$HeadURL: $:</xsd:headURL>\n");
			header.append("<xsd:id>$Id:  $:</xsd:id>\n");
			header.append("</svn>\n");
			header.append("<description/>\n");
			header.append("<type>FUSION</type>\n");
			header.append("<engine_type>SELENIUM</engine_type>\n");
			header.append("<modules/>\n");
			header.append("<mapper_use_type>UNDEFINED</mapper_use_type>\n");
			header.append("<validate_parent_child_relationships_for_mapped_data>true</validate_parent_child_relationships_for_mapped_data>\n");
			header.append("<fusionNavigation hasScope=\"false\" taskName=\""+DEFAULT_TASK_NAME+"\" isBatchingAllowed=\"true\" applicationKey=\"APPLICATION_HOME_PAGE\">\n");

			header.append(BLOCKS_COMMENT_LINE).append("\n\n");
			header.append(COMMANDS_COMMENT_LINE).append("\n\n");

			InventoryHierarchy rootInventoryHierarchy=buildInventoryHierarchy();
			header.append(rootInventoryHierarchy.getRepeatXMLTags()).append("\n\n");

			header.append("</fusionNavigation>\n</navigation>");
			navigationTextEditorPanel.getTextArea().insert(header.toString(), 0);

			navigationTextEditorPanel.executeIndent();
			navigationTextEditorPanel.setCaretAfterText(COMMANDS_COMMENT_LINE);
			navigationTextEditorPanel.getTextArea().insert("\n", navigationTextEditorPanel.getTextArea().getCaretPosition());
			saveNavigation();
		}
	}
	
	private InventoryHierarchy buildInventoryHierarchy() throws Exception {
		Iterator<String> iterator=getInventoryNameToInventoryMap().keySet().iterator();
		InventoryHierarchy rootInventoryHierarchy=null;
		Map<String,InventoryHierarchy> inventoryNameToInventoryHierarchyMap=new HashMap<String,InventoryHierarchy>();
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			Inventory inventory=getInventoryNameToInventoryMap().get(inventoryName);
			InventoryHierarchy inventoryHierarchy=new InventoryHierarchy();
			inventoryHierarchy.setInventory(inventory);
			inventoryNameToInventoryHierarchyMap.put(inventoryName, inventoryHierarchy);
			if (inventory.getParentName()==null || inventory.getParentName().isEmpty() ) {
				rootInventoryHierarchy=inventoryHierarchy;
			}
		}
		iterator=getInventoryNameToInventoryMap().keySet().iterator();
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			Inventory inventory=getInventoryNameToInventoryMap().get(inventoryName);
			InventoryHierarchy inventoryHierarchy=inventoryNameToInventoryHierarchyMap.get(inventoryName);
			
			if (inventory.getParentName()!=null && !inventory.getParentName().isEmpty() ) {
				InventoryHierarchy parentInventoryHierarchy=inventoryNameToInventoryHierarchyMap.get(inventory.getParentName());
				if (parentInventoryHierarchy==null) {
					throw new Exception("Cannot find parent inventory: '"+inventory.getParentName()+"' for the inventory: '"+inventoryName+"'");
				}
				List<InventoryHierarchy> children=parentInventoryHierarchy.getChildren();
				children.add(inventoryHierarchy);
			}
		}
		return rootInventoryHierarchy;
	}

	public JLabel getSavedLabel() {
		return savedLabel;
	}
	
	public File getFirstFileInFolder(File folder) {
		File[] files=folder.listFiles();
		if (files==null || files.length==0) {
			return null;
		}
		return files[0];
	}

}
