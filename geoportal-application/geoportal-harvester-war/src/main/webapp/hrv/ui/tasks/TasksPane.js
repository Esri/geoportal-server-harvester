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
        "dojo/text!./templates/TasksPane.html",
        "dojo/topic",
        "dojo/dom-style",
        "hrv/ui/tasks/Tasks"
      ],
  function(declare,lang,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template,topic,domStyle){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
    
      postCreate: function(){
        topic.subscribe("nav",lang.hitch(this,this._onNav));
      },
      
      _onNav: function(evt) {
        domStyle.set(this.domNode,"display", evt.type==="tasks"? "block": "none");
      }
    });
});
