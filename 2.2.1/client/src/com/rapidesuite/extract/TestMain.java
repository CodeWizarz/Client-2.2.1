package com.rapidesuite.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.BindingProvider;

import org.apache.commons.codec.binary.Base64;

import com.oracle.xmlns.oxp.service.v2.ArrayOfItemData;
import com.oracle.xmlns.oxp.service.v2.ArrayOfParamNameValue;
import com.oracle.xmlns.oxp.service.v2.ArrayOfString;
import com.oracle.xmlns.oxp.service.v2.ArrayOfTemplateFormatLabelValue;
import com.oracle.xmlns.oxp.service.v2.ArrayOfTemplateFormatsLabelValues;
import com.oracle.xmlns.oxp.service.v2.CatalogContents;
import com.oracle.xmlns.oxp.service.v2.CatalogService;
import com.oracle.xmlns.oxp.service.v2.CatalogService_Service;
import com.oracle.xmlns.oxp.service.v2.ItemData;
import com.oracle.xmlns.oxp.service.v2.ParamNameValue;
import com.oracle.xmlns.oxp.service.v2.ParamNameValues;
import com.oracle.xmlns.oxp.service.v2.ReportDefinition;
import com.oracle.xmlns.oxp.service.v2.ReportRequest;
import com.oracle.xmlns.oxp.service.v2.ReportResponse;
import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.oracle.xmlns.oxp.service.v2.ReportService_Service;
import com.oracle.xmlns.oxp.service.v2.TemplateFormatLabelValue;
import com.oracle.xmlns.oxp.service.v2.TemplateFormatsLabelValues;
import com.rapidesuite.configurator.utility.XmlNavigatorParser;
import com.rapidesuite.configurator.utility.XmlNavigatorParser.EbsResponsibilityRecord;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.extract.model.NavigatorNode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class TestMain {

	// BANGKOK
	String reportServiceEndpointUrl = "http://fusion01.rapidesuite.com:18621/xmlpserver/services/v2/ReportService";
	String catalogServiceEndpointUrl = "http://fusion01.rapidesuite.com:18621/xmlpserver/services/v2/CatalogService";

	String username = "FAAdmin";
	String password = "Oracle123";

	// MEXICO:
	/*
	    String endpointUrl = "https://ecbw-test.bi.us2.oraclecloud.com/xmlpserver/services/v2/ReportService";
	    String username = "Rapid";
	    String password = "Test12345";
	    final String reportAbsolutePath = "/XX Rapid/TEST/MyReport.xdo";           
	 */

	public ReportService getReportService() throws Exception {
		ReportService_Service reportService_Service = new ReportService_Service();
		ReportService reportService = reportService_Service.getReportService();

		//weblogic.wsee.jws.jaxws.owsm.SecurityPoliciesFeature securityFeatures = new weblogic.wsee.jws.jaxws.owsm.SecurityPoliciesFeature(new String[] {"oracle/wss_username_token_client_policy" });
		//ReportService reportService = reportService_Service.getReportService(securityFeatures);

		Map<String, Object> requestContext = ((BindingProvider) reportService).getRequestContext();
		requestContext.put(BindingProvider.USERNAME_PROPERTY, username);
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, reportServiceEndpointUrl);

		return reportService;
	}

	public CatalogService getCatalogService() throws Exception {
		CatalogService_Service catalogService_Service = new CatalogService_Service();
		CatalogService catalogService = catalogService_Service.getCatalogService();

		Map<String, Object> requestContext = ((BindingProvider) catalogService).getRequestContext();
		requestContext.put(BindingProvider.USERNAME_PROPERTY, username);
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, catalogServiceEndpointUrl);

		return catalogService;
	}

	public void saveOutput(ReportResponse reportResponse,File outputFile) throws Exception {
		String contentType = reportResponse.getReportContentType();
		System.out.println(contentType);
		byte[] baReport = reportResponse.getReportBytes();
		// FileOutputStream fio = new FileOutputStream("D:\\test.pdf");
		FileOutputStream fio = new FileOutputStream(outputFile);
		fio.write(baReport);
		fio.close();

		System.out.println("output:"+new String(baReport));
	}

	public void testRunReportWithParameters(String reportAbsolutePath) throws Exception {
		System.out.println("####  testRunReportWithParameters: '"+reportAbsolutePath+"' ####");
		ReportService reportService =getReportService();
		ReportRequest repRequest = new ReportRequest();
		repRequest.setReportAbsolutePath(reportAbsolutePath);
		// repRequest.setAttributeTemplate("Chart and Table Layout");
		//repRequest.setAttributeFormat("pdf");
		//repRequest.setAttributeFormat("text/xml"); // DO NOT USE THIS AS IT IS THE SERVER REPORT PROPERTIES THAT CONTROLS THE OUTPUT FORMAT.
		// SO IF THE REPORT IS SET AS XML AS DEFAULT THEN THAT'S WHAT WE WILL RECEIVE.
		// repRequest.setAttributeLocale("en-US");
		repRequest.setSizeOfDataChunkDownload(-1);

		ArrayOfParamNameValue arrayOfParamNameValue = new 	ArrayOfParamNameValue();
		ParamNameValues paramNameValues = new ParamNameValues();

		ParamNameValue paramNameValue = new ParamNameValue();
		ArrayOfString arrayOfString = new ArrayOfString();
		paramNameValue.setName("INC_LAST_UPDATED_BY");
		arrayOfString.getItem().add("1");
		paramNameValue.setValues(arrayOfString);
		arrayOfParamNameValue.getItem().add(paramNameValue);

		paramNameValue = new ParamNameValue();
		arrayOfString = new ArrayOfString();
		paramNameValue.setName("LAST_UPDATED_BY_USER_NAME_LIST");
		arrayOfString.getItem().add("RAPID_PD");
		paramNameValue.setValues(arrayOfString);
		arrayOfParamNameValue.getItem().add(paramNameValue);


		paramNameValues.setListOfParamNameValues(arrayOfParamNameValue);
		repRequest.setParameterNameValues(paramNameValues);

		/*
		 * Note that parameters updates in BI publisher take about 1 minute to refresh when called by Web services (old parameters values still show)
		 */
		/*
	    	ParamNameValues paramNameValuesT=reportService.getReportParameters(repRequest, username, password);
	    	System.out.println("paramNameValuesT:"+paramNameValuesT.toString());
	    	ArrayOfParamNameValue arrayOfParamNameValueT= paramNameValuesT.getListOfParamNameValues();
	    	List<ParamNameValue> list=arrayOfParamNameValueT.getItem();
	    	for (ParamNameValue item:list) {
	    		System.out.println("item.getName():"+item.getName());
	    		System.out.println("item.getValues():"+item.getValues());
	    		System.out.println("item.getDefaultValue():"+item.getDefaultValue());

	    		if (item.getName().equalsIgnoreCase("INC_LAST_UPDATED_BY")) {
	    			ArrayOfString arrayOfString=new ArrayOfString();
	    			arrayOfString.getItem().add("1");
	    			item.setValues(arrayOfString);
	    			//item.setDefaultValue("hello world");
	    			System.out.println("item.getValues() after update:"+item.getValues().getItem());
	    		}

	    		if (item.getName().equalsIgnoreCase("LAST_UPDATED_BY_USER_NAME_LIST")) {
	    			ArrayOfString arrayOfString=new ArrayOfString();
	    			arrayOfString.getItem().add("RAPID_PD");
	    			item.setValues(arrayOfString);
	    			System.out.println("item.getValues() after update:"+item.getValues().getItem());
	    		}
	    	}
	    	repRequest.setParameterNameValues(paramNameValuesT);
		 */
		ReportResponse reportResponse = reportService.runReport(repRequest, username, password);
		saveOutput(reportResponse,new File("testRunReportWithParameters.xml"));
	}

	public void testGetReportTemplate() throws Exception {
		System.out.println("####  testCreateReport ####");
		ReportService reportService =getReportService();

		String reportAbsolutePath="/TESTOD REPORT.xdo";
		String templateID="T1";//"TEsdfsdfT2222";
		String locale=null;//"sdfsdf";

		byte[] responseText=reportService.getTemplate(reportAbsolutePath, templateID, locale, username, password);
		//byte[] responseText=reportService.getReportSampleData(reportAbsolutePath,username, password);
		//System.out.println("responseText:"+responseText);

		//byte[] decoded = Base64.decodeBase64(responseText);
		//System.out.println("decoded: '"+new String(decoded)+"'");

		FileOutputStream fio = new FileOutputStream(new File("HELLO.xpt"));
		fio.write(responseText);
		fio.close();

		//byte[] bytesEncoded = Base64.encodeBase64("PD94bWwgdmVyc21vbj0iMS4wIiB1bmNvZG1uZz0iVVRGLTgiPz48dGVzdD48ZmllbGRfdmFsPkItQi1CLUItQjwvZmllbGRfdmFsPjwvdGVzdD4=".getBytes());
		//byte[] bytesEncoded = "PD94bWwgdmVyc21vbj0iMS4wIiB1bmNvZG1uZz0iVVRGLTgiPz48dGVzdD48ZmllbGRfdmFsPkItQi1CLUItQjwvZmllbGRfdmFsPjwvdGVzdD4=".getBytes();
		//System.out.println("ecncoded value is " + new String(bytesEncoded ));
		// Decode data on other side, by processing encoded data
		//byte[] valueDecoded= Base64.decodeBase64(bytesEncoded );
		//System.out.println("Decoded value is " + new String(valueDecoded));
	}

	public void testCreateReport() throws Exception {
		System.out.println("####  testCreateReport ####");
		ReportService reportService =getReportService();

		Path path = Paths.get("HELLO.xpt");
		byte[] data = Base64.encodeBase64(Files.readAllBytes(path));

		String reportName="TESTOD1";
		String folderAbsolutePathURL="/";
		String dataModelURL="/testOD.xdm";
		String templateFileName=null;
		byte[] templateData=data;
		String xliffFileName=null;
		byte[] xliffData=null;
		boolean updateFlag=true;

		String responseText=reportService.createReport(reportName, folderAbsolutePathURL, dataModelURL, templateFileName,
				templateData, xliffFileName, xliffData, updateFlag, username, password);
		System.out.println("responseText:"+responseText);

		String reportAbsolutePath="/"+reportName+".xdo";

		updateReportDefinitionToXMLOnly(reportAbsolutePath);

		byte[] templateXPT=reportService.getTemplate(reportAbsolutePath, "Default Template", null, username, password);
		FileOutputStream fio = new FileOutputStream(new File("Default Template.xpt"));
		fio.write(templateXPT);
		fio.close();

		//testRunReportWithParameters(reportAbsolutePath);
	}

	public void updateReportDefinitionToXMLOnly(String reportAbsolutePath) throws Exception {
		System.out.println("####  updateReportDefinition ####");
		ReportService reportService =getReportService();
		System.out.println("reportAbsolutePath:"+reportAbsolutePath);
		ReportDefinition reportDefinition=reportService.getReportDefinition(reportAbsolutePath,  username, password);
		System.out.println("BEFORE CHANGES:");
		printReportDefinition(reportDefinition);

		ArrayOfTemplateFormatsLabelValues list=reportDefinition.getListOfTemplateFormatsLabelValues();    	
		for (TemplateFormatsLabelValues item:list.getItem()) {
			//item.setTemplateID("T2"); // !!!!!!!! THIS DOES NOT WORK !!!!!!!
			ArrayOfTemplateFormatLabelValue array=item.getListOfTemplateFormatLabelValue();
			array.getItem().clear();
			TemplateFormatLabelValue templateFormatLabelValue=new TemplateFormatLabelValue();
			templateFormatLabelValue.setTemplateFormatLabel("Data");
			templateFormatLabelValue.setTemplateFormatValue("xml");
			array.getItem().add(templateFormatLabelValue);
		}
		//reportDefinition.setDefaultOutputFormat(newDefaultOutputFormat); // !!!!!!!! THIS DOES NOT WORK !!!!!!!
		reportService.updateReportDefinition(reportAbsolutePath, reportDefinition, username, password);

		reportDefinition=reportService.getReportDefinition(reportAbsolutePath,  username, password);
		System.out.println("AFTER CHANGES:");
		printReportDefinition(reportDefinition);
	}

	public void printReportDefinition(ReportDefinition reportDefinition) throws Exception {
		System.out.println("reportDefinition.getDataModelURL():"+reportDefinition.getDataModelURL());
		System.out.println("reportDefinition.getDefaultOutputFormat():"+reportDefinition.getDefaultOutputFormat());
		System.out.println("reportDefinition.getDefaultTemplateId():"+reportDefinition.getDefaultTemplateId());
		System.out.println("reportDefinition.getReportType():"+reportDefinition.getReportType());
		System.out.println("reportDefinition.getReportName():"+reportDefinition.getReportName());
		ArrayOfString tids=reportDefinition.getTemplateIds();
		System.out.println("reportDefinition, start getTemplateIds:"+tids.getItem().size());
		for (String item:tids.getItem()) {
			System.out.println("reportDefinition, item:"+item);
		}
		System.out.println("reportDefinition, end getTemplateIds");
		ArrayOfTemplateFormatsLabelValues list=reportDefinition.getListOfTemplateFormatsLabelValues();
		System.out.println("reportDefinition, start getListOfTemplateFormatsLabelValues:"+list.getItem().size());
		for (TemplateFormatsLabelValues item:list.getItem()) {
			System.out.println("reportDefinition, item.getTemplateID:"+item.getTemplateID());
			System.out.println("reportDefinition, item.getTemplateBaseLocale:"+item.getTemplateBaseLocale());
			System.out.println("reportDefinition, item.getTemplateDefaultLocale:"+item.getTemplateDefaultLocale());
			System.out.println("reportDefinition, item.getTemplateType:"+item.getTemplateType());
			System.out.println("reportDefinition, item.getTemplateURL:"+item.getTemplateURL());
			ArrayOfTemplateFormatLabelValue array=item.getListOfTemplateFormatLabelValue();
			for (TemplateFormatLabelValue it:array.getItem()) {
				System.out.println("reportDefinition, it.getTemplateFormatLabel:"+it.getTemplateFormatLabel());
				System.out.println("reportDefinition, it.getTemplateFormatValue:"+it.getTemplateFormatValue());
			}
		}
		System.out.println("reportDefinition, end getListOfTemplateFormatsLabelValues");
	}

	public void createCatalogFolders(String folderAbsolutePath) throws Exception {
		System.out.println("####  createCatalogFolders ####");
		System.out.println("folderAbsolutePath:"+folderAbsolutePath);
		CatalogService catalogService=getCatalogService();

		String returnValue=catalogService.createFolder(folderAbsolutePath,  username, password);
		System.out.println("returnValue:"+returnValue);
	}

	public void uploadDataSet(String datasetObjectAbsolutePathURL) throws Exception {
		System.out.println("####  uploadDataSet ####");
		System.out.println("datasetObjectAbsolutePathURL:"+datasetObjectAbsolutePathURL);
		CatalogService catalogService=getCatalogService();

		String objectType="xdmz";

		Path path = Paths.get("C:/Users/olivier.deruelle/Documents/FUSIONS/ORACLE BI PUBLISHER/testDS2.xdmz");
		byte[] objectZippedData=Files.readAllBytes(path);

		String returnValue=catalogService.uploadObject(datasetObjectAbsolutePathURL, objectType, objectZippedData, username, password);
		System.out.println("returnValue:"+returnValue);
	}

	public void uploadReport(String reportObjectAbsolutePathURL) throws Exception {
		System.out.println("####  uploadReport ####");
		System.out.println("reportObjectAbsolutePathURL:"+reportObjectAbsolutePathURL);
		CatalogService catalogService=getCatalogService();

		String objectType="xdoz";

		Path path = Paths.get("C:/Users/olivier.deruelle/Documents/FUSIONS/ORACLE BI PUBLISHER/testReport1.xdoz");
		byte[] objectZippedData=Files.readAllBytes(path);

		String returnValue=catalogService.uploadObject(reportObjectAbsolutePathURL, objectType, objectZippedData, username, password);
		System.out.println("returnValue:"+returnValue);
	}
	
	public void printFolderContents(String folderAbsolutePath) throws Exception {
		System.out.println("####  printFolderContents ####");
		System.out.println("folderAbsolutePath:"+folderAbsolutePath);
		CatalogService catalogService=getCatalogService();

		CatalogContents catalogContents=catalogService.getFolderContents(folderAbsolutePath, username, password);
		ArrayOfItemData arrayOfItemData=catalogContents.getCatalogContents();
		
		List<ItemData> itemList=arrayOfItemData.getItem();
		System.out.println("printFolderContents, itemList.size():"+itemList.size());
		for (ItemData itemData:itemList) {
			System.out.println("printFolderContents, itemData.getDisplayName(): '"+itemData.getDisplayName()+"'");
		}
	}

	public void parseNavigator(File file) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(file);

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        NodeList nodes = (NodeList)xp.evaluate("//n[@t='Define Common Applications Configuration for Financials']", doc, XPathConstants.NODESET);

        System.out.println(nodes.getLength());

        for (int i=0, len=nodes.getLength(); i<len; i++) {
            Node item = nodes.item(i);
            System.out.println(item.getNodeName() + " : " + item.getTextContent());
            NamedNodeMap attributes=item.getAttributes();
            for (int j=0;j<attributes.getLength();j++) {
            	Node attribute=attributes.item(j);
            	System.out.println("Attribute: "+attribute.getNodeName() + " = " + attribute.getNodeValue());
            }
            Node tempNode=item;
            while (tempNode.getParentNode()!=null) {
            	System.out.println("tempNode: t: '"+tempNode.getAttributes().getNamedItem("t")+"'");
            	tempNode=tempNode.getParentNode();
            }
        }
    }
	
	public void parseNavigator2(File file) throws Exception {
		FileInputStream fis = new FileInputStream(file);

		final XmlNavigatorParser xmlNavigatorParser = new XmlNavigatorParser();
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser parser = factory.newSAXParser();
		parser.parse(new InputSource(new InputStreamReader(fis, CoreConstants.CHARACTER_SET_ENCODING)), xmlNavigatorParser);
		fis.close();

		List<EbsResponsibilityRecord> list= xmlNavigatorParser.allRecords;
		Map<String,NavigatorNode> lookup=new TreeMap<String,NavigatorNode>();
		for (EbsResponsibilityRecord ebsResponsibilityRecord: list) {
			String subMenuId=ebsResponsibilityRecord.getSubMenuId();
			String functionId=ebsResponsibilityRecord.getFormFunctionId();
			
			String key=null;
			if (subMenuId==null || subMenuId.isEmpty()) {
				key="F-"+functionId;
			}
			else {
				key="M-"+subMenuId;
			}
			NavigatorNode node=lookup.get(key);
			if (node==null) {
				lookup.put(key, new NavigatorNode(ebsResponsibilityRecord));
			}
		}

		Set<String> keySet = lookup.keySet();
		for (String id : keySet) {
			NavigatorNode navigatorNode = lookup.get(id);
			String parentId = navigatorNode.getEbsResponsibilityRecord().getMenuId();
			NavigatorNode parentNavigatorNode = lookup.get("M-"+parentId);
			if (parentNavigatorNode != null) {
				navigatorNode.setParent(parentNavigatorNode);
				parentNavigatorNode.addChild(navigatorNode);
			}
		}
		NavigatorNode navigatorNode=lookup.get("F-10391");
		System.out.println("navigatorNode getMenuId: '"+navigatorNode.getEbsResponsibilityRecord().getMenuId()+"'");
		List<String> fullPathList=navigatorNode.getFullPathList();
		for (String item:fullPathList) {
			System.out.println("item: '"+item+"'");
		}
		/*
		NavigatorNode tempNode=navigatorNode;
		System.out.println("navigatorNode: "+navigatorNode);
		while (tempNode.getParent()!=null) {
			System.out.println("tempNode: t: '"+tempNode.getEbsResponsibilityRecord().getDisplayText()+"'");
			tempNode=tempNode.getParent();
		}
		*/		
	}
	
	public void runSameReportInParallel(String reportAbsolutePath) throws Exception {
		int workersThread=500;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (int i = 0; i < workersThread; i++) {
        	Runnable worker = new WorkerThread(reportAbsolutePath);
        	executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
	}

	public class WorkerThread implements Runnable {

		private String reportAbsolutePath;

		public WorkerThread(String reportAbsolutePath){
			this.reportAbsolutePath=reportAbsolutePath;
		}

		@Override
		public void run() {
			try {
				System.out.println("####  runSameReportInParallel: '"+reportAbsolutePath+"' ####");
	    		ReportService reportService =getReportService();
	    		ReportRequest repRequest = new ReportRequest();
	    		repRequest.setReportAbsolutePath(reportAbsolutePath);
	    		repRequest.setSizeOfDataChunkDownload(-1);
	    		ReportResponse reportResponse = reportService.runReport(repRequest, username, password);
	    		byte[] baReport = reportResponse.getReportBytes();
	    		System.out.println("output received!");
	    		System.out.println("output received:"+new String(baReport));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void runReportThread(final String reportAbsolutePath) {
		Thread t = new Thread()
		{
			public void run()
			{
				try {
					System.out.println("####  runSameReportInParallel: '"+reportAbsolutePath+"' ####");
		    		ReportService reportService =getReportService();
		    		ReportRequest repRequest = new ReportRequest();
		    		repRequest.setReportAbsolutePath(reportAbsolutePath);
		    		repRequest.setSizeOfDataChunkDownload(-1);
		    		ReportResponse reportResponse = reportService.runReport(repRequest, username, password);
		    		byte[] baReport = reportResponse.getReportBytes();
		    		System.out.println("output received!");
		    		System.out.println("output received:"+new String(baReport));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
	}

	public static void main(String[] args) throws Exception {
		TestMain testMain=new TestMain();
		
		testMain.runSameReportInParallel("/RapidExtract/olivier.deruelle7631/25-Mar-2016 10-15-43/Reports/-1000918467.xdo");
		
		//File file=new File("D:/RES-REPOSITORY/rapidesuite/controldata/FUSION_11.1.9/trunk/core/knowledgebase/swiftconfig-navigator.xml");
		//testMain.parseNavigator2(file);
		
		//testMain.printFolderContents("/");
		
		//testMain.createCatalogFolders("/RapidExtractDataSets");
		//testMain.uploadDataSet("/RapidExtractDataSets/testDS2");
		//testMain.uploadReport("/testReport1");

		//String reportAbsolutePath =null;
		//reportAbsolutePath="/TESTOD REPORT.xdo";
		//reportAbsolutePath="/TESTOD1.xdo";
		//reportAbsolutePath="/testReport1.xdo";
		//testMain.testRunReportWithParameters(reportAbsolutePath);
		
		//testMain.testCreateReport();
		//testMain.testGetReportTemplate();
		//testMain.updateReportDefinitionToXMLOnly(reportAbsolutePath);
	}

}
