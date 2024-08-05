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
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;

public class DFFEngine {

	private String dynamicSQLReportPath;
	private String getValueSetTableNonSQLReportPath;
	private WebServiceInfo reportWebServiceInfo;
	private ReportService reportService;
	private Map<String,String> concatenatedValueSetKeyToValueMap;
	
	public DFFEngine(WebServiceInfo reportWebServiceInfo,ReportService reportService,String dynamicSQLReportPath,String getValueSetTableNonSQLReportPath) {
		this.reportWebServiceInfo=reportWebServiceInfo;
		this.reportService=reportService;
		this.dynamicSQLReportPath=dynamicSQLReportPath;
		this.getValueSetTableNonSQLReportPath=getValueSetTableNonSQLReportPath;
		concatenatedValueSetKeyToValueMap=new HashMap<String,String>();
	}
	
	/*
	 *  LOGIC:
	 *  
	 *  1. Run a dynamic query to retrieve the order of Attributes used by that Form. Note that the returned result is ordered by
	 *  	GLOBAL then CONTEXT_CODE then CONTEXT_VALUE_X (all the DFF attributes for CONTEXT_VALUE_1 then 2...)
	 *  2. loop all the rows returned by the SQL query (containing all rows which contains at least one value in the DFF fields or in the context code field)
	 *  	for every record:
	 *  	2.0. Iterate thru all the GLOBAL elements and apply Value Sets if any => 1 row per GLOBAL element
	 *  	2.1. if the CONTEXT_CODE is null:
	 *  		 then FINISHED
	 *  		 else
	 *  			- translate the value stored in the context code to a 'Name' => add 1 row 
	 *  			- Iterate thru all the CONTEXT elements for that context code and apply Value Sets if any
	 *  				=> 1 row per CONTEXT element
	 *  
	 *  end result:
	 *  	- 1 row returned for each DFF field used (different logic than EBS where we had 1 row but many columns)
	 *  
	 *  XX_AP1	Aging Periods	XX_AP_CON		RAPID1	21-MAR-16 10.00.27.402000000 AM
		XX_AP1	XX_AP1			5001			RAPID1	21-MAR-16 10.00.27.402000000 AM
		XX_AP1	XX_AP3			Yes				RAPID1	21-MAR-16 10.00.27.402000000 AM
		XX_AP1	XX_AP_CONS3		14498000		RAPID1	21-MAR-16 10.00.27.402000000 AM
		XX_AP1	XX_AP4			Yes				RAPID1	21-MAR-16 10.00.27.402000000 AM
		XX_AP1	XX_AP_CONS4		00000000		RAPID1	21-MAR-16 10.00.27.402000000 AM
		XX_AP1	XX_AP_CONS5		14498000		RAPID1	21-MAR-16 10.00.27.402000000 AM
		XX_AP1	XX_AP_CONS6		14500000		RAPID1	21-MAR-16 10.00.27.402000000 AM
	 *  
	 */
	public List<ExtractDataRow> apply(ExtractInventoryRecord extractInventoryRecord,List<ExtractFlexfieldTempRow> extractFlexfieldTempRowList) throws Exception {
		DFFSQLElement dffSQLElement=getDFFSQLElement(extractInventoryRecord.getDataSet().getSqlQuery());
		DFFStructure dffStructure=new DFFStructure();
		
		List<ExtractDataRow> toReturn=new ArrayList<ExtractDataRow>();
		
		byte[] rawDFFStructure=getRawDFFStructure(dynamicSQLReportPath,reportWebServiceInfo,reportService,
				dffSQLElement.getApplicationId(),dffSQLElement.getDffCode());
		initGlobalAndContextValuesDFFStructure(dffStructure,rawDFFStructure);
		//FileUtils.println("initGlobalAndContextValuesDFFStructure, isEmpty:"+isEmpty);
		/*if (isEmpty) {
			return toReturn;
		}*/
		byte[] rawContextCodeAndValueSetCodeAndPrompt=getRawContextCodeAndValueSetCodeAndPrompt(dynamicSQLReportPath,reportWebServiceInfo,reportService,dffSQLElement.getApplicationId(),dffSQLElement.getDffCode());
		initContextCodePromptDFFStructure(dffStructure,rawContextCodeAndValueSetCodeAndPrompt);
		//FileUtils.println("initContextCodePromptDFFStructure, isEmpty:"+isEmpty);
		/*if (isEmpty) {
			return toReturn;
		}*/
		byte[] rawContextCodeToContextNameMap=getRawContextCodeToContextNameMap(dynamicSQLReportPath,reportWebServiceInfo,reportService,dffSQLElement.getApplicationId(),dffSQLElement.getDffCode());
		initContextCodeToContextNameMapDFFStructure(dffStructure,rawContextCodeToContextNameMap);
				
		Inventory inventory=extractInventoryRecord.getInventory();
		ExtractDataRow extractDataRow=null;
		for (ExtractFlexfieldTempRow extractFlexfieldTempRow:extractFlexfieldTempRowList) {
			String contextCode=extractFlexfieldTempRow.getContextCode();
			//FileUtils.println("#### apply, contextCode:"+contextCode);
			List<String> fkValues=getForeignKeyValues(inventory,extractFlexfieldTempRow);
			//FileUtils.println("apply, fkValues:"+fkValues);
			
			// GLOBAL rows:
			List<DFFElement> globalElementsList=dffStructure.getGlobalElements();
			for (DFFElement dffElement:globalElementsList) {
				extractDataRow=createNewRecord(fkValues,extractFlexfieldTempRow,dffElement);
				if (extractDataRow!=null){
					toReturn.add(extractDataRow);
				}
			}
						
			if (contextCode!=null && !contextCode.isEmpty() ) {
				// context code:
				String name=dffStructure.getContextCodePrompt();
				String contextName=dffStructure.getContextCodeToContextNameMap().get(contextCode);
				//FileUtils.println("apply, initContextCodePromptDFFStructure, contextCode: '"+contextCode+"' name: '"+name+"' contextName: '"+contextName+"'");
				if (contextName==null) {
					extractDataRow=createNewRecordContextCode(dffStructure,fkValues,extractFlexfieldTempRow,name,contextCode);
				}
				else {
					extractDataRow=createNewRecordContextCode(dffStructure,fkValues,extractFlexfieldTempRow,name,contextName);
				}				
				toReturn.add(extractDataRow);
				
				// context relative rows:
				List<DFFElement> contextRelativeDFFElements=dffStructure.getContextCodeToDFFElementsMap().get(contextCode);
				if (contextRelativeDFFElements!=null) {
					for (DFFElement dffElement:contextRelativeDFFElements) {
						extractDataRow=createNewRecord(fkValues,extractFlexfieldTempRow,dffElement);
						if (extractDataRow!=null){
							toReturn.add(extractDataRow);
						}
					}
				}
			}
		}
		return toReturn;
	}
	
