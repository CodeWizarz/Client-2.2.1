package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.extract.controller.ExecutionController;
import com.rapidesuite.extract.model.ExtractInventoryRecord;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.inject.gui.TabChangeListener;
import com.rapidesuite.inject.gui.TabbedPaneUI;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.UIConstants;

@SuppressWarnings("serial")
public class ExtractMainPanel extends JPanel {

	private ExtractMain extractMain;
	private TabSelectionPanel tabSelectionPanel;
	private TabOptionsPanel tabOptionsPanel;
	private TabExecutionPanel tabExecutionPanel;
	
	private JButton backButton;
	private JButton startButton;
	private JButton stopButton;
	private JButton saveGridToExcelButton;
	private JButton viewOrgStructureButton;
	
	private JLabel elapsedExecutionTimeLabel;
	private JLabel elapsedExecutionTimeValueLabel;
	private JLabel scriptExecutionCountLabel;
	private JLabel scriptExecutionCountValueLabel;
	private JLabel remarksLabel;
	private JProgressBar progressBar;
	private long startTime;
	private ExecutionController executionController;
	private JTabbedPane jtp;
	
	public ExtractMainPanel(ExtractMain extractMain) {
		this.extractMain=extractMain;
		setLayout(new BorderLayout());
		createComponents();
	}

