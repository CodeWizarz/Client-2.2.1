package com.rapidesuite.build.core.shell;

import java.util.List;

public class Case
{

	private String caseQuestion;
	private String caseAnswer;
	private List<QA> caseQAList;

	public String getCaseAnswer()
	{
		return caseAnswer;
	}

	public void setCaseAnswer(String caseAnswer)
	{
		this.caseAnswer = caseAnswer;
	}

	public String getCaseQuestion()
	{
		return caseQuestion;
	}

	public void setCaseQuestion(String caseQuestion)
	{
		this.caseQuestion = caseQuestion;
	}

	public List<QA> getCaseQAList()
	{
		return caseQAList;
	}

	public void setCaseQAList(List<QA> caseQAList)
	{
		this.caseQAList = caseQAList;
	}

}