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
package com.esri.geoportal.commons.meta.util;

import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaAnalyzer;
import com.esri.geoportal.commons.meta.MetaException;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Document;

/**
 * Multi meta analyzer wrapper;
 */
public class MultiMetaAnalyzerWrapper implements MetaAnalyzer {
  private final List<MetaAnalyzer> analyzers;

  /**
   * Creates instance of the wrapper.
   * @param analyzers list of analyzers
   */
  public MultiMetaAnalyzerWrapper(List<MetaAnalyzer> analyzers) {
    this.analyzers = analyzers;
  }

  /**
   * Creates instance of the wrapper.
   * @param analyzers list of analyzers
   */
  public MultiMetaAnalyzerWrapper(MetaAnalyzer...analyzers) {
    this.analyzers = Arrays.asList(analyzers);
  }

  @Override
  public MapAttribute extract(Document doc) throws MetaException {
    if (analyzers!=null) {
      for (MetaAnalyzer a: analyzers) {
        MapAttribute extract = a.extract(doc);
        if (extract!=null) {
          return extract;
        }
      }
    }
    return null;
  }
}
