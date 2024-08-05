package com.rapidesuite.snapshot.view.convert;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.snapshot.view.UIConstants;

@SuppressWarnings("serial")
public class ConvertTargetGridTotalsPanel extends ConvertGridTotalsPanel {

	public ConvertTargetGridTotalsPanel(ConvertTargetPanel convertTargetPanel) {
		super(convertTargetPanel);
	}

	public void updateTotalLabels() {
		int sumTotalRecords=0;
		int sumTotalConfigurationRecords=0;
		int sumTotalPostConfigurationRecords=0;
		int sumTotalPostImplementationRecords=0;
		for(int row = 0;row < convertPanelGeneric.getTable().getRowCount();row++) {
			int modelRowIndex=convertPanelGeneric.getTable().convertRowIndexToModel(row);
			ConvertTargetGridRecordInformation convertTargetGridRecordInformation=((ConvertTargetPanel)convertPanelGeneric).getSelectedConvertTargetGridRecordInformationList()
					.get(modelRowIndex);
			sumTotalRecords=sumTotalRecords+convertTargetGridRecordInformation.getTotalRecords();
			sumTotalConfigurationRecords=sumTotalConfigurationRecords+convertTargetGridRecordInformation.getTotalRecordsConfiguration();
			sumTotalPostConfigurationRecords=sumTotalPostConfigurationRecords+convertTargetGridRecordInformation.getTotalRecordsPostConfiguration();
			sumTotalPostImplementationRecords=sumTotalPostImplementationRecords+convertTargetGridRecordInformation.getTotalRecordsPostImplementation();
        }
		String formattedNumber="";
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_RECORDS_CREATED+formattedNumber);
		
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalConfigurationRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalConfigurationRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_CONFIGURATION_RECORDS_CREATED+formattedNumber);
		
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalPostConfigurationRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalPostConfigurationRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_POST_CONFIGURATION_RECORDS_CREATED+formattedNumber);
		
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalPostImplementationRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalPostImplementationRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_POST_IMPLEMENTATION_RECORDS_CREATED+formattedNumber);
	}
	
}
