package com.rapidesuite.snapshot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.reverse.ReverseMain;
import com.rapidesuite.snapshot.model.GenericControllerCancellationWorker;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.MainPanel;
import com.rapidesuite.snapshot.view.ServerFrame;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;
import com.rapid4Cloud.snapshotArguments.SnapshotArgumentsDocument;
import com.rapid4Cloud.snapshotLogFile.StatusType;

/*
 * used for this for generating new builds..
 */
public class SnapshotMain extends SwiftGUIMain {
	
	public static final String REGISTRATION_URL = "http://www.rapid4cloud.com/my-account/";
	public static final String SUPPORT_TICKET_R4C_URL = "https://support.rapid4cloud.com/";
	public static final String REGISTRATION_VIDEO_URL = "http://www.rapid4cloud.com/how-to-activate-rapidsnapshot/";
	public static final String SUPPORT_TICKET_SITE_URL = "https://www.rapid4cloud.com/ticket/";
	private boolean isAutomationMode = false;
	private File automationLogFolder;
	private Map<String,String> userMap = new HashMap<String,String>();
	private SnapshotArgumentsDocument snapshotArgumentsDocument;
	private boolean isProcessComplete = false;
	private boolean isTimeLimitExceeded = false;
	private String cachedUser;
	/*
	,  mac.last_updated_by  as rsc_last_updated_by
	,  mac.last_update_date  as rsc_last_update_date
	,  mac.created_by  as rsc_created_by 
	,  mac.creation_date  as rsc_creation_date
	,  mac.ORGANIZATION_ID as rsc_inv_org_id
	,  null as rsc_ou_id
	,  null as rsc_ledger_id
	,  null as rsc_bg_id
	*/
	public static final int INVENTORY_EXTRA_COLUMNS_COUNT=8;
	public static final String SNAPSHOT_ENVIRONMENT_FILE_EXTENSION=".se";
	public static final String SNAPSHOT_PACKAGE_FILE_EXTENSION=".sp";
	public static final String SERVER_CONNECTED_STATUS="Connected";
	
	private JFrame frame;
	private MainPanel mainPanel;
	public static final int FRAME_WIDTH=1730;
	public static final int FRAME_HEIGHT=760;
	private boolean isAlreadyClickedNewSnapshot = false;

	public SnapshotMain() throws Exception {
		super(CoreConstants.SHORT_APPLICATION_NAME.snapshot);
		super.createComponents(false,REGISTRATION_VIDEO_URL,getApplicationIconPath());
		frame=super.getRootFrame();
		ReverseMain.IS_FUSION_DB=false;
		setAutomationMode(false);
		createComponents();
		ModelUtils.createRegistrationWindow(frame,CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString(),true,false,REGISTRATION_VIDEO_URL,SnapshotMain.getSharedApplicationIconPath());
		mainPanel.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().initializeSupportedOracleReleases();
		mainPanel.openSchemaManagementWindowIfNoConnections();
	}
	
	public SnapshotMain(String args[]) throws Exception {
		super(CoreConstants.SHORT_APPLICATION_NAME.snapshot);
		super.createComponents(false,REGISTRATION_VIDEO_URL,getApplicationIconPath());
		frame=super.getRootFrame();
		ReverseMain.IS_FUSION_DB=false;
		setAutomationMode(true);
		createComponents();
		ModelUtils.createRegistrationWindow(frame,CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString(),true,false,REGISTRATION_VIDEO_URL,SnapshotMain.getSharedApplicationIconPath());
		mainPanel.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().initializeSupportedOracleReleases();
		mainPanel.openSchemaManagementWindowIfNoConnections();
		readXMLArgumentFile(args);
		runningInAutomatedModeMain();	
	}
	public void createComponents() throws Exception{
		frame.setIconImage(GUIUtils.getImageIcon(this.getClass(), getApplicationIconPath()).getImage());
		frame.setTitle(UIConstants.FRAME_TITLE_PREFIX+" - "+super.getApplicationVersion());
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setLayout(new BorderLayout());
			
		mainPanel=new MainPanel(this);
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
	}
		
