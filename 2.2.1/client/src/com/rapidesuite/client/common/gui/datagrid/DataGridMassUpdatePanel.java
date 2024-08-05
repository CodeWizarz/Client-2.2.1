package com.rapidesuite.client.common.gui.datagrid;

import java.awt.Dimension;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.openswing.swing.client.DateControl;
import org.openswing.swing.client.NumericControl;
import org.openswing.swing.client.TextControl;
import org.openswing.swing.domains.java.Domain;
import org.openswing.swing.domains.java.DomainPair;
import org.openswing.swing.table.columns.client.Column;
import org.openswing.swing.table.columns.client.ComboColumn;
import org.openswing.swing.table.columns.client.DecimalColumn;
import org.openswing.swing.table.columns.client.IntegerColumn;
import org.openswing.swing.table.columns.client.TextColumn;
import org.openswing.swing.util.client.ClientSettings;

@SuppressWarnings("serial")
public class DataGridMassUpdatePanel extends JPanel {

	private List<DataGridColumn> dataGridUserDefinedColumns;
	private Map<DataGridColumn,JCheckBox> columnToStatusMap;
	private Map<DataGridColumn,JComponent> columnToComponentMap;

	public DataGridMassUpdatePanel(List<DataGridColumn> dataGridUserDefinedColumns) throws Exception {
		this.dataGridUserDefinedColumns=dataGridUserDefinedColumns;
		columnToStatusMap=new HashMap<DataGridColumn,JCheckBox>();
		columnToComponentMap=new HashMap<DataGridColumn,JComponent>();
		initComponents();
	}

	private void initComponents() throws Exception {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for(DataGridColumn dataGridUserDefinedColumn:dataGridUserDefinedColumns) {
			if (!dataGridUserDefinedColumn.getColumn().isEditableOnEdit()) {
				continue;
			}
			JPanel tempPanel=new JPanel();
			tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
			JComponent component = createColumnComponent(dataGridUserDefinedColumn);
			JCheckBox checkBox=new JCheckBox(dataGridUserDefinedColumn.getColumnTitle());
			checkBox.setMaximumSize(new Dimension(DataGridConstants.PANEL_MASS_UPDATE_COMPONENT_WIDTH,DataGridConstants.PANEL_MASS_UPDATE_COMPONENT_HEIGHT));

			columnToStatusMap.put(dataGridUserDefinedColumn,checkBox);
			columnToComponentMap.put(dataGridUserDefinedColumn,component);
			
			tempPanel.add(checkBox);
			tempPanel.add(Box.createRigidArea(new Dimension(2,0)));
			tempPanel.add(component);
			this.add(tempPanel);
			this.add(Box.createRigidArea(new Dimension(0,2)));
		}
	}

