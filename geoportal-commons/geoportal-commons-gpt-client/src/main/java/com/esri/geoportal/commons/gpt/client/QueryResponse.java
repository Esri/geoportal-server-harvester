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
package com.esri.geoportal.commons.gpt.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.List;

/**
 * Query response.
 */
/*package*/ final class QueryResponse {
  private static final ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }
    
  public String _scroll_id;
  public Hits hits;
 
  @JsonDeserialize(using = QueryResponse.HitsDeserializer.class)
  public static final class Hits {
    public long total;
    public List<Hit> hits;
  }
  
  public static final class Hit {
    public String _id;
    public Source _source;
  }
  
  public static final class Source {
    public String src_uri_s;
    public String src_lastupdate_dt;
  }
  
  public static final class HitsDeserializer extends JsonDeserializer<Hits> {

    @Override
    public Hits deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonNode node = jp.getCodec().readTree(jp);
      Hits hits = new Hits();
      
      if (node.has("total")) {
        if (node.get("total").isNumber()) {
          hits.total = node.get("total").asInt();
        } else if (node.get("total").isObject() && node.get("total").has("value") && node.get("total").get("value").isNumber()) {
          hits.total = node.get("total").get("value").asInt();
        }
      }
      
      if (node.has("hits")) {
        JsonNode hitsNode = node.get("hits");
        hits.hits = mapper.treeToValue(hitsNode, List.class);
      }
      return hits;
    }
    
  }
}
