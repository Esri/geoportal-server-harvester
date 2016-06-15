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
package com.esri.geoportal.harvester.folder;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.base.DataReferenceSerializer;
import java.io.File;

/**
 * Folder application.
 * Java application designed to read data from the pipeline and publish it into the local folder.
 * It takes a root folder as an argument.
 */
public class FolderApplication {
  /**
   * Main method.
   * @param args arguments: a root folder
   * @throws Exception if any exception occurs
   */
  public static void main(String[] args) throws Exception {
    if (args.length==1) {
      File rootFolder = new File(args[0]);
      FolderConnector connector = new FolderConnector();
      EntityDefinition def = new EntityDefinition();
      FolderBrokerDefinitionAdaptor adaptor = new FolderBrokerDefinitionAdaptor(def);
      adaptor.setRootFolder(rootFolder);
      FolderBroker broker = connector.createBroker(def);
    
      DataReferenceSerializer ser = new DataReferenceSerializer();
      DataReference ref = null;
      while (( ref = ser.deserialize(System.in))!=null) {
        broker.publish(ref);
      }
    }
  }
}
