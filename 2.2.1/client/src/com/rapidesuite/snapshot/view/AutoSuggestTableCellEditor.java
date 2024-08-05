package com.rapidesuite.snapshot.view;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

@SuppressWarnings("serial")
public class AutoSuggestTableCellEditor extends DefaultCellEditor {

	private FilteringTable filteringTable;
	private Map<Integer,Set<String>> columnPositionToDataValuesMap;
	private Map<Integer,String> columnPositionToFilteringValueMap;
	@SuppressWarnings("rawtypes")
	private AutoCompleteSupport autoCompleteSupport;
	private int currentColumn;
	private boolean isRefreshEveryListOnUse;
	
	public AutoSuggestTableCellEditor(FilteringTable filteringTable,JComboBox<String> comboBox,boolean isRefreshEveryListOnUse) {
		super(comboBox);
		this.filteringTable=filteringTable;
		currentColumn=-1;
		this.isRefreshEveryListOnUse=isRefreshEveryListOnUse;
		columnPositionToDataValuesMap=new HashMap<Integer,Set<String>>();
		columnPositionToFilteringValueMap=new HashMap<Integer,String>();
		comboBox.getEditor().getEditorComponent().setBackground(Color.YELLOW);
	    JTextField text = ((JTextField) comboBox.getEditor().getEditorComponent());
        text.setBackground(Color.YELLOW);
	}

	private Set<String> getDistinctValues(int column) { 
		DefaultTableModel model = (DefaultTableModel) filteringTable.getDataTable().getModel();
		Set<String> set=new TreeSet<String>();
		for (int i = 0; i < model.getRowCount(); i++){
			Object rowObject=model.getValueAt(i,column);
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
				if (value==null) {
					continue;
				}
			}
			set.add(value);
		}
		return set;
	}

	@SuppressWarnings({ "unchecked" })
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
			int row, int column) {
		final JComboBox<String> editor = (JComboBox<String>) super.getTableCellEditorComponent(table, value, isSelected,
				row, column);
		if (currentColumn!=column || isRefreshEveryListOnUse) {
			if (autoCompleteSupport!=null) {
				// we need to save the value to restore next time the user click on the field
				String previousColumnFilteringValue=(String) filteringTable.getFixedTable().getModel().getValueAt(row,currentColumn);
				columnPositionToFilteringValueMap.put(currentColumn, previousColumnFilteringValue);
				//System.out.println("column: "+currentColumn+" saving value: '"+previousColumnFilteringValue+"'");
				try{
					autoCompleteSupport.uninstall();
				}
				catch(java.lang.IllegalStateException e) {
					e.printStackTrace();
				}
			}
			currentColumn=column;
			Set<String> orderedSet=columnPositionToDataValuesMap.get(column);
			//if (orderedSet==null) {
				orderedSet=getDistinctValues(column);
				columnPositionToDataValuesMap.put(column,orderedSet);
			//}
			final List<String> tempListToAdd=new ArrayList<String>(orderedSet);
			Object[] elements = tempListToAdd.toArray();
			EventList<Object> eventList=GlazedLists.eventListOf(elements);
			autoCompleteSupport=AutoCompleteSupport.install(editor, eventList);
			final JTextComponent tc = (JTextComponent) editor.getEditor().getEditorComponent();
			String filteredValue=columnPositionToFilteringValueMap.get(column);
			
			String modelValue=(String) filteringTable.getFixedTable().getValueAt(0, column);
			if (filteredValue==null && modelValue!=null) {
				tc.setText(modelValue);
			}
			else {
				tc.setText(filteredValue);
			}
			//System.out.println("column: "+currentColumn+" restoring value: '"+filteredValue+"' modelValue:'"+modelValue+"'");
		}	
						
		return editor;
	}
	
}