package com.rapidesuite.extract.model;

public class EnterpriseInformation {

	private GenericObjectInformation genericObjectInformation;
	
	public EnterpriseInformation(GenericObjectInformation genericObjectInformation) {
		this.genericObjectInformation=genericObjectInformation;
	}

	public String getEnterpriseId() {
		return genericObjectInformation.getValue1();
	}
	public void setEnterpriseId(String enterpriseId) {
		genericObjectInformation.setValue1(enterpriseId);
	}
	public String getEnterpriseName() {
		return genericObjectInformation.getValue2();
	}
	public void setEnterpriseName(String enterpriseName) {
		genericObjectInformation.setValue2(enterpriseName);
	}
	
}
