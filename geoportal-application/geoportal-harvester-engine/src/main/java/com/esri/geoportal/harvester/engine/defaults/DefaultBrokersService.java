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
package com.esri.geoportal.harvester.engine.defaults;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.engine.services.BrokersService;
import com.esri.geoportal.harvester.engine.managers.BrokerDefinitionManager;
import com.esri.geoportal.harvester.engine.registers.InboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.registers.OutboundConnectorRegistry;
import com.esri.geoportal.harvester.engine.utils.BrokerReference;
import static com.esri.geoportal.harvester.engine.utils.BrokerReference.Category.INBOUND;
import static com.esri.geoportal.harvester.engine.utils.BrokerReference.Category.OUTBOUND;
import com.esri.geoportal.harvester.engine.utils.CrudlException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
  public List<BrokerReference> getBrokersDefinitions(BrokerReference.Category category, Locale locale) throws DataProcessorException {
    if (category != null) {
      try {
        Set<String> brokerTypes = listTypesByCategory(category, locale);
        return brokerDefinitionManager.list().stream()
                .filter(e -> brokerTypes.contains(e.getValue().getType()))
                .map(e -> new BrokerReference(e.getKey(), category, e.getValue()))
                .collect(Collectors.toList());
      } catch (CrudlException ex) {
        throw new DataProcessorException(String.format("Error getting brokers for category: %s", category), ex);
      }
    } else {
      return Stream.concat(getBrokersDefinitions(INBOUND,locale).stream(), getBrokersDefinitions(OUTBOUND,locale).stream()).collect(Collectors.toList());
    }
  }

  @Override
  public BrokerReference findBroker(UUID brokerId, Locale locale) throws DataProcessorException {
    try {
      EntityDefinition brokerDefinition = brokerDefinitionManager.read(brokerId);
      if (brokerDefinition != null) {
        BrokerReference.Category category = getBrokerCategoryByType(brokerDefinition.getType(), locale);
        if (category != null) {
          return new BrokerReference(brokerId, category, brokerDefinition);
        }
      }
      return null;
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error finding broker: %s", brokerId), ex);
    }
  }

  @Override
  public BrokerReference createBroker(EntityDefinition brokerDefinition, Locale locale) throws DataProcessorException {
    BrokerReference.Category category = getBrokerCategoryByType(brokerDefinition.getType(), locale);
    if (category != null) {
      try {
        UUID id = brokerDefinitionManager.create(brokerDefinition);
        return new BrokerReference(id, category, brokerDefinition);
      } catch (CrudlException ex) {
        throw new DataProcessorException(String.format("Error creating broker: %s", brokerDefinition), ex);
      }
    }
    return null;
  }

  @Override
  public BrokerReference updateBroker(UUID brokerId, EntityDefinition brokerDefinition, Locale locale) throws DataProcessorException {
    try {
      EntityDefinition oldBrokerDef = brokerDefinitionManager.read(brokerId);
      if (oldBrokerDef != null) {
        if (!brokerDefinitionManager.update(brokerId, brokerDefinition)) {
          oldBrokerDef = null;
        }
      }
      BrokerReference.Category category = oldBrokerDef != null ? getBrokerCategoryByType(oldBrokerDef.getType(), locale) : null;
      return category != null ? new BrokerReference(brokerId, category, brokerDefinition) : null;
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error updating broker: %s <-- %s", brokerId, brokerDefinition), ex);
    }
  }

  @Override
  public boolean deleteBroker(UUID brokerId) throws DataProcessorException {
    try {
      return brokerDefinitionManager.delete(brokerId);
    } catch (CrudlException ex) {
      throw new DataProcessorException(String.format("Error deleting broker: %s", brokerId), ex);
    }
  }

  /**
   * Lists types by category.
   *
   * @param category category
   * @param locale locale
   * @return set of types within the category
   */
  private Set<String> listTypesByCategory(BrokerReference.Category category, Locale locale) {
    List<UITemplate> templates
            = category == INBOUND ? inboundConnectorRegistry.getTemplates(locale)
                    : category == OUTBOUND ? outboundConnectorRegistry.getTemplates(locale)
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
   * @param locale locale
   * @return broker category or <code>null</code> if category couldn't be
   * determined
   */
  private BrokerReference.Category getBrokerCategoryByType(String brokerType, Locale locale) {
    Set<String> inboundTypes = inboundConnectorRegistry.getTemplates(locale).stream().map(t -> t.getType()).collect(Collectors.toSet());
    Set<String> outboundTypes = outboundConnectorRegistry.getTemplates(locale).stream().map(t -> t.getType()).collect(Collectors.toSet());
    return inboundTypes.contains(brokerType) ? INBOUND : outboundTypes.contains(brokerType) ? OUTBOUND : null;
  }
}
