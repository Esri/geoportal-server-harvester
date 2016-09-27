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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Array attribute.
 */
public final class ArrayAttribute implements Attribute {
  private final Attribute [] attributes;

  public ArrayAttribute(Attribute[] attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public Attribute[] getAttributes() {
    return attributes;
  }

  @Override
  public Map<String, String> flatten(String prefix) {
    HashMap<String,String> flat = new HashMap<>();
    for (int i=0; i<attributes.length; i++) {
      Map<String, String> f = attributes[i].flatten(prefix!=null? String.format("%s:%d", prefix, i): String.format("%d", i));
      flat.putAll(f);
    }
    return flat;
  }
  
  @Override
  public String toString() {
    return String.format("[ %s ]", Arrays.asList(attributes).stream().map(Object::toString).collect(Collectors.joining(", ")));
  }
}
