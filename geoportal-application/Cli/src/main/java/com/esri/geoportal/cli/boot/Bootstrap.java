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
import com.esri.geoportal.harvester.agp.AgpOutputConnector;
import com.esri.geoportal.harvester.agpsrc.AgpInputConnector;
import com.esri.geoportal.harvester.ags.AgsConnector;
import com.esri.geoportal.harvester.console.ConsoleConnector;
import com.esri.geoportal.harvester.csw.CswConnector;
import com.esri.geoportal.harvester.engine.defaults.DefaultBrokersService;
import com.esri.geoportal.harvester.engine.defaults.DefaultProcessor;
import com.esri.geoportal.harvester.engine.filters.RegExFilter;
import com.esri.geoportal.harvester.engine.managers.BrokerDefinitionManager;
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
import com.esri.geoportal.harvester.engine.transformers.XsltTransformer;
import com.esri.geoportal.harvester.engine.triggers.AtTrigger;
import com.esri.geoportal.harvester.engine.triggers.NowTrigger;
import com.esri.geoportal.harvester.engine.triggers.PeriodTrigger;
import com.esri.geoportal.harvester.folder.FolderConnector;
import com.esri.geoportal.harvester.gptsrc.GptConnector;
import com.esri.geoportal.harvester.unc.UncConnector;
import com.esri.geoportal.harvester.waf.WafConnector;
import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Bootstrap.
 */
public class Bootstrap {

  private TriggerRegistry triggerRegistry;
  private TransformerRegistry transformerRegistry;
  private StatisticsRegistry statisticsRegistry;
  private ProcessorRegistry processorRegistry;
  private OutboundConnectorRegistry outboundConnectorRegistry;
  private InboundConnectorRegistry inboundConnectorRegistry;
  private FilterRegistry filterRegistry;
  
  /**
   * Creates engine.
   * @return engine.
   */
  public Engine createEngine() {
    return null;
  }
  
  // <editor-fold defaultstate="collapsed" desc="services factories">
  protected BrokersService createBrokersService() throws IOException, TransformerConfigurationException, XPathExpressionException {
    BrokersService srv = new DefaultBrokersService(
            createInboundConnectorRegistry(), 
            createOutboundConnectorRegistry(), 
            createBrokerDefinitionManager());
    return srv;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="managers factories">
  protected BrokerDefinitionManager createBrokerDefinitionManager() {
    BrokerDefinitionManager mgr = new MemBrokerDefinitionManager();    
    return mgr;
  }
  
  protected ReportManager createReportManager() {
    ReportManager mgr = new MemReportManager();
    return mgr;
  }
  
  protected ProcessManager createProcessManager() {
    ProcessManager mgr = new MemProcessManager();
    return mgr;
  }
  
  protected TaskManager createTaskManager() {
    TaskManager mgr = new MemTaskManager();
    return mgr;
  }
  
  protected TriggerInstanceManager createTriggerInstanceManager() {
    TriggerInstanceManager mgr = new MemTriggerInstanceManager();
    return mgr;
  }
  
  protected TriggerManager createTriggerManager() {
    TriggerManager mgr = new MemTriggerManager();
    return mgr;
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
      inboundConnectorRegistry.put(AgsConnector.TYPE, new AgsConnector(metaBuilder));
      inboundConnectorRegistry.put(GptConnector.TYPE, new GptConnector());
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
      processorRegistry = new ProcessorRegistry();

      processorRegistry.put(DefaultProcessor.TYPE, new DefaultProcessor());
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
