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
package com.esri.geoportal.harvester.engine.utils;

import com.esri.geoportal.harvester.api.base.CredentialsDefinitionAdaptor;
import com.esri.geoportal.harvester.api.base.TextScrambler;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * JSON serializer/deserializer utility class.
 */
public class JsonSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
      SimpleModule simMod = new SimpleModule();
      // TODO uncomment in final version
      //simMod.addSerializer(String.class, new StringSerializer());
      //simMod.addDeserializer(String.class, new StringDeserializer());
      mapper.registerModule(simMod);
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

  /**
   * Serialize object into JSON.
   * @param def definition to serialize
   * @return serialized definition
   * @throws IOException if serializing fails
   */
  public static String serialize(Object def) throws IOException {
    return mapper.writeValueAsString(def);
  }

  /**
   * De-serialize task definition.
   * @param <T> type of the definition
   * @param strDef JSON form of definition
   * @param clazz class of the definition
   * @return definition
   * @throws IOException if de-serializing definition fails
   */
  public static <T> T deserialize(String strDef, Class<T> clazz) throws IOException {
    return mapper.readValue(strDef, clazz);
  }

  /**
   * De-serialize task definition.
   * @param <T> type of the definition
   * @param reader reader
   * @param clazz class of the definition
   * @return definition
   * @throws IOException if de-serializing definition fails
   */
  public static <T> T deserialize(Reader reader, Class<T> clazz) throws IOException {
    return mapper.readValue(reader, clazz);
  }

  /**
   * De-serialize task definition.
   * @param <T> type of the definition
   * @param inputStream input stream
   * @param clazz class of the definition
   * @return definition
   * @throws IOException if de-serializing definition fails
   */
  public static <T> T deserialize(InputStream inputStream, Class<T> clazz) throws IOException {
    return mapper.readValue(inputStream, clazz);
  }
  
  private static class StringSerializer extends com.fasterxml.jackson.databind.JsonSerializer<String>  {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
      if (CredentialsDefinitionAdaptor.P_CRED_PASSWORD.equals(gen.getOutputContext().getCurrentName())) {
        value = TextScrambler.encode(value);
      }
      gen.writeString(value);
    }
    
  }
  
  private static class StringDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String value = p.getValueAsString();
      if (CredentialsDefinitionAdaptor.P_CRED_PASSWORD.equals(p.getCurrentName())) {
        value = TextScrambler.decode(value);
      }
      return value;
    }
    
  }
}
