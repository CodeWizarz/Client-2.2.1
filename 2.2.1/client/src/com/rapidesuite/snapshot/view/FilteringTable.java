package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import com.rapidesuite.inject.InjectMain;

public abstract class FilteringTable {
	
	private JScrollPane fixedScrollPane;
	private JTable fixedTable;
	private JTable dataTable;
	private String editedFilterValue;
	private TableRowSorter<TableModel> sorter;
	private final static int ROW_HEIGHT=23;
	
	@SuppressWarnings({ "serial", "unused", "rawtypes" })
	public FilteringTable(final Vector<String> columnNames,JScrollPane variableScroll,final JTable dataTable,final int headerHeight
			,boolean isRefreshEveryListOnUse) {
		this.dataTable=dataTable;
		final Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		Vector<Object> row = new Vector<Object>();
		data.add(row);
		for (String columnName:columnNames) {
			row.add("");
		}
		
		AbstractTableModel fixedModel = new AbstractTableModel() {     
			public int getColumnCount() { return columnNames.size(); }
			public int getRowCount() { return 1; }
			public Object getValueAt(int row, int col) {
				return data.get(row).get(col);
			}
			public String getColumnName(int col) {
				return (String)columnNames.get(col);
			}
			@Override
			public boolean isCellEditable(int row, int column) {
				return true;
			}
			public void setValueAt(Object obj, int row, int col) {
				data.get(row).setElementAt(obj, col);
			}
		};

		fixedTable = new JTable( fixedModel ) {

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int columnIndex){
				Component c=super.prepareRenderer(renderer, rowIndex, columnIndex);

				String filterModelValue=(String) fixedTable.getModel().getValueAt(rowIndex, columnIndex);
				if (filterModelValue==null || filterModelValue.isEmpty()) {
					c.setBackground(Color.decode("#91d1d9"));
				}
				else {
					c.setBackground(new Color(222,234,157) );
				}

				((JLabel)c).setHorizontalAlignment(SwingConstants.LEFT);
				
				showBalloons(rowIndex,columnIndex,fixedTable);
				
				return c;
			}

		};
		fixedTable.setTableHeader(new JTableHeader(fixedTable.getColumnModel()) {
			@Override public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = headerHeight;
				return d;
			}
		});
		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem applyFilteringMenuItem=new JMenuItem("Apply filtering");
	    popupMenu.add(applyFilteringMenuItem);
	    applyFilteringMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	applyFiltering();
            }
        });
	    JMenuItem resetFilteringMenuItem=new JMenuItem("Reset filtering");
	    popupMenu.add(resetFilteringMenuItem);
	    resetFilteringMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	resetFiltering();
            }
        });
	    fixedTable.getTableHeader().addMouseListener(new MouseAdapter() {
	         public void mouseClicked(MouseEvent me) {
	            if (SwingUtilities.isRightMouseButton(me))
	               popupMenu.show(fixedTable, me.getX(), me.getY());
	         }
	      });
		fixedTable.getTableHeader().setReorderingAllowed(false);
		fixedTable.setRowSelectionAllowed(false);
		fixedTable.setSelectionForeground(Color.BLACK);
		fixedTable.setFont(new Font("Arial", Font.PLAIN, InjectMain.FONT_SIZE_SMALL));
		fixedTable.setRowHeight(ROW_HEIGHT);
		fixedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		for (String columnName:columnNames) {
			TableColumn tableColumnSource = dataTable.getColumn(columnName);
			TableColumn tableColumn = fixedTable.getColumn(columnName);
			tableColumn.setMinWidth(tableColumnSource.getMinWidth());
			tableColumn.setMaxWidth(tableColumnSource.getMaxWidth());
			tableColumn.setPreferredWidth(tableColumnSource.getPreferredWidth());
		}
		
		MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer();
		Enumeration enumeration = fixedTable.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			((TableColumn) enumeration.nextElement()).setHeaderRenderer(renderer);
		}
				
		sorter = new TableRowSorter<TableModel>(dataTable.getModel());
		dataTable.setRowSorter(sorter);
		addListenerToSorter(sorter,dataTable);
				
		JComboBox<String> comboBox=new JComboBox<String>();
		final JTextComponent tc = (JTextComponent) comboBox.getEditor().getEditorComponent();
		tc.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = tc.getText();
                editedFilterValue=text;
                filter();
            }
            @Override
            public void removeUpdate(DocumentEvent e) 
            { 
                String text = tc.getText();
                editedFilterValue=text;
                filter();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                String text = tc.getText();
                editedFilterValue=text;
                filter();
            }
          });
		
		AutoSuggestTableCellEditor singleclickComboxBoxCellEditor = new AutoSuggestTableCellEditor(this,comboBox,isRefreshEveryListOnUse);
		singleclickComboxBoxCellEditor.setClickCountToStart(1);
		for (int i = 0; i < fixedTable.getColumnCount(); i++) {
			fixedTable.setDefaultEditor(fixedTable.getColumnClass(i), singleclickComboxBoxCellEditor);
		} 

		TableColumnModelListener tableColumnModelListener = new TableColumnModelListener() {
			public void columnAdded(TableColumnModelEvent e) {
			}

			public void columnMarginChanged(ChangeEvent e) {
				TableColumn tableColumn=fixedTable.getTableHeader().getResizingColumn();
				if (tableColumn==null) {
					return;
				}
				TableColumn column= dataTable.getColumn(tableColumn.getIdentifier());
				column.setWidth(tableColumn.getWidth());
				column.setMinWidth(tableColumn.getWidth());
				column.setMaxWidth(tableColumn.getWidth());
			}

			public void columnMoved(TableColumnModelEvent e) {
			}

			public void columnRemoved(TableColumnModelEvent e) {
			}

			public void columnSelectionChanged(ListSelectionEvent e) {
			}
		};
		TableColumnModel columnModel = fixedTable.getColumnModel();
		columnModel.addColumnModelListener(tableColumnModelListener);

		fixedScrollPane= new JScrollPane(fixedTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());
		fixedTable.setFillsViewportHeight(true);

		final JScrollBar fixedScrollBar = fixedScrollPane.getHorizontalScrollBar();
		final JScrollBar variableScrollBar = variableScroll.getHorizontalScrollBar();
		variableScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				fixedScrollBar.setValue(e.getValue());
			}
		});
	}

	protected abstract void showBalloons(int rowIndex,int columnIndex, JTable fixedTable);

	protected void resetFiltering() {		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Enumeration<TableColumn> columns=fixedTable.getColumnModel().getColumns();
				while (columns.hasMoreElements()) {
					TableColumn column=columns.nextElement();
					int colIndex=column.getModelIndex();
					fixedTable.getModel().setValueAt("",0, colIndex);
				}		
				fixedTable.repaint();
				applyFiltering();
			}
		});
	}
	
	public JScrollPane getFixedScrollPane() {
		return fixedScrollPane;
	}
		
	public void applyFiltering() {
		filter();
	}
		
	private void filter(){
		final int editingColumn=fixedTable.getEditingColumn();
		RowFilter<Object, Object> filter = new RowFilter<Object, Object>() {
			@SuppressWarnings("rawtypes")
			public boolean include(Entry entry) {
				return includeForApplyFilteringManualEntry(entry,editingColumn); 
			}
		};
	    sorter.setRowFilter(filter);
	}
	
	@SuppressWarnings("rawtypes")
	private boolean includeForApplyFilteringManualEntry(Entry entry,int editingColumn) {
		boolean matches=true;
		Enumeration<TableColumn> columns=fixedTable.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			TableColumn column=columns.nextElement();
			int colIndex=column.getModelIndex();
			String filterModelValue=(String) fixedTable.getModel().getValueAt(0, colIndex);
			//System.out.println("filterModelValue:"+filterModelValue);
			String tmp=filterModelValue;
			if (colIndex==editingColumn) {
				// the filtervalue will be blank if the cell is being edited so we pull the value from the textfield
				tmp=editedFilterValue;
			}	
			if (tmp==null) {
				tmp="";
			}
			if (tmp.isEmpty()) {
				continue;
			}
			Object rowObject=entry.getValue(colIndex);
			String value=null;
			if (rowObject instanceof Boolean) {
				boolean isSelected=((Boolean)rowObject).booleanValue();
				if (isSelected) {
					value="Selected";
				}
				else {
					value="Unselected";
				}
			}
			else {
				value=(String)rowObject;
			}
			if (value==null) {
				value="";
			}
			else {
				value=value.replaceAll("\n"," ");
			}
			try{
				if (tmp.equalsIgnoreCase(">")) {
					matches=true;
				}
				else
				if (tmp.startsWith(">") || tmp.startsWith("=")) {
					// NUMBER MATCHING
					String sourceNumStr=tmp.substring(1);
					if (sourceNumStr==null || sourceNumStr.isEmpty()) {
						matches=true;
					}
					else {
						if (value==null || value.isEmpty()) {
							matches=false;
						}
						else {
							try{
								Integer sourceNum=Integer.valueOf(sourceNumStr);
								Number numValue=NumberFormat.getNumberInstance(java.util.Locale.US).parse(value);
								if (tmp.startsWith(">")) {
									if (numValue.intValue() > sourceNum) {
										matches=true;
									}
									else {
										matches=false;
									}
								}
								else {
									if (numValue.intValue() == sourceNum) {
										matches=true;
									}
									else {
										matches=false;
									}
								}
								
							}
							catch(NumberFormatException e) {
								matches=true;
							}
						}
					}
				}
				else {
					// STRING MATCHING
					//String patternString = ".*"+tmp+".*";
					tmp = escapeRE(tmp);
					tmp = tmp.replaceAll("\\r\\n|\\r|\\n", " ");
					//System.out.println("tmp:"+tmp);
					String patternString = tmp.replaceAll("%",".*");
					//System.out.println("patternString:"+patternString);
					Pattern pattern = Pattern.compile(patternString,Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(value);
					matches = matcher.matches();
				}
				if (!matches) {
					break;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return matches;
	}
	
	 public static String escapeRE(String str) {
	     Pattern escaper = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");
		 return escaper.matcher(str).replaceAll("\\\\$0");
	}

	@SuppressWarnings({ "rawtypes" })
	private void addListenerToSorter(RowSorter rowSorter,final JTable table) {

		rowSorter.addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
				final int newRowCount = table.getRowCount();
				SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	sorterChangedAction(newRowCount);
                    }
                });
			}
		});
	}
	
	public abstract void sorterChangedAction(int newRowCount);

	public void setColumnFilterValue(String columnName,String text) {
		int colIndex=fixedTable.getColumnModel().getColumnIndex(columnName);
		fixedTable.getModel().setValueAt(text,0, colIndex);
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	fixedTable.repaint();
            }
        });
	}

	public void removeRowFilter() {
		sorter.setRowFilter(null);
	}

	public JTable getDataTable() {
		return dataTable;
	}

	public JTable getFixedTable() {
		return fixedTable;
	}

}
