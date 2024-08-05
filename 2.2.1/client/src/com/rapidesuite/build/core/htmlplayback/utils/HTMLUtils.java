package com.rapidesuite.build.core.htmlplayback.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplateManager;

public class HTMLUtils
{

	public static final String TEMPLATE_PARAMETERS_SEPARATOR = "##%!%##";

	public static List<String> readLines(BufferedReader br) throws Exception
	{
		List<String> toReturn = new ArrayList<String>();

		String line = br.readLine();
		StringBuffer temp = null;
		boolean isTemplateTextArea = false;
		while ( null != line )
		{
			if ( line.trim().startsWith(TEMPLATE_PARAMETERS_SEPARATOR) )
			{
				if ( toReturn.isEmpty() )
				{
					throw new IllegalArgumentException("The presence of a TEMPLATE_PARAMETERS_SEPARATOR at the beginning of a line is interpreted to mean a continuation the previous line.  However, there were no previous lines.");
				}
				String previousLine = toReturn.get(toReturn.size() - 1);
				String newCompositeLine = previousLine + line;
				toReturn.set(toReturn.size() - 1, newCompositeLine);
			}
			else
			{
				if ( isTemplateTextArea && (line.startsWith("#") || line.startsWith("TEMPLATE")) )
				{
					toReturn.add(temp.toString());
					isTemplateTextArea = false;
				}
				if ( line.startsWith(HTMLTemplateManager.TEMPLATE_TEXTAREA) )
				{
					isTemplateTextArea = true;
					temp = new StringBuffer("");
					temp.append(line.toString());
				}
				else
				{
					if ( isTemplateTextArea )
					{
						temp.append("<br/>" + line.toString());
					}
					else
					{
						toReturn.add(line.toString());
					}
				}
			}
			line = br.readLine();
		}
		if ( isTemplateTextArea )
		{
			toReturn.add(temp.toString());
		}
		List<String> vagueifiedReturnValueToAvoidHavingToConvertLegacyCode = new ArrayList<String>();
		vagueifiedReturnValueToAvoidHavingToConvertLegacyCode.addAll(toReturn);
		return vagueifiedReturnValueToAvoidHavingToConvertLegacyCode;
	}

	public static boolean deleteDir(File dir)
	{
		if ( dir.isDirectory() )
		{
			String[] children = dir.list();
			for ( int i = 0; i < children.length; i++ )
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if ( !success )
				{
					return false;
				}
			}
		}
		return dir.delete();
	}

	public static String getTextContent(Node node)
	{
		switch (node.getNodeType()) {
		case Node.COMMENT_NODE:
		case Node.PROCESSING_INSTRUCTION_NODE:
			return node.getNodeValue();

		case Node.TEXT_NODE:
		case Node.CDATA_SECTION_NODE:
		case Node.ELEMENT_NODE:
		case Node.ATTRIBUTE_NODE:
		case Node.ENTITY_NODE:
		case Node.ENTITY_REFERENCE_NODE:
		case Node.DOCUMENT_FRAGMENT_NODE:
			StringBuffer result = new StringBuffer();

			addTextContent(node, result);
			return result.toString();
		default:
			return null;
		}
	}

	public static void addTextContent(Node node, StringBuffer target)
	{
		switch (node.getNodeType()) {
		case Node.TEXT_NODE:
		case Node.CDATA_SECTION_NODE:
			target.append(node.getNodeValue());
			break;

		case Node.ELEMENT_NODE:
		case Node.ATTRIBUTE_NODE:
		case Node.ENTITY_NODE:
		case Node.ENTITY_REFERENCE_NODE:
		case Node.DOCUMENT_FRAGMENT_NODE:
			NodeList children = node.getChildNodes();

			for ( int idx = 0; idx < children.getLength(); idx++ )
			{
				addTextContent(children.item(idx), target);
			}
			break;
		}
	}

	public static void print(String message, PrintWriter printWriter)
	{
		printWriter.print(message);
	}

	public static void println(String message, PrintWriter printWriter)
	{
		print(message + "\n", printWriter);
	}

	public static void println(PrintWriter printWriter)
	{
		println("\n", printWriter);
	}

}