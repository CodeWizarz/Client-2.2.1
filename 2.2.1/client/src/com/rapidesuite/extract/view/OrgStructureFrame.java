package com.rapidesuite.extract.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oracle.xmlns.oxp.service.v2.ReportService;
import com.rapidesuite.extract.ExtractMain;
import com.rapidesuite.extract.ExtractUtils;
import com.rapidesuite.extract.WebServiceInfo;
import com.rapidesuite.extract.model.BusinessUnitInformation;
import com.rapidesuite.extract.model.EnterpriseInformation;
import com.rapidesuite.extract.model.GenericObjectInformation;
import com.rapidesuite.extract.model.InventoryOrgInformation;
import com.rapidesuite.extract.model.LedgerInformation;
import com.rapidesuite.extract.model.LegalEntityInformation;
import com.rapidesuite.extract.model.ParameterNameValue;
import com.rapidesuite.inject.InjectUtils;
import com.rapidesuite.snapshot.view.UIUtils;

public class OrgStructureFrame {

	protected JDialog dialog;
	private JPanel mainPanel;
	private ApplicationInfoPanel applicationInfoPanel;
	private JTree tree;
	
	private Map<String, EnterpriseInformation> enterpriseIdToEnterpriseInformationMap;
	private List<EnterpriseInformation> orderedEnterpriseInformationList;
	private Map<String, LegalEntityInformation> legalEntityIdToLegalEntityInformationMap;
	private List<LegalEntityInformation> orderedLegalEntityInformationList;
	private Map<String, LedgerInformation> ledgerIdToLedgerInformationMap;
	private List<LedgerInformation> orderedLedgerInformationList;
	private Map<String,BusinessUnitInformation> businessUnitIdToBusinessUnitInformationMap;
	private List<BusinessUnitInformation> orderedBusinessUnitInformationList;
	private Map<String, InventoryOrgInformation> inventoryOrgIdToInventoryOrgInformationMap;
	private List<InventoryOrgInformation> orderedInventoryOrgInformationList;
	
	private Map<String, List<String>> enterpriseIdToListLegalEntityMap;
	private Map<String, List<String>> legalEntityIdToListLedgerIdMap;
	private Map<String, List<String>> ledgerIdToListBusinessIdMap;
	private Map<String, List<String>> businessUnitIdToListOrganizationIdMap;
	
	private Map<String, String> legalEntityIdToEnterpriseIdMap;
	private Map<String, String> ledgerIdToLegalEntityIdMap;
	private Map<String, String> businessUnitIdToLedgerIdMap;
	private Map<String, String> organizationIdToBusinessUnitIdMap;
		
	private JLabel totalEnterprisesLabel;
	private JLabel totalLegalEntitiesLabel;
	private JLabel totalLedgersLabel;
	private JLabel totalBusinessUnitsLabel;
	private JLabel totalInventoryOrganizationsLabel;
	private List<String> updatedListOfLegalEntities;
	
	public OrgStructureFrame(ApplicationInfoPanel applicationInfoPanel) {
		this.applicationInfoPanel=applicationInfoPanel;
		int width=790;
		int height=450;
		mainPanel=new JPanel();
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.decode("#dbdcdf"));
		//mainPanel.setBackground(Color.BLUE);
		
