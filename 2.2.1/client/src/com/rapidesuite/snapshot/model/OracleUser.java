package com.rapidesuite.snapshot.model;

public class OracleUser {

	private String name;
	private Long id;
	
	public OracleUser(String name,Long id) {
		this.name=name;
		this.id=id;
	}

	public String getName() {
		return name;
	}

	public Long getId() {
		return id;
	}
	
}