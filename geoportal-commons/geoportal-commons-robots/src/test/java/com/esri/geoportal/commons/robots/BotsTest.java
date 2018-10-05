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
package com.esri.geoportal.commons.robots;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Bots test.
 */
public class BotsTest {

  @Rule
  public MockServerRule server = new MockServerRule(this,5000);
  
  private MockServerClient client;

  @Before
  public void setupClass() throws IOException {
    client = new MockServerClient("localhost", 5000);
    client.when(
                    request()
                    .withMethod("GET")
                    .withPath("/robots.txt")
            )
            .respond(
                    response()
                    .withStatusCode(200)
                    .withBody(readAndClose(getClass().getResourceAsStream("/robots.txt")))
            );
  }
  
  @After
  public void destroy() {
    client.stop();
  }
  

  @Test
  public void testAccessGranted() throws IOException {
    Bots bots = BotsUtils.readBots("http://localhost:5000/robots.txt");
    Access access = BotsUtils.requestAccess(bots, "http://localhost:5000/data.txt");
    
    assertTrue("Access denied", access.hasAccess());
  }

  @Test
  public void testAccessDenied() throws IOException {
    Bots bots = BotsUtils.readBots("http://localhost:5000/robots.txt");
    Access access = BotsUtils.requestAccess(bots, "http://localhost:5000/tmp/data.txt");
    
    assertFalse("Access granted", access.hasAccess());
  }

  @Test
  public void testAccessInapplicable() throws IOException {
    Bots bots = BotsUtils.readBots("http://localhost:5000/robots.txt");
    Access access = BotsUtils.requestAccess(bots, "http://localhost:5000/test/data.txt");
    
    assertTrue("Access denied", access.hasAccess());
  }

  private String readAndClose(InputStream in) throws IOException {
    try (InputStream input=in) {
      return IOUtils.toString(input, "UTF-8");
    }
  }
  
}
