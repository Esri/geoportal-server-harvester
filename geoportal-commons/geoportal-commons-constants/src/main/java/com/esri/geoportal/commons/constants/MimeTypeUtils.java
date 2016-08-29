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
package com.esri.geoportal.commons.constants;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mime type utils.
 */
public class MimeTypeUtils {
  private static final Logger LOG = Logger.getLogger(MimeTypeUtils.class.getCanonicalName());
  private static final String mappingFile = "mime.types";
  private static final Map<String,MimeType> mapping = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  static {
    loadMapping();
  }
  
  /**
   * Maps extension to the mime type
   * @param ext extension
   * @return mime type or <code>null</code> if no mapping found for the extension
   */
  public static MimeType mapExtension(String ext) {
    return mapping.get(ext);
  }
  
  private static void loadMapping() {
    try (InputStream mappingStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(mappingFile);) {
      Properties props=new Properties();
      props.load(mappingStream);
      
      props.entrySet().stream().forEach(e->{
        MimeType mimeType = MimeType.parse(e.getKey().toString());
        if (mimeType!=null) {
          Arrays.asList(e.getValue().toString().split(", ")).stream().forEach(ext->{
            if (!mapping.containsKey(ext)) {
              mapping.put(ext, mimeType);
            }
          });
        }
      });
      
    } catch (Exception ex) {
      LOG.log(Level.SEVERE, String.format("Unable to load MimeType's extensions."), ex);
    }
  }
}
