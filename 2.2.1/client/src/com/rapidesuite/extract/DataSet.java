package com.rapidesuite.extract;

import java.io.File;
import java.util.List;

import com.rapidesuite.configurator.domain.Inventory;

public class DataSet {

	private String identifier;
	private String description;
	private String sqlQuery;
	private List<DataSetElement> dataSetElementList;
	private List<DataSetParameter> dataSetParameterList;
	private Inventory inventory;
	private File sqlFile;
	
	public String getSqlQuery() {
		return sqlQuery;
	}
	
	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}
	
	public List<DataSetElement> getDataSetElementList() {
		return dataSetElementList;
	}
	
	public void setDataSetElementList(List<DataSetElement> dataSetElementList) {
		this.dataSetElementList = dataSetElementList;
	}
	
	public List<DataSetParameter> getDataSetParameterList() {
		return dataSetParameterList;
	}
	
	public void setDataSetParameterList(List<DataSetParameter> dataSetParameterList) {
		this.dataSetParameterList = dataSetParameterList;
	}
		
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setSQLFile(File sqlFile) {
		this.sqlFile=sqlFile;
	}

	public void setInventory(Inventory inventory) {
		this.inventory=inventory;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public File getSQLFile() {
		return sqlFile;
	}

	public void setIdentifier(String identifier) {
		this.identifier=identifier;
	}
	
}
