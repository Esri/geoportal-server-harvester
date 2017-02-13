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
package com.esri.geoportal.commons.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * URI utils.
 */
public class UriUtils {
  
  public static String escapeUri(String input) {
    int endScheme = input.indexOf(":");
    String scheme = endScheme>0? input.substring(0,endScheme): null;
    String rest = endScheme>0? input.substring(endScheme+1): input;
    
    UUID uuid = checkUuid(rest);
    if (uuid!=null) {
      scheme = "uuid";
      rest = uuid.toString();
    }
    
    return (scheme!=null? escape(scheme,SCHEMECHARS,SKIP) + ":": "") + escape(rest,ALLOWEDCHARS,ESC);
  }
  
  private static UUID checkUuid(String input) {
    if (input.startsWith("{") && input.endsWith("}")) {
      String strip = input.substring(1, input.length());
      try {
        UUID uuid = UUID.fromString(strip);
        return uuid;
      } catch (Exception ex) {
      }
    }
    return null;
  }
  
  private static String escape(String input, String ALLOWEDCHARS, Replacer replacer) {
      StringBuilder sb = new StringBuilder();
      for (int i=0; i<input.length(); i++) {
        char ch = input.charAt(i);
        if (ALLOWEDCHARS.lastIndexOf(ch)>=0) {
          sb.append(ch);
        } else {
          sb.append(replacer.replace(ch));
        }
      }
      return sb.toString();
  }
  
  private static interface Replacer {
    String replace(char input);
  }
  
  private static final String SCHEMECHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-.";
  private static final String ALLOWEDCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.!~*'();/?:@&#=+$,";
  
  private static final Replacer SKIP = (ch)->"";
  private static final Replacer ESC = (ch)->{
    String str = new String(new char[]{ch});
    try {
      return URLEncoder.encode(str,"UTF-8");
    } catch (UnsupportedEncodingException ex) {
      return str;
    }
  };
}
