package com.rapidesuite.client.common.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.rapidesuite.build.core.htmlplayback.engines.xul.XULConstants;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.AbstractConfig;
import com.rapidesuite.core.utility.CoreUtil;
import com.sun.jna.Platform;


public class Config extends AbstractConfig
{
    private static Config instance = null;
    public static Config getInstance() {
        if(instance == null) {
           instance = new Config();
        }
        return instance;
    }

    public static void customInitInstance(final String enginePropertiesFileName) {
    	instance = new Config(enginePropertiesFileName);
    }

    protected Config()
    {
        super(UtilsConstants.ENGINE_PROPERTIES_FILE_NAME, AbstractConfig.DEFAULT_CONFIG_POLLING_INTERVAL_MS);
    }

    protected Config(final String enginePropertiesFileName) {
    	super(enginePropertiesFileName, AbstractConfig.DEFAULT_CONFIG_POLLING_INTERVAL_MS);
    }

    @Override
    public boolean containsProperty(final String name) {
       return super.containsProperty(name);
    }

    @Override
    public void configUpdated()
    {
        //do nothing.
    }

	public static String
		PROPERTY_PATH_TO_7ZIP_LINUX = "PATH_TO_7ZIP_LINUX",
		PROPERTY_PATH_TO_7ZIP_WINDOWS = "PATH_TO_7ZIP_WINDOWS",
		DB_USER_LANGUAGE="DB_USER_LANGUAGE",
		IS_ENVIRONMENT_VALIDATION_MANDATORY="IS_ENVIRONMENT_VALIDATION_MANDATORY",
		CMD_TEXT_EDITOR_WINDOWS="CMD_TEXT_EDITOR_WINDOWS",
		CMD_TEXT_EDITOR_LINUX="CMD_TEXT_EDITOR_LINUX",
        TEMP_FOLDER_EXPIRY_MINUTES="TEMP_FOLDER_EXPIRY_MINUTES",

        PATCH_URL = "PATCH_URL",
        PATCH_URL_USER_NAME = "PATCH_URL_USER_NAME",
        PATCH_URL_PASSWORD = "PATCH_URL_PASSWORD",
        PATCHING_ENABLED = "PATCHING_ENABLED",
        JAVA_WINDOWS_64="JAVA_WINDOWS_64",
        JAVA_WINDOWS_32="JAVA_WINDOWS_32",
        JAVA_LINUX="JAVA_LINUX",
        TICKET_ATTACHMENT_DEFAULT_DIRECTORY_WINDOWS="TICKET_ATTACHMENT_DEFAULT_DIRECTORY_WINDOWS",
        TICKET_ATTACHMENT_DEFAULT_DIRECTORY_LINUX="TICKET_ATTACHMENT_DEFAULT_DIRECTORY_LINUX",
        BUGTRACKER_WEBSITE_ADDRESS="BUGTRACKER_WEBSITE_ADDRESS",
        BROWSER_VIEW_SOURCE_WINDOWS_64="BROWSER_VIEW_SOURCE_WINDOWS_64",
        BROWSER_VIEW_SOURCE_WINDOWS_32="BROWSER_VIEW_SOURCE_WINDOWS_32",
        BROWSER_VIEW_SOURCE_LINUX="BROWSER_VIEW_SOURCE_LINUX",
        INTERNAL_STAFF_PERMISSION_FILE_PATH="INTERNAL_STAFF_PERMISSION_FILE_PATH",
        EBS_VERSION_MAPPING_FILE="EBS_VERSION_MAPPING_FILE",
        MAX_INSTALLATION_PATH_LENGTH_WINDOWS="MAX_INSTALLATION_PATH_LENGTH_WINDOWS",

        REVERSE_OUTPUT_FOLDER="REVERSE_OUTPUT_FOLDER",
		REVERSE_TEMPLATE_FOLDER="REVERSE_TEMPLATE_FOLDER",
		REVERSE_TEMPLATE_NAME="REVERSE_TEMPLATE_NAME",
		REVERSE_WORKERS_COUNT="REVERSE_WORKERS_COUNT",
		REVERSE_DATA_LABEL="REVERSE_DATA_LABEL",
		REVERSE_FETCH_SIZE="REVERSE_FETCH_SIZE",
		REVERSE_MAXIMUM_RECORDS_PER_DATA_FILE="REVERSE_MAXIMUM_RECORDS_PER_DATA_FILE",
		REVERSE_ZIP_FILE_LOCATION="REVERSE_ZIP_FILE_LOCATION",
		REVERSE_RETRY_SLEEP_TIME="REVERSE_RETRY_SLEEP_TIME",
		REVERSE_IS_EXTRACT_AUDIT = "REVERSE_IS_EXTRACT_AUDIT",
		REVERSE_SESSIONS_FOLDER = "REVERSE_SESSIONS_FOLDER",
		RSC_PREREQUISITE_OBJECTS_KEYWORD ="RSC_PREREQUISITE_OBJECTS_KEYWORD",
		REVERSE_JVM_ARGUMENTS = "REVERSE_JVM_ARGUMENTS",
		REVERSE_CREATED_BY_USER_IDS_FOR_SEEDED_USER_CALCULATION = "REVERSE_CREATED_BY_USER_IDS_FOR_SEEDED_USER_CALCULATION",
		REVERSE_PRINT_SQL_TO_LOG = "REVERSE_PRINT_SQL_TO_LOG",
		REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_11="REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_11",
		REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_12="REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_12",
		REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_FUSION="REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_FUSION",
		REVERSE_MAX_POTENTIAL_SEEDED_USER_COUNT="REVERSE_MAX_POTENTIAL_SEEDED_USER_COUNT",
		REVERSE_TERMINATE_AFTER_FAILED_INTIALIZATION_IN_AUTOMATED_MODE="REVERSE_TERMINATE_AFTER_FAILED_INTIALIZATION_IN_AUTOMATED_MODE",

