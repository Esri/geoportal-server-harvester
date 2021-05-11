/*
 * Copyright 2020 Esri, Inc.
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
package com.esri.geoportal.commons.thredds.client;

import java.net.URI;
import java.net.URL;

/**
 * Record
 */
public class Record {
  public final URL catalog;
  public final String id;
  public final URI uri;

  /**
   * Creates instance of the record.
   * @param catalog catalog URL
   * @param id id
   * @param uri record URI
   */
  public Record(URL catalog, String id, URI uri) {
    if (catalog==null) {
      throw new IllegalArgumentException("Empty catalog");
    }
    if (id==null) {
      throw new IllegalArgumentException("Empty id");
    }
    if (uri==null) {
      throw new IllegalArgumentException("Empty uri");
    }
    this.catalog = catalog;
    this.id = id;
    this.uri = uri;
  }
  
  
}
