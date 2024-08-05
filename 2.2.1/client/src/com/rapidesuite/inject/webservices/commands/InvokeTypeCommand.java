package com.rapidesuite.inject.webservices.commands;

import java.util.Arrays;
import java.util.List;

import com.erapidsuite.configurator.navigation0005.FusionWebServiceNavigationType;
import com.erapidsuite.configurator.navigation0005.InvokeType;
import com.erapidsuite.configurator.navigation0005.MethodParameterType;
import com.erapidsuite.configurator.navigation0005.MethodParametersType;
import com.rapidesuite.inject.commands.Command;
import com.rapidesuite.inject.gui.ApplicationInfoPanel;
import com.rapidesuite.inject.webservices.FusionWebServiceWorker;
import com.rapidesuite.inject.webservices.WebServicesUtils;

public class InvokeTypeCommand extends Command {

	public InvokeTypeCommand(FusionWebServiceWorker fusionWebServiceWorker) {
		super(fusionWebServiceWorker);
	}

	public void process(InvokeType invokeType) throws Exception {
		String methodName=invokeType.getMethodName();
		worker.println("Method name: '"+methodName+"'");
		MethodParametersType methodParametersType=invokeType.getMethodParameters();

		MethodParameterType[] methodParameters=methodParametersType.getMethodParameterArray();
		List<MethodParameterType> methodParametersList=Arrays.asList(methodParameters);
		
		FusionWebServiceNavigationType fusionWebServiceNavigation=((FusionWebServiceWorker)worker).getFusionWebServiceNavigation();
		
		String applicationKey=worker.getBatchInjectionTracker().getScriptGridTracker().getScript().getApplicationKey();		
		String domainURLAsString=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getApplicationInfoPanel().getUrl(applicationKey); 
		String username=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getApplicationInfoPanel().getUserName(applicationKey);
		String password=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getApplicationInfoPanel().getPassword(applicationKey);
		String endpointPrefixUrl=fusionWebServiceNavigation.getEndPointURLPath();
		String endPointServletName=fusionWebServiceNavigation.getEndPointURLServletName();
		String sdoPackageName=fusionWebServiceNavigation.getSdoPackageName();
		String sdoName=fusionWebServiceNavigation.getSdoName();
		
		String applicationDBKey=ApplicationInfoPanel.DATABASE_APPLICATION_KEY;
		String jdbcString=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getApplicationInfoPanel().getUrl(applicationDBKey); 
		String dbUsername=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getApplicationInfoPanel().getUserName(applicationDBKey);
		String dbPassword=worker.getBatchInjectionTracker().getScriptGridTracker().getInjectMain().getScriptManager().getInjectMain().getApplicationInfoPanel().getPassword(applicationDBKey);
			
		WebServicesUtils.invokeWebService(worker,domainURLAsString,username,password,endpointPrefixUrl,endPointServletName,sdoPackageName,sdoName,
				methodName,methodParametersList,jdbcString,dbUsername,dbPassword);
		
		//com.rapidesuite.inject.webservices.TestWebServices.testCreateLocation();
	}

}