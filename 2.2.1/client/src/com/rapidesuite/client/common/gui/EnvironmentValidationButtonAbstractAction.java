/**************************************************
 * $Revision: 35838 $:
 * $Author: fajrian.yunus $:
 * $Date: 2013-09-03 13:48:58 +0700 (Tue, 03 Sep 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/EnvironmentValidationButtonAbstractAction.java $:
 * $Id: EnvironmentValidationButtonAbstractAction.java 35838 2013-09-03 06:48:58Z fajrian.yunus $:
*/
package com.rapidesuite.client.common.gui;



public abstract class EnvironmentValidationButtonAbstractAction
{
    public static interface NextButtonWrapper
    {
        public void setNextButtonIsEnabled(boolean isEnabled);
        public boolean isNextButtonEnabled();
        public void simulateClickingNext();
    }

	public abstract boolean beforeExecuteAction(final EnvironmentValidationPanel environmentValidationPanel);
	public abstract void executeAction(final EnvironmentValidationPanel environmentValidationPanel);
    public abstract void afterExecuteAction(final EnvironmentValidationPanel environmentValidationPanel, NextButtonWrapper nextButtonWrapper);
}