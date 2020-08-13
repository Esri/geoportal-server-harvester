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

import com.esri.geoportal.commons.constants.MimeType;
import java.net.URL;
import java.util.Date;

/**
 * Content.
 */
public class Content {
  public final String id;
  public final URL url;
  public final Date lastModifiedDate;
  public final MimeType contentType;
  public final byte [] body;

  public Content(String id, URL url, Date lastModifiedDate, MimeType contentType, byte [] body) {
    if (id==null) {
      throw new IllegalArgumentException("Missing id");
    }
    if (url==null) {
      throw new IllegalArgumentException("Missing url");
    }
    this.id = id;
    this.url = url;
    this.lastModifiedDate = lastModifiedDate;
    this.contentType = contentType;
    this.body = body;
  }
  
  
}
