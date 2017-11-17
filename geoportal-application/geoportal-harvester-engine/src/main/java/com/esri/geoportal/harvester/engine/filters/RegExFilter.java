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
package com.esri.geoportal.harvester.engine.filters;

import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Filter;
import com.esri.geoportal.harvester.api.FilterInstance;
import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Regular expression filer.
 * <p>
 * Filters which passes data with source URI matching given regular expression.
 */
public class RegExFilter implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(RegExFilter.class);
  public static final String F_REGEX_PATTERN = "f-regex-pattern";
  public static final String TYPE = "REGEX";

  @Override
  public FilterInstance createInstance(EntityDefinition filterDefinition) throws InvalidDefinitionException {
    return new RegExpFilterInstance(filterDefinition);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("EngineResource", locale);
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.StringArgument(F_REGEX_PATTERN, bundle.getString("engine.filters.regexfiler.pattern"), true));
    UITemplate uiTemplate = new UITemplate(getType(), bundle.getString("engine.filters.regexfiler"), arguments);
    return uiTemplate;
  }
  
  /**
   * Filter instance.
   */
  private class RegExpFilterInstance implements FilterInstance {
    private final EntityDefinition definition;
    private final Pattern pattern;

    /**
     * Creates instance of the filter instance.
     * @param definition filter definition
     */
    public RegExpFilterInstance(EntityDefinition definition) throws InvalidDefinitionException {
      this.definition = definition;
      
      String strPattern = definition.getProperties().get(F_REGEX_PATTERN);
      try {
        pattern = Pattern.compile(strPattern);
      } catch (PatternSyntaxException ex) {
        throw new InvalidDefinitionException(formatForLog("Invalid pattern: %s", strPattern), ex);
      }
    }

    @Override
    public void initialize(InitContext context) throws DataProcessorException {
      // empty initialization
    }

    @Override
    public void terminate() {
      // empty termination
    }

    @Override
    public EntityDefinition getFilterDefinition() {
      return definition;
    }

    @Override
    public boolean test(DataReference dataReference) {
      String sourceUri = dataReference.getSourceUri().toASCIIString();
      Matcher matcher = pattern.matcher(sourceUri);
      boolean result = matcher.matches();
      if (!result) {
        LOG.debug(formatForLog("Source URI: %s does not match pattern: %s", sourceUri, pattern.pattern()));
      }
      return result;
    }
  }
}