	public void createComponents(){
		jtp = new JTabbedPane();
		jtp.setOpaque(true);
		jtp.setBackground(Color.decode("#343836"));
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
			}
		};
		jtp.addChangeListener(changeListener);

		String tabName="";
		int tabIndex=0;
		int tabWidth=120;
		int tabHeight=50;

		InjectUtils.addMenuTab(jtp,extractMain,CoreConstants.SHORT_APPLICATION_NAME.extract.toString(),tabIndex,tabHeight);
		tabIndex++;

		tabSelectionPanel=new TabSelectionPanel(extractMain);
		tabOptionsPanel=new TabOptionsPanel(extractMain);
		tabExecutionPanel=new TabExecutionPanel(extractMain);

		tabName="SELECTION";
		InjectUtils.addTab(jtp,tabName,tabSelectionPanel,tabIndex,tabWidth,tabHeight,true);
		tabIndex++;

		tabName="EXECUTION";
		InjectUtils.addTab(jtp,tabName,tabExecutionPanel,tabIndex,tabWidth,tabHeight,true);
		tabIndex++;
		
		tabName="OPTIONS";
		InjectUtils.addTab(jtp,tabName,tabOptionsPanel,tabIndex,tabWidth,tabHeight,true);
		tabIndex++;

		int tabIndexGreaterThanNotDisplayWhiteBar=3;
		jtp.setUI(new TabbedPaneUI(tabIndexGreaterThanNotDisplayWhiteBar));
		jtp.addChangeListener(new TabChangeListener(jtp));
		selectSelectionTabPanel();
		jtp.setEnabledAt(0, false);
		add(jtp,BorderLayout.CENTER);
		
		int borderSize=20;
		
		JPanel southPanel=new JPanel();
		add(southPanel,BorderLayout.SOUTH);
		southPanel.setBorder(BorderFactory.createEmptyBorder(0,borderSize, 0, 0));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setOpaque(true);
		southPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		remarksLabel=new JLabel();
		remarksLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(remarksLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		southPanel.add(remarksLabel);

		JPanel actionsPanel=new JPanel();
		actionsPanel.setOpaque(true);
		actionsPanel.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
		actionsPanel.setAlignmentX( Component.LEFT_ALIGNMENT);
		southPanel.add(actionsPanel);

		JPanel tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		//tempPanel.setBackground(Color.RED);
		actionsPanel.add(tempPanel);
		elapsedExecutionTimeLabel=new JLabel("Elapsed execution time: ");
		elapsedExecutionTimeLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(elapsedExecutionTimeLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(elapsedExecutionTimeLabel);
		elapsedExecutionTimeValueLabel=new JLabel("");
		elapsedExecutionTimeValueLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(elapsedExecutionTimeValueLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(elapsedExecutionTimeValueLabel);

		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		//tempPanel.setBackground(Color.BLUE);
		actionsPanel.add(tempPanel);
		scriptExecutionCountLabel=new JLabel("Execution Inventories count: ");
		scriptExecutionCountLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(scriptExecutionCountLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(scriptExecutionCountLabel);
		scriptExecutionCountValueLabel=new JLabel("");
		scriptExecutionCountValueLabel.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(scriptExecutionCountValueLabel,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(scriptExecutionCountValueLabel);

		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 3, 0, 100));
		//tempPanel.setBackground(Color.yellow);
		actionsPanel.add(tempPanel);
		JLabel label=new JLabel("Progress: ");
		label.setForeground(Color.decode("#2F3436"));
		InjectUtils.assignArialPlainFont(label,ExecutionPanel.FONT_SIZE_NORMAL);
		tempPanel.add(label);
		tempPanel.add(Box.createRigidArea(new Dimension(50, 10)));
		progressBar = new JProgressBar();

		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);		
		tempPanel.add(progressBar);

		tempPanel=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BorderLayout());
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		tempPanel.setOpaque(false);
		tempPanel.setBackground(Color.green);
		actionsPanel.add(tempPanel);
		JPanel tempPanelButton=InjectUtils.getXPanel(Component.LEFT_ALIGNMENT);
		tempPanelButton.setOpaque(false);
		tempPanelButton.setBackground(Color.red);
		tempPanelButton.add(Box.createGlue());
		tempPanel.add(tempPanelButton,BorderLayout.CENTER);
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/inject/button_back.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		backButton = new JButton();
		backButton.setIcon(ii);
		backButton.setBorderPainted(false);
		backButton.setContentAreaFilled(false);
		backButton.setFocusPainted(false);
		backButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_back_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		backButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanelButton.add(backButton);	
		tempPanelButton.add(Box.createRigidArea(new Dimension(15, 15)));
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				previousPanel();
			}
		});

		iconURL = this.getClass().getResource("/images/inject/button_start.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startButton = new JButton();
		startButton.setIcon(ii);
		startButton.setBorderPainted(false);
		startButton.setContentAreaFilled(false);
		startButton.setFocusPainted(false);
		startButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_start_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		startButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanelButton.add(startButton);	
		tempPanelButton.add(Box.createRigidArea(new Dimension(15, 15)));
		ActionListener onStartButtonIsClicked = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				startExecution();
			}
		};
		startButton.addActionListener(onStartButtonIsClicked);


		iconURL = this.getClass().getResource("/images/inject/button_stop1.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		stopButton = new JButton();
		stopButton.setIcon(ii);
		stopButton.setBorderPainted(false);
		stopButton.setContentAreaFilled(false);
		stopButton.setFocusPainted(false);
		stopButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/inject/button_stop1_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		stopButton.setRolloverIcon(new RolloverIcon(ii));
		tempPanelButton.add(stopButton);	
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				stopExecution();
			}
		}
				);
		tempPanelButton.add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_save_grid_to_excel.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveGridToExcelButton = new JButton();
		saveGridToExcelButton.setIcon(ii);
		saveGridToExcelButton.setBorderPainted(false);
		saveGridToExcelButton.setContentAreaFilled(false);
		saveGridToExcelButton.setFocusPainted(false);
		saveGridToExcelButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_save_grid_to_excel_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		saveGridToExcelButton.setRolloverIcon(new RolloverIcon(ii));
		saveGridToExcelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				saveGridToExcel();
			}
		}
				);
		tempPanelButton.add(saveGridToExcelButton);
		tempPanelButton.add(Box.createRigidArea(new Dimension(15, 15)));
		
		iconURL = this.getClass().getResource("/images/extract/button_view_org_structure.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		viewOrgStructureButton = new JButton();
		viewOrgStructureButton.setIcon(ii);
		viewOrgStructureButton.setBorderPainted(false);
		viewOrgStructureButton.setContentAreaFilled(false);
		viewOrgStructureButton.setFocusPainted(false);
		viewOrgStructureButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/extract/button_view_org_structure_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		viewOrgStructureButton.setRolloverIcon(new RolloverIcon(ii));
		viewOrgStructureButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				viewOrgStructure();
			}
		}
				);
		tempPanelButton.add(viewOrgStructureButton);
		
		
		tempPanelButton.add(Box.createGlue());	
	}
	
	protected void viewOrgStructure() {
		final OrgStructureFrame orgStructureFrame=extractMain.getApplicationInfoPanel().getOrgStructureFrame();
		try{
			SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					orgStructureFrame.createTree();
					return null;
				}
			};

			String msg="Analyzing structure, please wait...";
			final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			final JDialog dialog = new JDialog();
			dialog.setTitle("Operation in Progress");
			dialog.setModal(true);
			dialog.setContentPane(optionPane);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.pack();

			mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("state")) {
						if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
							dialog.dispose();
						}
					}
				}
			});
			mySwingWorker.execute();

			dialog.setLocationRelativeTo(getExtractMain().getRootFrame());
			dialog.setVisible(true);
			orgStructureFrame.setVisible(true);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}	
	}

	public void selectSelectionTabPanel(){
		jtp.setSelectedIndex(1);
	}
	
	public void resetPanels() {
		selectSelectionTabPanel();
		elapsedExecutionTimeValueLabel.setText("");
		scriptExecutionCountValueLabel.setText("");
		remarksLabel.setText("");
		progressBar.setValue(0);
		startTime=0;
		tabExecutionPanel.getExtractInventoryGridResultPanel().displayInventories(new ArrayList<ExtractInventoryRecord>());
	}

	protected void previousPanel() {
		extractMain.moveToPanel(InjectMain.APPLICATION_INFO_PANEL);
	}
	
	public ExtractMain getExtractMain() {
		return extractMain;
	}

	public TabSelectionPanel getTabSelectionPanel() {
		return tabSelectionPanel;
	}

	public TabOptionsPanel getTabOptionsPanel() {
		return tabOptionsPanel;
	}

	public TabExecutionPanel getTabExecutionPanel() {
		return tabExecutionPanel;
	}

	public void lockUI()
	{
		startButton.setEnabled(false);
		backButton.setEnabled(false);
		stopButton.setEnabled(true);
		tabSelectionPanel.lockUI();
		tabOptionsPanel.lockUI();
		tabSelectionPanel.getExtractInventoryGridSelectionPanel().setIsExecutionStarted(true);
		tabSelectionPanel.getExtractInventoryGridSelectionPanel().repaint();
		extractMain.getApplicationInfoPanel().getSelectPackageButton().setEnabled(false);
	}
	
	public void unlockUI()
	{
		startButton.setEnabled(true);
		backButton.setEnabled(true);
		stopButton.setEnabled(false);	
		tabSelectionPanel.unlockUI();
		tabOptionsPanel.unlockUI();
		tabSelectionPanel.getExtractInventoryGridSelectionPanel().setIsExecutionStarted(false);
		tabSelectionPanel.getExtractInventoryGridSelectionPanel().repaint();
		extractMain.getApplicationInfoPanel().getSelectPackageButton().setEnabled(true);
		tabSelectionPanel.getExtractInventoryGridSelectionPanel().getFiltersPanel().unlockUI();
	}
		
	public void startExecution()
	{
		try
		{
			final List<ExtractInventoryRecord> selectedExtractInventoryGridRecordsList=tabSelectionPanel.getExtractInventoryGridSelectionPanel().
					getSelectedExtractInventoryGridRecordsList();
			if (selectedExtractInventoryGridRecordsList.isEmpty()) {
				GUIUtils.popupErrorMessage("You must select at least one inventory!");
				return;
			}
			
			tabSelectionPanel.getExtractInventoryGridSelectionPanel().getFiltersPanel().getUnappliedFiltersLabel().setText("");
			tabSelectionPanel.getExtractInventoryGridSelectionPanel().getFiltersPanel().lockUI();
			
			progressBar.setValue(0);
			progressBar.setString("0 %");
			progressBar.setMaximum(selectedExtractInventoryGridRecordsList.size());
			startTime=System.currentTimeMillis();
			scriptExecutionCountValueLabel.setText("0 / "+selectedExtractInventoryGridRecordsList.size());
			
			executionController=new ExecutionController(extractMain, selectedExtractInventoryGridRecordsList);
			
			for (ExtractInventoryRecord extractInventoryRecord:selectedExtractInventoryGridRecordsList) {
				extractInventoryRecord.setStatus(UIConstants.UI_STATUS_PENDING);
				extractInventoryRecord.setRemarks("");
				extractInventoryRecord.setTotalRecords(-1);
				extractInventoryRecord.setExecutionTime(null);
				extractInventoryRecord.setDataSet(null);
				extractInventoryRecord.setStartTime(-1);
				extractInventoryRecord.setRawTimeInSecs(0);
				extractInventoryRecord.setResultGridIndex(-1);
			}
			
			tabExecutionPanel.getExtractInventoryGridResultPanel().displayInventories(selectedExtractInventoryGridRecordsList);
		
			tabExecutionPanel.getExtractInventoryGridResultPanel().getLastExecutionStartTimeLabelValue().setText(
					executionController.getFormattedStartDate());
			tabExecutionPanel.getExtractInventoryGridResultPanel().getLastExecutionDownloadFolderComponent().setText("");
						
			executionController.startExecution();
			jtp.setSelectedIndex(2);
			lockUI();
			updateProgressUIThread();
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}
	
	public void stopExecution()
	{
		try{
			SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					executionController.stopExecution();
					while (!executionController.isExecutionCompleted()) {
						Thread.sleep(1000);
					}
					unlockUI();
					return null;
				}
			};

			String msg="Stopping execution, please wait...";
			final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			final JDialog dialog = new JDialog();
			dialog.setTitle("Stopping execution");
			dialog.setModal(true);
			dialog.setContentPane(optionPane);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.pack();

			mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("state")) {
						if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
							dialog.dispose();
						}
					}
				}
			});
			mySwingWorker.execute();

			dialog.setLocationRelativeTo(extractMain.getRootFrame());
			dialog.setVisible(true);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}
	
	private void updateProgressUIThread() {
		Thread t = new Thread()
		{
			public void run()
			{
				try {
					int totalCount=executionController.getTotalTasks();
					while (!executionController.isExecutionCompleted()) {
						updateProgressUI(totalCount);
						Thread.sleep(1000);
					}
					updateProgressUI(totalCount);
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
			}
		};
		t.start();
	}
	
	private void updateProgressUI(final int totalCount) {
		final int completedCount=executionController.getTotalCompletedTasks();
		final String msg=Utils.getExecutionTime(startTime,System.currentTimeMillis());
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					int percentage = completedCount * 100 / totalCount;
					
					/*System.out.println("updateProgressUI, completedCount:"+completedCount+
							" progressBar.getValue():"+progressBar.getValue()+
							" progressBar.getMaximum():"+progressBar.getMaximum()+
							" progressBar.getMinimum():"+progressBar.getMinimum()+
							" percentage: "+percentage);
							*/
					progressBar.setValue(completedCount);
					progressBar.setString(percentage+" %");
					String text=completedCount+" / "+totalCount;
					
					scriptExecutionCountValueLabel.setText(text);
					elapsedExecutionTimeValueLabel.setText(msg);
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
			}
		});
	}
	
	public JLabel getElapsedExecutionTimeValueLabel() {
		return elapsedExecutionTimeValueLabel;
	}

	public ExecutionController getExecutionController() {
		return executionController;
	}

	protected void saveGridToExcel() {
		tabExecutionPanel.getExtractInventoryGridResultPanel().saveGridToExcel();
	}
		
	public File getSeededTemplateFolder() {
		File templateFolder=new File("sessions");
		templateFolder.mkdirs();
		
		return templateFolder;
	}
	
}