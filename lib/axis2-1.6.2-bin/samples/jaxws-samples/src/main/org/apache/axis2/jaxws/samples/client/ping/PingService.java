/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.samples.client.ping;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.net.MalformedURLException;
import java.net.URL;


@WebServiceClient(name = "PingService", targetNamespace = "http://org/apache/axis2/jaxws/samples/ping/", wsdlLocation = "WEB-INF/wsdl/Ping.wsdl")
public class PingService
        extends Service {

    private final static URL PINGSERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("file:/WEB-INF/wsdl/Ping.wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        PINGSERVICE_WSDL_LOCATION = url;
    }

    public PingService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public PingService() {
        super(PINGSERVICE_WSDL_LOCATION, new QName("http://org/apache/axis2/jaxws/samples/ping/", "PingService"));
    }

    /**
     * @return returns PingServicePortType
     */
    @WebEndpoint(name = "PingServicePort")
    public PingServicePortTypeClient getPingServicePort() {
        return (PingServicePortTypeClient) super.getPort(new QName("http://org/apache/axis2/jaxws/samples/ping/", "PingServicePort"), PingServicePortTypeClient.class);
    }

}
