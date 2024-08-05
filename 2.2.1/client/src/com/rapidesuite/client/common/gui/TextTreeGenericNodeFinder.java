/**************************************************
 * $Revision: 31694 $:
 * $Author: john.snell $:
 * $Date: 2013-03-04 13:33:20 +0700 (Mon, 04 Mar 2013) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/TextTreeGenericNodeFinder.java $:
 * $Id: TextTreeGenericNodeFinder.java 31694 2013-03-04 06:33:20Z john.snell $:
 */

package com.rapidesuite.client.common.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class TextTreeGenericNodeFinder extends JPanel
{

	private JButton searchButton;
	protected AutoSuggestField autoSuggestField;
	private List<String> list;
	
	public TextTreeGenericNodeFinder(List<String> list){
		this.list=list;
		createComponents();
	}

	public void createComponents(){
		setBorder(BorderFactory.createEtchedBorder());
		
		searchButton = new JButton("Find");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchForNodes();
			}
		});
		
		autoSuggestField=new AutoSuggestField(list);
		add(new JLabel("Search: "));
		add(autoSuggestField);
		add(searchButton);
	}

	public abstract void searchForNodes();
	
	public void lockAll() {
		 setComponentsStatus(false);
	}
	
	public void unlockAll() {
		 setComponentsStatus(true);
	}
	
	public void setComponentsStatus(boolean isEnabled) {
		searchButton.setEnabled(isEnabled);
		autoSuggestField.setEnabled(isEnabled);
	}

	public AutoSuggestField getAutoSuggestField() {
		return autoSuggestField;
	}
	
}