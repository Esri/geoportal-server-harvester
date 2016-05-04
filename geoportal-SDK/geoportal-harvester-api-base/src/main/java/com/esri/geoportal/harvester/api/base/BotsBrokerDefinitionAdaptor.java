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
package com.esri.geoportal.harvester.api.base;

import com.esri.geoportal.commons.robots.BotsConfig;
import com.esri.geoportal.commons.robots.BotsConfigImpl;
import com.esri.geoportal.commons.robots.BotsMode;
import com.esri.geoportal.harvester.api.BrokerDefinition;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Bots properties adaptor.
 */
public abstract class BotsBrokerDefinitionAdaptor extends BrokerDefinitionAdaptor {
  protected static final String P_BOTS_AGENT = "bots.config.agent";
  protected static final String P_BOTS_ENABLED = "bots.config.enabled";
  protected static final String P_BOTS_OVERRIDE = "bots.config.override";
  protected static final String P_BOTS_MODE = "bots.mode";


  /**
   * Creates instance of the adaptor.
   * @param def broker definition
   */
  public BotsBrokerDefinitionAdaptor(BrokerDefinition def) {
    super(def);
  }
  
  /**
   * Gets bots mode.
   *
   * @return bots mode
   */
  public BotsMode getBotsMode() {
    String sBootMode = get(P_BOTS_MODE);
    return BotsMode.parseMode(sBootMode);
  }

  /**
   * Sets bots mode.
   *
   * @param botsMode bots mode
   */
  public void setBotsMode(BotsMode botsMode) {
    set(P_BOTS_MODE, botsMode.name());
  }

  /**
   * Gets bots config.
   *
   * @return bots config
   */
  public BotsConfig getBotsConfig() {
    return new BotsConfigImpl(
            StringUtils.defaultIfBlank(get(P_BOTS_AGENT), BotsConfig.DEFAULT.getUserAgent()), 
            BooleanUtils.toBoolean(StringUtils.defaultIfBlank(get(P_BOTS_ENABLED), Boolean.toString(BotsConfig.DEFAULT.isEnabled()))), 
            BooleanUtils.toBoolean(StringUtils.defaultIfBlank(get(P_BOTS_OVERRIDE), Boolean.toString(BotsConfig.DEFAULT.isOverride())))
    );
  }

  /**
   * Sets bots config.
   *
   * @param botsConfig bots config
   */
  public void setBotsConfig(BotsConfig botsConfig) {
    set(P_BOTS_AGENT, botsConfig.getUserAgent());
    set(P_BOTS_ENABLED, Boolean.toString(botsConfig.isEnabled()));
    set(P_BOTS_OVERRIDE, Boolean.toString(botsConfig.isOverride()));
  }
}