	private JComponent createColumnComponent(DataGridColumn dataGridUserDefinedColumn) throws Exception {
		JComponent result=null;
		Column column=dataGridUserDefinedColumn.getColumn();
		if (column.getColumnType()==Column.TYPE_COMBO) {
			/*
			DataGridComboColumn dcc=(DataGridComboColumn)dataGridUserDefinedColumn;
			List<SimpleEntry<String,String>> pairs=dcc.getKeyValuePairs();
			Vector<String> items = new Vector<String>();
			for(SimpleEntry<String,String> pair:pairs) {
				items.add(pair.getValue());
			}
			*/
			Domain domain = null;
			if (((ComboColumn)column).getDomainId()!=null) {
				domain = ClientSettings.getInstance().getDomain(((ComboColumn)column).getDomainId());
			}
			else {
				domain = ((ComboColumn)column).getDomain();
			}
			DomainPair[] pairs = domain.getDomainPairList();
			Vector<String> items = new Vector<String>();
			for(int i=0;i<pairs.length;i++) {
				items.add(ClientSettings.getInstance().getResources().getResource(pairs[i].getDescription()));
			}
			JComboBox comboBox=new JComboBox(items);
			result=comboBox;
		}
		else
		if (column.getColumnType()==Column.TYPE_CHECK) {
			JCheckBox checkBox=new JCheckBox();
			result=checkBox;
		}
		else
		if (column.getColumnType()==Column.TYPE_DATE ||
				column.getColumnType()==Column.TYPE_DATE_TIME ||
				column.getColumnType()==Column.TYPE_TIME
		)
		{
			DateControl dateControl=new DateControl();
			dateControl.setDateType(column.getColumnType());
			result=dateControl;
		}
		else
		if (column.getColumnType()== Column.TYPE_INT){
			NumericControl num = new NumericControl();
			num.setDecimals(0);
			num.setMaxValue(((IntegerColumn)column).getMaxValue());
			num.setMinValue(((IntegerColumn)column).getMinValue());
			num.setGrouping(((IntegerColumn)column).isGrouping());
			result=num;
		}
		else
		if (column.getColumnType()==Column.TYPE_DEC ||
				column.getColumnType()==Column.TYPE_PERC)
		{
			NumericControl num = new NumericControl();
			num.setDecimals(((DecimalColumn)column).getDecimals());
			num.setMaxValue(((DecimalColumn)column).getMaxValue());
			num.setMinValue(((DecimalColumn)column).getMinValue());
			num.setGrouping(((DecimalColumn)column).isGrouping());
			result=num;
		}
		else
		if (column.getColumnType()==Column.TYPE_TEXT)
		{
			TextControl edit = new TextControl();
			if (((TextColumn)column).isRpadding() && (((TextColumn)column).getMaxCharacters()>0)){
				edit.setRpadding(true);
			}
			edit.setMaxCharacters(((TextColumn)column).getMaxCharacters());
			result=edit;
		}
		else
		if (column.getColumnType()==Column.TYPE_LOOKUP)
		{
				TextControl edit = new TextControl();
				result=edit;
		}
		else{
			throw new Exception("unsupported column type: "+column.getColumnType());
		}
		result.setMaximumSize(new Dimension(DataGridConstants.PANEL_MASS_UPDATE_COMPONENT_WIDTH,DataGridConstants.PANEL_MASS_UPDATE_COMPONENT_HEIGHT));
		
		return result;
	}
	
	public Map<DataGridColumn,String> getDataGridColumnsToValueMap()
	{
		Map<DataGridColumn,String> res=new HashMap<DataGridColumn,String>();
		Set<DataGridColumn> set=columnToStatusMap.keySet();
		Iterator<DataGridColumn> iterator=set.iterator();
		while (iterator.hasNext()) {
			DataGridColumn dataGridColumn=iterator.next();
			JCheckBox checkBox=columnToStatusMap.get(dataGridColumn);
			if (checkBox.isSelected()) {
				JComponent component=columnToComponentMap.get(dataGridColumn);
				String value="";
				if (component instanceof JComboBox) {
					JComboBox c=(JComboBox)component;
					value=((String)c.getSelectedItem());
				}
				else
				if (component instanceof JCheckBox) {
					JCheckBox c=(JCheckBox)component;
					value=Boolean.valueOf(c.isSelected()).toString();
				}
				else
				if (component instanceof DateControl) {
					DateControl c=(DateControl)component;
					Date date=(Date) c.getValue();
					DateFormat df = new SimpleDateFormat(DataGridConstants.ORACLE_DATE_FORMAT);
					java.util.Date utilDate=new java.util.Date(date.getTime());
					value= df.format(utilDate);
				}
				else
				if (component instanceof NumericControl) {
					NumericControl c=(NumericControl)component;
					BigDecimal bd=(BigDecimal) c.getValue();
					if (bd!=null) {
						value=bd.toString();
					}
				}
				else
				if (component instanceof TextControl) {
					value=(String) ((TextControl)component).getValue();
				}
				
				res.put(dataGridColumn,value);
			}
		}
		return res;
	}

}
