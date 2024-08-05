/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/reverse/gui/DataExtractionEBSPropertiesValidationPanel.java $:
 * $Id: DataExtractionEBSPropertiesValidationPanel.java 31694 2013-03-04 06:33:20Z john.snell $:
 */

package com.rapidesuite.reverse.gui;

import com.rapidesuite.client.common.gui.EBSPropertiesValidationPanel;
import com.rapidesuite.reverse.ReverseMain;

@SuppressWarnings("serial")
public class DataExtractionEBSPropertiesValidationPanel extends EBSPropertiesValidationPanel {

	private ReverseMain ReverseMain;

	public DataExtractionEBSPropertiesValidationPanel(ReverseMain ReverseMain) {
		super(ReverseMain);
		this.ReverseMain=ReverseMain;
	}

	public ReverseMain getReverseMain() {
		return ReverseMain;
	}
}