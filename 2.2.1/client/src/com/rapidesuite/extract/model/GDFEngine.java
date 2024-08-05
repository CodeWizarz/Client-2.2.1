package com.rapidesuite.extract.model;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;

public class GDFEngine {

	private String dynamicSQLReportPath;
	private String getValueSetTableNonSQLReportPath;
	private WebServiceInfo reportWebServiceInfo;
	private ReportService reportService;
	private Map<String,String> concatenatedValueSetKeyToValueMap;
	
	public GDFEngine(WebServiceInfo reportWebServiceInfo,ReportService reportService,String dynamicSQLReportPath,String getValueSetTableNonSQLReportPath) {
		this.reportWebServiceInfo=reportWebServiceInfo;
		this.reportService=reportService;
		this.dynamicSQLReportPath=dynamicSQLReportPath;
		this.getValueSetTableNonSQLReportPath=getValueSetTableNonSQLReportPath;
		concatenatedValueSetKeyToValueMap=new HashMap<String,String>();
	}
	
	/*
	 *  LOGIC:
	 *  
	 *  1. Run a dynamic query to retrieve the CONTEXT_CODE then CONTEXT_VALUE_X (all the GDF attributes for CONTEXT_VALUE_1 then 2...)
	 *  2. loop all the rows returned by the SQL query (containing all rows which contains at least one value in the GDF fields or in the context code field)
	 *  	for every record:
	 *  	2.01. if the CONTEXT_CODE is null:
	 *  		 then FINISHED
	 *  		 else
	 *  			- translate the value stored in the context code to a 'Name' => add 1 row 
	 *  			- Iterate thru all the CONTEXT elements for that context code and apply Value Sets if any
	 *  				=> 1 row per CONTEXT element
	 *  
	 *  end result:
	 *  	- 1 row returned for each GDF field used (different logic than EBS where we had 1 row but many columns)
	 */
	public List<ExtractDataRow> apply(ExtractInventoryRecord extractInventoryRecord,List<ExtractFlexfieldTempRow> extractFlexfieldTempRowList) throws Exception {
		List<ExtractDataRow> toReturn=new ArrayList<ExtractDataRow>();
		
		GDFSQLElement gdfSQLElement=getGDFSQLElement(extractInventoryRecord.getDataSet().getSqlQuery());
		
		String contextPromptName=getContextPromptName(dynamicSQLReportPath,reportWebServiceInfo,reportService,gdfSQLElement.getGDFCode());
		Map<String, String> contextCodeToContextValueMap=getContextCodeToContextValueMap(dynamicSQLReportPath,reportWebServiceInfo,reportService,
				gdfSQLElement.getGDFCode());
		GDFStructure gdfStructure=getRawGDFStructure(dynamicSQLReportPath,reportWebServiceInfo,reportService,gdfSQLElement.getGDFCode());
		
		Inventory inventory=extractInventoryRecord.getInventory();
		ExtractDataRow extractDataRow=null;
		for (ExtractFlexfieldTempRow extractFlexfieldTempRow:extractFlexfieldTempRowList) {
			String contextCode=extractFlexfieldTempRow.getContextCode();
			//FileUtils.println("#### apply, contextCode:"+contextCode);
			
			List<String> fkValues=DFFEngine.getForeignKeyValues(inventory,extractFlexfieldTempRow);
			//FileUtils.println("apply, fkValues:"+fkValues);
			
			// GDF context prompt name and value:
			String contextValue=contextCodeToContextValueMap.get(contextCode);
			extractDataRow=createNewRecord(fkValues,extractFlexfieldTempRow,contextPromptName,contextValue);
			toReturn.add(extractDataRow);
			
			List<GDFElement> contextRelativeGDFElements=gdfStructure.getContextCodeToGDFElementsMap().get(contextCode);
			//FileUtils.println("apply, contextRelativeGDFElements:"+contextRelativeGDFElements.size());
			if (contextRelativeGDFElements==null || contextRelativeGDFElements.isEmpty()) {
				continue;
			}
			
			// context relative rows:
			List<ExtractDataRow> contextRecordsList=new ArrayList<ExtractDataRow>();
			for (GDFElement gdfElement:contextRelativeGDFElements) {
				extractDataRow=createNewRecord(fkValues,extractFlexfieldTempRow,gdfElement);
				if (extractDataRow!=null){
					contextRecordsList.add(extractDataRow);
				}
			}
			toReturn.addAll(contextRecordsList);
		}
		return toReturn;
	}
	
	private ExtractDataRow createNewRecord(List<String> fkValues,ExtractFlexfieldTempRow extractFlexfieldTempRow,String fieldName,
			String fieldValue) throws Exception {
		ExtractDataRow extractDataRow=new ExtractDataRow();
		
		DFFEngine.setGenericFields(extractDataRow,extractFlexfieldTempRow);
		
		List<String> dataValues=new ArrayList<String>();
		dataValues.addAll(fkValues);
		dataValues.add(fieldName);
		dataValues.add(fieldValue);
		extractDataRow.setDataValues(dataValues.toArray(new String[dataValues.size()]));

		return extractDataRow;
	}
	
