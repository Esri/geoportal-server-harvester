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
package com.esri.geoportal.commons.constants;

/**
 * Mime type.
 */
public enum MimeType {
  APPLICATION_ESRI_MXD("application/vnd.esri.mxd"),
  APPLICATION_ESRI_SD("application/vnd.esri.sd"),
  APPLICATION_ESRI_MPK("application/vnd.esri.mpk"),
  APPLICATION_ESRI_BPK("application/vnd.esri.bpk"),
  APPLICATION_ESRI_NMPK("application/vnd.esri.nmpk"),
  APPLICATION_ESRI_TPK("application/vnd.esri.tpk"),
  APPLICATION_ESRI_PPKX("application/vnd.esri.ppkx"),
  APPLICATION_ESRI_NMF("application/vnd.esri.nmf"),
  APPLICATION_ESRI_3DD("application/vnd.esri.3dd"),
  APPLICATION_ESRI_SXD("application/vnd.esri.sxd"),
  APPLICATION_ESRI_PMF("application/vnd.esri.pmf"),
  APPLICATION_ESRI_MAPX("application/vnd.esri.mapx"),
  APPLICATION_ESRI_PAGX("application/vnd.esri.pagx"),
  APPLICATION_ESRI_APTX("application/vnd.esri.aptx"),
  APPLICATION_ESRI_LYR("application/vnd.esri.lyr"),
  APPLICATION_ESRI_LYRX("application/vnd.esri.lyrx"),
  APPLICATION_ESRI_LPK("application/vnd.esri.lpk"),
  APPLICATION_ESRI_NMC("application/vnd.esri.nmc"),
  APPLICATION_ESRI_SPK("application/vnd.esri.spk"),
  APPLICATION_ESRI_STYLX("application/vnd.esri.stylx"),
  APPLICATION_ESRI_GCPK("application/vnd.esri.gcpk"),
  APPLICATION_ESRI_RPK("application/vnd.esri.rpk"),
  APPLICATION_ESRI_WPK("application/vnd.esri.wpk"),
  APPLICATION_ESRI_ADDIN("application/vnd.esri.addin"),
  APPLICATION_ESRI_ADDINX("application/vnd.esri.addinx"),
  APPLICATION_ESRI_EAZ("application/vnd.esri.eaz"),
  APPLICATION_ESRI_TASKS("application/vnd.esri.tasks"),
  APPLICATION_ESRI_GPK("application/vnd.esri.gpk"),
  APPLICATION_ESRI_GPKX("application/vnd.esri.gpkx"),
  
  APPLICATION_ATOM_XML("application/atom+xml"),
  APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
  APPLICATION_JSON("application/json"),
  APPLICATION_OCTET_STREAM("application/octet-stream"),
  APPLICATION_XHTML_XML("application/xhtml+xml"),
  APPLICATION_XML("application/xml"),
  APPLICATION_PDF("application/pdf"),
  APPLICATION_MSWORD("application/msword"),
  APPLICATION_DOCUMENT("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
  APPLICATION_MSEXCEL("application/vnd.ms-excel"),
  APPLICATION_POWERPOINT("application/vnd.ms-powerpoint"),
  APPLICATION_PRESENTATION("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
  APPLICATION_KML("application/vnd.google-earth.kml+xml"),
  APPLICATION_KMZ("application/vnd.google-earth.kmz"),
  APPLICATION_ZIP("application/zip"),
  APPLICATION_VISIO("application/x-visio"),
  
  IMAGE_GIF("image/gif"),
  IMAGE_JPEG("image/jpeg"),
  IMAGE_PNG("image/png"),
  IMAGE_TIFF("image/tiff"),
  
  MULTIPART_FORM_DATA("multipart/form-data"),
  
  TEXT_HTML("text/html"),
  TEXT_CSS("text/css"),
  TEXT_PLAIN("text/plain"),
  TEXT_CSV("text/csv"),
  TEXT_XML("text/xml");
  
  private final String name;
  
  /**
   * Creates instance of the enum.
   * @param name name
   */
  MimeType(String name) {
    this.name = name;
  }
  
  /**
   * Gets name.
   * @return name
   */
  public String getName() {
    return name;
  }
  
  @Override
  public String toString() {
    return name;
  }
  
  /**
   * Parses string representation of the mime type.
   * @param strMimeType string representation of the mime type
   * @param defaultMimeType default mime type
   * @return mime type or default mime type
   */
  public static MimeType parse(String strMimeType, MimeType defaultMimeType) {
    for (MimeType mime: values()) {
      if (mime.getName().equals(strMimeType)) {
        return mime;
      }
    }
    return defaultMimeType;
  }
  
  /**
   * Parses string representation of the mime type.
   * @param strMimeType string representation of the mime type
   * @return mime type or default mime type or <code>null</code> if invalid mime
   */
  public static MimeType parse(String strMimeType) {
    return parse(strMimeType, null);
  }
}
