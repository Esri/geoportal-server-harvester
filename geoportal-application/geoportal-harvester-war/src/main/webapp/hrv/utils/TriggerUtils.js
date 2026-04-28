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

        const dayMap = new Map([
          ["1", i18n.dayOfWeek.day1],
          ["2", i18n.dayOfWeek.day2],
          ["3", i18n.dayOfWeek.day3],
          ["4", i18n.dayOfWeek.day4],
          ["5", i18n.dayOfWeek.day5],
          ["6", i18n.dayOfWeek.day6],
          ["7", i18n.dayOfWeek.day7],
          ["1,2,3,4,5,6,7", i18n.dayOfWeek.dayAll]
          
        ]);
        var atTime = props["t-at-time"];
        var atDay = props["t-at-day"];
        
        return string.substitute(i18n.triggers.runsAt, {time: dayMap.get(atDay)+":"+atTime});
      }
      
      return null;
    }
    
  };
});
