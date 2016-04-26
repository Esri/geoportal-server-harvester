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
package com.esri.geoportal.commons.csw.client.impl;

import com.esri.geoportal.commons.csw.client.IRecord;
import java.util.Date;

/**
 * Basic record implementation.
 */
public class Record implements IRecord {
  private final String id;
  private final Date lastModifiedDate;

  /**
   * Creates instance of the record.
   * @param id record id
   * @param lastModifiedDate last modifed date
   */
  public Record(String id, Date lastModifiedDate) {
    this.id = id;
    this.lastModifiedDate = lastModifiedDate;
  }

  /**
   * Gets id.
   * @return id
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Gets last modified date.
   * @return last modified date
   */
  @Override
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }
  
  @Override
  public String toString() {
    return String.format("RECORD :: id: %s, last-modified: %s", id, lastModifiedDate);
  }
}
