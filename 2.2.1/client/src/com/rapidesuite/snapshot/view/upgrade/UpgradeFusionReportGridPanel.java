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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
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

import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.snapshot.view.FilteringTable;
import com.rapidesuite.snapshot.view.RolloverMouseAdapter;
import com.rapidesuite.snapshot.view.SnapshotFilteringTable;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class UpgradeFusionReportGridPanel extends JPanel {

	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	public static String COLUMN_HEADING_STATUS="Status";
	public static String COLUMN_HEADING_REMARKS="Remarks";
	public static String COLUMN_HEADING_DOWNLOAD_TIME="Execution\nTime";
	public static String COLUMN_HEADING_RAW_TIME="Raw time\n(in Secs)";

	private static int COLUMN_HEADING_MIN_WIDTH=10;
	
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=200;
	private static int COLUMN_HEADING_STATUS_WIDTH=90;
	private static int COLUMN_HEADING_REMARKS_WIDTH=200;
	private static int COLUMN_HEADING_DOWNLOAD_TIME_WIDTH=100;
	private static int COLUMN_HEADING_RAW_TIME_WIDTH=70;

	protected JTable table;
	private FilteringTable filteringTable;
	protected List<FusionInventoryRow> fusionInventoryRowList;
	private String currentCellValueClicked;

	public UpgradeFusionReportGridPanel() {	
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

		filteringTable=new SnapshotFilteringTable(columnNames,variableScroll,table,33,null,true,false);
		centerPanel.add(filteringTable.getFixedScrollPane(),BorderLayout.NORTH);
		centerPanel.add(variableScroll,BorderLayout.CENTER);
	}
	
	public List<FusionInventoryRow> getFilteredRecordsList()
	{
		List<FusionInventoryRow> toReturn=new ArrayList<FusionInventoryRow>();
		for(int row = 0;row < table.getRowCount();row++) {
			int modelRowIndex=table.convertRowIndexToModel(row);
			FusionInventoryRow fusionInventoryRow=fusionInventoryRowList.get(modelRowIndex);
			toReturn.add(fusionInventoryRow);
		}
		return toReturn;
	}

	public void displayInventories(List<FusionInventoryRow> fusionInventoryRowList) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		filteringTable.removeRowFilter();
		tableModel.setRowCount(0);

		this.fusionInventoryRowList=fusionInventoryRowList;
		
		int counter=0;
		for (FusionInventoryRow fusionInventoryRow:fusionInventoryRowList) {
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
	
			rowGrid.add(fusionInventoryRow.getInventoryName());
			rowGrid.add(fusionInventoryRow.getReportStatus());
			rowGrid.add(fusionInventoryRow.getReportRemarks());
			rowGrid.add(fusionInventoryRow.getDownloadTime());
			long downloadRawTimeInSecs=fusionInventoryRow.getDownloadRawTimeInSecs();
			rowGrid.add(""+downloadRawTimeInSecs);

			tableModel.addRow(rowGrid);
		}
	}

	public void refreshGrid(final List<FusionInventoryRow> fusionInventoryRowList) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TableModel model = table.getModel();
				int gridIndex=0;
				for (FusionInventoryRow fusionInventoryRow:fusionInventoryRowList) {
					int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
					int colIndexDownloadTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_DOWNLOAD_TIME);
					int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
					int colIndexRawTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_RAW_TIME);

					try {
						String downloadTime=fusionInventoryRow.getDownloadTime();
						if (downloadTime==null) {
							model.setValueAt("",gridIndex,table.getColumnModel().getColumnIndex(COLUMN_HEADING_DOWNLOAD_TIME));
						}
						else {
							model.setValueAt(downloadTime,gridIndex,colIndexDownloadTime);
							String downloadRawTimeInSecs=""+fusionInventoryRow.getDownloadRawTimeInSecs();
							model.setValueAt(downloadRawTimeInSecs,gridIndex,colIndexRawTime);
						}					

						String reportRemarks=fusionInventoryRow.getReportRemarks();
						model.setValueAt(reportRemarks,gridIndex,colIndexRemarks);

						String reportStatus=fusionInventoryRow.getReportStatus();
						model.setValueAt(reportStatus,gridIndex,colIndexStatus);
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