		BUILD_TERMINATE_AFTER_FAILED_INJECTION="BUILD_TERMINATE_AFTER_FAILED_INJECTION",
		BUILD_PRE_INJECTION_DELAY_SECONDS_ON_AUTOMATED_MODE="BUILD_PRE_INJECTION_DELAY_SECONDS_ON_AUTOMATED_MODE",
		BUILD_SSH_SOCKET_TIMEOUT_SECONDS="BUILD_SSH_SOCKET_TIMEOUT_SECONDS",
		BUILD_API_INJECTION_COMMIT_AFTER_X_ROWS="BUILD_API_INJECTION_COMMIT_AFTER_X_ROWS",
		BUILD_API_INJECTION_WORKER_COUNT="BUILD_API_INJECTION_WORKER_COUNT",
		BUILD_FLD_FORM_OPEN_MAX_ITERATION="BUILD_FLD_FORM_OPEN_MAX_ITERATION",
		BUILD_FLD_FORM_CLOSE_MAX_ITERATION="BUILD_FLD_FORM_CLOSE_MAX_ITERATION",
		BUILD_HTML_FORM_CLOSE_MAX_ITERATION="BUILD_HTML_FORM_CLOSE_MAX_ITERATION",
		BUILD_FLD_BWE_VALIDATION_OPEN_TIMEOUT="BUILD_FLD_BWE_VALIDATION_OPEN_TIMEOUT",
		BUILD_FLD_BWE_VALIDATION_CLOSE_TIMEOUT="BUILD_FLD_BWE_VALIDATION_CLOSE_TIMEOUT",
		BUILD_FLD_BROWSER_LAUNCHER_COMMAND_WINDOWS_64="BUILD_FLD_BROWSER_LAUNCHER_COMMAND_WINDOWS_64",
		BUILD_FLD_BROWSER_LAUNCHER_COMMAND_WINDOWS_32="BUILD_FLD_BROWSER_LAUNCHER_COMMAND_WINDOWS_32",
		BUILD_FLD_BROWSER_LAUNCHER_COMMAND_LINUX="BUILD_FLD_BROWSER_LAUNCHER_COMMAND_LINUX",
		BUILD_FIREFOX_PATH_WINDOWS_64="BUILD_FIREFOX_PATH_WINDOWS_64",
		BUILD_FIREFOX_PATH_WINDOWS_32="BUILD_FIREFOX_PATH_WINDOWS_32",
		BUILD_FIREFOX_PATH_LINUX="BUILD_FIREFOX_PATH_LINUX",
		BUILD_FLD_SPLIT="BUILD_FLD_SPLIT",
		BUILD_FLD_SPLIT_MAX_COUNT="BUILD_FLD_SPLIT_MAX_COUNT",
		BUILD_FTP_CONNECT_TIMEOUT="BUILD_FTP_CONNECT_TIMEOUT",
		BUILD_FLD_FORMS_INJECTION_COMPLETION_BASED_ON_LOG_FILE="BUILD_FLD_FORMS_INJECTION_COMPLETION_BASED_ON_LOG_FILE",
		BUILD_WAIT_TIME_FOR_ORACLE_FORM_TO_CLOSE="BUILD_WAIT_TIME_FOR_ORACLE_FORM_TO_CLOSE",
		BUILD_PAUSE_BETWEEN_SCRIPT_UPLOAD_AND_SCRIPT_EXECUTE="BUILD_PAUSE_BETWEEN_SCRIPT_UPLOAD_AND_SCRIPT_EXECUTE",
		BUILD_PAUSE_BETWEEN_SCRIPT_COMPLETED_AND_LOG_FILE_RETRIEVE="BUILD_PAUSE_BETWEEN_SCRIPT_COMPLETED_AND_LOG_FILE_RETRIEVE",
		BUILD_PAUSE_BETWEEN_SCRIPT_COMPLETED_AND_BROWSER_TERMINATION="BUILD_PAUSE_BETWEEN_SCRIPT_COMPLETED_AND_BROWSER_TERMINATION",
		BUILD_FORCE_CLOSE_BROWSER="BUILD_FORCE_CLOSE_BROWSER",
		BUILD_TEST_IL_FILE="BUILD_TEST_IL_FILE",
		BUILD_TEST_IL_HOST_NAME="BUILD_TEST_IL_HOST_NAME",
		BUILD_TEST_IL_PORT_NUMBER="BUILD_TEST_IL_PORT_NUMBER",
		BUILD_HTML_SPLIT="BUILD_HTML_SPLIT",
		BUILD_HTML_SPLIT_MAX_COUNT="BUILD_HTML_SPLIT_MAX_COUNT",
		BUILD_RESET_DOM_LOADING_TIMER="BUILD_RESET_DOM_LOADING_TIMER",
		BUILD_HTML_DEBUG="BUILD_HTML_DEBUG",
		BUILD_FLD_LOG_SIZE_MAX_CONFIRMATION_DELAY_SECONDS="BUILD_FLD_LOG_SIZE_MAX_CONFIRMATION_DELAY_SECONDS",
		BUILD_HTML_LOG_SIZE_MAX_CONFIRMATION_DELAY_SECONDS="BUILD_HTML_LOG_SIZE_MAX_CONFIRMATION_DELAY_SECONDS",
        BUILD_INJECTORS_PACKAGE_FILE = "BUILD_INJECTORS_PACKAGE_FILE",
        BUILD_ENVIRONMENT_FILE = "BUILD_ENVIRONMENT_FILE",
        BUILD_SINGLE_SIGN_ON_FIELD_IDENTIFIER_TYPE_ID = "ID",
        BUILD_SINGLE_SIGN_ON_FIELD_IDENTIFIER_TYPE_NAME = "NAME",
        BUILD_API_PACKAGES_OUTPUT_FOLDER ="BUILD_API_PACKAGES_OUTPUT_FOLDER",
        BUILD_API_PACKAGES_INPUT_FOLDER ="BUILD_API_PACKAGES_INPUT_FOLDER",
        BUILD_API_JDBC_STRING="BUILD_API_JDBC_STRING",
        BUILD_API_DBMS_OUTPUT_ENABLED="BUILD_API_DBMS_OUTPUT_ENABLED",
        BUILD_EXPECTED_FLD_APPLET_TITLE="BUILD_EXPECTED_FLD_APPLET_TITLE",
        BUILD_SSH_PASSPHRASE="BUILD_SSH_PASSPHRASE",
        BUILD_BROWSER_SANITY_CHECK_WAITING_PERIOD_SECONDS="BUILD_BROWSER_SANITY_CHECK_WAITING_PERIOD_SECONDS",
        BUILD_LOCALHOST_HTTP_PORT_STARTING_NUMBER="BUILD_LOCALHOST_HTTP_PORT_STARTING_NUMBER",
        BUILD_FLD_WAITING_TIME_BEFORE_ITERATION_RETRY_SECONDS="BUILD_FLD_WAITING_TIME_BEFORE_ITERATION_RETRY_SECONDS",
        BUILD_HTML_WAITING_TIME_BEFORE_ITERATION_RETRY_SECONDS="BUILD_HTML_WAITING_TIME_BEFORE_ITERATION_RETRY_SECONDS",
        BUILD_FLD_MAXIMUM_ITERATION_EXECUTION_ATTEMPTS="BUILD_FLD_MAXIMUM_ITERATION_EXECUTION_ATTEMPTS",
        BUILD_HTML_MAXIMUM_ITERATION_EXECUTION_ATTEMPTS="BUILD_HTML_MAXIMUM_ITERATION_EXECUTION_ATTEMPTS",
        BUILD_ITERATION_LOG_FOLDER="BUILD_ITERATION_LOG_FOLDER",
        BUILD_CONTINUE_ON_FAILED_INJECTOR="BUILD_CONTINUE_ON_FAILED_INJECTOR",
        BUILD_CONTINUE_ON_FAILED_ITERATION="BUILD_CONTINUE_ON_FAILED_ITERATION",
        BUILD_SPLIT_INJECTOR_BUFFER_SIZE="BUILD_SPLIT_INJECTOR_BUFFER_SIZE",
        BUILD_ROBOT_PASTE_MAX_DATA_SIZE_DURING_FLD_LOGGING_CHARS="BUILD_ROBOT_PASTE_MAX_DATA_SIZE_DURING_FLD_LOGGING_CHARS",
        BUILD_ROBOT_PASTE_DELAY_AFTER_INJECTOR_START_SECONDS="BUILD_ROBOT_PASTE_DELAY_AFTER_INJECTOR_START_SECONDS",
        BUILD_ROBOT_PASTE_BEFORE_KEY_PRESS_DELAY_MS = "BUILD_ROBOT_PASTE_BEFORE_KEY_PRESS_DELAY_MS",
        BUILD_ROBOT_PASTE_DELAY_AFTER_LOG_HINT_SECONDS = "BUILD_ROBOT_PASTE_DELAY_AFTER_LOG_HINT_SECONDS",
        BUILD_JVM_ARGUMENTS = "BUILD_JVM_ARGUMENTS",
        BUILD_MAX_LINES_IN_PARTIAL_SOURCE_FILE="BUILD_MAX_LINES_IN_PARTIAL_SOURCE_FILE",
        BUILD_HALT_FLD_INJECTION_IF_LOG_MISMATCHES="BUILD_HALT_FLD_INJECTION_IF_LOG_MISMATCHES",
        BUILD_VIEW_LAST_SUCCESSFUL_OR_FAILED_ITERATION_COUNT="BUILD_VIEW_LAST_SUCCESSFUL_OR_FAILED_ITERATION_COUNT",
        BUILD_ASSUME_FLD_LOG_ALWAYS_END_WITH_NEW_LINE="BUILD_ASSUME_FLD_LOG_ALWAYS_END_WITH_NEW_LINE",
        BUILD_OPEN_SCRIPT_AND_LOG_IN_HTML_BY_DEFAULT="BUILD_OPEN_SCRIPT_AND_LOG_IN_HTML_BY_DEFAULT",
        
