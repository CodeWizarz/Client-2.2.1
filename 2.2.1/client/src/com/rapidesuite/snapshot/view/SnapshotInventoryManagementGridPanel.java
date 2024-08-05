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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import com.rapidesuite.inject.InjectUtils;

@SuppressWarnings("serial")
public class SnapshotInventoryManagementGridPanel extends JPanel {

	public static String COLUMN_HEADING_SELECTION="Selection";
	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_MODULE_NAME="Application\nNames";
	public static String COLUMN_HEADING_FORM_PATH="Menu Paths";
	public static String COLUMN_HEADING_FORM_NAME="Form Name";	
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	public static String COLUMN_HEADING_EXECUTABLE="Executable";
	public static String COLUMN_HEADING_LISTABLE="Retrievable";
	
	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_SELECTION_WIDTH=60;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=300;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_EXECUTABLE_WIDTH=120;
	private static int COLUMN_HEADING_LISTABLE_WIDTH=120;
	private static int COLUMN_HEADING_MODULE_NAME_WIDTH=160;
	private static int COLUMN_HEADING_FORM_PATH_WIDTH=90;
	private static int COLUMN_HEADING_FORM_NAME_WIDTH=160;	
	
	
	private SnapshotInventoryGridFrame snapshotInventoryGridFrame;
	protected JTable table;
	private FilteringTable filteringTable;
	protected List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordsList;
	private boolean hasSelectionColumn = true;
	private SnapshotInventoryTotalsPanel snapshotInventoryTotalsPanel;
	private String currentCellValueClicked;
	boolean isRefreshEveryListOnUse;

	public SnapshotInventoryManagementGridPanel(SnapshotInventoryGridFrame snapshotInventoryGridFrame,boolean isShowBalloons) {
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Color.decode("#dbdcdf"));
		setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		this.snapshotInventoryGridFrame=snapshotInventoryGridFrame;
		createComponents(isShowBalloons,30);
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
		columnNames.add(COLUMN_HEADING_FORM_PATH);
		columnNames.add(COLUMN_HEADING_FORM_NAME);	
		columnNames.add(COLUMN_HEADING_INVENTORY_NAME);
		columnNames.add(COLUMN_HEADING_EXECUTABLE);
		columnNames.add(COLUMN_HEADING_LISTABLE);		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();

		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked" })
			@Override
			public Class getColumnClass(int column) {
				int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				int colExecutableIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_EXECUTABLE);
				int colListableIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_LISTABLE);				
				if (column==colSelectionIndex || column==colExecutableIndex || column==colListableIndex ) {
					return Boolean.class;
				}
				return String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				int colSelectionIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
				int colExecutableIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_EXECUTABLE);
				int colListableIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_LISTABLE);
				
				if (column==colSelectionIndex || column==colExecutableIndex || column==colListableIndex) {
					return true;
				}
				return false;
			}


			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int vColIndex) {
				Component component =null;
				try{
					component = super.prepareRenderer(renderer, rowIndex, vColIndex);

					int colIndexSelection=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);		
					component.setBackground(Color.WHITE);
					if (vColIndex==colIndexSelection) {
						boolean isEnabled=true;
						component.setEnabled(isEnabled);
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
		
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_PATH,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_PATH_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_INVENTORY_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_INVENTORY_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_EXECUTABLE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_EXECUTABLE_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_LISTABLE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_LISTABLE_WIDTH,false);
	
		RolloverMouseAdapter rolloverAdapter = new RolloverMouseAdapter(table);
		table.addMouseListener(rolloverAdapter);
		table.addMouseMotionListener(rolloverAdapter);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_MODULE_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FORM_PATH).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_FORM_NAME).setCellRenderer(leftRenderer);
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
		table.setRowHeight(22);
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 80));

		JScrollPane variableScroll = new JScrollPane(table ,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};

		snapshotInventoryTotalsPanel=new ViewerSnapshotInventoryManagementTotalsPanel(this,30);
		boolean isRefreshEveryListOnUse = true;
		filteringTable=new SnapshotFilteringTable(columnNames,variableScroll,table,33,snapshotInventoryTotalsPanel,isRefreshEveryListOnUse,
				isShowBalloons);
		centerPanel.add(filteringTable.getFixedScrollPane(),BorderLayout.NORTH);
		centerPanel.add(variableScroll,BorderLayout.CENTER);
		
	
		if (hasSelectionColumn) {
			String[] columnsCanBeSelected = {COLUMN_HEADING_EXECUTABLE,COLUMN_HEADING_LISTABLE};
			UIUtils.setPopupMenuDymanicColumn(table,columnsCanBeSelected,COLUMN_HEADING_SELECTION);
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
		}
		
		JPanel southPanel=new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		add(southPanel,BorderLayout.SOUTH);
		southPanel.add(snapshotInventoryTotalsPanel);
	}
	public void displayInventories(List<SnapshotInventoryGridRecord> snapshotInventoryGridRecordList) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		filteringTable.removeRowFilter();
		tableModel.setRowCount(0);

		this.snapshotInventoryGridRecordsList=snapshotInventoryGridRecordList;
		for (SnapshotInventoryGridRecord snapshotInventoryGridRecord:snapshotInventoryGridRecordList) {
			Vector<Object> rowGrid = new Vector<Object>();
			String filteringResult=snapshotInventoryGridRecord.getFilteringResult();
			if (filteringResult==null) {
				filteringResult="";
			}
			rowGrid.add(true);

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
			if(snapshotInventoryGridRecord.isExecutable()){
				rowGrid.add(true);
			}else{
				rowGrid.add(false);
			}
			if(snapshotInventoryGridRecord.isListable()){
				rowGrid.add(true);
			}else{
				rowGrid.add(false);
			}
			rowGrid.add(true);
			tableModel.addRow(rowGrid);
		}
		filteringTable.applyFiltering();
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

	public List<SnapshotInventoryGridRecord> getFilteredSnapshotInventoryGridRecordsList(){
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

	public JTable getTable() {
		return table;
	}

	public SnapshotInventoryTotalsPanel getSnapshotInventoryTotalsPanel() {
		return snapshotInventoryTotalsPanel;
	}


	public SnapshotInventoryGridFrame getSnapshotInventoryGridFrame() {
		return snapshotInventoryGridFrame;
	}
	
	
}
