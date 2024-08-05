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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.MultiLineHeaderRenderer;
import com.rapidesuite.snapshot.view.RolloverMouseAdapter;
import com.rapidesuite.snapshot.view.SnapshotFilteringTable;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;
import com.rapidesuite.snapshot.view.convert.ConvertDataGridTotalsPanel;

@SuppressWarnings("serial")
public class UpgradeDataGrid extends JFrame  {

	private Inventory inventory;
	private JTable table;
	private List<DataRow> dataRows;
	protected String currentCellValueClicked;
	private SnapshotFilteringTable filteringTable;

	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_TARGET_STATUS="Record Status";

	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=50;
	private static int COLUMN_HEADING_STATUS_WIDTH=100;
	private static int COLUMN_HEADING_INVENTORY_NAMES_WIDTH=120;
	private static int FRAME_WIDTH=1000;
	private static int FRAME_HEIGHT=600;

	private static String RECORD_STATUS_MISSING_REQUIRED_DATA="Missing Required Data";
	private static String RECORD_STATUS_INVALID_VALUES="Invalid Values";
	private static String RECORD_STATUS_VALID="Valid";
	
	private static String RSC_HEADER_LAST_UPDATE_NAME="RSC last updated by name";
	private static String RSC_HEADER_LAST_UPDATE_DATE="RSC last update date";
	private static String RSC_HEADER_CREATED_NAME="RSC Created by name";
	private static String RSC_HEADER_CREATION_DATE="RSC Creation date";
	private static int COLUMN_HEADING_RSC_WIDTH=160;
	
	protected JPanel northPanel;
	protected JPanel centerPanel;
	protected JPanel southPanel;
	private ConvertDataGridTotalsPanel convertDataGridTotalsPanel;
	private Map<Integer, Set<String>> columnIndexToSubstitutionKeysSetMap;
	private Set<String> columnNamesToIgnore;
	private Map<String, Set<String>> targetInventoryNameToMandatoryColumnNamesToIgnoreMap;

	public UpgradeDataGrid(JFrame rootFrame,Inventory inventory,List<DataRow> dataRows,boolean isSourcePanel,
			Map<String, Set<String>> targetInventoryNameToMandatoryColumnNamesToIgnoreMap) throws Exception {
		String title=inventory.getName();
		this.setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
		this.setTitle(title);
		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		UIUtils.setFramePosition(rootFrame,this);
		this.setLayout(new BorderLayout());
		this.inventory=inventory;
		this.dataRows=dataRows;
		columnIndexToSubstitutionKeysSetMap=new HashMap<Integer, Set<String>>();
		this.targetInventoryNameToMandatoryColumnNamesToIgnoreMap=targetInventoryNameToMandatoryColumnNamesToIgnoreMap;
		createComponents(isSourcePanel);
	}

