package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.balloontip.BalloonTip.Orientation;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.inject.gui.TabChangeListener;
import com.rapidesuite.inject.gui.TabbedPaneUI;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.UserInformation;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private SnapshotMain snapshotMain;
	private TabSnapshotsPanel tabSnapshotsPanel;
	private TabOptionsPanel tabOptionsPanel;
	JMenuItem inventoryMgtMenuItem;

	
		
	public MainPanel(SnapshotMain snapshotMain) {
		this.snapshotMain=snapshotMain;
		setLayout(new BorderLayout());
		createComponents();
		tabSnapshotsPanel.getServerSelectionPanel().refreshServerConnections(null);
	}
	
	public void createComponents(){
		final JTabbedPane jtp = new JTabbedPane();
		jtp.setOpaque(true);
		jtp.setBackground(Color.decode("#343836"));
		 ChangeListener changeListener = new ChangeListener() {
		      public void stateChanged(ChangeEvent changeEvent) {
		        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
		        int index = sourceTabbedPane.getSelectedIndex();
		        if (index==2) {
		        	checkPermissionForOptionsTab();
		        	if (!UIConstants.BALLOON_OPTIONS_TRIGGERED) {
		        		UIConstants.BALLOON_OPTIONS_TRIGGERED=true;
		        		UIUtils.showBalloon(tabOptionsPanel.getWorkersTextField(),
		        				"<html>The <b>Worker count</b> controls how many queries will run in parallel.<br/><b>Note that it may impact the"+
	        					" performance of your EBS instance if it is set too high.</b><br/>Please confirm with your DBA if you want to change the default value."
		        				,Orientation.RIGHT_BELOW, tabOptionsPanel.isShowBalloons());
		        	
		        		UIUtils.showBalloon(tabOptionsPanel.getShowTotalsDetailsCheckbox(),
			        			"<html>This will cause the computation of the Default, Added and Updated records count "+
		        		"for each of your snapshot.<br/><b>Note that it may be slow if your"+
			        	" snapshots contain a lot of Configurations.</b>",
			        	tabOptionsPanel.isShowBalloons());
		        		
		        		UIUtils.showBalloon(tabOptionsPanel.getSeededUsersTextArea(),
			        			"<html>This Information is used to define what Configurations are considered as 'Default' versus 'Added/ Updated'.<br/>"+
			        	"All configurations created by the following Oracle users are considered as 'Default' (seeded data).<br/>"+
		        		"<b>Modify only if you know what you are doing.</b>",
		        		tabOptionsPanel.isShowBalloons());
		        		
			        	UIUtils.showBalloon(tabOptionsPanel.getSaveOptions(),
			        			"<html>Save your settings for the next restart!",
			        			tabOptionsPanel.isShowBalloons());
		        	}
		        	
		        	
		        }
		      }
		      
		    };
		jtp.addChangeListener(changeListener);

		String tabName="";
		int tabIndex=0;
		int tabWidth=120;
		int tabHeight=50;

		ImageIcon ii=null;
		URL iconURL =null;
		
		JButton menuButton =new JButton();		
		iconURL = this.getClass().getResource("/images/snapshot/icon_menu.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		menuButton.setIcon(ii);
		menuButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/icon_menu_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		menuButton.setRolloverIcon(new RolloverIcon(ii));
		menuButton.setOpaque(false);
		Dimension dim=new Dimension(50,50);
		menuButton.setSize(dim);
		menuButton.setFocusPainted(true);
		menuButton.setBorderPainted(false);
		menuButton.setContentAreaFilled(false);
		
		JPanel panel=new JPanel();
		panel.setOpaque(true);
		panel.setBackground(Color.red);
		panel.add(menuButton);	
		
		jtp.addTab("",new JPanel());
		jtp.setTabComponentAt(tabIndex, menuButton);
		jtp.setEnabledAt(tabIndex,false);
		
		int menuItemsHeight=160;
		UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(ExecutionPanel.BLUE_BACKGROUND_COLOR, 0));
		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(BorderFactory.createLineBorder(ExecutionPanel.BLUE_BACKGROUND_COLOR,1));

		final JMenu adminMenu = new JMenu("Admin Menu");
		adminMenu.setOpaque(true);
		adminMenu.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		adminMenu.setForeground(Color.decode("#FFFFFF") );
		adminMenu.setBackground(Color.decode("#343836"));
		adminMenu.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
	

		JMenuItem supportTicketMenuItem = new JMenuItem("Raise a Support Ticket");
		snapshotMain.addSupportTicketListener(supportTicketMenuItem,SnapshotMain.SUPPORT_TICKET_SITE_URL);
		supportTicketMenuItem.setOpaque(true);
		supportTicketMenuItem.setPreferredSize(new Dimension(200, tabHeight));
		supportTicketMenuItem.setForeground(Color.decode("#FFFFFF") );
		supportTicketMenuItem.setBackground(Color.decode("#343836"));
		supportTicketMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		
		JMenuItem schemaMgtMenuItem = new JMenuItem();
		schemaMgtMenuItem.setText("Schema Management");
		schemaMgtMenuItem.setOpaque(true);
		schemaMgtMenuItem.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		schemaMgtMenuItem.setForeground(Color.decode("#FFFFFF") );
		schemaMgtMenuItem.setBackground(Color.decode("#343836"));
		schemaMgtMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		schemaMgtMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openSchemaMgtDialogWindow();
			}
		});
		
		JMenuItem userMgtMenuItem = new JMenuItem();
		userMgtMenuItem.setText("User Management");
		userMgtMenuItem.setOpaque(true);
		userMgtMenuItem.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		userMgtMenuItem.setForeground(Color.decode("#FFFFFF") );
		userMgtMenuItem.setBackground(Color.decode("#343836"));
		userMgtMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		userMgtMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openUserMgtDialogWindow();
			}
		});
		
		inventoryMgtMenuItem = new JMenuItem();
		inventoryMgtMenuItem.setText("Inventory Management");
		inventoryMgtMenuItem.setOpaque(true);
		inventoryMgtMenuItem.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		inventoryMgtMenuItem.setForeground(Color.decode("#AAAAAA") );
		inventoryMgtMenuItem.setBackground(Color.decode("#909090"));
		inventoryMgtMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		inventoryMgtMenuItem.setEnabled(false);
		inventoryMgtMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openInventoryMgtDialogWindow();
			}
		});
		
		JMenuItem registerMenuItem = new JMenuItem();
		registerMenuItem.setText("Register");
		registerMenuItem.setOpaque(true);
		registerMenuItem.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		registerMenuItem.setForeground(Color.decode("#FFFFFF") );
		registerMenuItem.setBackground(Color.decode("#343836"));
		registerMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		registerMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionOpenRegistrationWindow();
			}
		});
		
		JMenuItem userGuideMenuItem = new JMenuItem("User Guide");
		userGuideMenuItem.setOpaque(true);
		userGuideMenuItem.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		userGuideMenuItem.setForeground(Color.decode("#FFFFFF") );
		userGuideMenuItem.setBackground(Color.decode("#343836"));
		userGuideMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		userGuideMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processActionOpenUserGuide();
			}
		});
		
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setOpaque(true);
		exitMenuItem.setPreferredSize(new Dimension(menuItemsHeight, tabHeight));
		exitMenuItem.setForeground(Color.decode("#FFFFFF") );
		exitMenuItem.setBackground(Color.decode("#343836"));
		exitMenuItem.setFont( new Font( "Arial", Font.PLAIN, ExecutionPanel.FONT_SIZE_BIG ) );
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				snapshotMain.close();
			}
		});
		
		popupMenu.add(supportTicketMenuItem);
		adminMenu.add(schemaMgtMenuItem);
		adminMenu.add(inventoryMgtMenuItem);
		popupMenu.add(adminMenu);
		popupMenu.add(userMgtMenuItem);
		popupMenu.add(registerMenuItem);
		popupMenu.add(userGuideMenuItem);
		popupMenu.add(exitMenuItem);
		
        
		menuButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                  Component source = (Component)evt.getSource(); 
                  Dimension size = source.getSize(); 
                  int xPos = ((size.width - popupMenu.getPreferredSize().width) / 2); 
                  int yPos = size.height;
                  popupMenu.show(source, xPos, yPos);
            }
        });
		
		tabIndex++;

		tabOptionsPanel=new TabOptionsPanel(this);
		tabSnapshotsPanel=new TabSnapshotsPanel(this);
		
		tabName="SNAPSHOTS";
		InjectUtils.addTab(jtp,tabName,tabSnapshotsPanel,tabIndex,tabWidth,tabHeight,true);
		tabIndex++;

		tabName="OPTIONS";
		InjectUtils.addTab(jtp,tabName,tabOptionsPanel,tabIndex,tabWidth,tabHeight,true);
		tabIndex++;
				
		int tabIndexGreaterThanNotDisplayWhiteBar=2;
		jtp.setUI(new TabbedPaneUI(tabIndexGreaterThanNotDisplayWhiteBar));
		jtp.addChangeListener(new TabChangeListener(jtp));
		jtp.setSelectedIndex(1);
		jtp.setEnabledAt(0, false);
		add(jtp,BorderLayout.CENTER);
		
		Container glassPane = (Container) snapshotMain.getRootFrame().getRootPane().getGlassPane();
		glassPane.setVisible(true);
		glassPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 15);
		gbc.anchor = GridBagConstraints.NORTHEAST;
		iconURL = jtp.getClass().getResource("/images/Logo_rapid.png");
		ImageIcon imageIconLogo=null;
		try{ imageIconLogo=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		int logoWidth=105;
		int logoHeight=48;
		JLabel logoLabel = new JLabel(imageIconLogo);
		logoLabel.setMinimumSize( new Dimension(logoWidth,logoHeight));
		logoLabel.setMaximumSize( new Dimension(logoWidth,logoHeight));
		logoLabel.setPreferredSize(new Dimension(logoWidth,logoHeight));
		logoLabel.setHorizontalAlignment(JLabel.RIGHT);
		glassPane.add(logoLabel, gbc); 
		
		UIUtils.showBalloon(menuButton, "<html>Ask your DBA to use the <b>'Schema Management'</b> menu to setup<br/>the Working area on your Oracle Instances."
				+ " <br/>Or<br/>Open the <b>'User Guide'</b> for more help!", tabOptionsPanel.isShowBalloons());
		//TimingUtils.showTimedBalloon(balloonTip, 10000);
		//FadingUtils.fadeInBalloon(myBalloonTip, null, 5000, 24);
		//FadingUtils.fadeOutBalloon(myBalloonTip, null, 5000, 24);
	}
	
	protected void openUserMgtDialogWindow() {
		if  (tabSnapshotsPanel.getServerSelectionPanel().isConnected()) {
			int width=1000;
			int height=650;
			UserManagementPanel userManagementPanel=new UserManagementPanel(tabSnapshotsPanel);
			JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindow(tabSnapshotsPanel.
					getMainPanel().getSnapshotMain().getRootFrame(),"User Management",width,height,
					userManagementPanel,null,true,SnapshotMain.getSharedApplicationIconPath());
			userManagementPanel.setDialog(dialog);
			dialog.setVisible(true);
		}
		else {
			GUIUtils.popupInformationMessage("You must first connect to a schema!");
		}
	}

	protected void processActionOpenUserGuide() {
		try{
			File file=new File("docs"+File.separator+"rapidsnapshot"+File.separator+"RapidSnapshot - User Guide.pdf");
			Desktop.getDesktop().open(file);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}	public SnapshotMain getSnapshotMain() {
		return snapshotMain;
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public TabOptionsPanel getTabOptionsPanel() {
		return tabOptionsPanel;
	}

	public void openSchemaMgtDialogWindow() {
		int width=750;
		int height=650;
		AdminPasswordPanel adminPasswordPanel=new AdminPasswordPanel(tabSnapshotsPanel);
		JDialog dialog=UIUtils.displayOperationInProgressComplexModalWindow(tabSnapshotsPanel.
				getMainPanel().getSnapshotMain().getRootFrame(),"Schema creation",width,height,
				adminPasswordPanel,null,true,SnapshotMain.getSharedApplicationIconPath());
		adminPasswordPanel.setDialog(dialog);
		dialog.setVisible(true);
	}

	protected void processActionOpenRegistrationWindow() {
		ModelUtils.createRegistrationWindow(snapshotMain.getRootFrame(),CoreConstants.SHORT_APPLICATION_NAME.snapshot.toString(),true,true,
				SnapshotMain.REGISTRATION_VIDEO_URL,SnapshotMain.getSharedApplicationIconPath());
	}
	
	public void openSchemaManagementWindowIfNoConnections() {
		File serverConnectionsFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getServerConnectionsFolder();
		if ( ! hasServerConnections(serverConnectionsFolder)){
			openSchemaMgtDialogWindow();
		}
	}
	
	public static boolean hasServerConnections(File serverConnectionsFolder) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(SnapshotMain.SNAPSHOT_ENVIRONMENT_FILE_EXTENSION.toLowerCase());
			}
		};
		File[] listOfFiles = serverConnectionsFolder.listFiles(filter);
		if (listOfFiles!=null && listOfFiles.length>0) {
			return true;
		}
		return false;
	}

	public JMenuItem getInventoryMgtMenuItem() {
		return inventoryMgtMenuItem;
	}
	protected void openInventoryMgtDialogWindow() {
		if  (tabSnapshotsPanel.getServerSelectionPanel().isConnected()) {			
			try{
				if ( ! tabSnapshotsPanel.getServerSelectionPanel().hasUserManagerRole()) {
					String errMsg = "You are not allowed to manage inventories!";
					GUIUtils.popupErrorMessage(getSnapshotMain().getRootFrame(),errMsg);
					return;
				}	
				SnapshotInventoryManagementFrame snapshotInventoryManagementFrame =new SnapshotInventoryManagementFrame(tabSnapshotsPanel);
				snapshotInventoryManagementFrame.setVisible(true);
			}catch(Exception e) {
				String errMsg = "Unexpected error: "+e.getMessage();
				GUIUtils.popupErrorMessage(errMsg);
				return;
			}
		}
		else {
			GUIUtils.popupInformationMessage("You must first connect to a schema!");
		}
	}
	public void checkPermissionForOptionsTab(){
		Map<String,Boolean> permissionOnOptionsTabMap = new HashMap<String,Boolean>();
		if  (tabSnapshotsPanel.getServerSelectionPanel().isConnected()) {			
			try{
				Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
				List<UserInformation> userInformationList=ModelUtils.getSnapshotUserInformationList(snapshotEnvironmentProperties);
				if(!userInformationList.isEmpty()){
					if(tabSnapshotsPanel.getServerSelectionPanel().getSelectedUser()==null){
						tabSnapshotsPanel.getServerSelectionPanel().userLogin();
					}
					if (tabSnapshotsPanel.getServerSelectionPanel().getSelectedUser()==null) { 
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_WORKERS,false);
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_CONNECT_FOLDR,false);
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_DOWNLD_FOLDR,false);
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_TMP_FOLDR,false);
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_SNP_PRFX,false);
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_RESET_OPTN,false);
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_DELETE_OPTION,false);
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_TOTAL_DETAIL_OPTION,false);
					}
					else {
						UserInformation selectedUser = tabSnapshotsPanel.getServerSelectionPanel().getSelectedUser();
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_WORKERS,selectedUser.isEnableDefaultParallelWorkersField());
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_CONNECT_FOLDR,selectedUser.isEnableServerConnectionsDefaultFolderButton());
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_DOWNLD_FOLDR,selectedUser.isEnableDownloadFolderButton());
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_TMP_FOLDR,selectedUser.isEnableTemplateFolderButton());
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_SNP_PRFX,selectedUser.isEnableDefaultSnapshotNamePrefixField());
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_RESET_OPTN,selectedUser.isEnableResetOptionButton());
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_DELETE_OPTION,selectedUser.isEnableDeleteOptionCheckBox());
						permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_TOTAL_DETAIL_OPTION,selectedUser.isEnableDisplayTotalDetailOption());
					}

				}else{
					permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_WORKERS,true);
					permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_CONNECT_FOLDR,true);
					permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_DOWNLD_FOLDR,true);
					permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_TMP_FOLDR,true);
					permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_SNP_PRFX,true);
					permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_RESET_OPTN,true);
					permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_DELETE_OPTION,true);
					permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_TOTAL_DETAIL_OPTION,true);
				}
				setPermissionToComponent(permissionOnOptionsTabMap);
			}catch(Exception e) {
				String errMsg = "Unexpected error: "+e.getMessage();
				GUIUtils.popupErrorMessage(errMsg);
				return;
			}
		}
		else {
			GUIUtils.popupInformationMessage("You must first connect to a schema!");
			permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_WORKERS,false);
			permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_CONNECT_FOLDR,false);
			permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_DOWNLD_FOLDR,false);
			permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_TMP_FOLDR,false);
			permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_SNP_PRFX,false);
			permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_RESET_OPTN,false);
			permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_DELETE_OPTION,false);
			permissionOnOptionsTabMap.put(ModelUtils.IS_ENABLED_TOTAL_DETAIL_OPTION,false);
			setPermissionToComponent(permissionOnOptionsTabMap);
		}
		try{
			tabOptionsPanel.setValues();
		}catch(Exception e2){
			String errMsg = "Unexpected error: "+e2.getMessage();
			GUIUtils.popupErrorMessage(errMsg);
			return;			
		}
		
	}
	
	public void setPermissionToComponent(Map<String,Boolean> permissionOnOptionsTabMap){
		boolean isEnable = permissionOnOptionsTabMap.get(ModelUtils.IS_ENABLED_WORKERS);
		tabOptionsPanel.setEnableOnWorkersTextFiled(isEnable);
		isEnable = permissionOnOptionsTabMap.get(ModelUtils.IS_ENABLED_CONNECT_FOLDR);
		tabOptionsPanel.setEnableOnDefaultServerInformationFolderButton(isEnable);
		isEnable = permissionOnOptionsTabMap.get(ModelUtils.IS_ENABLED_DOWNLD_FOLDR);
		tabOptionsPanel.setEnableOnDownloadFolderButton(isEnable);
		isEnable = permissionOnOptionsTabMap.get(ModelUtils.IS_ENABLED_TMP_FOLDR);
		tabOptionsPanel.setEnableOnTemplateFolderButton(isEnable);
		isEnable = permissionOnOptionsTabMap.get(ModelUtils.IS_ENABLED_SNP_PRFX);
		tabOptionsPanel.setEnableOnDefaultSnapshotNameTextField(isEnable);
		isEnable = permissionOnOptionsTabMap.get(ModelUtils.IS_ENABLED_RESET_OPTN);
		tabOptionsPanel.setEnableOnResetOptionsButton(isEnable);
		isEnable = permissionOnOptionsTabMap.get(ModelUtils.IS_ENABLED_DELETE_OPTION);
		tabOptionsPanel.setEnableOnDeleteOption(isEnable);
		isEnable = permissionOnOptionsTabMap.get(ModelUtils.IS_ENABLED_TOTAL_DETAIL_OPTION);
		tabOptionsPanel.setEnableOnShowTotalsDetailsCheckbox(isEnable);
	}
	
}
