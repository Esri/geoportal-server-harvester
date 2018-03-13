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

import com.esri.geoportal.commons.meta.MetaAnalyzer;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.util.MultiMetaAnalyzerWrapper;
import com.esri.geoportal.commons.meta.xml.SimpleDcMetaAnalyzer;
import com.esri.geoportal.commons.meta.xml.SimpleDcMetaBuilder;
import com.esri.geoportal.commons.meta.xml.SimpleFgdcMetaAnalyzer;
import com.esri.geoportal.commons.meta.xml.SimpleIso15115MetaAnalyzer;
import com.esri.geoportal.commons.meta.xml.SimpleIso15115_2MetaAnalyzer;
import com.esri.geoportal.commons.meta.xml.SimpleIso15119MetaAnalyzer;
import com.esri.geoportal.geoportal.commons.geometry.GeometryService;
import com.esri.geoportal.harvester.ckan.CkanConnector;
import com.esri.geoportal.harvester.ckan.data.gov.DataGovConnector;
import com.esri.geoportal.harvester.agp.AgpOutputConnector;
import com.esri.geoportal.harvester.agpsrc.AgpInputConnector;
import com.esri.geoportal.harvester.ags.AgsConnector;
import com.esri.geoportal.harvester.console.ConsoleConnector;
import com.esri.geoportal.harvester.csw.CswConnector;
import com.esri.geoportal.harvester.engine.defaults.DefaultEngine;
import com.esri.geoportal.harvester.engine.defaults.DefaultBrokersService;
import com.esri.geoportal.harvester.engine.defaults.DefaultExecutionService;
import com.esri.geoportal.harvester.engine.defaults.DefaultProcessesService;
import com.esri.geoportal.harvester.engine.defaults.DefaultTriggersService;
import com.esri.geoportal.harvester.engine.defaults.DefaultTasksService;
import com.esri.geoportal.harvester.engine.defaults.DefaultTemplatesService;
import com.esri.geoportal.harvester.engine.filters.RegExFilter;
import com.esri.geoportal.harvester.engine.managers.BrokerDefinitionManager;
import com.esri.geoportal.harvester.engine.managers.HistoryManager;
import com.esri.geoportal.harvester.engine.managers.ProcessManager;
import com.esri.geoportal.harvester.engine.managers.ReportManager;
import com.esri.geoportal.harvester.engine.managers.TaskManager;
import com.esri.geoportal.harvester.engine.managers.TriggerInstanceManager;
import com.esri.geoportal.harvester.engine.managers.TriggerManager;
import com.esri.geoportal.harvester.engine.registers.FilterRegistry;
import com.esri.geoportal.harvester.engine.registers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.ProcessorRegistry;
import com.esri.geoportal.harvester.engine.registers.StatisticsRegistry;
import com.esri.geoportal.harvester.engine.registers.TransformerRegistry;
import com.esri.geoportal.harvester.engine.registers.TriggerRegistry;
import com.esri.geoportal.harvester.engine.services.BrokersService;
import com.esri.geoportal.harvester.engine.services.Engine;
import com.esri.geoportal.harvester.engine.services.ExecutionService;
import com.esri.geoportal.harvester.engine.services.ProcessesService;
import com.esri.geoportal.harvester.engine.services.TasksService;
import com.esri.geoportal.harvester.engine.services.TemplatesService;
import com.esri.geoportal.harvester.engine.services.TriggersService;
import com.esri.geoportal.harvester.engine.transformers.XsltTransformer;
import com.esri.geoportal.harvester.engine.triggers.AtTrigger;
import com.esri.geoportal.harvester.engine.triggers.NowTrigger;
import com.esri.geoportal.harvester.engine.triggers.PeriodTrigger;
import com.esri.geoportal.harvester.folder.FolderConnector;
import com.esri.geoportal.harvester.gptsrc.GptConnector;
import com.esri.geoportal.harvester.unc.UncConnector;
import com.esri.geoportal.harvester.waf.WafConnector;
import com.esri.geoportal.harvester.api.ex.*;
import com.esri.geoportal.harvester.engine.defaults.DefaultProcessor;
import java.io.IOException;
import java.net.URL;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.http.impl.client.HttpClients;

/**
 * Bootstrap.
 */
public class Bootstrap {

