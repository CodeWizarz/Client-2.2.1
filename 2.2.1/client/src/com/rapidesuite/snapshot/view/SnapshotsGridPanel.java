package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.java.balloontip.BalloonTip;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ButtonEditor;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.controller.SnapshotController;
import com.rapidesuite.snapshot.model.MemoryInformation;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotDeleteSwingWorker;
import com.rapidesuite.snapshot.model.UserInformation;

@SuppressWarnings("serial")
public class SnapshotsGridPanel extends JPanel{

	private TabSnapshotsPanel tabSnapshotsPanel;
	private JTable table;
	private boolean isPanelLocked;
	private List<SnapshotGridRecord> snapshotGridRecords;
	private JLabel totalRecordsValueLabel;
	private JLabel totalUsedSpaceValueLabel;
	private JLabel totalFreeSpaceValueLabel;
	private JLabel totalSpaceValueLabel;
	private JLabel availableSnapshotsLabel;
	private JLabel loginAsLabel;
	
	private static String COLUMN_HEADING_SELECTION="Selection";
	private static String COLUMN_HEADING_ROW_NUM="Sequence";
	private static String COLUMN_HEADING_SNAPSHOT_NAME="Name";
	private static String COLUMN_HEADING_SNAPSHOT_DATE="Snapshot Date";
	private static String COLUMN_HEADING_STATUS="Status";
	private static String COLUMN_HEADING_COMPLETION_DATE="Completed on";
	private static String COLUMN_HEADING_CREATED_BY="Created By";
	private static String COLUMN_HEADING_TOTAL_INVENTORIES="Total\nInventories";
	private static String COLUMN_HEADING_TOTAL_INVENTORIES_SELECTED="Total Selected\nInventories";
	private static String COLUMN_HEADING_TOTAL_INVENTORIES_FAILED="Total Failed\nInventories";
	private static String COLUMN_HEADING_TOTAL_RECORD_COUNT="Total\nRecords";
	private static String COLUMN_HEADING_TOTAL_DEFAULT_COUNT="Total\nDefault";
	private static String COLUMN_HEADING_TOTAL_ADDED_COUNT="Total\nAdded";
	private static String COLUMN_HEADING_TOTAL_UPDATED_COUNT="Total\nUpdated";
	private static String COLUMN_HEADING_VIEW="View";
	private static String COLUMN_HEADING_UPGRADE="Type";
	private static String COLUMN_HEADING_TEMPLATE_NAME="Template\nName";
	
	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_SELECTION_WIDTH=90;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=70;
	private static int COLUMN_HEADING_SNAPSHOT_NAME_WIDTH=220;
	private static int COLUMN_HEADING_SNAPSHOT_DATE_WIDTH=130;
	private static int COLUMN_HEADING_STATUS_WIDTH=90;
	private static int COLUMN_HEADING_CREATED_BY_WIDTH=90;
	private static int COLUMN_HEADING_COMPLETION_DATE_WIDTH=130;
	private static int COLUMN_HEADING_TOTAL_INVENTORIES_WIDTH=80;
	private static int COLUMN_HEADING_TOTAL_INVENTORIES_SELECTED_WIDTH=100;
	private static int COLUMN_HEADING_TOTAL_INVENTORIES_FAILED_WIDTH=100;
	private static int COLUMN_HEADING_TOTAL_RECORD_COUNT_WIDTH=90;
	private static int COLUMN_HEADING_TOTAL_DEFAULT_COUNT_WIDTH=90;
	private static int COLUMN_HEADING_TOTAL_ADDED_COUNT_WIDTH=90;
	private static int COLUMN_HEADING_TOTAL_UPDATED_COUNT_WIDTH=90;
	private static int COLUMN_HEADING_VIEW_WIDTH=75;
	private static int COLUMN_HEADING_UPGRADE_WIDTH=70;
	private static int COLUMN_HEADING_TEMPLATE_NAME_WIDTH=90;
	
