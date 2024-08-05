package com.rapidesuite.snapshot.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.rapidesuite.snapshot.model.BusinessGroup;
import com.rapidesuite.snapshot.model.FormInformation;
import com.rapidesuite.snapshot.model.InventoryOrganization;
import com.rapidesuite.snapshot.model.Ledger;
import com.rapidesuite.snapshot.model.LegalEntity;
import com.rapidesuite.snapshot.model.ModelUtils;
import com.rapidesuite.snapshot.model.OperatingUnit;
import com.rapidesuite.snapshot.model.ModelUtils.SELECTION_LEVEL;

public abstract class ViewerAndComparisionAbstractController extends GenericController {
	
	Map<Long, List<InventoryOrganization>> operatingUnitIdToInventoryOrganizationsList = null;
	Map<Long, List<Ledger>> operatingUnitToLedgersMap = null;
	Map<Long, List<LegalEntity>> operatingUnitToLegalEntityMap = null;
	Map<Long, List<BusinessGroup>> operatingUnitToBusinessGroupMap = null;
	
	protected void initMaps(Connection connection) throws Exception {
		operatingUnitIdToInventoryOrganizationsList = ModelUtils.getOperatingUnitIdToInventoryOrganizationsList(connection);
		operatingUnitToLedgersMap = ModelUtils.getOperatingUnitToLedgersMap(connection);
		operatingUnitToLegalEntityMap = ModelUtils.getOperatingUnitIdToLegalEntityList(connection);
		operatingUnitToBusinessGroupMap = ModelUtils.getOperatingUnitIdToBusinessGroupList(connection);
	}
	
	private void appendIfNotAlreadyPresent(StringBuffer toReturn, String append) {
		if(toReturn.length() > 0 && !toReturn.toString().endsWith(append)) toReturn.append(append);
	}
	
	public StringBuffer getOperatingUnitWhereClauseFiltering(List<OperatingUnit> selectedOperatingUnits, FormInformation formInformation,
			SELECTION_LEVEL selectionLevel) throws Exception {
		StringBuffer toReturn = new StringBuffer("");
		
		if(selectionLevel == null) {
			selectionLevel = SELECTION_LEVEL.GLOBAL;
		}
		
		toReturn.append(getChartOfAccountsWhereClauseFiltering(selectedOperatingUnits));
		
		appendIfNotAlreadyPresent(toReturn, " or ");
		toReturn.append(" ( ");
		
		switch(selectionLevel) {
		case INVENTORY_ORGANIZATION:
			if(selectedOperatingUnits.isEmpty()) {
				toReturn.append(" rsc_inv_org_id IS NOT NULL ");
			} else {
				toReturn.append(getInventoryOrgsWhereClauseFiltering(selectedOperatingUnits));
			}
			break;
		case OPERATING_UNIT:
			if(selectedOperatingUnits.isEmpty()) {
				toReturn.append(" rsc_inv_org_id IS NOT NULL OR rsc_ou_id IS NOT NULL ");
			} else {
				toReturn.append(getInventoryOrgsWhereClauseFiltering(selectedOperatingUnits));
				appendIfNotAlreadyPresent(toReturn, " or ");
				toReturn.append(getOperatingUnitWhereClauseFiltering(selectedOperatingUnits));
			}
			break;
		case LEDGER:
			if(selectedOperatingUnits.isEmpty()) {
				toReturn.append(" rsc_inv_org_id IS NOT NULL OR rsc_ou_id IS NOT NULL OR rsc_ledger_id IS NOT NULL ");
			} else {
				toReturn.append(getInventoryOrgsWhereClauseFiltering(selectedOperatingUnits));
				appendIfNotAlreadyPresent(toReturn, " or ");
				toReturn.append(getOperatingUnitWhereClauseFiltering(selectedOperatingUnits));
				appendIfNotAlreadyPresent(toReturn, " or ");
				toReturn.append(getLedgersWhereClauseFiltering(selectedOperatingUnits, formInformation));
			}
			break;
		case BUSINESS_GROUP:
		case GLOBAL:
			if(selectedOperatingUnits.isEmpty()) {
				toReturn.append(" rsc_inv_org_id IS NOT NULL OR rsc_ou_id IS NOT NULL OR rsc_ledger_id IS NOT NULL OR rsc_bg_id IS NOT NULL ");
			} else {
				toReturn.append(getInventoryOrgsWhereClauseFiltering(selectedOperatingUnits));
				appendIfNotAlreadyPresent(toReturn, " or ");
				toReturn.append(getOperatingUnitWhereClauseFiltering(selectedOperatingUnits));
				appendIfNotAlreadyPresent(toReturn, " or ");
				toReturn.append(getLedgersWhereClauseFiltering(selectedOperatingUnits, formInformation));
				appendIfNotAlreadyPresent(toReturn, " or ");
				toReturn.append(getBusinessGroupsWhereClauseFiltering(selectedOperatingUnits));
			}
			break;
		}
		
		toReturn.append(" ) ");

		if(selectionLevel == SELECTION_LEVEL.GLOBAL) {
			appendIfNotAlreadyPresent(toReturn, " or ");
			toReturn.append(" ( rsc_inv_org_id IS NULL AND rsc_ou_id IS NULL AND rsc_ledger_id IS NULL AND rsc_bg_id IS NULL ) ");
		}

		return toReturn;
	}
	
