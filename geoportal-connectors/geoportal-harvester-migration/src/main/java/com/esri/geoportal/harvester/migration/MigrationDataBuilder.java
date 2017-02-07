/*
 * Copyright 2017 Esri, Inc.
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
package com.esri.geoportal.harvester.migration;

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Migration data builder.
 */
/*package*/ class MigrationDataBuilder {

  private final MigrationBrokerDefinitionAdaptor definition;
  private final URI brokerUri;
  private final Map<Integer, String> userMap;
  private final Map<String, MigrationHarvestSite> sites;

  /**
   * Creates instance of the builder.
   *
   * @param definition broker definition
   * @param brokerUri broker URI
   * @param userMap user map
   * @param sites sites map
   */
  public MigrationDataBuilder(MigrationBrokerDefinitionAdaptor definition, URI brokerUri, Map<Integer, String> userMap, Map<String, MigrationHarvestSite> sites) {
    this.definition = definition;
    this.brokerUri = brokerUri;
    this.userMap = userMap;
    this.sites = sites;
  }

  public DataReference buildReference(MigrationData data, String xml) throws IOException, URISyntaxException {
    SimpleDataReference ref = new SimpleDataReference(
            brokerUri,
            definition.getEntityDefinition().getLabel(),
            data.docuuid,
            data.updateDate,
            createSourceUri(data),
            xml.getBytes("UTF-8"),
            MimeType.APPLICATION_XML
    );
    String owner = userMap.get(data.owner);
    if (owner != null) {
      ref.getAttributesMap().put("owner", owner);
    }
    return ref;
  }

  public URI createSourceUri(MigrationData data) throws URISyntaxException {
    MigrationHarvestSite site = data.siteuuid!=null? sites.get(data.siteuuid): null;
    if (site!=null) {
      String type = StringUtils.trimToEmpty(site.type).toUpperCase();
      switch (type) {
        case "CSW":
          return new URI("uuid",data.sourceuri,null);
      }
    }
    return URI.create(data.sourceuri);
  }
}
