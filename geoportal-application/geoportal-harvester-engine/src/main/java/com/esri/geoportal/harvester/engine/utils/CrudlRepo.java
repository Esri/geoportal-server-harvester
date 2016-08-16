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
package com.esri.geoportal.harvester.engine.utils;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * CrudlRepo (create, read, update, delete, list)
 * @param <T> type of the managed data
 */
public interface CrudlRepo<T> {

  /**
   * Creates process.
   * @param data data
   * @return id of the data
   * @throws CrudlException if operation performed on repository fails
   */
  UUID create(T data) throws CrudlException;

  /**
   * Removes data.
   * @param id data id
   * @return <code>true</code> if updated
   * @throws CrudlException if operation performed on repository fails
   */
  boolean delete(UUID id) throws CrudlException;

  /**
   * Reads data by id.
   * @param id data id
   * @return data or <code>null</code> if no corresponding data
   * @throws CrudlException if operation performed on repository fails
   */
  T read(UUID id) throws CrudlException;

  /**
   * Updates data.
   * @param id data id
   * @param data data
   * @return <code>true</code> if updated
   * @throws CrudlException if operation performed on repository fails
   */
  boolean update(UUID id, T data) throws CrudlException;

  /**
   * Lists all data.
   * @return all data
   * @throws CrudlException if operation performed on repository fails
   */
  Collection<Map.Entry<UUID, T>> list() throws CrudlException;
  
}
