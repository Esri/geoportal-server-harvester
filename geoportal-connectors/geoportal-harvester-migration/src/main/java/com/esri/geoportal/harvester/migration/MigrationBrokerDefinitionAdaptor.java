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

import com.esri.geoportal.harvester.api.base.BrokerDefinitionAdaptor;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import static com.esri.geoportal.harvester.migration.MigrationConstants.P_JNDI_NAME;
import static com.esri.geoportal.harvester.migration.MigrationConstants.P_PRESERVE_UUIDS;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Migration broker definition adaptor.
 */
/*package*/ class MigrationBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {
  private String jndi;
  private boolean preserveUuids;

  /**
   * Creates instance of the adaptor.
   *
   * @param def broker definition
   * @throws InvalidDefinitionException if invalid definition
   */
  public MigrationBrokerDefinitionAdaptor(EntityDefinition def) throws InvalidDefinitionException {
    super(def);
    if (StringUtils.trimToEmpty(def.getType()).isEmpty()) {
      def.setType(MigrationConnector.TYPE);
    } else if (!MigrationConnector.TYPE.equals(def.getType())) {
      throw new InvalidDefinitionException("Broker definition doesn't match");
    } else {
      jndi = get(P_JNDI_NAME);
      preserveUuids = BooleanUtils.toBoolean(get(P_PRESERVE_UUIDS));
    }
  }

  @Override
  public void override(Map<String, String> params) {
    consume(params,P_JNDI_NAME);
  }

  public String getJndi() {
    return jndi;
  }

  public void setJndi(String jndi) {
    this.jndi = jndi;
    set(P_JNDI_NAME,jndi);
  }

  public boolean getPreserveUuids() {
    return preserveUuids;
  }

  public void setPreserveUuids(boolean preserveUuids) {
    this.preserveUuids = preserveUuids;
    set(P_PRESERVE_UUIDS,Boolean.toString(preserveUuids));
  }
}
