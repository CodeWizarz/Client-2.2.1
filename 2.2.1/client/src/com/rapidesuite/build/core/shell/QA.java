package com.rapidesuite.build.core.shell;

import java.util.List;

public class QA
{

	private String question;
	private String answer;
	private boolean optional;
	private int timeout;
	private List<Case> cases;

	public boolean isOptional()
	{
		return optional;
	}

	public void setOptional(boolean optional)
	{
		this.optional = optional;
	}

	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public List<Case> getCases()
	{
		return cases;
	}

	public void setCases(List<Case> cases)
	{
		this.cases = cases;
	}

	public String getQuestion()
	{
		return question;
	}

	public void setQuestion(String question)
	{
		this.question = question;
	}

	public String getAnswer()
	{
		return answer;
	}

	public void setAnswer(String answer)
	{
		this.answer = answer;
	}

}