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
import com.esri.geoportal.harvester.support.ProcessStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
@Tag(name = "Processor Controller", description = "Provides access to processor's information.")
public class ProcessorController {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessorController.class);
  
  @Autowired
  private Engine engine;
  
  /**
   * Lists all processors.
   * @return array of processors templates
   */
   @Operation(description = "Lists all processeors (array of processors templates).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operation is successful",
                     content = @Content( mediaType = "application/json", 
                     array = @ArraySchema(    
                             schema = @Schema(implementation = UITemplate.class)))
                    ),        
        @ApiResponse(responseCode = "500", description = "Inetrnal Server Error.",content = @Content(schema = @Schema()))
    })
  @RequestMapping(value = "/rest/harvester/processors/types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public UITemplate[] listProcessorTypes() {
    LOG.debug(String.format("GET /rest/harvester/processors/types"));
    return engine.getTemplatesService().getProcessorsTemplates(LocaleContextHolder.getLocale()).toArray(new UITemplate[0]);
  }
  
}
