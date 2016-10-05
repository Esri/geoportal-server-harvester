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
package com.esri.geoportal.commons.utils;

import org.apache.http.client.config.RequestConfig;

/**
 * Various constants used by harvester.
 */
public class Constants {
  /** default connection timeout (120000 milliseconds) */
  public static final int CONNECTION_TIMEOUT = 120000;
  /** default socket timeout (60000 milliseconds) */
  public static final int SOCKET_TIMEOUT = 60000;
  
  /**
   * HTTP Request configuration with timeouts.
   */
  public static final RequestConfig DEFAULT_REQUEST_CONFIG = 
          org.apache.http.client.config.RequestConfig.custom()
          .setConnectTimeout(CONNECTION_TIMEOUT)
          .setSocketTimeout(SOCKET_TIMEOUT).build();
}
