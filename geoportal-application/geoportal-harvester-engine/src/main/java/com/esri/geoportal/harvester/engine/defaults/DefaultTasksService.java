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
package com.esri.geoportal.harvester.engine.defaults;

import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.Filter;
import com.esri.geoportal.harvester.api.FilterInstance;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.Transformer;
import com.esri.geoportal.harvester.api.TransformerInstance;
import com.esri.geoportal.harvester.api.base.BrokerLinkActionAdaptor;
import com.esri.geoportal.harvester.api.base.FilterLinkActionAdaptor;
import com.esri.geoportal.harvester.api.base.SimpleInitContext;
import com.esri.geoportal.harvester.api.base.SimpleLink;
import com.esri.geoportal.harvester.api.base.TransformerLinkActionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.LinkDefinition;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.general.Link;
import com.esri.geoportal.harvester.api.general.LinkAction;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import com.esri.geoportal.harvester.engine.services.TasksService;
import com.esri.geoportal.harvester.engine.managers.History;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.managers.TaskManager;
import com.esri.geoportal.harvester.engine.registers.FilterRegistry;
import com.esri.geoportal.harvester.engine.registers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.registers.TransformerRegistry;
import com.esri.geoportal.harvester.engine.utils.CrudlException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

/**
 * Default tasks service.
 */
public class DefaultTasksService implements TasksService {
  protected final InboundConnectorRegistry inboundConnectorRegistry;
  protected final OutboundConnectorRegistry outboundConnectorRegistry;
  protected final TransformerRegistry transformerRegistry;
  protected final FilterRegistry filterRegistry;
  protected final ProcessorRegistry processorRegistry;
  protected final TaskManager taskManager;
  protected final HistoryManager historyManager;

  /**
   * Creates instance of the service.
   * @param executionService inbound connector registry.
   * @param taskManager task manager
   * @param historyManager history manager
   */
  public DefaultTasksService(
          InboundConnectorRegistry inboundConnectorRegistry, 
          OutboundConnectorRegistry outboundConnectorRegistry, 
          TransformerRegistry transformerRegistry,
          FilterRegistry filterRegistry,
          ProcessorRegistry processorRegistry, 
          TaskManager taskManager, HistoryManager historyManager) {
    this.inboundConnectorRegistry = inboundConnectorRegistry;
    this.outboundConnectorRegistry = outboundConnectorRegistry;
    this.transformerRegistry = transformerRegistry;
    this.filterRegistry = filterRegistry;
    this.processorRegistry = processorRegistry;
    this.taskManager = taskManager;
    this.historyManager = historyManager;
  }

  @Override
  public List<Map.Entry<UUID, TaskDefinition>> selectTaskDefinitions(Predicate<? super Map.Entry<UUID, TaskDefinition>> predicate) throws DataProcessorException {
    try {
      return taskManager.list().stream().filter(predicate != null ? predicate : (Map.Entry<UUID, TaskDefinition> e) -> true).collect(Collectors.toList());
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error selecting task definitions."), ex);
    }
  }

  @Override
  public TaskDefinition readTaskDefinition(UUID taskId) throws DataProcessorException {
    try {
      return taskManager.read(taskId);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error reading task definition: %s", taskId), ex);
    }
  }

  @Override
  public boolean deleteTaskDefinition(UUID taskId) throws DataProcessorException {
    try {
      historyManager.purgeHistory(taskId);
      return taskManager.delete(taskId);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error deleting task definition: %s", taskId), ex);
    }
  }

  @Override
  public UUID addTaskDefinition(TaskDefinition taskDefinition) throws DataProcessorException {
    try {
      return taskManager.create(taskDefinition);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error adding task definition: %s", taskDefinition), ex);
    }
  }

  @Override
  public TaskDefinition updateTaskDefinition(UUID taskId, TaskDefinition taskDefinition) throws DataProcessorException {
    try {
      TaskDefinition oldTaskDef = taskManager.read(taskId);
      if (oldTaskDef != null) {
        if (!taskManager.update(taskId, taskDefinition)) {
          oldTaskDef = null;
        }
      }
      return oldTaskDef;
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error updating task definition: %s <-- %s", taskId, taskDefinition), ex);
    }
  }
  
  @Override
  public History getHistory(UUID taskId) throws DataProcessorException {
    try {
      return historyManager.buildHistory(taskId);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error getting history for: %s", taskId), ex);
    }
  }
  
  @Override
  public List<String> getFailedDocuments(UUID eventId) throws DataProcessorException {
    try {
      return historyManager.listFailedData(eventId);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error getting history for: %s", eventId), ex);
    }
  }
  
  @Override
  public void purgeHistory(UUID taskId) throws DataProcessorException {
    try {
      historyManager.purgeHistory(taskId);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error purging history for: %s", taskId), ex);
    }
  }
  