	@SuppressWarnings("rawtypes")
	private void createComponents(final boolean isSourcePanel) throws Exception{
		northPanel=InjectUtils.getXPanel(Component.CENTER_ALIGNMENT);
		northPanel.setOpaque(true);
		northPanel.setBackground(Color.decode("#dbdcdf"));
		add(northPanel,BorderLayout.NORTH);

		centerPanel=new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setOpaque(true);
		centerPanel.setBackground(Color.decode("#dbdcdf"));
		add(centerPanel,BorderLayout.CENTER);

		southPanel=new JPanel();
		southPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setOpaque(true);
		southPanel.setBackground(Color.decode("#dbdcdf"));
		add(southPanel,BorderLayout.SOUTH);

		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		if (!isSourcePanel) {
			columnNames.add(COLUMN_HEADING_TARGET_STATUS);
		}

		final List<Field> fields=inventory.getFieldsUsedForDataEntry();
		int colIndex=0;
		for (Field field:fields) {
			columnNames.add(field.getName());
			Set<String> substitutionKeysSet=ModelUtils.getSubstitutionKeysSet(field.getSubstitution());
			columnIndexToSubstitutionKeysSetMap.put(colIndex, substitutionKeysSet);
			colIndex++;
		}
		if (isSourcePanel) {
			columnNames.add(RSC_HEADER_LAST_UPDATE_NAME);
			columnNames.add(RSC_HEADER_LAST_UPDATE_DATE);
			columnNames.add(RSC_HEADER_CREATED_NAME);
			columnNames.add(RSC_HEADER_CREATION_DATE);
		}
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		int rowIndex=0;

		if (!isSourcePanel) {
			columnNamesToIgnore=targetInventoryNameToMandatoryColumnNamesToIgnoreMap.get(inventory.getName());
		}
		for (DataRow dataRow:dataRows) {
			Vector<Object> row = new Vector<Object>();
			data.add(row);

			rowIndex++;
			row.add(""+rowIndex);

			if (!isSourcePanel){
				row.add("");
			}
			boolean hasAtLeastOneColumnsWithoutDataButRequired=false;
			boolean hasAtLeastOneColumnsWithInvalidValue=false;
			colIndex=0;

			String[] dataValuesArray=dataRow.getDataValues();
			for (Field field:fields) {
				String sourceValue=dataValuesArray[colIndex];
				if (sourceValue==null || sourceValue.isEmpty() ) {
					if (!isSourcePanel && field.getMandatory() && (columnNamesToIgnore==null || !columnNamesToIgnore.contains(field.getName())) ) {
						hasAtLeastOneColumnsWithoutDataButRequired=true;
					}
				}

				Set<String> substitutionKeysSet=columnIndexToSubstitutionKeysSetMap.get(colIndex);
				if (substitutionKeysSet!=null && !substitutionKeysSet.isEmpty() && sourceValue!=null && !sourceValue.isEmpty()) {
					boolean hasValue=substitutionKeysSet.contains(sourceValue);
					if (!hasValue) {
						hasAtLeastOneColumnsWithInvalidValue=true;
					}
				}
				if (sourceValue==null)  {
					sourceValue="";
				}
				row.add(sourceValue);
				colIndex++;
			}
			String rowStatus="";
			if (hasAtLeastOneColumnsWithoutDataButRequired) {
				rowStatus=RECORD_STATUS_MISSING_REQUIRED_DATA;
			}
			if (hasAtLeastOneColumnsWithInvalidValue) {
				if (rowStatus.isEmpty()) {
					rowStatus=RECORD_STATUS_INVALID_VALUES;
				}
				else {
					rowStatus=rowStatus+" | "+RECORD_STATUS_INVALID_VALUES;
				}
			}
			if (rowStatus.isEmpty()) {
				rowStatus=RECORD_STATUS_VALID;
			}
			if (!isSourcePanel){
				row.set(1, rowStatus);
			}
			if (isSourcePanel) {
				String rscLastUpdatedByName=dataRow.getRscLastUpdatedByName();
				row.add(rscLastUpdatedByName);
				String rscLastUpdateDate=dataRow.getRscLastUpdateDate();
				row.add(rscLastUpdateDate);
				String rscCreatedByName=dataRow.getRscCreatedByName();
				row.add(rscCreatedByName);
				String rscCreationDate=dataRow.getRscCreationDate();
				row.add(rscCreationDate);
			}
		}

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
				if (isSourcePanel){
					return null;
				}
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				try {
					String value=(String) getValueAt(rowIndex, colIndex);
					int inventoryColIndex=colIndex-2;
					if (inventoryColIndex>=0) {
						Set<String> substitutionKeysSet=columnIndexToSubstitutionKeysSetMap.get(inventoryColIndex);
						if (substitutionKeysSet!=null && !substitutionKeysSet.isEmpty()) {
							tip="<html>Allowed values: "+substitutionKeysSet+"</html>";
						}
						else {
							value=value.replaceAll("\n","<br>");
							tip="<html>"+value+"</html>";
						}
					}
					else {						
						value=value.replaceAll("\n","<br>");
						tip="<html>"+value+"</html>";
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				return tip;
			}

			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int colIndex) {
				Component component =null;
				try{
					component = super.prepareRenderer(renderer, rowIndex, colIndex);

					if (rowIndex%2 == 0){
						component.setBackground(Color.WHITE);
					}
					else {
						Color alternateColor =Color.decode("#dbdbdb");
						component.setBackground(alternateColor);
					}

					if (colIndex <=1) {
						// ignore the row number and the status columns
						return component;
					}

					if (!isSourcePanel){
						String value= (String) getValueAt(rowIndex, colIndex);
						int inventoryColIndex=colIndex-2;
						if (value!=null && !value.isEmpty() ) {						
							Set<String> substitutionKeysSet=columnIndexToSubstitutionKeysSetMap.get(inventoryColIndex);
							if (substitutionKeysSet!=null && !substitutionKeysSet.isEmpty() && !value.isEmpty() ) {
								boolean hasValue=substitutionKeysSet.contains(value);
								if (!hasValue) {
									component.setBackground(Color.red);
								}
							}
						}
						else {
							Field field=fields.get(inventoryColIndex);
							if (!isSourcePanel && field.getMandatory() && (columnNamesToIgnore==null || !columnNamesToIgnore.contains(field.getName())) ) {
								component.setBackground(Color.red);
							}
						}
					}
					return component;
				}
				catch(Exception e) {
					e.printStackTrace();
					return component;
				}
			}
		};

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		UIUtils.setColumnSize(table,COLUMN_HEADING_ROW_NUM,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_ROW_NUM_WIDTH,false);
		if (isSourcePanel) {
			UIUtils.setColumnSize(table,RSC_HEADER_LAST_UPDATE_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_RSC_WIDTH,false);
			UIUtils.setColumnSize(table,RSC_HEADER_LAST_UPDATE_DATE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_RSC_WIDTH,false);
			UIUtils.setColumnSize(table,RSC_HEADER_CREATED_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_RSC_WIDTH,false);
			UIUtils.setColumnSize(table,RSC_HEADER_CREATION_DATE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_RSC_WIDTH,false);
		}
		else {
			UIUtils.setColumnSize(table,COLUMN_HEADING_TARGET_STATUS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_STATUS_WIDTH,false);
		}
		
		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(centerRenderer);
		for (Field field:fields) {
			String columnName=field.getName();
			TableColumn column=table.getColumn(columnName);
			UIUtils.setColumnSize(table,columnName,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_INVENTORY_NAMES_WIDTH,false);
			column.setHeaderRenderer(leftRenderer);
		}	

