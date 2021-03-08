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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
  private static final String DEFAULT_MATCH_PATTERN = "";

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
    Map<String, WafFile> files = new TreeMap<>(String.CASE_INSENSITIVE_ORDER) {
      @Override
      public WafFile put(String key, WafFile value) {
        // The code below is intended to identify descriptors for binary files,
        // for example if the binary file is some_imgae.jpg then some_image.jpg.xml would be
        // a descriptor for the binary files. If the binary file is found it supersedes binary
        // file and will be published instead
        if (key.toLowerCase().endsWith(".xml")) {
          String orgPath = key.replaceAll("\\.[xX][mM][lL]$", "");
          this.remove(orgPath);
          return super.put(key, value);
        } else {
          String xmlPath = key + ".xml";
          if (!this.containsKey(xmlPath)) {
            return super.put(key, value);
          } else {
            return null;
          }
        }
      }
    };
    List<WafFolder> subFolders = new ArrayList<>();
    
    HtmlUrlScrapper scrapper = new HtmlUrlScrapper(httpClient, creds);
    
    try {
      List<URL> urls = scrapper.scrap(folderUrl);
      for (URL u: urls) {
        if (Thread.currentThread().isInterrupted()) {
          return new WafFolderContent(this, Collections.emptyList(), Collections.emptyList());
        }
        if (u.toExternalForm().endsWith("/") || !cutOff(u.toExternalForm(),"/").contains(".")) {
          subFolders.add(new WafFolder(broker, u, matchPattern, creds));
        } else if (StringUtils.isBlank(matchPattern) || multiMatchUrl(u,matchPattern)) {
          files.put(u.toExternalForm(), new WafFile(broker, u, creds));
        }
      }
    } catch (HttpResponseException ex) {
      if (ex.getStatusCode()!=403) {
        throw ex;
      }
    }
    
    LOG.debug(formatForLog("WAF FILES in %s: %s",folderUrl,files.values().toString()));
    LOG.debug(formatForLog("WAF SUBFOLDERS in %s: %s",folderUrl,subFolders.toString()));

    return new WafFolderContent(this, subFolders, files.values().stream().collect(Collectors.toList()));
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
      PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:"+pattern);
      Path path = fileSystem.getPath(first, subList.toArray(new String[]{}));
      // noXmlPath is a path to the file with potiential '.xm' suffix being removed
      // this is to assure that binary file descriptors are still being matched, for example:
      // if the pattern is **.jpg, then both 'some_image.jpg' and 'some_image.jpg.xml' will match.
      Path noXmlPath = Path.of(path.toString().replaceAll("\\.[xX][mM][lL]$", ""));
      return pathMatcher.matches(path) || pathMatcher.matches(noXmlPath);
    } else {
      return false;
    }
  }
}
