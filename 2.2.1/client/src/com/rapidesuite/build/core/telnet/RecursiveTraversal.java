package com.rapidesuite.build.core.telnet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.rapidesuite.client.common.util.FileUtils;

public class RecursiveTraversal
{

	private List<Question> tree;
	private List<String> promptsEndWith;
	private List<List<String>> promptsInclude;
	private boolean COMPLETED;
	private long questionsTimeout;
	private Map<String, String> properties;

	public RecursiveTraversal()
	{
		tree = new ArrayList<Question>();
		promptsEndWith = new ArrayList<String>();
		promptsInclude = new ArrayList<List<String>>();
		COMPLETED = false;
		questionsTimeout = 10;
	}

	public void traverse(Node parentNode, Question parentQuestion, Case parentCase, Response parentResponse)
	{
		Question childQuestion = null;
		Response childResponse = null;
		Case childCase = null;
		for ( Node node = parentNode.getFirstChild(); node != null; node = node.getNextSibling() )
		{
			if ( !COMPLETED && parentNode.getNodeName().equalsIgnoreCase("QUESTIONS") )
			{
				NamedNodeMap np = parentNode.getAttributes();
				Node prompt = np.getNamedItem("waitingtime");
				if ( prompt != null )
				{
					if ( prompt.getNodeValue() != null && !prompt.getNodeValue().equals("") )
					{
						questionsTimeout = Long.valueOf(prompt.getNodeValue()).longValue();
					}
				}
				COMPLETED = true;
			}
			if ( node.getNodeName().equalsIgnoreCase("PROMPT") )
			{
				NamedNodeMap np = node.getAttributes();
				Node prompt = np.getNamedItem("endWith");
				if ( prompt != null )
				{
					if ( prompt.getNodeValue() != null && !prompt.getNodeValue().equals("") )
					{
						promptsEndWith.add(prompt.getNodeValue());
					}
				}
				else
				{
					prompt = np.getNamedItem("include");
					if ( prompt != null )
					{
						if ( prompt.getNodeValue() != null && !prompt.getNodeValue().equals("") )
						{
							StringTokenizer st = new StringTokenizer(prompt.getNodeValue(), ",");
							List<String> tokens = new ArrayList<String>();
							while ( st.hasMoreTokens() )
							{
								String ele = st.nextToken();
								tokens.add(ele);

							}
							if ( !tokens.isEmpty() )
							{
								promptsInclude.add(tokens);
							}
						}
					}
				}
			}
			else if ( node.getNodeName().equalsIgnoreCase("QUESTION") )
			{
				childQuestion = new Question();
				try
				{
					NamedNodeMap np = node.getAttributes();
					Node opt = np.getNamedItem("optional");
					if ( opt != null )
					{
						if ( opt.getNodeValue() != null && opt.getNodeValue().equalsIgnoreCase("yes") )
						{
							boolean option = Boolean.TRUE.booleanValue();
							childQuestion.setOptional(option);
						}
					}
					Node timeout = np.getNamedItem("timeout");
					if ( timeout != null )
					{
						if ( timeout.getNodeValue() != null )
						{
							long to = Long.valueOf(timeout.getNodeValue()).longValue();
							childQuestion.setTimeout(to);
						}
					}
				}
				catch ( Exception e )
				{
					FileUtils.printStackTrace(e);
				}

				if ( node.getParentNode().getNodeName().equalsIgnoreCase("QUESTIONS") )
				{
					tree.add(childQuestion);
					childQuestion.setParent(parentCase);

				}
				else if ( node.getParentNode().getNodeName().equalsIgnoreCase("CASE") )
				{
					parentCase.addQuestion(childQuestion);
					childQuestion.setParent(parentCase);
				}
				traverse(node, childQuestion, parentCase, parentResponse);
			}
			else if ( node.getNodeName().equalsIgnoreCase("RESPONSE") )
			{
				childResponse = new Response();
				if ( node.getParentNode().getNodeName().equalsIgnoreCase("QUESTION") )
				{
					parentQuestion.setResponse(childResponse);
				}
				else if ( node.getParentNode().getNodeName().equalsIgnoreCase("CASE") )
				{
					parentCase.setResponse(childResponse);
				}
				traverse(node, parentQuestion, parentCase, childResponse);
			}
			else if ( node.getNodeName().equalsIgnoreCase("CASE") )
			{
				childCase = new Case();
				if ( node.getParentNode().getNodeName().equalsIgnoreCase("QUESTION") )
				{
					parentQuestion.addCase(childCase);
					childCase.setParent(parentQuestion);
				}
				traverse(node, parentQuestion, childCase, parentResponse);
			}
			else
			{
				// Case of text data:
				// Skip empty data:
				if ( node.getNodeValue() != null && node.getNodeValue().trim().equals("") || node.getNodeValue().equalsIgnoreCase("\r")
						|| node.getNodeValue().equalsIgnoreCase("\n") || node.getNodeValue().equalsIgnoreCase("\r\n") )
				{
					continue;
				}
				if ( node.getParentNode().getNodeName().equalsIgnoreCase("QUESTION") )
				{
					parentQuestion.setPattern(replaceVariablesParenthesis(properties, node.getNodeValue()));
				}
				else if ( node.getParentNode().getNodeName().equalsIgnoreCase("RESPONSE") )
				{
					parentResponse.setResponse(replaceVariablesParenthesis(properties, node.getNodeValue()));
				}
				else if ( node.getParentNode().getNodeName().equalsIgnoreCase("CASE") )
				{
					parentCase.setPattern(node.getNodeValue());
				}
				traverse(node, parentQuestion, parentCase, parentResponse);
			}
		}
	}

	public static String replaceVariables(Map<String, String> environmentProperties, String target)
	{
		if ( environmentProperties == null )
			return target;
		Iterator<Map.Entry<String, String>> environmentPropertiesIterator = environmentProperties.entrySet().iterator();
		String src = null;
		if ( target == null )
			return target;
		String res = target;
		while ( environmentPropertiesIterator.hasNext() )
		{
			Map.Entry<String, String> entry = environmentPropertiesIterator.next();
			// replace space by _ otherwise velocity will not replace the
			// strings:
			String key = entry.getKey();
			src = key.replaceAll(" ", "_");
			src = "\\$" + src;
			String value = environmentProperties.get(key);
			res = res.replaceAll(src, value);
		}
		return res;
	}

	public static String replaceVariablesParenthesis(Map<String, String> environmentProperties, String target)
	{
		if ( environmentProperties == null )
		{
			return target;
		}
		Iterator<Map.Entry<String, String>> environmentPropertiesIterator = environmentProperties.entrySet().iterator();
		String src = null;
		if ( target == null )
		{
			return target;
		}
		String res = target;
		while ( environmentPropertiesIterator.hasNext() )
		{
			Map.Entry<String, String> entry = environmentPropertiesIterator.next();
			String key = entry.getKey();
			String tmp = key.replaceAll(" ", "_");
			src = "\\$" + tmp;
			String value = environmentProperties.get(key);
			res = res.replaceAll(src, value);
			src = "\\$\\{" + tmp + "\\}";
			res = res.replaceAll(src, value);
		}
		return res;
	}

	public List<Question> getTree()
	{
		return tree;
	}

	public void setTree(List<Question> tree)
	{
		this.tree = tree;
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

	public long getQuestionsTimeout()
	{
		return questionsTimeout;
	}

	public void setQuestionsTimeout(long questionsTimeout)
	{
		this.questionsTimeout = questionsTimeout;
	}

}
