/*
 * Copyright 2018 Esri, Inc.
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
package com.esri.geoportal.harvester.jdbc;

import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.util.StringUtil;

/**
 * JDBC validator utility class.
 */
/*package*/ class JdbcValidator {
  private static final String [] SQL_MODYFYING_COMMANDS = {"ALTER", "CREATE", "DELETE", "DROP", "INSERT", "TRUNCATE", "UPDATE"};
  private static final Set<String> EXEMPT_WORDS = Arrays.stream(SQL_MODYFYING_COMMANDS).collect(Collectors.toSet());
  private static final String regExp = String.format(
          "^((?!(%s)).)*$", // regular expression maching strings which don't contan %s; %s will be substituted with pipe (|) separated word(s)
          EXEMPT_WORDS.stream()
                  // map each word into case-insensitive expression, for example: ALTER -> [aA][lL][tT][eE][rR]
                  .map(w->w.chars()
                          // map each character into lower-case upper-case expression, for example: A -> [aA]
                          .mapToObj(ch->String.format("[%s%s]", StringUtil.toLowerCase((char)ch), StringUtil.toUpperCase((char)ch)))
                          // join all characters together into a single string
                          .collect(Collectors.joining()))
                  // join all words together by pipe (!)
                  .collect(Collectors.joining("|"))
  );

  public static void validateStatement(String sqlStatement) throws InvalidDefinitionException {
    String[] words = sqlStatement.split("\\p{Space}");
    if (words.length > 1) {
      if (Arrays.stream(words).anyMatch(w->EXEMPT_WORDS.contains(w.toUpperCase()))) {
        throw new InvalidDefinitionException("Invalid SQL statement definition.");
      }
    }
  }
  
  public static String getRegExp() {
    return regExp;
  }
}
