package com.rapidesuite.extract.model;

public class GDFElement  {

	private String prompt;
	private String columnName;
	private String valueSetCode;
	private String valueSetId;
		
	public String getPrompt() {
		return prompt;
	}
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	public String getValueSetCode() {
		return valueSetCode;
	}
	public void setValueSetCode(String valueSetCode) {
		this.valueSetCode = valueSetCode;
	}
	public String getValueSetId() {
		return valueSetId;
	}
	public void setValueSetId(String valueSetId) {
		this.valueSetId = valueSetId;
	}
		
}