	private StringBuffer getOperatingUnitWhereClauseFiltering(List<OperatingUnit> selectedOperatingUnits) {
		StringBuffer toReturn = new StringBuffer("");
		
		if(selectedOperatingUnits.isEmpty()) {
			return toReturn;
		}
		
		toReturn.append(" rsc_ou_id in (");
		for (int i = 0; i < selectedOperatingUnits.size(); i++) {
			OperatingUnit operatingUnit = selectedOperatingUnits.get(i);
			toReturn.append(operatingUnit.getId());
			if ((i + 1) < selectedOperatingUnits.size()) {
				toReturn.append(",");
			}
		}
		toReturn.append(")");

		return toReturn;
	}

	private StringBuffer getInventoryOrgsWhereClauseFiltering(List<OperatingUnit> selectedOperatingUnits) {
		StringBuffer toReturn = new StringBuffer("");
		

		List<InventoryOrganization> list = new ArrayList<InventoryOrganization>();
		
		if(selectedOperatingUnits.isEmpty()) {
			for (Entry<Long, List<InventoryOrganization>> entrySet : operatingUnitIdToInventoryOrganizationsList.entrySet()) {
				list.addAll(entrySet.getValue());
			}
		} else {
			for (int i = 0; i < selectedOperatingUnits.size(); i++) {
				OperatingUnit operatingUnit = selectedOperatingUnits.get(i);
				List<InventoryOrganization> inventoryOrganizationList = operatingUnitIdToInventoryOrganizationsList
						.get(operatingUnit.getId());
				if (inventoryOrganizationList != null) {
					list.addAll(inventoryOrganizationList);
				}
			}
			
			List<InventoryOrganization> inventoryOrganizationList = operatingUnitIdToInventoryOrganizationsList
					.get(new Long(-1));
			if (inventoryOrganizationList != null) {
				list.addAll(inventoryOrganizationList);
			}
		}
		
		if (!list.isEmpty()) {
			toReturn.append(" rsc_inv_org_id in (");
			toReturn.append(getInventoryOrgIds(list));
			toReturn.append(")");
		}
		return toReturn;
	}

	private StringBuffer getBusinessGroupsWhereClauseFiltering(List<OperatingUnit> selectedOperatingUnits) {
		StringBuffer toReturn = new StringBuffer("");
		
		List<BusinessGroup> list = new ArrayList<BusinessGroup>();
		
		if(selectedOperatingUnits.isEmpty()) {
			for (Entry<Long, List<BusinessGroup>> entrySet : operatingUnitToBusinessGroupMap.entrySet()) {
				list.addAll(entrySet.getValue());
			}
		} else {
			for (int i = 0; i < selectedOperatingUnits.size(); i++) {
				OperatingUnit operatingUnit = selectedOperatingUnits.get(i);
				List<BusinessGroup> BusinessGroupList = operatingUnitToBusinessGroupMap
						.get(operatingUnit.getId());
				if (BusinessGroupList != null) {
					list.addAll(BusinessGroupList);
				}
			}
			
			List<BusinessGroup> BusinessGroupList = operatingUnitToBusinessGroupMap
					.get(new Long(-1));
			if (BusinessGroupList != null) {
				list.addAll(BusinessGroupList);
			}
		}
		
		if (!list.isEmpty()) {
			toReturn.append(" rsc_bg_id in (");
			toReturn.append(getBusinessGroupIds(list));
			toReturn.append(")");
		}
		return toReturn;
	}

