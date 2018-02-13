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
        "dojo/text!./templates/Task.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/string",
        "dojo/dom-attr",
        "dojo/topic",
        "dojo/on",
        "dojo/json",
        "dojo/promise/all",
        "dijit/registry",
        "dijit/Dialog",
        "dijit/ConfirmDialog",
        "hrv/rest/Tasks",
        "hrv/rest/Triggers",
        "hrv/ui/tasks/SchedulerEditorPane",
        "hrv/utils/TaskUtils"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,string,domAttr,topic,on,json,all,
           registry, Dialog,ConfirmDialog,
           TasksREST, TriggersREST,
           SchedulerEditorPane, TaskUtils
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      
      constructor: function(args) {
        this.data = args;
        this.label = TaskUtils.makeLabel(this.data.taskDefinition);
      },
    
      postCreate: function(){
        domAttr.set(this.exportNode,"href",TasksREST.export(this.data.uuid));
      },
      
      _onRemove: function() {
        var dlg = new ConfirmDialog({
          title: this.i18n.tasks.removeDialog.title,
          content: string.substitute(this.i18n.tasks.removeDialog.content,{title: TaskUtils.makeLabel(this.data.taskDefinition)}),
          "class": "h-tasks-remove-dialog",
          onExecute: lang.hitch(this,function(){
            this.emit("remove",{data: this.data});
          })
        });
        dlg.show();
      },
      
      _onRun: function() {
        var dlg = new ConfirmDialog({
          title: this.i18n.tasks.runDialog.title,
          content: string.substitute(this.i18n.tasks.runDialog.content,{title: TaskUtils.makeLabel(this.data.taskDefinition)})+
                  "<div class='h-tasks-run-dialog-options'>"+
                  "<button data-dojo-type='dijit/form/CheckBox' id='ignoreRobots'></button>"+
                  "<label for='ignoreRobots'>" +this.i18n.tasks.runDialog.ignoreRobots+ "</label><br>"+
                  "<button data-dojo-type='dijit/form/CheckBox' id='incremental'></button>"+
                  "<label for='incremental'>" +this.i18n.tasks.runDialog.incremental+ "</label>"+
                  "</div>",
          "class": "h-tasks-run-dialog",
          parseOnLoad: true,
          onExecute: lang.hitch(this,function(){
            var ignoreRobots = registry.byId("ignoreRobots");
            this.data.taskDefinition.ignoreRobotsTxt = ignoreRobots.checked;
            var incremental = registry.byId("incremental");
            this.data.taskDefinition.incremental = incremental.checked;
            this.emit("run",{data: this.data});
            ignoreRobots.destroyRecursive();
            incremental.destroyRecursive();
          }),
          onCancel: lang.hitch(this,function() {
            var ignoreRobots = registry.byId("ignoreRobots");
            var incremental = registry.byId("incremental");
            ignoreRobots.destroyRecursive();
            incremental.destroyRecursive();
          })
        });
        dlg.show();
      },
      
      _onHistory: function() {
        this.emit("history",{data: this.data});
      },
      
      _onSchedule: function(evt) {
        TasksREST.triggers(this.data.uuid).then(
          lang.hitch(this,function(triggers){
            var close = function() {
              schedulerEditorDialog.destroy();
              schedulerEditorPane.destroy();
            };
            
            var data = {};
            if (triggers.length>0) {
              data.type = triggers[0].triggerDefinition.type;
              lang.mixin(data,triggers[0].triggerDefinition.properties);
            }
            var schedulerEditorPane = new SchedulerEditorPane(data);

            // create editor dialog box
            var schedulerEditorDialog = new Dialog({
              title: this.i18n.tasks.editor.caption,
              content: schedulerEditorPane,
              onHide: close
            });

            on(schedulerEditorPane,"submit",lang.hitch(this, function(evt){
              TasksREST.triggers(this.data.uuid).then(
                lang.hitch(this,function(triggers){
                  console.log("Triggers", triggers);

                  var deferred = [];
                  array.forEach(triggers,lang.hitch(this,function(trigger){
                    deferred.push(TriggersREST.delete(trigger.uuid));
                  }));

                  all(deferred).then(lang.hitch(this,function(response){
                    if (evt.triggerDefinition.type!=="NULL") {
                      TasksREST.schedule(this.data.uuid,json.stringify(evt.triggerDefinition)).then(
                        lang.hitch(this,function(response){
                          close();
                        }),
                        lang.hitch(this,function(error){
                          console.error(error);
                          topic.publish("msg",new Error("Unable to schedule task"));
                          close();
                        })
                      );
                    } else {
                      close();
                    }
                  }),lang.hitch(this,function(error){
                    console.error(error);
                    topic.publish("msg",new Error("Unable to delete current task triggers"));
                    close();
                  }));
                }),
                lang.hitch(this,function(error){
                  console.error(error);
                  topic.publish("msg",new Error("Unable to access tasks information"));
                  close();
                })
              );
            }));

            schedulerEditorDialog.show();
          }),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg",new Error("Unable to read scheduling"));
          })
        );
      }
    });
});
