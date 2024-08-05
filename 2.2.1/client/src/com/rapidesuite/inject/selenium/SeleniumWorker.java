package com.rapidesuite.inject.selenium;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.util.EntityUtils;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.erapidsuite.configurator.navigation0005.BlockType;
import com.erapidsuite.configurator.navigation0005.ClickType;
import com.erapidsuite.configurator.navigation0005.DefineVariableType;
import com.erapidsuite.configurator.navigation0005.DependenciesType;
import com.erapidsuite.configurator.navigation0005.ErrorType;
import com.erapidsuite.configurator.navigation0005.ExecuteBlockType;
import com.erapidsuite.configurator.navigation0005.ExtractType;
import com.erapidsuite.configurator.navigation0005.FindElementType;
import com.erapidsuite.configurator.navigation0005.FindElementsType;
import com.erapidsuite.configurator.navigation0005.Navigation;
import com.erapidsuite.configurator.navigation0005.NavigationCallType;
import com.erapidsuite.configurator.navigation0005.NavigationDocument;
import com.erapidsuite.configurator.navigation0005.FusionNavigationType;
import com.erapidsuite.configurator.navigation0005.IfThenElseType;
import com.erapidsuite.configurator.navigation0005.ParameterType;
import com.erapidsuite.configurator.navigation0005.ParametersType;
import com.erapidsuite.configurator.navigation0005.PauseType;
import com.erapidsuite.configurator.navigation0005.PostURLType;
import com.erapidsuite.configurator.navigation0005.RepeatType;
import com.erapidsuite.configurator.navigation0005.ScrollType;
import com.erapidsuite.configurator.navigation0005.SelectType;
import com.erapidsuite.configurator.navigation0005.SendKeysType;
import com.erapidsuite.configurator.navigation0005.SetVariableType;
import com.erapidsuite.configurator.navigation0005.TemplateClickType;
import com.erapidsuite.configurator.navigation0005.TemplateEndSectionType;
import com.erapidsuite.configurator.navigation0005.TemplateGridFindRowType;
import com.erapidsuite.configurator.navigation0005.TemplateGridSearchBarType;
import com.erapidsuite.configurator.navigation0005.TemplateInputType;
import com.erapidsuite.configurator.navigation0005.TemplateLOVType;
import com.erapidsuite.configurator.navigation0005.TemplateRadioButtonType;
import com.erapidsuite.configurator.navigation0005.TemplateSelectType;
import com.erapidsuite.configurator.navigation0005.TemplateStartSectionType;
import com.erapidsuite.configurator.navigation0005.TemplateTextAreaType;
import com.erapidsuite.configurator.navigation0005.WaitType;
import com.erapidsuite.configurator.navigation0005.BreakType;
import com.erapidsuite.configurator.navigation0005.WindowType;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.BatchInjectionTracker;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.RecordTracker;
import com.rapidesuite.inject.ScriptGridTracker;
import com.rapidesuite.inject.ScriptManager;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.commands.BlockTypeCommand;
import com.rapidesuite.inject.commands.ExecuteBlockTypeCommand;
import com.rapidesuite.inject.commands.IfThenElseTypeCommand;
import com.rapidesuite.inject.commands.RepeatTypeCommand;
import com.rapidesuite.inject.gui.ScriptsGrid;
import com.rapidesuite.inject.selenium.commands.ClickTypeCommand;
import com.rapidesuite.inject.selenium.commands.DefineVariableTypeCommand;
import com.rapidesuite.inject.selenium.commands.ErrorTypeCommand;
import com.rapidesuite.inject.selenium.commands.ExtractTypeCommand;
import com.rapidesuite.inject.selenium.commands.FindElementTypeCommand;
import com.rapidesuite.inject.selenium.commands.FindElementsTypeCommand;
import com.rapidesuite.inject.selenium.commands.ParameterTypeCommand;
import com.rapidesuite.inject.selenium.commands.PauseTypeCommand;
import com.rapidesuite.inject.selenium.commands.PostURLTypeCommand;
import com.rapidesuite.inject.selenium.commands.ScrollTypeCommand;
import com.rapidesuite.inject.selenium.commands.SelectTypeCommand;
import com.rapidesuite.inject.selenium.commands.SendKeysTypeCommand;
import com.rapidesuite.inject.selenium.commands.SetVariableTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateClickTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateEndSectionTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateGridFindRowTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateGridSearchBarTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateInputTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateLOVTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateRadioButtonTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateSelectTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateStartSectionTypeCommand;
import com.rapidesuite.inject.selenium.commands.TemplateTextAreaTypeCommand;
import com.rapidesuite.inject.selenium.commands.WaitTypeCommand;
import com.rapidesuite.inject.selenium.commands.WindowTypeCommand;

public class SeleniumWorker extends Worker{

	private WebDriver webDriver;
	private WebDriverWait webDriverWait;
	private FusionNavigationType fusionCurrentScriptNavigationType;

	private boolean isWebDriverQuit;
	private List<WebElement> currentWebElements;
	private WebElement currentWebElement;

	private boolean isBreakDetected;
	private Map<String,Object> variableNameToValueMap;
	private Map<String,DefineVariableType.Type.Enum> variableNameToTypeMap;
	private String vncDisplayName;
	private Stack<String> windowHandlesStack;
	private Map<String, String> parameterNameToValueMap;
	private String taskName;
	private boolean isRelativeWebElementReferenceInUse;
	private String relativeElementXpath;
	private boolean isNodeKillTimerAlreadyCancelled;
	
