/*
 * Copyright 2016 Esri, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.commons.csw.client.impl;

import com.esri.geoportal.commons.csw.client.IProfile;
import com.esri.geoportal.commons.csw.client.IProfiles;
import com.esri.geoportal.commons.csw.client.IRecords;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

/**
 * CSW client test.
 */
public class ClientTest {
  
  public static ClientAndServer server;
  
  public static MockServerClient client;

  @BeforeClass
  public static void setup() throws IOException {
    server = startClientAndServer(5000);
    client = new MockServerClient("localhost", 5000);
    client.when(
                    request()
                    .withMethod("GET")
                    .withPath("/robots.txt")
            )
            .respond(
                    response()
                    .withStatusCode(200)
                    .withBody(readAndClose(ClientTest.class.getResourceAsStream("/robots.txt")))
            );
    client.when(
                    request()
                    .withMethod("GET")
                    .withQueryStringParameter("request", "GetCapabilities")
                    .withQueryStringParameter("service", "CSW")
                    .withPath("/csw")
            )
            .respond(
                    response()
                    .withStatusCode(200)
                    .withHeaders(
                      new Header("Content-Type", "application/xml; charset=utf-8")
                    )
                    .withBody(readAndClose(ClientTest.class.getResourceAsStream("/GetCapabilitiesResponse.xml")))
            );
    client.when(
                    request()
                    .withMethod("POST")
                    .withPath("/csw")
            )
            .respond(
                    response()
                    .withStatusCode(200)
                    .withHeaders(
                      new Header("Content-Type", "application/xml; charset=utf-8")
                    )
                    .withBody(readAndClose(ClientTest.class.getResourceAsStream("/GetRecordsResponse.xml")))
            );
    client.when(
                    request()
                    .withMethod("GET")
                    .withQueryStringParameter("request", "GetRecordById")
                    .withQueryStringParameter("service", "CSW")
                    .withPath("/csw")
            )
            .respond(
                    response()
                    .withStatusCode(200)
                    .withHeaders(
                      new Header("Content-Type", "application/xml; charset=utf-8")
                    )
                    .withBody(readAndClose(ClientTest.class.getResourceAsStream("/GetRecordByIdResponse.xml")))
            );
  }
  
  @AfterClass
  public static void after()  {
    server.stop();
    client.stop();
  }

  @Test
  public void testGetRecords() throws Exception {
    ProfilesService profilesService = new ProfilesService(null);
    profilesService.initialize();
    
    IProfiles profiles = profilesService.newProfiles();
    IProfile defaultProfile = profiles.getDefaultProfile();
    assertNotNull("No default profile", defaultProfile);
    
    Bots bots = BotsUtils.readBots("http://localhost:5000/robots.txt");
    BotsHttpClient httpClient = new BotsHttpClient(bots);
    
    Client cswClient = new Client(profilesService, httpClient, new URL("http://localhost:5000/csw"), defaultProfile, null);
    IRecords records = cswClient.findRecords(1, 10, null, null);
    
    assertNotNull("No records", records);
  }
  
  @Test
  public void testReadMetadata() throws Exception {
    ProfilesService profilesService = new ProfilesService(null);
    profilesService.initialize();
    
    IProfiles profiles = profilesService.newProfiles();
    IProfile defaultProfile = profiles.getDefaultProfile();
    assertNotNull("No default profile", defaultProfile);
    
    Bots bots = BotsUtils.readBots("http://localhost:5000/robots.txt");
    BotsHttpClient httpClient = new BotsHttpClient(bots);
    
    Client cswClient = new Client(profilesService, httpClient, new URL("http://localhost:5000/csw"), defaultProfile, null);
    String metadata = cswClient.readMetadata("{093CBDB1-9D7A-4602-9937-2EC89359E633}");
    
    assertNotNull("No metadata", metadata);
    assertNotEquals("No metadata", metadata.length(), 0);
  }

  private static String readAndClose(InputStream in) throws IOException {
    try (InputStream input=in) {
      return IOUtils.toString(input, "UTF-8");
    }
  }
  
}
