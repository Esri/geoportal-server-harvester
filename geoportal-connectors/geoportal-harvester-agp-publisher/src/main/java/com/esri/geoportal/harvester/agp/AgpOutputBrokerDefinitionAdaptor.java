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
package com.esri.geoportal.harvester.agp;

import static com.esri.geoportal.harvester.agp.AgpConstants.*;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.CredentialsDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * ArcGIS Portal definition adaptor.
 */
/*package*/ class AgpOutputBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {

  private static final Integer DEFAULT_MAX_REDIRECTS = 5;

  private final CredentialsDefinitionAdaptor credAdaptor;

  private URL hostUrl;
  private String folderId;
  private boolean cleanup;
  private Integer maxRedirects;
  private boolean uploadFiles;
  private boolean markdown2html;
  private String oAuthToken;

  
  /**
   * Creates instance of the adaptor.
   *
   * @param def broker definition
   * @throws IllegalArgumentException if invalid broker definition
   */
  public AgpOutputBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    this.credAdaptor = new CredentialsDefinitionAdaptor(def);
    if (credAdaptor.getCredentials().isEmpty()) {
      throw new InvalidDefinitionException("Empty credentials");
    }
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(AgpOutputConnector.TYPE);
    } else if (!AgpOutputConnector.TYPE.equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      try {
        hostUrl = new URL(get(P_HOST_URL));
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL, get(P_HOST_URL)), ex);
      }
      folderId = get(P_FOLDER_ID);
      cleanup = Boolean.parseBoolean(get(P_FOLDER_CLEANUP));
      maxRedirects = NumberUtils.toInt(get(P_MAX_REDIRECTS), DEFAULT_MAX_REDIRECTS);
      uploadFiles = Boolean.parseBoolean(get(P_UPLOAD));
      markdown2html = Boolean.parseBoolean(get(P_MARKDOWN2HTML));
      oAuthToken = get(P_TOKEN);
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params, P_HOST_URL);
    consume(params, P_FOLDER_ID);
    consume(params, P_FOLDER_CLEANUP);
    consume(params, P_MAX_REDIRECTS);
    consume(params, P_UPLOAD);
    consume(params, P_MARKDOWN2HTML);
    consume(params, P_TOKEN);
    credAdaptor.override(params);
  }

  /**
   * Gets host URL.
   *
   * @return host URL
   */
  public URL getHostUrl() {
    return hostUrl;
  }

  /**
   * Sets host URL
   *
   * @param url host URL
   */
  public void setHostUrl(URL url) {
    this.hostUrl = url;
    set(P_HOST_URL, url.toExternalForm());
  }

  /**
   * Gets folder id.
   *
   * @return folder id
   */
  public String getFolderId() {
    return folderId;
  }

  /**
   * Sets folder id.
   *
   * @param folderId folder id
   */
  public void setFolderId(String folderId) {
    this.folderId = folderId;
    set(P_FOLDER_ID, folderId);
  }

  /**
   * Gets credentials.
   *
   * @return credentials
   */
  public SimpleCredentials getCredentials() {
    return credAdaptor.getCredentials();
  }

  /**
   * Sets credentials.
   *
   * @param cred credentials
   */
  public void setCredentials(SimpleCredentials cred) {
    credAdaptor.setCredentials(cred);
  }

  /**
   * Gets permission to cleanup.
   *
   * @return <code>true</code> if cleanup permitted
   */
  public boolean getCleanup() {
    return cleanup;
  }

  /**
   * Sets permission to cleanup.
   *
   * @param cleanup <code>true</code> to permit cleanup
   */
  public void setCleanup(boolean cleanup) {
    this.cleanup = cleanup;
    set(P_FOLDER_CLEANUP, Boolean.toString(cleanup));
  }

  /**
   * Gets maximum of redirects.
   * @return the maxRedirects
   */
  public Integer getMaxRedirects() {
    return maxRedirects;
  }

  /**
   * Sets maximum of redirects.
   * @param maxRedirects the maxRedirects to set
   */
  public void setMaxRedirects(Integer maxRedirects) {
    this.maxRedirects = maxRedirects;
    set(P_MAX_REDIRECTS, Integer.toString(maxRedirects));
  }

  public boolean isUploadFiles() {
    return uploadFiles;
  }

  public void setUploadFiles(boolean uploadFiles) {
    this.uploadFiles = uploadFiles;
    set(P_UPLOAD, Boolean.toString(uploadFiles));
  }

  public boolean isMarkdown2HTML() {
      return markdown2html;
  }
  
  public void setMarkdown2HTML(boolean markdown2html) {
      this.markdown2html = markdown2html;
      set(P_MARKDOWN2HTML, Boolean.toString(markdown2html));        
  }
  
  public String getOAuthToken()
  {
      return oAuthToken;
  }
  public void setOAuthToken(String oAuthToken) {
      this.oAuthToken = oAuthToken;
      set(P_TOKEN, oAuthToken);        
  }
}
