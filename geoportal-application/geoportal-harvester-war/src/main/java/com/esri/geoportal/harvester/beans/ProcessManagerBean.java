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
package com.esri.geoportal.harvester.beans;

import com.esri.geoportal.harvester.engine.ProcessManager;
import com.esri.geoportal.harvester.engine.Process;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Process manager bean.
 */
@Service
public class ProcessManagerBean implements ProcessManager  {
  private final HashMap<UUID,Process> processes = new HashMap<>();

  @Override
  public UUID create(Process process) {
    UUID id = UUID.randomUUID();
    processes.put(id, process);
    return id;
  }

  @Override
  public boolean update(UUID id, Process process) {
    return processes.put(id, process)!=null;
  }

  @Override
  public Process read(UUID id) {
    return processes.get(id);
  }

  @Override
  public boolean delete(UUID id) {
    return processes.remove(id)!=null;
  }

  @Override
  public Collection<Map.Entry<UUID, Process>> select() {
    return processes.entrySet();
  }
}
