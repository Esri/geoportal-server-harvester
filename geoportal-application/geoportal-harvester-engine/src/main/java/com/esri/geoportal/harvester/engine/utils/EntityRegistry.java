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
package com.esri.geoportal.harvester.engine.utils;

import java.util.Arrays;
import java.util.List;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.general.Entity;
import java.util.HashMap;
import java.util.Locale;

/**
 * Entity registry.
 * @param <E> type of the factory
 */
public abstract class EntityRegistry<E extends Entity> extends HashMap<String,E> {
  /**
   * Registers entity.
   * @param entity entity
   */
  public void register(E entity) {
    put(entity.getType(), entity);
  }
  
  /**
   * Gets all templates.
   * @param locale locale
   * @return list of all templates
   */
  public List<UITemplate> getTemplates(Locale locale) {
    return Arrays.asList(values().stream().map(f->f.getTemplate(locale)).toArray(UITemplate[]::new));
  }
}
