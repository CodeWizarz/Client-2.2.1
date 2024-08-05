package com.rapidesuite.extract;

import java.io.File;

import com.oracle.xmlns.oxp.service.v2.ArrayOfParamNameValue;
import com.oracle.xmlns.oxp.service.v2.ArrayOfString;
import com.oracle.xmlns.oxp.service.v2.CatalogService;
import com.oracle.xmlns.oxp.service.v2.ParamNameValue;
import com.oracle.xmlns.oxp.service.v2.ParamNameValues;
import com.oracle.xmlns.oxp.service.v2.ReportRequest;
import com.oracle.xmlns.oxp.service.v2.ReportResponse;
import com.oracle.xmlns.oxp.service.v2.ReportService;

public class TestReports {

	public static void main(String[] args) throws Exception {  
		//createAndRunDynamicSQL();
		runDynamicSQL();
	}
	
	private static WebServiceInfo getPrivateCatalogWebServiceInfo(String serviceEndpointUrl) {
    	String username = "FAAdmin";
    	String password = "Oracle123";
		WebServiceInfo webServiceInfo=new WebServiceInfo();
		webServiceInfo.setWebServiceEndpointUrl(serviceEndpointUrl);
		webServiceInfo.setUsername(username);
		webServiceInfo.setPassword(password);
    	return webServiceInfo;
	}
	
	public static void createAndRunDynamicSQL() throws Exception {  
    	WebServiceInfo catalogWebServiceInfo=getPrivateCatalogWebServiceInfo("http://fusion01.rapidesuite.com:18621/xmlpserver/services/v2/CatalogService");
    	WebServiceInfo reportWebServiceInfo=getPrivateCatalogWebServiceInfo("http://fusion01.rapidesuite.com:18621/xmlpserver/services/v2/ReportService");
    	CatalogService catalogService=ExtractUtils.getCatalogService(catalogWebServiceInfo);
    	ReportService reportService=ExtractUtils.getReportService(reportWebServiceInfo);
    	
		String name="dataset-nonsql";
		String objectType="xdmz";
		String objectAbsolutePathURL="/"+name;
		
		String dataSetAbsolutePathURL=objectAbsolutePathURL+".xdm";
		boolean isDataSetExist=ExtractUtils.objectExistBIWebService(catalogWebServiceInfo,catalogService,dataSetAbsolutePathURL);
		if (isDataSetExist) {
			System.out.println("DataSet '"+dataSetAbsolutePathURL+"' already exists, deleting...");
			boolean retValue=ExtractUtils.deleteObjectBIWebService(catalogWebServiceInfo,catalogService,dataSetAbsolutePathURL);
			System.out.println("retValue:"+retValue);
		}
		File archiveFile=new File("D:/RES-REPOSITORY/rapidesuite/programs/trunk/client/EXTRACT/customBIPackages/nonsql/dataset-nonsql/"+name+".xdmz");
		System.out.println("Uploading DataSet archive to '"+objectAbsolutePathURL+"'");
		String returnValue=ExtractUtils.uploadObjectBIWebService(catalogWebServiceInfo,catalogService,objectAbsolutePathURL,archiveFile,objectType);
		System.out.println("returnValue:"+returnValue);
		
		/*
		 * IMPORTANT: MAKE SURE THE DATASET NAME IS THE SAME AS ABOVE IN THE REPORT ARCHIVE !!!!!
		 */
		name="report-nonsql";
		objectType="xdoz";
		objectAbsolutePathURL="/"+name;
		archiveFile=new File("D:/RES-REPOSITORY/rapidesuite/programs/trunk/client/EXTRACT/customBIPackages/nonsql/report-nonsql/"+name+".xdoz");
		
		String reportAbsolutePathURL=objectAbsolutePathURL+".xdo";
		boolean isReportExist=ExtractUtils.objectExistBIWebService(catalogWebServiceInfo,catalogService,reportAbsolutePathURL);
		if (isReportExist) {
			System.out.println("Report '"+reportAbsolutePathURL+"' already exists, deleting...");
			boolean retValue=ExtractUtils.deleteObjectBIWebService(catalogWebServiceInfo,catalogService,reportAbsolutePathURL);
			System.out.println("retValue:"+retValue);
		}
		System.out.println("Uploading Report archive to '"+objectAbsolutePathURL+"'");
		returnValue=ExtractUtils.uploadObjectBIWebService(catalogWebServiceInfo,catalogService,objectAbsolutePathURL,archiveFile,objectType);
		System.out.println("returnValue:"+returnValue);

		String reportObjectAbsolutePathURL="/"+name+".xdo";
		System.out.println("Executing report '"+reportObjectAbsolutePathURL+"'...");		
		
		String sqlQuery="Select code_combination_id From GL_CODE_COMBINATIONS";
		byte[] outputBytes=testRunReportWithParameters(reportWebServiceInfo,reportService,reportObjectAbsolutePathURL,sqlQuery);
		
		String result=new String(outputBytes);
		System.out.println("result: \n'"+result+"'");
	}
	
