package com.rapidesuite.designers.navigation;

import java.awt.BorderLayout;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.RandomAccessFile;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.domain.Navigation;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserFunction;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;

@SuppressWarnings("serial")
public class NavigationJWebBrowser extends JFrame {

	private static final String FUSION_BASE_URL = "https://fusion01.rapidesuite.com:18614";
	private static final String FUSION_HOME_PAGE_URL = FUSION_BASE_URL+"/homePage";
	private static final String FUSION_SETUP_PAGE_URL = FUSION_BASE_URL+"/setup/faces/TaskListManagerTop";
	private static final String TASK_NAME_PLACEHOLDER = "TASK-NAME-PLACEHOLDER";
	
	private JWebBrowser webBrowser;
	private JPopupMenu contextMenu;
	private NavigationEditorMain navigationEditorMain;
	protected boolean isFusionURLLoaded;
	protected boolean redirectedAfterLogin;
	private int documentsToWaitBeforRedirecting = 4 ;
	protected boolean isGrabberCodeInjected;
	private int closedCertificatesCount;
	private Navigation navigation;
	
	public NavigationJWebBrowser(NavigationEditorMain navigationEditorMain, Navigation navigation) {
		this.navigationEditorMain=navigationEditorMain;
		this.navigation=navigation;
		setTitle("Navigation Browser");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(1200, 800);
		setLocationByPlatform(true);

		JPanel panel=new JPanel();
		panel.setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.CENTER);
		
		// This is to clear all cookies sessions...
		NativeInterface.close();
		NativeInterface.open();
		
		JPanel webBrowserPanel = new JPanel(new BorderLayout());
		//webBrowser = new JWebBrowser(JWebBrowser.useXULRunnerRuntime());
		webBrowser = new JWebBrowser();
		
		webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
		panel.add(webBrowserPanel, BorderLayout.CENTER);
		
		// Create an additional bar allowing to show/hide the menu bar of the web browser.
		/*
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
		JCheckBox menuBarCheckBox = new JCheckBox("Menu Bar", webBrowser.isMenuBarVisible());
		menuBarCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				webBrowser.setMenuBarVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		buttonPanel.add(menuBarCheckBox);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		*/
		/*
		JButton selectionMode = new JButton("Selection Mode");
		selectionMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String source = getStringFromFile("./functions/widgetGrabberEngine.js");
				runJavaScript(source);			
			}
		});
		buttonPanel.add(selectionMode);	   
		 */

		webBrowser.addWebBrowserListener(new WebBrowserListener(){

			@Override
			public void loadingProgressChanged(WebBrowserEvent arg0) {
				//System.out.println("webBrowser.getLoadingProgress(): "+webBrowser.getLoadingProgress()+
				//		" webBrowser.getResourceLocation(): "+webBrowser.getResourceLocation());
				//System.out.println("webBrowser.getPageTitle():"+webBrowser.getPageTitle());
				String pageTitle=webBrowser.getPageTitle();
				if(webBrowser.getLoadingProgress() == 100) {
					System.out.println("PAGE LOADED webBrowser.getPageTitle():"+webBrowser.getPageTitle());
					setTitle("Navigation Browser - "+webBrowser.getPageTitle());
					if (!isFusionURLLoaded) {
						isFusionURLLoaded=true;
						System.out.println("Fusion Login page loaded... Page title:"+webBrowser.getPageTitle());
						autoLogin();
					}
					else {
						if (redirectedAfterLogin || (pageTitle!=null && pageTitle.contains("welcome"))  ) {
							redirectedAfterLogin=true;
							//documentsToWaitBeforeInjectingGrabberCode--;
							//if(documentsToWaitBeforeInjectingGrabberCode == 0){
							if ( pageTitle!=null &&
									(pageTitle.contains("Overview - Setup and Maintenance - Oracle Applications")||
											pageTitle.trim().toLowerCase().contains("/setup/faces/TaskListManagerTop".toLowerCase()))
									){
								isGrabberCodeInjected=true;
								System.out.println("Injecting grabber code...");
								String source = getStringFromFile("./functions/widgetGrabberEngine.js");
								runJavaScript(source);
								goToTask();
							}
						}
						else {
							documentsToWaitBeforRedirecting--;
							if(!redirectedAfterLogin && (
									documentsToWaitBeforRedirecting == 0
									|| (pageTitle!=null && pageTitle.trim().toLowerCase().contains("homePage/faces/AtkHomePageWelcome")
									))
									){
								redirectedAfterLogin=true;
								System.out.println("Redirecting to Setup page... Page title:"+webBrowser.getPageTitle());
								webBrowser.navigate(FUSION_SETUP_PAGE_URL);  
							}					
						}
					}
				}
			}

			@Override
			public void commandReceived(WebBrowserCommandEvent arg0) {
			}

			@Override
			public void locationChangeCanceled(WebBrowserNavigationEvent arg0) {
			}

			@Override
			public void locationChanged(WebBrowserNavigationEvent arg0) {
			}

			@Override
			public void locationChanging(WebBrowserNavigationEvent arg0) {
			}

			@Override
			public void statusChanged(WebBrowserEvent arg0) {
			}

			@Override
			public void titleChanged(WebBrowserEvent arg0) {
			}

			@Override
			public void windowClosing(WebBrowserEvent arg0) {
			}

			@Override
			public void windowOpening(WebBrowserWindowOpeningEvent arg0) {
			}

			@Override
			public void windowWillOpen(WebBrowserWindowWillOpenEvent arg0) {
			}
		});		
		
