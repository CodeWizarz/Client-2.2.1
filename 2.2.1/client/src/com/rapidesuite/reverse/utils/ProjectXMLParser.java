/**************************************************
 * $Revision: 42119 $:
 * $Author: john.snell $:
 * $Date: 2014-07-08 16:25:39 +0700 (Tue, 08 Jul 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/utils/ProjectXMLParser.java $:
 * $Id: ProjectXMLParser.java 42119 2014-07-08 09:25:39Z john.snell $:
*/

package com.rapidesuite.reverse.utils;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ProjectXMLParser  extends org.xml.sax.helpers.DefaultHandler
{

  protected SAXParser parser;
  protected StringBuffer buffTemp;
  protected String buff;
  protected HashMap<String,String> res;
  protected String currentRSCTableName;

  public ProjectXMLParser()
  throws Exception{
    parser = new SAXParser();
    parser.setContentHandler(this);
          parser.setErrorHandler(this);
    buffTemp=new StringBuffer("");
     res= new HashMap<String,String>();
  }

  public HashMap<String,String> parseXML(Reader reader)
  throws Exception{
      org.xml.sax.InputSource input=new InputSource(reader);
      parser.parse(input);
      return res;
  }

  public HashMap<String,String> parseXML(InputStream inputStream)
  throws Exception{
      org.xml.sax.InputSource input=new InputSource(inputStream);
      parser.parse(input);
      return res;
  }

  // Called at the beginning of parsing.  We use it as an init() method
    public void startDocument() {

    }

    // When the parser encounters plain text (not XML elements), it calls
    // this method, which accumulates them in a string buffer.
    // Note that this method may be called multiple times, even with no
    // intervening elements.
    public void characters(char[] buffer, int start, int length) {
       String temp= new String(buffer, start, length);//.trim();
        if ( !temp.equals("") && !temp.equalsIgnoreCase("null") ) {
          buffTemp.append(temp);
          buff=buffTemp.toString();
         // System.out.println("buffer: '"+buff+"'");
        }
    }

    // At the beginning of each new element, erase any accumulated text.
    public void startElement(String namespaceURL, String localName,
                             String qname, Attributes attributes) {

    }

    // Take special action when we reach the end of selected elements.
    // Although we don't use a validating parser, this method does assume
    // that the web.xml file we're parsing is valid.
    public void endElement(String namespaceURL, String localName, String qname)
    {
        buff=buff.trim();
        if (localName.equals("mapping")) {
              currentRSCTableName=null;
        }
        if (localName.equals("rsc-table-name")) {
              currentRSCTableName=buff;
        }
        else
        if (localName.equals("sql-file-path")) {
             if (currentRSCTableName!=null)  res.put(currentRSCTableName.toLowerCase().trim(),buff);
        }

        buffTemp= new StringBuffer("");
        buff="";
    }

    // Called at the end of parsing.  Used here to print our results.
    public void endDocument() {

    }

    // Issue a warning
    public void warning(SAXParseException exception) {
        System.err.println("WARNING: line " + exception.getLineNumber() + ": "+
                           exception.getMessage());
    }

    // Report a parsing error
    public void error(SAXParseException exception) {
        System.err.println("ERROR: line " + exception.getLineNumber() + ": " +
                           exception.getMessage());
    }

    // Report a non-recoverable error and exit
    public void fatalError(SAXParseException exception) throws SAXException {
        throw new SAXException("FATAL ERROR when parsing line "+exception.getLineNumber()+ "'"+
        buff+"' : " +
                           exception.getMessage());
    }

}