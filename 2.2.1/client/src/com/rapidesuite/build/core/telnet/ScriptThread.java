/**************************************************************
 * $Revision: 40184 $:
 * $Author: john.snell $:
 * $Date: 2014-03-27 13:25:50 +0700 (Thu, 27 Mar 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/ScriptThread.java $:
 * $Id: ScriptThread.java 40184 2014-03-27 06:25:50Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

public class ScriptThread
{
	private BuilderWizardTelnet bwt;
	private Questions questions;
	private QuestionTimeoutThread qq;
	private Telnet telnet;
	private boolean completed;

	public ScriptThread(Telnet telnet, BuilderWizardTelnet bwt, Questions questions)
	{
		this.telnet = telnet;
		this.bwt = bwt;
		this.questions = questions;
		this.completed = false;
	}

	public boolean isCompleted()
	{
		return completed;
	}

	public BuilderWizardTelnet getBuilderWizardTelnet()
	{
		return bwt;
	}

	public Question getCurrentQuestion()
	{
		return questions.getCurrentQuestion();
	}

	public void start()
	{
		questions.init();

		if ( getQuestionTimeout() != -1 )
			startCurrentQuestionTimeout();

		telnet.start();
	}

	public void stopAll()
	{
		bwt.stopAll();
	}

	public void moveToNextQuestion()
	{
		questions.moveToNextQuestion();
	}

	public long getQuestionTimeout()
	{
		if ( getCurrentQuestion() == null )
			return -1;
		return getCurrentQuestion().getTimeout();
	}

	public void setTextSent(String text)
	{
		bwt.getTerminal().setReceivedText(text);
	}

	public void setTextReceived(String text)
	{
		bwt.getTerminal().setReceivedText(text);
	}

	public boolean hasPrompt(String text)
	{
		return questions.hasPrompt(text);
	}

	public void startCurrentQuestionTimeout()
	{
		qq = new QuestionTimeoutThread(this, getQuestionTimeout());
		qq.start();
	}

	public void stopCurrentQuestionTimeout()
	{
		if ( qq != null )
		{
			qq.BLOCKED = false;
		}
	}

	public boolean hasPattern(String text)
	{
		Question question = questions.getCurrentQuestion();
		if ( question == null )
			return false;

		if ( question.hasPattern(text) )
		{
			return true;
		}
		return false;
	}

	public void setCompleted(boolean completed)
	{
		this.completed = completed;
	}

}