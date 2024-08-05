package com.rapidesuite.inject.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.ScriptManager;
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public class ExecutionPanel extends JPanel {

	public final static Color EXECUTION_SCREEN_NORTH_PANEL_BACKGROUND_COLOR=Color.decode("#4B4F4E");
	public final static Color BLUE_BACKGROUND_COLOR=Color.decode("#047FC0");
	public final static Color GREY_BACKGROUND_COLOR=Color.decode("#FAFCFC");

	public static int FONT_SIZE_SMALL=12;
	public static int FONT_SIZE_NORMAL=12;
	public static int FONT_SIZE_BIG=12;

	private InjectMain injectMain;
	private OptionsTabPanel optionsTabPanel;
	private ExecutionTabPanel executionTabPanel;
	
	public ExecutionPanel(InjectMain injectMain)	throws Exception {
		this.injectMain=injectMain;
		createComponents();
	}

	public void createComponents() throws Exception{
		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		JPanel mainPanel=new JPanel();
		this.add(mainPanel);
		
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		mainPanel.setOpaque(true);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.decode("#343836") );

		/*
		 * MENU BARS
		 */

		final JTabbedPane jtp = new JTabbedPane();
		jtp.setOpaque(false);

		String tabName="";
		int tabIndex=0;
		int tabWidth=150;
		int tabHeight=50;

		InjectUtils.addMenuTab(jtp,injectMain,CoreConstants.SHORT_APPLICATION_NAME.inject.toString(),tabIndex,tabHeight);
		tabIndex++;

		tabName="EXECUTION";
		executionTabPanel=new ExecutionTabPanel(this);
		InjectUtils.addTab(jtp,tabName,executionTabPanel,tabIndex,tabWidth,tabHeight,true);
		tabIndex++;

		tabName="OPTIONS";
		optionsTabPanel=new OptionsTabPanel(this);
		InjectUtils.addTab(jtp,tabName,optionsTabPanel,tabIndex,tabWidth,tabHeight,true);
		tabIndex++;

		int tabIndexGreaterThanNotDisplayWhiteBar=2;
		jtp.setUI(new TabbedPaneUI(tabIndexGreaterThanNotDisplayWhiteBar));
		jtp.addChangeListener(new TabChangeListener(jtp));
		jtp.setSelectedIndex(1);
		jtp.setEnabledAt(0, false);
		mainPanel.add(jtp);
		
		InjectUtils.addLogo(jtp,injectMain);
	}

	public void unlockUI()
	{
		optionsTabPanel.unlockUI();
		executionTabPanel.unlockUI();
		injectMain.getApplicationInfoPanel().getSelectPackageButton().setEnabled(true);
	}
	
	public void lockUI()
	{
		optionsTabPanel.lockUI();
		executionTabPanel.lockUI();
		injectMain.getApplicationInfoPanel().getSelectPackageButton().setEnabled(false);
	}

	public InjectMain getInjectMain() {
		return injectMain;
	}

	public ScriptManager getScriptManager() {
		return executionTabPanel.getScriptManager();
	}

	public OptionsTabPanel getOptionsTabPanel() {
		return optionsTabPanel;
	}

	public ExecutionTabPanel getExecutionTabPanel() {
		return executionTabPanel;
	}	

}
