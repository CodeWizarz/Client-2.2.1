/**************************************************
 * $Revision: 41874 $:
 * $Author: fajrian.yunus $:
 * $Date: 2014-06-25 17:20:14 +0700 (Wed, 25 Jun 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/gui/EnvironmentValidationProperty.java $:
 * $Id: EnvironmentValidationProperty.java 41874 2014-06-25 10:20:14Z fajrian.yunus $:
 */

package com.rapidesuite.client.common.gui;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class EnvironmentValidationProperty {

	@Override
    public String toString()
    {
        return "EnvironmentValidationProperty [key=" + key + ", mappings=" + mappings + "]";
    }

    private String key;
	private JLabel labelComponent;
	private JComponent valueComponent;
	private boolean isOptional;
	private Map<String,String> mappings;

	public EnvironmentValidationProperty(String key,JLabel labelComponent,JComponent valueComponent,
			boolean isOptional){
		this.key=key;
		this.labelComponent=labelComponent;
		this.valueComponent=valueComponent;
		this.isOptional=isOptional;
	}

	public boolean isOptional() {
		return isOptional;
	}

	public JLabel getLabelComponent() {
		return labelComponent;
	}

	public JComponent getValueComponent() {
		return valueComponent;
	}

	public String getKey() {
		return key;
	}

	public void setMappings(Map<String,String> mappings) {
		this.mappings=mappings;
	}

	public Map<String, String> getMappings() {
		return mappings;
	}

	public String getMappingValue(String valueParameter) {
		Iterator<String> iterator=mappings.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			String value=mappings.get(key);
			if (value.equalsIgnoreCase(valueParameter)) {
				return key;
			}
		}
		return null;
	}

}