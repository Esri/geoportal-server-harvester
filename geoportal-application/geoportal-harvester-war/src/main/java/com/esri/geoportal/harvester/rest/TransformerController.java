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
import com.esri.geoportal.harvester.engine.services.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transformer controller.
 * Provides access to transformer information.
 * <pre><code>
   GET /rest/harvester/transformers          - gets a list of all transformers
 * </code></pre>
 */
@RestController
public class TransformerController {
  private static final Logger LOG = LoggerFactory.getLogger(TransformerController.class);
  
  @Autowired
  private Engine engine;
  
  /**
   * Lists all transformers.
   * @return array of transformer templates
   */
  @RequestMapping(value = "/rest/harvester/transformers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public UITemplate[] list() {
    LOG.debug(String.format("GET /rest/harvester/transformers"));
    return engine.getTemplatesService().getTransformerTemplates().toArray(new UITemplate[0]);
  }
  
}
