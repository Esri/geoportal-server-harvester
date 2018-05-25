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
package com.esri.geoportal.commons.csw.client;

import java.util.Date;

/**
 * Client interface.
 */
public interface IClient {
  /**
   * Finds all records.
   * @param start start record
   * @param max number of records to return
   * @param from optional from date
   * @param to optional to date
   * @return records
   * @throws Exception if finding records fails
   */
  IRecords findRecords(int start, int max, Date from, Date to) throws Exception;
  
  /**
   * Finds all records.
   * @param start start record
   * @param max number of records to return
   * @param from optional from date
   * @param to optional to date
   * @param searchText search text
   * @return records
   * @throws Exception if finding records fails
   */
  default IRecords findRecords(int start, int max, Date from, Date to, String searchText) throws Exception {
    return findRecords(start, max, from, to);
  }
  
  /**
   * Reads metadata.
   * @param id id of the record
   * @return metadata
   * @throws Exception if reading metadata fails
   */
  String readMetadata(String id) throws Exception;
}
