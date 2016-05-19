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

import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import java.util.List;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

/**
 * Task.
 */
public class Task implements Closeable {
  private final TaskDefinition taskDefinition;
  private final InputBroker dataSource;
  private final List<OutputBroker> dataDestinations;
  
  /**
   * Creates instance of the task.
   * @param taskDefiniton task definition
   * @param dataSource data source
   * @param dataDestinations data destination
   */
  public Task(TaskDefinition taskDefiniton, InputBroker dataSource, List<OutputBroker> dataDestinations) {
    this.taskDefinition = taskDefiniton;
    this.dataSource = dataSource;
    this.dataDestinations = dataDestinations;
  }

  /**
   * Gets task definition.
   * @return task definition
   */
  public TaskDefinition getTaskDefinition() {
    return taskDefinition;
  }

  /**
   * Gets data source.
   * @return data source
   */
  public InputBroker getDataSource() {
    return dataSource;
  }

  /**
   * Gets data publisher.
   * @return data publisher
   */
  public List<OutputBroker> getDataDestinations() {
    return dataDestinations;
  }

  @Override
  public void close() throws IOException {
    try {
      getDataSource().close();
    } finally {
      getDataDestinations().stream().forEach(d -> {
        try {
          d.close();
        } catch (Exception ex) {}
      });
    }
  }
  
  @Override
  public String toString() {
    return String.format("TASK :: %s", taskDefinition);
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof Task) {
      Task t = (Task)o;
      return getTaskDefinition().equals(t.getTaskDefinition());
    }

    return false;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + Objects.hashCode(this.taskDefinition);
    return hash;
  }
}
