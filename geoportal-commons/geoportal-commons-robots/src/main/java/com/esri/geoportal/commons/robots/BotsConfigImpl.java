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

/**
 * Bots config implementation.
 */
public class BotsConfigImpl implements BotsConfig{
  private final String userAgent;
  private final boolean enabled;
  private final boolean override;

  public BotsConfigImpl(String userAgent, boolean enabled, boolean override) {
    this.userAgent = userAgent;
    this.enabled = enabled;
    this.override = override;
  }

  @Override
  public String getUserAgent() {
    return userAgent;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean isOverride() {
    return override;
  }
  
  @Override
  public String toString() {
    return String.format("%s/%s/override:%b", userAgent, enabled? "enabled": "disabled", override);
  }
  
}
