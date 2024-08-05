package com.rapidesuite.client.common.gui.datagrid;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

public class ListExternalizable implements Externalizable {

    private List<String[]> list;

    public ListExternalizable() {
        this(null);
    }

    public ListExternalizable(List<String[]> list) {
        this.list = list;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    	if (list==null) {
    		out.writeUTF("0");
    		return;
    	}
        out.writeUTF(String.valueOf(list.size()));
        for (String[] row:list) {
        	out.writeUTF(String.valueOf(row.length));
        	for (String val:row) {
        		if (val==null) {
        			val="";
        		}
            	out.writeUTF(val);
            }
        }
    }

    public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
    	int listSize   = Integer.valueOf(in.readUTF()).intValue();
    	if ( listSize ==0) {
    		list = new ArrayList<String[]>();
    		return;
    	}
    	list = new ArrayList<String[]>(listSize);
    	for (int i=0; i<listSize; i++) {
    		int rowSize= Integer.valueOf(in.readUTF()).intValue();
    		String[] row=new String[rowSize]; 
    		for (int j=0; j<rowSize; j++) {
    			 row[j]=in.readUTF();
    		}
    		list.add(row);
    	}
    }

    public List<String[]> getList() {
    	return list;
	}

}