	public static void main(String[] args) throws Exception
	{
		try
		{
			// this needs to be called before creating any components !!!
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("TabbedPane.selected", Color.decode("#047FC0"));
			UIManager.put("ProgressBar.foreground", Color.decode("#047FC0") );
			
			createLogFile(CoreConstants.SHORT_APPLICATION_NAME.snapshot);
			
			if(args.length>0){
				new SnapshotMain(args);
			}else{
				new SnapshotMain();
			}
			
			/*
			String REVERSE_SQL_PATH="D:/RES-REPOSITORY/rapidesuite/controldata/12.1.3/branches/core/v2.0.2.0/snapshot_sql"; 
			com.rapidesuite.snapshot.model.ModelUtils.updateReverseSQLRemoveFormTypeInformation(new File(REVERSE_SQL_PATH));
						
			String REVERSE_SQL_PATH="D:/RES-REPOSITORY/rapidesuite/programs/trunk/client/RAPIDSNAPSHOT/reverse-R12.1.3-KBv2.0.2.0-ERP-Scenarios-396847/"+
					"svn_snapshot_sql"; 
			com.rapidesuite.snapshot.model.ModelUtils.updateReverSQLWithFormTypeInformation(new File(REVERSE_SQL_PATH));
			*/
			
			//String INVENTORIES="D:/RES-REPOSITORY/rapidesuite/programs/2.1.0/client/deploy/snapshot-packages/12.2.4/control/inventories"; 
			//String INVENTORIES="D:/RES-REPOSITORY/rapidesuite/programs/2.1.0/client/deploy/snapshot-packages/12.1.3/control/inventories"; 
			//String INVENTORIES="D:/RES-REPOSITORY/rapidesuite/controldata/12.2.5/trunk/core/knowledgebase/inventory"; 
					
			//File outputFile=new File("inventoriesFormInfo.xml");
			//com.rapidesuite.snapshot.model.ModelUtils.generateXMLFileInventoryNameToFunctionIds(new File(INVENTORIES),outputFile);
			
			//String jdbcString="jdbc:oracle:thin:@oratest73.rapidesuite.com:1521:ERPP"; // R12.2.4
			//String jdbcString="jdbc:oracle:thin:@oratest16:1521:ERPP"; // R12.1.3
			//String jdbcString="jdbc:oracle:thin:@oratest97.rapidesuite.com:1521:ERPP"; // R12.2.5
			//com.rapidesuite.snapshot.model.ModelUtils.generateInventoryFormInformation(new File(INVENTORIES),jdbcString,outputFile);

			/*
			String sourceFolderPath="D:/RES-REPOSITORY/rapidesuite/controldata/12.2.4/branches/core/v2.0.2.0/reverse_sql";
			String targetFolderPath="D:/RES-REPOSITORY/rapidesuite/controldata/12.2.4/branches/core/v2.0.2.0/snapshot_sql";
			com.rapidesuite.snapshot.model.ModelUtils.flattenFolders(new File(sourceFolderPath),new File(targetFolderPath));
			*/
		}
		catch (Throwable t)
		{
			onFatalErrorDuringApplicationInitialization(t);
		}
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
		throw new UnsupportedOperationException();
	}

	@Override
	protected Set<File> getFilesToAttachInTicket() {
		final Set<File> output = new HashSet<File>();

		if (FileUtils.getLogFile() != null) {
			output.add(FileUtils.getLogFile());
		}

		if (SwiftBuildFileUtils.getConsoleLogFile() != null) {
			output.add(SwiftBuildFileUtils.getConsoleLogFile());
		}		

		output.add(new File(UtilsConstants.ENGINE_PROPERTIES_FILE_NAME));
		output.add(new File(UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME));

		return output;
	}

	@Override
	public String getApplicationIconPath() {
		return getSharedApplicationIconPath();
	}

	public static String getSharedApplicationIconPath() {
		return "/images/snapshot/snapshot.png";
	}
	
	@Override
	protected String getRootFrameTitle() {
		String rootFrameTitle = UIConstants.FRAME_TITLE_PREFIX+" - " + this.getApplicationVersion();
		return rootFrameTitle;
	}

	public MainPanel getMainPanel() {
		return mainPanel;
	}
		
	public File getSeededTemplateFolder() {
		File templateFolder=new File("sessions");
		templateFolder.mkdirs();
		
		return templateFolder;
	}
	
