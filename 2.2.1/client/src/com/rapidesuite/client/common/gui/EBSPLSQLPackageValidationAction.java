/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/EBSPLSQLPackageValidationAction.java $:
 * $Id: EBSPLSQLPackageValidationAction.java 31694 2013-03-04 06:33:20Z john.snell $:
*/
package com.rapidesuite.client.common.gui;


public class EBSPLSQLPackageValidationAction extends PLSQLPackageValidationButtonAbstractAction
{
	
	private final EBSPropertiesValidationPanel ebsPropertiesValidationPanel;
	
	public EBSPLSQLPackageValidationAction(
			EBSPropertiesValidationPanel ebsPropertiesValidationPanel) {
		this.ebsPropertiesValidationPanel=ebsPropertiesValidationPanel;
	}

	public boolean beforeExecuteAction(
			PLSQLPackageValidationPanel plsqlPackageValidationPanel) {
		return true;
	}
	
	public void executeAction(PLSQLPackageValidationPanel plsqlPackageValidationPanel) {
	}

	public void afterExecuteAction(
			PLSQLPackageValidationPanel plsqlPackageValidationPanel) {
	}

}