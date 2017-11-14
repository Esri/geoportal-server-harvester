/*
 * Copyright 2017 Esri, Inc.
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

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

/**
 * CRLF utilities.
 */
public class CrlfUtils {
  public static final String STD_REPLACEMENT = ", ";
  
  /**
   * Sanitizes new line type characters in the input.
   * @param str input string.
   * @param subst replacement
   * @return sanitized string
   */
  public static String crlfSanitize(String str, String subst) {
    if (str == null) {
      return str;
    }
    
    return  escapeHtml4(str.replaceAll("(\n|\r|\f)+(\\s+(\n|\r|\f)+)?", subst));
  }
  
  
  /**
   * Sanitizes new line type characters in the input withe standard replacement.
   * @param str input string.
   * @return sanitized string
   */
  public static String crlfSanitize(String str) {
    return crlfSanitize(str, STD_REPLACEMENT);
  }
}
