/**************************************************
 * $Revision: 60351 $:
 * $Author: hassan.jamil $:
 * $Date: 2016-12-21 16:41:50 +0700 (Wed, 21 Dec 2016) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/utils/DFFRow.java $:
 * $Id: DFFRow.java 60351 2016-12-21 09:41:50Z hassan.jamil $:
 */
package com.rapidesuite.reverse.utils;

import java.util.LinkedList;
import java.util.regex.Pattern;

public class DFFRow
{

	private String columnName;
	private String contextCode;
	private boolean isContextCodeToDisplay;
	private boolean isGlobal;
	private String contextDisplayName;
	private String valueSetQuery;
	private String formLeftPrompt;
	
	/**
	 * The list of REGEXes to handle the PARAMETERIZED valueSetQueries in the DFF from Oracle.
	 * 
	 *  Other REGEXes need to be added to this list incrementally so that we can cater all of the cases
	 *  
	 *  IMPORTANT NOTE: Keep in mind that these are executed sequentially
	 */
	@SuppressWarnings("serial")
	public static final LinkedList<String> PARAMETERIZED_WHERE_CLAUSE_REGEXES = new LinkedList<String>() {
	{
		/**
		 * To cater clauses like 
		 * ORGANIZATION_ID = :$PROFILES$.PER_ORGANIZATION_ID
		 * or 
		 * O.BUSINESS_GROUP_ID =   :$PROFILES$.PER_BUSINESS_GROUP_ID
		 * or
		 * HOU.BUSINESS_GROUP_ID =
		 * :$PROFILES$.PER_BUSINESS_GROUP_ID
		 */
		add("[a-zA-Z_.]+[\r\n\t ]*[\\+\\-\\\\*]*[\r\n\t ]*[0-9]*[\r\n\t ]*=[\r\n\t ]*:\\$[a-zA-Z]+\\$.[a-zA-Z_]+");
		
		/**
		 * To cater clauses like 
		 * NVL(BUSINESS_GROUP_ID, :$PROFILES$.PER_BUSINESS_GROUP_ID) = :$PROFILES$.PER_BUSINESS_GROUP_ID
		 * or 
		 * NVL(PUR.BUSINESS_GROUP_ID, :$PROFILES$.PER_BUSINESS_GROUP_ID) =
		 * :$PROFILES$.PER_BUSINESS_GROUP_ID
		 */
		add("NVL\\([a-zA-Z_.]*,[\r\n\t ]*:\\$[a-zA-Z]+\\$.[a-zA-Z_]+\\)[\r\n\t ]*=[\r\n\t ]*:\\$[a-zA-Z]+\\$.[a-zA-Z_]+");
		
		/**
		 * To cater clauses like 
		 * PDB.BALANCE_TYPE_ID = SUBSTR(:$FLEX$.PAY_GB_BALANCE_NAME,2)
		 * or 
		 * HOI.ORG_INFORMATION_ID <>
		 * NVL(:$PROFILES$.PER_ORG_INFORMATION_ID,-1)
		 */
		add("[a-zA-Z0-9_.']+[\r\n\t ]*[=<>!]+[\r\n\t ]*[a-zA-Z_]+[\r\n\t ]*\\([\r\n\t ]*:\\$[a-zA-Z]+\\$.[a-zA-Z_]+[\r\n\t ]*[,]*[\r\n\t ]*[-a-zA-Z0-9]*[\r\n\t ]*\\)");

		/**
		 * To cater clauses like 
		 * SUBSTR(:$FLEX$.PAY_GB_BALANCE_NAME,2) = PDB.BALANCE_TYPE_ID
		 * or 
		 * ABCD.GET_SOMETHING(:$FLEX$.PAY_GB_BALANCE_NAME) > 1
		 */
		add("[a-zA-Z_.]+[\r\n\t ]*\\([\r\n\t ]*:\\$[a-zA-Z]+\\$.[a-zA-Z_]+[\r\n\t ]*[,]*[\r\n\t ]*[-a-zA-Z0-9]*[\r\n\t ]*\\)[\r\n\t ]*[=<>!]+[\r\n\t ]*[a-zA-Z0-9_.']+");
		
		/**
		 * To cater clauses like 
		 * :$FLEX$.WORKING_HOURS IS NOT NULL
		 * or 
		 * :$FLEX$.WORKING_HOURS IS NULL
		 * or 
		 * :$FLEX$.WORKING_HOURS IS 
		 * NOT NULL
		 */
		add(":\\$[a-zA-Z]+\\$.[a-zA-Z_]+[\r\n\t ]*IS[\r\n\t ]*[NOT]*[\r\n\t ]*NULL");
	}};
	
