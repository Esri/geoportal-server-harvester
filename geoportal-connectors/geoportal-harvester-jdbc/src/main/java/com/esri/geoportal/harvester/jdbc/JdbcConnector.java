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

import com.esri.geoportal.harvester.api.defs.EntityDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker;
import com.esri.geoportal.harvester.api.specs.InputConnector;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * JDBC connector.
 */
public class JdbcConnector  implements InputConnector<InputBroker> {
  public static final String TYPE = "JDBC";

  @Override
  public InputBroker createBroker(EntityDefinition definition) throws InvalidDefinitionException {
    return new JdbcBroker(this, new JdbcBrokerDefinitionAdaptor(definition));
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("JdbcResource", locale);
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.StringArgument(JdbcConstants.P_JDBC_DRIVER_CLASS, bundle.getString("jdbc.driver"), true));
    arguments.add(new UITemplate.StringArgument(JdbcConstants.P_JDBC_CONNECTION, bundle.getString("jdbc.connection"), true));
    arguments.add(new UITemplate.StringArgument(JdbcConstants.P_JDBC_USERNAME, bundle.getString("jdbc.username")));
    arguments.add(new UITemplate.StringArgument(JdbcConstants.P_JDBC_PASSWORD, bundle.getString("jdbc.password")) {
      public boolean isPassword() {
        return true;
      }
    });
    arguments.add(new UITemplate.StringArgument(JdbcConstants.P_JDBC_SQL_STATEMENT, bundle.getString("jdbc.sql"), true));
    return new UITemplate(getType(), bundle.getString("jdbc"), arguments);
  }
}