	public final static String BROWSER_PREFIX="WORKER: ";
	public final static int BLOCK_REPEATER_TIMEOUT_RETRIES=300;
	public final static String WILDCARD_KEYWORD="##REGEX_WILDCARD##";


	public SeleniumWorker(BatchInjectionTracker batchInjectionTracker,int workerId){
		super(batchInjectionTracker,workerId);
		variableNameToValueMap=new HashMap<String,Object>();
		variableNameToTypeMap=new HashMap<String,DefineVariableType.Type.Enum>();
		windowHandlesStack=new Stack<String>();
		parameterNameToValueMap=new HashMap<String,String>();
	}

	public boolean isWebDriverQuit() {
		return isWebDriverQuit;
	}

	public void quitWebDriver() {
		if (webDriver!=null) {
			if (!isWebDriverQuit) {
				println("##############################");
				try{
					webDriver.quit();
					println("WORKER web driver closed!");
				}
				catch(org.openqa.selenium.remote.UnreachableBrowserException e) {
					println("WORKER web driver not properly closed due to the error below!");
					printStackTrace(e);
				}				
				isWebDriverQuit=true;
				println("##############################");
			}
		}
	}

	public void stopExecution(boolean isQuitBrowser) throws InterruptedException {
		isStopped=true;
		batchInjectionTracker.setCompleted(true);
		if (isQuitBrowser) {
			try{
				quitWebDriver();
			}
			catch(Exception e) {
				FileUtils.printStackTrace(e);
				isWebDriverQuit=true;
			}
		}
	}
	
	public WebDriver getWebDriver() {
		return webDriver;
	}

	public WebDriverWait getWebDriverWait() {
		return webDriverWait;
	}

	public FusionNavigationType getFusionNavigationType() {
		return fusionCurrentScriptNavigationType;
	}	

