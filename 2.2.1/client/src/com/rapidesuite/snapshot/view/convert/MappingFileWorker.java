package com.rapidesuite.snapshot.view.convert;

import java.io.File;
import java.util.Map;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;

public class MappingFileWorker extends SnapshotSwingWorker {

	private ConvertFrame convertFrame;
	private File mappingFile;
	
	public MappingFileWorker(ConvertFrame convertFrame,File mappingFile) {
		super(true);
		this.convertFrame=convertFrame;
		this.mappingFile=mappingFile;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();		
		return null;
	}
	
	public void processAction() {
		try{
			/*
			File tempMappingExtractionFolder=new File(FileUtils.getTemporaryFolder(),mappingFile.getName());
			tempMappingExtractionFolder.mkdirs();
			String pathToFilter=null;
			Convert7zExtractor.unpackFrom7zArchiveFile(this,mappingFile,tempMappingExtractionFolder,"Target Mapping File Extraction -",pathToFilter);
			*/
			
			Map<String, File> sourceInventoryNameToMappingFileMap=ModelUtils.getFileNameToFileMap(mappingFile,true);
			convertFrame.setSourceInventoryNameToMappingFileMap(sourceInventoryNameToMappingFileMap);			
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