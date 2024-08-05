package com.rapidesuite.reverse.utils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import com.rapidesuite.client.common.gui.ExecutionStatusTreeTableNode;
import com.rapidesuite.client.common.gui.TextTreeNode;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.TemplateUtils;
import com.rapidesuite.reverse.session.DataExtractionSession;
import com.rapidesuite.reverse.session.Ous;
import com.rapidesuite.reverse.session.Ous.OperatingUnit;
import com.rapidesuite.reverse.session.Users;
import com.rapidesuite.reverse.session.Users.OracleUser;
import com.rapidesuite.reverse.gui.DataExtractionPanel;
import com.rapidesuite.reverse.utils.DataExtractionDatabaseUtils.OracleUserRetrievalExecutionMonitor;

public class DataExtractionTemplateUtils
{

	public static String SESSION_EXTRACTION_FOLDER_NAME="extraction";	
	
	public static DataExtractionSession createDataExtractionSession(final DataExtractionPanel dataExtractionPanel) {
		final DataExtractionSession dataExtractionSession = new DataExtractionSession();

		dataExtractionSession.setWc(BigInteger.valueOf(dataExtractionPanel.getDataExtractionOptionsPanel().getWorkersCount()));
		dataExtractionSession.setOus(new Ous());
		Map<Long, String> operatingUnitIdToNameMap=dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getOperatingUnitSelectionPanel().getOperatingUnitIdToNameMap();
		for (final Long operatingUnitId : dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getOperatingUnitSelectionPanel().getSelectedOperatingUnitIds()) {
			OperatingUnit ou = new OperatingUnit();
			ou.setOuId(BigInteger.valueOf(operatingUnitId));
			ou.setValue(operatingUnitIdToNameMap.get(operatingUnitId));
			dataExtractionSession.getOus().getOperatingUnit().add(ou);
		}
		dataExtractionSession.setOperatingUnitSpecificType(dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().isOperatingUnitLevelSelected());
		dataExtractionSession.setInstanceLevelType(dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().isInstanceLevelSelected());
		
		dataExtractionSession.setUsers(new Users());
		final Set<Integer> allSelectedUserIds = new HashSet<Integer>();
		allSelectedUserIds.addAll(dataExtractionPanel.getOracleUsersSelectionPanel().getSelectedSeededOracleUserIds());
		allSelectedUserIds.addAll(dataExtractionPanel.getOracleUsersSelectionPanel().getSelectedStandardOracleUserIds());
		for (final Integer userId : allSelectedUserIds) {
			OracleUser oracleUser = new OracleUser();
			oracleUser.setUserId(BigInteger.valueOf(userId));
			dataExtractionSession.getUsers().getOracleUser().add(oracleUser);
		}
		
		dataExtractionSession.setOracleUserInclude(dataExtractionPanel.getOracleUsersSelectionPanel().isIncludeOracleUsers());
		dataExtractionSession.setZipFolder(dataExtractionPanel.getDataExtractionOptionsPanel().getSelectedZipFolder().getAbsolutePath());
		dataExtractionSession.setAnalyseMode(dataExtractionPanel.getDataExtractionOptionsPanel().isAnalyseMode());
		dataExtractionSession.setFromDate(dataExtractionPanel.getOracleDatesSelectionPanel().getFormattedFromDate());
		dataExtractionSession.setToDate(dataExtractionPanel.getOracleDatesSelectionPanel().getFormattedToDate());
		
		List<TreePath> selectedPaths=dataExtractionPanel.getInventoriesTreePanel().getAllCheckedPaths();
		if (selectedPaths!=null) {
			for (TreePath path:selectedPaths) {
				Object object=path.getLastPathComponent();
				final String nodePathValue;
				if (object instanceof ExecutionStatusTreeTableNode) {
					ExecutionStatusTreeTableNode node= (ExecutionStatusTreeTableNode)object;
					//write "null" string if node.getPath() is null
					nodePathValue = String.valueOf(node.getNodePath());
				} else {
					TextTreeNode node= (TextTreeNode)object;
					//write "null" string if node.getPath() is null
					nodePathValue = String.valueOf(node.getNodePath());
				}
				
				dataExtractionSession.getNode().add(nodePathValue);
			}
		}	
		
		return dataExtractionSession;
		
	}	

