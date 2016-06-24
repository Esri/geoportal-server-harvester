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

import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WAF folder.
 */
/*package*/ class WafFolder {
  private static final Logger LOG = LoggerFactory.getLogger(WafFolder.class);

  private final WafBroker broker;
  private final URL folderUrl;
  private final SimpleCredentials creds;

  /**
   * Creates instance of the folder.
   *
   * @param broker broker
   * @param folderUrl folder URL
   * @param creds credentials
   */
  public WafFolder(WafBroker broker, URL folderUrl, SimpleCredentials creds) {
    this.broker = broker;
    this.folderUrl = folderUrl;
    this.creds = creds;
  }

  /**
   * Reads content of the folder.
   * @param httpClient HTTP client
   * @return content
   * @throws IOException if error reading content
   * @throws URISyntaxException if invalid URL
   */
  public WafFolderContent readContent(BotsHttpClient httpClient) throws IOException, URISyntaxException {
    HtmlUrlScrapper scrapper = new HtmlUrlScrapper(httpClient, creds);
    List<URL> urls = scrapper.scrap(folderUrl);

    List<WafFile> files = new ArrayList<>();
    List<WafFolder> subFolders = new ArrayList<>();

    urls.forEach(u -> {
      if (u.toExternalForm().toLowerCase().endsWith(".xml")) {
        files.add(new WafFile(broker, u, creds));
      } else {
        subFolders.add(new WafFolder(broker, u, creds));
      }
    });

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
}