	/**
	 * The list of REGEXes to handle the USERENV('SESSIONID') valueSetQueries in the DFF from Oracle.
	 * 
	 *  Other REGEXes need to be added to this list incrementally so that we can cater all of the cases
	 *  
	 *  IMPORTANT NOTE: Keep in mind that these are executed sequentially
	 */
	@SuppressWarnings("serial")
	public static final LinkedList<String> USERENV_SESSIONID_WHERE_CLAUSE_REGEXES = new LinkedList<String>() {
	{
		/**
		 * To cater clauses like 
		 * FND.SESSION_ID = USERENV ('SESSIONID')
		 * or
		 * S.SESSION_ID = 
		 * USERENV('SESSIONID')
		 */
		add("[a-zA-Z._]+[\r\n\t ]*=[\r\n\t ]*USERENV[\r\n\t ]*\\([\r\n\t ]*'SESSIONID'[\r\n\t ]*\\)");
		
		/**
		 * To cater clauses like 
		 * USERENV ('SESSIONID') = FND.SESSION_ID
		 * or
		 * USERENV('SESSIONID') = 
		 * S.SESSION_ID
		 */
		add("USERENV[\r\n\t ]*\\([\r\n\t ]*'SESSIONID'[\r\n\t ]*\\)[\r\n\t ]*=[\r\n\t ]*[a-zA-Z._]+");
	}};
	
	/**
	 * REGEX to find the USERENV('SESSIONID') in a valueSetQuery
	 */
	public static final String USERENV_SESSIONID_REGEX = "USERENV[\r\n\t ]*\\([\r\n\t ]*'SESSIONID'[\r\n\t ]*\\)";
	
	/**
	 * Pre-compiled REGEX pattern to find the USERENV('SESSIONID') in the valueSetQuery
	 * 
	 * Pre-compiled because compiling it every time will be resource intensive.
	 */
	public static final Pattern USERENV_SESSIONID_PATTERN = Pattern.compile(USERENV_SESSIONID_REGEX);

	/**
	 * REGEX to find the USERENV('LANG') in a valueSetQuery
	 */
	public static final String USERENV_LANG_REGEX = "USERENV[\r\n\t ]*\\([\r\n\t ]*'LANG'[\r\n\t ]*\\)";
	
	/**
	 * Pre-compiled REGEX pattern to find the USERENV('LANG') in the valueSetQuery
	 * 
	 * Pre-compiled because compiling it every time will be resource intensive.
	 */
	public static final Pattern USERENV_LANG_PATTERN = Pattern.compile(USERENV_LANG_REGEX);

	/**
	 * REGEX to find the fnd_sessions in a valueSetQuery
	 */
	public static final String FND_SESSIONS_REGEX = "(?i)fnd_sessions";
	
	/**
	 * Pre-compiled REGEX pattern to find the fnd_sessions in the valueSetQuery
	 * 
	 * Pre-compiled because compiling it every time will be resource intensive.
	 */
	public static final Pattern FND_SESSIONS_PATTERN = Pattern.compile(FND_SESSIONS_REGEX);
	
	/**
	 * The replacement value for where clauses
	 */
	public static String WHERE_CLAUSE_REGEX_REPLACMENT = "1=1";
	
	/**
	 * The replacement value for USERENV('LANG') in there where clauses (not the entire where clause)
	 */
	public static String LANG_REGEX_REPLACMENT = "'US'";
	
	/**
	 * The replacement value for USERENV('LANG') in there where clauses (not the entire where clause)
	 */
	public static String FND_SESSIONS_REPLACMENT = "DUMMY_FND_SESSIONS";
	