	public static void createLogFile(CoreConstants.SHORT_APPLICATION_NAME shortApplicationName) throws IOException {
		File applicationFolder=new File(FileUtils.getUserHomeFolder(),shortApplicationName.toString());
		File logFolder=new File(applicationFolder,"logs");
		FileUtils.deleteDirectory(logFolder);
		logFolder.mkdirs();
		File logFile=new File(logFolder,shortApplicationName.toString() + "-" + SwiftGUIMain.getStartTime() + "-" + 
		FileUtils.LOG_FILE_NAME_PREFIX + FileUtils.LOG_FILE_EXTENSION);
		FileUtils.createLogFile(logFile);
	}
    private void readXMLArgumentFile(String[] args) throws Exception{
    	String filePath = "";
         try { 
			  filePath = args[0].substring(args[0].indexOf("=")+1, args[0].length()).trim();//-arguments_file=C:\Users\warangkana.yoomieng\Downloads\Extract\argumentsFile.xml
			  File argumentsFile = new File(filePath);
			  validateNavigationXML(argumentsFile);
			  snapshotArgumentsDocument= SnapshotArgumentsDocument.Factory.parse(argumentsFile);	
	     } catch (Exception e) {
	    	String errMsg = "Cannot continue to read XML argument file. File path "+filePath+" "+e.getMessage();
	    	FileUtils.printStackTrace(e);
	    	FileUtils.println("ERROR :"+errMsg);
			throw new Exception(errMsg);
	    }		       	
    } 
	private void validateNavigationXML(File argumentsFile) throws Exception {
		List<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
		XmlOptions xmlOptions = InjectUtils.getXmlOptions(validationErrors);
		SnapshotArgumentsDocument snapshotArgumentsDocument= SnapshotArgumentsDocument.Factory.parse(argumentsFile);
		InjectUtils.validateXMLBeanDocument(snapshotArgumentsDocument,xmlOptions,validationErrors);
    } 
	public boolean isAutomationMode() {
		return isAutomationMode;
	}

