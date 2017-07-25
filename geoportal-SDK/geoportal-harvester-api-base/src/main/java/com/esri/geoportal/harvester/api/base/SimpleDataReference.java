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
package com.esri.geoportal.harvester.api.base;

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.commons.constants.MimeType;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple data reference.
 */
public final class SimpleDataReference implements DataReference {
  private static final long serialVersionUID = 1L;
  
  // info
  private final URI brokerUri;
  private final String brokerName;
  private final String id;
  private final Date lastModifiedDate;
  private final URI sourceUri;
  
  // data
  private final Map<MimeType,byte []> content = new HashMap<>();
  private final HashMap<String,Object> attributesMap = new HashMap<>();

  /**
   * Creates instance of the data reference.
   * @param brokerUri broker URI
   * @param brokerName broker name
   * @param id record id
   * @param lastModifiedDate last modified date
   * @param sourceUri source URI
   */
  public SimpleDataReference(URI brokerUri, String brokerName, String id, Date lastModifiedDate, URI sourceUri) {
    this.brokerUri = brokerUri;
    this.brokerName = brokerName;
    this.id = id;
    this.lastModifiedDate = lastModifiedDate;
    this.sourceUri = sourceUri;
  }

  /**
   * Adds content of a particular type to the reference.
   * @param mimeType mime type
   * @param content content
   */
  public void addContext(MimeType mimeType, byte [] content) {
    this.content.put(mimeType, content);
  }
  
  @Override
  public URI getBrokerUri() {
    return brokerUri;
  }

  @Override
  public String getBrokerName() {
    return brokerName;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  @Override
  public URI getSourceUri() {
    return sourceUri;
  }

  @Override
  public byte[] getContent(MimeType...mimeType) throws IOException {
    for (MimeType mt: mimeType) {
      byte [] bytes = content.get(mt);
      if (bytes!=null) {
        return bytes;
      }
    }
    return null;
  }

  @Override
  public Set<MimeType> getContentType() {
    return content.keySet();
  }

  @Override
  public HashMap<String, Object> getAttributesMap() {
    return attributesMap;
  }

  @Override
  public DataReference getOriginDataReference() {
    return null;
  }
  
  @Override
  public String toString() {
    return String.format("id: %s, modified: %s, source URI: %s, broker URI: %s", id, lastModifiedDate, sourceUri, brokerUri);
  }
}
