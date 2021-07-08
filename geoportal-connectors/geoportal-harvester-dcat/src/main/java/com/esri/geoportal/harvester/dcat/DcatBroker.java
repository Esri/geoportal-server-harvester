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

import com.esri.geoportal.commons.constants.ItemType;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.constants.MimeTypeUtils;
import com.esri.geoportal.commons.dcat.client.DcatParser;
import com.esri.geoportal.commons.dcat.client.DcatParserAdaptor;
import com.esri.geoportal.commons.dcat.client.dcat.DcatDistribution;
import com.esri.geoportal.commons.dcat.client.dcat.DcatRecord;
import com.esri.geoportal.commons.http.BotsHttpClient;
import com.esri.geoportal.commons.meta.ArrayAttribute;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.StringAttribute;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_REFERENCES;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_RESOURCE_URL;
import static com.esri.geoportal.commons.meta.util.WKAConstants.WKA_RESOURCE_URL_SCHEME;
import com.esri.geoportal.commons.robots.Bots;
import com.esri.geoportal.commons.robots.BotsUtils;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.commons.utils.UriUtils;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.TransformerException;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.w3c.dom.Document;

/**
 * CKAN broker.
 */
/*package*/ class DcatBroker implements InputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(DcatBroker.class);

  private final DcatConnector connector;
  private final DcatBrokerDefinitionAdaptor definition;
  private final MetaBuilder metaBuilder;
  private final ArrayList<DcatIter> iterators = new ArrayList<>();

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
    CloseableHttpClient http = HttpClientBuilder.create().useSystemProperties().setRedirectStrategy(LaxRedirectStrategy.INSTANCE).build();
    if (context.getTask().getTaskDefinition().isIgnoreRobotsTxt()) {
      httpClient = http;
    } else {
      Bots bots = BotsUtils.readBots(definition.getBotsConfig(), http, definition.getHostUrl());
      httpClient = new BotsHttpClient(http, bots);
    }
  }

  @Override
  public void terminate() {
    new ArrayList<>(iterators).forEach(DcatIter::close);

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
    final File file = downloadFile(definition.getHostUrl());
    try {

      DcatIter iter = new DcatIter(new FileInputStream(file)) {
        @Override
        protected void onClose() {
          safeDeleteFile(file);
          iterators.remove(this);
        }
      };

      iterators.add(iter);
      return iter;
    } catch (IOException ex) {
      safeDeleteFile(file);
      throw new DataInputException(this, String.format("Error reading content of %s", definition.getHostUrl().toExternalForm()), ex);
    }
  }

  private File downloadFile(URL fileToDownload) throws DataInputException {
    try {
      File tempFile = File.createTempFile("dcat-", "json");
      HttpGet request = new HttpGet(fileToDownload.toExternalForm());
      try (
              OutputStream outputStream = new FileOutputStream(tempFile);
              CloseableHttpResponse response = httpClient.execute(request);
              InputStream inputStream = response.getEntity().getContent();) {
        IOUtils.copy(inputStream, outputStream);
      }
      return tempFile;
    } catch (IOException ex) {
      throw new DataInputException(this, String.format("Error downloading DCAT file: %s", definition.getHostUrl().toExternalForm()), ex);
    }
  }

  private void safeDeleteFile(File file) {
    if (file != null) {
      try {
        file.delete();
      } catch (Exception ex) {
        LOG.debug("Error deleting dcat temporary file", ex);
      }
    }
  }

  private class DcatIter implements InputBroker.Iterator {

    private final InputStream input;

    private DcatParser parser;
    private DcatParserAdaptor adaptor;
    private java.util.Iterator<DcatRecord> iterator;

    public DcatIter(InputStream input) {
      this.input = input;
    }

    @Override
    public boolean hasNext() throws DataInputException {
      if (parser == null) {
        try {
          parser = new DcatParser(input);
        } catch (IOException ex) {
          close();
          throw new DataInputException(DcatBroker.this, String.format("Error parsing DCAT file"), ex);
        }
      }

      if (adaptor == null) {
        adaptor = new DcatParserAdaptor(parser);
      }

      if (iterator == null) {
        iterator = adaptor.iterator();
      }

      boolean hasMore = iterator.hasNext();
      if (!hasMore) {
        close();
      }

      return hasMore;
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
                URI.create(UriUtils.escapeUri(r.getIdentifier())),
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

            // collect URL's and types from all available distributions
            final List<Attribute> references = new ArrayList<>();
            for (DcatDistribution dist : r.getDistribution()) {
              String url = StringUtils.trimToNull(StringUtils.defaultIfEmpty(dist.getAccessURL(), dist.getDownloadURL()));
              if (url != null) {
                HashMap<String, Attribute> reference = new HashMap<>();
                MimeType mimeType = MimeType.parse(dist.getFormat());
                if (mimeType != null) {
                  reference.put(WKAConstants.WKA_RESOURCE_URL, new StringAttribute(url));
                  reference.put(WKAConstants.WKA_RESOURCE_URL_SCHEME, new StringAttribute(generateSchemeName(mimeType)));
                } else {
                  String schemeName = generateSchemeName(url);
                  if (schemeName != null) {
                    reference.put(WKAConstants.WKA_RESOURCE_URL, new StringAttribute(url));
                    reference.put(WKAConstants.WKA_RESOURCE_URL_SCHEME, new StringAttribute(schemeName));
                  }
                }
              }
            }

            // produce attribute(s) depending if there is one or more than one references
            if (references.size() == 1) {
              Map<String, Attribute> namedAttributes = references.get(0).getNamedAttributes();
              attributes.put(WKA_RESOURCE_URL, namedAttributes.get(WKA_RESOURCE_URL));
              if (namedAttributes.containsKey(WKA_RESOURCE_URL_SCHEME)) {
                attributes.put(WKA_RESOURCE_URL_SCHEME, namedAttributes.get(WKA_RESOURCE_URL_SCHEME));
              }
            } else if (!references.isEmpty()) {
              attributes.put(WKA_REFERENCES, new ArrayAttribute(references));
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

    protected void onClose() {
      // called upon closing iterator
    }

    private void close() {
      adaptor = null;
      parser = null;
      if (input != null) {
        try {
          input.close();
        } catch (IOException ex) {
          LOG.debug("Error closing dcat iterator.", ex);
        }
      }
      onClose();
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

  private String generateSchemeName(String url) {
    String serviceType = url != null ? ItemType.matchPattern(url).stream()
            .filter(it -> it.getServiceType() != null)
            .map(ItemType::getServiceType)
            .findFirst().orElse(null) : null;
    if (serviceType != null) {
      return "urn:x-esri:specification:ServiceType:ArcGIS:" + serviceType;
    }
    if (url != null) {
      int idx = url.lastIndexOf(".");
      if (idx >= 0) {
        String ext = url.substring(idx + 1);
        MimeType mimeType = MimeTypeUtils.mapExtension(ext);
        return generateSchemeName(mimeType);
      }
    }
    return null;
  }

  private String generateSchemeName(MimeType mimeType) {
    return mimeType != null ? "urn:" + mimeType.getName() : null;
  }
}
