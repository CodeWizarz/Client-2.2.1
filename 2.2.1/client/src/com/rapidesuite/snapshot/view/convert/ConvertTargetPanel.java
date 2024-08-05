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
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.rapidesuite.snapshot.controller.convert.ConvertEngine;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.FilteringTable;
import com.rapidesuite.snapshot.view.GridButtonRenderer;
import com.rapidesuite.snapshot.view.RolloverIcon;
import com.rapidesuite.snapshot.view.RolloverMouseAdapter;
import com.rapidesuite.snapshot.view.SnapshotFilteringTable;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

@SuppressWarnings("serial")
public class ConvertTargetPanel extends ConvertPanelGeneric {

	protected List<ConvertTargetGridRecordInformation> convertTargetGridRecordInformationList;
	private Map<String,ConvertTargetGridRecordInformation> targetInventoryNameToConverTargetGridRecordInformationMap;
	
	public static String COLUMN_HEADING_ROW_NUM="Row #";
	public static String COLUMN_HEADING_INVENTORY_NAME="Inventory name";
	public static String COLUMN_HEADING_STATUS="Status";
	public static String COLUMN_HEADING_TOTAL_RECORDS_COUNT="Total Records";
	public static String COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT="Total Records\nConfiguration";
	public static String COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT="Total Records\nPost Configuration";
	public static String COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT="Total Records\nPost Implementation";
	public static String COLUMN_HEADING_VIEW="View Records";
	public static String COLUMN_HEADING_CONFIGURATION_VIEW="View Records\nConfiguration";
	public static String COLUMN_HEADING_POST_CONFIGURATION_VIEW="View Records\nPost Configuration";
	public static String COLUMN_HEADING_POST_IMPLEMENTATION_VIEW="View Records\nPost Implementation";
	public static String COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS="Total Records\nMissing Required Data";
	public static String COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION="Total Records\nInvalid Values";
	public static String COLUMN_HEADING_REMARKS="Remarks";

	private static int COLUMN_HEADING_MIN_WIDTH=10;
	private static int COLUMN_HEADING_ROW_NUM_WIDTH=60;
	private static int COLUMN_HEADING_STATUS_WIDTH=70;
	private static int COLUMN_HEADING_INVENTORY_NAME_WIDTH=300;
	private static int COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH=140;
	private static int COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT_WIDTH=140;
	private static int COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT_WIDTH=140;
	private static int COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT_WIDTH=140;
	
	private static int COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS_WIDTH=160;
	private static int COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION_WIDTH=140;
	private static int COLUMN_HEADING_VIEW_WIDTH=140;
	private static int COLUMN_HEADING_CONFIGURATION_VIEW_WIDTH=140;
	private static int COLUMN_HEADING_POST_CONFIGURATION_VIEW_WIDTH=140;
	private static int COLUMN_HEADING_POST_IMPLEMENTATION_VIEW_WIDTH=140;
	private static int COLUMN_HEADING_REMARKS_WIDTH=180;
	
	public static String STATUS_VALID="Valid";
	public static String STATUS_INVALID="Invalid";
	
	public ConvertTargetPanel(ConvertMainPanel convertMainPanel) {
		super(convertMainPanel);
		this.createComponents();
	}

