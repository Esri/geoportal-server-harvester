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
package com.esri.geoportal.commons.csw.client.impl;

import static com.esri.core.geometry.Operator.Type.Contains;
import static com.esri.core.geometry.Operator.Type.Intersects;
import com.esri.geoportal.commons.csw.client.ICriteria;
import com.esri.geoportal.commons.csw.client.IProfile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Profile implementation.
 */
public class Profile implements IProfile {
  private final Logger LOG = LoggerFactory.getLogger(Profile.class);

  private String id;
  private String name;
  private String description;
  private String getRecordsReqXslt;
  private String getRecordsRspXslt;
  private String getRecordByIdReqKVP;
  private String getRecordByIdRspXslt;

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getKvp() {
    return getRecordByIdReqKVP;
  }

  public void setKvp(String getRecordByIdReqKVP) {
    this.getRecordByIdReqKVP = getRecordByIdReqKVP;
  }

  @Override
  public String getGetRecordsReqXslt() {
    return getRecordsReqXslt;
  }

  public void setGetRecordsReqXslt(String getRecordsReqXslt) {
    this.getRecordsReqXslt = getRecordsReqXslt;
  }

  public String getGetRecordsRspXslt() {
    return getRecordsRspXslt;
  }

  public void setGetRecordsRspXslt(String getRecordsRspXslt) {
    this.getRecordsRspXslt = getRecordsRspXslt;
  }

  public String getGetRecordByIdRspXslt() {
    return getRecordByIdRspXslt;
  }

  public void setGetRecordByIdRspXslt(String getRecordByIdRspXslt) {
    this.getRecordByIdRspXslt = getRecordByIdRspXslt;
  }

  @Override
  public String generateCSWGetRecordsRequest(ICriteria criteria) {
    String internalRequestXml = createInternalXmlRequest(criteria);
    try (
            ByteArrayInputStream internalRequestInputStream = new ByteArrayInputStream(internalRequestXml.getBytes("UTF-8"));
            InputStream reqXsltInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(Constants.CONFIG_FOLDER_PATH + "/" + getGetRecordsReqXslt())) {
      
      // create internal request DOM
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document internalRequestDOM = builder.parse(new InputSource(internalRequestInputStream));

      // create transformer
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Templates template = transformerFactory.newTemplates(new StreamSource(reqXsltInputStream));
      Transformer transformer = template.newTransformer();
      
      // perform transformation
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(internalRequestDOM), new StreamResult(writer));
      
      return writer.toString();
    } catch (Exception ex) {
      LOG.warn("Error creating CSW get records request.", ex);
      return "";
    }
  }

  @Override
  public String generateCSWGetMetadataByIDRequestURL(String baseURL, String recordId) {
    StringBuilder sb = new StringBuilder();
    sb.append(baseURL)
            .append(baseURL.endsWith("?") ? "" : baseURL.contains("?") ? "&" : "?")
            .append(getKvp())
            .append("&ID=")
            .append(recordId);
    return sb.toString();
  }

  @Override
  public String getResponsexslt() {
    return Constants.CONFIG_FOLDER_PATH + "/" + getGetRecordsRspXslt();
  }

  @Override
  public String getMetadataxslt() {
    return Constants.CONFIG_FOLDER_PATH + "/" + getGetRecordByIdRspXslt();
  }

  /**
   * Creates to internal xml request.
   *
   * @return string representing internal xml request
   */
  private String createInternalXmlRequest(ICriteria criteria) {
    String request = "<?xml version='1.0' encoding='UTF-8' ?>";
    request += "<GetRecords>" + "<StartPosition>" + criteria.getStartPosition()
            + "</StartPosition>";
    request += "<MaxRecords>" + criteria.getMaxRecords() + "</MaxRecords>";
    request += "<KeyWord>" + StringEscapeUtils.escapeXml11(criteria.getSearchText()) + "</KeyWord>";
    request += ("<LiveDataMap>" + criteria.isLiveDataAndMapsOnly() + "</LiveDataMap>");
    if (criteria.getEnvelope() != null) {
      request += ("<Envelope>");
      request += "<MinX>" + criteria.getEnvelope().getXMin() + "</MinX>";
      request += "<MinY>" + criteria.getEnvelope().getYMin() + "</MinY>";
      request += "<MaxX>" + criteria.getEnvelope().getXMax() + "</MaxX>";
      request += "<MaxY>" + criteria.getEnvelope().getYMax() + "</MaxY>";
      request += "</Envelope>";
      request += "<RecordsFullyWithinEnvelope>" + criteria.getOperation() == Contains + "</RecordsFullyWithinEnvelope>";
      request += "<RecordsIntersectWithEnvelope>" + criteria.getOperation() == Intersects + "</RecordsIntersectWithEnvelope>";

    }
    request += "</GetRecords>";

    return request;
  }

  @Override
  public String toString() {
    return String.format("PROFILE :: %s, %s", id, name);
  }
}
