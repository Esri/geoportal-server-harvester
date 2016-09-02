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

import java.util.HashMap;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

/**
 * Namespace context impl.
 */
public class NamespaceContextImpl implements NamespaceContext {
  
  private final HashMap<String,String> prefs = new HashMap<>();
  private final HashMap<String,String> uris = new HashMap<>();
  
  /**
   * Creates instance of the context.
   * @param data prefix/uri pairs
   */
  public NamespaceContextImpl(String...data) {
    for (int i=0; i<data.length-1; i+=2) {
      String pref = data[i];
      String uri = data[i+1];
      prefs.put(pref, uri);
      uris.put(uri, pref);
    }
  }

  @Override
  public String getNamespaceURI(String prefix) {
    return prefs.get(prefix);
  }

  @Override
  public String getPrefix(String namespaceURI) {
    return uris.get(namespaceURI);
  }

  @Override
  public Iterator getPrefixes(String namespaceURI) {
    return prefs.keySet().iterator();
  }
  
}
