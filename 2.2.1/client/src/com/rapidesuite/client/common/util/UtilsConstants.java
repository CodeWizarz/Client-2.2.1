/**************************************************
 * $Revision: 76777 $:
 * $Author: warangkana.yoomieng $:
 * $Date: 2021-09-10 15:08:13 +0700 (Fri, 10 Sep 2021) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/util/UtilsConstants.java $:
 * $Id: UtilsConstants.java 76777 2021-09-10 08:08:13Z warangkana.yoomieng $:
 */

package com.rapidesuite.client.common.util;

import java.awt.Color;
import java.io.File;

import com.sun.jna.Platform;

public class UtilsConstants
{
	public static final String FORWARD_SLASH = "/";
	public static final String MANUAL_STOP_MSG="ERROR: manual stop";
	public static final String BWE_FILE_EXTENSION  = "bwe";
	public static final String UPDATES_FOLDER_NAME = "updates";
	public static final String BEFORE_RUNNING_UPDATER_FLAG_FILENAME = "before_running_updater.txt";
	public static final String UPDATE_COMPLETED_FLAG_FILENAME = "update_completed.txt";
	public static final String EFFECTIVE_ENGINE_PROPERTIES_FILE_NAME = "effective_engine_properties.properties";
	public static final long UPDATER_FILE_OPERATION_WAITING_TIME_MS = 250;
	public static final long APPLICATION_EXIT_UPDATING_WAITING_TIME_MS = 1000;
	public static final String OLD_VERSION_BACKUP_FOLDER_NAME_PREFIX = "old_version_";
	public static final long MAX_WAITING_DURATION_BEFORE_UPDATER_CLOSE_MS = 12000;
	public static final String APPLICATION_STARTED_FILENAME_PREFIX = "application_started_";

	public static final String ENGINE_PROPERTIES_FILE_NAME="engine.properties";
	public static final String APPLICATION_VERSION_FILE_NAME="version.txt";
	public static final File UPDATER_LOG_FILE=new File("updater.log");
	public static final String PATCH_RELEASE_NOTES_FILE_NAME="release_notes.txt";

	public static final String EXECUTABLE_NAME_BUILD = Platform.isWindows()?"build.bat":"build.py";
	public static final String EXECUTABLE_NAME_REVERSE = Platform.isWindows()?"reverse.bat":"reverse.py";
	public static final String EXECUTABLE_NAME_VALIDATE = Platform.isWindows()?"validate.bat":"validate.py";

	public static final Color BACKGROUND_COLOR_SELECTION=new Color(234,234,255);

	public static final String JAVA_DATE_FORMAT = "dd-MMM-yyyy";
	public static final String JAVA_DATE_TIME_FORMAT = JAVA_DATE_FORMAT+" HH:mm:ss";
	public static final String ORACLE_DATE_FORMAT = "DD-MON-YYYY";
	public static final String ORACLE_DATE_TIME_FORMAT = ORACLE_DATE_FORMAT+" HH24:MI:SS";

	public static final int PATCH_FRAME_WIDTH = 450;
	public static final int PATCH_FRAME_HEIGHT = 600;

	public static final String TEMP_XML_DATA_FILE_NAME="tempxmlfile.xml";
	public static final String TEMP_XML_DATA_FILES_ARCHIVE_NAME="temparchive.zip";
	public static final String XML_FILE_EXTENSION  = "xml";
	
	public static final String TEMP_XLSX_REVERSE_TREE_ALL_STATUSES_FILE_NAME = "Reverse_tree_export_AllStatuses";
	public static final String TEMP_XLSX_REVERSE_TREE_ERROR_STATUSES_FILE_NAME = "Reverse_tree_export_OnlyError";

	public static final String ZIP_FILE_EXTENSION  = "zip";
	public static final String SEVENZIP_FILE_EXTENSION  = "7z";
	public static final String PANEL_PACKAGES_SELECTION  = "packagesSelectionPanel";
	public static final String PANEL_ENVIRONMENT_SELECTION  = "environmentSelectionPanel";
	public static final String REPLACEMENTS_PROPERTIES_FILE_NAME="replacements.properties";
	public static final String REPLACEMENTS_DELIMITER="##";
	public static final String NODE_PATH_SEPARATOR="##@@##";

