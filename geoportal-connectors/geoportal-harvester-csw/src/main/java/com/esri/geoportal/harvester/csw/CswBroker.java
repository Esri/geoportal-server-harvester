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
package com.esri.geoportal.harvester.csw;

import com.esri.geoportal.commons.csw.client.IClient;
import com.esri.geoportal.commons.csw.client.IRecord;
import com.esri.geoportal.commons.csw.client.IRecords;
import com.esri.geoportal.commons.csw.client.ObjectFactory;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.harvester.api.DataInputException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.InputBroker;
import com.esri.geoportal.harvester.api.support.SimpleDataReference;
import java.io.IOException;
import java.util.Iterator;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * CSW broker.
 */
public class CswBroker implements InputBroker<String> {
  private static final int PAGE_SIZE = 10;

  private final CswDefinition attributes;
  private CloseableHttpClient httpclient;
  private IClient client;
  private Iterator<IRecord> recs;
  private int start = 1;
  private boolean noMore;

  public CswBroker(CswDefinition attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean hasNext() throws DataInputException {
    try {
      assertClient();
      
      if (noMore) {
        return false;
      }
      
      if (recs==null) {
        IRecords r = client.findRecords(start, PAGE_SIZE);
        if (r.isEmpty()) {
          noMore = true;
        } else {
          recs = r.iterator();
        }
        return hasNext();
      }
      
      if (!recs.hasNext()) {
        recs = null;
        start += PAGE_SIZE;
        return hasNext();
      }
      
      return true;
    } catch (Exception ex) {
      throw new DataInputException(this, "Error reading data.", ex);
    }
  }

  @Override
  public DataReference<String> next() throws DataInputException {
    try {
      IRecord rec = recs.next();
      String metadata = client.readMetadata(rec.getId());
      return new SimpleDataReference<>(rec.getId(), rec.getLastModifiedDate(), metadata);
    } catch (Exception ex) {
      throw new DataInputException(this, "Error reading data.", ex);
    }
  }

  /**
   * Asserts executor.
   * @throws IOException if creating executor fails
   */
  private void assertClient() throws IOException {
    if (client==null) {
      CloseableHttpClient httpclient = HttpClients.createDefault();
      Bots bots = BotsUtils.readBots(attributes.getBotsConfig(), httpclient, attributes.getBotsMode(), attributes.getHostUrl());
      ObjectFactory cf = new ObjectFactory();
      client = cf.newClient(attributes.getHostUrl().toExternalForm(), attributes.getProfile(), bots, attributes.getBotsMode());
    }
  }

  @Override
  public void close() throws IOException {
    if (httpclient!=null) {
      httpclient.close();
    }
  }

  @Override
  public String toString() {
    return String.format("CSW [%s, %s]", attributes.getHostUrl(), attributes.getProfile());
  }
  
}
