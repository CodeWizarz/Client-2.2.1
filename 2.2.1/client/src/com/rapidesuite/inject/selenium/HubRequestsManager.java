package com.rapidesuite.inject.selenium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rapidesuite.client.common.util.FileUtils;

public class HubRequestsManager {
		
	public static NodesInfo getNodesInfo(String serverId) throws Exception {
		try{
			Set<String> busyNodesList=new TreeSet<String>();
			Set<String> freeNodesList=new TreeSet<String>();
			
			String url="http://" + serverId + "/grid/admin/SeleniumHubServlet?action=getNodesList";
			URL proxyApi = new URL(url);
			//System.out.println("##### url: "+url);
			HttpClient client = HttpClientBuilder.create().build();
			BasicHttpRequest r = new BasicHttpRequest("GET", proxyApi.toExternalForm());

			int indexOfColumn=serverId.indexOf(":");
			if (indexOfColumn==-1) {
				return new NodesInfo(freeNodesList,busyNodesList);
			}
			String hubHost=serverId.substring(0,indexOfColumn);
			int hubPort=Integer.valueOf(serverId.substring(indexOfColumn+1));
			//System.out.println("hubHost:"+hubHost+" hubPort:"+hubPort);
			HttpHost httpHost = new HttpHost(hubHost, hubPort);
			HttpResponse response = client.execute(httpHost, r);
			if (response.getStatusLine().getStatusCode()!=200) {
				System.out.println("Incorrect response code! status code: "+response.getStatusLine().getStatusCode());
				return null;
			}
			JSONObject jsonObject = extractObject(response);
			
			JSONArray proxies=jsonObject.getJSONArray("FreeProxies");
			//System.out.println("Free proxies count: "+proxies.length()+" proxies:"+proxies);
			
			for (int i=0;i<proxies.length();i++) {
				String jsonString=(String) proxies.get(i);
				//System.out.println("jsonString: "+jsonString);
				freeNodesList.add(jsonString);
				/*
				 * String jsonString=(String) proxies.get(i);
				 * JSONObject jsonObjectNode=new JSONObject(jsonString);
				JSONArray configuration=jsonObjectNode.getJSONArray("configuration");
				
				*/
			}
			proxies=jsonObject.getJSONArray("BusyProxies");
			//System.out.println("Busy proxies count: "+proxies.length()+" proxies:"+proxies);
			
			for (int i=0;i<proxies.length();i++) {
				String jsonString=(String) proxies.get(i);
				busyNodesList.add(jsonString);
			}
			NodesInfo nodesInfo=new NodesInfo(freeNodesList,busyNodesList);

			return nodesInfo;

		}
		catch(org.apache.http.conn.HttpHostConnectException e) {
			FileUtils.printStackTrace(e);
			throw new Exception("The server is unreachable, please make sure the server is started.\nError: \""+e.getMessage()+"\"");
		}
	}
	
	public static void startKillNodeTimer(String serverId,String nodeId) throws Exception {
		try{
			String url="http://" + serverId + "/grid/admin/SeleniumHubServlet?action=startKillNodeTimer&nodeId="+nodeId;
			URL proxyApi = new URL(url);
			//System.out.println("##### url: "+url);
			HttpClient client = HttpClientBuilder.create().build();
			BasicHttpRequest r = new BasicHttpRequest("GET", proxyApi.toExternalForm());

			int indexOfColumn=serverId.indexOf(":");
			String hubHost=serverId.substring(0,indexOfColumn);
			int hubPort=Integer.valueOf(serverId.substring(indexOfColumn+1));
			//System.out.println("hubHost:"+hubHost+" hubPort:"+hubPort);
			HttpHost httpHost = new HttpHost(hubHost, hubPort);
			HttpResponse response = client.execute(httpHost, r);
			if (response.getStatusLine().getStatusCode()!=200) {
				System.out.println("Incorrect response code! status code: "+response.getStatusLine().getStatusCode());
			}
		}
		catch(org.apache.http.conn.HttpHostConnectException e) {
			FileUtils.printStackTrace(e);
			throw new Exception("The server is unreachable, please make sure the server is started.\nError: \""+e.getMessage()+"\"");
		}
	}
		
	private static JSONObject extractObject(HttpResponse resp) throws IOException, JSONException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		StringBuilder s = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			s.append(line);
		}
		rd.close();
		//System.out.println("s.toString():"+s.toString());
		return new JSONObject(s.toString());
	}

	public static void stopKillNodeTimer(String serverId, String nodeId) throws Exception {
		try{
			String url="http://" + serverId + "/grid/admin/SeleniumHubServlet?action=stopKillNodeTimer&nodeId="+nodeId;
			URL proxyApi = new URL(url);
			//System.out.println("##### url: "+url);
			HttpClient client = HttpClientBuilder.create().build();
			BasicHttpRequest r = new BasicHttpRequest("GET", proxyApi.toExternalForm());

			int indexOfColumn=serverId.indexOf(":");
			String hubHost=serverId.substring(0,indexOfColumn);
			int hubPort=Integer.valueOf(serverId.substring(indexOfColumn+1));
			//System.out.println("hubHost:"+hubHost+" hubPort:"+hubPort);
			HttpHost httpHost = new HttpHost(hubHost, hubPort);
			HttpResponse response = client.execute(httpHost, r);
			if (response.getStatusLine().getStatusCode()!=200) {
				System.out.println("Incorrect response code! status code: "+response.getStatusLine().getStatusCode());
			}
		}
		catch(org.apache.http.conn.HttpHostConnectException e) {
			FileUtils.printStackTrace(e);
			throw new Exception("The server is unreachable, please make sure the server is started.\nError: \""+e.getMessage()+"\"");
		}
	}
	
} 