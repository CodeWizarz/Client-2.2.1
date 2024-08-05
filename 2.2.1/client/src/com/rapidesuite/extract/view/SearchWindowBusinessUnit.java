package com.rapidesuite.extract.view;

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
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;
import com.rapidesuite.extract.model.BusinessUnit;
import com.rapidesuite.extract.model.ParameterNameValue;
import com.rapidesuite.snapshot.view.SearchWindow;

public class SearchWindowBusinessUnit extends SearchWindow{

	private FilterBusinessUnit filterBusinessUnit;

	public SearchWindowBusinessUnit(JFrame rootFrame,FilterBusinessUnit filterBusinessUnit,String title,Map<String,Object> selectedResultsKeyToObjectMap) {
		super(rootFrame, title ,SearchWindow.WILDCARD_KEYWORD,selectedResultsKeyToObjectMap);
		this.filterBusinessUnit=filterBusinessUnit;
		processActionSearch(SearchWindow.WILDCARD_KEYWORD);
		dialog.setVisible(true);
	}

	@Override
	public Map<String,Object> search(String inputValue)  throws Exception {
		Map<String,BusinessUnit> businessUnitNameToObjectMap=new HashMap<String,BusinessUnit>();
		String dynamicSQLReportPath=filterBusinessUnit.getFiltersPanel().getExtractInventoryGridSelectionPanel().getExtractMain().getApplicationInfoPanel().getDynamicNonSQLReportPath();
		WebServiceInfo reportWebServiceInfo=filterBusinessUnit.getFiltersPanel().getExtractInventoryGridSelectionPanel().getExtractMain().getApplicationInfoPanel().getReportWebServiceInfo();
		ReportService reportService=filterBusinessUnit.getFiltersPanel().getExtractInventoryGridSelectionPanel().getExtractMain().getApplicationInfoPanel().getReportService();
		byte[] rawData=getRawDataBusinessUnits(dynamicSQLReportPath,reportWebServiceInfo,reportService);
		initBusinessUnits(businessUnitNameToObjectMap,rawData);
						
		Map<String,Object> toReturn = new TreeMap<String,Object>();
		toReturn.putAll(businessUnitNameToObjectMap);
		return toReturn;
	}

	public void apply(Map<String, Object> selectedResultsKeyToObjectMap) {
		filterBusinessUnit.getFiltersPanel().getUnappliedFiltersLabel().setText(UIConstants.UNAPPLIED_FILTERS_WARNING);
		filterBusinessUnit.setSelectedMap(selectedResultsKeyToObjectMap);
		dialog.dispose();
	}
	
	private byte[] getRawDataBusinessUnits(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String value="Select BU_NAME RES_NAME,BU_ID RES_BU_ID From FUN_ALL_BUSINESS_UNITS_V  ORDER BY BU_NAME ASC";
		
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(value);
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//FileUtils.println("data:"+data);
		
		return outputBytes;
	}
	
	private static void initBusinessUnits(Map<String,BusinessUnit> businessUnitNameToObjectMap,byte[] rawData) throws Exception {
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
	
	        				String fieldName="RES_NAME";
	    					String buName=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					if (buName.isEmpty()) {
	    						continue;
	    					}
	    					
	    					fieldName="RES_BU_ID";
	    					String buId=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					Long buIdLong=Long.valueOf(buId);
	    					BusinessUnit businessUnit=new BusinessUnit(buName,buIdLong,-1);
	    					businessUnitNameToObjectMap.put(buName, businessUnit);
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