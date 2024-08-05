/**************************************************************
 * $Revision: 31060 $:
 * $Author: john.snell $:
 * $Date: 2013-01-09 12:42:28 +0700 (Wed, 09 Jan 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/telnet/QuestionnaireParser.java $:
 * $Id: QuestionnaireParser.java 31060 2013-01-09 05:42:28Z john.snell $:
 **************************************************************/
package com.rapidesuite.build.core.telnet;

import java.io.InputStream;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class QuestionnaireParser extends DefaultHandler
{

	public static Questions parseDocument(InputStream inputStream) throws Exception
	{
		return parseDocument(new InputSource(inputStream));
	}

	public static Questions parseDocument(InputSource inputSource) throws Exception
	{
		DOMParser parser = new DOMParser();
		parser.parse(inputSource);
		Document document = parser.getDocument();
		NodeList nodes = document.getChildNodes();
		Questions questions = new Questions();
		int i = 0;
		Node node = nodes.item(i);
		int nodesCount = nodes.getLength();
		while ( node != null && i != nodesCount && node.getNodeName().equalsIgnoreCase("#comment") )
		{
			i++;
			if ( i != nodesCount )
				node = nodes.item(i);
		}
		RecursiveTraversal r = new RecursiveTraversal();
		r.traverse(node, null, null, null);
		questions.setListOfQuestion(r.getTree());
		questions.setPromptsInclude(r.getPromptsInclude());
		questions.setPromptsEndWith(r.getPromptsEndWith());

		return questions;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		// nothing to do.
	}

	public void characters(char[] ch, int start, int length) throws SAXException
	{
		// nothing to do.
	}

	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		// nothing to do.
	}
}
