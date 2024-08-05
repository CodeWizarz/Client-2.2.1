package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.SnapshotCreationController;
import com.rapidesuite.snapshot.model.InitCreationGridSwingWorker;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class SnapshotCreationFrame extends SnapshotCreationGenericFrame {

	private JLabel snapshotNameLabel;
	private JLabel selectedFormsCountLabel;
	private JLabel snapshotNameValueLabel;
	private JLabel selectedFormsCountValueLabel;
	private JLabel snapshotIdLabel;
	private JLabel snapshotIdValueLabel;
	private JLabel executionStatusLabel;
	private JLabel executionProgressLabel;
	private JLabel executionFailedRowsCountLabel;
	private JLabel executionFailedRowsCountLabelValue;
	private JComboBox<String> templateSelectionComboBox;
	private JButton templateCreationButton;
	private JButton templateDeleteButton;
	private JCheckBox isSnapshotForConversionCheckbox;
	private boolean isApplyTemplateSuccessfully = false;
	private boolean isInitialInventoriesSuccessfully = false;
	
	private FilterModulePanel filterModulePanel;

	public static final int FRAME_WIDTH=1700;
	public static final int FRAME_HEIGHT=760;
	
	private JCheckBox autoFreshFilteringCheckbox;
	
	public SnapshotCreationFrame(TabSnapshotsPanel tabSnapshotsPanel) {
		super(tabSnapshotsPanel);
		((JComponent)getContentPane()).setOpaque(true);
		setBackground(Color.decode("#4b4f4e"));
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
		setTitle(UIConstants.FRAME_TITLE_PREFIX+" - Snapshot creation for '"+tabSnapshotsPanel.getServerSelectionPanel().getSelectedServerConnection()+"'");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLayout(new BorderLayout());
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	closeWindow();
	        }
	    });
		createComponents();
		runDefaultProcess();
	}
	
	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
		northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(northPanel,BorderLayout.NORTH);
		
		JPanel northSubLeftPanel=new JPanel();
		northPanel.add(northSubLeftPanel);
		northSubLeftPanel.setOpaque(false);
		northSubLeftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		northSubLeftPanel.setLayout(new BoxLayout(northSubLeftPanel, BoxLayout.Y_AXIS));
		
		JPanel northSubRightPanel=new JPanel();
		northSubRightPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		northSubRightPanel.setOpaque(false);
		northSubRightPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		northSubRightPanel.setLayout(new BoxLayout(northSubRightPanel, BoxLayout.Y_AXIS));
		northPanel.add(northSubRightPanel);
		
		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northSubRightPanel.add(tempPanel);
		JLabel templateSelectionLabel=new JLabel("Template selection:");
		InjectUtils.assignArialPlainFont(templateSelectionLabel,InjectMain.FONT_SIZE_NORMAL);
		templateSelectionLabel.setForeground(Color.white);
		tempPanel.add(templateSelectionLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(3, 15)));
		templateSelectionComboBox = new JComboBox<String>();
		templateSelectionComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadTemplate();
			}
		});
		tempPanel.add(templateSelectionComboBox);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_save_template.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateCreationButton = new JButton();
		templateCreationButton.setIcon(ii);
		templateCreationButton.setBorderPainted(false);
		templateCreationButton.setContentAreaFilled(false);
		templateCreationButton.setFocusPainted(false);
		templateCreationButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_template_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateCreationButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(templateCreationButton);
		templateCreationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				processActionSaveNewTemplate();
			}
		});
		
		iconURL = this.getClass().getResource("/images/snapshot/button_delete_template.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateDeleteButton = new JButton();
		templateDeleteButton.setIcon(ii);
		templateDeleteButton.setBorderPainted(false);
		templateDeleteButton.setContentAreaFilled(false);
		templateDeleteButton.setFocusPainted(false);
		templateDeleteButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_delete_template_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		templateDeleteButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanel.add(templateDeleteButton);
		templateDeleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				processActionDeleteTemplate();
			}
		});
		
		refreshTemplates();
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northSubRightPanel.add(tempPanel);
		snapshotNameLabel=new JLabel("Snapshot name:");
		InjectUtils.assignArialPlainFont(snapshotNameLabel,InjectMain.FONT_SIZE_NORMAL);
		snapshotNameLabel.setForeground(Color.white);
		tempPanel.add(snapshotNameLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(3, 15)));
		snapshotNameValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(snapshotNameValueLabel,InjectMain.FONT_SIZE_NORMAL);
		snapshotNameValueLabel.setForeground(Color.white);
		tempPanel.add(snapshotNameValueLabel);
		
		JPanel tempLabelPanel=new JPanel();
		northSubRightPanel.add(tempLabelPanel);
		tempLabelPanel.setOpaque(false);
		tempLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		selectedFormsCountLabel=new JLabel("Number of selected Forms:", SwingConstants.LEFT);
		InjectUtils.assignArialPlainFont(selectedFormsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		selectedFormsCountLabel.setForeground(Color.white);
		tempLabelPanel.add(selectedFormsCountLabel);
		tempLabelPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		selectedFormsCountValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(selectedFormsCountValueLabel,InjectMain.FONT_SIZE_NORMAL);
		selectedFormsCountValueLabel.setForeground(Color.white);
		tempLabelPanel.add(selectedFormsCountValueLabel);
		
		tempLabelPanel=new JPanel();
		northSubRightPanel.add(tempLabelPanel);
		tempLabelPanel.setOpaque(false);
		tempLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		snapshotIdLabel=new JLabel("Snapshot ID:", SwingConstants.LEFT);
		InjectUtils.assignArialPlainFont(snapshotIdLabel,InjectMain.FONT_SIZE_NORMAL);
		snapshotIdLabel.setForeground(Color.white);
		tempLabelPanel.add(snapshotIdLabel);
		tempLabelPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		snapshotIdValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(snapshotIdValueLabel,InjectMain.FONT_SIZE_NORMAL);
		snapshotIdValueLabel.setForeground(Color.white);
		tempLabelPanel.add(snapshotIdValueLabel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northSubRightPanel.add(tempPanel);
		autoFreshFilteringCheckbox=new JCheckBox("Filtering Auto-Refresh");
		autoFreshFilteringCheckbox.setOpaque(false);
		autoFreshFilteringCheckbox.setVisible(true);
		autoFreshFilteringCheckbox.setSelected(true);
		InjectUtils.assignArialPlainFont(autoFreshFilteringCheckbox,InjectMain.FONT_SIZE_NORMAL);
		autoFreshFilteringCheckbox.setForeground(Color.white);
		tempPanel.add(autoFreshFilteringCheckbox);
		autoFreshFilteringCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				autoRefreshFiltering();
			}
		});
		
		Set<String> activatedPlugins=ModelUtils.getActivatedPlugins();
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
			tempPanel=new JPanel();
			tempPanel.setOpaque(false);
			tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			northSubRightPanel.add(tempPanel);
			isSnapshotForConversionCheckbox=new JCheckBox("Upgrade Type");
			isSnapshotForConversionCheckbox.setOpaque(false);
			isSnapshotForConversionCheckbox.setVisible(true);
			isSnapshotForConversionCheckbox.setSelected(false);
			InjectUtils.assignArialPlainFont(isSnapshotForConversionCheckbox,InjectMain.FONT_SIZE_NORMAL);
			isSnapshotForConversionCheckbox.setForeground(Color.white);
			tempPanel.add(isSnapshotForConversionCheckbox);
			isSnapshotForConversionCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					startInventoriesDisplayThread(2000);
				}
			});
		}

		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		add(centerPanel,BorderLayout.CENTER);
	
		this.setSnapshotInventoryGridActionPanel(new SnapshotCreationActionsPanel(this));
		boolean hasSelectionColumn=true;
		boolean hasViewChangesColumn=false;
		boolean hasFilteringResultColumn=false;
		snapshotInventoryDetailsGridPanel=new SnapshotInventoryDetailsGridPanel(this,hasSelectionColumn,
				hasViewChangesColumn,hasFilteringResultColumn,true,true,true,
				getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowBalloons(),false,30,true);
		snapshotInventoryDetailsGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
		filterModulePanel=new FilterModulePanel(this,new JLabel(),snapshotInventoryDetailsGridPanel);
		UIUtils.setDimension(filterModulePanel,800,150);
		northSubLeftPanel.add(filterModulePanel);
		
		centerPanel.add(snapshotInventoryDetailsGridPanel);
				
		JPanel southPanel=new JPanel();
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		add(southPanel,BorderLayout.SOUTH);
		
		JPanel southLabelPanel=new JPanel();
		southLabelPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		southLabelPanel.setOpaque(false);
		southLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		southPanel.add(southLabelPanel);
		
		executionStatusLabel=new JLabel("Execution time: ");
		InjectUtils.assignArialPlainFont(executionStatusLabel,InjectMain.FONT_SIZE_NORMAL);
		executionProgressLabel=new JLabel("Progress: ");
		InjectUtils.assignArialPlainFont(executionProgressLabel,InjectMain.FONT_SIZE_NORMAL);
		southLabelPanel.add(executionStatusLabel);
		southLabelPanel.add(Box.createRigidArea(new Dimension(180, 15)));
		southLabelPanel.add(executionProgressLabel);
		
		executionFailedRowsCountLabel=new JLabel("Failed rows: ");
		InjectUtils.assignArialPlainFont(executionFailedRowsCountLabel,InjectMain.FONT_SIZE_NORMAL);
		executionFailedRowsCountLabelValue=new JLabel();
		InjectUtils.assignArialPlainFont(executionFailedRowsCountLabelValue,InjectMain.FONT_SIZE_NORMAL);
		southLabelPanel.add(Box.createRigidArea(new Dimension(180, 15)));
		executionFailedRowsCountLabelValue.setOpaque(true);
		executionFailedRowsCountLabelValue.setBackground(Color.decode("#dbdcdf"));
		
		southLabelPanel.add(executionFailedRowsCountLabel);
		southLabelPanel.add(executionFailedRowsCountLabelValue);
		
		southPanel.add(this.getSnapshotInventoryGridActionPanel());
		autoRefreshFiltering();
	}

	protected void autoRefreshFiltering() {
		new Thread( new Runnable() {
			@Override
			public void run() {
				if (autoFreshFilteringCheckbox.isSelected()) {
					while (autoFreshFilteringCheckbox.isSelected()) {
						try {
							snapshotInventoryDetailsGridPanel.getFilteringTable().applyFiltering();
							Thread.sleep(1000);
						}
						catch (InterruptedException e) {
							FileUtils.printStackTrace(e);
							break;
						}
					}
				}
			}
		}).start();		
	}

	protected void processActionDeleteTemplate() {
		try {
			String templateName=(String)templateSelectionComboBox.getSelectedItem();
			if (!templateName.isEmpty()) {
				File templateFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getTemplateFolder();
				if(templateFolder.exists()){
					File templateFile=new File(templateFolder,templateName+".txt");
					boolean isDeleted=templateFile.delete();
					if (!isDeleted) {
						FileUtils.println("Unable to delete template file: '"+templateFile.getAbsolutePath()+"'");
					}
					else {
						refreshTemplates();
						GUIUtils.popupInformationMessage("Template '"+templateName+"' successfully deleted!");
					}
				}else{
					GUIUtils.popupErrorMessage("Cannot delete a template '"+templateName+"'.  Template folder '"+templateFolder.getAbsolutePath()+"' does not exist in your file system.\n Please check your setting in OPTIONS !! ");

				}
			}
		}
		catch (Exception e1) {
			FileUtils.printStackTrace(e1);
		}
	}

	protected void processActionSaveNewTemplate() {
		String templateName = JOptionPane.showInputDialog(this, "Enter the new template name:");
		if (templateName==null) {
			return;
		}
		List<SnapshotInventoryGridRecord> list=snapshotInventoryDetailsGridPanel.getSelectedSnapshotInventoryGridRecordsList();
		StringBuffer content=new StringBuffer("");
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:list) {
			content.append(snapshotInventoryGridRecord.getInventoryName()).append("\n");
		}
		File templateFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getTemplateFolder();
		if(templateFolder.exists()){

			File templateFile=new File(templateFolder,templateName+".txt");
			ModelUtils.writeToFile(templateFile, content.toString(),false);
			refreshTemplates();
			templateSelectionComboBox.setSelectedItem(templateName);
			GUIUtils.popupInformationMessage("Template '"+templateName+"' successfully created!");
		}else{
			GUIUtils.popupErrorMessage("Cannot save a template '"+templateName+"'.  Template folder '"+templateFolder.getAbsolutePath()+"' does not exist in your file system.\n Please check your setting in OPTIONS !! ");
		}
	}
	
	protected void refreshTemplates() {
		File templateFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getTemplateFolder();
		try {
			if(templateFolder.exists()){
				List<File> allTemplates=new ArrayList<File>();
				List<File> templatesList=FileUtils.listAllFiles(templateFolder,false,"txt");
				List<File> seededTemplatesList=FileUtils.listAllFiles(
						tabSnapshotsPanel.getMainPanel().getSnapshotMain().getSeededTemplateFolder(),false,"txt");
				allTemplates.addAll(seededTemplatesList);
				allTemplates.addAll(templatesList);
				
				templateSelectionComboBox.removeAllItems();
				templateSelectionComboBox.addItem("");
				for (File templateFile:allTemplates) {
					templateSelectionComboBox.addItem(CoreUtil.getFileNameWithoutExtension(templateFile) );
				}
			}else{
				throw new Exception("Cannot refresh templates! Template folder '"+templateFolder.getAbsolutePath()+"' does not exist in your file system.\n Please check your setting in OPTIONS !! ");
			}
		} catch (Exception e1) {
			FileUtils.printStackTrace(e1);
			GUIUtils.popupErrorMessage("Internal error: "+e1.getMessage());
		}		
	}
	
	public void loadTemplate() {
		try {
			if (snapshotInventoryDetailsGridPanel==null){
				return;
			}
			String templateName=(String)templateSelectionComboBox.getSelectedItem();
			if (templateName!=null && !templateName.isEmpty()) {
				Set<String> inventoriesToSelect=new TreeSet<String>();
				File templateFolder=tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().getTemplateFolder();
				if(templateFolder.exists()){	
					File templateFile=new File(templateFolder,templateName+".txt");
					// if the template is not found in the template folder, look for it in the sessions folder.
					if(!templateFile.exists()) {
						templateFolder=tabSnapshotsPanel.getMainPanel().getSnapshotMain().getSeededTemplateFolder();
						templateFile=new File(templateFolder,templateName+".txt");
						//throw new Exception("");
					}
					List<String> inventoriesList=ModelUtils.readContentsFromTemplateFile(templateFile);
					
					for (String inventoryName:inventoriesList) {
						inventoriesToSelect.add(inventoryName);
					}	
					snapshotInventoryDetailsGridPanel.setSelectionForInventories(inventoriesToSelect);
					snapshotInventoryDetailsGridPanel.getFilteringTable().setColumnFilterValue(
							SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_SELECTION,"Selected");
					snapshotInventoryDetailsGridPanel.getFilteringTable().applyFiltering();
				}else{
					throw new Exception("Cannot load a template '"+templateName+"'.  Template folder '"+templateFolder.getAbsolutePath()+"' does not exist in your file system.\n Please check your setting in OPTIONS !! ");
				}
			}else {
				snapshotInventoryDetailsGridPanel.setSelectionAll();
				snapshotInventoryDetailsGridPanel.getFilteringTable().setColumnFilterValue(
						SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_SELECTION,"");
				snapshotInventoryDetailsGridPanel.getFilteringTable().applyFiltering();
			}
			isApplyTemplateSuccessfully = true;
		}
		catch (Exception e1) {
			FileUtils.printStackTrace(e1);
			GUIUtils.popupErrorMessage("Internal error: "+e1.getMessage());
		}
	}

	public SnapshotCreationActionsPanel getSnapshotCreationActionsPanel() {
		return ((SnapshotCreationActionsPanel)this.getSnapshotInventoryGridActionPanel());
	}

	public SnapshotInventoryDetailsGridPanel getSnapshotInventoryDetailsGridPanel() {
		return snapshotInventoryDetailsGridPanel;
	}

	public JLabel getSnapshotNameValueLabel() {
		return snapshotNameValueLabel;
	}

	public JLabel getSelectedFormsCountValueLabel() {
		return selectedFormsCountValueLabel;
	}

	public JLabel getSnapshotIdValueLabel() {
		return snapshotIdValueLabel;
	}
	
	private void startInventoriesDisplayThread(final int delayInMs) {
		getTabSnapshotsPanel().getServerSelectionPanel().lockPanel();
		getTabSnapshotsPanel().getSnapshotsActionsPanel().lockPanel();
		getTabSnapshotsPanel().getSnapshotsGridPanel().lockPanel();
		new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
					displayInventories(delayInMs);
					isInitialInventoriesSuccessfully = true;
				} 
				catch (Exception e) {
					FileUtils.printStackTrace(e);
					GUIUtils.popupErrorMessage("Internal error: "+e.getMessage());
				}
			}
		}).start();
	}
	
	public void displayInventories(int delayInMs) throws Exception {
		InitCreationGridSwingWorker swingWorker=new InitCreationGridSwingWorker(this,delayInMs);
		final int width=450;
		final int height=150;
		swingWorker.setTotalSteps(2);
		UIUtils.displayOperationInProgressModalWindow(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),
				width,height,"Initialization...",swingWorker,SnapshotMain.getSharedApplicationIconPath());
	}

	public JLabel getExecutionStatusLabel() {
		return executionStatusLabel;
	}

	public JLabel getExecutionProgressLabel() {
		return executionProgressLabel;
	}
	
	public void viewChanges(int viewRow) {
	}

	public FilterModulePanel getFilterModulePanel() {
		return filterModulePanel;
	}
	
	@Override
	public void runDefaultProcess() {
		startInventoriesDisplayThread(0);
	}

	public void processActionTakeSnapshot(String snapshotName,String snapshotDescription) {
		getSnapshotNameValueLabel().setText(snapshotName);
		List<SnapshotInventoryGridRecord> selectedSnapshotInventoryGridRecordsList=getSnapshotInventoryDetailsGridPanel().
				getSelectedSnapshotInventoryGridRecordsList();
		getSelectedFormsCountValueLabel().setText(""+selectedSnapshotInventoryGridRecordsList.size());

		getSnapshotCreationActionsPanel().getSnapshotButton().setEnabled(false);
		filterModulePanel.setComponentsEnabled(false);
		snapshotCreationController=new SnapshotCreationController(this,snapshotName,snapshotDescription);
		snapshotCreationController.startExecution();
		getSnapshotCreationActionsPanel().getCancelButton().setEnabled(true);
	}

	public void closeWindow() {
    	if ( snapshotCreationController==null || snapshotCreationController.isExecutionCompleted()) {
    		getTabSnapshotsPanel().getServerSelectionPanel().unlockPanel();
			getTabSnapshotsPanel().getSnapshotsActionsPanel().unlockPanel();
			getTabSnapshotsPanel().getSnapshotsGridPanel().unlockPanel();
			tabSnapshotsPanel.getMainPanel().getSnapshotMain().setAlreadyClickedNewSnapshot(false);
    		setVisible(false);
            dispose();
    	}
    	else {
    		GUIUtils.popupErrorMessage("You cannot close this window until the Snapshot is completed!");
    	}
	}
	
	public boolean isSnapshotForConversionSelected() {
		return isSnapshotForConversionCheckbox!=null && isSnapshotForConversionCheckbox.isSelected();
	}

	public void setIsSnapshotForConversionCheckbox(boolean value) {
		isSnapshotForConversionCheckbox.setSelected(value);
	}

	public JLabel getExecutionFailedRowsCountLabelValue() {
		return executionFailedRowsCountLabelValue;
	}

	@Override
	public void processAction(SnapshotCreationInformationPanel snapshotCreationInformationPanel) {
		String snapshotName= snapshotCreationInformationPanel.getSnapshotNameTextField().getText();
		if (snapshotName==null || snapshotName.isEmpty()) {
			GUIUtils.popupErrorMessage("You must enter a snapshot name!");
			return;
		}
		String snapshotDescription=snapshotCreationInformationPanel.getSnapshotDescriptionTextArea().getText();
		snapshotCreationInformationPanel.getDialog().dispose();
		processActionTakeSnapshot(snapshotName,snapshotDescription);
	}

	public JComboBox<String> getTemplateSelectionComboBox() {
		return templateSelectionComboBox;
	}

	public boolean isApplyTemplateSuccessfully() {
		return isApplyTemplateSuccessfully;
	}

	public boolean isInitialInventoriesSuccessfully() {
		return isInitialInventoriesSuccessfully;
	}

	
}
