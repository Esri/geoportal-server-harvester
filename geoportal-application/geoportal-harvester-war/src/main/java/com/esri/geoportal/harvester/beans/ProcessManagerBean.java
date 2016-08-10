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

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.engine.managers.ProcessManager;
import com.esri.geoportal.harvester.engine.support.CrudlException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * DefaultProcess manager bean.
 */
@Service
public class ProcessManagerBean implements ProcessManager  {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessManagerBean.class);
  
  private final HashMap<UUID, ProcessInstance> processes = new HashMap<>();
  
  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    LOG.info("ProcessManagerBean initialized.");
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    LOG.info(String.format("ProcessManagerBean destroyed."));
  }

  @Override
  public UUID create(ProcessInstance process) throws CrudlException {
    // this prevents submiting the same process twice
    boolean exists = list().stream()
            .filter(p->p.getValue().getStatus()!=ProcessInstance.Status.completed)
            .map(e->e.getValue().getTask())
            .anyMatch(td->td.equals(process.getTask()));
    if (exists) {
      throw new CrudlException(String.format("Process based on the task %s already exists",process.getTask()));
    }
    
    UUID id = UUID.randomUUID();
    processes.put(id, process);
    return id;
  }

  @Override
  public boolean update(UUID id, ProcessInstance process) {
    return processes.put(id, process)!=null;
  }

  @Override
  public ProcessInstance read(UUID id) {
    return processes.get(id);
  }

  @Override
  public boolean delete(UUID id) {
    return processes.remove(id)!=null;
  }

  @Override
  public Collection<Map.Entry<UUID, ProcessInstance>> list() {
    return processes.entrySet();
  }
}
