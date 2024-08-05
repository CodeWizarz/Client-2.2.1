package com.rapidesuite.snapshot.model;

import org.apache.commons.lang3.StringUtils;

public class DataRow {

	private int snapshotId;
	private String[] dataValues;
	private int rscLastUpdatedBy;
	private String rscLastUpdateDate;
	private int rscCreatedBy; 
	private String rscCreationDate;
	private String rscCreatedByName;
	private String rscLastUpdatedByName;
	private String label;
	private String[] extraColumns;
	private String rscCOAId;
	private String rscInvOrgId;
	private String rscOUid;
	private String rscLedgerId;
	private String rscBGId;	
	public DataRow() {
	}

	public int getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(int snapshotId) {
		this.snapshotId = snapshotId;
	}

	public String[] getDataValues() {
		return dataValues;
	}

	public void setDataValues(String[] dataValues) {
		this.dataValues = dataValues;
	}

	public int getRscLastUpdatedBy() {
		return rscLastUpdatedBy;
	}

	public void setRscLastUpdatedBy(int rscLastUpdatedBy) {
		this.rscLastUpdatedBy = rscLastUpdatedBy;
	}

	public String getRscLastUpdateDate() {
		return rscLastUpdateDate;
	}

	public void setRscLastUpdateDate(String rscLastUpdateDate) {
		this.rscLastUpdateDate = rscLastUpdateDate;
	}

	public int getRscCreatedBy() {
		return rscCreatedBy;
	}

	public void setRscCreatedBy(int rscCreatedBy) {
		this.rscCreatedBy = rscCreatedBy;
	}

	public String getRscCreationDate() {
		return rscCreationDate;
	}

	public void setRscCreationDate(String rscCreationDate) {
		this.rscCreationDate = rscCreationDate;
	}

	public void setRscCreatedByName(String rscCreatedByName) {
		this.rscCreatedByName=rscCreatedByName;
	}

	public void setRscLastUpdatedByName(String rscLastUpdatedByName) {
		this.rscLastUpdatedByName=rscLastUpdatedByName;
	}

	public String getRscCreatedByName() {
		return rscCreatedByName;
	}

	public String getRscLastUpdatedByName() {
		return rscLastUpdatedByName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String[] getExtraColumns() {
		return extraColumns;
	}

	public void setExtraColumns(String[] extraColumns) {
		this.extraColumns = extraColumns;
	}
	
	public String getKey() {
		String joined= StringUtils.join(dataValues, "###");
		return joined;
	}

	public String getRscCOAId() {
		return rscCOAId;
	}

	public void setRscCOAId(String rscCOAId) {
		this.rscCOAId = rscCOAId;
	}

	public String getRscInvOrgId() {
		return rscInvOrgId;
	}

	public void setRscInvOrgId(String rscInvOrgId) {
		this.rscInvOrgId = rscInvOrgId;
	}

	public String getRscOUid() {
		return rscOUid;
	}

	public void setRscOUid(String rscOUid) {
		this.rscOUid = rscOUid;
	}

	public String getRscLedgerId() {
		return rscLedgerId;
	}

	public void setRscLedgerId(String rscLedgerId) {
		this.rscLedgerId = rscLedgerId;
	}

	public String getRscBGId() {
		return rscBGId;
	}

	public void setRscBGId(String rscBGId) {
		this.rscBGId = rscBGId;
	}
	
	
}
