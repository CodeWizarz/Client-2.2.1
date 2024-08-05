/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/PLSQLPackageValidationButtonAbstractAction.java $:
 * $Id: PLSQLPackageValidationButtonAbstractAction.java 31694 2013-03-04 06:33:20Z john.snell $:
*/
package com.rapidesuite.client.common.gui;

public abstract class PLSQLPackageValidationButtonAbstractAction
{
	
    public abstract boolean beforeExecuteAction(final PLSQLPackageValidationPanel plsqlPackageValidationPanel);
    public abstract void executeAction(final PLSQLPackageValidationPanel plsqlPackageValidationPanel);
    public abstract void afterExecuteAction(final PLSQLPackageValidationPanel plsqlPackageValidationPanel);

}