package com.rapidesuite.inject.gui;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import com.rapidesuite.build.utils.SwiftBuildFileUtils;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.RecordTracker;
import com.rapidesuite.inject.ScriptGridTracker;
import com.rapidesuite.inject.ScriptManager;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

@SuppressWarnings("serial")
public class TableGrid extends AbstractGrid {

	private ScriptsGrid scriptsGrid;
	private ScriptGridTracker scriptGridTracker;
	private Inventory inventory;
	
//	public static String COLUMN_HEADING_SELECTION="Selection"; //in base class AbstractGrid now
	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_BATCH_ID="Batch ID";
	public static String COLUMN_HEADING_WORKER_ID="Worker ID";
	public static String COLUMN_HEADING_STATUS="Injection Status";
	public static String COLUMN_HEADING_REMARKS="Remarks";
	public static String COLUMN_HEADING_FIELD_NAME="Field name";
	public static String COLUMN_HEADING_EXECUTION_TIME="Execution time";
	public static String COLUMN_HEADING_VNC="Display name";
	public static String COLUMN_HEADING_SCREENSHOT="Screenshot";
	public static String COLUMN_HEADING_VIEW_LOG="Log";
	public static String COLUMN_HEADING_OPEN_LOG_FOLDER="Log Folder";
	public static String COLUMN_HEADING_CLOSE_FIREFOX="Browser";
	public static String COLUMN_HEADING_LABEL="Label";

	private static int COLUMN_HEADING_SELECTION_WIDTH=60;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=50;
	private static int COLUMN_HEADING_BATCH_ID_WIDTH=60;
	private static int COLUMN_HEADING_WORKER_ID_WIDTH=70;
	private static int COLUMN_HEADING_STATUS_WIDTH=130;
	private static int COLUMN_HEADING_REMARKS_WIDTH=200;
	private static int COLUMN_HEADING_EXECUTION_TIME_WIDTH=100;
	private static int COLUMN_HEADING_VNC_WIDTH=250;
	private static int COLUMN_HEADING_SCREENSHOT_WIDTH=100;
	private static int COLUMN_HEADING_VIEW_LOG_WIDTH=100;
	private static int COLUMN_HEADING_OPEN_LOG_FOLDER_WIDTH=100;
	private static int COLUMN_HEADING_CLOSE_FIREFOX_WIDTH=100;
	private static int COLUMN_HEADING_FIELD_NAME_WIDTH=100;
	private static int COLUMN_HEADING_LABEL_WIDTH=100;
	private static int COLUMN_HEADING_INVENTORY_NAMES_WIDTH=120;
	private boolean isExecutionStarted;
	private JFrame frame;
	private List<Field> fields;
	
	private List<RecordTracker> recordTrackersList;
	private Map<Integer,List<TableGrid>> sortedChildrenMap;
	
	public TableGrid(ScriptsGrid scriptsGrid,ScriptGridTracker scriptGridTracker,Inventory inventory,
			List<RecordTracker> recordTrackersList,List<String[]> dataRows) throws Exception {
		this.inventory=inventory;
		this.scriptsGrid=scriptsGrid;
		this.scriptGridTracker=scriptGridTracker;

		if (recordTrackersList==null) {
			this.recordTrackersList=new ArrayList<RecordTracker>();
		}
		else {
			this.recordTrackersList = recordTrackersList;
		}
		//List<Field> fields=inventory.getFieldsUsedForPrimaryKey();
		fields=inventory.getFieldsUsedForDataEntry();
		sortedChildrenMap=new TreeMap<Integer,List<TableGrid>>();
		
		createVariableTable(inventory,dataRows);
	}
	
