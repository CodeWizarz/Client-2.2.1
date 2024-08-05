package com.rapidesuite.inject.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import com.erapidsuite.configurator.navigation0005.RepeatType;
import com.rapidesuite.inject.BatchInjectionTracker;
import com.rapidesuite.inject.RecordTracker;
import com.rapidesuite.inject.Worker;
import com.rapidesuite.inject.gui.ScriptsGrid;
import com.rapidesuite.inject.selenium.SeleniumUtils;
import com.rapidesuite.inject.selenium.SeleniumWorker;
import com.rapidesuite.inject.webservices.FusionWebServiceWorker;

public class RepeatTypeCommand extends Command{

	private boolean isUseExceptionAsRemark;
	private boolean isFailBatchOnFirstError;
	
	public RepeatTypeCommand(Worker worker,boolean isUseExceptionAsRemark,boolean isFailBatchOnFirstError) {
		super(worker);
		this.isUseExceptionAsRemark=isUseExceptionAsRemark;
		this.isFailBatchOnFirstError=isFailBatchOnFirstError;
	}

	public void process(RepeatType repeatType) throws Exception {
		String temp=repeatType.getInventoryName();
		String inventoryName=SeleniumUtils.applyParameters(worker,temp,((SeleniumWorker)worker).getParameterNameToValueMap());
		
		BatchInjectionTracker batchInjectionTracker=worker.getBatchInjectionTracker();

		worker.setCurrentRepeatInventoryName(inventoryName);
		
		worker.println("%%%%%%%%%%%%%%%  REPEAT %%%%%%%%%%%%%%%%%%%%%%%");
		Map<String, RecordTracker> inventoryToCurrentRecordTrackerMap=worker.getInventoryToCurrentRecordTrackerMap();
		List<RecordTracker> recordTrackerListToExecute=batchInjectionTracker.getRecordTrackerListToExecute(inventoryToCurrentRecordTrackerMap,inventoryName);
		worker.println("number of records in batch: "+recordTrackerListToExecute.size());
		for (RecordTracker recordTracker:recordTrackerListToExecute) {
			inventoryToCurrentRecordTrackerMap.put(inventoryName, recordTracker);
			int recordBatchId=recordTracker.getBatchId();
			boolean isRecordBelongToThisBatch=(recordBatchId==batchInjectionTracker.getBatchId());	
			worker.println("recordTracker: "+recordTracker.getGridIndex()+" isRecordBelongToThisBatch: "+isRecordBelongToThisBatch);
			if (worker instanceof FusionWebServiceWorker) {
				recordTracker.setStatus(ScriptsGrid.STATUS_PROCESSING);
				recordTracker.setRemarks("Executing Batch");
				recordTracker.setStartTime(System.currentTimeMillis());
			}
			else {
				if (isRecordBelongToThisBatch) {
					recordTracker.setStatus(ScriptsGrid.STATUS_PROCESSING);
					recordTracker.setRemarks("Executing Batch");
					recordTracker.setStartTime(System.currentTimeMillis());
				}
				else {
					// This record is being executed by another batch in parallel so we need
					// to wait until that batch is completed, otherwise it will result in errors when the record
					// is being created in the first batch and the second batch tries to create it as well!!!
					BatchInjectionTracker batchInjectionTrackerOther=((SeleniumWorker)worker).getScriptManager().getBatchIdToBatchInjectionTrackerMap().get(recordBatchId);
					int refreshTimeCounter=30;
					int counter=0;
					worker.println("recordTracker: "+recordTracker.getGridIndex()+" batchInjectionTrackerOther.isCompleted(): "+batchInjectionTrackerOther.isCompleted());
					while (! batchInjectionTrackerOther.isCompleted() ) {
						if (worker.isStopped()) {
							break;
						}
						worker.println("recordTracker: "+recordTracker.getGridIndex()+" counter:"+counter+" batchInjectionTrackerOther.isCompleted(): "+batchInjectionTrackerOther.isCompleted());
						recordTracker.setRemarks("This record is blocking batch # "+batchInjectionTracker.getBatchId()+" until its processing completes.");
						
						counter++;
						if ( counter % refreshTimeCounter ==0) {
							((SeleniumWorker)worker).getWebDriver().navigate().refresh(); // to avoid the 5 mins timeout from Oracle
						}
						Thread.sleep(1000);
					}
				}
			}

			if (worker.isStopped()) {
				recordTracker.setStatus(ScriptsGrid.STATUS_FAILED);
				recordTracker.setRemarks("Manual Stop!");
				break;
			}

			try{
				XmlObject[] xmlObjects=repeatType.selectPath("*");
				List<XmlObject> xmlObjectsSubList=Arrays.asList(xmlObjects);
				worker.println("recordTracker: "+recordTracker.getGridIndex()+" before processNavigationXMLObjects");
				worker.processNavigationXMLObjects(xmlObjectsSubList);
				worker.println("recordTracker: "+recordTracker.getGridIndex()+" after processNavigationXMLObjects");

				if (worker instanceof FusionWebServiceWorker) {
					recordTracker.setStatus(ScriptsGrid.STATUS_SUCCESS);
					recordTracker.setRemarks("");
					recordTracker.updateRecordExecutionTime();
					recordTracker.setIsSelected(false);
				}
				else {
					if (isRecordBelongToThisBatch) {
						recordTracker.setStatus(ScriptsGrid.STATUS_SUCCESS);
						recordTracker.setRemarks("");
						recordTracker.updateRecordExecutionTime();
						recordTracker.setIsSelected(false);
					}
				}
			}
			catch(Exception e) {
				worker.println("recordTracker: "+recordTracker.getGridIndex()+" ERROR");
				worker.printStackTrace(e);
				if (worker instanceof FusionWebServiceWorker) {
					recordTracker.setStatus(ScriptsGrid.STATUS_FAILED);
					recordTracker.setRemarks(e.getMessage());
					batchInjectionTracker.setError(true);
					recordTracker.updateRecordExecutionTime();
				}
				else {
					if (isRecordBelongToThisBatch) {
						recordTracker.setStatus(ScriptsGrid.STATUS_FAILED);
						if (isUseExceptionAsRemark) {
							recordTracker.setRemarks(e.getMessage());
						}
						else {
							recordTracker.setRemarks("Open the screenshot for details");
						}
						recordTracker.updateRecordExecutionTime();
					}
					if (isFailBatchOnFirstError) {
						throw e;
					}
				}
			}
		}
		if (worker instanceof SeleniumWorker) {
			((SeleniumWorker)worker).setCurrentWebElement(null);
		}
		worker.println("%%%%%%%%%%%%%%% END REPEAT %%%%%%%%%%%%%%%%%%%%%%%");
	}

}