	public static List<String> getForeignKeyValues(Inventory inventory,ExtractFlexfieldTempRow extractFlexfieldTempRow) throws Exception {
		List<String> toReturn=new ArrayList<String>();
		List<Field> fieldsUsedForPrimaryKey=inventory.getFieldsUsedForPrimaryKey();
		for (Field field:fieldsUsedForPrimaryKey) {
			if (field.getName().equalsIgnoreCase("Field Name")) {
				break;
			}
			String parentName=field.getParentName();
			if (parentName==null || parentName.isEmpty()) {
				// This is an error as ALL fields before 'Field Name' should be a foreign key to the parent table
				throw new Exception("Invalid Inventory, the field name '"+field.getName()+"' is not associated with the parent table");
			}
			String fieldHashName=field.getNameHash();
			String value=extractFlexfieldTempRow.getForeignKeyHashNameToValueMap().get(fieldHashName);
			if (value==null) {
				throw new Exception("Internal error, cannot find Foreign Key '"+fieldHashName+"' in the data.");
			}
			toReturn.add(value);
		}
		return toReturn;
	}
	
	private ExtractDataRow createNewRecordContextCode(DFFStructure dffStructure, List<String> fkValues,ExtractFlexfieldTempRow extractFlexfieldTempRow,String contextPrompt,String contextName) throws Exception {
		ExtractDataRow extractDataRow=new ExtractDataRow();
		
		setGenericFields(extractDataRow,extractFlexfieldTempRow);	
		
		List<String> dataValues=new ArrayList<String>();
		dataValues.addAll(fkValues);
		dataValues.add(contextPrompt);
		
		String contextCodeValueSetCode=dffStructure.getContextCodeValueSetCode();
		//FileUtils.println("##########createNewRecordContextCode, contextCodeValueSetCode:"+contextCodeValueSetCode+" contextName:"+contextName);
		if (contextCodeValueSetCode==null) {
			dataValues.add(contextName);
		}
		else {
			String convertedAttributeValue=getAttributeValueAfterApplyValueSet(reportWebServiceInfo,reportService,getValueSetTableNonSQLReportPath,
					contextCodeValueSetCode,contextName);
			//FileUtils.println("##########createNewRecordContextCode, convertedAttributeValue:"+convertedAttributeValue);
			dataValues.add(convertedAttributeValue);
		}
		
		extractDataRow.setDataValues(dataValues.toArray(new String[dataValues.size()]));

		return extractDataRow;
	}
	