	@SuppressWarnings("rawtypes")
	private void createComponents(){
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_ROW_NUM);
		columnNames.add(COLUMN_HEADING_INVENTORY_NAME);
		columnNames.add(COLUMN_HEADING_STATUS);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT);
		columnNames.add(COLUMN_HEADING_VIEW);
		columnNames.add(COLUMN_HEADING_CONFIGURATION_VIEW);
		columnNames.add(COLUMN_HEADING_POST_CONFIGURATION_VIEW);
		columnNames.add(COLUMN_HEADING_POST_IMPLEMENTATION_VIEW);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS);
		columnNames.add(COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION);
		columnNames.add(COLUMN_HEADING_REMARKS);
		
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

					if (rowIndex%2 == 0){
						component.setBackground(Color.WHITE);
					}
					else {
						Color alternateColor =Color.decode("#dbdbdb");
						component.setBackground(alternateColor);
					}
					
					String status= (String) getValueAt(rowIndex, table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS));		
					if (status!=null && status.equalsIgnoreCase(STATUS_VALID)) {
						component.setBackground(Color.decode("#6bb23d"));
					}
					else 
					if (status!=null && status.equalsIgnoreCase(STATUS_INVALID)) {
						component.setBackground(UIConstants.COLOR_RED);
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
		
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT_WIDTH,false);
		
		UIUtils.setColumnSize(table,COLUMN_HEADING_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_VIEW_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_CONFIGURATION_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_CONFIGURATION_VIEW_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_POST_CONFIGURATION_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_POST_CONFIGURATION_VIEW_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_POST_IMPLEMENTATION_VIEW,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_POST_IMPLEMENTATION_VIEW_WIDTH,false);
		
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_REMARKS,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_REMARKS_WIDTH,false);
		UIUtils.setColumnSize(table,COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION,COLUMN_HEADING_MIN_WIDTH,COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION_WIDTH,false);

		RolloverMouseAdapter rolloverAdapter = new RolloverMouseAdapter(table);
		table.addMouseListener(rolloverAdapter);
		table.addMouseMotionListener(rolloverAdapter);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		table.getColumn(COLUMN_HEADING_ROW_NUM).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT).setCellRenderer(centerRenderer);
				
		table.getColumn(COLUMN_HEADING_STATUS).setCellRenderer(centerRenderer);
		
		table.getColumn(COLUMN_HEADING_VIEW).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_CONFIGURATION_VIEW).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_POST_CONFIGURATION_VIEW).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_POST_IMPLEMENTATION_VIEW).setCellRenderer(centerRenderer);
		
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS).setCellRenderer(centerRenderer);
		table.getColumn(COLUMN_HEADING_REMARKS).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_INVENTORY_NAME).setCellRenderer(leftRenderer);
		table.getColumn(COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION).setCellRenderer(centerRenderer);
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
					int colIndexActionTotalRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_VIEW);		
					int colIndexActionTotalRecordsConfiguration=table.getColumnModel().getColumnIndex(COLUMN_HEADING_CONFIGURATION_VIEW);	
					int colIndexActionTotalRecordsPostConfiguration=table.getColumnModel().getColumnIndex(COLUMN_HEADING_POST_CONFIGURATION_VIEW);	
					int colIndexActionTotalRecordsPostImplementation=table.getColumnModel().getColumnIndex(COLUMN_HEADING_POST_IMPLEMENTATION_VIEW);	
					boolean isTotalRecords=false;
					boolean isTotalRecordsConfiguration=false;
					boolean isTotalRecordsPostConfiguration=false;
					boolean isTotalRecordsPostImplementation=false;
					boolean isViewColumn=false;
					
					if (column==colIndexActionTotalRecords) {
						isTotalRecords=true;
						isViewColumn=true;
					}
					else 
						if (column==colIndexActionTotalRecordsConfiguration) {
							isTotalRecordsConfiguration=true;
							isViewColumn=true;
						}
						else 
							if (column==colIndexActionTotalRecordsPostConfiguration) {
								isTotalRecordsPostConfiguration=true;
								isViewColumn=true;
							}
							else 
								if (column==colIndexActionTotalRecordsPostImplementation) {
									isTotalRecordsPostImplementation=true;
									isViewColumn=true;
								}
					
					if (isViewColumn) {
						viewConvertedData(row,isTotalRecords,isTotalRecordsConfiguration,isTotalRecordsPostConfiguration,
								isTotalRecordsPostImplementation);
					}
				}
			}
		});

		table.getTableHeader().setReorderingAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setSelectionForeground(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
		table.setBackground(Color.decode("#DBDBDB"));
		table.setRowHeight(22);
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000, 80));
		
		setCellButton(COLUMN_HEADING_VIEW,rolloverAdapter);
		setCellButton(COLUMN_HEADING_CONFIGURATION_VIEW,rolloverAdapter);
		setCellButton(COLUMN_HEADING_POST_CONFIGURATION_VIEW,rolloverAdapter);
		setCellButton(COLUMN_HEADING_POST_IMPLEMENTATION_VIEW,rolloverAdapter);
		
		JScrollPane variableScroll = new JScrollPane(table ,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			public void setColumnHeaderView(Component view) {} // work around
		};

		convertGridTotalsPanel=new ConvertTargetGridTotalsPanel(this);
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
	
	private void setCellButton(String columnName,RolloverMouseAdapter rolloverAdapter) {
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
		table.getColumn(columnName).setCellRenderer(gridButtonRenderer);
		ButtonEditor buttonEditor=new ButtonEditor(new JCheckBox(),borderButton,width,height,null,true);
		button=buttonEditor.getButton();
		iconURL = this.getClass().getResource("/images/snapshot/button_snapshot_view.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		button.setIcon(ii);
		table.getColumn(columnName).setCellEditor(buttonEditor);
	}

	protected void viewConvertedData(int viewRow, boolean isTotalRecords,
			boolean isTotalRecordsConfiguration, boolean isTotalRecordsPostConfiguration, boolean isTotalRecordsPostImplementation) {
		try{
			int modelRow=table.convertRowIndexToModel(viewRow);
			ConvertTargetGridRecordInformation convertTargetGridRecordInformation=convertTargetGridRecordInformationList.get(modelRow);
			
			if (isTotalRecords && convertTargetGridRecordInformation.getTotalRecords()==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Records: 0");
				return;
			}
			if (isTotalRecordsConfiguration && convertTargetGridRecordInformation.getTotalRecordsConfiguration()==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Configuration Records: 0");
				return;
			}
			if (isTotalRecordsPostConfiguration && convertTargetGridRecordInformation.getTotalRecordsPostConfiguration()==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Post Configuration Records: 0");
				return;
			}
			if (isTotalRecordsPostImplementation && convertTargetGridRecordInformation.getTotalRecordsPostImplementation()==0) {
				GUIUtils.popupInformationMessage("No records to view! Total Post Implementation Records: 0");
				return;
			}			
			
			JFrame rootFrame=convertMainPanel.getConvertFrame();
			
			Inventory targetInventory=convertTargetGridRecordInformation.getInventory();
			if (targetInventory==null) {
				File targetInventoryFile=convertTargetGridRecordInformation.getInventoryFile();
				targetInventory=FileUtils.getInventory(targetInventoryFile,convertTargetGridRecordInformation.getInventoryName());
				convertTargetGridRecordInformation.setInventory(targetInventory);
			}
			File dataFolder=null;
			File convertedFolder=convertMainPanel.getConvertFrame().getConvertController().getConvertedFolder();
			boolean isRecursive=false;
			if (isTotalRecords) {
				dataFolder=convertedFolder;
				isRecursive=true;
			}
			else 
				if (isTotalRecordsConfiguration) {
					dataFolder=new File(convertedFolder,ConvertEngine.DATA_FOLDER_NAME_CONFIGURATION);
				}
				else 
					if (isTotalRecordsPostConfiguration) {
						dataFolder=new File(convertedFolder,ConvertEngine.DATA_FOLDER_NAME_POST_CONFIGURATION);
					}
					else
						if (isTotalRecordsPostImplementation) {
							dataFolder=new File(convertedFolder,ConvertEngine.DATA_FOLDER_NAME_POST_IMPLEMENTATION);
						}
						else throw new Exception("Internal error, invalid output folder, cannot find data files.");

			List<File> fileList=ModelUtils.getFileNameList(dataFolder,targetInventory.getName(),isRecursive,true);
			List<String[]> finalList=new ArrayList<String[]>();
			for (File file:fileList) {
				List<String[]> dataRows=InjectUtils.parseXMLDataFile(file);
				finalList.addAll(dataRows);
			}
					
			ConvertDataGrid convertDataGrid=new ConvertDataGrid(rootFrame,targetInventory,finalList,false,
					convertMainPanel.getConvertFrame().getConvertController().getTargetInventoryNameToMandatoryColumnNamesToIgnoreMap());
			convertDataGrid.pack();
			convertDataGrid.setVisible(true);
		} 
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}

	public void displayInventories(List<ConvertTargetGridRecordInformation> convertTargetGridRecordInformationList) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		filteringTable.removeRowFilter();
		tableModel.setRowCount(0);

		targetInventoryNameToConverTargetGridRecordInformationMap=new HashMap<String,ConvertTargetGridRecordInformation>();
		this.convertTargetGridRecordInformationList=convertTargetGridRecordInformationList;
		for (ConvertTargetGridRecordInformation convertTargetGridRecordInformation:convertTargetGridRecordInformationList) {
			Vector<Object> rowGrid = new Vector<Object>();

			targetInventoryNameToConverTargetGridRecordInformationMap.put(convertTargetGridRecordInformation.getInventoryName(), convertTargetGridRecordInformation);
			
			String formattedRowNumber="";
			try{
				int visibleRowSequence=convertTargetGridRecordInformation.getGridIndex()+1;
				formattedRowNumber=Utils.formatNumberWithComma(visibleRowSequence);
			}
			catch(Exception e) {
			}
			rowGrid.add(formattedRowNumber);
			rowGrid.add(convertTargetGridRecordInformation.getInventoryName());
			rowGrid.add("");
			rowGrid.add(UIUtils.getFormattedNumber(convertTargetGridRecordInformation.getTotalRecordsConfiguration()));
			rowGrid.add("Open");
			rowGrid.add("0");
			rowGrid.add("0");
			rowGrid.add(convertTargetGridRecordInformation.getRemarks());

			tableModel.addRow(rowGrid);
		}
		convertGridTotalsPanel.updateTotalLabels();
		filteringTable.applyFiltering();
	}	

	public void refreshGrid() {
		refreshGrid(convertTargetGridRecordInformationList);
	}
	
	public void refreshGrid(final List<ConvertTargetGridRecordInformation> convertTargetGridRecordInformationListParam) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TableModel model = table.getModel();
				for (ConvertTargetGridRecordInformation convertTargetGridRecordInformation:convertTargetGridRecordInformationListParam) {
					int gridIndex=convertTargetGridRecordInformation.getGridIndex();
					
					int colIndexTotalRecords=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_COUNT);
					int colIndexTotalRecordsConfiguration=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_CONFIGURATION_COUNT);
					int colIndexTotalRecordsPostConfiguration=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_POST_CONFIGURATION_COUNT);
					int colIndexTotalRecordsPostImplementation=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_POST_IMPLEMENTATION_COUNT);
					int colIndexRemarks=table.getColumnModel().getColumnIndex(COLUMN_HEADING_REMARKS);
					int colIndexTotalRecordsPartialRequiredColumns=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_PARTIAL_REQUIRED_COLUMNS);
					int colIndexTotalRecordsInvalidValues=table.getColumnModel().getColumnIndex(COLUMN_HEADING_TOTAL_RECORDS_WRONG_SUBSTITUTION);
					int colIndexStatus=table.getColumnModel().getColumnIndex(COLUMN_HEADING_STATUS);

					try {
						String status=convertTargetGridRecordInformation.getStatus();
						model.setValueAt(status,gridIndex,colIndexStatus);
						
						int totalRecords=convertTargetGridRecordInformation.getTotalRecords();
						model.setValueAt(Utils.formatNumberWithComma(totalRecords),gridIndex,colIndexTotalRecords);
						int totalRecordsConfiguration=convertTargetGridRecordInformation.getTotalRecordsConfiguration();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsConfiguration),gridIndex,colIndexTotalRecordsConfiguration);
						int totalRecordsPostConfiguration=convertTargetGridRecordInformation.getTotalRecordsPostConfiguration();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsPostConfiguration),gridIndex,colIndexTotalRecordsPostConfiguration);
						int totalRecordsPostImplementation=convertTargetGridRecordInformation.getTotalRecordsPostImplementation();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsPostImplementation),gridIndex,colIndexTotalRecordsPostImplementation);
						
						int totalRecordsMissingRequiredColumns=convertTargetGridRecordInformation.getTotalRecordsMissingRequiredColumns();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsMissingRequiredColumns),gridIndex,colIndexTotalRecordsPartialRequiredColumns);
						
						int totalRecordsInvalidValues=convertTargetGridRecordInformation.getTotalRecordsInvalidValues();
						model.setValueAt(Utils.formatNumberWithComma(totalRecordsInvalidValues),gridIndex,colIndexTotalRecordsInvalidValues);

						String remarks=convertTargetGridRecordInformation.getRemarks();
						model.setValueAt(remarks,gridIndex,colIndexRemarks);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				table.repaint();
				convertGridTotalsPanel.updateTotalLabels();
			}
		});
	}

	public List<ConvertTargetGridRecordInformation> getConvertTargetGridRecordInformationList() {
		return convertTargetGridRecordInformationList;
	}

	public List<ConvertTargetGridRecordInformation> getSelectedConvertTargetGridRecordInformationList()
	{
		TableModel model = table.getModel();
		int numRows = model.getRowCount();
		List<ConvertTargetGridRecordInformation> toReturn=new ArrayList<ConvertTargetGridRecordInformation>();
		for (int i=0; i < numRows; i++) {		
			ConvertTargetGridRecordInformation convertTargetGridRecordInformation=convertTargetGridRecordInformationList.get(i);
			toReturn.add(convertTargetGridRecordInformation);	
		}
		return toReturn;
	}

	public List<ConvertTargetGridRecordInformation> getFilteredConvertTargetGridRecordInformationList()
	{
		List<ConvertTargetGridRecordInformation> toReturn=new ArrayList<ConvertTargetGridRecordInformation>();
		for(int row = 0;row < table.getRowCount();row++) {
			int modelRowIndex=table.convertRowIndexToModel(row);
			ConvertTargetGridRecordInformation convertTargetGridRecordInformation=convertTargetGridRecordInformationList.get(modelRowIndex);
			toReturn.add(convertTargetGridRecordInformation);
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