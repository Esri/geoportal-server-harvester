/*
 * Copyright 2016 Piotr Andzel.
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
package com.esri.geoportal.commons.csw.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Templates manager. Loads and buffers XSLT templates.
 */
/*package*/ class TemplatesManager {
  private static TemplatesManager instance = new TemplatesManager();
  
  private Map<String,Templates> cache = new TreeMap<>();
  
  /**
   * Gets singleton instance.
   * @return templates manager.
   */
  public static TemplatesManager getInstance() {
    return instance;
  }
  
  /**
   * Gets template by path.
   * @param path template path
   * @return template
   */
  public Templates getTemplate(String path) throws IOException, TransformerConfigurationException {
    Templates template = cache.get(path);
    if (template==null) {
      try (InputStream xsltStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        template = transformerFactory.newTemplates(new StreamSource(xsltStream));
        cache.put(path, template);
      }
    }
    return template;
  }
}
