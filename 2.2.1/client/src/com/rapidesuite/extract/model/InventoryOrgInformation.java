package com.rapidesuite.extract.model;

public class InventoryOrgInformation {

	private GenericObjectInformation genericObjectInformation;
	
	public InventoryOrgInformation(GenericObjectInformation genericObjectInformation) {
		this.genericObjectInformation=genericObjectInformation;
	}

	public String getInventoryOrgId() {
		return genericObjectInformation.getValue1();
	}
	public void setInventoryOrgId(String inventoryOrgId) {
		genericObjectInformation.setValue1(inventoryOrgId);
	}
	public String getInventoryOrgName() {
		return genericObjectInformation.getValue2();
	}
	public void setInventoryOrgName(String inventoryOrgName) {
		genericObjectInformation.setValue2(inventoryOrgName);
	}
	
}
