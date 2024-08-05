/**************************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/Case.java $:
 * $Id: Case.java 31694 2013-03-04 06:33:20Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import java.util.ArrayList;
import java.util.List;

import com.rapidesuite.client.common.util.FileUtils;

public class Case
{

	private List<Question> questions;
	private String pattern;
	private Response response;
	private Question parent;
	private int currentIndex;

	public Case()
	{
		questions = new ArrayList<Question>();
		currentIndex = -1;
	}

	public void printPattern()
	{
		FileUtils.println("  - '" + pattern + "'");
	}

	public Question moveToNextQuestion()
	{
		if ( questions.isEmpty() )
		{
			return getParent().moveToNextQuestion();
		}
		if ( (currentIndex + 1) == questions.size() )
		{
			return getParent().moveToNextQuestion();
		}
		currentIndex++;
		Question question = questions.get(currentIndex);
		return question;
	}

	public void setResponse(Response response)
	{
		this.response = response;
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}

	public void addQuestion(Question question)
	{
		questions.add(question);
	}

	public String toString(String indent)
	{
		if ( questions.isEmpty() )
		{
			return indent + "<Case ref=" + this + " parentQuestion=" + getParent() + " >" + pattern + response.toString() + "\n" + indent + "</Case>";
		}
		StringBuffer res = new StringBuffer("\n" + indent + "<Case ref=" + this + " parentQuestion=" + getParent() + " >");
		res.append(pattern + response.toString());
		for ( int i = 0; i < questions.size(); i++ )
		{
			Question question = questions.get(i);
			res.append("\n" + question.toString(indent + "   "));
			if ( (i + 1) != questions.size() )
			{
				res.append("\n");
			}
		}
		res.append("\n" + indent + "</Case>");
		return res.toString();
	}

	public Questions getTree()
	{
		return getParent().getTree();
	}

	public String shortDesc()
	{
		return "CASE:  " + "\n" + "      - ref:" + this + "\n" + "      - parentQuestion:" + getParent() + "\n" + "      - pattern:" + pattern + "\n" + "      - questions:"
				+ questions + "\n" + "\n";
	}

	public int getCurrentIndex()
	{
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex)
	{
		this.currentIndex = currentIndex;
	}

	public List<Question> getQuestions()
	{
		return questions;
	}

	public String getPattern()
	{
		return pattern;
	}

	public Response getResponse()
	{
		return response;
	}

	public Question getParent()
	{
		return parent;
	}

	public void setParent(Question parent)
	{
		this.parent = parent;
	}

}