/*
 * Copyright 2020 Esri, Inc.
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
package com.esri.geoportal.commons.thredds.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * THREDDS client.
 */
public class Client implements Closeable {

  private final Logger LOG = LoggerFactory.getLogger(Client.class);

  private final CloseableHttpClient httpClient;
  private final URL url;

  public static void main(String[] args) throws Exception {
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//    Client client = new Client(httpClient, new URL("https://chlthredds.erdc.dren.mil/thredds/catalog/wis/Atlantic/ST41001/1980/catalog.xml"));
    Client client = new Client(httpClient, new URL("https://data.nodc.noaa.gov/thredds/catalog.xml"));
    Content content = client.listItems(null);
    
    System.out.println(content.url);
    System.out.println(content.records);
    System.out.println(content.folders);
    System.out.println("Ready...");
  }

  /**
   * Creates instance of the client.
   *
   * @param httpClient HTTP client
   * @param url base URL
   */
  public Client(CloseableHttpClient httpClient, URL url) {
    this.httpClient = httpClient;
    this.url = url;
  }

  public Content listItems(URL url) throws IOException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException {
    ArrayList<Record> records = new ArrayList<>();
    ArrayList<URL> folders = new ArrayList<>();

    Document doc = readContent(url != null ? url : this.url);

    XPath xPath = XPathFactory.newInstance().newXPath();
    Node ndCatalog = (Node)xPath.evaluate("/catalog", doc, XPathConstants.NODE);
    
    String iso = StringUtils.trimToEmpty((String) xPath.evaluate("//service[@serviceType='ISO']/@base", ndCatalog, XPathConstants.STRING));
    if (!iso.isEmpty()) {
      // creating TFiles only if iso exist
      URL baseUrl = new URL(this.url, iso);
    
      NodeList ndDatasets = (NodeList)xPath.evaluate("//dataset[string-length(normalize-space(@urlPath))>0]", ndCatalog, XPathConstants.NODESET);
      for (int i=0; i < ndDatasets.getLength(); i++){
        Node ndDataset = ndDatasets.item(i);
        String urlPath = (String)xPath.evaluate("@urlPath",ndDataset,XPathConstants.STRING);
        String ID = (String)xPath.evaluate("@ID",ndDataset,XPathConstants.STRING);
        if (!urlPath.isEmpty()) {
          URL datasetUrl = new URL(baseUrl, urlPath);
          URIBuilder builder = new URIBuilder(datasetUrl.toURI());
          URL fetchUrl = builder.addParameter("catalog", this.url.toExternalForm()).addParameter("dataset", ID).build().toURL();
          records.add(new Record(ID, fetchUrl));
        }
      }
    }
    
    NodeList ndCatalogRefs = (NodeList)xPath.evaluate("//catalogRef/@href", ndCatalog, XPathConstants.NODESET);
    for (int i=0; i < ndCatalogRefs.getLength(); i++){
      Node ndCatalogRef = ndCatalogRefs.item(i);
      String catalogRefUrl = StringUtils.trimToEmpty(ndCatalogRef.getNodeValue());
      URL catalogUrl = new URL(this.url, catalogRefUrl);
      folders.add(catalogUrl);
    }
    
    
    return new Content(url, records, folders);
  }

  public Document readContent(URL url) throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    HttpGet method = new HttpGet(url.toURI());
    try (CloseableHttpResponse httpResponse = httpClient.execute(method); InputStream responseInputStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode() >= 400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      if (httpResponse.getStatusLine().getStatusCode() == 302) {
        HttpGet method2 = new HttpGet(URI.create(httpResponse.getFirstHeader("Location").getValue()));
        try (CloseableHttpResponse httpResponse2 = httpClient.execute(method2); InputStream responseInputStream2 = httpResponse2.getEntity().getContent();) {
          if (httpResponse2.getStatusLine().getStatusCode() >= 400) {
            throw new HttpResponseException(httpResponse2.getStatusLine().getStatusCode(), httpResponse2.getStatusLine().getReasonPhrase());
          }
          return readContentFromStream(responseInputStream2);
        }
      } else {
        return readContentFromStream(responseInputStream);
      }
    }
  }

  private Document readContentFromStream(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new InputStreamReader(stream, "UTF-8"));
    return builder.parse(is);
  }

  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
  }

}
