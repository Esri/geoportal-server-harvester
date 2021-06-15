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
        "dojo/text!./templates/Process.html",
        "dojo/_base/lang",
        "dojo/dom-class",
        "dojo/dom-style",
        "dojo/html",
        "dojo/topic",
        "dojo/router",
        "hrv/rest/Processes",
        "hrv/utils/TaskUtils"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,domClass,domStyle,html,topic,router,
           ProcessesREST, TaskUtils
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      data: null,
      timerHandler: 0,
      
      constructor: function(arg) {
        this.data = arg;
      },
      
      destroy: function() {
        if (this.timerHandler > 0) {
          clearTimeout(this.timerHandler);
        }
      },
    
      postCreate: function(){
        html.set(this.titleNode,TaskUtils.makeLabel(this.data.taskDefinition));
        var update = lang.hitch(this,function(){
          ProcessesREST.get(this.data.uuid).then(
            lang.hitch(this,function(result){
              html.set(this.statusNode, result.status);
              domClass.remove(this.statusNode,"h-status-submitted");
              domClass.remove(this.statusNode,"h-status-working");
              domClass.remove(this.statusNode,"h-status-aborting");
              domClass.remove(this.statusNode,"h-status-completed");
              domClass.add(this.statusNode,"h-status-"+result.status);
              domStyle.set(this.cancelNode,"display",result.status==="working"? "inline": "none");
              
              if (result.status==="working" || result.status==="aborting") {
                if (result.statistics) {
                  var now = new Date();
                  if (this.data.taskDefinition.source.type !== "SINK") {
                    var start = new Date(result.statistics.startDate);
                    var duration = (now-start)>0? (now-start)/1000/60: 0;
                    var velocity = result.statistics.acquired>0 && duration>0? Math.round(result.statistics.acquired/duration): null;
                    var progress = ""+result.statistics.acquired + (velocity? " ("+velocity+"/"+this.i18n.processes.min+")": "");
                    html.set(this.progressNode, progress);
                  } else {
                    var progress = ""+result.statistics.acquired;
                    html.set(this.progressNode, progress);
                  }
                }
                
                this.timerHandler = setTimeout(update,2000);
              }
              
              if (result.status==="completed") {
                if (result.statistics) {
                  var stats = ""+result.statistics.succeeded + "/" + result.statistics.acquired;
                  html.set(this.progressNode, stats);
                }
                domStyle.set(this.historyLinkNode, "display", "block");
              }
              
              this.data.status = result.status;
              topic.publish("process.status", this.data);
            }),
            lang.hitch(this,function(error){
              topic.publish("msg", new Error(this.i18n.processes.errors.canceling));
            })
        );
        });
        if (this.data.status==="working") {
          domStyle.set(this.cancelNode,"display","inline");
        }
        if (this.data.status!=="completed") {
          update();
        } else {
          if (this.data.statistics) {
            var stats = ""+this.data.statistics.succeeded + "/" + this.data.statistics.acquired;
            html.set(this.progressNode, stats);
          }
        }
          domStyle.set(this.historyLinkNode, "display", "block");
        topic.publish("process.status", this.data);
      },
      
      _onCancel: function(evt) {
        ProcessesREST.abort(this.data.uuid).then(
            lang.hitch(this,this._onCanceled),
            lang.hitch(this,function(error){
              topic.publish("msg", new Error(this.i18n.processes.errors.canceling));
            })
        );
      },
      
      _onCanceled: function(evt) {
        this.emit("reload");
      },
      
      _onHistory: function(evt) {
        router.go("/tasks/" + this.params.taskDefinition.ref + "/history");
      }
    });
});
