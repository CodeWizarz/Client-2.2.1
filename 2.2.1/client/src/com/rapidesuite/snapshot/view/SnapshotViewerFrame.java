package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.SnapshotViewerController;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class SnapshotViewerFrame extends SnapshotInventoryGridFrame  {

	private SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel;
	private SnapshotGridRecord snapshotGridRecord;
	private JCheckBox viewDataChangesOnlyCheckBox;
	private SnapshotViewerFiltersPanel snapshotViewerFiltersPanel;
	private JCheckBox downloadDataChangesOnlyCheckBox;
	private JCheckBox br100DataChangesOnlyCheckBox;
	
	public static final int FRAME_WIDTH=1680;
	public static final int FRAME_HEIGHT=760;

	public SnapshotViewerFrame(TabSnapshotsPanel tabSnapshotsPanel,SnapshotGridRecord snapshotGridRecord) {
		super(tabSnapshotsPanel);
		this.snapshotGridRecord=snapshotGridRecord;
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
		setTitle(UIConstants.FRAME_TITLE_PREFIX+" - Snapshot Viewer for '"+snapshotGridRecord.getName()+"'");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
		createComponents();

		runDefaultProcess();
	}
	
	public void closeWindow() {
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
		add(northPanel,BorderLayout.NORTH);

		JPanel tempPanel=new JPanel();
		northPanel.add(tempPanel);
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		JLabel snapshotNameLabel=new JLabel("Snapshot name: "+snapshotGridRecord.getName());
		InjectUtils.assignArialPlainFont(snapshotNameLabel,InjectMain.FONT_SIZE_NORMAL);
		snapshotNameLabel.setForeground(Color.white);
		tempPanel.add(snapshotNameLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(100,10)));
		JLabel snapshotIdLabel=new JLabel("Snapshot ID: "+snapshotGridRecord.getSnapshotId());
		InjectUtils.assignArialPlainFont(snapshotIdLabel,InjectMain.FONT_SIZE_NORMAL);
		snapshotIdLabel.setForeground(Color.white);
		tempPanel.add(snapshotIdLabel);
			
		snapshotViewerFiltersPanel =new SnapshotViewerFiltersPanel(this, snapshotGridRecord);
		northPanel.add(snapshotViewerFiltersPanel);
		
		tempPanel=new JPanel();
		tempPanel.setOpaque(true);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
		tempPanel.setBackground(Color.decode("#dbdcdf"));
		tempPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		northPanel.add(tempPanel);
		viewDataChangesOnlyCheckBox=new JCheckBox("View Data Option: Changes only");
		viewDataChangesOnlyCheckBox.setOpaque(false);
		viewDataChangesOnlyCheckBox.setSelected(true);
		viewDataChangesOnlyCheckBox.setFocusPainted(false);
		tempPanel.add(viewDataChangesOnlyCheckBox);
		downloadDataChangesOnlyCheckBox=new JCheckBox("Download Data Option: Changes only");
		downloadDataChangesOnlyCheckBox.setOpaque(false);
		downloadDataChangesOnlyCheckBox.setSelected(true);
		downloadDataChangesOnlyCheckBox.setFocusPainted(false);
		tempPanel.add(downloadDataChangesOnlyCheckBox);
		br100DataChangesOnlyCheckBox=new JCheckBox("BR100 Data Option: Changes only");
		br100DataChangesOnlyCheckBox.setOpaque(false);
		br100DataChangesOnlyCheckBox.setSelected(true);
		br100DataChangesOnlyCheckBox.setFocusPainted(false);
		tempPanel.add(br100DataChangesOnlyCheckBox);
				
		tempPanel.add(Box.createHorizontalGlue());
		
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		add(centerPanel,BorderLayout.CENTER);
		boolean hasSelectionColumn=true;
		boolean hasViewChangesColumn=true;
		boolean hasFilteringResultColumn=true;
		this.setSnapshotInventoryGridActionPanel(new SnapshotViewerActionPanel(this));
		snapshotInventoryDetailsGridPanel=new SnapshotInventoryDetailsGridPanel(this,hasSelectionColumn,hasViewChangesColumn,
				hasFilteringResultColumn,false,true,false,
				getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowBalloons(),false,30,true);
		centerPanel.add(snapshotInventoryDetailsGridPanel);

		JPanel southPanel=new JPanel();
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		add(southPanel,BorderLayout.SOUTH);
		southPanel.add(this.getSnapshotInventoryGridActionPanel());
	}
	
	public boolean isBR100DataChangesOnly() {
		return br100DataChangesOnlyCheckBox.isSelected();
	}

	private void displayDefaultInventories() {
		getTabSnapshotsPanel().getServerSelectionPanel().lockPanel();
		getTabSnapshotsPanel().getSnapshotsActionsPanel().lockPanel();
		getTabSnapshotsPanel().getSnapshotsGridPanel().lockPanel();
		startFiltering();
	}
	
	public void startFiltering() {
		SnapshotViewerController snapshotViewerController=new SnapshotViewerController(tabSnapshotsPanel,
				snapshotInventoryDetailsGridPanel,
				this,
				getSnapshotGridRecord(),
				snapshotViewerFiltersPanel);
		
		snapshotViewerController.startExecution();
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}

	public SnapshotViewerActionPanel getSnapshotViewerActionPanel() {
		return ((SnapshotViewerActionPanel)this.getSnapshotInventoryGridActionPanel());
	}

	public SnapshotInventoryDetailsGridPanel getSnapshotInventoryDetailsGridPanel() {
		return snapshotInventoryDetailsGridPanel;
	}

	public SnapshotGridRecord getSnapshotGridRecord() {
		return snapshotGridRecord;
	}

	public void viewChanges(int viewRow) {
		viewChangesLogic(viewRow,snapshotInventoryDetailsGridPanel,tabSnapshotsPanel,
				snapshotGridRecord,viewDataChangesOnlyCheckBox.isSelected());
	}
	
	public static void viewChangesLogic(int viewRow,SnapshotInventoryDetailsGridPanel snapshotInventoryDetailsGridPanel,
			TabSnapshotsPanel tabSnapshotsPanel,SnapshotGridRecord snapshotGridRecord,boolean viewDataChangesOnly) {
		Connection connection=null;
		try{
			TableModel model = snapshotInventoryDetailsGridPanel.getTable().getModel();
			int modelRow=snapshotInventoryDetailsGridPanel.getTable().convertRowIndexToModel(viewRow);
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=snapshotInventoryDetailsGridPanel.getSnapshotInventoryGridRecordsList().get(modelRow);

			String status= (String) model.getValueAt(modelRow, snapshotInventoryDetailsGridPanel.getTable().getColumnModel().getColumnIndex(SnapshotInventoryDetailsGridPanel.COLUMN_HEADING_STATUS));
			if (!
				(status.equalsIgnoreCase(UIConstants.UI_STATUS_COMPLETED) || status.equalsIgnoreCase(UIConstants.UI_STATUS_WARNING) )
				) {
				GUIUtils.popupErrorMessage("You cannot view the changes for that record because of the status: '"+status+"'");
				return;
			}
						
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);

			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties));

			String currentConnectionOracleRelease=tabSnapshotsPanel.getServerSelectionPanel().getCurrentConnectionOracleRelease();
			File inventoryFile=tabSnapshotsPanel.getSnapshotPackageSelectionPanel().getInventoryNameToInventoryFileMap(currentConnectionOracleRelease)
					.get(snapshotInventoryGridRecord.getInventoryName());
			if (inventoryFile==null) {
				throw new Exception("Unable to find the inventory file: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			}
			Inventory inventory=FileUtils.getInventory(inventoryFile,snapshotInventoryGridRecord.getInventoryName());

			List<SnapshotGridRecord> snapshotGridRecords=new ArrayList<SnapshotGridRecord>();
			snapshotGridRecords.add(snapshotGridRecord);

			int recordViewerFrameWidth=600;
			int recordViewerFrameHeight=600;

			StringBuffer whereClauseViewData=new StringBuffer("");
			if (snapshotInventoryGridRecord.getWhereClauseFilter()!=null) {
				whereClauseViewData.append(snapshotInventoryGridRecord.getWhereClauseFilter());
			}
			if (viewDataChangesOnly) {
				StringBuffer whereClauseSeededOracleUserIds=ModelUtils.getChangesOnlyWhereClause(
						tabSnapshotsPanel.getServerSelectionPanel().getOracleSeededUserIdToUserNameMap());
				whereClauseViewData.append(whereClauseSeededOracleUserIds);
			}
			StringBuffer sqlQueryDataRows=ModelUtils.getSQLQueryViewerDataRows(snapshotGridRecord.getSnapshotId(),
					snapshotInventoryGridRecord.getTableId(),whereClauseViewData);
			//FileUtils.println("sqlQueryDataRows:"+sqlQueryDataRows);
			
			int	totalRecordsCount=ModelUtils.getTotalRowsCount(connection,sqlQueryDataRows.toString());
			if (totalRecordsCount==0) {
				if (viewDataChangesOnly) {
					GUIUtils.popupInformationMessage("No records to view! Total Added/Updated Records: 0 . Please uncheck the 'View Data "+
					"Option: Changes only' checkbox to view all the records.");
				}
				else {
					GUIUtils.popupInformationMessage("No records to view! Total Records: 0 ");
				}
				return;
			}
			RecordFrame recordViewerFrame=new ViewerRecordFrame(recordViewerFrameWidth,recordViewerFrameHeight,
					snapshotEnvironmentProperties,inventory,snapshotGridRecords,
					totalRecordsCount,sqlQueryDataRows);
			recordViewerFrame.setVisible(true);
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

	public SnapshotViewerFiltersPanel getSnapshotViewerFiltersPanel() {
		return snapshotViewerFiltersPanel;
	}
	
	public JCheckBox getDownloadDataChangesOnlyCheckBox() {
		return downloadDataChangesOnlyCheckBox;
	}

	@Override
	public void runDefaultProcess() {
		displayDefaultInventories();
	}

}
