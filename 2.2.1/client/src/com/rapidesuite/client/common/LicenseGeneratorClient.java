package com.rapidesuite.client.common;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.rapidesuite.client.licenseClient0001.LicenseDocument;
import com.rapidesuite.client.licenseClient0001.Plugin;
import com.rapidesuite.client.licenseClient0001.Plugins;
import com.rapidesuite.configurator.utility.LicenseEncryptor;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.snapshot.model.ModelUtils;

public class LicenseGeneratorClient {
	// default value "pid.txt"
	@Parameter(names = {"-pid", "-pidfile"}, description = "Product Id file name (input key)")
	private String productIdFile = "pid.txt";

	// default value "Rapid4Cloud"
	@Parameter(names = {"-company", "-companyName"}, description = "Company name for the license")
	private String companyName = "Rapid4Cloud";

	// no default value, throws error.
	@Parameter(names = {"-expDate", "-expiryDate"}, description = "License Expiry date (license valid until) - Date Format: 2016.01.18-14:47:28")
	private String expDate = null;

	// default value "true"
	@Parameter(names = {"-validateHardware"}, description = "Should the hardware be validated by Client (true or false)")
	private String validateHardware = "true";

	// no default value
	@Parameter(names = {"-products"}, description = "Products to be licensed (comma separated values) - Format: INJECT,REVERSE,BUILD,SNAPSHOT")
	private String products = null;
	
	// no default value
	@Parameter(names = {"-plugins"}, description = "Plugins to be licensed (comma separated values) - Format: PLUGIN_SNAPSHOT_DATA_CONVERSION,...")
	private String plugins = null;

	// default value comes from the temp license
	@Parameter(names = {"-majorVersion", "-version"}, description = "Application Major Version for the CLIENT license")
	private String majorVersion = null;

	public static void main(String[] args) throws Exception
	{
		LicenseGeneratorClient licenseGeneratorClient = new LicenseGeneratorClient();
        new JCommander(licenseGeneratorClient, args);        
        licenseGeneratorClient.genLicMan();
	}
	
	public void genLicMan() throws Exception
	{
        if(StringUtils.isBlank(expDate)) {
        	throw new Exception("Expiry Date missing.");
        }

        if(StringUtils.isBlank(products)) {
        	throw new Exception("Product(s) missing.");
        }
        
		File srcFile=new File(productIdFile);
		
		LicenseDocument licDoc=ModelUtils.getPKDocument(srcFile);
		
		// dump the unencrypted source file
		File srcUnEncFile=new File(productIdFile + ".unenc.xml");
		ModelUtils.writeToFile(srcUnEncFile,StringEscapeUtils.unescapeXml(licDoc.toString()),false);
		
		String generationDateTime = LicenseEncryptor.formatExpirationDate(new Date(System.currentTimeMillis()));
		
		String activationDate=licDoc.getLicense().getActivationDate();
		if (activationDate==null) {
			licDoc.getLicense().setActivationDate(generationDateTime);
		}
		licDoc.getLicense().setCompanyName(companyName);
		licDoc.getLicense().setExpirationDate(expDate);
		
		licDoc.getLicense().setValidateHardware(validateHardware);
		
		String[] licensedProductsArray = products.split(",");
		licDoc.getLicense().setProductArray(licensedProductsArray);
		
		if (StringUtils.isNotBlank(plugins)) { 
			String[] pluginsArray = plugins.split(",");
			Plugins pluginsObj=licDoc.getLicense().addNewPlugins();
			for (String pluginStr:pluginsArray) {
				Plugin plugin=pluginsObj.addNewPlugin();
				plugin.setName(pluginStr);
			}
		}	
		
		if(majorVersion != null) {
			licDoc.getLicense().setClientMajorVersion(majorVersion);
		}
		
		// Prepare output filename
		StringBuffer productKeyFileNameBuffer = new StringBuffer("client_pky" + ".");
		productKeyFileNameBuffer.append("cn-" + licDoc.getLicense().getCompanyName() + ".");
		productKeyFileNameBuffer.append("mv-" + licDoc.getLicense().getClientMajorVersion() + ".");
		productKeyFileNameBuffer.append("hn-" + CoreUtil.joinCommaDelimitedLabels(Arrays.asList(licDoc.getLicense().getHostnameArray())).replaceAll(",", ".") + ".");
		productKeyFileNameBuffer.append("exp-" + licDoc.getLicense().getExpirationDate().replaceAll("[:-]", ".") + ".");
		productKeyFileNameBuffer.append("hw-" + licDoc.getLicense().getValidateHardware() + ".");
		productKeyFileNameBuffer.append("gen-" + generationDateTime.replaceAll("[:-]", ".") + ".");
		productKeyFileNameBuffer.append("txt");
		
		String productKeyFile = productKeyFileNameBuffer.toString();
		productKeyFile = productKeyFile.replaceAll("[^a-zA-Z0-9.-]", "_");
		
		File destFile=new File(productKeyFile);
				
		// dump the unencrypted destination file
		File destUnEncFile=new File(productKeyFile + ".unenc.xml");
		ModelUtils.writeToFile(destUnEncFile,StringEscapeUtils.unescapeXml(licDoc.toString()),false);
		
		String content=StringEscapeUtils.unescapeXml(licDoc.toString());
		String encText= ModelUtils.enc(content,ModelUtils.EK);
		ModelUtils.writeToFile(destFile,encText,false); 
	}
	
}