        ONLINE_ACTIVATION_URL="ONLINE_ACTIVATION_URL",
        
        INJECT_IS_FOCUS_MODE ="INJECT_IS_FOCUS_MODE",
        INJECT_FIREFOX_PATH_LINUX="INJECT_FIREFOX_PATH_LINUX",
        INJECT_FIREFOX_PATH_WINDOWS="INJECT_FIREFOX_PATH_WINDOWS",
       	INJECT_NODE_JAVA_PATH="INJECT_NODE_JAVA_PATH",
       	INJECT_IS_REUSE_NAVIGATION="INJECT_IS_REUSE_NAVIGATION",
       	INJECT_FIREFOX_PROFILE_NAME="FIREFOX_PROFILE_NAME",
       	INJECT_PAGE_LOAD_TIMEOUT_IN_SECONDS="INJECT_PAGE_LOAD_TIMEOUT_IN_SECONDS",
       	INJECT_HOME_PAGE_TIMER_ACTIVATION="INJECT_HOME_PAGE_TIMER_ACTIVATION",
       	INJECT_HOME_PAGE_TIMER_IN_MINS="INJECT_HOME_PAGE_TIMER_IN_MINS",
       			
       	SNAPSHOT_SHOW_WARNING_DUPLICATE_DATA ="SNAPSHOT_SHOW_WARNING_DUPLICATE_DATA",
       	UPGRADE_SCENARIO_EXPLODED_FOLDER="UPGRADE_SCENARIO_EXPLODED_FOLDER",
       	SNAPSHOT_OVERRIDE_PACKAGE_FOLDER="SNAPSHOT_OVERRIDE_PACKAGE_FOLDER",
       	UPGRADE_DISABLE_NAVIGATION_NAME_SELECTION="UPGRADE_DISABLE_NAVIGATION_NAME_SELECTION",
       	UPGRADE_EXTRA_RSC_INVENTORIES_FOLDER="UPGRADE_EXTRA_RSC_INVENTORIES_FOLDER",
       	SNAPSHOT_RECORD_PROCESSING_BATCH_SIZE = "SNAPSHOT_RECORD_PROCESSING_BATCH_SIZE",
       	SNAPSHOT_RECORD_PROCESSING_XLSX_BATCH_SIZE = "SNAPSHOT_RECORD_PROCESSING_XLSX_BATCH_SIZE",
       	SNAPSHOT_DELETE_BATCH_SIZE = "SNAPSHOT_DELETE_BATCH_SIZE",
       	SNAPSHOT_IMPORT_BATCH_SIZE = "SNAPSHOT_IMPORT_BATCH_SIZE",
       	SNAPSHOT_IS_EXPORT_SNAPSHOT_ENCRYPTED="IS_EXPORT_SNAPSHOT_ENCRYPTED"
        ;

	public static String getInjectFirefoxPath()
	{
		if (Config.getInstance().containsProperty(INJECT_FIREFOX_PATH_WINDOWS) || Config.getInstance().containsProperty(INJECT_FIREFOX_PATH_LINUX)) {
			return Config.getInstance().getStringProperty(getInjectFirefoxPathPropertyName());
		}
		return null;
	}
	
	public static int getInjectPageLoadTimeout() {
		final Integer output = Config.getInstance().getIntegerProperty(INJECT_PAGE_LOAD_TIMEOUT_IN_SECONDS);
		Assert.notNull(output);
		Assert.isTrue(output >= 0, INJECT_PAGE_LOAD_TIMEOUT_IN_SECONDS+" must be positive or zero");
		return output;
	}
	
	public static int getInjectHomePageTimerInMins() {
		final Integer output = Config.getInstance().getIntegerProperty(INJECT_HOME_PAGE_TIMER_IN_MINS);
		Assert.notNull(output);
		Assert.isTrue(output >= 0, INJECT_HOME_PAGE_TIMER_IN_MINS+" must be positive or zero");
		return output;
	}
	
	public static String getInjectFirefoxProfileName()
	{
		if (Config.getInstance().containsProperty(INJECT_FIREFOX_PROFILE_NAME)) {
			return Config.getInstance().getStringProperty(INJECT_FIREFOX_PROFILE_NAME);
		}
		return null;
	}
	
	public static Boolean getInjectIsReuseNavigation() {
		final Boolean output = Config.getInstance().getBooleanProperty(INJECT_IS_REUSE_NAVIGATION);
		Assert.notNull(output, INJECT_IS_REUSE_NAVIGATION+" is null");
		return output;
	}
	
	public static String getInjectFirefoxPathPropertyName()
	{
	        String toReturn = null;
	        if ( Platform.isWindows() )
	        {
	            toReturn = INJECT_FIREFOX_PATH_WINDOWS;
	        }
	        else if ( Platform.isLinux() )
	        {
	            toReturn = INJECT_FIREFOX_PATH_LINUX;
	        }
	        else
	        {
	            throw new Error("Unrecognized platform: " + Platform.getOSType());
	        }
	        return toReturn;
	}
	
	public static Boolean getInjectFocusMode() {
		final Boolean output = Config.getInstance().getBooleanProperty(INJECT_IS_FOCUS_MODE);
		Assert.notNull(output, INJECT_IS_FOCUS_MODE+" is null");
		return output;
	}
	
	public static Boolean getInjectHomePageTimerActivation() {
		final Boolean output = Config.getInstance().getBooleanProperty(INJECT_HOME_PAGE_TIMER_ACTIVATION);
		Assert.notNull(output, INJECT_HOME_PAGE_TIMER_ACTIVATION+" is null");
		return output;
	}
	
	public static Boolean getSnapshotShowWarningDuplicateData() {
		final Boolean output = Config.getInstance().getBooleanProperty(SNAPSHOT_SHOW_WARNING_DUPLICATE_DATA);
		Assert.notNull(output, SNAPSHOT_SHOW_WARNING_DUPLICATE_DATA+" is null");
		return output;
	}
	
	public static Boolean getUpgradeDisableNavigationNameSelection() {
		final Boolean output = Config.getInstance().getBooleanProperty(UPGRADE_DISABLE_NAVIGATION_NAME_SELECTION);
		Assert.notNull(output, UPGRADE_DISABLE_NAVIGATION_NAME_SELECTION+" is null");
		return output;
	}
	
