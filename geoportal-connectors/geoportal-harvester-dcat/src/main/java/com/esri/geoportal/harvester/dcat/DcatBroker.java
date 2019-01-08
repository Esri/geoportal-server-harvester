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
package com.esri.geoportal.harvester.dcat;

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.dcat.client.DcatParser;
import com.esri.geoportal.commons.dcat.client.DcatParserAdaptor;
import com.esri.geoportal.commons.dcat.client.dcat.DcatDistribution;
import com.esri.geoportal.commons.dcat.client.dcat.DcatRecord;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.StringAttribute;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.commons.utils.XmlUtils;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.ex.DataInputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esri.geoportal.harvester.api.DataContent;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import javax.xml.transform.TransformerException;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;

/**
 * CKAN broker.
 */
/*package*/ class DcatBroker implements InputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(DcatBroker.class);

  private final DcatConnector connector;
  private final DcatBrokerDefinitionAdaptor definition;
  private final MetaBuilder metaBuilder;

  protected CloseableHttpClient httpClient;
  protected TaskDefinition td;

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition definition
   * @param metaBuilder meta builder
   */
  public DcatBroker(DcatConnector connector, DcatBrokerDefinitionAdaptor definition, MetaBuilder metaBuilder) {
    this.connector = connector;
    this.definition = definition;
    this.metaBuilder = metaBuilder;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    td = context.getTask().getTaskDefinition();
    CloseableHttpClient http = HttpClientBuilder.create().useSystemProperties().build();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      httpClient = http;
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), http, definition.getHostUrl());
      httpClient = new BotsHttpClient(http, bots);
    }
  }

  @Override
  public void terminate() {
    if (httpClient != null) {
      try {
        httpClient.close();
      } catch (IOException ex) {
        LOG.error(String.format("Error terminating broker."), ex);
      }
    }
  }

  @Override
  public URI getBrokerUri() throws URISyntaxException {
    return new URI("DCAT", definition.getHostUrl().toExternalForm(), null);
  }

  @Override
  public Iterator iterator(IteratorContext iteratorContext) throws DataInputException {
    // TODO: provide DCAT iterator implementation
    File tempFile = null;
    try {
      tempFile = File.createTempFile("dcat-", "json");
      copyContentToFile(tempFile);

      InputStream input = new FileInputStream(tempFile);

      return new DcatIter(tempFile, input);
    } catch (IOException ex) {
      if (tempFile != null) {
        tempFile.delete();
      }
      throw new DataInputException(this, String.format("Error reading content of %s", definition.getHostUrl().toExternalForm()), ex);
    }
  }

  private void copyContentToFile(File outputFile) throws IOException {
    OutputStream outputStream = new FileOutputStream(outputFile);
    HttpGet request = new HttpGet(definition.getHostUrl().toExternalForm());
    try (CloseableHttpResponse response = httpClient.execute(request);
            InputStream inputStream = response.getEntity().getContent();) {
      IOUtils.copy(inputStream, outputStream);
    }
  }

  private class DcatIter implements InputBroker.Iterator {

    private final File tempFile;
    private final InputStream input;

    private DcatParser parser;
    private DcatParserAdaptor adaptor;
    private java.util.Iterator<DcatRecord> iterator;

    public DcatIter(File tempFile, InputStream input) {
      this.tempFile = tempFile;
      this.input = input;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      if (parser == null) {
        try {
          parser = new DcatParser(input);
        } catch (IOException ex) {
          close();
          throw new DataInputException(DcatBroker.this, String.format("Error parsing DCAT file %s", tempFile.toString()), ex);
        }
      }

      if (adaptor == null) {
        adaptor = new DcatParserAdaptor(parser);
      }

      if (iterator == null) {
        iterator = adaptor.iterator();
      }

      boolean more = iterator.hasNext();

      if (!more) {
        close();
      }

      return more;
    }

    @Override
    public DataReference next() throws DataInputException {
      DcatRecord r = iterator.next();
      try {
        SimpleDataReference ref = new SimpleDataReference(
                DcatBroker.this.getBrokerUri(),
                definition.getEntityDefinition().getLabel(),
                r.getIdentifier(),
                null,
                URI.create(r.getIdentifier()),
                td.getSource().getRef(), 
                td.getRef());
        
        if (definition.getEmitJson()) {
          try {
            String json = mapper.writeValueAsString(r);
            byte[] bytes = json.getBytes("UTF-8");
            ref.addContext(MimeType.APPLICATION_JSON, bytes);
          } catch (JsonProcessingException | UnsupportedEncodingException ex) {
            throw new DataInputException(DcatBroker.this, String.format("Error generating JSON"), ex);
          }
        }
        
        if (definition.getEmitXml()) {
          try {
            HashMap<String, Attribute> attributes = new HashMap<>();
            attributes.put(WKAConstants.WKA_IDENTIFIER, new StringAttribute(r.getIdentifier()));
            attributes.put(WKAConstants.WKA_TITLE, new StringAttribute(r.getTitle()));
            attributes.put(WKAConstants.WKA_DESCRIPTION, new StringAttribute(r.getDescription()));
            attributes.put(WKAConstants.WKA_MODIFIED, new StringAttribute(r.getModified()));
            
            for (DcatDistribution dist: r.getDistribution()) {
              MimeType mimeType = MimeType.parse(dist.getFormat());
              if (mimeType!=null) {
                attributes.put(WKAConstants.WKA_RESOURCE_URL, new StringAttribute(StringUtils.defaultIfEmpty(dist.getAccessURL(), dist.getDownloadURL())));
                attributes.put(WKAConstants.WKA_RESOURCE_URL_SCHEME, new StringAttribute(mimeType.getName()));
                break;
              }
            }
            
            MapAttribute attrs = new MapAttribute(attributes);
            Document document = metaBuilder.create(attrs);
            byte[] bytes = XmlUtils.toString(document).getBytes("UTF-8");
            ref.addContext(MimeType.APPLICATION_XML, bytes);
          } catch (MetaException | TransformerException | UnsupportedEncodingException ex) {
            throw new DataInputException(DcatBroker.this, String.format("Error generating XML"), ex);
          }
        }
        
        return ref;
      } catch (URISyntaxException ex) {
        throw new DataInputException(DcatBroker.this, String.format("Error creating data for %s", r.getIdentifier()), ex);
      }
    }

    private void close() {
      adaptor = null;
      parser = null;
      if (input != null) {
        try {
          input.close();
        } catch (IOException ex) {
          // ignore
        }
      }
      if (tempFile != null) {
        try {
          tempFile.delete();
        } catch (Exception ex) {
          // ignore
        }
      }
    }
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return true;
  }

  @Override
  public String toString() {
    return String.format("CKAN [%s]", definition.getHostUrl());
  }

  @Override
  public InputConnector getConnector() {
    return connector;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  @Override
  public DataContent readContent(String id) throws DataInputException {
    // TODO: provide DCAT iterator implementation
    return null;
  }
}
