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
package com.esri.geoportal.harvester.gpt;

import static com.esri.geoportal.harvester.gpt.GptConstants.*;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.CredentialsDefinitionAdaptor;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * GPT broker definition adaptor.
 */
/*package*/ class GptBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {

  private final CredentialsDefinitionAdaptor credAdaptor;
  private URL hostUrl;
  private boolean forceAdd;
  private boolean cleanup;
  private String index;
  private boolean emitXml = true;
  private boolean emitJson = false;
  private boolean translatePdf = true;
  private boolean editable = false;
  private String collections = "";

  /**
   * Creates instance of the adaptor.
   * @param def broker definition
   */
  public GptBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    this.credAdaptor = new CredentialsDefinitionAdaptor(def);
    if (credAdaptor.getCredentials().isEmpty()) {
      throw new InvalidDefinitionException("Empty credentials");
    }
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(GptConnector.TYPE);
    } else if (!GptConnector.TYPE.equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      try {
        String sHostUrl = get(P_HOST_URL);
        if (sHostUrl!=null) {
          sHostUrl = sHostUrl.replaceAll("/*$", "/");
        }
        hostUrl = new URL(sHostUrl);
      } catch (MalformedURLException ex) {
        throw new InvalidDefinitionException(String.format("Invalid %s: %s", P_HOST_URL,get(P_HOST_URL)), ex);
      }
      forceAdd = Boolean.parseBoolean(get(P_FORCE_ADD));
      cleanup  = Boolean.parseBoolean(get(P_CLEANUP));
      index  = get(P_INDEX);
      emitXml = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(get(P_ACCEPT_XML)), true);
      emitJson = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(get(P_ACCEPT_JSON)), false);
      translatePdf = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(get(P_TRANSLATE_PDF)), true);
      editable = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(get(P_EDITABLE)), false);
      collections = StringUtils.defaultIfBlank(get(P_COLLECTIONS), "");
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,P_HOST_URL);
    consume(params,P_FORCE_ADD);
    consume(params,P_CLEANUP);
    consume(params,P_INDEX);
    consume(params,P_ACCEPT_XML);
    consume(params,P_ACCEPT_JSON);
    consume(params,P_TRANSLATE_PDF);
    consume(params,P_EDITABLE);
    consume(params,P_COLLECTIONS);
    credAdaptor.override(params);
  }

  /**
   * Gets host URL.
   * @return host URL
   */
  public URL getHostUrl() {
    return hostUrl;
  }

  /**
   * Sets host URL.
   * @param url host URL
   */
  public void setHostUrl(URL url) {
    this.hostUrl = url;
    set(P_HOST_URL, url.toExternalForm());
  }

  /**
   * Gets credentials.
   * @return credentials
   */
  public SimpleCredentials getCredentials() {
    return credAdaptor.getCredentials();
  }

  /**
   * Sets credentials.
   * @param cred credentials
   */
  public void setCredentials(SimpleCredentials cred) {
    credAdaptor.setCredentials(cred);
  }
  
  /**
   * Gets if to force add.
   * @return <code>true</code> if to force add
   */
  public boolean getForceAdd() {
    return forceAdd;
  }
  
  /**
   * Sets to force add flag.
   * @param forceAdd <code>true</code> if to force add
   */
  public void setForceAdd(boolean forceAdd) {
    this.forceAdd = forceAdd;
    set(P_FORCE_ADD, Boolean.toString(forceAdd));
  }

  /**
   * Gets permission to cleanup.
   * @return <code>true</code> if cleanup permitted
   */
  public boolean getCleanup() {
    return cleanup;
  }

  /**
   * Sets permission to cleanup.
   * @param cleanup <code>true</code> to permit cleanup
   */
  public void setCleanup(boolean cleanup) {
    this.cleanup = cleanup;
    set(P_CLEANUP, Boolean.toString(cleanup));
  }

  /**
   * Gets index.
   * @return index
   */
  public String getIndex() {
    return index;
  }

  /**
   * Sets index.
   * @param index index
   */
  public void setIndex(String index) {
    this.index = index;
    set(P_INDEX, index);
  }

  public boolean getAcceptXml() {
    return emitXml;
  }

  public void setEmitXml(boolean emitXml) {
    this.emitXml = emitXml;
    set(P_ACCEPT_XML, BooleanUtils.toStringTrueFalse(emitXml));
  }

  public boolean getAcceptJson() {
    return emitJson;
  }

  public void setEmitJson(boolean emitJson) {
    this.emitJson = emitJson;
    set(P_ACCEPT_JSON, BooleanUtils.toStringTrueFalse(emitJson));
  }

  /**
   * @return the translatePdf
   */
  public boolean isTranslatePdf() {
    return translatePdf;
  }

  /**
   * @param translatePdf the translatePdf to set
   */
  public void setTranslatePdf(boolean translatePdf) {
    this.translatePdf = translatePdf;
    set(P_TRANSLATE_PDF, BooleanUtils.toStringTrueFalse(translatePdf));
  }

  /**
   * @return the editable
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * @param editable the editable to set
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
    set(P_EDITABLE, BooleanUtils.toStringTrueFalse(editable));
  }

  /**
   * Gets collections
   * @return the collections
   */
  public String getCollections() {
    return collections;
  }
  
  public String [] getCollectionsAsArray() {
    if (collections==null) return null;
    String [] collectionsArray = collections.split(",");
    if (collectionsArray==null || collectionsArray.length==0) return null;
    String[] finalCollections = Arrays.stream(collectionsArray)
      .map(StringUtils::trimToNull)
      .filter(collection -> collections!=null)
      .toArray(String[]::new);
    if (finalCollections==null || finalCollections.length==0) return null;
    return finalCollections;
  }

  /**
   * Sets collections
   * @param collections the editable to set
   */
  public void setCollections(String collections) {
    this.collections = StringUtils.defaultIfBlank(collections, "");
    set(P_COLLECTIONS, StringUtils.defaultIfBlank(collections, ""));
  }
}