  @Override
  public DataContent fetchContent(UUID taskId, String recordId, SimpleCredentials credentials) throws DataInputException {
    InputBroker broker = null;
    try {
      TaskDefinition taskDefinition = readTaskDefinition(taskId);
      Task task = this.createTask(taskDefinition);
      
      if (!task.getDataSource().hasAccess(credentials)) {
        throw new HttpResponseException(HttpStatus.SC_UNAUTHORIZED, "Invalid credentials");
      }
      
      broker = newInputBroker(taskDefinition.getSource());
      broker.initialize(new SimpleInitContext(task, new ArrayList<>()));
      
      return broker.readContent(recordId);
    } catch (InvalidDefinitionException|DataProcessorException|HttpResponseException ex) {
      throw new DataInputException(broker, String.format("Error fetching content from: %s -> $s", taskId, recordId), ex);
    } finally {
      if (broker!=null) {
        broker.terminate();
      }
    }
  }
  
  /**
   * Creates new input broker.
   * @param entityDefinition input broker definition
   * @return input broker
   * @throws InvalidDefinitionException if invalid definition
   */
  /*
  private InputBroker newInputBroker(EntityDefinition entityDefinition) throws InvalidDefinitionException {
    InputConnector<InputBroker> dsFactory = executionService.get(entityDefinition.getType());

    if (dsFactory == null) {
      throw new InvalidDefinitionException("Invalid input broker definition");
    }

    return dsFactory.createBroker(entityDefinition);
  }
  */
  
  /**
   * Creates new task.
   * @param taskDefinition task definition
   * @return task
   * @throws InvalidDefinitionException  if invalid definition
   */
  public Task createTask(TaskDefinition taskDefinition) throws InvalidDefinitionException {
    InputBroker dataSource = newInputBroker(taskDefinition.getSource());

    ArrayList<Link> dataDestinations = new ArrayList<>();
    for (LinkDefinition def : taskDefinition.getDestinations()) {
      dataDestinations.add(newLink(def));
    }
    
    Processor processor = newProcessor(taskDefinition.getProcessor());
    
    return new Task(taskDefinition.getName(), taskDefinition.getRef(), processor, dataSource, dataDestinations, taskDefinition.getKeywords(), taskDefinition.isIncremental(), taskDefinition.isIgnoreRobotsTxt());
  }
  
  /**
   * Creates new processor.
   * @param processorDefinition processor definition
   * @return processor
   * @throws InvalidDefinitionException if invalid definition
   */
  private Processor newProcessor(EntityDefinition processorDefinition) throws InvalidDefinitionException {
    Processor processor = processorDefinition == null
            ? processorRegistry.getDefaultProcessor()
            : processorRegistry.get(processorDefinition.getType()) != null
            ? processorRegistry.get(processorDefinition.getType())
            : null;
    if (processor == null) {
      throw new InvalidDefinitionException(String.format("Unable to select processor based on definition: %s", processorDefinition));
    }
    return processor;
  }
  
  /**
   * Creates new input broker.
   * @param entityDefinition input broker definition
   * @return input broker
   * @throws InvalidDefinitionException if invalid definition
   */
  private InputBroker newInputBroker(EntityDefinition entityDefinition) throws InvalidDefinitionException {
    InputConnector<InputBroker> dsFactory = inboundConnectorRegistry.get(entityDefinition.getType());

    if (dsFactory == null) {
      throw new InvalidDefinitionException("Invalid input broker definition");
    }

    return dsFactory.createBroker(entityDefinition);
  }

  /**
   * Creates new link.
   * @param linkDefinition link definition
   * @return link
   * @throws InvalidDefinitionException if invalid definition
   */
  private Link newLink(LinkDefinition linkDefinition) throws InvalidDefinitionException {
    LinkAction linkAction = newLinkAction(linkDefinition.getAction());
    ArrayList<Link> drains = new ArrayList<>();
    if (linkDefinition.getDrains()!=null) {
      for (LinkDefinition drainDef: linkDefinition.getDrains()) {
        drains.add(newLink(drainDef));
      }
    }
    return new SimpleLink(linkAction, drains);
  }
  
  /**
   * Creates new link action.
   * @param actionDefinition action definition
   * @return link action
   * @throws InvalidDefinitionException if invalid definition.
   */
  private LinkAction newLinkAction(EntityDefinition actionDefinition) throws InvalidDefinitionException {
    OutputConnector<OutputBroker> outputConnector = outboundConnectorRegistry.get(actionDefinition.getType());
    if (outputConnector!=null) {
      OutputBroker broker = outputConnector.createBroker(actionDefinition);
      return new BrokerLinkActionAdaptor(broker);
    }
    
    Filter filter = filterRegistry.get(actionDefinition.getType());
    if (filter!=null) {
      FilterInstance filterInstance = filter.createInstance(actionDefinition);
      return new FilterLinkActionAdaptor(filterInstance);
    }
    
    Transformer transformer = transformerRegistry.get(actionDefinition.getType());
    if (transformer!=null) {
      TransformerInstance transformerInstance = transformer.createInstance(actionDefinition);
      return new TransformerLinkActionAdaptor(transformerInstance);
    }
    
    throw new InvalidDefinitionException(String.format("Error creating link action for: %s", actionDefinition.getType()));
  }
}