	public SnapshotsGridPanel(TabSnapshotsPanel tabSnapshotsPanel) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		setLayout(new BorderLayout());
		createComponents();
		isPanelLocked=false;
	}

	@SuppressWarnings("rawtypes")
	public void createComponents(){
		JPanel northPanel=InjectUtils.getXPanel(Component.CENTER_ALIGNMENT);
		northPanel.setOpaque(true);
		northPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		northPanel.setBackground(Color.decode("#dbdcdf"));
		add(northPanel,BorderLayout.NORTH);
				
		JPanel centerPanel=new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		add(centerPanel,BorderLayout.CENTER);
		
		JPanel southPanel=new JPanel();
		southPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		southPanel.setOpaque(true);
		southPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		southPanel.setBackground(Color.decode("#dbdcdf"));
		add(southPanel,BorderLayout.SOUTH);
		
		Vector<String> columnNames = new Vector<String>();
		
		columnNames.add(COLUMN_HEADING_SELECTION);
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_SNAPSHOT_NAME);
		columnNames.add(COLUMN_HEADING_VIEW);
		final Set<String> activatedPlugins=ModelUtils.getActivatedPlugins();
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
			columnNames.add(COLUMN_HEADING_UPGRADE);
		}
		columnNames.add(COLUMN_HEADING_TEMPLATE_NAME);
		columnNames.add(COLUMN_HEADING_SNAPSHOT_DATE);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_CREATED_BY);
		columnNames.add(COLUMN_HEADING_COMPLETION_DATE);
		columnNames.add(COLUMN_HEADING_TOTAL_INVENTORIES);
		columnNames.add(COLUMN_HEADING_TOTAL_INVENTORIES_SELECTED);
		columnNames.add(COLUMN_HEADING_TOTAL_INVENTORIES_FAILED);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORD_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_DEFAULT_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_ADDED_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_UPDATED_COUNT);

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked" })
			@Override
			public Class getColumnClass(int column) {
				int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				if (column==colSelectionIndex) {
					return Boolean.class;
				}
				else {
					return String.class;
				}				
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				int colViewIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW);
				int colUpgradeIndex=-9999;
				if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
					colUpgradeIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_UPGRADE);
				}
				if (column==colSelectionIndex || column==colViewIndex || column==colUpgradeIndex
						) {
					return !isPanelLocked;
				}
				else {
					return false;
				}
			}
			
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                	int colSnapshotNameIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SNAPSHOT_NAME);
                	if  (colIndex==colSnapshotNameIndex) {
                		if (rowIndex>=0 && rowIndex<snapshotGridRecords.size()) {
                			SnapshotGridRecord snapshotGridRecord=snapshotGridRecords.get(rowIndex);
                			String clientHostName=snapshotGridRecord.getClientHostName();
                			if (clientHostName==null) {
                				clientHostName="N/A";
                			}
                			String description=snapshotGridRecord.getDescription();
                			if (description==null) {
                				description="N/A";
                			}
                			boolean isConversion=snapshotGridRecord.isConversion();
                			String conversionStr="";
                			if (isConversion) {
                				conversionStr="RAPIDMIGRATE snapshot<br/>";
                			}
                			tip="<html>"+
                			"Snapshot name: "+snapshotGridRecord.getName()+"<br/>"+
                			"Snapshot ID: "+snapshotGridRecord.getSnapshotId()+"<br/>"+
                			"Client Hostname: "+clientHostName+"<br/>"+
                			"Description: "+description+"<br/>"+conversionStr+"</html>";
                		}
                	}
                }
                catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }

			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int colIndex) {
				Component component = super.prepareRenderer(renderer, rowIndex, colIndex);

				String status= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS));		
				if (status.equalsIgnoreCase(UIConstants.UI_STATUS_PROCESSING)) {
					component.setBackground(UIConstants.COLOR_YELLOW);
					return component;
				}
				
				if (rowIndex%2 == 0){
					component.setBackground(Color.WHITE);
                }
                else {
                	Color alternateColor =Color.decode("#dbdbdb");
                	component.setBackground(alternateColor);
                }
				
				if (colIndex==table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION) ||
						colIndex==table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW)
					){
					component.setEnabled(!isPanelLocked);
				}
				
				int indexSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				if (colIndex==indexSelection) {
					Boolean isSelected= (Boolean) getValueAt(rowIndex,indexSelection);	
					if (isSelected) {
						component.setBackground(UIConstants.COLOR_BLUE);
					}
				} 

				int colViewIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW);
				if (colIndex==colViewIndex && rowIndex==0 && !UIConstants.BALLOON_GRID_SNAPSHOTS_TRIGGERED) {
					UIConstants.BALLOON_GRID_SNAPSHOTS_TRIGGERED=true;
					UIUtils.showCellBalloon("<html>Click on this button to view the Details of the snapshot.",
							BalloonTip.Orientation.RIGHT_BELOW,table,rowIndex,colIndex, 
							tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().isShowBalloons());
				}

				return component;
			}
			
		};
		
		ImageIcon ii=null;
		URL iconURL =null;
		
		Border borderButton=BorderFactory.createEmptyBorder(2, 25, 0, 0);
		int width=26;
		int height=14;
		
		RolloverMouseAdapter rolloverAdapter = new RolloverMouseAdapter(table);
		table.addMouseListener(rolloverAdapter);
		table.addMouseMotionListener(rolloverAdapter);
		
		GridBooleanRenderer gridBooleanRenderer=new GridBooleanRenderer(rolloverAdapter);
		table.getColumn(COLUMN_HEADING_SELECTION).setCellRenderer(gridBooleanRenderer);
		
		GridButtonRenderer gridButtonRenderer=new GridButtonRenderer(borderButton,width,height,true,rolloverAdapter);
		JButton button=gridButtonRenderer.getButton();
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_view.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_view_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setRolloverIcon(new RolloverIcon(ii));
		table.getColumn(COLUMN_HEADING_VIEW).setCellRenderer(gridButtonRenderer);
		ButtonEditor buttonEditor=new ButtonEditor(new JCheckBox(),borderButton,width,height,null,true);
		button=buttonEditor.getButton();
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_view.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setIcon(ii);
		table.getColumn(COLUMN_HEADING_VIEW).setCellEditor(buttonEditor);
								
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		UIUtils.setColumnSize(table,COLUMN_HEADING_SELECTION,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_SELECTION_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_ROW_NUM,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_ROW_NUM_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_SNAPSHOT_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_SNAPSHOT_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_SNAPSHOT_DATE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_SNAPSHOT_DATE_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_STATUS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_STATUS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_CREATED_BY,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_CREATED_BY_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_COMPLETION_DATE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_COMPLETION_DATE_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_INVENTORIES,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_INVENTORIES_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_INVENTORIES_SELECTED,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_INVENTORIES_SELECTED_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_INVENTORIES_FAILED,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_INVENTORIES_FAILED_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORD_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORD_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_DEFAULT_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_DEFAULT_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_ADDED_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_ADDED_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_UPDATED_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_UPDATED_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_VIEW_WIDTH,false);
		if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
			UIUtils.setColumnSize(table,COLUMN_HEADING_UPGRADE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_UPGRADE_WIDTH,false);
		}
		UIUtils.setColumnSize(table,COLUMN_HEADING_TEMPLATE_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TEMPLATE_NAME_WIDTH,false);
						
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
		table.setBackground(Color.decode("#DBDBDB"));
		table.setRowHeight(25);
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 37));

		MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer();
		Enumeration enumeration = table.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			((TableColumn) enumeration.nextElement()).setHeaderRenderer(renderer);
		}
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				Point point = evt.getPoint();
				int row,column;
				row 	= table.rowAtPoint(point);
				column 	= table.columnAtPoint(point);
				if (row >= 0 && column >= 0) {
					int colIndexView=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW);
					int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
					
					if (column==colIndexView) {
						if (!isPanelLocked) {
							try {
								processActionView(row);
							}
							catch (Exception ex) {
								FileUtils.printStackTrace(ex);
								GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+ex.getMessage());
							}
						}
						else {
							GUIUtils.popupInformationMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"You must close any open window first!");
						}
					}
					else 
						if (column==colSelectionIndex) {
							if (!isPanelLocked) {
								List<SnapshotGridRecord> selectedSnapshotGridRecordsInDateOrderList=getSelectedSnapshotGridRecordsInDateOrderList();
								if (selectedSnapshotGridRecordsInDateOrderList.isEmpty()) {
									tabSnapshotsPanel.getSnapshotsActionsPanel().getDeleteButton().setEnabled(false);
								}
								else {
									tabSnapshotsPanel.getSnapshotsActionsPanel().getDeleteButton().setEnabled(true);
								}
							}
							else {
								GUIUtils.popupInformationMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"You must close any open window first!");
							}
						}
				}
			}
		});
		UIUtils.setPopupMenu(table,COLUMN_HEADING_SELECTION);
		
		JPopupMenu popupMenu = table.getComponentPopupMenu();
		popupMenu.addSeparator();
		JMenu submenu = new JMenu("Administration");		
		JMenuItem menuItemCancelSnapshot = new JMenuItem("Cancel 'Processing' snapshots");
		menuItemCancelSnapshot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	cancelSnapshots(table, tabSnapshotsPanel);
            }
        });
		submenu.add(menuItemCancelSnapshot);		
		popupMenu.add(submenu);
						
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				
		JPanel tempPanel=new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.setOpaque(false);
		northPanel.add(tempPanel);
		
		availableSnapshotsLabel=new JLabel();
		InjectUtils.assignArialPlainFont(availableSnapshotsLabel,InjectMain.FONT_SIZE_NORMAL);
		availableSnapshotsLabel.setBackground(Color.decode("#343836"));
		tempPanel.add(availableSnapshotsLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(100, 15)));
		
		loginAsLabel=new JLabel();
		InjectUtils.assignArialPlainFont(loginAsLabel,InjectMain.FONT_SIZE_NORMAL);
		loginAsLabel.setBackground(Color.decode("#343836"));
		tempPanel.add(loginAsLabel);
				
		centerPanel.add(scrollPane);
		
		tempPanel=new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.setOpaque(false);
		southPanel.add(tempPanel);
		JLabel label=new JLabel("Total Records:");
		tempPanel.add(label);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setBackground(Color.decode("#343836"));
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		totalRecordsValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalRecordsValueLabel,InjectMain.FONT_SIZE_NORMAL);
		totalRecordsValueLabel.setBackground(Color.decode("#343836"));
		tempPanel.add(totalRecordsValueLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(100, 15)));
		
		label=new JLabel("Used Space:");
		tempPanel.add(label);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setBackground(Color.decode("#343836"));
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		totalUsedSpaceValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalUsedSpaceValueLabel,InjectMain.FONT_SIZE_NORMAL);
		totalUsedSpaceValueLabel.setBackground(Color.decode("#343836"));
		tempPanel.add(totalUsedSpaceValueLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(100, 15)));
		
		label=new JLabel("Free Space:");
		tempPanel.add(label);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setBackground(Color.decode("#343836"));
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		totalFreeSpaceValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalFreeSpaceValueLabel,InjectMain.FONT_SIZE_NORMAL);
		totalFreeSpaceValueLabel.setBackground(Color.decode("#343836"));
		tempPanel.add(totalFreeSpaceValueLabel);
		tempPanel.add(Box.createRigidArea(new Dimension(100, 15)));
		
		label=new JLabel("Total Space:");
		tempPanel.add(label);
		InjectUtils.assignArialPlainFont(label,InjectMain.FONT_SIZE_NORMAL);
		label.setBackground(Color.decode("#343836"));
		tempPanel.add(Box.createRigidArea(new Dimension(15, 15)));
		totalSpaceValueLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalSpaceValueLabel,InjectMain.FONT_SIZE_NORMAL);
		totalSpaceValueLabel.setBackground(Color.decode("#343836"));
		tempPanel.add(totalSpaceValueLabel);
	}

	private static void cancelSnapshots(final JTable table, final TabSnapshotsPanel tabSnapshotsPanel) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				List<SnapshotGridRecord> selectedSnapshotGridRecordsList = tabSnapshotsPanel.getSnapshotsGridPanel().getSelectedSnapshotGridRecordsInDateOrderList();
				if(selectedSnapshotGridRecordsList.size() <= 0) {
					GUIUtils.popupInformationMessage("No Snapshots selected, please select atleast one 'Processing' snapshot to cancel.");
					return;
				}

				List<SnapshotGridRecord> selectedSnapshotGridRecordsCancelList = new ArrayList<SnapshotGridRecord>();
				
				for (SnapshotGridRecord selectedSnapshotGridRecord : selectedSnapshotGridRecordsList) {
					if(selectedSnapshotGridRecord.getStatus().equalsIgnoreCase(ModelUtils.DB_STATUS_PROCESSING)) {
						selectedSnapshotGridRecordsCancelList.add(selectedSnapshotGridRecord);
					}
				}
				
				if(selectedSnapshotGridRecordsCancelList.size() <= 0) {
					GUIUtils.popupInformationMessage("No 'processing' Snapshots found within the selected snapshots, please select atleast one 'Processing' snapshot to cancel.");
					return;
				}
				
				
				StringBuffer messageBuffer = new StringBuffer(
						"<html><body>You are about to cancel the following 'Processing' snapshots.<br/>" +
								"Please make sure that these snapshots are not being taken by other users.<br/>" +
								"If some other user is currently processing this snapshot they will see undesired states.<br/>" +
								"<br/>"
								);
				for (SnapshotGridRecord selectedSnapshotGridRecordCancel : selectedSnapshotGridRecordsCancelList) {
					messageBuffer.append(selectedSnapshotGridRecordCancel.getName() + "<br/>");
				}
				messageBuffer.append("<br/>");
				messageBuffer.append("You are about to cancel " + selectedSnapshotGridRecordsCancelList.size() + " snapshot(s). This operation is irreversible.<br/>");
				messageBuffer.append("<br/>");
				messageBuffer.append("<b>Are you sure you want to continue?</b>");
				
				int response = JOptionPane.showConfirmDialog(null, messageBuffer.toString(), "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        		if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
        			return;
        		}

    			List<Integer> list=new ArrayList<Integer>();
    			List<SnapshotGridRecord> snapshotToValidateList=new ArrayList<SnapshotGridRecord>();
    			for (SnapshotGridRecord selectedSnapshotGridRecordCancel:selectedSnapshotGridRecordsCancelList) {
    				list.add(selectedSnapshotGridRecordCancel.getSnapshotId());
    				snapshotToValidateList.add(selectedSnapshotGridRecordCancel);
    			}

    			try {
    				validateDeletePermission(tabSnapshotsPanel,snapshotToValidateList);

    				ModelUtils.cancelSnapshotSoft(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel),list);
    				SnapshotController snapshotController=new SnapshotController(tabSnapshotsPanel,false);
    				snapshotController.startExecution();
    			} catch (Exception e) {
    				FileUtils.printStackTrace(e);
    				GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+e.getMessage());
    			}
			}
		});
	}

	protected void processActionDelete() {
		try{			
			String msg="";
			FileUtils.println("INFO : DETECT DELETE ACTION.");
			boolean isPhysicalDelete = tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().isPhysicalDelete();
			List<SnapshotGridRecord> selectedSnapshotGridRecordsList=tabSnapshotsPanel.getSnapshotsGridPanel().getSelectedSnapshotGridRecordsInDateOrderList();
/*			if(isPhysicalDelete && selectedSnapshotGridRecordsList.size()>1){
				throw new Exception("You can only select one snapshot at a time to delete!");
			}*/
			List<Integer> list=new ArrayList<Integer>();
			List<SnapshotGridRecord> snapshotToValidateList=new ArrayList<SnapshotGridRecord>();
			msg="<html><body>You have selected "+selectedSnapshotGridRecordsList.size()+" Snapshot(s) for deletion.<br/>This operation is unreversible -"+
					" ALL those snapshots will be lost.<br/><br/>"+
					"<b>Are you sure to continue with the deletion?</b>";
			for (SnapshotGridRecord snapshotGridRecord:selectedSnapshotGridRecordsList) {
				list.add(snapshotGridRecord.getSnapshotId());
				snapshotToValidateList.add(snapshotGridRecord);
			}
			validateDeletePermission(tabSnapshotsPanel,snapshotToValidateList);

			int response = JOptionPane.showConfirmDialog(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),msg, "Confirmation",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				if(isPhysicalDelete){
					processActionSnapshotPhysicalDelete(selectedSnapshotGridRecordsList);
					
				}else{
					ModelUtils.deleteSnapshotSoft(ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel),list);
					SnapshotController snapshotController=new SnapshotController(tabSnapshotsPanel,false);
					snapshotController.startExecution();
				}
			}			
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+e.getMessage());
		}
	}
	
	protected void processActionSnapshotPhysicalDelete(List<SnapshotGridRecord> snapshotGridRecordsList) {
		try{
			boolean isClearOutSoftDeleteSnapshot = tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().isClearOutSoftDeleteSnapshot();
			SnapshotDeleteSwingWorker swingWorker=new SnapshotDeleteSwingWorker(tabSnapshotsPanel,snapshotGridRecordsList,isClearOutSoftDeleteSnapshot);
			final int width=450;
			final int height=150;
			UIUtils.displayOperationInProgressModalWindow(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),
					width,height,"Initializing...",swingWorker,SnapshotMain.getSharedApplicationIconPath());
			
		}catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"Error: "+e.getMessage());
		}
	}

	
	public static void validateDeletePermission(TabSnapshotsPanel tabSnapshotsPanel,List<SnapshotGridRecord> selectedSnapshotGridRecordsList) throws Exception {
		if ( ! tabSnapshotsPanel.getServerSelectionPanel().hasUserWritePermission()) {
			throw new Exception("You are not allowed to delete Snapshots!");
		}
		UserInformation selectedUser=tabSnapshotsPanel.getServerSelectionPanel().getSelectedUser();
		if (selectedUser!=null) {
			Map<String, String> snapshotEnvironmentProperties=ModelUtils.getSnapshotEnvironmentProperties(tabSnapshotsPanel);
			List<UserInformation> userInformationList=ModelUtils.getSnapshotUserInformationList(snapshotEnvironmentProperties);
			Map<Integer, UserInformation> userIdToUserInformationMap=ModelUtils.getUserIdToUserInformationMap(userInformationList);
			for (SnapshotGridRecord snapshotGridRecord:selectedSnapshotGridRecordsList) {
				int userId=snapshotGridRecord.getUserId();
				if (userId<=0) {
					// old snapshot created by unknown so we don't allow to delete???
					throw new Exception("The snapshot '"+snapshotGridRecord.getName()+"' cannot be deleted.");
				}
				else {
					if (userId != selectedUser.getId()) {
						throw new Exception("Only the creator '"+
								userIdToUserInformationMap.get(userId).getLoginName()+"' of the snapshot '"+
								snapshotGridRecord.getName()+"' can delete it!");
					}
				}
			}		

			GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"You are not allowed to create Snapshots!");
			return;
		}
	}

	protected void processActionView(int row) throws Exception {
		SnapshotGridRecord snapshotGridRecord=snapshotGridRecords.get(row);
		if (snapshotGridRecord.getStatus().equalsIgnoreCase(ModelUtils.DB_STATUS_PROCESSING)) {
			GUIUtils.popupInformationMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"You cannot view this snapshot until the processing is complete.");
			return;
		}
		if ( ! tabSnapshotsPanel.getServerSelectionPanel().hasUserReadPermission()) {
			GUIUtils.popupErrorMessage(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),"You are not allowed to view Snapshots!");
			return;
		}
		
		if (snapshotGridRecord.isConversion()) {
			tabSnapshotsPanel.getSnapshotsActionsPanel().processActionLaunchUpgrade(snapshotGridRecord);
		}
		else {
			SnapshotViewerFrame snapshotViewerFrame=new SnapshotViewerFrame(tabSnapshotsPanel,snapshotGridRecord);
			UIUtils.setFramePosition(tabSnapshotsPanel.getMainPanel().getSnapshotMain().getRootFrame(),snapshotViewerFrame);
			tabSnapshotsPanel.setSnapshotViewerFrame(snapshotViewerFrame);
			snapshotViewerFrame.setVisible(true);
			snapshotViewerFrame.toFront();
		}		
	}
	
	public void resetGrid() {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		tableModel.setRowCount(0);
		table.repaint();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void displaySnapshots(List<SnapshotGridRecord> snapshotGridRecords,
			Map<Integer, UserInformation> userIdToUserInformationMap) throws Exception {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		tableModel.setRowCount(0);
		int counter=0;
		this.snapshotGridRecords=snapshotGridRecords;
		availableSnapshotsLabel.setText("Available Snapshots: "+Utils.formatNumberWithComma(snapshotGridRecords.size()));
		Set<String> activatedPlugins=ModelUtils.getActivatedPlugins();
		for (SnapshotGridRecord snapshotGridRecord:snapshotGridRecords) {
			Vector rowGrid = new Vector();
			counter++;
			rowGrid.add(false);
			rowGrid.add(Utils.formatNumberWithComma(counter));
			rowGrid.add(snapshotGridRecord.getName());
			rowGrid.add("View");
			if (activatedPlugins.contains(UtilsConstants.PLUGIN_SNAPSHOT_DATA_CONVERSION)) {
				if (snapshotGridRecord.isConversion()) {
					rowGrid.add("Upgrade");
				}
				else {
					rowGrid.add("Snapshot");
				}
			}
			String templateName = snapshotGridRecord.getTemplateName();
			rowGrid.add(templateName);
			String createdOn=snapshotGridRecord.getCreatedOn();
			rowGrid.add(createdOn);
			String status=snapshotGridRecord.getStatus();
			String uiStatus=ModelUtils.getUIStatusFromDBStatus(status);
			rowGrid.add(uiStatus);
			
			String createdBy="";
			String osUserName=snapshotGridRecord.getOsUserName();
			UserInformation userInformation=userIdToUserInformationMap.get(snapshotGridRecord.getUserId());
			if (userInformation!=null) {
				createdBy=userInformation.getLoginName()+" ("+osUserName+")";
			}
			else {
				createdBy=osUserName;
			}
			rowGrid.add(createdBy);
			rowGrid.add(snapshotGridRecord.getCompletedOn());
			rowGrid.add(Utils.formatNumberWithComma(snapshotGridRecord.getTotalInventories()));
			rowGrid.add(Utils.formatNumberWithComma(snapshotGridRecord.getTotalInventoriesSelected()));
			rowGrid.add(Utils.formatNumberWithComma(snapshotGridRecord.getTotalInventoriesFailed()));
			int totalRecords=snapshotGridRecord.getTotalRecords();
			rowGrid.add(Utils.formatNumberWithComma(totalRecords));
			
			if (getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowTotalDetails()) {
				int totalDefaultRecords=snapshotGridRecord.getTotalDefaultRecords();
				rowGrid.add(Utils.formatNumberWithComma(totalDefaultRecords));
				int totalAddedRecords=snapshotGridRecord.getTotalAddedRecords();
				rowGrid.add(Utils.formatNumberWithComma(totalAddedRecords));
				int totalUpdatedRecords=snapshotGridRecord.getTotalUpdatedRecords();
				rowGrid.add(Utils.formatNumberWithComma(totalUpdatedRecords));		
			}
			else {
				rowGrid.add(UIConstants.UI_NA);
				rowGrid.add(UIConstants.UI_NA);
				rowGrid.add(UIConstants.UI_NA);
			}
			
			tableModel.addRow(rowGrid);
		}
		table.repaint();
	}

	public TabSnapshotsPanel getTabSnapshotsPanel() {
		return tabSnapshotsPanel;
	}
	
	public void lockPanel() {
		isPanelLocked=true;
		refreshUI();
	}
	
	public void unlockPanel() {
		isPanelLocked=false;
		refreshUI();
	}	
	
	private void refreshUI() {
		SwingUtilities.invokeLater(new Runnable(){
		    public void run(){
		       table.repaint();
		    }
		}); 
	}
		
	public List<SnapshotGridRecord> getSelectedSnapshotGridRecordsInDateOrderList()	{
		TableModel model = table.getModel();
		int numRows = model.getRowCount();
		List<SnapshotGridRecord> toReturn=new ArrayList<SnapshotGridRecord>();
		int colIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		for (int i=0; i < numRows; i++) {
			boolean isSelected=(boolean) model.getValueAt(i,colIndex);
			if (isSelected) {
				SnapshotGridRecord snapshotGridRecord=snapshotGridRecords.get(i);
				toReturn.add(0,snapshotGridRecord);
			}
		}
		return toReturn;
	}
	
	public void displayTotals(final int grandTotalRecords,final MemoryInformation memoryInformation){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				try {
					totalRecordsValueLabel.setText(Utils.formatNumberWithComma(grandTotalRecords));
					if (memoryInformation!=null) {
						totalSpaceValueLabel.setText(""+memoryInformation.getTotalSpaceInGB()+" GB");
						totalUsedSpaceValueLabel.setText(""+memoryInformation.getTotalUsedSpaceInGB()+" GB");
						totalFreeSpaceValueLabel.setText(""+memoryInformation.getTotalFreeSpaceInGB()+" GB");
					}
				}
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
			}
		});
	}
	
	protected void resetLabels() {
		availableSnapshotsLabel.setText("");
		totalRecordsValueLabel.setText("");
		totalSpaceValueLabel.setText("");
		totalUsedSpaceValueLabel.setText("");
		totalFreeSpaceValueLabel.setText("");
		loginAsLabel.setText("");
	}

	public List<SnapshotGridRecord> getSnapshotGridRecords() {
		return snapshotGridRecords;
	}

	public JLabel getLoginAsLabel() {
		return loginAsLabel;
	}
	
}
