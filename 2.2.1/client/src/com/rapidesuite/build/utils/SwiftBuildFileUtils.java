/**************************************************
 * $Revision: 60470 $:
 * $Author: hassan.jamil $:
 * $Date: 2016-12-28 11:34:23 +0700 (Wed, 28 Dec 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/build/utils/SwiftBuildFileUtils.java $:
 * $Id: SwiftBuildFileUtils.java 60470 2016-12-28 04:34:23Z hassan.jamil $:
 */

package com.rapidesuite.build.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.util.Assert;

import com.rapidesuite.build.BuildMain;
import com.rapidesuite.build.SwiftBuildConstants;
import com.rapidesuite.build.core.controller.Injector;
import com.rapidesuite.build.core.fileprotocol.FileProtocolManager;
import com.rapidesuite.build.core.ftp.LogsDownloader;
import com.rapidesuite.build.core.ssh.SynchronousDownloader;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.client.common.util.UtilsConstants;
import com.rapidesuite.core.CoreConstants.INJECTOR_TYPE;
import com.rapidesuite.configurator.autoinjectors.navigation.fld.FLDNavigation;
import com.rapidesuite.core.CoreConstants;
import com.sun.jna.Platform;

public class SwiftBuildFileUtils
{
	private static File consoleLogFile;

	public static File getConsoleLogFile() {
		return consoleLogFile;
	}

	public static boolean isOracleAppsR11Version(String oracleAppsURL) throws Exception
	{
		if ( oracleAppsURL != null && oracleAppsURL.indexOf("dev60cgi") != -1 )
		{
			return true;
		}
		return false;
	}

	public static boolean isOracleAppsR12Version(String oracleAppsURL) throws Exception
	{
		if ( oracleAppsURL != null && oracleAppsURL.indexOf("frmservlet") != -1 )
		{
			return true;
		}
		return false;
	}

	public static boolean hasFLDTextCloseFormCommand(String text)
	{
		if ( text == null )
		{
			return false;
		}
		String userExitChoiceOkCommand = "USER_EXIT CHOICE OK";
		int indexOf = text.indexOf(CoreConstants.MENU_MAGIC_MAGIC_QUIT);
		if ( indexOf != -1 )
		{
			String subText = text.substring(indexOf + 1);
			indexOf = subText.indexOf(userExitChoiceOkCommand);
			return indexOf != -1;
		}
		// submit requests job don't have the same pattern: they all finish by
		// 'USER_EXIT CHOICE OK' only
		indexOf = text.lastIndexOf(userExitChoiceOkCommand);
		if ( indexOf != -1 )
		{
			String subText = text.substring(indexOf + userExitChoiceOkCommand.length() + 1);
			subText = subText.replace("\r\n", "").replace("\n", "").trim();
			// System.out.println("subText:'"+subText+"'");
			return subText.isEmpty();
		}
		return false;
	}

	public static void createTempFileForViewerAndShow(File injectorsPackageFile, String scriptName, CoreConstants.INJECTOR_TYPE scriptType, File targetFile, final boolean iterationTrackingEnabled, final boolean convertToHtml, final boolean showFile) throws Exception
	{
		InputStream inputStream = null;
		ZipFile zipFile = null;
		try
		{
			if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(injectorsPackageFile) )
			{
				inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(injectorsPackageFile, scriptName);
			}
			else
			{
				zipFile = new ZipFile(injectorsPackageFile);
				inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, scriptName);
			}

