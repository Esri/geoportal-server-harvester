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

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.engine.BrokerInfo;
import com.esri.geoportal.harvester.engine.Engine;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.UUID;

/**
 * Task definition JSON utility.
 */
public class TaskDefinitionJsonUtility {
  private final Engine engine;

  /**
   * Creates instance of the utility.
   * @param engine engine
   */
  public TaskDefinitionJsonUtility(Engine engine) {
    this.engine = engine;
  }
  
  /**
   * Deserializes task definition.
   * <p>
   * Any entity definition within could be a full entity definition or a simplified
   * JSON object with only "uuid" attribute.
   * @param taskDefinition task definition
   * @return task definition
   * @throws IOException if parsing task definition fails
   */
  public TaskDefinition deserialize(String taskDefinition) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    JsonDeserializer<EntityDefinition> deserializer = new JsonDeserializer<EntityDefinition>() {
      @Override
      public EntityDefinition deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        ObjectCodec oc = jp.getCodec();
        TreeNode treeNode = oc.readTree(jp);
        TreeNode uuidNode = treeNode.get("uuid");
        if (uuidNode!=null) {
          String sUuid = uuidNode.toString();
          try {
            UUID uuid = UUID.fromString(sUuid);
            BrokerInfo broker = engine.findBroker(uuid);
            if (broker==null) {
              throw new JsonParseException(jp,String.format("Invalid uuid: %s", sUuid));
            }
            return broker.getBrokerDefinition();
          } catch (IllegalArgumentException ex) {
            throw new JsonParseException(jp,String.format("Invalid uuid: %s", sUuid), ex);
          }
        } else {
          ObjectMapper localMapper = new ObjectMapper();
          EntityDefinition def = localMapper.treeToValue(treeNode, EntityDefinition.class);
          return def;
        }
      }
    };
    module.addDeserializer(EntityDefinition.class, deserializer);
    mapper.registerModule(module);
    
    return mapper.readValue(taskDefinition, TaskDefinition.class);
  }
}
