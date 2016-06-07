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
package com.esri.geoportal.harvester.engine;

import com.esri.geoportal.harvester.engine.support.ProcessReference;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import com.esri.geoportal.harvester.engine.BrokerInfo.Category;
import static com.esri.geoportal.harvester.engine.BrokerInfo.Category.INBOUND;
import static com.esri.geoportal.harvester.engine.BrokerInfo.Category.OUTBOUND;
import com.esri.geoportal.harvester.engine.support.CrudsException;
import com.esri.geoportal.harvester.engine.support.ReportBuilderAdaptor;
import com.esri.geoportal.harvester.engine.support.TriggerReference;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Harvesting engine.
 */
public class Engine {

  private static final Logger LOG = LoggerFactory.getLogger(Engine.class);

  private final ReportBuilder reportBuilder;
  private final TaskManager taskManager;
  private final ProcessManager processManager;
  private final TriggerManager triggerManager;
  private final HistoryManager historyManager;
  private final InboundConnectorRegistry inboundConnectorRegistry;
  private final OutboundConnectorRegistry outboundConnectorRegistry;
  private final TriggerRegistry triggerRegistry;
  private final ProcessorRegistry processorRegistry;
  private final BrokerDefinitionManager brokerDefinitionManager;

  /**
   * Creates instance of the engine.
   *
   * @param inboundConnectorRegistry inbound connector registry
   * @param outboundConnectorRegistry outbound connector registry
   * @param triggerRegistry trigger registry
   * @param processorRegistry processor registry
   * @param brokerDefinitionManager broker definition manager
   * @param taskManager task manager
   * @param processManager process manager
   * @param triggerManager trigger manager
   * @param historyManager history manager
   * @param reportBuilder report builder
   */
  public Engine(
          InboundConnectorRegistry inboundConnectorRegistry,
          OutboundConnectorRegistry outboundConnectorRegistry,
          TriggerRegistry triggerRegistry,
          ProcessorRegistry processorRegistry,
          BrokerDefinitionManager brokerDefinitionManager,
          TaskManager taskManager,
          ProcessManager processManager,
          TriggerManager triggerManager,
          HistoryManager historyManager,
          ReportBuilder reportBuilder
  ) {
    this.inboundConnectorRegistry = inboundConnectorRegistry;
    this.outboundConnectorRegistry = outboundConnectorRegistry;
    this.triggerRegistry = triggerRegistry;
    this.processorRegistry = processorRegistry;
    this.taskManager = taskManager;
    this.processManager = processManager;
    this.brokerDefinitionManager = brokerDefinitionManager;
    this.triggerManager = triggerManager;
    this.historyManager = historyManager;
    this.reportBuilder = reportBuilder;
  }

  /**
   * Fire all triggers.
   */
  public void fireTriggers() {
    triggerManager.getInstances().values().stream().forEach((inst) -> {
      try {
        Trigger.Context context = new TriggerContext();
        inst.activate(context);
      } catch (DataProcessorException|InvalidDefinitionException ex) {
        LOG.warn(String.format("Error activating trigger instance: %s", inst), ex);
      }
    });
  }

  /**
   * Lists all triggers.
   * @return list of triggers
   */
  public List<Trigger> listTriggers() {
    return new ArrayList<>(triggerRegistry.values());
  }
  
  /**
   * Gets inbound connector templates.
   *
   * @return collection of inbound connector templates
   */
  public Collection<UITemplate> getInboundConnectorTemplates() {
    return inboundConnectorRegistry.getTemplates();
  }

  /**
   * Gets outbound connector templates.
   *
   * @return collection of outbound connector templates
   */
  public Collection<UITemplate> getOutboundConnectorTemplates() {
    return outboundConnectorRegistry.getTemplates();
  }

  /**
   * Gets broker definitions.
   *
   * @param category broker category
   * @return broker infos
   * @throws CrudsException if accessing repository fails
   */
  public Collection<BrokerInfo> getBrokersDefinitions(Category category) throws CrudsException {
    if (category != null) {
      Set<String> brokerTypes = listTypesByCategory(category);
      return brokerDefinitionManager.select().stream()
              .filter(e -> brokerTypes.contains(e.getValue().getType()))
              .map(e -> new BrokerInfo(e.getKey(), category, e.getValue()))
              .collect(Collectors.toList());
    } else {
      return Stream.concat(getBrokersDefinitions(INBOUND).stream(), getBrokersDefinitions(OUTBOUND).stream()).collect(Collectors.toSet());
    }
  }

  /**
   * Finds broker by id.
   *
   * @param brokerId broker id
   * @return broker info or <code>null</code> if no broker corresponding to the
   * broker id can be found
   * @throws CrudsException if accessing repository fails
   */
  public BrokerInfo findBroker(UUID brokerId) throws CrudsException {
    EntityDefinition brokerDefinition = brokerDefinitionManager.read(brokerId);
    if (brokerDefinition != null) {
      Category category = getBrokerCategoryByType(brokerDefinition.getType());
      if (category != null) {
        return new BrokerInfo(brokerId, category, brokerDefinition);
      }
    }
    return null;
  }

