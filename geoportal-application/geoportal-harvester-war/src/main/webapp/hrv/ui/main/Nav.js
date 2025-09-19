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
  "dijit/_WidgetBase",
  "dijit/_TemplatedMixin",
  "dijit/_WidgetsInTemplateMixin",
  "dojo/i18n!../../nls/resources",
  "dojo/text!./templates/Nav.html",
  "dojo/_base/lang",
  "dojo/topic",
  "dojo/router",
  "dojo/dom-class"
], function (
  declare,
  _WidgetBase,
  _TemplatedMixin,
  _WidgetsInTemplateMixin,
  i18n,
  template,
  lang,
  topic, router,
  domClass
) {
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
    i18n: i18n,
    templateString: template,

    postCreate: function () {
      domClass.add(this.homeNode, "active-tab");
      this.own(topic.subscribe("nav",lang.hitch(this, function(params){
        switch(params.type) {
          case "processes":
            domClass.add(this.homeNode, "active-tab");
            domClass.remove(this.tasksNode, "active-tab");
            domClass.remove(this.brokersNode, "active-tab");
            domClass.remove(this.apiNode, "active-tab");
            break;
            
          case "brokers":
            domClass.add(this.brokersNode, "active-tab");
            domClass.remove(this.homeNode, "active-tab");
            domClass.remove(this.tasksNode, "active-tab");
            domClass.remove(this.apiNode, "active-tab");
            break;
          case "api":
            domClass.add(this.apiNode, "active-tab");
            domClass.remove(this.homeNode, "active-tab");
            domClass.remove(this.tasksNode, "active-tab");
            domClass.remove(this.brokersNode, "active-tab");
            break;
            
          default:
            domClass.add(this.tasksNode, "active-tab");
            domClass.remove(this.brokersNode, "active-tab");
            domClass.remove(this.homeNode, "active-tab");
            domClass.remove(this.apiNode, "active-tab");
            break;
        }
      })));
    },

    _onhome: function () {
      router.go("/home");
    },

    _onbrokers: function () {
      router.go("/brokers");
    },

    _ontasks: function () {
      router.go("/tasks");
    },
    _onapi: function () {
       // window.open("swagger-ui/index.html", '_blank');
      router.go("/api");
    }
    
  });
});
