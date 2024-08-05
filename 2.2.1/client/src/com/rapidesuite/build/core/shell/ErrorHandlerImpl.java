package com.rapidesuite.build.core.shell;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ErrorHandlerImpl implements ErrorHandler
{

	public void error(SAXParseException exception) throws SAXException
	{
		exception.printStackTrace();
	}

	public void fatalError(SAXParseException exception) throws SAXException
	{
		exception.printStackTrace();
	}

	public void warning(SAXParseException exception) throws SAXException
	{
		exception.printStackTrace();
	}

}