	public static Boolean getBuildContinueOnFailedIteration() {
		final Boolean output = Config.getInstance().getBooleanProperty(BUILD_CONTINUE_ON_FAILED_ITERATION);
		Assert.notNull(output, BUILD_CONTINUE_ON_FAILED_ITERATION+" is null");
		return output;
	}

	public static Boolean getBuildContinueOnFailedInjector() {
		final Boolean output = Config.getInstance().getBooleanProperty(BUILD_CONTINUE_ON_FAILED_INJECTOR);
		Assert.notNull(output, BUILD_CONTINUE_ON_FAILED_INJECTOR+" is null");
		return output;
	}

	public static File getBuildIterationLogFolder() {
    	final File output = new File(CoreUtil.normalizePathSeparators(Config.getInstance().getStringProperty(BUILD_ITERATION_LOG_FOLDER)));
		Assert.isTrue(!output.exists() || output.isDirectory(), String.format("If %s exists, it must be a directory", BUILD_ITERATION_LOG_FOLDER));
		if (!output.exists()) {
			output.mkdirs();
		}
		return output;
	}

	public static int getBuildFldMaximumIterationExecutionAttempts() {
		final Integer output = Config.getInstance().getIntegerProperty(BUILD_FLD_MAXIMUM_ITERATION_EXECUTION_ATTEMPTS);
		Assert.notNull(output);
		Assert.isTrue(output > 0, BUILD_FLD_MAXIMUM_ITERATION_EXECUTION_ATTEMPTS+" must be positive");
		return output;
	}

	public static int getBuildHtmlMaximumIterationExecutionAttempts() {
		final Integer output = Config.getInstance().getIntegerProperty(BUILD_HTML_MAXIMUM_ITERATION_EXECUTION_ATTEMPTS);
		Assert.notNull(output);
		Assert.isTrue(output > 0, BUILD_HTML_MAXIMUM_ITERATION_EXECUTION_ATTEMPTS+" must be positive");
		return output;
	}

	public static int getBuildFldWaitingTimeBeforeIterationRetrySeconds() {
		final Integer output = Config.getInstance().getIntegerProperty(BUILD_FLD_WAITING_TIME_BEFORE_ITERATION_RETRY_SECONDS);
		Assert.notNull(output);
		Assert.isTrue(output > 0, BUILD_FLD_WAITING_TIME_BEFORE_ITERATION_RETRY_SECONDS+" must be positive");
		return output;
	}

	public static int getBuildHtmlWaitingTimeBeforeIterationRetrySeconds() {
		final Integer output = Config.getInstance().getIntegerProperty(BUILD_HTML_WAITING_TIME_BEFORE_ITERATION_RETRY_SECONDS);
		Assert.notNull(output);
		Assert.isTrue(output > 0, BUILD_HTML_WAITING_TIME_BEFORE_ITERATION_RETRY_SECONDS+" must be positive");
		return output;
	}

	public static Integer getBuildBrowserSanityCheckWaitingPeriodSeconds() {
		final Integer output = Config.getInstance().getIntegerProperty(BUILD_BROWSER_SANITY_CHECK_WAITING_PERIOD_SECONDS);
		Assert.notNull(output);
		Assert.isTrue(output > 0, BUILD_BROWSER_SANITY_CHECK_WAITING_PERIOD_SECONDS+" must be positive");
		return output;
	}

	public static File getTempFolder() {
		if (SwiftGUIMain.getInstance() == null) {
			//for update
			return new File(Config.getInstance().getStringProperty(CoreConstants.CONFIG_CLIENT_TEMP_FOLDER));
		} else {
			//for build, reverse, validate
			return new File(Config.getInstance().getStringProperty(CoreConstants.CONFIG_CLIENT_TEMP_FOLDER), SwiftGUIMain.getInstance().getShortApplicationName().toString());
		}
	}
	
	public static String getClientTempFolderName() {
		return Config.getInstance().getStringProperty(CoreConstants.CONFIG_CLIENT_TEMP_FOLDER);
	}

	public static File getReverseOutputFolder() {
		return new File(Config.getInstance().getStringProperty(REVERSE_OUTPUT_FOLDER));
	}

	public static File getReverseSessionsFolder() {
		return new File(Config.getInstance().getStringProperty(REVERSE_SESSIONS_FOLDER));
	}

	public static Integer getBuildLocalhostHttpPortStartingNumber() {
		final Integer output = Config.getInstance().getIntegerProperty(BUILD_LOCALHOST_HTTP_PORT_STARTING_NUMBER);
		Assert.notNull(output, BUILD_LOCALHOST_HTTP_PORT_STARTING_NUMBER+" must not be null");
		Assert.isTrue(output >= 0, BUILD_LOCALHOST_HTTP_PORT_STARTING_NUMBER+" must not be negative");
		return output;
	}




    public static void logAllProperties()
    {
        Config.getInstance().checkLastModifiedTimeAndReloadAsNecessary();
        for ( Entry<Object, Object> entry :  System.getProperties().entrySet() )
        {
        	FileUtils.println("System.getProperties() : " + entry.getKey() + "=" + entry.getValue());
        }
        Config.getInstance().dumpConfigFileToLogFile();
    }


	public static String getDbUserLanguage() {
		if(Config.getInstance().containsProperty(DB_USER_LANGUAGE) && !Config.getInstance().isPropertyBlank(DB_USER_LANGUAGE))
			return Config.getInstance().getStringProperty(DB_USER_LANGUAGE);
		return null;
	}

	public static File getReverseEnvironmentFile() {
		if(Config.getInstance().containsProperty(CoreConstants.CONFIG_CLIENT_REVERSE_ENVIRONMENT_FILE) && !Config.getInstance().isPropertyBlank(CoreConstants.CONFIG_CLIENT_REVERSE_ENVIRONMENT_FILE))
			return Config.getInstance().getFileProperty(CoreConstants.CONFIG_CLIENT_REVERSE_ENVIRONMENT_FILE);
		return null;
	}

	public static File getReverseInventoriesPackageLocation() {
		if(Config.getInstance().containsProperty(CoreConstants.CONFIG_CLIENT_REVERSE_INVENTORIES_PACKAGE_LOCATION) && !Config.getInstance().isPropertyBlank(CoreConstants.CONFIG_CLIENT_REVERSE_INVENTORIES_PACKAGE_LOCATION))
			return Config.getInstance().getFileProperty(CoreConstants.CONFIG_CLIENT_REVERSE_INVENTORIES_PACKAGE_LOCATION);
		return null;
	}

	public static String getReverseExtractionStatusExportFileName() {
		if(Config.getInstance().containsProperty(CoreConstants.CONFIG_CLIENT_REVERSE_EXTRACTION_STATUS_EXPORT_FILE) && !Config.getInstance().isPropertyBlank(CoreConstants.CONFIG_CLIENT_REVERSE_EXTRACTION_STATUS_EXPORT_FILE))
			return Config.getInstance().getStringProperty(CoreConstants.CONFIG_CLIENT_REVERSE_EXTRACTION_STATUS_EXPORT_FILE);
		return null;
	}
	
	public static String getReverseOutputDetailsFile() {
		if(Config.getInstance().containsProperty(CoreConstants.CONFIG_CLIENT_REVERSE_OUTPUT_DETAILS_FILE) && !Config.getInstance().isPropertyBlank(CoreConstants.CONFIG_CLIENT_REVERSE_OUTPUT_DETAILS_FILE))
			return Config.getInstance().getStringProperty(CoreConstants.CONFIG_CLIENT_REVERSE_OUTPUT_DETAILS_FILE);
		return null;		
	}	

