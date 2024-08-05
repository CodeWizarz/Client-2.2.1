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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public class SnapshotDownloadGridPanel extends JPanel {

	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	public static String COLUMN_HEADING_STATUS="Status";
	public static String COLUMN_HEADING_TOTAL_DOWNLOADED_RECORDS_COUNT="Processed\nRecords";
	public static String COLUMN_HEADING_TOTAL_RECORDS_COUNT="Total\nRecords";
	public static String COLUMN_HEADING_REMARKS="Remarks";
	public static String COLUMN_HEADING_DOWNLOAD_TIME="Processing\nTime";
	public static String COLUMN_HEADING_RAW_TIME="Raw time\n(in Secs)";

	private static int COLUMN_HEADING_MIN_WIDTH=10;
	
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=200;
	private static int COLUMN_HEADING_STATUS_WIDTH=90;
	private static int COLUMN_HEADING_TOTAL_DOWNLOADED_RECORDS_COUNT_WIDTH=90;
	private static int COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH=100;
	private static int COLUMN_HEADING_REMARKS_WIDTH=200;
	private static int COLUMN_HEADING_DOWNLOAD_TIME_WIDTH=100;
	private static int COLUMN_HEADING_RAW_TIME_WIDTH=70;

	private TabSnapshotsPanel tabSnapshotsPanel;
	protected JTable table;
	private FilteringTable filteringTable;
	protected List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList;
	private String currentCellValueClicked;
	private SnapshotInventoryTotalsPanel downloadTotalsPanel;

	public SnapshotDownloadGridPanel(TabSnapshotsPanel tabSnapshotsPanel) {
		this.tabSnapshotsPanel=tabSnapshotsPanel;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		createComponents();
	}

	@SuppressWarnings("rawtypes")
	private void createComponents(){
		JPanel centerPanel=new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		add(centerPanel,BorderLayout.CENTER);

		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_INVENTORY_NAME);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_TOTAL_DOWNLOADED_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_REMARKS);
		columnNames.add(COLUMN_HEADING_DOWNLOAD_TIME);
		columnNames.add(COLUMN_HEADING_RAW_TIME);
	
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

					String status= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS));				
					component.setBackground(Color.WHITE);


					if (rowIndex%2 == 0){
						component.setBackground(Color.WHITE);
					}
					else {
						Color alternateColor =Color.decode("#dbdbdb");
						component.setBackground(alternateColor);
					}      

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
	
		UIUtils.setColumnSize(table,COLUMN_HEADING_ROW_NUM,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_ROW_NUM_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_INVENTORY_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_INVENTORY_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_STATUS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_STATUS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_DOWNLOADED_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_DOWNLOADED_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_REMARKS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_REMARKS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_DOWNLOAD_TIME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_DOWNLOAD_TIME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_RAW_TIME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_RAW_TIME_WIDTH,false);
		
		RolloverMouseAdapter rolloverAdapter = new RolloverMouseAdapter(table);
		table.addMouseListener(rolloverAdapter);
		table.addMouseMotionListener(rolloverAdapter);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_INVENTORY_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_STATUS).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_DOWNLOADED_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_REMARKS).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_DOWNLOAD_TIME).setCellRenderer(centerRenderer);
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

		downloadTotalsPanel=new DownloadTotalsPanel(this);
		downloadTotalsPanel.getPanel().setBackground(Color.decode("#343836"));
		InjectUtils.assignArialPlainFont(downloadTotalsPanel.getGridRowsCountLabel(),InjectMain.FONT_SIZE_NORMAL);
		downloadTotalsPanel.getGridRowsCountLabel().setForeground(Color.white);
		
		filteringTable=new SnapshotFilteringTable(columnNames,variableScroll,table,33,downloadTotalsPanel,true,
				tabSnapshotsPanel.getMainPanel().getTabOptionsPanel().isShowBalloons());
		centerPanel.add(filteringTable.getFixedScrollPane(),BorderLayout.NORTH);
		centerPanel.add(variableScroll,BorderLayout.CENTER);
		
		JPanel southPanel=new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#343836"));
		add(southPanel,BorderLayout.SOUTH);
		southPanel.add(downloadTotalsPanel);
	}
	
	public List<SnapshotInventoryGridRecord> getFilteredSnapshotInventoryGridRecordsList()
	{
		List<SnapshotInventoryGridRecord> toReturn=new ArrayList<SnapshotInventoryGridRecord>();
		for(int row = 0;row < table.getRowCount();row++) {
			int modelRowIndex=table.convertRowIndexToModel(row);
			SnapshotInventoryGridRecord snapshotInventoryGridRecord=snapshotInventoryGridRecordList.get(modelRowIndex);
			toReturn.add(snapshotInventoryGridRecord);
		}
		return toReturn;
	}

	protected void saveGridToExcel() {
		String EXCEL_FILE_EXTENSION=".xlsx";
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File("RAPIDSnapshot-grid"+EXCEL_FILE_EXTENSION));
		fileChooser.setDialogTitle("Specify a file to save");   

		int userSelection = fileChooser.showSaveDialog(tabSnapshotsPanel.
				getMainPanel().getSnapshotMain().getRootFrame());
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			int indexOf=fileToSave.getName().toLowerCase().indexOf(EXCEL_FILE_EXTENSION);
			File outputFile=fileToSave;
			if (indexOf==-1) {
				String newFileName=fileToSave.getAbsolutePath()+EXCEL_FILE_EXTENSION;
				outputFile=new File(newFileName);
			}
			try {
				ModelUtils.saveDownloadGridToExcel(outputFile,getFilteredSnapshotInventoryGridRecordsList());
			}
			catch (Exception e) {
				FileUtils.printStackTrace(e);
				GUIUtils.popupErrorMessage("Unable to save the grid to Excel: "+e.getMessage());
			}
		}
	}

	public void displayInventories(List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		filteringTable.removeRowFilter();
		tableModel.setRowCount(0);

		this.snapshotInventoryGridRecordList=snapshotInventoryGridRecordList;
		final String text=UIConstants.LABEL_GRID_ROWS + UIUtils.getFormattedNumber(snapshotInventoryGridRecordList.size());
		downloadTotalsPanel.getGridRowsCountLabel().setText(text);
		
		int counter=0;
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
			Vector<Object> rowGrid = new Vector<Object>();
			counter++;
			
			String formattedRowNumber="";
			try{
				int visibleRowSequence=counter;
				formattedRowNumber=Utils.formatNumberWithComma(visibleRowSequence);
			}
			catch(Exception e) {
			}
			rowGrid.add(formattedRowNumber);
	
			rowGrid.add(snapshotInventoryGridRecord.getInventoryName());
			rowGrid.add(snapshotInventoryGridRecord.getDownloadStatus());
			rowGrid.add(UIUtils.getFormattedNumber(snapshotInventoryGridRecord.getDownloadDownloadedRecordsCount()));
			rowGrid.add(UIUtils.getFormattedNumber(snapshotInventoryGridRecord.getDownloadTotalRecordsCount()));
			rowGrid.add(snapshotInventoryGridRecord.getDownloadRemarks());
			rowGrid.add(snapshotInventoryGridRecord.getDownloadTime());
			long downloadRawTimeInSecs=snapshotInventoryGridRecord.getDownloadRawTimeInSecs();
			rowGrid.add(""+downloadRawTimeInSecs);

			tableModel.addRow(rowGrid);
		}
	}

	public void refreshGrid(final List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TableModel model = table.getModel();
				int gridIndex=0;
				for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
					int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
					int colIndexDownloadTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_DOWNLOAD_TIME);
					int colIndexTotalRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
					int colIndexTotalDownloadedRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_DOWNLOADED_RECORDS_COUNT);
					int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
					int colIndexRawTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_RAW_TIME);

					try {						
						int downloadDownloadedRecordsCount=snapshotInventoryGridRecord.getDownloadDownloadedRecordsCount();
						model.setValueAt(Utils.formatNumberWithComma(downloadDownloadedRecordsCount),gridIndex,colIndexTotalDownloadedRecords);

						int downloadTotalRecordsCount=snapshotInventoryGridRecord.getDownloadTotalRecordsCount();
						model.setValueAt(Utils.formatNumberWithComma(downloadTotalRecordsCount),gridIndex,colIndexTotalRecords);
						
						String downloadTime=snapshotInventoryGridRecord.getDownloadTime();
						if (downloadTime==null) {
							model.setValueAt("",gridIndex,table.getColumnModel().getColumnIndex(COLUMN_HEADING_DOWNLOAD_TIME));
						}
						else {
							model.setValueAt(downloadTime,gridIndex,colIndexDownloadTime);
							String downloadRawTimeInSecs=""+snapshotInventoryGridRecord.getDownloadRawTimeInSecs();
							model.setValueAt(downloadRawTimeInSecs,gridIndex,colIndexRawTime);
						}					

						String downloadRemarks=snapshotInventoryGridRecord.getDownloadRemarks();
						model.setValueAt(downloadRemarks,gridIndex,colIndexRemarks);

						String downloadStatus=snapshotInventoryGridRecord.getDownloadStatus();
						model.setValueAt(downloadStatus,gridIndex,colIndexStatus);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					gridIndex++;
				}
				table.repaint();
			}
		});
	}
	
}