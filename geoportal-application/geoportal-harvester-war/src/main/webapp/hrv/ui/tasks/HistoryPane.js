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
        "dojo/text!./templates/HistoryPane.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/topic",
        "dojo/router",
        "dojo/dom-style",
        "dojo/html",
        "dojo/on",
        "dojo/dom-construct",
        "hrv/rest/Tasks",
        "hrv/ui/tasks/Event",
        "hrv/utils/TaskUtils"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,topic,router,
           domStyle,html,on,domConstruct,
           TasksREST,Event,TaskUtils
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      widgets: [],
      handles: [],
    
      postCreate: function(){
        this.own(topic.subscribe("nav",lang.hitch(this,this._onNav)));
        this.own(on(this, "event-clicked", lang.hitch(this, this._onEventClicked)));
        this.own(on(this, "more-clicked", lang.hitch(this, this._onMoreClicked)));
      },
      
      destroy: function() {
        this._empty();
      },
      
      _empty: function() {
        array.forEach(this.handles, function(handle){ handle.remove(); });
        this.handles = [];
        domConstruct.empty(this.failedNode);
      },
      
      _onEventClicked: function(evt) {
        this._empty();
        router.go("/tasks/" + evt.taskid + "/history/" + evt.data.uuid + "/failed");
      },
      
      _onMoreClicked: function(evt) {
        this._empty();
        router.go("/tasks/" + evt.taskid + "/history/" + evt.data.uuid + "/details");
      },
      
      loadFailedDocuments: function(eventid) {
        TasksREST.getFailedDocuments(eventid).then(lang.hitch(this, function(failedDocuments) { 
          this._handleFailedDocuments(failedDocuments); 
        }), lang.hitch(this, function(error){
          console.debug(error);
          topic.publish("msg", new Error(this.i18n.tasks.errors.accessFialed));
        }));
      },
      
      _handleFailedDocuments: function(failedDocuments) {
        if (failedDocuments) {
          array.forEach(failedDocuments, lang.hitch(this,function(recordId) {
            var span = domConstruct.create("div", {}, this.failedNode);
            var link = domConstruct.create("a", {innerHTML: recordId, href: "javascript:void(0)"}, span);
            this.handles.push(on(link, "click", lang.hitch(this, function(evt){
              TasksREST.getFailedRecord(this.data.uuid, recordId).then(lang.hitch(this, function(response){
                var newWindow = window.open(null, "_blank");
                newWindow.document.open();
                newWindow.document.write(response
                  .replace(/&/g, "&amp;")
                  .replace(/</g, "&lt;")
                  .replace(/>/g, "&gt;")
                  .replace(/"/g, "&quot;")
                  .replace(/'/g, "&#039;")
                );
                newWindow.document.close();
                newWindow.document.title = recordId;
              }), lang.hitch(this, function(error){
                console.debug(error);
                topic.publish("msg",topic.publish("msg", new Error(this.i18n.tasks.errors.accessFialed)));
              }));
            })));
          }));
        }
      },
      
      _onNav: function(evt) {
        this._empty();
        switch (evt.type) {
          case "history":
            this.destroyWidgets();
            this.loadHistory(evt.uuid);
            domStyle.set(this.domNode, "display", "block");
            break;
            
          case "details":
            if (this.widgets.length==0)
              this.loadHistory(evt.uuid);
            this.loadDetails(evt.uuid, evt.eventid).then(lang.hitch(this, function(details) {
              array.forEach(details, lang.hitch(this, function(detail){
                var span = domConstruct.create("div", {}, this.failedNode);
                var detailsNode = domConstruct.create("div", {innerHTML: detail, className: "h-event-details"}, span);
              }));
            }));
            domStyle.set(this.domNode, "display", "block");
            break;
            
          case "failed":
            if (this.widgets.length==0)
              this.loadHistory(evt.uuid);
            this.loadFailedDocuments(evt.eventid);
            domStyle.set(this.domNode, "display", "block");
            break;
            
          default:
            this.destroyWidgets();
            domStyle.set(this.domNode, "display", "none");
            break;
        }
      },
      
      destroyWidgets: function() {
        array.forEach(this.widgets,function(widget){
          widget.destroy();
        });
      },
      
      loadDetails: function(taskid, eventid) {
        return TasksREST.history(taskid).then(
          lang.hitch(this,function(history) {
            var event = history? history.find(function(h) { return h.uuid === eventid; }): null
            return event && event.details? event.details: null;
          }),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg",topic.publish("msg", new Error(this.i18n.tasks.errors.accessHistory)));
          })
        );
      },
      
      loadHistory: function(taskid) {
        if (taskid) {
          TasksREST.read(taskid).then(
            lang.hitch(this, function(response) {
              html.set(this.labelNode, TaskUtils.makeLabel(response.taskDefinition));
            }),
            lang.hitch(this,function(error){
              console.error(error);
              topic.publish("msg",topic.publish("msg", new Error(this.i18n.tasks.errors.accessHistory)));
            })
          );
          TasksREST.history(taskid).then(
            lang.hitch(this,function(history) { this.processHistory(taskid, history); }),
            lang.hitch(this,function(error){
              console.error(error);
              topic.publish("msg",topic.publish("msg", new Error(this.i18n.tasks.errors.accessHistory)));
            })
          );
        }
      },
      
      processHistory: function(taskid, history) {
        array.forEach(history.sort(function(l,r){return r.startTimestamp - l.startTimestamp;}),
          lang.hitch(this,function(event) { 
            this.processEvent(taskid, event);
          })
        );
      },
      
      processEvent: function(taskid, event) {
        var widget = new Event({taskid: taskid, event: event});
        widget.placeAt(this.contentNode);
        widget.startup();
        this.widgets.push(widget);
      }
    });
});