	private StringBuffer getLedgersWhereClauseFiltering(List<OperatingUnit> selectedOperatingUnits, FormInformation formInformation) throws Exception {
		StringBuffer toReturn = new StringBuffer("");

		List<Ledger> list = new ArrayList<Ledger>();
		
		if(selectedOperatingUnits.isEmpty()) {
			for (Entry<Long, List<Ledger>> entrySet : operatingUnitToLedgersMap.entrySet()) {
				list.addAll(entrySet.getValue());
			}
		} else {
			for (int i = 0; i < selectedOperatingUnits.size(); i++) {
				OperatingUnit operatingUnit = selectedOperatingUnits.get(i);
				List<Ledger> ledgerList = operatingUnitToLedgersMap.get(operatingUnit.getId());
				if (ledgerList != null) {
					list.addAll(ledgerList);
				}
			}
	
			List<Ledger> ledgerList = operatingUnitToLedgersMap.get(new Long(-1));
			if (ledgerList != null) {
				list.addAll(ledgerList);
			}
		}
		
		if (!list.isEmpty()) {
			toReturn.append(" ( rsc_ledger_id in (");
			toReturn.append(getLedgerIds(list));
			toReturn.append(")");
			
			if(formInformation.getLegalEntityFilteringColumnNumber() > -1) {
				List<LegalEntity> leList = new ArrayList<LegalEntity>();
				for (int i = 0; i < selectedOperatingUnits.size(); i++) {
					OperatingUnit operatingUnit = selectedOperatingUnits.get(i);
					List<LegalEntity> legalEntityList = operatingUnitToLegalEntityMap.get(operatingUnit.getId());
					if (legalEntityList != null) {
						leList.addAll(legalEntityList);
					}
				}
				
				toReturn.append(" and C" + formInformation.getLegalEntityFilteringColumnNumber() + " in (");
				toReturn.append(getLENames(leList));
				toReturn.append(")");
			}
			toReturn.append(")");
		}
		
		return toReturn;
	}

	private static StringBuffer getChartOfAccountsWhereClauseFiltering(List<OperatingUnit> selectedOperatingUnits) {
		StringBuffer toReturn = new StringBuffer("");
		
		if(selectedOperatingUnits.isEmpty()) {
			return toReturn;
		}
		
		Set<Integer> list = new HashSet<Integer>();
		for (int i = 0; i < selectedOperatingUnits.size(); i++) {
			OperatingUnit operatingUnit = selectedOperatingUnits.get(i);
			int coaId = operatingUnit.getCoaId();
			if (coaId != 0) {
				list.add(coaId);
			}
		}
		if (!list.isEmpty()) {
			toReturn.append(" rsc_coa_id in (");
			Iterator<Integer> iterator = list.iterator();
			while (iterator.hasNext()) {
				int coaId = iterator.next();
				toReturn.append(coaId);
				if (iterator.hasNext()) {
					toReturn.append(",");
				}
			}
			toReturn.append(")");
		}
		return toReturn;
	}

	private static StringBuffer getLedgerIds(List<Ledger> list) {
		StringBuffer toReturn = new StringBuffer("");
		for (int i = 0; i < list.size(); i++) {
			Ledger ledger = list.get(i);
			toReturn.append(ledger.getId());
			if ((i + 1) < list.size()) {
				toReturn.append(",");
			}
		}
		return toReturn;
	}

	private static StringBuffer getLENames(List<LegalEntity> list) {
		StringBuffer toReturn = new StringBuffer("");
		for (int i = 0; i < list.size(); i++) {
			LegalEntity le = list.get(i);
			toReturn.append("'" + le.getName() + "'");
			if ((i + 1) < list.size()) {
				toReturn.append(",");
			}
		}
		return toReturn;
	}

	private static StringBuffer getInventoryOrgIds(List<InventoryOrganization> inventoryOrganizationList) {
		StringBuffer toReturn = new StringBuffer("");
		for (int i = 0; i < inventoryOrganizationList.size(); i++) {
			InventoryOrganization inventoryOrganization = inventoryOrganizationList.get(i);
			toReturn.append(inventoryOrganization.getId());
			if ((i + 1) < inventoryOrganizationList.size()) {
				toReturn.append(",");
			}
		}
		return toReturn;
	}

	private static StringBuffer getBusinessGroupIds(List<BusinessGroup> businessGroupList) {
		StringBuffer toReturn = new StringBuffer("");
		for (int i = 0; i < businessGroupList.size(); i++) {
			BusinessGroup businessGroup = businessGroupList.get(i);
			toReturn.append(businessGroup.getId());
			if ((i + 1) < businessGroupList.size()) {
				toReturn.append(",");
			}
		}
		return toReturn;
	}

	public StringBuffer getOperatingUnitWhereClauseFiltering(StringBuffer operatingUnitWhereClauseFiltering,boolean isBGIncluded) throws Exception {
		StringBuffer toReturn=new StringBuffer("");
		if(operatingUnitWhereClauseFiltering.length() <= 0) {
			return toReturn;
		}
		toReturn.append(" and ( ");
		toReturn.append(operatingUnitWhereClauseFiltering);
		if (isBGIncluded) {
			toReturn.append(" or (rsc_bg_id=rsc_ou_id and rsc_bg_id<>0) ");
		}
		toReturn.append(")");
		
		return toReturn;
	}

}
