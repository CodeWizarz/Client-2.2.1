package com.rapidesuite.extract.model;

import java.io.Serializable;

import com.rapidesuite.extract.ExtractUtils.SELECTION_LEVEL;

@SuppressWarnings("serial")
public class ExtractGenericDataRow implements Serializable{

	private String rscLastUpdatedBy;
	private String rscLastUpdateDate;
	private String rscCreatedBy; 
	private String rscCreationDate;

	private long ledgerId;
	private long businessUnitId;
	private long legalEntityId;
	private long coaId;
	private long inventoryOrganizationId;
	private long businessGroupId;
	private long enterpriseId;
	
	public String getRscLastUpdatedBy() {
		return rscLastUpdatedBy;
	}
	public void setRscLastUpdatedBy(String rscLastUpdatedBy) {
		this.rscLastUpdatedBy = rscLastUpdatedBy;
	}
	public String getRscLastUpdateDate() {
		return rscLastUpdateDate;
	}
	public void setRscLastUpdateDate(String rscLastUpdateDate) {
		this.rscLastUpdateDate = rscLastUpdateDate;
	}
	public String getRscCreatedBy() {
		return rscCreatedBy;
	}
	public void setRscCreatedBy(String rscCreatedBy) {
		this.rscCreatedBy = rscCreatedBy;
	}
	public String getRscCreationDate() {
		return rscCreationDate;
	}
	public void setRscCreationDate(String rscCreationDate) {
		this.rscCreationDate = rscCreationDate;
	}
	public long getLedgerId() {
		return ledgerId;
	}
	public void setLedgerId(long ledgerId) {
		this.ledgerId = ledgerId;
	}
	public long getBusinessUnitId() {
		return businessUnitId;
	}
	public void setBusinessUnitId(long businessUnitId) {
		this.businessUnitId = businessUnitId;
	}
	public long getLegalEntityId() {
		return legalEntityId;
	}
	public void setLegalEntityId(long legalEntityId) {
		this.legalEntityId = legalEntityId;
	}
	public long getInventoryOrganizationId() {
		return inventoryOrganizationId;
	}
	public void setInventoryOrganizationId(long inventoryOrganizationId) {
		this.inventoryOrganizationId = inventoryOrganizationId;
	}
	public long getBusinessGroupId() {
		return businessGroupId;
	}
	public void setBusinessGroupId(long businessGroupId) {
		this.businessGroupId = businessGroupId;
	}
	public long getEnterpriseId() {
		return enterpriseId;
	}
	public void setEnterpriseId(long enterpriseId) {
		this.enterpriseId = enterpriseId;
	}
	public long getCoaId() {
		return coaId;
	}
	public void setCoaId(long coaId) {
		this.coaId = coaId;
	}
	
	/**
	 * returns the appropriate id based on the level specified.
	 * 
	 * @param supportSelectionLevel
	 * @return the id attributed to the level specified
	 */
	public long getSupportingId(SELECTION_LEVEL supportSelectionLevel) {
		switch(supportSelectionLevel) {
		case ENTERPRISE_BUSINESS_GROUP:
			return this.getBusinessGroupId() <= 0 ? this.getEnterpriseId() : this.getBusinessGroupId();
		case COA:
			return this.getCoaId();
		case LEDGER:
			return this.getLedgerId();
		case LEGAL_ENTITY:
			return this.getLegalEntityId();
		case BUSINESS_UNIT:
			return this.getBusinessUnitId();
		case ORGANIZATION:
			return this.getInventoryOrganizationId();
		default:
			return 0;
		}
	}
	
}
