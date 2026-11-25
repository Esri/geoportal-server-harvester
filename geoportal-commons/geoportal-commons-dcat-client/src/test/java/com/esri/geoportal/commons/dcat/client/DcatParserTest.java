/*
 * Copyright 2019 Esri.
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
package com.esri.geoportal.commons.dcat.client;

import com.esri.geoportal.commons.dcat.client.dcat.DcatRecord;
import java.io.InputStream;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 * Parser test.
 */
public class DcatParserTest {
  @Test
  public void preservesDistributionsInEmittedJson() throws Exception {
    String input = "{\"dataset\":[{\"identifier\":\"dataset-1\",\"keyword\":[\"water\"],"
        + "\"distribution\":[{\"@type\":\"dcat:Distribution\",\"title\":\"ArcGIS Hub Dataset\","
        + "\"format\":\"Web Page\",\"mediaType\":\"text/html\",\"accessURL\":\"https://example.org/dataset\"},"
        + "{\"title\":\"ArcGIS GeoService\",\"mediaType\":\"application/json\","
        + "\"accessURL\":\"https://example.org/FeatureServer\"}]}]}";
    DcatParser parser = new DcatParser(new java.io.StringReader(input));
    DcatParserAdaptor adaptor = new DcatParserAdaptor(parser);
    try {
      java.util.Iterator<DcatRecord> records = adaptor.iterator();
      assertTrue(records.hasNext());
      DcatRecord record = records.next();
      assertEquals(2, record.getDistribution().size());
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
      com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(mapper.writeValueAsString(record));
      assertTrue(json.has("distribution"));
      assertEquals(2, json.get("distribution").size());
      assertEquals("https://example.org/dataset", json.get("distribution").get(0).path("accessURL").asText());
      assertEquals("https://example.org/FeatureServer", json.get("distribution").get(1).path("accessURL").asText());
    } finally {
      adaptor.close();
    }
  }

  private static URL url;
  
  @BeforeClass
  public static void initClass() {
    // TODO: initialize url with sample data
  }
  
  @Test
  public void testSampleSite() throws Exception {
    if (url!=null) {
      try (InputStream is = url.openStream()) {
        DcatParser p = new DcatParser(is);
        DcatParserAdaptor a = new DcatParserAdaptor(p);
        for (DcatRecord r : a) {
          String title = r.getTitle();
          System.out.println(title);
        }
      }
    }
  }
}
