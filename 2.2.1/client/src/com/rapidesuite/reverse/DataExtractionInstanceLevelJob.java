/**************************************************
 * $Revision: 32247 $:
 * $Author: john.snell $:
 * $Date: 2013-04-19 16:25:44 +0700 (Fri, 19 Apr 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/DataExtractionInstanceLevelJob.java $:
 * $Id: DataExtractionInstanceLevelJob.java 32247 2013-04-19 09:25:44Z john.snell $:
 */
package com.rapidesuite.reverse;

import java.io.File;
import java.util.Map;

import com.rapidesuite.client.common.JobManager;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.configurator.domain.Inventory;

public class DataExtractionInstanceLevelJob extends DataExtractionJob
{

	public DataExtractionInstanceLevelJob(
			JobManager executionJobManager,
			int taskId,
			Map<String, String> environmentProperties,
			File prefixOutputFolder,
			String inventoryFileName,
			Inventory inventory,
			String plsqlPackageName,
            SwiftGUIMain swiftGUIMain) {
		super(executionJobManager,taskId,environmentProperties,inventoryFileName,inventory,prefixOutputFolder,plsqlPackageName,swiftGUIMain);
	}


}