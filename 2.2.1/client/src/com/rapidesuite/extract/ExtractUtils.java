package com.rapidesuite.extract;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.ws.BindingProvider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.inventory0007.Field;
import com.rapidesuite.core.utility.CoreUtil;
import com.oracle.xmlns.oxp.service.v2.ArrayOfItemData;
import com.oracle.xmlns.oxp.service.v2.ArrayOfParamNameValue;
import com.oracle.xmlns.oxp.service.v2.ArrayOfString;
import com.oracle.xmlns.oxp.service.v2.CatalogContents;
import com.oracle.xmlns.oxp.service.v2.CatalogService;
import com.oracle.xmlns.oxp.service.v2.CatalogService_Service;
import com.oracle.xmlns.oxp.service.v2.ItemData;
import com.oracle.xmlns.oxp.service.v2.ParamNameValue;
import com.oracle.xmlns.oxp.service.v2.ParamNameValues;
import com.oracle.xmlns.oxp.service.v2.ReportRequest;
import com.oracle.xmlns.oxp.service.v2.ReportResponse;
import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.oracle.xmlns.oxp.service.v2.ReportService_Service;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.SevenZipUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.configurator.utility.XmlNavigatorParser;
import com.rapidesuite.configurator.utility.XmlNavigatorParser.EbsResponsibilityRecord;
import com.rapidesuite.extract.model.BIRowInformation;
import com.rapidesuite.extract.model.ExtractDataRow;
import com.rapidesuite.extract.model.ExtractFlexfieldTempRow;
import com.rapidesuite.extract.model.ExtractGenericDataRow;
import com.rapidesuite.extract.model.ExtractInventoryRecord;
import com.rapidesuite.extract.model.FieldInformation;
import com.rapidesuite.extract.model.HookGetCodeCombination;
import com.rapidesuite.extract.model.HookGetSystemProfileValue;
import com.rapidesuite.extract.model.InventoryFieldsInformation;
import com.rapidesuite.extract.model.NavigatorNode;
import com.rapidesuite.extract.model.ParameterNameValue;
import com.rapidesuite.extract.view.ExtractInventoryGridResultPanel;
import com.rapidesuite.extract.view.FilterDatePanel;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.extract.model.MapGeneric;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.view.UIUtils;

public class ExtractUtils {
	
	public static final String[] BI_PUBLISHER_ROW_TAG_NAMES=new String[]{"QUERY","ROW"};
	
	/**
	 * 
	 * ENUM denoting the levels of data in FUSION
	 * NOTE: MIND THE ORDER
	 * 
	 * @author hassan.jamil
	 */
	public enum SELECTION_LEVEL {
		GLOBAL("Global"),
		//BUSINESS_GROUP("Business Group"),
		ENTERPRISE_BUSINESS_GROUP("Enterprise - Business Group"), 
		COA("Charter of Account"),
		LEDGER("Ledger"),
		LEGAL_ENTITY("Legal Entity"),
		BUSINESS_UNIT("Business Unit"),
		ORGANIZATION("Organization");
		
		private String level;
		
		SELECTION_LEVEL(String level) {
			this.level = level;
		}
		
		public String getLevel() {
			return this.level;
		}
	}
	
	/**
	 * The map of all the Levels and IDs and Names
	 */
	public static Map<SELECTION_LEVEL, Map<Long, String>> selectionLevelIDAndNameMapping = null;
	
	/*
	 * Generate a ZIP archive (extension: .xdmz ) with 2 files:
	 *  - _datamodel.xdm : which contains the dataset information in xml format.
	 *  - sample.xml : which contains sample data in xml format.
	 */
	public static void createXDMZFile(File outputFile,File folderToArchive,DataSet dataSet) throws Exception {
		File xdmFile=new File(folderToArchive,"_datamodel.xdm");
		createXDMFile(xdmFile,dataSet);
		
		File dataSampleFile=new File(folderToArchive,"sample.xml");
		createDataSetDataSampleFile(dataSampleFile,dataSet);
		
		//System.out.println(new java.util.Date()+" createXDMZFile, outputFile: '"+outputFile.getAbsolutePath()+"'");
		//System.out.println(new java.util.Date()+" createXDMZFile, folderToArchive: '"+folderToArchive.getAbsolutePath()+"'");
		//System.out.println(new java.util.Date()+" createXDMZFile, inventory: '"+dataSet.getInventory().getName()+"'");
		
		zipFolderNonRecursive(folderToArchive, outputFile);
	}
	
	/*
	 * This is to be used for running custom SQL queries via a PL/SQL block
	 */
	public static void createNonSQLXDMZFile(File outputFile,File folderToArchive,String plsqlCode,int parametersCount) throws Exception {
		File xdmFile=new File(folderToArchive,"_datamodel.xdm");
		createNonSQLXDMFile(xdmFile,plsqlCode,parametersCount);
		zipFolderNonRecursive(folderToArchive, outputFile);
	}
	
	public static void createNonSQLXDMFile(File outputFile,String plsqlCode,int parametersCount) {
		StringBuffer buffer=new StringBuffer("");
		buffer.append("<?xml version = '1.0' encoding = 'utf-8'?>\n");
		
		buffer.append("<dataModel xmlns=\"http://xmlns.oracle.com/oxp/xmlp\" version=\"2.0\" xmlns:xdm=\"http://xmlns.oracle.com/oxp/xmlp\" xmlns:xsd=\"http://wwww.w3.org/2001/XMLSchema\" defaultDataSourceRef=\"ApplicationDB_FSCM\">\n");
		buffer.append("	<description>\n");
		buffer.append("		<![CDATA[Dynamic SQL queries]]>\n");
		buffer.append("	</description>\n");
		buffer.append("	<dataProperties>\n");
		buffer.append("		<property name=\"include_parameters\" value=\"true\"/>\n");
		buffer.append("     <property name=\"include_null_Element\" value=\"false\"/>\n");
		buffer.append("     <property name=\"include_rowsettag\" value=\"false\"/>\n");
		buffer.append("     <property name=\"xml_tag_case\" value=\"upper\"/>\n");
		buffer.append("	</dataProperties>\n");
		buffer.append("	<dataSets>\n");
		buffer.append("		<dataSet name=\"QUERY\" type=\"simple\">\n");
		buffer.append("		<sql dataSourceRef=\"ApplicationDB_FSCM\" nsQuery=\"true\" sp=\"true\">\n");
		
		buffer.append("		<![CDATA[\n");
		buffer.append(plsqlCode);
		buffer.append("		]]>\n");
		
		buffer.append("		</sql>\n");
		buffer.append("</dataSet>\n");
		buffer.append("</dataSets>\n");
		buffer.append("	<output rootName=\"DATA_DS\" uniqueRowName=\"false\">\n");
		buffer.append("		<nodeList name=\"QUERY\"/>\n");
		buffer.append("	</output>\n");
		buffer.append("	<eventTriggers/>\n");
		buffer.append("	<lexicals/>\n");
		buffer.append("	<valueSets/>\n");
		buffer.append("	<parameters>\n");
		buffer.append("		<parameter name=\"xdo_cursor\" dataType=\"xsd:string\" rowPlacement=\""+0+"\">\n");
		buffer.append("			<input label=\"xdo_cursor\"/>\n");
		buffer.append("		</parameter>\n");
		for (int i=1;i<=parametersCount;i++) {
			buffer.append("		<parameter name=\"param"+i+"\" dataType=\"xsd:string\" rowPlacement=\""+i+"\">\n");
			buffer.append("			<input label=\"param"+i+"\"/>\n");
			buffer.append("		</parameter>\n");
		}
		buffer.append("	</parameters>\n");
		buffer.append("	<bursting/>\n");
		buffer.append("	<display>\n");
		buffer.append("	   	<layouts>\n");
		buffer.append("	     	<layout name=\"QUERY\" left=\"286px\" top=\"257px\"/>\n");
		buffer.append("	     	<layout name=\"DATA_DS\" left=\"6px\" top=\"257px\"/>\n");
		buffer.append("	 	</layouts>\n");
		buffer.append("	<groupLinks/>\n");
		buffer.append("	</display>\n");
		buffer.append("	</dataModel>\n");
		
		ModelUtils.writeToFile(outputFile,buffer.toString(),false); 
	}
	
	/*
	 * Generate a ZIP archive (extension: .xdoz ) with 1 file:
	 *  - _report.xdo : which contains the report information in xml format.
	 */
	public static void createXDOZFile(File outputFile,File folderToArchive,String dataSetAbsolutePath) throws Exception {
		File xdoFile=new File(folderToArchive,"_report.xdo");
		createXDOFile(xdoFile,dataSetAbsolutePath);
		zipFolderNonRecursive(folderToArchive, outputFile);
	}
	
	private static void createXDOFile(File xdoFile,String dataSetAbsolutePath) {
		StringBuffer buffer=new StringBuffer("");
		buffer.append("<?xml version = '1.0' encoding = 'utf-8'?>\n");
		buffer.append("<report xmlns=\"http://xmlns.oracle.com/oxp/xmlp\" xmlns:xsd=\"http://wwww.w3.org/2001/XMLSchema\" version=\"2.0\" "+
		"dataModel=\"true\" useBipParameters=\"true\" producerName=\"fin-financialCommon\" parameterVOName=\"\" parameterTaskFlow=\"\" "+
		"customDataControl=\"\" cachePerUser=\"true\" cacheSmartRefresh=\"false\" cacheUserRefresh=\"false\">\n");
		buffer.append("<dataModel url=\""+dataSetAbsolutePath+".xdm\"/>\n");
		buffer.append("<description/>\n");
		buffer.append("<property name=\"showControls\" value=\"true\"/>\n");
		buffer.append("<property name=\"online\" value=\"true\"/>\n");
		buffer.append("<property name=\"openLinkInNewWindow\" value=\"true\"/>\n");
		buffer.append("<property name=\"autoRun\" value=\"true\"/>\n");
		buffer.append("<property name=\"cacheDocument\" value=\"true\"/>\n");
		buffer.append("<property name=\"showReportLinks\" value=\"true\"/>\n");
		buffer.append("<property name=\"asynchronousRun\" value=\"false\"/>\n");
		buffer.append("<property name=\"useExcelProcessor\" value=\"false\"/>\n");
		buffer.append("<property name=\"cacheDuration\" value=\"30\"/>\n");
		buffer.append("<property name=\"controledByExtApp\" value=\"false\"/>\n");
		buffer.append("<parameters paramPerLine=\"3\" style=\"parameterLocation:in-horizontal;promptLocation:side;\"/>\n");
		buffer.append("<templates default=\"testTemplate\">\n");
		buffer.append("   <template label=\"testTemplate\" url=\"testTemplate.xpt\" type=\"xpt\" outputFormat=\"xml\" "+
		"defaultFormat=\"xml\" locale=\"en_US\" active=\"true\" viewOnline=\"true\"/>\n");
		buffer.append("</templates>\n");
		buffer.append("</report>\n");
		
		ModelUtils.writeToFile(xdoFile,buffer.toString(),false); 
	}

