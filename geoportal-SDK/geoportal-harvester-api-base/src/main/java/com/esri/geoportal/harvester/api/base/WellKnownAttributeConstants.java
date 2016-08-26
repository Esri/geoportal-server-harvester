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
package com.esri.geoportal.harvester.api.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Well known attribute constants.
 */
public final class WellKnownAttributeConstants {
  
  public static final String WKA_IDENTIFIER = "identifier";
  public static final String WKA_TITLE = "title";
  public static final String WKA_DESCRIPTION = "description";
  public static final String WKA_RESOURCE_URL = "resource.url";
  public static final String WKA_RESOURCE_URL_SCHEME = "resource.url.scheme";
  public static final String WKA_BBOX = "bbox";
  
  private static final Set<String> all = new HashSet(Arrays.asList(new String[]{
    WKA_IDENTIFIER, WKA_TITLE, WKA_DESCRIPTION, WKA_RESOURCE_URL, WKA_RESOURCE_URL_SCHEME, WKA_BBOX
  }));
  
  /**
   * Gets all constants.
   * @return all constants
   */
  public static final Set<String> getAll() {
    return all;
  }
}
