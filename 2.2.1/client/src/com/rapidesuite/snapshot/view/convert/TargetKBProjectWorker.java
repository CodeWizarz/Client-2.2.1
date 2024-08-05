package com.rapidesuite.snapshot.view.convert;

import java.io.File;
import java.util.Map;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;

public class TargetKBProjectWorker extends SnapshotSwingWorker {

	private ConvertFrame convertFrame;
	private File kbProjectFile;
	
	public TargetKBProjectWorker(ConvertFrame convertFrame,File kbProjectFile) {
		super(true);
		this.convertFrame=convertFrame;
		this.kbProjectFile=kbProjectFile;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();		
		return null;
	}
	
	public void processAction() {
		try{	
			/*
			File tempKBProjectExtractionFolder=new File(FileUtils.getTemporaryFolder(),kbProjectFile.getName());
			tempKBProjectExtractionFolder.mkdirs();
			String pathToFilter="knowledgebase\\inventory\\";
			Convert7zExtractor.unpackFrom7zArchiveFile(this,kbProjectFile,tempKBProjectExtractionFolder,"Target KB Project Extraction -",pathToFilter);
			
			File knowledgebaseFolder=new File(tempKBProjectExtractionFolder,"knowledgebase");
			File inventoryRootFolder=new File(knowledgebaseFolder,"inventory");
			*/
			
			Map<String, File> targetInventoryNameToInventoryFileMap=ModelUtils.getFileNameToFileMap(kbProjectFile,true);
			convertFrame.setTargetInventoryNameToInventoryFileMap(targetInventoryNameToInventoryFileMap);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}
	
	public ConvertFrame getConvertFrame() {
		return convertFrame;
	}

}