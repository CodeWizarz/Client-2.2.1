/**************************************************
 * $Revision: 58914 $:
 * $Author: hassan.jamil $:
 * $Date: 2016-10-13 10:03:53 +0700 (Thu, 13 Oct 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/SwiftBuildConstants.java $:
 * $Id: SwiftBuildConstants.java 58914 2016-10-13 03:03:53Z hassan.jamil $:
 */

package com.rapidesuite.build;

import java.awt.Color;
import java.io.File;





public class SwiftBuildConstants
{

	public static final String JFRAME_TITLE = "Rapid Build";

	//the width and height are made to approximate the golden ratio (i.e width = height * (1 + sqrt(5)) / 2)
	public static final int JFRAME_WIDTH = 1261;
	public static final int JFRAME_HEIGHT = 779;

	public static final String IMAGE_BACK = "/images/back16.gif";
	public static final String IMAGE_NEXT = "/images/forward16.gif";
	public static final String IMAGE_CHECK = "/images/check.gif";

	public static final String PANEL_HTML_VALIDATION = "PANEL_HTML_VALIDATION";
	public static final String PANEL_INJECTORS_PACKAGE_SELECTION = "PANEL_INJECTORS_PACKAGE_SELECTION";
	public static final String PANEL_ENVIRONMENT_VALIDATION = "PANEL_ENVIRONMENT_VALIDATION";
	public static final String PANEL_INJECTORS_PACKAGE_EXECUTION = "PANEL_INJECTORS_PACKAGE_EXECUTION";

	public static final String UPDATE_LOG_FOLDER = "update";
	public static final String SWIFTBUILD_LOG_FOLDER = "build";
	public static final String CUSTOM_PROPERTY_START_KEYWORD = "###";
	public static final String CUSTOM_PROPERTY_END_KEYWORD = CUSTOM_PROPERTY_START_KEYWORD;

	public static final String ENVIRONMENT_FILE_EXTENSION = "build";
	public static final String OLD_ENVIRONMENT_FILE_EXTENSION = "bwe";
	public static final String INJECTOR_LOG_FILE_EXTENSION = "log";
	public static final String INJECTORS_PACKAGE_FILE_EXTENSION = "bwp";
	public static final String LOG_FILE_EXTENSION = "txt";
	public static final String XML_FILE_EXTENSION = "xml";
	public static final String EXCEL_FILE_EXTENSION = "xls";

	public static final String SVN_REVISION_ATTRIBUTE = "revision";
	public static final String SVN_URL_TAG = "url";
	public static final String SVN_COMMIT_TAG = "commit";
	public static final String APPLICATION_VERSION_FILE_NAME = "version.txt";
	public static final String PROPERTY_START_SVN_REVISION_KEYWORD = "\\$Revision:";
	public static final String PROPERTY_END_SVN_REVISION_KEYWORD = "\\$:";

	public static final String PWD_FLD_KEYWORDS = "VALUE FNDSCSGN SIGNON PASSWORD 1 ";
	public static final String PWD_IL_KEYWORDS = "TEMPLATE_INPUT INPUT_BY=ID##%!%##PARAM1=passwordField##%!%##PARAM2=";

	public static final String CONNECTION_METHOD_DEFAULT_VALUE = "Select the protocol...";
	public static final String CONNECTION_METHOD_FTP_VALUE = "FTP - File Transfer Protocol";
	public static final String CONNECTION_METHOD_SFTP_VALUE = "SFTP - SSH File Transfer Protocol";
	public static final String CONNECTION_METHOD_SFTPK_VALUE = "SFTP with Private key file";
	public static final String CONNECTION_METHOD_FILE_VALUE = "Local Filesystem Folder";

	public static final String CONNECTION_SUCCESS_MESSAGE = "Connection and Login Successful";
	public static final String REMOTE_FOLDER_SUCCESS_MESSAGE = "Remote folder located.";

	public static final String FLD_SCRIPT_FOOTER_SEPARATOR = "# CLOSING ORACLE APPLICATIONS";
	public static final String EXTRA_PROPERTY_KEYWORD = "EXTRA_PROPERTY_";
	public static final String TIMER_PROPERTY_NAME = "TIMER";
	public static final String TIME_IN_SECOND_TOKEN = "%timeInSecond%";
	public static final String WAITING_MESSAGE = "Waiting for %timeInSecond% seconds...";
	public static final String RESUMING_MESSAGE = "Resuming process...";

	public static final String FLD_SCRIPTS_HOSTNAME_KEY = "FLD_SCRIPTS_HOSTNAME";
	public static final String FLD_SCRIPTS_TRANSFER_PROTOCOL_KEY = "FLD_SCRIPTS_TRANSFER_PROTOCOL";
	public static final String FLD_SCRIPTS_HOST_USER_NAME_KEY = "FLD_SCRIPTS_HOST_USER_NAME";
	public static final String FLD_SCRIPTS_HOST_PASSWORD_KEY = "FLD_SCRIPTS_HOST_PASSWORD";
	public static final String FLD_SCRIPTS_SSH_PRIVATE_KEY_LOCATION_KEY = "FLD_SCRIPTS_SSH_PRIVATE_KEY_LOCATION";
	public static final String FLD_SCRIPTS_FOLDER_KEY = "FLD_SCRIPTS_FOLDER";
	public static final String FLD_SCRIPTS_LOG_FOLDER_KEY = "FLD_SCRIPTS_LOG_FOLDER";
	public static final String FLD_SCRIPTS_URL_KEY = "FLD_SCRIPTS_URL";
	public static final String FLD_FORM_USER_NAME_KEY = "FLD_FORM_USER_NAME";
	public static final String FLD_FORM_PASSWORD_KEY = "FLD_FORM_PASSWORD";
    public static final String FLD_FORM_RESPONSIBILITY_KEY = "FLD_FORM_RESPONSIBILITY";
    