	public static void createXDMFile(File outputFile,DataSet dataSet) {
		StringBuffer buffer=new StringBuffer("");
		buffer.append("<?xml version = '1.0' encoding = 'utf-8'?>\n");
		
		buffer.append("<dataModel xmlns=\"http://xmlns.oracle.com/oxp/xmlp\" version=\"2.0\" xmlns:xdm=\"http://xmlns.oracle.com/oxp/xmlp\" xmlns:xsd=\"http://wwww.w3.org/2001/XMLSchema\" defaultDataSourceRef=\"ApplicationDB_FSCM\">\n");
		buffer.append("	<description>\n");
		buffer.append("		<![CDATA["+dataSet.getDescription()+"]]>\n");
		buffer.append("	</description>\n");
		buffer.append("	<dataProperties>\n");
		buffer.append("		<property name=\"include_parameters\" value=\"true\"/>\n");
		buffer.append("     <property name=\"include_null_Element\" value=\"false\"/>\n");
		buffer.append("     <property name=\"include_rowsettag\" value=\"false\"/>\n");
		buffer.append("     <property name=\"xml_tag_case\" value=\"upper\"/>\n");
		buffer.append("	</dataProperties>\n");
		buffer.append("	<dataSets>\n");
		buffer.append("	<dataSet name=\""+dataSet.getIdentifier()+"\" type=\"complex\">\n");
		buffer.append("		<sql>\n");
		
		String reformattedSQLQueryForSQLDataset=getReformattedSQLQueryForSQLDataset(dataSet);
		buffer.append(reformattedSQLQueryForSQLDataset);
		buffer.append("\n");
		
		buffer.append("		</sql>\n");
		buffer.append("</dataSet>\n");
		buffer.append("</dataSets>\n");
		buffer.append("<output rootName=\"DATA_DS\" uniqueRowName=\"false\">\n");
		buffer.append("<nodeList name=\"data-structure\">\n");
		buffer.append("<dataStructure tagName=\"DATA_DS\">\n");
		buffer.append("	<group name=\"G_1\" label=\"G_1\" source=\""+dataSet.getIdentifier()+"\">\n");
		
		List<DataSetElement> dataSetElementList=dataSet.getDataSetElementList();
		for (DataSetElement dataSetElement:dataSetElementList) {
			buffer.append("	<element name=\""+dataSetElement.getName()+"\" value=\""+dataSetElement.getValue()+"\" label=\""+dataSetElement.getLabel()+"\" dataType=\""+dataSetElement.getDataType()+"\""
					+ " breakOrder=\"\" fieldOrder=\""+dataSetElement.getFieldOrder()+"\"/>\n");
		}
		buffer.append(" </group>\n");
		buffer.append("	</dataStructure>\n");
		buffer.append("	</nodeList>\n");
		buffer.append("	</output>\n");
		buffer.append("	<eventTriggers/>\n");
		buffer.append("	<lexicals/>\n");
		buffer.append("	<valueSets/>\n");
		buffer.append("	<parameters>\n");
		buffer.append("		<parameter name=\"INC_LAST_UPDATED_BY\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"LAST_UPDATED_BY_USER_NAME_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"EXC_LAST_UPDATED_BY\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		
		buffer.append("		<parameter name=\"INC_CREATED_BY\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"SEEDED_USER_NAME_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"EXC_CREATED_BY\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		
		buffer.append("		<parameter name=\"INC_SEEDED_DATA\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"CREATED_BY_USER_NAME_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"EXC_SEEDED_DATA\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		
		buffer.append("		<parameter name=\"LAST_UPDATE_DATE_ON\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"LAST_UPDATE_DATE_MIN\" defaultValue=\"01-JAN-2001\" dataType=\"xsd:date\" rowPlacement=\"1\"><date format=\""+ExtractConstants.DATA_BI_PUBLISHER_DATE_FORMAT+"\"/></parameter>\n");
		buffer.append("		<parameter name=\"LAST_UPDATE_DATE_MAX\" defaultValue=\"01-JAN-2100\" dataType=\"xsd:date\" rowPlacement=\"1\"><date format=\""+ExtractConstants.DATA_BI_PUBLISHER_DATE_FORMAT+"\"/></parameter>\n");
		
		buffer.append("		<parameter name=\"CREATION_DATE_ON\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"CREATION_DATE_MIN\" defaultValue=\"01-JAN-2001\" dataType=\"xsd:date\" rowPlacement=\"1\"><date format=\""+ExtractConstants.DATA_BI_PUBLISHER_DATE_FORMAT+"\"/></parameter>\n");
		buffer.append("		<parameter name=\"CREATION_DATE_MAX\" defaultValue=\"01-JAN-2100\" dataType=\"xsd:date\" rowPlacement=\"1\"><date format=\""+ExtractConstants.DATA_BI_PUBLISHER_DATE_FORMAT+"\"/></parameter>\n");
		
		buffer.append("		<parameter name=\"INC_LEDGER_ID\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"LEDGER_ID_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
		
		buffer.append("		<parameter name=\"INC_CHART_OF_ACCOUNTS_ID\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"CHART_OF_ACCOUNTS_ID_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
		
		buffer.append("		<parameter name=\"INC_BUSINESS_UNIT_ID\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"BUSINESS_UNIT_ID_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
		
		buffer.append("		<parameter name=\"INC_LEGAL_ENTITY_ID\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"LEGAL_ENTITY_ID_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
		
		buffer.append("		<parameter name=\"INC_ORGANIZATION_ID\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"ORGANIZATION_ID_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
		
		buffer.append("		<parameter name=\"INC_BUSINESS_GROUP_ID\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"BUSINESS_GROUP_ID_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
		
		buffer.append("		<parameter name=\"INC_ENTERPRISE_ID\" defaultValue=\"0\" dataType=\"xsd:integer\" rowPlacement=\"1\"><input/></parameter>\n");
		buffer.append("		<parameter name=\"ENTERPRISE_ID_LIST\" dataType=\"xsd:string\" rowPlacement=\"1\"><input/></parameter>\n");
     
		buffer.append("	</parameters>\n");
		buffer.append("	<bursting/>\n");
		buffer.append("	</dataModel>\n");
		
		ModelUtils.writeToFile(outputFile,buffer.toString(),false); 
	}
	
	public static String getReformattedSQLQueryForSQLDataset(DataSet dataSet) {
		StringBuffer buffer=new StringBuffer("");
				
		// remove all comments /* */
		Pattern commentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
		String sqlQuery= commentPattern.matcher(dataSet.getSqlQuery()).replaceAll("");
		sqlQuery=StringEscapeUtils.escapeXml10(sqlQuery);
		
		buffer.append("SELECT * FROM (\n");
		buffer.append("--INNER_START\n");
				
		buffer.append(sqlQuery);
		
		buffer.append("\n\n");
		buffer.append("--INNER_END\n");
		buffer.append(") WHERE\n");
		
		//  INCLUDE OR EXCLUDE LAST UPDATE BY USERS.
		buffer.append("( :INC_LAST_UPDATED_BY=0 OR (:INC_LAST_UPDATED_BY=1 AND RSC_LAST_UPDATED_BY IN (Select Regexp_Substr(:LAST_UPDATED_BY_USER_NAME_LIST,'[^,]+', 1, Level) From Dual Connect By Regexp_Substr(:LAST_UPDATED_BY_USER_NAME_LIST, '[^,]+', 1, Level) Is Not Null) ) )\n");
		buffer.append("and ( :EXC_LAST_UPDATED_BY=0 OR (:EXC_LAST_UPDATED_BY=1 AND RSC_LAST_UPDATED_BY NOT IN (Select Regexp_Substr(:LAST_UPDATED_BY_USER_NAME_LIST,'[^,]+', 1, Level) From Dual Connect By Regexp_Substr(:LAST_UPDATED_BY_USER_NAME_LIST, '[^,]+', 1, Level) Is Not Null) ) )\n");
		
		//  INCLUDE OR EXCLUDE CREATED BY USERS.
		buffer.append("and ( :INC_CREATED_BY=0 OR (:INC_CREATED_BY=1 AND RSC_CREATED_BY IN (Select Regexp_Substr(:CREATED_BY_USER_NAME_LIST,'[^,]+', 1, Level) From Dual Connect By Regexp_Substr(:CREATED_BY_USER_NAME_LIST, '[^,]+', 1, Level) Is Not Null) ) )\n");
		buffer.append("and ( :EXC_CREATED_BY=0 OR (:EXC_CREATED_BY=1 AND RSC_CREATED_BY NOT IN (Select Regexp_Substr(:CREATED_BY_USER_NAME_LIST,'[^,]+', 1, Level) From Dual Connect By Regexp_Substr(:CREATED_BY_USER_NAME_LIST, '[^,]+', 1, Level) Is Not Null) ) )\n");
		
		//  INCLUDE OR EXCLUDE SEEDED USERS.
		buffer.append("and ( :INC_SEEDED_DATA=0 OR (:INC_SEEDED_DATA=1 AND RSC_CREATED_BY IN (Select Regexp_Substr(:SEEDED_USER_NAME_LIST,'[^,]+', 1, Level) From Dual Connect By Regexp_Substr(:SEEDED_USER_NAME_LIST, '[^,]+', 1, Level) Is Not Null) "+
		"AND RSC_LAST_UPDATED_BY IN (Select Regexp_Substr(:SEEDED_USER_NAME_LIST,'[^,]+', 1, Level) From Dual Connect By Regexp_Substr(:SEEDED_USER_NAME_LIST, '[^,]+', 1, Level) Is Not Null) ) )\n");
		buffer.append("and ( :EXC_SEEDED_DATA=0 OR (:EXC_SEEDED_DATA=1 AND RSC_CREATED_BY NOT IN (Select Regexp_Substr(:SEEDED_USER_NAME_LIST,'[^,]+', 1, Level) From Dual Connect By Regexp_Substr(:SEEDED_USER_NAME_LIST, '[^,]+', 1, Level) Is Not Null) "+
		"AND RSC_LAST_UPDATED_BY NOT IN (Select Regexp_Substr(:SEEDED_USER_NAME_LIST,'[^,]+', 1, Level) From Dual Connect By Regexp_Substr(:SEEDED_USER_NAME_LIST, '[^,]+', 1, Level) Is Not Null) ) )\n");
		
		//  INCLUDE OR EXCLUDE BASED ON LAST UPDATE DATE OR CREATION DATE.
		buffer.append("and ( :LAST_UPDATE_DATE_ON=0 OR (:LAST_UPDATE_DATE_ON=1 AND RSC_LAST_UPDATE_DATE BETWEEN :LAST_UPDATE_DATE_MIN AND :LAST_UPDATE_DATE_MAX ) )\n");
		buffer.append("and ( :CREATION_DATE_ON=0 OR (:CREATION_DATE_ON=1 AND RSC_CREATION_DATE BETWEEN :CREATION_DATE_MIN AND :CREATION_DATE_MAX ) )\n");
		
		buffer.append("and ( :INC_LEDGER_ID=0 OR (:INC_LEDGER_ID=1 AND RSC_LEDGER_ID IN (:LEDGER_ID_LIST) ) )\n");
		buffer.append("and ( :INC_CHART_OF_ACCOUNTS_ID=0 OR (:INC_CHART_OF_ACCOUNTS_ID=1 AND RSC_CHART_OF_ACCOUNTS_ID IN (:CHART_OF_ACCOUNTS_ID_LIST) ) )\n");
		buffer.append("and ( :INC_BUSINESS_UNIT_ID=0 OR (:INC_BUSINESS_UNIT_ID=1 AND RSC_BUSINESS_UNIT_ID IN (:BUSINESS_UNIT_ID_LIST) ) )\n");
		buffer.append("and ( :INC_LEGAL_ENTITY_ID=0 OR (:INC_LEGAL_ENTITY_ID=1 AND RSC_LEGAL_ENTITY_ID IN (:LEGAL_ENTITY_ID_LIST) ) )\n");
		buffer.append("and ( :INC_ORGANIZATION_ID=0 OR (:INC_ORGANIZATION_ID=1 AND RSC_ORGANIZATION_ID IN (:ORGANIZATION_ID_LIST) ) )\n");
		buffer.append("and ( :INC_BUSINESS_GROUP_ID=0 OR (:INC_BUSINESS_GROUP_ID=1 AND RSC_BUSINESS_GROUP_ID IN (:BUSINESS_GROUP_ID_LIST) ) )\n");
		buffer.append("and ( :INC_ENTERPRISE_ID=0 OR (:INC_ENTERPRISE_ID=1 AND RSC_ENTERPRISE_ID IN (:ENTERPRISE_ID_LIST) ) )\n");
		
		//(Select Regexp_Substr(:SEEDED_USER_NAME_LIST,'[^,]+', 1, Level) From Dual Connect By Regexp_Substr(:SEEDED_USER_NAME_LIST, '[^,]+', 1, Level) Is Not Null)
		//TODO: add rownum so we can reverse chunks in parallel
		return buffer.toString(); 
	}
	
	public static String getReformattedSQLQueryForNonSQLDataset(DataSet dataSet) {
		return getReformattedSQLQueryForNonSQLDataset(dataSet, false);
	}
	
	public static String getReformattedSQLQueryForNonSQLDataset(DataSet dataSet, boolean queryForCount) {
		StringBuffer buffer=new StringBuffer("");
				
		// remove all comments /* */
		Pattern commentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
		String sqlQuery= commentPattern.matcher(dataSet.getSqlQuery()).replaceAll("");
		//sqlQuery=StringEscapeUtils.escapeXml10(sqlQuery);

		// additional SELECT wrapper for ROWNUM
		if(!queryForCount) {
			buffer.append("SELECT * FROM (\n");
		}
		
		if(!queryForCount) {
			buffer.append("SELECT ROWNUM RNUM, INNER_QUERY.* FROM (\n");
		} else {
			buffer.append("SELECT COUNT(*) as QUERY FROM (\n");
		}
		//buffer.append("SELECT * FROM (\n");
		buffer.append("--INNER_START\n");
				
		buffer.append(sqlQuery);
		
		buffer.append("\n\n");
		buffer.append("--INNER_END\n");
		buffer.append(") INNER_QUERY \n");
		
		//additional SELECT wrapper for ROWNUM closed here
		if(!queryForCount) {
			buffer.append(") \n");
		}
		
		buffer.append("WHERE 1=1 \n");
			
		return buffer.toString(); 
	}
	
	public static void createDataSetDataSampleFile(File outputFile,DataSet dataSet) {
		StringBuffer buffer=new StringBuffer("");
		buffer.append("<?xml version = '1.0' encoding = 'utf-8'?>\n");
		
		buffer.append("<DATA_DS>\n");
		buffer.append("<INC_LAST_UPDATED_BY>0</INC_LAST_UPDATED_BY><LAST_UPDATED_BY_USER_NAME_LIST/><EXC_LAST_UPDATED_BY>0</EXC_LAST_UPDATED_BY><INC_CREATED_BY>0</INC_CREATED_BY><SEEDED_USER_NAME_LIST/><EXC_CREATED_BY>0</EXC_CREATED_BY><INC_SEEDED_DATA>0</INC_SEEDED_DATA><CREATED_BY_USER_NAME_LIST/><EXC_SEEDED_DATA>0</EXC_SEEDED_DATA><LAST_UPDATE_DATE_ON>0</LAST_UPDATE_DATE_ON><LAST_UPDATE_DATE_MIN/><LAST_UPDATE_DATE_MAX/><CREATION_DATE_ON>0</CREATION_DATE_ON><CREATION_DATE_MIN/><CREATION_DATE_MAX/><INC_LEDGER_ID>0</INC_LEDGER_ID><LEDGER_ID_LIST/><INC_CHART_OF_ACCOUNTS_ID>0</INC_CHART_OF_ACCOUNTS_ID><CHART_OF_ACCOUNTS_ID_LIST/><INC_BUSINESS_UNIT_ID>0</INC_BUSINESS_UNIT_ID><BUSINESS_UNIT_ID_LIST/><INC_LEGAL_ENTITY_ID>0</INC_LEGAL_ENTITY_ID><LEGAL_ENTITY_ID_LIST/><INC_ORGANIZATION_ID>0</INC_ORGANIZATION_ID><ORGANIZATION_ID_LIST/><INC_BUSINESS_GROUP_ID>0</INC_BUSINESS_GROUP_ID><BUSINESS_GROUP_ID_LIST/><INC_ENTERPRISE_ID>0</INC_ENTERPRISE_ID><ENTERPRISE_ID_LIST/>\n");
		buffer.append("<G_1>\n");
		
		List<DataSetElement> dataSetElementList=dataSet.getDataSetElementList();
		for (DataSetElement dataSetElement:dataSetElementList) {
			buffer.append("<"+dataSetElement.getName()+">"+dataSetElement.getSampleValue()+"</"+dataSetElement.getName()+">\n");
		}
		buffer.append("</G_1>\n");
		buffer.append("</DATA_DS>\n");
		
		ModelUtils.writeToFile(outputFile,buffer.toString(),false); 
	}
	
	public static String uploadObjectBIWebService(WebServiceInfo webServiceInfo,CatalogService catalogService,
			String objectAbsolutePathURL,File objectFile,String objectType) throws Exception {
		//FileUtils.println("uploadObjectBIWebService, objectAbsolutePathURL:"+objectAbsolutePathURL);

		Path path = Paths.get(objectFile.getAbsolutePath());
		//FileUtils.println("uploadObjectBIWebService, path:"+path);
		byte[] objectZippedData=Files.readAllBytes(path);
		//FileUtils.println("uploadObjectBIWebService, objectZippedData:"+objectZippedData);
		String returnValue=catalogService.uploadObject(objectAbsolutePathURL, objectType, objectZippedData, webServiceInfo.getUsername(), webServiceInfo.getPassword());
		return returnValue;
	}
	
	public static boolean objectExistBIWebService(WebServiceInfo webServiceInfo,CatalogService catalogService,String datasetObjectAbsolutePathURL) throws Exception {
		//System.out.println(new java.util.Date()+" objectExistBIWebService, datasetObjectAbsolutePathURL: '"+datasetObjectAbsolutePathURL+"'");
		boolean objectExist=catalogService.objectExist(datasetObjectAbsolutePathURL, webServiceInfo.getUsername(), webServiceInfo.getPassword());
		//System.out.println(new java.util.Date()+" objectExistBIWebService, objectExist: "+objectExist);
		
		return objectExist;
	}
	
	public static boolean deleteObjectBIWebService(WebServiceInfo webServiceInfo,CatalogService catalogService,String datasetObjectAbsolutePathURL) throws Exception {
		//System.out.println(new java.util.Date()+" objectExistBIWebService, datasetObjectAbsolutePathURL: '"+datasetObjectAbsolutePathURL+"'");
		boolean objectExist=catalogService.deleteObject(datasetObjectAbsolutePathURL, webServiceInfo.getUsername(), webServiceInfo.getPassword());
		//System.out.println(new java.util.Date()+" objectExistBIWebService, objectExist: "+objectExist);
		
		return objectExist;
	}
		
	public static CatalogService getCatalogService(WebServiceInfo webServiceInfo) throws Exception {
		String webServiceEndpointUrl=webServiceInfo.getWebServiceEndpointUrl();
		//System.out.println("getCatalogService webServiceEndpointUrl:" +webServiceEndpointUrl);
		if(!webServiceEndpointUrl.endsWith("?wsdl") && !webServiceEndpointUrl.endsWith("?WSDL")) {
			webServiceEndpointUrl += "?WSDL";
		}
		URL url=new URL(webServiceEndpointUrl); 
		CatalogService_Service catalogService_Service = new CatalogService_Service(url);
		CatalogService catalogService = catalogService_Service.getCatalogService();
		Map<String, Object> requestContext = ((BindingProvider) catalogService).getRequestContext();
		requestContext.put(BindingProvider.USERNAME_PROPERTY, webServiceInfo.getUsername());
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, webServiceInfo.getPassword());
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webServiceInfo.getWebServiceEndpointUrl());		
		return catalogService;
	}
	
	public static ReportService getReportService(WebServiceInfo webServiceInfo) throws Exception {
		String webServiceEndpointUrl=webServiceInfo.getWebServiceEndpointUrl();
		//System.out.println("getReportService webServiceEndpointUrl:" +webServiceEndpointUrl);
		if(!webServiceEndpointUrl.endsWith("?wsdl") && !webServiceEndpointUrl.endsWith("?WSDL")) {
			webServiceEndpointUrl += "?WSDL";
		}
		URL url=new URL(webServiceEndpointUrl);
		ReportService_Service reportService_Service = new ReportService_Service(url);
		ReportService reportService = reportService_Service.getReportService();

		Map<String, Object> requestContext = ((BindingProvider) reportService).getRequestContext();
		requestContext.put(BindingProvider.USERNAME_PROPERTY, webServiceInfo.getUsername());
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, webServiceInfo.getPassword());
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webServiceInfo.getWebServiceEndpointUrl());
		
		return reportService;
	}
	
	public static void createCatalogFolderBIWebService(WebServiceInfo webServiceInfo,CatalogService catalogService,String folderAbsolutePath) throws Exception {
		FileUtils.println("createCatalogFolderBIWebService, folderAbsolutePath:"+folderAbsolutePath);
		String returnValue=catalogService.createFolder(folderAbsolutePath,  webServiceInfo.getUsername(), webServiceInfo.getPassword());
		FileUtils.println(" returnValue: "+returnValue);
	}
	
	public static boolean isObjectExist(WebServiceInfo webServiceInfo,CatalogService catalogService,String folderAbsolutePath) throws Exception {
		FileUtils.println("isObjectExist, checking if objectExist:"+folderAbsolutePath);
		boolean objectExist=catalogService.objectExist(folderAbsolutePath, webServiceInfo.getUsername(), webServiceInfo.getPassword());
		FileUtils.println("isObjectExist, objectExist:"+objectExist);
				
		return objectExist;
	}
	
	public static Set<String> getFolderContents(WebServiceInfo webServiceInfo,CatalogService catalogService,String folderAbsolutePath) throws Exception {
		FileUtils.println("getFolderContents, folderAbsolutePath:"+folderAbsolutePath);

		CatalogContents catalogContents=catalogService.getFolderContents(folderAbsolutePath, webServiceInfo.getUsername(), webServiceInfo.getPassword());
		ArrayOfItemData arrayOfItemData=catalogContents.getCatalogContents();
		Set<String> toReturn=new HashSet<String>();
		
		if (arrayOfItemData==null) {
			return toReturn;
		}
		List<ItemData> itemList=arrayOfItemData.getItem();
		FileUtils.println("getFolderContents, itemList.size():"+itemList.size());
		for (ItemData itemData:itemList) {
			toReturn.add(itemData.getDisplayName());
			FileUtils.println("getFolderContents, itemData.getDisplayName(): '"+itemData.getDisplayName()+"'");
		}
		return toReturn;
	}
			
	public static DataSet createDataSetObject(File sqlFile,ExtractInventoryRecord extractInventoryRecord,String dataSetIdentifier) throws Exception {
		DataSet dataSet=new DataSet();
		if (sqlFile==null) {
			return dataSet;
		}
		Inventory inventory=extractInventoryRecord.getInventory();
		dataSet.setInventory(inventory);
		dataSet.setIdentifier(dataSetIdentifier);
		dataSet.setDescription(new java.util.Date()+" Data Model for Inventory: '"+inventory.getName()+"'");
		
		dataSet.setSQLFile(sqlFile);
		String reverseFileContent=FileUtils.readContentsFromSQLFile(sqlFile);	
		dataSet.setSqlQuery(reverseFileContent);
		
		verifySQLExtraColumnsPresence(reverseFileContent);
		
		List<DataSetElement> dataSetElementList=new ArrayList<DataSetElement>();
		
		int fieldCounter=1;
		if (extractInventoryRecord.hasDFFLogicToApply() || extractInventoryRecord.hasGDFLogicToApply()) {
			List<Field> fieldsUsedForPrimaryKey=inventory.getFieldsUsedForPrimaryKey();
			for (Field field:fieldsUsedForPrimaryKey) {
				String parentName=field.getParentName();
				if (parentName==null || parentName.isEmpty()) {
					continue;
				}
				DataSetElement dataSetElement=new DataSetElement();
				dataSetElementList.add(dataSetElement);

				String fieldName=field.getNameHash();
				if (fieldName==null || fieldName.isEmpty()) {
					throw new Exception("Invalid Inventory, missing field hash name.");
				}
				int indexOf=reverseFileContent.toLowerCase().indexOf(fieldName.toLowerCase());
				if (indexOf==-1) {
					throw new Exception("Invalid SQL, cannot find the field hash name: '"+fieldName+"'");
				}
				dataSetElement.setName(fieldName);
				dataSetElement.setValue(fieldName);
				dataSetElement.setLabel(fieldName);
				dataSetElement.setDataType("xsd:string");
				dataSetElement.setFieldOrder(fieldCounter);
				dataSetElement.setSampleValue("V"+fieldCounter);
				fieldCounter++;
			}
			
			DataSetElement dataSetElement=new DataSetElement();
			dataSetElementList.add(dataSetElement);
			dataSetElement.setName(ExtractConstants.RES_CONTEXT_CODE);
			dataSetElement.setValue(ExtractConstants.RES_CONTEXT_CODE);
			dataSetElement.setLabel(ExtractConstants.RES_CONTEXT_CODE);
			dataSetElement.setDataType("xsd:string");
			dataSetElement.setFieldOrder(fieldCounter);
			dataSetElement.setSampleValue("V"+fieldCounter);
			fieldCounter++;
			
			List<String> attributes=extractInventoryRecord.getAttributes();
			if (attributes.isEmpty()) {
				if (extractInventoryRecord.hasDFFLogicToApply()) {
					throw new Exception("Invalid SQL, cannot find comma separated list of DFF attributes for the parameter '"+ExtractConstants.DFF_ATTRIBUTES+"'");
				}
				else {
					throw new Exception("Invalid SQL, cannot find comma separated list of GDF attributes for the parameter '"+ExtractConstants.GDF_ATTRIBUTES+"'");
				}
			}
			int dffFieldsCounter=30;
			for (String attribute:attributes) {
				for (int i=1;i<=dffFieldsCounter;i++) {
					dataSetElement=new DataSetElement();
					dataSetElementList.add(dataSetElement);
					
					String name=attribute+i;
					
					dataSetElement.setName(name);
					dataSetElement.setValue(name);
					dataSetElement.setLabel(name);
					dataSetElement.setDataType("xsd:string");
					dataSetElement.setFieldOrder(fieldCounter);
					dataSetElement.setSampleValue("V"+fieldCounter);
					fieldCounter++;
				}
			}			
		}
		else {
			List<Field> fieldNamesUsedForDataEntry=inventory.getFieldsUsedForDataEntry();
			for (Field field:fieldNamesUsedForDataEntry) {
				DataSetElement dataSetElement=new DataSetElement();
				dataSetElementList.add(dataSetElement);
				// IMPORTANT: BI requires the field names to be the same as the aliases in the sql query!!! So using the hash_name from the inventory
				String fieldName=field.getNameHash();
				if (fieldName==null || fieldName.isEmpty()) {
					throw new Exception("Invalid Inventory, missing field hash name.");
				}
				int indexOf=reverseFileContent.toLowerCase().indexOf(fieldName.toLowerCase());
				if (indexOf==-1) {
					throw new Exception("Invalid SQL, cannot find the field hash name: '"+fieldName+"'");
				}
				
				dataSetElement.setName(fieldName);
				dataSetElement.setValue(fieldName);
				dataSetElement.setLabel(fieldName);
				dataSetElement.setDataType("xsd:string");
				dataSetElement.setFieldOrder(fieldCounter);
				dataSetElement.setSampleValue("V"+fieldCounter);
				fieldCounter++;
			}
		}		
		
		DataSetElement dataSetElement=getDataSetElement("RSC_LAST_UPDATED_BY",fieldCounter++,"xsd:string","");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_LAST_UPDATE_DATE",fieldCounter++,"xsd:date","01-JAN-2014 00:00:00");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_CREATED_BY",fieldCounter++,"xsd:string","0");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_CREATION_DATE",fieldCounter++,"xsd:date","01-JAN-2014 00:00:00");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_LEDGER_ID",fieldCounter++,"xsd:integer","0");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_CHART_OF_ACCOUNTS_ID",fieldCounter++,"xsd:integer","0");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_BUSINESS_UNIT_ID",fieldCounter++,"xsd:integer","0");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_LEGAL_ENTITY_ID",fieldCounter++,"xsd:integer","0");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_ORGANIZATION_ID",fieldCounter++,"xsd:integer","0");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_BUSINESS_GROUP_ID",fieldCounter++,"xsd:integer","0");
		dataSetElementList.add(dataSetElement);
		dataSetElement=getDataSetElement("RSC_ENTERPRISE_ID",fieldCounter++,"xsd:integer","0");
		dataSetElementList.add(dataSetElement);
	
		dataSet.setDataSetElementList(dataSetElementList);
		
		return dataSet;
	}
	
	private static void verifySQLExtraColumnsPresence(String sourceText) throws Exception {
		verifyTextPresence(sourceText,"RSC_LAST_UPDATED_BY");
		verifyTextPresence(sourceText,"RSC_LAST_UPDATE_DATE");
		verifyTextPresence(sourceText,"RSC_CREATED_BY");
		verifyTextPresence(sourceText,"RSC_CREATION_DATE");
		verifyTextPresence(sourceText,"RSC_LEDGER_ID");
		verifyTextPresence(sourceText,"RSC_CHART_OF_ACCOUNTS_ID");
		verifyTextPresence(sourceText,"RSC_BUSINESS_UNIT_ID");
		verifyTextPresence(sourceText,"RSC_LEGAL_ENTITY_ID");
		verifyTextPresence(sourceText,"RSC_ORGANIZATION_ID");
		verifyTextPresence(sourceText,"RSC_BUSINESS_GROUP_ID");
		verifyTextPresence(sourceText,"RSC_ENTERPRISE_ID");
	}
	
	private static void verifyTextPresence(String sourceText,String textToFind) throws Exception {
		int indexOf=sourceText.indexOf(textToFind);
		if (indexOf==-1) {
			throw new Exception("Missing column '"+textToFind+"' in the SQL query");
		}
	}
	
	private static DataSetElement getDataSetElement(String name,int fieldCounter,String dataType,String sampleValue) {
		DataSetElement dataSetElement=new DataSetElement();
		dataSetElement.setName(name);
		dataSetElement.setValue(name);
		dataSetElement.setLabel(name);
		dataSetElement.setDataType(dataType);
		dataSetElement.setFieldOrder(fieldCounter);
		dataSetElement.setSampleValue(sampleValue);
		
		return dataSetElement;
	}
	
	public static void saveOutput(byte[] baReport,File outputFile) throws Exception {	
		FileOutputStream fio = new FileOutputStream(outputFile,false);
		fio.write(baReport);
		fio.close();
	}
	
	public static InventoryFieldsInformation getInventoryFieldsInformation(Inventory inventory) throws Exception {
		InventoryFieldsInformation inventoryFieldsInformation=new InventoryFieldsInformation();
		List<Field> fieldNamesUsedForDataEntry=inventory.getFieldsUsedForDataEntry();
		int fieldPosition=1;
		for (Field field:fieldNamesUsedForDataEntry) {
			FieldInformation fieldInformation=new FieldInformation();
			fieldInformation.setPosition(fieldPosition);
			fieldPosition++;

			inventoryFieldsInformation.getHashNameToFieldInformationMap().put(field.getNameHash(),fieldInformation);
		}
		return inventoryFieldsInformation;
	}
	
	public static int getExtractedCountFromBIPubliser(byte[] outputBytes) throws Exception {
		int returnCount = 0;
		
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new ByteArrayInputStream(outputBytes));

		for(String rowTag:BI_PUBLISHER_ROW_TAG_NAMES) {
	        NodeList nodeList = document.getElementsByTagName(rowTag);
	        int recordCount=nodeList.getLength();
	        
	        if (recordCount>0)
	        {
	        	try{
	        		for (int i = 0; i < nodeList.getLength(); i++) {
	        			Node node = nodeList.item(i);
	        			if (node.getNodeType() == Node.ELEMENT_NODE) {
	        				Element element = (Element) node;
	        				String value = element.getTextContent().trim();
	        				
	        				returnCount = Integer.parseInt(value);
	        			}
	        		}
	        	} catch (Exception ex) {
	        		throw new Exception ("Error getting record count");
	        	}
	        }
		}
        return returnCount;
	}
		
