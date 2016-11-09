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
package com.esri.geoportal.commons.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Text scrambler.
 */
public class TextScrambler {

  
  /**
   * Scrambles text.
   * @param text text to scramble
   * @return scrambled text
   */
  public static String encode(String text) {
    try {
      text = URLEncoder.encode(text, "UTF-8");
      Base64.Encoder encoder = Base64.getEncoder();
      byte[] encoded = encoder.encode(text.getBytes("UTF-8"));
      
      return hash(text) + new String(encoded, "UTF-8");
    } catch (UnsupportedEncodingException|NoSuchAlgorithmException ex) {
      return text;
    }
  }
  
  /**
   * Unscrambles text.
   * @param str string to decode
   * @return decoded string
   */
  public static String decode(String str) {
    if (str.length()<16) {
      return str;
    }
    
    try {
      String sCrc = str.substring(0,24);
      String encText = str.substring(24);
      
      Base64.Decoder decoder = Base64.getDecoder();
      byte[] decoded = decoder.decode(encText.getBytes("UTF-8"));
      
      String text = URLDecoder.decode(new String(decoded,"UTF-8"), "UTF-8");
      if (!hash(text).equals(sCrc)) {
        return str;
      }
      
      return text;
    } catch (NumberFormatException|UnsupportedEncodingException|NoSuchAlgorithmException ex) {
      return str;
    }
  }
  
  private static String hash(String string) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(string.getBytes("UTF-8"));
    byte[] digest = md.digest();
    Base64.Encoder encoder = Base64.getEncoder();
    return encoder.encodeToString(digest);
  }
}
