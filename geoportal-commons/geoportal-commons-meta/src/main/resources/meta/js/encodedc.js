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
load("classpath:meta/js/base.js");

function create(attributes) {
  var B = newXmlBuilder();
  
  B.setPrefix("rdf", URI.RDF);
  B.writeStartDocument();
  
  B.writeStartElement("rdf","RDF",URI.RDF);
  B.writeAttribute("xmlns:rdf",URI.RDF);
  B.writeAttribute("xmlns:dc",URI.DC);
  B.writeAttribute("xmlns:dct",URI.DCT);
  B.writeAttribute("xmlns:ows",URI.OWS);
  B.writeAttribute("xmlns:dcmiBox",URI.DCMIBOX);
  
  B.writeStartElement(URI.RDF,"Description");
  B.writeAttribute("rdf:about",readValue(attributes,"identifier",""));
  
  B.writeStartElement("dc","identifier",URI.DC);
  B.writeCharacters(readValue(attributes,"identifier",""));
  B.writeEndElement();
  
  var title = readValue(attributes,"title");
  if (title) {
    B.writeStartElement("dc","title",URI.DC);
    B.writeCharacters(title);
    B.writeEndElement();
  }
  
  var description = readValue(attributes,"description");
  if (description) {
    B.writeStartElement("dc","description",URI.DC);
    B.writeCharacters(description);
    B.writeEndElement();
    B.writeStartElement("dct","abstract",URI.DCT);
    B.writeCharacters(description);
    B.writeEndElement();
  }
  
  var modified = readValue(attributes,"modified");
  if (modified) {
    B.writeStartElement("dc","date",URI.DC);
    B.writeCharacters(modified);
    B.writeEndElement();
  }
  
  var resourceUrl = readValue(attributes,"resource.url");
  if (resourceUrl) {
    B.writeStartElement("dct","references",URI.DCT);
    var scheme = readValue(attributes,"resource.url.scheme");
    if (scheme) {
      B.writeAttribute("scheme",scheme);
    }
    B.writeCharacters(resourceUrl);
    B.writeEndElement();
  }
  
  var urls = readArray(attributes,"references");
  if (urls) {
    for (i=0; i<urls.length; i++) {
      var urlInfo = urls[i];
      if (urlInfo) {
        if (urlInfo.isString()) {
          var value = urlInfo.getValue();
          if (value) {
            B.writeStartElement("dct","references",URI.DCT);
            B.writeCharacters(value);
            B.writeEndElement();
          }
        } else if (urlInfo.isMap()) {
          var values = urlInfo.getNamedAttributes();
          if (values) {
            var url = values.get("resource.url");
            var scheme = values.get("resource.url.scheme");
            if (url) {
              B.writeStartElement("dct","references",URI.DCT);
              if (scheme) {
                B.writeAttribute("dct:scheme",scheme);
              }
              B.writeCharacters(url);
              B.writeEndElement();
            }
          }
        }
      }
    }
  }
  
  var bbox = readValue(attributes,"bbox");
  if (bbox) {
    var bboxArr = bbox.split(",");
    var LowerCorner, UpperCorner;
    if (bboxArr && bboxArr.length==4) {
      LowerCorner = "" + bboxArr[0] + " " + bboxArr[1];
      UpperCorner = "" + bboxArr[2] + " " + bboxArr[3];
    } else if (bboxArr && bboxArr.length==2) {
      LowerCorner = "" + bboxArr[0];
      UpperCorner = "" + bboxArr[1];
    }
    if (LowerCorner && UpperCorner) {
      B.writeStartElement("ows","WGS84BoundingBox",URI.OWS);
      
      B.writeStartElement("ows","LowerCorner",URI.OWS);
      B.writeCharacters(LowerCorner.trim());
      B.writeEndElement();
      
      B.writeStartElement("ows","UpperCorner",URI.OWS);
      B.writeCharacters(UpperCorner.trim());
      B.writeEndElement();
      
      B.writeEndElement();
    }
  }
  
  B.writeEndElement();
  
  B.writeEndElement();
  
  B.writeEndDocument();
  
  return B.getXml();
}


