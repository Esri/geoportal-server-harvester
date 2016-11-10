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

var URI = {
  ATOM: "http://www.w3.org/2005/Atom",
  CSW: "http://www.opengis.net/cat/csw/3.0",
  DC: "http://purl.org/dc/elements/1.1/",
  DCT: "http://purl.org/dc/terms/",
  GEO: "http://a9.com/-/opensearch/extensions/geo/1.0/",
  GEOPOS: "http://www.w3.org/2003/01/geo/wgs84_pos#",
  GEORSS: "http://www.georss.org/georss",
  GEORSS10: "http://www.georss.org/georss/10",
  TIME: "http://a9.com/-/opensearch/extensions/time/1.0/",
  OPENSEARCH: "http://a9.com/-/spec/opensearch/1.1/",
  OWS: "http://www.opengis.net/ows/2.0",
  SDI: "http://www.geodata.gov/sdi_atom",
  RDF: "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
  DCMIBOX: "http://dublincore.org/documents/2000/07/11/dcmi-box/"
};

function newXmlBuilder() {
  var XmlBuilder = Java.type("com.esri.geoportal.commons.meta.js.XmlBuilder");
  var xmlBuilder = new XmlBuilder();
  xmlBuilder.init();
  return xmlBuilder;
}

function readValue(attributes,name,defValue) {
  var value = null;
  if (attributes.isMap()) {
    var attribute = attributes.getNamedAttributes().get(name);
    if (attribute && attribute.isString()) {
      value = attribute.getValue();
    }
  }
  return value? value: defValue;
}

function readArray(attributes,name,defValue) {
  var value = null;
  if (attributes.isMap()) {
    var attribute = attributes.getNamedAttributes().get(name);
    if (attribute && attribute.isArray()) {
      value = attribute.getAttributes();
    }
  }
  return value? value: defValue;
}

function readMap(attributes,name,defValue) {
  var value = null;
  if (attributes.isMap()) {
    var attribute = attributes.getNamedAttributes().get(name);
    if (attribute && attribute.isMap()) {
      value = attribute.getNamedAttributes();
    }
  }
  return value? value: defValue;
}


