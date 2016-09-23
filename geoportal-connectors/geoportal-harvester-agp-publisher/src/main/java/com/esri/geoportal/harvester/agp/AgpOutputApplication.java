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
package com.esri.geoportal.harvester.agp;

import com.esri.geoportal.commons.meta.MetaAnalyzer;
import com.esri.geoportal.commons.meta.util.MultiMetaAnalyzerWrapper;
import com.esri.geoportal.commons.meta.xml.SimpleDcMetaAnalyzer;
import com.esri.geoportal.commons.meta.xml.SimpleFgdcMetaAnalyzer;
import com.esri.geoportal.commons.meta.xml.SimpleIso15115MetaAnalyzer;
import com.esri.geoportal.commons.meta.xml.SimpleIso15115_2MetaAnalyzer;
import com.esri.geoportal.commons.meta.xml.SimpleIso15119MetaAnalyzer;
import com.esri.geoportal.commons.utils.SimpleCredentials;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.DataReferenceSerializer;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.specs.OutputBroker;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;

/**
 * Agp output application.
 */
public class AgpOutputApplication {

  public static void main(String[] args) throws Exception {
    if (args.length==3) {
      MetaAnalyzer metaAnalyzer = new MultiMetaAnalyzerWrapper(
              new SimpleDcMetaAnalyzer(),
              new SimpleFgdcMetaAnalyzer(),
              new SimpleIso15115MetaAnalyzer(),
              new SimpleIso15115_2MetaAnalyzer(),
              new SimpleIso15119MetaAnalyzer()
      );
      
      AgpOutputConnector connector = new AgpOutputConnector(metaAnalyzer);
      EntityDefinition def = new EntityDefinition();
      AgpOutputBrokerDefinitionAdaptor adaptor = new AgpOutputBrokerDefinitionAdaptor(def);
      adaptor.setHostUrl(new URL(args[0]));
      adaptor.setFolderId(StringUtils.trimToNull(args[1]));
      adaptor.setCredentials(new SimpleCredentials(args[2], args[3]));
      OutputBroker broker = connector.createBroker(def);
    
      DataReferenceSerializer ser = new DataReferenceSerializer();
      DataReference ref = null;
      while (( ref = ser.deserialize(System.in))!=null) {
        broker.publish(ref);
      }
    }
  }
}
