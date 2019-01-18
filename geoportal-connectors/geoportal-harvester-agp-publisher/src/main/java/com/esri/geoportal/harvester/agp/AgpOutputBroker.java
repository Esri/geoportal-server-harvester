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
package com.esri.geoportal.harvester.agp;

import com.esri.geoportal.commons.agp.client.AgpClient;
import com.esri.geoportal.commons.constants.DataType;
import com.esri.geoportal.commons.agp.client.DeleteResponse;
import com.esri.geoportal.commons.agp.client.FolderEntry;
import com.esri.geoportal.commons.agp.client.ItemEntry;
import com.esri.geoportal.commons.agp.client.ItemResponse;
import com.esri.geoportal.commons.constants.ItemType;
import com.esri.geoportal.commons.agp.client.QueryResponse;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaAnalyzer;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.BaseProcessInstanceListener;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.PublishingStatus;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.ex.DataOutputException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import com.esri.geoportal.harvester.api.specs.OutputConnector;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * ArcGIS Portal output broker.
 */
/*package*/ class AgpOutputBroker implements OutputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(AgpOutputBroker.class);
  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  private final AgpOutputConnector connector;
  private final AgpOutputBrokerDefinitionAdaptor definition;
  private final MetaAnalyzer metaAnalyzer;
  private CloseableHttpClient httpClient;
  private AgpClient client;
  private String token;
  private final Set<String> existing = new HashSet<>();
  private volatile boolean preventCleanup;

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition
   * @param metaAnalyzer
   */
  public AgpOutputBroker(AgpOutputConnector connector, AgpOutputBrokerDefinitionAdaptor definition, MetaAnalyzer metaAnalyzer) {
    this.connector = connector;
    this.definition = definition;
    this.metaAnalyzer = metaAnalyzer;
  }

  @Override
  public PublishingStatus publish(DataReference ref) throws DataOutputException {
    File fileToUpload = null;
    
    try {
      // extract map of attributes (normalized names)
      MapAttribute attributes = extractMapAttributes(ref);

      if (attributes == null) {
        throw new DataOutputException(this, ref.getId(), String.format("Error extracting attributes from data."));
      }

      // build typeKeywords array
      String src_source_type_s = URLEncoder.encode(ref.getBrokerUri().getScheme(), "UTF-8");
      String src_source_uri_s = URLEncoder.encode(ref.getBrokerUri().toASCIIString(), "UTF-8");
      String src_source_name_s = URLEncoder.encode(ref.getBrokerName(), "UTF-8");
      String src_uri_s = URLEncoder.encode(ref.getSourceUri().toASCIIString(), "UTF-8");
      String src_lastupdate_dt = ref.getLastModifiedDate() != null ? URLEncoder.encode(fromatDate(ref.getLastModifiedDate()), "UTF-8") : null;

      String[] typeKeywords = {
        String.format("src_source_type_s=%s", src_source_type_s),
        String.format("src_source_uri_s=%s", src_source_uri_s),
        String.format("src_source_name_s=%s", src_source_name_s),
        String.format("src_uri_s=%s", src_uri_s),
        String.format("src_lastupdate_dt=%s", src_lastupdate_dt)
      };

      String title = getAttributeValue(attributes, WKAConstants.WKA_TITLE, null);
      String description = getAttributeValue(attributes, WKAConstants.WKA_DESCRIPTION, null);
      String sThumbnailUrl = StringUtils.trimToNull(getAttributeValue(attributes, WKAConstants.WKA_THUMBNAIL_URL, null));
      String resourceUrl = getAttributeValue(attributes, WKAConstants.WKA_RESOURCE_URL, null);
      String bbox = getAttributeValue(attributes, WKAConstants.WKA_BBOX, null);

      // If the WKA_RESOURCE_URL is empty after parsing the XML file, see if it was set on the 
      // DataReference directly.
      if (resourceUrl == null || resourceUrl.isEmpty()) {
        resourceUrl = ((URI) ref.getAttributesMap().get(WKAConstants.WKA_RESOURCE_URL)).toString();
      }

      // check if the item is eligible for publishing
      ItemType itemType = createItemType(resourceUrl);
      // skip if no item type
      if (itemType == null) {
        return PublishingStatus.SKIPPED;
      }
      
      // download file
      String fileName = null;
      if (itemType.getDataType() == DataType.File && definition.isUploadFiles()) {
        FileName fn = getFileNameFromUrl(resourceUrl);
        fileName = fn.getFullName();
        fileToUpload = downloadFile(new URL(resourceUrl), fn);
      }

      try {

        // generate token
        if (token == null) {
          token = generateToken();
        }

        // check if item exists
        ItemEntry itemEntry = searchForItem(src_uri_s);

        if (itemEntry == null) {
          // add item if doesn't exist
          ItemResponse response = addItem(
                  title,
                  description,
                  new URL(resourceUrl),
                  sThumbnailUrl != null ? new URL(sThumbnailUrl) : null,
                  itemType,
                  extractEnvelope(bbox),
                  typeKeywords
          );

          if (response == null || !response.success) {
            String error = response != null && response.error != null && response.error.message != null ? response.error.message : null;
            throw new DataOutputException(this, ref.getId(), String.format("Error adding item: %s%s", ref, error != null ? "; " + error : ""));
          }

          client.share(definition.getCredentials().getUserName(), definition.getFolderId(), response.id, true, true, null, token);

          return PublishingStatus.CREATED;

        } else if (itemEntry.owner.equals(definition.getCredentials().getUserName())) {

          itemEntry = client.readItem(itemEntry.id, token);
          if (itemEntry == null) {
            throw new DataOutputException(this, ref.getId(), String.format("Unable to read item entry."));
          }

          // update item if does exist
          ItemResponse response = updateItem(
                  itemEntry.id,
                  itemEntry.owner,
                  itemEntry.ownerFolder,
                  title,
                  description,
                  new URL(resourceUrl),
                  sThumbnailUrl != null ? new URL(sThumbnailUrl) : null,
                  itemType,
                  extractEnvelope(bbox),
                  typeKeywords
          );

          if (response == null || !response.success) {
            String error = response != null && response.error != null && response.error.message != null ? response.error.message : null;
            throw new DataOutputException(this, ref.getId(), String.format("Error adding item: %s%s", ref, error != null ? "; " + error : ""));
          }

          existing.remove(itemEntry.id);

          return PublishingStatus.UPDATED;
        } else {
          return PublishingStatus.SKIPPED;
        }
      } catch (MalformedURLException ex) {
        return PublishingStatus.SKIPPED;
      }

    } catch (MetaException | IOException | ParserConfigurationException | SAXException | URISyntaxException ex) {
      throw new DataOutputException(this, ref.getId(), String.format("Error publishing data: %s", ref), ex);
    } finally {
      if (fileToUpload!=null) {
        fileToUpload.delete();
      }
    }
  }
  private ItemType createItemType(String resourceUrl) {

    // check if the item is eligible for publishing
    ItemType itemType = Stream.concat(
            ItemType.matchExt(resourceUrl.substring(resourceUrl.lastIndexOf(".") + 1)).stream(),
            ItemType.matchPattern(resourceUrl).stream()
    ).findFirst().orElse(null);

    if (itemType == null) {
      return null;
    }

    switch (itemType.getDataType()) {
      case Text:
        return null;
      case File:
        if (!itemType.hasUniqueMimeType()) {
          return null;
        }
        break;
    }

    return itemType;
  }

  private ItemEntry searchForItem(String src_uri_s) throws URISyntaxException, IOException {

    QueryResponse search = client.search(String.format("typekeywords:%s", String.format("src_uri_s=%s", src_uri_s)), 0, 0, token);
    ItemEntry itemEntry = search != null && search.results != null && search.results.length > 0 ? search.results[0] : null;
    
    return itemEntry;
  }

  private Double[] extractEnvelope(String sBbox) {
    Double[] envelope = null;
    if (sBbox != null) {
      String[] corners = sBbox.split(",");
      if (corners != null && corners.length == 2) {
        String[] minXminY = corners[0].split(" ");
        String[] maxXmaxY = corners[1].split(" ");
        if (minXminY != null && minXminY.length == 2 && maxXmaxY != null && maxXmaxY.length == 2) {
          minXminY[0] = StringUtils.trimToEmpty(minXminY[0]);
          minXminY[1] = StringUtils.trimToEmpty(minXminY[1]);
          maxXmaxY[0] = StringUtils.trimToEmpty(maxXmaxY[0]);
          maxXmaxY[1] = StringUtils.trimToEmpty(maxXmaxY[1]);

          Double minX = NumberUtils.isNumber(minXminY[0]) ? NumberUtils.createDouble(minXminY[0]) : null;
          Double minY = NumberUtils.isNumber(minXminY[1]) ? NumberUtils.createDouble(minXminY[1]) : null;
          Double maxX = NumberUtils.isNumber(maxXmaxY[0]) ? NumberUtils.createDouble(maxXmaxY[0]) : null;
          Double maxY = NumberUtils.isNumber(maxXmaxY[1]) ? NumberUtils.createDouble(maxXmaxY[1]) : null;

          if (minX != null && minY != null && maxX != null && maxY != null) {
            envelope = new Double[]{minX, minY, maxX, maxY};
          }
        }
      }
    }
    return envelope;
  }

  private String getAttributeValue(MapAttribute attributes, String attributeName, String defaultValue) {
    Attribute attr = attributes.getNamedAttributes().get(attributeName);
    return attr != null ? attr.getValue() : defaultValue;
  }

  private MapAttribute extractMapAttributes(DataReference ref) throws MetaException, IOException, ParserConfigurationException, SAXException {
    MapAttribute attributes = ref.getAttributesMap().values().stream()
            .filter(o -> o instanceof MapAttribute)
            .map(o -> (MapAttribute) o)
            .findFirst()
            .orElse(null);
    if (attributes == null) {
      Document doc = ref.getAttributesMap().values().stream()
              .filter(o -> o instanceof Document)
              .map(o -> (Document) o)
              .findFirst()
              .orElse(null);
      if (doc != null) {
        attributes = metaAnalyzer.extract(doc);
      } else {
        if (ref.getContentType().contains(MimeType.APPLICATION_XML) || ref.getContentType().contains(MimeType.TEXT_XML)) {
          String sXml = new String(ref.getContent(MimeType.APPLICATION_XML, MimeType.TEXT_XML), "UTF-8");
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
          factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
          factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
          factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
          factory.setXIncludeAware(false);
          factory.setExpandEntityReferences(false);
          factory.setNamespaceAware(true);
          DocumentBuilder builder = factory.newDocumentBuilder();
          doc = builder.parse(new InputSource(new StringReader(sXml)));
          attributes = metaAnalyzer.extract(doc);
        }
      }
    }
    return attributes;
  }

  private ItemResponse addItem(String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords) throws IOException, URISyntaxException {
    if (token == null) {
      token = generateToken();
    }
    ItemResponse response = addItem(
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, token);
    if (response.error != null && response.error.code == 498) {
      token = generateToken();
      response = addItem(
              title,
              description,
              url, thumbnailUrl,
              itemType, envelope, typeKeywords, token);
    }
    return response;
  }

  private ItemResponse addItem(String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords, String token) throws IOException, URISyntaxException {
    return client.addItem(
            definition.getCredentials().getUserName(),
            definition.getFolderId(),
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, null, token);
  }

  private ItemResponse updateItem(String id, String owner, String folderId, String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords) throws IOException, URISyntaxException {
    if (token == null) {
      token = generateToken();
    }
    ItemResponse response = updateItem(
            id,
            owner,
            folderId,
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, token);
    if (response.error != null && response.error.code == 498) {
      token = generateToken();
      response = updateItem(
              id,
              owner,
              folderId,
              title,
              description,
              url, thumbnailUrl,
              itemType, envelope, typeKeywords, token);
    }
    return response;
  }

  private ItemResponse updateItem(String id, String owner, String folderId, String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords, String token) throws IOException, URISyntaxException {
    return client.updateItem(
            owner,
            folderId,
            id,
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, null, token);
  }

  private DeleteResponse deleteItem(String id, String owner, String folderId) throws URISyntaxException, IOException {
    if (token == null) {
      token = generateToken();
    }
    DeleteResponse response = deleteItem(id, owner, folderId, token);
    if (response.error != null && response.error.code == 498) {
      token = generateToken();
      response = deleteItem(id, owner, folderId, token);
    }
    return response;
  }

  private DeleteResponse deleteItem(String id, String owner, String folderId, String token) throws URISyntaxException, IOException {
    return client.delete(id, owner, folderId, token);
  }

  private String generateToken(int minutes) throws URISyntaxException, IOException {
    return client.generateToken(minutes).token;
  }

  private String generateToken() throws URISyntaxException, IOException {
    return client.generateToken(60).token;
  }

  @Override
  public EntityDefinition getEntityDefinition() {
    return definition.getEntityDefinition();
  }

  @Override
  public OutputConnector getConnector() {
    return connector;
  }

  @Override
  public void initialize(InitContext context) throws DataProcessorException {
    definition.override(context.getParams());
    this.httpClient = HttpClientBuilder.create().useSystemProperties().build();
    this.client = new AgpClient(httpClient, definition.getHostUrl(), definition.getCredentials(), definition.getMaxRedirects());

    if (!context.canCleanup()) {
      preventCleanup = true;
    }
    if (definition.getCleanup() && !preventCleanup) {
      context.addListener(new BaseProcessInstanceListener() {
        @Override
        public void onError(DataException ex) {
          preventCleanup = true;
        }
      });
      try {
        String src_source_uri_s = URLEncoder.encode(context.getTask().getDataSource().getBrokerUri().toASCIIString(), "UTF-8");
        QueryResponse search = client.search(String.format("typekeywords:%s", String.format("src_source_uri_s=%s", src_source_uri_s)), 0, 0, generateToken(1));
        while (search != null && search.results != null && search.results.length > 0) {
          existing.addAll(Arrays.asList(search.results).stream().map(i -> i.id).collect(Collectors.toList()));
          if (search.nextStart > 0) {
            search = client.search(String.format("typekeywords:%s", String.format("src_source_uri_s=%s", src_source_uri_s)), 0, search.nextStart, generateToken(1));
          } else {
            break;
          }
        }
      } catch (URISyntaxException | IOException ex) {
        throw new DataProcessorException(String.format("Error collecting ids of existing items."), ex);
      }
    }

    try {
      String folderId = StringUtils.trimToNull(definition.getFolderId());
      if (folderId != null) {
        FolderEntry[] folders = this.client.listFolders(definition.getCredentials().getUserName(), generateToken(1));
        FolderEntry selectedFodler = folders != null
                ? Arrays.stream(folders).filter(folder -> folder.id != null && folder.id.equals(folderId)).findFirst().orElse(
                        Arrays.stream(folders).filter(folder -> folder.title != null && folder.title.equals(folderId)).findFirst().orElse(null)
                )
                : null;
        if (selectedFodler != null) {
          definition.setFolderId(selectedFodler.id);
        } else {
          definition.setFolderId(null);
        }
      } else {
        definition.setFolderId(null);
      }
    } catch (IOException | URISyntaxException ex) {
      throw new DataProcessorException(String.format("Error listing folders for user: %s", definition.getCredentials().getUserName()), ex);
    }
  }

  @Override
  public void terminate() {
    try {
      if (definition.getCleanup() && !preventCleanup) {
        token = generateToken();
        for (String id : existing) {
          ItemEntry item = client.readItem(id, token);
          deleteItem(item.id, item.owner, item.ownerFolder);
        }
      }
      client.close();
    } catch (IOException | URISyntaxException ex) {
      LOG.error(String.format("Error terminating broker."), ex);
    }
  }

  @Override
  public boolean hasAccess(SimpleCredentials creds) {
    return definition.getCredentials() == null || definition.getCredentials().isEmpty() ? true : definition.getCredentials().equals(creds);
  }

  private String fromatDate(Date date) {
    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    return FORMATTER.format(zonedDateTime);
  }

  private File downloadFile(URL fileToDownload, FileName fileName) throws IOException {
    
    File tempFile = File.createTempFile(fileName.name + "-", "." + fileName.ext);
    
    HttpGet request = new HttpGet(fileToDownload.toExternalForm());
    try (
            OutputStream outputStream = new FileOutputStream(tempFile);
            CloseableHttpResponse response = httpClient.execute(request);
            InputStream inputStream = response.getEntity().getContent();) {
      IOUtils.copy(inputStream, outputStream);
    }
    return tempFile;
  }

  private FileName getFileNameFromUrl(String resourceUrl) {
    String path = resourceUrl;
    String fullName = path.substring(path.lastIndexOf("/")+1);
    String ext = fullName.substring(fullName.lastIndexOf(".")+1);
    String name = ext.length() < fullName.length()? fullName.substring(0, fullName.lastIndexOf(".")): fullName;
    if (ext.equals(name))
      ext = null;
    
    return new FileName(name, ext);
  }
  
  private static class FileName {
    public final String name;
    public final String ext;

    public FileName(String name, String ext) {
      this.name = name;
      this.ext = ext;
    }

    public String getName() {
      return name;
    }

    public String getExt() {
      return ext;
    }
    
    public String getFullName() {
      return ext!=null? name + "." + ext: name;
    }
    
    public String toString() {
      return getFullName();
    }
  }
}
