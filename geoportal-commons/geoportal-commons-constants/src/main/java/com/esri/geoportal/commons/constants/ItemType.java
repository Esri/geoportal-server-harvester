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

import static com.esri.geoportal.commons.constants.DataType.Text;
import static com.esri.geoportal.commons.constants.DataType.File;
import static com.esri.geoportal.commons.constants.DataType.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Item types.
 */
public enum ItemType {
  SCIENCEBASE_DATASET("Document Link", URL, Pattern.compile(".+www\\.sciencebase\\.gov\\/catalog\\/item\\/.+"), "", MimeType.TEXT_HTML),
  WEB_MAP("Web Map", Text, MimeType.APPLICATION_JSON),
  CITYENGINE_WEB_SCENE("CityEngine Web Scene", File),
  WEB_SCENE("Web Scene", Text, MimeType.APPLICATION_JSON),
  PRO_MAP("Pro Map", File, MimeType.APPLICATION_ESRI_MAPX),
  FEATURE_SERVICE("Feature Service", URL, Pattern.compile(".+/FeatureServer$|.+/FeatureServer/\\d+$|.+/MapServer/\\d+$", Pattern.CASE_INSENSITIVE),"FeatureServer"),
  MAP_SERVICE("Map Service", URL, Pattern.compile(".+/MapServer$", Pattern.CASE_INSENSITIVE),"MapServer"),
  IMAGE_SERVICE("Image Service", URL, Pattern.compile(".+/ImageServer$", Pattern.CASE_INSENSITIVE),"ImageServer"),
  KML("KML", URL, MimeType.APPLICATION_KML, MimeType.APPLICATION_KMZ),
  WMS("WMS", URL, Pattern.compile(".+service=WMS.*$|.+/wms$", Pattern.CASE_INSENSITIVE), null),
  WFS("WFS", URL, Pattern.compile(".+service=WFS.*$|.+/wfs$", Pattern.CASE_INSENSITIVE), null),
  WPS("WPS", URL),
  WMTS("WMTS", URL, Pattern.compile(".+service=WMTS.*$|.+/wmts$", Pattern.CASE_INSENSITIVE), null),
  FEATURE_COLLECTION("Feature Collection", Text, MimeType.APPLICATION_JSON),
  FEATURE_COLLECTION_TEMPLATE("Feature Collection Template", Text, MimeType.APPLICATION_JSON),
  GEODATA_SERVICE("Geodata Service", URL, Pattern.compile(".+/GeoDataServer$", Pattern.CASE_INSENSITIVE),"GeoDataServer"),
  GLOBE_SERVICE("Globe Service", URL, Pattern.compile(".+/GlobeServer$", Pattern.CASE_INSENSITIVE),"GlobeServer"),
  GEOMETRY_SERVICE("Geometry Service", URL, Pattern.compile(".+/GeometryServer$", Pattern.CASE_INSENSITIVE),"GeometryServer"),
  GEOCODING_SERVICE("Geocoding Service", URL, Pattern.compile(".+/GeocodeServer$", Pattern.CASE_INSENSITIVE),"GeocodeServer"),
  NETWORK_ANALYSIS_SERVICE("Network Analysis Service", URL, Pattern.compile(".+/NAServer$", Pattern.CASE_INSENSITIVE),"NAServer"),
  GEOPROCESSING_SERVICE("Geoprocessing Service", URL, Pattern.compile(".+/GPServer$", Pattern.CASE_INSENSITIVE),"GPServer"),
  WORKFLOW_MANAGER_SERVICE("Workflow Manager Service", URL),
  WEB_MAPPING_APPLICATION("Web Mapping Application", Text, MimeType.APPLICATION_JSON),
  MOBILE_APPLICATION("Mobile Application", Text, MimeType.APPLICATION_JSON),
  CODE_ATTACHMENT("Code Attachment", File),
  OPERATION_DASHBOARD_ADD_IN("Operations Dashboard Add In", File),
  OPERATION_VIEW("Operations View", Text),
  OPERATION_DASHBOARD_EXTENSION("Operations Dashboard Extension", URL),
  NATIVE_APPLICATION("Native Application", File),
  NATIVE_APPLICATION_TEMPLATE("Native Application Template", File),
  NATIVE_APPLICATION_INSTALLER("Native Application Installer", File),
  WORKFORCE_PROJECT("Workforce Project", Text, MimeType.APPLICATION_JSON),
  FORM("Form", File),
  SYMBOL_SET("Symbol Set", Text, MimeType.APPLICATION_JSON),
  COLOR_SET("Color Set", Text, MimeType.APPLICATION_JSON),
  SHAPEFILE("Shapefile", File, MimeType.APPLICATION_ZIP),
  FILE_GEODATABASE("File Geodatabase", File, MimeType.APPLICATION_ZIP),
  CSV("CSV", File, MimeType.TEXT_CSV),
  CAD_DRAWING("CAD Drawing", File, MimeType.APPLICATION_ZIP),
  SERVICE_DEFINITION("Service Definition", File, MimeType.APPLICATION_ESRI_SD),
  DOCUMENT_LINK("Document Link", URL),
  MICROSOFT_WORD("Microsoft Word", File, MimeType.APPLICATION_MSWORD, MimeType.APPLICATION_DOCUMENT),
  MICROSOFT_POWERPOINT("Microsoft Powerpoint", File, MimeType.APPLICATION_POWERPOINT, MimeType.APPLICATION_PRESENTATION),
  MICROSOFT_EXCEL("Microsoft Excel", File, MimeType.APPLICATION_MSEXCEL),
  PDF("PDF", File, MimeType.APPLICATION_PDF),
  IMAGE("Image", File, MimeType.IMAGE_GIF, MimeType.IMAGE_JPEG, MimeType.IMAGE_PNG, MimeType.IMAGE_TIFF),
  VISIO_DOCUMENT("Visio Document", File, MimeType.APPLICATION_VISIO),
  IWORK_KEYNOTE("iWork Keynote", File, MimeType.APPLICATION_ZIP),
  IWORK_PAGES("iWork Pages", File, MimeType.APPLICATION_ZIP),
  IWORK_NUMBERS("iWork Numbers", File, MimeType.APPLICATION_ZIP),
  REPORT_TEMPLATE("Report Template", File, MimeType.APPLICATION_ZIP),
  STATISTICAL_DATA_COLLECTION("Statistical Data Collection", File),
  MAP_DOCUMENT("Map Document", File, MimeType.APPLICATION_ESRI_MXD),
  MAP_PACKAGE("Map Package", File, MimeType.APPLICATION_ESRI_MPK),
  MOBILE_BASEMAP_PACKAGE("Mobile Basemap Package", File, MimeType.APPLICATION_ESRI_BPK),
  MOBILE_MAP_PACKAGE("Mobile Map Package", File, MimeType.APPLICATION_ESRI_NMPK),
  TILE_PACKAGE("Tile Package", File, MimeType.APPLICATION_ESRI_TPK),
  PROJECT_PACKAGE("Project Package", File, MimeType.APPLICATION_ESRI_PPKX),
  TASK_PACKAGE("Task Package", File, MimeType.APPLICATION_ESRI_TASKS),
  ARCPAD_PACKAGE("ArcPad Package", File, MimeType.APPLICATION_ZIP),
  EXPLORER_MAP("Explorer Map", File, MimeType.APPLICATION_ESRI_NMF),
  GLOBE_DOCUMENT("Globe Document", File, MimeType.APPLICATION_ESRI_3DD),
  SCENE_DOCUMENT("Scene Document", File, MimeType.APPLICATION_ESRI_SXD),
  PUBLISHED_MAP("Published Map", File, MimeType.APPLICATION_ESRI_PMF),
  MAP_TEMPLATE("Map Template", File, MimeType.APPLICATION_ZIP),
  WINDOWS_MOBILE_PACKAGE("Windows Mobile Package", File),
  LAYOUT("Layout", File, MimeType.APPLICATION_ESRI_PAGX),
  PROJECT_TEMPLATE("Project Template", File, MimeType.APPLICATION_ESRI_APTX),
  LAYER("Layer", File, MimeType.APPLICATION_ESRI_LYR, MimeType.APPLICATION_ESRI_LYRX),
  LAYER_PACKAGE("Layer Package", File, MimeType.APPLICATION_ESRI_LPK),
  EXPLORER_LAYER("Explorer Layer", File, MimeType.APPLICATION_ESRI_NMC),
  SCENE_PACKAGE("Scene Package", File, MimeType.APPLICATION_ESRI_SPK),
  DESKTOP_STYLE("Desktop Style", File, MimeType.APPLICATION_ESRI_STYLX),
  GEOPROCESSING_PACKAGE("Geoprocessing Package", File, MimeType.APPLICATION_ESRI_GPK),
  GEOPROCESSING_PACKAGE_PRO("Geoprocessing Package (Pro version)", File, MimeType.APPLICATION_ESRI_GPKX),
  GEOPROCESSING_SAMPLE("Geoprocessing Sample", File),
  LOCATOR_PACKAGE("Locator Package", File, MimeType.APPLICATION_ESRI_GCPK),
  RULE_PACKAGE("Rule Package", File, MimeType.APPLICATION_ESRI_GPKX),
  RASTER_FUNCTION_TEMPLATE("Raster function template", File),
  WORKFLOW_MANAGER_PACKAGE("Workflow Manager Package", File, MimeType.APPLICATION_ESRI_WPK),
  DESKTOP_APPLICATION("Desktop Application", File, MimeType.APPLICATION_ZIP),
  DESKTOP_APPLICATION_TEMPLATE("Desktop Application Template", File, MimeType.APPLICATION_ZIP),
  CODE_SAMPLE("Code Sample", File),
  DESKTOP_ADD_IN("Desktop Add In", File, MimeType.APPLICATION_ESRI_ADDIN),
  EXPLORER_ADD_IN("Explorer Add In", File, MimeType.APPLICATION_ESRI_EAZ),
  ARCGIS_PRO_ADD_IN("ArcGIS Pro Add In", File, MimeType.APPLICATION_ESRI_ADDINX),
  METADATA_XML("Document Link", URL, Pattern.compile(".+\\.xml$", Pattern.CASE_INSENSITIVE), "", MimeType.APPLICATION_XML);

