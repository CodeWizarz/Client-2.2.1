package com.rapidesuite.client.common.gui.datagrid;

import java.io.File;

public class DataGridConstants {
	
	public final static int FRAME_MASS_UPDATE_WIDTH=680;
	public final static int FRAME_MASS_UPDATE_HEIGHT=620;
	public final static int PANEL_MASS_UPDATE_COMPONENT_WIDTH=320;
	public final static int PANEL_MASS_UPDATE_COMPONENT_HEIGHT=50;
	
	public final static String FILTER_EQUALS_TO="=";
	public final static String FILTER_NOT_EQUALS_TO="<>";
	public final static String FILTER_IS_NOT_FILLED="is null";
	public final static String FILTER_IS_FILLED="is not null";
	public final static String FILTER_LESS_OR_EQUALS_TO="<=";
	public final static String FILTER_LESS_THAN="<";
	public final static String FILTER_GREATER_OR_EQUALS_TO=">=";
	public final static String FILTER_GREATER_THAN=">";
	public final static String FILTER_CONTAINS="like";
	
	public final static int GRID_PAGINATION_BATCH_SIZE=100;
	public final static String ORACLE_DATE_FORMAT="dd-MMM-yyyy";
	public final static String ORACLE_TIME_FORMAT="HH:mm:ss";
	public final static String GENERATED_CLASS_FULL_NAME_PREFIX="com.rapidesuite.build.gui.apigrid.DataGridRowStub";
	public final static String LOOKUP_FRAME_TITLE="Lookup values";
	public final static String LOOKUP_CODE_ATTRIBUTE_NAME="lookupValue";
	public final static String LOOKUP_DESCRIPTION_ATTRIBUTE_NAME="descriptionLookupValue";
	public final static String LOOKUP_CODE_ATTRIBUTE_DESCRIPTION="Lookup code";
	
	//TODO: change this into a method
	public static final File EXPORT_GRID_STATUS_TEMP_FOLDER=new File(com.rapidesuite.client.common.util.Config.getTempFolder(),"gridStatus-export");

}
