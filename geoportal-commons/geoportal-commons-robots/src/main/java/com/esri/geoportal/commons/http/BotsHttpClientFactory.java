/*
 * Copyright 2016 Esri, Inc..
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
package com.esri.geoportal.commons.http;

import com.esri.geoportal.commons.robots.Bots;
import org.apache.http.client.HttpClient;

/**
 * Bots HTTP client factory.
 */
public interface BotsHttpClientFactory {
  /**
   * Creates new instance of the client.
   * @param bots robots.txt
   * @return HTTP client instance
   */
  BotsHttpClient create(Bots bots);
  
  /**
   * Creates new instance of the client.
   * @param client HTTP client
   * @param bots robots.txt
   * @return HTTP client instance
   */
  BotsHttpClient create(HttpClient client, Bots bots);
  
  /**
   * Standard factory
   */
  BotsHttpClientFactory STD = new BotsHttpClientFactory() {
    @Override
    public BotsHttpClient create(Bots bots) {
      return new BotsHttpClient(bots);
    }

    @Override
    public BotsHttpClient create(HttpClient client, Bots bots) {
      return new BotsHttpClient(client, bots);
    }
  };
}
