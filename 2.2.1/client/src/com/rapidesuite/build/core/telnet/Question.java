/**************************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/Question.java $:
 * $Id: Question.java 31694 2013-03-04 06:33:20Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import java.util.ArrayList;
import java.util.List;

import com.rapidesuite.client.common.util.FileUtils;

public class Question
{

	private long timeout;
	private String pattern;
	private List<Case> cases;
	private Response response;
	private Case parent;
	private int currentMatchedCaseIndex;
	private boolean COMPLETED;
	private boolean optional;
	private Questions tree;

	public Question()
	{
		timeout = -1;
		currentMatchedCaseIndex = -1;
		cases = new ArrayList<Case>();
		COMPLETED = false;
		optional = false;
		tree = null;
	}

	public void setTree(Questions tree)
	{
		this.tree = tree;
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}

	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	public void addCase(Case caseO)
	{
		cases.add(caseO);
	}

	public String getResponse()
	{

		if ( currentMatchedCaseIndex == -1 )
		{
			if ( this.response == null )
			{
				return null;
			}
			String res = this.response.getResponse();
			return res;
		}
		Case caseTemp = cases.get(currentMatchedCaseIndex);
		return caseTemp.getResponse().getResponse();
	}

	public void setResponse(Response resp)
	{
		// com.rapidesuite.build.core.log.LogWriter.println("We set the response of:"+resp);
		this.response = resp;
	}

	public void setOptional(boolean optional)
	{
		this.optional = optional;
	}

	public boolean hasPattern(String text)
	{
		boolean res = false;
		if ( cases.isEmpty() )
		{
			if ( text.endsWith(pattern) || text.endsWith(pattern + " ") )
			{
				res = true;
			}
			else
			{
				res = false;
			}
		}
		else
		{
			for ( int i = 0; i < cases.size(); i++ )
			{
				Case caseTemp = cases.get(i);
				if ( text.endsWith(caseTemp.getPattern()) || text.endsWith(caseTemp.getPattern() + " ") )
				{
					currentMatchedCaseIndex = i;

					res = true;
					break;
				}
			}
		}

		if ( !res && optional )
		{
			// Let's check if the question is optional, meaning that if we
			// manage to match
			// to a pattern, then we take it, otherwise, we move to the next
			// question and
			// we check the pattern of the next question.
			Question q = this.moveToNextQuestion();
			// we need to go back to the root.
			Questions t = this.getTree();
			if ( q == null )
			{
				t.blindMoveToNextQuestion();
				q = t.getCurrentQuestion();
			}
			else
			{
				t.setCurrentQuestion(q);
			}

			if ( q != null )
			{
				return q.hasPattern(text);
			}
			return false;
		}
		return res;
	}

	public Questions getTree()
	{
		if ( parent == null )
			return tree;
		return parent.getTree();
	}

	public void printPatterns()
	{
		if ( cases.isEmpty() )
		{
			FileUtils.println("\n   '" + pattern + "'");
		}
		else
		{
			FileUtils.println("   (" + cases.size() + " CASES) ");
			for ( int i = 0; i < cases.size(); i++ )
			{
				Case caseTemp = cases.get(i);
				caseTemp.printPattern();
			}
		}
	}

	public Question moveToNextQuestion()
	{
		if ( currentMatchedCaseIndex == -1 || COMPLETED )
		{
			if ( parent == null )
			{
				return null;
			}
			return parent.moveToNextQuestion();
		}
		COMPLETED = true;
		Case caseTemp = cases.get(currentMatchedCaseIndex);
		return caseTemp.moveToNextQuestion();
	}

	public String toString(String indent)
	{
		if ( cases.isEmpty() )
		{
			return indent + "<Question optional=" + optional + " ref=" + this + " parentCase=" + parent + " tree:" + tree + " >" + pattern + response + "\n" + indent
					+ "</Question>";
		}
		StringBuffer res = new StringBuffer(indent + "<Question  optional=" + optional + " ref=" + this + " parentCase=" + parent + " tree:" + tree + " >");
		for ( int i = 0; i < cases.size(); i++ )
		{
			Case caseO = cases.get(i);
			res.append(caseO.toString(indent + "   "));
			if ( (i + 1) != cases.size() )
			{
				res.append("\n");
			}
		}
		res.append("\n" + indent + "</Question>");
		return res.toString();
	}

	public String shortDesc()
	{
		return "QUESTION:  " + "\n" + "      - ref:" + this + "\n" + "      - optional:" + optional + "\n" + "      - parentCase:" + parent + "\n" + "      - pattern:" + pattern
				+ "\n" + "      - cases:" + cases + "\n" + "      - tree:" + tree + "\n" + "\n";
	}

	public boolean isOptional()
	{
		return optional;
	}

	public void set_timeout(long _timeout)
	{
		this.timeout = _timeout;
	}

	public long getTimeout()
	{
		return timeout;
	}

	public void setParent(Case parent)
	{
		this.parent = parent;
	}

	public Case getParent()
	{
		return parent;
	}

}
