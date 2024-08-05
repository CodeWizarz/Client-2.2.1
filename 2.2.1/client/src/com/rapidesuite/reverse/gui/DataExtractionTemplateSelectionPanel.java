/**************************************************
 * $Revision: 40330 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-04-01 16:20:01 +0700 (Tue, 01 Apr 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/gui/DataExtractionTemplateSelectionPanel.java $:
 * $Id: DataExtractionTemplateSelectionPanel.java 40330 2014-04-01 09:20:01Z fajrian.yunus $:
 */

package com.rapidesuite.reverse.gui;

import java.io.File;
import java.util.List;

import javax.swing.JLabel;

import com.rapidesuite.client.common.gui.TemplateSelectionPanel;
import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.TemplateUtils;
import com.rapidesuite.reverse.ReverseMain;
import com.rapidesuite.reverse.utils.DataExtractionDatabaseUtils.OracleUserRetrievalExecutionMonitor;
import com.rapidesuite.reverse.utils.DataExtractionTemplateUtils;

@SuppressWarnings("serial")
public class DataExtractionTemplateSelectionPanel extends TemplateSelectionPanel
{
	
	public DataExtractionTemplateSelectionPanel(DataExtractionPanel dataExtractionPanel){
		super(dataExtractionPanel);
	}
	
	public List<File> getTemplates(){
		return TemplateUtils.getTemplates(getTemplateFolder());	
	}

	public void saveSession(String templateName) throws Exception{
		DataExtractionTemplateUtils.saveSession( (DataExtractionPanel)executionPanel,getTemplateFolder(),templateName);	
	}
			
	public void restoreSession(final String templateName, final OracleUserRetrievalExecutionMonitor cancelFlag, final JLabel progressLabel) throws Exception{
		DataExtractionTemplateUtils.restoreSession((DataExtractionPanel)executionPanel,
				getTemplateFolder(),
				templateName, cancelFlag, progressLabel);
	}
	
	@Override
	protected File getTemplateFolder() {
		File output = Config.getReverseTemplateFolder()==null?null:new File(Config.getReverseTemplateFolder());
		if (output == null) {
			output = new File(new File(Config.getReverseSessionsFolder(), DataExtractionTemplateUtils.SESSION_EXTRACTION_FOLDER_NAME),
					((ReverseMain)executionPanel.getSwiftGUIMain()).getDataExtractionPackagesSelectionPanel().getInventoriesPackageFile().getName());
		}
		
		return output;		
	}
	
}