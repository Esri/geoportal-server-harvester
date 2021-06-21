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

define([
  "dojo/_base/declare",
  "dojo/i18n!../nls/resources",
  "dojo/_base/lang",
  "dojo/_base/array",
], function (declare, i18n, lang, array) {
  return {
    makeLabel: function (taskDefinition, long) {
      if (taskDefinition) {
        if (taskDefinition.name && taskDefinition.name.length > 0) {
          return taskDefinition.name;
        }
        var sourceLabel = this._makeEntityLabel(taskDefinition.source, long);
        var destLabel = "";
        if (taskDefinition.destinations) {
          array.forEach(
            taskDefinition.destinations,
            lang.hitch(this, function (linkDefinition) {
              var label = this._makeLinkLabel(linkDefinition, long);
              if (label) {
                if (!destLabel || destLabel.length == 0) {
                  destLabel = label;
                } else {
                  destLabel += ", " + label;
                }
              }
            })
          );
          if (taskDefinition.destinations.length > 1) {
            destLabel = "[" + destLabel + "]";
          }
        }
        return sourceLabel + " -> " + destLabel;
      } else {
        return "";
      }
    },

    _makeLinkLabel: function (linkDefinition, long) {
      if (linkDefinition.drains && linkDefinition.drains.length > 0) {
        var destLabel = null;
        array.forEach(
          linkDefinition.drains,
          lang.hitch(this, function (linkDefinition) {
            var label = this._makeLinkLabel(linkDefinition, long);
            if (label) {
              if (!destLabel || destLabel.length == 0) {
                destLabel = label;
              } else {
                destLabel += ", " + label;
              }
            }
          })
        );
        return destLabel;
      } else if (linkDefinition.action) {
        return this._makeEntityLabel(linkDefinition.action, long);
      } else {
        return null;
      }
    },

    _makeEntityLabel: function (entity, long) {
      if (!entity) return "undefined";
      if (!long && entity.label) return entity.label;
      return (
        (entity.type ? entity.type : "???") + " " + this._makePropertiesLabel(entity.properties)
      );
    },

    _hiddenProperties: ["cred-username", "cred-password", "folder-cleanup"],

    _makePropertiesLabel: function (properties) {
      var label = "";
      if (properties) {
        for (prop in properties) {
          if (this._hiddenProperties.indexOf(prop) < 0) {
            if (label.length > 0) {
              label += "," + properties[prop];
            } else {
              label = properties[prop];
            }
          }
        }
      }
      return "[" + label + "]";
    },
  };
});
