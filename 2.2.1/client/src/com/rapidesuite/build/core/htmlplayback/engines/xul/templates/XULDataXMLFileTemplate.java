/**************************************************
 * $Revision: 31060 $:
 * $Revision: 31060 $::
 * $Date: 2013-01-09 12:42:28 +0700 (Wed, 09 Jan 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/core/htmlplayback/engines/xul/templates/XULDataXMLFileTemplate.java $:
 * $Id: XULDataXMLFileTemplate.java 31060 2013-01-09 05:42:28Z john.snell $:
 *
 */

package com.rapidesuite.build.core.htmlplayback.engines.xul.templates;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.rapidesuite.build.core.htmlplayback.engines.HTMLEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULEngine;
import com.rapidesuite.build.core.htmlplayback.engines.xul.XULUtils;
import com.rapidesuite.build.core.htmlplayback.templates.HTMLTemplate;

public class XULDataXMLFileTemplate extends HTMLTemplate
{
	public static final String PRIMARY_KEY_ARRAY_NAME = "GLOBAL_primaryKeys";
	public static final String FILENAME_MATRIX_NAME = "GLOBAL_dataFilenameMatrix";
	public static final String FILENAME_FILEID_TO_COLUMNHEADER_LIST_MAP = "GLOBAL_dataRowBuffer_fileIDTo_columnHeaderListMap";

	private XULEngine engine;

	public XULDataXMLFileTemplate(String templateName)
	{
		super(templateName);
	}

	private static final String KEY_VALUE = "VALUE";
	private static final String KEY_ACTION = "ACTION";

	public static enum Action {
		INITIALIZE, FINALIZE, OPEN_ROW, CLOSE_ROW, WRITE_CELL, SET_LAST_AS_PRIMARY_KEY, WRITE_FOREIGN_KEY,
	}

	public void execute(HTMLEngine htmlEngine) throws Exception
	{
		engine = (XULEngine) htmlEngine;
		Map<String, String> parameters = getParameters();

		String action = parameters.get(KEY_ACTION);
		String fileID = parameters.get("FILEID");

		if ( Action.WRITE_CELL.toString().equals(action) )
		{
			writeCell(parameters, fileID);
		}
		else if ( Action.OPEN_ROW.toString().equals(action) )
		{
			openRow(parameters, fileID);
		}
		else if ( Action.CLOSE_ROW.toString().equals(action) )
		{
			closeRow(parameters, fileID);
		}
		else if ( Action.FINALIZE.toString().equals(action) )
		{
			finalizeFile(parameters, fileID);
		}
		else if ( Action.INITIALIZE.toString().equals(action) )
		{
			initializeFile(parameters, fileID);
		}
		else if ( Action.SET_LAST_AS_PRIMARY_KEY.toString().equals(action) )
		{
			specifyPrimaryKey(parameters);
		}
		else if ( Action.WRITE_FOREIGN_KEY.toString().equals(action) )
		{
			writeForeignKey(parameters, fileID);
		}
		else
		{
			throw new IllegalArgumentException("Unrecognized action = '" + action + "'.");
		}
	}

	private void initializeFile(Map<String, String> parameters, String fileID)
	{
		String rscTableName = parameters.get("RSC_TABLE_NAME");
		String fileName = rscTableName + ".xml";
		fileName = new File(engine.getReverseOutputFolder(), fileName).getAbsolutePath();

		StringBuffer buff = new StringBuffer(128);
		buff.append(FILENAME_MATRIX_NAME + "['" + fileID + "'] = '" + fileName + "';\n");

		List<String> rscColumnNames = XULUtils.extractParamtersWithIncreasingNumericSuffix("RSC_COLUMN_NAME_", parameters);
		buff.append(FILENAME_FILEID_TO_COLUMNHEADER_LIST_MAP + "['" + fileID + "'] = new Array();\n");
		for ( String rscColumnName : rscColumnNames )
		{
			buff.append(FILENAME_FILEID_TO_COLUMNHEADER_LIST_MAP + "['" + fileID + "']" + "[" + FILENAME_FILEID_TO_COLUMNHEADER_LIST_MAP + "['" + fileID + "'].length] = '"
					+ rscColumnName + "';\n");
		}

		XULUtils.insertAtHeadOfBuffer(buff);

		XULUtils.addToBuffer("steps[steps.length] = \"writeHeaderToDataXMLOutputFile('" + fileID + "', '" + rscTableName + "');\";\n ");
		openRow(parameters, fileID);
		XULUtils.addToBuffer("steps[steps.length] = \"writeColumnHeaders('" + fileID + "');\";\n ");
	}

	private void finalizeFile(Map<String, String> parameters, String fileID)
	{
		closeRow(parameters, fileID);
		XULUtils.addToBuffer("steps[steps.length] = \"writeFooterToDataXMLOutputFile('" + fileID + "');\";\n ");
	}

	private void openRow(Map<String, String> parameters, String fileID)
	{
		XULUtils.addToBuffer("steps[steps.length] = \"openRowInDataXMLOutputFile('" + fileID + "');\";\n ");
	}

	private void closeRow(Map<String, String> parameters, String fileID)
	{
		XULUtils.addToBuffer("steps[steps.length] = \"closeRowInDataXMLOutputFile('" + fileID + "');\";\n ");
	}

	private void writeForeignKey(Map<String, String> parameters, String fileID)
	{
		String foreignKeyName = parameters.get("FOREIGN_KEY_NAME");
		XULUtils.addToBuffer("steps[steps.length] = \"writeCellToDataXMLOutputFile('" + fileID + "', " + PRIMARY_KEY_ARRAY_NAME + "['" + foreignKeyName + "']);\";\n ");
	}

	private void specifyPrimaryKey(Map<String, String> parameters)
	{
		String name = parameters.get("NAME");
		XULUtils.addToBuffer("steps[steps.length] = \"" + PRIMARY_KEY_ARRAY_NAME + "['" + name + "'] = rscElt;\";\n ");
	}

	private void writeCell(Map<String, String> parameters, String fileID)
	{
		String value = parameters.get(KEY_VALUE);
		String rscColumnName = parameters.get("RSC_COLUMN_NAME");
		String line = "steps[steps.length] = \"storeColumnValue('" + fileID + "', '" + rscColumnName + "', ";
		if ( value == null )
		{
			line += "rscElt";
		}
		else
		{
			line += "'" + value + "'";
		}
		line += ");\";\n ";

		XULUtils.addToBuffer(line);
	}

}