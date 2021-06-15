/*
 * Copyright 2021 Esri, Inc.
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

define([
  "dojo/_base/declare",
  "dojo/i18n!../nls/resources",
  "dojo/_base/lang",
  "dojo/_base/array",
  "dojo/string"
  
], function (declare, i18n, lang, array, string) {
  return {
    makeSchedulingInfo: function(triggerDefinition) {
      var props = triggerDefinition.properties;
      
      if (props["t-period"]) {
        var periodLength = null;

        switch (props["t-period"]) {
          case "PT1H": 
            periodLength = i18n.periodical.hourly; 
            break;
          case "P1D": 
            periodLength = i18n.periodical.daily; 
            break;
          case "P1W": 
            periodLength = i18n.periodical.weekly; 
            break;
          case "P2W": 
            periodLength = i18n.periodical.biweekly; 
            break;
          case "P1M": 
            periodLength = i18n.periodical.monthly; 
            break;
        }

        if (periodLength) {
          return string.substitute(i18n.triggers.runsEvery, {period: periodLength.toLowerCase()});
        }

      } else if (props["t-at-time"]) {
        var atTime = props["t-at-time"];
        return string.substitute(i18n.triggers.runsAt, {time: atTime});
      }
      
      return null;
    }
    
  };
});
