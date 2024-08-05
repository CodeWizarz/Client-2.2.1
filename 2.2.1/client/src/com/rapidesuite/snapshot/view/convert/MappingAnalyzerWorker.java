package com.rapidesuite.snapshot.view.convert;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;

public class MappingAnalyzerWorker extends SnapshotSwingWorker {

	private ConvertFrame convertFrame;
	private boolean isSaveToExcel;
	private MappingAnalyzer mappingAnalyzer;
	
	public MappingAnalyzerWorker(ConvertFrame convertFrame,boolean isSaveToExcel) {
		super(true);
		this.convertFrame=convertFrame;
		this.isSaveToExcel=isSaveToExcel;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		processAction();		
		return null;
	}
	
	public void processAction() {
		try{
			mappingAnalyzer=new MappingAnalyzer(this,
					convertFrame.getConvertSourceGridRecordInformationList(),
					convertFrame.getSourceInventoryNameToInventoryFileMap(),
					convertFrame.getSourceInventoryNameToMappingFileMap());
			mappingAnalyzer.process(isSaveToExcel);
		}
		catch(Exception e) {
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to complete operation. Error: "+e.getMessage());
		}
	}
	
	public ConvertFrame getConvertFrame() {
		return convertFrame;
	}

	public MappingAnalyzer getMappingAnalyzer() {
		return mappingAnalyzer;
	}

}