		webBrowser.registerFunction(new WebBrowserFunction("javaCallback") {
			@Override
			public Object invoke(JWebBrowser webBrowser, Object... args) {
				if(args.length == 0) return 10 / 0;
				String action = args[0].toString();
				if (action == null)return 10 / 0;

				if(action.equals("GRAB")){
					if(args.length == 1)return 10 / 0;
					String htmlCode = args[1].toString();
					//System.out.println("LENGTH: "+args.length);
					System.out.println("RIGHT CLICK DETECTED:"+htmlCode);
					
					//parseHTML(htmlCode);
					showContextMenu(htmlCode);
				}
				return 10 / 0;
			}
		});
		
		//webBrowser = new JWebBrowser(JWebBrowser.useXULRunnerRuntime());
		//webBrowser.navigate("http://www.google.com");
		//webBrowser.navigate("http://www.browserproperties.com");
		webBrowser.navigate(FUSION_HOME_PAGE_URL);
		closeCertificatesThread();
	}

	protected void goToTask() {
		String taskName=navigation.getNavigationDocument().getNavigation().getFusionNavigation().getTaskName();
		boolean hasScope=navigation.getNavigationDocument().getNavigation().getFusionNavigation().getHasScope();
		if (hasScope) {
			// TODO: to implement
		}
		else {
			String source = getStringFromFile("./functions/goToTaskDirect.js");
			source=source.replaceAll(TASK_NAME_PLACEHOLDER,taskName);
			//System.out.println("Running task selection script: '"+source+"'");
			runJavaScript(source);
		}
	}

	protected void parseDataTemplates(String htmlCode) {
		try{
			Document document=Jsoup.parseBodyFragment(htmlCode);
			
			// Detect any blocks creation first
			Elements elements = document.getElementsByTag("h2");
			if (elements.size()>1) {
				throw new Exception("Please select only one Section at a time!");
			}
			if (!elements.isEmpty()) {
				String textValue=elements.get(0).text();
				System.out.println("parseTemplates, found BLOCK!!! textValue: '"+textValue+"'");
				navigationEditorMain.getNavigationTextEditorPanel().insertCommandBlock(textValue);
			}
						
			// Detect templates based on labels.
			elements = document.getElementsByTag("label");
			System.out.println("parseTemplates, elements:"+elements.size());	
			KBPlaceholderPanel kbPlaceholderPanel=null;
			for (Element labelElement:elements) {
				String forAttributeValue=labelElement.attr("for");
				if (forAttributeValue==null || forAttributeValue.isEmpty()) {
					continue;
				}
				System.out.println("parseTemplates, labelElement, forAttributeValue:"+forAttributeValue);
				
				// detecting radiobuttons
				String radioButonAttributeBaseValue=forAttributeValue.replace("::content","");
				System.out.println("parseTemplates, radioButonAttributeBaseValue: '"+radioButonAttributeBaseValue+"'");
				Elements tempElements=document.getElementsByAttributeValue("name", radioButonAttributeBaseValue);
				System.out.println("parseTemplates, tempElements: "+tempElements.size());
				for (Element tempElement:tempElements) {
					if (tempElement.tagName().equalsIgnoreCase("input")) {
						String tempAttributeValue=tempElement.attr("type");
						if (tempAttributeValue!=null && tempAttributeValue.equalsIgnoreCase("radio")) {
							String labelTextValue=labelElement.text();
							System.out.println("parseTemplates, found TEMPLATE_RADIOBUTTON!!! labelTextValue: '"+labelTextValue+"'");
							
							String selectedInventoryName="";
							if (kbPlaceholderPanel==null) {
								kbPlaceholderPanel=navigationEditorMain.getNavigationTextEditorPanel().openKBPlaceHolderDialogWindow(false);
								if (!kbPlaceholderPanel.isSubmitted()) {
									return;
								}
							}
							selectedInventoryName=kbPlaceholderPanel.getSelectedInventoryName();
							
							navigationEditorMain.getNavigationTextEditorPanel().insertTemplateRadioButton(selectedInventoryName,labelTextValue);
							break;
						}
					}
				}
								
				Element element=document.getElementById(forAttributeValue);
				if (element!=null && element.tagName().equalsIgnoreCase("input")) {
					String tempAttributeValue=element.attr("type");
					if (tempAttributeValue!=null && tempAttributeValue.equalsIgnoreCase("radio")) {
						// skip it as the code is more complex
					}
					else {
						String labelTextValue=labelElement.text();
						System.out.println("parseTemplates, found TEMPLATE_INPUT!!! labelTextValue: '"+labelTextValue+"'");
						navigationEditorMain.getNavigationTextEditorPanel().insertTemplateInput(labelTextValue);
					}
				}
				else
					if (element!=null && element.tagName().equalsIgnoreCase("select")) {
						String labelTextValue=labelElement.text();
						System.out.println("parseTemplates, found TEMPLATE_SELECT!!! labelTextValue: '"+labelTextValue+"'");
						navigationEditorMain.getNavigationTextEditorPanel().insertTemplateSelect(labelTextValue);
					}
					else
						if (element!=null && element.tagName().equalsIgnoreCase("textarea")) {
							String labelTextValue=labelElement.text();
							System.out.println("parseTemplates, found TEMPLATE_TEXTAREA!!! labelTextValue: '"+labelTextValue+"'");
							navigationEditorMain.getNavigationTextEditorPanel().insertTemplateTextArea(labelTextValue);
						}
				
			}
			navigationEditorMain.getNavigationTextEditorPanel().executeIndent();
		}
		catch(Exception e) {
			e.printStackTrace();
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}
	
	protected void parseActionTemplates(String htmlCode) {
		try{
			Document document=Jsoup.parseBodyFragment(htmlCode);
			
			Elements elements = document.getElementsByTag("button");
			System.out.println("parseActionTemplates, button, elements:"+elements.size());	
			for (Element element:elements) {
				String textValue=element.text();
				System.out.println("parseActionTemplates, found BUTTON!!! textValue: '"+textValue+"'");
				navigationEditorMain.getNavigationTextEditorPanel().insertCommandsButton(textValue,"textNode");
			}

			/*
			 * <a onclick="this.focus();return false" class="x1bj" theme="light" href="#">
			 * 		<img id="pt1:USma:0:MAnt2:1:r2:0:dynamicRegion1:0:pm1:r1:0:ap1:at1:_ATp:create::icon" 
			 * 			src="/setup/afr/remote/H4sIAAAAAAAAAA%3D%3DRc0_CsIwFIDxre/patterns/new_ena.png" title="Create" 
			 * 			alt="Create" class="x1bn" theme="light">
			 * </a>
			 */
			elements = document.getElementsByTag("a");
			System.out.println("parseActionTemplates, anchor, elements:"+elements.size());	
			for (Element element:elements) {
				Elements childElements=element.getElementsByTag("img");
				if (childElements!=null && !childElements.isEmpty()) {
					Element imgElement=childElements.get(0);
					String attributeValue=imgElement.attr("title");
					if (attributeValue!=null && !attributeValue.isEmpty()) {
						navigationEditorMain.getNavigationTextEditorPanel().insertCommandsAnchorImage(attributeValue,"title");
						System.out.println("parseActionTemplates, found ANCHOR!!! attributeValue: '"+attributeValue+"'");
					}
				}
			}
		}
		catch(Exception e) {
			GUIUtils.popupErrorMessage("Unable to parse HTML fragment: '"+htmlCode+"' . Error: "+e.getMessage());
		}
	}

	protected void showContextMenu(final String htmlCode) {
		try{
			int x = (int)MouseInfo.getPointerInfo().getLocation().getX() - (int)getLocationOnScreen().getX();
			int y = (int)MouseInfo.getPointerInfo().getLocation().getY() - (int)getLocationOnScreen().getY();
			System.out.println("htmlCode: '"+htmlCode+"'");
			
			contextMenu = new JPopupMenu();
			JMenuItem templates = new JMenuItem("Data Template(s) (checkbox, radiobutton, text fields, dropdown list, textarea)");
			templates.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					parseDataTemplates(htmlCode);
				}
			});		
			contextMenu.add(templates);
			
			JMenuItem buttons = new JMenuItem("Action Template(s) (button, link, menu)");
			buttons.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					parseActionTemplates(htmlCode);
				}
			});		
			contextMenu.add(buttons);
			
			JMenuItem links = new JMenuItem("Grid Template");
			links.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					parseActionGridTemplate(htmlCode);
				}
			});		
			contextMenu.add(links);
			
			contextMenu.show(this, x, y);
		}
		catch(Exception ex){
			ex.printStackTrace();	
		}
	}
	
	protected void parseActionGridTemplate(String htmlCode) {
		try{
			Document document=Jsoup.parseBodyFragment(htmlCode);
			
			
			Elements elements = document.getElementsByTag("h1");
			if (elements.size()>1) {
				throw new Exception("Please select only one Section at a time!");
			}
			if (elements.isEmpty()) {
				elements = document.getElementsByTag("h2");
				if (elements.size()>1) {
					throw new Exception("Please select only one Section at a time!");
				}
			}
			if (elements!=null) {
				String sectionTitle=elements.get(0).text();
				System.out.println("parseActionGridTemplate, found SECTION, sectionTitle: '"+sectionTitle+"'");
				elements = document.getElementsByAttributeValueEnding("id","scroller");
				System.out.println("parseActionGridTemplate, scroller elements: "+elements.size());
				if (!elements.isEmpty()) {
					navigationEditorMain.getNavigationTextEditorPanel().insertTemplateGridSearch(sectionTitle);
				}				
			}
		}
		catch(Exception e) {
			GUIUtils.popupErrorMessage("Unable to parse HTML fragment: '"+htmlCode+"' . Error: "+e.getMessage());
		}
	}

	public void autoLogin() {
		String source = getStringFromFile("./functions/loginToFusion.js");
		runJavaScript(source);
	}
	
	private static final int MAX_TITLE_LENGTH = 1024;
	public void closeCertificatesThread() {
		Thread t = new Thread() {
		    public void run() {
		    	try{
					Robot robot = new Robot();
					while (true) {
						if (closedCertificatesCount>=1) {
							Thread.sleep(50);
						}
						else {
							Thread.sleep(100);
						}
						
						closeCertificate(robot);
						if (closedCertificatesCount>=8) {
							break;
						}
					}
					System.out.println("!!! ALL CERTIFICATES CLOSED !!!");
				} 
				catch(Exception e){
					e.printStackTrace();
				}
		    }
		};
		t.start();
	}

	public void closeCertificate(Robot robot ) {
		char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
        String title=Native.toString(buffer);
        if (title!=null && title.equalsIgnoreCase("Security Alert")) {
    		robot.keyPress(KeyEvent.VK_TAB);
    		robot.keyRelease(KeyEvent.VK_TAB);
    		robot.keyPress(KeyEvent.VK_TAB);
    		robot.keyRelease(KeyEvent.VK_TAB);
    		robot.keyPress(KeyEvent.VK_ENTER);
    		robot.keyRelease(KeyEvent.VK_ENTER);
    		closedCertificatesCount++;
        }		
	}

	public void runJavaScript(String javascript){
		String injector ="(function(d, script) {\n"+
				"script = d.createElement('script');\n"+
				"script.type = 'text/javascript';\n"+
				"script.async = true;\n"+
				"script.onload = function(){\n"+
				"    // remote script has loaded\n"+
				"};\n"+
				"script.src = '<--#SOURCE#-->';\n"+
				"d.getElementsByTagName('head')[0].appendChild(script);\n"+
				"}(document));\n";	   
		injector.replace("<--#SOURCE#-->", javascript);
		webBrowser.executeJavascriptWithResult(javascript);
		//System.out.println("jsResult:"+jsResult);
	}

	public static String getStringFromFile(String path){
		String content="";
		File f = new File(path);
		if(!f.exists())return "";
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(f,"r");
			byte[] bytes = new byte[(int)raf.length()];
			raf.readFully(bytes);
			content = new String(bytes); 
		}catch(Exception ex){
			ex.printStackTrace();	
		}finally{
			try{raf.close();}catch (Exception ex){} 
		}
		return content;
	}    


}