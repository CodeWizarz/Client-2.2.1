package com.rapidesuite.snapshot.view.convert;

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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.gui.ButtonEditor;
import com.rapidesuite.snapshot.view.FilteringTable;
import com.rapidesuite.snapshot.view.GridButtonRenderer;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.RolloverMouseAdapter;
import com.rapidesuite.snapshot.view.SnapshotFilteringTable;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class ConvertSourcePanel extends ConvertPanelGeneric {

	protected List<ConvertSourceGridRecordInformation> convertSourceGridRecordInformationList;

	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_MODULE_NAME="Application\nNames";
	public static String COLUMN_HEADING_FORM_PATH="Menu Paths";
	public static String COLUMN_HEADING_FORM_NAME="Form Name";
	public static String COLUMN_HEADING_FORM_TYPE="Form Level";
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	public static String COLUMN_HEADING_IS_SUPPORTED="Support";
	public static String COLUMN_HEADING_STATUS="Status";
	public static String COLUMN_HEADING_TOTAL_RECORDS_COUNT="Total Records\nTo Convert";
	public static String COLUMN_HEADING_VIEW="View";
	//public static String COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_COUNT="Total Records\nNot Fully Converted";
	//public static String COLUMN_HEADING_COLUMN_NAMES_PARTIAL="Column Names\nNot Fully Converted";
	public static String COLUMN_HEADING_REMARKS="Remarks";
	public static String COLUMN_HEADING_EXECUTION_TIME="Execution\ntime";
	public static String COLUMN_HEADING_RAW_TIME="Raw time\n(in Secs)";

	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_MODULE_NAME_WIDTH=80;
	private static int COLUMN_HEADING_FORM_PATH_WIDTH=90;
	private static int COLUMN_HEADING_FORM_NAME_WIDTH=100;
	private static int COLUMN_HEADING_FORM_TYPE_WIDTH=90;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=160;
	private static int COLUMN_HEADING_STATUS_WIDTH=70;
	private static int COLUMN_HEADING_REMARKS_WIDTH=180;
	private static int COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH=100;
	//private static int COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_COUNT_WIDTH=140;
	//private static int COLUMN_HEADING_COLUMN_NAMES_PARTIAL_WIDTH=140;
	private static int COLUMN_HEADING_VIEW_WIDTH=80;
	private static int COLUMN_HEADING_EXECUTION_TIME_WIDTH=80;
	private static int COLUMN_HEADING_RAW_TIME_WIDTH=70;

	public ConvertSourcePanel(ConvertMainPanel convertMainPanel) {
		super(convertMainPanel);
		this.createComponents();
	}

	@SuppressWarnings("rawtypes")
	private void createComponents(){
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_MODULE_NAME);
		columnNames.add(COLUMN_HEADING_FORM_PATH);
		columnNames.add(COLUMN_HEADING_FORM_NAME);
		columnNames.add(COLUMN_HEADING_IS_SUPPORTED);
		columnNames.add(COLUMN_HEADING_INVENTORY_NAME);
		columnNames.add(COLUMN_HEADING_FORM_TYPE);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_VIEW);
		//columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_COUNT);
		//columnNames.add(COLUMN_HEADING_COLUMN_NAMES_PARTIAL);
		columnNames.add(COLUMN_HEADING_REMARKS);
		columnNames.add(COLUMN_HEADING_EXECUTION_TIME);	
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
					value=value.replaceAll("\n","<br>");
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
		UIUtils.setColumnSize(table,COLUMN_HEADING_FORM_TYPE,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_FORM_TYPE_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_INVENTORY_NAME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_INVENTORY_NAME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_STATUS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_STATUS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_VIEW_WIDTH,false);
		//UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_COUNT_WIDTH,false);
		//UIUtils.setColumnSize(table,COLUMN_HEADING_COLUMN_NAMES_PARTIAL,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_COLUMN_NAMES_PARTIAL_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_REMARKS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_REMARKS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_EXECUTION_TIME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_EXECUTION_TIME_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_RAW_TIME,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_RAW_TIME_WIDTH,false);
			
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
		table.getColumn(COLUMN_HEADING_STATUS).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_VIEW).setCellRenderer(centerRenderer);
		//table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_COUNT).setCellRenderer(centerRenderer);
		//table.getColumn(COLUMN_HEADING_COLUMN_NAMES_PARTIAL).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_REMARKS).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_INVENTORY_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_EXECUTION_TIME).setCellRenderer(centerRenderer);
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
					StringSelection sel  = new StringSelection(currentCellValueClicked); 
					Clipboard systemClipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
					systemClipboard.setContents(sel, sel); 
				}
				if (row >= 0 && column >= 0) {
					int colIndexAction=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW);		
					if (column==colIndexAction) {
						viewSourceData(row);
					}
				}
			}
		});
			
		ImageIcon ii=null;
		URL iconURL =null;

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
		table.setRowSelectionAllowed(false);
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

		convertGridTotalsPanel=new ConvertSourceGridTotalsPanel(this);
		filteringTable=new SnapshotFilteringTable(columnNames,variableScroll,table,33,convertGridTotalsPanel,true,false);
		
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

		southPanel.add(convertGridTotalsPanel);
	}

	protected void viewSourceData(int viewRow) {
		try{
			if (convertMainPanel.getConvertFrame().getConvertController()==null) {
				GUIUtils.popupInformationMessage("You must start the conversion prior viewing the records.");
				return;
			}
			int modelRow=table.convertRowIndexToModel(viewRow);
			ConvertSourceGridRecordInformation convertSourceGridRecordInformation=convertSourceGridRecordInformationList.get(modelRow);
			if ( !convertSourceGridRecordInformation.getStatus().equals(UIConstants.UI_STATUS_COMPLETED)) {
				GUIUtils.popupInformationMessage("Invalid conversion status!");
				return;
			}
			
			int	totalRecordsToConvert=convertSourceGridRecordInformation.getTotalRecordsToConvert();
			if (totalRecordsToConvert==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Records to convert: 0");
				return;
			}
			JFrame rootFrame=convertMainPanel.getConvertFrame();
			
			Inventory sourceInventory=convertSourceGridRecordInformation.getInventory();
			List<String[]> dataRows=InjectUtils.parseXMLDataFile(convertSourceGridRecordInformation.getSourceDataXMLFile());
			
			ConvertDataGrid convertDataGrid=new ConvertDataGrid(rootFrame,sourceInventory,dataRows,true,
					convertMainPanel.getConvertFrame().getConvertController().getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap());
			convertDataGrid.pack();
			convertDataGrid.setVisible(true);
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}

	public void displayInventories(List<ConvertSourceGridRecordInformation> convertSourceGridRecordInformationList) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		filteringTable.removeRowFilter();
		tableModel.setRowCount(0);

		this.convertSourceGridRecordInformationList=convertSourceGridRecordInformationList;
		for (ConvertSourceGridRecordInformation convertSourceGridRecordInformation:convertSourceGridRecordInformationList) {
			Vector<Object> rowGrid = new Vector<Object>();

			String formattedRowNumber="";
			try{
				int visibleRowSequence=convertSourceGridRecordInformation.getGridIndex()+1;
				formattedRowNumber=Utils.formatNumberWithComma(visibleRowSequence);
			}
			catch(Exception e) {
			}
			rowGrid.add(formattedRowNumber);
			rowGrid.add(convertSourceGridRecordInformation.getFormInformation().getFormattedApplicationNames());
			rowGrid.add(convertSourceGridRecordInformation.getFormInformation().getFormattedFormPaths());	
			rowGrid.add(convertSourceGridRecordInformation.getFormInformation().getFormName());
			rowGrid.add("");
			rowGrid.add(convertSourceGridRecordInformation.getInventoryName());
			rowGrid.add(convertSourceGridRecordInformation.getFormInformation().getFormType());	
			rowGrid.add("");
			rowGrid.add(UIUtils.getFormattedNumber(convertSourceGridRecordInformation.getTotalRecordsToConvert()));
			rowGrid.add("Open");
			rowGrid.add("0");
			rowGrid.add("");
			rowGrid.add(convertSourceGridRecordInformation.getRemarks());
			rowGrid.add(convertSourceGridRecordInformation.getExecutionTime());
			long rawTimeInSecs=convertSourceGridRecordInformation.getRawTimeInSecs();
			rowGrid.add(""+rawTimeInSecs);

			tableModel.addRow(rowGrid);
		}
		convertGridTotalsPanel.updateTotalLabels();
		filteringTable.applyFiltering();
	}	

	public void refreshGrid(final List<ConvertSourceGridRecordInformation> convertSourceGridRecordInformationList) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TableModel model = table.getModel();
				for (ConvertSourceGridRecordInformation convertSourceGridRecordInformation:convertSourceGridRecordInformationList) {
					int gridIndex=convertSourceGridRecordInformation.getGridIndex();
					int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);
					int colIndexExecutionTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_EXECUTION_TIME);
					int colIndexTotalRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
					int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
					int colIndexRawTime=table.getColumnModel().getColumnIndex(COLUMN_HEADING_RAW_TIME);
					int colIndexIsSupported=table.getColumnModel().getColumnIndex(COLUMN_HEADING_IS_SUPPORTED);
					//int colIndexTotalRecordsPartiallyConverted=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_COUNT);
					//int colIndexColumnNamesNotFullyConverted=table.getColumnModel().getColumnIndex(COLUMN_HEADING_COLUMN_NAMES_PARTIAL);
					
					try {
						int totalRecordsToConvert=convertSourceGridRecordInformation.getTotalRecordsToConvert();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsToConvert),gridIndex,colIndexTotalRecords);
						
						/*
						int totalRecordsPartiallyConverted=convertSourceGridRecordInformation.getTotalRecordsNotFullyConverted();
						if (totalRecordsPartiallyConverted==-1) {
							model.setValueAt("",gridIndex,colIndexTotalRecordsPartiallyConverted);
						}
						else {
							model.setValueAt(Utils.formatNumberWithComma(totalRecordsPartiallyConverted),gridIndex,colIndexTotalRecordsPartiallyConverted);
						}
						*/
						String executionTime=convertSourceGridRecordInformation.getExecutionTime();
						if (executionTime==null) {
							model.setValueAt("",gridIndex,table.getColumnModel().getColumnIndex(COLUMN_HEADING_EXECUTION_TIME));
						}
						else {
							model.setValueAt(executionTime,gridIndex,colIndexExecutionTime);
							String rawTimeInSecs=""+convertSourceGridRecordInformation.getRawTimeInSecs();
							model.setValueAt(rawTimeInSecs,gridIndex,colIndexRawTime);
						}

						String remarks=convertSourceGridRecordInformation.getRemarks();
						model.setValueAt(remarks,gridIndex,colIndexRemarks);
						
						//String columnNamesNotFullyConverted=convertSourceGridRecordInformation.getColumnNamesNotFullyConverted();
						//model.setValueAt(columnNamesNotFullyConverted,gridIndex,colIndexColumnNamesNotFullyConverted);

						String status=convertSourceGridRecordInformation.getStatus();
						model.setValueAt(status,gridIndex,colIndexStatus);
						
						String supportText=convertSourceGridRecordInformation.getSupportText();
						model.setValueAt(supportText,gridIndex,colIndexIsSupported);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				table.repaint();
			}
		});
	}

	public List<ConvertSourceGridRecordInformation> getConvertSourceGridRecordInformationList() {
		return convertSourceGridRecordInformationList;
	}

	public List<ConvertSourceGridRecordInformation> getSelectedConvertSourceGridRecordInformationList()
	{
		TableModel model = table.getModel();
		int numRows = model.getRowCount();
		List<ConvertSourceGridRecordInformation> toReturn=new ArrayList<ConvertSourceGridRecordInformation>();
		for (int i=0; i < numRows; i++) {		
			ConvertSourceGridRecordInformation migrationSourceGridRecordInformation=convertSourceGridRecordInformationList.get(i);
			toReturn.add(migrationSourceGridRecordInformation);	
		}
		return toReturn;
	}

	public List<ConvertSourceGridRecordInformation> getFilteredConvertSourceGridRecordInformationList()
	{
		List<ConvertSourceGridRecordInformation> toReturn=new ArrayList<ConvertSourceGridRecordInformation>();
		for(int row = 0;row < table.getRowCount();row++) {
			int modelRowIndex=table.convertRowIndexToModel(row);
			ConvertSourceGridRecordInformation migrationSourceGridRecordInformation=convertSourceGridRecordInformationList.get(modelRowIndex);
			toReturn.add(migrationSourceGridRecordInformation);
		}
		return toReturn;
	}

	public FilteringTable getFilteringTable() {
		return filteringTable;
	}

	public JTable getTable() {
		return table;
	}

	public ConvertGridTotalsPanel getConvertGridTotalsPanel() {
		return convertGridTotalsPanel;
	}

	public ConvertMainPanel getConvertMainPanel() {
		return convertMainPanel;
	}

}