	private ExtractDataRow createNewRecord(List<String> fkValues,ExtractFlexfieldTempRow extractFlexfieldTempRow,DFFElement dffElement) throws Exception {
		ExtractDataRow extractDataRow=new ExtractDataRow();
		
		setGenericFields(extractDataRow,extractFlexfieldTempRow);
		
		List<String> dataValues=new ArrayList<String>();
		dataValues.addAll(fkValues);
		String name=dffElement.getPrompt();
		dataValues.add(name);
		String columnName=dffElement.getColumnName();		
		String attributeValue=extractFlexfieldTempRow.getAttributeNameToValueMap().get(columnName);
		//FileUtils.println("### createNewRecord, attributeValue:"+attributeValue);
		if (attributeValue==null) {
			// This means no data setup in the form for that field.
			return null;
		}
		if (attributeValue.isEmpty()) {
			return null;
		}
		if (dffElement.getValidationType().equalsIgnoreCase("TABLE")) {
			String convertedAttributeValue=getAttributeValueAfterApplyValueSet(reportWebServiceInfo,reportService,getValueSetTableNonSQLReportPath,
					dffElement.getValueSetCode(),attributeValue);
			dataValues.add(convertedAttributeValue);
			//FileUtils.println("createNewRecord, convertedAttributeValue:"+convertedAttributeValue);
		}
		else {
			dataValues.add(attributeValue);
		}
		extractDataRow.setDataValues(dataValues.toArray(new String[dataValues.size()]));

		return extractDataRow;
	}
	
	public static void setGenericFields(ExtractDataRow extractDataRow,ExtractFlexfieldTempRow extractFlexfieldTempRow) {
		extractDataRow.setBusinessGroupId(extractFlexfieldTempRow.getBusinessGroupId());
		extractDataRow.setBusinessUnitId(extractFlexfieldTempRow.getBusinessUnitId());
		extractDataRow.setCoaId(extractFlexfieldTempRow.getCoaId());
		extractDataRow.setEnterpriseId(extractFlexfieldTempRow.getEnterpriseId());
		extractDataRow.setInventoryOrganizationId(extractFlexfieldTempRow.getInventoryOrganizationId());
		extractDataRow.setLedgerId(extractFlexfieldTempRow.getLedgerId());
		extractDataRow.setLegalEntityId(extractFlexfieldTempRow.getLegalEntityId());
		extractDataRow.setRscCreatedBy(extractFlexfieldTempRow.getRscCreatedBy());
		extractDataRow.setRscCreationDate(extractFlexfieldTempRow.getRscCreationDate());
		extractDataRow.setRscLastUpdateDate(extractFlexfieldTempRow.getRscLastUpdateDate());
		extractDataRow.setRscLastUpdatedBy(extractFlexfieldTempRow.getRscLastUpdatedBy());		
	}
	
