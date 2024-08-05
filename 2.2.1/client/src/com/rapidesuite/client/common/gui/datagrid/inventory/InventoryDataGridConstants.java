package com.rapidesuite.client.common.gui.datagrid.inventory;

import java.awt.Color;
import java.io.File;

public class InventoryDataGridConstants {
	
	public final static String ADDITIONAL_HEADER_COLUMN_CONTROL_TEXT="Control columns";
	public final static String ADDITIONAL_HEADER_COLUMN_DATA_TEXT="Data columns";
	
	public final static String EXTRA_COLUMN_ROW_SEQUENCE_ATTRIBUTE_NAME="99999999";
	public static final String EXTRA_COLUMN_NAVIGATION_ATTRIBUTE_NAME="77775";
	public static final String EXTRA_COLUMN_ENABLED_ATTRIBUTE_NAME="77776";
	public static final String EXTRA_COLUMN_STATUS_ATTRIBUTE_NAME="77778";
	public static final String EXTRA_COLUMN_MESSAGE_ATTRIBUTE_NAME="77779";
	public static final String EXTRA_COLUMN_RSC_DATA_LABEL_ATTRIBUTE_NAME="77780";
	public static final int EXTRA_COLUMN_CONTROL_COUNT=4;
	
	public static final String EXTRA_COLUMN_NAVIGATION_ATTRIBUTE_DESCRIPTION="Navigation Filter";
	public final static String EXTRA_COLUMN_ROW_SEQUENCE_ATTRIBUTE_DESCRIPTION="Row #";
	public static final String EXTRA_COLUMN_ENABLED_ATTRIBUTE_DESCRIPTION="On/Off";
	public static final String EXTRA_COLUMN_STATUS_ATTRIBUTE_DESCRIPTION="Row Status";
	public static final String EXTRA_COLUMN_MESSAGE_ATTRIBUTE_DESCRIPTION="Row Message";
	public static final String EXTRA_COLUMN_RSC_DATA_LABEL_ATTRIBUTE_DESCRIPTION="RSC Data Label";

	public final static String STATUS_COLUMN_SUCCESS_VALUE="Valid";
	public final static String STATUS_COLUMN_ERROR_VALUE="Invalid";
	public final static String STATUS_COLUMN_PENDING_VALUE="Unprocessed";
	public static final Color TABLE_HEADER_BACKGROUND_COLOR=new Color(255,209,164);
	public static final Color INVALID_OR_ERROR_COLOR  = new Color(243,146,137);
	public static final Color WARNING_COLOR  = new Color(255,172,89);
	public static final Color PROCESSING_COLOR  = new Color(255,255,100);
	public static final Color UNPROCESSED_COLOR  = new Color(157,206,255);
	public static final Color SUCCESS_COLOR  = new Color(135,211,158);
	
	public static final String NAVIGATION_NA_VALUE="n/a";
	
	public static String CELL_EDITABLE_KEYWORD="EDITABLE";
	public static String CELL_NON_EDITABLE_KEYWORD="NON_EDITABLE";
	
	//TODO: change DATA_TEMP_FOLDER and EXPORT_OVERRIDES_BR100_TEMP_FOLDER into method
	public static final File DATA_TEMP_FOLDER=new File(com.rapidesuite.client.common.util.Config.getTempFolder(),"data");
	public static final File OVERRIDES_TEMP_FOLDER=new File(DATA_TEMP_FOLDER,"overrides");
	public static final File EXPORT_OVERRIDES_BR100_TEMP_FOLDER=new File(com.rapidesuite.client.common.util.Config.getTempFolder(),"overrides-export");
	
	public static final int ROW_SEQUENCE_COLUMN_GRID_INDEX=0;
	public static final int ENABLED_COLUMN_GRID_INDEX=1;
	public static final int STATUS_COLUMN_GRID_INDEX=2;
	public static final int MESSAGE_COLUMN_GRID_INDEX=3;
				
	public static final String ERROR_ID_COLUMN_NAME = "RSC Error ID";
	public static final String ERROR_ID_BRACKETING_DELIMITER_IN_RETURN_VALUE = "@@ERROR_ID@@";
	public static final String ERROR_GET_MODE = "Get Error";
}
