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
package com.esri.geoportal.commons.agp.client;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Query response.
 */
public final class QueryResponse {
  public long total;
  public long start;
  public long num;
  public long nextStart;
  
  public ItemEntry [] results;
  
  @Override
  public String toString() {
    return String.format("[%s]", results!=null? Arrays.asList(results).stream().map(Object::toString).collect(Collectors.joining(", ")):"");
  }
}
