package com.rapidesuite.snapshot.model;

public class OperatingUnit extends MapGeneric{

	private int coaId;
	
	public OperatingUnit(String name, Long id) {
		super(name, id);
	}
	
	public OperatingUnit(String name,Long id, int coaId) {
		super(name, id);
		this.coaId=coaId;
	}

	public int getCoaId() {
		return coaId;
	}
	
}
