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
package com.esri.geoportal.harvester.engine.impl;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.engine.BrokersService;
import com.esri.geoportal.harvester.engine.managers.BrokerDefinitionManager;
import com.esri.geoportal.harvester.engine.managers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.managers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.support.BrokerReference;
import static com.esri.geoportal.harvester.engine.support.BrokerReference.Category.INBOUND;
import static com.esri.geoportal.harvester.engine.support.BrokerReference.Category.OUTBOUND;
import com.esri.geoportal.harvester.engine.support.CrudsException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default brokers service.
 */
public class DefaultBrokersService implements BrokersService {
  protected final InboundConnectorRegistry inboundConnectorRegistry;
  protected final OutboundConnectorRegistry outboundConnectorRegistry;
  protected final BrokerDefinitionManager brokerDefinitionManager;

  /**
   * Creates instance of the service.
   * @param inboundConnectorRegistry inbound connector registry.
   * @param outboundConnectorRegistry outbound connector registry
   * @param brokerDefinitionManager  broker definition manager
   */
  public DefaultBrokersService(InboundConnectorRegistry inboundConnectorRegistry, OutboundConnectorRegistry outboundConnectorRegistry, BrokerDefinitionManager brokerDefinitionManager) {
    this.inboundConnectorRegistry = inboundConnectorRegistry;
    this.outboundConnectorRegistry = outboundConnectorRegistry;
    this.brokerDefinitionManager = brokerDefinitionManager;
  }

  @Override
  public List<BrokerReference> getBrokersDefinitions(BrokerReference.Category category) throws DataProcessorException {
    if (category != null) {
      try {
        Set<String> brokerTypes = listTypesByCategory(category);
        return brokerDefinitionManager.select().stream()
                .filter(e -> brokerTypes.contains(e.getValue().getType()))
                .map(e -> new BrokerReference(e.getKey(), category, e.getValue()))
                .collect(Collectors.toList());
      } catch (CrudsException ex) {
        throw new DataProcessorException(String.format("Error getting brokers for category: %s", category), ex);
      }
    } else {
      return Stream.concat(getBrokersDefinitions(INBOUND).stream(), getBrokersDefinitions(OUTBOUND).stream()).collect(Collectors.toList());
    }
  }

  @Override
  public BrokerReference findBroker(UUID brokerId) throws DataProcessorException {
    try {
      EntityDefinition brokerDefinition = brokerDefinitionManager.read(brokerId);
      if (brokerDefinition != null) {
        BrokerReference.Category category = getBrokerCategoryByType(brokerDefinition.getType());
        if (category != null) {
          return new BrokerReference(brokerId, category, brokerDefinition);
        }
      }
      return null;
    } catch (CrudsException ex) {
      throw new DataProcessorException(String.format("Error finding broker: %s", brokerId), ex);
    }
  }

  @Override
  public BrokerReference createBroker(EntityDefinition brokerDefinition) throws DataProcessorException {
    BrokerReference.Category category = getBrokerCategoryByType(brokerDefinition.getType());
    if (category != null) {
      try {
        UUID id = brokerDefinitionManager.create(brokerDefinition);
        return new BrokerReference(id, category, brokerDefinition);
      } catch (CrudsException ex) {
        throw new DataProcessorException(String.format("Error creating broker: %s", brokerDefinition), ex);
      }
    }
    return null;
  }

  @Override
  public BrokerReference updateBroker(UUID brokerId, EntityDefinition brokerDefinition) throws DataProcessorException {
    try {
      EntityDefinition oldBrokerDef = brokerDefinitionManager.read(brokerId);
      if (oldBrokerDef != null) {
        if (!brokerDefinitionManager.update(brokerId, brokerDefinition)) {
          oldBrokerDef = null;
        }
      }
      BrokerReference.Category category = oldBrokerDef != null ? getBrokerCategoryByType(oldBrokerDef.getType()) : null;
      return category != null ? new BrokerReference(brokerId, category, brokerDefinition) : null;
    } catch (CrudsException ex) {
      throw new DataProcessorException(String.format("Error updating broker: %s <-- %s", brokerId, brokerDefinition), ex);
    }
  }

  @Override
  public boolean deleteBroker(UUID brokerId) throws DataProcessorException {
    try {
      return brokerDefinitionManager.delete(brokerId);
    } catch (CrudsException ex) {
      throw new DataProcessorException(String.format("Error deleting broker: %s", brokerId), ex);
    }
  }

  /**
   * Lists types by category.
   *
   * @param category category
   * @return set of types within the category
   */
  private Set<String> listTypesByCategory(BrokerReference.Category category) {
    List<UITemplate> templates
            = category == INBOUND ? inboundConnectorRegistry.getTemplates()
                    : category == OUTBOUND ? outboundConnectorRegistry.getTemplates()
                            : null;

    if (templates != null) {
      return templates.stream().map(t -> t.getType()).collect(Collectors.toSet());
    } else {
      return Collections.EMPTY_SET;
    }
  }

  /**
   * Gets broker category by broker type.
   *
   * @param brokerType broker type
   * @return broker category or <code>null</code> if category couldn't be
   * determined
   */
  private BrokerReference.Category getBrokerCategoryByType(String brokerType) {
    Set<String> inboundTypes = inboundConnectorRegistry.getTemplates().stream().map(t -> t.getType()).collect(Collectors.toSet());
    Set<String> outboundTypes = outboundConnectorRegistry.getTemplates().stream().map(t -> t.getType()).collect(Collectors.toSet());
    return inboundTypes.contains(brokerType) ? INBOUND : outboundTypes.contains(brokerType) ? OUTBOUND : null;
  }
}
