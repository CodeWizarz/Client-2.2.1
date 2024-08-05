package com.rapidesuite.client.common.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rapidesuite.core.utility.DomParser;

public class TemplateUtils
{
	private static String SESSION_FILE_EXTENSION_NAME="session";
	
	public static List<Integer> getSelectedNodesAttributeValue(DomParser parser,String tagName,String attributeName)
	throws Exception{
		List<Integer> output=new ArrayList<Integer>();
		if (parser==null) {
			return output;
		}
		Element element=parser.getRootElement();

		NodeList list=element.getElementsByTagName(tagName);
		if ( list==null ){
			return output;
		}

		for ( int i=0;i<list.getLength();i++ ){
			Node node=list.item(i);
			Map<String, String> attrs=getAttributesMap((Element)node);
			if (attrs != null) {
				String id=attrs.get(attributeName);
				if ( id!=null ) {
					output.add(Integer.parseInt(id));
				}
			}
		}
		return output;
	}	

	public static Map<Integer,String> getSelectedNodes(DomParser parser,String tagName,String attributeName)
	throws Exception{
		Map<Integer,String> res=new TreeMap<Integer,String>();
		if (parser==null) {
			return res;
		}
		Element element=parser.getRootElement();

		NodeList list=element.getElementsByTagName(tagName);
		if ( list==null ){
			return res;
		}

		for ( int i=0;i<list.getLength();i++ ){
			Node node=list.item(i);
			Node firstChild=node.getFirstChild();
			if ( firstChild==null ) {
				continue;
			}
			String val=firstChild.getNodeValue();
			if ( val==null || val.equals("") ) {
				continue;
			}

			Map<String, String> attrs=getAttributesMap((Element)node);
			if ( attrs==null ) {
				continue;
			}

			String id=attrs.get(attributeName);
			if ( id!=null ) {
				res.put(new Integer(id),val);
			}
		}
		return res;
	}

	public static Map<String, String> getAttributesMap(Element element)
	{
		Map<String, String> attributes = new HashMap<String, String>();
		if ( element != null && element.hasAttributes() )
		{
			NamedNodeMap attrs = element.getAttributes();
			for ( int i = 0; i < attrs.getLength(); i++ )
			{
				attributes.put(attrs.item(i).getNodeName(), attrs.item(i).getNodeValue());
			}
		}
		return attributes;
	}

	public static DomParser getParserXML(File xmlFile)
	throws Exception{
		FileInputStream fstream=null;
		try
		{
			if ( !xmlFile.exists() ) {
				return null;
			}
			fstream = new FileInputStream(xmlFile);
			DomParser parser = new DomParser(fstream);
			return parser;
		}
		finally
		{
			if ( fstream!=null ) {
				fstream.close();
			}
		}
	}

	public static String getValue(DomParser parser,String tagName)
	throws Exception{
		if (parser==null) {
			return "";
		}
		Element element=parser.getRootElement();
		NodeList list=element.getElementsByTagName(tagName);
		if ( list==null || list.getLength()==0 ) {
			return "";
		}

		Node node=list.item(0);
		Node firstChild=node.getFirstChild();
		if ( firstChild==null ) {
			return "";
		}
		String val=firstChild.getNodeValue();
		if ( val==null || val.isEmpty() ) {
			return "";
		}

		return val;
	}

	public static Map<String, String> getSelectedPaths(DomParser parser)
	throws Exception{
		Map<String, String> res=new HashMap<String, String>();
		if (parser==null) {
			return res;
		}
		Element element=parser.getRootElement();

		NodeList list=element.getElementsByTagName(UtilsConstants.XML_NODE_TAG_NAME);
		if ( list==null ) {
			return res;
		}

		for ( int i=0;i<list.getLength();i++ ){
			Node node=list.item(i);
			Node firstChild=node.getFirstChild();
			if ( firstChild==null ) {
				continue;
			}
			String fullPath=firstChild.getNodeValue();
			if ( fullPath==null || fullPath.isEmpty() ){
				continue;
			}
			res.put(fullPath,fullPath);
		}
		return res;
	}

	public static void writeTemplateToFile(File sessionFile,StringBuffer content)
	throws Exception{
		BufferedWriter out=null;
		try
		{
			out = new BufferedWriter(new FileWriter(sessionFile));
			out.write(content.toString());
		}
		finally
		{
			if ( out!=null ) {
				out.close();
			}
		}
	}

	public static File getTemplateFile(String templateName,final File templateFolder)
	{
		if (!templateFolder.exists()) {
			templateFolder.mkdirs();
		}
		return new File(templateFolder,templateName+"."+SESSION_FILE_EXTENSION_NAME);
	}
		
	public static List<File> getTemplates(final File templateFolder)
	{
		List<File> res=new ArrayList<File>();
		try
		{
			if ( templateFolder.exists() )
			{
				res=FileUtils.listAllFiles(templateFolder,false,SESSION_FILE_EXTENSION_NAME);
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
		}
		return res;
	}
	
}