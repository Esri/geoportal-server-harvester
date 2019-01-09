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
package com.esri.geoportal.commons.ckan.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Package result.
 */
public class Pkg {
  private static final ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }
  
  public Boolean success;
  public Dataset [] result;
  
  public void setResult(Object data) {
    if (data instanceof List) {
      
      // data is a list of maps; ieach map holds attributes of a dataset
      List ldata = (List)data;
      ArrayList<Dataset> list = new ArrayList<>();
      for (Object o: ldata) {
        if (o instanceof Map) {
          Map mo = (Map)o;
          list.add(mapper.convertValue(mo, Dataset.class));
        }
      }
      result = list.toArray(new Dataset[list.size()]);
      
    } else if (data instanceof Map) {
      
      // data is a map of attributes of a single dataset
      Map mdata = (Map)data;
      result = new Dataset[] { mapper.convertValue(mdata, Dataset.class) };
      
    } else if (data instanceof Dataset){
      
      // data is a Dataset object
      result = new Dataset[] { (Dataset)data };
    }
  }
}
