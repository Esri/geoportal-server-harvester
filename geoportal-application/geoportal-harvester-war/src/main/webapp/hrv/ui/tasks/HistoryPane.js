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
        "hrv/rest/Tasks",
        "hrv/ui/tasks/Event"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,topic,domStyle,html,
           TasksREST,Event
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      widgets: [],
    
      postCreate: function(){
        topic.subscribe("nav",lang.hitch(this,this._onNav));
      },
      
      _onNav: function(evt) {
        if (evt.type!=="history") {
          array.forEach(this.widgets,function(widget){
            widget.destroy();
          });
        }
        domStyle.set(this.domNode,"display", evt.type==="history"? "block": "none");
        if (evt.data && evt.data.taskDefinition) {
          html.set(this.labelNode, this.makeLabel(evt.data.taskDefinition));
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
        console.log(this.i18n.tasks.history.title, response);
        array.forEach(response.sort(function(l,r){return r.startTimestamp - l.startTimestamp;}),lang.hitch(this,this.processEvent));
      },
      
      processEvent: function(event) {
        var widget = new Event(event);
        widget.placeAt(this.contentNode);
        widget.startup();
        this.widgets.push(widget);
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
          if (taskDefinition.destinations.length>1) {
            destLabel = "[" + destLabel + "]";
          }
        }
        return sourceLabel + " -> " + destLabel;
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
