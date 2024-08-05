package com.rapidesuite.build.gui.apigrid;

import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.rapidesuite.client.common.gui.datagrid.DataGridConstants;
import com.rapidesuite.client.common.util.FileUtils;

public class APIDataGridConstants
{

	public final static int FRAME_WIDTH = 980;
	public final static int FRAME_HEIGHT = 620;
	public final static int FRAME_OTIONS_WIDTH = 350;
	public final static int FRAME_OTIONS_HEIGHT = 200;

	public static final String ORACLE_DEFAULT_VALUE_KEYWORD = "Default Value";
	public static final String MODE_CREATE = "create";
	public static final String MODE_UPDATE = "update";
	public static final String MODE_COLUMN_ATTRIBUTE_DESCRIPTION = "Mode";
	public static final String ORACLE_RESET_VALUE_KEYWORD = "ORACLE_RESET";
	public static final double ORACLE_RESET_VALUE_NUMBER = 9.99E125;
	public static java.sql.Date ORACLE_RESET_VALUE_DATE;
	public static final String PLSQL_PACKAGE_UTILITIES = "XX_RSC_UTILITY_MIGRATION_PKG";
	public static final String ORG_NAME_LOOKUP_VALUE_KEYWORD = "##DATAGRID_SELECTED_OU_NAME##";

	public static final Color TABLE_HEADER_BACKGROUND_COLOR = new Color(255, 209, 164);

	static
	{
		try
		{
			DateFormat df = new SimpleDateFormat(DataGridConstants.ORACLE_DATE_FORMAT);
			java.util.Date utilDate = df.parse("01-Jan-9999");
			ORACLE_RESET_VALUE_DATE = new java.sql.Date(utilDate.getTime());
		}
		catch ( ParseException e )
		{
			FileUtils.printStackTrace(e);
		}
	}

}
