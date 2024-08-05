/**************************************************
 * $Revision: 54554 $:
 * $Author: jannarong.wadthong $:
 * $Date: 2016-04-21 11:29:52 +0700 (Thu, 21 Apr 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/gui/DataExtractionPanel.java $:
 * $Id: DataExtractionPanel.java 54554 2016-04-21 04:29:52Z jannarong.wadthong $:
 */

package com.rapidesuite.reverse.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.springframework.util.Assert;

import com.rapidesuite.client.common.gui.ExecutionPanel;
import com.rapidesuite.client.common.gui.InventoriesTreePanel;
import com.rapidesuite.client.common.gui.InventoryTreeNode;
import com.rapidesuite.client.common.gui.TemplateSelectionPanel;
import com.rapidesuite.client.common.gui.TextTreeMouseListener;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.reverse.ReverseMain;

@SuppressWarnings("serial")
public class DataExtractionPanel extends ExecutionPanel
{

	private ReverseMain ReverseMain;
	private JTabbedPane tabbedPane;
	private InventoriesTreePanel inventoriesTreePanel;
	private DataExtractionEBSLevelSelectionPanel dataExtractionEBSLevelSelectionPanel;
	
	private DataExtractionOptionsPanel dataExtractionOptionsPanel;
	private OracleUsersSelectionPanel oracleUsersSelectionPanel;
	private OracleDatesSelectionPanel oracleDatesSelectionPanel;
	private TemplateSelectionPanel templateSelectionPanel;
	private String instanceName;
	private File reverseTreeExportAllStatusesFile;
	private File reverseTreeExportOnlyErrorFile;
	
	public DataExtractionPanel(ReverseMain ReverseMain)
	{
		super(ReverseMain);
		createComponents();
	}

