/*
 * Copyright 2016,2017 Esri, Inc.
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
package com.esri.geoportal.geoportal.commons.geometry;

import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 * Geometry service.
 */
public class GeometryService implements Closeable {
  private final CloseableHttpClient httpClient;
  private final URL geometryServiceUrl;

  private static final ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }
  
  public GeometryService(CloseableHttpClient client, URL geometryServiceUrl) {
    this.httpClient = client;
    this.geometryServiceUrl = geometryServiceUrl;
  }
  
  public MultiPoint project(MultiPoint mp, int fromWkid, int toWkid) throws IOException, URISyntaxException {
    HttpPost request = new HttpPost(createProjectUrl().toURI());
    
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    params.put("inSR", Integer.toString(fromWkid));
    params.put("outSR", Integer.toString(toWkid));
    params.put("geometries", createGeometries(mp));
    
    HttpEntity entrity = new UrlEncodedFormEntity(params.entrySet().stream()
            .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList()), "UTF-8");
    request.setEntity(entrity);
    
    try (CloseableHttpResponse httpResponse = httpClient.execute(request); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      MultiPointGeometry geom = mapper.readValue(contentStream, MultiPointGeometry.class);
      MultiPoint result  = new MultiPoint();
      geom.geometries[0].points.forEach(pt->result.add(pt[0], pt[1]));
      return result;
    }
  }

  public MultiPoint project(MultiPoint mp, String fromWkt, int toWkid) throws IOException, URISyntaxException {
    HttpPost request = new HttpPost(createProjectUrl().toURI());
    
    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    params.put("inSR", String.format("{\"wkt\": \"%s\"}", fromWkt.replaceAll("\"", "\\\\\"")));
    params.put("outSR", Integer.toString(toWkid));
    params.put("geometries", createGeometries(mp));
    
    HttpEntity entrity = new UrlEncodedFormEntity(params.entrySet().stream()
            .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList()), "UTF-8");
    request.setEntity(entrity);
    
    try (CloseableHttpResponse httpResponse = httpClient.execute(request); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      MultiPointGeometry geom = mapper.readValue(contentStream, MultiPointGeometry.class);
      MultiPoint result  = new MultiPoint();
      geom.geometries[0].points.forEach(pt->result.add(pt[0], pt[1]));
      return result;
    }
  }

  public MultiPoint fromGeoCoordinateString(List<String> coordinateStrings, String conversionType, int toWkid) throws IOException, URISyntaxException {
    HttpPost request = new HttpPost(createFromGeoCoordinateStringUrl().toURI());

    HashMap<String, String> params = new HashMap<>();
    params.put("f", "json");
    params.put("sr", Integer.toString(toWkid));
    params.put("strings", String.format("[\"%s\"]", String.join("\",\"", coordinateStrings)));
    params.put("conversionType", "UTM");
    params.put("coversionMode", "utmDefault");

    HttpEntity entity = new UrlEncodedFormEntity(params.entrySet().stream()
      .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList()), "UTF-8");
    request.setEntity(entity);
    
    try (CloseableHttpResponse httpResponse = httpClient.execute(request); InputStream contentStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode()>=400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }

      // System.out.println(IOUtils.readLines(contentStream));
      FromGeoCoordinateStringResponse response = mapper.readValue(contentStream, FromGeoCoordinateStringResponse.class);
      return response.toMultipointGeometry();
    }
  }
  
  private URL createProjectUrl() throws MalformedURLException {
    return new URL(geometryServiceUrl.toExternalForm().replaceAll("/*$", "/project"));
  }

  private URL createFromGeoCoordinateStringUrl() throws MalformedURLException {
    return new URL(geometryServiceUrl.toExternalForm().replaceAll("/*$", "/fromGeoCoordinateString"));
  }
  
  private static String createGeometries(MultiPoint mp) throws JsonProcessingException {
    MultiPointGeometry result = new MultiPointGeometry();
    result.geometries[0].points = new MultiPointList(mp).stream().map(p->new Double[]{p.getX(),p.getY()}).collect(Collectors.toList());
    return mapper.writeValueAsString(result);
  }

  @Override
  public void close() throws IOException {
    httpClient.close();
  }
  
  public static final class MultiPointGeometry {
    public String geometryType = "esriGeometryMultipoint";
    public MultiPointGeometries [] geometries = new MultiPointGeometries[]{ new MultiPointGeometries() };
  }
  
  public static final class MultiPointGeometries {
    public List<Double[]> points;
  }
  
  private static class MultiPointList extends AbstractList<Point> {
    private final MultiPoint mp;

    public MultiPointList(MultiPoint mp) {
      this.mp = mp;
    }

    @Override
    public Point get(int index) {
      return mp.getPoint(index);
    }

    @Override
    public int size() {
      return mp.getPointCount();
    }
  }

  private static final class FromGeoCoordinateStringResponse {
    public List<Double[]> coordinates;

    public MultiPoint toMultipointGeometry () {
      MultiPoint mp = new MultiPoint();

      coordinates.forEach(pointSet -> {
        mp.add(pointSet[0], pointSet[1]);
      });

      return mp;
    }
  }
}
