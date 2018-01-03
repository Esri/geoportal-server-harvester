/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.commons.robots;

import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/**
 * Parser of "robots.txt" file.
 */
public class BotsParser {

  private static final Logger LOG = LoggerFactory.getLogger(BotsParser.class);

  private final BotsConfig botsConfig;
  private final HttpClient httpClient;

  /**
   * Gets default instance.
   *
   * @param botsConfig bots configuration
   * @param httpClient http client
   * @return instance
   */
  public static BotsParser getInstance(BotsConfig botsConfig,HttpClient httpClient) {
      String userAgent = botsConfig.getUserAgent();

      LOG.info(String.format("Creating default RobotsTxtParser :: user-agent: %s", userAgent));

      return new BotsParser(botsConfig,httpClient);
  }

  /**
   * Creates instance of the parser.
   * @param botsConfig bots config
   * @param httpClient http client
   */
  /*package*/BotsParser(BotsConfig botsConfig,HttpClient httpClient) {
    this.botsConfig = botsConfig;
    this.httpClient = httpClient;
  }

  /**
   * Gets user agent.
   *
   * @return user agent
   */
  public String getUserAgent() {
    return botsConfig.getUserAgent();
  }

  /**
   * Parses context of the Robots.txt file if available.
   *
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public Bots readRobotsTxt(MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, String serverUrl) {
    if (serverUrl != null) {
      try {
        return BotsParser.this.readRobotsTxt(matchingStrategy, winningStrategy, new URL(serverUrl));
      } catch (MalformedURLException ex) {
        LOG.warn(String.format("Invalid server url: %s", serverUrl), ex);
      }
    }
    return null;
  }

  /**
   * Parses context of the Robots.txt file if available.
   *
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public Bots readRobotsTxt(MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, URL serverUrl) {
    if (serverUrl != null) {
      LOG.info(formatForLog("Accessing robots.txt for: %s", serverUrl.toExternalForm()));
      try {
        URL robotsTxtUrl = getRobotsTxtUrl(serverUrl);
        if (robotsTxtUrl != null) {
          HttpGet method = new HttpGet(robotsTxtUrl.toExternalForm());
          method.setConfig(DEFAULT_REQUEST_CONFIG);
          method.setHeader("User-Agent", botsConfig.getUserAgent());
          HttpResponse response = httpClient.execute(method);
          if (response.getStatusLine().getStatusCode()<300) {
            InputStream responseStream = response.getEntity().getContent();
            Bots robots = BotsParser.this.readRobotsTxt(matchingStrategy, winningStrategy, responseStream);
            if (robots != null) {
              LOG.info(formatForLog("Received Robotx.txt for: %s", serverUrl.toExternalForm()));
            }
            return robots;
          }
        }
      } catch (IOException ex) {
        LOG.debug(formatForLog("Unable to access robots.txt for: %s", serverUrl.toExternalForm()));
        LOG.debug("",ex);
      }
    }
    return null;
  }

  /**
   * Parses robots TXT
   *
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param robotsTxt stream of data
   * @return instance or RobotsTxt or <code>null</code>
   */
  public Bots readRobotsTxt(MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, InputStream robotsTxt) {
    Bots robots = null;

    try (BotsReader reader = new BotsReader(getUserAgent(), matchingStrategy, winningStrategy, robotsTxt);) {
      robots = reader.readRobotsTxt();
    } catch (IOException ex) {
      LOG.warn("Unable to parse robots.txt", ex);
      return null;
    }

    return robots;
  }

  private URL getRobotsTxtUrl(URL baseUrl) {
    try {
      if (baseUrl != null) {
        if (baseUrl.getPort() >= 0) {
          return new URL(String.format("%s://%s:%d/robots.txt", baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort()));
        } else {
          return new URL(String.format("%s://%s/robots.txt", baseUrl.getProtocol(), baseUrl.getHost()));
        }
      }
    } catch (MalformedURLException ex) {
      LOG.warn("Invalid robots.txt url.", ex);
    }
    return null;
  }
}
