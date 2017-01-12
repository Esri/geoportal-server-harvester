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
package com.esri.geoportal.harvester.api.defs;

import static com.esri.geoportal.commons.constants.CredentialsConstants.P_CRED_PASSWORD;
import com.esri.geoportal.commons.utils.TextScrambler;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Entity definition.
 * <p>
 * It is a map of properties mean to be serializable. Based upon this information,
 * a {@link com.esri.geoportal.harvester.api.Connector} will be able to produce an 
 * instance of the {@link com.esri.geoportal.harvester.api.Broker}, or a 
 * {@link com.esri.geoportal.harvester.api.Processor} can be produced by a factory.
 * <p>
 * This class must be extended with the class providing explicit methods translating
 * from/to the named attribute within the map. For example, a concretized class
 * may provide a pair of getter and setter: getHostUrl() and setHostUrl() which 
 * return or accept an argument of type URL. This value will be serialized into 
 * string and stored within the map under predetermined key.
 * 
 * @see com.esri.geoportal.harvester.api.Connector
 * @see com.esri.geoportal.harvester.api.Broker
 */
@JsonSerialize(using = EntityDefinition.Serializer.class)
@JsonDeserialize(using = EntityDefinition.Deserializer.class)
public final class EntityDefinition implements Serializable {
  private String type;
  private String label;
  private Map<String,String> properties = new LinkedHashMap<>();
  private List<String> keywords = new ArrayList<>();

  /**
   * Gets broker type.
   * @return broker type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets broker type.
   * @param type broker type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets label.
   * @return label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets label.
   * @param label label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Gets broker properties.
   * @return broker properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Sets broker properties.
   * @param properties broker properties
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = properties!=null? properties: new LinkedHashMap<>();
  }

  /**
   * Gets keywords.
   * @return keywords
   */
  public List<String> getKeywords() {
    return keywords;
  }

  /**
   * Sets keywords.
   * @param keywords keywords
   */
  public void setKeywords(List<String> keywords) {
    this.keywords = keywords!=null? keywords: new ArrayList<>();
  }
  
  @Override
  public String toString() {
    return String.format("%s[%s]", type, properties.entrySet().stream()
            .map(e->String.format("%s=%s", e.getKey(), P_CRED_PASSWORD.equals(e.getKey())? "*****": e.getValue()))
            .collect(Collectors.joining(", ")));
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof EntityDefinition) {
      EntityDefinition ed = (EntityDefinition)o;
      return ((getType()!=null && ed.getType()!=null && getType().equals(ed.getType()) || (getType()==null && ed.getType()==null))) && 
              getProperties().equals(ed.getProperties());
    }

    return false;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 89 * hash + Objects.hashCode(this.type);
    hash = 89 * hash + Objects.hashCode(this.properties);
    return hash;
  }
  
  /**
   * Serializer.
   */
  public static final class Serializer extends JsonSerializer<EntityDefinition> {

    @Override
    public void serialize(EntityDefinition value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
      gen.writeStartObject();
      
      gen.writeFieldName("type");
      gen.writeString(value.type);
      
      gen.writeFieldName("label");
      gen.writeString(value.label);
      
      gen.writeObjectFieldStart("properties");
      for (Map.Entry<String,String> e: value.properties.entrySet()) {
        gen.writeFieldName(e.getKey());
        gen.writeString(P_CRED_PASSWORD.equals(e.getKey()) && !e.getValue().isEmpty()? TextScrambler.encode(e.getValue()): e.getValue());
      }
      gen.writeEndObject();
      
      gen.writeArrayFieldStart("keywords");
      for (String k: value.keywords) {
        gen.writeString(k);
      }
      gen.writeEndArray();
      
      
      gen.writeEndObject();
    }
    
  }
  
  /**
   * Deserializer.
   */
  public static final class Deserializer extends JsonDeserializer<EntityDefinition> {

    @Override
    public EntityDefinition deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonNode node = jp.getCodec().readTree(jp);
      
      EntityDefinition ed = new EntityDefinition();
      
      ed.type = node.has("type")? node.get("type").asText(): "";
      ed.label = node.has("label")? node.get("label").asText(): "";
      
      if (node.has("properties") && node.get("properties").isObject()) {
        JsonNode properties = node.get("properties");
        Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
        while (fields.hasNext()) {
          Map.Entry<String, JsonNode> next = fields.next();
          ed.properties.put(next.getKey(), P_CRED_PASSWORD.equals(next.getKey())? TextScrambler.decode(next.getValue().asText()): next.getValue().asText());
        }
      }
      
      if (node.has("keywords") && node.get("keywords").isArray()) {
        JsonNode keywords = node.get("keywords");
        for (int i=0; i<keywords.size(); i++) {
          ed.keywords.add(keywords.get(i).asText());
        }
      }
      
      return ed;
    }
    
  }
}
