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
import java.net.URI;
import java.util.Date;

/**
 * Simple data reference.
 */
public class SimpleDataReference implements DataReference {
  private final URI sourceUri;
  private final Date lastModifiedDate;
  private final byte [] content;

  /**
   * Creates instance of the data reference.
   * @param sourceUri source uri
   * @param lastModifiedDate last modified date
   * @param content content
   */
  public SimpleDataReference(URI sourceUri, Date lastModifiedDate, byte [] content) {
    this.sourceUri = sourceUri;
    this.lastModifiedDate = lastModifiedDate;
    this.content = content;
  }

  @Override
  public URI getSourceUri() {
    return sourceUri;
  }

  @Override
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  @Override
  public byte [] getContent() {
    return content;
  }
  
  @Override
  public String toString() {
    return String.format("REF %s (%s)", sourceUri, lastModifiedDate);
  }
}
