package com.rapidesuite.client.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.erapidsuite.configurator.license0001.LicenseDocument;
import com.rapidesuite.configurator.utility.LicenseEncryptor;
import com.rapidesuite.core.utility.CoreUtil;
import com.rapidesuite.snapshot.model.ModelUtils;

public class LicenseGeneratorConfigurator {
	// default value "pid.txt"
	@Parameter(names = {"-pid", "-pidfile"}, description = "Product Id file name (input key)")
	private String productIdFile = "pid.txt";

	// default value "Rapid4Cloud"
	@Parameter(names = {"-company", "-companyName"}, description = "Company name for the license")
	private String companyName = "Rapid4Cloud";

	// no default value, throws error.
	@Parameter(names = {"-expDate", "-expiryDate"}, description = "License Expiry date (license valid until) - Date Format: 2016.01.18-14:47:28")
	private String expDate = null;

	// default value 1
	@Parameter(names = {"-maxConcurrentUsers", "-maxSessions", "-sessions"}, description = "Maximum concurrent sessions for license")
	private int maxConcurrentUsers = 1;

	// default value "true"
	@Parameter(names = {"-validateHardware"}, description = "Should the hardware be validated by Configurator (true or false)")
	private String validateHardware = "true";

	// no default value
	@Parameter(names = {"-additionalHosts", "-addHosts", "-hosts"}, description = "Additional hosts to be licensed (comma separated values) - Format: rcqa06,rcqa06.rapidesuite.com,rcqa06.rapid4cloud.com")
	private String additionalHosts = null;

	// no default value
	@Parameter(names = {"-additionalPorts", "-addPorts", "-ports"}, description = "Additional ports to be licensed (comma separated values) - Format: 7001,80,443")
	private String additionalPorts = null;

	// default value comes from the temp license
	@Parameter(names = {"-rootDomain", "-rootDomainName"}, description = "Root Domain for the CONFIGURATOR license")
	private String rootDomain = null;

	// default value comes from the temp license
	@Parameter(names = {"-majorVersion", "-version"}, description = "Application Major Version for the CONFIGURATOR license")
	private String majorVersion = null;

	public static void main(String[] args) throws Exception
	{
		LicenseGeneratorConfigurator licenseGeneratorConfigurator = new LicenseGeneratorConfigurator();
        new JCommander(licenseGeneratorConfigurator, args);        
        licenseGeneratorConfigurator.genLicMan();
	}
	
	public void genLicMan() throws Exception
	{
        if(StringUtils.isBlank(expDate)) {
        	throw new Exception("Expiry Date missing.");
        }
        
		File srcFile=new File(productIdFile);
		
		LicenseDocument licDoc=getPKDocument(srcFile);
		
		// dump the unencrypted source file
		File srcUnEncFile=new File(productIdFile + ".unenc.xml");
		ModelUtils.writeToFile(srcUnEncFile,StringEscapeUtils.unescapeXml(licDoc.toString()),false);
		
		licDoc.getLicense().setCompanyName(companyName);
		licDoc.getLicense().setExpirationDate(expDate);
		licDoc.getLicense().setMaximumConcurrentUserSessions(maxConcurrentUsers);
		
		licDoc.getLicense().setValidateHardware(validateHardware);
		
		if(additionalHosts != null) {
			licDoc.getLicense().setHostnameArray(ArrayUtils.addAll(licDoc.getLicense().getHostnameArray(), additionalHosts.split(",")));
		}
		
		if(additionalPorts != null) {
			String[] additionalPortsStr = additionalPorts.split(",");
			int[] additionalPortsInt = new int[additionalPortsStr.length];
			for(int i=0; i<additionalPortsStr.length; i++) {
				additionalPortsInt[i] = Integer.parseInt(additionalPortsStr[i]);
			}
			licDoc.getLicense().setPortArray(ArrayUtils.addAll(licDoc.getLicense().getPortArray(), additionalPortsInt));
		}
		
		if(StringUtils.isNotBlank(rootDomain)) {
			licDoc.getLicense().setHostRootDomain(rootDomain);
		}
		
		if(majorVersion != null) {
			licDoc.getLicense().setConfiguratorMajorVersion(majorVersion);
		}
		
		StringBuffer productKeyFileNameBuffer = new StringBuffer("configurator_pky" + ".");
		
		// Prepare output filename
		String generationDateTime = LicenseEncryptor.formatExpirationDate(new Date(System.currentTimeMillis()));
		
		productKeyFileNameBuffer.append("cn-" + licDoc.getLicense().getCompanyName() + ".");
		productKeyFileNameBuffer.append("mv-" + licDoc.getLicense().getConfiguratorMajorVersion() + ".");
		productKeyFileNameBuffer.append("hn-" + CoreUtil.joinCommaDelimitedLabels(Arrays.asList(licDoc.getLicense().getHostnameArray())).replaceAll(",", ".") + ".");
		productKeyFileNameBuffer.append("rd-" + licDoc.getLicense().getHostRootDomain() + ".");
		productKeyFileNameBuffer.append("exp-" + licDoc.getLicense().getExpirationDate().replaceAll("[:-]", ".") + ".");
		productKeyFileNameBuffer.append("ul-" + licDoc.getLicense().getMaximumConcurrentUserSessions() + ".");
		productKeyFileNameBuffer.append("hw-" + licDoc.getLicense().getValidateHardware() + ".");
		productKeyFileNameBuffer.append("gen-" + generationDateTime.replaceAll("[:-]", ".") + ".");
		productKeyFileNameBuffer.append("txt");
		
		String productKeyFile = productKeyFileNameBuffer.toString();
		productKeyFile = productKeyFile.replaceAll("[^a-zA-Z0-9.-]", "_");
		
		File destFile=new File(productKeyFile);

		// dump the unencrypted destination file
		File destUnEncFile = new File(productKeyFile + ".unenc.xml");
		ModelUtils.writeToFile(destUnEncFile, StringEscapeUtils.unescapeXml(licDoc.toString()), false);
		
		String content=StringEscapeUtils.unescapeXml(licDoc.toString());
		String encText= ModelUtils.enc(content,ModelUtils.EK);
		ModelUtils.writeToFile(destFile,encText,false); 
	}
	
	public LicenseDocument getPKDocument(File file) throws Exception{
		FileInputStream fis = new FileInputStream(file);
		final byte[] eBytes = IOUtils.toByteArray(fis);
		final String licText = new String(eBytes);
		final String decText =ModelUtils.dec(licText,ModelUtils.EK);
		
		LicenseDocument licenseDocument = LicenseDocument.Factory.parse(decText);
		return licenseDocument;
	}
	
}
