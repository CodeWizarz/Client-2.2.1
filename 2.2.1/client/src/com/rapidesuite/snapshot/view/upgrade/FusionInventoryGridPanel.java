package com.rapidesuite.snapshot.view.upgrade;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ButtonEditor;
import com.rapidesuite.snapshot.controller.upgrade.UpgradeEngine;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.FilteringTable;
import com.rapidesuite.snapshot.view.GridButtonRenderer;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.RolloverMouseAdapter;
import com.rapidesuite.snapshot.view.SnapshotFilteringTable;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class FusionInventoryGridPanel extends GenericInventoryGridPanel {

	protected List<FusionInventoryRow> fusionInventoryRowList;
	private Map<String,List<FusionInventoryRow>> inventoryNameToFusionInventoryRowsMap;
	private FusionInventoryGridSummaryPanel fusionInventoryGridSummaryPanel;
	
	public static String COLUMN_HEADING_SELECTION="Selection";
	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_NAVIGATION_NAME="Navigation name";
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	public static String COLUMN_HEADING_STATUS="Conversion\nStatus";
	public static String COLUMN_HEADING_TOTAL_RECORDS_COUNT="Total\nRecords";
	public static String COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT="Total Records\nConfiguration";
	public static String COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT="Total Records\nPost Configuration";
	public static String COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT="Total Records\nPost Implementation";
	public static String COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_OBSOLETE_COUNT="Total Records\nPost Impl Obsolete";
	public static String COLUMN_HEADING_VIEW="View Records";
	public static String COLUMN_HEADING_CONFIGURATION_VIEW="View Records\nConfiguration";
	public static String COLUMN_HEADING_POST_CONFIGURATION_VIEW="View Records\nPost Configuration";
	public static String COLUMN_HEADING_POST_IMPLEMENTATION_VIEW="View Records\nPost Implementation";
	public static String COLUMN_HEADING_POST_IMPLEMENTATION_OBSOLETE_VIEW="View Records\nPost Impl Obsolete";
	public static String COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS="Total Records\nMissing Required Data";
	public static String COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION="Total Records\nInvalid Values";
	public static String COLUMN_HEADING_REMARKS="Remarks";

	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_SELECTION_WIDTH=60;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_STATUS_WIDTH=70;
	private static int COLUMN_HEADING_NAVIGATION_NAME_WIDTH=250;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=250;
	private static int COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH=80;
	private static int COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT_WIDTH=140;
	private static int COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT_WIDTH=140;
	private static int COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT_WIDTH=140;
	private static int COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_OBSOLETE_COUNT_WIDTH=140;
	
	private static int COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS_WIDTH=160;
	private static int COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION_WIDTH=140;
	private static int COLUMN_HEADING_VIEW_WIDTH=140;
	private static int COLUMN_HEADING_CONFIGURATION_VIEW_WIDTH=140;
	private static int COLUMN_HEADING_POST_CONFIGURATION_VIEW_WIDTH=140;
	private static int COLUMN_HEADING_POST_IMPLEMENTATION_VIEW_WIDTH=140;
	private static int COLUMN_HEADING_REMARKS_WIDTH=180;
	
	public static String STATUS_VALID="Valid";
	public static String STATUS_INVALID="Invalid";
	
	public FusionInventoryGridPanel(TabUpgradeMainPanel tabUpgradeMainPanel) {
		super(tabUpgradeMainPanel);
		this.createComponents();
	}

	@SuppressWarnings("rawtypes")
	private void createComponents(){
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_SELECTION);
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_NAVIGATION_NAME);
		columnNames.add(COLUMN_HEADING_INVENTORY_NAME);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_OBSOLETE_COUNT);
		columnNames.add(COLUMN_HEADING_VIEW);
		columnNames.add(COLUMN_HEADING_CONFIGURATION_VIEW);
		columnNames.add(COLUMN_HEADING_POST_CONFIGURATION_VIEW);
		columnNames.add(COLUMN_HEADING_POST_IMPLEMENTATION_VIEW);
		columnNames.add(COLUMN_HEADING_POST_IMPLEMENTATION_OBSOLETE_VIEW);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION);
		columnNames.add(COLUMN_HEADING_REMARKS);
		
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
				if (column==colSelectionIndex) {
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
					String value="";
					int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
					if  (colIndex==colSelectionIndex) {
						int modelRow=table.convertRowIndexToModel(rowIndex);
						FusionInventoryRow fusionInventoryRow=fusionInventoryRowList.get(modelRow);
						
						Set<String> ebsInventoryNameSet=fusionInventoryRow.getFusionInventoryInformation().getEbsInventoryNameSet();
						if (ebsInventoryNameSet==null) {
							value="!!! NO EBS INVENTORIES MAPPED !!!";
						}
						else {
							value="EBS inventories: "+ebsInventoryNameSet;
						}
					}
					else {
						value=(String) getValueAt(rowIndex, colIndex);
						value=value.replaceAll("\n","<br>");
					}				
					tip="<html>"+value+"</html>";
				}
				catch (RuntimeException e1) {
					//catch null pointer exception if mouse is over an empty line
				}
				return tip;
			}

			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int vColIndex) {
				Component component =null;
				try{
					component = super.prepareRenderer(renderer, rowIndex, vColIndex);

					if (rowIndex%2 == 0){
						component.setBackground(Color.WHITE);
					}
					else {
						Color alternateColor =Color.decode("#dbdbdb");
						component.setBackground(alternateColor);
					}
					
					String status= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS));		
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

		UIUtils.setColumnSize(table,COLUMN_HEADING_SELECTION,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_SELECTION_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_ROW_NUM,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_ROW_NUM_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_NAVIGATION_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_NAVIGATION_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_INVENTORY_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_INVENTORY_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_STATUS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_STATUS_WIDTH,false);
		
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_OBSOLETE_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_OBSOLETE_COUNT_WIDTH,false);

		UIUtils.setColumnSize(table,COLUMN_HEADING_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_VIEW_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_CONFIGURATION_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_CONFIGURATION_VIEW_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_POST_CONFIGURATION_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_POST_CONFIGURATION_VIEW_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_POST_IMPLEMENTATION_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_POST_IMPLEMENTATION_VIEW_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_POST_IMPLEMENTATION_OBSOLETE_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_POST_IMPLEMENTATION_VIEW_WIDTH,false);

		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_REMARKS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_REMARKS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION_WIDTH,false);

		RolloverMouseAdapter rolloverAdapter = new RolloverMouseAdapter(table);
		table.addMouseListener(rolloverAdapter);
		table.addMouseMotionListener(rolloverAdapter);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_OBSOLETE_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_STATUS).setCellRenderer(centerRenderer);
		
		table.getColumn(COLUMN_HEADING_VIEW).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_CONFIGURATION_VIEW).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_POST_CONFIGURATION_VIEW).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_POST_IMPLEMENTATION_VIEW).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_POST_IMPLEMENTATION_OBSOLETE_VIEW).setCellRenderer(centerRenderer);
				
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_REMARKS).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_NAVIGATION_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_INVENTORY_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION).setCellRenderer(centerRenderer);
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				//System.out.println(new java.util.Date()+" : mouseClicked!!!");
				Point point = evt.getPoint();
				int row,column;
				row 	= table.rowAtPoint(point);
				column 	= table.columnAtPoint(point);
				try{
					Object value=table.getValueAt(row, column);
					if (value!=null) { 
						currentCellValueClicked=value.toString();
						StringSelection sel  = new StringSelection(currentCellValueClicked); 
						Clipboard systemClipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
						systemClipboard.setContents(sel, sel); 
					}
				}
				catch(Throwable e) {
					e.printStackTrace();
				}
				//System.out.println("row:"+row+" column:"+column);
				if (row >= 0 && column >= 0) {
					int colIndexActionTotalRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW);		
					int colIndexActionTotalRecordsConfiguration=table.getColumnModel().getColumnIndex(COLUMN_HEADING_CONFIGURATION_VIEW);	
					int colIndexActionTotalRecordsPostConfiguration=table.getColumnModel().getColumnIndex(COLUMN_HEADING_POST_CONFIGURATION_VIEW);	
					int colIndexActionTotalRecordsPostImplementation=table.getColumnModel().getColumnIndex(COLUMN_HEADING_POST_IMPLEMENTATION_VIEW);	
					int colIndexActionTotalRecordsPostImplementationObsolete=table.getColumnModel().getColumnIndex(COLUMN_HEADING_POST_IMPLEMENTATION_OBSOLETE_VIEW);	

					boolean isTotalRecords=false;
					boolean isTotalRecordsConfiguration=false;
					boolean isTotalRecordsPostConfiguration=false;
					boolean isTotalRecordsPostImplementation=false;
					boolean isTotalRecordsPostImplementationObsolete=false;
					boolean isViewColumn=false;
					
					int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
					if (column==colSelectionIndex) {
						selectSameNavigationRows(row);
						tabUpgradeMainPanel.selectEBSInventoriesRelatedToSelectedFusionInventories(true);
					}
					else
					if (column==colIndexActionTotalRecords) {
						isTotalRecords=true;
						isViewColumn=true;
					}
					else 
						if (column==colIndexActionTotalRecordsConfiguration) {
							isTotalRecordsConfiguration=true;
							isViewColumn=true;
						}
						else 
							if (column==colIndexActionTotalRecordsPostConfiguration) {
								isTotalRecordsPostConfiguration=true;
								isViewColumn=true;
							}
							else 
								if (column==colIndexActionTotalRecordsPostImplementation) {
									isTotalRecordsPostImplementation=true;
									isViewColumn=true;
								}
								else 
									if (column==colIndexActionTotalRecordsPostImplementationObsolete) {
										isTotalRecordsPostImplementationObsolete=true;
										isViewColumn=true;
									}
										
					if (isViewColumn) {
						viewConvertedData(row,isTotalRecords,isTotalRecordsConfiguration,isTotalRecordsPostConfiguration,
								isTotalRecordsPostImplementation,isTotalRecordsPostImplementationObsolete);
					}
				}
			}
		});

		setPopupMenu(table,COLUMN_HEADING_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
		table.setBackground(Color.decode("#DBDBDB"));
		table.setRowHeight(22);
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 80));
		
		setCellButton(COLUMN_HEADING_VIEW,rolloverAdapter);
		setCellButton(COLUMN_HEADING_CONFIGURATION_VIEW,rolloverAdapter);
		setCellButton(COLUMN_HEADING_POST_CONFIGURATION_VIEW,rolloverAdapter);
		setCellButton(COLUMN_HEADING_POST_IMPLEMENTATION_VIEW,rolloverAdapter);
		setCellButton(COLUMN_HEADING_POST_IMPLEMENTATION_OBSOLETE_VIEW,rolloverAdapter);
		
		JScrollPane variableScroll = new JScrollPane(table ,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};

		fusionInventoryGridSummaryPanel=new FusionInventoryGridSummaryPanel(this);
		filteringTable=new SnapshotFilteringTable(columnNames,variableScroll,table,33,fusionInventoryGridSummaryPanel,true,false);
		
		centerPanel.add(filteringTable.getFixedScrollPane(),BorderLayout.NORTH);
		centerPanel.add(variableScroll,BorderLayout.CENTER);

		table.addKeyListener(new  KeyAdapter()
		{
			@Override 
			public void keyReleased(KeyEvent event) { 
				if (currentCellValueClicked==null) {
					currentCellValueClicked="";
				}
				StringSelection sel  = new StringSelection(currentCellValueClicked); 
				Clipboard systemClipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
				systemClipboard.setContents(sel, sel);
			};
		});

		southPanel.add(fusionInventoryGridSummaryPanel);
	}
	
	protected void selectSameNavigationRows(int viewRow) {
		try{
			if (tabUpgradeMainPanel.isDisableNavigationNameSelection()) {
				return;
			}
			TableModel model = table.getModel();
			int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);

			int modelRow=table.convertRowIndexToModel(viewRow);
			boolean isSelected=(boolean)model.getValueAt(modelRow, colSelectionIndex);
			FusionInventoryRow fusionInventoryRow=fusionInventoryRowList.get(modelRow);
			Set<String> navigationNameSet=fusionInventoryRow.getFusionInventoryInformation().getNavigationNameSet();
			if (navigationNameSet.isEmpty() || (
					navigationNameSet.size()==1 && navigationNameSet.iterator().next().equalsIgnoreCase("all_inventories"))
			){
				return;
			}
			int numRows = model.getRowCount();
			for (int i=0; i < numRows; i++) {		
				FusionInventoryRow fusionInventoryRowTemp=fusionInventoryRowList.get(i);
				Set<String> navigationNameTempSet=fusionInventoryRowTemp.getFusionInventoryInformation().getNavigationNameSet();
				if (navigationNameTempSet.isEmpty()) {
					continue;
				}
				for (String navigationName:navigationNameSet) {
					if (navigationNameTempSet.contains(navigationName)) {
						int viewRowTemp=table.convertRowIndexToView(i);
						if (viewRowTemp==-1) {
							continue;
						}
						table.setValueAt(isSelected,viewRowTemp,colSelectionIndex);
						break;
					}
				}
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}

	private void setCellButton(String columnName,RolloverMouseAdapter rolloverAdapter) {
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
		table.getColumn(columnName).setCellRenderer(gridButtonRenderer);
		ButtonEditor buttonEditor=new ButtonEditor(new JCheckBox(),borderButton,width,height,null,true);
		button=buttonEditor.getButton();
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_view.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setIcon(ii);
		table.getColumn(columnName).setCellEditor(buttonEditor);
	}

	protected void viewConvertedData(int viewRow, boolean isTotalRecords,
			boolean isTotalRecordsConfiguration, boolean isTotalRecordsPostConfiguration, boolean isTotalRecordsPostImplementation, boolean isTotalRecordsPostImplementationObsolete) {
		try{
			int modelRow=table.convertRowIndexToModel(viewRow);
			FusionInventoryRow fusionInventoryRow=fusionInventoryRowList.get(modelRow);
			
			if (isTotalRecords && fusionInventoryRow.getTotalRecords()==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Records: 0");
				return;
			}
			if (isTotalRecordsConfiguration && fusionInventoryRow.getTotalRecordsConfiguration()==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Configuration Records: 0");
				return;
			}
			if (isTotalRecordsPostConfiguration && fusionInventoryRow.getTotalRecordsPostConfiguration()==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Post Configuration Records: 0");
				return;
			}
			if (isTotalRecordsPostImplementation && fusionInventoryRow.getTotalRecordsPostImplementation()==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Post Implementation Records: 0");
				return;
			}
			if (isTotalRecordsPostImplementationObsolete && fusionInventoryRow.getTotalRecordsPostImplementationObsolete()==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Post Impl Obsolete Records: 0");
				return;
			}
						
			JFrame rootFrame=tabUpgradeMainPanel.getUpgradeFrame();
			
			Inventory targetInventory=fusionInventoryRow.getInventory();
			File dataFolder=null;
			File convertedFolder=tabUpgradeMainPanel.getUpgradeController().getConvertedFolder();
			boolean isRecursive=false;
			if (isTotalRecords) {
				dataFolder=convertedFolder;
				isRecursive=true;
			}
			else 
				if (isTotalRecordsConfiguration) {
					dataFolder=new File(convertedFolder,UpgradeEngine.DATA_FOLDER_NAME_CONFIGURATION);
				}
				else 
					if (isTotalRecordsPostConfiguration) {
						dataFolder=new File(convertedFolder,UpgradeEngine.DATA_FOLDER_NAME_POST_CONFIGURATION);
					}
					else
						if (isTotalRecordsPostImplementation) {
							File subFolderPostImplementation=new File(convertedFolder,UpgradeEngine.DATA_FOLDER_NAME_POST_IMPLEMENTATION);
							dataFolder=new File(subFolderPostImplementation,UpgradeEngine.DATA_FOLDER_NAME_SUB_POST_IMPLEMENTATION);
						}
						else
							if (isTotalRecordsPostImplementationObsolete) {
								File subFolderPostImplementation=new File(convertedFolder,UpgradeEngine.DATA_FOLDER_NAME_POST_IMPLEMENTATION);
								dataFolder=new File(subFolderPostImplementation,UpgradeEngine.DATA_FOLDER_NAME_SUB_OBSOLETE_POST_IMPLEMENTATION);
							}
						else throw new Exception("Internal error, invalid output folder, cannot find data files.");

			List<File> fileList=ModelUtils.getFileNameList(dataFolder,targetInventory.getName(),isRecursive,true);
			List<String[]> finalList=new ArrayList<String[]>();
			for (File file:fileList) {
				List<String[]> dataRows=InjectUtils.parseXMLDataFile(file);
				finalList.addAll(dataRows);
			}
			List<DataRow> dataRows=ModelUtils.convert(targetInventory,finalList);
			 
			UpgradeDataGrid upgradeDataGrid=new UpgradeDataGrid(rootFrame,targetInventory,dataRows,false,
					tabUpgradeMainPanel.getUpgradeController().getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap());
			upgradeDataGrid.pack();
			upgradeDataGrid.setVisible(true);
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}

	public void displayFusionInventories(List<FusionInventoryRow> fusionInventoryRowList) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		filteringTable.removeRowFilter();
		tableModel.setRowCount(0);

		inventoryNameToFusionInventoryRowsMap=new HashMap<String,List<FusionInventoryRow>>();
		this.fusionInventoryRowList=fusionInventoryRowList;
		for (FusionInventoryRow fusionInventoryRow:fusionInventoryRowList) {
			Vector<Object> rowGrid = new Vector<Object>();

			List<FusionInventoryRow> fusionInventoryRowListTemp=inventoryNameToFusionInventoryRowsMap.get(fusionInventoryRow.getInventoryName());
			if (fusionInventoryRowListTemp==null) {
				fusionInventoryRowListTemp=new ArrayList<FusionInventoryRow>();
				inventoryNameToFusionInventoryRowsMap.put(fusionInventoryRow.getInventoryName(), fusionInventoryRowListTemp);
			}
			fusionInventoryRowListTemp.add(fusionInventoryRow);
			
			rowGrid.add(false);
			String formattedRowNumber="";
			try{
				int visibleRowSequence=fusionInventoryRow.getGridIndex()+1;
				formattedRowNumber=Utils.formatNumberWithComma(visibleRowSequence);
			}
			catch(Exception e) {
			}
			rowGrid.add(formattedRowNumber);
			rowGrid.add(fusionInventoryRow.getFusionInventoryInformation().getConcatenatedNavigationNames());
			rowGrid.add(fusionInventoryRow.getInventoryName());
			rowGrid.add("");
			rowGrid.add("0");
			rowGrid.add("0");
			rowGrid.add("0");
			rowGrid.add("0");
			rowGrid.add("0");
			
			rowGrid.add("View");
			rowGrid.add("View");
			rowGrid.add("View");
			rowGrid.add("View");
			rowGrid.add("View");
			
			rowGrid.add("0");
			rowGrid.add("0");
			
			rowGrid.add(fusionInventoryRow.getRemarks());

			tableModel.addRow(rowGrid);
		}
		fusionInventoryGridSummaryPanel.updateTotalLabels();
		table.changeSelection(0, 0, false, false);
		filteringTable.applyFiltering();
	}	
	
	public void refreshGrid(final List<FusionInventoryRow> fusionInventoryRowList) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TableModel model = table.getModel();
				for (FusionInventoryRow fusionInventoryRow:fusionInventoryRowList) {
					int gridIndex=fusionInventoryRow.getGridIndex();
					int colIndexTotalRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
					int colIndexTotalRecordsConfiguration=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT);
					int colIndexTotalRecordsPostConfiguration=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT);
					int colIndexTotalRecordsPostImplementation=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT);
					int colIndexTotalRecordsPostImplementationObsolete=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_OBSOLETE_COUNT);					
					int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
					int colIndexTotalRecordsPartialRequiredColumns=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS);
					int colIndexTotalRecordsInvalidValues=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION);
					int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);

					try {
						String status=fusionInventoryRow.getStatus();
						model.setValueAt(status,gridIndex,colIndexStatus);
						
						int totalRecords=fusionInventoryRow.getTotalRecords();
						model.setValueAt(Utils.formatNumberWithComma(totalRecords),gridIndex,colIndexTotalRecords);
						int totalRecordsConfiguration=fusionInventoryRow.getTotalRecordsConfiguration();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsConfiguration),gridIndex,colIndexTotalRecordsConfiguration);
						int totalRecordsPostConfiguration=fusionInventoryRow.getTotalRecordsPostConfiguration();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsPostConfiguration),gridIndex,colIndexTotalRecordsPostConfiguration);
						int totalRecordsPostImplementation=fusionInventoryRow.getTotalRecordsPostImplementation();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsPostImplementation),gridIndex,colIndexTotalRecordsPostImplementation);
						int totalRecordsPostImplementationObsolete=fusionInventoryRow.getTotalRecordsPostImplementationObsolete();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsPostImplementationObsolete),gridIndex,colIndexTotalRecordsPostImplementationObsolete);
						
						int totalRecordsMissingRequiredColumns=fusionInventoryRow.getTotalRecordsMissingRequiredColumns();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsMissingRequiredColumns),gridIndex,colIndexTotalRecordsPartialRequiredColumns);
						
						int totalRecordsInvalidValues=fusionInventoryRow.getTotalRecordsInvalidValues();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsInvalidValues),gridIndex,colIndexTotalRecordsInvalidValues);

						String remarks=fusionInventoryRow.getRemarks();
						model.setValueAt(remarks,gridIndex,colIndexRemarks);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				table.repaint();
				fusionInventoryGridSummaryPanel.updateTotalLabels();
			}
		});
	}

	public List<FusionInventoryRow> getFusionInventoryRowList() {
		return fusionInventoryRowList;
	}
	
	private JPopupMenu setPopupMenu(final JTable table,final String selectionColumnName) {
		final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Select");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(table,selectionColumnName,true,false);
            }
        });
        popupMenu.add(item);
        
        item = new JMenuItem("Select All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(table,selectionColumnName,true,true);
            }
        });
        popupMenu.add(item);
        
        item= new JMenuItem("Deselect");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(table,selectionColumnName,false,false);
            }
        });
        popupMenu.add(item);
        
        item= new JMenuItem("Deselect All");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	setSelectionOnRows(table,selectionColumnName,false,true);
            }
        });
        popupMenu.add(item);
		table.setComponentPopupMenu(popupMenu);
		return popupMenu;
	}	
	
	private void setSelectionOnRows(final JTable table,String selectionColumnName,final boolean isSelected,final boolean isAllRows) {
		final int colIndexSelection=table.getColumnModel().getColumnIndex(selectionColumnName);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int[] rows=null;
				if (isAllRows) {
					table.setRowSelectionInterval(0, table.getRowCount() - 1);
				}
				rows=table.getSelectedRows();
				//List<Integer> rowIndexesTableGrid=new ArrayList<Integer>();
				for(int i=0;i<rows.length;i++){					
					table.setValueAt(isSelected,rows[i],colIndexSelection);
					//int modelIndex=table.convertRowIndexToModel(rows[i]);
					//rowIndexesTableGrid.add(modelIndex);
				}
				table.repaint();
				tabUpgradeMainPanel.selectEBSInventoriesRelatedToSelectedFusionInventories(true);
			}
		});
	}
	
	public List<FusionInventoryRow> getSelectedFusionInventoryRowList()
	{
		TableModel model = table.getModel();
		int numRows = model.getRowCount();
		int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		List<FusionInventoryRow> toReturn=new ArrayList<FusionInventoryRow>();
		for (int i=0; i < numRows; i++) {		
			boolean isSelected=(boolean)model.getValueAt(i, colSelectionIndex);
			if (isSelected) {
				FusionInventoryRow fusionInventoryRow=fusionInventoryRowList.get(i);
				toReturn.add(fusionInventoryRow);
			}
		}
		return toReturn;
	}
	
	public boolean isFusionInventorySelected(int gridIndex)
	{
		TableModel model = table.getModel();
		int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		return (boolean)model.getValueAt(gridIndex, colSelectionIndex);
	}

	public List<FusionInventoryRow> getFilteredConvertTargetGridRecordInformationList()
	{
		List<FusionInventoryRow> toReturn=new ArrayList<FusionInventoryRow>();
		for(int row = 0;row < table.getRowCount();row++) {
			int modelRowIndex=table.convertRowIndexToModel(row);
			FusionInventoryRow fusionInventoryRow=fusionInventoryRowList.get(modelRowIndex);
			toReturn.add(fusionInventoryRow);
		}
		return toReturn;
	}

	public FilteringTable getFilteringTable() {
		return filteringTable;
	}

	public JTable getTable() {
		return table;
	}

	public Map<String, List<FusionInventoryRow>> getInventoryNameToFusionInventoryRowsMap() {
		return inventoryNameToFusionInventoryRowsMap;
	}

}