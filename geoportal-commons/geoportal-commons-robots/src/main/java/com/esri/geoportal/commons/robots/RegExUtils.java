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

import java.util.regex.Pattern;

/**
 * Regular expression utils.
 */
/*package*/ class RegExUtils {
  /**
   * Compiles wildcard pattern into a regular expression.
   * <p>
   * Allowed wildcards:<br>
   * <br>
   * &nbsp;&nbsp;&nbsp;* - matches any sequence of characters<br>
   * &nbsp;&nbsp;&nbsp;$ - matches end of sequence<br>
   * @param patternWithWildcards pattern with wildcards
   * @return compiled pattern
   */
  public static Pattern compileWildcardPattern(String patternWithWildcards) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<patternWithWildcards.length(); i++) {
      char c = patternWithWildcards.charAt(i);
      switch (c) {
        case '*':
          sb.append(".*");
          break;
        case '$':
          if (i==patternWithWildcards.length()-1) {
            sb.append(c);
          } else {
            sb.append("[").append(c).append("]");
          }
          break;
        case '[':
        case ']':
          sb.append("[").append("\\").append(c).append("]");
          break;
        default:
          sb.append("[").append(c).append("]");
      }
    }
    return Pattern.compile(sb.toString());
  }
}
