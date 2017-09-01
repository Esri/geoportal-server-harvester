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
package com.esri.geoportal.commons.meta.js;

import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.script.Invocable;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.esri.geoportal.commons.utils.XmlUtils;

/**
 * Base JavaScript metadata builder.
 */
public class BaseJSMetaBuilder implements MetaBuilder {
  private static final ScriptEngines engines = new ScriptEngines();

  private final String javascriptPath;
  
  /**
   * Creates instance of the builder.
   * @param javascriptPath javascript file path
   */
  public BaseJSMetaBuilder(String javascriptPath) {
    this.javascriptPath = javascriptPath;
  }

  @Override
  public Document create(MapAttribute wellKnowsAttributes) throws MetaException {
    try {
      String xmlString = execute(wellKnowsAttributes).toString();
      return XmlUtils.toDocument(xmlString);
    } catch (IOException|ParserConfigurationException|SAXException ex) {
      throw new MetaException("Error creating document.", ex);
    }
  }
  
  private Object execute(MapAttribute wellKnowsAttributes) throws MetaException {
    try {
      SimpleBindings bindings = new SimpleBindings();
      bindings.put("attributes", wellKnowsAttributes);
      Invocable invocable = (Invocable)engines.getCachedEngine(javascriptPath);
      return invocable.invokeFunction("create", wellKnowsAttributes);
    } catch (ScriptException|NoSuchMethodException|IOException|URISyntaxException ex) {
      throw new MetaException("Error executing script.", ex);
    }
  }
}
