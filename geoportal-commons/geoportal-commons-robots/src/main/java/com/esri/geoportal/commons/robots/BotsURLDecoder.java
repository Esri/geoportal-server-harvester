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
package com.esri.geoportal.commons.robots;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * URL decoder for robots.
 */
/*package*/ class BotsURLDecoder {
  /**
   * Decodes URL octets except %2f (i.e. / character)
   * @param str string to encode
   * @return encoded string
   */
  public static String decode(String str) throws UnsupportedEncodingException {
    if (str!=null) {
      StringBuilder sb = new StringBuilder();
      for (int idx = str.toLowerCase().indexOf("%2f"); idx>=0; idx = str.toLowerCase().indexOf("%2f")) {
        sb.append(URLDecoder.decode(str.substring(0, idx),"UTF-8")).append(str.substring(idx, idx+3));
        str = str.substring(idx+3);
      }
      sb.append(URLDecoder.decode(str,"UTF-8"));
      str = sb.toString();
    }
    return str;
  }
}
