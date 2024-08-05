package com.rapidesuite.inject.gui;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.fusionData0000.FusionDataType;
import com.rapidesuite.core.fusionData0000.InventoryMapType;
import com.rapidesuite.core.fusionData0000.LType;
import com.rapidesuite.core.fusionData0000.MType;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InventoryPC;
import com.rapidesuite.inject.RecordTracker;
import com.rapidesuite.inject.ScriptGridTracker;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.MultiLineHeaderRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@SuppressWarnings("serial")
public class ScriptsGrid extends AbstractGrid {

	//	public static String COLUMN_HEADING_SELECTION="Selection"; //in base class AbstractGrid now
	public static String COLUMN_HEADING_ROW_NUM="Script #";
	public static String COLUMN_HEADING_NAME="Name";
	public static String COLUMN_HEADING_TYPE="Type";
	public static String COLUMN_HEADING_STATUS="Status";
	public static String COLUMN_HEADING_PERCENTAGE="% Success";
	public static String COLUMN_HEADING_TOTAL_RECORDS="Total";
	public static String COLUMN_HEADING_REMAINING_RECORDS="Remaining";
	public static String COLUMN_HEADING_SUCCESS_RECORDS="Success";
	public static String COLUMN_HEADING_FAILED_RECORDS="Failed";
	public static String COLUMN_HEADING_OPEN_GRID="Data";
	public static String COLUMN_HEADING_SCRIPT_BATCH_SIZE="Batch\nsize";
	public static String COLUMN_HEADING_RETRIES="Retries";
	public static String COLUMN_HEADING_TOTAL_BATCH_COUNT="Total\n# Batch";
	public static String COLUMN_HEADING_COMPLETED_BATCH_COUNT="Completed\n# Batch";
	public static String COLUMN_HEADING_REMARKS="Remarks";
	public static String COLUMN_HEADING_TOTAL_EXECUTION_TIME="Execution Time";
	public static String COLUMN_HEADING_START_EXECUTION_TIME="Start Time";
	public static String COLUMN_HEADING_END_EXECUTION_TIME="End Time";

	private int COLUMN_HEADING_SELECTION_WIDTH=60;
	private int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private int COLUMN_HEADING_NAME_WIDTH=280;
	private int COLUMN_HEADING_TYPE_WIDTH=40;
	private int COLUMN_HEADING_STATUS_WIDTH=70;
	private int COLUMN_HEADING_PERCENTAGE_WIDTH=90;
	private int COLUMN_HEADING_TOTAL_RECORDS_WIDTH=60;
	private int COLUMN_HEADING_REMAINING_RECORDS_WIDTH=80;
	private int COLUMN_HEADING_SUCCESS_RECORDS_WIDTH=60;
	private int COLUMN_HEADING_FAILED_RECORDS_WIDTH=60;
	private int COLUMN_HEADING_OPEN_GRID_WIDTH=80;
	private int COLUMN_HEADING_RETRIES_WIDTH=70;
	private int COLUMN_HEADING_SCRIPT_BATCH_SIZE_WIDTH=60;
	private int COLUMN_HEADING_TOTAL_BATCH_COUNT_WIDTH=60;
	private int COLUMN_HEADING_COMPLETED_BATCH_COUNT_WIDTH=80;
	private int COLUMN_HEADING_REMARKS_WIDTH=150;
	private int COLUMN_HEADING_TOTAL_EXECUTION_TIME_WIDTH=120;
	private int COLUMN_HEADING_START_EXECUTION_TIME_WIDTH=150;
	private int COLUMN_HEADING_END_EXECUTION_TIME_WIDTH=150;

	//private final JTable table;
	private Map<Integer,Map<String,TableGrid>> scriptIdToTableGridMap;
	private Map<Integer,List<InventoryPC>> scriptIdToRootInventoryPCsMap;
	private Map<Integer,TableGridsTabbed> tableGridsTabbedMap;

	private final Object lock = new Object();
	private boolean isExecutionStarted;
	private ScriptGridTracker openedScriptGridTracker;
	private ExecutionTabPanel executionTabPanel;
	private List<JProgressBar> bars;

