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

import com.esri.geoportal.harvester.api.Filter;
import com.esri.geoportal.harvester.api.FilterInstance;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.Transformer;
import com.esri.geoportal.harvester.api.TransformerInstance;
import com.esri.geoportal.harvester.api.base.BrokerLinkActionAdaptor;
import com.esri.geoportal.harvester.api.base.FilterLinkActionAdaptor;
import com.esri.geoportal.harvester.api.base.SimpleLink;
import com.esri.geoportal.harvester.api.base.TransformerLinkActionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.LinkDefinition;
import com.esri.geoportal.harvester.api.defs.Task;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.general.Link;
import com.esri.geoportal.harvester.api.general.LinkAction;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputBroker.IteratorContext;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import com.esri.geoportal.harvester.engine.services.ExecutionService;
import com.esri.geoportal.harvester.engine.services.ProcessesService;
import com.esri.geoportal.harvester.engine.registers.FilterRegistry;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.registers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.registers.TransformerRegistry;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.registers.TriggerRegistry;
import com.esri.geoportal.harvester.engine.utils.ProcessReference;
import java.util.ArrayList;

/**
 * Default execution service.
 */
public class DefaultExecutionService implements ExecutionService {
  protected final InboundConnectorRegistry inboundConnectorRegistry;
  protected final OutboundConnectorRegistry outboundConnectorRegistry;
  protected final TransformerRegistry transformerRegistry;
  protected final FilterRegistry filterRegistry;
  protected final ProcessorRegistry processorRegistry;
  protected final TriggerRegistry triggerRegistry;
  protected final TriggerManager triggerManager;
  protected final TriggerInstanceManager triggerInstanceManager;
  protected final HistoryManager historyManager;
  protected final ProcessesService processesService;

  /**
   * Creates instance of the service.
   * @param inboundConnectorRegistry inbound connector registry.
   * @param outboundConnectorRegistry outbound connector registry
   * @param transformerRegistry transformer registry
   * @param filterRegistry filter registry
   * @param processorRegistry processor registry
   * @param triggerRegistry trigger registry
   * @param triggerManager trigger manager
   * @param triggerInstanceManager trigger instance manager
   * @param historyManager history manager
   * @param processesService processes service
   */
  public DefaultExecutionService(
          InboundConnectorRegistry inboundConnectorRegistry, 
          OutboundConnectorRegistry outboundConnectorRegistry, 
          TransformerRegistry transformerRegistry,
          FilterRegistry filterRegistry,
          ProcessorRegistry processorRegistry, 
          TriggerRegistry triggerRegistry, 
          TriggerManager triggerManager, 
          TriggerInstanceManager triggerInstanceManager, 
          HistoryManager historyManager, 
          ProcessesService processesService
  ) {
    this.inboundConnectorRegistry = inboundConnectorRegistry;
    this.outboundConnectorRegistry = outboundConnectorRegistry;
    this.transformerRegistry = transformerRegistry;
    this.filterRegistry = filterRegistry;
    this.processorRegistry = processorRegistry;
    this.triggerRegistry = triggerRegistry;
    this.triggerManager = triggerManager;
    this.triggerInstanceManager = triggerInstanceManager;
    this.historyManager = historyManager;
    this.processesService = processesService;
  }

  @Override
  public ProcessReference execute(TaskDefinition taskDefinition, IteratorContext iteratorContext) throws InvalidDefinitionException, DataProcessorException {
    Task task = createTask(taskDefinition);
    return processesService.createProcess(task, iteratorContext);
  }
  
  /**
   * Creates new task.
   * @param taskDefinition task definition
   * @return task
   * @throws InvalidDefinitionException  if invalid definition
   */
  private Task createTask(TaskDefinition taskDefinition) throws InvalidDefinitionException {
    InputBroker dataSource = newInputBroker(taskDefinition.getSource());

    ArrayList<Link> dataDestinations = new ArrayList<>();
    for (LinkDefinition def : taskDefinition.getDestinations()) {
      dataDestinations.add(newLink(def));
    }
    
    Processor processor = newProcessor(taskDefinition.getProcessor());
    
    return new Task(taskDefinition.getRef(), processor, dataSource, dataDestinations, taskDefinition.getKeywords(), taskDefinition.isIncremental(), taskDefinition.isIgnoreRobotsTxt());
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
