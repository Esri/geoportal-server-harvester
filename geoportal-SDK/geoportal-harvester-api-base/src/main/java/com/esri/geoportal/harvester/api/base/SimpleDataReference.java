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
import com.esri.geoportal.harvester.api.mime.MimeType;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;

/**
 * Simple data reference.
 */
public final class SimpleDataReference implements DataReference {
  private final URI brokerUri;
  private final String id;
  private final Date lastModifiedDate;
  private final URI sourceUri;
  private final byte [] content;
  private final MimeType contentType;
  private final HashMap<String,Object> attributesMap = new HashMap<>();

  /**
   * Creates instance of the data reference.
   * @param brokerUri broker URI
   * @param id record id
   * @param lastModifiedDate last modified date
   * @param sourceUri source URI
   * @param content content
   * @param contentType content type
   */
  public SimpleDataReference(URI brokerUri, String id, Date lastModifiedDate, URI sourceUri, byte [] content, MimeType contentType) {
    this.brokerUri = brokerUri;
    this.id = id;
    this.lastModifiedDate = lastModifiedDate;
    this.sourceUri = sourceUri;
    this.content = content;
    this.contentType = contentType;
  }

  @Override
  public URI getBrokerUri() {
    return brokerUri;
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
  public byte [] getContent() {
    return content;
  }

  @Override
  public MimeType getContentType() {
    return contentType;
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
    return String.format("REF %s | %s | %s", id, lastModifiedDate, sourceUri);
  }
}
