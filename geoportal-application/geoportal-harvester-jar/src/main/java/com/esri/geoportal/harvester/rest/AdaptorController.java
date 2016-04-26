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
package com.esri.geoportal.harvester.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.esri.geoportal.harvester.beans.EngineBean;
/**
 * Adaptor information controller.
 * <p>
 * It provides a way to obtain information about available adaptors (sources, 
 * destinations). There is a separate REST endpoint for source and destination.
 * <pre><code>
   /rest/harvester/adaptors/sources
   /rest/harvester/adaptors/destinations
 * </code></pre>
 * It would alway return a JSON array of strings, where each string is an
 * identifier of the adaptor, for example:
 * <pre><code>
   [ 
     "WAF, 
     "CSW" 
   ]
   
   or
   
   [ 
     "GPT", 
     "FOLDER" 
   ]
 * </code></pre>
 */
@RestController
public class AdaptorController {
  
  @Autowired
  private EngineBean engine;
  
  /**
   * Lists all source types. A source might be: WAF, CSW, etc.
   * @return 
   */
  @RequestMapping(value = "/rest/harvester/adaptors/sources", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public String[] listSourceTypes() {
    return engine.getSourcesTypes().toArray(new String[0]);
  }
  
  /**
   * Lists all destination types. A destination might be: GPT, FOLDER, etc.
   * @return 
   */
  @RequestMapping(value = "/rest/harvester/adaptors/destinations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public String[] listDestinationTypes() {
    return engine.getDestinationsTypes().toArray(new String[0]);
  }
}
