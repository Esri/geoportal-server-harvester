/*
 * Copyright 2016 Esri, Inc..
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
package com.esri.geoportal.harvester.waf;

import com.esri.geoportal.harvester.api.DataConnectorTemplate;
import static com.esri.geoportal.harvester.waf.WafAttributesAdaptor.P_HOST_URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.esri.geoportal.harvester.api.DataInput;
import com.esri.geoportal.harvester.api.DataInputFactory;

/**
 * Folder input factory.
 */
public class WafInputFactory implements DataInputFactory {

  @Override
  public DataInput create(Map<String, String> attributes) throws IllegalArgumentException {
    WafAttributesAdaptor attr = new WafAttributesAdaptor(attributes);
    return new WafDataInput(attr);
  }

  @Override
  public DataConnectorTemplate getTemplate() {
    List<DataConnectorTemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new DataConnectorTemplate.StringArgument(P_HOST_URL, "URL"));
    return new DataConnectorTemplate("WAF", "Web accessible folder", arguments);
  }
  
}