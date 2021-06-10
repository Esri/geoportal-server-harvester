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
        "dojo/string",
        "dojo/html",
        "dojo/topic",
        "dijit/ConfirmDialog",
        "hrv/rest/Triggers",
        "hrv/utils/TaskUtils"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,string,html,topic,
           ConfirmDialog,
           TriggersREST, TaskUtils
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      data: null,
      scheduled: "",
      
      constructor: function(arg) {
        this.data = arg;
        
        if (arg && arg.triggerDefinition && arg.triggerDefinition.properties) {
          var props = arg.triggerDefinition.properties;
          if (props["t-period"]) {
            var periodLength = null;
            
            switch (props["t-period"]) {
              case "PT1H": 
                periodLength = this.i18n.periodical.hourly; 
                break;
              case "P1D": 
                periodLength = this.i18n.periodical.daily; 
                break;
              case "P1W": 
                periodLength = this.i18n.periodical.weekly; 
                break;
              case "P2W": 
                periodLength = this.i18n.periodical.biweekly; 
                break;
              case "P1M": 
                periodLength = this.i18n.periodical.monthly; 
                break;
            }
            
            if (periodLength) {
              this.scheduled = string.substitute(this.i18n.triggers.runsEvery, {period: periodLength.toLowerCase()});
            }
            
          } else if (props["t-at-time"]) {
            var atTime = props["t-at-time"];
            this.scheduled = string.substitute(this.i18n.triggers.runsAt, {time: atTime});
          }
        }
      },
    
      postCreate: function(){
        html.set(this.titleNode,TaskUtils.makeLabel(this.data.triggerDefinition.taskDefinition));
      },
      
      _onCancel: function(evt) {
        var dlg = new ConfirmDialog({
          title: this.i18n.triggers.removeDialog.title,
          content: string.substitute(this.i18n.triggers.removeDialog.content,{title: TaskUtils.makeLabel(this.data.triggerDefinition.taskDefinition)}),
          "class": "h-processes-trigger-remove-dialog",
          onExecute: lang.hitch(this,function(){
            TriggersREST.delete(this.data.uuid).then(
                lang.hitch(this,this._onCanceled),
                lang.hitch(this,function(error){
                  topic.publish("msg", new Error(this.i18n.triggers.errors.canceling));
                })
            );
          })
        });
        dlg.show();
      },
      
      _onCanceled: function(evt) {
        this.emit("reload");
      }
    });
});