	private ExtractDataRow createNewRecord(List<String> fkValues,ExtractFlexfieldTempRow extractFlexfieldTempRow,GDFElement gdfElement) throws Exception {
		ExtractDataRow extractDataRow=new ExtractDataRow();
		
		DFFEngine.setGenericFields(extractDataRow,extractFlexfieldTempRow);
		
		List<String> dataValues=new ArrayList<String>();
		dataValues.addAll(fkValues);
		String name=gdfElement.getPrompt();
		dataValues.add(name);
		String columnName=gdfElement.getColumnName();		
		String attributeValue=extractFlexfieldTempRow.getAttributeNameToValueMap().get(columnName);
		//FileUtils.println("### createNewRecord, attributeValue:"+attributeValue);
		if (attributeValue==null || attributeValue.isEmpty()) {
			// This means no data setup in the form for that field.
			return null;
		}
		if (gdfElement.getValueSetCode()!=null) {
			String convertedAttributeValue=getAttributeValueAfterApplyValueSet(reportWebServiceInfo,reportService,getValueSetTableNonSQLReportPath,
					gdfElement.getValueSetCode(),attributeValue);
			dataValues.add(convertedAttributeValue);
			//FileUtils.println("createNewRecord, convertedAttributeValue:"+convertedAttributeValue);
		}
		else {
			dataValues.add(attributeValue);
		}
		dataValues.add(attributeValue);
		
		extractDataRow.setDataValues(dataValues.toArray(new String[dataValues.size()]));

		return extractDataRow;
	}
	
	public String getAttributeValueAfterApplyValueSet(
			WebServiceInfo reportWebServiceInfo,ReportService reportService,String getValueSetTableNonSQLReportPath,String valueSetCode,
			String attributeValue)  {
		String key=valueSetCode+"###"+attributeValue;
		try{
			if (valueSetCode==null || valueSetCode.isEmpty()) {
				return attributeValue;
			}
			String cachedValue=concatenatedValueSetKeyToValueMap.get(key);
			if (cachedValue!=null) {
				return cachedValue;
			}
			byte[] rawValueSetTableData=DFFEngine.getRawValueSetTableData(reportWebServiceInfo,reportService,getValueSetTableNonSQLReportPath,valueSetCode,attributeValue);
			String valueSetValue=DFFEngine.getValueSetValue(rawValueSetTableData);
			concatenatedValueSetKeyToValueMap.put(key,valueSetValue);
			
			return valueSetValue;
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			FileUtils.println("getAttributeValueAfterApplyValueSet, valueSetCode: '"+valueSetCode+"' attributeValue: '"+attributeValue+"'");
			concatenatedValueSetKeyToValueMap.put(key,attributeValue);
		}
		// If there are some issues to run the Value set query (example the query definition is invalid) or it does not return any data
		// then we return blank.
		return attributeValue;
	}
	
	private GDFSQLElement getGDFSQLElement(String sqlQuery) throws Exception {
		String[] lines=sqlQuery.split(ExtractConstants.END_OF_LINE);
		GDFSQLElement gdfSQLElement=new GDFSQLElement();
		for (String line:lines) {
			int indexOf=line.indexOf(ExtractConstants.GDF_LOGIC_TO_APPLY);
			if (indexOf!=-1) {
				int indexOfEqualSign=line.indexOf("=");
				if (indexOfEqualSign==-1) {
					throw new Exception("Missing '=' sign in the SQL query when applying the GDF logic!");
				}
				String gdfCode=line.substring(indexOfEqualSign+1);
				gdfSQLElement.setGDFCode(gdfCode);
				return gdfSQLElement;
			}
		}
		 throw new Exception("Cannot find "+ExtractConstants.GDF_LOGIC_TO_APPLY+" in the SQL query.");
	}
	
