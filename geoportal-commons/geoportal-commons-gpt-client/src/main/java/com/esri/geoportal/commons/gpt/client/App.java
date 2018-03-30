/*
 * Copyright 2018 pete5162.
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
package com.esri.geoportal.commons.gpt.client;

import com.esri.geoportal.commons.utils.SimpleCredentials;
import java.net.URL;
import java.util.List;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author pete5162
 */
public class App {
  public static void main(String[] args) throws Exception {
    URL gptUrl = new URL("http://localhost:8090/geoportal/");
    SimpleCredentials creds = new SimpleCredentials("gptadmin", "gptadmin");
    
    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build(); Client client = new Client(httpClient, gptUrl, creds, null);) {
      List<String> listIds = client.listIds();
      System.out.println(listIds);
    }
  }
}
