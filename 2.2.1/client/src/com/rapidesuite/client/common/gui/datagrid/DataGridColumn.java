package com.rapidesuite.client.common.gui.datagrid;

import org.openswing.swing.table.columns.client.Column;

public class DataGridColumn {
	
	private String attributeName;
	private String attributeDescription;
	private Class<?> attributeClass;
	private Column column; 
	private boolean isRequired;
	private int columnSequence;
	private DataGridLookupController dataGridLookupController;
	private boolean isUpdatable;
	private boolean isResetable;
	private boolean isPCColumn;
	
	public DataGridColumn(String attributeName,String attributeDescription,Class<?> attributeClass,
			Column column,boolean isRequired,boolean isUpdatable,boolean isResetable,boolean isPCColumn){
		this(attributeName,attributeDescription,attributeClass,column,isRequired,-1,isUpdatable,isResetable,isPCColumn);
	}
	
	public DataGridColumn(String attributeName,String attributeDescription,Class<?> attributeClass,
			Column column,boolean isRequired,int columnSequence,boolean isUpdatable,boolean isResetable,boolean isPCColumn){
		this.attributeName=attributeName;
		this.attributeDescription=attributeDescription;
		this.attributeClass=attributeClass; 
		this.column=column;
		this.isRequired=isRequired;
		this.columnSequence=columnSequence;
		this.isUpdatable=isUpdatable;
		this.isResetable=isResetable;
		this.isPCColumn=isPCColumn;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public String getAttributeDescription() {
		return attributeDescription;
	}

	public Class<?> getAttributeClass() {
		return attributeClass;
	}

	public Column getColumn() {
		return column;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public DataGridLookupController getDataGridLookupController() {
		return dataGridLookupController;
	}

	public void setDataGridLookupController(
			DataGridLookupController dataGridLookupController) {
		this.dataGridLookupController = dataGridLookupController;
	}

	public String getColumnTitle() {
		String toReturn=attributeDescription;
		if (columnSequence!=-1) {
			toReturn=String.valueOf(columnSequence)+". "+toReturn;
		}
		if (isRequired) {
			toReturn+=" (Required)";
		}
		if (isPCColumn) {
			toReturn+=" (P/C)";
		}
		return toReturn;
	}

	public boolean isUpdatable() {
		return isUpdatable;
	}

	public boolean isResetable() {
		return isResetable;
	}

	@Override
	public String toString()
	{
		return "DataGridColumn [attributeName=" + attributeName + ", attributeDescription=" + attributeDescription + ", attributeClass=" + attributeClass + ", column=" + column
				+ ", isRequired=" + isRequired + ", columnSequence=" + columnSequence + ", dataGridLookupController=" + dataGridLookupController + ", isUpdatable=" + isUpdatable
				+ ", isResetable=" + isResetable + ", isPCColumn=" + isPCColumn + "]";
	}
	
}
