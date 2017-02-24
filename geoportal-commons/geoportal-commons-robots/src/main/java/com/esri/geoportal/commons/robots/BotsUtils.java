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

import java.net.URL;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Robots txt utility class/shortcut methods.
 * @see com.esri.geoportal.commons.robots API
 */
public final class BotsUtils {
  private static final MatchingStrategy DEFAULT_MATCHING_STRATEGY = MatchingStrategy.SIMPLE_PATTERN_STRATEGY;
  private static final WinningStrategy DEFAULT_WINNIG_STRATEGY = WinningStrategy.LONGEST_PATH_STRATEGY;
  
  /**
   * Gets default parser
   * @return default parser (never <code>null</code>)
   */
  public static BotsParser parser() {
    return parser(BotsConfig.DEFAULT,HttpClientBuilder.create().build());
  }
  
  /**
   * Gets default parser
   * @param botsConfig bots config
   * @param httpClient http client
   * @return default parser (never <code>null</code>)
   */
  public static BotsParser parser(BotsConfig botsConfig,HttpClient httpClient) {
    return BotsParser.getInstance(botsConfig,httpClient);
  }
  
  /**
   * Reads robots.txt
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, String serverUrl) {
    return parser().readRobotsTxt(matchingStrategy, winningStrategy, serverUrl);
  }
  
  /**
   * Reads robots.txt
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, URL serverUrl) {
    return parser().readRobotsTxt(matchingStrategy, winningStrategy, serverUrl);
  }
  
  /**
   * Reads robots.txt
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(String serverUrl) {
    return parser().readRobotsTxt(DEFAULT_MATCHING_STRATEGY, DEFAULT_WINNIG_STRATEGY, serverUrl);
  }
  
  /**
   * Reads robots.txt
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(URL serverUrl) {
    return parser().readRobotsTxt(DEFAULT_MATCHING_STRATEGY, DEFAULT_WINNIG_STRATEGY, serverUrl);
  }

  
  /**
   * Reads robots.txt
   * @param botsConfig bots config
   * @param httpClient http client
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(BotsConfig botsConfig, HttpClient httpClient, String serverUrl) {
    return parser(botsConfig,httpClient).readRobotsTxt(DEFAULT_MATCHING_STRATEGY, DEFAULT_WINNIG_STRATEGY, serverUrl);
  }
  
  /**
   * Reads robots.txt
   * @param botsConfig bots config
   * @param httpClient http client
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(BotsConfig botsConfig, HttpClient httpClient, URL serverUrl) {
    return parser(botsConfig,httpClient).readRobotsTxt(DEFAULT_MATCHING_STRATEGY, DEFAULT_WINNIG_STRATEGY, serverUrl);
  }
  
  /**
   * Request access to the resource.
   * @param bots robots
   * @param path relative path to the resource
   * @return access (never <code>null</code>
   */
  public static Access requestAccess(Bots bots, String path) {
    if (bots!=null) {
      List<Access> matching = bots.select(path, bots.getMatchingStrategy());
      Access winner = bots.getWinningStrategy().selectWinner(matching);
      if (winner!=null) {
        return winner;
      }
    }
    return Access.ALLOW;
  }
}
