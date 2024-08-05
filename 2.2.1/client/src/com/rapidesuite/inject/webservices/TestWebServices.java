package com.rapidesuite.inject.webservices;

import oracle.jbo.common.service.ReflectionHelper;

public class TestWebServices {

	@SuppressWarnings("rawtypes")
	public static void testCreateCalendar() throws Exception {
		String username = "RAPID1";
		String password = "RSCuser2!";
		
		String endPointServletName="CalendarsService";
		String endpointUrl = "http://fusion01.rapidesuite.com:18603/finGlCalAcc/"+endPointServletName;
		String sdoPackageName="com.oracle.xmlns.apps.financials.generalledger.calendars.accounting.calendarsservice";
		String sdoName="Calendar"; 
		String serviceMethodName="createCalendar";
		
		String serviceSoapHttpPortName="get"+endPointServletName+"SoapHttpPort";
		String serviceClassName=sdoPackageName+"."+endPointServletName+"_Service";
		Object serviceSoapHttpPortObject=WebServicesUtils.getServiceObject(endpointUrl,username,password,serviceClassName,serviceSoapHttpPortName);
		String sdoFactoryClassName=sdoPackageName+".ObjectFactory";
		String sdoClassName=sdoPackageName+"."+sdoName; 
		Object sdoObject = ReflectionHelper.newInstance(sdoClassName);		
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"CalendarStartDate","java.sql.Date","2015-01-01T00:00:00Z","");
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"NonAdjPeriodFreqCode","java.lang.String","MONTH","");
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"UserPeriodSetName","java.lang.String","OD-CAL-API1","");
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"LatestYearStartDate","java.sql.Date","2015-01-01T00:00:00Z","");
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"PeriodSetName","java.lang.String","OD-CAL-API1","");
		
		Class[] paramTypes = new Class[1];
		paramTypes[0] = sdoObject.getClass();
		Object[] paramsValues = new Object[1];
		paramsValues[0] = sdoObject;
		ReflectionHelper.invokeMethod(serviceSoapHttpPortObject, serviceMethodName, paramTypes, paramsValues);
	}
	
	@SuppressWarnings("rawtypes")
	public static  void testCreateLocation() throws Exception {
		String username = "RAPID1";
		String password = "RSCuser2!";
		
		String endPointServletName="LocationService";
		String endpointUrl = "http://fusion01.rapidesuite.com:18619/locationsService/"+endPointServletName;
		String sdoPackageName="com.oracle.xmlns.apps.hcm.locations.locationservice";
		String sdoName="Location"; 
		String serviceMethodName="createLocation";
		
		String serviceSoapHttpPortName="get"+endPointServletName+"SoapHttpPort";
		String serviceClassName=sdoPackageName+"."+endPointServletName+"_Service";
		Object serviceSoapHttpPortObject=WebServicesUtils.getServiceObject(endpointUrl,username,password,serviceClassName,serviceSoapHttpPortName);
		String sdoFactoryClassName=sdoPackageName+".ObjectFactory";
		String sdoClassName=sdoPackageName+"."+sdoName; 
		
		System.out.println("endPointServletName:"+endPointServletName);
		System.out.println("serviceSoapHttpPortName:"+serviceSoapHttpPortName);
		System.out.println("serviceClassName:"+serviceClassName);
		System.out.println("sdoFactoryClassName:"+sdoFactoryClassName);
		System.out.println("sdoClassName:"+sdoClassName);
		
		Object sdoObject = ReflectionHelper.newInstance(sdoClassName);		
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"ActiveStatus","java.lang.String","A","");
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"LocationCode","java.lang.String","ODTEST","");
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"RangeStartDate","java.sql.Date","01-Jan-1951","dd-MMM-yyyy");
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"InternalLocationCode","java.lang.String","ODTEST","");
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"SetId","java.lang.Long","0","");
		WebServicesUtils.invokeReflectionMethod(sdoObject,sdoFactoryClassName,sdoName,"Country","java.lang.String","US","");
				
		Class[] paramTypes = new Class[2];
		paramTypes[0] = sdoObject.getClass();
		paramTypes[1] = String.class;
		Object[] paramsValues = new Object[2];
		paramsValues[0] = sdoObject;
		paramsValues[1] = null;
		
		Object res=ReflectionHelper.invokeMethod(serviceSoapHttpPortObject, serviceMethodName, paramTypes, paramsValues);
		System.out.println("res:"+res);
	}



	public static void main(String[] args) throws Exception {
		//testWebServices.testCreateCalendar();
		TestWebServices.testCreateLocation();
	}

}
