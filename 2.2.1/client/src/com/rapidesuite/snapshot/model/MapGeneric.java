package com.rapidesuite.snapshot.model;

public class MapGeneric {
	
	private Long id;
	private String name;
	
	public MapGeneric(String name,Long id) {
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