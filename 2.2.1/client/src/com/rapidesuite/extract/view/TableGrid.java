package com.rapidesuite.extract.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.extract.model.ExtractDataRow;
import com.rapidesuite.inject.gui.CustomHeaderRenderer;
import com.rapidesuite.inject.gui.ScriptsGrid;
import com.rapidesuite.inject.gui.TableHeaderRenderer;

public class TableGrid {

	private Inventory inventory;
	private List<ExtractDataRow> extractDataRows;
	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_CREATION_DATE="Creation Date";
	public static String COLUMN_HEADING_CREATED_BY="Created By";
	public static String COLUMN_HEADING_LAST_UPDATE_DATE="Last Update Date";
	public static String COLUMN_HEADING_LAST_UPDATED_BY="Last Updated By";

	private static int COLUMN_HEADING_ROW_NUM_WIDTH=50;
	private static int COLUMN_HEADING_INVENTORY_NAMES_WIDTH=120;
	
	private JTable table;
	private JFrame frame;
	private List<Field> fields;
	private String currentCellValueClicked;
	
	public TableGrid(Inventory inventory,List<ExtractDataRow> extractDataRows) throws Exception {
		this.inventory=inventory;
		this.extractDataRows=extractDataRows;
		fields=inventory.getFieldsUsedForDataEntry();
		createVariableTable();
	}
	
	public Vector<String> getAllColumns(){
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		List<Field> fields=inventory.getFieldsUsedForDataEntry();
		for (Field field:fields) {
			columnNames.add(field.getName());
		}
		columnNames.add(COLUMN_HEADING_CREATION_DATE);
		columnNames.add(COLUMN_HEADING_CREATED_BY);
		columnNames.add(COLUMN_HEADING_LAST_UPDATE_DATE);
		columnNames.add(COLUMN_HEADING_LAST_UPDATED_BY);
		
		return columnNames;
	}
	
	public void createVariableTable() throws Exception {
		Vector<String> columnNames = getAllColumns();
				
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		int rowIndex=0;
		for (ExtractDataRow extractDataRow:extractDataRows) {
			Vector<Object> row = new Vector<Object>();
			data.add(row);
			rowIndex++;
			row.add(Utils.formatNumberWithComma(rowIndex));
			for (Field field:fields) {
				int index=inventory.getIndexForColumn(field.getName());
				String[] dataValues=extractDataRow.getDataValues();
				row.add(dataValues[index]);
			}
			row.add(extractDataRow.getRscCreationDate());
			row.add(extractDataRow.getRscCreatedBy());
			row.add(extractDataRow.getRscLastUpdateDate());
			row.add(extractDataRow.getRscLastUpdatedBy());
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
				return false;
			}
			
			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int colIndex) {
				Component component= super.prepareRenderer(renderer, rowIndex, colIndex);
				
				if (rowIndex%2 == 0){
					component.setBackground(Color.WHITE);
                }
                else {
                	Color alternateColor =Color.decode("#dbdbdb");
                	component.setBackground(alternateColor);
                }

				return component;
			}

			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				try {
					tip = "<html>" +getValueAt(rowIndex, colIndex).toString()+"</html>";
				} 
				catch (RuntimeException e1) {
					//catch null pointer exception if mouse is over an empty line
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

		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(renderer);
		for (Field field:fields) {
			TableColumn column=table.getColumn(field.getName());
			column.setCellRenderer(renderer);
			if (field.getParentName()!=null && !field.getParentName().isEmpty()) {
				column.setHeaderRenderer(new CustomHeaderRenderer("Parent/ Child field",ScriptsGrid.yellowColor));
			}
		}
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    table.setRowSelectionAllowed(true);
		table.setSelectionForeground(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// THIS CORRUPT UTF-8 VALUES - THEY WILL SHOW AS SQUARES IN THE JTABLE CELLS.
		//table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
	    table.setBackground(Color.decode("#DBDBDB"));
	    table.setRowHeight(18);
	    JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 30));
		
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

	public JTable getTable() {
		return table;
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
		
	public static void setColumnSize(JTable table,List<String> columnNames) {
		TableColumn columnRowNum = table.getColumn(COLUMN_HEADING_ROW_NUM);
		columnRowNum.setMinWidth(COLUMN_HEADING_ROW_NUM_WIDTH);
		columnRowNum.setMaxWidth(COLUMN_HEADING_ROW_NUM_WIDTH);
		columnRowNum.setPreferredWidth(COLUMN_HEADING_ROW_NUM_WIDTH);
		columnRowNum.setHeaderRenderer(new TableHeaderRenderer());
		
		for (String columnName:columnNames) {
			TableColumn column=table.getColumn(columnName);
			column.setMinWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
			column.setPreferredWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
			column.setHeaderRenderer(new TableHeaderRenderer());
		}
	
		columnRowNum = table.getColumn(COLUMN_HEADING_CREATION_DATE);
		columnRowNum.setMinWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
		columnRowNum.setPreferredWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
		columnRowNum.setHeaderRenderer(new TableHeaderRenderer());
		
		columnRowNum = table.getColumn(COLUMN_HEADING_CREATED_BY);
		columnRowNum.setMinWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
		columnRowNum.setPreferredWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
		columnRowNum.setHeaderRenderer(new TableHeaderRenderer());
		
		columnRowNum = table.getColumn(COLUMN_HEADING_LAST_UPDATE_DATE);
		columnRowNum.setMinWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
		columnRowNum.setPreferredWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
		columnRowNum.setHeaderRenderer(new TableHeaderRenderer());
		
		columnRowNum = table.getColumn(COLUMN_HEADING_LAST_UPDATED_BY);
		columnRowNum.setMinWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
		columnRowNum.setPreferredWidth(COLUMN_HEADING_INVENTORY_NAMES_WIDTH);
		columnRowNum.setHeaderRenderer(new TableHeaderRenderer());
	}

	public List<ExtractDataRow> getExtractDataRows() {
		return extractDataRows;
	}

}