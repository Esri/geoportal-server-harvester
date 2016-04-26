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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Content analyzer.
 */
/*package*/ class ContentAnalyzer {
  private final URL root;

  public ContentAnalyzer(URL root) {
    this.root = root;
  }
  
  public List<URL> analyze(String content) throws URISyntaxException, MalformedURLException {
    ArrayList<URL> list = new ArrayList<>();

    Pattern hrefPattern = Pattern.compile("href\\p{Space}*=\\p{Space}*[\"'][^\"']*[\"']", Pattern.CASE_INSENSITIVE);
    Pattern quotPattern = Pattern.compile("[\"'][^\"']*[\"']", Pattern.CASE_INSENSITIVE);
    Matcher hrefMatcher = hrefPattern.matcher(content);

    int startIndex = 0;
    while (hrefMatcher.find(startIndex)) {
      String group = hrefMatcher.group();
      Matcher quotMatcher = quotPattern.matcher(group);
      if (quotMatcher.find()) {
        String extractedUrl = quotMatcher.group().replaceAll("^.|.$", "");
        try {
          URL url = root.toURI().resolve(extractedUrl).toURL();
          if (url.toExternalForm().startsWith(root.toExternalForm()) && url.toExternalForm().length() > root.toExternalForm().length()) {
            list.add(url);
          }
        } catch (NullPointerException|IllegalArgumentException ex) {}
      }
      startIndex = hrefMatcher.end();
    }

    return list;
  }
}
