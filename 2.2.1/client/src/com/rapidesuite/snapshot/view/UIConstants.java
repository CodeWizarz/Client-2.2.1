package com.rapidesuite.snapshot.view;

import java.awt.Color;

public class UIConstants {

	public final static String UI_STATUS_CANCELLED="Cancelled";
	public final static String UI_STATUS_PENDING="Pending";
	public final static String UI_STATUS_PROCESSING="Processing";
	public final static String UI_STATUS_COMPLETED="Completed";
	public final static String UI_STATUS_FAILED="Failed";
	public final static String UI_STATUS_UNSELECTED="Unselected";
	public static final String UI_STATUS_WARNING ="Warning";
	public static final String UI_STATUS_RESET ="Cleaned up";
	public static final String UI_STATUS_TIME_LIMIT_EXCEEDED ="Time limit exceeded";
	
	public static final Color COLOR_RED=Color.decode("#E85129");
	public static final Color COLOR_YELLOW=Color.decode("#FFFFD9");
	public static final Color COLOR_BLUE=new Color(0,162,232 );
	public static final Color COLOR_GREEN=Color.decode("#59A203");
	public static final Color COLOR_GREY=new Color(249,249,249);
	public static final Color COLOR_ORANGE =Color.ORANGE;
	
	public static final String FRAME_TITLE_PREFIX="RAPIDSnapshot";
	
	public static final String LABEL_GRID_ROWS="Grid Rows: ";
	public static final String LABEL_GRID_TOTAL_RECORDS="Total Records: ";
	public static final String LABEL_GRID_TOTAL_ADDED_RECORDS="Total Added Records: ";
	public static final String LABEL_GRID_TOTAL_UPDATED_RECORDS="Total Updated Records: ";
	public static final String LABEL_GRID_TOTAL_DEFAULT_RECORDS="Total Default Records: ";
	public static final String LABEL_GRID_TOTAL_CHANGES="Total Changes: ";
	public static final String LABEL_GRID_TOTAL_RECORDS_CREATED="Total Records: ";
	public static final String LABEL_GRID_TOTAL_CONFIGURATION_RECORDS_CREATED="Total Configuration: ";
	public static final String LABEL_GRID_TOTAL_POST_CONFIGURATION_RECORDS_CREATED="Total Post Configuration: ";
	public static final String LABEL_GRID_TOTAL_POST_IMPLEMENTATION_RECORDS_CREATED="Total Post Impl: ";
	public static final String LABEL_GRID_TOTAL_POST_IMPLEMENTATION_OBSOLETE_RECORDS_CREATED="Total Post Impl Obsolete: ";
	public static final String LABEL_GRID_TOTAL_RECORDS_TO_CONVERT="Total Records to Convert: ";
	
	public static final String UNAPPLIED_FILTERS_WARNING="You have unapplied Filters!";
	public static final String FILTERED_OUT_PREFIX="Excluded: ";
	public static final String FILTERED_IN_PREFIX="Matching";
	
	public final static String UI_FORM_TYPE_OU="Operating Unit";
	public final static String UI_FORM_TYPE_GLOBAL="Instance Level";
	public final static String UI_NA="N/A";
	
	public static final String DOWNLOAD_PROGRESS_LABEL_PREFIX="Worker # ";

	public static final int BALLOON_FADEIN_DELAY=500;
	public static final int BALLOON_FADEIN_FRAMERATE=24;
	
	public static boolean BALLOON_OPTIONS_TRIGGERED=false;
	public static boolean BALLOON_SNAPSHOT_CREATION_FILTER_TRIGGERED=false;
	public static boolean BALLOON_SNAPSHOT_CREATION_START_TRIGGERED=false;
	public static boolean BALLOON_ADMIN_PASSWORD_TRIGGERED=false;
	public static boolean BALLOON_REGISTER_TRIGGERED=false;
	public static boolean BALLOON_GRID_SNAPSHOTS_TRIGGERED=false;
	public static boolean BALLOON_GRID_FILTERING_TRIGGERED=false;
	
	public static int DELTA_X_POSITION_FRAMES=30;
	public static int DELTA_Y_POSITION_FRAMES=30;
	
}
