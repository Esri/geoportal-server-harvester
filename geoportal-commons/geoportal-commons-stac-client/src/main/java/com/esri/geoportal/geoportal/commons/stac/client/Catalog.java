/*
 * Copyright 2021 Esri, Inc.
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
package com.esri.geoportal.geoportal.commons.stac.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

/**
 * STAC catalog.
 */
public class Catalog {
  public String id;
  public String title;
  public String description;
  public String [] keywords;
  public JsonNode extent;
  public Map<String, JsonNode> properties;
  public Map<String, JsonNode> assets;
  public Link [] links;
}
