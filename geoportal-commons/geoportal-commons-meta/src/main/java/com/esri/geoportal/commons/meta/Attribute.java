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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Attribute.
 * <p>
 * Attribute can hold a string, an array of attributes or a map of named attributes.
 */
public interface Attribute {
  /**
   * Is attribute a string.
   * @return <code>true</code> if an attributes is a string
   */
  default boolean isString() { return false; }
  /**
   * Is attribute a map.
   * @return <code>true</code> if an attributes is a map
   */
  default boolean isMap() { return false; }
  /**
   * Is attribute an array.
   * @return <code>true</code> if an attributes is an array
   */
  default boolean isArray() { return false; }
  
  /**
   * Gets value.
   * @return value or <code>null</code>
   */
  default String getValue() { return null; }
  
  /**
   * Gets named attributes.
   * @return named attributes or <code>null</code>
   */
  default Map<String, Attribute> getNamedAttributes() { return null; }
  
  /**
   * Gets an array of attributes.
   * @return array of attributes
   */
  default Attribute[] getAttributes() { return null; }
  
  /**
   * Scans attribute for matching sub-attributes.
   * @param pred predicate
   * @return stream of matching attributes
   */
  default Stream<Map.Entry<String, Attribute>> scan(Predicate<Map.Entry<String, Attribute>> pred) {
    ArrayList<Map.Entry<String, Attribute>> attributes = new ArrayList<>();
    
    Map.Entry<String, Attribute> thisAttribute = new ImmutablePair<>(null, this);
    if (pred.test(thisAttribute)) {
      attributes.add(thisAttribute);
    }
    
    Map<String, Attribute> namedAttributes = getNamedAttributes();
    if (namedAttributes!=null) {
      attributes.addAll(namedAttributes.entrySet().stream().filter(e->pred.test(e)).collect(Collectors.toList()));
      attributes.addAll(namedAttributes.entrySet().stream().flatMap(e->e.getValue().scan(pred)).collect(Collectors.toList()));
    }
    
    Attribute[] arrAttributes = getAttributes();
    if (arrAttributes!=null) {
      attributes.addAll(Arrays.stream(arrAttributes).map(attr->new ImmutablePair<String,Attribute>(null, attr)).filter(e->pred.test(e)).collect(Collectors.toList()));
      attributes.addAll(Arrays.stream(arrAttributes).flatMap(attr->attr.scan(pred)).collect(Collectors.toList()));
    }
    
    return attributes.stream();
  }
}
