package com.rapidesuite.client.common.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;

import com.rapidesuite.client.common.util.FileUtils;

public class TextTreeMouseListener extends MouseAdapter{ 
	
	protected JTree tree;
	
	public TextTreeMouseListener(JTree tree) {
		this.tree=tree;
	}
	
	public void mouseClicked(MouseEvent me){ 
		refreshTree(tree);
    } 
	
	public static void refreshTree(final JTree tree){ 
        Runnable r=new Runnable(){
    		public void run(){
    			try {
    				com.rapidesuite.client.common.util.Utils.sleep(1000);
    				tree.treeDidChange();
				} catch (Exception ex) {
					FileUtils.printStackTrace(ex);
				}
    		}
    	};
    	Thread t=new Thread(r);
    	t.start();
    } 

	
} 