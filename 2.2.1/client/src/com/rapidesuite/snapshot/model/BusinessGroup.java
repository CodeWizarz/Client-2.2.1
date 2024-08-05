package com.rapidesuite.snapshot.model;

public class BusinessGroup extends MapGeneric{

	public BusinessGroup(String name, Long id) {
		super(name, id);
	}
	
	public BusinessGroup(Long id) {
		super(null, id);
	}
		
}