  /**
   * Creates a broker.
   *
   * @param brokerDefinition broker definition
   * @return broker info or <code>null</code> if broker has not been created
   * @throws CrudsException if accessing repository fails
   */
  public BrokerInfo createBroker(EntityDefinition brokerDefinition) throws CrudsException {
    Category category = getBrokerCategoryByType(brokerDefinition.getType());
    if (category != null) {
      try {
        UUID id = brokerDefinitionManager.create(brokerDefinition);
        return new BrokerInfo(id, category, brokerDefinition);
      } catch (IllegalArgumentException ex) {
        LOG.warn("Attempt to submit process based on the same task twice.", ex);
        return null;
      }
    }
    return null;
  }

  /**
   * Creates a broker.
   *
   * @param brokerId broker id
   * @param brokerDefinition broker definition
   * @return broker info or <code>null</code> if broker has not been created
   * @throws CrudsException if accessing repository fails
   */
  public BrokerInfo updateBroker(UUID brokerId, EntityDefinition brokerDefinition) throws CrudsException {
    EntityDefinition oldBrokerDef = brokerDefinitionManager.read(brokerId);
    if (oldBrokerDef != null) {
      if (!brokerDefinitionManager.update(brokerId, brokerDefinition)) {
        oldBrokerDef = null;
      }
    }
    Category category = oldBrokerDef != null ? getBrokerCategoryByType(oldBrokerDef.getType()) : null;
    return category != null ? new BrokerInfo(brokerId, category, brokerDefinition) : null;
  }

  /**
   * Deletes broker.
   *
   * @param brokerId broker id
   * @return <code>true</code> if broker has been deleted
   * @throws CrudsException if accessing repository fails
   */
  public boolean deleteBroker(UUID brokerId) throws CrudsException {
    return brokerDefinitionManager.delete(brokerId);
  }

  /**
   * Gets process by process id.
   *
   * @param processId process id.
   * @return process or <code>null</code> if no process available for the given
   * process id
   * @throws CrudsException if accessing repository fails
   */
  public Processor.Process getProcess(UUID processId) throws CrudsException {
    return processManager.read(processId);
  }

  /**
   * Selects processes by predicate.
   *
   * @param predicate predicate
   * @return list of processes matching predicate
   * @throws CrudsException if accessing repository fails
   */
  public List<Map.Entry<UUID, Processor.Process>> selectProcesses(Predicate<? super Map.Entry<UUID, Processor.Process>> predicate) throws CrudsException {
    return processManager.select().stream().filter(predicate != null ? predicate : (Map.Entry<UUID, Processor.Process> e) -> true).collect(Collectors.toList());
  }

  /**
   * Creates task to initialize.
   *
   * @param taskDefinition task definition
   * @return task
   * @throws InvalidDefinitionException if one of broker definitions appears to
   * be invalid
   */
  public Task createTask(TaskDefinition taskDefinition) throws InvalidDefinitionException {
    InputConnector<InputBroker> dsFactory = inboundConnectorRegistry.get(taskDefinition.getSource().getType());

    if (dsFactory == null) {
      throw new IllegalArgumentException("Invalid data source init parameters");
    }

    InputBroker dataSource = dsFactory.createBroker(taskDefinition.getSource());

    ArrayList<OutputBroker> dataDestinations = new ArrayList<>();
    for (EntityDefinition def : taskDefinition.getDestinations()) {
      OutputConnector<OutputBroker> dpFactory = outboundConnectorRegistry.get(def.getType());
      if (dpFactory == null) {
        throw new IllegalArgumentException("Invalid data publisher init parameters");
      }

      OutputBroker dataPublisher = dpFactory.createBroker(def);
      dataDestinations.add(dataPublisher);
    }

    return new Task(taskDefinition, dataSource, dataDestinations);
  }

  /**
   * Creates process.
   *
   * @param processorDefinition process definition
   * @param task task for the process
   * @return process handle
   * @throws InvalidDefinitionException if processor definition is invalid
   * @throws CrudsException if accessing repository fails
   */
  public ProcessReference createProcess(EntityDefinition processorDefinition, Task task) throws InvalidDefinitionException, CrudsException {
    Processor processor = processorDefinition == null
            ? processorRegistry.getDefaultProcessor()
            : processorRegistry.get(processorDefinition.getType()) != null
            ? processorRegistry.get(processorDefinition.getType())
            : null;
    if (processor == null) {
      throw new InvalidDefinitionException(String.format("Unable to select processor based on definition: %s", processorDefinition));
    }
    Processor.Process process = processor.createProcess(task);
    process.addListener(new ReportBuilderAdaptor(process, reportBuilder));
    UUID id = processManager.create(process);
    return new ProcessReference(id, process);
  }

  /**
   * Submits task definition.
   *
   * @param taskDefinition task definition
   * @return process handle
   * @throws InvalidDefinitionException invalid definition exception
   * @throws CrudsException if accessing repository fails
   */
  public ProcessReference submitTaskDefinition(TaskDefinition taskDefinition) throws InvalidDefinitionException, CrudsException {
    Task task = createTask(taskDefinition);
    ProcessReference prInfo = createProcess(taskDefinition.getProcessor(), task);
    return prInfo;
  }
  
