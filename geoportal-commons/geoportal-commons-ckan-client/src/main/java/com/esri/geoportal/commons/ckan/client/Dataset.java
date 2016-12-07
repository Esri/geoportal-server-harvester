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
package com.esri.geoportal.commons.ckan.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Dataset.
 */
public class Dataset {
  public String license_title;
  public String maintainer;
  public List<Relationship> relationships_as_object;
  @JsonProperty("private")
  public Boolean priv;
  public String maintainer_email;
  public Long num_tags;
  public String id;
  public String metadata_created;
  public String metadata_modified;
  public String author;
  public String author_email;
  public String state;
  public String version;
  public String creator_user_id;
  public String type;
  public List<Resource> resources;
  public Long num_resources;
  public List<Tag> tags;
  public List<Group> groups;
  public String license_id;
  public List<Relationship> relationships_as_subject;
  public Organization organization;
  public String name;
  public Boolean isopen;
  public String url;
  public String notes;
  public String owner_org;
  public String title;
  public String revision_id;
}
