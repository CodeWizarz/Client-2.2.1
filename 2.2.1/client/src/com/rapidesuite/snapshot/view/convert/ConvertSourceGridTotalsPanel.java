package com.rapidesuite.snapshot.view.convert;

import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.Utils;
import com.rapidesuite.snapshot.view.UIConstants;

@SuppressWarnings("serial")
public class ConvertSourceGridTotalsPanel extends ConvertGridTotalsPanel {

	public ConvertSourceGridTotalsPanel(ConvertSourcePanel convertSourcePanel) {
		super(convertSourcePanel);
	}

	public void updateTotalLabels() {
		int sumTotalRecords=0;
		for(int row = 0;row < convertPanelGeneric.getTable().getRowCount();row++) {
			int modelRowIndex=convertPanelGeneric.getTable().convertRowIndexToModel(row);
			ConvertSourceGridRecordInformation convertSourceGridRecordInformation=((ConvertSourcePanel)convertPanelGeneric).getSelectedConvertSourceGridRecordInformationList()
					.get(modelRowIndex);
			sumTotalRecords=sumTotalRecords+convertSourceGridRecordInformation.getTotalRecordsToConvert();
        }
		String formattedNumber="";
		try {
			formattedNumber = Utils.formatNumberWithComma(sumTotalRecords);
		} catch (Exception e) {
			FileUtils.printStackTrace(e);
		}
		getTotalRecordsCountLabel().setText(UIConstants.LABEL_GRID_TOTAL_RECORDS_TO_CONVERT+formattedNumber);
	}
	
}