	public static byte[] testRunReportWithParameters(WebServiceInfo webServiceInfo,ReportService reportService,String reportAbsolutePath,
			String sqlQuery) throws Exception {
		ReportRequest repRequest = new ReportRequest();
		repRequest.setReportAbsolutePath(reportAbsolutePath);
		repRequest.setSizeOfDataChunkDownload(-1);
		
		ArrayOfParamNameValue arrayOfParamNameValue = new 	ArrayOfParamNameValue();
		ParamNameValues paramNameValues = new ParamNameValues();

		ParamNameValue paramNameValue = new ParamNameValue();
		ArrayOfString arrayOfString = new ArrayOfString();
		paramNameValue.setName("param1");
		arrayOfString.getItem().add(sqlQuery);
		paramNameValue.setValues(arrayOfString);
		arrayOfParamNameValue.getItem().add(paramNameValue);

		paramNameValues.setListOfParamNameValues(arrayOfParamNameValue);
		repRequest.setParameterNameValues(paramNameValues);

		ReportResponse reportResponse = reportService.runReport(repRequest, webServiceInfo.getUsername(), webServiceInfo.getPassword());
		ExtractUtils.saveOutput(reportResponse.getReportBytes(),new File("AAA.xml"));
		byte[] baReport = reportResponse.getReportBytes();
		
		return baReport;
	}
	
	public static void runDynamicSQL() throws Exception {  
    	WebServiceInfo reportWebServiceInfo=getPrivateCatalogWebServiceInfo("http://fusion01.rapidesuite.com:18621/xmlpserver/services/v2/ReportService");
    	ReportService reportService=ExtractUtils.getReportService(reportWebServiceInfo);
    		
		String reportObjectAbsolutePathURL="/RapidExtract/58719133/Reports/DYNSQL.xdo";
		System.out.println("Executing report '"+reportObjectAbsolutePathURL+"'...");		
		
		String sqlQuery="Select COM.CODE_COMBINATION_ID KEY,fnd_flex_ext.GET_SEGS('GL',Stri.Key_Flexfield_Code,STRI.STRUCTURE_INSTANCE_NUMBER,COM.CODE_COMBINATION_ID) VALUE"+
		" From Fnd_Kf_Str_Instances_B Stri,Gl_Code_Combinations Com"+
		" WHERE STRI.APPLICATION_ID = 101	AND stri.STRUCTURE_INSTANCE_NUMBER = com.chart_of_accounts_id";
		byte[] outputBytes=testRunReportWithParameters(reportWebServiceInfo,reportService,reportObjectAbsolutePathURL,sqlQuery);
		
		String result=new String(outputBytes);
		System.out.println("result: \n'"+result+"'");
	}
	
}
