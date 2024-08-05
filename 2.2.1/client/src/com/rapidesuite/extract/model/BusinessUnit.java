package com.rapidesuite.extract.model;

public class BusinessUnit {
	
	private String name;
	private Long id;
	private int coaId;
	
	public BusinessUnit(String name,Long id, int coaId) {
		this.name=name;
		this.id=id;
		this.coaId=coaId;
	}

	public String getName() {
		return name;
	}

	public Long getId() {
		return id;
	}

	public int getCoaId() {
		return coaId;
	}
	
}