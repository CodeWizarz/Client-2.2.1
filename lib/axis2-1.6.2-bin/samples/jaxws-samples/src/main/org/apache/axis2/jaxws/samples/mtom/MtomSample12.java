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

package org.apache.axis2.jaxws.samples.mtom;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


@WebService(name = "MtomSampleService12",
        targetNamespace = "http://org/apache/axis2/jaxws/samples/mtom/")
public interface MtomSample12 {


    /**
     * @param input
     * @return returns org.apache.axis2.jaxws.samples.mtom.ImageDepot
     */
    @WebMethod(action = "sendImage")
    @WebResult(name = "output", targetNamespace = "http://org/apache/axis2/jaxws/samples/mtom/")
    @RequestWrapper(localName = "sendImage", targetNamespace = "http://org/apache/axis2/jaxws/samples/mtom/", className = "org.apache.axis2.jaxws.samples.mtom.SendImage")
    @ResponseWrapper(localName = "sendImageResponse", targetNamespace = "http://org/apache/axis2/jaxws/samples/mtom/", className = "org.apache.axis2.jaxws.samples.mtom.SendImageResponse")
    public ImageDepot sendImage(
            @WebParam(name = "input", targetNamespace = "http://org/apache/axis2/jaxws/samples/mtom/")
            ImageDepot input);

}
