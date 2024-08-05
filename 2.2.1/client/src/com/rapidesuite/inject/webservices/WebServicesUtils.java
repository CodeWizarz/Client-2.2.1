package com.rapidesuite.inject.webservices;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceFeature;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.KbValueToDBValueConversionSQLQueryType;
import com.erapidsuite.configurator.navigation0005.MethodParameterType;
import com.erapidsuite.configurator.navigation0005.MethodParameterValueType;
import com.erapidsuite.configurator.navigation0005.ObjectParameterType;
import com.erapidsuite.configurator.navigation0005.ObjectParameterValueType;
import com.erapidsuite.configurator.navigation0005.ObjectParametersType;
import com.erapidsuite.configurator.navigation0005.QueryType;
import com.erapidsuite.configurator.navigation0005.ValueKBType;
import com.rapidesuite.client.common.util.DatabaseUtils;
import com.rapidesuite.configurator.dao.DirectConnectDao;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.inject.Worker;

import oracle.jbo.common.service.ReflectionHelper;
import weblogic.wsee.jws.jaxws.owsm.SecurityPoliciesFeature;

public class WebServicesUtils {

	
	@SuppressWarnings("rawtypes")
	public static Object getServiceObject(String endpointUrl,String username,String password,String serviceClassName,String serviceSoapHttpPortName) 
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, ClassNotFoundException {
		SecurityPoliciesFeature securityFeatures = new SecurityPoliciesFeature(new String[] {
		"oracle/wss_username_token_client_policy" });
		
		Object serviceObject =ReflectionHelper.newInstance(serviceClassName);
		Class[] paramTypes = new Class[1];
		paramTypes[0] = WebServiceFeature[].class;
		Object[] paramsValues = new Object[1];
		WebServiceFeature[] array = new WebServiceFeature[1];
		array[0] = securityFeatures;
		paramsValues[0] = array;
		Object serviceSoapHttpPortObject = ReflectionHelper.invokeMethod(serviceObject, serviceSoapHttpPortName, paramTypes, paramsValues);
		
		Map<String, Object> requestContext = ((BindingProvider) serviceSoapHttpPortObject).getRequestContext();
		requestContext.put(BindingProvider.USERNAME_PROPERTY, username);
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);
		