  private final String typeName;
  private final DataType dataType;
  private final MimeType[] mimeTypes;
  private final Pattern pattern;
  private final String serviceType;

  ItemType(String typeName, DataType dataType, Pattern pattern, String serviceType, MimeType... mimeTypes) {
    this.typeName = typeName;
    this.dataType = dataType;
    this.mimeTypes = mimeTypes;
    this.pattern = pattern;
    this.serviceType = serviceType;
  }

  ItemType(String typeName, DataType dataType, MimeType... mimeTypes) {
    this(typeName, dataType, null, null, mimeTypes);
  }

  /**
   * Gets type name.
   *
   * @return type name
   */
  public String getTypeName() {
    return typeName;
  }

  /**
   * Gets data type.
   *
   * @return data type
   */
  public DataType getDataType() {
    return dataType;
  }

  /**
   * Gets mime types.
   *
   * @return mime types
   */
  public MimeType[] getMimeTypes() {
    return mimeTypes;
  }

  /**
   * Gets pattern.
   * @return pattern
   */
  public Pattern getPattern() {
    return pattern;
  }
  
  /**
   * Gets service type.
   * @return service type
   */
  public String getServiceType() {
    return serviceType;
  }
  
  public boolean hasUniqueMimeType() {
    if (getMimeTypes()==null || getMimeTypes().length==0) {
      return true;
    }
    return Stream.of(ItemType.values())
            .filter(it -> it!=this)
            .filter(it -> {
              if (it.getMimeTypes()==null) {
                return false;
              }
              return Stream.of(it.getMimeTypes()).filter(mt -> {
                return Stream.of(this.getMimeTypes()).filter(x -> x==mt).count() > 0;
              }).count() > 0;
             })
            .count() == 0;
  }
  
  /**
   * Match item types by pattern.
   * @param value value to match
   * @return list of matching item types
   */
  public static List<ItemType> matchPattern(String value) {
    ArrayList<ItemType> matches = new ArrayList<>();
    for (ItemType it: ItemType.values()) {
      if (it.getPattern()!=null && it.getPattern().matcher(value).matches()) {
        matches.add(it);
      }
    }
    return matches;
  }
  
  /**
   * Match item types by mime type.
   * @param mimeType mime type
   * @return list of matching item types
   */
  public static List<ItemType> matchMimeType(MimeType mimeType) {
    ArrayList<ItemType> matches = new ArrayList<>();
    for (ItemType it: ItemType.values()) {
      if (it.getMimeTypes()!=null && Arrays.asList(it.getMimeTypes()).contains(mimeType)) {
        matches.add(it);
      }
    }
    return matches;
  }
  
  /**
   * Match item types by file name extension.
   * @param ext file name extension
   * @return list of matching item types
   */
  public static List<ItemType> matchExt(String ext) {
    ArrayList<ItemType> matches = new ArrayList<>();
    MimeType mimeType = MimeTypeUtils.mapExtension(ext);
    if (mimeType!=null) {
      return matchMimeType(mimeType);
    }
    return matches;
  }
}
