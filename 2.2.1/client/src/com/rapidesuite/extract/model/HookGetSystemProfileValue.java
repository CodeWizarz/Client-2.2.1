package com.rapidesuite.extract.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;

public class HookGetSystemProfileValue {
	
	private Map<String,Map<String,String>> sqlStatementToKeyValueMap;
	private final Object lock = new Object();
	
	public HookGetSystemProfileValue() {
		sqlStatementToKeyValueMap=new HashMap<String,Map<String,String>>();
	}
	
	public Map<String, String> getKeyValueMap(String sqlStatement) throws Exception {
		synchronized(lock) {
			Map<String, String> keyValueMap=sqlStatementToKeyValueMap.get(sqlStatement);
			return keyValueMap;
		}
	}
	
	public void putKeyValueMap(String sqlStatement,Map<String, String> keyValueMap) throws Exception {
		synchronized(lock) {
			sqlStatementToKeyValueMap.put(sqlStatement, keyValueMap);
		}
	}
	
	public void init()  {
		sqlStatementToKeyValueMap=new HashMap<String,Map<String,String>>();
	}
	
	private Map<String, String> runFunction(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService,String sqlStatement)
	{
		try{
			List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();

			ParameterNameValue parameterNameValue=new ParameterNameValue();
			parametersList.add(parameterNameValue);
			parameterNameValue.setName("param1");
			parameterNameValue.setValue(sqlStatement);

			//System.out.println("runFunction:"+sqlStatement);
			byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//			String data=new String(outputBytes,"ISO-8859-1");
			//System.out.println("data:"+data);
			Map<String, String> keyToValueMap=ExtractUtils.getLOVFromXMLReport(outputBytes,true);

			return keyToValueMap;
		}
		catch(Exception e) {
			FileUtils.println("HookGetSystemProfileValue, function did not run: '"+sqlStatement+"'");
			return new HashMap<String, String>();
		}
	}

	public List<ExtractDataRow> apply(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService,
			int fieldIndex, List<ExtractDataRow> listToApply) throws Exception {
		for (ExtractDataRow extractDataRow:listToApply){
			String[] dataValues=extractDataRow.getDataValues();
			String key=dataValues[fieldIndex];
			if (key==null || key.isEmpty()) {
				continue;
			}
			final String[] tokens = key.split(Pattern.quote(ExtractConstants.FUNCTION_KEYS_SEPARATOR));
			if (tokens==null) {
				throw new Exception("Unable to split key using Delimiter: '"+ExtractConstants.FUNCTION_KEYS_SEPARATOR+"' : "+key);
			}
			if (tokens.length==0) {
				// both parameters are null
				dataValues[fieldIndex]="";
			}
			else {
				if (tokens.length==1) {
					// if the second parameter is null then we return the first one
					if ( key.toLowerCase().endsWith(ExtractConstants.FUNCTION_KEYS_SEPARATOR.toLowerCase())) {
						String profileOptionValueParameter=tokens[0];
						dataValues[fieldIndex]=profileOptionValueParameter;
					}
					else {
						throw new Exception("Unsupported after splitting key using Delimiter: '"+ExtractConstants.FUNCTION_KEYS_SEPARATOR+"' : "+key);
					}
				}
				else {
					if (tokens.length!=2) {
						throw new Exception("Too many tokens ("+tokens.length+") after splitting key using Delimiter: '"+ExtractConstants.FUNCTION_KEYS_SEPARATOR+"' : "+key);
					}

					String profileOptionValueParameter=tokens[0];
					String sqlValidationParameter=tokens[1];
					if (sqlValidationParameter==null || sqlValidationParameter.isEmpty() || sqlValidationParameter.equalsIgnoreCase("NULL")) {
						dataValues[fieldIndex]=profileOptionValueParameter;
					}

					String sqlStatement=getSQLStatement(sqlValidationParameter);
					Map<String, String> keyValueMap=getKeyValueMap(sqlStatement);
					if (keyValueMap!=null){
						String value=keyValueMap.get(profileOptionValueParameter);
						if (value!=null){
							dataValues[fieldIndex]=value;
						}
						else {
							dataValues[fieldIndex]="";
						}
					}
					else {
						keyValueMap=runFunction(dynamicSQLReportPath,reportWebServiceInfo,reportService,sqlStatement);
						putKeyValueMap(sqlStatement, keyValueMap);
						String value=keyValueMap.get(profileOptionValueParameter);
						if (value!=null){
							dataValues[fieldIndex]=value;
						}
						else {
							dataValues[fieldIndex]="";
						}
					}
				}
			}
		}
		return listToApply;
	}

	private String getSQLStatement(String sqlValidationParameter) {
		StringBuffer toReturn=new StringBuffer("");
		/*
		int indexOfSelect=sqlValidationParameter.toUpperCase().indexOf("SELECT");
		if (indexOfSelect==-1) {
			throw new Exception("Error: GET_SYSTEM_PROFILE_VALUE: no 'SELECT' keyword in the SQL Validation field!");
		}
		int indexOfFrom=sqlValidationParameter.toUpperCase().indexOf("FROM");
		if (indexOfFrom==-1) {
			throw new Exception("Error: GET_SYSTEM_PROFILE_VALUE: no 'FROM' keyword in the SQL Validation field!");
		}
		int indexOfWhere=sqlValidationParameter.toUpperCase().indexOf("WHERE");
		int indexOfOrderBy=sqlValidationParameter.toUpperCase().indexOf("ORDER BY");
		int indexOfFirstComma=sqlValidationParameter.toUpperCase().indexOf(",");
		if (indexOfFrom==-1) {
			throw new Exception("Error: GET_SYSTEM_PROFILE_VALUE: no ',' keyword in the SQL Validation field!");
		}
		int indexOfSecondComma=sqlValidationParameter.toUpperCase().indexOf(",",indexOfFirstComma+1);
	
		String meaning=sqlValidationParameter.substring(indexOfSelect+6, indexOfFirstComma);
		*/
		toReturn.append(sqlValidationParameter);
				
		return toReturn.toString();
	}
	
}