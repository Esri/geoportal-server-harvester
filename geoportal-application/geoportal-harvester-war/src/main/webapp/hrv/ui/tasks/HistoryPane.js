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
           lang,array,topic,domStyle,html,on,domConstruct,
           TasksREST,Event,TaskUtils
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      widgets: [],
      handles: [],
    
      postCreate: function(){
        topic.subscribe("nav",lang.hitch(this,this._onNav));
        this.own(on(this, "event-clicked", lang.hitch(this, this._onEventClicked)));
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
        TasksREST.getFailedDocuments(evt.data.uuid).then(lang.hitch(this, function(failedDocuments) { 
          this._handleFailedDocuments(failedDocuments); 
        }), lang.hitch(this, function(error){
          console.err(error);
          topic.publish("msg",new Error("Unable to access failed documents information"));
        }));
      },
      
      _handleFailedDocuments: function(failedDocuments) {
        if (failedDocuments) {
          array.forEach(failedDocuments, lang.hitch(this,function(recordId) {
            var span = domConstruct.create("span", {}, this.failedNode);
            var link = domConstruct.create("a", {innerHTML: recordId, href: "#"}, span);
            this.handles.push(on(link, "click", lang.hitch(this, function(evt){
              console.log("Clicked failed document id", this.data, recordId);
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
              }));
            })));
          }));
        }
      },
      
      _onNav: function(evt) {
        this._empty();
        if (evt.type!=="history") {
          array.forEach(this.widgets,function(widget){
            widget.destroy();
          });
        }
        domStyle.set(this.domNode,"display", evt.type==="history"? "block": "none");
        if (evt.data && evt.data.taskDefinition) {
          this.data = evt.data;
          html.set(this.labelNode, TaskUtils.makeLabel(evt.data.taskDefinition));
          TasksREST.history(evt.data.uuid).then(
            lang.hitch(this,this.processHistory),
            lang.hitch(this,function(error){
              console.error(error);
              topic.publish("msg",new Error("Unable to access history information"));
            })
          );
        }
      },
      
      processHistory: function(response) {
        array.forEach(response.sort(function(l,r){return r.startTimestamp - l.startTimestamp;}),lang.hitch(this,this.processEvent));
      },
      
      processEvent: function(event) {
        var widget = new Event(event);
        widget.placeAt(this.contentNode);
        widget.startup();
        this.widgets.push(widget);
      }
    });
});
