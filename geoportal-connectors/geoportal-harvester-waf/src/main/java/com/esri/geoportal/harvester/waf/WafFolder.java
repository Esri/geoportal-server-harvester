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
package com.esri.geoportal.harvester.waf;

import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WAF folder.
 */
/*package*/ class WafFolder {
  private static final Logger LOG = LoggerFactory.getLogger(WafFolder.class);
  private static final FileSystem fileSystem = FileSystems.getDefault();
  private static final String DEFAULT_MATCH_PATTERN = "**.xml";

  private final WafBroker broker;
  private final URL folderUrl;
  private final String matchPattern;
  private final SimpleCredentials creds;

  /**
   * Creates instance of the folder.
   *
   * @param broker broker
   * @param folderUrl folder URL
   * @param creds credentials
   */
  public WafFolder(WafBroker broker, URL folderUrl, String matchPattern, SimpleCredentials creds) {
    this.broker = broker;
    this.folderUrl = folderUrl;
    this.matchPattern = StringUtils.defaultIfBlank(matchPattern, DEFAULT_MATCH_PATTERN);
    this.creds = creds;
  }

  /**
   * Reads content of the folder.
   * @param httpClient HTTP client
   * @return content
   * @throws IOException if error reading content
   * @throws URISyntaxException if invalid URL
   */
  public WafFolderContent readContent(CloseableHttpClient httpClient) throws IOException, URISyntaxException {
    List<WafFile> files = new ArrayList<>();
    List<WafFolder> subFolders = new ArrayList<>();
    
    HtmlUrlScrapper scrapper = new HtmlUrlScrapper(httpClient, creds);
    
    try {
      List<URL> urls = scrapper.scrap(folderUrl);

      urls.forEach(u -> {
        if (u.toExternalForm().endsWith("/") || !cutOff(u.toExternalForm(),"/").contains(".")) {
          subFolders.add(new WafFolder(broker, u, matchPattern, creds));
        } else if (multiMatchUrl(u,matchPattern)) {
          files.add(new WafFile(broker, u, creds));
        }
      });
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode()!=403) {
        throw ex;
      }
    }
    
    LOG.debug(formatForLog("WAF FILES in %s: %s",folderUrl,files.toString()));
    LOG.debug(formatForLog("WAF SUBFOLDERS in %s: %s",folderUrl,subFolders.toString()));

    return new WafFolderContent(this, subFolders, files);
  }

  /**
   * Gets folder URL.
   * @return folder URL
   */
  public URL getFolderUrl() {
    return folderUrl;
  }

  @Override
  public String toString() {
    return folderUrl.toString();
  }
  
  /**
   * Cuts last part by string.
   * @param s input string
   * @param cut cut string
   * @return result
   */
  private String cutOff(String s, String cut) {
    int lastIndex = s.lastIndexOf(cut);
    return lastIndex>=0? s.substring(lastIndex+1): s;
  }
  
  /**
   * Matches file
   * @param file file
   * @param patterns comma separated match patterns (glob)
   * @return <code>true</code> if URL matches the pattern
   */
  private boolean multiMatchUrl(URL u, String patterns) {
    return Arrays.stream(patterns.split(","))
            .map(pattern -> StringUtils.trimToEmpty(pattern))
            .anyMatch(pattern -> this.matchUrl(u, pattern));
  }
  
  /**
   * Matches URL
   * @param u url
   * @param pattern match patter (glob)
   * @return <code>true</code> if URL matches the pattern
   */
  private boolean matchUrl(URL u, String pattern) {
    String[] split = u.getPath().split("(/|\\\\)+");
    List<String> items = Arrays.asList(split).stream().filter(s->s!=null && !s.isEmpty()).collect(Collectors.toList());
    if (!items.isEmpty()) {
      String first = items.get(0);
      List<String> subList = items.subList(1, items.size());
      Path path = fileSystem.getPath(first, subList.toArray(new String[]{}));
      PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:"+pattern);
      return pathMatcher.matches(path);
    } else {
      return false;
    }
  }
}
