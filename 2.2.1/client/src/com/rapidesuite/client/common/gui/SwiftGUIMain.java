/**************************************************
 * $Revision: 59443 $:
 * $Author: olivier.deruelle $:
 * $Date: 2016-11-09 18:24:54 +0700 (Wed, 09 Nov 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/SwiftGUIMain.java $:
 * $Id: SwiftGUIMain.java 59443 2016-11-09 11:24:54Z olivier.deruelle $:
 */

package com.rapidesuite.client.common.gui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.utils.TaskListUtils;
import com.rapidesuite.client.common.JobManager;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.configurator.domain.Update;
import com.rapidesuite.configurator.utility.UpdateUtils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.sun.jna.Platform;

public abstract class SwiftGUIMain
{
    static
    {
        forceTimezoneForOldOracleDatabaseVersions();
    }

    //WARNING: It must be done before temp folder is cleared
    private void tellUpdaterAboutSuccessfulApplicationStartUp()
    {
    	final File updateFolder = new File(Config.getTempFolder(), UtilsConstants.UPDATES_FOLDER_NAME);
    	if (updateFolder.isDirectory()) {
        	final long currentTimestamp = new Date().getTime();
        	final File applicationStartedFile = new File(Config.getTempFolder(), UtilsConstants.APPLICATION_STARTED_FILENAME_PREFIX+currentTimestamp);
        	CoreUtil.writeToFile(".", false, applicationStartedFile.getAbsoluteFile().getParentFile(), applicationStartedFile.getName());
        	applicationStartedFile.setReadable(true, false);
        	applicationStartedFile.setWritable(true, false);
        	FileUtils.println("Finished writing application started flag file");

        	try {
				com.rapidesuite.client.common.util.Utils.sleep(UtilsConstants.APPLICATION_EXIT_UPDATING_WAITING_TIME_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }

    private void clearTempFolder() {
        final int maxNumberOfDeletionAttempt = 3;

        for (int i = 1 ; i <= maxNumberOfDeletionAttempt ; i++) {
            try
            {
                org.apache.commons.io.FileUtils.deleteDirectory(Config.getTempFolder());
                FileUtils.println("Finished deleting temp directory");
                Config.getTempFolder().mkdirs();
                break;
            }
            catch(Throwable t)
            {
                if (i == maxNumberOfDeletionAttempt) {
                    t.printStackTrace();
                    GUIUtils.popupErrorMessage("Error while deleting old temp folder: " + CoreUtil.getAllThrowableMessages(t));
                    FileUtils.printStackTrace(t);
                } else {
                    try {
                        com.rapidesuite.client.common.util.Utils.sleep(UtilsConstants.APPLICATION_EXIT_UPDATING_WAITING_TIME_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private static void forceTimezoneForOldOracleDatabaseVersions()
    { //Oracle 9i doesn't recognize "Bangkok", but does recognize "Thailand".  et al.
        Properties p = System.getProperties();
        final String PROPERTY_NAME_TIMEZONE = "user.timezone";
        Map<String,String> replacementTimezones = new HashMap<String,String>();
        replacementTimezones.put("Asia/Bangkok", "Asia/Thailand");
        String timezone = p.getProperty(PROPERTY_NAME_TIMEZONE);
        for ( Entry<String, String> entry : replacementTimezones.entrySet() )
        {
            if (timezone.indexOf(entry.getKey()) != -1 )
            {
                timezone = timezone.replaceAll(entry.getKey(), entry.getValue());
                p.setProperty(PROPERTY_NAME_TIMEZONE, timezone);
                System.setProperties(p);

                System.err.println("Forcing " + PROPERTY_NAME_TIMEZONE + " from " + entry.getKey() + " to " + entry.getValue());
                break;
            }
        }
    }

    private Properties replacementsProperties;

    public Properties getReplacementsProperties()
    {
        return replacementsProperties;
    }


	protected JFrame rootFrame;
	protected CardLayout cardLayout;
	protected JPanel cardComponent;

	protected JMenuBar menuBar;
	protected JMenu menuTools;

	private int bweNameLabelIndex;
	private List<JLabel> informationLabelsAtMenuBar;

	protected JobManager executionJobManager;
	protected ExecutionPanel executionPanel;
	private String applicationVersion;
	private PatchFrame patchFrame;

	private CoreConstants.SHORT_APPLICATION_NAME shortApplicationName;
	public CoreConstants.SHORT_APPLICATION_NAME getShortApplicationName()
	{
	    return this.shortApplicationName;
	}
	private static String startTime = CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE.getDateFormat().format(new Date());
	public static String getStartTime()
	{
	    return startTime;
	}


	private static SwiftGUIMain instance;
	public static SwiftGUIMain getInstance()
	{
	    return instance;
	}
		
	public SwiftGUIMain(File rootLogFolder,CoreConstants.SHORT_APPLICATION_NAME shortApplicationName,boolean isDeleteFolder) throws Exception
	{
		FileUtils.init(rootLogFolder,shortApplicationName,isDeleteFolder);
	    SwiftGUIMainInit(shortApplicationName);
	}
	
	public SwiftGUIMain(CoreConstants.SHORT_APPLICATION_NAME shortApplicationName) throws Exception
	{
		SwiftGUIMainInit(shortApplicationName);
	}
	
	public void SwiftGUIMainInit(CoreConstants.SHORT_APPLICATION_NAME shortApplicationName) throws Exception
	{
		SwiftGUIMainInit(shortApplicationName,false);
	}
	
	public SwiftGUIMain(File rootLogFolder,CoreConstants.SHORT_APPLICATION_NAME shortApplicationName,boolean isDeleteFolder,
			boolean isValidateInstance) throws Exception
	{
		FileUtils.init(rootLogFolder,shortApplicationName,isDeleteFolder);
	    SwiftGUIMainInit(shortApplicationName,isValidateInstance);
	}
	
	public void SwiftGUIMainInit(CoreConstants.SHORT_APPLICATION_NAME shortApplicationName,boolean isValidateInstance) throws Exception
	{
		if (isValidateInstance) {
			Assert.isNull(instance);
		}
		instance = this;
		
	    this.shortApplicationName = shortApplicationName;
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        //WARNING: It must be done before temp folder is cleared
        tellUpdaterAboutSuccessfulApplicationStartUp();
        clearTempFolder();

		String dbUserLanguage=Config.getDbUserLanguage();
		if (dbUserLanguage!=null) {
			Locale locale = new Locale(dbUserLanguage);
			Locale.setDefault(locale);
		}
        replacementsProperties= new Properties();
        try{
        	File file=new File(UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME);
        	if (file.exists()) {
        		replacementsProperties.load(new FileInputStream(UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME));
        	}
        }
        catch(Exception e)
        {
            FileUtils.printStackTrace(e);
        }
        String buildJvmInformation = System.getProperty("java.version")+" - "+System.getProperty("os.arch")+" - "+System.getProperty("java.vendor");
        FileUtils.println("JVM Information: "+buildJvmInformation);

        Config.logAllProperties();
        for ( Map.Entry<Object, Object> entry : replacementsProperties.entrySet() )
        {
            FileUtils.println(UtilsConstants.REPLACEMENTS_PROPERTIES_FILE_NAME + " : " + entry.getKey() + "=" + entry.getValue());
        } 

        String loadingMsg=
                "\n************************************\n"+
                "************************************\n"+
                "LOADING "+shortApplicationName.toString()+"... PLEASE WAIT.\n";
        System.out.println(loadingMsg);
        FileUtils.println(loadingMsg);

        FileUtils.initializeSevenZipLibrary();
        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        UIManager.put("ToolTip.background",new Color(255,255,15));
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        UIManager.put("ToolTip.border", border);
        
        showErrorPopupAndTerminateIfInstallationDirectoryIsTooLong();
        
        startSuicideWatch();
	}

	public JFrame getRootFrame() {
		return rootFrame;
	}

	public void revalidateFrame()
	{
		rootFrame.validate();
		rootFrame.setVisible(true);
	}

	public void close()
	{
		System.exit(0);
	}

	public void createComponents(String registrationURL,String iconPath)
	throws Exception
	{
		createComponents(true,registrationURL,iconPath);
	}
	
	public void createComponents(boolean hasMenuBar,final String registrationURL,final String iconPath)
	throws Exception
	{
		rootFrame = new JFrame();
		rootFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //without this, there can be annoying InterruptedException printout during System.exit
		rootFrame.addWindowListener (new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				close();
			}
		});
		rootFrame.setIconImage(GUIUtils.getImageIcon(this.getClass(), getApplicationIconPath()).getImage());
		menuBar = new JMenuBar();
		this.menuBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		if (hasMenuBar) {
			rootFrame.setJMenuBar(menuBar);
		}
		menuTools = new JMenu();
		menuTools.setIcon(GUIUtils.getImageIcon(this.getClass(), "/images/menu_button.png"));
		this.menuTools.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

		informationLabelsAtMenuBar = new ArrayList<JLabel>();

		ClassLoader cl = this.getClass().getClassLoader();
		applicationVersion=FileUtils.getApplicationVersion(cl,UtilsConstants.APPLICATION_VERSION_FILE_NAME);
		FileUtils.println("Application Version: " +applicationVersion);

//		JMenuItem menuFileCheckForUpdates = new JMenuItem("Check for Updates");
//		menuFileCheckForUpdates.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e)
//			{
//				try{
//					showUpdatesFrame();
//				}
//				catch(Exception ex)
//				{
//					FileUtils.printStackTrace(ex);
//				}
//			}
//		}
//		);
//
//		menuTools.add(menuFileCheckForUpdates);

		final JMenuItem ticketMenuItem = new JMenuItem();
		ticketMenuItem.setText("Open Ticket");
		addSupportTicketListener(ticketMenuItem);
		
		JMenuItem registerMenuItem = new JMenuItem();
		registerMenuItem.setText("Register");
		registerMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ModelUtils.createRegistrationWindow(getRootFrame(),shortApplicationName.toString(),false,true,registrationURL,iconPath);
			}
		});		
		menuTools.add(ticketMenuItem);
		menuTools.add(registerMenuItem);
		menuBar.add(menuTools);

		this.bweNameLabelIndex = appendToInformationLabels();

		Container contentPane = rootFrame.getContentPane();
		cardLayout = new CardLayout();
		cardComponent = new JPanel(cardLayout);
		contentPane.add(cardComponent, BorderLayout.CENTER);
	}
	
	public void addSupportTicketListener(JMenuItem ticketMenuItem) {
		 addSupportTicketListener(ticketMenuItem,Config.getBugtrackerWebsiteAddress());
	}	

	public void addSupportTicketListener(JMenuItem ticketMenuItem,final String url) {
		ticketMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtils.println("Preparing to create ticket");
				final Set<File> filesToSendAsTicketAttachment = getFilesToAttachInTicket();

				if (filesToSendAsTicketAttachment != null) {
					final Iterator<File> iterator = filesToSendAsTicketAttachment.iterator();
					while(iterator.hasNext()) {
						final File file = iterator.next();

						if (!file.exists()) {
							iterator.remove();
						}
					}
				}

				if (filesToSendAsTicketAttachment != null && !filesToSendAsTicketAttachment.isEmpty()) {
					File zipFileFolder = null;
					if (Platform.isWindows()) {
						String defaultDirectory = Config.getTicketAttachmentDefaultDirectoryWindows();
						defaultDirectory = defaultDirectory.replace("%USERPROFILE%", System.getProperty("user.home")).trim();
						zipFileFolder = new File(defaultDirectory);
					} else {
						String defaultDirectory = Config.getTicketAttachmentDefaultDirectoryLinux();
						defaultDirectory = defaultDirectory.replace("${HOME}", System.getProperty("user.home"));
						defaultDirectory = defaultDirectory.replace("$HOME", System.getProperty("user.home")).trim();
						zipFileFolder = new File(defaultDirectory);
					}

				    if (!zipFileFolder.exists()) {
				    	zipFileFolder.mkdirs();
				    }
				    File outputZipFile=new File(zipFileFolder,getTicketAttachmentDefaultNameWithoutExtension()+".zip");
				    final File defaultOutputZipFile = new File(zipFileFolder, outputZipFile.getName());
				    final JFileChooser zipFileChooser=new JFileChooser(zipFileFolder);
				    zipFileChooser.setDialogTitle("Save ticket attachment");
				    zipFileChooser.setFileFilter(new FileNameExtensionFilter("Zip", "zip"));
				    zipFileChooser.setSelectedFile(defaultOutputZipFile);

				    while(true) {
				    	final int saveFile = zipFileChooser.showSaveDialog(null);
				    	if (saveFile == JFileChooser.APPROVE_OPTION) {
				    		outputZipFile = zipFileChooser.getSelectedFile();
							if (outputZipFile.exists()) {
								final int overwrite = JOptionPane.showConfirmDialog(null, outputZipFile.getName()+" already exists. Do you want to replace it?", "Confirm to Overwrite", JOptionPane.YES_NO_OPTION);
								if (overwrite == JOptionPane.YES_OPTION) {
									break;
								} else {
									zipFileChooser.setSelectedFile(defaultOutputZipFile);
								}
							} else {
								break;
							}
				    	} else {
				    		return;
				    	}
				    }

				    outputZipFile = outputZipFile.getAbsoluteFile();

					if (!outputZipFile.getParentFile().exists()) {
						outputZipFile.getParentFile().mkdirs();
					}
					try {
						FileUtils.zipFiles(filesToSendAsTicketAttachment, outputZipFile);
						final String zipFilePath = outputZipFile.getPath();
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(zipFilePath), null);
						GUIUtils.popupInformationMessage("The attachment path has been copied to your clipboard.\nThe path is "+zipFilePath+""
								+ ".\n\nPlease attach that file when creating the support ticket!");
						TaskListUtils.startBrowser(null, url, true);
					} catch (Exception e1) {
						FileUtils.printStackTrace(e1);
						GUIUtils.popupErrorMessage(e1.getMessage());

					}
				} else {
					GUIUtils.popupErrorMessage("You don't have any file to attach");
				}
			}

		});
	}
	
