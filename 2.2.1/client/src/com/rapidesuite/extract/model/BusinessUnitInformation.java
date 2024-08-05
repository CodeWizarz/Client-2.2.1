package com.rapidesuite.extract.model;

public class BusinessUnitInformation {

	private GenericObjectInformation genericObjectInformation;
	
	public BusinessUnitInformation(GenericObjectInformation genericObjectInformation) {
		this.genericObjectInformation=genericObjectInformation;
	}

	public String getBusinessUnitId() {
		return genericObjectInformation.getValue1();
	}
	public void setBusinessUnitId(String businessUnitId) {
		genericObjectInformation.setValue1(businessUnitId);
	}
	public String getBusinessUnitName() {
		return genericObjectInformation.getValue2();
	}
	public void setBusinessUnitName(String businessUnitName) {
		genericObjectInformation.setValue2(businessUnitName);
	}
	
}
