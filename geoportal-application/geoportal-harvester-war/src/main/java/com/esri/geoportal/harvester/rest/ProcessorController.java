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
package com.esri.geoportal.harvester.rest;

import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.beans.EngineBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Processor controller.
 * Provides access to processor's information.
 * <pre><code>
   GET /rest/harvester/processors/types      - gets a list of all processors
 * </code></pre>
 */
@RestController
public class ProcessorController {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessorController.class);
  
  @Autowired
  private EngineBean engine;
  
  /**
   * Lists all processors.
   * @return array of processors templates
   */
  @RequestMapping(value = "/rest/harvester/processors/types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public UITemplate[] listProcessorTypes() {
    LOG.debug(String.format("GET /rest/harvester/processors/types"));
    return engine.getProcessorsTemplates().toArray(new UITemplate[0]);
  }
  
}