			if (CoreConstants.INJECTOR_TYPE.TYPE_SQL.equals(scriptType) && Utils.hasAccessToInternalStaffsOnlyFeatures()) {
				//Assert.isTrue(Utils.hasAccessToInternalStaffsOnlyFeatures(), SwiftBuildConstants.VALID_INTERNAL_STAFF_PERMISSION_FILE_IS_MISSING_MESSAGE);
				OutputStream plaintextSqlOutputStream = null;
				try {
					targetFile.getParentFile().mkdirs();
					plaintextSqlOutputStream = new FileOutputStream(targetFile);
					IOUtils.copy(inputStream, plaintextSqlOutputStream);
					plaintextSqlOutputStream.flush();
				} finally {
					IOUtils.closeQuietly(plaintextSqlOutputStream);
					IOUtils.closeQuietly(inputStream);
				}

				InputStream sqlInputStream = null;
				byte bytes[];
				try
				{
					sqlInputStream=FileUtils.getInputStreamFromSQLFile(targetFile);
					bytes = IOUtils.toByteArray(sqlInputStream);
				}
				finally
				{
					IOUtils.closeQuietly(sqlInputStream);
				}

				org.apache.commons.io.FileUtils.writeByteArrayToFile(targetFile, bytes);
				if (showFile)	FileUtils.startTextEditor(Config.getCmdTextEditor(), targetFile);
			} else if (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(scriptType)) {
				
				if (convertToHtml) {
					generatePartialLogMainContentHtml(injectorsPackageFile.getName(), scriptType, inputStream, targetFile, false);
					if (showFile)	FileUtils.startSourceViewerBrowser(Config.getBrowserViewSource(), targetFile);
				} else {
					generatePartialLogMainContentPlain(scriptType, inputStream, targetFile);
					if (showFile)	FileUtils.startTextEditor(Config.getCmdTextEditor(), targetFile);
				}
				
				
			} else if (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(scriptType) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(scriptType)) {
				
				if (convertToHtml) {
					generatePartialLogMainContentFldScriptAndLog(injectorsPackageFile.getName(), scriptType, inputStream, targetFile, false, iterationTrackingEnabled, true);
					if (showFile)	FileUtils.startSourceViewerBrowser(Config.getBrowserViewSource(), targetFile);
				} else {
					generatePartialLogMainContentFldScriptAndLogPlain(scriptType, inputStream, targetFile);
					if (showFile)	FileUtils.startTextEditor(Config.getCmdTextEditor(), targetFile);
				}
				
				
			} else {
				createTempFileForViewer(injectorsPackageFile.getName(), scriptType, inputStream, targetFile);
				if (showFile)	FileUtils.startTextEditor(Config.getCmdTextEditor(), targetFile);
			}
		}
		finally
		{
			if ( zipFile != null )
			{
				zipFile.close();
			}
		}
	}

	public static void createTempFileForViewer(File sourceFile, CoreConstants.INJECTOR_TYPE scriptType, File targetFile) throws Exception
	{
		createTempFileForViewer(sourceFile.getName(), scriptType, new FileInputStream(sourceFile), targetFile);
	}
	
	private static enum FLD_LEXEME_TYPE {
		NEW_LINE(null),
		SPACE(null),
		NORMAL_TOKEN("normal"),
		STRING("string"),
		NUMBER("number"),
		COMMENT("comment"),
		LINE_CONTINUATION("normal");
		
		private final String defaultCssClass;
		private FLD_LEXEME_TYPE(final String defaultCssClass) {
			this.defaultCssClass = defaultCssClass;
		}
		
		public String getDefaultCssClass() {
			return this.defaultCssClass;
		}
	}
	
	private static class FldLexeme {
		private final String text;
		private final FLD_LEXEME_TYPE fldLexemeType;
		private String cssClass;
		
		public FldLexeme(final String text, final FLD_LEXEME_TYPE fldLexemeType) {
			this.text = text;
			this.fldLexemeType = fldLexemeType;
			this.cssClass = null;
		}
		
		public void setCssClass(final String cssClass) {
			this.cssClass = cssClass;
		}
		
		public String getText() {
			return this.text;
		}
		
		public boolean isPunctuationLexeme() {
			return FLD_LEXEME_TYPE.NEW_LINE.equals(fldLexemeType) || FLD_LEXEME_TYPE.SPACE.equals(fldLexemeType) || FLD_LEXEME_TYPE.LINE_CONTINUATION.equals(fldLexemeType);
		}
		
		private String getCssClassesString() {
			if (this.fldLexemeType.getDefaultCssClass() == null && this.cssClass == null) {
				return "";
			} else if (this.cssClass != null) {
				return " class='"+this.cssClass+"'";
			} else {
				return " class='"+this.fldLexemeType.getDefaultCssClass()+"'";
			}
		}
		
		public FLD_LEXEME_TYPE getFldLexemeType() {
			return this.fldLexemeType;
		}
		
		public String getHtml() {
			if (FLD_LEXEME_TYPE.NEW_LINE.equals(this.fldLexemeType)) {
				return this.text.replaceAll("\n", "<br/>");
			} else if (FLD_LEXEME_TYPE.SPACE.equals(this.fldLexemeType)) {
				return this.text.replaceAll("[ \t\r\f\u000b]", "&nbsp;");
			} else if (FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(this.fldLexemeType)) {
				return "<span"+this.getCssClassesString()+">"+this.text+"</span>";
			} else if (FLD_LEXEME_TYPE.STRING.equals(this.fldLexemeType)) {
				return "<span"+this.getCssClassesString()+">"+StringEscapeUtils.escapeHtml4(this.text)+"</span>";
			} else if (FLD_LEXEME_TYPE.NUMBER.equals(this.fldLexemeType)) {
				return "<span"+getCssClassesString()+">"+this.text+"</span>";
			} else if (FLD_LEXEME_TYPE.COMMENT.equals(this.fldLexemeType)) {
				String textToPrint = this.text;
				boolean hasTableAndFieldName = false;
				final String toSectionId = "# TO_SECTION_ID:";
				if(this.text.startsWith(toSectionId)) {
					String textWithoutToSectionId = this.text.substring(toSectionId.length());
					if (textWithoutToSectionId.contains("'")) {
						String textStartingFromTheFirstApostrophe = textWithoutToSectionId.substring(textWithoutToSectionId.indexOf("'"));
						if (1 <= StringUtils.countMatches(textStartingFromTheFirstApostrophe, ":")) {
							String chunks[] = textStartingFromTheFirstApostrophe.split(":");
							if (chunks.length >= 2) {
								String output = toSectionId;
								output += "<span class='table_name'>" + StringEscapeUtils.escapeHtml4(chunks[0]) + "</span>";
								output += ":";
								output += "<span class='field_name'>" + StringEscapeUtils.escapeHtml4(chunks[1]) + "</span>";
								for (int i = 2 ; i < chunks.length ; i++) {
									output += ":";
									output += StringEscapeUtils.escapeHtml4(chunks[i]);								
								}
								textToPrint = output;
								hasTableAndFieldName = true;
							}
						}						
					}

				}
				
				if (!hasTableAndFieldName) {
					textToPrint = StringEscapeUtils.escapeHtml4(textToPrint);
				}
				
				return "<span"+getCssClassesString()+">"+textToPrint+"</span>";
			} else if (FLD_LEXEME_TYPE.LINE_CONTINUATION.equals(this.fldLexemeType)) {
				return "<span"+this.getCssClassesString()+">"+this.text+"</span>";
			} else {
				throw new Error(this.fldLexemeType+" is of unknown type");
			}
		}
	}
	
	private static boolean matchesRegex(final String originalString, final String regex) {
		 Pattern p = Pattern.compile(regex, Pattern.DOTALL);
		 Matcher m = p.matcher(originalString);
		 return m.lookingAt();
	}	

	private static String getMatch(final String originalString, final String regex) {
		 Pattern p = Pattern.compile(regex, Pattern.DOTALL);
		 Matcher m = p.matcher(originalString);
		 if (m.lookingAt()) {
			 return originalString.substring(m.start(), m.end());
		 } else {
			 return "";
		 }
	}	
	
	private static void flushFldLexemesParsingBuffer(StringBuilder buffer, final List<FldLexeme> lexemes, final FLD_LEXEME_TYPE currentState) {
		if (buffer.length() == 0) {
			return;
		}
		
		lexemes.add(new FldLexeme(buffer.toString(), currentState));
		buffer.setLength(0);
	}		
	
	public static List<FldLexeme> doGeneratePartialLogMainContentFldScriptAndLog(final String line, final List<FldLexeme> existingLexemes, final boolean iterationMarkingIsEnabled, final BufferedWriter bufferedFileWriter) throws Exception {		
		final List<FldLexeme> lexemes = new ArrayList<FldLexeme>();
		lexemes.addAll(existingLexemes);
		
		if (line != null) {
			if (StringUtils.isBlank(line)) {
				lexemes.add(new FldLexeme(line, FLD_LEXEME_TYPE.SPACE));
			} else if (line.startsWith("#")) {
				lexemes.add(new FldLexeme(line, FLD_LEXEME_TYPE.COMMENT));
			} else {
				String lineProcessed  = line;
				final StringBuilder buffer = new StringBuilder();
				FLD_LEXEME_TYPE currentState = FLD_LEXEME_TYPE.SPACE;
				
				while(lineProcessed.length() > 0) {
					
					final String spaceOrEolRegex = "(\\s+|$)";

					final String numberRegex = "^-?\\d+(\\.\\d+)?";
					final String spaceRegex = "^[ \t\r\f\u000b]+";
					final String lineContinuationRegex = "^\\\\";
					final String normalTokenRegex = "^[^\"\\s]+";
					
					final String match;
					if (FLD_LEXEME_TYPE.SPACE.equals(currentState) && matchesRegex(lineProcessed, numberRegex+spaceOrEolRegex)) {
						match = getMatch(lineProcessed, numberRegex);
						flushFldLexemesParsingBuffer(buffer, lexemes, currentState);
						currentState = FLD_LEXEME_TYPE.NUMBER;
						buffer.append(match);					
					} else if (!FLD_LEXEME_TYPE.STRING.equals(currentState) && matchesRegex(lineProcessed, spaceRegex)) {
						match = getMatch(lineProcessed, spaceRegex);
						flushFldLexemesParsingBuffer(buffer, lexemes, currentState);
						currentState = FLD_LEXEME_TYPE.SPACE;
						buffer.append(match);	
					} else if (FLD_LEXEME_TYPE.SPACE.equals(currentState) && matchesRegex(lineProcessed, lineContinuationRegex+spaceOrEolRegex)) {
						match = getMatch(lineProcessed, lineContinuationRegex);
						flushFldLexemesParsingBuffer(buffer, lexemes, currentState);
						currentState = FLD_LEXEME_TYPE.LINE_CONTINUATION;
						buffer.append(match);						
					} else if (FLD_LEXEME_TYPE.SPACE.equals(currentState) && matchesRegex(lineProcessed, normalTokenRegex)) {
						match = getMatch(lineProcessed, normalTokenRegex);
						flushFldLexemesParsingBuffer(buffer, lexemes, currentState);
						currentState =  FLD_LEXEME_TYPE.NORMAL_TOKEN;
						buffer.append(match);		
					} else if (FLD_LEXEME_TYPE.SPACE.equals(currentState) && lineProcessed.startsWith("\"")) {
						match = "\"";
						flushFldLexemesParsingBuffer(buffer, lexemes, currentState);
						currentState = FLD_LEXEME_TYPE.STRING;
						buffer.append(match);						
					} else if (FLD_LEXEME_TYPE.STRING.equals(currentState) && lineProcessed.indexOf('"') >= 0 
							&& (lineProcessed.indexOf('"') == 0 || lineProcessed.charAt(lineProcessed.indexOf('"') - 1)  != '\\')) {
						match = lineProcessed.substring(0, lineProcessed.toString().indexOf('"')+1);
						buffer.append(match);
						flushFldLexemesParsingBuffer(buffer, lexemes, currentState);
						currentState = FLD_LEXEME_TYPE.SPACE;						
					} else if (FLD_LEXEME_TYPE.STRING.equals(currentState) && lineProcessed.indexOf("\\\"") >= 0) {
						match = lineProcessed.substring(0, lineProcessed.indexOf("\\\"")+2);
						buffer.append(match);						
					} else {
						throw new Exception("Unknown lexeme. Current state = "+currentState+". The remaining line is '"+lineProcessed+"'");
					}
					
					lineProcessed = lineProcessed.substring(match.length());
				}
				flushFldLexemesParsingBuffer(buffer, lexemes, currentState);
			}
			
			lexemes.add(new FldLexeme("\n", FLD_LEXEME_TYPE.NEW_LINE));			
		}

	
		
		List<FldLexeme> lexemeLine = new ArrayList<FldLexeme>();
		boolean breakLogicalLineAfterEncounteringLineBreak = true;
		
		List<List<FldLexeme>> tempLexemeLines = new ArrayList<List<FldLexeme>>();
		boolean isIterationBoundaryLine = false;
		boolean isInsideIterationBoundaryBlock = false;
		boolean isInsideLogIterationBoundaryBlock = false;
		boolean hasSeenMenuCustomDiagnosticsExamine = false;
		boolean hasSeenStarsIterationBoundary = false;
		boolean hasSeenUserExitResponseDoneCancel = false;
		
		final List<FldLexeme> lexemesToDelete = new ArrayList<FldLexeme>();
		
		final String ITERATION_BOUNDARY_HIGHLIGHT_CSS_CLASS = "iteration_boundary_highlight";
		final String ITERATION_BOUNDARY_CSS_CLASS = "iteration_boundary";
		
		for (int i = 0 ; i < lexemes.size() ; i++) {
			final FldLexeme lexeme = lexemes.get(i);
			lexemeLine.add(lexeme);
			
			if (FLD_LEXEME_TYPE.COMMENT.equals(lexeme.getFldLexemeType()) && lexeme.getText().startsWith(FLDNavigation.RAPID_BUILD_ITERATION_TRACKING_BEGIN)) {
				isInsideIterationBoundaryBlock = true;
			}
			
			if (isInsideIterationBoundaryBlock) {
				if (iterationMarkingIsEnabled && FLD_LEXEME_TYPE.STRING.equals(lexeme.getFldLexemeType()) && (lexeme.getText().contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX) || lexeme.getText().contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX))) {
					lexeme.setCssClass(ITERATION_BOUNDARY_HIGHLIGHT_CSS_CLASS);
				} else if (!iterationMarkingIsEnabled && FLD_LEXEME_TYPE.COMMENT.equals(lexeme.getFldLexemeType()) && lexeme.getText().startsWith(CoreConstants.ITERATION_SEPARATOR)) {
					lexeme.setCssClass(ITERATION_BOUNDARY_HIGHLIGHT_CSS_CLASS);
				} else {			
					lexeme.setCssClass(ITERATION_BOUNDARY_CSS_CLASS);
				}
			}
			
			if (iterationMarkingIsEnabled && FLD_LEXEME_TYPE.STRING.equals(lexeme.getFldLexemeType())) {
				if (lexeme.getText().contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX) || lexeme.getText().contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX)) {
					isIterationBoundaryLine = true;
				}
			} else if (!iterationMarkingIsEnabled && FLD_LEXEME_TYPE.COMMENT.equals(lexeme.getFldLexemeType())) {
				if (lexeme.getText().startsWith(CoreConstants.ITERATION_SEPARATOR)) {
					isIterationBoundaryLine = true;
				}				
			}
			
			if (FLD_LEXEME_TYPE.LINE_CONTINUATION.equals(lexeme.getFldLexemeType())) {
				breakLogicalLineAfterEncounteringLineBreak = false;
			}
			
			boolean isRapidbuildIterationTrackingEndLine = false;
			boolean thisLineIsAlreadyClosed = false;
			for (final FldLexeme l : lexemeLine) {
				if (FLD_LEXEME_TYPE.COMMENT.equals(l.getFldLexemeType()) && l.getText().startsWith(FLDNavigation.RAPID_BUILD_ITERATION_TRACKING_END)) {
					isRapidbuildIterationTrackingEndLine = true;
				}
				if (FLD_LEXEME_TYPE.NEW_LINE.equals(l.getFldLexemeType())) {
					thisLineIsAlreadyClosed = true;
				}
			}
			if (!thisLineIsAlreadyClosed) {
				isRapidbuildIterationTrackingEndLine = false;
			}
			
			if (breakLogicalLineAfterEncounteringLineBreak && FLD_LEXEME_TYPE.NEW_LINE.equals(lexeme.getFldLexemeType())) {
				boolean isCommentLine = false;
				for (final FldLexeme l : lexemeLine) {
					if (FLD_LEXEME_TYPE.COMMENT.equals(l.getFldLexemeType())) {
						isCommentLine = true;
						break;
					}
				}
				
				boolean isIterationSeparatorLine = false;
				if (isCommentLine) {
					for (final FldLexeme l : lexemeLine) {
						if (FLD_LEXEME_TYPE.COMMENT.equals(l.getFldLexemeType()) && l.getText().startsWith(CoreConstants.ITERATION_SEPARATOR)) {
							isIterationSeparatorLine = true;
							break;
						}
					}					
				}
				
				
				boolean isMenuCustomDiagnosticsExamineLine = true;
				int normalTokenCountForMenuCustomDiagnosticsExamine = 0;
				for (final FldLexeme l : lexemeLine) {
					if (l.isPunctuationLexeme()) {
						continue;
					} else if (FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(l.getFldLexemeType())) {
						normalTokenCountForMenuCustomDiagnosticsExamine++;
						if (normalTokenCountForMenuCustomDiagnosticsExamine == 1) {
							if (!l.getText().equals("MENU")) {
								isMenuCustomDiagnosticsExamineLine = false;
								break;
							}
						} else if (normalTokenCountForMenuCustomDiagnosticsExamine == 2) {
							if (!l.getText().equals("CUSTOM")) {
								isMenuCustomDiagnosticsExamineLine = false;
								break;
							}					
						} else if (normalTokenCountForMenuCustomDiagnosticsExamine == 3) {
							if (!l.getText().equals("DIAGNOSTICS")) {
								isMenuCustomDiagnosticsExamineLine = false;
								break;
							}					
						} else if (normalTokenCountForMenuCustomDiagnosticsExamine == 4) {
							if (!l.getText().equals("EXAMINE")) {
								isMenuCustomDiagnosticsExamineLine = false;
								break;
							}					
						} else {
							isMenuCustomDiagnosticsExamineLine = false;
							break;
						}
					} else {
						isMenuCustomDiagnosticsExamineLine = false;
						break;
					}
				}
				if (isMenuCustomDiagnosticsExamineLine) {
					isMenuCustomDiagnosticsExamineLine = normalTokenCountForMenuCustomDiagnosticsExamine == 4;
				}
				
				boolean isStarsIterationBoundaryLine = false;
				for (final FldLexeme l : lexemeLine) {
					if (FLD_LEXEME_TYPE.STRING.equals(l.getFldLexemeType())) {
						if (l.getText().contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX) || l.getText().contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX)) {
							isStarsIterationBoundaryLine = true;
							break;
						}
					}
				}	
				
				
				boolean isUserExitResponseDoneCancelLine = true;
				int normalTokenCountForUserExitResponseDoneCancel = 0;
				for (final FldLexeme l : lexemeLine) {
					if (l.isPunctuationLexeme()) {
						continue;
					} else if (FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(l.getFldLexemeType())) {
						normalTokenCountForUserExitResponseDoneCancel++;
						if (normalTokenCountForUserExitResponseDoneCancel == 1) {
							if (!l.getText().equals("USER_EXIT")) {
								isUserExitResponseDoneCancelLine = false;
								break;
							}
						} else if (normalTokenCountForUserExitResponseDoneCancel == 2) {
							if (!l.getText().equals("RESPONSE")) {
								isUserExitResponseDoneCancelLine = false;
								break;
							}					
						} else if (normalTokenCountForUserExitResponseDoneCancel == 3) {
							if (!l.getText().equals("DONE")) {
								isUserExitResponseDoneCancelLine = false;
								break;
							}					
						} else if (normalTokenCountForUserExitResponseDoneCancel == 4) {
							if (!l.getText().equals("CANCEL")) {
								isUserExitResponseDoneCancelLine = false;
								break;
							}					
						} else {
							isUserExitResponseDoneCancelLine = false;
							break;
						}
					} else {
						isUserExitResponseDoneCancelLine = false;
						break;
					}
				}
				isUserExitResponseDoneCancelLine = normalTokenCountForUserExitResponseDoneCancel == 4;	
				
				boolean isBlankLine = true;
				for (final FldLexeme l : lexemeLine) {
					if (!l.isPunctuationLexeme()) {
						isBlankLine = false;
						break;
					}
				}
				
				final boolean isNotBlankAndNotCommentAndIsNotStarsIterationBoundaryLine = !isBlankLine && !isCommentLine && !isStarsIterationBoundaryLine;
				
				
				if (!isIterationBoundaryLine && !isInsideIterationBoundaryBlock && !isInsideLogIterationBoundaryBlock && !hasSeenMenuCustomDiagnosticsExamine && !hasSeenStarsIterationBoundary && !hasSeenUserExitResponseDoneCancel && isCommentLine) {
					isInsideLogIterationBoundaryBlock = true;
					tempLexemeLines.add(lexemeLine);	
					lexemeLine = new ArrayList<FldLexeme>();
				} else if (isInsideLogIterationBoundaryBlock && !hasSeenMenuCustomDiagnosticsExamine && !hasSeenStarsIterationBoundary && !hasSeenUserExitResponseDoneCancel && isMenuCustomDiagnosticsExamineLine) {
					hasSeenMenuCustomDiagnosticsExamine = true;
					tempLexemeLines.add(lexemeLine);
					lexemeLine = new ArrayList<FldLexeme>();
				} else if (isInsideLogIterationBoundaryBlock && hasSeenMenuCustomDiagnosticsExamine && !hasSeenStarsIterationBoundary && !hasSeenUserExitResponseDoneCancel && isStarsIterationBoundaryLine) {
					hasSeenStarsIterationBoundary = true;
					tempLexemeLines.add(lexemeLine);
					lexemeLine = new ArrayList<FldLexeme>();
				} else if (isInsideLogIterationBoundaryBlock && hasSeenMenuCustomDiagnosticsExamine && hasSeenStarsIterationBoundary && !hasSeenUserExitResponseDoneCancel && isUserExitResponseDoneCancelLine) {
					hasSeenUserExitResponseDoneCancel = true;
					tempLexemeLines.add(lexemeLine);	
					lexemeLine = new ArrayList<FldLexeme>();
				} else if (isInsideLogIterationBoundaryBlock && isBlankLine && isInsideLogIterationBoundaryBlock && !hasSeenUserExitResponseDoneCancel) {
					tempLexemeLines.add(lexemeLine);	
					lexemeLine = new ArrayList<FldLexeme>();
				} else if (isInsideIterationBoundaryBlock && !isRapidbuildIterationTrackingEndLine) {
					tempLexemeLines.add(lexemeLine);	
					lexemeLine = new ArrayList<FldLexeme>();
				} else if (isInsideLogIterationBoundaryBlock && hasSeenMenuCustomDiagnosticsExamine && !hasSeenUserExitResponseDoneCancel && !isNotBlankAndNotCommentAndIsNotStarsIterationBoundaryLine) {
					tempLexemeLines.add(lexemeLine);	
					lexemeLine = new ArrayList<FldLexeme>();
				} else {
					
					for (final List<FldLexeme> lexemeLineToPrint : tempLexemeLines) {
						if (isInsideLogIterationBoundaryBlock && hasSeenMenuCustomDiagnosticsExamine && hasSeenStarsIterationBoundary && hasSeenUserExitResponseDoneCancel) {
							for (final FldLexeme lexemeToPrint : lexemeLineToPrint) {
								if (FLD_LEXEME_TYPE.STRING.equals(lexemeToPrint.getFldLexemeType()) && (lexemeToPrint.getText().contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_PREFIX) || lexemeToPrint.getText().contains(CoreConstants.RAPIDBUILD_ITERATION_NUMBER_CLOSURE_PREFIX))) {
									lexemeToPrint.setCssClass(ITERATION_BOUNDARY_HIGHLIGHT_CSS_CLASS);
								} else {
									lexemeToPrint.setCssClass(ITERATION_BOUNDARY_CSS_CLASS);
								}
							}
						}
						lexemesToDelete.addAll(lexemeLineToPrint);
						final String html = fldLexemeLineToHtmlString(lexemeLineToPrint);
						bufferedFileWriter.write(html);
					}
					
					isInsideLogIterationBoundaryBlock = false;
					hasSeenMenuCustomDiagnosticsExamine = false;
					hasSeenStarsIterationBoundary = false;
					hasSeenUserExitResponseDoneCancel = false;
					tempLexemeLines.clear();
					
					lexemesToDelete.addAll(lexemeLine);	
					
					if (!iterationMarkingIsEnabled && isIterationSeparatorLine) {
						for (final FldLexeme l : lexemeLine) {
							if (FLD_LEXEME_TYPE.COMMENT.equals(l.getFldLexemeType()) && l.getText().startsWith(CoreConstants.ITERATION_SEPARATOR)) {
								l.setCssClass(ITERATION_BOUNDARY_HIGHLIGHT_CSS_CLASS);
							}
						}
					}
					
					
					final String html = fldLexemeLineToHtmlString(lexemeLine);
					bufferedFileWriter.write(html);
					
				}
				breakLogicalLineAfterEncounteringLineBreak = true;
				isIterationBoundaryLine = false;					
			} else if (FLD_LEXEME_TYPE.NEW_LINE.equals(lexeme.getFldLexemeType())) {
				breakLogicalLineAfterEncounteringLineBreak = true;
				isIterationBoundaryLine = false;
			}			
			
			if (isRapidbuildIterationTrackingEndLine) {
				isInsideIterationBoundaryBlock = false; 
			}		
		}	
		
		lexemes.removeAll(lexemesToDelete);
		return lexemes;		
	}
	
	private static String fldLexemeLineToHtmlString(final List<FldLexeme> lexemeLine) {
		List<FldLexeme> nonPunctuationLexemes = new ArrayList<FldLexeme>();
		StringBuilder html = new StringBuilder();
		
		for (int i = 0 ; i < lexemeLine.size() ; i++) {
			FldLexeme lexemeToPrint = lexemeLine.get(i);
			
			if (!lexemeToPrint.isPunctuationLexeme()) {
				nonPunctuationLexemes.add(lexemeToPrint);
			}
			
			if (!lexemeToPrint.isPunctuationLexeme() && !FLD_LEXEME_TYPE.COMMENT.equals(lexemeToPrint.getFldLexemeType())) {
				if (nonPunctuationLexemes.size() == 6) {
					if (FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(0).getFldLexemeType()) && "CLICK".equals(nonPunctuationLexemes.get(0).getText()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(1).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(2).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(3).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NUMBER.equals(nonPunctuationLexemes.get(4).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(5).getFldLexemeType())) {
						FldLexeme replacement = new FldLexeme(nonPunctuationLexemes.get(5).getText(), FLD_LEXEME_TYPE.STRING);
						nonPunctuationLexemes.set(5, replacement);
						lexemeToPrint = replacement;
					}
				}
				
				if (nonPunctuationLexemes.size() == 7) {
					if (FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(0).getFldLexemeType()) && "USER_EXIT".equals(nonPunctuationLexemes.get(0).getText()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(1).getFldLexemeType()) && "RESPONSE".equals(nonPunctuationLexemes.get(1).getText()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(2).getFldLexemeType()) && ("VALIDATE".equals(nonPunctuationLexemes.get(2).getText()) || "OK".equals(nonPunctuationLexemes.get(2).getText())) &&
							FLD_LEXEME_TYPE.NUMBER.equals(nonPunctuationLexemes.get(3).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(4).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(5).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(6).getFldLexemeType())) {
						FldLexeme replacement = new FldLexeme(nonPunctuationLexemes.get(6).getText(), FLD_LEXEME_TYPE.STRING);
						nonPunctuationLexemes.set(6, replacement);
						lexemeToPrint = replacement;
					}
				}
				
				if (nonPunctuationLexemes.size() == 6) {
					if (FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(0).getFldLexemeType()) && "USER_EXIT".equals(nonPunctuationLexemes.get(0).getText()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(1).getFldLexemeType()) && "FLEX".equals(nonPunctuationLexemes.get(1).getText()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(2).getFldLexemeType()) && "UPDATE".equals(nonPunctuationLexemes.get(2).getText()) &&
							FLD_LEXEME_TYPE.NUMBER.equals(nonPunctuationLexemes.get(3).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NUMBER.equals(nonPunctuationLexemes.get(4).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(5).getFldLexemeType())) {
						FldLexeme replacement = new FldLexeme(nonPunctuationLexemes.get(5).getText(), FLD_LEXEME_TYPE.STRING);
						nonPunctuationLexemes.set(5, replacement);
						lexemeToPrint = replacement;
					}
				}
				
				if (nonPunctuationLexemes.size() == 3) {
					if (FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(0).getFldLexemeType()) && "USER_EXIT".equals(nonPunctuationLexemes.get(0).getText()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(1).getFldLexemeType()) && "LOV".equals(nonPunctuationLexemes.get(1).getText()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(2).getFldLexemeType())) {
						FldLexeme replacement = new FldLexeme(nonPunctuationLexemes.get(2).getText(), FLD_LEXEME_TYPE.STRING);
						nonPunctuationLexemes.set(2, replacement);
						lexemeToPrint = replacement;
					}
				}	
				
				if (nonPunctuationLexemes.size() == 4) {
					if (FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(0).getFldLexemeType()) && "USER_EXIT".equals(nonPunctuationLexemes.get(0).getText()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(1).getFldLexemeType()) && "LOV".equals(nonPunctuationLexemes.get(1).getText()) &&
							FLD_LEXEME_TYPE.STRING.equals(nonPunctuationLexemes.get(2).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(3).getFldLexemeType())) {
						FldLexeme replacement = new FldLexeme(nonPunctuationLexemes.get(3).getText(), FLD_LEXEME_TYPE.STRING);
						nonPunctuationLexemes.set(3, replacement);
						lexemeToPrint = replacement;
					}
				}
				
				if (nonPunctuationLexemes.size() == 6) {
					if (FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(0).getFldLexemeType()) && "VALUE".equals(nonPunctuationLexemes.get(0).getText()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(1).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(2).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(3).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NUMBER.equals(nonPunctuationLexemes.get(4).getFldLexemeType()) &&
							FLD_LEXEME_TYPE.NORMAL_TOKEN.equals(nonPunctuationLexemes.get(5).getFldLexemeType())) {
						FldLexeme replacement = new FldLexeme(nonPunctuationLexemes.get(5).getText(), FLD_LEXEME_TYPE.STRING);
						nonPunctuationLexemes.set(5, replacement);
						lexemeToPrint = replacement;
					}
				}			
				
				html.append(lexemeToPrint.getHtml());
			} else {
				html.append(lexemeToPrint.getHtml());
			}
			
			
		}
		lexemeLine.clear();
		nonPunctuationLexemes.clear();
		
		return html.toString();
	}	
	
	private static void writeHtmlWrapperTop(final Writer writer, final String title) throws IOException {
		final File cssFile = new File(new File("misc"), "source_file_viewer.css");
		writer.write("<!DOCTYPE html>");
		writer.write("<html>");
		writer.write("<head>");
		writer.write("<meta http-equiv='Content-Type' content='text/html; charset="+CoreConstants.CHARACTER_SET_ENCODING+"'>");
		writer.write("<link rel='stylesheet' type='text/css' href='"+cssFile.getAbsoluteFile().toURI().toString()+"'>");
		writer.write("<title>"+title+"</title>");
		writer.write("</head>");
		writer.write("<body>");			
	}
	
	private static void writeHtmlWrapperBottom(final Writer writer) throws IOException {
		writer.write("</body>");	
		writer.write("</html>");	
	}	
	
	public static void generatePartialLogMainContentFldScriptAndLogPlain(CoreConstants.INJECTOR_TYPE scriptType, InputStream in, final File targetFile) throws Exception {

		OutputStream fos = null;
		Writer osw = null;
		BufferedWriter bufferedFileWriter = null;
		
		Reader isr = null;
		BufferedReader br = null;
		try {
			fos = new FileOutputStream(targetFile);
			osw = new OutputStreamWriter(fos,CoreConstants.CHARACTER_SET_ENCODING);
			bufferedFileWriter = new BufferedWriter(osw, 1024*1024); //1 MB buffer
			
			isr = new InputStreamReader(in, CoreConstants.CHARACTER_SET_ENCODING);
			br = new BufferedReader(new InputStreamReader(in, CoreConstants.CHARACTER_SET_ENCODING));
			
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String strLineProcessed = removePasswordFromInputString(strLine, scriptType).replaceAll("\r?\n", "");
				bufferedFileWriter.append(strLineProcessed.trim()+"\n");
			}
			
		} finally {
			IOUtils.closeQuietly(bufferedFileWriter);
			IOUtils.closeQuietly(osw);
			IOUtils.closeQuietly(fos);
			
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(isr);
		}
			
	}
	
	public static void generatePartialLogMainContentFldScriptAndLog(String sourceFileName, CoreConstants.INJECTOR_TYPE scriptType, InputStream in, final File targetFile, boolean ignoreMaxLineCountLimit, final boolean iterationMarkingIsEnabled, final boolean includeHtmlBoilerPlate) throws Exception {
		OutputStream fos = null;
		Writer osw = null;
		BufferedWriter bufferedFileWriter = null;
		
		Reader isr = null;
		BufferedReader br = null;
		try {
			fos = new FileOutputStream(targetFile);
			osw = new OutputStreamWriter(fos,CoreConstants.CHARACTER_SET_ENCODING);
			bufferedFileWriter = new BufferedWriter(osw, 1024*1024); //1 MB buffer
			
			isr = new InputStreamReader(in, CoreConstants.CHARACTER_SET_ENCODING);
			br = new BufferedReader(new InputStreamReader(in, CoreConstants.CHARACTER_SET_ENCODING));
			
			
			final List<String> rawTexts = new ArrayList<String>();
			int numberOfLinesRead = 0;
			List<FldLexeme> fldLexemes = new ArrayList<FldLexeme>();
			if (includeHtmlBoilerPlate) {
				writeHtmlWrapperTop(bufferedFileWriter, sourceFileName);		
			}
			
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (!ignoreMaxLineCountLimit && numberOfLinesRead >= Config.getBuildMaxLinesInPartialSourceFile()) {
					String importantNote = 	"<font color='red'>*******************************<b>END OF HTML LOG</b>*******************************</font><br/>" + 
											"<font color='red' size='5'><b><u>IMPORTANT NOTE:</u> The FLD log file has been truncated at " + Config.getBuildMaxLinesInPartialSourceFile() + " lines (number controlled by the property [BUILD_MAX_LINES_IN_PARTIAL_SOURCE_FILE] in the engine.properties file).<br/>" +
											"If you wish to see more lines then please switch to the TEXT option (checkbox in the execution panel) or change the property (beware of the impact on your system memory, i.e. the browser may not load and hang or crash).</b></font>";
					bufferedFileWriter.append(importantNote);
					break;
				}
				numberOfLinesRead++;
				String strLineProcessed = removePasswordFromInputString(strLine, scriptType).replaceAll("\r?\n", "");
				rawTexts.add(strLineProcessed);
				
				fldLexemes = doGeneratePartialLogMainContentFldScriptAndLog(strLineProcessed, fldLexemes, iterationMarkingIsEnabled, bufferedFileWriter);
			}
			
			fldLexemes = doGeneratePartialLogMainContentFldScriptAndLog(null, fldLexemes, iterationMarkingIsEnabled, bufferedFileWriter);
			
			if (includeHtmlBoilerPlate) {
				writeHtmlWrapperBottom(bufferedFileWriter);			
			}
			
		} finally {
			IOUtils.closeQuietly(bufferedFileWriter);
			IOUtils.closeQuietly(osw);
			IOUtils.closeQuietly(fos);
			
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(isr);
		}
	}
	
	public static void wrapInAnHtml(String title, final File targetFile, final List<File> originalFiles) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		BufferedWriter bufferedFileWriter = null;
		FileOutputStream targetFos = null;
		OutputStreamWriter osw = null;
		try  {
			targetFos = new FileOutputStream(targetFile);
			osw = new OutputStreamWriter(targetFos, CoreConstants.CHARACTER_SET_ENCODING);
			bufferedFileWriter = new BufferedWriter(osw, 1024*1024);

			writeHtmlWrapperTop(bufferedFileWriter, title);
			
			for (int i = 0 ; i < originalFiles.size() ; i++) {
				if (i > 0) {
					bufferedFileWriter.write("<br/><br/><br/>");
				}
				BufferedReader br = null;
				FileInputStream origFos = null;
				InputStreamReader isr = null;
				try {
					origFos = new FileInputStream(originalFiles.get(i));
					isr = new InputStreamReader(origFos, CoreConstants.CHARACTER_SET_ENCODING);
					br = new BufferedReader(isr);
					IOUtils.copy(br, bufferedFileWriter);
					
					br.close();
					isr.close();
					origFos.close();
				}
				finally {
					IOUtils.closeQuietly(br);
					IOUtils.closeQuietly(isr);
					IOUtils.closeQuietly(origFos);
				}
			}
		
			writeHtmlWrapperBottom(bufferedFileWriter);
			
			bufferedFileWriter.close();
			osw.close();
			targetFos.close();
		}
		finally {
			IOUtils.closeQuietly(bufferedFileWriter);
			IOUtils.closeQuietly(osw);
			IOUtils.closeQuietly(targetFos);
		}
	}
	
	public static void generatePartialLogMainContentPlain(CoreConstants.INJECTOR_TYPE scriptType, InputStream in, final File targetFile) throws Exception {
		Reader isr = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		
		try {
			isr = new InputStreamReader(in, CoreConstants.CHARACTER_SET_ENCODING);
			br = new BufferedReader(isr);
			pw = new PrintWriter(targetFile, CoreConstants.CHARACTER_SET_ENCODING);
		    String strLine;
			while ( (strLine = br.readLine()) != null )
			{
				final String lineWithoutPassword = removePasswordFromInputString(strLine, scriptType);
				pw.println(lineWithoutPassword.trim());
			}
			
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(pw);
		}
			
	}

	public static void generatePartialLogMainContentHtml(String sourceFileName, CoreConstants.INJECTOR_TYPE scriptType, InputStream in, final File targetFile, boolean ignoreMaxLineCountLimit) throws Exception {
		final String PARAM_VALUE_CLASS = "param_value";
		final String PARAM_NAME_CLASS = "param_name";
		final String COMMENT_CLASS = "comment";
		final String COMMAND_CLASS = "command";
		
		Reader isr = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		
		try {
			isr = new InputStreamReader(in, CoreConstants.CHARACTER_SET_ENCODING);
			br = new BufferedReader(isr);
			pw = new PrintWriter(targetFile, CoreConstants.CHARACTER_SET_ENCODING);
			
	        pw.println("<html>");
	        pw.println("<head>");
	        pw.println("<title>"+sourceFileName+"</title>");
	        pw.println("<meta http-equiv='Content-Type' content='text/html; charset="+CoreConstants.CHARACTER_SET_ENCODING+"'>");
            pw.println("<link rel='stylesheet' type='text/css' href='"+SwiftBuildConstants.SOURCE_FILE_VIEWER_CSS.getAbsolutePath()+"' />");
	        pw.println("</head>");
	        pw.println("<body>");

	        //ignore header and footer for line count
		    int lineCount = 0;
		    String strLine;
			while ( (strLine = br.readLine()) != null )
			{
                if (++lineCount >= Config.getBuildMaxLinesInPartialSourceFile() && !ignoreMaxLineCountLimit)
                {
                    break;
                }

				final String lineWithoutPassword = removePasswordFromInputString(strLine, scriptType);
				String lineDecorated = lineWithoutPassword.trim();

				boolean isComment = false;

				if (lineDecorated.startsWith("#")) {
					lineDecorated = "<span class='"+COMMENT_CLASS+"'>"+lineDecorated+"</span>";
					isComment = true;
				}

				if (!isComment && lineDecorated.contains(" ")) {
					int firstSpaceIndex = lineDecorated.indexOf(' ');
					int endIndex = lineDecorated.indexOf(CoreConstants.RSC_SEPARATOR);
					if (endIndex == -1) {
						endIndex = lineDecorated.length();
					}
					if (endIndex > firstSpaceIndex+1) {
						final String firstParamNameAndValueWithoutStyle = lineDecorated.substring(firstSpaceIndex+1, endIndex);
						if (firstParamNameAndValueWithoutStyle.contains("=")) {
							int equalSignIndex = firstParamNameAndValueWithoutStyle.indexOf('=');
							if (equalSignIndex > 0 && equalSignIndex < firstParamNameAndValueWithoutStyle.length()-1) {
								final String paramNameWithoutStyle = firstParamNameAndValueWithoutStyle.substring(0, equalSignIndex);
								final String paramNameWithStyle = "<span class='"+PARAM_NAME_CLASS+"'>"+paramNameWithoutStyle+"</span>";

								final String paramValueWithoutStyle = firstParamNameAndValueWithoutStyle.substring(equalSignIndex+1);
								final String paramValueWithStyle = "<span class='"+PARAM_VALUE_CLASS+"'>"+paramValueWithoutStyle+"</span>";

								lineDecorated = lineDecorated.replace(' '+paramNameWithoutStyle+'='+paramValueWithoutStyle, ' '+paramNameWithStyle+'='+paramValueWithStyle);
							}
						}
					}
				}

				while (!isComment && lineDecorated.contains(CoreConstants.RSC_SEPARATOR)) {
					final int firstParamIndex = lineDecorated.indexOf(CoreConstants.RSC_SEPARATOR);

					StringBuffer thisParamWithSeparatorWithoutStyle = new StringBuffer();
					for (int i = firstParamIndex ; ; i++) {
						if (i >= lineDecorated.length() || (i > firstParamIndex && lineDecorated.substring(i).startsWith(CoreConstants.RSC_SEPARATOR))) {
							break;
						}
						thisParamWithSeparatorWithoutStyle.append(lineDecorated.charAt(i));
					}

					final String thisParamWithoutSeparatorWithoutStyle = thisParamWithSeparatorWithoutStyle.toString().replace(CoreConstants.RSC_SEPARATOR, " ");

					final int equalSignIndex = thisParamWithoutSeparatorWithoutStyle.indexOf('=');
					Assert.isTrue(equalSignIndex >= 0, lineWithoutPassword+" has parameter chunk without '=' sign, which is invalid");
					Assert.isTrue(equalSignIndex > 0, lineWithoutPassword+" '=' character is not preceeded by parameter name");

					final String paramNameWithoutStyle = thisParamWithoutSeparatorWithoutStyle.substring(" ".length(), equalSignIndex);
					final String paramNameWithStyle = "<span class='"+PARAM_NAME_CLASS+"'>"+paramNameWithoutStyle+"</span>";
					
					final String thisParamValue;
					final String thisParamValueWithStyle;
					final String thisParamWithoutSeparatorWithStyle;
					if (equalSignIndex >= thisParamWithoutSeparatorWithoutStyle.length()-1) {
						thisParamValue = "";
						thisParamValueWithStyle = "<span class='"+PARAM_VALUE_CLASS+"'>"+thisParamValue+"</span>";
						thisParamWithoutSeparatorWithStyle = thisParamWithoutSeparatorWithoutStyle.replace(paramNameWithoutStyle+'=', paramNameWithStyle+'=').replaceAll("=$", "="+thisParamValueWithStyle);
					} else {
						thisParamValue = thisParamWithoutSeparatorWithoutStyle.substring(equalSignIndex+1);
						thisParamValueWithStyle = "<span class='"+PARAM_VALUE_CLASS+"'>"+thisParamValue+"</span>";			
						thisParamWithoutSeparatorWithStyle = thisParamWithoutSeparatorWithoutStyle.replace(paramNameWithoutStyle+'=', paramNameWithStyle+'=').replace('='+thisParamValue, '='+thisParamValueWithStyle);
					}					

					

					lineDecorated = lineDecorated.replace(thisParamWithSeparatorWithoutStyle.toString(), thisParamWithoutSeparatorWithStyle);
				}

				if (!isComment && StringUtils.isNotBlank(lineDecorated)) {
					int commandEndIndex = lineDecorated.indexOf(' ');
					if (commandEndIndex == -1) {
						commandEndIndex = lineDecorated.length();
					}
					final String commandWithoutStyle = lineDecorated.substring(0, commandEndIndex);
					final String commandWithStyle = "<span class='"+COMMAND_CLASS+"'>"+commandWithoutStyle+"</span>";

					if (commandEndIndex == lineDecorated.length()) {
						lineDecorated = commandWithStyle;
					} else {
						lineDecorated = commandWithStyle + lineDecorated.substring(commandEndIndex);
					}
				}

				if (lineDecorated.isEmpty()) {
					lineDecorated = "&nbsp;";
				}

				pw.println("<div>"+lineDecorated.trim()+"</div>");
			}

			pw.println("</body>");
            pw.println("</html>");			
			
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(pw);
		}
	}

	private static void createTempFileForViewer(String sourceFileName, CoreConstants.INJECTOR_TYPE scriptType, InputStream in, File targetFile) throws Exception
	{
		BufferedReader br = null;
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		BufferedWriter bw = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(in, CoreConstants.CHARACTER_SET_ENCODING));

			// cater for scenario where the input and target are the same file
			File tempFile = new File( targetFile.getParentFile(),  "temp"+Utils.getUniqueFilenameSuffix() +".temp");

			fos = new FileOutputStream(tempFile, true);
			writer = new OutputStreamWriter(fos, CoreConstants.CHARACTER_SET_ENCODING);
			bw = new BufferedWriter(writer);

			String strLine;
			while ( (strLine = br.readLine()) != null )
			{
				bw.write(removePasswordFromInputString(strLine, scriptType));
			}
			bw.flush();
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(bw);
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(fos);

			if(targetFile.exists() && !targetFile.delete()){
				throw new Exception("Unable to delete file: " + targetFile.getName());
			}
			if(!tempFile.renameTo(targetFile)){
				throw new Exception("Unable to rename file: " + tempFile.getName());
			}
		}
		finally
		{
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(bw);
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(fos);
		}

	}

	public static String removePasswordFromInputString(String line, CoreConstants.INJECTOR_TYPE scriptType) throws Exception
	{
		StringBuffer buffer = new StringBuffer("");
		String keyword = null;
		if (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(scriptType) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(scriptType))
		{
			keyword = SwiftBuildConstants.PWD_FLD_KEYWORDS;
		}
		else if (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(scriptType))
		{
			keyword = SwiftBuildConstants.PWD_IL_KEYWORDS;
		}
		String AUTO_REPLACE_KEYWORD = "SWIFTBUILD_AUTO_REMOVE_PWD\n";
		if ( keyword != null && line.startsWith(keyword) )
		{
			buffer.append(keyword).append(AUTO_REPLACE_KEYWORD);
		}else{
			buffer.append(line).append("\n");
		}
		return buffer.toString();
	}

	public static boolean isEmptyInjector(String scriptName, File injectorsPackageFile)
	{
		InputStream inputStream = null;
		BufferedReader br = null;
		ZipFile zipFile = null;
		try
		{
			if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(injectorsPackageFile) )
			{
				inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(injectorsPackageFile, scriptName);
			}
			else
			{
				zipFile = new ZipFile(injectorsPackageFile);
				inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, scriptName);
			}
			br = new BufferedReader(new InputStreamReader(inputStream));
			String strLine;
			while ( (strLine = br.readLine()) != null )
			{
				if ( strLine.startsWith(CoreConstants.ITERATION_SEPARATOR) || strLine.startsWith("# Script Identification:") || strLine.startsWith("# SECTIONID: 'IF") )
				{
					return false;
				}
			}
			return true;
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return false;
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(br);
			if ( zipFile != null )
			{
				try
				{
					zipFile.close();
				}
				catch ( IOException e )
				{
					FileUtils.printStackTrace(e);
				}
			}
		}
	}

	public static boolean hasReplacementTokens(Map<String, String> environmentProperties)
	{
		Iterator<String> iter = environmentProperties.keySet().iterator();
		while ( iter.hasNext() )
		{
			String key = iter.next();

			if ( key.startsWith(SwiftBuildConstants.EXTRA_PROPERTY_KEYWORD) )
			{
				key = key.substring(SwiftBuildConstants.EXTRA_PROPERTY_KEYWORD.length());
			}

			if ( key.startsWith(SwiftBuildConstants.CUSTOM_PROPERTY_START_KEYWORD) && key.endsWith(SwiftBuildConstants.CUSTOM_PROPERTY_END_KEYWORD) )
			{
				return true;
			}
		}
		return false;
	}

	public static Properties getReplacementTokens(Map<String, String> environmentProperties)
	{
		Properties res = new Properties();
		Iterator<String> iter = environmentProperties.keySet().iterator();
		while ( iter.hasNext() )
		{
			String key = iter.next();

			String val = key;
			if ( key.startsWith(SwiftBuildConstants.EXTRA_PROPERTY_KEYWORD) )
			{
				val = key.substring(SwiftBuildConstants.EXTRA_PROPERTY_KEYWORD.length());

			}

			if ( val.startsWith(SwiftBuildConstants.CUSTOM_PROPERTY_START_KEYWORD) && val.endsWith(SwiftBuildConstants.CUSTOM_PROPERTY_END_KEYWORD) )
			{
				res.setProperty(val, environmentProperties.get(key));
			}
		}
		return res;
	}

	public static File getLogsFolderFromName(String name)
	{
		File folder = createTempFolderFromName(name, "logs");
		return folder;
	}

	public static File createTempFolderFromName(String name, String subFolderName)
	{
		File relativeFolder = getRelativeFolderFromName(name);

		File folder = new File(FileUtils.getLogFolder().getAbsolutePath() + UtilsConstants.FORWARD_SLASH + relativeFolder + UtilsConstants.FORWARD_SLASH + subFolderName);
		if ( !folder.exists() )
		{
			folder.mkdirs();
		}

		return folder;
	}

	public static File getRelativeFolderFromName(String name)
	{
		String temp = name.replaceAll(" ", "-");
		int pathIndex = temp.lastIndexOf(UtilsConstants.FORWARD_SLASH);

		int startIndex = 0;
		if ( pathIndex != -1 )
		{
			startIndex = pathIndex + 1;
		}

		int endIndex = temp.lastIndexOf(".");
		String folderName = temp.substring(startIndex, endIndex);
		File folder = new File(folderName);

		return folder;
	}

	public static void downloadFLDLogs(BuildMain BuildMain, List<String> fileNamesToDownload) throws Exception
	{
		File logFolder = getLogFolder(BuildMain);
		String remoteFolder = BuildMain.getSwiftBuildPropertiesValidationPanel().getFLDScriptsLogFolder();

		if ( BuildMain.isSSHProtocol() )
		{
			SynchronousDownloader d = new SynchronousDownloader(BuildMain, remoteFolder, logFolder.getAbsolutePath(), fileNamesToDownload);
			d.downloadFiles();
		}
		else if (BuildMain.isFileProtocol())
		{
			FileProtocolManager.downloadFiles(remoteFolder, logFolder.getAbsolutePath(), fileNamesToDownload, true);
		}
		else
		{
			String hostName = BuildMain.getSwiftBuildPropertiesValidationPanel().getFLDScriptsHostName();
			String userName = BuildMain.getSwiftBuildPropertiesValidationPanel().getFLDScriptsHostUserName();
			String password = BuildMain.getSwiftBuildPropertiesValidationPanel().getFLDScriptsHostPassword();

			LogsDownloader d = new LogsDownloader(hostName, userName, password, remoteFolder, fileNamesToDownload, logFolder.getAbsolutePath());
			d.downloadFiles(true);
		}
	}

	public static void downloadFLDLogFile(BuildMain BuildMain, String logFileName) throws Exception
	{
		List<String> fileNamesToDownload = new ArrayList<String>();
		fileNamesToDownload.add(logFileName);
		downloadFLDLogs(BuildMain, fileNamesToDownload);
	}

	public static File getLogFolder(BuildMain BuildMain)
	{
		String injectorsPackageFileName = BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile().getAbsolutePath();
		if(Platform.isWindows()) {
			injectorsPackageFileName = injectorsPackageFileName.replaceAll("\\\\", "/");
		}
		return SwiftBuildFileUtils.getLogsFolderFromName(injectorsPackageFileName);
	}

	public static File getLogFile(BuildMain BuildMain, String injectorName)
	{
		File logFolder = getLogFolder(BuildMain);
		return new File(logFolder.getAbsolutePath() + UtilsConstants.FORWARD_SLASH + injectorName + "." + SwiftBuildConstants.INJECTOR_LOG_FILE_EXTENSION);
	}

	public static File showInjectorContent(BuildMain BuildMain, Injector injector, final boolean iterationTrackingEnabled, final boolean convertToHtml, final boolean showFile) throws Exception
	{
		File tempFolder = FileUtils.getTemporaryFolder();
		String fileName = tempFolder.getAbsolutePath() + UtilsConstants.FORWARD_SLASH;
		if (CoreConstants.INJECTOR_TYPE.TYPE_HTMLFORM.equals(injector.getType())) {
			if (injector.getName().contains(".")) {
				fileName += injector.getName().replaceAll("\\..*$", ".html");
			} else {
				fileName += injector.getName()+".html";
			}			
		} else if (CoreConstants.INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || CoreConstants.INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) {
			if (injector.getName().contains(".")) {
				fileName += injector.getName().replaceAll("\\..*$", ".html");
			} else {
				fileName += injector.getName()+".html";
			}			
		} else {
			fileName += injector.getName();
		}

		File targetFile = new File(fileName);

		FileUtils.println("Creating temporary file for viewing: '" + targetFile + "'");
		SwiftBuildFileUtils.createTempFileForViewerAndShow(BuildMain.getInjectorsPackageSelectionPanel().getInjectorsPackageFile(), injector.getName(), injector.getType(), targetFile, iterationTrackingEnabled, convertToHtml, showFile);
		return targetFile;
	}

	public static void openFolder(File folder) throws Exception
	{
		Desktop desktop = Desktop.getDesktop();
		desktop.open(folder);
	}

	public static boolean isFLDScriptCompleted(File localFLDLogFile) throws Exception
	{
		String text = org.apache.commons.io.FileUtils.readFileToString(localFLDLogFile);
		return SwiftBuildFileUtils.hasFLDTextCloseFormCommand(text);
	}

	public static void saveToFile(File file, String output, boolean append) throws Exception
	{
		PrintWriter printWriter = null;
		try
		{
			printWriter = new PrintWriter(new FileWriter(file, append));
			printWriter.print(output);
		}
		finally
		{
			IOUtils.closeQuietly(printWriter);
		}
	}


	public static void initConsoleLogFolder() throws Exception
	{
	    String shortApplicationName = SwiftGUIMain.getInstance().getShortApplicationName().toString();
		consoleLogFile = new File(new File(Config.getLogFolder(),shortApplicationName),
		        shortApplicationName + "-" + SwiftGUIMain.getStartTime() + "-" +  SwiftBuildConstants.CONSOLE_LOG_FILE_NAME_PREFIX + FileUtils.LOG_FILE_EXTENSION);
		consoleLogFile.createNewFile();
	}

	public static void cleanupTempFolder() throws IOException
	{
		org.apache.commons.io.FileUtils.deleteDirectory(com.rapidesuite.client.common.util.Config.getTempFolder());
	}

	public static boolean isSubmitSingleRequestJob(File injectorsPackageFile, String injectorName)
	{
		ZipFile zipFile = null;
		InputStream inputStream = null;
		BufferedReader br = null;
		try
		{
			if ( InjectorsPackageUtils.isEncryptedInjectorsPackage(injectorsPackageFile) )
			{
				inputStream = InjectorsPackageUtils.getInputStreamFromEncryptedZIPFile(injectorsPackageFile, injectorName);
			}
			else
			{
				zipFile = new ZipFile(injectorsPackageFile);
				inputStream = InjectorsPackageUtils.getInputStreamFromUnencryptedZIPFile(zipFile, injectorName);
			}
			br = new BufferedReader(new InputStreamReader(inputStream));
			String strLine;
			int counter = 0;
			while ( (strLine = br.readLine()) != null )
			{
				if ( strLine.startsWith(CoreConstants.ITERATION_SEPARATOR) )
				{
					counter++;
					if ( counter > 1 )
					{
						return false;
					}
				}
			}
			return counter == 1;
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			return false;
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(br);
			if ( zipFile != null )
			{
				try
				{
					zipFile.close();
				}
				catch ( IOException e )
				{
					FileUtils.printStackTrace(e);
				}
			}
		}
	}

	public static boolean hasFLDLastCommand(String command, String text)
	{
		if ( text == null )
		{
			return false;
		}
		int indexOf = text.lastIndexOf(command);
		if ( indexOf != -1 )
		{
			String subText = text.substring(indexOf + command.length() + 1);
			subText = subText.replace("\r\n", "").replace("\n", "").trim();
			return subText.isEmpty();
		}
		return false;
	}

	public static boolean isEndOfScript(String text)
	{
		if ( text == null )
		{
			return false;
		}
		int indexOf = text.indexOf(CoreConstants.MENU_MAGIC_MAGIC_QUIT);
		if ( indexOf != -1 )
		{
			String subText = text.substring(indexOf + 1);
			indexOf = subText.indexOf("USER_EXIT CHOICE OK");
			return indexOf != -1;
		}
		return false;
	}
	
	public static String readFldFileToStringWithOffsetWithMaxLimit(final File fileToRead, final String encoding, final long offset, final MutableBoolean fileWasReadOfToEof, final int maxLimit) throws FileNotFoundException, IOException {
		Assert.notNull(fileToRead, "fileToRead must not be null");
		Assert.isTrue(fileToRead.exists(), fileToRead.getAbsolutePath()+" does not exist");
		Assert.isTrue(fileToRead.isFile(), fileToRead.getAbsolutePath()+" is not a file");
		Assert.isTrue(offset >= 0, "offset = "+offset+". It must not be negative");
		
		try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(fileToRead), encoding))) {
			IOUtils.skipFully(r, offset); //skip "offset" number of characters
			
			StringBuilder strBldr = new StringBuilder();
			int charAsInt;
			int numberOfLines = 0;
			StringBuilder thisLine = new StringBuilder();
			//Reader.read() reads 1 char, NOT 1 byte
			while ((charAsInt = r.read()) != -1) {
				final char c = (char) charAsInt;
				strBldr.append(c);
				thisLine.append(c);
				if (c == '\n') {
					if (!thisLine.toString().matches("^(.*\\s)?\\\\\\s*$") //not ended with backslash
							&& !thisLine.toString().startsWith("#") && StringUtils.isNotBlank(thisLine.toString())
							&& !thisLine.toString().trim().equals(CoreConstants.MENU_MAGIC_MAGIC_QUIT)) {
						numberOfLines++;
					}
					thisLine.setLength(0);
					
					if (numberOfLines >= maxLimit) {
						break;
					}
				}
			}
			fileWasReadOfToEof.setValue(charAsInt == -1);
			final String output = strBldr.toString();
			return output;
		}
	}	
	
    public static void concatenateTextFiles(final File outputFile, final List<File> originalFiles) throws UnsupportedEncodingException, FileNotFoundException, IOException {
    	int index = 0;
    	for (final File originalFile : originalFiles) {
    		if (originalFile == null || !originalFile.isFile()) {
    			continue;
    		}
    		try (Reader reader = new InputStreamReader(new FileInputStream(originalFile), CoreConstants.CHARACTER_SET_ENCODING);
    				Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile, index > 0), CoreConstants.CHARACTER_SET_ENCODING)) {
    			if (index > 0) {
    				final String separator = System.lineSeparator()+System.lineSeparator()+System.lineSeparator();
    				IOUtils.write(separator, writer);
    			}
    			IOUtils.copy(reader, writer);
    		}
    		index++;
    	}
    }	
    
    public static void viewMergedLogs(final Injector injector, final List<File> originalLogFiles, final boolean convertToHtml) throws Exception {
		if (!originalLogFiles.isEmpty()) {
			if (INJECTOR_TYPE.TYPE_NEWFLDFORM.equals(injector.getType()) || INJECTOR_TYPE.TYPE_FLDFORM.equals(injector.getType())) {
				final File mergedFile = new File(Config.getTempFolder(), injector.getNameWithoutExtension()+Utils.getUniqueFilenameSuffix()+".html");
				if (convertToHtml) {
					SwiftBuildFileUtils.wrapInAnHtml(injector.getName(), mergedFile, originalLogFiles);
					FileUtils.startSourceViewerBrowser(Config.getBrowserViewSource(), mergedFile.getAbsoluteFile());
				} else {
					SwiftBuildFileUtils.concatenateTextFiles(mergedFile, originalLogFiles);
					FileUtils.startTextEditor(Config.getCmdTextEditor(), mergedFile.getAbsoluteFile());					
				}
			} else {
				final File mergedFile = new File(Config.getTempFolder(), injector.getNameWithoutExtension()+Utils.getUniqueFilenameSuffix()+".log");
				SwiftBuildFileUtils.concatenateTextFiles(mergedFile, originalLogFiles);
				FileUtils.startTextEditor(Config.getCmdTextEditor(), mergedFile.getAbsoluteFile());
			}										
		}	    	
    }
}