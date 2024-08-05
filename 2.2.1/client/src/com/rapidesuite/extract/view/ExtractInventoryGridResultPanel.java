package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
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

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.model.ExtractDataRow;
import com.rapidesuite.extract.model.ExtractInventoryRecord;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ExecutionPanel;
import com.rapidesuite.snapshot.view.FilteringTable;
import com.rapidesuite.snapshot.view.GridButtonRenderer;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.RolloverMouseAdapter;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class ExtractInventoryGridResultPanel extends ExtractInventoryGridPanelGeneric {

	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_MODULE_NAME="Module Name";
	public static String COLUMN_HEADING_FORM_PATH="Menu Path";
	public static String COLUMN_HEADING_FORM_NAME="Task Name";
	public static String COLUMN_HEADING_FORM_TYPE="Task Level";
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	public static String COLUMN_HEADING_STATUS="Status";
	public static String COLUMN_HEADING_TOTAL_RECORDS_COUNT="Total\nRecords";
	public static String COLUMN_HEADING_REMARKS="Remarks";
	public static String COLUMN_HEADING_EXECUTION_TIME="Execution\ntime";
	public static String COLUMN_HEADING_RAW_TIME="Raw time\n(in Secs)";
	public static String COLUMN_HEADING_VIEW="View\nData";

	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_MODULE_NAME_WIDTH=80;
	private static int COLUMN_HEADING_FORM_PATH_WIDTH=90;
	private static int COLUMN_HEADING_FORM_NAME_WIDTH=160;
	private static int COLUMN_HEADING_FORM_TYPE_WIDTH=90;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=160;
	private static int COLUMN_HEADING_STATUS_WIDTH=80;
	private static int COLUMN_HEADING_REMARKS_WIDTH=180;
	private static int COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH=70;
	private static int COLUMN_HEADING_EXECUTION_TIME_WIDTH=80;
	private static int COLUMN_HEADING_RAW_TIME_WIDTH=70;
	private static int COLUMN_HEADING_VIEW_WIDTH=80;

	protected JTable table;
	protected List<ExtractInventoryRecord> extractInventoryRecordList;
	private String currentCellValueClicked;
	private ExtractFilteringGridTable extractFilteringGridTable;

	private JLabel catalogURLLabel;
	private JLabel reportURLLabel;
	private JLabel lastExecutionStartTimeLabel;
	private JLabel lastExecutionDownloadFolderLabel;
	private JLabel totalFailedRowsLabel;

	private JLabel catalogURLLabelValue;
	private JLabel reportURLLabelValue;
	private JLabel lastExecutionStartTimeLabelValue;
	private JLabel lastExecutionDownloadFolderLabelValue;
	private JLabel totalFailedRowsLabelValue;

	private final String LABEL_CATALOG_TEXT="BI Publisher Catalog URL:"; 
	private final String LABEL_REPORT_TEXT="BI Publisher Report URL:";
	public final static String LABEL_START_TIME_TEXT="Last execution Start Time:"; 
	public final static String LABEL_DATA_FOLDER_TEXT="Last execution Data ZIP File:"; 
	
	public ExtractInventoryGridResultPanel(ExtractMain extractMain) {
		super(extractMain);
		this.createComponents();
	}

	@SuppressWarnings("rawtypes")
	private void createComponents(){
		int labelsWidth=170;
		int height=14;

		JPanel tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(tempPanel);
		catalogURLLabel=new JLabel(LABEL_CATALOG_TEXT);
		catalogURLLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		tempPanel.add(catalogURLLabel);
		InjectUtils.assignArialPlainFont(catalogURLLabel,InjectMain.FONT_SIZE_NORMAL);
		catalogURLLabel.setForeground(Color.decode("#2F3436"));
		UIUtils.setDimension(catalogURLLabel,labelsWidth,height);
		catalogURLLabelValue=new JLabel();
		tempPanel.add(catalogURLLabelValue);
		InjectUtils.assignArialPlainFont(catalogURLLabelValue,InjectMain.FONT_SIZE_NORMAL);
		catalogURLLabelValue.setBackground(Color.decode("#2F3436"));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(tempPanel);
		reportURLLabel=new JLabel(LABEL_REPORT_TEXT);
		tempPanel.add(reportURLLabel);
		InjectUtils.assignArialPlainFont(reportURLLabel,InjectMain.FONT_SIZE_NORMAL);
		reportURLLabel.setForeground(Color.decode("#2F3436"));
		UIUtils.setDimension(reportURLLabel,labelsWidth,height);
		reportURLLabelValue=new JLabel();
		tempPanel.add(reportURLLabelValue);
		InjectUtils.assignArialPlainFont(reportURLLabelValue,InjectMain.FONT_SIZE_NORMAL);
		reportURLLabelValue.setBackground(Color.decode("#2F3436"));

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(tempPanel);
		lastExecutionDownloadFolderLabel=new JLabel(LABEL_DATA_FOLDER_TEXT);
		tempPanel.add(lastExecutionDownloadFolderLabel);
		InjectUtils.assignArialPlainFont(lastExecutionDownloadFolderLabel,InjectMain.FONT_SIZE_NORMAL);
		lastExecutionDownloadFolderLabel.setForeground(Color.decode("#2F3436"));
		UIUtils.setDimension(lastExecutionDownloadFolderLabel,labelsWidth,height);

		lastExecutionDownloadFolderLabelValue = new JLabel();
		tempPanel.add(lastExecutionDownloadFolderLabelValue);
		lastExecutionDownloadFolderLabelValue.setHorizontalAlignment(SwingConstants.LEFT);
		lastExecutionDownloadFolderLabelValue.setOpaque(false);		
		lastExecutionDownloadFolderLabelValue.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lastExecutionDownloadFolderLabelValue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (extractMain.getExtractMainPanel().getExecutionController()!=null) {
					extractMain.getExtractMainPanel().getExecutionController().openDownloadFolder();
				}
			}
		});

		tempPanel=new JPanel();
		tempPanel.setOpaque(false);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		northPanel.add(tempPanel);
		lastExecutionStartTimeLabel=new JLabel(LABEL_START_TIME_TEXT);
		tempPanel.add(lastExecutionStartTimeLabel);
		InjectUtils.assignArialPlainFont(lastExecutionStartTimeLabel,InjectMain.FONT_SIZE_NORMAL);
		lastExecutionStartTimeLabel.setForeground(Color.decode("#2F3436"));
		UIUtils.setDimension(lastExecutionStartTimeLabel,labelsWidth,height);
		lastExecutionStartTimeLabelValue=new JLabel();
		tempPanel.add(lastExecutionStartTimeLabelValue);
		InjectUtils.assignArialPlainFont(lastExecutionStartTimeLabelValue,InjectMain.FONT_SIZE_NORMAL);
		lastExecutionStartTimeLabelValue.setBackground(Color.decode("#2F3436"));

		Vector<String> columnNames = new Vector<String>();

		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_MODULE_NAME);
		columnNames.add(COLUMN_HEADING_FORM_PATH);
		columnNames.add(COLUMN_HEADING_FORM_NAME);
		columnNames.add(COLUMN_HEADING_INVENTORY_NAME);
		columnNames.add(COLUMN_HEADING_FORM_TYPE);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_REMARKS);
		columnNames.add(COLUMN_HEADING_EXECUTION_TIME);
		columnNames.add(COLUMN_HEADING_RAW_TIME);
		columnNames.add(COLUMN_HEADING_VIEW);

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();

		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked" })
			@Override
			public Class getColumnClass(int column) {
				return String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				try {
					String value=(String) getValueAt(rowIndex, colIndex);
					value=value.replaceAll("\n","<br/>");

					int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
					if (colIndex==colIndexRemarks) {
						tip="<html><p width=\"600\">"+value+"</p></html>";
					}
					else {
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
					component.setBackground(Color.WHITE);

					if (rowIndex%2 == 0){
						component.setBackground(Color.WHITE);
					}
					else {
						Color alternateColor =Color.decode("#dbdbdb");
						component.setBackground(alternateColor);
					}

					String status= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS));
					String totRecordsStr= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_COUNT));