	public void createComponents()
	{
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		dataExtractionEBSLevelSelectionPanel=new DataExtractionEBSLevelSelectionPanel(this);
		
		DataExtractionInventoriesCheckTreeCellRenderer dataExtractionInventoriesCheckTreeCellRenderer=new DataExtractionInventoriesCheckTreeCellRenderer(this);
		
		inventoriesTreePanel= new InventoriesTreePanel(this,true,dataExtractionInventoriesCheckTreeCellRenderer);
		tempPanel.add(dataExtractionEBSLevelSelectionPanel.getInnerPanel());
		tempPanel.add(inventoriesTreePanel);
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Inventories", null,tempPanel,"Inventories selection");
		oracleUsersSelectionPanel=new OracleUsersSelectionPanel(this);
		tabbedPane.addTab("Filtering by Oracle users", null,oracleUsersSelectionPanel ,"Oracle Users selection");
		oracleDatesSelectionPanel=new OracleDatesSelectionPanel(this);
		tabbedPane.addTab("Filtering by Date", null,oracleDatesSelectionPanel ,"Oracle Dates selection");
		dataExtractionOptionsPanel=new DataExtractionOptionsPanel(this);
		tabbedPane.addTab("Options", null,dataExtractionOptionsPanel ,"Options selection");
		executionStatusPanel=new DataExtractionStatusPanel(this);
		tabbedPane.addTab("Extraction Status", null,executionStatusPanel ,"Extraction Status");
				
		/*
		 * adding components to panels:
		 */
		setLayout(new BorderLayout());
		
		JPanel northPanel= new JPanel();
		//northPanel.setBackground(Color.RED);
		northPanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));  
		add(northPanel, BorderLayout.NORTH);

		templateSelectionPanel=new DataExtractionTemplateSelectionPanel(this);
		
		northPanel.add(templateSelectionPanel);
		
		JPanel centerPanel= new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));  
		add(centerPanel, BorderLayout.CENTER);
		centerPanel.add(tabbedPane);

		JPanel southPanel= new JPanel();
		southPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));  
		add(southPanel, BorderLayout.SOUTH);

		JPanel panel= new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));  
		//panel.setBackground(Color.BLUE);
		southPanel.add(panel);
		panel.add(statusLabel); 

		panel= new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));  
		//panel.setBackground(Color.BLUE);
		southPanel.add(panel);
		panel.add(backJButton);
		panel.add(Box.createRigidArea(new Dimension(50,10)));
		panel.add(startJButton); 
		panel.add(stopJButton); 
	}

	public void lockAll() {
		super.lockAll();
		inventoriesTreePanel.lockAll();
		templateSelectionPanel.lockAll();
		oracleDatesSelectionPanel.lockAll();
		oracleUsersSelectionPanel.lockAll();	
		dataExtractionOptionsPanel.lockAll();
		executionStatusPanel.setErrorsOnlyCheckBox(false);
		executionStatusPanel.lockAll();
	}

	public void unlockAll() {
		super.unlockAll();
		inventoriesTreePanel.unlockAll();
		templateSelectionPanel.unlockAll();
		oracleDatesSelectionPanel.unlockAll();
		oracleUsersSelectionPanel.unlockAll();	
		dataExtractionOptionsPanel.unlockAll();
		executionStatusPanel.setErrorsOnlyCheckBox(true);
		executionStatusPanel.unlockAll();
		TextTreeMouseListener.refreshTree(inventoriesTreePanel.getTree());
	}
	
	public void startExecution() throws Exception
	{
		this.executionStatusPanel.clearSelectedRowLabel();
//		if (dataExtractionOptionsPanel.isAuditExtractionEnabled() && 	!getOracleUsersSelectionPanel().isStandardOracleUserLoaded()	) 
//		{
//			GUIUtils.popupErrorMessage("Audit extraction is enabled and Oracle users loading is still in progress, please wait until completion...");
//			return;
//		}
		List<Long> ouIds=dataExtractionEBSLevelSelectionPanel.getOperatingUnitSelectionPanel().getSelectedOperatingUnitIds();
		if  (  (ouIds==null || ouIds.isEmpty()) &&	dataExtractionEBSLevelSelectionPanel.isOperatingUnitLevelSelected() ){
			GUIUtils.popupErrorMessage("Please, select at least one operating unit to reverse.");
			return;
		}
		List<InventoryTreeNode> inventoryTreeNodes=inventoriesTreePanel.getSelectedInventoryTreeNodes();
		if (inventoryTreeNodes.isEmpty()) 
		{
			GUIUtils.popupErrorMessage("No inventories selected!");
			return;
		}		
		GUIUtils.showInProgressMessage(statusLabel,"Launching workers...");
		((ReverseMain)getSwiftGUIMain()).startReverseExecution(inventoryTreeNodes);
		lockAll();
		stopJButton.setEnabled(true);
		tabbedPane.setSelectedIndex(4);
	}
	
	public void stopExecution() throws Exception
	{
		GUIUtils.showInProgressMessage(getStatusLabel(),"Manual stop, cleaning up current jobs...");
		getSwiftGUIMain().manualStop();
		stopJButton.setEnabled(false);
		backJButton.setEnabled(false);
		Runnable r=new Runnable(){
    		public void run(){
    			while (getSwiftGUIMain().getExecutionJobManager().hasRemainingTasks()) {
    				try {
						com.rapidesuite.client.common.util.Utils.sleep(1000);
					} catch (InterruptedException e) {
						FileUtils.printStackTrace(e);
						break;
					}
    			}
    			unlockAll();
    		}
    	};
    	Thread t=new Thread(r);
    	t.start();
	}
	
	public void loadSeededOracleUsers()  {
		oracleUsersSelectionPanel.loadSeededOracleUsers();
	}
	
	public void loadInstanceName()  {
		try{
			instanceName=DatabaseUtils.getRscSourceKey(((ReverseMain)ReverseMain).getDataExtractionEBSPropertiesValidationPanel().
					getEnvironmentValidationPanel().getEnvironmentPropertiesMap());
		}
		catch(Exception e){
			FileUtils.printStackTrace(e);
		}
	}

	public boolean isAnalyseMode() {
		return dataExtractionOptionsPanel.isAnalyseMode();
	}
		
	public DataExtractionOptionsPanel getDataExtractionOptionsPanel() {
		return dataExtractionOptionsPanel;
	}

	public OracleDatesSelectionPanel getOracleDatesSelectionPanel() {
		return oracleDatesSelectionPanel;
	}

	public OracleUsersSelectionPanel getOracleUsersSelectionPanel() {
		return oracleUsersSelectionPanel;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public DataExtractionStatusPanel getReverseExecutionStatusPanel() {
		return (DataExtractionStatusPanel)executionStatusPanel;
	}

	public TemplateSelectionPanel getTemplateSelectionPanel() {
		return templateSelectionPanel;
	}

	public InventoriesTreePanel getInventoriesTreePanel() {
		return inventoriesTreePanel;
	}
	
	public void reloadTree() {
		try {
			inventoriesTreePanel.reloadTree(getSwiftGUIMain().getRootFrame(),((ReverseMain)getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().getInventoryTree(true),false);
		} 
		catch (Exception e1) {
			FileUtils.printStackTrace(e1);
			GUIUtils.popupErrorMessage(e1.getMessage());
		}
	}

	public DataExtractionEBSLevelSelectionPanel getDataExtractionEBSLevelSelectionPanel() {
		return dataExtractionEBSLevelSelectionPanel;
	}

	@Override
	public boolean operate() throws Throwable {
		Assert.isTrue(Config.isAutomatedRun(), "must be in automated run mode");	
		
		//wait until initialization finished
		while (!this.isVisible() || this.dataExtractionEBSLevelSelectionPanel.getOperatingUnitSelectionPanel() == null
				|| this.startJButton == null || !this.startJButton.isEnabled()
				|| this.inventoriesTreePanel == null || this.inventoriesTreePanel.getCheckTreeManager() == null
				|| this.inventoriesTreePanel.getSelectedTextTreeNodes() == null || this.onStartButtonIsClicked == null) {
			com.rapidesuite.client.common.util.Utils.sleep(UtilsConstants.REVERSE_VALIDATE_AUTOMATED_RUN_WAITING_TIME_PERIOD_MS);
		}
		
		if (UtilsConstants.NO_TEMPLATE_CHOSEN_OPTION.equals(this.templateSelectionPanel.getSelectedTemplate())) {
			final List<Component> checkboxes = this.dataExtractionEBSLevelSelectionPanel.getOperatingUnitSelectionPanel().getAllComponents();
			for (final Component checkboxComp : checkboxes) {
				if (checkboxComp instanceof JCheckBox) {
					final JCheckBox checkbox = (JCheckBox) checkboxComp;
					checkbox.setSelected(true);
				}
			}			
		}
		

		
		onStartButtonIsClicked.actionPerformed(null);
		
		//the start button is disabled during processing
		while (!this.startJButton.isEnabled()) {
			com.rapidesuite.client.common.util.Utils.sleep(UtilsConstants.REVERSE_VALIDATE_AUTOMATED_RUN_WAITING_TIME_PERIOD_MS);		
		}
		
		if (this.executionStatusPanel instanceof DataExtractionStatusPanel) {
			final DataExtractionStatusPanel dataExtractionStatusPanel = (DataExtractionStatusPanel) this.executionStatusPanel;
			dataExtractionStatusPanel.simulateClickingExportTreeButton();
		}
		
		boolean output = false;
		
		if (getSwiftGUIMain() != null && getSwiftGUIMain().getExecutionJobManager() != null) {
			output = !getSwiftGUIMain().getExecutionJobManager().isManualStopped();
		}
		
		return output;		
	}

	public File getReverseTreeExportAllStatusesFile() {
		return reverseTreeExportAllStatusesFile;
	}

	public void setReverseTreeExportAllStatusesFile(
			File reverseTreeExportAllStatusesFile) {
		this.reverseTreeExportAllStatusesFile = reverseTreeExportAllStatusesFile;
	}

	public File getReverseTreeExportOnlyErrorFile() {
		return reverseTreeExportOnlyErrorFile;
	}

	public void setReverseTreeExportOnlyErrorFile(
			File reverseTreeExportOnlyErrorFile) {
		this.reverseTreeExportOnlyErrorFile = reverseTreeExportOnlyErrorFile;
	}
	
}