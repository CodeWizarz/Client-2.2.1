/**************************************************
 * $Revision: 48695 $:
 * $Author: olivier.deruelle $:
 * $Date: 2015-08-04 18:12:36 +0700 (Tue, 04 Aug 2015) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/DataExtractionOperatingUnitLevelJob.java $:
 * $Id: DataExtractionOperatingUnitLevelJob.java 48695 2015-08-04 11:12:36Z olivier.deruelle $:
 */
package com.rapidesuite.reverse;

import java.io.File;
import java.util.Map;

import com.rapidesuite.client.common.JobManager;
import com.rapidesuite.client.common.gui.SwiftGUIMain;
import com.rapidesuite.configurator.domain.Inventory;

public class DataExtractionOperatingUnitLevelJob extends DataExtractionJob
{

	private String businessGroupName;
	private Long businessGroupId;
	private String operatingUnitName;
	private Long operatingUnitId;

	public DataExtractionOperatingUnitLevelJob(JobManager executionJobManager,
			int taskId, Map<String, String> environmentProperties,
			File prefixOutputFolder, String inventoryFileName,
			Inventory inventory,
			String businessGroupName,
			Long businessGroupId,
			String operatingUnitName,
			Long operatingUnitId,
			String plsqlPackageName,
			SwiftGUIMain swiftGUIMain)
	{
		super(executionJobManager,taskId,environmentProperties,inventoryFileName,inventory,prefixOutputFolder,plsqlPackageName, swiftGUIMain);
		this.businessGroupName=businessGroupName;
		this.businessGroupId=businessGroupId;
		this.operatingUnitName=operatingUnitName;
		this.operatingUnitId=operatingUnitId;
	}

	public String getBusinessGroupName() {
		return businessGroupName;
	}

	public Long getBusinessGroupId() {
		return businessGroupId;
	}

	public String getOperatingUnitName() {
		return operatingUnitName;
	}

	public Long getOperatingUnitId() {
		return operatingUnitId;
	}

}