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
        "dojo/_base/lang",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/App.html",
        "dijit/layout/ContentPane", 
        "dijit/layout/LayoutContainer",
        "hrv/ui/home/HomePane",
        "hrv/ui/connectors/ConnectorsPane",
        "hrv/ui/brokers/BrokersPane",
        "hrv/ui/tasks/TasksPane",
        "hrv/ui/processes/ProcessesPane"
      ],
  function(declare,lang,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
    
      postCreate: function(){
        
      }
    });
});