	public void setAutomationMode(boolean isAutomationMode) {
		this.isAutomationMode = isAutomationMode;
	}
	private void createAutomationLogFolder() throws Exception{
		try{
			File logFolderFile = new File(snapshotArgumentsDocument.getSnapshotArguments().getLogFolderPath());
			FileUtils.println("INFO : create Snapshot automation result folder.");
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
			Date now = new Date();
			String strDate = format.format(now);
			File logFolder=new File(logFolderFile,"rapidsnapshot_"+strDate);
			setAutomationLogFolder(logFolder);
			logFolder.mkdir();				
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Cannot create the automation log folder, error : "+e.getMessage());
		}
		
	}
	private void processCheckIfExecutionTimeExceedTimeLimit() throws Exception{
		int timeLimit = snapshotArgumentsDocument.getSnapshotArguments().getTimeLimit();
		if(timeLimit>0){
			new Thread( new Runnable() {
				@Override
				public void run()  {
					try {
						String logFileName = getAutomationLogFolder().getName()+".xml";
						File logFile = new File(getAutomationLogFolder(),logFileName);
						int timeLimit = 0;
						timeLimit = snapshotArgumentsDocument.getSnapshotArguments().getTimeLimit();
						while(!logFile.exists()){
							Thread.sleep(2000);
						}
						//Date startTime = new Date();
						long startTime=System.currentTimeMillis();
						while(!isProcessComplete()){
							long currentTime = System.currentTimeMillis();
							long diff  = currentTime-startTime;
							long diffMinutes = diff / (60 * 1000); 
							if(diffMinutes>=timeLimit){
								Thread.sleep(5000);
								setTimeLimitExceeded(true);
								break;
								
							}else{
								Thread.sleep(60000);
							}
						}
						if(isTimeLimitExceeded()){
							processActionCancel();
						}
						
					} catch (Exception e) {
						FileUtils.printStackTrace(e); 
						String errMsg = "Internal Error  : "+e.getMessage();
						try{
							processGenericException(errMsg,e);
						}catch(Exception e2){
							FileUtils.printStackTrace(e2); 
						}

					}
				}
			}).start();	
		}
	}
	private void runningInAutomatedModeMain() throws Exception {
		try{
			createAutomationLogFolder();
			//Select Connection Name from List
			applyConnectionAndConnect();
			//Start 'Take Snapshot'
			FileUtils.println("INFO : take a new snapshot.");
			mainPanel.getTabSnapshotsPanel().getSnapshotsActionsPanel().processActionNewSnapshot();
			//Avoid an error for displayInventories
			while(!mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame().isInitialInventoriesSuccessfully()){
				Thread.sleep(3000);
			}
			while(!mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame().
					getSnapshotInventoryDetailsGridPanel().isLoadAllInventorySuccessfully()){
				Thread.sleep(3000);
			}
			applyTemplate();
			//Start Snapshot
			FileUtils.println("INFO : start to take a snapshot");
			String snapshotName = getAutomationLogFolder().getName();
			mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame().processActionTakeSnapshot(snapshotName, snapshotName);	
			processCheckIfExecutionTimeExceedTimeLimit();
		}catch(Exception e){
			String errMsg = "An error occured when trying to run Snapshot automatically, error : "+e.getMessage();	
			processGenericException(errMsg,e);
		}
	}
	
	
	private void applyTemplate() throws Exception{
		String templateSelectionName = snapshotArgumentsDocument.getSnapshotArguments().getTemplateSelectionName();
		FileUtils.println("INFO : apply the template '"+templateSelectionName+"'.");
		try{
			if(templateSelectionName!=null && !"".equals(templateSelectionName)){
				if("ALL".equalsIgnoreCase(templateSelectionName)){
					FileUtils.println("INFO : selected all inventories to take a snapshot.");
				}else{
					mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame().getTemplateSelectionComboBox().setSelectedItem(templateSelectionName);
					String selectedTemplateName = mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame().getTemplateSelectionComboBox().getSelectedItem().toString();
					if(!templateSelectionName.equals(selectedTemplateName)){
						String errMsg = "Cannot apply the template '"+templateSelectionName+"' because it is not found in the list.";
						processGenericException(errMsg,null);
					}else{
						while(!mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame().isApplyTemplateSuccessfully()){
							Thread.sleep(1000);
						}
						Thread.sleep(10000);
					}	
				}			
			}else{
				String errMsg = "Cannot continue taking a snapshot because there is no selected inventory, please check your argument file.";
				processGenericException(errMsg,null);
			}			
		}
		catch(Exception e){
			String errMsg = "Cannot apply the template '"+templateSelectionName+"', error : "+e.getMessage();
			processGenericException(errMsg,e);	
		}
	}
	private void applyConnectionAndConnect() throws Exception{
		try{
			String connectionName = snapshotArgumentsDocument.getSnapshotArguments().getConnectionName();
			FileUtils.println("INFO : apply connection '"+connectionName+"'.");
			if(connectionName!=null){
				mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getServerSelectionComboBox().setSelectedItem(connectionName);
				String selectedConnectionName = mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().getSelectedServerConnection().toString();
				if(!connectionName.equals(selectedConnectionName)){
					String errMsg = "Cannot start any Snapshot process. Connection '"+connectionName+"' is not found in the list.";
					processGenericException(errMsg,null);
				}
				FileUtils.println("INFO : connect to the connection '"+connectionName+"'.");
				mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().processActionServerConnect();
				while(!mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().isConnected()
				      ||!mainPanel.getTabSnapshotsPanel().getServerSelectionPanel().isConnectedAndInstalledPLSQLCompletely()){
					Thread.sleep(3000);
				}				
			}else{
				String errMsg = "Cannot start Snapshot, the connection is not specified.";
				processGenericException(errMsg,null);				
			}
			
		}catch(Exception e){
			String errMsg = "Error occured when trying to select the connection : "+e.getMessage();
			processGenericException(errMsg,e);		
		}
	}
	public void processGenericException(String errMsg,Exception exception) throws Exception{
		if(exception!=null){
			FileUtils.printStackTrace(exception); 
		}
		FileUtils.println("ERROR :"+errMsg);
		writeToAutomationLogFile(StatusType.FAILED,errMsg);
		closeSnapshotAfterLogged();
		throw new Exception(errMsg);		
	}	
	public File getAutomationLogFolder() {
		return automationLogFolder;
	}

	public void setAutomationLogFolder(File automationLogFolder) {
		this.automationLogFolder = automationLogFolder;
	}
	
