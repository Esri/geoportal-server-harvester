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
package com.esri.geoportal.harvester.api;

import com.esri.geoportal.commons.constants.MimeType;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Data reference. It provides access to data itself as well as vital data 
 * information.
 */
public interface DataReference extends Serializable {
  
  /**
   * Gets data record id.
   * @return data record id
   */
  String getId();
  
  /**
   * Gets last modified date.
   * @return last modified date or <code>null</code> if no date information available
   */
  Date getLastModifiedDate();
  
  /**
   * Gets content.
   * @return content
   * @throws IOException if getting content fails
   */
  byte [] getContent() throws IOException;
  
  /**
   * Gets content type.
   * @return content type
   */
  MimeType getContentType();
  
  /**
   * Gets source URI.
   * @return source URI
   */
  URI getSourceUri();
  
  /**
   * Gets broker URI.
   * @return broker URI.
   */
  URI getBrokerUri();
  
  /**
   * Gets broker name if any.
   * @return broker name or <code>null</code> if no broker name
   */
  String getBrokerName();
  
  /**
   * Gets attributes map.
   * @return attributes map
   */
  Map<String,Object> getAttributesMap();
  
  /**
   * Gets origin data reference.
   * @return origin data reference
   */
  DataReference getOriginDataReference();
}
