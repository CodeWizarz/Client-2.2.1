package com.rapidesuite.build.core.shell;

import java.util.ArrayList;

class AllQA
{

	private ArrayList<QA> structure;
	private int defaultWaitingTime;

	public int getDefaultWaitingTime()
	{
		return defaultWaitingTime;
	}

	public void setDefaultWaitingTime(int defaultWaitingTime)
	{
		this.defaultWaitingTime = defaultWaitingTime;
	}

	public void addToStructure(QA qa)
	{
		structure.add(qa);
	}

	public ArrayList<QA> getStructure()
	{
		return structure;
	}

	public void setStructure(ArrayList<QA> structure)
	{
		this.structure = structure;
	}

}