	public static Vector<String> getColumns(Inventory inventory){
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_SELECTION);
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_BATCH_ID);
		columnNames.add(COLUMN_HEADING_WORKER_ID);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_REMARKS);
		columnNames.add(COLUMN_HEADING_FIELD_NAME);
		columnNames.add(COLUMN_HEADING_EXECUTION_TIME);		
		columnNames.add(COLUMN_HEADING_VNC);
		columnNames.add(COLUMN_HEADING_SCREENSHOT);
		columnNames.add(COLUMN_HEADING_VIEW_LOG);
		columnNames.add(COLUMN_HEADING_OPEN_LOG_FOLDER);
		columnNames.add(COLUMN_HEADING_CLOSE_FIREFOX);
		columnNames.add(COLUMN_HEADING_LABEL);
		List<Field> fields=inventory.getFieldsUsedForDataEntry();
		for (Field field:fields) {
			columnNames.add(field.getName());
		}
		
		return columnNames;
	}
	
	public void createVariableTable(Inventory inventory,List<String[]> dataRows) throws Exception {
		Vector<String> columnNames = getColumns(inventory);
		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		int rowIndex=0;
		for (String[] dataRow:dataRows) {
			RecordTracker recordTracker=recordTrackersList.get(rowIndex);
			Vector<Object> row = new Vector<Object>();
			data.add(row);
			rowIndex++;
			row.add(recordTracker.getIsSelected());
			row.add(Utils.formatNumberWithComma(rowIndex));
			int batchId=recordTracker.getBatchId();
			String batchIdStr="";
			if (batchId!=0) {
				batchIdStr=""+batchId;
			}
			row.add(batchIdStr);
			int workerId=recordTracker.getWorkerId();
			String workerIdStr="";
			if (workerId!=0) {
				workerIdStr=""+workerId;
			}
			row.add(workerIdStr);			
			row.add(recordTracker.getStatus());
			row.add("");
			row.add("");
			row.add("");
			row.add("");
			row.add("Open");
			row.add("View");
			row.add("Open");
			row.add("Close");
			row.add(dataRow[dataRow.length-2]);
			for (Field field:fields) {
				int index=inventory.getIndexForColumn(field.getName());
				row.add(dataRow[index]);
			}
		}		

		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
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
					//String status= (String) getValueAt(row, table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS));	
					//if (status.equalsIgnoreCase(STATUS_SUCCESS)) {
					//	return false;
					//}					
					return !isExecutionStarted;
				case 9:
					return true;
				case 10:
					return true;
				case 11:
					return true;
				case 12:
					return true;
				default:
					return false;
				}
			}
			
			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int colIndex) {
				Component component= super.prepareRenderer(renderer, rowIndex, colIndex);

				int colIndexSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				String status= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS));				
				
				if (colIndex==colIndexSelection) {
					component.setEnabled(!isExecutionStarted);
				}				
				
				if (status.equalsIgnoreCase(STATUS_PROCESSING)) {
					component.setBackground(yellowColor);
				}
				else 
					if (status.equalsIgnoreCase(STATUS_FAILED) ) {
						component.setBackground(redColor);
					}
					else 
						if (status.equalsIgnoreCase(STATUS_PENDING) || status.equalsIgnoreCase(STATUS_QUEUED)) {
							component.setBackground(Color.WHITE);
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
	                String tip = null;
	                java.awt.Point p = e.getPoint();
	                int rowIndex = rowAtPoint(p);
	                int colIndex = columnAtPoint(p);

	                int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
	                if (colIndex==colIndexRemarks) {
	                	try {
	                		tip = "<html><p width=\"500\">" +getValueAt(rowIndex, colIndex).toString()+"</p></html>";
	                	} catch (RuntimeException e1) {
	                		//catch null pointer exception if mouse is over an empty line
	                	}
	                }

	                return tip;
	            }

		};
		List<Field> fields=inventory.getFieldsUsedForDataEntry();
		List<String> extraColumnNames=new ArrayList<String>();
		for (Field field:fields) {
			extraColumnNames.add(field.getName());
		}
		setColumnSize(table,extraColumnNames);

		Border borderButton=BorderFactory.createEmptyBorder(2, 20, 0, 0);
		int width=70;
		int height=17;
		ButtonRenderer buttonRenderer=new ButtonRenderer(borderButton,width,height);

		//table.getColumn(COLUMN_HEADING_VNC).setCellRenderer(buttonRenderer);
		//table.getColumn(COLUMN_HEADING_VNC).setCellEditor(new ButtonEditor(new JCheckBox()));
		table.getColumn(COLUMN_HEADING_SCREENSHOT).setCellRenderer(buttonRenderer);
		table.getColumn(COLUMN_HEADING_SCREENSHOT).setCellEditor(new ButtonEditor(new JCheckBox(),borderButton,width,height,null));
		table.getColumn(COLUMN_HEADING_VIEW_LOG).setCellRenderer(buttonRenderer);
		table.getColumn(COLUMN_HEADING_VIEW_LOG).setCellEditor(new ButtonEditor(new JCheckBox(),borderButton,width,height,null));
		table.getColumn(COLUMN_HEADING_OPEN_LOG_FOLDER).setCellRenderer(buttonRenderer);
		table.getColumn(COLUMN_HEADING_OPEN_LOG_FOLDER).setCellEditor(new ButtonEditor(new JCheckBox(),borderButton,width,height,null));
		table.getColumn(COLUMN_HEADING_CLOSE_FIREFOX).setCellRenderer(buttonRenderer);
		table.getColumn(COLUMN_HEADING_CLOSE_FIREFOX).setCellEditor(new ButtonEditor(new JCheckBox(),borderButton,width,height,null));
	
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(renderer);
		table.getColumn(COLUMN_HEADING_BATCH_ID).setCellRenderer(renderer);
		table.getColumn(COLUMN_HEADING_WORKER_ID).setCellRenderer(renderer);
		table.getColumn(COLUMN_HEADING_STATUS).setCellRenderer(renderer);
		table.getColumn(COLUMN_HEADING_REMARKS).setCellRenderer(renderer);
		table.getColumn(COLUMN_HEADING_LABEL).setCellRenderer(renderer);
		table.getColumn(COLUMN_HEADING_EXECUTION_TIME).setCellRenderer(renderer);
		for (Field field:fields) {
			TableColumn column=table.getColumn(field.getName());
			column.setCellRenderer(renderer);
			if (field.getParentName()!=null && !field.getParentName().isEmpty()) {
				//table.getTableHeader().setDefaultRenderer(new ForeignKeyHeaderRenderer());
				column.setHeaderRenderer(new CustomHeaderRenderer("Parent/ Child field",yellowColor));
			}
		}
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
	    table.setBackground(Color.decode("#DBDBDB"));
	    table.setRowHeight(18);
	    JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 30));
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent evt) {
				Point point = evt.getPoint();
				int row,column;
				row 	= table.rowAtPoint(point);
				column 	= table.columnAtPoint(point);
				if (row >= 0 && column >= 0) {
					//System.out.println("Cell Value at Row:"+row+" | Column:"+column+" is " + table.getValueAt(row,column));

					int colIndexViewLog=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW_LOG);
					int colIndexOpenLogFolder=table.getColumnModel().getColumnIndex(COLUMN_HEADING_OPEN_LOG_FOLDER);
					int colIndexScreenshot=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SCREENSHOT);
					int colIndexCloseFirefox=table.getColumnModel().getColumnIndex(COLUMN_HEADING_CLOSE_FIREFOX);
					int colIndexSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
					
					if (column==colIndexSelection) {
						setEnableOnChildRows(row);
					}
					else
					if (column==colIndexViewLog) {
						viewLog( (String)table.getValueAt(row, table.getColumnModel().getColumnIndex(COLUMN_HEADING_WORKER_ID)));
					}
					else
					if (column==colIndexOpenLogFolder) {
						openLogFolder();
					}
					else
					if (column==colIndexScreenshot) {
						openScreenshot( (String)table.getValueAt(row, table.getColumnModel().getColumnIndex(COLUMN_HEADING_WORKER_ID)));
					}
					else
					if (column==colIndexCloseFirefox) {
						closeFirefox( (String)table.getValueAt(row, table.getColumnModel().getColumnIndex(COLUMN_HEADING_WORKER_ID)));
					}
				}
			}
		});
				
		setPopupMenu(this);	
	}

	protected void openLogFolder() {
		try {
			File logFolder=InjectUtils.getScriptLogFolder(scriptGridTracker);
			if (!logFolder.exists()) {
				logFolder.mkdirs();
			}
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(logFolder);
			}
			else {
				SeleniumUtils.startLinuxFileBrowser(logFolder);
			}
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to open log folder. Error: "+e.getMessage());		 
		}
	}

	protected void setEnableOnChildRows(int row) {
		Boolean isEnabled=(Boolean)table.getValueAt(row, table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION));
		List<Integer> rowIndexesTableGrid=new ArrayList<Integer>();
		rowIndexesTableGrid.add(row);
		scriptsGrid.setEnableOnDependantRows(scriptGridTracker,this,rowIndexesTableGrid,isEnabled);
		
		ScriptsGrid.setSelectionOnRows(this,isEnabled,false);
		
		//update the original recordTrackersList
		recordTrackersList.get(row).setIsSelected(isEnabled);
	}
	
	protected void closeFirefox(String workerIdStr) {
		try{
			if (!scriptsGrid.getExecutionTabPanel().getExecutionPanel().getOptionsTabPanel().getOptionsHTMLPlaybackPanel().isServerMode()) {
				GUIUtils.popupInformationMessage("Only available in server mode.");
				return;
			}
			ScriptManager scriptManager=scriptsGrid.getExecutionTabPanel().getExecutionPanel().getInjectMain().getScriptManager();
			if (scriptManager==null) {
				GUIUtils.popupInformationMessage("You must start an injection first.");
				return;
			}
			if (workerIdStr.isEmpty()) {
				GUIUtils.popupInformationMessage("No worker, you must start an injection first.");
				return;
			}
			int workerId=Integer.valueOf(workerIdStr);
			Worker worker=scriptManager.getWorker(workerId);
			if (worker==null) {
				GUIUtils.popupInformationMessage("This worker has no Firefox attached anymore, unable to close it.");
				return;
			}
			if (worker instanceof SeleniumWorker) {
				SeleniumWorker seleniumWorker=(SeleniumWorker)worker;
				if (seleniumWorker.getWebDriver()==null) {
					GUIUtils.popupInformationMessage("Unable to retrieve Firefox instance, please try again later...");
					return;
				}
				worker.stopExecution(true);
			}
			else {
				GUIUtils.popupInformationMessage("Feature not available for this type of script!");
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
		}
	}

	private void openScreenshot(String workerIdStr) {
		try{
			ScriptManager scriptManager=scriptsGrid.getExecutionTabPanel().getExecutionPanel().getInjectMain().getScriptManager();
			if (scriptManager==null) {
				GUIUtils.popupInformationMessage("You must start an injection before taking a screenshot");
				return;
			}
			if (workerIdStr.isEmpty()) {
				GUIUtils.popupInformationMessage("No worker, you must start an injection before taking a screenshot");
				return;
			}
			int workerId=Integer.valueOf(workerIdStr);
			Worker worker=scriptManager.getWorker(workerId);
			if (worker==null) {
				GUIUtils.popupInformationMessage("This worker has no Firefox attached anymore, unable to take a screenshot."+
					" Opening previous screenshot...");
				openPreviousScreenshot(workerId);
				return;
			}
			if (worker instanceof SeleniumWorker) {
				SeleniumWorker seleniumWorker=(SeleniumWorker)worker;
				if (seleniumWorker.getWebDriver()==null) {
					GUIUtils.popupInformationMessage("Firefox is starting, please try again later...");
					return;
				}
				File file=new File(seleniumWorker.getWorkerLogFolder(),"screenshotWorker"+workerId+".png");
				try{
					SeleniumUtils.captureScreenShot(seleniumWorker.getWebDriver(),file);
				}
				catch(org.openqa.selenium.UnhandledAlertException e) {
					openPreviousScreenshot(workerId);
				}
				openFile(file);
			}
			else {
				GUIUtils.popupInformationMessage("Feature not available for this type of script!");
			}
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to take screenshot, error: "+e.getMessage());
		}
	}
	
	private void openPreviousScreenshot(int workerId) throws IOException {
		String relativeLogFolderName=scriptGridTracker.getLogFolderName();
		File logsFolder = SwiftBuildFileUtils.getLogsFolderFromName(scriptGridTracker.getInjectMain().getApplicationInfoPanel().getInjectionPackage().getName());
		File workerLogFolder=new File(logsFolder,relativeLogFolderName);
		File file=new File(workerLogFolder,"screenshotWorker"+workerId+".png");
		openFile(file);
	}
	
	private void openFile(File file) throws IOException {
		//Desktop.getDesktop().open(file);
		ImageFileOpener.createFrame(file);
	}

	public void setIsExecutionStarted(boolean isExecutionStarted) {
		this.isExecutionStarted=isExecutionStarted;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				table.repaint(); 
			}
		});
	}
	
	private void viewLog(String workerId) {
		if (workerId==null || workerId.isEmpty()) {
			GUIUtils.popupInformationMessage("Unable to find the Log. The worker may not have started yet! Please try again later.");
		}
		else {
			try {
				File logFile=InjectUtils.getScriptLogFile(scriptGridTracker,workerId,true);
				FileUtils.startTextEditor(Config.getCmdTextEditor(),logFile);
			} catch (Exception e) {
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage("Unable to view Log. Error: "+e.getMessage());		 
			}
		}
	}
	
	public boolean isSelected(int gridIndex) throws Exception
	{		
		return recordTrackersList.get(gridIndex).getIsSelected();
	}

	public ScriptsGrid getScriptsGrid() {
		return scriptsGrid;
	}

	public ScriptGridTracker getScriptGridTracker() {
		return scriptGridTracker;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}
	
	public void updateTableGrid(final List<RecordTracker> recordTrackersList)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (RecordTracker recordTracker:recordTrackersList) {
					updateTableGridAction(recordTracker);
				}				
				table.repaint(); 
			}
		});
	}
		
	private void updateTableGridAction(final RecordTracker recordTracker)
	{
		final TableModel model = table.getModel();
		final int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
		final int gridIndex=recordTracker.getGridIndex();
		final int colIndexFieldName=table.getColumnModel().getColumnIndex(COLUMN_HEADING_FIELD_NAME);
		final int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
		int colIndexWorkerID=table.getColumnModel().getColumnIndex(COLUMN_HEADING_WORKER_ID);
		int colIndexBatchID=table.getColumnModel().getColumnIndex(COLUMN_HEADING_BATCH_ID);
		int colIndexVNC=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VNC);
		int colIndexExecutionTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_EXECUTION_TIME);
		
		final String status=recordTracker.getStatus();
		model.setValueAt(status,gridIndex,colIndexStatus);
		
		int workerId=recordTracker.getWorkerId();
		if (workerId==0) {
			model.setValueAt("",gridIndex,colIndexWorkerID);
		}
		else {
			model.setValueAt(""+workerId,gridIndex,colIndexWorkerID);
		}
		
		int batchId=recordTracker.getBatchId();
		if (batchId==0) {
			model.setValueAt("",gridIndex,colIndexBatchID);
		}
		else {
			model.setValueAt(""+batchId,gridIndex,colIndexBatchID);
		}
		
		String fieldName=recordTracker.getFieldName();
		if (fieldName!=null) {
			model.setValueAt(fieldName,gridIndex,colIndexFieldName);
		}
		String remarks=recordTracker.getRemarks();
		if (remarks!=null) {
			model.setValueAt(remarks,gridIndex,colIndexRemarks);
		}
		String vncDisplayName=recordTracker.getVncDisplayName();
		if (remarks!=null) {
			model.setValueAt(vncDisplayName,gridIndex,colIndexVNC);
		}	
		String executionTime=recordTracker.getExecutionTime();
		if (executionTime!=null) {
			model.setValueAt(executionTime,gridIndex,colIndexExecutionTime);
		}
		
		model.setValueAt(recordTracker.getIsSelected(),gridIndex,table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION));
	}

	public void updateSelectionTableGrid(final List<Integer> rowGrids,final boolean iSelected) throws Exception
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final TableModel model = table.getModel();
				int colIndexSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				for (int rowGridIndex:rowGrids) {
					model.setValueAt(iSelected,rowGridIndex,colIndexSelection);

					//update the original recordTrackersList
					recordTrackersList.get(rowGridIndex).setIsSelected(iSelected);
				}				
				table.repaint(); 
			}
		});
	}
	
	public void updateSelectionRow(final Integer rowGridIndex,final boolean iSelected) throws Exception
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final TableModel model = table.getModel();
				int colIndexSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				model.setValueAt(iSelected,rowGridIndex,colIndexSelection);				
				table.repaint(); 
			}
		});
	}
	
	public static void setColumnSize(JTable table,List<String> columnNames) {
		TableColumn columnSelection = table.getColumn(COLUMN_HEADING_SELECTION);
		columnSelection.setMinWidth(COLUMN_HEADING_SELECTION_WIDTH);
		columnSelection.setMaxWidth(COLUMN_HEADING_SELECTION_WIDTH);
		columnSelection.setPreferredWidth(COLUMN_HEADING_SELECTION_WIDTH);
		columnSelection.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnRowNum = table.getColumn(COLUMN_HEADING_ROW_NUM);
		columnRowNum.setMinWidth(COLUMN_HEADING_ROW_NUM_WIDTH);
		columnRowNum.setMaxWidth(COLUMN_HEADING_ROW_NUM_WIDTH);
		columnRowNum.setPreferredWidth(COLUMN_HEADING_ROW_NUM_WIDTH);
		columnRowNum.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnBatchId = table.getColumn(COLUMN_HEADING_BATCH_ID);
		columnBatchId.setMinWidth(COLUMN_HEADING_BATCH_ID_WIDTH);
		columnBatchId.setMaxWidth(COLUMN_HEADING_BATCH_ID_WIDTH);
		columnBatchId.setPreferredWidth(COLUMN_HEADING_BATCH_ID_WIDTH);
		columnBatchId.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnWorkerId = table.getColumn(COLUMN_HEADING_WORKER_ID);
		columnWorkerId.setMinWidth(COLUMN_HEADING_WORKER_ID_WIDTH);
		columnWorkerId.setMaxWidth(COLUMN_HEADING_WORKER_ID_WIDTH);
		columnWorkerId.setPreferredWidth(COLUMN_HEADING_WORKER_ID_WIDTH);
		columnWorkerId.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnStatus = table.getColumn(COLUMN_HEADING_STATUS);
		columnStatus.setMinWidth(COLUMN_HEADING_STATUS_WIDTH);
		columnStatus.setMaxWidth(COLUMN_HEADING_STATUS_WIDTH);
		columnStatus.setPreferredWidth(COLUMN_HEADING_STATUS_WIDTH);
		columnStatus.setHeaderRenderer(new TableHeaderRenderer());
		
		TableColumn columnExecutionTime = table.getColumn(COLUMN_HEADING_EXECUTION_TIME);
		columnExecutionTime.setMinWidth(COLUMN_HEADING_EXECUTION_TIME_WIDTH);
		columnExecutionTime.setMaxWidth(COLUMN_HEADING_EXECUTION_TIME_WIDTH);
		columnExecutionTime.setPreferredWidth(COLUMN_HEADING_EXECUTION_TIME_WIDTH);
		columnExecutionTime.setHeaderRenderer(new TableHeaderRenderer());
		
		TableColumn columnRemarks = table.getColumn(COLUMN_HEADING_REMARKS);
		columnRemarks.setMinWidth(COLUMN_HEADING_REMARKS_WIDTH);
		columnRemarks.setPreferredWidth(COLUMN_HEADING_REMARKS_WIDTH);
		columnRemarks.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnVNC= table.getColumn(COLUMN_HEADING_VNC);
		columnVNC.setMinWidth(COLUMN_HEADING_VNC_WIDTH);
		columnVNC.setPreferredWidth(COLUMN_HEADING_VNC_WIDTH);
		columnVNC.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnFocusBrowser = table.getColumn(COLUMN_HEADING_SCREENSHOT);
		columnFocusBrowser.setMinWidth(COLUMN_HEADING_SCREENSHOT_WIDTH);
		columnFocusBrowser.setMaxWidth(COLUMN_HEADING_SCREENSHOT_WIDTH);
		columnFocusBrowser.setPreferredWidth(COLUMN_HEADING_SCREENSHOT_WIDTH);
		columnFocusBrowser.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnViewLog = table.getColumn(COLUMN_HEADING_VIEW_LOG);
		columnViewLog.setMinWidth(COLUMN_HEADING_VIEW_LOG_WIDTH);
		columnViewLog.setMaxWidth(COLUMN_HEADING_VIEW_LOG_WIDTH);
		columnViewLog.setPreferredWidth(COLUMN_HEADING_VIEW_LOG_WIDTH);
		columnViewLog.setHeaderRenderer(new TableHeaderRenderer());
		
		TableColumn columnOpenLogFolder = table.getColumn(COLUMN_HEADING_OPEN_LOG_FOLDER);
		columnOpenLogFolder.setMinWidth(COLUMN_HEADING_OPEN_LOG_FOLDER_WIDTH);
		columnOpenLogFolder.setMaxWidth(COLUMN_HEADING_OPEN_LOG_FOLDER_WIDTH);
		columnOpenLogFolder.setPreferredWidth(COLUMN_HEADING_OPEN_LOG_FOLDER_WIDTH);
		columnOpenLogFolder.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnCloseFirefox = table.getColumn(COLUMN_HEADING_CLOSE_FIREFOX);
		columnCloseFirefox.setMinWidth(COLUMN_HEADING_CLOSE_FIREFOX_WIDTH);
		columnCloseFirefox.setMaxWidth(COLUMN_HEADING_CLOSE_FIREFOX_WIDTH);
		columnCloseFirefox.setPreferredWidth(COLUMN_HEADING_CLOSE_FIREFOX_WIDTH);
		columnCloseFirefox.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnOpenChildren = table.getColumn(COLUMN_HEADING_FIELD_NAME);
		columnOpenChildren.setMinWidth(COLUMN_HEADING_FIELD_NAME_WIDTH);
		columnOpenChildren.setPreferredWidth(COLUMN_HEADING_FIELD_NAME_WIDTH);
		columnOpenChildren.setHeaderRenderer(new TableHeaderRenderer());

		TableColumn columnLabel = table.getColumn(COLUMN_HEADING_LABEL);
		columnLabel.setMinWidth(COLUMN_HEADING_LABEL_WIDTH);
		//columnLabel.setMaxWidth(COLUMN_HEADING_LABEL_WIDTH);
		columnLabel.setPreferredWidth(COLUMN_HEADING_LABEL_WIDTH);		
		columnLabel.setHeaderRenderer(new TableHeaderRenderer());
		
		for (String columnName:columnNames) {
			TableColumn column=table.getColumn(columnName);
			column.setMinWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
			//column.setMaxWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
			column.setPreferredWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
			column.setHeaderRenderer(new TableHeaderRenderer());
		}
	}
	
	public void addChild(Integer sequence,TableGrid tableGrid) {
		List<TableGrid> list=sortedChildrenMap.get(sequence);
		if (list==null) {
			list=new ArrayList<TableGrid>();
			sortedChildrenMap.put(sequence,list);
		}
		list.add(tableGrid);
	}

	public Map<Integer, List<TableGrid>> getSortedChildrenMap() {
		return sortedChildrenMap;
	}
	
	public List<TableGrid> getSortedChildrenList() {
		List<TableGrid> toReturn=new ArrayList<TableGrid>();
		Iterator<Integer> iterator=sortedChildrenMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer sequence=iterator.next();
			List<TableGrid> list=sortedChildrenMap.get(sequence);
			for (TableGrid tableGrid:list) {
				toReturn.add(tableGrid);
				toReturn.addAll(tableGrid.getSortedChildrenList());
			}			
		}
		return toReturn;
	}
	
	public void buildTree(DefaultMutableTreeNode parentNode) {
		Iterator<Integer> iterator=sortedChildrenMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer sequence=iterator.next();
			List<TableGrid> list=sortedChildrenMap.get(sequence);
			for (TableGrid tableGrid:list) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(tableGrid.getInventory().getName());
				parentNode.add(node);
				tableGrid.buildTree(node);
			}			
		}
	}
	
	public List<RecordTracker> getRecordTrackerList() {
		return this.recordTrackersList;
	}

}
