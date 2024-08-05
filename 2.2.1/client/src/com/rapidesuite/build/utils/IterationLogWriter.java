package com.rapidesuite.build.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.UUID;

import org.springframework.util.Assert;

import au.com.bytecode.opencsv.CSVWriter;

import com.rapidesuite.configurator.IterationLogger;
import com.rapidesuite.configurator.IterationLogger.ITERATION_STATUS;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.core.CoreConstants;

public class IterationLogWriter {

	public static void initiateIterationLogFileAndUuidIfNotExists(final File directory, final String ebsServerUrl, final Long historyId) throws IOException {
		Assert.notNull(directory);
		Assert.notNull(ebsServerUrl);
		Assert.isTrue(!directory.isFile(), "directory must not be a file");
		directory.mkdirs();

		final File iterationLog = new File(directory, IterationLogger.ITERATION_LOG_FILE_NAME);
		try (final CSVWriter writer = CoreUtil.createCsvWriter(new OutputStreamWriter(new FileOutputStream(iterationLog), CoreConstants.CHARACTER_SET_ENCODING))) {
			writer.writeNext(new String[]{IterationLogger.EBS_SERVER_URL_KEY, ebsServerUrl});
			writer.writeNext(new String[]{IterationLogger.HISTORY_ID_KEY, historyId==null?"":String.valueOf(historyId)});
			writer.writeNext(new String[]{IterationLogger.UUID_KEY, UUID.randomUUID().toString()});
			writer.writeNext(new String[]{}); //empty line

			writer.writeNext(new String[]{IterationLogger.START_OF_INJECTION_LOG_KEYWORD});
			String logColumns[] = new String[IterationLogger.LOG_COLUMNS_SIZE];
			logColumns[IterationLogger.LOG_INJECTOR_NUMBER_INDEX] = "Injector Number";
			logColumns[IterationLogger.LOG_INJECTOR_NAME_INDEX] = "Injector Name";
			logColumns[IterationLogger.LOG_ITERATION_NUMBER_INDEX] = "Iteration Number";
			logColumns[IterationLogger.LOG_STATUS_INDEX] = "Iteration Status";
			logColumns[IterationLogger.LOG_TIME_INDEX] = "Time";
			logColumns[IterationLogger.LOG_LOG_INDEX] = "Log Content";
			writer.writeNext(logColumns);			
		}
	}

	public static void appendInjectionLogToLogFile(final File directory, final Integer injectorNumber, final String injectorName, Integer iterationNumber, final ITERATION_STATUS iterationStatus, final Date time, final String logContent) throws IOException {
		Assert.notNull(injectorNumber);
		Assert.notNull(injectorName);
		Assert.notNull(iterationStatus);
		Assert.notNull(time);
		Assert.notNull(logContent);

		if (iterationNumber == null || iterationNumber <= 0) {
			iterationNumber = -1;
		}
		
		try (final CSVWriter writer = CoreUtil.createCsvWriter(new OutputStreamWriter(new FileOutputStream(new File(directory, IterationLogger.ITERATION_LOG_FILE_NAME), true), CoreConstants.CHARACTER_SET_ENCODING))) {
			String[] newLine = new String[IterationLogger.LOG_COLUMNS_SIZE];
			newLine[IterationLogger.LOG_INJECTOR_NUMBER_INDEX] = String.valueOf(injectorNumber);
			newLine[IterationLogger.LOG_INJECTOR_NAME_INDEX] = String.valueOf(injectorName);
			newLine[IterationLogger.LOG_ITERATION_NUMBER_INDEX] = String.valueOf(iterationNumber);
			newLine[IterationLogger.LOG_STATUS_INDEX] = String.valueOf(iterationStatus);
			newLine[IterationLogger.LOG_TIME_INDEX] = CoreConstants.DATE_FORMAT_PATTERN.FILENAME_SAFE_PRECISE.getDateFormat().format(time);
			newLine[IterationLogger.LOG_LOG_INDEX] = logContent;
			writer.writeNext(newLine);			
		}
	}
}
