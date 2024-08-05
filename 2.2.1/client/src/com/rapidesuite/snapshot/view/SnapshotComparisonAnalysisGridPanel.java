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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.gui.ButtonEditor;

@SuppressWarnings("serial")
public class SnapshotComparisonAnalysisGridPanel extends JPanel {

	public static String COLUMN_HEADING_SELECTION="Selection";
	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_MODULE_NAME="Application\nName";
	public static String COLUMN_HEADING_FORM_PATH="Menu Path";
	public static String COLUMN_HEADING_FORM_NAME="Form Name";
	public static String COLUMN_HEADING_FORM_TYPE="Form Level";
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	public static String COLUMN_HEADING_FILTERING_RESULT="Filtering\nResult";
	public static String COLUMN_HEADING_VIEW="View\nData";
	
	public static String COLUMN_HEADING_FIRST_TOTAL_RECORDS_COUNT="Total Records\n";
	public static String COLUMN_HEADING_TOTAL_RECORDS_COUNT="Total Changes\n";
	
	private static int COLUMN_HEADING_SELECTION_WIDTH=60;
	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_MODULE_NAME_WIDTH=80;
	private static int COLUMN_HEADING_FORM_PATH_WIDTH=90;
	private static int COLUMN_HEADING_FORM_NAME_WIDTH=160;
	private static int COLUMN_HEADING_FORM_TYPE_WIDTH=90;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=160;
	private static int COLUMN_HEADING_FILTERING_RESULT_WIDTH=80;
	private static int COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH=150;
	private static int COLUMN_HEADING_VIEW_WIDTH=80;
	
	protected JTable table;
	private FilteringTable filteringTable;
	protected List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList;
	private SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame;
	private JLabel gridRowsCountLabel;
	private ComparisonInventoryTotalsPanel comparisonInventoryTotalsPanel;
	private String currentCellValueClicked;
	
	public SnapshotComparisonAnalysisGridPanel(SnapshotComparisonAnalysisFrame snapshotComparisonAnalysisFrame){
		this.snapshotComparisonAnalysisFrame=snapshotComparisonAnalysisFrame;
		setLayout(new BorderLayout());
		createComponents();
	}

