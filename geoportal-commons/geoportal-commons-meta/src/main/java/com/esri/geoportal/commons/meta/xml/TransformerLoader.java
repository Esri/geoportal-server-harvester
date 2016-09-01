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
package com.esri.geoportal.commons.meta.xml;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Transformer loader.
 */
/*package*/ class TransformerLoader {

  /**
   * Load transformer from the classpath.
   * @param sourceName source file name
   * @return template
   * @throws IOException if loading source file fails
   * @throws TransformerConfigurationException if parsing source file fails
   */
  public static Templates loadTransformer(String sourceName) throws IOException, TransformerConfigurationException {
    try (final InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(sourceName)) {
      TransformerFactory transFact = TransformerFactory.newInstance();
      Source source = new StreamSource(input);
      return transFact.newTemplates(source);
    }
  }
}