	@SuppressWarnings("rawtypes")
	public ScriptsGrid(final ExecutionTabPanel executionTabPanel) throws Exception {
		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		scriptIdToTableGridMap=new HashMap<Integer,Map<String,TableGrid>>();
		this.executionTabPanel=executionTabPanel;
		tableGridsTabbedMap=new HashMap<Integer,TableGridsTabbed>();

		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_SELECTION);
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_NAME);
		columnNames.add(COLUMN_HEADING_TYPE);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_PERCENTAGE);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS);
		columnNames.add(COLUMN_HEADING_REMAINING_RECORDS);
		columnNames.add(COLUMN_HEADING_SUCCESS_RECORDS);
		columnNames.add(COLUMN_HEADING_FAILED_RECORDS);
		columnNames.add(COLUMN_HEADING_OPEN_GRID);
		columnNames.add(COLUMN_HEADING_SCRIPT_BATCH_SIZE);
		columnNames.add(COLUMN_HEADING_RETRIES);
		columnNames.add(COLUMN_HEADING_TOTAL_BATCH_COUNT);
		columnNames.add(COLUMN_HEADING_COMPLETED_BATCH_COUNT);
		columnNames.add(COLUMN_HEADING_REMARKS);
		columnNames.add(COLUMN_HEADING_TOTAL_EXECUTION_TIME);
		columnNames.add(COLUMN_HEADING_START_EXECUTION_TIME);
		columnNames.add(COLUMN_HEADING_END_EXECUTION_TIME);

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked" })
			@Override
			public Class getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				switch (column) {
				case 0:
					return !isExecutionStarted;
				case 10:
					return true;
				case 11:
					try {
						ScriptGridTracker scriptGridTracker = executionTabPanel.getScriptGridTracker(row);
						//boolean isBatchingBasedOnRootInventoryUniqueRecords=scriptGridTracker.getNavigation().getFusionNavigation().getIsBatchingBasedOnRootInventoryUniqueRecords();
						boolean isBatchingAllowed=scriptGridTracker.getNavigation().getFusionNavigation().getIsBatchingAllowed();
						if (!isBatchingAllowed) {
							return false;
						}
					} 
					catch (Exception e1) {
						FileUtils.printStackTrace(e1);
					}
					return !isExecutionStarted;
				default:
					return false;
				}
			}

			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int colIndex) {
				Component component = super.prepareRenderer(renderer, rowIndex, colIndex);

				String status= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS));	
				if (status==null) {
					return component;
				}
				if (status.equalsIgnoreCase(STATUS_PROCESSING)) {
					int colIndexFailedRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_FAILED_RECORDS);
					String failedCount= (String) getValueAt(rowIndex, colIndexFailedRecords);
					if (colIndex==colIndexFailedRecords && !failedCount.equals("0")) {
						component.setBackground(redColor);
					}
					else {
						component.setBackground(yellowColor);
					}
				}
				else
					if (status.equalsIgnoreCase(STATUS_QUEUED)) {
						component.setBackground(Color.decode("#F6F6F6"));
					}
					else 
						if (status.equalsIgnoreCase(STATUS_RETRY)) {
							component.setBackground(yellowColor);
						}
						else 
							if (status.equalsIgnoreCase(STATUS_FAILED) ) {
								component.setBackground(redColor);
							}
							else 
								if (status.equalsIgnoreCase(STATUS_STOPPED) ) {
									component.setBackground(redColor);
								}
								else 
									if (status.equalsIgnoreCase(STATUS_PENDING)) {
										component.setBackground(Color.decode("#F6F6F6"));
									}
									else 
										if (status.equalsIgnoreCase(STATUS_SUCCESS)) {
											component.setBackground(greenColor);
										}

				if (isCellSelected(rowIndex, colIndex)) {
					component.setBackground(blueColor);
				} 
				return component;
			}

			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				int colScriptNameIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_NAME);
				if  (colIndex==colScriptNameIndex) {
					if (rowIndex>=0) {
						ScriptGridTracker scriptGridTracker;
						try {
							scriptGridTracker = executionTabPanel.getScriptGridTracker(rowIndex);
							boolean isBatchingBasedOnRootInventoryUniqueRecords=scriptGridTracker.getNavigation().getFusionNavigation().getIsBatchingBasedOnRootInventoryUniqueRecords();
							String tooltip="<html>Navigation name: "+scriptGridTracker.getScript().getName()+"<br/>";
							if (isBatchingBasedOnRootInventoryUniqueRecords) {
								tooltip+="Batching based on Root Inventory Header records.<br/>";
							}
							boolean isBatchingAllowed=scriptGridTracker.getNavigation().getFusionNavigation().getIsBatchingAllowed();
							if (!isBatchingAllowed) {
								tooltip+="Batching not allowed (*).<br/>";
							}
							tooltip+="</html>";
							return tooltip;
						} 
						catch (Exception e1) {
							FileUtils.printStackTrace(e1);
						}
					}
				}
				return null;
			}

		};

		TableColumn columnSelection = table.getColumn(COLUMN_HEADING_SELECTION);
		//columnSelection.setMaxWidth(COLUMN_HEADING_SELECTION_WIDTH);
		columnSelection.setMinWidth(COLUMN_HEADING_SELECTION_WIDTH);
		columnSelection.setPreferredWidth(COLUMN_HEADING_SELECTION_WIDTH);
		columnSelection.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnRowNum = table.getColumn(COLUMN_HEADING_ROW_NUM);
		//columnRowNum.setMaxWidth(COLUMN_HEADING_ROW_NUM_WIDTH);
		columnRowNum.setMinWidth(COLUMN_HEADING_ROW_NUM_WIDTH);
		columnRowNum.setPreferredWidth(COLUMN_HEADING_ROW_NUM_WIDTH);
		columnRowNum.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnHeadingName = table.getColumn(COLUMN_HEADING_NAME);
		columnHeadingName.setMinWidth(COLUMN_HEADING_NAME_WIDTH);
		columnHeadingName.setPreferredWidth(COLUMN_HEADING_NAME_WIDTH);
		columnHeadingName.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnHeadingType = table.getColumn(COLUMN_HEADING_TYPE);
		columnHeadingType.setMinWidth(COLUMN_HEADING_TYPE_WIDTH);
		columnHeadingType.setPreferredWidth(COLUMN_HEADING_TYPE_WIDTH);
		columnHeadingType.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnStatus = table.getColumn(COLUMN_HEADING_STATUS);
		//columnStatus.setMaxWidth(COLUMN_HEADING_STATUS_WIDTH);
		columnStatus.setMinWidth(COLUMN_HEADING_STATUS_WIDTH);
		columnStatus.setPreferredWidth(COLUMN_HEADING_STATUS_WIDTH);
		columnStatus.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnPercentage = table.getColumn(COLUMN_HEADING_PERCENTAGE);
		columnPercentage.setMinWidth(COLUMN_HEADING_PERCENTAGE_WIDTH);
		columnPercentage.setPreferredWidth(COLUMN_HEADING_PERCENTAGE_WIDTH);
		columnPercentage.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnTotalRecords = table.getColumn(COLUMN_HEADING_TOTAL_RECORDS);
		//columnTotalRecords.setMaxWidth(COLUMN_HEADING_TOTAL_RECORDS_WIDTH);
		columnTotalRecords.setMinWidth(COLUMN_HEADING_TOTAL_RECORDS_WIDTH);
		columnTotalRecords.setPreferredWidth(COLUMN_HEADING_TOTAL_RECORDS_WIDTH);
		columnTotalRecords.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnTotalToProcess = table.getColumn(COLUMN_HEADING_REMAINING_RECORDS);
		//columnTotalToProcess.setMaxWidth(COLUMN_HEADING_REMAINING_RECORDS_WIDTH);
		columnTotalToProcess.setMinWidth(COLUMN_HEADING_REMAINING_RECORDS_WIDTH);
		columnTotalToProcess.setPreferredWidth(COLUMN_HEADING_REMAINING_RECORDS_WIDTH);
		columnTotalToProcess.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnTotalSuccess = table.getColumn(COLUMN_HEADING_SUCCESS_RECORDS);
		//columnTotalSuccess.setMaxWidth(COLUMN_HEADING_SUCCESS_RECORDS_WIDTH);
		columnTotalSuccess.setMinWidth(COLUMN_HEADING_SUCCESS_RECORDS_WIDTH);
		columnTotalSuccess.setPreferredWidth(COLUMN_HEADING_SUCCESS_RECORDS_WIDTH);
		columnTotalSuccess.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnTotalFailed = table.getColumn(COLUMN_HEADING_FAILED_RECORDS);
		//columnTotalFailed.setMaxWidth(COLUMN_HEADING_FAILED_RECORDS_WIDTH);
		columnTotalFailed.setMinWidth(COLUMN_HEADING_FAILED_RECORDS_WIDTH);
		columnTotalFailed.setPreferredWidth(COLUMN_HEADING_FAILED_RECORDS_WIDTH);
		columnTotalFailed.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnOpenGrid = table.getColumn(COLUMN_HEADING_OPEN_GRID);
		//columnOpenGrid.setMaxWidth(COLUMN_HEADING_OPEN_GRID_WIDTH);
		columnOpenGrid.setMinWidth(COLUMN_HEADING_OPEN_GRID_WIDTH);
		columnOpenGrid.setPreferredWidth(COLUMN_HEADING_OPEN_GRID_WIDTH);
		columnOpenGrid.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnScriptBatchSize = table.getColumn(COLUMN_HEADING_SCRIPT_BATCH_SIZE);
		columnScriptBatchSize.setMinWidth(COLUMN_HEADING_SCRIPT_BATCH_SIZE_WIDTH);
		columnScriptBatchSize.setPreferredWidth(COLUMN_HEADING_SCRIPT_BATCH_SIZE_WIDTH);
		columnScriptBatchSize.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnExecutionRetries= table.getColumn(COLUMN_HEADING_RETRIES);
		columnExecutionRetries.setMinWidth(COLUMN_HEADING_RETRIES_WIDTH);
		columnExecutionRetries.setPreferredWidth(COLUMN_HEADING_RETRIES_WIDTH);
		columnExecutionRetries.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnTotalBatchCount = table.getColumn(COLUMN_HEADING_TOTAL_BATCH_COUNT);
		//columnTotalBatchCount.setMaxWidth(COLUMN_HEADING_TOTAL_BATCH_COUNT_WIDTH);
		columnTotalBatchCount.setMinWidth(COLUMN_HEADING_TOTAL_BATCH_COUNT_WIDTH);
		columnTotalBatchCount.setPreferredWidth(COLUMN_HEADING_TOTAL_BATCH_COUNT_WIDTH);
		columnTotalBatchCount.setHeaderRenderer(new TableHeaderRenderer());		

		TableColumn columnCompletedBatchCount = table.getColumn(COLUMN_HEADING_COMPLETED_BATCH_COUNT);
		//columnCompletedBatchCount.setMaxWidth(COLUMN_HEADING_COMPLETED_BATCH_COUNT_WIDTH);
		columnCompletedBatchCount.setMinWidth(COLUMN_HEADING_COMPLETED_BATCH_COUNT_WIDTH);
		columnCompletedBatchCount.setPreferredWidth(COLUMN_HEADING_COMPLETED_BATCH_COUNT_WIDTH);
		columnCompletedBatchCount.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnRemarks = table.getColumn(COLUMN_HEADING_REMARKS);
		columnRemarks.setMinWidth(COLUMN_HEADING_REMARKS_WIDTH);
		columnRemarks.setPreferredWidth(COLUMN_HEADING_REMARKS_WIDTH);			
		columnRemarks.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnExecutionTime = table.getColumn(COLUMN_HEADING_TOTAL_EXECUTION_TIME);
		columnExecutionTime.setMinWidth(COLUMN_HEADING_TOTAL_EXECUTION_TIME_WIDTH);
		columnExecutionTime.setPreferredWidth(COLUMN_HEADING_TOTAL_EXECUTION_TIME_WIDTH);	
		columnExecutionTime.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnExecutionStartTime = table.getColumn(COLUMN_HEADING_START_EXECUTION_TIME);
		columnExecutionStartTime.setMinWidth(COLUMN_HEADING_START_EXECUTION_TIME_WIDTH);
		columnExecutionStartTime.setPreferredWidth(COLUMN_HEADING_START_EXECUTION_TIME_WIDTH);	
		columnExecutionStartTime.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnExecutionEndTime = table.getColumn(COLUMN_HEADING_END_EXECUTION_TIME);
		columnExecutionEndTime.setMinWidth(COLUMN_HEADING_END_EXECUTION_TIME_WIDTH);
		columnExecutionEndTime.setPreferredWidth(COLUMN_HEADING_END_EXECUTION_TIME_WIDTH);	
		columnExecutionEndTime.setHeaderRenderer(new TableHeaderRenderer());

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		columnHeadingName.setCellRenderer(leftRenderer);
		columnHeadingType.setCellRenderer(centerRenderer);
		columnRowNum.setCellRenderer(centerRenderer);
		columnStatus.setCellRenderer(centerRenderer);		

		columnPercentage.setCellRenderer(new TableCellRenderer() { //sets a progress bar as renderer
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JProgressBar bar = bars.get(row);
				try {
					ScriptGridTracker scriptGridTracker=executionTabPanel.getScriptGridTracker(row);
					int totalRecordsSuccess=scriptGridTracker.getTotalSuccessRecords();
					bar.setValue(totalRecordsSuccess);
					bar.setForeground(Color.white);
					scriptGridTracker.setPercentageComplete(bar.getPercentComplete());
					String status= scriptGridTracker.getStatus();
					if (status.equalsIgnoreCase(STATUS_PROCESSING)) {
						bar.setBackground(yellowColor);
						bar.setForeground(new Color(102,226,253));
					}
					else
						if (status.equalsIgnoreCase(STATUS_FAILED)|| status.equalsIgnoreCase(STATUS_STOPPED) ) {
							bar.setBackground(redColor);
							bar.setForeground(redColor);
						}
						else 
							if (status.equalsIgnoreCase(STATUS_SUCCESS)) {
								if (scriptGridTracker.getTotalRecords()==0) {
									bar.setValue(1);
								}
								bar.setBackground(greenColor);
								bar.setForeground(greenColor);
							}

					if (isSelected) {
						Color backgroundColor=bar.getBackground();
						bar.setBackground(backgroundColor.brighter());
						Color foregroundColor=bar.getForeground();
						bar.setForeground(foregroundColor.brighter());
					} 
				} 
				catch (Exception e) {
					FileUtils.printStackTrace(e);
				}
				return bar;
			}
		});
		columnTotalRecords.setCellRenderer(centerRenderer);
		columnTotalToProcess.setCellRenderer(centerRenderer);
		columnTotalSuccess.setCellRenderer(centerRenderer);
		columnTotalFailed.setCellRenderer(centerRenderer);
		columnScriptBatchSize.setCellRenderer(centerRenderer);
		columnExecutionRetries.setCellRenderer(centerRenderer);
		columnTotalBatchCount.setCellRenderer(centerRenderer);
		columnCompletedBatchCount.setCellRenderer(centerRenderer);
		columnRemarks.setCellRenderer(leftRenderer);
		columnExecutionTime.setCellRenderer(centerRenderer);
		columnExecutionStartTime.setCellRenderer(centerRenderer);
		columnExecutionEndTime.setCellRenderer(centerRenderer);

		Border borderButton=BorderFactory.createEmptyBorder(2, 10, 0, 0);
		int width=64;
		int height=17;
		ButtonRenderer buttonRenderer=new ButtonRenderer(borderButton,width,height);

		table.getColumn(COLUMN_HEADING_OPEN_GRID).setCellRenderer(buttonRenderer);
		table.getColumn(COLUMN_HEADING_OPEN_GRID).setCellEditor(new ButtonEditor(new JCheckBox(),borderButton,width,height,null));
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setForeground(Color.BLACK);
		table.setBackground(Color.decode("#DBDBDB"));
		table.setRowHeight(22);
		table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 37));

		MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer();
		Enumeration enumeration = table.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			((TableColumn) enumeration.nextElement()).setHeaderRenderer(renderer);
		}

		table.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent evt) {
				Point point = evt.getPoint();
				int row,column;
				row 	= table.rowAtPoint(point);
				column 	= table.columnAtPoint(point);
				if (row >= 0 && column >= 0) {
					//System.out.println("Cell Value at Row:"+row+" | Column:"+column+" is " + table.getValueAt(row,column));

					int colIndexOpenGrid=table.getColumnModel().getColumnIndex(COLUMN_HEADING_OPEN_GRID);
					if (column==colIndexOpenGrid) {
						try {
							openTableGrids(executionTabPanel.getScriptGridTracker(row));
						} catch (Exception e) {
							FileUtils.printStackTrace(e);
							GUIUtils.popupErrorMessage(e.getMessage());
						}
					}
					int colIndexSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
					if(column==colIndexSelection) {
						boolean selectionStatus = false;
						if(ScriptsGrid.this.isScriptSelected(row)) {
							selectionStatus = true;
						}
						ScriptsGrid.setSelectionOnRows(ScriptsGrid.this,selectionStatus,false);
					}
				}
			}

		});

		setPopupMenu(this);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scrollPane);
	}

	public void setIsExecutionStarted(boolean isExecutionStarted) {
		this.isExecutionStarted=isExecutionStarted;
		setTableGridIsExecutionStarted(isExecutionStarted);
	}

	public boolean isExecutionStarted() {
		return isExecutionStarted;
	}

	public Map<Integer,Map<String,TableGrid>> getScriptIdToTableGridMap() {
		return scriptIdToTableGridMap;
	}

	public void loadScripts(File injectionPackage,List<ScriptGridTracker> filteredScriptGridTrackerList) throws Exception {
		closeTableGrids();
		scriptIdToTableGridMap=new HashMap<Integer,Map<String,TableGrid>>();
		scriptIdToRootInventoryPCsMap=new HashMap<Integer,List<InventoryPC>>();
		tableGridsTabbedMap=new HashMap<Integer,TableGridsTabbed>();
		bars = new ArrayList<JProgressBar>(); 
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);
		int rowIndex=0;
		for (ScriptGridTracker scriptGridTracker:filteredScriptGridTrackerList) {
			Vector<Object> row = new Vector<Object>();
			rowIndex++;

			row.add(true);
			row.add(Utils.formatNumberWithComma(rowIndex));


			row.add(scriptGridTracker.getDisplayName());
			row.add(scriptGridTracker.getDisplayType());
			row.add("Pending");
			row.add("");

			int totalRecords=InjectUtils.getTotalDataRows(executionTabPanel.getExecutionPanel().getInjectMain(),injectionPackage,scriptGridTracker.getScript());
			int progressBarMax=totalRecords;
			if (totalRecords==0) {
				progressBarMax=1;
			}
			JProgressBar progressBar=new JProgressBar(0, progressBarMax);
			progressBar.setStringPainted(true);
			progressBar.setOpaque(true);
			progressBar.setForeground(Color.white);
			progressBar.setValue(0);
			progressBar.setBorderPainted(false);
			progressBar.setUI( new BasicProgressBarUI() {
				protected Color getSelectionBackground() { return Color.black; } // string painted color 5%
				protected Color getSelectionForeground() { return Color.black; }
			});

			bars.add(progressBar);

			row.add(Utils.formatNumberWithComma(totalRecords));
			row.add(Utils.formatNumberWithComma(totalRecords));
			row.add("0");
			row.add("0");
			row.add("View");

			int val=scriptGridTracker.getBatchSize();
			String valStr="";
			if (val>=0) {
				valStr=""+val;
			}
			row.add(valStr);
			row.add("");
			row.add("");
			row.add("");
			row.add("");
			row.add("");
			row.add("");
			row.add("");
			model.addRow(row);
		}
		table.repaint(); 
	}

	public List<ScriptGridTracker> getSelectedScriptTackers() throws Exception
	{
		int numRows = table.getRowCount();
		if (table.isEditing()) {
			table.getCellEditor().cancelCellEditing();
		}

		TableModel model = table.getModel();
		List<ScriptGridTracker> toReturn=new ArrayList<ScriptGridTracker>();
		int colIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		int colIndexBatchSize=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SCRIPT_BATCH_SIZE);
		for (int i=0; i < numRows; i++) {
			boolean isSelected=(boolean) model.getValueAt(i,colIndex);
			if (isSelected) {
				ScriptGridTracker scriptGridTracker=executionTabPanel.getScriptGridTracker(i);
				String batchSizeStr=(String) model.getValueAt(i,colIndexBatchSize);

				if (batchSizeStr!=null) {
					if (batchSizeStr.trim().isEmpty()) {
						scriptGridTracker.setBatchSize(0);
					}
					else {
						try{
							int batchSize=Integer.valueOf(batchSizeStr);
							scriptGridTracker.setBatchSize(batchSize);
						}
						catch(NumberFormatException e) {
							FileUtils.printStackTrace(e);
						}
					}
				}
				toReturn.add(scriptGridTracker);
			}
		}
		return toReturn;
	}

	private void openTableGrids(final ScriptGridTracker scriptGridTracker) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					closeTableGrids();
					openedScriptGridTracker=scriptGridTracker;
					List<Inventory> rootInventories=InjectUtils.getRootInventories(
							executionTabPanel.getExecutionPanel().getInjectMain(),executionTabPanel.getExecutionPanel().getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());

					List<TableGrid> tableGrids=new ArrayList<TableGrid>();
					for (Inventory rootInventory:rootInventories) {
						TableGrid rootTableGrid=getTableGrid(scriptGridTracker,rootInventory.getName());
						List<TableGrid> tableGridTempList=loadAndOpenChildGridTables(scriptGridTracker,rootInventory.getName());
						tableGrids.addAll(tableGridTempList);
						tableGrids.add(0, rootTableGrid);
					}
					updateTableGrids(scriptGridTracker,false);

					TableGridsTabbed tableGridsTabbed=tableGridsTabbedMap.get(scriptGridTracker.getScript().getUniqueID().intValue());
					if (tableGridsTabbed==null) {
						tableGridsTabbed=new TableGridsTabbed(scriptGridTracker,tableGrids);
						tableGridsTabbedMap.put(scriptGridTracker.getScript().getUniqueID().intValue(), tableGridsTabbed);
					}
					for (int i=tableGrids.size()-1;i>=0;i--) {
						TableGrid tableGrid=tableGrids.get(i);
						tableGrid.setFrame(tableGridsTabbed);
					}	
					tableGridsTabbed.setVisible(true);
				} 
				catch (Exception e) {
					FileUtils.printStackTrace(e);
					GUIUtils.popupErrorMessage(e.getMessage());		   
				}
			}
		});
	}

	private List<TableGrid> loadAndOpenChildGridTables(ScriptGridTracker scriptGridTracker,String inventoryName) throws Exception {
		List<Inventory> childInventories=InjectUtils.getChildren(executionTabPanel.getExecutionPanel().getInjectMain(),scriptGridTracker,inventoryName);
		List<TableGrid> toReturn=new  ArrayList<TableGrid>();
		List<InventoryPC> rootInventoryPCList=scriptIdToRootInventoryPCsMap.get(scriptGridTracker.getScript().getUniqueID().intValue());
		if (rootInventoryPCList==null || rootInventoryPCList.isEmpty()) {
			rootInventoryPCList=buildRowsPC(scriptGridTracker); 
			//rootInventoryPC.debugMaps();
			scriptIdToRootInventoryPCsMap.put(scriptGridTracker.getScript().getUniqueID().intValue(),rootInventoryPCList);
		}

		for (Inventory inventory:childInventories) {
			TableGrid childTableGrid=getTableGrid(scriptGridTracker,inventory.getName());
			toReturn.add(childTableGrid);

			List<TableGrid> temp=loadAndOpenChildGridTables(scriptGridTracker,inventory.getName());
			toReturn.addAll(temp);			
		}
		return toReturn;
	}

	public TableGrid getTableGrid(final ScriptGridTracker scriptGridTracker,String inventoryName) throws Exception {	
		synchronized(lock) {
			Map<String, TableGrid> inventoryNameToTableGridMap=scriptIdToTableGridMap.get(scriptGridTracker.getScript().getUniqueID().intValue());
			if (inventoryNameToTableGridMap==null) {
				inventoryNameToTableGridMap=new HashMap<String, TableGrid>();
				scriptIdToTableGridMap.put(scriptGridTracker.getScript().getUniqueID().intValue(), inventoryNameToTableGridMap);
			}
			TableGrid tableGrid=inventoryNameToTableGridMap.get(inventoryName);
			if (tableGrid==null) {
				tableGrid=createTableGrid(scriptGridTracker,inventoryName);
				inventoryNameToTableGridMap.put(inventoryName, tableGrid);
				//updateTableGrids(scriptGridTracker);
			}
			return tableGrid;
		}
	}

	private void setTableGridIsExecutionStarted(boolean isExecutionStarted) {	
		Iterator<Integer> iterator=scriptIdToTableGridMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key=iterator.next();
			Map<String, TableGrid> inventoryNameToTableGridMap=scriptIdToTableGridMap.get(key);
			Iterator<String> iteratorTemp=inventoryNameToTableGridMap.keySet().iterator();
			while (iteratorTemp.hasNext()) {
				String keyTemp=iteratorTemp.next();
				TableGrid tableGrid=inventoryNameToTableGridMap.get(keyTemp);
				tableGrid.setIsExecutionStarted(isExecutionStarted);
			}	
		}
	}

	private TableGrid createTableGrid(ScriptGridTracker scriptGridTracker,String inventoryName) throws Exception {
		final Inventory inventory=InjectUtils.getInventory(executionTabPanel.getExecutionPanel().getInjectMain(),
				executionTabPanel.getExecutionPanel().getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript(),inventoryName); 
		final List<String[]> dataRows=InjectUtils.getDataRows(executionTabPanel.getExecutionPanel().getInjectMain(),
				executionTabPanel.getExecutionPanel().getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript(),inventory);
		List<RecordTracker> recordTrackersList=scriptGridTracker.getInventoryToRecordTrackerMap().get(inventoryName);

		TableGrid tableGrid = new TableGrid(this,scriptGridTracker,inventory,recordTrackersList,dataRows);

		return tableGrid;
	}


	public void updateScriptGridTrackers(final List<ScriptGridTracker> scriptGridTrackersList) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (ScriptGridTracker scriptGridTracker:scriptGridTrackersList) {
					TableModel model = table.getModel();
					int gridIndex=scriptGridTracker.getGridIndex();
					int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
					int colIndexRemaining=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMAINING_RECORDS);
					int colIndexSuccess=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SUCCESS_RECORDS);
					int colIndexFailed=table.getColumnModel().getColumnIndex(COLUMN_HEADING_FAILED_RECORDS);
					int colIndexExecutionTotalTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_EXECUTION_TIME);
					int colIndexExecutionStartTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_START_EXECUTION_TIME);
					int colIndexExecutionEndTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_END_EXECUTION_TIME);
					int colIndexTotalBatchCount=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_BATCH_COUNT);
					int colIndexCompletedBatchCount=table.getColumnModel().getColumnIndex(COLUMN_HEADING_COMPLETED_BATCH_COUNT);
					int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
					int colIndexRetries=table.getColumnModel().getColumnIndex(COLUMN_HEADING_RETRIES);

					long startTime=scriptGridTracker.getStartTime();
					try {
						model.setValueAt(Utils.formatNumberWithComma(scriptGridTracker.getTotalRemainingRecords()),gridIndex,colIndexRemaining);
						model.setValueAt(Utils.formatNumberWithComma(scriptGridTracker.getTotalSuccessRecords()),gridIndex,colIndexSuccess);
						model.setValueAt(Utils.formatNumberWithComma(scriptGridTracker.getTotalFailedRecords()),gridIndex,colIndexFailed);
						model.setValueAt(Utils.formatNumberWithComma(scriptGridTracker.getTotalBatchCount()),gridIndex,colIndexTotalBatchCount);
						model.setValueAt(Utils.formatNumberWithComma(scriptGridTracker.getCompletedBatchCount()),gridIndex,colIndexCompletedBatchCount);

						if (startTime==0) {
							model.setValueAt("",gridIndex,colIndexExecutionTotalTime);
							model.setValueAt("",gridIndex,colIndexExecutionStartTime);
							model.setValueAt("",gridIndex,colIndexExecutionEndTime);
						}
						else {
							model.setValueAt(scriptGridTracker.getExecutionTotalTime(),gridIndex,colIndexExecutionTotalTime);
							model.setValueAt(scriptGridTracker.getExecutionStartTime(),gridIndex,colIndexExecutionStartTime);
							model.setValueAt(scriptGridTracker.getExecutionEndTime(),gridIndex,colIndexExecutionEndTime);
						}
						String status=scriptGridTracker.getStatus();
						model.setValueAt(status,gridIndex,colIndexStatus);
						if (status.equals(ScriptsGrid.STATUS_SUCCESS)) {
							table.setValueAt(false,gridIndex,table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION));
						}

						String remarks=scriptGridTracker.getRemarks();
						if (remarks!=null) {
							model.setValueAt(remarks,gridIndex,colIndexRemarks);
						}

						int executionRetries=scriptGridTracker.getExecutionRetries();
						model.setValueAt(""+executionRetries,gridIndex,colIndexRetries);
					}
					catch (Exception e) {
						FileUtils.printStackTrace(e);
					}
				}
				table.repaint();
			}
		});
	}

	public String getStatus(final int gridIndex) throws Exception
	{
		TableModel model = table.getModel();
		int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
		return (String) model.getValueAt(gridIndex,colIndexStatus);
	}

	public boolean isScriptSelected(int scriptGridIndex)
	{
		TableModel model = table.getModel();
		int colIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		boolean isSelected=(boolean) model.getValueAt(scriptGridIndex,colIndex);
		return isSelected;
	}

	private void closeTableGrids() {
		if (scriptIdToTableGridMap==null) {
			return;
		}
		Iterator<Integer> iterator=scriptIdToTableGridMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key=iterator.next();
			Map<String, TableGrid> inventoryNameToTableGridMap=scriptIdToTableGridMap.get(key);
			if (inventoryNameToTableGridMap==null) {
				continue;
			}
			Iterator<String> iteratorTemp=inventoryNameToTableGridMap.keySet().iterator();
			while (iteratorTemp.hasNext()) {
				String keyTemp=iteratorTemp.next();
				TableGrid tableGrid=inventoryNameToTableGridMap.get(keyTemp);
				if (tableGrid.getFrame()!=null) {
					tableGrid.getFrame().setVisible(false);
					tableGrid.getFrame().dispose();
				}
			}	
		}
	}

	public void updateTableGrids(final List<ScriptGridTracker> scriptGridTrackersList,final boolean isUpdateOnlyIfVisible) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (ScriptGridTracker scriptGridTracker:scriptGridTrackersList) {
					updateTableGrids(scriptGridTracker,isUpdateOnlyIfVisible);
				}
			}
		});
	}

	public void updateTableGrids(ScriptGridTracker scriptGridTracker,boolean isUpdateOnlyIfVisible) {
		if (isUpdateOnlyIfVisible && openedScriptGridTracker!=null && scriptGridTracker.getScript().getUniqueID()!= openedScriptGridTracker.getScript().getUniqueID()) {
			return;
		}
		Map<String, TableGrid> tableGrids=scriptIdToTableGridMap.get(scriptGridTracker.getScript().getUniqueID().intValue());
		if (tableGrids!=null) {
			Map<String, List<RecordTracker>> inventoryToRecordTrackerMap=scriptGridTracker.getInventoryToRecordTrackerMap(); 
			Iterator<String> iterator=tableGrids.keySet().iterator();
			while ( iterator.hasNext()) {
				String key=iterator.next();
				TableGrid tableGrid=tableGrids.get(key);
				List<RecordTracker> recordTrackersList=inventoryToRecordTrackerMap.get(key);
				if (recordTrackersList!=null) {
					tableGrid.updateTableGrid(recordTrackersList);
				}
			}
		}
	} 
	
	protected void setEnableOnDependantRows(ScriptGridTracker scriptGridTracker,TableGrid tableGrid,List<Integer> rowIndexesTableGrid, boolean isEnabled) {
		if(scriptIdToRootInventoryPCsMap.isEmpty()) {
			return;
		}
		List<InventoryPC> rootInventoryPCList=scriptIdToRootInventoryPCsMap.get(scriptGridTracker.getScript().getUniqueID().intValue());
		if (rootInventoryPCList==null || rootInventoryPCList.isEmpty()) {
			return;
		}
		for (InventoryPC rootInventoryPC:rootInventoryPCList) {
			setEnableOnDependantRows(scriptGridTracker,rootInventoryPC,tableGrid,rowIndexesTableGrid,isEnabled);
		}
	}

	private void setEnableOnDependantRows(ScriptGridTracker scriptGridTracker,InventoryPC rootInventoryPC,
			TableGrid tableGrid,List<Integer> rowIndexesTableGrid, boolean isEnabled) {
		try{
			InventoryPC inventoryPC=rootInventoryPC.getInventoryPCFromRoot(tableGrid.getInventory().getName());
			if (inventoryPC==null) {
				throw new Exception("Unexpected error, cannot find reference to '"+tableGrid.getInventory().getName()+"'");
			}
			inventoryPC.setEnableOnChildRows(this,scriptGridTracker,rowIndexesTableGrid,isEnabled);
			if (isEnabled) {
				inventoryPC.setEnableOnParentRows(this,scriptGridTracker,rowIndexesTableGrid,rootInventoryPC);
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to switch the selection checkboxes on the child rows. Error: "+e.getMessage());
		}
	}

	protected List<InventoryPC> buildRowsPC(ScriptGridTracker scriptGridTracker) throws Exception {
		FusionDataType fusionDataType=InjectUtils.getFusionDataType(executionTabPanel.getExecutionPanel().getInjectMain(),
				executionTabPanel.getExecutionPanel().getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());
		List<InventoryMapType> inventoryMapTypeList=InjectUtils.getInventoryMapTypeList(executionTabPanel.getExecutionPanel().getInjectMain(),
				executionTabPanel.getExecutionPanel().getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());
		Map<String,String> codeToInventoryNameMap=new HashMap<String,String>();
		for (InventoryMapType inventoryMap:inventoryMapTypeList){
			String name=inventoryMap.getName();
			String code=inventoryMap.getCode();
			codeToInventoryNameMap.put(code,name);
		}

		List<Inventory> rootInventories=InjectUtils.getRootInventories(executionTabPanel.getExecutionPanel().getInjectMain(),
				executionTabPanel.getExecutionPanel().getInjectMain().getApplicationInfoPanel().getInjectionPackage(),scriptGridTracker.getScript());
		Map<String, Inventory> inventoryMap=executionTabPanel.getInventoryMap(scriptGridTracker);		

		Map<String , InventoryPC> inventoryPCMap=new HashMap<String , InventoryPC>();
		Iterator<String> iterator=inventoryMap.keySet().iterator();
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			Inventory inventory=inventoryMap.get(inventoryName);
			String parentInventoryName=inventory.getParentName();
			InventoryPC inventoryPC=new InventoryPC(inventoryName,parentInventoryName);
			inventoryPCMap.put(inventoryName, inventoryPC);
		}
		iterator=inventoryMap.keySet().iterator();
		while (iterator.hasNext()) {
			String inventoryName=iterator.next();
			Inventory inventory=inventoryMap.get(inventoryName);
			String parentInventoryName=inventory.getParentName();

			if (parentInventoryName!=null && !parentInventoryName.isEmpty()) {
				InventoryPC parentInventoryPC=inventoryPCMap.get(parentInventoryName);
				if (parentInventoryPC==null) {
					throw new Exception("Incorrect package: missing inventory '"+parentInventoryName+"'");
				}
				List<InventoryPC> childInventoryList=parentInventoryPC.getChildInventoryList();
				InventoryPC inventoryPCtemp=inventoryPCMap.get(inventoryName);
				childInventoryList.add(inventoryPCtemp);
			}
		}
		
		List<InventoryPC> toReturn=new ArrayList<InventoryPC>();
		for (Inventory rootInventory:rootInventories) {
			InventoryPC rootInventoryPC=inventoryPCMap.get(rootInventory.getName());
			rootInventoryPC.setAllInventoryPCMap(inventoryPCMap);
			toReturn.add(rootInventoryPC);
		}
				
		LType[] lArray=fusionDataType.getLArray();
		for (LType l:lArray) {
			MType[] mArray=l.getMArray();

			Map<String,Integer> inventoryNameToRowIndexMap=new HashMap<String,Integer>();
			for (MType m:mArray) {
				String code=m.getC();
				int rowIndex=m.getR().intValue();
				String inventoryName=codeToInventoryNameMap.get(code);
				inventoryNameToRowIndexMap.put(inventoryName, rowIndex);
			}
			for (Inventory rootInventory:rootInventories) {
				InventoryPC rootInventoryPC=inventoryPCMap.get(rootInventory.getName());
				rootInventoryPC.initMaps(inventoryNameToRowIndexMap);
			}
		}
		return toReturn;
	}

	public ExecutionTabPanel getExecutionTabPanel() {
		return executionTabPanel;
	}
	
	public void verifyInconsistenciesInSelectionRows(ScriptGridTracker scriptGridTracker) {
		try{
			List<InventoryPC> rootInventoryPCList=scriptIdToRootInventoryPCsMap.get(scriptGridTracker.getScript().getUniqueID().intValue());
			if (rootInventoryPCList==null || rootInventoryPCList.isEmpty()) {
				// Table grids not opened yet. so nothing to check.
			}
			else {
				for (InventoryPC rootInventoryPC:rootInventoryPCList) {
					verifyInconsistenciesInSelectionRows(scriptGridTracker,rootInventoryPC);
				}
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to verify Inconsistencies in selection rows. Error: "+e.getMessage());
		}
	}
	
	private void verifyInconsistenciesInSelectionRows(ScriptGridTracker scriptGridTracker,InventoryPC rootInventoryPC) {
		try{
			if (rootInventoryPC==null) {
				// Table grids not opened yet. so nothing to check.
			}
			else {
				rootInventoryPC.selectParentRowIfChildRowIsSelected(this, scriptGridTracker);
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to verify Inconsistencies in selection rows. Error: "+e.getMessage());
		}
	}

	public TableGrid getTableGridFromMap(int scriptId,String inventoryName) throws Exception
	{
		Map<String, TableGrid> inventoryToTableGridMap=scriptIdToTableGridMap.get(scriptId);
		if (inventoryToTableGridMap==null) {
			return null;
		}
		TableGrid tableGrid=inventoryToTableGridMap.get(inventoryName);
		if (tableGrid==null) {
			return null;
		}
		return tableGrid;
	}

	/*
	 * If the grid was never opened, then 
	 *		if status == SUCCESS then NO
	 *		else YES
	 *
	 * If the grid was opened, then
	 * 		if record checkbox selected then YES
	 * 		else NO
	 * 
	 */
	public boolean isRecordSelected(RecordTracker recordTracker,int scriptId,String inventoryName) throws Exception {
		TableGrid tableGrid=getTableGridFromMap(scriptId,inventoryName);
		if (tableGrid==null) {
			String status=recordTracker.getStatus();
			if (status.equalsIgnoreCase(ScriptsGrid.STATUS_SUCCESS) ) {
				return false;
			}
			else {
				return true;
			}
		}
		else {
			boolean isRecordSelected=tableGrid.isSelected(recordTracker.getGridIndex());
			if (isRecordSelected) {
				return true;
			}
			else {
				return false;
			}
		}
	}

}
