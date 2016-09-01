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
import java.util.Properties;

/**
 * Attribute utils.
 */
public class AttributeUtils {
  
  /**
   * Creates attribute from properties.
   * @param props properties
   * @return attribute
   */
  public static MapAttribute fromProperties(Properties props) {
    HashMap<String,Attribute> attrMap =  new HashMap<>();
    props.keySet().stream().map(Object::toString).forEach(key->{
      attrMap.put(key, new StringAttribute(props.getProperty(key)));
    });
    return new MapAttribute(attrMap);
  }
  
  /**
   * Creates properties from attribute
   * @param attr object attribute
   * @return properties
   */
  public static Properties toProperties(MapAttribute attr) {
    Properties props = new Properties();
    
    attr.getNamedAttributes().entrySet().stream().forEach(na->{
      if (na.getValue().isString()) {
        props.setProperty(na.getKey(), na.getValue().getValue());
      } else if (na.getValue().isArray()) {
        // skip arrays
      } else if (na.getValue().isMap()) {
        // skip objects
      }
    });
    return props;
  }
}