		return serviceSoapHttpPortObject;
	}

	@SuppressWarnings("rawtypes")
	public static  Object invokeReflectionMethod(Object sdoObject, String sdoFactoryFullClassName,String sdoName,String attributeName, String parameterTypeAsString,
			String parameterValueAsString,String format) 
					throws WebServicesFormatException, InstantiationException, IllegalAccessException, ClassNotFoundException, 
					InvocationTargetException, NoSuchMethodException, ParseException, DatatypeConfigurationException {
		Class[] paramsClasses = new Class[1];
		Object[] paramsValues = new Object[1];
		String methodName="set"+attributeName;

		if (parameterTypeAsString.equalsIgnoreCase("java.lang.String") ||
				parameterTypeAsString.equalsIgnoreCase("java.lang.Integer") ||
				parameterTypeAsString.equalsIgnoreCase("java.lang.Long") ||
				parameterTypeAsString.equalsIgnoreCase("java.sql.Date")
				) {
			boolean isAttributeJABXParameter=isAttributeJABXParameter(sdoObject,attributeName);

			if (isAttributeJABXParameter) {
				Object sdoFactoryObject =ReflectionHelper.newInstance(sdoFactoryFullClassName);
				String jabxMethodName="create"+sdoName+attributeName;
				Object jabxObject =invokeReflectionMethod(sdoFactoryObject,jabxMethodName,parameterTypeAsString,parameterValueAsString,format);
				paramsClasses[0] = JAXBElement.class;
				paramsValues[0] = jabxObject;
			}
			else {
				setArraysReflectionMethod(paramsClasses,paramsValues,parameterTypeAsString,parameterValueAsString,format);
			}
		}
		Object result = ReflectionHelper.invokeMethod(sdoObject, methodName, paramsClasses, paramsValues);
		return result;
	}

	@SuppressWarnings("rawtypes")
	private static boolean isAttributeJABXParameter(Object sdoObject, String attributeName) throws WebServicesFormatException {
		Method[] allMethods = sdoObject.getClass().getDeclaredMethods();
		//System.out.println("allMethods size:"+allMethods.length);
		String methodToFind="set"+attributeName;
		//System.out.println("method to Find:"+methodToFind);
		for (Method method : allMethods) {
			//System.out.println("method:"+method.getName());
			if (!method.getName().equalsIgnoreCase(methodToFind)) {
				continue;
			}
			//System.out.println("Found it!");
			Class<?>[] pType = method.getParameterTypes();
			if (pType.length!=1) {
				throw new WebServicesFormatException("Internal error: the method for the attribute name has more than one parammeter!\n"+
						" - method name: '"+method+"'\n"+
						" - number of parameters: "+pType.length+"\n"+
						" - parameters: "+Arrays.asList(pType));
			}
			Class parameterClass=pType[0];
			//Type[] gpType = m.getGenericParameterTypes();
			//System.out.println("parameterClass: "+parameterClass);
			if (parameterClass==javax.xml.bind.JAXBElement.class) {
				//System.out.println("Found a JABX parameter!");
				return true;
			}
		}
		//System.out.println("Not a JABX parameter!");
		return false;
	}

	@SuppressWarnings("rawtypes")
	private static Object invokeReflectionMethod(Object sdoObject,String methodName, String parameterTypeAsString,
			String parameterValueAsString,String format) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			ParseException, WebServicesFormatException, DatatypeConfigurationException {
		Class[] paramsClasses = new Class[1];
		Object[] paramsValues = new Object[1];
		setArraysReflectionMethod(paramsClasses,paramsValues,parameterTypeAsString,parameterValueAsString,format);
		Object object =ReflectionHelper.invokeMethod(sdoObject, methodName, paramsClasses, paramsValues);
		return object;
	}

	@SuppressWarnings("rawtypes")
	private static void setArraysReflectionMethod(Class[] paramsClasses,Object[] paramsValues, String parameterTypeAsString,
			String parameterValueAsString,String format) throws ParseException, WebServicesFormatException, DatatypeConfigurationException {
		if (parameterTypeAsString.equalsIgnoreCase("java.lang.String")) {
			paramsClasses[0] = String.class;
			paramsValues[0] = parameterValueAsString;
		}
		else
			if (parameterTypeAsString.equalsIgnoreCase("java.lang.Integer")) {
				paramsClasses[0] = Integer.class;
				paramsValues[0] = Integer.valueOf(parameterValueAsString);
			}
			else
				if (parameterTypeAsString.equalsIgnoreCase("java.lang.Long")) {
					paramsClasses[0] = Long.class;
					paramsValues[0] = Long.valueOf(parameterValueAsString);
				}
				else
					if (parameterTypeAsString.equalsIgnoreCase("java.sql.Date")) {
						if (format==null) {
							throw new WebServicesFormatException("Incorrect Navigation, you must specify a Date format for this field.");
						}
						DateFormat df = new StrictSimpleDateFormat(format);
						//System.out.println("parameterValueAsString:"+parameterValueAsString);
						//df.setLenient(false);
						Date dob = df.parse(parameterValueAsString);
						if (dob==null) {
							throw new WebServicesFormatException("Not exact format match!");
						}
						//System.out.println("dob:"+dob.toString());
					    GregorianCalendar c = new GregorianCalendar();
					    c.setTimeInMillis(dob.getTime());
					    XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
					   // System.out.println("xmlGregorianCalendar:"+xmlGregorianCalendar.toString());
						paramsClasses[0] = XMLGregorianCalendar.class;
						paramsValues[0] = xmlGregorianCalendar;
					}
	}
	
	@SuppressWarnings("rawtypes")
	public static void invokeWebService(Worker worker,String domainURLAsString,String username,String password,String endpointPrefixUrl,String endPointServletName
			,String sdoPackageName,String sdoName,String serviceMethodName,List<MethodParameterType> methodParametersList,
			String jdbcString,String dbUserName,String dbPassword) throws Exception {
		String endpointUrl = domainURLAsString+"/"+endpointPrefixUrl+"/"+endPointServletName;
		String serviceSoapHttpPortName="get"+endPointServletName+"SoapHttpPort";
		String serviceClassName=sdoPackageName+"."+endPointServletName+"_Service";
		
		worker.println("domainURLAsString: "+domainURLAsString);
		worker.println("username: "+username);
		//worker.println("password: "+password); // TODO: to comment once development is complete
		worker.println("endpointPrefixUrl: "+endpointPrefixUrl);
		worker.println("endPointServletName: "+endPointServletName);
		worker.println("sdoPackageName: "+sdoPackageName);
		worker.println("sdoName: "+sdoName);
		worker.println("serviceMethodName: "+serviceMethodName);
		
		Object serviceSoapHttpPortObject=WebServicesUtils.getServiceObject(endpointUrl,username,password,serviceClassName,serviceSoapHttpPortName);
		
		List<Class> paramTypes=new ArrayList<Class>();
		List<Object> paramValues=new ArrayList<Object>();

		worker.println("Method parameters count: "+methodParametersList.size());
		for (MethodParameterType methodParameter:methodParametersList) {
			String methodVariableName=methodParameter.getVariableName();
			String methodVariableType=methodParameter.getVariableType();
			worker.println("Method parameter name: '"+methodVariableName+"' type: '"+methodVariableType+"'");

			Object sdoObject = ReflectionHelper.newInstance(methodVariableType);	
			paramTypes.add(sdoObject.getClass());
			
			ObjectParametersType objectParametersType=methodParameter.getObjectParameters();
			if (objectParametersType!=null) {
				paramValues.add(sdoObject);
				String sdoFactoryClassName=sdoPackageName+".ObjectFactory";
				
				List<ObjectParameterType> objectParametersList=Arrays.asList(objectParametersType.getObjectParameterArray());
				worker.println("Object parameters count: "+objectParametersList.size());
				for (ObjectParameterType objectParameter:objectParametersList) {
					String objectVariableName=objectParameter.getVariableName();
					com.erapidsuite.configurator.navigation0005.ObjectParameterType.VariableType.Enum objectVariableType=objectParameter.getVariableType();
					String objectVariableFormat=objectParameter.getVariableFormat();
					worker.println("Object parameter name: '"+objectVariableName+"' type: '"+objectVariableType+"' format: '"+objectVariableFormat+"'");

					ObjectParameterValueType objectParameterValue=objectParameter.getObjectParameterValue();
					String nodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(worker.getBatchInjectionTracker(),objectParameterValue,"",false);
					worker.println("nodeValue: '"+nodeValue+"'");
					
					try{
						// TODO
						//nodeValue=applySubstitutions(objectParameter,nodeValue);
						
						KbValueToDBValueConversionSQLQueryType kbValueToDBValueConversionSQLQuery=objectParameter.getKbValueToDBValueConversionSQLQuery();
						if (kbValueToDBValueConversionSQLQuery!=null) {

							nodeValue=getDBValue(worker,kbValueToDBValueConversionSQLQuery,nodeValue,jdbcString,dbUserName,dbPassword); 
							worker.println("nodeValue (After DB query): '"+nodeValue+"'");
						}
						WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,objectVariableName,objectVariableType.toString(),nodeValue,objectVariableFormat);
					}
					catch(NumberFormatException e) {
						String msg=getFormatErrorMsg(worker,e,objectParameter,nodeValue,"Number");
						throw new Exception(msg);
					}
					catch(java.text.ParseException e) {
						String msg=getFormatErrorMsg(worker,e,objectParameter,nodeValue,objectVariableFormat);
						throw new Exception(msg);
					}
					catch(WebServicesFormatException e) {
						String msg=getFormatErrorMsg(worker,e,objectParameter,nodeValue,objectVariableFormat);
						throw new Exception(msg);
					}
					catch(WebServicesKBtoDBConversionException e) {
						String msg=getKBtoDBConversionErrorMsg(worker,e,objectParameter,nodeValue);
						throw new Exception(msg);
					}
					catch(WebServicesSQLQueryException e) {
						String msg=getSQLErrorMsg(worker,e,objectParameter);
						throw new Exception(msg);
					}
					catch(WebServicesSubstitutionsException e) {
						String msg=getSubstitutionsErrorMsg(worker,e,objectParameter,nodeValue);
						throw new Exception(msg);
					}
				}
			}
			else {
				MethodParameterValueType methodParameterValue=methodParameter.getMethodParameterValue();
				
				String nodeValue=InjectUtils.replaceNodeValueInventoryAndColumnNamesByValue(worker.getBatchInjectionTracker(),methodParameterValue,"",false);
				worker.println("nodeValue: '"+nodeValue+"'");
				
				paramValues.add(nodeValue);
			}			
		}
		Class[] classArray = new Class[paramTypes.size()];
		paramTypes.toArray(classArray);
		try{
			ReflectionHelper.invokeMethod(serviceSoapHttpPortObject, serviceMethodName, classArray, paramValues.toArray());
		}
		catch(InvocationTargetException  e) {
			worker.printStackTrace(e);
			Throwable cause = e.getCause();
			worker.printStackTrace(cause);
			throw new Exception(cause.getMessage());
		}
	}
	
	/*
	private static String applySubstitutions(ObjectParameterType objectParameter,String sourceValue) throws WebServicesSubstitutionsException {
		SubstitutionsType substitutions=objectParameter.getSubstitutions();
		if (substitutions!=null) {
			SubstitutionType[] substitutionsArray=substitutions.getSubstitutionArray();
			List<String> lov=new ArrayList<String>();
			for (SubstitutionType substitution:substitutionsArray) {
				String substitutionSource=substitution.getSource();
				lov.add(substitutionSource);
				if (substitutionSource.equalsIgnoreCase(sourceValue)) {
					return substitution.getTarget();
				}
			}
			throw new WebServicesSubstitutionsException("No substitution found in the allowed List Of Values: "+lov);
		}
		return sourceValue;
	}
	*/

	private static String getSubstitutionsErrorMsg(Worker worker,WebServicesSubstitutionsException e,ObjectParameterType objectParameter,String nodeValue) {
		worker.printStackTrace(e);
		String msg="Invalid value '"+nodeValue+"'";
		ValueKBType[] array=objectParameter.getObjectParameterValue().getValueKBArray();
		if (array!=null && array.length>0) {
			ValueKBType valueKBType=array[0];
			msg=msg+" for the column '"+valueKBType.getColumnName()+"'.";
		}
		msg=msg+" "+e.getMessage();		
		return msg;
	}
	
	private static String getFormatErrorMsg(Worker worker,Throwable e,ObjectParameterType objectParameter,String nodeValue,String objectVariableFormat) {
		worker.printStackTrace(e);
		String msg="Wrong format for the value '"+nodeValue+"'";
		ValueKBType[] array=objectParameter.getObjectParameterValue().getValueKBArray();
		if (array!=null && array.length>0) {
			ValueKBType valueKBType=array[0];
			msg=msg+" for the column '"+valueKBType.getColumnName()+"'.";
		}
		if (objectVariableFormat!=null && !objectVariableFormat.isEmpty()) {
			msg=msg+" Expected format: '"+objectVariableFormat+"' !";
		}		
		return msg;
	}
	
	private static String getKBtoDBConversionErrorMsg(Worker worker,Throwable e,ObjectParameterType objectParameter,String nodeValue) {
		worker.printStackTrace(e);
		String msg="Incorrect value '"+nodeValue+"'";
		ValueKBType[] array=objectParameter.getObjectParameterValue().getValueKBArray();
		if (array!=null && array.length>0) {
			ValueKBType valueKBType=array[0];
			msg=msg+" for the column '"+valueKBType.getColumnName()+"'.";
		}
		msg=msg+" "+e.getMessage();	
		return msg;
	}	

	private static String getSQLErrorMsg(Worker worker,Throwable e,ObjectParameterType objectParameter) {
		worker.printStackTrace(e);
		ValueKBType[] array=objectParameter.getObjectParameterValue().getValueKBArray();
		String msg="Internal error";
		if (array!=null && array.length>0) {
			ValueKBType valueKBType=array[0];
			msg=msg+" for Column '"+valueKBType.getColumnName()+"'.";
		}
		msg=msg+" "+e.getMessage();	
		return msg;
	}
	
	private static String getDBValue(Worker worker,KbValueToDBValueConversionSQLQueryType kbValueToDBValueConversionSQLQuery,String parameterValue,
			String jdbcString,String dbUserName,String dbPassword) throws Exception {
		QueryType query=kbValueToDBValueConversionSQLQuery.getQuery();
		
		String nodeValue=((SimpleValue)((XmlObject)query)).getStringValue();
		if (nodeValue==null || nodeValue.trim().isEmpty()) {
			throw new Exception("Invalid navigation: <QueryType> must contain a text node with the actual Query to run!");
		}
		BigInteger columnPosition=query.getDbValueColumnPosition();
		String queryWhereClauseParameter=kbValueToDBValueConversionSQLQuery.getQueryWhereClauseParameter();
		
		String sqlQuery=nodeValue.trim();
		if (queryWhereClauseParameter!=null) {
			sqlQuery=sqlQuery+" "+queryWhereClauseParameter.trim();
			worker.println("queryWhereClauseParameter: '"+queryWhereClauseParameter.trim()+"'");
		}
		sqlQuery=sqlQuery.replaceAll("##RAPID_PARAMETER##",parameterValue);
		worker.println("sql query to execute: '"+sqlQuery+"'");
		
		return executeQuery(worker,jdbcString,dbUserName,dbPassword,sqlQuery,columnPosition.intValue());
	}
	
	private static String executeQuery(Worker worker,String jdbcString,String dbUserName,
			String dbPassword,String sqlQuery,int resultSetColumnToReturn) 
	throws ClassNotFoundException, WebServicesKBtoDBConversionException, WebServicesDBConnectionException, WebServicesSQLQueryException {
		Connection connection=null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			try{
				connection = DatabaseUtils.getJDBCConnection(jdbcString,dbUserName,dbPassword);
			}
			catch(SQLException e) {
				worker.printStackTrace(e);
				String msg="Unable to connect to the Database. Please verify your information in the Applications panel. Error Message: "+e.getMessage();
				throw new WebServicesDBConnectionException(msg);
			}
			try{
				statement= connection.prepareStatement(sqlQuery);
				statement.execute();
				resultSet=statement.executeQuery();
				if ( resultSet.next() )	{
					String result = resultSet.getString(resultSetColumnToReturn);
					return result;
				}
				else {
					throw new WebServicesKBtoDBConversionException("Unable to find the Database Value (ID). Query: '"+sqlQuery+"'");
				}
			}
			catch(SQLException e) {
				worker.printStackTrace(e);
				throw new WebServicesSQLQueryException("Invalid query: '"+sqlQuery+"'. Error message: "+e.getMessage());
			}
		}
		finally
		{
			DirectConnectDao.closeQuietly(resultSet);
			DirectConnectDao.closeQuietly(statement);
			DirectConnectDao.closeQuietly(connection);
		}
	}
	
}
