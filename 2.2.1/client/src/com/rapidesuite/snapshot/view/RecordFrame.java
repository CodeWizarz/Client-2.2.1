package com.rapidesuite.snapshot.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.inject.InjectMain;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.SnapshotMain;
import com.rapidesuite.snapshot.model.DataRow;
import com.rapidesuite.snapshot.model.ModelUtils;

@SuppressWarnings("serial")
public abstract class RecordFrame extends JFrame{

	public static String COLUMN_HEADING_FIELDS="Field names";
	public static int COLUMN_HEADING_FIELDS_WIDTH=200;
	public static int COLUMN_HEADING_BASELINE_WIDTH=200;
	public static final String FRAME_TITLE="RAPIDSnapshot - Record Viewer";
	public final static String FIELD_AUDIT_NAME="Audit fields";
	
	public static final String FIELD_AUDIT_LAST_UPDATED_BY="Updated By";
	public static final String FIELD_AUDIT_LAST_UPDATE_DATE="Updated On";
	public static final String FIELD_AUDIT_CREATED_BY="Created By";
	public static final String FIELD_AUDIT_CREATION_DATE="Created On";
	
	protected Inventory inventory;
	protected List<SnapshotGridRecord> snapshotGridRecords;
	private int currentRecordIndex;
	private Map<String,String> snapshotEnvironmentProperties;
	private int	totalRecordsCount;
	
	private JPanel northPanel;
	private JPanel centerPanel;
	private JPanel southPanel;
	private JLabel  recordsNavigationLabel;
	private JButton nextButton;
	private JButton previousButton;
	private JButton firstButton;
	private JButton lastButton;
	private JTextField inputRecordNumberTextField;
	
	private JLabel  statusLabel;
	
