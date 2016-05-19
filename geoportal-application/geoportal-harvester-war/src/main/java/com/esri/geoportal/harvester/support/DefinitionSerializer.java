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
 * Task definition serializer.
 */
public class DefinitionSerializer {
  
  /**
   * De-serializes task definition.
   * <p>
   * Any entity definition within could be a full entity definition or a simplified
   * JSON object with only "uuid" attribute.
   * @param engine engine
   * @param taskDefinition task definition
   * @return task definition
   * @throws IOException if parsing task definition fails
   */
  public static TaskDefinition deserialize(Engine engine, String taskDefinition) throws IOException {
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

  /**
   * Serialize task definition into JSON.
   * @param taskDef task definition
   * @return serialized task definition
   * @throws JsonProcessingException if serializing fails
   */
  public static String serializeTaskDef(TaskDefinition taskDef) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(taskDef);
  }

  /**
   * De-serialize task definition.
   * @param strTaskDef JSON form of task definition
   * @return task definition
   * @throws IOException if de-serializing task definition fails
   */
  public static TaskDefinition deserializeTaskDef(String strTaskDef) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(strTaskDef, TaskDefinition.class);
  }

  /**
   * Serialize entity definition into JSON.
   * @param entityDef entity definition
   * @return serialized entity definition
   * @throws JsonProcessingException if serializing fails
   */
  public static String serializeEntityDef(EntityDefinition entityDef) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(entityDef);
  }

  /**
   * De-serialize entity definition.
   * @param strEntityDef JSON form of entity definition
   * @return entity definition
   * @throws IOException if de-serializing entity definition fails
   */
  public static EntityDefinition deserializeEntityDef(String strEntityDef) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(strEntityDef, EntityDefinition.class);
  }
}
