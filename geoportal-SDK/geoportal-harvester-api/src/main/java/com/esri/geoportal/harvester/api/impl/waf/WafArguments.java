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
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Piotr Andzel
 */
public class WafArguments {
  private final BrokerDefinition def;

  public WafArguments(BrokerDefinition def) {
    this.def = def;
  }
  
  public URL getUrl() {
    try {
      return new URL(def.get("url"));
    } catch (MalformedURLException|NullPointerException ex) {
      return null;
    }
  }
  
  public void setUrl(URL url) {
    def.put("url", url.toExternalForm());
  }
}
