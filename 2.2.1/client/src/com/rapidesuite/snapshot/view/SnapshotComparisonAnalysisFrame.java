package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.SnapshotComparisonController;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.TotalsObject;

@SuppressWarnings("serial")
public class SnapshotComparisonAnalysisFrame  extends SnapshotInventoryGridFrame {
	
	private SnapshotComparisonAnalysisGridPanel snapshotComparisonAnalysisGridPanel;
	private List<SnapshotGridRecord> snapshotGridRecords;
	private SnapshotComparisonFiltersPanel snapshotComparisonFiltersPanel;
	private Map<String, String> inventoryNameToFormTypeMap;
	private Map<Integer,TotalsObject> tableIdToTotalsObjectMap;
	private Map<String, String> snapshotEnvironmentProperties;
	private SnapshotComparisonController snapshotComparisonController;
	
	public static final int FRAME_WIDTH=1680;
	public static final int FRAME_HEIGHT=760;

	public SnapshotComparisonAnalysisFrame(TabSnapshotsPanel tabSnapshotsPanel,List<SnapshotGridRecord> snapshotGridRecords) {
		super(tabSnapshotsPanel);
		this.snapshotGridRecords=snapshotGridRecords;
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
		setTitle(UIConstants.FRAME_TITLE_PREFIX+" - Snapshots Comparison");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
		createComponents();

		snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
		tableIdToTotalsObjectMap=new HashMap<Integer,TotalsObject>();
		inventoryNameToFormTypeMap=new HashMap<String, String>();
		setVisible(true);
		runDefaultProcess();
	}

	protected void closeWindow() {
		getTabSnapshotsPanel().getServerSelectionPanel().unlockPanel();
		getTabSnapshotsPanel().getSnapshotsActionsPanel().unlockPanel();
		getTabSnapshotsPanel().getSnapshotsGridPanel().unlockPanel();
		setVisible(false);
		dispose();
	}

	public void createComponents(){
		JPanel northPanel=new JPanel();
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#4b4f4e"));
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(northPanel,BorderLayout.NORTH);

		JPanel northSubCenterPanel=new JPanel();
		northSubCenterPanel.setOpaque(false);
		northSubCenterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		northSubCenterPanel.setLayout(new BoxLayout(northSubCenterPanel, BoxLayout.X_AXIS));
		northPanel.add(northSubCenterPanel);

		snapshotComparisonFiltersPanel =new SnapshotComparisonFiltersPanel(this);
		snapshotComparisonFiltersPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
		northSubCenterPanel.add(snapshotComparisonFiltersPanel);
		
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));

		add(centerPanel,BorderLayout.CENTER);
		this.setSnapshotInventoryGridActionPanel(new SnapshotComparisonActionPanel(this));
		snapshotComparisonAnalysisGridPanel=new SnapshotComparisonAnalysisGridPanel(this);
		centerPanel.add(snapshotComparisonAnalysisGridPanel);

		JPanel southPanel=new JPanel();
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		add(southPanel,BorderLayout.SOUTH);
		southPanel.add(this.getSnapshotInventoryGridActionPanel());
	}

	private void displayDefaultInventories() {
		getTabSnapshotsPanel().getServerSelectionPanel().lockPanel();
		getTabSnapshotsPanel().getSnapshotsActionsPanel().lockPanel();
		getTabSnapshotsPanel().getSnapshotsGridPanel().lockPanel();
		startFiltering();
	}

	public void startFiltering() {
		snapshotComparisonController=new SnapshotComparisonController(this);
		snapshotComparisonController.startExecution();
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public SnapshotComparisonActionPanel getSnapshotComparisonActionPanel() {
		return ((SnapshotComparisonActionPanel)this.getSnapshotInventoryGridActionPanel());
	}

	public Map<String, String> getSnapshotEnvironmentProperties() {
		return snapshotEnvironmentProperties;
	}

	public List<SnapshotGridRecord> getSnapshotGridRecordsInDateOrder() {
		return snapshotGridRecords;
	}

	public void viewChanges(int viewRow) {
		Connection connection=null;
		try{
			int modelRow=snapshotComparisonAnalysisGridPanel.getTable().convertRowIndexToModel(viewRow);
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=snapshotComparisonAnalysisGridPanel.getSnapshotInventoryGridRecordsList().get(modelRow);
			if (snapshotInventoryGridRecord.getFilteringResult().startsWith(UIConstants.FILTERED_OUT_PREFIX)) {
				GUIUtils.popupErrorMessage("You cannot view the changes for that record because it is filtered out.");
				return;
			}
			
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties));
			
			List<Integer> primaryKeysPositionList=ModelUtils.getPrimaryKeysPositionList(snapshotInventoryGridRecord.getInventory());
			int comparisonDataRowsCount=ModelUtils.getComparisonDataRowsCount(connection,snapshotComparisonController.getComparisonId(),
					snapshotInventoryGridRecord.getTableId(),primaryKeysPositionList);
			
			int recordViewerFrameWidth=800;
			int recordViewerFrameHeight=600;
			RecordFrame recordViewerFrame=new ComparisonRecordFrame(recordViewerFrameWidth,recordViewerFrameHeight,
					snapshotEnvironmentProperties,snapshotInventoryGridRecord.getInventory(),snapshotGridRecords,
					comparisonDataRowsCount,snapshotComparisonController.getComparisonId(),snapshotInventoryGridRecord.getTableId());
			recordViewerFrame.processActionNext();
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
		finally {
			DirectConnectDao.closeQuietly(connection);	  
		}
	}

	public Map<String, String> getInventoryNameToFormTypeMap() {
		return inventoryNameToFormTypeMap;
	}

	public void setInventoryNameToFormTypeMap(Map<String, String> inventoryNameToFormTypeMap) {
		this.inventoryNameToFormTypeMap=inventoryNameToFormTypeMap;
	}

	public Map<Integer, TotalsObject> getTableIdToTotalsObjectMap() {
		return tableIdToTotalsObjectMap;
	}

	public SnapshotComparisonFiltersPanel getSnapshotComparisonFiltersPanel() {
		return snapshotComparisonFiltersPanel;
	}

	public SnapshotComparisonAnalysisGridPanel getSnapshotComparisonAnalysisGridPanel() {
		return snapshotComparisonAnalysisGridPanel;
	}

	public SnapshotGridRecord getSnapshotGridRecordWithChanges() {
		return snapshotGridRecords.get(1);
	}

	public SnapshotComparisonController getSnapshotComparisonController() {
		return snapshotComparisonController;
	}

	@Override
	public void runDefaultProcess() {
		displayDefaultInventories();
	}

	public List<SnapshotGridRecord> getSnapshotGridRecords() {
		return snapshotGridRecords;
	}

}
