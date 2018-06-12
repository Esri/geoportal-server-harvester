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
import java.util.Set;

/**
 * Data content.
 */
public interface DataContent {

  /**
   * Gets content.
   * @param mimeType required mime type
   * @return content or <code>nulle</code> if content by the mime type unavailable
   * @throws IOException if getting content fails
   */
  byte[] getContent(MimeType... mimeType) throws IOException;

  /**
   * Gets content type.
   * @return content type variances
   */
  Set<MimeType> getContentType();
}
