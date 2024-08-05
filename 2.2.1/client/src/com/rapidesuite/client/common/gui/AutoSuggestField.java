package com.rapidesuite.client.common.gui;

import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class AutoSuggestField extends JComboBox{
    private static final long serialVersionUID = -259165960224946764L;
    private final JTextField tf;
	private final Vector<String> fullList;
	private boolean hide_flag = false;

	public AutoSuggestField(List<String> list) {
		fullList = new Vector<String>(list);
		setEditable(true);
		tf = (JTextField)getEditor().getEditorComponent();
		tf.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						String text = tf.getText();
						if(text.length()==0) {
							hidePopup();
							setModel(new DefaultComboBoxModel<String>(fullList), "");
						}
						else{
							DefaultComboBoxModel<String> m = getSuggestedModel(fullList, text);
							if(m.getSize()==0 || hide_flag) {
								hidePopup();
								hide_flag = false;
							}
							else{
								setModel(m, text);
								showPopup();
							}
						}
					}
				});
			}
			public void keyPressed(KeyEvent e) {
				String text = tf.getText();
				int code = e.getKeyCode();
				if(code==KeyEvent.VK_ENTER) {
					if(!fullList.contains(text)) {
						fullList.addElement(text);
						Collections.sort(fullList);
						setModel(getSuggestedModel(fullList, text), text);
					}
					hide_flag = true;
				}
				else if(code==KeyEvent.VK_ESCAPE) {
					hide_flag = true;
				}
				else if(code==KeyEvent.VK_RIGHT) {
					for(int i=0;i<fullList.size();i++) {
						String str = fullList.elementAt(i);
						if(str.indexOf(text)!=-1 ) {
							setSelectedIndex(-1);
							tf.setText(str);
							return;
						}
					}
				}
			}
		});
		setModel(new DefaultComboBoxModel<String>(fullList), "");
	}

	private void setModel(DefaultComboBoxModel<String> mdl, String str) {
		setModel(mdl);
		setSelectedIndex(-1);
		tf.setText(str);
	}

	public String getText(){
		JTextField t=(JTextField)getEditor().getEditorComponent();
		return t.getText();
	}

	private static DefaultComboBoxModel<String> getSuggestedModel(java.util.List<String> list, String text) {
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		for(String s: list) {
			if(s.toLowerCase().indexOf(text.toLowerCase())!=-1) {
				m.addElement(s);
			}
		}
		return m;
	}

}