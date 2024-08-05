package com.rapidesuite.snapshot.model;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.rapidesuite.snapshot.view.UIConstants;

public class FormInformation {

	private int functionId;
	private String formName;
	private Set<String> applicationNameSet;
	private Set<String> fullPathSet;
	
	private boolean hasLedgerId;
	private boolean hasCOAId;
	private boolean hasOperatingUnitId;
	private boolean hasInventoryOrganizationId;
	private boolean hasBusinessGroupId;
	private String formattedApplicationNames;
	private String formattedFormPaths;
	
	private boolean disableOUAndLevelFiltering = false;
	private int legalEntityFilteringColumnNumber = -1;
	
	public FormInformation() {
		applicationNameSet=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		fullPathSet=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	}
	
	public int getFunctionId() {
		return functionId;
	}
	public void setFunctionId(int functionId) {
		this.functionId = functionId;
	}
	public String getFormName() {
		if (formName==null || formName.isEmpty()) {
			return UIConstants.UI_NA;
		}
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public Set<String> getApplicationNameSet() {
		return applicationNameSet;
	}

	public Set<String> getFullPathSet() {
		return fullPathSet;
	}
	public boolean hasLedgerId() {
		return hasLedgerId;
	}
	public void setHasLedgerId(boolean hasLedgerId) {
		this.hasLedgerId = hasLedgerId;
	}
	public boolean hasCOAId() {
		return hasCOAId;
	}
	public void setHasCOAId(boolean hasCOAId) {
		this.hasCOAId = hasCOAId;
	}
	public boolean hasOperatingUnitId() {
		return hasOperatingUnitId;
	}
	public void setHasOperatingUnitId(boolean hasOperatingUnitId) {
		this.hasOperatingUnitId = hasOperatingUnitId;
	}
	public boolean hasInventoryOrganizationId() {
		return hasInventoryOrganizationId;
	}
	public void setHasInventoryOrganizationId(boolean hasInventoryOrganizationId) {
		this.hasInventoryOrganizationId = hasInventoryOrganizationId;
	}
	public boolean hasBusinessGroupId() {
		return hasBusinessGroupId;
	}
	public void setHasBusinessGroupId(boolean hasBusinessGroupId) {
		this.hasBusinessGroupId = hasBusinessGroupId;
	}
	public String getFormType() {
		if (hasBusinessGroupId ||
			hasLedgerId ||
			hasCOAId ||
			hasOperatingUnitId ||
			hasInventoryOrganizationId	
		) {
			return UIConstants.UI_FORM_TYPE_OU;
		}
		return UIConstants.UI_FORM_TYPE_GLOBAL;
	}
	
	public String getFormattedApplicationNames() {
		if (formattedApplicationNames!=null) {
			return formattedApplicationNames;
		}
		StringBuffer sb=new StringBuffer("");
		Iterator<String> iterator=applicationNameSet.iterator();
		while (iterator.hasNext()) {
			String val=iterator.next();
			
			sb.append(val);
			if (iterator.hasNext()) {
				sb.append(" | \n");
			}
		}
		if (sb.toString().isEmpty()) {
			formattedApplicationNames=UIConstants.UI_NA;
		}
		else {
			formattedApplicationNames=sb.toString();
		}
		return formattedApplicationNames;
	}
	
	public String getFormattedFormPaths() {
		if (formattedFormPaths!=null) {
			return formattedFormPaths;
		}
		StringBuffer sb=new StringBuffer("");
		Iterator<String> iterator=fullPathSet.iterator();
		while (iterator.hasNext()) {
			String val=iterator.next();

			sb.append(val);
			if (iterator.hasNext()) {
				sb.append(" | \n");
			}
		}
		if (sb.toString().isEmpty()) {
			formattedFormPaths=UIConstants.UI_NA;
		}
		else {
			formattedFormPaths=sb.toString();
		}
		return formattedFormPaths;
	}
	public int getLegalEntityFilteringColumnNumber() {
		return legalEntityFilteringColumnNumber;
	}
	public void setLegalEntityFilteringColumnNumber(int legalEntityFilteringColumnNumber) {
		this.legalEntityFilteringColumnNumber=legalEntityFilteringColumnNumber;
	}
	
	public boolean isDisableOUAndLevelFiltering() {
		return disableOUAndLevelFiltering;
	}

	public void setDisableOUAndLevelFiltering(boolean disableOUAndLevelFiltering) {
		this.disableOUAndLevelFiltering = disableOUAndLevelFiltering;
	}
	
}
