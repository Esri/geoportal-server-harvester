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
package com.esri.geoportal.commons.gpt.client;

import java.net.URI;
import java.util.Date;

/**
 * Entry reference.
 */
public final class EntryRef {
  private final String id;
  private final URI sourceUri;
  private final Date lastModified;

  /**
   * Creates instance of the reference.
   * @param id record id
   * @param sourceUri source URI
   * @param lastModified last modified date
   */
  public EntryRef(String id, URI sourceUri, Date lastModified) {
    this.id = id;
    this.sourceUri = sourceUri;
    this.lastModified = lastModified;
  }

  /**
   * Gets record id.
   * @return record id
   */
  public String getId() {
    return id;
  }

  /**
   * Gets source URI.
   * @return source URI
   */
  public URI getSourceUri() {
    return sourceUri;
  }

  /**
   * Gets last modified date.
   * @return last modified date
   */
  public Date getLastModified() {
    return lastModified;
  }
  
  @Override
  public String toString() {
    return String.format("id: %s, uri: %s, modified: %s", id, sourceUri, lastModified);
  }
}