    public static final String BUILD_HTML_URL_OVERRIDE = "BUILD_HTML_URL_OVERRIDE";
    public static final String BUILD_SINGLE_SIGN_ON_FIELD_IDENTIFIER_TYPE = "BUILD_SINGLE_SIGN_ON_FIELD_IDENTIFIER_TYPE";
    public static final String BUILD_SINGLE_SIGN_ON = "BUILD_SINGLE_SIGN_ON";
    public static final String BUILD_SINGLE_SIGN_ON_USERNAME_FIELD_IDENTIFIER_VALUE = "BUILD_SINGLE_SIGN_ON_USERNAME_FIELD_IDENTIFIER_VALUE";
    public static final String BUILD_SINGLE_SIGN_ON_PASSWORD_FIELD_IDENTIFIER_VALUE = "BUILD_SINGLE_SIGN_ON_PASSWORD_FIELD_IDENTIFIER_VALUE";
    public static final String BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER		= "BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER";
    public static final String BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER_VALUE 	= "BUILD_SINGLE_SIGN_ON_SUBMIT_BUTTON_IDENTIFIER_VALUE";
	
	public static enum TRANSFER_PROTOCOL {
		SFTP,
		SFTPK,
		FTP,
		FILE;
		
		public static TRANSFER_PROTOCOL valueOfAcceptsNull(final String arg) {
			if (arg == null) {
				return null;
			} else {
				return TRANSFER_PROTOCOL.valueOf(arg);
			}
		}
	}

	public static final String VALIDATION_SUCCESS_VALUE = "SUCCESS";
	public static final String VALIDATION_WARNING_VALUE = "WARNING";
	public static final String VALIDATION_STOPPED_VALUE = "Validation stopped.";
	public static final String VALIDATION_FAILED = "FAILED";

	public static final String IS_ENVIRONMENT_VALIDATION_MANDATORY_PROPERTY = "IS_ENVIRONMENT_VALIDATION_MANDATORY";
	public static final String INJECTOR_EXECUTION_SUCCESS_VALUE = "SUCCESS";
	public static final String INJECTOR_EXECUTION_MANUAL_STOP_VALUE = "STOPPED";
	public static final String INJECTOR_EXECUTION_ERROR_VALUE = "ERROR";
	public static final String INJECTOR_EXECUTION_PARTIAL_COMPLETE_VALUE = "Partially Complete";
	public static final String INJECTOR_EXECUTION_LOG_MISMATCH_VALUE = "Log Mismatch";

	public static Color INJECTOR_EXECUTION_SUCCESS_SELECTED_COLOR = new Color(0x98fb98);//light green
	public static Color INJECTOR_EXECUTION_SUCCESS_UNSELECTED_COLOR = new Color(102, 166, 83);//green

	public static Color INJECTOR_EXECUTION_MANUAL_STOP_SELECTED_COLOR = new Color(0xff9999); //light red
	public static Color INJECTOR_EXECUTION_MANUAL_STOP_UNSELECTED_COLOR = Color.RED;

	public static Color INJECTOR_EXECUTION_ERROR_SELECTED_COLOR = new Color(0xff9999); //light red
	public static Color INJECTOR_EXECUTION_ERROR_UNSELECTED_COLOR = Color.RED;

	public static Color INJECTOR_EXECUTION_PARTIAL_COMPLETE_SELECTED_COLOR = new Color(0xffec8b); //light orange
	public static Color INJECTOR_EXECUTION_PARTIAL_COMPLETE_UNSELECTED_COLOR = Color.ORANGE;

	public static Color INJECTOR_EXECUTION_LOG_MISMATCH_SELECTED_COLOR = new Color(0xf9d1af); //light brown
	public static Color INJECTOR_EXECUTION_LOG_MISMATCH_UNSELECTED_COLOR = new Color(0xdb9356); //brown

	public static String CONSOLE_LOG_FILE_NAME_PREFIX = "console";

	//the values of ENVIRONMENT_VALIDATION_PANEL_MAX_HEIGHT, ENVIRONMENT_VALIDATION_PANEL_MIN_TEXT_FIELD_WIDTH, ENVIRONMENT_VALIDATION_PANEL_MAX_SIDE_PADDING are obtained from experiment
	public static final int ENVIRONMENT_VALIDATION_PANEL_MAX_HEIGHT = 660;
	public static final int ENVIRONMENT_VALIDATION_PANEL_MIN_TEXT_FIELD_WIDTH = 300;
	public static final int ENVIRONMENT_VALIDATION_PANEL_MAX_SIDE_PADDING = 306;

	public static final File PARTIAL_SOURCE_FILE_TEMPLATE = new File("misc/partial_source_file_template.html");
	public static final File SOURCE_FILE_VIEWER_CSS = new File("misc/source_file_viewer.css");
    public static final File SOURCE_FILE_FLD_ENABLE_MENU_DIAGNOSTICS = new File("misc/enable_checkboxes_template.fld");

	public static final String VALID_INTERNAL_STAFF_PERMISSION_FILE_IS_MISSING_MESSAGE = "No valid internal staff permission file is found. If the file has expired, please replace it with the new one and restart this application";
	
	public static final String API_INJECTOR_RECORD_COUNT_START_TAG = "<recordsCount>";
	public static final String API_INJECTOR_RECORD_COUNT_END_TAG = "</recordsCount>";
}