	public static String getReverseTemplateFolder() {
		if(Config.getInstance().containsProperty(REVERSE_TEMPLATE_FOLDER) && !Config.getInstance().isPropertyBlank(REVERSE_TEMPLATE_FOLDER))
			return Config.getInstance().getStringProperty(REVERSE_TEMPLATE_FOLDER);
		return null;
	}

	public static String getReverseTemplateName() {
		if(Config.getInstance().containsProperty(REVERSE_TEMPLATE_NAME) && !Config.getInstance().isPropertyBlank(REVERSE_TEMPLATE_NAME))
			return Config.getInstance().getStringProperty(REVERSE_TEMPLATE_NAME);
		return null;
	}

	public static int getReverseWorkersCount() {
		return Config.getInstance().getIntegerProperty(REVERSE_WORKERS_COUNT);
	}

    public static String getBuildFirefoxPathPropertyName()
    {
        String toReturn = null;
        if ( Platform.isWindows() )
        {
            if ( Platform.is64Bit() )
            {
                toReturn = BUILD_FIREFOX_PATH_WINDOWS_64;
            }
            else
            {
                File temp = new File("c:/Program Files (x86)");
                if ( temp.exists() && temp.isDirectory() )
                {
                    toReturn = BUILD_FIREFOX_PATH_WINDOWS_64;
                }
                else
                {
                    toReturn = BUILD_FIREFOX_PATH_WINDOWS_32;
                }
            }
        }
        else if ( Platform.isLinux() )
        {
            toReturn = BUILD_FIREFOX_PATH_LINUX;
        }
        else
        {
            throw new Error("Unrecognized platform: " + Platform.getOSType());
        }
        return toReturn;
    }

	public static String getBuildFirefoxPath()
	{
		return Config.getInstance().getStringProperty(getBuildFirefoxPathPropertyName());
	}

	public static String getReverseDataLabel() {
		return Config.getInstance().getStringProperty(REVERSE_DATA_LABEL);
	}

	public static int getReverseFetchSize() {
		return Config.getInstance().getIntegerProperty(REVERSE_FETCH_SIZE);
	}

	public static int getMaximumRecordsPerDataFile() {
		return Config.getInstance().getIntegerProperty(REVERSE_MAXIMUM_RECORDS_PER_DATA_FILE);
	}

	//Truncates the file if it exists, if it doesnt exist, it creates it.
	public static File getReverseZipFileLocation() {
		String result = null;
		if(Config.getInstance().containsProperty(REVERSE_ZIP_FILE_LOCATION) && !Config.getInstance().isPropertyBlank(REVERSE_ZIP_FILE_LOCATION)) {
			result = Config.getInstance().getStringProperty(REVERSE_ZIP_FILE_LOCATION);
			return new File(result);
		}
		return null;
	}

	public static int getReverseRetrySleepTime() {
		return Config.getInstance().getIntegerProperty(REVERSE_RETRY_SLEEP_TIME);
	}

	public static String getRscPrerequisiteObjectsKeyword () {
		return Config.getInstance().getStringProperty(RSC_PREREQUISITE_OBJECTS_KEYWORD );
	}

	public static int getBuildSshSocketTimeoutSeconds() {
		return Config.getInstance().getIntegerProperty(BUILD_SSH_SOCKET_TIMEOUT_SECONDS);
	}

	public static int getBuildApiInjectionCommitAfterXRows() {
		final int COMMIT_AFTER_X_ROWS = Config.getInstance().getIntegerProperty(BUILD_API_INJECTION_COMMIT_AFTER_X_ROWS);
        if ( COMMIT_AFTER_X_ROWS <= 0 ) throw new Error("Properties error: " + Config.BUILD_API_INJECTION_COMMIT_AFTER_X_ROWS + " must be greater than 0.");
        return COMMIT_AFTER_X_ROWS;
	}

	public static int getBuildApiInjectionWorkerCount() {
        return Config.getInstance().getIntegerProperty(BUILD_API_INJECTION_WORKER_COUNT);
    }

	public static int getBuildFldFormOpenMaxIteration() {
		return Config.getInstance().getIntegerProperty(BUILD_FLD_FORM_OPEN_MAX_ITERATION);
	}

	public static int getBuildFldFormCloseMaxIteration() {
		if(Config.getInstance().containsProperty(BUILD_FLD_FORM_CLOSE_MAX_ITERATION) && !Config.getInstance().isPropertyBlank(BUILD_FLD_FORM_CLOSE_MAX_ITERATION))
			return Config.getInstance().getIntegerProperty(BUILD_FLD_FORM_CLOSE_MAX_ITERATION);
		return -1;
	}

	public static int getBuildHtmlFormCloseMaxIteration() {
		if(Config.getInstance().containsProperty(BUILD_HTML_FORM_CLOSE_MAX_ITERATION) && !Config.getInstance().isPropertyBlank(BUILD_HTML_FORM_CLOSE_MAX_ITERATION))
			return Config.getInstance().getIntegerProperty(BUILD_HTML_FORM_CLOSE_MAX_ITERATION);
		return -1;
	}

	public static int getBuildFldBweValidationOpenTimeout() {
		return Config.getInstance().getIntegerProperty(BUILD_FLD_BWE_VALIDATION_OPEN_TIMEOUT);
	}

	public static int getBuildFldBweValidationCloseTimeout() {
		return Config.getInstance().getIntegerProperty(BUILD_FLD_BWE_VALIDATION_CLOSE_TIMEOUT);
	}


	public static String getBuildFldBrowserLauncherCommandPropertyName()
	{
        String toReturn = null;
        if ( Platform.isWindows() )
        {
            if ( Platform.is64Bit() )
            {
                toReturn = BUILD_FLD_BROWSER_LAUNCHER_COMMAND_WINDOWS_64;
            }
            else
            {
                File temp = new File("c:/Program Files (x86)");
                if ( temp.exists() && temp.isDirectory() )
                {
                    toReturn = BUILD_FLD_BROWSER_LAUNCHER_COMMAND_WINDOWS_64;
                }
                else
                {
                    toReturn = BUILD_FLD_BROWSER_LAUNCHER_COMMAND_WINDOWS_32;
                }
            }
        }
        else if ( Platform.isLinux() )
        {
            toReturn = BUILD_FLD_BROWSER_LAUNCHER_COMMAND_LINUX;
        }
        else
        {
            throw new Error("Unrecognized platform: " + Platform.getOSType());
        }
        return toReturn;
	}

	public static String getBuildFldBrowserLauncherCommand()
	{
        return Config.getInstance().getStringProperty(getBuildFldBrowserLauncherCommandPropertyName());
	}

	public static boolean getBuildFldSplit() {
		return Config.getInstance().getBooleanProperty(BUILD_FLD_SPLIT);
	}

	public static int getBuildFldSplitMaxCount() {
		return Config.getInstance().getIntegerProperty(BUILD_FLD_SPLIT_MAX_COUNT);
	}

	public static int getBuildFtpConnectTimeout() {
		return Config.getInstance().getIntegerProperty(BUILD_FTP_CONNECT_TIMEOUT);
	}

	public static boolean getBuildFldFormsInjectionCompletionBasedOnLogFile() {
		return Config.getInstance().getBooleanProperty(BUILD_FLD_FORMS_INJECTION_COMPLETION_BASED_ON_LOG_FILE);
	}

