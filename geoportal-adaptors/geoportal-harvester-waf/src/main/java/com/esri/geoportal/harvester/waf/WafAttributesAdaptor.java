/*
 * Copyright 2016 Esri, Inc..
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

import com.esri.geoportal.harvester.api.support.BotsAttributesAdaptor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * WAF attributes adaptor.
 */
public class WafAttributesAdaptor extends BotsAttributesAdaptor {
  public static final String P_HOST_URL    = "waf.host.url";

  private URL hostUrl;
  
  /**
   * Creates instance of the adaptor.
   * @param attributes attributes
   */
  public WafAttributesAdaptor(Map<String, String> attributes) {
    super(attributes);
    try {
      this.hostUrl = new URL(attributes.get(P_HOST_URL));
    } catch(MalformedURLException ex) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_HOST_URL, attributes.get(P_HOST_URL)));
    }
  }
  
  /**
   * Creates empty instance of the adaptor.
   */
  public WafAttributesAdaptor() {
    super(new HashMap<>());
  }

  /**
   * Sets host url.
   * @param hostUrl host url 
   */
  public void setHostUrl(URL hostUrl) {
    if (hostUrl==null) {
      throw new IllegalArgumentException(String.format("Invalid %s (%s)", P_HOST_URL, hostUrl));
    }
    this.hostUrl = hostUrl;
    attributes.put(P_HOST_URL, hostUrl.toExternalForm());
  }

  /**
   * Gets host url.
   * @return host url
   */
  public URL getHostUrl() {
    return hostUrl;
  }
  
  @Override
  public String toString() {
    return String.format("WAF :: url: %s", hostUrl);
  }
}
