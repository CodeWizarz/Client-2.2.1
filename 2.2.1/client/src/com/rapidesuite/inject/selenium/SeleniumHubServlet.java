package com.rapidesuite.inject.selenium;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.common.exception.GridException;
import org.openqa.grid.internal.ProxySet;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

import com.rapidesuite.client.common.util.Config;

@SuppressWarnings("serial")
public class SeleniumHubServlet extends RegistryBasedServlet {
	
	private Set<String> nodeIdsToKillSet;
	private Map<String,SeleniumNodeInformation> displayNameToNodeInformationMap;
	private String serverId;
	private int killTimerInMs;
	
	public SeleniumHubServlet() throws Exception {
		this(null);
	}

	public SeleniumHubServlet(Registry registry) throws Exception {
		super(registry);
		nodeIdsToKillSet=new HashSet<String>();
		displayNameToNodeInformationMap=new HashMap<String,SeleniumNodeInformation>();
		List<SeleniumNodeInformation> nodesListingFile=SeleniumHub.readNodesListingFile();
		for (SeleniumNodeInformation seleniumNodeInformation:nodesListingFile) {
    		displayNameToNodeInformationMap.put(seleniumNodeInformation.getDisplayName(), seleniumNodeInformation);
		}
		try{
			String sysProp1 = "sysProp1";
			serverId=System.getProperty(sysProp1);
			System.out.println(new java.util.Date()+" serverId:" + serverId);
			killTimerInMs=Config.getInjectHomePageTimerInMins()*60*1000;
			System.out.println(new java.util.Date()+" killTimerInMs: '"+killTimerInMs+"'");
		}
		catch(Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String action = request.getParameter("action");
        if ( action != null && action.equalsIgnoreCase("getNodesList") ) {
        	processGetNodesListRequest(response);
		}
        else
        	if ( action != null && action.equalsIgnoreCase("startKillNodeTimer") ) {
        		processStartKillNodeTimerRequest(request,response);
        	}
        	else
            	if ( action != null && action.equalsIgnoreCase("stopKillNodeTimer") ) {
            		processStopKillNodeTimerRequest(request,response);
    		}
	}
	
	private void processStopKillNodeTimerRequest(HttpServletRequest request, HttpServletResponse response) {
		final String nodeId = request.getParameter("nodeId");
		System.out.println(new java.util.Date()+" processStopKillNodeTimerRequest, nodeId: '"+nodeId+"'");
		nodeIdsToKillSet.remove(nodeId);
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(200);
	}

	protected void processStartKillNodeTimerRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String nodeId = request.getParameter("nodeId");
		System.out.println(new java.util.Date()+" processStartKillNodeTimerRequest, nodeId: '"+nodeId+"'");
		nodeIdsToKillSet.add(nodeId);
		startKillNodeTimer(nodeId);
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(200);
	}

	private void startKillNodeTimer(final String nodeId) {
		new Thread( new Runnable() {
		    @Override
		    public void run() {
		    	try {
			    	System.out.println(new java.util.Date()+" startKillNodeTimer, nodeId: '"+nodeId+"' Timer started set at "+killTimerInMs+" ms");
					Thread.sleep(killTimerInMs);
					
					if (nodeIdsToKillSet.contains(nodeId)) {
						nodeIdsToKillSet.remove(nodeId);
						SeleniumNodeInformation seleniumNodeInformation=displayNameToNodeInformationMap.get(nodeId);
						System.out.println(new java.util.Date()+" startKillNodeTimer, nodeId: '"+nodeId+"' stopping node...");
						SeleniumHubTable.stopNode(null,seleniumNodeInformation);
						System.out.println(new java.util.Date()+" startKillNodeTimer, nodeId: '"+nodeId+"' after stopping node.");
						
						// wait 10 secs then restart the node to give time to be killed
						Thread.sleep(10000);
						System.out.println(new java.util.Date()+" startKillNodeTimer, nodeId: '"+nodeId+"' restarting node.");
						SeleniumHubTable.startNode(serverId,seleniumNodeInformation);
					}
					else {
						System.out.println(new java.util.Date()+" startKillNodeTimer, nodeId: '"+nodeId+"' Timer completed but no action required!");
					}
				} 
		    	catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		}).start();
	}

	protected void processGetNodesListRequest(HttpServletResponse response) throws IOException {
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(200);
		JSONObject res;
		try {
			res = getNodesListResponse();
			response.getWriter().print(res);
			response.getWriter().close();
		}
		catch (JSONException e) {
			throw new GridException(e.getMessage());
		}
	}

	private JSONObject getNodesListResponse() throws IOException, JSONException {
		JSONObject requestJSON = new JSONObject();
		ProxySet proxies = this.getRegistry().getAllProxies();
		Iterator<RemoteProxy> iterator = proxies.iterator();
		JSONArray busyProxies = new JSONArray();
		JSONArray freeProxies = new JSONArray();
		while (iterator.hasNext()) {
			RemoteProxy eachProxy = iterator.next();
			RegistrationRequest registrationRequest=eachProxy.getOriginalRegistrationRequest();
			//String host=(String) registrationRequest.getConfiguration().get("host");
			//Integer port=(Integer) registrationRequest.getConfiguration().get("port");
			String nodeIdentifier=(String) registrationRequest.getConfiguration().get("id");
			//System.out.println("nodeIdentifier:"+nodeIdentifier);
			/*
			Object websockifyPortObject=registrationRequest.getConfiguration().get("websockifyPort");
			String websockifyPort="";
			if (websockifyPortObject instanceof String) {
				websockifyPort=(String)websockifyPortObject;
			}
			else {
				websockifyPort=((Integer)websockifyPortObject).toString();
			}
			System.out.println("websockifyPort:"+websockifyPort);
			*/
			String id=nodeIdentifier;
			//test.getAssociatedJSON()
			if (eachProxy.isBusy()) {
				//busyProxies.put(eachProxy.getOriginalRegistrationRequest().getAssociatedJSON());
				busyProxies.put(id);
			}
			else {
				//freeProxies.put(eachProxy.getOriginalRegistrationRequest().getAssociatedJSON());
				freeProxies.put(id);
			}
		}
		requestJSON.put("BusyProxies", busyProxies);
		requestJSON.put("FreeProxies", freeProxies);

		return requestJSON;
	}

} 