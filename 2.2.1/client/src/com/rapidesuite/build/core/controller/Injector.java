package com.rapidesuite.build.core.controller;

import java.io.File;

import org.springframework.util.Assert;

import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;

public class Injector
{

	private String name;
	private CoreConstants.INJECTOR_TYPE type;
	private int index;
	private String status;

	public Injector(String name, CoreConstants.INJECTOR_TYPE type, int index)
	{
		this.name = name;
		this.type = type;
		this.index = index;
	}

	public int getIndex()
	{
		return index;
	}

	public String getName()
	{
		return name;
	}

	public CoreConstants.INJECTOR_TYPE getType()
	{
		return type;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public String getNameWithoutExtension() {
		if (this.name == null) {
			return null;
		} else {
			return CoreUtil.getFileNameWithoutExtension(new File(this.name));
		}
	}
	
	public int getNumberFromUserPerspective() {
		Assert.notNull(name);
		Assert.isTrue(name.matches("^\\d+[^\\d].*$"));
		final String numberStr = name.replaceAll("[^\\d].*$", "");
		final int output = Integer.parseInt(numberStr);
		return output;
	}

}