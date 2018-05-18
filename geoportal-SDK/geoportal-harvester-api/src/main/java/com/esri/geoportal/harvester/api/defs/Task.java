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
package com.esri.geoportal.harvester.api.defs;

import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.general.Link;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task. Holds all the information needed to create 
 * {@link com.esri.geoportal.harvester.api.ProcessInstance}. Task itself can be
 * created based on the {@link com.esri.geoportal.harvester.api.defs.TaskDefinition}.
 */
public final class Task {
  private final String name;
  private final Processor processor;
  private final InputBroker dataSource;
  private final List<Link> dataDestinations;
  private final List<String> keywords;
  private final boolean incremental;
  private final boolean ignoreRobotsTxt;
  private String ref;
  
  /**
   * Creates instance of the task.
   * @param name name
   * @param ref task reference
   * @param processor processor
   * @param dataSource data source
   * @param dataDestinations data destination
   * @param keyywords keywords
   * @param incremental incremental flag
   * @param ignoreRobotsTxt ignore robots flag
   */
  public Task(String name, String ref, Processor processor, InputBroker dataSource, List<Link> dataDestinations, List<String> keyywords, boolean incremental, boolean ignoreRobotsTxt) {
    this.name = name;
    this.ref = ref;
    this.processor = processor;
    this.dataSource = dataSource;
    this.dataDestinations = dataDestinations;
    this.keywords = keyywords;
    this.incremental = incremental;
    this.ignoreRobotsTxt = ignoreRobotsTxt;
  }
  
  /**
   * Creates instance of the task.
   * @param name name
   * @param ref task reference
   * @param processor processor
   * @param dataSource data source
   * @param dataDestinations data destination
   */
  public Task(String name, String ref, Processor processor, InputBroker dataSource, List<Link> dataDestinations) {
    this(name, ref, processor, dataSource, dataDestinations, Collections.emptyList(), false, false);
  }

  /**
   * Gets name.
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets task reference.
   * @return task reference
   */
  public String getRef() {
    return ref;
  }

  /**
   * Gets task reference.
   * @param ref task reference
   */
  public void setRef(String ref) {
    this.ref = ref;
  }

  /**
   * Gets task definition.
   * @return task definition
   */
  public TaskDefinition getTaskDefinition() {
    TaskDefinition taskDefinition = new TaskDefinition();
    taskDefinition.setName(name);
    taskDefinition.setProcessor(processor!=null? processor.getEntityDefinition(): null);
    taskDefinition.setSource(dataSource!=null? dataSource.getEntityDefinition(): null);
    taskDefinition.setDestinations(dataDestinations!=null? dataDestinations.stream().map(d->d.getLinkDefinition()).collect(Collectors.toList()): null);
    taskDefinition.setKeywords(keywords);
    taskDefinition.setIncremental(incremental);
    taskDefinition.setIgnoreRobotsTxt(ignoreRobotsTxt);
    taskDefinition.setRef(getRef());
    return taskDefinition;
  }

  /**
   * Gets processor.
   * @return processor
   */
  public Processor getProcessor() {
    return processor;
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
  public List<Link> getDataDestinations() {
    return dataDestinations;
  }
  
  @Override
  public String toString() {
    return String.format("%s", getTaskDefinition());
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    if (obj instanceof Task) {
      Task t = (Task)obj;
      return getTaskDefinition().equals(t.getTaskDefinition());
    }

    return false;
  }
}
