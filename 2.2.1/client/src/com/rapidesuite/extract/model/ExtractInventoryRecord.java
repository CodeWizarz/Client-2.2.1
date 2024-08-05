package com.rapidesuite.extract.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rapidesuite.configurator.domain.Inventory;
import com.rapidesuite.extract.DataSet;
import com.rapidesuite.extract.ExtractUtils.SELECTION_LEVEL;

public class ExtractInventoryRecord {

	private String status;
	private String applicationName;
	private String formPath;
	private String inventoryName;
	private String remarks;
	private String executionTime;
	private long startTime;
	private long rawTimeInSecs;
	private int selectionGridIndex;
	private int resultGridIndex;
	private int totalRecords;
	private final Object lock = new Object();
	private Inventory inventory;
	private SELECTION_LEVEL formType;
	private StringBuffer whereClauseFilter;
	private String formName;
	private boolean hasLedgerId;
	private boolean hasCOAId;
	private boolean hasBusinessUnitId;
	private boolean hasLegalEntityId;
	private boolean hasOrganizationId;
	private boolean hasBusinessGroupId;
	private boolean hasEnterpriseId;
	private DataSet dataSet;
	private Set<String> functionNamesSet;
	private boolean hasDFFLogicToApply;
	private List<String> attributes;
	private boolean hasGDFLogicToApply;
	private boolean hasKFFLogicToApply;
	
	public ExtractInventoryRecord(String inventoryName) {
		this.inventoryName=inventoryName;
		functionNamesSet=new HashSet<String>();
		attributes=new ArrayList<String>();
	}

	public Long getStartTime() {
		return startTime;
	}
	
	public long getRawTimeInSecs() {
		return rawTimeInSecs;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public String getStatus() {
		synchronized(lock) {
			return status;
		}
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setStatus(String status) {
		synchronized(lock) {
			this.status=status;
		}
	}

	public void setRemarks(String remarks) {
		this.remarks=remarks;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}

	public String getInventoryName() {
		return inventoryName;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}
	
	public void setRawTimeInSecs(long rawTimeInSecs) {
		this.rawTimeInSecs=rawTimeInSecs;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getFormPath() {
		return formPath;
	}

	public void setFormPath(String formPath) {
		this.formPath = formPath;
	}

	public SELECTION_LEVEL getFormType() {
		if (formType!=null) {
			return formType;
		}
		
		if(hasOrganizationId) {
			formType = SELECTION_LEVEL.ORGANIZATION;
		} else if(hasBusinessUnitId) {
			formType = SELECTION_LEVEL.BUSINESS_UNIT;
		} else if(hasLegalEntityId) {
			formType = SELECTION_LEVEL.LEGAL_ENTITY;
		} else if(hasLedgerId) {
			formType = SELECTION_LEVEL.LEDGER;
		} else if(hasCOAId) {
			formType = SELECTION_LEVEL.COA;
		} else if(hasBusinessGroupId || hasEnterpriseId) {
			formType = SELECTION_LEVEL.ENTERPRISE_BUSINESS_GROUP;
		} else {
			formType = SELECTION_LEVEL.GLOBAL;
		}
		return formType;
	}	
		
	public void setWhereClauseFilter(StringBuffer whereClauseFilter) {
		this.whereClauseFilter=whereClauseFilter;
	}

	public StringBuffer getWhereClauseFilter() {
		return whereClauseFilter;
	}

	public String getFormName() {
		return formName;
	}
	
	public void setFormName(String formName) {
		this.formName=formName;
	}

	public boolean isHasLedgerId() {
		return hasLedgerId;
	}

	public void setHasLedgerId(boolean hasLedgerId) {
		this.hasLedgerId = hasLedgerId;
	}

	public boolean isHasCOAId() {
		return hasCOAId;
	}

	public void setHasCOAId(boolean hasCOAId) {
		this.hasCOAId = hasCOAId;
	}

	public boolean isHasBusinessUnitId() {
		return hasBusinessUnitId;
	}

	public void setHasBusinessUnitId(boolean hasBusinessUnitId) {
		this.hasBusinessUnitId = hasBusinessUnitId;
	}

	public boolean isHasLegalEntityId() {
		return hasLegalEntityId;
	}

	public void setHasLegalEntityId(boolean hasLegalEntityId) {
		this.hasLegalEntityId = hasLegalEntityId;
	}

	public boolean isHasOrganizationId() {
		return hasOrganizationId;
	}

	public void setHasOrganizationId(boolean hasOrganizationId) {
		this.hasOrganizationId = hasOrganizationId;
	}

	public boolean isHasBusinessGroupId() {
		return hasBusinessGroupId;
	}

	public void setHasBusinessGroupId(boolean hasBusinessGroupId) {
		this.hasBusinessGroupId = hasBusinessGroupId;
	}

	public boolean isHasEnterpriseId() {
		return hasEnterpriseId;
	}

	public void setHasEnterpriseId(boolean hasEnterpriseId) {
		this.hasEnterpriseId = hasEnterpriseId;
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	public int getSelectionGridIndex() {
		return selectionGridIndex;
	}

	public int getResultGridIndex() {
		return resultGridIndex;
	}

	public void setSelectionGridIndex(int selectionGridIndex) {
		this.selectionGridIndex = selectionGridIndex;
	}

	public void setResultGridIndex(int resultGridIndex) {
		this.resultGridIndex = resultGridIndex;
	}

	public Set<String> getFunctionNamesSet() {
		return functionNamesSet;
	}

	public boolean hasDFFLogicToApply() {
		return hasDFFLogicToApply;
	}
	
	public boolean hasGDFLogicToApply() {
		return hasGDFLogicToApply;
	}
	
	public boolean hasKFFLogicToApply() {
		return hasKFFLogicToApply;
	}

	public void setHasDFFLogicToApply(boolean hasDFFLogicToApply) {
		this.hasDFFLogicToApply = hasDFFLogicToApply;
	}

	public void setHasGDFLogicToApply(boolean hasGDFLogicToApply) {
		this.hasGDFLogicToApply = hasGDFLogicToApply;
	}
	
	public void setHasKFFLogicToApply(boolean hasKFFLogicToApply) {
		this.hasKFFLogicToApply = hasKFFLogicToApply;
	}
	
	public void setAttributes(List<String> attributes) {
		this.attributes=attributes;
	}

	public List<String> getAttributes() {
		return attributes;
	}
	
}
