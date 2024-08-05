package com.rapidesuite.inject.webservices;
/*
import java.io.FileNotFoundException;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import com.oracle.xmlns.apps.hcm.locations.locationservice.types.CreateBasicLocationDocument;
import com.oracle.xmlns.apps.hcm.locations.locationservice.types.CreateBasicLocationDocument.CreateBasicLocation;
import com.oracle.xmlns.apps.hcm.locations.locationservice.types.CreateBasicLocationResponseDocument;

import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.impl.dom.SOAPHeaderBlockImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11HeaderBlockImpl;

import odtest.LocationServiceStub;

import org.apache.rampart.RampartMessageData;

import javax.xml.stream.XMLStreamException;
*/

public class TestFusionWebServiceAXIS {

	public static void main(java.lang.String args[]){
		/*
		try{

			LocationServiceStub stub =	new LocationServiceStub("http://fusion01.rapidesuite.com:18619/locationsService/LocationService?WSDL");

			CreateBasicLocationDocument req =CreateBasicLocationDocument.Factory.newInstance();
			CreateBasicLocation data = req.addNewCreateBasicLocation();
			data.setLocationName("OD WSDL");
			
			ServiceClient client = stub._getServiceClient();
			SOAP11Factory factory = new SOAP11Factory();
			OMNamespace SecurityElementNamespace = factory.createOMNamespace("http://schemas.xmlsoap.org/ws/2002/12/secext", "wss");
			OMElement usernameTokenEl = factory.createOMElement("UsernameToken", SecurityElementNamespace);
			OMElement usernameEl = factory.createOMElement("Username", SecurityElementNamespace);
			usernameEl.setText("123");
			usernameTokenEl.addChild(usernameEl);
			OMElement passwordEl = factory.createOMElement("Password", SecurityElementNamespace);
			passwordEl.setText("123");
			usernameTokenEl.addChild(passwordEl);
			SOAPHeaderBlockImpl block = new SOAP11HeaderBlockImpl(null,"Security", SecurityElementNamespace, null,factory,false);
			block.addChild(usernameTokenEl);
			client.addHeader(block);
			
			System.out.println("before");
			CreateBasicLocationResponseDocument response=stub.createBasicLocation(req);
			System.out.println("Response: " + response.toString());

		}
		catch(Exception e){
			e.printStackTrace();
		}
		*/
	}
/*
	private static Policy loadPolicy(String filePath)
			throws XMLStreamException, FileNotFoundException {
		StAXOMBuilder builder = new StAXOMBuilder(filePath);
		return PolicyEngine.getPolicy(builder.getDocumentElement());
	}
*/
}
