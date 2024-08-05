package com.rapidesuite.build.core.shell;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rapidesuite.build.core.ManualStopException;

public class TelnetScriptHandler extends DomParser
{

	private SSHTerminal terminal;
	private SSHShellRobot robot;
	private AllQA allQA;
	String holdingQuestion = "";
	String firstQuestionAfterCase = "";

	public TelnetScriptHandler(InputStream in, SSHTerminal terminal, SSHShellRobot robot)
	{
		super(in);
		this.terminal = terminal;
		this.robot = robot;
		this.allQA = setup(this.allQA);
	}

	public AllQA getAllQA()
	{
		return allQA;
	}

	private AllQA setup(List<QA> qas)
	{
		AllQA allQA = new AllQA();
		ArrayList<QA> structure = new ArrayList<QA>();
		allQA.setStructure(structure);
		for ( int i = 0; i < qas.size(); i++ )
		{
			QA qa = qas.get(i);
			allQA.addToStructure(qa);
		}
		return allQA;
	}

	private AllQA setup(AllQA allQA)
	{
		allQA = new AllQA();
		if ( getRootElement().hasAttribute("waitingtime") )
			allQA.setDefaultWaitingTime(Integer.parseInt(getRootElement().getAttribute("waitingtime")));
		ArrayList<QA> structure = new ArrayList<QA>();
		allQA.setStructure(structure);
		List<Node> mainQuestionList = getChildrenByName(getRootElement(), "QUESTION");
		for ( int i = 0; i < mainQuestionList.size(); i++ )
		{
			Node mainQuestion = (Node) mainQuestionList.get(i);
			QA qa = new QA();
			if ( contain((Element) mainQuestion, "CASE") )
			{
				List<Case> cases = getCases((Element) mainQuestion);
				qa.setCases(cases);
				assignAttributeValues(mainQuestion, qa);
				allQA.addToStructure(qa);
			}
			else
			{
				assignAttributeValues(mainQuestion, qa);
				qa.setQuestion(getQuestion(mainQuestion));
				qa.setAnswer(getAnswer(mainQuestion));
				allQA.addToStructure(qa);
			}
		}
		return allQA;
	}

	private List<Case> getCases(Element element)
	{
		List<Case> cases = new ArrayList<Case>();
		List<QA> qaList = new ArrayList<QA>();
		List<Node> caseList = getChildrenByName(element, "CASE");
		for ( int i = 0; i < caseList.size(); i++ )
		{
			Case myCase = new Case();
			Node myCaseNode = (Node) caseList.get(i);
			if ( contain((Element) myCaseNode, "QUESTION") )
			{
				myCase.setCaseQuestion(getQuestion(myCaseNode));
				myCase.setCaseAnswer(getAnswer(myCaseNode));
				List<Node> caseQuestions = getChildrenByName((Element) myCaseNode, "QUESTION");
				for ( int j = 0; j < caseQuestions.size(); j++ )
				{
					QA qa = new QA();
					Node question = (Node) caseQuestions.get(j);
					assignAttributeValues(question, qa);
					qa.setQuestion(getQuestion(question));
					qa.setAnswer(getAnswer(question));
					if ( contain((Element) question, "CASE") )
					{
						List<Case> cs = getCases((Element) question);
						qa.setCases(cs);
					}
					qaList.add(qa);
				}
				myCase.setCaseQAList(qaList);
				cases.add(myCase);
			}
			else
			{
				myCase.setCaseQuestion(getQuestion(myCaseNode));
				myCase.setCaseAnswer(getAnswer(myCaseNode));
				cases.add(myCase);
			}
		}
		return cases;
	}

	private String getQuestion(Node questionNode)
	{
		NodeList questionComposition = questionNode.getChildNodes();
		Node questionText = questionComposition.item(0);
		return questionText.getNodeValue();
	}

	private String getAnswer(Node questionNode)
	{
		NodeList questionComposition = questionNode.getChildNodes();
		Node responseNode = questionComposition.item(1);
		if ( responseNode.hasChildNodes() )
		{
			if ( responseNode.getFirstChild().getNodeValue().trim().equals("") )
			{
				return null;
			}
			return responseNode.getFirstChild().getNodeValue();
		}
		return null;
	}

	private void assignAttributeValues(Node question, QA qa)
	{
		if ( question.hasAttributes() && question.getAttributes().getNamedItem("optional") != null )
		{
			if ( question.getAttributes().getNamedItem("optional").getNodeValue().equalsIgnoreCase("yes") )
				qa.setOptional(true);
		}
		if ( question.hasAttributes() && question.getAttributes().getNamedItem("timeout") != null )
		{
			qa.setTimeout(Integer.parseInt(question.getAttributes().getNamedItem("timeout").getNodeValue()));
		}
	}

	private boolean valid(String str)
	{
		if ( str == null || str.equals("") )
			return false;
		return true;
	}

	public void stopAction() throws Exception
	{
		robot.at.errorStop();
		throw new ManualStopException("Telnet stop initiated...");
	}

	public boolean isStopped()
	{
		if ( robot.at.getActionManager().isExecutionStopped() )
		{
			return true;
		}
		return false;
	}

