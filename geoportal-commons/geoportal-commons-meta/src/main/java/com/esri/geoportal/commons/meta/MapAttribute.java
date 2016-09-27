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
package com.esri.geoportal.commons.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Object attribute.
 */
public class MapAttribute implements Attribute {
  private final Map<String,Attribute> attributes;

  public MapAttribute(Map<String, Attribute> attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean isMap() {
    return true;
  }

  @Override
  public Map<String, Attribute> getNamedAttributes() {
    return attributes;
  }

  @Override
  public Map<String, String> flatten(String prefix) {
    HashMap<String,String> flat = new HashMap<>();
    attributes.entrySet().forEach(entry->{
      Map<String, String> f = entry.getValue().flatten(prefix!=null? String.format("%s.%s", prefix, entry.getKey()): String.format("%s", entry.getKey()));
      flat.putAll(f);
    });
    return flat;
  }
  
  @Override
  public String toString() {
    return String.format("{ %s }", attributes.entrySet().stream().map(e->String.format("%s: %s", e.getKey(), e.getValue())).collect(Collectors.joining(", ")));
  }
}
