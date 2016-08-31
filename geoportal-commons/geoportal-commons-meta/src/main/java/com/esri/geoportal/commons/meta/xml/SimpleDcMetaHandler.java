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
import javax.xml.transform.TransformerConfigurationException;

/**
 * Simple DC meta handler.
 */
public class SimpleDcMetaHandler extends BaseXmlMetaHandler {
  
  /**
   * Creates instance of the handler.
   */
  public SimpleDcMetaHandler() throws IOException, TransformerConfigurationException {
    super("meta/decodedc.xslt","meta/encodedc.xslt");
  }
  
  
}
