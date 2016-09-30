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
package com.esri.geoportal.harvester.api.base;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.zip.CRC32;
import org.apache.commons.lang3.StringUtils;

/**
 * Simple scrambler.
 */
public class SimpleScrambler {
  
  /**
   * Encodes string.
   * @param txt string to encode
   * @return encoded string or <code>null</code> if error encoding string
   */
  public static String encode(String txt) {
    txt = StringUtils.defaultIfEmpty(txt, "");
    try {
      CRC32 crC32 = new CRC32();
      crC32.update(txt.getBytes("UTF-8"));
      long crc = crC32.getValue();
      String crctxt = String.format("%10d%s", crc, txt);
      Base64.Encoder encoder = Base64.getEncoder();
      return encoder.encodeToString(crctxt.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException ex) {
      return null;
    }
  }
  
  /**
   * Decodes string.
   * @param encoded encoded string to decode
   * @return decoded string or <code>null</code> if error decoding string
   */
  public static String decode(String encoded) {
    try {
      encoded = StringUtils.defaultIfEmpty(encoded, "");
      Base64.Decoder decoder = Base64.getDecoder();
      String crctxt = new String(decoder.decode(encoded),"UTF-8");
      if (crctxt.length()<10) {
        return null;
      }
      long crc = Long.parseLong(StringUtils.trimToEmpty(crctxt.substring(0,10)));
      String txt = crctxt.substring(10);
      CRC32 crC32 = new CRC32();
      crC32.update(txt.getBytes("UTF-8"));
      if (crc!=crC32.getValue()) {
        return null;
      }
      return txt;
    } catch (NumberFormatException|UnsupportedEncodingException ex) {
      return null;
    }
  }
}
