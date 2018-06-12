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
package com.esri.geoportal.harvester.engine.managers;

import com.esri.geoportal.harvester.engine.utils.CrudlException;
import java.util.UUID;
import com.esri.geoportal.harvester.engine.utils.CrudlRepo;
import java.util.List;

/**
 * History manager.
 */
public interface HistoryManager extends CrudlRepo<History.Event> {
  /**
   * Builds history for the task.
   * @param uuid id of the task stored in the repo.
   * @return history
   * @throws CrudlException if building history fails
   */
  History buildHistory(UUID uuid) throws CrudlException;
  
  /**
   * Purges history for a given task id.
   * @param taskId task id.
   * @throws CrudlException if purging history fails
   */
  void purgeHistory(UUID taskId) throws CrudlException;

  /**
   * Lists failed data for a given event.
   * @param eventId event id
   * @return list of failed data
   * @throws CrudlException if unable to read information
   */
  List<String> listFailedData(UUID eventId) throws CrudlException;

  /**
   * Stores failed data for a given event.
   * @param eventId event id
   * @param dataId data id
   * @throws CrudlException if unable to store data
   */
  void storeFailedDataId(UUID eventId, String dataId) throws CrudlException;
}
