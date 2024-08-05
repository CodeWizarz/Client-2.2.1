package com.rapidesuite.extract.view;

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
import java.util.Set;
import java.util.Vector;

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
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.extract.model.ExtractInventoryRecord;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.snapshot.view.FilteringTable;
import com.rapidesuite.snapshot.view.RolloverMouseAdapter;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class ExtractInventoryGridSelectionPanel extends ExtractInventoryGridPanelGeneric {

	public static String COLUMN_HEADING_SELECTION="Selection";
	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_MODULE_NAME="Module Name";
	public static String COLUMN_HEADING_FORM_PATH="Menu Path";
	public static String COLUMN_HEADING_FORM_NAME="Task Name";
	public static String COLUMN_HEADING_FORM_TYPE="Task Level";
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	
	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_SELECTION_WIDTH=60;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_MODULE_NAME_WIDTH=160;
	private static int COLUMN_HEADING_FORM_PATH_WIDTH=150;
	private static int COLUMN_HEADING_FORM_NAME_WIDTH=340;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=315;
	private static int COLUMN_HEADING_FORM_TYPE_WIDTH=100;

	protected JTable table;
	protected List<ExtractInventoryRecord> extractInventoryRecordList;
	private String currentCellValueClicked;
	private ExtractFilteringGridTable extractFilteringGridTable;
	private boolean isExecutionStarted;
	private FilterGeneralPanel filterGeneralPanel;
	private FiltersPanel filtersPanel;
	private TemplatePanel templatePanel;

	public ExtractInventoryGridSelectionPanel(ExtractMain extractMain) {
		super(extractMain);
		this.createComponents();
	}
	
	public void setIsExecutionStarted(boolean isExecutionStarted) {
		this.isExecutionStarted=isExecutionStarted;
	}

	@SuppressWarnings("rawtypes")
	private void createComponents(){
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_SELECTION);
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_MODULE_NAME);
		columnNames.add(COLUMN_HEADING_FORM_PATH);
		columnNames.add(COLUMN_HEADING_FORM_NAME);
		columnNames.add(COLUMN_HEADING_INVENTORY_NAME);
		columnNames.add(COLUMN_HEADING_FORM_TYPE);
	
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
					return !isExecutionStarted;
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
					int colFormPathIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_FORM_PATH);
					int colFormNameIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_FORM_NAME);
					int colInventoryNameIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_INVENTORY_NAME);

					if  (colIndex==colApplicationNameIndex ||
							colIndex==colFormPathIndex ||
							colIndex==colFormNameIndex ||
							colIndex==colInventoryNameIndex
							) {
						
						String tableIdText="";
						if  (colIndex==colInventoryNameIndex) {
							//int modelRow=table.convertRowIndexToModel(rowIndex);
							//ExtractInventoryRecord extractInventoryRecord=extractInventoryRecordList.get(modelRow);
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
					component.setBackground(Color.WHITE);

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
				catch(Exception e) {
					return component;
				}
			}
		};
				
		UIUtils.setColumnSize(table,COLUMN_HEADING_SELECTION,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_SELECTION_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_ROW_NUM,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_ROW_NUM_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_MODULE_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_MODULE_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_PATH,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_PATH_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_TYPE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_TYPE_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_INVENTORY_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_INVENTORY_NAME_WIDTH,false);
	
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
		table.getColumn(COLUMN_HEADING_FORM_TYPE).setCellRenderer(leftRenderer);
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
		UIUtils.setPopupMenu(table,COLUMN_HEADING_SELECTION);
		
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
		
		filtersPanel =new FiltersPanel(extractMain.getRootFrame(),this);
		northPanel.add(filtersPanel);
	}

	public void displayInventories(List<ExtractInventoryRecord> extractInventoryRecordList) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		extractFilteringGridTable.removeRowFilter();
		tableModel.setRowCount(0);
		isExecutionStarted=false;
		this.extractInventoryRecordList=extractInventoryRecordList;
		gridRowsCountLabel.setText(UIConstants.LABEL_GRID_ROWS + extractInventoryRecordList.size());
		for (ExtractInventoryRecord extractInventoryRecord:extractInventoryRecordList) {
			Vector<Object> rowGrid = new Vector<Object>();

			rowGrid.add(true);
			String formattedRowNumber="";
			try{
				int visibleRowSequence=extractInventoryRecord.getSelectionGridIndex()+1;
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
		
			tableModel.addRow(rowGrid);
		}
		extractFilteringGridTable.applyFiltering();
	}
	
	public void loadFilterPanel() {	
		if(templatePanel == null) {
			templatePanel =new TemplatePanel(extractMain,this);
		}
		northPanel.add(templatePanel);		
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
	
	public List<ExtractInventoryRecord> getSelectedExtractInventoryGridRecordsList()
	{
		TableModel model = table.getModel();
		int numRows = model.getRowCount();
		List<ExtractInventoryRecord> toReturn=new ArrayList<ExtractInventoryRecord>();
		int colIndex=table.getColumnModel().getColumnIndex(COLUMN_HEADING_SELECTION);
		for (int i=0; i < numRows; i++) {
			boolean isSelected=(boolean) model.getValueAt(i,colIndex);
			if (isSelected) {
				ExtractInventoryRecord extractInventoryRecord=extractInventoryRecordList.get(i);
				toReturn.add(extractInventoryRecord);
			}
		}
		return toReturn;
	}

	public FilteringTable getFilteringTable() {
		return extractFilteringGridTable;
	}

	public JTable getTable() {
		return table;
	}

	@Override
	public void updateTotalLabels() {
	}

	public FiltersPanel getFiltersPanel() {
		return filtersPanel;
	}

	public FilterGeneralPanel getFilterGeneralPanel() {
		return filterGeneralPanel;
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

}
