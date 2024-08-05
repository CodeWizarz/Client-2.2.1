package com.rapidesuite.extract.model;

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

	/**
	 * Name only, no ID
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * Name only, no ID
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapGeneric other = (MapGeneric) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}