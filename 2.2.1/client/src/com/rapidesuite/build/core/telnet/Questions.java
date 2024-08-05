/**************************************************************
 * $Revision: 31060 $:
 * $Author: john.snell $:
 * $Date: 2013-01-09 12:42:28 +0700 (Wed, 09 Jan 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/Questions.java $:
 * $Id: Questions.java 31060 2013-01-09 05:42:28Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import java.util.ArrayList;
import java.util.List;

public class Questions
{

	private int currentQuestionIndex;
	private List<Question> listOfQuestion;
	private Question currentQuestion;
	private List<String> promptsEndWith;
	private List<List<String>> promptsInclude;

	public Questions()
	{
		listOfQuestion = new ArrayList<Question>();
		currentQuestionIndex = -1;
		promptsEndWith = new ArrayList<String>();
		promptsInclude = new ArrayList<List<String>>();
	}

	public boolean hasPrompt(String text)
	{
		for ( int i = 0; i < promptsEndWith.size(); i++ )
		{
			String prompt = promptsEndWith.get(i);
			if ( text.endsWith(prompt) )
				return true;
		}
		for ( int i = 0; i < promptsInclude.size(); i++ )
		{
			List<String> prompts = promptsInclude.get(i);
			String textTemp = text;
			boolean SUCCESS = true;
			for ( int j = 0; j < prompts.size(); j++ )
			{
				String ele = prompts.get(j);
				int index = textTemp.indexOf(ele);
				if ( index == -1 )
				{
					SUCCESS = false;
					break;
				}

				textTemp = textTemp.substring(index);
			}
			if ( SUCCESS )
				return true;
		}
		return false;
	}

	public Question getCurrentQuestion()
	{
		return currentQuestion;
	}

	public void setCurrentQuestion(Question q)
	{
		currentQuestion = q;
	}

	public void setTree()
	{
		for ( int i = 0; i < listOfQuestion.size(); i++ )
		{
			Question question = listOfQuestion.get(i);
			question.setTree(this);
		}
	}

	public void init()
	{
		currentQuestionIndex++;
		currentQuestion = listOfQuestion.get(currentQuestionIndex);
	}

	public void moveToNextQuestion()
	{
		if ( currentQuestion == null )
		{
			if ( (currentQuestionIndex + 1) == listOfQuestion.size() )
			{
				currentQuestion = null;
			}
		}
		else
		{
			currentQuestion = currentQuestion.moveToNextQuestion();
			if ( currentQuestion == null )
			{
				if ( (currentQuestionIndex + 1) == listOfQuestion.size() )
				{
					currentQuestion = null;
				}
				else
				{
					currentQuestionIndex++;
					currentQuestion = listOfQuestion.get(currentQuestionIndex);
					currentQuestion.printPatterns();
				}
			}
			else
			{
				currentQuestion.printPatterns();
			}
		}
	}

	public void blindMoveToNextQuestion()
	{
		if ( (currentQuestionIndex + 1) == listOfQuestion.size() )
		{
			currentQuestion = null;
		}
		else
		{
			currentQuestionIndex++;
			currentQuestion = listOfQuestion.get(currentQuestionIndex);
			currentQuestion.printPatterns();
		}
	}

	public void addQuestion(Question question)
	{
		listOfQuestion.add(question);
	}

	public int getCurrentQuestionIndex()
	{
		return currentQuestionIndex;
	}

	public void setCurrentQuestionIndex(int currentQuestionIndex)
	{
		this.currentQuestionIndex = currentQuestionIndex;
	}

	public List<Question> getListOfQuestion()
	{
		return listOfQuestion;
	}

	public void setListOfQuestion(List<Question> listOfQuestion)
	{
		this.listOfQuestion = listOfQuestion;
	}

	public List<String> getPromptsEndWith()
	{
		return promptsEndWith;
	}

	public void setPromptsEndWith(List<String> promptsEndWith)
	{
		this.promptsEndWith = promptsEndWith;
	}

	public List<List<String>> getPromptsInclude()
	{
		return promptsInclude;
	}

	public void setPromptsInclude(List<List<String>> promptsInclude)
	{
		this.promptsInclude = promptsInclude;
	}

}