//					System.out.println("rowIndex:"+rowIndex+" totRecordsStr:'"+totRecordsStr+"'");
					if (status!=null && status.equalsIgnoreCase(UIConstants.UI_STATUS_PROCESSING)) {
						component.setBackground(Color.decode("#fbf468"));
					}
					else 
						if (status!=null && (
								status.equalsIgnoreCase(UIConstants.UI_STATUS_FAILED) || status.equalsIgnoreCase(UIConstants.UI_STATUS_CANCELLED)) ) {
							component.setBackground(UIConstants.COLOR_RED);
						}
						else
							if ( (status!=null && status.equalsIgnoreCase(UIConstants.UI_STATUS_WARNING))
									||
									(totRecordsStr!=null && totRecordsStr.equals("0"))
									) {
								component.setBackground(Color.ORANGE);
							}
							else 
								if (status!=null && status.equalsIgnoreCase(UIConstants.UI_STATUS_COMPLETED)) {
									component.setBackground(Color.decode("#6bb23d"));
								}							
								else
									if (status!=null &&  (status.equalsIgnoreCase(UIConstants.UI_STATUS_PENDING) || status.isEmpty()) ) {
										if (rowIndex%2 == 0){
											component.setBackground(Color.WHITE);
										}
										else {
											Color alternateColor =Color.decode("#dbdbdb");
											component.setBackground(alternateColor);
										}     
									}

					return component;
				}
				catch(Exception e) {
					return component;
				}
			}
		};

		UIUtils.setColumnSize(table,COLUMN_HEADING_ROW_NUM,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_ROW_NUM_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_MODULE_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_MODULE_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_PATH,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_PATH_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_INVENTORY_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_INVENTORY_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_TYPE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_TYPE_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_STATUS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_STATUS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_REMARKS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_REMARKS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_EXECUTION_TIME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_EXECUTION_TIME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_RAW_TIME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_RAW_TIME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_VIEW_WIDTH,false);

		RolloverMouseAdapter rolloverAdapter = new RolloverMouseAdapter(table);
		table.addMouseListener(rolloverAdapter);
		table.addMouseMotionListener(rolloverAdapter);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		Border borderButton=BorderFactory.createEmptyBorder(2, 25, 0, 0);
		int width=26;
		height=14;
		ImageIcon ii=null;
		URL iconURL =null;

		GridButtonRenderer gridButtonRenderer=new GridButtonRenderer(borderButton,width,height,true,rolloverAdapter);
		JButton button=gridButtonRenderer.getButton();
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_view.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setIcon(ii);
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_view_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setRolloverIcon(new RolloverIcon(ii));
		table.getColumn(COLUMN_HEADING_VIEW).setCellRenderer(gridButtonRenderer);

		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_MODULE_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FORM_PATH).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FORM_TYPE).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FORM_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_INVENTORY_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_STATUS).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_REMARKS).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_EXECUTION_TIME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_RAW_TIME).setCellRenderer(centerRenderer);

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
					int colIndexView=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW);		
					if (column==colIndexView) {
						viewData(row);
					}
				}
			}
		});

		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
		table.setBackground(Color.decode("#DBDBDB"));
		table.setRowHeight(18);
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 80));

		JScrollPane variableScroll = new JScrollPane(table ,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};

		extractFilteringGridTable=new ExtractFilteringGridTable(this,columnNames,variableScroll,table,33,true,false);
		centerPanel.add(extractFilteringGridTable.getFixedScrollPane(),BorderLayout.NORTH);
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

		totalFailedRowsLabel=new JLabel(UIConstants.LABEL_GRID_TOTAL_FAILED_ROWS);
		southPanel.add(Box.createRigidArea(new Dimension(50, 15)));
		southPanel.add(totalFailedRowsLabel);
		InjectUtils.assignArialPlainFont(totalFailedRowsLabel,InjectMain.FONT_SIZE_NORMAL);
		totalFailedRowsLabel.setForeground(Color.decode("#2F3436"));

		totalFailedRowsLabelValue=new JLabel();
		southPanel.add(totalFailedRowsLabelValue);
		InjectUtils.assignArialPlainFont(totalFailedRowsLabelValue,InjectMain.FONT_SIZE_NORMAL);
		totalFailedRowsLabelValue.setForeground(Color.decode("#2F3436"));
		totalFailedRowsLabelValue.setOpaque(true);
		totalFailedRowsLabelValue.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
	}

	protected void viewData(int viewRow) {
		int modelRow=table.convertRowIndexToModel(viewRow);
		ExtractInventoryRecord extractInventoryRecord=extractInventoryRecordList.get(modelRow);

		String status=extractInventoryRecord.getStatus();
		if (!status.equalsIgnoreCase(UIConstants.UI_STATUS_COMPLETED)) {
			GUIUtils.popupErrorMessage("You cannot view the Data for that Inventory because of the status: '"+status+"'");
			return;
		}
		int totalRecords=extractInventoryRecord.getTotalRecords();
		if (totalRecords==0) {
			GUIUtils.popupInformationMessage("There is no Data to view");
			return;
		}
		try{
			Inventory inventory=extractInventoryRecord.getInventory();
			String dataSetIdentifier=extractInventoryRecord.getDataSet().getIdentifier();
			File tempDataFolder=extractMain.getExtractMainPanel().getExecutionController().getTempDataFolder(dataSetIdentifier);
			File serializedDataFile=new File(tempDataFolder,"DAS.xml");

			List<ExtractDataRow> extractDataRows=ExtractUtils.loadExtractDataRows(serializedDataFile);

			TableGrid tableGrid=new TableGrid(inventory,extractDataRows);
			TableGridFrame tableGridFrame=new TableGridFrame(extractMain,tableGrid);
			tableGridFrame.setVisible(true);
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage(e.getMessage());
		}
	}

	public void displayInventories(List<ExtractInventoryRecord> extractInventoryRecordList) {
		catalogURLLabelValue.setText(extractMain.getApplicationInfoPanel().getCatalogWebServiceInfo().getWebServiceEndpointUrl());
		reportURLLabelValue.setText(extractMain.getApplicationInfoPanel().getReportWebServiceInfo().getWebServiceEndpointUrl());

		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		extractFilteringGridTable.removeRowFilter();
		tableModel.setRowCount(0);

		totalFailedRowsLabelValue.setText("");
		lastExecutionStartTimeLabelValue.setText("");
		lastExecutionDownloadFolderLabelValue.setText("");

		this.extractInventoryRecordList=extractInventoryRecordList;
		gridRowsCountLabel.setText(UIConstants.LABEL_GRID_ROWS + extractInventoryRecordList.size());
		int resultGridIndex=0;
		for (ExtractInventoryRecord extractInventoryRecord:extractInventoryRecordList) {
			Vector<Object> rowGrid = new Vector<Object>();

			String formattedRowNumber="";
			try{
				extractInventoryRecord.setResultGridIndex(resultGridIndex++);
				int visibleRowSequence=extractInventoryRecord.getResultGridIndex()+1;
				formattedRowNumber=Utils.formatNumberWithComma(visibleRowSequence);
			}
			catch(Exception e) {
			}
			rowGrid.add(formattedRowNumber);
			rowGrid.add(extractInventoryRecord.getApplicationName());
			rowGrid.add(extractInventoryRecord.getFormPath());	
			rowGrid.add(extractInventoryRecord.getFormName());
			rowGrid.add(extractInventoryRecord.getInventoryName());
			rowGrid.add(extractInventoryRecord.getFormType().getLevel());
			rowGrid.add(extractInventoryRecord.getStatus());
			if (extractInventoryRecord.getTotalRecords()!=-1) {
				rowGrid.add(extractInventoryRecord.getTotalRecords());
			}
			else {
				rowGrid.add("");
			}
			rowGrid.add(extractInventoryRecord.getRemarks());
			rowGrid.add(extractInventoryRecord.getExecutionTime());
			rowGrid.add(extractInventoryRecord.getRawTimeInSecs());
			rowGrid.add("View");

			tableModel.addRow(rowGrid);
		}
		extractFilteringGridTable.applyFiltering();
	}	

	public List<ExtractInventoryRecord> getExtractInventoryRecordList() {
		return extractInventoryRecordList;
	}

	public List<ExtractInventoryRecord> getFilteredExtractInventoryGridRecordsList()
	{
		List<ExtractInventoryRecord> toReturn=new ArrayList<ExtractInventoryRecord>();
		for(int row = 0;row < table.getRowCount();row++) {
			int modelRowIndex=table.convertRowIndexToModel(row);
			ExtractInventoryRecord extractInventoryRecord=extractInventoryRecordList.get(modelRowIndex);
			toReturn.add(extractInventoryRecord);
		}
		return toReturn;
	}

	public FilteringTable getFilteringTable() {
		return extractFilteringGridTable;
	}

	public JTable getTable() {
		return table;
	}

	public void refreshGrid(final List<ExtractInventoryRecord> extractInventoryRecordList) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TableModel model = table.getModel();
				for (ExtractInventoryRecord extractInventoryRecord:extractInventoryRecordList) {
					int gridIndex=extractInventoryRecord.getResultGridIndex();
					int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
					int colIndexExecutionTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_EXECUTION_TIME);
					int colIndexTotalRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
					int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
					int colIndexRawTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_RAW_TIME);

					try {
						int totalRecords=extractInventoryRecord.getTotalRecords();
						if (totalRecords!=-1) {
							model.setValueAt(Utils.formatNumberWithComma(totalRecords),gridIndex,colIndexTotalRecords);
						}
						else {
							model.setValueAt("",gridIndex,colIndexTotalRecords);
						}

						String executionTime=extractInventoryRecord.getExecutionTime();
						if (executionTime==null) {
							model.setValueAt("",gridIndex,table.getColumnModel().getColumnIndex(COLUMN_HEADING_EXECUTION_TIME));
						}
						else {
							model.setValueAt(executionTime,gridIndex,colIndexExecutionTime);
							String rawTimeInSecs=""+extractInventoryRecord.getRawTimeInSecs();
							model.setValueAt(rawTimeInSecs,gridIndex,colIndexRawTime);
						}					

						String remarks=extractInventoryRecord.getRemarks();
						model.setValueAt(remarks,gridIndex,colIndexRemarks);

						String status=extractInventoryRecord.getStatus();
						model.setValueAt(status,gridIndex,colIndexStatus);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				//TODO: update total records extracted
				//snapshotInventoryTotalsPanel.updateTotalLabels();
				table.repaint();
			}
		});
	}

	public JLabel getLastExecutionStartTimeLabelValue() {
		return lastExecutionStartTimeLabelValue;
	}

	public JLabel getLastExecutionDownloadFolderComponent() {
		return lastExecutionDownloadFolderLabelValue;
	}

	public void updateTotalLabels() {
		int sumTotalRecords=0;
		int sumTotalFailedRows=0;
		for(int row = 0;row < getTable().getRowCount();row++) {
			int modelRowIndex=getTable().convertRowIndexToModel(row);
			ExtractInventoryRecord extractInventoryRecord=extractInventoryRecordList.get(modelRowIndex);
			if (extractInventoryRecord.getStatus().equals(UIConstants.UI_STATUS_FAILED)) {
				sumTotalFailedRows++;
			}
			if (extractInventoryRecord.getTotalRecords()==-1) {
				continue;
			}
			sumTotalRecords=sumTotalRecords+extractInventoryRecord.getTotalRecords();
		}
		String formattedSumTotalRecordsNumber="";
		try {
			formattedSumTotalRecordsNumber = Utils.formatNumberWithComma(sumTotalRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		String formattedSumTotalFailedRowsNumber="";
		try {
			formattedSumTotalFailedRowsNumber = Utils.formatNumberWithComma(sumTotalFailedRows);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		totalRecordsCountLabel.setText(UIConstants.LABEL_GRID_TOTAL_RECORDS+formattedSumTotalRecordsNumber);
		if (sumTotalFailedRows>0) {
			totalFailedRowsLabelValue.setBackground(UIConstants.COLOR_RED);
		}
		else {
			totalFailedRowsLabelValue.setBackground(ExecutionPanel.GREY_BACKGROUND_COLOR);
		}
		totalFailedRowsLabelValue.setText(" "+formattedSumTotalFailedRowsNumber+" ");
	}

	protected void saveGridToExcel() {
		String EXCEL_FILE_EXTENSION=".xlsx";
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File("RapidExtract-grid"+EXCEL_FILE_EXTENSION));
		fileChooser.setDialogTitle("Specify a file to save");   

		int userSelection = fileChooser.showSaveDialog(extractMain.getRootFrame());
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			int indexOf=fileToSave.getName().toLowerCase().indexOf(EXCEL_FILE_EXTENSION);
			File outputFile=fileToSave;
			if (indexOf==-1) {
				String newFileName=fileToSave.getAbsolutePath()+EXCEL_FILE_EXTENSION;
				outputFile=new File(newFileName);
			}
			try {
				List<ExtractInventoryRecord> filteredExtractInventoryRecordList=getFilteredExtractInventoryGridRecordsList();
				ExtractUtils.saveGridToExcel(outputFile,filteredExtractInventoryRecordList);
			}
			catch (Exception e) {
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage("Unable to save the grid to Excel: "+e.getMessage());
			}
		}
	}

}