	public static int getBuildWaitTimeForOracleFormToClose() {
		return Config.getInstance().getIntegerProperty(BUILD_WAIT_TIME_FOR_ORACLE_FORM_TO_CLOSE);
	}

	public static int getBuildPauseBetweenScriptUploadAndScriptExecute() {
		return Config.getInstance().getIntegerProperty(BUILD_PAUSE_BETWEEN_SCRIPT_UPLOAD_AND_SCRIPT_EXECUTE);
	}

	public static int getBuildPauseBetweenScriptCompletedAndLogFileRetrieve() {
		return Config.getInstance().getIntegerProperty(BUILD_PAUSE_BETWEEN_SCRIPT_COMPLETED_AND_LOG_FILE_RETRIEVE);
	}

	public static int getBuildPauseBetweenScriptCompletedAndBrowserTermination() {
		return Config.getInstance().getIntegerProperty(BUILD_PAUSE_BETWEEN_SCRIPT_COMPLETED_AND_BROWSER_TERMINATION);
	}

	public static boolean getBuildForceCloseBrowser() {
		return Config.getInstance().getBooleanProperty(BUILD_FORCE_CLOSE_BROWSER);
	}

	public static String getBuildTestIlFile() {
		return Config.getInstance().getStringProperty(BUILD_TEST_IL_FILE);
	}

	public static String getBuildTestIlHostName() {
		return Config.getInstance().getStringProperty(BUILD_TEST_IL_HOST_NAME);
	}

	public static String getBuildTestIlPortNumber() {
		return Config.getInstance().getStringProperty(BUILD_TEST_IL_PORT_NUMBER);
	}

	public static boolean getBuildHtmlSplit() {
		return Config.getInstance().getBooleanProperty(BUILD_HTML_SPLIT);
	}

	public static int getBuildHtmlSplitMaxCount() {
		return Config.getInstance().getIntegerProperty(BUILD_HTML_SPLIT_MAX_COUNT);
	}

	public static int getBuildResetDomLoadingTimer() {
		if(Config.getInstance().containsProperty(BUILD_RESET_DOM_LOADING_TIMER) && !Config.getInstance().isPropertyBlank(BUILD_RESET_DOM_LOADING_TIMER))
			return Config.getInstance().getIntegerProperty(BUILD_RESET_DOM_LOADING_TIMER);
		return XULConstants.RESET_DOM_LOADING_DEFAULT_TIME;
	}

	public static boolean getBuildHtmlDebug() {
		return Config.getInstance().getBooleanProperty(BUILD_HTML_DEBUG);
	}

	public static boolean isEnvironmentValidationMandatory() {
		return Config.getInstance().getBooleanProperty(IS_ENVIRONMENT_VALIDATION_MANDATORY);
	}

	public static int getBuildFldLogSizeMaxConfirmationDelaySeconds() {
		return Config.getInstance().getIntegerProperty(BUILD_FLD_LOG_SIZE_MAX_CONFIRMATION_DELAY_SECONDS);
	}

	public static int getBuildHtmlLogSizeMaxConfirmationDelaySeconds() {
		return Config.getInstance().getIntegerProperty(BUILD_HTML_LOG_SIZE_MAX_CONFIRMATION_DELAY_SECONDS);
	}


	public static String getCmdTextEditor()
	{
        String toReturn = null;
        if ( Platform.isWindows() )
        {
            toReturn = Config.getInstance().getStringProperty(CMD_TEXT_EDITOR_WINDOWS);
        }
        else if ( Platform.isLinux() )
        {
            toReturn = Config.getInstance().getStringProperty(CMD_TEXT_EDITOR_LINUX);
        }
        else
        {
            throw new Error("Unrecognized platform: " + Platform.getOSType());
        }
        return toReturn;
	}

	public static File getBuildInjectorsPackageFile() {
		if(Config.getInstance().containsProperty(BUILD_INJECTORS_PACKAGE_FILE) && !Config.getInstance().isPropertyBlank(BUILD_INJECTORS_PACKAGE_FILE))
			return Config.getInstance().getFileProperty(BUILD_INJECTORS_PACKAGE_FILE);
		return null;
	}

	public static File getBuildEnvironmentFile() {
		if(Config.getInstance().containsProperty(BUILD_ENVIRONMENT_FILE) && !Config.getInstance().isPropertyBlank(BUILD_ENVIRONMENT_FILE))
			return Config.getInstance().getFileProperty(BUILD_ENVIRONMENT_FILE);
		return null;
	}

	public static boolean getReversePrintSqlToLog() {
		return Config.getInstance().getBooleanProperty(REVERSE_PRINT_SQL_TO_LOG) && Utils.hasAccessToInternalStaffsOnlyFeatures();
	}
	
    public static List<String> getReverseExpectedSeededUsersEbsVersion11() {
    	final String usersCommaSeparated = Config.getInstance().getProperty(REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_11);
    	return Utils.splitAndTrimCommaSeparatedString(usersCommaSeparated);
    }
    
    public static List<String> getReverseExpectedSeededUsersEbsVersion12() {
    	final String usersCommaSeparated = Config.getInstance().getProperty(REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_12);
    	return Utils.splitAndTrimCommaSeparatedString(usersCommaSeparated);
    }
    
    public static List<String> getReverseExpectedSeededUsersEbsVersionFusion() {
    	final String usersCommaSeparated = Config.getInstance().getProperty(REVERSE_EXPECTED_SEEDED_USERS_EBS_VERSION_FUSION);
    	return Utils.splitAndTrimCommaSeparatedString(usersCommaSeparated);
    }
    
    public static int getReverseMaxPotentialUserCount() {
    	return Config.getInstance().getIntegerProperty(REVERSE_MAX_POTENTIAL_SEEDED_USER_COUNT);
    }	

	public static boolean getReverseTerminateAfterFailedInitializationInAutomatedMode() {
		return Config.getInstance().getBooleanProperty(REVERSE_TERMINATE_AFTER_FAILED_INTIALIZATION_IN_AUTOMATED_MODE);
	}

	public static boolean isExtractAudit() {
		if(Config.getInstance().containsProperty(REVERSE_IS_EXTRACT_AUDIT) && !Config.getInstance().isPropertyBlank(REVERSE_IS_EXTRACT_AUDIT))
			return Config.getInstance().getBooleanProperty(REVERSE_IS_EXTRACT_AUDIT);
		return false;
	}

	public static File getBuildApiPackagesOutputFolder() {
		return Config.getInstance().getDirectoryProperty(BUILD_API_PACKAGES_OUTPUT_FOLDER);
	}

	public static File getBuildApiPackagesInputFolder() {
		return Config.getInstance().getDirectoryProperty(BUILD_API_PACKAGES_INPUT_FOLDER);
	}

	public static String getBuildApiJdbcString() {
		if(Config.getInstance().containsProperty(BUILD_API_JDBC_STRING) && !Config.getInstance().isPropertyBlank(BUILD_API_JDBC_STRING))
			return Config.getInstance().getStringProperty(BUILD_API_JDBC_STRING);
		return null;
	}

	public static boolean getBuildApiDbmsOutputEnabled() {
		return Config.getInstance().getBooleanProperty(BUILD_API_DBMS_OUTPUT_ENABLED);
	}

	public static boolean isAutomatedRun() {
		return Config.getInstance().getBooleanProperty(CoreConstants.CONFIG_CLIENT_AUTOMATED_RUN);
	}

