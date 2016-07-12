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
        "dojo/_base/array",
        "dojo/topic",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/Event.html"
      ],
  function(declare,lang,array,topic,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      startTime: null,
      endTime: null,
      
      constructor: function(args) {
        this.data = args;
        this.startTime = this.data.startTimestamp? new Date(this.data.startTimestamp).toString(): "?";
        this.endTime = this.data.endTimestamp? new Date(this.data.endTimestamp).toString(): "?";
      },
    
      postCreate: function(){
      }
    });
});
