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

import com.esri.geoportal.harvester.api.DataAdaptorTemplate;
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
 * It would alway return a JSON array of templates, where each template is a
 * blueprint how to build UI for each adaptor, for example:
 * <pre><code>
   [ 
      {
        name: "WAF",
        label: "Web accessible folder",
        arguments: [
          {
            name: "waf.host.url",
            label: "URL",
            type: "string"
          }
        ]
      }, 
      {
        name: "CSW",
        label: "Catalogue service for the web",
        arguments: [
        {
          name: "csw.host.url",
          label: "URL",
          type: "string"
        },
        {
        name: "csw.profile.id",
        label: "Profile",
        choices: [
          {
            name: "urn:ogc:CSW:2.0.1:HTTP:OGCCORE:GeoNetwork",
            value: "GeoNetwork CSW 2.0.1 OGCCORE"
          },
          {
            name: "urn:ogc:CSW:2.0.2:HTTP:APISO:PYCSW",
            value: "pycsw"
          },
          {
            name: "urn:ogc:CSW:2.0.2:HTTP:OGCCORE:ESRI:GPT",
            value: "ArcGIS Server Geoportal Extension (GPT)"
          },
          {
            name: "urn:ogc:CSW:2.0.2:HTTP:APISO:GeoNetwork",
            value: "GeoNetwork CSW 2.0.2 APISO"
          },
          {
            name: "urn:ogc:CSW:2.0.2:HTTP:OGCISO:ESRI:GPT",
            value: "ArcGIS Server Geoportal Extension (version 10) CSW ISO AP"
          },
        ],
        default: "urn:ogc:CSW:2.0.2:HTTP:OGCCORE:ESRI:GPT",
        type: "choice"
        }
        ]
        } 
   ]
   
   or
   
   [ 
      {
        name: "GPT",
        label: "Geoportal Server New Generation",
        arguments: [
          {
            name: "gpt.host.url",
            label: "URL",
            type: "string"
          },
          {
            name: "gpt.user.name",
            label: "User name",
            type: "string"
          },
          {
            name: "gpt.user.password",
            label: "User password",
            password: true,
            type: "string"
          }
        ]
      }, 
      {
        name: "FOLDER",
        label: "Folder",
        arguments: [
          {
            name: "folder.root.folder",
            label: "Root folder",
            type: "string"
          },
          {
            name: "folder.host.url",
            label: "Source host URL",
            type: "string"
          }
        ]
      } 
   ]
 * </code></pre>
 */
@RestController
public class AdaptorController {
  
  @Autowired
  private EngineBean engine;
  
  /**
   * Lists all source types. A source might be: WAF, CSW, etc.
   * @return array of source types
   */
  @RequestMapping(value = "/rest/harvester/adaptors/sources", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public DataAdaptorTemplate[] listSourceTypes() {
    return engine.getSourcesTypes().toArray(new DataAdaptorTemplate[0]);
  }
  
  /**
   * Lists all destination types. A destination might be: GPT, FOLDER, etc.
   * @return array of destination types
   */
  @RequestMapping(value = "/rest/harvester/adaptors/destinations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public DataAdaptorTemplate[] listDestinationTypes() {
    return engine.getDestinationsTypes().toArray(new DataAdaptorTemplate[0]);
  }
}