	public RecordFrame(int frameWidth,int frameHeight,
			Map<String,String> snapshotEnvironmentProperties,Inventory inventory,
			List<SnapshotGridRecord> snapshotGridRecords,
			int	totalRecordsCount) {
		setIconImage(GUIUtils.getImageIcon(this.getClass(), SnapshotMain.getSharedApplicationIconPath()).getImage());
		setTitle(FRAME_TITLE+" ("+inventory.getName()+")");
		setLayout(new BorderLayout());
		setSize(frameWidth, frameHeight);
		((JComponent)getContentPane()).setOpaque(true);
		((JComponent)getContentPane()).setBackground(Color.decode("#dbdcdf"));
		((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		this.snapshotEnvironmentProperties=snapshotEnvironmentProperties;
		this.inventory=inventory;
		this.totalRecordsCount=totalRecordsCount;
		this.snapshotGridRecords=snapshotGridRecords;
		currentRecordIndex=-1;
		
		createComponents();
	}
	
	public void createComponents(){
		northPanel=new JPanel();
		northPanel.setOpaque(false);
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(northPanel,BorderLayout.NORTH);
		
		centerPanel=new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(centerPanel,BorderLayout.CENTER);
		
		southPanel=new JPanel();
		southPanel.setOpaque(false);
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(southPanel,BorderLayout.SOUTH);

		JPanel navigationPanel=new JPanel();
		navigationPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		navigationPanel.setOpaque(false);
		navigationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.X_AXIS));
		northPanel.add(navigationPanel);

		ImageIcon ii=null;
		URL iconURL =null;
		
		iconURL = this.getClass().getResource("/images/snapshot/button_first.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		firstButton = new JButton();
		firstButton.setIcon(ii);
		firstButton.setBorderPainted(false);
		firstButton.setContentAreaFilled(false);
		firstButton.setFocusPainted(false);
		firstButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_first_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		firstButton.setRolloverIcon(new RolloverIcon(ii));
		navigationPanel.add(firstButton);
		firstButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionFirst();
			}
		}
				);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_previous.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		previousButton = new JButton();
		previousButton.setIcon(ii);
		previousButton.setBorderPainted(false);
		previousButton.setContentAreaFilled(false);
		previousButton.setFocusPainted(false);
		previousButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_previous_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		previousButton.setRolloverIcon(new RolloverIcon(ii));
		navigationPanel.add(previousButton);
		previousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionPrevious();
			}
		}
				);

		recordsNavigationLabel=new JLabel();
		InjectUtils.assignArialPlainFont(recordsNavigationLabel,InjectMain.FONT_SIZE_NORMAL);
		navigationPanel.add(Box.createRigidArea(new Dimension(20, 30)));
		navigationPanel.add(recordsNavigationLabel);
		navigationPanel.add(Box.createRigidArea(new Dimension(10, 30)));
		
		inputRecordNumberTextField=new JTextField();
		UIUtils.setDimension(inputRecordNumberTextField, 70, 25);
		inputRecordNumberTextField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				try{
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						JTextField textField = (JTextField) e.getSource();
						String text = textField.getText();
						try {
							int customRecordIndex=Integer.valueOf(text);
							processActionCustomRecordIndex(customRecordIndex);
							textField.setText("");
						}
						catch (NumberFormatException en) {
							throw new Exception("You must enter a valid number!");
						}
					}
				}
				catch(Exception ex) {
					FileUtils.printStackTrace(ex);
					GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+ex.getMessage());
				}
			}
		});
		
		navigationPanel.add(inputRecordNumberTextField);
		navigationPanel.add(Box.createRigidArea(new Dimension(20, 30)));
		
		iconURL = this.getClass().getResource("/images/snapshot/button_next.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		nextButton = new JButton();
		nextButton.setIcon(ii);
		nextButton.setBorderPainted(false);
		nextButton.setContentAreaFilled(false);
		nextButton.setFocusPainted(false);
		nextButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_next_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		nextButton.setRolloverIcon(new RolloverIcon(ii));
		navigationPanel.add(nextButton);
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionNext();
			}
		}
				);
		
		iconURL = this.getClass().getResource("/images/snapshot/button_last.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		lastButton = new JButton();
		lastButton.setIcon(ii);
		lastButton.setBorderPainted(false);
		lastButton.setContentAreaFilled(false);
		lastButton.setFocusPainted(false);
		lastButton.setRolloverEnabled(true);
		iconURL = this.getClass().getResource("/images/snapshot/button_last_rollover.png");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		lastButton.setRolloverIcon(new RolloverIcon(ii));
		navigationPanel.add(lastButton);
		lastButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				processActionLast();
			}
		}
				);
		
		navigationPanel.add(Box.createRigidArea(new Dimension(40, 30)));
		iconURL = this.getClass().getResource("/images/indicator.gif");
		try{ ii=new ImageIcon(iconURL); }catch(Exception e) {FileUtils.printStackTrace(e);}
		statusLabel=new JLabel("Fetching data...");
		statusLabel.setIcon(ii);
		statusLabel.setVisible(false);
		InjectUtils.assignArialPlainFont(statusLabel,InjectMain.FONT_SIZE_NORMAL);
		navigationPanel.add(statusLabel);
	}

	protected void processActionLast() {
		try {
			currentRecordIndex=totalRecordsCount;
			setNavigationButtonsEnabled();
			processActionThread();
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}		
	}

	protected void processActionFirst() {
		try {
			currentRecordIndex=1;
			setNavigationButtonsEnabled();
			processActionThread();
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}
	
	protected void processActionCustomRecordIndex(int customRecordIndex) throws Exception {
		if (customRecordIndex <= 0) {
			throw new Exception("The record index must be greater than 0!");
		}
		if (customRecordIndex > totalRecordsCount) {
			throw new Exception("The record index must be equal or less than "+totalRecordsCount+"!");
		}
		currentRecordIndex=customRecordIndex;
		setNavigationButtonsEnabled();
		processActionThread();
	}

	public void processActionNext() {
		try {
			if (currentRecordIndex<totalRecordsCount) {
				if (currentRecordIndex==-1) {
					currentRecordIndex=0;
				}
				currentRecordIndex++;
				setNavigationButtonsEnabled();
				processActionThread();
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}

	protected void processActionPrevious() {
		try {
			if (currentRecordIndex > 0) {
				currentRecordIndex--;
				setNavigationButtonsEnabled();
				processActionThread();
			}
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Cannot complete operation. Error: "+e.getMessage());
		}
	}
	
	private void processActionThread() {
		statusLabel.setVisible(true);
		Thread thread = new Thread(){
			public void run(){
				List<Map<Integer, DataRow>> dataRows=fetchRecordsParent();
				displayRecords(dataRows);
				statusLabel.setVisible(false);
			}
		};
		thread.start();
	}
	
	private void setNavigationButtonsEnabled() {
		if (currentRecordIndex==1) {
			previousButton.setEnabled(false);
			firstButton.setEnabled(false);
		}
		else {
			previousButton.setEnabled(true);
			firstButton.setEnabled(true);
		}
		if (currentRecordIndex==totalRecordsCount) {
			nextButton.setEnabled(false);
			lastButton.setEnabled(false);
		}
		else {
			nextButton.setEnabled(true);
			lastButton.setEnabled(true);
		}
	}
	
	private List<Map<Integer, DataRow>> fetchRecordsParent() {
		Connection connection=null;
		try{
			connection=DatabaseUtils.getJDBCConnection(
					ModelUtils.getJDBCString(snapshotEnvironmentProperties),
					ModelUtils.getDBUserName(snapshotEnvironmentProperties),
					ModelUtils.getDBPassword(snapshotEnvironmentProperties));

			int startRowNumToFetch=currentRecordIndex;
			int endRowNumToFetch=currentRecordIndex;

			List<Map<Integer, DataRow>> dataRows=fetchRecords(connection,inventory,startRowNumToFetch,endRowNumToFetch);
			return dataRows;
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Error: "+e.getMessage());
			return new ArrayList<Map<Integer, DataRow>>();
		}
		finally {
			DirectConnectDao.closeQuietly(connection);	  
		}
	}
	
	public abstract List<Map<Integer, DataRow>> fetchRecords(Connection connection,
			Inventory inventory, int startRowNumToFetch, int endRowNumToFetch) throws Exception;

	private void displayRecords(List<Map<Integer, DataRow>> dataRows) {
		try {
			recordsNavigationLabel.setText(Utils.formatNumberWithComma(currentRecordIndex)+" / "+Utils.formatNumberWithComma(totalRecordsCount));
			for (Map<Integer, DataRow> snapshotIdToDataRowMap:dataRows) {
				JTable table=createRecordTable(snapshotGridRecords);
				JScrollPane tableScrollPane=createJScrollPane(table);
				JPanel panel=new JPanel();
				centerPanel.removeAll();
				centerPanel.add(panel);
				panel.setLayout(new BorderLayout());
				panel.setOpaque(true);
				panel.add(tableScrollPane,BorderLayout.CENTER);
				
				initTableModel(table,inventory,snapshotIdToDataRowMap,snapshotGridRecords);
			}
			setVisible(true);
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initTableModel(JTable table,Inventory inventory,Map<Integer, DataRow> snapshotIdToDataRowMap,
			List<SnapshotGridRecord> snapshotGridRecords) throws Exception {
		List<String> dataInventoryFields=inventory.getFieldNamesUsedForDataEntry();
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		StringBuffer buffer=new StringBuffer("");
		for (int i=0;i<dataInventoryFields.size();i++) {
			String fieldName=dataInventoryFields.get(i);
			//System.out.println("fields: i="+i+" fieldName:"+fieldName);
			Vector<String> row = new Vector<String>();
			row.add(fieldName);
			for (SnapshotGridRecord snapshotGridRecord:snapshotGridRecords) {
				int snapshotId=snapshotGridRecord.getSnapshotId();
				DataRow dataRow=snapshotIdToDataRowMap.get(snapshotId);
				String value=null;
				if (dataRow==null) {
					// This means that the record was either added or PK updated.
					value=""; 
				}
				else {
					value=dataRow.getDataValues()[i];
					buffer.append(value);
				}
				row.add(value);
			}
			tableModel.addRow(row);
		}
		Vector row = new Vector();
		row.add(FIELD_AUDIT_NAME);
		tableModel.addRow(row);

		initTableExtraColumn(table,FIELD_AUDIT_CREATED_BY,snapshotIdToDataRowMap,snapshotGridRecords);
		initTableExtraColumn(table,FIELD_AUDIT_CREATION_DATE,snapshotIdToDataRowMap,snapshotGridRecords);
		initTableExtraColumn(table,FIELD_AUDIT_LAST_UPDATED_BY,snapshotIdToDataRowMap,snapshotGridRecords);
		initTableExtraColumn(table,FIELD_AUDIT_LAST_UPDATE_DATE,snapshotIdToDataRowMap,snapshotGridRecords);
	}
	
	private void initTableExtraColumn(JTable table,String columnName,Map<Integer, DataRow> snapshotIdToDataRowMap,
			List<SnapshotGridRecord> snapshotGridRecords) throws Exception {
		Vector<String> row = new Vector<String>();
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		row.add(columnName);
		for (SnapshotGridRecord snapshotGridRecord:snapshotGridRecords) {
			int snapshotId=snapshotGridRecord.getSnapshotId();
			DataRow dataRow=snapshotIdToDataRowMap.get(snapshotId);
			String value=null;
			if (dataRow==null) {
				// This means that the record was either added or PK updated.
				value=""; 
			}
			else {
				if (columnName.equalsIgnoreCase(FIELD_AUDIT_LAST_UPDATED_BY)) {
					value=dataRow.getRscLastUpdatedByName();
				}
				else
					if (columnName.equalsIgnoreCase(FIELD_AUDIT_LAST_UPDATE_DATE)) {
						value=dataRow.getRscLastUpdateDate();
					}
					else
						if (columnName.equalsIgnoreCase(FIELD_AUDIT_CREATED_BY)) {
							value=dataRow.getRscCreatedByName();
						}
						else
							if (columnName.equalsIgnoreCase(FIELD_AUDIT_CREATION_DATE)) {
								value=dataRow.getRscCreationDate();
							}
							else {
								throw new Exception("column name is not valid: '"+columnName+"'");
							}
			}
			row.add(value);
		}
		tableModel.addRow(row);
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static JTable createRecordTable(List<SnapshotGridRecord> snapshotGridRecords) throws Exception {		
		final Vector<String> columnNames = new Vector<String>();
		columnNames.add(COLUMN_HEADING_FIELDS);
		for (SnapshotGridRecord snapshotGridRecord:snapshotGridRecords) {
			String name=snapshotGridRecord.getName();
			columnNames.add(name);
		}

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		final JTable table = new JTable(model) {

			private static final long serialVersionUID = 1L;
			
			@SuppressWarnings({ })
			@Override
			public Class getColumnClass(int column) {
				return String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int colIndex) {
				Component component = super.prepareRenderer(renderer, rowIndex, colIndex);	
				component.setBackground(UIConstants.COLOR_GREY);
				component.setForeground(Color.black);
				String currentFieldName= (String) getValueAt(rowIndex,0);
				if (currentFieldName.equalsIgnoreCase(FIELD_AUDIT_NAME)) {
					component.setBackground(Color.decode("#4a4f4e"));
					component.setForeground(Color.white);
				}
				else
					if (colIndex==0) {
						component.setBackground(Color.LIGHT_GRAY);
					}
					else {
						String currentCellValue= (String) getValueAt(rowIndex,colIndex);	
						if (currentCellValue==null) {
							currentCellValue="";
						}
						String previousCellValue= (String) getValueAt(rowIndex,colIndex-1);	
						if (previousCellValue==null) {
							previousCellValue="";
						}
						boolean isColorChange=false;
						if ( (colIndex-1) !=0 ) {
							if (!currentCellValue.equals(previousCellValue) ) {
								isColorChange=true;
								component.setBackground(Color.decode("#fbf468"));
							}
						}
						if (!isColorChange) {
							if (rowIndex%2 == 0){
								component.setBackground(Color.WHITE);
							}
							else {
								Color alternateColor =Color.decode("#dbdbdb");
								component.setBackground(alternateColor);
							}
						}
					}

				return component;
			}

			// public boolean getScrollableTracksViewportWidth() {
			//	   return getPreferredSize().width < getParent().getWidth();
			//	 }

		};

		UIUtils.setColumnSize(table,COLUMN_HEADING_FIELDS,COLUMN_HEADING_FIELDS_WIDTH,COLUMN_HEADING_FIELDS_WIDTH,false);
		for (SnapshotGridRecord snapshotGridRecord:snapshotGridRecords) {
			String name=snapshotGridRecord.getName();
			UIUtils.setColumnSize(table,name,COLUMN_HEADING_BASELINE_WIDTH,COLUMN_HEADING_BASELINE_WIDTH,false);
		}

		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setSelectionForeground(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		/*
		 * IMPORTANT: THIS WILL SCREW UP THAI CHARACTERS (UTF8) SHOWING SQUARES INSTEAD!!!
		 */
		//table.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
		
		table.setBackground(Color.decode("#DBDBDB"));
		table.setRowHeight(20);
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(10000,35));
		//table.setFillsViewportHeight( true );

		return table;
	}

	private static JScrollPane createJScrollPane(JTable table) throws Exception {
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}


}
