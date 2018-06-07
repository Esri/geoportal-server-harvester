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
        "hrv/rest/Tasks",
        "hrv/ui/tasks/Event",
        "hrv/utils/TaskUtils"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,topic,domStyle,html,on,
           TasksREST,Event,TaskUtils
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      widgets: [],
    
      postCreate: function(){
        topic.subscribe("nav",lang.hitch(this,this._onNav));
        this.own(on(this, "event-clicked", lang.hitch(this, this._onEventClicked)));
      },
      
      _onEventClicked: function(evt) {
        TasksREST.getFailedDocuments(evt.data.uuid).then(lang.hitch(this, this._handleFailedDocuments), lang.hitch(this, function(error){
          console.err(error);
          topic.publish("msg",new Error("Unable to access failed documents information"));
        }));
      },
      
      _handleFailedDocuments: function(failedDocumentsArray) {
        console.log(failedDocumentsArray);
      },
      
      _onNav: function(evt) {
        if (evt.type!=="history") {
          array.forEach(this.widgets,function(widget){
            widget.destroy();
          });
        }
        domStyle.set(this.domNode,"display", evt.type==="history"? "block": "none");
        if (evt.data && evt.data.taskDefinition) {
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