	public void startExecution(){
		FileUtils.println("startExecution: '"+batchInjectionTracker.getScriptGridTracker().getDisplayName()+"'");
		try{			
			batchInjectionTracker.setStarted(true);

			if (batchInjectionTracker.getScriptGridTracker().getStatus().equals(ScriptsGrid.STATUS_PENDING)) {
				batchInjectionTracker.getScriptGridTracker().setRemarks("");
				batchInjectionTracker.getScriptGridTracker().setStatus(ScriptsGrid.STATUS_PROCESSING);
			}

			println("#################################################");
			println("#################################################");
			println("####### LOG FOR WORKER: "+workerId+" ############");
			println("#################################################");
			println("#################################################");

			/*
			 * We need to set the first record of the root table to processing otherwise it is confusing
			 * to the end user when there is a failure before the script reaches the first repeat.
			 * 
			 */
			List<Inventory> rootInventories=InjectUtils.getRootInventories(batchInjectionTracker.getScriptGridTracker().getInjectMain(),
					batchInjectionTracker.getScriptGridTracker().getInjectMain().getApplicationInfoPanel().getInjectionPackage(),
					batchInjectionTracker.getScriptGridTracker().getScript());
			if (rootInventories.size()==1) {
				// only support single root navigations for now
				Inventory rootInventory=rootInventories.get(0);
				batchInjectionTracker.updateFirstRecordStatusAndRemarks(ScriptsGrid.STATUS_QUEUED,rootInventory,"waiting for worker");
			}
			/*
			 *
			 * http://toolsqa.com/selenium-webdriver/custom-firefox-profile/
			 * https://groups.google.com/forum/#!msg/selenium-users/a2fNfF-mD_E/xk7x4IaICSoJ
			 *
			 Operating System 					Profile Folder Path
			 	Windows XP / 2000 / Vista / 7 	%AppData%\Mozilla\Firefox\Profilesxxxxxxxx.default
				Linux 							~/.mozilla/firefox/xxxxxxxx.default/
				Mac OS X 						~/Library/Application Support/Firefox/Profiles/xxxxxxxx.default/
			 */
			
			FirefoxProfile profile =null;
			String injectFirefoxProfileName=Config.getInjectFirefoxProfileName();
			println("Creating Firefox profile, injectFirefoxProfileName: '"+injectFirefoxProfileName+"'");
			if (injectFirefoxProfileName==null || injectFirefoxProfileName.trim().isEmpty()) {
				profile = new FirefoxProfile();
				println("NEW Firefox profile created!");
			}
			else {
				ProfilesIni profilesIni = new ProfilesIni();
				profile = profilesIni.getProfile(injectFirefoxProfileName);
				println("Using Firefox profile name: '"+injectFirefoxProfileName+"'");
				if (profile==null) {
					throw new Exception("Unable to find the Firefox profile name: '"+injectFirefoxProfileName+"'");
				}
			}
			
			profile.setPreference("focusmanager.testmode", true);
			//profile.setPreference("webdriver.load.strategy", "unstable");
			
			boolean isServerMode=batchInjectionTracker.getScriptGridTracker().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isServerMode();
			if (isServerMode) {
				DesiredCapabilities capabilities = DesiredCapabilities.firefox();
				capabilities.setCapability(FirefoxDriver.PROFILE, profile);
				URL url = new URL("http://"+batchInjectionTracker.getScriptGridTracker().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getHubID()+"/wd/hub");

				println("Waiting for a RemoteWebDriver to be created...");
				webDriver=SeleniumUtils.getRemoteWebDriverFactory(url,capabilities);
				println("RemoteWebDriver created!");
				// Query the driver to find out more information
				//Capabilities actualCapabilities = ((RemoteWebDriver) webDriver).getCapabilities();
				//actualCapabilities.

				//https://github.com/nicegraham/selenium-grid2-api
				//http://stackoverflow.com/questions/7190362/remotewebdriver-and-grid-is-it-possible-to-get-the-server-ip

				String serverId=batchInjectionTracker.getScriptGridTracker().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getHubID();
				StringTokenizer st = new StringTokenizer(serverId, ":");
				String hostName = st.nextToken();
				String portStr = st.nextToken();
				int port=Integer.valueOf(portStr);

				HttpHost host = new HttpHost(hostName,port); 
				//DefaultHttpClient client = new DefaultHttpClient();
				HttpClient client = HttpClientBuilder.create().build();
				URL testSessionApi = new URL("http://"+batchInjectionTracker.getScriptGridTracker().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getHubID()+
						"/grid/api/testsession?session=" +  ((RemoteWebDriver) webDriver).getSessionId()); 
				BasicHttpEntityEnclosingRequest r = new 
						BasicHttpEntityEnclosingRequest("POST", testSessionApi.toExternalForm()); 
				HttpResponse response  = client.execute(host,r);
				JSONObject object = new JSONObject(EntityUtils.toString(response.getEntity()));   
				vncDisplayName =(String) object.get("proxyId");

				batchInjectionTracker.updateVNCDisplayName(vncDisplayName);

				//TODO: call the server again to get the websockifyport NO JUST PUT THE NODE ID AS WE DON'T KNOW THE VNC PASSWORD!!!
				// END-USER WILL NEED TO OPEN THE CONTROLLER.

				println("SERVER MODE ENABLED, starting a remote webDriver: "+webDriver);
				if (isStopped) {
					quitWebDriver();
					return;
				}
			}
			else {
				String firefoxPath=Config.getInjectFirefoxPath();
				// This may take 10 seconds to start up so we need to know if stop Execution was called prior.
				//firefoxPath="D:/FirefoxPortable/FirefoxPortable.exe";
				if (firefoxPath!=null) {
					System.setProperty("webdriver.firefox.bin", firefoxPath);
				}
				boolean isFocusMode=Config.getInjectFocusMode();
				if (isFocusMode) {
					println("STARTING FIREFOX");
					//ProfilesIni profileIni = new ProfilesIni();
					//profile = profileIni.getProfile("default");
					//File file = new File("firebug-1.9.2.xpi");
					//profile.addExtension(file);
					//profile.setPreference("extensions.firebug.currentVersion", "1.9.2");
					webDriver = new FirefoxDriver(profile);
					println("FIREFOX STARTED");
				}
				else {
					webDriver = new FirefoxDriver();
				}
			}

			if (rootInventories.size()==1) {
				// only support single root navigations for now
				Inventory rootInventory=rootInventories.get(0);
				batchInjectionTracker.updateFirstRecordStatusAndRemarks(ScriptsGrid.STATUS_PROCESSING,rootInventory,"Processing Batch");
			}
			
			int pageLoadTimeout=Config.getInjectPageLoadTimeout();
			if (pageLoadTimeout>0) {
				println("SETTING PAGE LOAD TIMEOUT TO "+pageLoadTimeout+" SECONDS.");
				webDriver.manage().timeouts().pageLoadTimeout(pageLoadTimeout,TimeUnit.SECONDS);
			}
			//webDriver.manage().timeouts().setScriptTimeout(5,TimeUnit.MILLISECONDS);
			println("FIREFOX WINDOW MAXIMIZE");
			webDriver.manage().window().maximize();
			String applicationKey=getBatchInjectionTracker().getScriptGridTracker().getScript().getApplicationKey();
			String url=batchInjectionTracker.getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getApplicationInfoPanel().getUrl(applicationKey); 

			boolean isHomePageTimerActivationOn=Config.getInjectHomePageTimerActivation();
			if (isServerMode && isHomePageTimerActivationOn) {
				println("Starting KillNode Timer request to Hub to monitor Locking on HomePage.");
				HubRequestsManager.startKillNodeTimer(batchInjectionTracker.getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().
						getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getHubID(),vncDisplayName);
				println("Request sent.");
			}
			webDriver.get(url);

			long sleepTimeInSecs=batchInjectionTracker.getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getTimeoutInMins()*60;
			webDriverWait = new WebDriverWait(webDriver,sleepTimeInSecs);

			List<XmlObject> xmlObjectsList=new ArrayList<XmlObject>();
			blockNameToCommandsMap=new HashMap<String,List<XmlObject>>();
			
			/*
			 * Adding all the blocks defined in routines
			 */
			List<BlockType> routinesBlockList=getScriptManager().getRoutinesBlockList();
			for (BlockType blockType:routinesBlockList) {
				BlockTypeCommand blockTypeCommand=new BlockTypeCommand(this);
				blockTypeCommand.process(blockType);
			}

			File preStepsFolder=new File("fusion"+File.separator+applicationKey);
			if (!preStepsFolder.exists()) {
				String applicationName=batchInjectionTracker.getScriptGridTracker().getInjectMain().getApplicationInfoPanel().getApplicationKeysToNamesMap().get(applicationKey);
				throw new Exception("This application name is not supported. (application name: '"+applicationName+"')");
			}
					
			boolean isRedirectAfterLogin=batchInjectionTracker.getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsGeneralPanel().isRedirectAfterLogin();
			
			File fusionLoginNavigationFile=null;
			if (isRedirectAfterLogin) {
				fusionLoginNavigationFile=new File(preStepsFolder+File.separator+ScriptManager.LOGIN_NAVIGATION_FILE_NAME+ScriptManager.INIT_NAVIGATIONS_FILE_EXTENSION);
			}
			else {
				fusionLoginNavigationFile=new File(preStepsFolder+File.separator+ScriptManager.LOGIN_NAVIGATION_FILE_NAME+ScriptManager.INIT_NAVIGATIONS_FILE_NAME_SUFFIX+ScriptManager.INIT_NAVIGATIONS_FILE_EXTENSION);
			}
			//System.out.println("fusionLoginNavigationFile:"+fusionLoginNavigationFile.getAbsolutePath());
			
			NavigationDocument fusionLoginNavigationDocument=InjectUtils.getFusionNavigationDocument(fusionLoginNavigationFile);
			List<XmlObject> xmlObjectsListTemp=InjectUtils.parseFusionNavigationDocument(fusionLoginNavigationDocument.getNavigation());
			xmlObjectsList.addAll(xmlObjectsListTemp);

			Navigation navigation=getNavigation(batchInjectionTracker.getScriptGridTracker());
			fusionCurrentScriptNavigationType=navigation.getFusionNavigation();
			 
			NavigationCallType navigationCallType=fusionCurrentScriptNavigationType.getNavigationCall();
			
			// We need to store it otherwise we will use the wrong task name if the navigation is a sub-routine.
			taskName=fusionCurrentScriptNavigationType.getTaskName();
			if (navigationCallType!=null) {
				String navigationNameToCall=navigationCallType.getName();
				println("Navigation to Call detected: '"+navigationNameToCall+"'");
				/*
				 * overriding the navigation with the one to call
				 */
				NavigationDocument navigationDocumentTemp=SeleniumUtils.getNavigationDocument(batchInjectionTracker,navigationNameToCall);
				
				ParametersType parametersType=fusionCurrentScriptNavigationType.getParameters();
				if (parametersType!=null) {
					ParameterType[] parametersArray=parametersType.getParameterArray();
					ParameterTypeCommand parameterTypeCommand=new ParameterTypeCommand(this);
					parameterTypeCommand.process(parametersArray,false);
				}
				navigation=navigationDocumentTemp.getNavigation();
				fusionCurrentScriptNavigationType=navigation.getFusionNavigation();
			}
						
			RepeatType[] repeatTypeArray=fusionCurrentScriptNavigationType.getRepeatArray();
			if (repeatTypeArray==null || repeatTypeArray.length==0) {
				throw new Exception("Invalid navigation, it must contain at least one repeating group.");
			}
			boolean isIgnoreTaskSearchNavigation=fusionCurrentScriptNavigationType.getIsIgnoreTaskSearchNavigation();
			if (!isIgnoreTaskSearchNavigation) {
				File fusionTaskSelectionNavigationFile=null;

				if ( fusionCurrentScriptNavigationType.getHasScope()) {
					if (isRedirectAfterLogin) {
						fusionTaskSelectionNavigationFile=new File(preStepsFolder+File.separator+ScriptManager.TASK_VIA_PROJECT_NAVIGATION_FILE_NAME+ScriptManager.INIT_NAVIGATIONS_FILE_EXTENSION);
					}
					else {
						fusionTaskSelectionNavigationFile=new File(preStepsFolder+File.separator+ScriptManager.TASK_VIA_PROJECT_NAVIGATION_FILE_NAME+ScriptManager.INIT_NAVIGATIONS_FILE_NAME_SUFFIX+ScriptManager.INIT_NAVIGATIONS_FILE_EXTENSION);
					}				
				}
				else {
					if (isRedirectAfterLogin) {
						fusionTaskSelectionNavigationFile=new File(preStepsFolder+File.separator+ScriptManager.TASK_VIA_DIRECT_SEARCH_NAVIGATION_FILE_NAME+ScriptManager.INIT_NAVIGATIONS_FILE_EXTENSION);
					}
					else {
						fusionTaskSelectionNavigationFile=new File(preStepsFolder+File.separator+ScriptManager.TASK_VIA_DIRECT_SEARCH_NAVIGATION_FILE_NAME+ScriptManager.INIT_NAVIGATIONS_FILE_NAME_SUFFIX+ScriptManager.INIT_NAVIGATIONS_FILE_EXTENSION);
					}				
				}
				//System.out.println("fusionTaskSelectionNavigationFile:"+fusionTaskSelectionNavigationFile.getAbsolutePath());

				if (fusionTaskSelectionNavigationFile.exists()) {
					NavigationDocument fusionTaskSelectionNavigationDocument=InjectUtils.getFusionNavigationDocument(fusionTaskSelectionNavigationFile);
					xmlObjectsListTemp=InjectUtils.parseFusionNavigationDocument(fusionTaskSelectionNavigationDocument.getNavigation());
					xmlObjectsList.addAll(xmlObjectsListTemp);
				}
			}
			else {
				println("IGNORING TASK SEARCH NAVIGATION AS PER THE ATTRIBUTE SET 'isIgnoreTaskSearchNavigation' ");
			}
						
			xmlObjectsListTemp=InjectUtils.parseFusionNavigationDocument(navigation);
			xmlObjectsList.addAll(xmlObjectsListTemp);

			processNavigationXMLObjects(xmlObjectsList);

			println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			println("!!!!!! WORKER COMPLETED WITHOUT ERRORS !!!!!!");
			println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		catch(Throwable exception) {
			batchInjectionTracker.setError(true);
			if (webDriver==null) {
				isWebDriverQuit=true;
			}
			printStackTrace(exception);			
			updateAllRemainingRecordsToFailed("Batch failed! This record has been stopped.");
			
			SeleniumUtils.changeBrowserTitle(webDriver,batchInjectionTracker.getScriptGridTracker().getGridIndex()+1,this);
			String template=
					"There was a problem with the injection - please recheck that the error is not DATA related then if not to open a support request with Rapid HelpDesk!<br/>"+
							"You can open a ticket by clicking on the 'RAPID' button in the main window then 'Open ticket'.<br/>"+
							"If any issues, please manually provide the Screenshot and Debug files located at: <br/>"+
							workerLogFolder.getAbsolutePath()+"<br/><br/>";

			String details="";
			int indexOf=-1;
			String errorMsg="";
			if (exception.getMessage()!=null) {
				indexOf=exception.getMessage().indexOf("Command duration or timeout:");
				if (indexOf!=-1) {
					errorMsg=exception.getMessage().substring(0,indexOf);
				}
				else {
					errorMsg=exception.getMessage();
				}
			}
			details+=errorMsg;
			File file=new File(workerLogFolder,"screenshotWorker"+workerId+".png");
			try {
				SeleniumUtils.captureScreenShot(webDriver,file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			SeleniumUtils.createErrorPopup(webDriver,template,details);
		}
		finally {
			/*
			 * COMPLETED: means either the worker has processed all the records without errors or it stops because of an error
			 */
			batchInjectionTracker.setCompleted(true);
			batchInjectionTracker.getScriptGridTracker().incrementCompletedBatchCount();
			batchInjectionTracker.getScriptGridTracker().getInjectMain().getScriptManager().unreserveWorker(batchInjectionTracker);
			if (!batchInjectionTracker.isError()) {
				quitWebDriver();
			}
		}
	}
		
	private Navigation getNavigation(ScriptGridTracker scriptGridTracker) throws Exception {
		boolean isReuseNavigation=Config.getInjectIsReuseNavigation();
		Navigation navigation=null;
		if (isReuseNavigation) {
			 navigation=scriptGridTracker.getNavigation();
		}
		else {
			NavigationDocument fusionCurrentScriptNavigationDocument=InjectUtils.getFusionNavigationDocument(
					scriptGridTracker.getInjectMain(),
					scriptGridTracker.getInjectMain().getApplicationInfoPanel().getInjectionPackage(),
					scriptGridTracker.getScript());	
			navigation=fusionCurrentScriptNavigationDocument.getNavigation();
		}
		return navigation;
	}
	
	public void processNavigationXMLObjects(List<XmlObject> xmlObjectsList) throws Exception {
		//System.out.println("xmlObjectsList:"+xmlObjectsList.size());
		String taskName=getTaskName();		
		for (XmlObject xmlObject:xmlObjectsList) {
			if (isStopped) {
				throw new Exception("Injection stopped!");
			}

			String text=xmlObject.toString();			
			String nodeValue=((SimpleValue)xmlObject).getStringValue();
			nodeValue=nodeValue.replace(ScriptManager.KEY_TASK_NAME,taskName);
			String tagName=xmlObject.getDomNode().getNodeName();
			String fullCommand=text.replaceAll("xml-fragment", tagName);
			fullCommand=SeleniumUtils.formatCommand(fullCommand);

			String implementationProject=batchInjectionTracker.getScriptGridTracker().getInjectMain().getApplicationInfoPanel().getImplementationProject();
			nodeValue=nodeValue.replace(ScriptManager.KEY_IMPLEMENTATION_PROJECT,implementationProject);

			println("###################################################################");
			println("###################################################################");
			println("Executing TAG NAME: "+tagName);

			if (xmlObject instanceof SendKeysType ||
					xmlObject instanceof FindElementType||
					xmlObject instanceof FindElementsType||
					xmlObject instanceof SelectType||
					xmlObject instanceof TemplateInputType||
					xmlObject instanceof TemplateSelectType||
					xmlObject instanceof TemplateTextAreaType||
					xmlObject instanceof TemplateRadioButtonType||
					xmlObject instanceof TemplateStartSectionType||
					xmlObject instanceof TemplateLOVType
					) {
				String kbColumnName=InjectUtils.getKBColumnName(xmlObject);
				if (kbColumnName!=null && currentRepeatInventoryName!=null) {
					RecordTracker recordTracker=inventoryToCurrentRecordTrackerMap.get(currentRepeatInventoryName);
					if (recordTracker!=null) {
						recordTracker.setFieldName(kbColumnName);
						//getSeleniumScriptManager().getFusionMain().getScriptsGrid().updateTableGrid(currentRepeatInventoryName,recordTracker);
					}
				}

				boolean isReplaceSingleQuotes=false;
				if (xmlObject instanceof FindElementType || xmlObject instanceof FindElementsType) {
					isReplaceSingleQuotes=true;
				}
				nodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(batchInjectionTracker,xmlObject,nodeValue,isReplaceSingleQuotes);
				nodeValue=nodeValue.replace(ScriptManager.KEY_TASK_NAME,taskName);
				println("nodeValue after replaceNodeValueInventoryAndColumnNamesByValue: '"+nodeValue+"'");
			}
			if ( !( (xmlObject instanceof BlockType) ||
					(xmlObject instanceof RepeatType) ||
					(xmlObject instanceof IfThenElseType)
					)){
				String tmp=nodeValue.replaceAll("\n","");
				println("nodeValue: '"+tmp+"'");
			}
			else {
				println("not printing nodeValue for this tag name");
			}

			if ( !( (xmlObject instanceof PauseType) ||
					(xmlObject instanceof PostURLType) 
					)){
				println("Current selected webElement:");
				SeleniumUtils.printElement(this,webDriver,currentWebElement);
			}
			else {
				println("not printing the current selected webElement.");
			}			

			boolean isCheckDomError=true;
			if (xmlObject instanceof ParametersType) {
				ParametersType parametersType=(ParametersType)xmlObject;
				ParameterType[] parametersArray=parametersType.getParameterArray();
				ParameterTypeCommand parameterTypeCommand=new ParameterTypeCommand(this);
				parameterTypeCommand.process(parametersArray,false);
				isCheckDomError=false;
			}
			else
			if (xmlObject instanceof WaitType) {
				println(fullCommand,true);
				WaitType waitType=(WaitType)xmlObject;
				WaitTypeCommand waitTypeCommand=new WaitTypeCommand(this);
				waitTypeCommand.process(waitType,nodeValue);
			}
			else
				if (xmlObject instanceof SendKeysType) {
					println(fullCommand,true);
					SendKeysType sendKeysType=(SendKeysType)xmlObject;
					SendKeysTypeCommand sendKeysTypeCommand=new SendKeysTypeCommand(this);
					sendKeysTypeCommand.process(sendKeysType,nodeValue);
					if (sendKeysType.getIsSkipCheckDomError()) {
						isCheckDomError=false;
					}
				}
				else
					if (xmlObject instanceof SelectType) {
						println(fullCommand,true);
						SelectType selectType=(SelectType)xmlObject;
						SelectTypeCommand selectTypeCommand=new SelectTypeCommand(this);
						selectTypeCommand.process(selectType,nodeValue);
					}
					else
						if (xmlObject instanceof FindElementType) {
							println(fullCommand,true);
							FindElementType findElementType=(FindElementType)xmlObject;
							FindElementTypeCommand findElementTypeCommand=new FindElementTypeCommand(this);
							int maxRetries=3;
							findElementTypeCommand.process(findElementType,nodeValue,maxRetries);
							if (findElementType.getIsSkipCheckDomError()) {
								isCheckDomError=false;
							}
						}
						else
							if (xmlObject instanceof FindElementsType) {
								println(fullCommand,true);
								FindElementsType findElementsType=(FindElementsType)xmlObject;
								FindElementsTypeCommand findElementsTypeCommand=new FindElementsTypeCommand(this);
								findElementsTypeCommand.process(findElementsType,nodeValue);
								if (findElementsType.getIsSkipCheckDomError()) {
									isCheckDomError=false;
								}
							}
							else
								if (xmlObject instanceof ClickType) {
									println(fullCommand,true);
									ClickType clickType=(ClickType)xmlObject;
									ClickTypeCommand clickTypeCommand=new ClickTypeCommand(this);
									clickTypeCommand.process(clickType);
									if (clickType.getIsSkipCheckDomError()) {
										isCheckDomError=false;
									}
								}
								else
									if (xmlObject instanceof IfThenElseType) {
										IfThenElseType ifThenElseType=(IfThenElseType)xmlObject;
										IfThenElseTypeCommand ifThenElseTypeCommand=new IfThenElseTypeCommand(this);
										ifThenElseTypeCommand.process(ifThenElseType);
										isCheckDomError=false;
									}
									else
										if (xmlObject instanceof BlockType) {
											BlockType blockType=(BlockType)xmlObject;
											BlockTypeCommand blockTypeCommand=new BlockTypeCommand(this);
											blockTypeCommand.process(blockType);
											isCheckDomError=false;
										}
										else
											if (xmlObject instanceof ExecuteBlockType) {
												println(fullCommand,true);
												ExecuteBlockType executeBlockType=(ExecuteBlockType)xmlObject;
												ExecuteBlockTypeCommand executeBlockTypeCommand=new ExecuteBlockTypeCommand(this);
												executeBlockTypeCommand.process(executeBlockType);
												isCheckDomError=false;
											}
											else
												if (xmlObject instanceof RepeatType) {
													RepeatType repeatType=(RepeatType)xmlObject;
													println(SeleniumUtils.getRepeatTypeFriendlyXML(repeatType),true);
													
													/*
													 * Cancel the node kill timer here because we are sure
													 * that we are not stuck at the home page if we reached that step.
													 */
													cancelNodeKillTimer();
													
													RepeatTypeCommand repeatTypeCommand=new RepeatTypeCommand(this,false,true);			
													repeatTypeCommand.process(repeatType);
													isCheckDomError=false;
												}
												else
													if (xmlObject instanceof PauseType) {
														println(fullCommand,true);
														PauseType pauseType=(PauseType)xmlObject;
														PauseTypeCommand pauseTypeCommand=new PauseTypeCommand(this);
														pauseTypeCommand.process(pauseType);
														isCheckDomError=false;
													}
													else
														if (xmlObject instanceof DependenciesType) {
															continue;
														}
														else
															if (xmlObject instanceof ErrorType) {
																ErrorType errorType=(ErrorType)xmlObject;
																ErrorTypeCommand errorTypeCommand=new ErrorTypeCommand(this);
																errorTypeCommand.process(errorType);
																isCheckDomError=false;
															}
															else
																if (xmlObject instanceof BreakType) {
																	println(fullCommand,true);
																	isBreakDetected=true;
																	continue;
																}
																else
																	if (xmlObject instanceof DefineVariableType) {
																		DefineVariableType defineVariableType=(DefineVariableType)xmlObject;
																		DefineVariableTypeCommand defineVariableTypeCommand=new DefineVariableTypeCommand(this);
																		defineVariableTypeCommand.process(defineVariableType);
																		isCheckDomError=false;
																	}
																	else
																		if (xmlObject instanceof SetVariableType) {
																			SetVariableType setVariableType=(SetVariableType)xmlObject;
																			SetVariableTypeCommand setVariableTypeCommand=new SetVariableTypeCommand(this);
																			setVariableTypeCommand.process(setVariableType);
																			isCheckDomError=false;
																		}
																		else
																			if (xmlObject instanceof ExtractType) {
																				ExtractType extractType=(ExtractType)xmlObject;
																				ExtractTypeCommand extractTypeCommand=new ExtractTypeCommand(this);
																				extractTypeCommand.process(extractType);
																				isCheckDomError=false;
																			}
																			else
																				if (xmlObject instanceof ScrollType) {
																					ScrollType scrollType=(ScrollType)xmlObject;
																					ScrollTypeCommand scrollTypeCommand=new ScrollTypeCommand(this);
																					scrollTypeCommand.process(scrollType);
																				}
																				else
																					if (xmlObject instanceof PostURLType) {
																						PostURLType postURLType=(PostURLType)xmlObject;
																						PostURLTypeCommand postURLTypeCommand=new PostURLTypeCommand(this);
																						postURLTypeCommand.process(postURLType);
																						if (postURLType.getIsSkipCheckDomError()) {
																							isCheckDomError=false;
																						}
																					}
																					else
																						if (xmlObject instanceof WindowType) {
																							WindowType windowType=(WindowType)xmlObject;
																							WindowTypeCommand windowTypeCommand=new WindowTypeCommand(this);
																							windowTypeCommand.process(windowType);
																						}																						
																							else
																								if (xmlObject instanceof TemplateInputType) {
																									TemplateInputType templateInputType=(TemplateInputType)xmlObject;
																									TemplateInputTypeCommand templateInputTypeCommand=new TemplateInputTypeCommand(this);
																									templateInputTypeCommand.process(templateInputType,nodeValue);
																								}
																								else
																									if (xmlObject instanceof TemplateSelectType) {
																										TemplateSelectType templateSelectType=(TemplateSelectType)xmlObject;
																										TemplateSelectTypeCommand templateSelectTypeCommand=new TemplateSelectTypeCommand(this);
																										templateSelectTypeCommand.process(templateSelectType,nodeValue);
																									}
																									else
																										if (xmlObject instanceof TemplateTextAreaType) {
																											TemplateTextAreaType templateTextAreaType=(TemplateTextAreaType)xmlObject;
																											TemplateTextAreaTypeCommand templateTextAreaTypeCommand=new TemplateTextAreaTypeCommand(this);
																											templateTextAreaTypeCommand.process(templateTextAreaType,nodeValue);
																										}
																										else
																											if (xmlObject instanceof TemplateRadioButtonType) {
																												TemplateRadioButtonType templateRadioButtonType=(TemplateRadioButtonType)xmlObject;
																												TemplateRadioButtonTypeCommand templateRadioButtonTypeCommand=new TemplateRadioButtonTypeCommand(this);
																												templateRadioButtonTypeCommand.process(templateRadioButtonType,nodeValue);
																											}
																											else
																												if (xmlObject instanceof TemplateLOVType) {
																													TemplateLOVType templateLOVType=(TemplateLOVType)xmlObject;
																													TemplateLOVTypeCommand templateLOVTypeCommand=new TemplateLOVTypeCommand(this);
																													templateLOVTypeCommand.process(templateLOVType);
																												}
																												else
																													if (xmlObject instanceof TemplateStartSectionType) {	
																														TemplateStartSectionType templateStartSectionType=(TemplateStartSectionType)xmlObject;
																														TemplateStartSectionTypeCommand templateStartSectionTypeCommand=new TemplateStartSectionTypeCommand(this);
																														templateStartSectionTypeCommand.process(templateStartSectionType,nodeValue);
																													}
																													else
																														if (xmlObject instanceof TemplateEndSectionType) {	
																															TemplateEndSectionType templateEndSectionType=(TemplateEndSectionType)xmlObject;
																															TemplateEndSectionTypeCommand templateEndSectionTypeCommand=new TemplateEndSectionTypeCommand(this);
																															templateEndSectionTypeCommand.process(templateEndSectionType);
																														}
																														else
																															if (xmlObject instanceof TemplateGridSearchBarType) {	
																																TemplateGridSearchBarType templateGridSearchBarType=(TemplateGridSearchBarType)xmlObject;
																																TemplateGridSearchBarTypeCommand templateGridSearchBarTypeCommand= new TemplateGridSearchBarTypeCommand(this);
																																templateGridSearchBarTypeCommand.process(templateGridSearchBarType);
																															}
																															else
																																if (xmlObject instanceof TemplateClickType) {	
																																	TemplateClickType templateClickType=(TemplateClickType)xmlObject;
																																	TemplateClickTypeCommand templateClickTypeCommand=new TemplateClickTypeCommand(this);
																																	templateClickTypeCommand.process(templateClickType,nodeValue);
																																}
																																	else
																																		if (xmlObject instanceof TemplateGridFindRowType) {	
																																			TemplateGridFindRowType templateGridFindRowType=(TemplateGridFindRowType)xmlObject;
																																			TemplateGridFindRowTypeCommand templateGridFindRowTypeCommand=new TemplateGridFindRowTypeCommand(this);
																																			templateGridFindRowTypeCommand.process(templateGridFindRowType);
																																		}
																														
																						else
																						{
																							println(fullCommand,true);
																							throw new Exception("UNKNOWN TAG: "+tagName);
																						}
			if (isCheckDomError) {
				checkIfNoDOMError();
			}

		}
	}

	private void cancelNodeKillTimer() throws Exception {
		boolean isServerMode=batchInjectionTracker.getScriptGridTracker().getInjectMain().getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isServerMode();
		boolean isHomePageTimerActivationOn=Config.getInjectHomePageTimerActivation();
		if (isServerMode && isHomePageTimerActivationOn) {
			if (!isNodeKillTimerAlreadyCancelled) {
				isNodeKillTimerAlreadyCancelled=true;
				println("Starting KillNode Timer request to Hub to cancel timer.");
				HubRequestsManager.stopKillNodeTimer(batchInjectionTracker.getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().
						getExecutionPanelUI().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().getHubID(),vncDisplayName);
				println("Request sent.");
			}
		}
	}

	public void checkIfNoDOMError() throws Exception {
		boolean hasError=false;
		try{
			this.println("Checking for presence of an image element ERROR //img[@title='Error'] ...",false);
			String xpath="//img[@title='Error']";
			WebElement webElement=webDriver.findElement(By.xpath(xpath));
			this.println("Found ERROR image, checking isDisplayed attribute: "+webElement.isDisplayed(),false);
			if (webElement.isDisplayed()) {
				hasError=true;
			}
		}
		catch (NoSuchElementException e){
			this.println("No ERROR image found in DOM",false);
		}
		catch(Exception e) {
			printStackTrace(e);
		}
		if (hasError) {
			throw new Exception("An Oracle error was detected on the page. Please check your data and try again!");
		}
	}

	public boolean getIsBreakDetected() {
		return isBreakDetected;
	}

	public void resetBreakDetected() {
		isBreakDetected=false;
	}

	public Map<String, Object> getVariableNameToValueMap() {
		return variableNameToValueMap;
	}

	public Map<String, DefineVariableType.Type.Enum> getVariableNameToTypeMap() {
		return variableNameToTypeMap;
	}

	public List<WebElement> getCurrentWebElements() {
		return currentWebElements;
	}

	public void setCurrentWebElements(List<WebElement> currentWebElements) {
		this.currentWebElements = currentWebElements;
	}

	public WebElement getCurrentWebElement() {
		return currentWebElement;
	}

	public void setCurrentWebElement(WebElement currentWebElement) {
		this.currentWebElement = currentWebElement;
	}

	public String getVNCDisplayName() {
		return vncDisplayName;
	}

	public Stack<String> getWindowHandlesStack() {
		return windowHandlesStack;
	}

	public Map<String, String> getParameterNameToValueMap() {
		return parameterNameToValueMap;
	}
	
	public String getTaskName() {
		return taskName;
	}

	public boolean isRelativeWebElementReferenceInUse() {
		return isRelativeWebElementReferenceInUse;
	}

	public void setRelativeWebElementReferenceInUse(boolean isRelativeWebElementReferenceInUse) {
		this.isRelativeWebElementReferenceInUse = isRelativeWebElementReferenceInUse;
	}

	public String getRelativeWebElementXPATH() {
		return relativeElementXpath;
	}

	public void setRelativeWebElementXPATH(String relativeElementXpath) {
		this.relativeElementXpath = relativeElementXpath;
	}

	public WebElement getRelativeWebElement() {
		println("Searching for relative web element with xpath: '"+relativeElementXpath+"'");
		WebElement webElement=webDriver.findElement(By.xpath(relativeElementXpath));
		return webElement;
	}


}
