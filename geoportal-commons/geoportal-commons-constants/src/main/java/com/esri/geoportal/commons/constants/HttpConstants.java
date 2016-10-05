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
package com.esri.geoportal.commons.constants;

/**
 * HTTP constants.
 */
public class HttpConstants {
  /** default connection timeout (120000 milliseconds) */
  private static final int CONNECTION_TIMEOUT = 120000;
  /** default socket timeout (60000 milliseconds) */
  private static final int SOCKET_TIMEOUT = 60000;
  /** user agent */
  private static final String USER_AGENT = "GeoportalServer";
  
  /**
   * Gets connection timeout.
   * @return connection timeout
   */
  public static int getConnectionTimeout() {
    return CONNECTION_TIMEOUT;
  }
  
  /**
   * Gets socket timeout.
   * @return socket timeout
   */
  public static int getSocketTimeout() {
    return SOCKET_TIMEOUT;
  }
  
  /**
   * Gets user agent.
   * @return user agent
   */
  public static String getUserAgent() {
    return USER_AGENT;
  }
}
