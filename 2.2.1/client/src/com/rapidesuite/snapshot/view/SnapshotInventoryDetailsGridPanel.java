package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.StringUtils;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ButtonEditor;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.InventoryCleanupSwingWorker;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class SnapshotInventoryDetailsGridPanel extends JPanel {

	public static String COLUMN_HEADING_SELECTION="Selection";
	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_MODULE_NAME="Application\nNames";
	public static String COLUMN_HEADING_FORM_PATH="Menu Paths";
	public static String COLUMN_HEADING_FORM_NAME="Form Name";
	public static String COLUMN_HEADING_FORM_TYPE="Form Level";
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	public static String COLUMN_HEADING_STATUS="Snapshot\nStatus";
	public static String COLUMN_HEADING_FILTERING_RESULT="Filtering\nResult";
	public static String COLUMN_HEADING_TOTAL_RECORDS_COUNT="Total\nRecords";
	public static String COLUMN_HEADING_TOTAL_DEFAULT_RECORDS_COUNT="Total\nDefault Records";
	public static String COLUMN_HEADING_TOTAL_ADDED_RECORDS_COUNT="Total\nAdded Records";
	public static String COLUMN_HEADING_TOTAL_UPDATED_RECORDS_COUNT="Total\nUpdated Records";
	public static String COLUMN_HEADING_REMARKS="Remarks";
	public static String COLUMN_HEADING_EXECUTION_TIME="Execution\ntime";
	public static String COLUMN_HEADING_RAW_TIME="Raw time\n(in Secs)";
	public static String COLUMN_HEADING_VIEW="View\nData";
	public static String COLUMN_HEADING_CLEANUP="Cleanup";
	public static String COLUMN_HEADING_COMPLETED_ON="Completed on";
	public static String COLUMN_HEADING_CREATED_ON="Created on";
	
	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_SELECTION_WIDTH=60;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_MODULE_NAME_WIDTH=80;
	private static int COLUMN_HEADING_FORM_PATH_WIDTH=90;
	private static int COLUMN_HEADING_FORM_NAME_WIDTH=160;
	private static int COLUMN_HEADING_FORM_TYPE_WIDTH=90;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=160;
	private static int COLUMN_HEADING_STATUS_WIDTH=100;
	private static int COLUMN_HEADING_FILTERING_RESULT_WIDTH=80;
	private static int COLUMN_HEADING_REMARKS_WIDTH=180;
	private static int COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH=70;
	private static int COLUMN_HEADING_TOTAL_DEFAULT_RECORDS_COUNT_WIDTH=100;
	private static int COLUMN_HEADING_TOTAL_ADDED_RECORDS_COUNT_WIDTH=100;
	private static int COLUMN_HEADING_TOTAL_UPDATED_RECORDS_COUNT_WIDTH=120;
	private static int COLUMN_HEADING_EXECUTION_TIME_WIDTH=80;
	private static int COLUMN_HEADING_RAW_TIME_WIDTH=70;
	private static int COLUMN_HEADING_VIEW_WIDTH=80;
	private static int COLUMN_HEADING_CLEANUP_WIDTH=80;
	private static int COLUMN_HEADING_CREATED_ON_WIDTH=130;
	private static int COLUMN_HEADING_COMPLETED_ON_WIDTH=130;
	
	private SnapshotInventoryGridFrame snapshotInventoryGridFrame;
	protected JTable table;
	private FilteringTable filteringTable;
	protected List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList;
	private boolean hasSelectionColumn;
	private boolean hasViewChangesColumn;
	private boolean hasFilteringResultColumn;
	private SnapshotInventoryTotalsPanel snapshotInventoryTotalsPanel;
	private boolean isRefreshEveryListOnUse;
	private String currentCellValueClicked;
	private boolean hasCleanupColumn;
	private boolean isSnapshotCreation;
	private boolean isUpgrade;
	private boolean hasMenuPathColumn;
	private boolean isLoadAllInventorySuccessfully = false;

	public SnapshotInventoryDetailsGridPanel(SnapshotInventoryGridFrame snapshotInventoryGridFrame,
			boolean hasSelectionColumn,boolean hasViewChangesColumn,boolean hasFilteringResultColumn,boolean isRefreshEveryListOnUse,
			boolean hasCleanupColumn,boolean isSnapshotCreation,boolean isShowBalloons,boolean isUpgrade,int topBorderSpace,
			boolean hasMenuPathColumn) {
		this.hasMenuPathColumn=hasMenuPathColumn;
		this.hasSelectionColumn=hasSelectionColumn;
		this.isSnapshotCreation=isSnapshotCreation;
		this.isRefreshEveryListOnUse=isRefreshEveryListOnUse;
		this.hasCleanupColumn=hasCleanupColumn;
		this.hasViewChangesColumn=hasViewChangesColumn;
		this.hasFilteringResultColumn=hasFilteringResultColumn;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		this.snapshotInventoryGridFrame=snapshotInventoryGridFrame;
		this.isUpgrade=isUpgrade;
		createComponents(isShowBalloons,topBorderSpace);
	}

	@SuppressWarnings("rawtypes")
	private void createComponents(boolean isShowBalloons,int topBorderSpace){
		JPanel northPanel=InjectUtils.getXPanel(Component.CENTER_ALIGNMENT);
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#dbdcdf"));
		add(northPanel,BorderLayout.NORTH);

		JPanel centerPanel=new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		add(centerPanel,BorderLayout.CENTER);

		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_SELECTION);
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_MODULE_NAME);
		if (hasMenuPathColumn) {
			columnNames.add(COLUMN_HEADING_FORM_PATH);
		}
		columnNames.add(COLUMN_HEADING_FORM_NAME);
		columnNames.add(COLUMN_HEADING_INVENTORY_NAME);
		columnNames.add(COLUMN_HEADING_VIEW);
		columnNames.add(COLUMN_HEADING_FORM_TYPE);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_FILTERING_RESULT);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_DEFAULT_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_ADDED_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_UPDATED_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_REMARKS);
		columnNames.add(COLUMN_HEADING_EXECUTION_TIME);	
		columnNames.add(COLUMN_HEADING_RAW_TIME);
		columnNames.add(COLUMN_HEADING_CREATED_ON);
		columnNames.add(COLUMN_HEADING_COMPLETED_ON);
		columnNames.add(COLUMN_HEADING_CLEANUP);

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
				return String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				int colViewIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW);
				int colCleanupIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_CLEANUP);
				if (column==colSelectionIndex) {
					if (isUpgrade) {
						return  snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel()!=null &&
								snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton()!=null &&
								snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton().isEnabled();
					}
					if (hasViewChangesColumn) {
						String totalRecordsCount= (String) getValueAt(row, table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_COUNT));		
						return !totalRecordsCount.equals("0");
					}
					return snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton()!=null &&
							snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton().isEnabled();
				}
				else
					if (column==colViewIndex) {
						return true;
					}
					else
						if (column==colCleanupIndex) {
							if (hasCleanupColumn) {
								return snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton()!=null &&
										snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton().isEnabled();
							}
							return true;
						}
				return false;
			}

			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);

				try {
					int colApplicationNameIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_MODULE_NAME);
					int colFormPathIndex=-9999;
					if (hasMenuPathColumn) {
						colFormPathIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_FORM_PATH);
					}
					int colFormNameIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_FORM_NAME);
					int colInventoryNameIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_INVENTORY_NAME);
					int colRemarksIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);

					if  (colIndex==colApplicationNameIndex ||
							colIndex==colFormPathIndex ||
							colIndex==colFormNameIndex ||
							colIndex==colInventoryNameIndex ||
							colIndex==colRemarksIndex
							) {
						
						String tableIdText="";
						if  (colIndex==colInventoryNameIndex) {
							int modelRow=table.convertRowIndexToModel(rowIndex);
							SnapshotInventoryGridRecord snapshotInventoryGridRecord=snapshotInventoryGridRecordsList.get(modelRow);
							tableIdText=" (ID: "+snapshotInventoryGridRecord.getTableId()+")";
						}
						String value=(String) getValueAt(rowIndex, colIndex);
						value=value.replaceAll("\n","<br>")+tableIdText;
						tip="<html>"+value+"</html>";
					}
				}
				catch (RuntimeException e1) {
					//catch null pointer exception if mouse is over an empty line
				}

				return tip;
			}

			/*
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
                ((Graphics2D) g).setPaint(new GradientPaint(0f, 0f, Color.WHITE,getWidth(), getHeight(), Color.ORANGE));
                ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER).derive(0.3f));
                g.fillRect(0, 0, getWidth(), getHeight());
			}
			 */

			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int vColIndex) {
				Component component =null;
				try{
					component = super.prepareRenderer(renderer, rowIndex, vColIndex);

					int colIndexSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
					String status= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS));				
					component.setBackground(Color.WHITE);

					if (vColIndex==colIndexSelection) {
						boolean isEnabled=true;
						
						if (isUpgrade) {
							isEnabled=snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton()!=null &&
									snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton().isEnabled();
						}
						else {
							if (hasViewChangesColumn) {
								String totalRecordsCount= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_COUNT));		
								isEnabled=!totalRecordsCount.equals("0");
							}
							else {
								isEnabled=snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton()!=null &&
										snapshotInventoryGridFrame.getSnapshotInventoryGridActionPanel().getExecutionButton().isEnabled();
							}
						}
						component.setEnabled(isEnabled);
					}

					if (hasViewChangesColumn && !isUpgrade) { 
						if (rowIndex%2 == 0){
							component.setBackground(Color.WHITE);
						}
						else {
							Color alternateColor =Color.decode("#dbdbdb");
							component.setBackground(alternateColor);
						}      
					}
					else{
						if (status!=null && status.equalsIgnoreCase(UIConstants.UI_STATUS_PROCESSING)) {
							component.setBackground(Color.decode("#fbf468"));
						}
						else 
							if (status!=null && (
									status.equalsIgnoreCase(UIConstants.UI_STATUS_FAILED) || status.equalsIgnoreCase(UIConstants.UI_STATUS_CANCELLED)) ) {
								component.setBackground(UIConstants.COLOR_RED);
							}
							else 
								if (status!=null && status.equalsIgnoreCase(UIConstants.UI_STATUS_COMPLETED)) {
									component.setBackground(Color.decode("#6bb23d"));
								}
								else 
									if (status!=null && status.equalsIgnoreCase(UIConstants.UI_STATUS_WARNING)) {
										component.setBackground(UIConstants.COLOR_ORANGE);
									}
									else
										if (status!=null &&  (
												status.equalsIgnoreCase(UIConstants.UI_STATUS_PENDING) || status.isEmpty()) ) {
											if (rowIndex%2 == 0){
												component.setBackground(Color.WHITE);
											}
											else {
												Color alternateColor =Color.decode("#dbdbdb");
												component.setBackground(alternateColor);
											}     
										}
					}
					if (isCellSelected(rowIndex, vColIndex)) {
						component.setBackground(UIConstants.COLOR_BLUE);
					} 

					return component;
				}
				catch(Exception e) {
					return component;
				}
			}
		};

		if (hasSelectionColumn) {
			UIUtils.setColumnSize(table,COLUMN_HEADING_SELECTION,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_SELECTION_WIDTH,false);
		}
		else {
			UIUtils.setColumnSize(table,COLUMN_HEADING_SELECTION,0,0,true);
		}
		UIUtils.setColumnSize(table,COLUMN_HEADING_ROW_NUM,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_ROW_NUM_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_MODULE_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_MODULE_NAME_WIDTH,false);
		if (hasMenuPathColumn) {
			UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_PATH,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_PATH_WIDTH,false);
		}
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_TYPE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_TYPE_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_INVENTORY_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_INVENTORY_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_STATUS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_STATUS_WIDTH,false);
		if (hasFilteringResultColumn) {
			UIUtils.setColumnSize(table,COLUMN_HEADING_FILTERING_RESULT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FILTERING_RESULT_WIDTH,false);
		}
		else {
			UIUtils.setColumnSize(table,COLUMN_HEADING_FILTERING_RESULT,0,0,true);
		}
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_DEFAULT_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_DEFAULT_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_ADDED_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_ADDED_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_UPDATED_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_UPDATED_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_REMARKS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_REMARKS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_EXECUTION_TIME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_EXECUTION_TIME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_RAW_TIME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_RAW_TIME_WIDTH,false);
		if (hasViewChangesColumn) {
			UIUtils.setColumnSize(table,COLUMN_HEADING_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_VIEW_WIDTH,false);
		}
		else {
			UIUtils.setColumnSize(table,COLUMN_HEADING_VIEW,0,0,true);
		}
		if (hasCleanupColumn) {
			UIUtils.setColumnSize(table,COLUMN_HEADING_CLEANUP,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_CLEANUP_WIDTH,false);
		}
		else {
			UIUtils.setColumnSize(table,COLUMN_HEADING_CLEANUP,0,0,true);
		}
		UIUtils.setColumnSize(table,COLUMN_HEADING_CREATED_ON,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_CREATED_ON_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_COMPLETED_ON,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_COMPLETED_ON_WIDTH,false);		
		RolloverMouseAdapter rolloverAdapter = new RolloverMouseAdapter(table);
		table.addMouseListener(rolloverAdapter);
		table.addMouseMotionListener(rolloverAdapter);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_MODULE_NAME).setCellRenderer(leftRenderer);
		if (hasMenuPathColumn) {
			table.getColumn(COLUMN_HEADING_FORM_PATH).setCellRenderer(leftRenderer);
		}
		table.getColumn(COLUMN_HEADING_FORM_TYPE).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FORM_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_STATUS).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_FILTERING_RESULT).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_DEFAULT_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_ADDED_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_UPDATED_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_REMARKS).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_INVENTORY_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_EXECUTION_TIME).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_RAW_TIME).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_CREATED_ON).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_COMPLETED_ON).setCellRenderer(leftRenderer);
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				Point point = evt.getPoint();
				int row,column;
				row 	= table.rowAtPoint(point);
				column 	= table.columnAtPoint(point);
				Object value=table.getValueAt(row, column);
				if (value!=null) { 
					currentCellValueClicked=value.toString();
					//System.out.println("currentCellValueClicked:"+currentCellValueClicked);
					StringSelection sel  = new StringSelection(currentCellValueClicked); 
					Clipboard systemClipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
					systemClipboard.setContents(sel, sel); 
				}				
				if (row >= 0 && column >= 0) {
					int colIndexAction=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW);		
					int colIndexCleanup=table.getColumnModel().getColumnIndex(COLUMN_HEADING_CLEANUP);		
					if (column==colIndexAction) {
						viewChanges( row);
					}
					else 
						if (column==colIndexCleanup) {
							try{
								cleanupTables( row);
							}
							catch (Exception ex) {
								FileUtils.printStackTrace(ex);
								GUIUtils.popupErrorMessage("Error: "+ex.getMessage());
							}
						}
				}
			}
		});

		ImageIcon ii=null;
		URL iconURL =null;

		Border borderButton=BorderFactory.createEmptyBorder(2, 25, 0, 0);
		int width=26;
		int height=14;
		
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

		gridButtonRenderer=new GridButtonRenderer(borderButton,width,height,true,rolloverAdapter);
		button=gridButtonRenderer.getButton();
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_delete.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_delete_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setRolloverIcon(new RolloverIcon(ii));
		table.getColumn(COLUMN_HEADING_CLEANUP).setCellRenderer(gridButtonRenderer);
		
		buttonEditor=new ButtonEditor(new JCheckBox(),borderButton,width,height,null,true);
		button=buttonEditor.getButton();
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_delete.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setIcon(ii);
		table.getColumn(COLUMN_HEADING_CLEANUP).setCellEditor(buttonEditor);

		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
		table.setBackground(Color.decode("#DBDBDB"));
		table.setRowHeight(22);
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 80));

		JScrollPane variableScroll = new JScrollPane(table ,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};

		snapshotInventoryTotalsPanel=new ViewerInventoryTotalsPanel(this,topBorderSpace);
		filteringTable=new SnapshotFilteringTable(columnNames,variableScroll,table,33,snapshotInventoryTotalsPanel,isRefreshEveryListOnUse,
				isShowBalloons);
		centerPanel.add(filteringTable.getFixedScrollPane(),BorderLayout.NORTH);
		centerPanel.add(variableScroll,BorderLayout.CENTER);
		
		if (hasSelectionColumn) {
			UIUtils.setPopupMenu(table,COLUMN_HEADING_SELECTION);
			table.addKeyListener(new  KeyAdapter()
			{
				@Override 
				public void keyReleased(KeyEvent event) { 
					//	if (event.isControlDown()) { 
					//	if (event.getKeyCode()==KeyEvent.VK_C) { // Copy                        
					//System.out.println("keyReleased, currentCellValueClicked:"+currentCellValueClicked);
					if (currentCellValueClicked==null) {
						currentCellValueClicked="";
					}
					StringSelection sel  = new StringSelection(currentCellValueClicked); 
					Clipboard systemClipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
					systemClipboard.setContents(sel, sel); 
					//	} 
					//	}
				};
			});
		}

		JPanel southPanel=new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		add(southPanel,BorderLayout.SOUTH);
		southPanel.add(snapshotInventoryTotalsPanel);
	}

	protected void cleanupTables(int row) throws Exception {
		
		if (snapshotInventoryGridFrame instanceof SnapshotViewerFrame) {
			SnapshotViewerFrame snapshotViewerFrame=(SnapshotViewerFrame)snapshotInventoryGridFrame;
			List<SnapshotGridRecord> snapshotToValidatelist=new ArrayList<SnapshotGridRecord>();
			SnapshotGridRecord snapshotGridRecord=snapshotViewerFrame.getSnapshotGridRecord();
			snapshotToValidatelist.add(snapshotGridRecord);
			SnapshotsGridPanel.validateDeletePermission(snapshotInventoryGridFrame.getTabSnapshotsPanel(),snapshotToValidatelist);
		}
		
		int response = JOptionPane.showConfirmDialog(null, "All the Snapshots History for this Inventory will be deleted, please confirm?", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			int modelRow=table.convertRowIndexToModel(row);
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=getSnapshotInventoryGridRecordsList().get(modelRow);

			InventoryCleanupSwingWorker swingWorker=new InventoryCleanupSwingWorker(
					snapshotInventoryGridFrame.getTabSnapshotsPanel(),snapshotInventoryGridRecord);
			final int width=450;
			final int height=150;
			UIUtils.displayOperationInProgressModalWindow(snapshotInventoryGridFrame,width,height,"Cleanup in progress...",swingWorker,SnapshotMain.getSharedApplicationIconPath());
		}
	}

	protected void saveGridToExcel() {
		String EXCEL_FILE_EXTENSION=".xlsx";
		JFileChooser fileChooser = new JFileChooser();
		
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
		Date now = new Date();
		String strDate = format.format(now);
		File file=new File("rapidsnapshot-grid-"+strDate+EXCEL_FILE_EXTENSION);
		
		fileChooser.setSelectedFile(file);
		fileChooser.setDialogTitle("Specify a file to save");   

		int userSelection = fileChooser.showSaveDialog(snapshotInventoryGridFrame);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			int indexOf=fileToSave.getName().toLowerCase().indexOf(EXCEL_FILE_EXTENSION);
			File outputFile=fileToSave;
			if (indexOf==-1) {
				String newFileName=fileToSave.getAbsolutePath()+EXCEL_FILE_EXTENSION;
				outputFile=new File(newFileName);
			}
			try{
				writeGridRecordsListToExcel(outputFile,true);
			}catch(Exception e){
				FileUtils.printStackTrace(e);
			}
		}
	}
	
	
	public void saveGridToExcelInAutomationMode(File outputFile) throws Exception{
		try{
			String EXCEL_FILE_EXTENSION=".xlsx";
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH-mm-ss");
			Date now = new Date();
			String strDate = format.format(now);
			outputFile=new File(outputFile,"rapidsnapshot-grid-"+strDate+EXCEL_FILE_EXTENSION);	
			writeGridRecordsListToExcel(outputFile,false);	
		}catch(Exception e){
			FileUtils.printStackTrace(e);
			throw new Exception("Cannot save the grid to excel file, error : "+e.getMessage());
		}	
	}
	
	public void writeGridRecordsListToExcel(File outputFile,boolean isDisplayPopup) throws Exception{
		try {
			List<SnapshotInventoryGridRecord> filteredSnapshotInventoryGridRecordsList=getFilteredSnapshotInventoryGridRecordsList();
			ModelUtils.saveSnapshotInventoryDetailsGridToExcel(outputFile,filteredSnapshotInventoryGridRecordsList,hasFilteringResultColumn);
			if(isDisplayPopup){
				GUIUtils.popupInformationMessage("File saved!");
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			if(isDisplayPopup){
				GUIUtils.popupErrorMessage("Unable to save the grid to Excel: "+e.getMessage());
			}
		}	
	}
	public void displayInventories(List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		filteringTable.removeRowFilter();
		tableModel.setRowCount(0);

		this.snapshotInventoryGridRecordsList=snapshotInventoryGridRecordList;
		//totalRowsCountLabel.setText(UIConstants.LABEL_TOTAL_ROWS + snapshotInventoryGridRecordList.size());
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
			Vector<Object> rowGrid = new Vector<Object>();

			String filteringResult=snapshotInventoryGridRecord.getFilteringResult();
			if (filteringResult==null) {
				filteringResult="";
			}
			
			if (isSnapshotCreation) {
				rowGrid.add(true);
			}
			else {
				int totalRecords=snapshotInventoryGridRecord.getTotalRecords();
				if (totalRecords==0) {
					rowGrid.add(false);
				}
				else {
					if (filteringResult!=null && filteringResult.equals(UIConstants.FILTERED_IN_PREFIX)) {
						rowGrid.add(true);
					}
					else {
						rowGrid.add(false);
					}
				}
			}
			String formattedRowNumber="";
			try{
				int visibleRowSequence=snapshotInventoryGridRecord.getGridIndex()+1;
				formattedRowNumber=Utils.formatNumberWithComma(visibleRowSequence);
			}
			catch(Exception e) {
			}
			rowGrid.add(formattedRowNumber);
			rowGrid.add(snapshotInventoryGridRecord.getFormInformation().getFormattedApplicationNames());
			if (hasMenuPathColumn) {
				rowGrid.add(snapshotInventoryGridRecord.getFormInformation().getFormattedFormPaths());
			}
			rowGrid.add(snapshotInventoryGridRecord.getFormInformation().getFormName());
			rowGrid.add(snapshotInventoryGridRecord.getInventoryName());
			rowGrid.add("Open");
			rowGrid.add(snapshotInventoryGridRecord.getFormInformation().getFormType());	
			rowGrid.add(ModelUtils.getUIStatusFromDBStatus(snapshotInventoryGridRecord.getStatus()));

			
			rowGrid.add(filteringResult);

			if (snapshotInventoryGridRecord.isDisplayTotalRecords()) {
				rowGrid.add(UIUtils.getFormattedNumber(snapshotInventoryGridRecord.getTotalRecords()));
			}
			else {
				rowGrid.add(UIConstants.UI_NA);
			}
			
			if (snapshotInventoryGridRecord.isDisplayTotalDefaultRecords()) {
				rowGrid.add(UIUtils.getFormattedNumber(snapshotInventoryGridRecord.getTotalDefaultRecords()));
			}
			else {
				rowGrid.add(UIConstants.UI_NA);
			}
			
			if (snapshotInventoryGridRecord.isDisplayTotalAddedRecords()) {
				rowGrid.add(UIUtils.getFormattedNumber(snapshotInventoryGridRecord.getTotalAddedRecords()));
			}
			else {
				rowGrid.add(UIConstants.UI_NA);
			}
			
			if (snapshotInventoryGridRecord.isDisplayTotalUpdatedRecords()) {
				rowGrid.add(UIUtils.getFormattedNumber(snapshotInventoryGridRecord.getTotalUpdatedRecords()));
			}
			else {
				rowGrid.add(UIConstants.UI_NA);
			}
			
			StringBuffer remarkStringBuffer = new StringBuffer();
			if(snapshotInventoryGridRecord.getFormInformation().isDisableOUAndLevelFiltering()) {
				remarkStringBuffer.append(ModelUtils.DISABLE_OU_AND_LEVEL_FILTER_REMARK);
			}
			if(StringUtils.isNotBlank(snapshotInventoryGridRecord.getRemarks())) {
				remarkStringBuffer.append(snapshotInventoryGridRecord.getRemarks());
			}			
			rowGrid.add(remarkStringBuffer.toString());
			
			rowGrid.add(snapshotInventoryGridRecord.getExecutionTime());
			long rawTimeInSecs=snapshotInventoryGridRecord.getRawTimeInSecs();
			rowGrid.add(""+rawTimeInSecs);
			if(snapshotInventoryGridRecord.getCreatedOn()!=null){
				rowGrid.add(snapshotInventoryGridRecord.getCreatedOn());
			}else{
				rowGrid.add("");
			}
			if(snapshotInventoryGridRecord.getCompletedOn()!=null){
				rowGrid.add(snapshotInventoryGridRecord.getCompletedOn());
			}else{
				rowGrid.add("");
			}

			tableModel.addRow(rowGrid);
		}
		snapshotInventoryTotalsPanel.updateTotalLabels();
		filteringTable.applyFiltering();
		isLoadAllInventorySuccessfully = true;

	}	

	public void refreshGrid(final List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TableModel model = table.getModel();
				for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
					int gridIndex=snapshotInventoryGridRecord.getGridIndex();
					int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
					int colIndexExecutionTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_EXECUTION_TIME);
					int colIndexTotalRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
					int colIndexTotalDefaultRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_DEFAULT_RECORDS_COUNT);
					int colIndexTotalAddedRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_ADDED_RECORDS_COUNT);
					int colIndexTotalUpdatedRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_UPDATED_RECORDS_COUNT);
					int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
					int colIndexRawTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_RAW_TIME);
					int colIndexCreatedOn = table.getColumnModel().getColumnIndex(COLUMN_HEADING_CREATED_ON);
					int colIndexCompletedOn = table.getColumnModel().getColumnIndex(COLUMN_HEADING_COMPLETED_ON);
					try {
						if (snapshotInventoryGridRecord.getStatus().equalsIgnoreCase(UIConstants.UI_STATUS_COMPLETED) ||
							snapshotInventoryGridRecord.getStatus().equalsIgnoreCase(UIConstants.UI_STATUS_WARNING)) {
							int totalRecords=snapshotInventoryGridRecord.getTotalRecords();
							model.setValueAt(Utils.formatNumberWithComma(totalRecords),gridIndex,colIndexTotalRecords);

							if (snapshotInventoryGridRecord.isDisplayTotalDefaultRecords()) {
								int totalDefaultRecords=snapshotInventoryGridRecord.getTotalDefaultRecords();
								model.setValueAt(Utils.formatNumberWithComma(totalDefaultRecords),gridIndex,colIndexTotalDefaultRecords);
							}
							else {
								model.setValueAt(UIConstants.UI_NA,gridIndex,colIndexTotalDefaultRecords);
							}
							
							if (snapshotInventoryGridRecord.isDisplayTotalAddedRecords()) {
								int totalAddedRecords=snapshotInventoryGridRecord.getTotalAddedRecords();
								model.setValueAt(Utils.formatNumberWithComma(totalAddedRecords),gridIndex,colIndexTotalAddedRecords);
							}
							else {
								model.setValueAt(UIConstants.UI_NA,gridIndex,colIndexTotalAddedRecords);
							}
							
							if (snapshotInventoryGridRecord.isDisplayTotalUpdatedRecords()) {
								int totalUpdatedRecords=snapshotInventoryGridRecord.getTotalUpdatedRecords();
								model.setValueAt(Utils.formatNumberWithComma(totalUpdatedRecords),gridIndex,colIndexTotalUpdatedRecords);
							}
							else {
								model.setValueAt(UIConstants.UI_NA,gridIndex,colIndexTotalUpdatedRecords);
							}
						}

						//long startTime=snapshotInventoryGridRecord.getStartTime();
						String executionTime=snapshotInventoryGridRecord.getExecutionTime();
						if (executionTime==null) {
							model.setValueAt("",gridIndex,table.getColumnModel().getColumnIndex(COLUMN_HEADING_EXECUTION_TIME));
						}
						else {
							model.setValueAt(executionTime,gridIndex,colIndexExecutionTime);
							String rawTimeInSecs=""+snapshotInventoryGridRecord.getRawTimeInSecs();
							model.setValueAt(rawTimeInSecs,gridIndex,colIndexRawTime);
						}					
						if(snapshotInventoryGridRecord.getCreatedOn()==null){
							model.setValueAt("",gridIndex,table.getColumnModel().getColumnIndex(COLUMN_HEADING_CREATED_ON));
						}else{
							model.setValueAt(snapshotInventoryGridRecord.getCreatedOn(),gridIndex,colIndexCreatedOn);
						}
						if(snapshotInventoryGridRecord.getCompletedOn()==null){
							model.setValueAt("",gridIndex,table.getColumnModel().getColumnIndex(COLUMN_HEADING_COMPLETED_ON));
						}else{
							model.setValueAt(snapshotInventoryGridRecord.getCompletedOn(),gridIndex,colIndexCompletedOn);
						}
						String remarks=snapshotInventoryGridRecord.getRemarks();
						model.setValueAt(remarks,gridIndex,colIndexRemarks);

						String status=snapshotInventoryGridRecord.getStatus();
						model.setValueAt(status,gridIndex,colIndexStatus);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				snapshotInventoryTotalsPanel.updateTotalLabels();
				table.repaint();
			}
		});
	}

	public List<SnapshotInventoryGridRecord> getSnapshotInventoryGridRecordsList() {
		return snapshotInventoryGridRecordsList;
	}

	public List<SnapshotInventoryGridRecord> getSelectedSnapshotInventoryGridRecordsList()
	{
		TableModel model = table.getModel();
		int numRows = model.getRowCount();
		List<SnapshotInventoryGridRecord> toReturn=new ArrayList<SnapshotInventoryGridRecord>();
		int colIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		for (int i=0; i < numRows; i++) {
			boolean isSelected=(boolean) model.getValueAt(i,colIndex);
			if (isSelected) {
				SnapshotInventoryGridRecord snapshotInventoryGridRecord=snapshotInventoryGridRecordsList.get(i);
				toReturn.add(snapshotInventoryGridRecord);
			}
		}
		return toReturn;
	}

	public List<SnapshotInventoryGridRecord> getFilteredSnapshotInventoryGridRecordsList()
	{
		List<SnapshotInventoryGridRecord> toReturn=new ArrayList<SnapshotInventoryGridRecord>();
		for(int row = 0;row < table.getRowCount();row++) {
			int modelRowIndex=table.convertRowIndexToModel(row);
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=snapshotInventoryGridRecordsList.get(modelRowIndex);
			toReturn.add(snapshotInventoryGridRecord);
		}
		return toReturn;
	}

	public FilteringTable getFilteringTable() {
		return filteringTable;
	}

	public void viewChanges(int viewRow) {
		snapshotInventoryGridFrame.viewChanges(viewRow);
	}

	public JTable getTable() {
		return table;
	}

	public SnapshotInventoryTotalsPanel getSnapshotInventoryTotalsPanel() {
		return snapshotInventoryTotalsPanel;
	}

	public void setSelectionOnlyForModules(Set<String> selectedModules) {
		TableModel model = table.getModel();
		int numRows = model.getRowCount();
		int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		int colModuleIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_MODULE_NAME);
		for (int i=0; i < numRows; i++) {
			String applicationName=(String) model.getValueAt(i,colModuleIndex);
			Iterator<String> iterator=selectedModules.iterator();
			boolean hasMatch=false;
			while (iterator.hasNext()) {
				String selectedModule=iterator.next();
				if (applicationName.contains(selectedModule)) {
					model.setValueAt(true,i,colSelectionIndex);
					hasMatch=true; 
					break;
				}
			}
			if (!hasMatch) {
				model.setValueAt(false,i,colSelectionIndex);
			}
		}
	}

	public SnapshotInventoryGridFrame getSnapshotInventoryGridFrame() {
		return snapshotInventoryGridFrame;
	}
	
	public void setSelectionForInventories(Set<String> inventoriesToSelect) {
		TableModel model = table.getModel();
		int numRows = model.getRowCount();
		int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		int colInvIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_INVENTORY_NAME);
		for (int i=0; i < numRows; i++) {
			String inventoryName=(String) model.getValueAt(i,colInvIndex);
			if (inventoriesToSelect.contains(inventoryName)) {
				model.setValueAt(true,i,colSelectionIndex);
			}
			else{
				model.setValueAt(false,i,colSelectionIndex);
			}
		}
	}
	
	public void setSelectionAll() {
		TableModel model = table.getModel();
		int numRows = model.getRowCount();
		int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		for (int i=0; i < numRows; i++) {
			model.setValueAt(true,i,colSelectionIndex);
		}
	}

	public boolean isLoadAllInventorySuccessfully() {
		return isLoadAllInventorySuccessfully;
	}

}
