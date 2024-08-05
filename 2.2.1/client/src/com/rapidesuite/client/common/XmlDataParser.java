package com.rapidesuite.client.common;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlDataParser extends DefaultHandler
{
    private boolean insideHeaders = false;
    private String[] currentRow = null;
    private int currentColumnIndex = 0;
    private StringBuffer currentContentBuffer = new StringBuffer(1024);
    private boolean isParseNavigationNamesOnly;
    private List<String> headerList = new ArrayList<String>();
    private List<String[]> rowList = new ArrayList<String[]>();
    private final List<String> mapperList = new ArrayList<String>();
    
    public XmlDataParser(boolean isParseNavigationNamesOnly) {
    	this.isParseNavigationNamesOnly=isParseNavigationNamesOnly;
    }
    
    public List<String> getMapperList()
    {
    	return this.mapperList;
    }
    
    public void startElement(String namespaceUri,
                             String localName,
                             String qualifiedName,
                             Attributes attributes)
    throws SAXException
    {
        currentContentBuffer.setLength(0);
        if ( qualifiedName.equals("r") )
        {
        	if (isParseNavigationNamesOnly) {
        		throw new SAXException("STOP. Only parsing navigation tags.");
        	}
            currentRow = new String[headerList.size()];
        }
        else 
        if ( qualifiedName.equals("h") )
        {
            insideHeaders = true;
        }
    }

    public void endElement(String namespaceUri,
                           String localName,
                           String qualifiedName)
    throws SAXException
    {
        if ( qualifiedName.equals("c") )
        {
            String data = this.currentContentBuffer.toString();
            if ( insideHeaders )
            {
                this.headerList.add(data.trim());
            }
            else
            {
                currentRow[currentColumnIndex++] = data;
            }
        }
        else if ( qualifiedName.equals("r") )
        {
            this.rowList.add(this.currentRow);
            currentColumnIndex = 0;
        }
        else if ( qualifiedName.equals("h") )
        {
            insideHeaders = false;
        }
        else if ( qualifiedName.equals("e") )
        {
            this.mapperList.add(this.currentContentBuffer.toString().trim());
        }
    }

    public void characters(char[] chars,
                           int startIndex,
                           int endIndex)
    {
        this.currentContentBuffer.append(chars, startIndex, endIndex);
    }

	public List<String> getHeaderList() {
		return headerList;
	}

	public List<String[]> getRowList() {
		return rowList;
	}
}