	public static List<ExtractDataRow> getExtractDataRowsFromBIPublisher(Inventory inventory,File biXMLFile) throws Exception {
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(biXMLFile);
		
        PrintWriter pw=null;
		List<ExtractDataRow> dataRows=new ArrayList<ExtractDataRow>();
        NodeList nodeList = getNodeList(document);
        if (nodeList!=null && nodeList.getLength()>0)
        {
        	try{
        		InventoryFieldsInformation inventoryFieldsInformation=getInventoryFieldsInformation(inventory);
                List<Field> fieldNamesUsedForDataEntry=inventory.getFieldsUsedForDataEntry();
                int inventoryColumnsCount=fieldNamesUsedForDataEntry.size();
        		for (int i = 0; i < nodeList.getLength(); i++) {
        			Node node = nodeList.item(i);
        			if (node.getNodeType() == Node.ELEMENT_NODE) {
        				//FileUtils.println("data row: "+i+" node:"+node);
        				Element element = (Element) node;

        				BIRowInformation biRowInformation=getInventoryDataEntryFieldOnlyTagNameToValueMap(node);

        				// Validate if:
        				// - there are aliases in the query that are not defined in the inventory.
        				// - there are aliases in the query which position does not match the field in the inventory.
        				// - there are fields in the inventory without the corresponding alias in the data.
        				// Optimization: only check the first row as the others will have the same format.
        				if (i==0) {
        					int tagPosition=1;
        					for ( String tagName:biRowInformation.getOrderedTagNamesList()) {
        						FieldInformation fieldInformation=inventoryFieldsInformation.getHashNameToFieldInformationMap().get(tagName);
        						if (fieldInformation==null) {
        							throw new Exception("The SQL query has a column alias '"+tagName+"' which does not belong to the inventory.");
        						}
        						int fieldPosition=fieldInformation.getFieldPosition();
        						if (fieldPosition!=tagPosition) {
        							throw new Exception("The SQL query has a column alias '"+tagName+"' which is not positioned correctly."+
        									" Position in the inventory: "+fieldPosition+" Position in the SQL query: '"+tagPosition+"'");
        						}
        						tagPosition++;
        					}
        					for (Field field:fieldNamesUsedForDataEntry) {
        						FieldInformation fieldInformation=inventoryFieldsInformation.getHashNameToFieldInformationMap().get(field.getNameHash());
        						if (fieldInformation==null) {
        							throw new Exception("The Inventory has a field '"+field.getName()+"' which cannot be found in the query.");
        						}
        					}
        				}
        				
        				ExtractDataRow extractDataRow=new ExtractDataRow();
        				int index=0;
        				String[] dataValues= new String[inventoryColumnsCount];
        				extractDataRow.setDataValues(dataValues);
        				for (Field field:fieldNamesUsedForDataEntry) {
        					String fieldName=field.getNameHash();
        					String value=biRowInformation.getTagNameToValueMap().get(fieldName);
        					//FileUtils.println("fieldName: '"+fieldName+"' value:'"+value+"'");
        					dataValues[index]=value;
        					index++;
        				}
        				setGenericFields(extractDataRow,element);

        				dataRows.add(extractDataRow);
        			}
        		}
        	}
        	finally {
        		IOUtils.closeQuietly(pw);
        	}
        }
		return dataRows;
	}
	
