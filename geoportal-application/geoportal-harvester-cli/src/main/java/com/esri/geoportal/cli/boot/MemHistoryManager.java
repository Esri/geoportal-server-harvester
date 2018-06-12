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
package com.esri.geoportal.cli.boot;

import com.esri.geoportal.harvester.engine.managers.History;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.utils.CrudlException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * In-memory history manager.
 */
public class MemHistoryManager extends MemCruds<History.Event> implements HistoryManager {

  @Override
  public History buildHistory(UUID uuid) throws CrudlException {
    History history = new History();
    history.addAll(mem.entrySet().stream().filter(e->e.getValue().getTaskId().equals(uuid)).map(e->e.getValue()).collect(Collectors.toList()));
    return history;
  }

  @Override
  public void purgeHistory(UUID taskId) throws CrudlException {
    mem.entrySet().stream().filter(e->e.getValue().getTaskId().equals(taskId)).map(e->e.getKey()).forEach(uuid->mem.remove(uuid));
  }

  @Override
  public List<String> listFailedData(UUID eventId) throws CrudlException {
    return new ArrayList<>();
  }

  @Override
  public void storeFailedDataId(UUID eventId, String dataId) throws CrudlException {
  }
  
}