	public void closeSnapshotAfterLogged(){
		    if(isAutomationMode){
				System.exit(0);	    	
		    }
	} 
	public void close(){
		try{
	    	if ( mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame().getSnapshotCreationController()!=null 
	    			&& !mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame().getSnapshotCreationController().isExecutionCompleted()) {
	    		GUIUtils.popupErrorMessage("You cannot close this window until the Snapshot is completed!");
	    		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    	}else{
	    		System.exit(0);
	    	}			
		}catch(NullPointerException ne){
			FileUtils.println("Close RapidSnapshot, no the creation frame.");
			System.exit(0);
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			System.exit(0);
		}
	}
	public void writeToAutomationLogFile(StatusType.Enum status, String errMsg) throws Exception {
		//FileUtils.println("WRITING LOG FILE....STATUS = "+status+" ERROR MESSAGE = >>"+errMsg+"<<");
		if(isAutomationMode){
			try{
					String logFileName = getAutomationLogFolder().getName()+".xml";
					File logFile = new File(getAutomationLogFolder(),logFileName);
				    Date sysDate = new Date();
				    SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
					StringBuffer content=new StringBuffer("");
					content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					content.append("<snapshotLogFile xmlns=\"http://snapshotLogFile.rapid4cloud.com\" xmlns:xsd=\"http://xsdutility.configurator.erapidsuite.com\">\n");
					content.append("<status>"+status+"</status>\n");
					content.append("<errorMessage>"+errMsg+"</errorMessage>\n"); 
					content.append("<lastUpdatedDate>"+dt.format(sysDate)+"</lastUpdatedDate>\n");
					content.append("</snapshotLogFile>\n");	
					String xmlString =formatXML(content.toString());	
					ModelUtils.writeToFile(logFile, xmlString, false);	
					//FileUtils.println("WRITING LOG FILE SUCCESSFULLY...");							
			}catch(Exception e){			
				FileUtils.println("CANNOT WRITE LOG FILE...."+e.getMessage());
				FileUtils.printStackTrace(e); 
			}
		}
	}
	public String formatXML(String input){
		try 
		{
			Document doc = DocumentHelper.parseText(input);  
			StringWriter sw = new StringWriter();  
			OutputFormat format = OutputFormat.createPrettyPrint();  
			format.setIndent(true);
			format.setIndentSize(3); 
			XMLWriter xw = new XMLWriter(sw, format);  
			xw.write(doc);  

			return sw.toString();
		}
		catch(Exception e){
			FileUtils.printStackTrace(e);
			return input;
		}
	}
	
	protected void processActionCancel() {
		mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame().getSnapshotCreationActionsPanel().getCancelButton().setEnabled(false);
		GenericControllerCancellationWorker swingWorker=new GenericControllerCancellationWorker(mainPanel.getTabSnapshotsPanel()
				.getSnapshotCreationFrame().getSnapshotCreationActionsPanel()
				.getSnapshotCreationGenericFrame().getSnapshotCreationController());
		final int width=450;
		final int height=150;
		UIUtils.displayOperationInProgressModalWindow(mainPanel.getTabSnapshotsPanel().getSnapshotCreationFrame()
				.getSnapshotCreationActionsPanel().getSnapshotCreationGenericFrame()
				,width,height,"Cancel in progress...",swingWorker,SnapshotMain.getSharedApplicationIconPath());	
	}	
	
	
	public SnapshotArgumentsDocument getSnapshotArgumentsDocument() {
		return snapshotArgumentsDocument;
	}

	public boolean isAlreadyClickedNewSnapshot() {
		return isAlreadyClickedNewSnapshot;
	}

	public void setAlreadyClickedNewSnapshot(boolean isAlreadyClickedNewSnapshot) {
		this.isAlreadyClickedNewSnapshot = isAlreadyClickedNewSnapshot;
	}

	public boolean isProcessComplete() {
		return isProcessComplete;
	}

	public void setProcessComplete(boolean isProcessComplete) {
		this.isProcessComplete = isProcessComplete;
	}

	public boolean isTimeLimitExceeded() {
		return isTimeLimitExceeded;
	}

	public void setTimeLimitExceeded(boolean isTimeLimitExceeded) {
		this.isTimeLimitExceeded = isTimeLimitExceeded;
	}

	public String getCachedUser() {
		return cachedUser;
	}

	public void setCachedUser(String cachedUser) {
		this.cachedUser = cachedUser;
	}
	
}
