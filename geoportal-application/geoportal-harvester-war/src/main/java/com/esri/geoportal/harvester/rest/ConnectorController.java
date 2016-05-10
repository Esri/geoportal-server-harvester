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

import com.esri.geoportal.harvester.api.ConnectorTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.esri.geoportal.harvester.beans.EngineBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
/**
 * Connector information controller.
 * <p>
 * It provides a way to obtain information about available adaptors (sources, 
 * destinations). There is a separate REST endpoint for source and destination.
 * <pre><code>
   /rest/harvester/connectors/inbound
   /rest/harvester/connectors/outbound
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
public class ConnectorController {
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorController.class);
  
  @Autowired
  private EngineBean engine;
  
  /**
   * Lists all inbound connectors. A connector might be: WAF, CSW, etc.
   * @return array of connector templates
   */
  @RequestMapping(value = "/rest/harvester/connectors/inbound", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ConnectorTemplate[] listInboundConnectors() {
    LOG.debug(String.format("GET /rest/harvester/connectors/inbound"));
    return engine.getInboundConnectorTemplates().toArray(new ConnectorTemplate[0]);
  }
  
  /**
   * Lists all outbound connectors. A connector might be: GPT, FOLDER, etc.
   * @return array of connector templates
   */
  @RequestMapping(value = "/rest/harvester/connectors/outbound", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ConnectorTemplate[] listOutboundConnectors() {
    LOG.debug(String.format("GET /rest/harvester/connectors/outbound"));
    return engine.getOutboundConnectorTemplates().toArray(new ConnectorTemplate[0]);
  }
  
  /**
   * Get single inbound connector.
   * @param name id of the connector
   * @return connector template
   */
  @RequestMapping(value = "/rest/harvester/connectors/inbound/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ConnectorTemplate getInboundConnector(@PathVariable String name) {
    return engine.getInboundConnectorTemplates().stream().filter(a->a.getType().equals(name)).findFirst().orElse(null);
  }
  
  /**
   * Get single outbound connector.
   * @param name id of the connector
   * @return connector template
   */
  @RequestMapping(value = "/rest/harvester/connectors/outbound/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ConnectorTemplate getOutboundConnector(@PathVariable String name) {
    return engine.getOutboundConnectorTemplates().stream().filter(a->a.getType().equals(name)).findFirst().orElse(null);
  }
}
