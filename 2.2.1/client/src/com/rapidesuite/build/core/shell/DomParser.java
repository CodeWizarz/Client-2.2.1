package com.rapidesuite.build.core.shell;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DomParser
{

	private static DocumentBuilderFactory factory;
	private static DocumentBuilder documentBuilder;
	private static Document document;
	private static Element rootElement;

	protected DomParser(InputStream in)
	{
		factory = DocumentBuilderFactory.newInstance();
		if ( factory != null )
		{
			try
			{
				documentBuilder = factory.newDocumentBuilder();
			}
			catch ( ParserConfigurationException ex )
			{
				ex.printStackTrace();
			}
			ErrorHandlerImpl errorHandler = new ErrorHandlerImpl();
			if ( documentBuilder != null )
			{
				documentBuilder.setErrorHandler(errorHandler);
				try
				{
					document = documentBuilder.parse(in);
					rootElement = document.getDocumentElement();
				}
				catch ( IOException ex )
				{
					ex.printStackTrace();
				}
				catch ( SAXException ex )
				{
					ex.printStackTrace();
				}
			}
		}
	}

	protected Element getRootElement()
	{
		if ( rootElement != null )
			return rootElement;
		return null;
	}

	protected List<Node> getChildrenByName(Element element, String name)
	{
		if ( element != null )
		{
			ArrayList<Node> children = new ArrayList<Node>();
			NodeList list = element.getChildNodes();
			for ( int i = 0; i < list.getLength(); i++ )
			{
				Node child = list.item(i);
				if ( child.getNodeType() == Node.ELEMENT_NODE )
				{
					if ( child.getNodeName().equalsIgnoreCase(name) )
						children.add(child);
				}
			}
			if ( !children.isEmpty() )
				return children;
			return null;
		}
		return null;
	}

	protected boolean contain(Element element, String tagName)
	{
		return element.getElementsByTagName(tagName).getLength() > 0;
	}

}