	private DFFSQLElement getDFFSQLElement(String sqlQuery) throws Exception {
		String[] lines=sqlQuery.split(ExtractConstants.END_OF_LINE);
		DFFSQLElement dffSQLElement=new DFFSQLElement();
		for (String line:lines) {
			int indexOf=line.indexOf(ExtractConstants.DFF_LOGIC_TO_APPLY);
			if (indexOf==-1) {
				continue;
			}
			int indexOfEqualSign=line.indexOf("=");
			if (indexOfEqualSign==-1) {
				throw new Exception("Missing '=' sign in the SQL query when applying the DFF logic!");
			}
			int indexOfSeparator=line.toLowerCase().indexOf(ExtractConstants.FUNCTION_KEYS_SEPARATOR.toLowerCase());
			if (indexOfSeparator==-1) {
				throw new Exception("Missing '"+ExtractConstants.FUNCTION_KEYS_SEPARATOR+"' in the SQL query when applying the DFF logic (APPLICATION_ID)");
			}		
			String applicationId=line.substring(indexOfEqualSign+1, indexOfSeparator);
			String dffCode=line.substring(indexOfSeparator+ExtractConstants.FUNCTION_KEYS_SEPARATOR.length());
			dffSQLElement.setApplicationId(applicationId);
			dffSQLElement.setDffCode(dffCode);
			
			return dffSQLElement;
		}
		throw new Exception("Invalid DFF format in the SQL file");
	}

	/*
	 * 
	 *  TO DEBUG:
	 * 
	 Select Distinct GLOBALSEGMENT.Prompt RES_PROMPT
				,GLOBALSEGMENT.CONTEXT_CODE RES_CONTEXT_CODE
				,GLOBALSEGMENT.SEGMENT_CODE RES_SEGMENT_CODE
				,GLOBALSEGMENT.COLUMN_NAME RES_COLUMN_NAME
				,GLOBALSEGMENT.SEQUENCE_NUMBER RES_SEQUENCE_NUMBER
				,VALUESET.VALUE_SET_CODE RES_VALUE_SET_CODE
				,VALUESET.VALIDATION_TYPE RES_VALIDATION_TYPE
				,VALUESET.value_data_type RES_VALUE_DATA_TYPE
				 FROM FND_DF_SEGMENTS_VL GLOBALSEGMENT,FND_VS_VALUE_SETS ValueSet
				 Where GLOBALSEGMENT.Value_Set_Id = Valueset.Value_Set_Id
				 AND GLOBALSEGMENT.APPLICATION_ID = applicationId
				 And GLOBALSEGMENT.Descriptive_Flexfield_Code = 'dffCode'
				  and GlobalSegment.ENABLED_FLAG='Y'
				 ORDER BY CONTEXT_CODE,SEQUENCE_NUMBER
	 */
	private byte[] getRawDFFStructure(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService,
			String applicationId,String dffCode) throws Exception {	
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String value=getDffStructureSQLQuery(applicationId,dffCode);
					
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(value);
		
		//FileUtils.println("Get Raw Data for DFF structure, applicationId: '"+applicationId+"' dffCode: '"+dffCode+"'");
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//FileUtils.println("data:"+data);
		
		return outputBytes;
	}
	
	private static String getDffStructureSQLQuery(String applicationId,String dffCode) {
		String sqlQuery="Select Distinct GLOBALSEGMENT.Prompt RES_PROMPT\n"+
				",GLOBALSEGMENT.CONTEXT_CODE RES_CONTEXT_CODE\n"+
				",GLOBALSEGMENT.SEGMENT_CODE RES_SEGMENT_CODE\n"+
				",GLOBALSEGMENT.COLUMN_NAME RES_COLUMN_NAME\n"+
				",GLOBALSEGMENT.SEQUENCE_NUMBER RES_SEQUENCE_NUMBER\n"+
				",VALUESET.VALUE_SET_CODE RES_VALUE_SET_CODE\n"+
				",VALUESET.VALIDATION_TYPE RES_VALIDATION_TYPE\n"+
				",VALUESET.value_data_type RES_VALUE_DATA_TYPE\n"+
				" FROM FND_DF_SEGMENTS_VL GLOBALSEGMENT,FND_VS_VALUE_SETS ValueSet\n"+
				" Where GLOBALSEGMENT.Value_Set_Id = Valueset.Value_Set_Id\n"+
				" AND GLOBALSEGMENT.APPLICATION_ID = "+applicationId+"\n"+
				" And GLOBALSEGMENT.Descriptive_Flexfield_Code = '"+dffCode+"'\n"+
				" and GlobalSegment.ENABLED_FLAG='Y'"+
				" ORDER BY CONTEXT_CODE,SEQUENCE_NUMBER";
		return sqlQuery;
	}
	
