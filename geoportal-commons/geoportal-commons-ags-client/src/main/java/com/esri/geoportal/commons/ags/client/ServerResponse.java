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
package com.esri.geoportal.commons.ags.client;

import java.util.List;

/**
 * Map server response.
 */
public final class ServerResponse {
  public String url;
  public String json;
  
  public String mapName;
  public String serviceDescription;
  public String name;
  public String description;
  public SpatialReferenceInfo spatialReference;
  public ExtentInfo initialExtent;
  public ExtentInfo fullExtent;
  public List<LayerRef> layers;
  public ItemInfo itemInfo;
  
  @Override
  public String toString() {
    return String.format("{ \"mapName\": \"%s\", \"serviceDescription\": \"%s\", \"spatialReference\": %s, \"initialExtent\": %s, \"fullExtent\": %s}", mapName, serviceDescription, spatialReference, initialExtent, fullExtent);
  }
}
