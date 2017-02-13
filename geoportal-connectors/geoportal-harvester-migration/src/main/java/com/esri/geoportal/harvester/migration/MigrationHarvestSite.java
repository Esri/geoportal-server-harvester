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
package com.esri.geoportal.harvester.migration;

/**
 * Migration harvest site.
 */
/*package*/ class MigrationHarvestSite {

  public String docuuid;
  public String title;
  public String type;
  public String host;
  public String protocol;
  public Frequency frequency;

  static enum Frequency {
    Monthy, BiWeekly, Weekly, Dayly, Hourly, Once, Skip, AdHoc;

    public static Frequency parse(String freq) {
      for (Frequency f : values()) {
        if (f.name().equalsIgnoreCase(freq)) {
          return f;
        }
      }
      return null;
    }
  }
}
