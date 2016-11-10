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
package com.esri.geoportal.harvester.beans;

import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.js.SimpleDcMetaBuilder;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

/**
 * Default metadata handler.
 */
@Service
public class DefaultMetadataBuilderBean implements MetaBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultMetadataBuilderBean.class);
  private MetaBuilder xmlMetaBuilder;
  
  /**
   * Initializes bean.
   */
  @PostConstruct
  public void init() {
    xmlMetaBuilder = new SimpleDcMetaBuilder();
    LOG.info("DefaultMetadataBuilderBean initialized.");
  }

  @Override
  public Document create(MapAttribute wellKnowsAttributes) throws MetaException {
    return xmlMetaBuilder.create(wellKnowsAttributes);
  }
  
  /**
   * Destroys bean.
   */
  @PreDestroy
  public void destroy() {
    LOG.info(String.format("DefaultMetadataBuilderBean destroyed."));
  }
  
}