	public DFFRow(String columnName,String contextCode,boolean isContextCodeToDisplay,
			boolean isGlobal,String contextDisplayName,String valueSetQuery,String formLeftPrompt){
		this.columnName=columnName;
		this.contextCode=contextCode;
		this.isContextCodeToDisplay=isContextCodeToDisplay;
		this.isGlobal=isGlobal;
		this.contextDisplayName=contextDisplayName;
		this.valueSetQuery=valueSetQuery;
		
		sanatizeValueSetQuery();
				
		this.formLeftPrompt=formLeftPrompt;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getContextCode() {
		return contextCode;
	}

	public boolean isContextCodeToDisplay() {
		return isContextCodeToDisplay;
	}

	public boolean isGlobal() {
		return isGlobal;
	}

	public String getContextDisplayName() {
		return contextDisplayName;
	}

	public String getValueSetQuery() {
		return valueSetQuery;
	}
	
	public boolean hasValueSetQuery() {
		return valueSetQuery!=null;
	}

	public String getFormLeftPrompt() {
		return formLeftPrompt;
	}
	
	/**
	 * Checks if the valueSetQuery contains place-holders for parameterization like ':$FLEX$' or ':$PROFILES$'
	 * 
	 * Also check if there is a valueSetQuery 
	 * 
	 * @return true if it contains, false if there is no valueSetQuery or doesn't contains
	 */
	public boolean valueSetQueryContainsPlaceHolders() {
		return hasValueSetQuery() && this.valueSetQuery.contains(":$");
	}
	
	/**
	 * Checks if the valueSetQuery contains SESSIONID
	 * 
	 * Also check if there is a valueSetQuery 
	 * 
	 * @return true if it contains, false if there is no valueSetQuery or doesn't contains
	 */
	public boolean valueSetQueryContainsSESSIONID() {
		return hasValueSetQuery() &&
				USERENV_SESSIONID_PATTERN.matcher(this.valueSetQuery).find();
	}
	
	/**
	 * Checks if the valueSetQuery contains LANG
	 * 
	 * Also check if there is a valueSetQuery 
	 * 
	 * @return true if it contains, false if there is no valueSetQuery or doesn't contains
	 */
	public boolean valueSetQueryContainsLANG() {
		return hasValueSetQuery() &&
				USERENV_LANG_PATTERN.matcher(this.valueSetQuery).find();
	}
	
	/**
	 * Checks if the valueSetQuery contains FND_SESSIONS
	 * 
	 * Also check if there is a valueSetQuery 
	 * 
	 * @return true if it contains, false if there is no valueSetQuery or doesn't contains
	 */
	public boolean valueSetQueryContainsFNDSESSIONS() {
		return hasValueSetQuery() &&
				FND_SESSIONS_PATTERN.matcher(this.valueSetQuery).find();
	}
	
	/**
	 * Sanitize on the valueSetQuery
	 * i.e. removing place holders where clauses (replacing with 1=1), removing USERENV('SESSIONID') where clauses (replacing with 1=1) and removing USERENV('LANG') (replacing with 'US') 
	 */
	public void sanatizeValueSetQuery() {
		// if the valueSetQuery contains place-holders
		if(this.valueSetQueryContainsPlaceHolders()) {
			// replace the where clauses containing those place-holders with '1=1' using the REGEXes defined in PARAMETERIZED_WHERE_CLAUSE_REGEXES
			for (String parameterized_where_clause_regex : PARAMETERIZED_WHERE_CLAUSE_REGEXES) {
				this.valueSetQuery = this.valueSetQuery.replaceAll(parameterized_where_clause_regex, WHERE_CLAUSE_REGEX_REPLACMENT);	
			}
		}
		
		// if the valueSetQuery contains USERENV('SESSIONID')
		if(valueSetQueryContainsSESSIONID()) {
			// replace the where clauses containing those USERENV('SESSIONID') with '1=1' using the REGEXes defined in USERENV_SESSIONID_WHERE_CLAUSE_REGEXES
			for (String parameterized_where_clause_regex : USERENV_SESSIONID_WHERE_CLAUSE_REGEXES) {
				this.valueSetQuery = this.valueSetQuery.replaceAll(parameterized_where_clause_regex, WHERE_CLAUSE_REGEX_REPLACMENT);	
			}
		}
		
		// if the valueSetQuery contains USERENV('LANG')
		if(valueSetQueryContainsLANG()) {
			// replace the USERENV('LANG') part of the where clause with ''US'' (including the terminating single quotes) using the REGEX defined in USERENV_LANG_REGEX
			this.valueSetQuery = this.valueSetQuery.replaceAll(USERENV_LANG_REGEX, LANG_REGEX_REPLACMENT);
		}
		
		// if the valueSetQuery contains FND_SESSIONS
		if(valueSetQueryContainsFNDSESSIONS()) {
			// replace the 'FND_SESSIONS' part of the where clause with 'DUMMY_FND_SESSIONS' (including the terminating single quotes) using the REGEX defined in FND_SESSIONS_REGEX
			this.valueSetQuery = this.valueSetQuery.replaceAll(FND_SESSIONS_REGEX, FND_SESSIONS_REPLACMENT);
		}
	}
	
}