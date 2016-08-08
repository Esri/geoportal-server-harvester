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
        "dojo/topic",
        "dojo/on",
        "dojo/json",
        "dojo/promise/all",
        "dijit/Dialog",
        "hrv/rest/Tasks",
        "hrv/rest/Triggers",
        "hrv/ui/tasks/SchedulerEditorPane",
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,topic,on,json,all,
           Dialog,
           TasksREST, TriggersREST,
           SchedulerEditorPane
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      
      constructor: function(args) {
        this.data = args;
        this.label = this.makeLabel(this.data.taskDefinition);
      },
    
      postCreate: function(){
      },
      
      _onRemove: function() {
        this.emit("remove",{data: this.data});
      },
      
      _onRun: function() {
        this.emit("run",{data: this.data});
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
      },
      
      makeLabel: function(taskDefinition) {
        var sourceLabel = taskDefinition.source? taskDefinition.source.label: "";
        var destLabel = "";
        if (taskDefinition.destinations) {
          array.forEach(taskDefinition.destinations,lang.hitch(this,function(linkDefinition){
            var label =this.makeLinkLabel(linkDefinition);
            if (label) {
              if (!destLabel || destLabel.length==0) {
                destLabel = label;
              } else {
                destLabel += ", "+label;
              }
            }
          }));
        }
        return sourceLabel + " -> [" + destLabel + "]";
      },
      
      makeLinkLabel: function(linkDefinition) {
        if (linkDefinition.drains && linkDefinition.drains.length>0) {
          var destLabel = null;
          array.forEach(linkDefinition.drains,lang.hitch(this,function(linkDefinition){
            var label = this.makeLinkLabel(linkDefinition);
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
    });
});
