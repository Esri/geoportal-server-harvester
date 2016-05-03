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
package com.esri.geoportal.harvester.console;

import com.esri.geoportal.harvester.api.BrokerDefinition;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.support.DataReferenceSerializer;

/**
 * Application.
 */
public class Application {
  public static void main(String[] args) throws Exception {
    ConsoleConnector connector = new ConsoleConnector();
    BrokerDefinition def = new BrokerDefinition();
    ConsoleBrokerDefinitionAdaptor definition = new ConsoleBrokerDefinitionAdaptor(def);
    ConsoleBroker broker = connector.createBroker(def);
    DataReferenceSerializer ser = new DataReferenceSerializer();
    DataReference<String> ref = null;
    while (( ref = ser.deserialize(System.in))!=null) {
      broker.publish(ref);
    }
  }
}