	public static BIRowInformation getInventoryDataEntryFieldOnlyTagNameToValueMap(Node nodeParam) throws Exception {
		BIRowInformation biRowInformation=new BIRowInformation();
		NodeList nodeList =nodeParam.getChildNodes();
		if (nodeList==null) {
			return biRowInformation;
		}
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;

				String tagName=element.getTagName();
				
				if (	tagName.equalsIgnoreCase("RSC_LAST_UPDATED_BY") ||
						tagName.equalsIgnoreCase("RSC_LAST_UPDATE_DATE") ||
						tagName.equalsIgnoreCase("RSC_CREATED_BY") ||
						tagName.equalsIgnoreCase("RSC_CREATION_DATE") ||
						tagName.equalsIgnoreCase("RSC_LEDGER_ID") ||
						tagName.equalsIgnoreCase("RSC_CHART_OF_ACCOUNTS_ID") ||
						tagName.equalsIgnoreCase("RSC_BUSINESS_UNIT_ID") ||
						tagName.equalsIgnoreCase("RSC_LEGAL_ENTITY_ID") ||
						tagName.equalsIgnoreCase("RSC_ORGANIZATION_ID") ||
						tagName.equalsIgnoreCase("RSC_BUSINESS_GROUP_ID") ||
						tagName.equalsIgnoreCase("RSC_ENTERPRISE_ID") ||
						tagName.equalsIgnoreCase("ROWNUM") ||
						tagName.equalsIgnoreCase("RNUM")
				) {
					continue;
				}
				String value=element.getTextContent();
				value=convertBIDateIfApplicable(value);
				//FileUtils.println("tagName: '"+tagName+"' value: '"+value+"'");

				biRowInformation.getTagNameToValueMap().put(tagName, value);
				biRowInformation.getOrderedTagNamesList().add(tagName);
			}
		}
		return biRowInformation;
	}
	
	public static String getDataFromBIOutput(Element element,String fieldName) {
		NodeList fieldNodeList=element.getElementsByTagName(fieldName.toUpperCase()); // XML is case sensitive - and BI Publisher outputs in UPPER case.
		if (fieldNodeList!=null) {
			Node node=fieldNodeList.item(0);
			if (node!=null) {
				String output=node.getTextContent();
				return output;
			}
		}
		return "";
	}
	
	private static String convertBIDateIfApplicable(String value) {
		if (value==null) {
			return value;
		}

		int count = value.split("-",-1).length-1;
		if (count!=2){
			return value;
		}
		
		try {			
			/*
			 *  ALL sql queries use either mask: '20-07-2012 00:00:00' or '20-07-2012'
			 *  depending if there is a time component in the Application form.
			 */
			//System.out.println("value:"+value);
			String patternString  = "[0-9]{2}-[0-9]{2}-[0-9]{4}"; 
			//System.out.println("patternString date:"+patternString);
			Pattern pattern = Pattern.compile(patternString,Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(value);
			boolean isDateOnly = matcher.matches();
			//System.out.println("isDateOnly:"+isDateOnly);
			if (isDateOnly) {
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				Date date = dateFormat.parse(value);
				DateFormat newDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDateStr = newDateFormat.format(date);
				//System.out.println("formatted date:"+formattedDateStr);
				return formattedDateStr;
			}
			else {
				patternString  = "[0-9]{2}-[0-9]{2}-[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}"; 
				//System.out.println("patternString DateTime:"+patternString);
				pattern = Pattern.compile(patternString,Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(value);
				boolean isDateTime = matcher.matches();
				if (isDateTime) {
					DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					Date date = dateFormat.parse(value);
					DateFormat newDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
					String formattedDateStr = newDateFormat.format(date);
					//System.out.println("formatted DateTime:"+formattedDateStr);
					return formattedDateStr;
				}
			}
		}
		catch (ParseException e) {
			FileUtils.println("Unable to convert value to Date, value: '"+value+"'");
		}
		return value;
	}
	
	private static String convertBIDate(String biDateStr) {
		try {
			/*
			 * Removing the GMT component for now. Somehow, it doesn't parse even when using those formats:
			 *  yyyy-MM-dd'T'HH:mm:ss.SSSZ or  yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
			 * Also, keeping in line with the RapidReverse logic where the Date/Time is actually in the DB format
			 * otherwise Configurator will not understand it.
			 */
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			// Example: 2015-12-28T20:01:44.484+07:00  
			// 		or	2016-07-06T23:33:13.654-04:00
			int indexOf=biDateStr.indexOf("+");
			if (indexOf!=-1) {
				biDateStr=biDateStr.substring(0,indexOf);
			}
			else {
				indexOf=biDateStr.lastIndexOf("-");
				if (indexOf!=-1) {
					biDateStr=biDateStr.substring(0,indexOf);
				}
			}

			Date date = dateFormat.parse(biDateStr);
			DateFormat newDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			String formattedDateStr = newDateFormat.format(date);
			//System.out.println("convertBIDate, formattedDateStr:"+formattedDateStr);
			return formattedDateStr;
		}
		catch(Exception e) {
			//e.printStackTrace();
			FileUtils.printStackTrace(e);
		}
		return biDateStr;
	}

	public static void saveExtractDataRowsToRSCXMLFormat(Inventory inventory,List<ExtractDataRow> extractDataRows,File rscXMLFile) throws Exception {
		PrintWriter pw=null;
		try{
			List<Field> fieldNamesUsedForDataEntry=inventory.getFieldsUsedForDataEntry();
			pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(rscXMLFile,false), "UTF8"));
			FileUtils.writeFileHeader(pw,inventory.getName(),fieldNamesUsedForDataEntry,true);
			writeDataRows(pw,extractDataRows,ExtractConstants.DATA_LABEL);
			FileUtils.writeDataFileFooter(pw);
		}
		finally {
			IOUtils.closeQuietly(pw);
		}
	}
	
	public static void writeDataRows(PrintWriter pw,List<ExtractDataRow> dataRows,String label)	{
		for ( ExtractDataRow dataRow:dataRows ){
			writeDataRow(pw,dataRow,label);
		}
		pw.flush();
	}
	
	private static void writeDataRow(PrintWriter pw,ExtractDataRow dataRow,String label) {
		pw.println("<r>");
		for ( String column : dataRow.getDataValues() ) {
			if ( column == null || column.trim().length() == 0 ){
				pw.println("<c/>");
			}
			else{
				pw.println("<c>" + StringEscapeUtils.escapeXml10(Utils.stripNonValidXMLCharacters(column)) + "</c>");
			}
		}
		pw.println("<c>" + StringEscapeUtils.escapeXml10(label) + "</c>");
		pw.println("<c/>");
		String userName=dataRow.getRscLastUpdatedBy();
		pw.println("<c>" +  userName+ "</c>");
		pw.println("<c>" + dataRow.getRscLastUpdateDate() + "</c>");
		pw.println("</r>");
	}
	
	public static File getExtractSQLFile(WebServiceInfo webServiceInfo,ReportService reportService,String reportAbsolutePath,
			SortedMap<String, SortedSet<File>> inventoryNameToSQLFileMap, String inventoryName) 
					throws Exception {
		SortedSet<File> sqlFiles = inventoryNameToSQLFileMap.get(inventoryName);
		if (sqlFiles==null) {
			throw new Exception("Unable to locate the SQL file.");
		}
		if (sqlFiles.size()==1) {
			return sqlFiles.first();
		}
		FileUtils.println("Inventory: "+inventoryName+" is mapped to "+sqlFiles.size()+" SQL files.");
		for (File sqlFile:sqlFiles) {
			try{
				if (sqlFile==null ) {
					throw new Exception("Cannot find the SQL file (check the project XML file). No records extracted.");
				}
				FileUtils.println("Running query from SQL file: '"+sqlFile.getAbsolutePath()+"' ...");
				String sqlFileContent=FileUtils.readContentsFromSQLFile(sqlFile);
				
				List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
				
				String countSQLQuery = "SELECT COUNT(*) from ( " + sqlFileContent + " )";
				
				ParameterNameValue parameterNameValue=new ParameterNameValue();
				parametersList.add(parameterNameValue);
				parameterNameValue.setName("param1");
				parameterNameValue.setValue(countSQLQuery);
				
				byte[] outputBytes=ExtractUtils.runReportWithStringParameters(webServiceInfo,reportService,reportAbsolutePath,parametersList);
				int count = getExtractedCountFromBIPubliser(outputBytes);
				
				FileUtils.println(String.format("Query returned '%d' so using this SQL file: '%s' ...", count, sqlFile.getAbsolutePath()));
				return sqlFile;
			}
			catch (Exception e) {
				FileUtils.println("Failure: error: "+e.getMessage());
			}
		}
		return null;
	}
	
	public static byte[] runReportWithStringParameters(WebServiceInfo webServiceInfo,ReportService reportService,String reportAbsolutePath,
			List<ParameterNameValue> parametersList) throws Exception {
		ReportRequest repRequest = new ReportRequest();
		repRequest.setReportAbsolutePath(reportAbsolutePath);
		// repRequest.setAttributeTemplate("Chart and Table Layout");
		//repRequest.setAttributeFormat("pdf");
		//repRequest.setAttributeFormat("text/xml"); // DO NOT USE THIS AS IT IS THE SERVER REPORT PROPERTIES THAT CONTROLS THE OUTPUT FORMAT.
		// SO IF THE REPORT IS SET AS XML AS DEFAULT THEN THAT'S WHAT WE WILL RECEIVE.
		// repRequest.setAttributeLocale("en-US");
		repRequest.setSizeOfDataChunkDownload(-1);
		
		ParamNameValues paramNameValues = new ParamNameValues();
		
		ArrayOfParamNameValue arrayOfParamNameValue = new 	ArrayOfParamNameValue();
		for (ParameterNameValue parameterNameValue:parametersList) {
			ParamNameValue paramNameValue = new ParamNameValue();
			ArrayOfString arrayOfString = new ArrayOfString();
			paramNameValue.setName(parameterNameValue.getName());
			arrayOfString.getItem().add(parameterNameValue.getValue());
			paramNameValue.setValues(arrayOfString);
			arrayOfParamNameValue.getItem().add(paramNameValue);
		}		

		paramNameValues.setListOfParamNameValues(arrayOfParamNameValue);
		repRequest.setParameterNameValues(paramNameValues);

		ReportResponse reportResponse = reportService.runReport(repRequest, webServiceInfo.getUsername(), webServiceInfo.getPassword());
		//saveOutput(reportResponse.getReportBytes(),new File("AAA.xml"));
		byte[] baReport = reportResponse.getReportBytes();
		
		return baReport;
	}
		
	public static void unpackExtractionPackage(File archiveFile) throws Exception {
		File tempOutputFolder = createTempExtractionPackageFolder(archiveFile);
		SevenZipUtils.decompressFile(archiveFile,tempOutputFolder);
		//APIDataGridOptionsFrame.unzipBWPFile(archiveFile,tempOutputFolder);
	}
	
	private static File createTempExtractionPackageFolder(File archiveFile) throws Exception {
		File tempOutputFolder = new File(Config.getTempFolder(),ExtractConstants.EXTRACT_ZIP_TEMP_FOLDER);
		org.apache.commons.io.FileUtils.deleteDirectory(tempOutputFolder);
		tempOutputFolder.mkdirs();
		tempOutputFolder = new File(tempOutputFolder,archiveFile.getName());
		org.apache.commons.io.FileUtils.deleteDirectory(tempOutputFolder);
		tempOutputFolder.mkdirs();
		
		return tempOutputFolder;
	}
	
	public static void setTime(ExtractInventoryRecord extractInventoryRecord) {
		Long currentTime=System.currentTimeMillis();
		String msg=Utils.getExecutionTime(extractInventoryRecord.getStartTime(),currentTime);
		extractInventoryRecord.setExecutionTime(msg);
		extractInventoryRecord.setRawTimeInSecs(UIUtils.getRawTimeInSecs(extractInventoryRecord.getStartTime()));
	}
	
	public static String ORAGANIZATION="RSC_ORGANIZATION_ID";
	public static String BUSINESS_UNIT="RSC_BUSINESS_UNIT_ID";
	public static String LEGAL_ENTITY="RSC_LEGAL_ENTITY_ID";
	public static String LEDGER="RSC_LEDGER_ID";
	public static String CHART_OF_ACCOUNT="RSC_CHART_OF_ACCOUNTS_ID";
	public static String BUSINESS_GROUP="RSC_BUSINESS_GROUP_ID";
	public static String ENTERPRISE="RSC_ENTERPRISE_ID";
	
	public static String REGEX_CAST_NUMBER = "cast[ ]*\\([ ]*null[ ]+as[ ]+number\\)[ ]+as[ ]+";
	public static String REGEX_NORMAL = "null[ ]+as[ ]+";
	public static String REGEX_MIN = "null[ ]+";
	
	public static Pattern ORAGANIZATION_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+ORAGANIZATION, Pattern.CASE_INSENSITIVE);
	public static Pattern ORAGANIZATION_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+ORAGANIZATION, Pattern.CASE_INSENSITIVE);
	public static Pattern ORAGANIZATION_REGEX_MIN_PATTERN = Pattern.compile(REGEX_MIN+ORAGANIZATION, Pattern.CASE_INSENSITIVE);
	
	public static Pattern BUSINESS_UNIT_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+BUSINESS_UNIT, Pattern.CASE_INSENSITIVE);
	public static Pattern BUSINESS_UNIT_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+BUSINESS_UNIT, Pattern.CASE_INSENSITIVE);
	public static Pattern BUSINESS_UNIT_REGEX_MIN_PATTERN = Pattern.compile(REGEX_MIN+BUSINESS_UNIT, Pattern.CASE_INSENSITIVE);
	
	public static Pattern LEGAL_ENTITY_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+LEGAL_ENTITY, Pattern.CASE_INSENSITIVE);
	public static Pattern LEGAL_ENTITY_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+LEGAL_ENTITY, Pattern.CASE_INSENSITIVE);
	public static Pattern LEGAL_ENTITY_REGEX_MIN_PATTERN = Pattern.compile(REGEX_MIN+LEGAL_ENTITY, Pattern.CASE_INSENSITIVE);
	
	public static Pattern LEDGER_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+LEDGER, Pattern.CASE_INSENSITIVE);
	public static Pattern LEDGER_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+LEDGER, Pattern.CASE_INSENSITIVE);
	public static Pattern LEDGER_REGEX_MIN_PATTERN = Pattern.compile(REGEX_MIN+LEDGER, Pattern.CASE_INSENSITIVE);
	
	public static Pattern CHART_OF_ACCOUNT_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+CHART_OF_ACCOUNT, Pattern.CASE_INSENSITIVE);
	public static Pattern CHART_OF_ACCOUNT_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+CHART_OF_ACCOUNT, Pattern.CASE_INSENSITIVE);
	public static Pattern CHART_OF_ACCOUNT_REGEX_MIN_PATTERN = Pattern.compile(REGEX_MIN+CHART_OF_ACCOUNT, Pattern.CASE_INSENSITIVE);
	
	public static Pattern BUSINESS_GROUP_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+BUSINESS_GROUP, Pattern.CASE_INSENSITIVE);
	public static Pattern BUSINESS_GROUP_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+BUSINESS_GROUP, Pattern.CASE_INSENSITIVE);
	public static Pattern BUSINESS_GROUP_REGEX_MIN_PATTERN = Pattern.compile(REGEX_MIN+BUSINESS_GROUP, Pattern.CASE_INSENSITIVE);
	
	public static Pattern ENTERPRISE_REGEX_CAST_NUMBER_PATTERN = Pattern.compile(REGEX_CAST_NUMBER+ENTERPRISE, Pattern.CASE_INSENSITIVE);
	public static Pattern ENTERPRISE_REGEX_NORMAL_PATTERN = Pattern.compile(REGEX_NORMAL+ENTERPRISE, Pattern.CASE_INSENSITIVE);
	public static Pattern ENTERPRISE_REGEX_MIN_PATTERN = Pattern.compile(REGEX_MIN+ENTERPRISE, Pattern.CASE_INSENSITIVE);
	
	public static void computeInformationFromSQLFile(File sqlFile,ExtractInventoryRecord extractInventoryRecord) throws Exception {
		String sqlFileContent=FileUtils.readContentsFromSQLFile(sqlFile).toLowerCase();

		boolean hasOrganizationId=
				(!ORAGANIZATION_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() && 
						!ORAGANIZATION_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find() && 
						!ORAGANIZATION_REGEX_MIN_PATTERN.matcher(sqlFileContent).find() );
		
		boolean hasBusinessUnitId=
				(!BUSINESS_UNIT_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() && 
						!BUSINESS_UNIT_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find() && 
						!BUSINESS_UNIT_REGEX_MIN_PATTERN.matcher(sqlFileContent).find() );
				
		boolean hasLegalEntityId=
				(!LEGAL_ENTITY_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() && 
						!LEGAL_ENTITY_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find() && 
						!LEGAL_ENTITY_REGEX_MIN_PATTERN.matcher(sqlFileContent).find() );
		
		boolean hasLedgerId=
				(!LEDGER_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() && 
						!LEDGER_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find()&& 
						!LEDGER_REGEX_MIN_PATTERN.matcher(sqlFileContent).find() );
		
		boolean hasCOAId=
				(!CHART_OF_ACCOUNT_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() && 
						!CHART_OF_ACCOUNT_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find()&& 
						!CHART_OF_ACCOUNT_REGEX_MIN_PATTERN.matcher(sqlFileContent).find() );
		
		boolean hasBusinessGroupId=
				(!BUSINESS_GROUP_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() && 
						!BUSINESS_GROUP_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find()&& 
						!BUSINESS_GROUP_REGEX_MIN_PATTERN.matcher(sqlFileContent).find() );
		
		boolean hasEnterpriseId=
				(!ENTERPRISE_REGEX_CAST_NUMBER_PATTERN.matcher(sqlFileContent).find() && 
						!ENTERPRISE_REGEX_NORMAL_PATTERN.matcher(sqlFileContent).find()&& 
						!ENTERPRISE_REGEX_MIN_PATTERN.matcher(sqlFileContent).find() );
		
		extractInventoryRecord.setHasLedgerId(hasLedgerId);
		extractInventoryRecord.setHasCOAId(hasCOAId);
		extractInventoryRecord.setHasBusinessUnitId(hasBusinessUnitId);
		extractInventoryRecord.setHasLegalEntityId(hasLegalEntityId);
		extractInventoryRecord.setHasOrganizationId(hasOrganizationId);
		extractInventoryRecord.setHasBusinessGroupId(hasBusinessGroupId);
		extractInventoryRecord.setHasEnterpriseId(hasEnterpriseId);		
				
		boolean hasGetCodeCombinationFunction=sqlFileContent.toLowerCase().indexOf(ExtractConstants.FUNCTION_GET_CODE_COMBINATION.toLowerCase())!=-1;
		if (hasGetCodeCombinationFunction) {
			extractInventoryRecord.getFunctionNamesSet().add(ExtractConstants.FUNCTION_GET_CODE_COMBINATION);
		}
		boolean hasGetSystemProfileValueFunction=sqlFileContent.toLowerCase().indexOf(ExtractConstants.FUNCTION_GET_SYSTEM_PROFILE_VALUE.toLowerCase())!=-1;
		if (hasGetSystemProfileValueFunction) {
			extractInventoryRecord.getFunctionNamesSet().add(ExtractConstants.FUNCTION_GET_SYSTEM_PROFILE_VALUE);
		}
		boolean hasDFFLogicToApply=sqlFileContent.toLowerCase().indexOf(ExtractConstants.DFF_LOGIC_TO_APPLY.toLowerCase())!=-1;
		boolean hasGDFLogicToApply=sqlFileContent.toLowerCase().indexOf(ExtractConstants.GDF_LOGIC_TO_APPLY.toLowerCase())!=-1;
		boolean hasKFFLogicToApply=sqlFileContent.toLowerCase().indexOf(ExtractConstants.KFF_LOGIC_TO_APPLY.toLowerCase())!=-1;
		if (hasDFFLogicToApply || hasGDFLogicToApply || hasKFFLogicToApply) {
			String[] lines=sqlFileContent.split(ExtractConstants.END_OF_LINE);
			String key=null;
			if (hasDFFLogicToApply) {
				key=ExtractConstants.DFF_ATTRIBUTES;
				extractInventoryRecord.setHasDFFLogicToApply(true);
			}
			else
			if (hasKFFLogicToApply) {
				extractInventoryRecord.setHasKFFLogicToApply(true);
			}
			else {
				key=ExtractConstants.GDF_ATTRIBUTES;
				extractInventoryRecord.setHasGDFLogicToApply(true);
			}
			if (key!=null) {
				setAttributes(key,lines,extractInventoryRecord);
			}
		}
	}
	
	private static void setAttributes(String key,String[] lines,ExtractInventoryRecord extractInventoryRecord) {
		for (String line:lines) {
			int indexOfAttributes=line.indexOf(key.toLowerCase());
			if (indexOfAttributes==-1) {
				continue;
			}
			if (indexOfAttributes!=-1) {
				try{
					int afterEqualSignIndex=indexOfAttributes+key.length()+1;
					String attributes=line.substring(afterEqualSignIndex);
					String[] attributesArray=attributes.split(",");
					if (attributesArray!=null) {
						extractInventoryRecord.setAttributes(Arrays.asList(attributesArray));
					}					
				}
				catch(Exception e) {
					FileUtils.printStackTrace(e);
				}
			}				
		}
	}
	
	public static void zipFolderNonRecursive(File srcFolder,File destZipFile) throws Exception {
		if (!srcFolder.exists() || !FileUtils.hasAtLeastOneFile(srcFolder)) {
			return;
		}
		ZipOutputStream zipOutputStream = null;
		try{
			FileOutputStream fileOutputStream = new FileOutputStream(destZipFile);
			zipOutputStream = new ZipOutputStream(fileOutputStream);
			for (File file : srcFolder.listFiles()) {
				byte[] buf = new byte[1024];
				int len;
				FileInputStream in=null;
				try{
					in = new FileInputStream(file);
					zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
					while ((len = in.read(buf)) > 0) {
						zipOutputStream.write(buf, 0, len);
					}
				}
				finally{
					IOUtils.closeQuietly(in);
				}
			}
			zipOutputStream.flush();
		}
		finally{
			IOUtils.closeQuietly(zipOutputStream);
		}
	}
	
	public static String processBIConnectionExceptions(Throwable e,String url,String token1,String token2) {
		FileUtils.printStackTrace(e);
		String message=e.getMessage();
				
		if (message!=null && (
				message.toLowerCase().startsWith("No such operation".toLowerCase()) ||
				message.toLowerCase().startsWith("The server sent HTTP status code 200".toLowerCase())||
				message.toLowerCase().startsWith("HTTP transport error".toLowerCase())||
				message.toLowerCase().startsWith("The server sent HTTP status code 404".toLowerCase())||
				message.toLowerCase().startsWith("Unsupported endpoint address".toLowerCase())
		)) {
			return "<html>Invalid "+token1+" Web Service URL specified! Usually the format is: "+
					"<b>http://hostname:port/xmlpserver/services/v2/"+token2+"</b>"+
					"<br/>Please recheck with your Administrator.<br/>Error: "+message+"</html>";
		}
		if (message!=null && message.toLowerCase().startsWith("oracle.xdo.webservice.exception.InvalidParametersException".toLowerCase())) {
			return "<html>Invalid "+token1+" folder path specified! Verify that the Path is created in BI Publisher"+
					" and also that it is a <b><u>case-sensitive</u></b> match.<br/>Error: "+message+"</html>";
		}
		if (message!=null && message.toLowerCase().startsWith("No AccessDeniedException operation".toLowerCase())) {
			return "<html>Invalid Credentials for the URL: '"+url+"'<br/>Please recheck with your Administrator.<br/>Error: "+message+"</html>";
		}
		
		return "<html>Validation failed.<br/>Error: "+message+"</html>";
	}

	public static List<String[]> getDataRows(File dataFile) throws Exception {
		List<String[]> dataRows=InjectUtils.parseXMLDataFile(dataFile);
		return dataRows;
	}
	
	public static void saveGridToExcel(File outputFile,List<ExtractInventoryRecord> filteredExtractInventoryRecordList) throws Exception {
		List<String[]> rows=new ArrayList<String[]>();
		int counter=0;
		int totalColumnsToDisplay=11;		
		for (ExtractInventoryRecord extractInventoryRecord:filteredExtractInventoryRecordList) {
			counter++;
			int index=0;
			String[] row=new String[totalColumnsToDisplay];
			row[index++]=""+counter;
			String applicationName=extractInventoryRecord.getApplicationName();
			if (applicationName==null) {
				applicationName="";
			}
			row[index++]=applicationName;
			String menuPath=extractInventoryRecord.getFormPath();
			if (menuPath==null) {
				menuPath="";
			}
			row[index++]=menuPath;
			String formName=extractInventoryRecord.getFormName();
			if (formName==null) {
				formName="";
			}
			row[index++]=formName;
			
			SELECTION_LEVEL formType=extractInventoryRecord.getFormType();
			if (formType==null) {
				formType=SELECTION_LEVEL.GLOBAL;
			}
			row[index++]=formType.getLevel();
			
			row[index++]=extractInventoryRecord.getInventoryName();
			
			String status=extractInventoryRecord.getStatus();
			if (status==null) {
				status="";
			}
			row[index++]=status;
			
			int totalRecords=extractInventoryRecord.getTotalRecords();
			if (totalRecords==-1) {
				row[index++]="";
			}
			else {
				row[index++]=""+totalRecords;
			}
			
			String remarks=extractInventoryRecord.getRemarks();
			if (remarks==null) {
				remarks="";
			}
			row[index++]=remarks;
			String executionTime=extractInventoryRecord.getExecutionTime();
			if (executionTime==null) {
				executionTime="";
			}
			row[index++]=executionTime;

			row[index++]=""+extractInventoryRecord.getRawTimeInSecs();

			rows.add(row);
		}
		String[] headerRow=new String[totalColumnsToDisplay];
		int index=0;
		String NEW_LINE="\n";
		
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_ROW_NUM.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_MODULE_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_FORM_PATH.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_FORM_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_FORM_TYPE.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_INVENTORY_NAME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_STATUS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_TOTAL_RECORDS_COUNT.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_REMARKS.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_EXECUTION_TIME.replaceAll(NEW_LINE," ");
		headerRow[index++]=ExtractInventoryGridResultPanel.COLUMN_HEADING_RAW_TIME.replaceAll(NEW_LINE," ");
		rows.add(0,headerRow);

		String sheetTitle="Status";
		ModelUtils.doCreateAndSaveXLSXExcelFile(sheetTitle,rows,outputFile, false, false);
	}
			
	private static Map<String,String> getFieldsToFunctionNameMap(String sqlQuery) throws Exception {
		String[] lines=sqlQuery.split(ExtractConstants.END_OF_LINE);
		Map<String,String> toReturn=new HashMap<String,String>();
		for (String line:lines) {
			int indexOf=line.indexOf(ExtractConstants.SQL_FIELD_TO_APPLY_FUNCTION_KEYWORD);
			if (indexOf==-1) {
				continue;
			}
			int indexOfEqualSign=line.indexOf("=");
			if (indexOfEqualSign==-1) {
				throw new Exception("Missing '=' sign in the SQL query when applying the Function keyword!");
			}
			int indexOfSharpSign=line.indexOf("/");
			if (indexOfSharpSign==-1) {
				throw new Exception("Missing '/' sign in the SQL query when applying the Function keyword!");
			}
			String fieldName=line.substring(indexOfEqualSign+1, indexOfSharpSign);
			String functionName=line.substring(indexOfSharpSign+1);
			toReturn.put(fieldName, functionName);
		}
		return toReturn;
	}

	public static List<ExtractDataRow> applyFunctions(
			HookGetCodeCombination hookGetCodeCombination,
			HookGetSystemProfileValue hookGetSystemProfileValue,
			String dynamicSQLReportPath,
			WebServiceInfo reportWebServiceInfo,
			ReportService reportService,
			ExtractInventoryRecord extractInventoryRecord,
			List<ExtractDataRow> extractDataRowList) throws Exception {
		String sqlQuery=extractInventoryRecord.getDataSet().getSqlQuery();
		Map<String,String> fieldsToFunctionNameMap=getFieldsToFunctionNameMap(sqlQuery);
		Iterator<String> iterator=fieldsToFunctionNameMap.keySet().iterator();
		
		List<ExtractDataRow> listToApply=extractDataRowList;
		while(iterator.hasNext()) {
			String fieldName=iterator.next();
			String functionName=fieldsToFunctionNameMap.get(fieldName);
			int fieldIndex=getHashIndex(extractInventoryRecord.getInventory(),fieldName);

			if (functionName.trim().equalsIgnoreCase(ExtractConstants.FUNCTION_GET_CODE_COMBINATION)) {
				listToApply=hookGetCodeCombination.apply(fieldIndex,listToApply);
			}
			else
			if (functionName.trim().equalsIgnoreCase(ExtractConstants.FUNCTION_GET_SYSTEM_PROFILE_VALUE)) {
				listToApply=hookGetSystemProfileValue.apply(dynamicSQLReportPath,reportWebServiceInfo,reportService,fieldIndex,listToApply);
			}
			else {
				throw new Exception("Unsupported function name: '"+functionName+"'");
			}
		}
		return listToApply;
	}
	
	public static int getHashIndex(Inventory inventory,String fieldName) throws Exception {
		List<String> fields=inventory.getHashesForFieldsUsedForDataEntry();
		int index=-1;
		for (String field:fields){
			index++;
			if (field.equalsIgnoreCase(fieldName)) {
				return index;
			}
		}
		throw new Exception("Unable to find the hashName: '"+fieldName+"' in the Inventory as defined in the SQL query (function)");
	}

	public static void saveExtractDataRows(File serializedDataFile,List<ExtractDataRow> extractDataRowList) throws Exception {
		FileOutputStream fos=null;
		ObjectOutputStream oos=null;
		try{
			fos= new FileOutputStream(serializedDataFile);
			oos= new ObjectOutputStream(fos);
			oos.writeObject(extractDataRowList);
		}
		finally {
			if (oos!=null) {
				oos.close();
			}
			if (fos!=null) {
				fos.close();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<ExtractDataRow> loadExtractDataRows(File serializedDataFile) throws Exception {
		FileInputStream fis=null;
		ObjectInputStream ois=null;
        try
        {
    		fis = new FileInputStream(serializedDataFile);
            ois = new ObjectInputStream(fis);
            return (ArrayList<ExtractDataRow>) ois.readObject();
        }
		finally {
			if (ois!=null) {
				ois.close();
			}
			if (fis!=null) {
				fis.close();
			}
		}
	}
	
	public static Map<String,String> getLOVFromXMLReport(byte[] outputBytes,boolean isKeyInSecondColumn) throws Exception {
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(outputBytes);
		Document document = builder.parse(input);
		

        Map<String,String> toReturn=new HashMap<String,String>();
		
        for(String rowTag:BI_PUBLISHER_ROW_TAG_NAMES) {
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
	
	        				int keyIndex=0;
	        				int valueIndex=1;
	        				if (isKeyInSecondColumn) {
	        					keyIndex=1;	
	        					 valueIndex=0;
	        				}
	        				String key=getDataFromBIOutput(element,keyIndex);
	        				String value=getDataFromBIOutput(element,valueIndex);
	        				toReturn.put(key,value);
	        			}
	        		}
	        	}
	        	finally {
	        		IOUtils.closeQuietly(pw);
	        	}
	        }
        }
		return toReturn;
	}
	
	private static String getDataFromBIOutput(Element elementParam,int position) {
		NodeList fieldNodeList=elementParam.getChildNodes();
		if (fieldNodeList==null) {
			return "";
		}
		int elementIndex=-1;
		for (int i = 0; i < fieldNodeList.getLength(); i++) {
			Node node = fieldNodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				elementIndex++;
				if ( position != elementIndex) {
					continue;
				}
				String output=node.getTextContent();
				return output;
			}
		}
		return "";
	}
		
	public static void createDynamicSQLObjects(WebServiceInfo catalogWebServiceInfo,String nonSQLCatalogPath) throws Exception {
		CatalogService catalogService=ExtractUtils.getCatalogService(catalogWebServiceInfo);
		
		String dataSetId=ExtractConstants.BI_PUBLISHER_DYNAMIC_SQL_DATASET_NAME;
		String plsqlCode=
		"DECLARE\n"+
		" 	type refcursor is REF CURSOR;\n"+
		" 	xdo_cursor refcursor;\n"+
		"	str varchar2(32000);\n"+
		"Begin\n"+
		"	str := :param1;\n"+
		"	OPEN :xdo_cursor FOR str;\n"+
		"END;\n";
	
		String folderName="dyn-sql";
		int parametersCount=1;
		
		createNonSQLDataset(nonSQLCatalogPath,catalogService,catalogWebServiceInfo,dataSetId,plsqlCode,folderName,parametersCount);
		createNonSQLReport(nonSQLCatalogPath,catalogService,catalogWebServiceInfo,dataSetId,folderName);
	}
	
	
	/*
	 *
	 *  USE BELOW PL/SQL IN SQL DEVELOPER TO DEBUG THE VALUE SET QUERIES BEING EXECUTED
	 * 
DECLARE
 	type refcursor is REF CURSOR;
 	Xdo_Cursor Refcursor;
	param1 Varchar2(1000);
  Param2 Varchar2(1000);
  ret Varchar2(1000);
	strQuery varchar2(32000);
	L_RT_VALUE_SET FUSION.FND_FLEX_VS_SETUP_APIS.RT_VALUE_SET;
  Type Cv_Typ Is Ref Cursor;
  cv cv_typ;
Begin
	Param1 := 'xx_table';
  param2 := 'ATTRIBUTE8';
	Fnd_Flex_Vs_Setup_Apis.Get_Value_Set(P_Value_Set_Code => Param1,X_Value_Set => L_Rt_Value_Set);
  Dbms_Output.Put_Line('L_Rt_Value_Set.From_Clause = ' || L_Rt_Value_Set.From_Clause);
  Dbms_Output.Put_Line('L_Rt_Value_Set.Id_Column_Name = ' || L_Rt_Value_Set.Id_Column_Name);
  Dbms_Output.Put_Line('L_Rt_Value_Set.Where_Clause = ' || L_Rt_Value_Set.Where_Clause);
  Strquery := 'select '|| L_Rt_Value_Set.Value_Column_Name||' RES_RESULT from '||L_Rt_Value_Set.From_Clause ||' where 1=1'||	L_Rt_Value_Set.Where_Clause|| ' and  '|| L_Rt_Value_Set.Id_Column_Name ||' = '''|| Param2 ||'''';
  --strquery :=  'select * from dual where '''||param1||'''='''||param1||''' ';

  Dbms_Output.Put_Line('Strquery = ' || Strquery);
  
  OPEN cv FOR Strquery;
  LOOP
  Fetch Cv Into ret;
  Exit When Cv%Notfound;
    DBMS_OUTPUT.PUT_LINE('Ret = ' || Ret); 
  End Loop;
  CLOSE cv;

  DBMS_OUTPUT.PUT_LINE('DONE'); 
END;
	 */
	public static void createGetValueSetTableTypeSQLObjects(WebServiceInfo catalogWebServiceInfo,String nonSQLCatalogPath) throws Exception {
		CatalogService catalogService=ExtractUtils.getCatalogService(catalogWebServiceInfo);
		 
		String dataSetId=ExtractConstants.BI_PUBLISHER_GET_VALUE_TABLE_TYPE_SQL_DATASET_NAME;
		String plsqlCode=
		"DECLARE\n"+
		" 	type refcursor is REF CURSOR;\n"+
		" 	xdo_cursor refcursor;\n"+
		"	param1 Varchar2(1000);\n"+
		"	param2 Varchar2(1000);\n"+
		"	strQuery varchar2(32000);\n"+
		"	L_RT_VALUE_SET FUSION.FND_FLEX_VS_SETUP_APIS.RT_VALUE_SET;\n"+
		"Begin\n"+
		"	param1 := :param1;\n"+
		"	param2 := :param2;\n"+
		"	FND_FLEX_VS_SETUP_APIS.GET_VALUE_SET(param1, L_RT_VALUE_SET);\n"+
		" 	strQuery := 'select DISTINCT '|| L_Rt_Value_Set.Value_Column_Name||' RES_RESULT from '||L_Rt_Value_Set.From_Clause||' where 1=1 ';\n"+
	    "  	If (L_Rt_Value_Set.Where_Clause Is Not Null) then\n"+
	    "      	strQuery:=strQuery||' and '||L_Rt_Value_Set.Where_Clause;\n"+
	    "  	End If;\n"+
	    "  	If (L_Rt_Value_Set.ID_COLUMN_NAME Is Null) then\n"+
	    "   	strQuery:=strQuery||' and '|| L_Rt_Value_Set.Value_Column_Name ||' = '''|| Param2 ||'''';\n"+
	    "  	Else \n"+
	    "		strQuery:=strQuery||' and '|| L_Rt_Value_Set.Id_Column_Name ||' = '''|| Param2 ||'''';\n"+
	    "	End If;\n"+
		"	OPEN :xdo_cursor FOR strQuery;\n"+
		"END;\n";
		
		String folderName="gvtt-sql";
		int parametersCount=2;
		
		createNonSQLDataset(nonSQLCatalogPath,catalogService,catalogWebServiceInfo,dataSetId,plsqlCode,folderName,parametersCount);
		createNonSQLReport(nonSQLCatalogPath,catalogService,catalogWebServiceInfo,dataSetId,folderName);
	}
	
	private static void createNonSQLDataset(String nonSQLCatalogPath,CatalogService catalogService, WebServiceInfo catalogWebServiceInfo,
		String dataSetId,String plsqlCode,String folderName,int parametersCount) throws Exception {
		String datasetObjectAbsolutePathURL=nonSQLCatalogPath+dataSetId;
		
		File nonSQLTempFolder=new File(Config.getTempFolder(),folderName);
		File nonSQLDataSetTempFolder=new File(nonSQLTempFolder,"DS");
		nonSQLDataSetTempFolder.mkdirs();
		
		String objectType="xdmz";
		String xdmzFileName="DS."+objectType;
		File xdmzFile=new File(nonSQLDataSetTempFolder.getParentFile(),xdmzFileName);
		ExtractUtils.createNonSQLXDMZFile(xdmzFile,nonSQLDataSetTempFolder,plsqlCode,parametersCount); 
		
		FileUtils.println("createNonSQLDataset(), dataSetId: '"+dataSetId+"' Uploading DataSet archive...");
		String returnValue=ExtractUtils.uploadObjectBIWebService(catalogWebServiceInfo,catalogService,datasetObjectAbsolutePathURL,xdmzFile,objectType);
		FileUtils.println("createNonSQLDataset(), dataSetId: '"+dataSetId+"' returnValue:"+returnValue);
	}
	
	private static void createNonSQLReport(String nonSQLCatalogPath,CatalogService catalogService,WebServiceInfo catalogWebServiceInfo
			,String dataSetId,String folderName) throws Exception {
		String datasetObjectAbsolutePathURL=nonSQLCatalogPath+dataSetId;
		String reportObjectAbsolutePathURL=nonSQLCatalogPath+dataSetId;
		
		File nonSQLTempFolder=new File(Config.getTempFolder(),folderName);
		File nonSQLReportTempFolder=new File(nonSQLTempFolder,"RE");
		nonSQLReportTempFolder.mkdirs();
		
		String objectType="xdoz";
		String xdozFileName="RE."+objectType;
		File xdozFile=new File(nonSQLReportTempFolder.getParentFile(),xdozFileName);
		ExtractUtils.createXDOZFile(xdozFile,nonSQLReportTempFolder,datasetObjectAbsolutePathURL);
	
		FileUtils.println("createNonSQLReport(), dataSetId: '"+dataSetId+"' Uploading Report archive...");
		String returnValue=ExtractUtils.uploadObjectBIWebService(catalogWebServiceInfo,catalogService,reportObjectAbsolutePathURL,xdozFile,objectType);
		FileUtils.println("createNonSQLReport(), dataSetId: '"+dataSetId+"' returnValue:"+returnValue);
	}
	
	public static void deleteBIFolder(String objectAbsolutePathURL,CatalogService catalogService,WebServiceInfo catalogWebServiceInfo) {
		try {			
			FileUtils.println("deleteFolder(),  objectAbsolutePathURL:"+objectAbsolutePathURL);
			boolean isDeleted= ExtractUtils.deleteObjectBIWebService(catalogWebServiceInfo,catalogService,objectAbsolutePathURL);
			FileUtils.println("deleteFolder(), isDeleted:"+isDeleted);
		}
		catch (Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to delete BI custom folder: '"+objectAbsolutePathURL+"' Error: "+e.getMessage());
		}
	}
	
	public static NodeList getNodeList(Document document) {
		for (String rowTag:BI_PUBLISHER_ROW_TAG_NAMES) {
			NodeList nodeList = document.getElementsByTagName(rowTag);
			int recordCount=nodeList.getLength();
			if (recordCount>0) {
				return nodeList;
			}
		}
		return null;
	}
	
	public static List<ExtractFlexfieldTempRow> getExtractFlexfieldsTempRowsFromBIPublisher(ExtractInventoryRecord extractInventoryRecord,
			File biXMLOutputFile) throws Exception {
		List<ExtractFlexfieldTempRow> toReturn=new ArrayList<ExtractFlexfieldTempRow>();
		
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(biXMLOutputFile);
		
		Inventory inventory=extractInventoryRecord.getInventory();
		
        List<Field> fieldsUsedForPrimaryKey=inventory.getFieldsUsedForPrimaryKey();
        PrintWriter pw=null;
        NodeList nodeList = getNodeList(document);
        if (nodeList!=null && nodeList.getLength()>0)
        {
        	try{
        		for (int i = 0; i < nodeList.getLength(); i++) {
        			Node node = nodeList.item(i);
        			if (node.getNodeType() == Node.ELEMENT_NODE) {
        				Element element = (Element) node;

        				ExtractFlexfieldTempRow extractFlexfieldTempRow=new ExtractFlexfieldTempRow();

                		Map<String,String> foreignKeyHashNameToValueMap=extractFlexfieldTempRow.getForeignKeyHashNameToValueMap();
        				for (Field field:fieldsUsedForPrimaryKey) {
        					String parentName=field.getParentName();
        					if (parentName==null || parentName.isEmpty()) {
        						continue;
        					}
        					String fieldName=field.getNameHash();
        					String value=getDataFromBIOutput(element,fieldName);		
        					value=convertBIDateIfApplicable(value);		
        					foreignKeyHashNameToValueMap.put(fieldName,value);
        				}
        				
        				String contextCode=getDataFromBIOutput(element,ExtractConstants.RES_CONTEXT_CODE);
        				extractFlexfieldTempRow.setContextCode(contextCode);
        				
        				Map<String,String> attributeNameToValueMap=extractFlexfieldTempRow.getAttributeNameToValueMap();
        				addTagNameValueDataFromBIOutput(element,attributeNameToValueMap);        				
        				setGenericFields(extractFlexfieldTempRow,element);

        				toReturn.add(extractFlexfieldTempRow);
        			}
        		}
        	}
        	finally {
        		IOUtils.closeQuietly(pw);
        	}
        }
		return toReturn;
	}
	
	private static void setGenericFields(ExtractGenericDataRow extractGenericDataRow,Element element) throws Exception {
		String rscLastUpdatedByName=getDataFromBIOutput(element,"RSC_LAST_UPDATED_BY");
		extractGenericDataRow.setRscLastUpdatedBy(rscLastUpdatedByName);
		String rscLastUpdateDate=getDataFromBIOutput(element,"RSC_LAST_UPDATE_DATE");
		rscLastUpdateDate=convertBIDate(rscLastUpdateDate);
		extractGenericDataRow.setRscLastUpdateDate(rscLastUpdateDate);
		
		String rscCreatedByName=getDataFromBIOutput(element,"RSC_CREATED_BY");
		extractGenericDataRow.setRscCreatedBy(rscCreatedByName);
		String rscCreationDate=getDataFromBIOutput(element,"RSC_CREATION_DATE");
		rscCreationDate=convertBIDate(rscCreationDate);
		extractGenericDataRow.setRscCreationDate(rscCreationDate);

		String temp=getDataFromBIOutput(element,"RSC_LEDGER_ID");
		if (temp!=null && !temp.isEmpty()) {
			extractGenericDataRow.setLedgerId(Long.valueOf(temp).longValue());
		}
		temp=getDataFromBIOutput(element,"RSC_CHART_OF_ACCOUNTS_ID");
		if (temp!=null && !temp.isEmpty()) {
			extractGenericDataRow.setCoaId(Long.valueOf(temp).longValue());
		}
		temp=getDataFromBIOutput(element,"RSC_BUSINESS_UNIT_ID");
		if (temp!=null && !temp.isEmpty()) {
			extractGenericDataRow.setBusinessUnitId(Long.valueOf(temp).longValue());
		}
		temp=getDataFromBIOutput(element,"RSC_LEGAL_ENTITY_ID");
		if (temp!=null && !temp.isEmpty()) {
			extractGenericDataRow.setLegalEntityId(Long.valueOf(temp).longValue());
		}
		temp=getDataFromBIOutput(element,"RSC_ORGANIZATION_ID");
		if (temp!=null && !temp.isEmpty()) {
			extractGenericDataRow.setInventoryOrganizationId(Long.valueOf(temp).longValue());
		}
		temp=getDataFromBIOutput(element,"RSC_BUSINESS_GROUP_ID");
		if (temp!=null && !temp.isEmpty()) {
			extractGenericDataRow.setBusinessGroupId(Long.valueOf(temp).longValue());
		}
		temp=getDataFromBIOutput(element,"RSC_ENTERPRISE_ID");
		if (temp!=null && !temp.isEmpty()) {
			extractGenericDataRow.setEnterpriseId(Long.valueOf(temp).longValue());
		}
	}
	
	public static List<ParameterNameValue> getParametersExcludeSeededUsers(ExtractMain extractMain) throws Exception {
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String oracleSeededUserNames=extractMain.getExtractMainPanel().getTabOptionsPanel().getOracleSeededUserNames();
		if (oracleSeededUserNames==null || oracleSeededUserNames.isEmpty()) {
			throw new Exception("Missing Oracle Seeded users from the Options panel");
		}
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("EXC_SEEDED_DATA");
		parameterNameValue.setValue("1");
		
		parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("SEEDED_USER_NAME_LIST");
		parameterNameValue.setValue(oracleSeededUserNames);
						
		return parametersList;
	}
	
	public static String getWhereConditionExcludeSeededUsers(ExtractMain extractMain) throws Exception {
		StringBuffer buffer=new StringBuffer("");
		String oracleSeededUserNames=extractMain.getExtractMainPanel().getTabOptionsPanel().getOracleSeededUserNames();
		if (oracleSeededUserNames==null || oracleSeededUserNames.isEmpty()) {
			throw new Exception("Missing Oracle Seeded users from the Options panel");
		}
		String[] array=oracleSeededUserNames.split(",");
		int counter=0;
		StringBuffer usersBuffer=new StringBuffer("");
		for (String val:array) {
			usersBuffer.append("'").append(val).append("'");
			if ( (counter+1) < array.length) {
				usersBuffer.append(",");
			}
			counter++;
		}
		
		buffer.append(" AND ( RSC_LAST_UPDATED_BY NOT IN (").append(usersBuffer).append(") ");
		buffer.append(" or RSC_CREATED_BY NOT IN (").append(usersBuffer).append(") ) ");
					
		return buffer.toString();
	}
	
	public static String getWhereConditionCreatedByUsers(ExtractMain extractMain) throws Exception {
		 List<String> list=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
					.getFilterUserPanel().getCreatedBySelectedUserNames();
		 boolean isCreatedByInclude=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
			.getFilterUserPanel().isCreatedByInclude();
		 if (isCreatedByInclude) {
			 return getWhereConditionUsers(list,"RSC_CREATED_BY",true);
		 }
		 else {
			 return getWhereConditionUsers(list,"RSC_CREATED_BY",false);
		 }
	}
	
	public static String getWhereConditionLastUpdatedByUsers(ExtractMain extractMain) throws Exception {
		 List<String> list=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
					.getFilterUserPanel().getLastUpdatedBySelectedUserNames();
		 boolean isLastUpdatedByInclude=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
			.getFilterUserPanel().isLastUpdatedByInclude();
		 if (isLastUpdatedByInclude) {
			 return getWhereConditionUsers(list,"RSC_LAST_UPDATED_BY",true);
		 }
		 else {
			 return getWhereConditionUsers(list,"RSC_LAST_UPDATED_BY",false);
		 }
	}
	
	private static String getWhereConditionUsers(List<String> list,String dbColumnName,boolean isInclude) throws Exception {
		StringBuffer buffer=new StringBuffer("");
		if (list==null || list.isEmpty()) {
			return buffer.toString();
		}
		buffer.append(" AND ");
		if (isInclude) {
			buffer.append(" "+dbColumnName+" IN ( ");
		}
		else {
			buffer.append(" "+dbColumnName+" NOT IN ( ");
		}
		
		int counter=0;
		for (String name:list) {
			buffer.append("'").append(name).append("'");
			if ( (counter+1) < list.size()) {
				buffer.append(",");
			}
			counter++;
		}
		buffer.append(" ) ");
		return buffer.toString();
	}
	
	public static String getWhereConditionCreationDate(ExtractMain extractMain) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String comparator=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getCreationDateComparator();
		Date fromDate=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getFromCreationDate();
		String formattedFromDate=format.format(fromDate);
		Date toDate=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getToCreationDate();
		String formattedToDate=format.format(toDate);		
		
		return getWhereConditionDate(comparator,formattedFromDate,formattedToDate,"RSC_CREATION_DATE");
	}
	
	public static String getWhereConditionLastUpdateDate(ExtractMain extractMain) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String comparator=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getLastUpdateDateComparator();
		Date fromDate=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getFromLastUpdateDate();
		String formattedFromDate=format.format(fromDate);
		Date toDate=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getToLastUpdateDate();
		String formattedToDate=format.format(toDate);		
		
		return getWhereConditionDate(comparator,formattedFromDate,formattedToDate,"RSC_LAST_UPDATE_DATE");
	}
	
	private static String getWhereConditionDate(String comparator,String formattedFromDate,String formattedToDate,String dbColumnName) {
		StringBuffer buffer=new StringBuffer("");
		buffer.append(" AND ").append(dbColumnName).append(" BETWEEN "); 
		String dbDateFormat ="yyyy-MM-dd HH24:MI:SS";
	
		// Format that works in BIP: 
		// Select sysdate From Dual Where 1=1 AND sysdate BETWEEN To_Date('2016-03-28 01:01:44','yyyy-MM-dd HH24:MI:SS') AND
		// To_Date('2016-03-29 20:01:44','yyyy-MM-dd HH24:MI:SS')

		if (comparator.equalsIgnoreCase(FilterDatePanel.COMPARISON_BETWEEN)) {
			buffer.append("to_date('").append(formattedFromDate.toUpperCase()).append("','").append(dbDateFormat).append("')").append(" AND ").
			append("to_date('").append(formattedToDate.toUpperCase()).append("','").append(dbDateFormat).append("')");
		}
		else 
		if (comparator.equalsIgnoreCase(FilterDatePanel.COMPARISON_GREATER_OR_EQUAL_THAN)) {
			buffer.append("to_date('").append(formattedFromDate.toUpperCase()).append("','").append(dbDateFormat).append("')").append(" AND ").
			append("to_date('").append("2500-01-01 00:00:00").append("','").append(dbDateFormat).append("')");
		}
		else {
			buffer.append("to_date('").append("1950-01-01 00:00:00").append("','").append(dbDateFormat).append("')").append(" AND ").
			append("to_date('").append(formattedFromDate.toUpperCase()).append("','").append(dbDateFormat).append("')");
		}						
		return buffer.toString();
	}
	
	public static byte[] getRawDualQuery(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) 
			throws Exception {			
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String value="SELECT 1 from dual";
		
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(value);
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
		//String data=new String(outputBytes,"ISO-8859-1");
		//System.out.println("data:"+data);
		
		return outputBytes;
	}
	
	/**
	 * Gets the Levels, ID and Name mapping from Fusion
	 * 
	 * @param dynamicSQLReportPath
	 * @param reportWebServiceInfo
	 * @param reportService
	 * @return Map<SELECTION_LEVEL, Map<Long, String>> a map of levels and the IDs and Names mappings in that level
	 * @throws Exception
	 */
	public static Map<SELECTION_LEVEL, Map<Long, String>> getIDAndNamesMapping(String dynamicSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) 
			throws Exception {
		Map<SELECTION_LEVEL, Map<Long, String>> toReturn = new HashMap<SELECTION_LEVEL, Map<Long, String>>();
		
		for(int selectCount=SELECTION_LEVEL.values().length-1; selectCount>=0; selectCount--) {
			List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
			
			String mappingSQL="";
			
			switch (SELECTION_LEVEL.values()[selectCount]) {
			case ORGANIZATION:
				mappingSQL="select distinct ORGANIZATION_ID ,BusinessUnitEO.name from HR_ORGANIZATION_V BusinessUnitEO";
				break;
			case LEGAL_ENTITY: 
				mappingSQL="select XXX.LEGAL_ENTITY_ID ,XXX.NAME from XLE_ENTITY_PROFILES XXX";
				break;
			case LEDGER:
				mappingSQL="select LEDGER_ID ,NAME from gl_ledgers";
				break;
			case BUSINESS_UNIT:
				mappingSQL="select bu_id,bu_name from FUN_ALL_BUSINESS_UNITS_V";
				break;
			case COA: 
				mappingSQL="SELECT STRUCTURE_INSTANCE_NUMBER,name FROM FND_KF_STR_INSTANCES_VL";
				break;
			case ENTERPRISE_BUSINESS_GROUP:
				mappingSQL = "SELECT distinct Enterprise.business_group_id " + ",Enterprise.name " + "FROM " + "( "
						+ " select OrganizationUnitTranslationD1.name,ORGANIZATIONUNITDEO.ORGANIZATION_ID,OrganizationUnitDEO.business_group_id,OrganizationUnitDEO.EFFECTIVE_START_DATE "
						+ " ,OrganizationUnitDEO.last_update_date "
						+ " , ROW_NUMBER() over ( PARTITION BY OrganizationUnitDEO.business_group_id "
						+ " ORDER BY OrganizationUnitDEO.last_update_date desc ) AS Line "
						+ " from HR_ALL_ORGANIZATION_UNITS_F ORGANIZATIONUNITDEO "
						+ " ,HR_ORGANIZATION_UNITS_F_TL OrganizationUnitTranslationD1 "
						+ " ,HR_LOCATIONS_ALL_F_VL LOCATIONDPEO "
						+ " ,HR_ORG_UNIT_CLASSIFICATIONS_F ORGUNITCLASSIFICATIONDEO " + ",PER_ADDRESSES_F ADDRESSDPEO "
						+ " WHERE ORGUNITCLASSIFICATIONDEO.CLASSIFICATION_CODE = 'ENTERPRISE' "
						+ " AND ( ( (OrganizationUnitDEO.LOCATION_ID = LocationDPEO.LOCATION_ID(+)) "
						+ " AND (OrganizationUnitDEO.ORGANIZATION_ID = OrganizationUnitTranslationD1.ORGANIZATION_ID) "
						+ " AND (OrganizationUnitDEO.EFFECTIVE_START_DATE = OrganizationUnitTranslationD1.EFFECTIVE_START_DATE) "
						+ " AND (OrganizationUnitDEO.EFFECTIVE_END_DATE = OrganizationUnitTranslationD1.EFFECTIVE_END_DATE)) "
						+ " AND OrganizationUnitTranslationD1.Language = sys_context ('USERENV', 'LANG')) "
						+ " AND (LOCATIONDPEO.ADDRESS_ID = ADDRESSDPEO.ADDRESS_ID(+)) "
						+ " AND ORGANIZATIONUNITDEO.ORGANIZATION_ID = OrgUnitClassificationDEO.ORGANIZATION_ID "
						+ " AND (ORGANIZATIONUNITDEO.EFFECTIVE_START_DATE BETWEEN ORGUNITCLASSIFICATIONDEO.EFFECTIVE_START_DATE AND "
						+ " OrgUnitClassificationDEO.EFFECTIVE_END_DATE) "
						+ " AND (sysdate BETWEEN ADDRESSDPEO.EFFECTIVE_START_DATE(+) AND ADDRESSDPEO.EFFECTIVE_END_DATE(+)) "
						+ " ORDER BY OrganizationUnitDEO.EFFECTIVE_START_DATE desc " + ") Enterprise "
						+ " WHERE Line = 1 " + "and Enterprise.business_group_id = 1";
				break;
			default:
				mappingSQL="";
			}
			
			if(StringUtils.isBlank(mappingSQL)) {
				continue;
			}
			
			ParameterNameValue parameterNameValue=new ParameterNameValue();
			parametersList.add(parameterNameValue);
			parameterNameValue.setName("param1");
			parameterNameValue.setValue(mappingSQL);
			
			byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicSQLReportPath,parametersList);
			//String data=new String(outputBytes,"ISO-8859-1");
			
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(outputBytes);
			Document document = null;
			try {
				document = builder.parse(input);
			} catch (Exception ex) {
				FileUtils.println("Unable to parse the output, please debug the report output.");
				FileUtils.printStackTrace(ex);
				
				// nothing to do here anymore, move on to the next iteration.
				continue;
			}
			
			for(String rowTag:BI_PUBLISHER_ROW_TAG_NAMES) {
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
		
		        				int keyIndex=0;
		        				int valueIndex=1;
		        				Long key=Long.parseLong(getDataFromBIOutput(element,keyIndex));
		        				String keyValue=getDataFromBIOutput(element,valueIndex);
		        				Map<Long, String> idAndNameMap = toReturn.get(SELECTION_LEVEL.values()[selectCount]);
		        				if(idAndNameMap == null) {
		        					idAndNameMap = new HashMap<Long, String>();
		        					toReturn.put(SELECTION_LEVEL.values()[selectCount], idAndNameMap);
		        				}
		        				idAndNameMap.put(key, keyValue);
		        			}
		        		}
		        	}
		        	finally {
		        		IOUtils.closeQuietly(pw);
		        	}
		        }
	        }
			
		}
		
		return toReturn;
	}
	
	public static void addTagNameValueDataFromBIOutput(Element rowElement,Map<String,String> tagNameToValueMap) {
		NodeList fieldNodeList=rowElement.getChildNodes();
		if (fieldNodeList!=null) {
			//FileUtils.println(" PROCESSING DFF ROW...");
			for (int i = 0; i < fieldNodeList.getLength(); i++) {
    			Node node = fieldNodeList.item(i);
    			if (node.getNodeType() == Node.ELEMENT_NODE) {
    				Element element = (Element) node;
    				String tagName=element.getTagName();
    				String output=node.getTextContent();
    				//FileUtils.println(" name: '"+tagName+"' output before: '"+output+"'");
    				output=convertBIDateIfApplicable(output);
    				tagNameToValueMap.put(tagName, output);
    				//FileUtils.println(" name: '"+tagName+"' output after: '"+output+"'");
    			}
			}	
		}
	}

	public static List<ParameterNameValue> getParametersCreatedByUsers(ExtractMain extractMain) throws Exception {
		 List<String> list=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
					.getFilterUserPanel().getCreatedBySelectedUserNames();
		 boolean isCreatedByInclude=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
			.getFilterUserPanel().isCreatedByInclude();
		 if (isCreatedByInclude) {
			 return getParametersUsers(list,"INC_CREATED_BY","CREATED_BY_USER_NAME_LIST");
		 }
		 else {
			 return getParametersUsers(list,"EXC_CREATED_BY","CREATED_BY_USER_NAME_LIST");
		 }
	}
	
	public static List<ParameterNameValue> getParametersLastUpdateByUsers(ExtractMain extractMain) throws Exception {
		 List<String> list=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
					.getFilterUserPanel().getLastUpdatedBySelectedUserNames();
		 boolean isLastUpdatedByInclude=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
					.getFilterUserPanel().isLastUpdatedByInclude();
		 if (isLastUpdatedByInclude) {
			 return getParametersUsers(list,"INC_LAST_UPDATED_BY","LAST_UPDATED_BY_USER_NAME_LIST");
		 }
		 else {
			 return getParametersUsers(list,"EXC_LAST_UPDATED_BY","LAST_UPDATED_BY_USER_NAME_LIST");
		 }
	}
	
	private static List<ParameterNameValue> getParametersUsers(List<String> list,String parameter1,String parameter2) throws Exception {
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		if (list==null || list.isEmpty()) {
			return parametersList;
		}
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName(parameter1);
		parameterNameValue.setValue("1");
		
		parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName(parameter2);
		
		StringBuffer strBuffer=new StringBuffer("");
		int counter=0;
		for (String name:list) {
			strBuffer.append(name);
			if ( (counter+1) < list.size()) {
				strBuffer.append(",");
			}
			counter++;
		}		
		parameterNameValue.setValue(strBuffer.toString());
						
		return parametersList;
	}
	
	public static List<ParameterNameValue> getParametersCreationDate(ExtractMain extractMain) {
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

		String comparator=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getCreationDateComparator();
		Date fromDate=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getFromCreationDate();
		String formattedFromDate=format.format(fromDate);
		Date toDate=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getToCreationDate();
		String formattedToDate=format.format(toDate);		
		
		return getParametersDate(comparator,formattedFromDate,formattedToDate,"CREATION_DATE_ON","CREATION_DATE_MIN","CREATION_DATE_MAX");
	}
	
	public static List<ParameterNameValue> getParametersLastUpdateDate(ExtractMain extractMain) {
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

		String comparator=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getLastUpdateDateComparator();
		Date fromDate=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getFromLastUpdateDate();
		String formattedFromDate=format.format(fromDate);
		Date toDate=extractMain.getExtractMainPanel().getTabSelectionPanel().getExtractInventoryGridSelectionPanel().getFiltersPanel()
				.getFilterDatePanel().getToLastUpdateDate();
		String formattedToDate=format.format(toDate);		
		
		return getParametersDate(comparator,formattedFromDate,formattedToDate,"LAST_UPDATE_DATE_ON","LAST_UPDATE_DATE_MIN","LAST_UPDATE_DATE_MAX");
	}

	private static List<ParameterNameValue> getParametersDate(String comparator,String formattedFromDate,
			String formattedToDate,String parameter1,String parameter2,String parameter3) {
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName(parameter1);
		parameterNameValue.setValue("1");
		
		//System.out.println("formattedFromDate:"+formattedFromDate);
		//System.out.println("formattedToDate:"+formattedToDate);
		
		ParameterNameValue minParameterNameValue=new ParameterNameValue();
		parametersList.add(minParameterNameValue);
		minParameterNameValue.setName(parameter2);
		
		ParameterNameValue maxParameterNameValue=new ParameterNameValue();
		parametersList.add(maxParameterNameValue);
		maxParameterNameValue.setName(parameter3);
		
		if (comparator.equalsIgnoreCase(FilterDatePanel.COMPARISON_BETWEEN)) {
			minParameterNameValue.setValue(formattedFromDate);
			maxParameterNameValue.setValue(formattedToDate);
		}
		else 
		if (comparator.equalsIgnoreCase(FilterDatePanel.COMPARISON_GREATER_OR_EQUAL_THAN)) {
			minParameterNameValue.setValue(formattedFromDate);
			maxParameterNameValue.setValue("01-JAN-2500 00:00:00");
		}
		else {
			minParameterNameValue.setValue("01-JAN-1950 00:00:00");
			maxParameterNameValue.setValue(formattedFromDate);
		}						
		return parametersList;
	}

	public static void saveExtractDataRowsToRSCExcelFormat(Inventory inventory,List<ExtractDataRow> extractDataRows,File rscExcelFile)throws Exception {
		List<Field> inventoryFieldsDataOnly=inventory.getFieldsUsedForDataEntry();
		List<String[]> dataRows=new ArrayList<String[]>();
		int extraColumnsCount=7;
		String[] headerRow=new String[extraColumnsCount+inventoryFieldsDataOnly.size()];
		dataRows.add(headerRow);
		int index=0;
		headerRow[index++]="Seq";
		for ( Field field : inventoryFieldsDataOnly )
		{
			String fieldName=field.getName();
			if (fieldName==null) {
				fieldName="";
			}
			headerRow[index++]=fieldName;
		}
		headerRow[index++]="RSC Data Label";
		headerRow[index++]="Navigation Filter";
		headerRow[index++]="RSC last updated by name";
		headerRow[index++]="RSC last update date";
		headerRow[index++]="RSC Created by name";
		headerRow[index++]="RSC Creation date";

		int recordNumber=0;			
		for ( ExtractDataRow extractDataRow:extractDataRows ){
			recordNumber++;
			index=0;

			String[] row=new String[extraColumnsCount+inventoryFieldsDataOnly.size()];
			dataRows.add(row);
			row[index++]=""+recordNumber;
			for ( String column : extractDataRow.getDataValues() ) {
				if (column==null) {
					column="";
				}
				row[index++]=column;
			}
			row[index++]=ExtractConstants.DATA_LABEL;
			row[index++]="";

			String userName=extractDataRow.getRscLastUpdatedBy();
			if (userName==null) {
				userName="";
			}
			row[index++]=userName;

			String str=extractDataRow.getRscLastUpdateDate();
			if (str==null) {
				str="";
			}
			row[index++]=str;

			userName=extractDataRow.getRscCreatedBy();
			if (userName==null) {
				userName="";
			}
			row[index++]=userName;

			str=extractDataRow.getRscCreationDate();
			if (str==null) {
				str="";
			}
			row[index++]=str;
		}
		FileUtils.createAndSaveRowsToXLSXFile("Data",rscExcelFile,dataRows);
	}
	
	public static Map<String,NavigatorNode> getFunctionIdToNavigatorNode(File file) throws Exception {
		FileInputStream fis =null;
		Map<String,NavigatorNode> toReturn=new TreeMap<String,NavigatorNode>();
		String functionIdKeyword="F-";
		String menuIdKeyword="M-";
		try{
			fis =new FileInputStream(file);

			final XmlNavigatorParser xmlNavigatorParser = new XmlNavigatorParser();
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final SAXParser parser = factory.newSAXParser();
			parser.parse(new InputSource(new InputStreamReader(fis, CoreConstants.CHARACTER_SET_ENCODING)), xmlNavigatorParser);

			List<EbsResponsibilityRecord> list= xmlNavigatorParser.allRecords;
			Map<String,NavigatorNode> lookupMap=new TreeMap<String,NavigatorNode>();
			for (EbsResponsibilityRecord ebsResponsibilityRecord: list) {
				String subMenuId=ebsResponsibilityRecord.getSubMenuId();
				String functionId=ebsResponsibilityRecord.getFormFunctionId();

				String key=null;
				if (subMenuId==null || subMenuId.isEmpty()) {
					key=functionIdKeyword+functionId;
				}
				else {
					key=menuIdKeyword+subMenuId;
				}
				NavigatorNode node=lookupMap.get(key);
				if (node==null) {
					lookupMap.put(key, new NavigatorNode(ebsResponsibilityRecord));
				}
			}

			Set<String> keySet = lookupMap.keySet();
			for (String id : keySet) {
				NavigatorNode navigatorNode = lookupMap.get(id);
				String parentId = navigatorNode.getEbsResponsibilityRecord().getMenuId();
				NavigatorNode parentNavigatorNode = lookupMap.get(menuIdKeyword+parentId);
				if (parentNavigatorNode != null) {
					navigatorNode.setParent(parentNavigatorNode);
					parentNavigatorNode.addChild(navigatorNode);
				}
			}
			
			keySet = lookupMap.keySet();
			for (String id : keySet) {
				if (id.startsWith(functionIdKeyword)) {
					NavigatorNode navigatorNode = lookupMap.get(id);
					toReturn.put(id.replace(functionIdKeyword,""),navigatorNode);
				}
			}
			return toReturn;	
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			return toReturn;
		}
		finally {
			if (fis!=null) {
				fis.close();
			}
		}
	}

	public static Map<String,File> getFileNameToFileMap(File rootFolder,boolean isRecursive,String fileExtension)
	{
		Map<String,File> toReturn=new TreeMap<String,File>(String.CASE_INSENSITIVE_ORDER);
		File[] files=rootFolder.listFiles();
		if (files==null) {
			return toReturn;
		}
		for ( File file : rootFolder.listFiles())
		{
			if(file.isDirectory())
	        {
				if (isRecursive) {
					Map<String,File> tempMap=getFileNameToFileMap(file,isRecursive,fileExtension);
					toReturn.putAll(tempMap);
				}
	        }
	        else
	        {
	        	if (fileExtension!=null && !file.getName().toLowerCase().endsWith(fileExtension.toLowerCase()) ) {
	        		continue;
	        	}
	        	String tableName = CoreUtil.getTableName(file.getName());
	        	toReturn.put(tableName,file);
	        }			
		}
		return toReturn;
	}

	public static SortedMap<String, SortedSet<File>> getInventoryToSQLMap(File rootFolder,boolean isRecursive,String fileExtension)
	{
		SortedMap<String, SortedSet<File>> toReturn=new TreeMap<String,SortedSet<File>>(String.CASE_INSENSITIVE_ORDER);
		File[] files=rootFolder.listFiles();
		if (files==null) {
			return toReturn;
		}
		for ( File file : rootFolder.listFiles())
		{
			if(file.isDirectory())
	        {
				if (isRecursive) {
					SortedMap<String, SortedSet<File>> tempMap=getInventoryToSQLMap(file,isRecursive,fileExtension);
					toReturn.putAll(tempMap);
				}
	        }
	        else
	        {
	        	if (fileExtension!=null && !file.getName().toLowerCase().endsWith(fileExtension.toLowerCase()) ) {
	        		continue;
	        	}
	        	String tableName = CoreUtil.getTableName(file.getName());
	        	SortedSet<File> fileSet = toReturn.get(tableName);
	        	if(fileSet == null) {
	        		fileSet = new TreeSet<File>();
	        		toReturn.put(tableName,fileSet);
	        	}
	        	fileSet.add(file);
	        }			
		}
		return toReturn;
	}
	
	/**
	 * Splits the input list of dataRows into FUSION levels.
	 * 
	 * @param extractDataRows
	 * @return Map<SELECTION_LEVEL, Map<MapGeneric, List<ExtractDataRow>>> a map of fusion levels and ID and Name mapping and the list of dataRows on particular Level and under a certain ID and Name map
	 */
	public static Map<SELECTION_LEVEL, Map<MapGeneric, List<ExtractDataRow>>> getSelectionLevelAndExtractDataRowsMap(List<ExtractDataRow> extractDataRows) {
		Map<SELECTION_LEVEL, Map<MapGeneric, List<ExtractDataRow>>> toReturn = new HashMap<SELECTION_LEVEL, Map<MapGeneric, List<ExtractDataRow>>>();
		
		try {
			for (ExtractDataRow extractDataRow : extractDataRows) {
				for(int i=SELECTION_LEVEL.values().length-1; i>=0; i--) {
					Long entityId = extractDataRow.getSupportingId(SELECTION_LEVEL.values()[i]);
					
					if (entityId > 0 || (SELECTION_LEVEL.values()[i] == SELECTION_LEVEL.GLOBAL && entityId == 0)) {
						String entityName = entityId.toString();
						Map<Long, String> idAndNameMapping = ExtractUtils.selectionLevelIDAndNameMapping.get(SELECTION_LEVEL.values()[i]);
						if(idAndNameMapping != null && !idAndNameMapping.isEmpty()) {
							entityName = idAndNameMapping.get(entityId);
							if(StringUtils.isEmpty(entityName)) {
								entityName = entityId.toString();
							}
						}
						
						MapGeneric mapGeneric = new MapGeneric(entityName, entityId);
						Map<MapGeneric, List<ExtractDataRow>> idToDatarowMap = toReturn.get(SELECTION_LEVEL.values()[i]);
						if(idToDatarowMap == null) {
							idToDatarowMap = new HashMap<MapGeneric, List<ExtractDataRow>>();
							toReturn.put(SELECTION_LEVEL.values()[i], idToDatarowMap);
						}
						List<ExtractDataRow> datarows = idToDatarowMap.get(mapGeneric);
						if (datarows == null) {
							datarows = new ArrayList<ExtractDataRow>();
							idToDatarowMap.put(mapGeneric, datarows);
						}
						datarows.add(extractDataRow);
						break;
					}
				}
			}
		} catch (Exception ex) {
			FileUtils.printStackTrace(ex);
		}
		
		return toReturn;
	}
	
}
