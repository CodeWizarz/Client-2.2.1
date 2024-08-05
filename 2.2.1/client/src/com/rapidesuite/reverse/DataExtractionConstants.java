/**************************************************
 * $Revision: 48695 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-08-04 18:12:36 +0700 (Tue, 04 Aug 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/DataExtractionConstants.java $:
 * $Id: DataExtractionConstants.java 48695 2015-08-04 11:12:36Z olivier.deruelle $:
 */

package com.rapidesuite.reverse;

import java.io.File;


public class DataExtractionConstants
{

	public static final int RECORD_PROCESSING_BATCH_SIZE=100;
	public static final String REVERSE_FRAME_TITLE="Rapid Reverse";

	public static final int JFRAME_WIDTH=1050;
	public static final int JFRAME_HEIGHT=690;
	public static final int ORACLE_USERS_SCROLLPANE_WIDTH=500;
	public static final int ORACLE_USERS_SCROLLPANE_HEIGHT=450;
	public static final int OU_SCROLLPANE_WIDTH=500;
	public static final int OU_SCROLLPANE_HEIGHT=450;
	public static final int BG_SCROLLPANE_WIDTH=500;
	public static final int BG_SCROLLPANE_HEIGHT=150;

	public static final int SKIP_ROWS = 0;
	public static final String SQL_STATEMENT_HEADER = "SQL STATEMENT";
	public static final String RSC_TABLE_NAME_HEADER = "RSC Table name";
	public static final String RSC_COLUMN_NAME_HEADER = "RSC Column name";
	public static final String ORACLE_FIELD_TYPE_HEADER = "Oracle Field Type";

	public static final String PATCH_FOLDER_NAME  = "patches";

	public static final String PANEL_LOG  = "logPanel";

	public static final String USER_ID_UNUSED_PROPERTY="-9999";
	public static final String DFF_TABLE_QUERY_KEYWORD="DFF_TABLE_QUERY";

	public static final String XML_OUTPUT_FOLDER_TAG_NAME = "output-folder";
	public static final String XML_OUTPUT_CLEAN_TAG_NAME = "output-clean";
	public static final String XML_ZIP_OUTPUT_FOLDER_TAG_NAME = "zip-output-folder";
	public static final String XML_REVERSE_IS_SELECTED_ATTR_NAME = "isSelected";
	public static final String XML_AUDIT_EXTRACT_TAG_NAME = "audit-extract";
	public static final String XML_THRESHOLD_DATE_TAG_NAME = "threshold-date";

	public static final String SEEDED_DATE="SEEDED_DATE";

	public static final String RSC_LAST_UPDATED_BY_COLUMN_NAME="rsc_last_updated_by";
	public static final String RSC_USER_ID_LIST="USER_ID_LIST";
	public static final String RSC_USER_NAME_LIST="USER_NAME_LIST";
	public static final String RSC_USER_ID_LIST_OLD="SEEDED_USER_ID_LIST";
	public static final String RSC_LAST_UPDATED_BY_OPERATOR="LAST_UPDATED_BY_OPERATOR";
	public static final String RSC_LAST_UPDATE_DATE_OPERATOR="LAST_UPDATE_DATE_OPERATOR";
	public static final String RSC_IN_OPERATOR="IN";
	public static final String RSC_NOT_IN_OPERATOR="NOT IN";


	public static final String INSTANCE_LEVEL_FOLDER_NAME="Instance level";
	public static final File INSTANCE_LEVEL_FOLDER=new File(INSTANCE_LEVEL_FOLDER_NAME);
	public static final String BUILD_INFO_FILE_FOLDER="build.info";
	public static final String BUILD_INFO_FILE_SUFFIX=".build.info";

}