	public static Boolean getBuildTerminateAfterFailedInjection() {
		return Config.getInstance().getBooleanProperty(BUILD_TERMINATE_AFTER_FAILED_INJECTION);
	}

	public static int getBuildPreInjectionDelaySecondsOnAutomatedMode() {
	    int output = Config.getInstance().getIntegerProperty(BUILD_PRE_INJECTION_DELAY_SECONDS_ON_AUTOMATED_MODE);
		Assert.isTrue(output >= 0, BUILD_PRE_INJECTION_DELAY_SECONDS_ON_AUTOMATED_MODE + " must be positive integer");
		return output;
	}

    public static int getBuildRobotPasteMaxDataSizeChars()
    {
        int output = Config.getInstance().getIntegerProperty(BUILD_ROBOT_PASTE_MAX_DATA_SIZE_DURING_FLD_LOGGING_CHARS);
        Assert.isTrue(output >= 1, BUILD_ROBOT_PASTE_MAX_DATA_SIZE_DURING_FLD_LOGGING_CHARS + " must be positive integer");
        return output;
    }

    public static int getBuildRobotPasteDelayAfterInjectorStartSeconds()
    {
        int output = Config.getInstance().getIntegerProperty(BUILD_ROBOT_PASTE_DELAY_AFTER_INJECTOR_START_SECONDS);
        Assert.isTrue(output >= 1, BUILD_ROBOT_PASTE_DELAY_AFTER_INJECTOR_START_SECONDS + " must be positive integer");
        return output;
    }

    public static int getBuildRobotBeforeKeyPressDelayMS()
    {
        int output = Config.getInstance().getIntegerProperty(BUILD_ROBOT_PASTE_BEFORE_KEY_PRESS_DELAY_MS);
        Assert.isTrue(output >= 1, BUILD_ROBOT_PASTE_BEFORE_KEY_PRESS_DELAY_MS + " must be positive integer");
        return output;
    }

    public static int getBuildRobotPasteDelayAfterLogIntSeconds()
    {
        int output = Config.getInstance().getIntegerProperty(BUILD_ROBOT_PASTE_DELAY_AFTER_LOG_HINT_SECONDS);
        Assert.isTrue(output >= 1, BUILD_ROBOT_PASTE_DELAY_AFTER_LOG_HINT_SECONDS + " must be positive integer");
        return output;
    }

	public static File getLogFolder() {
		File folder = new File(Config.getInstance().getStringProperty(CoreConstants.CONFIG_CLIENT_LOG_FOLDER));
		folder.mkdirs();
		return folder;
	}

	public static String getBuildExpectedFldAppletTitle() {
		String output = Config.getInstance().getStringProperty(BUILD_EXPECTED_FLD_APPLET_TITLE);
		Assert.isTrue(StringUtils.isNotBlank(output), BUILD_EXPECTED_FLD_APPLET_TITLE + " must not be blank");
		return output;
	}

	public static boolean containsBuildSshPassphrase() {
		return Config.getInstance().containsProperty(BUILD_SSH_PASSPHRASE);
	}

	public static String getBuildSshPassphrase() {
		return Config.getInstance().getStringProperty(BUILD_SSH_PASSPHRASE);
	}

	public static void validateConfig()
	{
	    Config.getInstance().validate();
	}
	protected void validate() {

		Boolean automatedRun = Config.isAutomatedRun();

		if (Boolean.TRUE.equals(automatedRun)) {

			Config.getBuildEnvironmentFile();
			Config.getBuildInjectorsPackageFile();
			Config.getBuildPreInjectionDelaySecondsOnAutomatedMode();

		}
		
		if (Config.getInstance().getBooleanProperty(REVERSE_PRINT_SQL_TO_LOG)) {
			Assert.isTrue(Utils.hasAccessToInternalStaffsOnlyFeatures(), REVERSE_PRINT_SQL_TO_LOG+" requires <br/>"+INTERNAL_STAFF_PERMISSION_FILE_PATH+" to be set");
		}

		boolean terminateAfterFailedInjection = Config.getBuildTerminateAfterFailedInjection();

		Assert.isTrue(automatedRun || !terminateAfterFailedInjection, "if "+CoreConstants.CONFIG_CLIENT_AUTOMATED_RUN+"=false, "+BUILD_TERMINATE_AFTER_FAILED_INJECTION+" must be false");

		Assert.isTrue(Config.getBuildViewLastSuccessfulOrFailedIterationCount() > 0, BUILD_VIEW_LAST_SUCCESSFUL_OR_FAILED_ITERATION_COUNT+" must be positive");
	}

	public static int getBuildSplitInjectorBufferSize() {
		return Config.getInstance().getIntegerProperty(BUILD_SPLIT_INJECTOR_BUFFER_SIZE);
	}

	public static int getBuildMaxLinesInPartialSourceFile() {
		return Config.getInstance().getIntegerProperty(BUILD_MAX_LINES_IN_PARTIAL_SOURCE_FILE);
	}
	

	public static boolean getBuildHaltFldInjectionIfLogMismatches() {
		return Config.getInstance().getBooleanProperty(BUILD_HALT_FLD_INJECTION_IF_LOG_MISMATCHES);
	}
	
	public static int getBuildViewLastSuccessfulOrFailedIterationCount() {
		return Config.getInstance().getIntegerProperty(BUILD_VIEW_LAST_SUCCESSFUL_OR_FAILED_ITERATION_COUNT);
	}

	public static Map<String, String> getEffectivePropertiesMap() {
		Config.getInstance().updateConfigProperties();
		final Map<String, String> configMap = Config.getInstance().getConcurrentMap();
		final Map<String, String> output = new HashMap<String, String>();
		for (final String key : configMap.keySet()) {
			//NOTE: TEMP_FOLDER has special treatment (see Config.getTempFolder())
			if (CoreConstants.CONFIG_CLIENT_TEMP_FOLDER.equals(key)) {
				output.put(key, new String(Config.getTempFolder().getPath()));
			} else {
				output.put(key, configMap.get(key));
			}
		}
		return output;
	}


    public static String getPatchUrl() {
        return Config.getInstance().getStringProperty(PATCH_URL);
    }

    public static String getPatchUrlUserName() {
        return Config.getInstance().getStringProperty(PATCH_URL_USER_NAME);
    }

    public static String getPatchUrlPassword() {
        return Config.getInstance().getStringProperty(PATCH_URL_PASSWORD);
    }
    public static boolean isPatchingEnabled() {
        return Config.getInstance().getBooleanProperty(PATCHING_ENABLED);
    }

    public static String getTicketAttachmentDefaultDirectoryWindows() {
    	return Config.getInstance().getStringProperty(TICKET_ATTACHMENT_DEFAULT_DIRECTORY_WINDOWS);
    }

    public static String getTicketAttachmentDefaultDirectoryLinux() {
    	return Config.getInstance().getStringProperty(TICKET_ATTACHMENT_DEFAULT_DIRECTORY_LINUX);
    }

    public static String getBugtrackerWebsiteAddress() {
    	return Config.getInstance().getStringProperty(BUGTRACKER_WEBSITE_ADDRESS);
    }

