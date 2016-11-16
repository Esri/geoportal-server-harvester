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



define(["dojo/_base/declare",
        "dojo/i18n!../nls/resources",
        "dojo/_base/lang",
        "dojo/_base/array"
      ],
  function(declare,i18n,
           lang,array
          ){
    return {
      makeLabel: function(taskDefinition) {
        var sourceLabel = taskDefinition.source? taskDefinition.source.label: "";
        var destLabel = "";
        if (taskDefinition.destinations) {
          array.forEach(taskDefinition.destinations,lang.hitch(this,function(linkDefinition){
            var label =this._makeLinkLabel(linkDefinition);
            if (label) {
              if (!destLabel || destLabel.length==0) {
                destLabel = label;
              } else {
                destLabel += ", "+label;
              }
            }
          }));
          if (taskDefinition.destinations.length>1) {
            destLabel = "[" + destLabel + "]";
          }
        }
        return sourceLabel + " -> " + destLabel;
      },
      
      _makeLinkLabel: function(linkDefinition) {
        if (linkDefinition.drains && linkDefinition.drains.length>0) {
          var destLabel = null;
          array.forEach(linkDefinition.drains,lang.hitch(this,function(linkDefinition){
            var label = this._makeLinkLabel(linkDefinition);
            if (label) {
              if (!destLabel || destLabel.length==0) {
                destLabel = label;
              } else {
                destLabel += ", "+label;
              }
            }
          }));
          return destLabel;
        } else if (linkDefinition.action) {
          return linkDefinition.action.label;
        } else {
          return null;
        }
      }
    };
  }
);