	private String getContextPromptName(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService,
			String gdfCode) throws Exception {	
			List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
			
			String sqlQuery=
			" Select prompt From Fnd_Df_Segments_Vl  Where Descriptive_Flexfield_Code = '"+gdfCode+"' and context_code='Context Data Element'";
	 
			ParameterNameValue parameterNameValue=new ParameterNameValue();
			parametersList.add(parameterNameValue);
			parameterNameValue.setName("param1");
			parameterNameValue.setValue(sqlQuery);
			
			//FileUtils.println("getContextPromptName, sqlQuery: '"+sqlQuery+"'");
			byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//			String data=new String(outputBytes,"ISO-8859-1");
			//FileUtils.println("data:"+data);
			
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(outputBytes);
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
		        				
		        				String fieldName="prompt";
		    					return ExtractUtils.getDataFromBIOutput(element,fieldName);
		        			}
		        		}
		        	}
		        	finally {
		        		IOUtils.closeQuietly(pw);
		        	}
		        }
			}
	        throw new Exception("Cannot find the GDF context prompt name");
		}

	/*
	 * 
	 *  TO DEBUG:
	 * 
	  select DESCRIPTIVE_FLEXFIELD_NAME,DESCRIPTIVE_FLEX_CONTEXT_CODE from FND_DESCR_FLEX_CONTEXTS_VL where DESCRIPTIVE_FLEX_CONTEXT_NAME = 'Receivables Batch Source Information for Brazil';
	 */
	private Map<String, String> getContextCodeToContextValueMap(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService,
		String gdfCode) throws Exception {	
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String sqlQuery=
		"Select Descriptive_Flex_Context_Code,Descriptive_Flex_Context_Name From Fnd_Descr_Flex_Contexts_Vl "+
		"Where Descriptive_Flexfield_Name = '"+gdfCode+"' and global_flag='N'";
 
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(sqlQuery);
		
		//FileUtils.println("getGDFContextCode, sqlQuery: '"+sqlQuery+"'");
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
		//String data=new String(outputBytes,"ISO-8859-1");
		//FileUtils.println("data:"+data);
		
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(outputBytes);
		Document document = builder.parse(input);
		
		Map<String,String> contextCodeToContextNameMap=new HashMap<String,String>();
		
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
	        				
	        				String fieldName="Descriptive_Flex_Context_Code";
	    					String contextCode=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					
	    					fieldName="Descriptive_Flex_Context_Name";
	    					String contextValue=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					
	    					contextCodeToContextNameMap.put(contextCode, contextValue);
	        			}
	        		}
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
		}
        return contextCodeToContextNameMap;
	}
	
	
	/*
	 *  TO DEBUG:
	 * 
 Select End_User_Column_Name,Application_Column_Name,FLEX_VALUE_SET_ID From Fnd_Descr_Flex_Column_Usages Where Descriptive_Flexfield_Name = 'JG_FA_CATEGORY_BOOKS' And Descriptive_Flex_Context_Code = 'JE_IT_ASSET_MAINTENANCE'
 and ENABLED_FLAG='Y' order by COLUMN_SEQ_NUM;
 	 */
	private GDFStructure getRawGDFStructure(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService,
		String descFlexfieldName) throws Exception {	
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String sqlQuery=" Select Descriptive_Flex_Context_Code,End_User_Column_Name,Application_Column_Name,FLEX_VALUE_SET_ID,"+
		"(select value_set_code From Fnd_Vs_Value_Sets where value_set_id =FLEX_VALUE_SET_ID) VALUE_SET_CODE From "+
		"Fnd_Descr_Flex_Column_Usages Where Descriptive_Flexfield_Name = '"+descFlexfieldName+"' and ENABLED_FLAG='Y' order by Descriptive_Flex_Context_Code,COLUMN_SEQ_NUM";
					
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(sqlQuery);
		
		//FileUtils.println("getRawGDFStructure, descFlexfieldName: '"+descFlexfieldName+"' sqlQuery: '"+sqlQuery+"'");
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//FileUtils.println("data:"+data);
		
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(outputBytes);
		Document document = builder.parse(input);
		
		for(String rowTag:ExtractUtils.BI_PUBLISHER_ROW_TAG_NAMES) {
			PrintWriter pw=null;
	        NodeList nodeList = document.getElementsByTagName(rowTag);
	        int recordCount=nodeList.getLength();
	        GDFStructure gdfStructure=new GDFStructure();
	        Map<String, List<GDFElement>> contextCodeToGDFElementsMap=gdfStructure.getContextCodeToGDFElementsMap();
	        if (recordCount>0)
	        {
	        	try{        		
	        		for (int i = 0; i < nodeList.getLength(); i++) {
	        			Node node = nodeList.item(i);
	        			if (node.getNodeType() == Node.ELEMENT_NODE) {
	        				Element element = (Element) node;
	
	        				GDFElement gdfElement=new GDFElement();
	        				
	        				String fieldName="Descriptive_Flex_Context_Code";
	    					String value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					List<GDFElement> contextRelatedGDFElements=contextCodeToGDFElementsMap.get(value);
	    					if (contextRelatedGDFElements==null) {
	    						contextRelatedGDFElements=new ArrayList<GDFElement>();
	    						contextCodeToGDFElementsMap.put(value, contextRelatedGDFElements);
	    					}
	    					contextRelatedGDFElements.add(gdfElement);
	    					    					
	        				fieldName="End_User_Column_Name";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					gdfElement.setPrompt(value);
	    					
	    					fieldName="Application_Column_Name";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					if (value.isEmpty()) {
	    						throw new Exception("Missing context code in the DFF Structure.");
	    					}
	    					gdfElement.setColumnName(value);
	    					    					
	    					fieldName="FLEX_VALUE_SET_ID";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					gdfElement.setValueSetId(value);
	    					
	    					fieldName="VALUE_SET_CODE";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					gdfElement.setValueSetCode(value);
	        			}
	        		}
	        		return gdfStructure;
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
		}
        throw new Exception("No records found for the GDF query!");
	}
	
}
