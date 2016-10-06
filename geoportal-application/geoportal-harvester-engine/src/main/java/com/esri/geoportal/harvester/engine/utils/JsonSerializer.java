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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * JSON serializer/deserializer utility class.
 */
public class JsonSerializer {

  /**
   * Serialize object into JSON.
   * @param def definition to serialize
   * @return serialized definition
   * @throws IOException if serializing fails
   */
  public static String serialize(Object def) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper.readValue(inputStream, clazz);
  }
}
