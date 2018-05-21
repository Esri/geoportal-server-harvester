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
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/TaskRenamePane.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/query",
        "dojo/dom-construct",
        "dojo/dom-attr",
        "dojo/topic",
        "dijit/form/CheckBox",
        "dijit/form/RadioButton",
        "dijit/form/Form",
        "hrv/rest/Brokers"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,query,domConstruct,domAttr,topic,
           CheckBox,RadioButton,Form,
           BrokersREST){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      radios: [],
      checks: [],
      
      constructor: function(args) {
        this.data = args;
      },
    
      postCreate: function(){
        domAttr.set(this.taskName, "value", this.data.taskDefinition.name);
      },
      
      _onSubmit: function() {
        var taskDefinition = lang.mixin(null, this.data.taskDefinition);
        taskDefinition.name = domAttr.get(this.taskName, "value");
        this.emit("rename-submit", {taskDefinition: taskDefinition});
      },
      
      _onCancel: function() {
        this.emit("rename-close");
      }
    });
});
