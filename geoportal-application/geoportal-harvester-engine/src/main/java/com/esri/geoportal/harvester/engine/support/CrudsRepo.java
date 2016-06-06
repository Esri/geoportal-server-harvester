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
package com.esri.geoportal.harvester.engine.support;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * CrudsRepo (create, read, update, delete, select)
 * @param <T> type of the managed data
 */
public interface CrudsRepo<T> {

  /**
   * Creates process.
   * @param data data
   * @return id of the data
   * @throws CrudsException if operation performed on repository fails
   */
  UUID create(T data) throws CrudsException;

  /**
   * Removes data.
   * @param id data id
   * @return <code>true</code> if updated
   * @throws CrudsException if operation performed on repository fails
   */
  boolean delete(UUID id) throws CrudsException;

  /**
   * Reads data by id.
   * @param id data id
   * @return data or <code>null</code> if no corresponding data
   * @throws CrudsException if operation performed on repository fails
   */
  T read(UUID id) throws CrudsException;

  /**
   * Lists all data.
   * @return all data
   * @throws CrudsException if operation performed on repository fails
   */
  Collection<Map.Entry<UUID, T>> select() throws CrudsException;

  /**
   * Updates data.
   * @param id data id
   * @param data data
   * @return <code>true</code> if updated
   * @throws CrudsException if operation performed on repository fails
   */
  boolean update(UUID id, T data) throws CrudsException;
  
}