  private TriggerRegistry triggerRegistry;
  private TransformerRegistry transformerRegistry;
  private StatisticsRegistry statisticsRegistry;
  private MemProcessorRegistry processorRegistry;
  private OutboundConnectorRegistry outboundConnectorRegistry;
  private InboundConnectorRegistry inboundConnectorRegistry;
  private FilterRegistry filterRegistry;
  
  private final BrokerDefinitionManager brokerDefinitionManager = new MemBrokerDefinitionManager();    
  private final ReportManager reportManager;
  private final HistoryManager historyManager = new MemHistoryManager();
  private final ProcessManager processManager = new MemProcessManager();
  private final TaskManager taskManager = new MemTaskManager();
  private final TriggerInstanceManager triggerInstanceManager = new MemTriggerInstanceManager();
  private final TriggerManager triggerManager = new MemTriggerManager();
  
  private BrokersService brokerService;
  private ExecutionService executionService;
  private ProcessesService processesService;
  private TasksService taskService;
  private TemplatesService templatesService;
  private TriggersService triggersService;
  private final String geometryServiceUrl;
  
  /**
   * Creates instance of the bootstrap.
   * @param geometryServiceUrl
   * @param reportManager report manager
   */
  public Bootstrap(String geometryServiceUrl, ReportManager reportManager) {
    this.geometryServiceUrl = geometryServiceUrl;
    this.reportManager = reportManager;
  }
  
  /**
   * Creates engine.
   * @return engine.
   * @throws com.esri.geoportal.harvester.api.ex.DataProcessorException if error creating engine
   */
  public Engine createEngine() throws DataProcessorException {
    try {
      DefaultEngine engine = new DefaultEngine(
              createTemplatesService(), 
              createBrokersService(), 
              createTasksService(), 
              createProcessesService(), 
              createTriggersService(), 
              createExecutionService());
      processorRegistry.setDefaultProcessor(new DefaultProcessor());
      engine.init();
      return engine;
    } catch (IOException|TransformerConfigurationException|XPathExpressionException ex) {
      throw new DataProcessorException("Error creating engine.", ex);
    }
  }
  
  // <editor-fold defaultstate="collapsed" desc="services factories">
  protected BrokersService createBrokersService() throws IOException, TransformerConfigurationException, XPathExpressionException {
    if (brokerService==null) {
      brokerService = new DefaultBrokersService(
              createInboundConnectorRegistry(), 
              createOutboundConnectorRegistry(), 
              createBrokerDefinitionManager());
    }
    return brokerService;
  }
  
  protected TemplatesService createTemplatesService() throws IOException, TransformerConfigurationException, XPathExpressionException {
    if (templatesService==null) {
      templatesService = new DefaultTemplatesService(
              createInboundConnectorRegistry(), 
              createOutboundConnectorRegistry(), 
              createTransformerRegistry(), 
              createFilterRegistry(), 
              createTriggerRegistry(), 
              createProcessorRegistry());
    }
    return templatesService;
  }
  
  protected TasksService createTasksService() throws IOException, TransformerConfigurationException, XPathExpressionException {
    if (taskService==null) {
      taskService = new DefaultTasksService(
              createTaskManager(), 
              createHistoryManager() 
      );
    }
    return taskService;
  }
  
  private ProcessesService createProcessesService() {
    if (processesService==null) {
      processesService = new DefaultProcessesService(
              createProcessManager(), 
              createReportManager(), 
              createStatisticsRegistry());
    }
    return processesService;
  }
  
  protected ExecutionService createExecutionService() throws IOException, TransformerConfigurationException, XPathExpressionException {
    if (executionService==null) {
      executionService = new DefaultExecutionService(
              createInboundConnectorRegistry(), 
              createOutboundConnectorRegistry(), 
              createTransformerRegistry(), 
              createFilterRegistry(), 
              createProcessorRegistry(), 
              createTriggerRegistry(), 
              createTriggerManager(), 
              createTriggerInstanceManager(), 
              createHistoryManager(), 
              createProcessesService());
    }
    return executionService;
  }
  
