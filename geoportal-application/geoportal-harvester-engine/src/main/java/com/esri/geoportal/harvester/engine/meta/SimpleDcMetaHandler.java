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
package com.esri.geoportal.harvester.engine.meta;

import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.MetaHandler;
import com.esri.geoportal.commons.meta.ObjectAttribute;
import org.w3c.dom.Document;

/**
 * Simple DC meta handler.
 */
public class SimpleDcMetaHandler implements MetaHandler {

  @Override
  public Document create(ObjectAttribute wellKnowsAttributes) throws MetaException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public ObjectAttribute extract(Document doc) throws MetaException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
