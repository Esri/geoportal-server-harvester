/*
 * Copyright 2016 Esri, Inc..
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
package com.esri.geoportal.commons.csw.client;

/**
 * Csw profile.
 */
public interface IProfile {
  /**
   * Gets profile id.
   * @return profile id
   */
  String getId();
  
  /**
   * Gets profile name.
   * @return profile name
   */
  String getName();
  
  /**
   * Gets profile description.
   * @return profile description
   */
  String getDescription();
  
  /**
   * Generates POST request body to search for the records.
   * @param criteria search criteria
   * @return POST body
   */
  String generateCSWGetRecordsRequest(ICriteria criteria);
  
  /**
   * Generates URL to fetch a single metadata by id.
   * @param baseURL base URL
   * @param recordId record id
   * @return record URL
   */
  String generateCSWGetMetadataByIDRequestURL(String baseURL, String recordId);
  
  String getKvp();
  String getGetRecordsReqXslt();
  
  String getResponsexslt();
  String getMetadataxslt();
}
