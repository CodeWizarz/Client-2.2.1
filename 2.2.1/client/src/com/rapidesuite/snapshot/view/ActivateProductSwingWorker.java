package com.rapidesuite.snapshot.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.rapidesuite.client.common.util.Config;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.client.common.util.InsecureSSLSocketFactory;
import com.rapidesuite.snapshot.model.SnapshotSwingWorker;

public class ActivateProductSwingWorker extends SnapshotSwingWorker {
	
	String activationCommandParamName;
	String activationCommandParamvalue;
	String activationCodeParamName;
	String activationCodeParamValue;
	String activationPIDParamName;
	String activationPIDParamValue;
	
	String generatedProductKey;
	
	public String getGeneratedProductKey() {
		return generatedProductKey;
	}
	
	public ActivateProductSwingWorker(String activationCommandParamName, String activationCommandParamvalue, String activationCodeParamName, String activationCodeParamValue, 
			String activationPIDParamName, String activationPIDParamValue) {
		super(false);
		
		this.activationCommandParamName = activationCommandParamName;
		this.activationCommandParamvalue = activationCommandParamvalue;
		this.activationCodeParamName = activationCodeParamName;
		this.activationCodeParamValue = activationCodeParamValue;
		this.activationPIDParamName = activationPIDParamName;
		this.activationPIDParamValue = activationPIDParamValue;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		generatedProductKey = processAction();
		return null;
	}
	
	private String processAction() {
		super.updateExecutionLabels("Activating, please wait....");
		
		try {
			//CloseableHttpClient client = HttpClientBuilder.create().build();
			CloseableHttpClient client = (CloseableHttpClient) InsecureSSLSocketFactory.getNewHttpClient();
			HttpPost post = new HttpPost(Config.getOnlineActivationURL());
	
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair(activationCommandParamName, activationCommandParamvalue));
			urlParameters.add(new BasicNameValuePair(activationCodeParamName, activationCodeParamValue));
			urlParameters.add(new BasicNameValuePair(activationPIDParamName, activationPIDParamValue));
	
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
	
			HttpResponse response = client.execute(post);
			
			if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new Exception("Activation failed: Server returned " + response.getStatusLine().getStatusCode());
			}
	
			BufferedReader rd = new BufferedReader(
	                        new InputStreamReader(response.getEntity().getContent()));
	
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			
			JSONObject jSONRespObject = new JSONObject(result.toString());
			
			if(jSONRespObject.getBoolean("activated") == false) {
				throw new Exception("Activation failed: " + jSONRespObject.getString("message"));
			}
			
			return jSONRespObject.getString("product_key");
			
		} catch (UnsupportedEncodingException useex) {
			GUIUtils.popupErrorMessage(useex.getMessage());
			return null;
		} catch (ClientProtocolException cpex) {
			GUIUtils.popupErrorMessage(cpex.getMessage());
			return null;
		} catch (JSONException jex) {
			GUIUtils.popupErrorMessage(jex.getMessage());
			return null;
		} catch (IOException ioex) {
			GUIUtils.popupErrorMessage(ioex.getMessage());
			return null;
		} catch (Exception ex) {
			GUIUtils.popupErrorMessage(ex.getMessage());
			return null;
		}
		finally {
		}
	}

}
