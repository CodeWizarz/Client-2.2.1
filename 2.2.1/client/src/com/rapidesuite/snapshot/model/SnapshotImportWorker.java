package com.rapidesuite.snapshot.model;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.core.CoreConstants;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.snapshot.controller.SnapshotImportController;
import com.rapidesuite.snapshot.view.SnapshotInventoryGridRecord;
import com.rapidesuite.snapshot.view.UIConstants;
import com.rapidesuite.snapshot.view.UIUtils;

public class SnapshotImportWorker extends GenericWorker{

	private int totalImportedRecords;
	private SnapshotInventoryGridRecord snapshotInventoryGridRecord;
	private SnapshotImportController snapshotImportController;
	
	public SnapshotImportWorker(SnapshotImportController snapshotImportController) {
		super(snapshotImportController,true);
	}
		
	@Override
	public void execute(Object task) {
		snapshotInventoryGridRecord=(SnapshotInventoryGridRecord)task;
		snapshotImportController=(SnapshotImportController)super.getGenericController();
		try {
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_PROCESSING);
			snapshotInventoryGridRecord.setDownloadStartTime(System.currentTimeMillis());
			
			String currentConnectionOracleRelease=snapshotImportController.getTabSnapshotsPanel().getServerSelectionPanel().getCurrentConnectionOracleRelease();
			File inventoryFile=snapshotImportController.getTabSnapshotsPanel().getSnapshotPackageSelectionPanel().
					getInventoryNameToInventoryFileMap(currentConnectionOracleRelease).get(snapshotInventoryGridRecord.getInventoryName());
			if (inventoryFile==null) {
				throw new Exception("Unable to find the inventory file: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			}
			
			Inventory inventory=FileUtils.getInventory(inventoryFile,snapshotInventoryGridRecord.getInventoryName());
			snapshotInventoryGridRecord.setInventory(inventory);

			File dataFile=new File(snapshotImportController.getSourceFolder(),snapshotInventoryGridRecord.getInventoryName()+".xml");
        	if (!dataFile.exists()) {
        		snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_COMPLETED);
				snapshotInventoryGridRecord.setDownloadRemarks("Ignored - missing data file in the zip archive!");
				return;
        	}			
			
			String dbSchema=ModelUtils.getDBUserName(ModelUtils.getSnapshotEnvironmentProperties(snapshotImportController.getTabSnapshotsPanel()));
			File plsqlPackageFile=snapshotImportController.getTabSnapshotsPanel().
					getSnapshotPackageSelectionPanel().getPLSQLPackageFile(currentConnectionOracleRelease);
			
			File mainSQLQueryFile=snapshotImportController.getTabSnapshotsPanel().
					getSnapshotPackageSelectionPanel().getInventoryNameToReverseSQLFileMap(currentConnectionOracleRelease).get(
					snapshotInventoryGridRecord.getInventoryName());
			if (mainSQLQueryFile==null) {
				throw new SnapshotWarningException("Unsupported (SQL not available)");
			}
			
			String sqlQuery=ModelUtils.getSQLQueryAfterKeywordsReplacement(snapshotImportController.getTabSnapshotsPanel().getMainPanel().getSnapshotMain()
					,mainSQLQueryFile,plsqlPackageFile,dbSchema);
			int largeColumnPosition=ModelUtils.getLargeColumnPosition(sqlQuery);
			int inventoryColumnsCount=inventory.getFieldsUsedForDataEntry().size();
			String dffStructureQuery=ModelUtils.getDFFStructureQuery(sqlQuery);
			boolean isDFFConversionType=dffStructureQuery!=null && getSnapshotImportController().getSnapshotGridRecord().isConversion();
			
			boolean isTableAlreadyCreated=snapshotImportController.getTableIds().contains(snapshotInventoryGridRecord.getTableId());
			if (!isTableAlreadyCreated) {
				snapshotInventoryGridRecord.setDownloadRemarks("Creating DB Table.");
				ModelUtils.createTables(this.getJDBCConnection(), 
						dbSchema,inventory,snapshotInventoryGridRecord.getTableId(),
						inventoryColumnsCount,isDFFConversionType,largeColumnPosition);
			}
			snapshotInventoryGridRecord.setDownloadRemarks("Importing Data...");			
			int importedRecords=importData(isDFFConversionType);
			snapshotInventoryGridRecord.setDownloadDownloadedRecordsCount(importedRecords);
			
			totalImportedRecords=totalImportedRecords+importedRecords;
			if(snapshotImportController.isCancelled()) {
				snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_CANCELLED);
			}
			else {
				snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_COMPLETED);
				snapshotInventoryGridRecord.setDownloadRemarks("");
			}
		}
		catch(Throwable e) {
			FileUtils.println("Error for inventory: '"+snapshotInventoryGridRecord.getInventoryName()+"'");
			FileUtils.printStackTrace(e);
			snapshotInventoryGridRecord.setDownloadStatus(UIConstants.UI_STATUS_FAILED);
			snapshotInventoryGridRecord.setDownloadRemarks(e.getMessage());
			super.setTotalFailedTasks(super.getTotalFailedTasks()+1);
		}
		finally  {
			UIUtils.setDownloadTime(snapshotInventoryGridRecord);
		}
	}

	private int importData(boolean isDFFConversionType) throws Exception {
		SnapshotDBDataImporter snapshotDBDataImporter=new SnapshotDBDataImporter(this,isDFFConversionType,Config.getSnapshotImportBatchSize());
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser parser = factory.newSAXParser();
        
        InputStream dataFileStream = null;
        try{
        	File dataFile=new File(snapshotImportController.getSourceFolder(),snapshotInventoryGridRecord.getInventoryName()+".xml");
        	dataFileStream=CoreUtil.getInputStream(false, dataFile);
        	parser.parse(new InputSource(new InputStreamReader(dataFileStream, CoreConstants.CHARACTER_SET_ENCODING)),snapshotDBDataImporter);
        }
        finally{
        	IOUtils.closeQuietly(dataFileStream);
        }
        return snapshotDBDataImporter.getRowCount();
	}

	public SnapshotInventoryGridRecord getSnapshotInventoryGridRecord() {
		return snapshotInventoryGridRecord;
	}

	public SnapshotImportController getSnapshotImportController() {
		return snapshotImportController;
	}

	public int getTotalImportedRecords() {
		return totalImportedRecords;
	}
	
}