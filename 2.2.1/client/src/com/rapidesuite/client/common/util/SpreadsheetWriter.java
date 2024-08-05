package com.rapidesuite.client.common.util;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.ss.util.CellReference;

public  class SpreadsheetWriter
{

	private final Writer writer;
	private int rowNumber;

	public SpreadsheetWriter(Writer out){
		writer = out;
	}

	public void beginSheet() throws IOException {
		beginWorkSheet();
		beginTabSheet();
	}
	
	public void beginWorkSheet() throws IOException {
		writer.write("<?xml version=\"1.0\" encoding=\"" + com.rapidesuite.core.CoreConstants.CHARACTER_SET_ENCODING + "\"?>" +
		"<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" );
	}
	
	public void beginSheet(String sheetFormat) throws IOException {
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "+
				" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" );
		writer.write(sheetFormat+"\n");			
		writer.write("<sheetData>\n");
	}
	
	public void beginTabSheet() throws IOException {
		writer.write("<sheetData>\n");
	}
	
	public void endSheet() throws IOException {
		endTabSheet();
		endWorkSheet();
	}
	
	public void endWorkSheet() throws IOException {
		writer.write("</worksheet>");
	}

	public void endTabSheet() throws IOException {
		writer.write("</sheetData>");
	}

	/**
	 * Insert a new row
	 *
	 * @param rownum 0-based row number
	 */
	public void insertRow(int rownum) throws IOException {
		writer.write("<row r=\""+(rownum+1)+"\">\n");
		this.rowNumber = rownum;
	}

	/**
	 * Insert row end marker
	 */
	public void endRow() throws IOException {
		writer.write("</row>\n");
	}

	public void createCell(int columnIndex, String value, int styleIndex) throws IOException {
		String ref = new CellReference(rowNumber, columnIndex).formatAsString();
		writer.write("<c r=\""+ref+"\" t=\"inlineStr\"");
		if(styleIndex != -1) {
			writer.write(" s=\""+styleIndex+"\"");
		}
		writer.write(">");
		writer.write("<is><t>"+StringEscapeUtils.escapeXml10(replaceSpecialEmptySpaceCharacters(value))+"</t></is>");
		writer.write("</c>");
	}
	
	public static String replaceSpecialEmptySpaceCharacters(String value)
    {
        String stringToSearch = value;
        int len = stringToSearch.length();
        StringBuffer toReturn=new StringBuffer("");
        for (int i = 0; i < len; i++)
        {
            char ch = stringToSearch.charAt(i);
            if (!Character.isWhitespace(ch) && !Character.isLetterOrDigit(ch) && Integer.toHexString(ch).equalsIgnoreCase("0") )
            {
                toReturn.append(" ");
            }
            else {
            	toReturn.append(ch);
            }
        }
        return toReturn.toString();
    }

	public void createCell(int columnIndex, String value) throws IOException {
		createCell(columnIndex, value, 1);
	}

	public Writer getWriter() {
		return writer;
	}

}