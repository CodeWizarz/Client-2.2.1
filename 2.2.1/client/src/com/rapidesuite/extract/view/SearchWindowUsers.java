package com.rapidesuite.extract.view;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;
import com.rapidesuite.extract.model.ParameterNameValue;
import com.rapidesuite.snapshot.model.OracleUser;
import com.rapidesuite.snapshot.view.SearchWindow;

public class SearchWindowUsers extends SearchWindow{

	private FilterUserPanel filterUserPanel;
	private boolean isLastUpdateBy;
	private static final int MAX_QUERY_COUNT=20;

	public SearchWindowUsers(JFrame rootFrame,FilterUserPanel filterUserPanel,String title,Map<String,Object> selectedResultsKeyToObjectMap,boolean isLastUpdateBy) {
		super(rootFrame, title ,null,selectedResultsKeyToObjectMap);
		this.filterUserPanel=filterUserPanel;
		this.isLastUpdateBy=isLastUpdateBy;
		dialog.setVisible(true);
	}

	@Override
	public Map<String,Object> search(String inputValue)  throws Exception {	
//		if (inputValue.equals(SearchWindow.WILDCARD_KEYWORD) ) {
//			throw new Exception("You must use \""+SearchWindow.WILDCARD_KEYWORD+"\" with other characters. Search ALL is not allowed!");
//		}
		String dynamicSQLReportPath=filterUserPanel.getFiltersPanel().getExtractInventoryGridSelectionPanel().getExtractMain().getApplicationInfoPanel().getDynamicNonSQLReportPath();
		WebServiceInfo reportWebServiceInfo=filterUserPanel.getFiltersPanel().getExtractInventoryGridSelectionPanel().getExtractMain().getApplicationInfoPanel().getReportWebServiceInfo();
		ReportService reportService=filterUserPanel.getFiltersPanel().getExtractInventoryGridSelectionPanel().getExtractMain().getApplicationInfoPanel().getReportService();
		
		byte[] rawDataUsersCount=getRawDataUsersCount(dynamicSQLReportPath,reportWebServiceInfo,reportService,inputValue);
		int totalCount=getDataUsersCount(rawDataUsersCount);
		if (totalCount>MAX_QUERY_COUNT) {
			warningLabel.setText(" "+totalCount+" values were found but the maximum allowed selection is "+MAX_QUERY_COUNT+", so "+
					"please refine your search or select only "+MAX_QUERY_COUNT+" values!");
		}

		Map<String, OracleUser> oracleUserMap=new HashMap<String,OracleUser>();
		byte[] rawData=getRawDataUsers(dynamicSQLReportPath,reportWebServiceInfo,reportService,inputValue);
		initUsers(oracleUserMap,rawData);
		
		Map<String,Object> toReturn = new TreeMap<String,Object>();
		toReturn.putAll(oracleUserMap);
		return toReturn;
	}

	public void apply(Map<String, Object> selectedResultsKeyToObjectMap) {
		if (selectedResultsKeyToObjectMap.size()>MAX_QUERY_COUNT) {
			statusLabel.setText("The maximum number of values that is allowed to select is "+MAX_QUERY_COUNT+" . Please remove some selected values!");
			statusLabel.setBackground(Color.decode("#ee3630"));
			return;
		}
		filterUserPanel.getFiltersPanel().getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
		filterUserPanel.setSelectedMap(isLastUpdateBy,selectedResultsKeyToObjectMap);
		dialog.dispose();
	}	
	
	private byte[] getRawDataUsersCount(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService, String inputValue) throws Exception {			
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String value="select COUNT(*) RES_CNT from PER_USERS where upper(Username) like upper('"+inputValue+"')";
		
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(value);
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//FileUtils.println("data:"+data);
		
		return outputBytes;
	}
	
	private static int getDataUsersCount(byte[] rawDataUsersCount) throws Exception {
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(rawDataUsersCount);
		Document document = builder.parse(input);
		
		for(String rowTag:ExtractUtils.BI_PUBLISHER_ROW_TAG_NAMES) {
			PrintWriter pw=null;
	        NodeList nodeList = document.getElementsByTagName(rowTag);
	        int recordCount=nodeList.getLength();
	        if (recordCount>0)
	        {
	        	try{        		
	        		for (int i = 0; i < nodeList.getLength(); i++) {
	        			Node node = nodeList.item(i);
	        			if (node.getNodeType() == Node.ELEMENT_NODE) {
	        				Element element = (Element) node;
	
	        				String fieldName="RES_CNT";
	    					String value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					int usersCnt=Integer.valueOf(value).intValue();
	    					
	    					return usersCnt;
	        			}
	        		}
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
		}
        return 0;
	}
	
	private byte[] getRawDataUsers(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService, String inputValue) 
			throws Exception {			
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
//		String value="select * from ("+
//				"Select Username RES_USER_NAME,User_Id RES_USER_ID From Per_Users where upper(Username) like upper('"+inputValue+"') Order By Username"+
//				") where rownum <= "+MAX_QUERY_COUNT;
		String value = "Select Username RES_USER_NAME,User_Id RES_USER_ID From Per_Users where upper(Username) like upper('"+inputValue+"') Order By Username";
		
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(value);
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
//		FileUtils.println("data:"+data);
		
		return outputBytes;
	}
	
	private static void initUsers(Map<String,OracleUser> oracleUserMap,byte[] rawData) throws Exception {
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(rawData);
		Document document = builder.parse(input);
		
		for(String rowTag:ExtractUtils.BI_PUBLISHER_ROW_TAG_NAMES) {
			PrintWriter pw=null;
	        NodeList nodeList = document.getElementsByTagName(rowTag);
	        int recordCount=nodeList.getLength();
	        if (recordCount>0)
	        {
	        	try{        		
	        		for (int i = 0; i < nodeList.getLength(); i++) {
	        			Node node = nodeList.item(i);
	        			if (node.getNodeType() == Node.ELEMENT_NODE) {
	        				Element element = (Element) node;
	
	        				String fieldName="RES_USER_NAME";
	    					String userName=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					if ( userName.isEmpty()) {
	    						continue;
	    					}
	    					
	    					fieldName="RES_USER_ID";
	    					String userId=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					Long userIdLong=Long.valueOf(userId);
	    					OracleUser oracleUser=new OracleUser( userName,userIdLong);
	    					oracleUserMap.put( userName, oracleUser);
	        			}
	        		}
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
		}
	}
	
}