package com.rapidesuite.snapshot.view;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JFrame;

public class SearchWindowModules  extends SearchWindow{

	private FilterModulePanel filterModulePanel;

	public SearchWindowModules(JFrame rootFrame,FilterModulePanel filterModulePanel, 
			String title,Map<String,Object> selectedResultsKeyToObjectMap) {
		super(rootFrame, title ,SearchWindow.WILDCARD_KEYWORD,selectedResultsKeyToObjectMap);
		this.filterModulePanel=filterModulePanel;
		processActionSearch(SearchWindow.WILDCARD_KEYWORD);
		dialog.setVisible(true);
	}

	@Override
	public Map<String,Object> search(String inputValue)  throws Exception {
		Set<String> moduleSet=filterModulePanel.getAllModuleSet();
		Map<String,Object> toReturn=new TreeMap<String,Object>();
		Iterator<String> iterator=moduleSet.iterator();
		while (iterator.hasNext()) {
			String module=iterator.next();
			if (inputValue.contains(SearchWindow.WILDCARD_KEYWORD)) {
				String tmp=inputValue.replaceAll(SearchWindow.WILDCARD_KEYWORD, ".*");
				boolean isMatch=module.toLowerCase().matches(tmp.toLowerCase());
				if (isMatch) {
					toReturn.put(module, module);
				}
			}
			else 
				if (module.equalsIgnoreCase(inputValue)) {
					toReturn.put(module, module);
				}
		}
		return toReturn;

	}

	public void apply(Map<String, Object> selectedResultsKeyToObjectMap) {
		filterModulePanel.setSelectedMap(selectedResultsKeyToObjectMap);
		filterModulePanel.processActionModulesSelection();
		dialog.dispose();
	}

}