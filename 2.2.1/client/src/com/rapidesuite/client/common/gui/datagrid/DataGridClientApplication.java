package com.rapidesuite.client.common.gui.datagrid;

import java.util.AbstractMap.SimpleEntry;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openswing.swing.domains.java.Domain;
import org.openswing.swing.internationalization.java.EnglishOnlyResourceFactory;
import org.openswing.swing.util.client.ClientSettings;

import com.rapidesuite.client.common.util.FileUtils;

public class DataGridClientApplication {
	
	private static Hashtable<String,Domain> allDomains=new Hashtable<String,Domain>();
	private static Properties allProperties=new Properties();
		
	public synchronized static void initializeResources(Properties props,Hashtable<String,Domain> domains)  {
		//System.out.println(new java.util.Date()+" : ############## initializeResources....");
		new ClientSettings(getEnglishOnlyResourceFactory(props),getAllDomains(domains));
		//System.out.println(new java.util.Date()+" : ############## initializeResources COMPLETED.");
	}
	
	public static EnglishOnlyResourceFactory getEnglishOnlyResourceFactory(Properties props) { 
		while (true) {
			try{
				return new EnglishOnlyResourceFactory("GBP",getAllProperties(props),true);
			} 
			catch(ConcurrentModificationException e) {
				FileUtils.println("WARNING: unable to create a ClientSettings object (ConcurrentModificationException), retrying...");
				try {
					com.rapidesuite.client.common.util.Utils.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public static Hashtable<String, Domain> getAllDomains(Hashtable<String,Domain> domains) { 
		allDomains.putAll(domains);	
		return allDomains;
	}
	
	public static Properties getAllProperties(Properties props) { 
		allProperties.putAll(props);	
		return allProperties;
	}
	
	public static Properties getClientSettingsProperties(Map<String,String> columnNamesToTextMap) { 
		Properties props = new Properties();
		Iterator<String> iterator=columnNamesToTextMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			String value=columnNamesToTextMap.get(key);
			props.setProperty(key,value);
		}
		return props;
	}
	
	public static Hashtable<String,Domain> getClientSettingsDomains(
			String domainIdPrefix,Properties properties,Map<String,List<SimpleEntry<String,String>>> columnNameKeyValuePairs) { 
		Hashtable<String,Domain> domains = new Hashtable<String,Domain>();
		Iterator<String> iterator=columnNameKeyValuePairs.keySet().iterator();
		while (iterator.hasNext()) {
			String key=iterator.next();
			Domain domain = new Domain(domainIdPrefix+key);
			List<SimpleEntry<String,String>> keyValuePairs=columnNameKeyValuePairs.get(key);
			addDomainPair(properties,domain,keyValuePairs); 
			domains.put(domain.getDomainId(),domain);
		}
		return domains;
	}
	
	public static void addDomainPair(Properties properties,Domain domain,List<SimpleEntry<String,String>> keyValuePairs) { 
		for (SimpleEntry<String,String> keyValuePair:keyValuePairs){
			domain.addDomainPair(keyValuePair.getKey(),keyValuePair.getKey());
			properties.setProperty(keyValuePair.getValue(),keyValuePair.getValue());
		}
	}
	
}