		createComponents();
		dialog=UIUtils.displayOperationInProgressComplexModalWindow(applicationInfoPanel.getExtractMain().getRootFrame(),
				"Organization Structure",width,height,mainPanel,null,true,ExtractMain.getSharedApplicationIconPath());
	}

	private void createComponents() {
		int widthEmptyBordersPanels=20;
		
		JPanel topPanel=new JPanel();
		topPanel.setOpaque(true);
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		//topPanel.setBackground(Color.GREEN);
		mainPanel.add(topPanel,BorderLayout.NORTH);
		
		JPanel centerPanel=new JPanel();
		centerPanel.setOpaque(true);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, widthEmptyBordersPanels, 0, widthEmptyBordersPanels));
		centerPanel.setLayout(new BorderLayout());
		//centerPanel.setBackground(Color.red);
		mainPanel.add(centerPanel,BorderLayout.CENTER);
		
		JPanel bottomPanel=new JPanel();
		bottomPanel.setOpaque(true);
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		//bottomPanel.setBackground(Color.WHITE);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 0));
		mainPanel.add(bottomPanel,BorderLayout.SOUTH);
		
		totalEnterprisesLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalEnterprisesLabel,UIConstants.FONT_SIZE_NORMAL);
		bottomPanel.add(totalEnterprisesLabel);
		totalLegalEntitiesLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalLegalEntitiesLabel,UIConstants.FONT_SIZE_NORMAL);
		bottomPanel.add(totalLegalEntitiesLabel);
		totalLedgersLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalLedgersLabel,UIConstants.FONT_SIZE_NORMAL);
		bottomPanel.add(totalLedgersLabel);
		totalBusinessUnitsLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalBusinessUnitsLabel,UIConstants.FONT_SIZE_NORMAL);
		bottomPanel.add(totalBusinessUnitsLabel);
		totalInventoryOrganizationsLabel=new JLabel();
		InjectUtils.assignArialPlainFont(totalInventoryOrganizationsLabel,UIConstants.FONT_SIZE_NORMAL);
		bottomPanel.add(totalInventoryOrganizationsLabel);
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
	    tree = new JTree(root);
	    tree.setRootVisible(false);
	    BasicTreeUI basicTreeUI = (BasicTreeUI) tree.getUI();
	    basicTreeUI.setLeftChildIndent(30);
	    
	    JScrollPane scrollPane = new JScrollPane(tree);
	    centerPanel.add(scrollPane,BorderLayout.CENTER);
	}
	
	public void createTree() throws Exception {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		
		String dynamicNonSQLReportPath=applicationInfoPanel.getDynamicNonSQLReportPath();
		WebServiceInfo reportWebServiceInfo=applicationInfoPanel.getReportWebServiceInfo();
		ReportService reportService=applicationInfoPanel.getReportService();
		
		initEnterpriseStructures(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
		initLegalEntityStructures(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
		initLedgerStructures(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
		initBusinessUnitStructures(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
		initInventoryOrganizationStructures(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
		
		initEnterpriseToLegalEntityMapping(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
		initLegalEntityToLedgerMapping(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
		initBusinessUnitToLedgerMapping(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
		initInventoryOrganizationToBusinessUnitMapping(dynamicNonSQLReportPath,reportWebServiceInfo,reportService);
		
		addEnterprises(root);
		
		for (InventoryOrgInformation inventoryOrgInformation:orderedInventoryOrgInformationList) {
			String businessUnitId=organizationIdToBusinessUnitIdMap.get(inventoryOrgInformation.getInventoryOrgId());
			if (businessUnitId==null) {
				System.out.println("Master org: '"+inventoryOrgInformation.getInventoryOrgName()+"'");
			}
		}
		
		totalEnterprisesLabel.setText("Total Enterprises: "+orderedEnterpriseInformationList.size());
		totalLegalEntitiesLabel.setText("Total Legal Entities: "+updatedListOfLegalEntities.size());
		totalLedgersLabel.setText("Total Ledgers: "+orderedLedgerInformationList.size());
		totalBusinessUnitsLabel.setText("Total Business Units: "+orderedBusinessUnitInformationList.size());
		totalInventoryOrganizationsLabel.setText("Total Inventory Organizations: "+orderedInventoryOrgInformationList.size());
		
		expandAll(tree, new TreePath(root));
	}
	
	@SuppressWarnings("rawtypes")
	private void expandAll(JTree tree, TreePath parent) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path);
			}
		}
		tree.expandPath(parent);
		// tree.collapsePath(parent);
	}
	
	private void addEnterprises(DefaultMutableTreeNode parentNode) {
		if (orderedEnterpriseInformationList!=null) {
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Enterprises ("+orderedEnterpriseInformationList.size()+")");
			parentNode.add(rootNode);
			for (EnterpriseInformation enterpriseInformation:orderedEnterpriseInformationList) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(enterpriseInformation.getEnterpriseName());
				rootNode.add(node);
			    addLegalEntities(node,enterpriseInformation);
			}
		}
	}
	
	private void addLegalEntities(DefaultMutableTreeNode parentNode, EnterpriseInformation enterpriseInformation) {
		List<String> listTemp=enterpriseIdToListLegalEntityMap.get(enterpriseInformation.getEnterpriseId());
		if (listTemp!=null) {
			
			// Ignoring Legal entities without anything under (just garbage entities)
			updatedListOfLegalEntities=new ArrayList<String>();
			for (String id:listTemp) {
				LegalEntityInformation legalEntityInformation=legalEntityIdToLegalEntityInformationMap.get(id);
				List<String> mappedValues=legalEntityIdToListLedgerIdMap.get(legalEntityInformation.getLegalEntityId());
				if (mappedValues!=null && !mappedValues.isEmpty()) {
					updatedListOfLegalEntities.add(id);
				}
			}			
			
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Legal Entities ("+updatedListOfLegalEntities.size()+")");
			parentNode.add(rootNode);
			for (String id:updatedListOfLegalEntities) {
				LegalEntityInformation legalEntityInformation=legalEntityIdToLegalEntityInformationMap.get(id);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(legalEntityInformation.getLegalEntityName());
				rootNode.add(node);
				addLedgers(node,legalEntityInformation);
			}
		}
	}
	
	private void addLedgers(DefaultMutableTreeNode parentNode, LegalEntityInformation legalEntityInformation) {
		List<String> listTemp=legalEntityIdToListLedgerIdMap.get(legalEntityInformation.getLegalEntityId());
		if (listTemp!=null) {
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Ledgers ("+listTemp.size()+")");
			parentNode.add(rootNode);
			for (String id:listTemp) {
				LedgerInformation ledgerInformation=ledgerIdToLedgerInformationMap.get(id);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(ledgerInformation.getLedgerName());
				rootNode.add(node);
				addBusinessUnits(node,ledgerInformation);
			}
		}
	}
	
	private void addBusinessUnits(DefaultMutableTreeNode parentNode,LedgerInformation ledgerInformation) {
		List<String> listTemp=ledgerIdToListBusinessIdMap.get(ledgerInformation.getLedgerId());
		if (listTemp!=null) {
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Business Units ("+listTemp.size()+")");
			parentNode.add(rootNode);
			for (String id:listTemp) {
				BusinessUnitInformation businessUnitInformation=businessUnitIdToBusinessUnitInformationMap.get(id);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(businessUnitInformation.getBusinessUnitName());
				rootNode.add(node);
				addInventoryOrgs(node,businessUnitInformation);
			}
		}
	}
	
	private void addInventoryOrgs(DefaultMutableTreeNode parentNode,BusinessUnitInformation businessUnitInformation) {
		List<String> listTemp=businessUnitIdToListOrganizationIdMap.get(businessUnitInformation.getBusinessUnitId());
		if (listTemp!=null) {
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Inventory Organizations ("+listTemp.size()+")");
			parentNode.add(rootNode);
			for (String id:listTemp) {
				InventoryOrgInformation inventoryOrgInformation=inventoryOrgIdToInventoryOrgInformationMap.get(id);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(inventoryOrgInformation.getInventoryOrgName());
				rootNode.add(node);
			}
		}
	}
	
		
	private void initEnterpriseStructures(String dynamicNonSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		String sqlQuery="Select Organization_Id,Name From Fusion.Hr_Organization_V\n"+
					"where CLASSIFICATION_CODE = 'ENTERPRISE'and (sysdate BETWEEN EFFECTIVE_START_DATE AND EFFECTIVE_END_DATE) Order By upper(name)";

		List<GenericObjectInformation> list=initCollection(dynamicNonSQLReportPath,reportWebServiceInfo,reportService,sqlQuery,"Organization_Id","Name");			
		enterpriseIdToEnterpriseInformationMap=new HashMap<String,EnterpriseInformation>();
		orderedEnterpriseInformationList=new ArrayList<EnterpriseInformation>();
		for (GenericObjectInformation genericObjectInformation:list) {
			EnterpriseInformation enterpriseInformation=new EnterpriseInformation(genericObjectInformation);
			orderedEnterpriseInformationList.add(enterpriseInformation);
			enterpriseIdToEnterpriseInformationMap.put(enterpriseInformation.getEnterpriseId(), enterpriseInformation);
		}
	}
	
	private void initLegalEntityStructures(String dynamicNonSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		String sqlQuery="SELECT legal_entity_id,Legal_Entity_Identifier||' ('||name||')' RES_NAME FROM XLE_ENTITY_PROFILES ORDER BY Upper(res_name)";
		List<GenericObjectInformation> list=initCollection(dynamicNonSQLReportPath,reportWebServiceInfo,reportService,sqlQuery,"legal_entity_id","RES_NAME");			
		legalEntityIdToLegalEntityInformationMap=new HashMap<String,LegalEntityInformation>();
		orderedLegalEntityInformationList=new ArrayList<LegalEntityInformation>();
		for (GenericObjectInformation genericObjectInformation:list) {
			LegalEntityInformation legalEntityInformation=new LegalEntityInformation(genericObjectInformation);
			orderedLegalEntityInformationList.add(legalEntityInformation);
			legalEntityIdToLegalEntityInformationMap.put(legalEntityInformation.getLegalEntityId(), legalEntityInformation);
		}
	}
	
	private void initLedgerStructures(String dynamicNonSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		String sqlQuery="Select Ledger_Id,Name From Gl_Ledgers Where Chart_Of_Accounts_Id Is Not Null And\n"+
				"ledger_id in (\n"+
				"SELECT  GLLEDGERS.ledger_id\n"+
				" from GL_LEDGER_CONFIG_DETAILS GlLedgerConfigDetails\n"+
				",XLE_ENTITY_PROFILES XLEENTITYPROFILES\n"+
                "   ,GL_LEDGERS GlLedgers\n"+
                "  WHERE GLLEDGERCONFIGDETAILS.OBJECT_TYPE_CODE = 'LEGAL_ENTITY'\n"+
                "  AND GLLEDGERCONFIGDETAILS.SETUP_STEP_CODE = 'NONE'\n"+
                " AND XLEENTITYPROFILES.LEGAL_ENTITY_ID = GLLEDGERCONFIGDETAILS.OBJECT_ID\n"+
                " And Glledgerconfigdetails.Configuration_Id = Glledgers.Configuration_Id\n"+
                " And Glledgers.Ledger_Category_Code = 'PRIMARY'\n"+
                ")  Order By Upper(Name)";
		
		List<GenericObjectInformation> list=initCollection(dynamicNonSQLReportPath,reportWebServiceInfo,reportService,sqlQuery,"ledger_id","name");
		ledgerIdToLedgerInformationMap=new HashMap<String,LedgerInformation>();
		orderedLedgerInformationList=new ArrayList<LedgerInformation>();
		for (GenericObjectInformation genericObjectInformation:list) {
			LedgerInformation ledgerInformation=new LedgerInformation(genericObjectInformation);
			orderedLedgerInformationList.add(ledgerInformation);
			ledgerIdToLedgerInformationMap.put(ledgerInformation.getLedgerId(), ledgerInformation);
		}		
	}

	private void initBusinessUnitStructures(String dynamicNonSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		String sqlQuery="Select organization_id,name From Hr_Organization_V Where Classification_Code = 'FUN_BUSINESS_UNIT' and  organization_id in "+
				"(Select Bu_Id From Fun_Fin_Business_Units_V) order by upper(name)";
		List<GenericObjectInformation> list=initCollection(dynamicNonSQLReportPath,reportWebServiceInfo,reportService,sqlQuery,"organization_id","name");			
		businessUnitIdToBusinessUnitInformationMap=new HashMap<String,BusinessUnitInformation>();
		orderedBusinessUnitInformationList=new ArrayList<BusinessUnitInformation>();
		for (GenericObjectInformation genericObjectInformation:list) {
			BusinessUnitInformation businessUnitInformation=new BusinessUnitInformation(genericObjectInformation);
			orderedBusinessUnitInformationList.add(businessUnitInformation);
			businessUnitIdToBusinessUnitInformationMap.put(businessUnitInformation.getBusinessUnitId(), businessUnitInformation);
		}
	}
	
	private void initInventoryOrganizationStructures(String dynamicNonSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		String sqlQuery="SELECT distinct organization_id,name FROM HR_ORGANIZATION_V WHERE CLASSIFICATION_CODE = 'INV' order by upper(name)";
		List<GenericObjectInformation> list=initCollection(dynamicNonSQLReportPath,reportWebServiceInfo,reportService,sqlQuery,"organization_id","name");
		inventoryOrgIdToInventoryOrgInformationMap=new HashMap<String,InventoryOrgInformation>();
		orderedInventoryOrgInformationList=new ArrayList<InventoryOrgInformation>();
		for (GenericObjectInformation genericObjectInformation:list) {
			InventoryOrgInformation inventoryOrgInformation=new InventoryOrgInformation(genericObjectInformation);
			orderedInventoryOrgInformationList.add(inventoryOrgInformation);
			inventoryOrgIdToInventoryOrgInformationMap.put(inventoryOrgInformation.getInventoryOrgId(), inventoryOrgInformation);
		}		
	}
	
	private void initEnterpriseToLegalEntityMapping(String dynamicNonSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		String sqlQuery="SELECT legal_entity_id,enterprise_id FROM XLE_ENTITY_PROFILES";
		
		legalEntityIdToEnterpriseIdMap=new HashMap<String,String>();
		enterpriseIdToListLegalEntityMap=new HashMap<String,List<String>>();		
		List<GenericObjectInformation> list=initCollection(dynamicNonSQLReportPath,reportWebServiceInfo,reportService,sqlQuery,"legal_entity_id","enterprise_id");	
		for (GenericObjectInformation genericObjectInformation:list) {
			legalEntityIdToEnterpriseIdMap.put(genericObjectInformation.getValue1(), genericObjectInformation.getValue2());
		}
		
		for (LegalEntityInformation legalEntityInformation:orderedLegalEntityInformationList) {
			String id=legalEntityIdToEnterpriseIdMap.get(legalEntityInformation.getLegalEntityId());
			if (id!=null) {
				List<String> listTemp=enterpriseIdToListLegalEntityMap.get(id);
				if (listTemp==null) {
					listTemp=new ArrayList<String>();
					enterpriseIdToListLegalEntityMap.put(id, listTemp);
				}
				listTemp.add(legalEntityInformation.getLegalEntityId());
			}
		}
	}
	
	private void initLegalEntityToLedgerMapping(String dynamicNonSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		String sqlQuery="SELECT XLEENTITYPROFILES.LEGAL_ENTITY_ID, GLLEDGERS.ledger_id\n"+
					" from GL_LEDGER_CONFIG_DETAILS GlLedgerConfigDetails\n"+
					",XLE_ENTITY_PROFILES XLEENTITYPROFILES\n"+
                    ",GL_LEDGERS GlLedgers\n"+
                    " WHERE GLLEDGERCONFIGDETAILS.OBJECT_TYPE_CODE = 'LEGAL_ENTITY'\n"+
                    " AND GLLEDGERCONFIGDETAILS.SETUP_STEP_CODE = 'NONE'\n"+
                    " AND XLEENTITYPROFILES.LEGAL_ENTITY_ID = GLLEDGERCONFIGDETAILS.OBJECT_ID\n"+
                    " And Glledgerconfigdetails.Configuration_Id = Glledgers.Configuration_Id\n"+
                    " and GLLEDGERS.LEDGER_CATEGORY_CODE = 'PRIMARY'";
		
		ledgerIdToLegalEntityIdMap=new HashMap<String,String>();
		legalEntityIdToListLedgerIdMap=new HashMap<String,List<String>>();		
		
		List<GenericObjectInformation> list=initCollection(dynamicNonSQLReportPath,reportWebServiceInfo,reportService,sqlQuery,"ledger_id","LEGAL_ENTITY_ID");
		for (GenericObjectInformation genericObjectInformation:list) {
			ledgerIdToLegalEntityIdMap.put(genericObjectInformation.getValue1(), genericObjectInformation.getValue2());
		}
		
		for (LedgerInformation ledgerInformation:orderedLedgerInformationList) {
			String id=ledgerIdToLegalEntityIdMap.get(ledgerInformation.getLedgerId());
			if (id!=null) {
				List<String> listTemp=legalEntityIdToListLedgerIdMap.get(id);
				if (listTemp==null) {
					listTemp=new ArrayList<String>();
					legalEntityIdToListLedgerIdMap.put(id, listTemp);
				}
				listTemp.add(ledgerInformation.getLedgerId());
			}
		}
	}
	
	private void initBusinessUnitToLedgerMapping(String dynamicNonSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		String sqlQuery="Select Bu_Id,Primary_Ledger_Id From Fun_Fin_Business_Units_V";
		
		businessUnitIdToLedgerIdMap=new HashMap<String,String>();
		ledgerIdToListBusinessIdMap=new HashMap<String,List<String>>();		
		
		List<GenericObjectInformation> list=initCollection(dynamicNonSQLReportPath,reportWebServiceInfo,reportService,sqlQuery,"Bu_Id","Primary_Ledger_Id");
		for (GenericObjectInformation genericObjectInformation:list) {
			businessUnitIdToLedgerIdMap.put(genericObjectInformation.getValue1(), genericObjectInformation.getValue2());
		}
		
		for (BusinessUnitInformation businessUnitInformation:orderedBusinessUnitInformationList) {
			String id=businessUnitIdToLedgerIdMap.get(businessUnitInformation.getBusinessUnitId());
			if (id!=null) {
				List<String> listTemp=ledgerIdToListBusinessIdMap.get(id);
				if (listTemp==null) {
					listTemp=new ArrayList<String>();
					ledgerIdToListBusinessIdMap.put(id, listTemp);
				}
				listTemp.add(businessUnitInformation.getBusinessUnitId());
			}
		}
	}
	
	private void initInventoryOrganizationToBusinessUnitMapping(String dynamicNonSQLReportPath,WebServiceInfo reportWebServiceInfo,ReportService reportService) throws Exception {			
		String sqlQuery="SELECT ORGANIZATIONPARAMETEREO.ORGANIZATION_ID\n"+
						",ORGANIZATIONPARAMETEREO.BUSINESS_UNIT_ID\n"+
						"FROM INV_ORG_PARAMETERS OrganizationParameterEO\n"+
						",HR_ORGANIZATION_V InventoryOrganizationEO\n"+
						" WHERE InventoryOrganizationEO.ORGANIZATION_ID = OrganizationParameterEO.ORGANIZATION_ID\n"+
						" AND InventoryOrganizationEO.CLASSIFICATION_CODE = 'INV'\n"+
						" AND OrganizationParameterEO.INVENTORY_FLAG = 'Y'";
		
		organizationIdToBusinessUnitIdMap=new HashMap<String,String>();
		businessUnitIdToListOrganizationIdMap=new HashMap<String,List<String>>();		
		
		List<GenericObjectInformation> list=initCollection(dynamicNonSQLReportPath,reportWebServiceInfo,reportService,sqlQuery,"ORGANIZATION_ID","BUSINESS_UNIT_ID");
		for (GenericObjectInformation genericObjectInformation:list) {
			organizationIdToBusinessUnitIdMap.put(genericObjectInformation.getValue1(), genericObjectInformation.getValue2());
		}
		
		for (InventoryOrgInformation inventoryOrgInformation:orderedInventoryOrgInformationList) {
			String id=organizationIdToBusinessUnitIdMap.get(inventoryOrgInformation.getInventoryOrgId());
			if (id!=null) {
				List<String> listTemp=businessUnitIdToListOrganizationIdMap.get(id);
				if (listTemp==null) {
					listTemp=new ArrayList<String>();
					businessUnitIdToListOrganizationIdMap.put(id, listTemp);
				}
				listTemp.add(inventoryOrgInformation.getInventoryOrgId());
			}
		}
	}
	
		
	private List<GenericObjectInformation> initCollection(String dynamicNonSQLReportPath,
			WebServiceInfo reportWebServiceInfo,ReportService reportService,String sqlQuery,String field1,String field2) throws Exception {	
		List<ParameterNameValue> parametersList=new ArrayList<ParameterNameValue>();
		
		String parameter=sqlQuery;
					
		ParameterNameValue parameterNameValue=new ParameterNameValue();
		parametersList.add(parameterNameValue);
		parameterNameValue.setName("param1");
		parameterNameValue.setValue(parameter);
		
		byte[] outputBytes=ExtractUtils.runReportWithStringParameters(reportWebServiceInfo,reportService,dynamicNonSQLReportPath,parametersList);
//		String data=new String(outputBytes,"ISO-8859-1");
		//FileUtils.println("data:"+data);
		
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(outputBytes);
		Document document = builder.parse(input);
	
		PrintWriter pw=null;
        List<GenericObjectInformation> toReturn=new ArrayList<GenericObjectInformation>();
        NodeList nodeList = ExtractUtils.getNodeList(document);
        if (nodeList!=null && nodeList.getLength()>0)
        {
        	try{        		
        		for (int i = 0; i < nodeList.getLength(); i++) {
        			Node node = nodeList.item(i);
        			if (node.getNodeType() == Node.ELEMENT_NODE) {
        				Element element = (Element) node;

    					String valueField1=ExtractUtils.getDataFromBIOutput(element,field1);
    					String valueField2=ExtractUtils.getDataFromBIOutput(element,field2);
    					
    					GenericObjectInformation genericObjectInformation=new GenericObjectInformation(valueField1,valueField2);
    					toReturn.add(genericObjectInformation);
        			}
        		}
        	}
        	finally {
        		IOUtils.closeQuietly(pw);
        	}
        }
        return toReturn;
	}

	public void setVisible(boolean isVisible) {
		dialog.setVisible(isVisible);
	}
	
}
