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
 * Filter controller.
 * Provides access to filter information.
 * <pre><code>
   GET /rest/harvester/filters          - gets a list of all filters
 * </code></pre>
 */
@RestController
public class FilterController {
  private static final Logger LOG = LoggerFactory.getLogger(FilterController.class);
  
  @Autowired
  private EngineBean engine;
  
  /**
   * Lists all filters.
   * @return array of filter templates
   */
  @RequestMapping(value = "/rest/harvester/filters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public UITemplate[] list() {
    LOG.debug(String.format("GET /rest/harvester/filters"));
    return engine.getTemplatesService().getFilterTemplates().toArray(new UITemplate[0]);
  }
  
}
