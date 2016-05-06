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
package com.esri.geoportal.harvester.waf;

import com.esri.geoportal.harvester.api.BrokerDefinition;
import com.esri.geoportal.harvester.api.base.BotsBrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.BotsBrokerDefinitionAdaptor;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;

/**
 * WAF broker definition adaptor.
 */
public class WafBrokerDefinitionAdaptor extends BotsBrokerDefinitionAdaptor {
  public static final String P_HOST_URL    = "waf-host-url";
  
  private URL hostUrl;

  public WafBrokerDefinitionAdaptor(BrokerDefinition def) throws IllegalArgumentException {
    super(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(WafConnector.TYPE);
    } else if (!WafConnector.TYPE.equals(def.getType())) {
      throw new IllegalArgumentException("Broker definition doesn't match");
    } else {
      try {
        hostUrl = new URL(get(P_HOST_URL));
      } catch (MalformedURLException ex) {
        throw new IllegalArgumentException(String.format("Invalid %s: %s", P_HOST_URL,get(P_HOST_URL)), ex);
      }
    }
  }
  
  public URL getHostUrl() {
    return hostUrl;
  }
  
  public void setHostUrl(URL url) {
    this.hostUrl = url;
    set(P_HOST_URL, url.toExternalForm());
  }
}
