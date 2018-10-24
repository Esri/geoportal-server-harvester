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
package com.esri.geoportal.harvester.sink;

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import static com.esri.geoportal.harvester.sink.SinkConstants.P_DROP_FOLDER;

/**
 * Sink connector.
 */
public class SinkConnector implements InputConnector<InputBroker> {
  public static final String TYPE = "SINK";
  private final SinkContext ctx;

  /**
   * Creates instance of the connector.
   * @param attemptCount number of IO attempts
   * @param attemptDelay delay between IO attempts
   */
  public SinkConnector(int attemptCount, int attemptDelay) {
    this.ctx = new SinkContext(attemptCount, attemptDelay);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * Gets sink context.
   * @return sink context
   */
  public SinkContext getCtx() {
    return ctx;
  }
  
  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("SinkResource", locale);
    List<UITemplate.Argument> args = new ArrayList<>();
    args.add(new UITemplate.StringArgument(P_DROP_FOLDER, bundle.getString("sink.dropFolder"), true){
      @Override
      public String getHint() {
        return bundle.getString("sink.hint");
      }
    });
    return new UITemplate(getType(), bundle.getString("sink"), args);
  }

  @Override
  public void validateDefinition(EntityDefinition definition) throws InvalidDefinitionException {
    new SinkBrokerDefinitionAdaptor(definition);
  }

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new SinkBroker(this, new SinkBrokerDefinitionAdaptor(definition));
  }
  
}