	public abstract void showUpdatesFrame();

	public void showUpdatesFrame(String executableFileName)
	{
		try
		{
			if (patchFrame!=null) {
				patchFrame.dispose();
			}
		    PatchManager patchManager=new PatchManager(executableFileName);
			patchFrame=new PatchFrame(this,patchManager);
			patchFrame.setVisible(true);
		}
		catch (Exception e)
		{
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}

	public void switchToPanel(String panelId)
	{
		cardLayout.show(cardComponent, panelId);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					com.rapidesuite.client.common.util.Utils.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				rootFrame.repaint();
			}
		});

	}

	public void resetExecutionJobManagerList()
	{
		executionJobManager=null;
	}

	public boolean isReversalStopped()
	{
		return true;
	}

	public void manualStop() throws InterruptedException
	{
		if (executionJobManager !=null) {
			executionJobManager.manualStop();
		}
	}

	public JobManager getExecutionJobManager() {
		return executionJobManager;
	}

	public abstract void initEnvironment() throws Exception;

	public abstract void initExecutionPanel() throws Exception;

	public ExecutionPanel getExecutionPanel() {
		return executionPanel;
	}

	public String getApplicationMajorVersion() {
		int lastIndexOf= applicationVersion.lastIndexOf(".");
		return applicationVersion.substring(0, lastIndexOf);
	}

	public int getApplicationRevision() {
		int lastIndexOf= applicationVersion.lastIndexOf(".");
		return Integer.valueOf(applicationVersion.replaceAll("\n","").substring(lastIndexOf+1)).intValue();
	}

	public abstract Map<String,String> getEnvironmentPropertiesMap();

	protected Dimension getEffectiveScreenSize() {
		Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		Assert.notNull(r);
		Dimension output = r.getSize();
		return output;
	}


   private Map<String,Boolean> packageNameToContainsRscAuditColumnMap = new HashMap<String,Boolean>();

   public Map<String,Boolean> getPackageNameToContainsRscAuditColumnMap()
   {
       return this.packageNameToContainsRscAuditColumnMap;
   }

   public void checkUpdateSilently(final String executableName) {
		if (!Config.isAutomatedRun() && Config.isPatchingEnabled()) {
			final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						final PatchManager patchManager = new PatchManager(executableName);
						final Update latestUpdate = UpdateUtils.getPatch(getApplicationMajorVersion(), getApplicationRevision(), patchManager.getDownloadURL(), patchManager.getUserName(), patchManager.getPassword());
						if (latestUpdate != null) {
							final int proceedWithUpdate = JOptionPane.showConfirmDialog(getRootFrame(), "An update to Rapid Client is available. Do you want to update Rapid Client? (You will need to close all other Rapid Client applications beforehand)", "Update Available", JOptionPane.YES_NO_OPTION);
							if ( proceedWithUpdate == JOptionPane.YES_OPTION )
							{
								showUpdatesFrame();
							}
						}
					} catch (Throwable e) {
						final String message = "Checking for Updates to Rapid Client failed";
						GUIUtils.popupErrorMessage(message+ " : " + CoreUtil.getAllThrowableMessages(e));
						FileUtils.println(message);
						FileUtils.printStackTrace(e);
					}
				}

			};
			Thread t = new Thread(runnable);
			t.start();

		}
	}

   protected abstract void runAutomatically();

   public String getApplicationVersion() {
	   return this.applicationVersion;
   }
   protected abstract Set<File> getFilesToAttachInTicket();

   private String getTicketAttachmentDefaultNameWithoutExtension() {
	   final String output = "Rapid"+StringUtils.capitalize(this.shortApplicationName.toString())+this.applicationVersion+"-TicketSet-"+CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE.getDateFormat().format(new Date());
	   return output;
   }

   protected static Border getLeftOnlyBorderForFileNameAtMenuBar() {
	   return BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, Color.BLACK), BorderFactory.createEmptyBorder(0, 10, 0, 10));
   }

   public void setInformationLabelText(final String text, final int index) {
	   Assert.isTrue(0 <= index && index < informationLabelsAtMenuBar.size(), "informationLabelsAtMenuBar index is out of range");
	   informationLabelsAtMenuBar.get(index).setVisible(true);
	   informationLabelsAtMenuBar.get(index).setText(text);
	   informationLabelsAtMenuBar.get(index).setBorder(getLeftOnlyBorderForFileNameAtMenuBar());
   }

   public void clearAndHideInformationLabelText(final int index) {
	   Assert.isTrue(0 <= index && index < informationLabelsAtMenuBar.size(), "informationLabelsAtMenuBar index is out of range");
	   informationLabelsAtMenuBar.get(index).setVisible(false);
	   informationLabelsAtMenuBar.get(index).setText("");
   }

   public abstract String getApplicationIconPath();

   public List<JLabel> getInformationLabelsAtMenuBar() {
	   return informationLabelsAtMenuBar;
   }

   public int appendToInformationLabels() {
	   final JLabel label = new JLabel();
	   label.setVisible(false);
	   this.informationLabelsAtMenuBar.add(label);
	   this.menuBar.add(label);
	   return this.informationLabelsAtMenuBar.size()-1;
   }

   public int getBweNameLabelIndex() {
	   return bweNameLabelIndex;
   }

   protected abstract String getRootFrameTitle();
   
   private void showErrorPopupAndTerminateIfInstallationDirectoryIsTooLong() throws Exception {
	   if (Platform.isWindows()) {
		   if (Config.getMaxInstallationPathLengthWindows() < 30) {
			   GUIUtils.popupErrorMessage(Config.MAX_INSTALLATION_PATH_LENGTH_WINDOWS+" must be at least 30 letters. It is now "+Config.getMaxInstallationPathLengthWindows()+" letters");
			   System.exit(1);
		   }
		   
		   final String thisApplicationDirectoryPath = System.getProperty("user.dir");
		   final File thisApplicationDirectory = new File(thisApplicationDirectoryPath).getAbsoluteFile();
           final int thisApplicationDirectoryPathLength = thisApplicationDirectoryPath.length();
           
    	   final String applicationVersion=FileUtils.getApplicationVersion(this.getClass().getClassLoader(),UtilsConstants.APPLICATION_VERSION_FILE_NAME);   
    	   final String suggestedPath = "C:\\rapidclient-"+applicationVersion;
    	   final int suggestedPathLength = suggestedPath.length();     
    	   
    	   if (suggestedPathLength > Config.getMaxInstallationPathLengthWindows()) {
    		   FileUtils.printStackTrace(new Exception("The value of "+Config.MAX_INSTALLATION_PATH_LENGTH_WINDOWS+" ("+Config.getMaxInstallationPathLengthWindows()+" letters) is shorter than the suggested path length ("+suggestedPathLength+" letters). The suggestedPath path is '"+suggestedPath+"'"));
			   GUIUtils.popupErrorMessage("The value of "+Config.MAX_INSTALLATION_PATH_LENGTH_WINDOWS+" ("+Config.getMaxInstallationPathLengthWindows()+" letters) is shorter than the suggested path length ("+suggestedPathLength+" letters)");
			   System.exit(1);
    	   }
    	      	   
           if (thisApplicationDirectoryPathLength > Config.getMaxInstallationPathLengthWindows()) {
        	   FileUtils.println("Installation folder is too long");
        	   FileUtils.println("\tThe installation folder is '"+thisApplicationDirectoryPath+"' ("+thisApplicationDirectoryPathLength+" letters)");
        	   FileUtils.println("\tThe suggested new installation folder is '"+suggestedPath+"' ("+suggestedPathLength+" letters)");
        	   FileUtils.println("\tThe configured value of maximum path length for Windows is "+Config.getMaxInstallationPathLengthWindows()+" letters");
        	   
        	   String installationDirectoryHtml = "<span>"+thisApplicationDirectoryPath.substring(0, Math.min(thisApplicationDirectoryPath.length(), suggestedPathLength))+"</span>";
        	   if (thisApplicationDirectoryPath.length() > suggestedPathLength) {
        		   installationDirectoryHtml += "<span style='color:red;'>"+thisApplicationDirectoryPath.substring(suggestedPathLength)+"</span>";
        	   }
        			   
        	   GUIUtils.popupErrorMessage(
        			   "<html>"+
        					   "Your installation folder path is too long ("+thisApplicationDirectoryPathLength+" letters). It must not exceed "+Config.getMaxInstallationPathLengthWindows()+" letters. The folder path is</br>"+
        					   "<pre style='color:blue;'>"+installationDirectoryHtml+"</pre>"+
        					   "<span>Please move the contents of the <span style='font-style:italic;'>"+thisApplicationDirectory.getName()+"</span> folder to a shorter path, for example:</span><br/>"+
        					   "<pre style='color:blue;'>"+suggestedPath+"</pre>"+
        			   "</html>");
        	   System.exit(1);
           } 	
       }	   
   }
   
   protected static void onFatalErrorDuringApplicationInitialization(Throwable tr) {
	   try {
		   tr.printStackTrace();
		   FileUtils.printStackTrace(tr);
		   GUIUtils.popupErrorMessage("<html>"+CoreUtil.getAllThrowableMessagesHTML(tr)+"</html>");		   
	   }
	   finally {
		   System.exit(1);	
	   }
   }
   
   public void startSuicideWatch()
   {
       Thread t = new Thread(new Runnable(){

        @Override
        public void run()
        {
            try
            {
                File file = new File(Config.getTempFolder(),CoreConstants.SUICIDE_FILE);
                while ( true )
                {
                    if ( file.isFile() )
                    {
                        System.err.println("Ordered to terminate by external flag.");
                        System.exit(1);
                    }
                    Thread.sleep(1000);
                }
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }
        }
           
       });
       t.start();
   }
}