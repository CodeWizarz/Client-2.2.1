package com.rapidesuite.inject.selenium;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;

public class SeleniumNodeProxy extends DefaultRemoteProxy{

	public SeleniumNodeProxy(RegistrationRequest request, Registry registry) {
		super(request, registry);
	}

}