	@SuppressWarnings("rawtypes")
	private void createComponents(){
		JPanel centerPanel=new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#343836"));
		add(centerPanel,BorderLayout.CENTER);
				
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_SELECTION);
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_MODULE_NAME);
		columnNames.add(COLUMN_HEADING_FORM_PATH);
		columnNames.add(COLUMN_HEADING_FORM_NAME);
		columnNames.add(COLUMN_HEADING_INVENTORY_NAME);
		columnNames.add(COLUMN_HEADING_FORM_TYPE);
		columnNames.add(COLUMN_HEADING_FILTERING_RESULT);
		columnNames.add(COLUMN_HEADING_VIEW);
		List<SnapshotGridRecord> snapshotGridRecords=snapshotComparisonAnalysisFrame.getSnapshotGridRecordsInDateOrder();
		SnapshotGridRecord snapshotGridRecord=snapshotGridRecords.get(0);
		String name=snapshotGridRecord.getName();	
		columnNames.add(COLUMN_HEADING_FIRST_TOTAL_RECORDS_COUNT+name);
		for (int i=1;i<snapshotGridRecords.size();i++) {
			snapshotGridRecord=snapshotGridRecords.get(i);
			name=snapshotGridRecord.getName();			
			columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_COUNT+name);
		}
				
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
					String filteringResult= (String) getValueAt(row, table.getColumnModel().getColumnIndex(COLUMN_HEADING_FILTERING_RESULT));
					if ( filteringResult.startsWith(UIConstants.FILTERED_OUT_PREFIX)) {
						return false;
					}
					return true;
				}
				return false;
			}
			
			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int vColIndex) {
				Component component =null;
				try{
					component = super.prepareRenderer(renderer, rowIndex, vColIndex);
					
					int colIndexSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
					if (vColIndex==colIndexSelection) {
						boolean isEnabled=true;
						String filteringResult= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_FILTERING_RESULT));
						if ( filteringResult.startsWith(UIConstants.FILTERED_OUT_PREFIX)) {	
							isEnabled=false;
						}
						component.setEnabled(isEnabled);
					}
					
				}
				catch(Exception e) {
					return null;
				}
				
				if (rowIndex%2 == 0){
					component.setBackground(Color.WHITE);
				}
				else {
					Color alternateColor =Color.decode("#dbdbdb");
					component.setBackground(alternateColor);
				}      

				if (isCellSelected(rowIndex, vColIndex)) {
					component.setBackground(UIConstants.COLOR_BLUE);
				} 

				return component;
			}

		};
	
		UIUtils.setColumnSize(table,COLUMN_HEADING_SELECTION,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_SELECTION_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_ROW_NUM,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_ROW_NUM_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_MODULE_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_MODULE_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_PATH,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_PATH_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_TYPE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_TYPE_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_INVENTORY_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_INVENTORY_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FILTERING_RESULT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FILTERING_RESULT_WIDTH,false);
		snapshotGridRecord=snapshotGridRecords.get(0);
		name=snapshotGridRecord.getName();	
		UIUtils.setColumnSize(table,COLUMN_HEADING_FIRST_TOTAL_RECORDS_COUNT+name,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH,false);
		for (int i=1;i<snapshotGridRecords.size();i++) {
			snapshotGridRecord=snapshotGridRecords.get(i);
			name=snapshotGridRecord.getName();			
			UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_COUNT+name,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH,false);
		}		
		UIUtils.setColumnSize(table,COLUMN_HEADING_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_VIEW_WIDTH,false);
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_MODULE_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FORM_PATH).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FORM_TYPE).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FORM_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FILTERING_RESULT).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_INVENTORY_NAME).setCellRenderer(leftRenderer);
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
					if (column==colIndexAction) {
						viewChanges( row);
					}
				}
			}
		});
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
		UIUtils.setPopupMenu(table,COLUMN_HEADING_SELECTION);
		
		ImageIcon ii=null;
		URL iconURL =null;

		RolloverMouseAdapter rolloverAdapter = new RolloverMouseAdapter(table);
		table.addMouseListener(rolloverAdapter);
		table.addMouseMotionListener(rolloverAdapter);
		
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
		
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
		table.setBackground(Color.decode("#DBDBDB"));
		table.setRowHeight(22);
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 30));
				
		JScrollPane variableScroll = new JScrollPane(table ,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};
		gridRowsCountLabel=new JLabel(UIConstants.LABEL_GRID_ROWS);
		comparisonInventoryTotalsPanel=new ComparisonInventoryTotalsPanel(this);
		filteringTable=new SnapshotFilteringTable(columnNames,variableScroll,table,33,comparisonInventoryTotalsPanel,false,
				snapshotComparisonAnalysisFrame.getTabSnapshotsPanel().getMainPanel().getTabOptionsPanel().isShowBalloons());
		centerPanel.add(filteringTable.getFixedScrollPane(),BorderLayout.NORTH);
		centerPanel.add(variableScroll,BorderLayout.CENTER);
		
		JPanel southPanel=new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setOpaque(true);
		add(southPanel,BorderLayout.SOUTH);
		
		southPanel.add(comparisonInventoryTotalsPanel);
	}
		
	public void displayInventories(List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		filteringTable.removeRowFilter();
		tableModel.setRowCount(0);
				
		this.snapshotInventoryGridRecordsList=snapshotInventoryGridRecordList;
		List<SnapshotGridRecord> snapshotGridRecords=snapshotComparisonAnalysisFrame.getSnapshotGridRecordsInDateOrder();
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
			Vector<Object> rowGrid = new Vector<Object>();

			String resultingResult=snapshotInventoryGridRecord.getFilteringResult();
			if ( resultingResult==null || resultingResult.startsWith(UIConstants.FILTERED_OUT_PREFIX)) {
				rowGrid.add(false);
			}
			else {
				rowGrid.add(true);
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
			rowGrid.add(snapshotInventoryGridRecord.getFormInformation().getFormattedFormPaths());	
			rowGrid.add(snapshotInventoryGridRecord.getFormInformation().getFormName());
			rowGrid.add(snapshotInventoryGridRecord.getInventoryName());
			rowGrid.add(snapshotInventoryGridRecord.getFormInformation().getFormType());
			rowGrid.add(snapshotInventoryGridRecord.getFilteringResult());
			rowGrid.add("Open");
			rowGrid.add(UIUtils.getFormattedNumber(snapshotInventoryGridRecord.getTotalRecords()));
			for (int i=1;i<snapshotGridRecords.size();i++) {
				int index=i-1;
				List<ComparisonChangesRecord> comparisonChangesRecordList=snapshotInventoryGridRecord.getComparisonChangesRecordList();
				if (comparisonChangesRecordList.isEmpty()) {
					rowGrid.add("0");
				}
				else {
					int size=comparisonChangesRecordList.size();
					if ( index < size) {
						ComparisonChangesRecord comparisonChangesRecord=comparisonChangesRecordList.get(index);
						rowGrid.add(UIUtils.getFormattedNumber(comparisonChangesRecord.getTotalChanges()));	
					}
					else {
						rowGrid.add("0");
					}
				}
			}			
			tableModel.addRow(rowGrid);
		}
		comparisonInventoryTotalsPanel.updateTotalLabels();
	}
	
	public List<SnapshotInventoryGridRecord> getSnapshotInventoryGridRecordsList() {
		return snapshotInventoryGridRecordsList;
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


	public FilteringTable getFilteringTable() {
		return filteringTable;
	}
	
	public void viewChanges(int viewRow) {
		snapshotComparisonAnalysisFrame.viewChanges(viewRow);
	}

	public JLabel getGridRowsCountLabel() {
		return gridRowsCountLabel;
	}

	public JTable getTable() {
		return table;
	}
	
}
