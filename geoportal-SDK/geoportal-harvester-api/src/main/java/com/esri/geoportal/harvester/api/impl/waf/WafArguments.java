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
package com.esri.geoportal.harvester.api.impl.waf;

import com.esri.geoportal.harvester.api.n.BrokerDefinition;
import com.esri.geoportal.harvester.api.n.InvalidDefinitionException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Piotr Andzel
 */
public class WafArguments {
  public static final String ARG_URL = "waf.url";
  private final BrokerDefinition def;
  private URL url;

  public WafArguments(BrokerDefinition def) throws InvalidDefinitionException {
    this.def = def;
    try {
      url = new URL(def.get(ARG_URL));
    } catch (MalformedURLException|NullPointerException ex) {
      throw new InvalidDefinitionException("Invalid URL", ex);
    }
  }
  
  public URL getUrl() {
    return url;
  }
  
  public void setUrl(URL url) {
    this.url = url;
    def.put(ARG_URL, url.toExternalForm());
  }
}
