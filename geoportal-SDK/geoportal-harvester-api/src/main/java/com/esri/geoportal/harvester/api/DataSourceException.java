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
package com.esri.geoportal.harvester.api;

/**
 * Data source exception.
 * <p>
 * Exception associated with the source.
 */
public class DataSourceException extends DataException {
  private final DataSource dataSource;

  /**
   * Gets data source.
   * @return data source
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Creates a new instance of <code>DataSourceException</code> without detail
   * message.
   * @param dataSource data source
   */
  public DataSourceException(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Constructs an instance of <code>DataSourceException</code> with the
   * specified detail message.
   *
   * @param dataSource data source
   * @param msg the detail message.
   */
  public DataSourceException(DataSource dataSource, String msg) {
    super(msg);
    this.dataSource = dataSource;
  }

  /**
   * Constructs an instance of <code>DataSourceException</code> with the
   * specified detail message.
   *
   * @param dataSource data source
   * @param msg the detail message.
   * @param t cause
   */
  public DataSourceException(DataSource dataSource, String msg, Throwable t) {
    super(msg,t);
    this.dataSource = dataSource;
  }
}
