package com.rapidesuite.extract;

public class DataSetElement {

	private String name;
	private String value;
	private String label;
	private String dataType;
	private int fieldOrder;
	private String sampleValue;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public int getFieldOrder() {
		return fieldOrder;
	}
	public void setFieldOrder(int fieldOrder) {
		this.fieldOrder = fieldOrder;
	}
	public void setSampleValue(String sampleValue) {
		this.sampleValue=sampleValue;
	}
	public String getSampleValue() {
		return sampleValue;
	}
	
}
