package com.rapidesuite.extract.model;

public class LedgerInformation {

	private GenericObjectInformation genericObjectInformation;
	
	public LedgerInformation(GenericObjectInformation genericObjectInformation) {
		this.genericObjectInformation=genericObjectInformation;
	}

	public String getLedgerId() {
		return genericObjectInformation.getValue1();
	}
	public void setLedgerId(String ledgerId) {
		genericObjectInformation.setValue1(ledgerId);
	}
	public String getLedgerName() {
		return genericObjectInformation.getValue2();
	}
	public void setLedgerName(String ledgerName) {
		genericObjectInformation.setValue2(ledgerName);
	}
	
}
