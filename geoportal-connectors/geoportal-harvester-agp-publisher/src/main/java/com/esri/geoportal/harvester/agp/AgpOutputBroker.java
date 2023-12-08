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
import com.esri.geoportal.commons.agp.client.QueryResponse;
import com.esri.geoportal.commons.constants.ItemType;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.doc.DocUtils;
import com.esri.geoportal.commons.meta.ArrayAttribute;
import com.esri.geoportal.commons.meta.Attribute;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaAnalyzer;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.BaseProcessInstanceListener;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.pdf.PdfUtils;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;


/**
 * ArcGIS Portal output broker.
 */
/*package*/ class AgpOutputBroker implements OutputBroker {

  private static final Logger LOG = LoggerFactory.getLogger(AgpOutputBroker.class);
  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  private final AgpOutputConnector connector;
  private final AgpOutputBrokerDefinitionAdaptor definition;
  private final MetaAnalyzer metaAnalyzer;
  private final String geometryServiceUrl;
  private CloseableHttpClient httpClient;
  private AgpClient client;
  private String token;
  private final Set<String> existing = new HashSet<>();
  private volatile boolean preventCleanup;
  private final Integer sizeLimit;

  /**
   * Creates instance of the broker.
   *
   * @param connector connector
   * @param definition
   * @param metaAnalyzer
   * @param sizeLimit TIKA size limit
   */
  public AgpOutputBroker(AgpOutputConnector connector, AgpOutputBrokerDefinitionAdaptor definition, MetaAnalyzer metaAnalyzer, String geometryServiceUrl, Integer sizeLimit) {
    this.connector = connector;
    this.definition = definition;
    this.metaAnalyzer = metaAnalyzer;
    this.geometryServiceUrl = geometryServiceUrl;
    this.sizeLimit = sizeLimit;
  }

  @Override
  public PublishingStatus publish(DataReference ref) throws DataOutputException {
    File fileToUpload = null;
    File metadataFile = null;
    boolean deleteTempFile = false;
    boolean deleteMetadataFile = false;
    Parser markdownParser = Parser.builder().build();
    HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    
    try {

      byte[] content    = null;

      if (ref.getContent(MimeType.APPLICATION_PDF) != null) {
        content = PdfUtils.generateMetadataXML(ref.getContent(MimeType.APPLICATION_PDF), ref.getSourceUri().getPath(), ref.getSourceUri().toASCIIString(), geometryServiceUrl); 
        
      } else if (ref.getContent(MimeType.APPLICATION_XML, MimeType.TEXT_XML) != null) {        	
        content = ref.getContent(MimeType.APPLICATION_XML, MimeType.TEXT_XML);

      } else {       
          final MimeType [] toBeSkipped = new MimeType[]{MimeType.APPLICATION_PDF, MimeType.APPLICATION_XML, MimeType.TEXT_XML, MimeType.APPLICATION_JSON};
          Set <MimeType> types      = ref.getContentType().stream()
                  .filter(t->!Arrays.stream(toBeSkipped).anyMatch(s->s==t))
                  .collect(Collectors.toSet());
          if (!types.isEmpty()) {
            byte[] rawContent = ref.getContent(types.toArray(new MimeType[types.size()]));
            content = rawContent!=null ? DocUtils.generateMetadataXML(rawContent, new File(ref.getId()).getName(), sizeLimit) : null;
          }
      }
        
      // extract map of attributes (normalized names)
      MapAttribute attributes = extractMapAttributes(ref, content);
      
      // transform metadata to ArcGIS XML
      String arcgisMetadata = transformMetadataToArcGISXML(content);
          

      if (attributes == null) {
        throw new DataOutputException(this, ref, String.format("Error extracting attributes from data."));
      }

      // build typeKeywords array
      String src_source_type_s = URLEncoder.encode(ref.getBrokerUri().getScheme(), "UTF-8");
      String src_source_uri_s = URLEncoder.encode(ref.getBrokerUri().toASCIIString(), "UTF-8");
      String src_source_name_s = URLEncoder.encode(ref.getBrokerName(), "UTF-8");
      String src_uri_s = URLEncoder.encode(ref.getSourceUri().toASCIIString(), "UTF-8");
      String src_lastupdate_dt = ref.getLastModifiedDate() != null ? URLEncoder.encode(fromatDate(ref.getLastModifiedDate()), "UTF-8") : null;

      String title = getAttributeValue(attributes, WKAConstants.WKA_TITLE, null);
      String description = getAttributeValue(attributes, WKAConstants.WKA_DESCRIPTION, null);
      // convert title and description from markdown to HTML
      if (definition.isMarkdown2HTML()) {
          //Node titleNode = markdownParser.parse(title);
          //title = htmlRenderer.render(titleNode);
          Node descriptionNode = markdownParser.parse(description);
          
          description = htmlRenderer.render(descriptionNode);
          description = description.replaceAll("[*] ","<li>");
          description = description.replaceAll("\\\\n","<br/>");
          description = replaceSpecialCharacters(description);
          //description = description.replaceAll("[^\\x00-\\x7F]", "");
          
          // also update arcgisMetadata with the new description as idAbs
          // only if there is exactly 1 idAbs element
          String[] metadataPieces = arcgisMetadata.split("idAbs>");
          if (metadataPieces.length == 3) {
            metadataPieces[1] = "idAbs><![CDATA[" + description + "]]></idAbs>";
            arcgisMetadata = metadataPieces[0] + metadataPieces[1] + metadataPieces[2]; 
          }
      }
      String sThumbnailUrl = StringUtils.trimToNull(getAttributeValue(attributes, WKAConstants.WKA_THUMBNAIL_URL, null));
      String resourceUrl = getAttributeValue(attributes, WKAConstants.WKA_RESOURCE_URL, null);
      
      //Hardcoded url just for dev testing
      //resourceUrl="https://services.arcgis.com/RhGiohBHzSBKt1MS/arcgis/rest/services/group_of_layers/FeatureServer/1";
      // clean up resource URL
      resourceUrl = resourceUrl.replace("http:", "https:")
                               .replace(":80/", "/");
      String bbox = getAttributeValue(attributes, WKAConstants.WKA_BBOX, null);

      String[] typeKeywords = {
        String.format("src_source_type_s=%s", src_source_type_s),
        String.format("src_source_uri_s=%s", src_source_uri_s),
        String.format("src_source_name_s=%s", src_source_name_s),
        String.format("src_uri_s=%s", src_uri_s),
        String.format("src_lastupdate_dt=%s", src_lastupdate_dt),
        String.format("resourceURL=%s", URLEncoder.encode(resourceUrl, "UTF-8"))
      };
      
      // check if the item is eligible for publishing
      // based on the main link in the item itself (not source metadata link)
      ItemType itemType = createItemType(resourceUrl);
     
      // If the WKA_RESOURCE_URL is empty after parsing the XML file, see if it was set on the 
      // DataReference directly.
      if (itemType==null) {
        if (ref.getAttributesMap().get(WKAConstants.WKA_REFERENCES) != null && ref.getAttributesMap().get(WKAConstants.WKA_REFERENCES) instanceof ArrayAttribute) {
          ArrayAttribute references = (ArrayAttribute)ref.getAttributesMap().get(WKAConstants.WKA_REFERENCES);
          for (Attribute reference: references.getAttributes()) {
            Attribute refUrlAttribute = reference.getNamedAttributes().get(WKAConstants.WKA_RESOURCE_URL);
            if (refUrlAttribute!=null) {
              String refUrl = refUrlAttribute.getValue();
              ItemType it = createItemType(refUrl);
              if (it!=null) {
                resourceUrl = refUrl;
                itemType = it;
                break;
              }
            }
          }
        }
      }

      // skip if no item type
      if (itemType == null) {
        LOG.debug(String.format("Resource '%s' with resource url '%s' skipped for unrecognized item type", title, resourceUrl));
        return PublishingStatus.SKIPPED;
      }
      
      if (itemType.equals(ItemType.TILED_IMAGERY)) {
        ArrayList<String> newTypeKeywords = new ArrayList<>();
        // import typeKeywords
        newTypeKeywords.addAll(Arrays.asList(typeKeywords));
        // add new type keyword
        newTypeKeywords.add("Tiled Imagery");

        typeKeywords = newTypeKeywords.toArray(new String[newTypeKeywords.size()]);

        // finally, set the item type to IMAGE_SERVICE so that ArcGIS Portal/Online understand it
        itemType = ItemType.IMAGE_SERVICE;
      }
      
      // download file
      if (itemType.getDataType() == DataType.File && definition.isUploadFiles()) {
        try {
          if (new File(resourceUrl.replaceAll("^file://", "")).exists()) {
            fileToUpload = new File(resourceUrl.replaceAll("^file://", ""));
          } else {
            FileName fn = getFileNameFromUrl(resourceUrl);
            fileToUpload = downloadFile(new URL(resourceUrl), fn);
            deleteTempFile = true;
          }
        } catch (IOException ex) {
          LOG.warn(String.format("Error downloading file '%s'. Registering URL only.", resourceUrl), ex);
          fileToUpload = null;
        }
      }

      // create temporary file for the metadata
      Path tmpFile = Files.createTempFile(null, null);
      Files.write(tmpFile, arcgisMetadata.getBytes(StandardCharsets.UTF_8));
      metadataFile = tmpFile.toFile();
      //deleteMetadataFile = true;

      try {

        // generate token       
        if ((definition.useOAuth()) || (token == null)) {
           token = definition.getOAuthToken();
           //oAuth token was not generated, hence generate token from user password. In this case subLayer metadata update will fail
           if(token == null || token.isBlank())
           {
               token = generateToken(60);
           }
        }
        // check if item exists
        ItemEntry itemEntry = searchForItem(resourceUrl);

        if (itemEntry == null) {
          
            // neither the potential sub layer, nor the parent layer exist. Add a new portal item
         
            ItemResponse response = addItem(
                    title,
                    description,
                    new URL(resourceUrl),
                    sThumbnailUrl != null ? new URL(sThumbnailUrl) : null,
                    itemType,
                    extractEnvelope(bbox),
                    typeKeywords,
                    null,
                    metadataFile,
                    token
            );

            if (response == null || !response.success) {
              String error = response != null && response.error != null && response.error.message != null ? response.error.message : null;
              throw new DataOutputException(this, ref, String.format("Error adding item: %s%s", ref, error != null ? "; " + error : ""));
            } else {
              System.out.print("addItem -> " + response.toString());

              // Now upload full metadata
              String metadataAdded = client.writeItemMetadata(response.id, arcgisMetadata, token);
              System.out.print("METADATA -> " + metadataAdded);
            }

            client.share(definition.getCredentials().getUserName(), definition.getFolderId(), response.id, true, true, null, token);

            return PublishingStatus.CREATED;

        } else {
          // there is an item registered for this resourceUrl, try to update
          // if the item is not owned by the registered account
          // assume the item is in a shared update group.
          // if this fails and the registered account cannot update the existing item
          // then this item will be 'skipped'

          itemEntry = client.readItem(itemEntry.id, token);
          if (itemEntry == null) {
            throw new DataOutputException(this, ref, String.format("Unable to read item entry."));
          }
          
          // Workaround for ArcGIS Portal/Online bug: look at typeKeywords in existing entry
          // if it has 'Tiled Imagery' then re-add it to the list because otherwise it gets dropped
          if (ArrayUtils.contains(itemEntry.typeKeywords, "Tiled Imagery")) {
            if (!ArrayUtils.contains(typeKeywords, "Tiled Imagery")) {
                ArrayList<String> newTypeKeywords = new ArrayList<>();
                // import typeKeywords
                newTypeKeywords.addAll(Arrays.asList(typeKeywords));
                // add new type keyword
                newTypeKeywords.add("Tiled Imagery");

                typeKeywords = newTypeKeywords.toArray(new String[newTypeKeywords.size()]);
            }
          }
     
          // update item if does exist
          
          // if the item url is the same as the resourceUrl, proceed.
          // otherwise the metadata is for a sublayer, but the item is the parent
          if (resourceUrl.equals(itemEntry.url)) {
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
                    typeKeywords,
                    fileToUpload,
                    metadataFile,
                    token
            );

            if (response == null || !response.success) {
              String error = response != null && response.error != null && response.error.message != null ? response.error.message : null;
              throw new DataOutputException(this, ref, String.format("Error adding item: %s%s", ref, error != null ? "; " + error : ""));
            } else {
              // String metadataAdded = client.writeItemMetadata(response.id, arcgisMetadata, token);
              System.out.print("updateItem -> " + response.toString());
            }

            existing.remove(itemEntry.id);

            return PublishingStatus.UPDATED;
          } else {
              // the metadata is apparently for a sublayer              
              return updateSubLayerMetadata(resourceUrl, arcgisMetadata);
          }
        }
      } catch (MalformedURLException ex) {
        return PublishingStatus.SKIPPED;
      }

    } catch (MetaException | IOException | ParserConfigurationException | SAXException | URISyntaxException ex) {
      throw new DataOutputException(this, ref, String.format("Error publishing data: %s Exception: "+ex, ref), ex);
    } finally {
      if (fileToUpload!=null && deleteTempFile) {
        fileToUpload.delete();
      }
      if (metadataFile!=null && deleteMetadataFile) {
        metadataFile.delete();
      } 
    }
  }
  
  
  private PublishingStatus updateSubLayerMetadata(String resourceUrl, String arcgisMetadata) throws URISyntaxException, IOException {
    String parentUrl = resourceUrl.substring(0,resourceUrl.lastIndexOf("/"));
    String featureServerToken = generateToken(60, parentUrl,token);
    String metadataUpdateURI = resourceUrl + "/metadata/update/";
    boolean wasUpdated = client.writeSubLayerMetadata(metadataUpdateURI, arcgisMetadata, featureServerToken);
    System.out.println("update metadata at " + metadataUpdateURI + " was a succes: " + wasUpdated);

    if (wasUpdated) {
        return PublishingStatus.UPDATED;
    } else {
        return PublishingStatus.SKIPPED;
    }
  }
  
  
  private ItemType createItemType(String resourceUrl) {
    
    resourceUrl = StringUtils.trimToEmpty(resourceUrl);

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

    // QueryResponse search = client.search(String.format("typekeywords:%s", String.format("src_uri_s=%s", src_uri_s)), 0, 0, token);
    // for ArcGIS Portal/Online, look for the resource URL instead of the source XML URI
    QueryResponse search = client.search(String.format("url:%s", String.format("%s", src_uri_s)), 0, 0, token);
    ItemEntry itemEntry = search != null && search.results != null && search.results.length > 0 ? search.results[0] : null;
    
    // if no results found and the source uri (src_uri_s) ends in /0, /1, ... this may be a sublayer
    // look for the parent service
    if (itemEntry == null) {
        String[] uri_parts = src_uri_s.split("/");
        
        // iff the last part is a number, this could be a sublayer
        if (StringUtils.isNumeric(uri_parts[uri_parts.length-1])) {
            String regex = "\\/" + uri_parts[uri_parts.length-1] + "$";
            String mother_uri = "";
            try {
                mother_uri = src_uri_s.replaceFirst(regex, "");
            } catch (Exception e) {
                System.out.println(e.toString());
            }
 
            // now search for mother uri
            search = client.search(String.format("url:%s", String.format("%s", mother_uri)), 0, 0, token);
            itemEntry = search != null && search.results != null && search.results.length > 0 ? search.results[0] : null;
        }
    }
    
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

  private MapAttribute extractMapAttributes(DataReference ref, byte [] content) throws MetaException, IOException, ParserConfigurationException, SAXException {
    MapAttribute attributes = ref.getAttributesMap().values().stream()
            .filter(o -> o instanceof MapAttribute)
            .map(o -> (MapAttribute) o)
            .findFirst()
            .orElse(new MapAttribute(new HashMap<String, Attribute>()));
    
    Document doc = ref.getAttributesMap().values().stream()
            .filter(o -> o instanceof Document)
            .map(o -> (Document) o)
            .findFirst()
            .orElse(null);
    
    if (doc != null) {
      attributes.getNamedAttributes().putAll(metaAnalyzer.extract(doc).getNamedAttributes());
    } else {
      String sXml = "";
      if (ref.getContentType().contains(MimeType.APPLICATION_XML) || ref.getContentType().contains(MimeType.TEXT_XML)) {
        sXml = new String(ref.getContent(MimeType.APPLICATION_XML, MimeType.TEXT_XML), "UTF-8");
      } else if (content!=null) {
        sXml = new String(content, "UTF-8");
      }
      doc = stringToDoc(sXml);
      
      if (doc!=null) {
        MapAttribute extractedAttributes = metaAnalyzer.extract(doc);
        if (extractedAttributes!=null) {
          Map<String, Attribute> namedAttributes = extractedAttributes.getNamedAttributes();
          if (namedAttributes!=null) {
            attributes.getNamedAttributes().putAll(namedAttributes);
          }
        }
      }
    }
      
    return attributes;
  }
  
  private Document stringToDoc(String sXml) throws SAXException, IOException, ParserConfigurationException {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      factory.setNamespaceAware(true);
      factory.setValidating(false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      
      org.w3c.dom.Document theDocument = builder.parse(new InputSource(new StringReader(sXml)));
      
      return theDocument;
  }


  /*
    clean up html tags from inText (for example <br> or &nbsp;) that are hard for XML
  */
  private String sanitize(String inText) {
      String result = inText.replace("&nbsp;", "&amp;nbsp;")
              .replace("<br>","<br/>")
              .replace("</br>", "<br/>")
              .replace("’", "&#8217;");      
      
      return result;
  }  
  private String transformMetadataToArcGISXML(byte [] content) throws MetaException, IOException, ParserConfigurationException, SAXException {
      String sXml = new String(content, StandardCharsets.UTF_8);
      String sXsd = "xsi:schemaLocation=\"http://www.isotc211.org/2005/gmi ftp://ftp.ncddc.noaa.gov/pub/Metadata/Online_ISO_Training/Intro_to_ISO/schemas/ISObio/schema.xsd\"";
      sXml = sXml.replace(sXsd, "");
      System.out.println("XML -> \n" + sXml);
      org.w3c.dom.Document doc = stringToDoc(sXml);
      String arcgisXML = metaAnalyzer.transform2ArcGISXML(doc);
      System.out.println("ArcGIS XML -> \n" + arcgisXML);
      
      return arcgisXML;        
  }
  
  private ItemResponse addItem(String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords, File fileToUpload) throws IOException, URISyntaxException {
    if (token == null) {
      token = generateToken();
    }
    ItemResponse response = addItem(
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, fileToUpload, token);
    if (response.error != null && response.error.code == 498) {
      token = generateToken();
      response = addItem(
              title,
              description,
              url, thumbnailUrl,
              itemType, envelope, typeKeywords, fileToUpload, token);
    }
    return response;
  }

  private ItemResponse addItem(String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords, File fileToUpload, String token) throws IOException, URISyntaxException {
    return client.addItem(
            definition.getCredentials().getUserName(),
            definition.getFolderId(),
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, null, fileToUpload, null, token);
  }

  private ItemResponse addItem(String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords, File fileToUpload, File metadataFile, String token) throws IOException, URISyntaxException {
    return client.addItem(
            definition.getCredentials().getUserName(),
            definition.getFolderId(),
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, null, fileToUpload, metadataFile, token);
  }
  
  private ItemResponse updateItem(String id, String owner, String folderId, String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords, File fileToUpload) throws IOException, URISyntaxException {
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
            itemType, envelope, typeKeywords, fileToUpload, token);
    if (response.error != null && response.error.code == 498) {
      token = generateToken();
      response = updateItem(
              id,
              owner,
              folderId,
              title,
              description,
              url, thumbnailUrl,
              itemType, envelope, typeKeywords, fileToUpload, token);
    }
    return response;
  }

  private ItemResponse updateItem(String id, String owner, String folderId, String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords, File fileToUpload, String token) throws IOException, URISyntaxException {
    return client.updateItem(
            owner,
            folderId,
            id,
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, null, fileToUpload, token);
  }
  
  private ItemResponse updateItem(String id, String owner, String folderId, String title, String description, URL url, URL thumbnailUrl, ItemType itemType, Double[] envelope, String[] typeKeywords, File fileToUpload, File metadataFile, String token) throws IOException, URISyntaxException {
    return client.updateItem(
            owner,
            folderId,
            id,
            title,
            description,
            url, thumbnailUrl,
            itemType, envelope, typeKeywords, null, fileToUpload, metadataFile, token);
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
   private String generateToken(int minutes,String serverUrl,String token) throws URISyntaxException, IOException {
    return client.generateToken(minutes,serverUrl,token).token;
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
    this.httpClient = HttpClientBuilder.create().useSystemProperties().setRedirectStrategy(LaxRedirectStrategy.INSTANCE).build();
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
      if (client!=null) {
        client.close();
      }
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

  private File downloadFile(URL fileToDownload, FileName fileName) throws IOException, URISyntaxException {
    
    File tempFile = File.createTempFile(fileName.name + "-", "." + fileName.ext);
    
    URI uri  = new URI(fileToDownload.getProtocol(), fileToDownload.getAuthority(), fileToDownload.getHost(), fileToDownload.getPort(), fileToDownload.getPath(), fileToDownload.getQuery(), fileToDownload.getRef());
    
    HttpGet request = new HttpGet(uri);
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
  
    private String replaceSpecialCharacters(String inputText) {
        String outputText = "";
        String sourceText = inputText;
        
        HashMap<String, String> extendedCharacters = new HashMap<>();

        extendedCharacters.put("\\x91", "&lsquo;");
        extendedCharacters.put("\\x92", "&rsquo;");
        extendedCharacters.put("\\x93", "&ldquo;");
        extendedCharacters.put("\\x94", "&rdquo;");
        
        for (HashMap.Entry<String, String> val: extendedCharacters.entrySet()) {
            outputText = sourceText.replaceAll(val.getKey(), val.getValue());
            sourceText = outputText;
        }
        return outputText;
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
    
    @Override
    public String toString() {
      return getFullName();
    }
  }
}
