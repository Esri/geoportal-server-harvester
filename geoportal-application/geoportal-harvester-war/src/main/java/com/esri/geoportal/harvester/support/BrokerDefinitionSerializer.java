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
package com.esri.geoportal.harvester.support;

import com.esri.geoportal.harvester.api.BrokerDefinition;
import com.esri.geoportal.harvester.waf.WafBrokerDefinitionAdaptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;

/**
 * Broker definition serializer.
 */
public class BrokerDefinitionSerializer {

  /**
   * Serialize broker definition into JSON.
   * @param brokerDef broker definition
   * @return serialized broker definition
   * @throws JsonProcessingException if serializing fails
   */
  public static String serializeBrokerDef(BrokerDefinition brokerDef) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(brokerDef);
  }

  /**
   * De-serialize task definition.
   * @param strBrokerDef JSON form of broker definition
   * @return broker definition
   * @throws IOException if de-serializing task definition fails
   */
  public static BrokerDefinition deserializeBrokerDef(String strBrokerDef) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(strBrokerDef, BrokerDefinition.class);
  }
}
