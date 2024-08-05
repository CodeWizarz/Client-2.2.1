package com.rapidesuite.extract.model;

public class LegalEntityInformation {

	private GenericObjectInformation genericObjectInformation;
	
	public LegalEntityInformation(GenericObjectInformation genericObjectInformation) {
		this.genericObjectInformation=genericObjectInformation;
	}

	public String getLegalEntityId() {
		return genericObjectInformation.getValue1();
	}
	public void setLegalEntityId(String legalEntityId) {
		genericObjectInformation.setValue1(legalEntityId);
	}
	public String getLegalEntityName() {
		return genericObjectInformation.getValue2();
	}
	public void setLegalEntityName(String legalEntityName) {
		genericObjectInformation.setValue2(legalEntityName);
	}
	
}