	private byte[] getRawContextCodeAndValueSetCodeAndPrompt(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService,
			String applicationId,String dffCode) throws Exception {			
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String value=
				"SELECT GLOBALSEGMENT.PROMPT RES_PROMPT,GLOBALSEGMENT.VALUE_SET_ID RES_VALUE_SET_ID,\n"+
				"( select value_set_code From Fnd_Vs_Value_Sets where value_set_id = Globalsegment.Value_Set_Id) res_value_set_code\n"+
				" FROM FND_DF_SEGMENTS_VL GLOBALSEGMENT\n"+
				" WHERE GLOBALSEGMENT.APPLICATION_ID           = "+applicationId+"\n"+
				" And Globalsegment.Descriptive_Flexfield_Code =  '"+dffCode+"'\n"+
				" AND GLOBALSEGMENT.COLUMN_NAME   like '%ATTRIBUTE_CATEGORY%'";
		//FileUtils.println("getRawContextCodeAndValueSetIDPrompt, value:\n"+value);
		
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(value);
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//FileUtils.println("getRawContextCodeAndValueSetIDPrompt, data:"+data);
		
		return outputBytes;
	}
	
	private byte[] getRawContextCodeToContextNameMap(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService,
			String applicationId,String dffCode) throws Exception {	
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String value=
				"select T.CONTEXT_CODE RES_CONTEXT_CODE,T.name  RES_NAME\n"+
				"FROM FND_DF_CONTEXTS_B B, FND_DF_CONTEXTS_TL T\n"+
				" WHERE B.APPLICATION_ID           = T.APPLICATION_ID\n"+
				" AND B.DESCRIPTIVE_FLEXFIELD_CODE = T.DESCRIPTIVE_FLEXFIELD_CODE\n"+
				" AND B.CONTEXT_CODE               = T.CONTEXT_CODE\n"+
				" AND T.LANGUAGE                   = USERENV('LANG') \n"+
				" and T.APPLICATION_ID           	 = "+applicationId+"\n"+
				" And T.Descriptive_Flexfield_Code = '"+dffCode+"'\n";
		
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(value);
		//FileUtils.println("getRawContextCodeToContextNameMap:\n"+value);
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//FileUtils.println("data:"+data);
		
		return outputBytes;
	}

