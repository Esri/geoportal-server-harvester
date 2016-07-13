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
package com.esri.geoportal.harvester.engine.support;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.LinkDefinition;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Link definition deserializer.
 */
public class LinkDefinitionDeserializer extends JsonDeserializer<LinkDefinition> {

  @Override
  public LinkDefinition deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectCodec codec = p.getCodec();
    TreeNode treeNode = codec.readTree(p);
    
    LinkDefinition linkDefinition = new LinkDefinition();
    
    if (treeNode.isArray()) {
    
      TreeNode actionNode = treeNode.get("action");
      TreeNode drainsNode = treeNode.get("drains");
      
      EntityDefinition actionDefinition = actionNode.traverse(codec).readValueAs(EntityDefinition.class);
      linkDefinition.setAction(actionDefinition);
      
      List<LinkDefinition> drainsDefinition = drainsNode.traverse(codec).readValueAs(List.class);
      linkDefinition.setDrains(drainsDefinition);
      
    } else if (treeNode.isObject()) {
      
      EntityDefinition actionDefinition = treeNode.traverse(codec).readValueAs(EntityDefinition.class);
      linkDefinition.setAction(actionDefinition);
      
      linkDefinition.setDrains(Collections.emptyList());
      
    } else {
      throw new JsonParseException(p, "Error parsing LinkDefinition");
    }

    return linkDefinition;
  }
  
}