	public static final String OPERATING_UNIT_NAME_PROPERTY="OPERATING_UNIT_NAME";
	public static final String OPERATING_UNIT_ID_PROPERTY="OPERATING_UNIT_ID";
	public static final String BUSINESS_UNIT_NAME_PROPERTY="BUSINESS_UNIT_NAME";
	public static final String BUSINESS_UNIT_ID_PROPERTY="BUSINESS_UNIT_ID";
	public static final String BUSINESS_GROUP_NAME_PROPERTY="BUSINESS_GROUP_NAME";
	public static final String BUSINESS_GROUP_ID_PROPERTY="BUSINESS_GROUP_ID";
	public static final String CMD_TEXT_EDITOR_PROPERTY="CMD_TEXT_EDITOR";

	public static final Color GREY_COLOR  = Color.LIGHT_GRAY;

	public static final Color OPERATING_UNIT_SPECIFIC_COLOR  = new Color(234,234,255);
	public static final Color INSTANCE_LEVEL_COLOR  = new Color(238,240,172);

	public static final Color INVALID_OR_ERROR_COLOR  = new Color(243,146,137);
	public static final Color WARNING_COLOR  = new Color(255,172,89);
	public static final Color PROCESSING_COLOR  = new Color(255,255,100);
	public static final Color UNPROCESSED_COLOR  = new Color(157,206,255);
	public static final Color SUCCESS_COLOR  = new Color(135,211,158);

	public static final String PANEL_DATA_EXECUTION_SELECTION  = "dataExecutionSelectionPanel";

	public static final String XML_WORKER_COUNT = "wc";
	public static final String XML_NODE_TAG_NAME = "node";

	public static final String SUBVERSION_REVISION_XML_KEYWORD="$Revision:";
	public static final String SUBVERSION_START_REVISION_KEYWORD="\\"+UtilsConstants.SUBVERSION_REVISION_XML_KEYWORD;

	public static final String SUBVERSION_END_REVISION_KEYWORD="\\$:";

	public static final String RES_PLSQL_PACKAGE_NAME_KEY = "RES_PLSQL_PACKAGE_NAME";

	public static final long REVERSE_VALIDATE_AUTOMATED_RUN_WAITING_TIME_PERIOD_MS = 500;

	public static final String NO_TEMPLATE_CHOSEN_OPTION = "-";
	public static final String REVERSE_VALIDATE_BWE_VALIDATION_FAILURE_SCREENSHOT_PREFIX = "bwe_validation_failure_";

	public static final String DEFAULT_DATABASE_USER_NAME = "APPS";
	public static final String DEFAULT_DATABASE_SERVICE_TYPE = "Select database service type...";
	public static final String SID_DATABASE_SERVICE_TYPE = "SID";
	public static final String SERVICE_NAME_DATABASE_SERVICE_TYPE = "Service Name";
	public static final String SNAPSHOT_PLSQL_VOLUME_BATCHING_KEYWORD = "R4C_PLSQL_VOLUME_BATCHING";


	public static enum DATABASE_USERNAME_STATUS {
		OK (""),
		EMPTY ("<br/>You have not entered database connection information. Please note that some BWP files need a database connection."),
		NOT_STANDARD ("<br/>You have entered a non-standard oracle username for connecting to the database.<br/>Normally only the "+DEFAULT_DATABASE_USER_NAME+" username should be used to internally connect to the database.<br/>While you may be able to connect using this username, you may encounter errors during processing as a direct result of the use of this username");

		private final String message;
		private DATABASE_USERNAME_STATUS(final String message) {
			this.message = message;
		}
		public String getMessage() {
			return message;
		}
	}
	
	public static final String PLUGIN_SNAPSHOT_DATA_CONVERSION = "PLUGIN_SNAPSHOT_DATA_CONVERSION";
	public static final String PLUGIN_SNAPSHOT_REPORTS_GENERATION = "PLUGIN_SNAPSHOT_REPORTS_GENERATION";
	public static final String PLUGIN_UPGRADE_REPORTS = "PLUGIN_UPGRADE_REPORTS";
	
}