  protected TriggersService createTriggersService() throws IOException, TransformerConfigurationException, XPathExpressionException {
    if (triggersService==null) {
      triggersService = new DefaultTriggersService(
              createTriggerRegistry(), 
              createTriggerManager(), 
              createHistoryManager(), 
              createTriggerInstanceManager(), 
              createExecutionService());
    }
    return triggersService;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="managers factories">
  protected BrokerDefinitionManager createBrokerDefinitionManager() {
    return brokerDefinitionManager;
  }
  
  protected HistoryManager createHistoryManager() {
    return historyManager;
  }
  
  protected ReportManager createReportManager() {
    return reportManager;
  }
  
  protected ProcessManager createProcessManager() {
    return processManager;
  }
  
  protected TaskManager createTaskManager() {
    return taskManager;
  }
  
  protected TriggerInstanceManager createTriggerInstanceManager() {
    return triggerInstanceManager;
  }
  
  protected TriggerManager createTriggerManager() {
    return triggerManager;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="registries factories">
  protected FilterRegistry createFilterRegistry() {
    if (filterRegistry==null) {
      filterRegistry = new FilterRegistry();

      filterRegistry.put(RegExFilter.TYPE, new RegExFilter());
    }
    
    return filterRegistry;
  }
  
  protected InboundConnectorRegistry createInboundConnectorRegistry() throws IOException, TransformerConfigurationException {
    if (inboundConnectorRegistry==null) {
      inboundConnectorRegistry = new InboundConnectorRegistry();

      MetaBuilder metaBuilder = new SimpleDcMetaBuilder();

      inboundConnectorRegistry.put(CswConnector.TYPE, new CswConnector());
      inboundConnectorRegistry.put(WafConnector.TYPE, new WafConnector());
      inboundConnectorRegistry.put(UncConnector.TYPE, new UncConnector());
      inboundConnectorRegistry.put(AgpInputConnector.TYPE, new AgpInputConnector(metaBuilder));
      inboundConnectorRegistry.put(AgsConnector.TYPE, new AgsConnector(metaBuilder, new GeometryService(HttpClients.custom().build(), new URL(geometryServiceUrl))));
      inboundConnectorRegistry.put(GptConnector.TYPE, new GptConnector());
      inboundConnectorRegistry.put(CkanConnector.TYPE, new CkanConnector(metaBuilder));
      inboundConnectorRegistry.put(DataGovConnector.TYPE, new DataGovConnector(metaBuilder));
    }
    
    return inboundConnectorRegistry;
  }
  
  protected OutboundConnectorRegistry createOutboundConnectorRegistry() throws IOException, TransformerConfigurationException, XPathExpressionException {
    if (outboundConnectorRegistry==null) {
      outboundConnectorRegistry = new OutboundConnectorRegistry();

      MetaAnalyzer metaAnalyzer = new MultiMetaAnalyzerWrapper(
              new SimpleDcMetaAnalyzer(), 
              new SimpleFgdcMetaAnalyzer(), 
              new SimpleIso15115MetaAnalyzer(), 
              new SimpleIso15115_2MetaAnalyzer(), 
              new SimpleIso15119MetaAnalyzer());

      outboundConnectorRegistry.put(AgpOutputConnector.TYPE, new AgpOutputConnector(metaAnalyzer));
      outboundConnectorRegistry.put(ConsoleConnector.TYPE, new ConsoleConnector());
      outboundConnectorRegistry.put(FolderConnector.TYPE, new FolderConnector());
      outboundConnectorRegistry.put(com.esri.geoportal.harvester.gpt.GptConnector.TYPE, new com.esri.geoportal.harvester.gpt.GptConnector());
    }
    
    return outboundConnectorRegistry;
  }
  
  protected ProcessorRegistry createProcessorRegistry() {
    if (processorRegistry==null) {
      processorRegistry = new MemProcessorRegistry();
    }
    
    return processorRegistry;
  }
  
  protected StatisticsRegistry createStatisticsRegistry() {
    if (statisticsRegistry==null) {
      statisticsRegistry = new StatisticsRegistry();
    }
    return statisticsRegistry;
  }
  
  protected TransformerRegistry createTransformerRegistry() {
    if (transformerRegistry==null) {
      transformerRegistry = new TransformerRegistry();

      transformerRegistry.put(XsltTransformer.TYPE, new XsltTransformer());
    }
    
    return transformerRegistry;
  }
  
  protected TriggerRegistry createTriggerRegistry() {
    if (triggerRegistry==null) {
      triggerRegistry = new TriggerRegistry();

      triggerRegistry.put(NowTrigger.TYPE, new NowTrigger());
      triggerRegistry.put(AtTrigger.TYPE, new AtTrigger());
      triggerRegistry.put(PeriodTrigger.TYPE, new PeriodTrigger());
    }
    
    return triggerRegistry;
  }
  // </editor-fold>
}
