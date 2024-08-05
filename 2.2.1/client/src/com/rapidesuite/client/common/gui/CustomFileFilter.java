/**************************************************
 * $Revision: 39031 $:
 * $Author: john.snell $:
 * $Date: 2014-02-14 13:50:12 +0700 (Fri, 14 Feb 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/CustomFileFilter.java $:
 * $Id: CustomFileFilter.java 39031 2014-02-14 06:50:12Z john.snell $:
*/
package com.rapidesuite.client.common.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import com.rapidesuite.core.utility.CoreUtil;

public class CustomFileFilter extends FileFilter
{
	private Map<String,String> extensionTypes;

	public CustomFileFilter(String extensionType) {
		extensionTypes=new HashMap<String,String>();
		extensionTypes.put(extensionType.toLowerCase(), extensionType.toLowerCase());
	}

	public CustomFileFilter(Map<String,String> extensionTypes) {
		this.extensionTypes=extensionTypes;
	}

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = CoreUtil.getFileExtension(f);
        if (extension != null &&
        	extensionTypes.get(extension.toLowerCase())!=null) {
             return true;
        }
		return false;
    }

    public String getDescription() {
    	Iterator<String> iterator=extensionTypes.keySet().iterator();
    	String res="";
    	while (iterator.hasNext()) {
    		String key=iterator.next();
    		res+=" "+key;
    	}
        return res.trim();
    }

}