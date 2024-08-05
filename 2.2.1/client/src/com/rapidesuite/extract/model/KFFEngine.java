package com.rapidesuite.extract.model;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.extract.ExtractConstants;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;

public class KFFEngine {
	
	private List<Integer> selectClauseKFFFieldPositionList;
	private List<Integer> kffNewColumnSequenceList;
	private String kffPrefixAlias;
	private String applicationId;
	private String kffCode;
	private String columnNamePrefix;
	
	public KFFEngine(String sqlContent, Inventory inventory) throws Exception{
		kffNewColumnSequenceList=new ArrayList<Integer>();
		parseMetadata(sqlContent);
		
		selectClauseKFFFieldPositionList=new ArrayList<Integer>();
		List<String> fields=inventory.getHashesForFieldsUsedForDataEntry();
		int index=1; // starting at 1 since the segment DB columns start at 1 in Oracle.
		for (String field:fields){
			if (field.startsWith(kffPrefixAlias)) {
				selectClauseKFFFieldPositionList.add(index);
			}
			index++;
		}
		if (selectClauseKFFFieldPositionList.isEmpty()) {
			throw new Exception("Invalid KFF metadata, no '"+kffPrefixAlias+"' alias fields.");
		}
	}
	
	private void parseMetadata(String sqlContent) throws Exception {
		String[] lines=sqlContent.split(ExtractConstants.END_OF_LINE);
		for (String line:lines) {
			int indexOf=line.indexOf(ExtractConstants.KFF_LOGIC_TO_APPLY);
			if (indexOf!=-1) {
				int indexOfEqualSign=line.indexOf("=");
				if (indexOfEqualSign==-1) {
					throw new Exception("Missing '=' sign in the SQL query when applying the KFF logic!");
				}
				int indexOfSeparator=line.toLowerCase().indexOf(ExtractConstants.FUNCTION_KEYS_SEPARATOR.toLowerCase());
				if (indexOfSeparator==-1) {
					throw new Exception("Missing '"+ExtractConstants.FUNCTION_KEYS_SEPARATOR+"' in the SQL query when applying the KFF logic (APPLICATION_ID)");
				}		
				applicationId=line.substring(indexOfEqualSign+1, indexOfSeparator);
				kffCode=line.substring(indexOfSeparator+ExtractConstants.FUNCTION_KEYS_SEPARATOR.length());
			}
			else {
				indexOf=line.indexOf(ExtractConstants.KFF_PREFIX_ALIAS);
				if (indexOf!=-1) {
					int indexOfEqualSign=line.indexOf("=");
					if (indexOfEqualSign==-1) {
						throw new Exception("Missing '=' sign in the SQL query when applying the KFF logic!");
					}
					kffPrefixAlias=line.substring(indexOfEqualSign+1);
				}
				else {
					indexOf=line.indexOf(ExtractConstants.KFF_COLUMN_NAME);
					if (indexOf!=-1) {
						int indexOfEqualSign=line.indexOf("=");
						if (indexOfEqualSign==-1) {
							throw new Exception("Missing '=' sign in the SQL query when applying the KFF logic!");
						}
						columnNamePrefix=line.substring(indexOfEqualSign+1);
					}
				}
			}
		}
		if ( kffPrefixAlias==null || applicationId==null || kffCode==null || columnNamePrefix==null) {
			throw new Exception("Invalid KFF metadata in the SQL file");
		}
	}
	
	public void init(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {	
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String value="SELECT "+
			"KeyFlexfieldSegmentEO.COLUMN_NAME AS RES_COLUMN_NAME "+
			"FROM FND_KF_SEGMENTS_VL KeyFlexfieldSegmentEO "+
			",FND_KF_STRUCTURES_VL KeyFlexfieldStructureEO "+
			"WHERE KeyFlexfieldStructureEO.STRUCTURE_ID = KeyFlexfieldSegmentEO.STRUCTURE_ID "+
			"And Keyflexfieldstructureeo.Application_Id = "+applicationId+" "+
			"And Keyflexfieldstructureeo.Structure_Code = '"+kffCode+"' "+
			"order by SEQUENCE_NUMBER";
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(value);
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//System.out.println("data:"+data);
		
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
	        				
	        				String fieldName="RES_COLUMN_NAME";
	        				String columnName=ExtractUtils.getDataFromBIOutput(element,fieldName);
	        				String seqStr=columnName.replaceAll(columnNamePrefix,"");
	        				int kffSeq=Integer.valueOf(seqStr).intValue();
	        				kffNewColumnSequenceList.add(kffSeq);
	        			}
	        		}
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
		}
		if(CollectionUtils.isEmpty(kffNewColumnSequenceList)) {
			throw new Exception("Unable to retrieve KFF structure");
		}
	}

	/*
	 * Resequence the data values as per the ordering specified in the kffSequenceList 
	 */
	public List<ExtractDataRow> apply(List<ExtractDataRow> listToApply) throws Exception {
		Map<Integer,String> kffPositionToValueMap=new HashMap<Integer,String>();
		for (ExtractDataRow extractDataRow:listToApply){
			String[] dataValues=extractDataRow.getDataValues();
			//System.out.println("####apply");
			for (Integer selectClauseKFFFieldPosition:selectClauseKFFFieldPositionList) {
				int originalDataValueIndex=selectClauseKFFFieldPosition-1; // dataValues start at 0
				String dataValue=dataValues[originalDataValueIndex];

				kffPositionToValueMap.put(originalDataValueIndex, dataValue);
				//System.out.println("selectClauseKFFFieldPosition:"+selectClauseKFFFieldPosition+" originalDataValueIndex:"+originalDataValueIndex+" dataValue:"+dataValue);
			}

			int index=0;
			for (Integer kffNewColumnSequence:kffNewColumnSequenceList) {
				int sourceDataValueIndex=kffNewColumnSequence-1; // dataValues start at 0
				String newDataValue=kffPositionToValueMap.get(sourceDataValueIndex);
				Integer selectClauseKFFFieldPosition=selectClauseKFFFieldPositionList.get(index);
				int targetDataValueIndex=selectClauseKFFFieldPosition-1; // dataValues start at 0
				
				//System.out.println("index:"+index+" kffNewColumnSequence:"+kffNewColumnSequence+" selectClauseKFFFieldPosition:"+selectClauseKFFFieldPosition+
				//		" sourceDataValueIndex: "+sourceDataValueIndex+" targetDataValueIndex:"+targetDataValueIndex+" oldValue:"+dataValues[targetDataValueIndex]+
				//		" newValue:"+newDataValue);
				dataValues[targetDataValueIndex]=newDataValue;
				
				index++;
			}
		}
		return listToApply;
	}

}