	public static void saveSession(DataExtractionPanel dataExtractionPanel,final File templateFolder,String templateName)
	throws Exception{

		final DataExtractionSession dataExtractionSession = createDataExtractionSession(dataExtractionPanel);
		com.rapidesuite.reverse.session.ObjectFactory of = new com.rapidesuite.reverse.session.ObjectFactory();
        JAXBContext jaxbContext = JAXBContext.newInstance(com.rapidesuite.reverse.session.DataExtractionSession.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(of.createDataExtractionSession(dataExtractionSession), sw);
        final StringBuffer content = sw.getBuffer();
        
		File sessionFile=TemplateUtils.getTemplateFile(
				templateName,templateFolder);
		TemplateUtils.writeTemplateToFile(sessionFile,content);
	}
		
	public static void restoreSession(DataExtractionPanel dataExtractionPanel,final File templateFolder,String templateName, final OracleUserRetrievalExecutionMonitor cancelFlag, final JLabel progressLabel)
	throws Exception{
		File templateFile= TemplateUtils.getTemplateFile(templateName,templateFolder);
			
		DataExtractionSession dataExtractionSession = null;
		if (templateFile != null && templateFile.isFile()) {
			try (final Reader reader = new FileReader(templateFile)) {
		        Unmarshaller jaxbUnmarshaller = JAXBContext.newInstance(DataExtractionSession.class).createUnmarshaller();
		        jaxbUnmarshaller.setEventHandler(new ValidationEventHandler() {

					@Override
					public boolean handleEvent(ValidationEvent event) {
				        FileUtils.println("Session file parsing error: "+event.getMessage());
						if (event.getMessage() != null && event.getMessage().contains("uri:\"\"")) {
							throw new RuntimeException("Failed to parse the session file, please generate a new one. Detailed error message is available in system log.");
						}				        
						return event.getSeverity() != ValidationEvent.ERROR && event.getSeverity() != ValidationEvent.FATAL_ERROR;
					}
		        	
		        });
		        JAXBElement<DataExtractionSession> root = jaxbUnmarshaller.unmarshal(new StreamSource(reader), DataExtractionSession.class);
		        dataExtractionSession = root.getValue();			
			}			
		}
		
		Map<String,String> selectedPaths=new HashMap<String, String>();
		if (dataExtractionSession != null) {
			List<Integer> selectedOracleUserIds = new ArrayList<Integer>();
			for (final OracleUser oracleUser : dataExtractionSession.getUsers().getOracleUser()) {
				selectedOracleUserIds.add(oracleUser.getUserId().intValue());
			}
			dataExtractionPanel.getOracleUsersSelectionPanel().restoreOracleUsers(selectedOracleUserIds, cancelFlag, progressLabel);			
			
			Map<Integer,String> selectedOperatingUnitIdToNameMap= new HashMap<Integer, String>();
			for (final OperatingUnit operatingUnit : dataExtractionSession.getOus().getOperatingUnit()) {
				selectedOperatingUnitIdToNameMap.put(operatingUnit.getOuId().intValue(), operatingUnit.getValue());
			}
			dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().getOperatingUnitSelectionPanel().restoreOperatingUnits(selectedOperatingUnitIdToNameMap);
			dataExtractionPanel.getOracleUsersSelectionPanel().setOracleUsersInclude(dataExtractionSession.isOracleUserInclude());
			dataExtractionPanel.getDataExtractionOptionsPanel().setWorkersCount(dataExtractionSession.getWc().intValue());
			if (dataExtractionSession.getZipFolder() != null && !dataExtractionSession.getZipFolder().isEmpty()) {
				dataExtractionPanel.getDataExtractionOptionsPanel().setSelectedZipFolder(new File(dataExtractionSession.getZipFolder()));
			}
			dataExtractionPanel.getDataExtractionOptionsPanel().setAnalyseMode(dataExtractionSession.isAnalyseMode());
			String fromDate = dataExtractionSession.getFromDate();
			String toDate = dataExtractionSession.getToDate();
			dataExtractionPanel.getOracleDatesSelectionPanel().restoreDates(fromDate, toDate);

			for (String path : dataExtractionSession.getNode()) {
				if (path == null || path.isEmpty() ){
					continue;
				}
				// replace number followed by '-' (without quotes) (e.g. 0001-) from the node name for matching and display.
				// once it is removed, it will not be saved to the newly created template sessions.
				path = path.replaceAll("###[0-9]{4}-", "###");
				selectedPaths.put(path, path);
			}
			boolean isOperatingUnitLevelSelected = dataExtractionSession.isOperatingUnitSpecificType();
			boolean isInstanceLevelSetupSelected = dataExtractionSession.isInstanceLevelType();
			dataExtractionPanel.getDataExtractionEBSLevelSelectionPanel().restoreTypes(isInstanceLevelSetupSelected,isOperatingUnitLevelSelected);
		}
		dataExtractionPanel.reloadTree();
		if (dataExtractionSession==null) {
			dataExtractionPanel.getInventoriesTreePanel().selectAllPaths();
		} else {
			dataExtractionPanel.getInventoriesTreePanel().restoreSelectedPaths(selectedPaths);
		}		
	}
	
}