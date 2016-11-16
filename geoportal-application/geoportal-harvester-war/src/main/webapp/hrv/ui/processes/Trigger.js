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
        "dojo/text!./templates/Trigger.html",
        "dojo/_base/lang",
        "dojo/dom-class",
        "dojo/dom-style",
        "dojo/html",
        "dojo/topic",
        "hrv/rest/Triggers",
        "hrv/utils/TaskUtils"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,domClass,domStyle,html,topic,
           TriggersREST, TaskUtils
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      data: null,
      
      constructor: function(arg) {
        this.data = arg;
      },
    
      postCreate: function(){
        html.set(this.titleNode,this._makeLabel(this.data.triggerDefinition));
      },
      
      _makeLabel: function(triggerDefinition) {
        return TaskUtils.makeLabel(triggerDefinition.taskDefinition);
      },
      
      _onCancel: function(evt) {
        TriggersREST.delete(this.data.uuid).then(
            lang.hitch(this,this._onCanceled),
            lang.hitch(this,function(error){
              topic.publish("msg",this.i18n.triggers.errors.canceling);
            })
        );
      },
      
      _onCanceled: function(evt) {
        this.emit("reload");
      }
    });
});