	public void drill(int startIndex, AllQA allQA) throws Exception
	{
		int caseIndex = -1;
		int questionLength = -1;
		int nextQuestionIndex = -1;
		int questionWithCasesIndex = -1;
		String str = "";

		if ( allQA != null )
		{
			for ( int i = startIndex; i < allQA.getStructure().size(); i++ )
			{
                robot.getClient().getTransport().setHeartbeatInterval(30);
				if ( isStopped() )
					stopAction();
				com.rapidesuite.client.common.util.Utils.sleep(3000);
				if ( i < nextQuestionIndex )
					continue;
				QA qa = (QA) allQA.getStructure().get(i);
				boolean isOptional = qa.isOptional();
				String question = qa.getQuestion();
				if ( question != null )
					questionLength = question.length();
				String answer = qa.getAnswer();
				boolean hasCases = (qa.getCases() != null);
				if ( hasCases )
				{
					List<Case> cases = qa.getCases();
					String caseQuestion = "";
					AllQA myAllQA = null;
					caseIndex = pickupCase(cases, holdingQuestion);
					Case caseObject = cases.get(caseIndex);
					robot.sendText(caseObject.getCaseAnswer());
					if ( caseObject.getCaseQAList() != null )
					{
						List<QA> questionList = caseObject.getCaseQAList();
						myAllQA = setup(questionList);
						drill(0, myAllQA);
					}
					else
					{
						caseQuestion = caseObject.getCaseQuestion();
						String cQuestion = terminal.receiveArea.getText();
						cQuestion = cQuestion.substring(cQuestion.length() - questionLength - 1);
						if ( !caseQuestion.equals("") && cQuestion.indexOf(caseQuestion) != -1 )
						{
							robot.sendText(answer);
							continue;
						}
					}
				}
				else
				{
					if ( valid(question) && isOptional )
					{
						boolean flag = false;
						str = terminal.receiveArea.getText();
						str = str.substring(str.length() - questionLength - 1);
						if ( str.indexOf(question) != -1 )
						{
							robot.sendText(answer);
							continue;
						}
						while ( true )
						{
                            robot.getClient().getTransport().setHeartbeatInterval(30);
							if ( isStopped() )
								stopAction();
							com.rapidesuite.client.common.util.Utils.sleep(3000);
							if ( i == allQA.getStructure().size() - 1 )
							{
								str = terminal.receiveArea.getText();
								if ( firstQuestionAfterCase.length() > 0 )
								{
									str = str.substring(str.length() - firstQuestionAfterCase.length() - 1);
									if ( str.indexOf(firstQuestionAfterCase) != -1 )
										break;
								}
							}
							str = terminal.receiveArea.getText();
							str = str.substring(str.length() - questionLength - 1);
							if ( i == allQA.getStructure().size() - 1 )
							{
								if ( str.indexOf(firstQuestionAfterCase) != -1 )
									break;
							}
							if ( str.indexOf(question) != -1 )
							{
								robot.sendText(answer);
								break;
							}
							for ( int a = i + 1; a < allQA.getStructure().size(); a++ )
							{
								QA qa1 = (QA) allQA.getStructure().get(a);
								if ( qa1.getCases() != null )
								{
									questionWithCasesIndex = a;
									QA afterCase = (QA) allQA.getStructure().get(a + 1);
									firstQuestionAfterCase = afterCase.getQuestion();
									break;
								}
							}
							if ( questionWithCasesIndex != -1 )
							{
								for ( int b = i + 1; b < questionWithCasesIndex; b++ )
								{
									QA qa9 = (QA) allQA.getStructure().get(b);
									str = terminal.receiveArea.getText();
									if ( str.length() - questionLength > 1 )
										str = str.substring(str.length() - questionLength - 1);
									if ( str.indexOf(qa9.getQuestion()) != -1 )
									{
										nextQuestionIndex = b;
										flag = true;
										break;
									}
								}
								QA qa2 = (QA) allQA.getStructure().get(questionWithCasesIndex);
								for ( int k = 0; k < qa2.getCases().size(); k++ )
								{
									Case ce = qa2.getCases().get(k);
									str = terminal.receiveArea.getText();
									str = str.substring(str.length() - questionLength - 1);
									if ( str.indexOf(ce.getCaseQuestion()) != -1 )
									{
										holdingQuestion = ce.getCaseQuestion();
										nextQuestionIndex = questionWithCasesIndex;
										flag = true;
										break;
									}
								}
							}
							else
							{
								for ( int c = i + 1; c < allQA.getStructure().size(); c++ )
								{
									QA qa3 = (QA) allQA.getStructure().get(c);
									str = terminal.receiveArea.getText();
									if ( str.length() - qa3.getQuestion().length() > 1 )
										str = str.substring(str.length() - qa3.getQuestion().length() - 1);
									if ( str.indexOf(qa3.getQuestion()) != -1 )
									{
										nextQuestionIndex = c;
										flag = true;
										break;
									}
								}
							}
							if ( flag )
							{
								break;
							}
						}
						continue;
					}
					else if ( valid(question) && !isOptional )
					{
						str = terminal.receiveArea.getText();
						if ( str != null && str.length() - questionLength > 1 )
						{
							str = str.substring(str.length() - questionLength - 1);
							while ( str.indexOf(question) == -1 )
							{
								if ( isStopped() )
									stopAction();
								com.rapidesuite.client.common.util.Utils.sleep(3000);
								str = terminal.receiveArea.getText();
								if ( str != null && str.length() - questionLength > 1 )
									str = str.substring(str.length() - questionLength - 1);
							}
						}
						robot.sendText(answer);
						continue;
					}
					else
					{
						continue;
					}
				}
			}
		}
	}

	public int pickupCase(List<Case> cases, String question)
	{
		for ( int i = 0; i < cases.size(); i++ )
		{
			Case c = cases.get(i);
			if ( c.getCaseQuestion().trim().equalsIgnoreCase(question.trim()) )
				return i;
		}
		return -1;
	}

}