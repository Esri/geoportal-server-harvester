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
package com.esri.geoportal.harvester.engine;

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
   */
  UUID create(T data);

  /**
   * Removes data.
   * @param id data id
   */
  void delete(UUID id);

  /**
   * Reads data by id.
   * @param id data id
   * @return data or <code>null</code> if no corresponding data
   */
  T read(UUID id);

  /**
   * Lists all data.
   * @return all data
   */
  Collection<Map.Entry<UUID, T>> select();

  /**
   * Updates data.
   * @param id data id
   * @param data data
   */
  void update(UUID id, T data);
  
}