  /**
   * Schedules task with trigger.
   * @param taskDefinition task definition
   * @param triggerDefinition trigger definition
   * @return trigger definition
   * @throws InvalidDefinitionException if invalid definition
   * @throws DataProcessorException if error processing data
   * @throws CrudsException if accessing repository fails
   */
  public TriggerReference scheduleTask(TaskDefinition taskDefinition, EntityDefinition triggerDefinition) throws InvalidDefinitionException, DataProcessorException, CrudsException {
    TriggerDefinition trigDef = new TriggerDefinition();
    trigDef.setType(triggerDefinition.getType());
    trigDef.setTaskDefinition(taskDefinition);
    trigDef.setArguments(triggerDefinition.getProperties());
    
    UUID id = triggerManager.create(trigDef);
    Trigger.Context context = new TriggerContext();
    triggerManager.getInstances().get(id).activate(context);
    
    return new TriggerReference(id, trigDef);
  }

  /**
   * Selects task definitions.
   *
   * @param predicate predicate
   * @return list of task definitions matching predicate
   * @throws CrudsException if accessing repository fails
   */
  public List<Map.Entry<UUID, TaskDefinition>> selectTaskDefinitions(Predicate<? super Map.Entry<UUID, TaskDefinition>> predicate) throws CrudsException {
    return taskManager.select().stream().filter(predicate != null ? predicate : (Map.Entry<UUID, TaskDefinition> e) -> true).collect(Collectors.toList());
  }

  /**
   * Reads task definition.
   *
   * @param taskId task id
   * @return task definition
   * @throws CrudsException if accessing repository fails
   */
  public TaskDefinition readTaskDefinition(UUID taskId) throws CrudsException {
    return taskManager.read(taskId);
  }

  /**
   * Deletes task definition.
   *
   * @param taskId task id
   * @return <code>true</code> if task definition has been deleted
   * @throws CrudsException if accessing repository fails
   */
  public boolean deleteTaskDefinition(UUID taskId) throws CrudsException {
    return taskManager.delete(taskId);
  }

  /**
   * Adds task definition.
   *
   * @param taskDefinition task definition
   * @return id of a new task
   * @throws CrudsException if accessing repository fails
   */
  public UUID addTaskDefinition(TaskDefinition taskDefinition) throws CrudsException {
    return taskManager.create(taskDefinition);
  }

  /**
   * Updates task.
   *
   * @param taskId task id
   * @param taskDefinition task definition
   * @return old task definition or <code>null</code> if no old task
   * @throws CrudsException if accessing repository fails
   */
  public TaskDefinition updateTaskDefinition(UUID taskId, TaskDefinition taskDefinition) throws CrudsException {
    TaskDefinition oldTaskDef = taskManager.read(taskId);
    if (oldTaskDef != null) {
      if (!taskManager.update(taskId, taskDefinition)) {
        oldTaskDef = null;
      }
    }
    return oldTaskDef;
  }

  /**
   * Lists types by category.
   *
   * @param category category
   * @return set of types within the category
   */
  private Set<String> listTypesByCategory(Category category) {
    List<UITemplate> templates
            = category == INBOUND ? inboundConnectorRegistry.getTemplates()
                    : category == OUTBOUND ? outboundConnectorRegistry.getTemplates()
                            : null;

    if (templates != null) {
      return templates.stream().map(t -> t.getType()).collect(Collectors.toSet());
    } else {
      return Collections.EMPTY_SET;
    }
  }

  /**
   * Gets broker category by broker type.
   *
   * @param brokerType broker type
   * @return broker category or <code>null</code> if category couldn't be
   * determined
   */
  private Category getBrokerCategoryByType(String brokerType) {
    Set<String> inboundTypes = inboundConnectorRegistry.getTemplates().stream().map(t -> t.getType()).collect(Collectors.toSet());
    Set<String> outboundTypes = outboundConnectorRegistry.getTemplates().stream().map(t -> t.getType()).collect(Collectors.toSet());
    return inboundTypes.contains(brokerType) ? INBOUND : outboundTypes.contains(brokerType) ? OUTBOUND : null;
  }

  /**
   * Engine-bound trigger context.
   */
  private class TriggerContext implements Trigger.Context {

    @Override
    public synchronized Processor.Process submit(TaskDefinition taskDefinition) throws DataProcessorException, InvalidDefinitionException {
      ProcessReference ref;
      try {
        ref = submitTaskDefinition(taskDefinition);
        return ref != null ? ref.getProcess() : null;
      } catch (CrudsException ex) {
        throw new DataProcessorException(String.format("Error submiting task definition: %s", taskDefinition), ex);
      }
    }
    
    @Override
    public Date lastHarvest(UUID uuid) throws DataProcessorException {
      try {
        History history = historyManager.buildHistory(uuid);
        History.Event lastEvent = history.lastEvent();
        return lastEvent!=null? lastEvent.getTimestamp(): null;
      } catch (CrudsException ex) {
        throw new DataProcessorException(String.format("Error getting last harvest for: %s", uuid), ex);
      }
    }
  }
}