		RolloverMouseAdapter rolloverAdapter = new RolloverMouseAdapter(table);
		table.addMouseListener(rolloverAdapter);
		table.addMouseMotionListener(rolloverAdapter);

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

		convertDataGridTotalsPanel=new ConvertDataGridTotalsPanel();
		convertDataGridTotalsPanel.getGridRowsCountLabel().setText(UIConstants.LABEL_GRID_ROWS+
				Utils.formatNumberWithComma(dataRows.size()));
		filteringTable=new SnapshotFilteringTable(columnNames,variableScroll,table,33,convertDataGridTotalsPanel,true,false);

		filteringTable.getFixedTable().getColumn(COLUMN_HEADING_ROW_NUM).setHeaderRenderer(new MultiLineHeaderRenderer(Color.BLACK,Color.WHITE));
		if (isSourcePanel) {
			filteringTable.getFixedTable().getColumn(RSC_HEADER_LAST_UPDATE_NAME).setHeaderRenderer(new MultiLineHeaderRenderer(Color.BLACK,Color.WHITE));
			filteringTable.getFixedTable().getColumn(RSC_HEADER_LAST_UPDATE_DATE).setHeaderRenderer(new MultiLineHeaderRenderer(Color.BLACK,Color.WHITE));
			filteringTable.getFixedTable().getColumn(RSC_HEADER_CREATED_NAME).setHeaderRenderer(new MultiLineHeaderRenderer(Color.BLACK,Color.WHITE));
			filteringTable.getFixedTable().getColumn(RSC_HEADER_CREATION_DATE).setHeaderRenderer(new MultiLineHeaderRenderer(Color.BLACK,Color.WHITE));
		}
		else {
			filteringTable.getFixedTable().getColumn(COLUMN_HEADING_TARGET_STATUS).setHeaderRenderer(new MultiLineHeaderRenderer(Color.BLACK,Color.WHITE));
		}
		
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

		southPanel.add(convertDataGridTotalsPanel);
	}

}