	public static String getBrowserViewSourcePropertyName()
	{
        String toReturn = null;
        if ( Platform.isWindows() )
        {
            if ( Platform.is64Bit() )
            {
                toReturn = BROWSER_VIEW_SOURCE_WINDOWS_64;
            }
            else
            {
                File temp = new File("c:/Program Files (x86)");
                if ( temp.exists() && temp.isDirectory() )
                {
                    toReturn = BROWSER_VIEW_SOURCE_WINDOWS_64;
                }
                else
                {
                    toReturn = BROWSER_VIEW_SOURCE_WINDOWS_32;
                }
            }
        }
        else if ( Platform.isLinux() )
        {
            toReturn = BROWSER_VIEW_SOURCE_LINUX;
        }
        else
        {
            throw new Error("Unrecognized platform: " + Platform.getOSType());
        }
        return toReturn;
	}

	public static String getBrowserViewSource()
	{
        return Config.getInstance().getStringProperty(getBrowserViewSourcePropertyName());
	}

    public static File getInternalStaffPermissionFilePath() {
    	if (Config.getInstance().containsProperty(INTERNAL_STAFF_PERMISSION_FILE_PATH)) {
        	final String filePath = Config.getInstance().getStringProperty(INTERNAL_STAFF_PERMISSION_FILE_PATH);
        	if (StringUtils.isBlank(filePath)) {
        		return null;
        	} else {
        		File f = new File(filePath);
        		if (f.isFile()) {
        			return f;
        		} else {
        			return null;
        		}
        	}
    	} else {
    		return null;
    	}
    }
    
    public static File getEbsVersionMappingFile() {
    	return Config.getInstance().getFileProperty(EBS_VERSION_MAPPING_FILE);
    }
    
    public static int getMaxInstallationPathLengthWindows() {
    	return Config.getInstance().getIntegerProperty(MAX_INSTALLATION_PATH_LENGTH_WINDOWS);
    }

    public static String getBuildJvmArguments() {
        return Config.getInstance().getStringProperty(BUILD_JVM_ARGUMENTS);
    }

    public static String getReverseJvmArguments() {
        return Config.getInstance().getStringProperty(REVERSE_JVM_ARGUMENTS);
    }

    public static String getPathToJavaExecutable()
    {
        String propertyName = null;
        if ( Platform.isWindows() )
        {
            if ( Platform.is64Bit() )
            {
                propertyName = JAVA_WINDOWS_64;
            }
            else
            {
                File temp = new File("c:/Program Files (x86)");
                if ( temp.exists() && temp.isDirectory() )
                {
                    propertyName = JAVA_WINDOWS_64;
                }
                else
                {
                    propertyName = JAVA_WINDOWS_32;
                }
            }
        }
        else if ( Platform.isLinux() )
        {
            propertyName = JAVA_LINUX;
        }
        else
        {
            throw new Error("Unrecognized platform: " + Platform.getOSType());
        }
        return Config.getInstance().getStringProperty(propertyName);
    }

    @Override
    protected void logConfigPropertiesFileContent(String openingLine, String closingLine) {
    	FileUtils.logConfigPropertiesFileContent(openingLine, closingLine, CoreUtil.excludePasswordPropertiesForLogging(this.getConcurrentMap()));
    }


    public static String getCreatedByUserIdsForSeededUserCalculation()
    {
        return Config.getInstance().getStringProperty(REVERSE_CREATED_BY_USER_IDS_FOR_SEEDED_USER_CALCULATION);
    }
    
    public static int getTempFolderExpiryMinutes()
    {
        return Config.getInstance().getIntegerProperty(TEMP_FOLDER_EXPIRY_MINUTES, 1, null);
    }
    
    public static boolean getBuildAssumeFldLogAlwaysAssumeWithNewLine() {
    	return Config.getInstance().getBooleanProperty(BUILD_ASSUME_FLD_LOG_ALWAYS_END_WITH_NEW_LINE);
    }

    public static boolean getBuildOpenScriptAndLogInHtmlByDefault() {
    	return Config.getInstance().getBooleanProperty(BUILD_OPEN_SCRIPT_AND_LOG_IN_HTML_BY_DEFAULT);
    }

    public static void main(String[] args)
    {
        final String FAKE_PROPERTY_JAVA_PATH = "JAVA_PATH";
        if ( args.length != 1 )
        {
            System.err.println("Usage: com.rapidesuite.client.common.util.Config <property-name>");
        }
        String propertyName = args[0];
        if ( propertyName.equals(FAKE_PROPERTY_JAVA_PATH) )
        {
            System.out.print(getPathToJavaExecutable());
        }
        else
        {
            System.out.print(Config.getInstance().getStringProperty(propertyName));
        }
    }

	public static String getInjectNodeJavaPath() {
		return Config.getInstance().getStringProperty(INJECT_NODE_JAVA_PATH);
	}
	
	public static String getUpgradeScenarioExplodedFolderPath() {
		return Config.getInstance().getStringProperty(UPGRADE_SCENARIO_EXPLODED_FOLDER);
	}
	
	public static String getSnapshotOverridePackageFolderPath() {
		return Config.getInstance().getStringProperty(SNAPSHOT_OVERRIDE_PACKAGE_FOLDER);
	}	
	
	public static String getUpgradeExtraRSCInventoriesFolderPath() {
		return Config.getInstance().getStringProperty(UPGRADE_EXTRA_RSC_INVENTORIES_FOLDER);
	}	
		
	public static File getPathTo7zip()
	{
        if ( Platform.isWindows() )
        {
            return Config.getInstance().getFileProperty(PROPERTY_PATH_TO_7ZIP_WINDOWS);
        }
        else if ( Platform.isLinux() )
        {
            return Config.getInstance().getFileProperty(PROPERTY_PATH_TO_7ZIP_LINUX);
        }
        else
        {
            throw new Error("Unrecognized platform: " + Platform.getOSType());
        }
	}

	public static String getOnlineActivationURL() {
		return Config.getInstance().getStringProperty(ONLINE_ACTIVATION_URL);
	}
	public static int getSnapshotRecordProcessingBatchSize(){
		if(!Config.getInstance().isPropertyBlank(SNAPSHOT_RECORD_PROCESSING_BATCH_SIZE)){
			return Config.getInstance().getIntegerProperty(SNAPSHOT_RECORD_PROCESSING_BATCH_SIZE);
		}else{
			return 5000;
		}
	}
	public static int getSnapshotRecordProcessingXlsxBatchSize(){
		if(!Config.getInstance().isPropertyBlank(SNAPSHOT_RECORD_PROCESSING_XLSX_BATCH_SIZE)){
			return Config.getInstance().getIntegerProperty(SNAPSHOT_RECORD_PROCESSING_XLSX_BATCH_SIZE);
		}else{
			return 50000;
		}
	}	
	public static int getSnapshotDeleteBatchSize(){
		if(!Config.getInstance().isPropertyBlank(SNAPSHOT_DELETE_BATCH_SIZE)){
			return Config.getInstance().getIntegerProperty(SNAPSHOT_DELETE_BATCH_SIZE);
		}else{
			return 10000;
		}
	}	
	public static int getSnapshotImportBatchSize(){
		if(!Config.getInstance().isPropertyBlank(SNAPSHOT_IMPORT_BATCH_SIZE)){
			return Config.getInstance().getIntegerProperty(SNAPSHOT_IMPORT_BATCH_SIZE);
		}else{
			return 10000;
		}
	}
	public static boolean isEncryptedExportedSnapshort(){
    	if (Config.getInstance().containsProperty(SNAPSHOT_IS_EXPORT_SNAPSHOT_ENCRYPTED)) {
    		return Config.getInstance().getBooleanProperty(SNAPSHOT_IS_EXPORT_SNAPSHOT_ENCRYPTED);
    	} else {
    		return true;
    	}
	}
}
