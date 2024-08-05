/**************************************************
 * $Revision: 48695 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-08-04 18:12:36 +0700 (Tue, 04 Aug 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/DataExtractionBusinessGroupLevelJob.java $:
 * $Id: DataExtractionBusinessGroupLevelJob.java 48695 2015-08-04 11:12:36Z olivier.deruelle $:
 */
package com.rapidesuite.reverse;

import java.io.File;
import java.util.Map;

import com.rapidesuite.client.common.JobManager;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.configurator.domain.Inventory;

public class DataExtractionBusinessGroupLevelJob extends DataExtractionJob
{

	private String businessGroupName;
	private Long businessGroupId;

	public DataExtractionBusinessGroupLevelJob(JobManager executionJobManager,
			int taskId,
			Map<String, String> environmentProperties,
			File prefixOutputFolder,
			String inventoryFileName,
			Inventory inventory,
			String businessGroupName,
			Long businessGroupId,
			String plsqlPackageName,
            SwiftGUIMain swiftGUIMain) {
		super(executionJobManager,taskId,environmentProperties,inventoryFileName,inventory,prefixOutputFolder,plsqlPackageName,swiftGUIMain);
		this.businessGroupName=businessGroupName;
		this.businessGroupId=businessGroupId;
	}

	public String getBusinessGroupName() {
		return businessGroupName;
	}

	public Long getBusinessGroupId() {
		return businessGroupId;
	}

}