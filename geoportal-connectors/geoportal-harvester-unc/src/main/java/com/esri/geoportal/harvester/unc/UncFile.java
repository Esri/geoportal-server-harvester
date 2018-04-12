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
package com.esri.geoportal.harvester.unc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.constants.MimeTypeUtils;
import com.esri.geoportal.commons.meta.AttributeUtils;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.meta.xml.SimpleDcMetaBuilder;
import com.esri.geoportal.commons.utils.PdfUtils;
import com.esri.geoportal.commons.utils.XmlUtils;
import com.esri.geoportal.harvester.api.base.SimpleDataReference;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

/**
 * UNC file.
 */
/*package*/ class UncFile {
  private final UncBroker broker;
  private final Path file;

  /**
   * Creates instance of UNC file.
   * @param broker broker
   * @param file file
   */
  public UncFile(UncBroker broker, Path file) {
    this.broker = broker;
    this.file = file;
  }

  /**
   * Reads content.
   * @return content reference
   * @throws IOException if reading content fails
   * @throws URISyntaxException if file url is an invalid URI
   */
  public SimpleDataReference readContent() throws IOException, URISyntaxException {
    Date lastModifiedDate = readLastModifiedDate();
    MimeType contentType = readContentType();
    try (InputStream input = Files.newInputStream(file)) {
      SimpleDataReference ref = new SimpleDataReference(broker.getBrokerUri(), broker.getEntityDefinition().getLabel(), file.toAbsolutePath().toString(), lastModifiedDate, file.toUri(), broker.td.getSource().getRef(), broker.td.getRef());

      // Determine if we're looking at a PDF file
      if (MimeType.APPLICATION_PDF.equals(contentType)) {
        Properties metaProps = PdfUtils.readMetadata(input);
        
        if (metaProps != null) {
          Properties props = new Properties();
          props.put(WKAConstants.WKA_TITLE, metaProps.getOrDefault(PdfUtils.PROP_TITLE, file.getFileName().toString()));
          props.put(WKAConstants.WKA_DESCRIPTION, metaProps.getOrDefault(PdfUtils.PROP_SUBJECT, "<no description>"));
          props.put(WKAConstants.WKA_MODIFIED, metaProps.getOrDefault(PdfUtils.PROP_MODIFICATION_DATE, lastModifiedDate));
          props.put(WKAConstants.WKA_RESOURCE_URL, file.toAbsolutePath().toString());

          try {
            MapAttribute attr = AttributeUtils.fromProperties(props);
            Document document = new SimpleDcMetaBuilder().create(attr);
            byte [] bytes = XmlUtils.toString(document).getBytes("UTF-8");
            ref.addContext(MimeType.APPLICATION_XML, bytes);
          } catch (MetaException | TransformerException ex) {
            throw new IOException(ex);
          }
        } else {
          ref.addContext(contentType, IOUtils.toByteArray(input));  
        }
      } else {
        ref.addContext(contentType, IOUtils.toByteArray(input));
      }
      return ref;
    }
  }

  /**
   * Reads last modified date.
   * @return last modified date
   */
  private Date readLastModifiedDate() throws IOException {
    return new Date(Files.getLastModifiedTime(file).toMillis());
  }
  
  /**
   * Reads content type.
   * @return content type or <code>null</code> if unable to read content type
   */
  private MimeType readContentType() {
    try {
      String strFileUrl = file.toAbsolutePath().toString();
      int lastDotIndex = strFileUrl.lastIndexOf(".");
      String ext = lastDotIndex>=0? strFileUrl.substring(lastDotIndex+1): "";
      return MimeTypeUtils.mapExtension(ext);
    } catch (Exception ex) {
      return null;
    }
  }
  
  @Override
  public String toString() {
    return file.toString();
  }
}
