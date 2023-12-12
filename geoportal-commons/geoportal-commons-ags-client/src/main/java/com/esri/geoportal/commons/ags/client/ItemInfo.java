/*
 * Copyright 2021 Piotr Andzel.
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

/**
 * Item Info.
 */
public final class ItemInfo {
  public String culture;
  public String name;
  public String guid;
  public String catalogPath;
  public String snippet;
  public String description;
  public String summary;
  public String title;
  public String [] tags;
  public String type;
  public String [] typeKeywords;
  public String thumbnail;
  public String url;
  public Double [][] extent;
  public String spatialReference;
  public String accessInformation;
  public String licenseInfo;
  public boolean hasMetadata;
  public String metadataXML;
}