	private static boolean initGlobalAndContextValuesDFFStructure(DFFStructure dffStructure,byte[] rawDFFStructure) throws Exception {
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(rawDFFStructure);
		Document document = builder.parse(input);
		
		List<DFFElement> globalElements=dffStructure.getGlobalElements();
		Map<String,List<DFFElement>> contextCodeToDFFElementsMap=dffStructure.getContextCodeToDFFElementsMap();
		
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
	
	        				DFFElement dffElement=new DFFElement();
	        				
	        				String fieldName="RES_PROMPT";
	    					String value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					dffElement.setPrompt(value);
	    					
	    					fieldName="RES_CONTEXT_CODE";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					if (value.isEmpty()) {
	    						throw new Exception("Missing context code in the DFF Structure.");
	    					}
	    					dffElement.setContextCode(value);
	    					if (value.equalsIgnoreCase(ExtractConstants.DFF_GLOBAL_DATA_ELEMENTS)){
	    						globalElements.add(dffElement);
	    					}
	    					else {
	    						List<DFFElement> contextRelatedDFFElements=contextCodeToDFFElementsMap.get(value);
	    						if (contextRelatedDFFElements==null) {
	    							contextRelatedDFFElements=new ArrayList<DFFElement>();
	    							contextCodeToDFFElementsMap.put(value, contextRelatedDFFElements);
	    						}
	    						contextRelatedDFFElements.add(dffElement);
	    					}
	    					
	    					fieldName="RES_SEGMENT_CODE";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					dffElement.setSegmentCode(value);
	    					
	    					fieldName="RES_COLUMN_NAME";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					dffElement.setColumnName(value);
	    					
	    					fieldName="RES_SEQUENCE_NUMBER";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					dffElement.setSequenceNumber(value);
	    					
	    					fieldName="RES_VALUE_SET_CODE";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					dffElement.setValueSetCode(value);
	    					
	    					fieldName="RES_VALIDATION_TYPE";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					dffElement.setValidationType(value);
	    					
	    					fieldName="RES_VALUE_DATA_TYPE";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					dffElement.setValueDataType(value);
	        			}
	        		}
	        		return false;
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
		}
        return true;
	}
	
	private static boolean initContextCodePromptDFFStructure(DFFStructure dffStructure,byte[] rawContextCodeAndValueSetCodeAndPrompt) throws Exception {
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(rawContextCodeAndValueSetCodeAndPrompt);
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
	
	        				String fieldName="RES_PROMPT";
	    					String value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					if (value.isEmpty()) {
	    						throw new Exception("Missing context code prompt in the DFF Structure.");
	    					}
	    					dffStructure.setContextCodePrompt(value);
	    					
	    					fieldName="RES_VALUE_SET_ID";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					dffStructure.setContextCodeValueSetId(value);
	    					
	    					fieldName="res_value_set_code";
	    					value=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					dffStructure.setContextCodeValueSetCode(value);
	        			}
	        		}
	        		return false;
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
		}
        return true;
	}
	
	private static boolean initContextCodeToContextNameMapDFFStructure(DFFStructure dffStructure,byte[] rawContextCodeToContextNameMap) throws Exception {
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(rawContextCodeToContextNameMap);
		Document document = builder.parse(input);

		for(String rowTag:ExtractUtils.BI_PUBLISHER_ROW_TAG_NAMES) {
			PrintWriter pw=null;
	        NodeList nodeList = document.getElementsByTagName(rowTag);
	        int recordCount=nodeList.getLength();
	        if (recordCount>0)
	        {
	        	try{        
	        		Map<String, String> contextCodeToContextNameMap=dffStructure.getContextCodeToContextNameMap();
	        		for (int i = 0; i < nodeList.getLength(); i++) {
	        			Node node = nodeList.item(i);
	        			if (node.getNodeType() == Node.ELEMENT_NODE) {
	        				Element element = (Element) node;
	
	        				String fieldName="RES_CONTEXT_CODE";
	    					String contextCode=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					if (contextCode.isEmpty()) {
	    						throw new Exception("Missing context code in the DFF Structure.");
	    					}
	    					
	    					fieldName="RES_NAME";
	    					String contextCodeName=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					if (contextCodeName.isEmpty()) {
	    						throw new Exception("Missing context code name in the DFF Structure.");
	    					}
	    					//FileUtils.println("initContextCodeToContextNameMapDFFStructure, contextCode: '"+contextCode+"' contextCodeName: '"+contextCodeName+"'");
	    					contextCodeToContextNameMap.put(contextCode, contextCodeName);
	        			}
	        		}
	        		return false;
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
		}
        return true;
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
			byte[] rawValueSetTableData=getRawValueSetTableData(reportWebServiceInfo,reportService,getValueSetTableNonSQLReportPath,valueSetCode,attributeValue);
			String valueSetValue=getValueSetValue(rawValueSetTableData);
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

	/*
	 * The report will return one row with one column containing the value.
	 */
	public static byte[] getRawValueSetTableData(
			WebServiceInfo reportWebServiceInfo,ReportService reportService,String getValueSetTableNonSQLReportPath,String valueSetCode,String attributeValue) throws Exception {	
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(valueSetCode);
		
		parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param2");
		parameterNameValue.setValue(attributeValue);
		
		//FileUtils.println("getRawValueSetTableData: '"+attributeValue+"'");
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,getValueSetTableNonSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//FileUtils.println("data:"+data);
		
		return outputBytes;
	}
	
	public static String getValueSetValue(byte[] rawValueSetTableData) throws Exception {
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(rawValueSetTableData);
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
	
	        				String fieldName="RES_RESULT";
	    					String valueSetValue=ExtractUtils.getDataFromBIOutput(element,fieldName);
	    					if (valueSetValue.isEmpty()) {
	    						throw new Exception("Missing Result after running the Value Set query.");
	    					}
	    					//FileUtils.println("getValueSetValue, valueSetValue:"+valueSetValue);
	    					return valueSetValue;
	        			}
	        		}
	        		//throw new Exception("Missing Result after running the Value Set query.");
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
//	        else {
//	        	throw new Exception("Unable to retrieve the Result after running the Value Set query.");
//	        }
		}
		return null;
	}	
		
}
