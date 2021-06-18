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
        "dojo/text!./templates/ProcessesPane.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/topic",
        "dojo/on",
        "dojo/dom-style",
        "dojo/dom-construct",
        "dijit/form/Button",
        "hrv/rest/Processes",
        "hrv/rest/Triggers",
        "hrv/ui/processes/Process",
        "hrv/ui/processes/Trigger"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,topic,on,domStyle,domConstruct,
           Button,
           ProcessesREST,TriggersREST,Process, Trigger
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      processWidgets: [],
      triggerWidgets: [],
    
      postCreate: function(){
        this.own(topic.subscribe("nav",lang.hitch(this,this._onNav)));
        this.own(topic.subscribe("triggers.list", lang.hitch(this, function(){
          this.loadTriggers();
        })));
      },
      
      processProcesses: function(response) {
        this.processes = response;
        
        response = response.sort(function(l, r) {
          if (l.status === r.status) {
            if (l.statistics && r.statistics) {
              if (l.status === "completed") {
                if (l.statistics.endDate && r.statistics.endDate) {
                  if (l.statistics.endDate < r.statistics.endDate) return 1;
                  if (l.statistics.endDate > r.statistics.endDate) return -1;
                }
              } else {
                if (l.statistics.startDate && r.statistics.startDate) {
                  if (l.statistics.startDate < r.statistics.startDate) return 1;
                  if (l.statistics.startDate > r.statistics.startDate) return -1;
                }
              }
            }
            return 0;
          }
          return l.status === "completed"? 1: -1;
        });
        
        array.forEach(response,lang.hitch(this,this.processSingleProcess));
      },
      
      processSingleProcess: function(info) {
        var widget = new Process(info).placeAt(this.processesNode);
        this.processWidgets.push(widget);
        this.own(on(widget,"refresh",lang.hitch(this,this.refreshProcesses)));
        widget.startup();
      },
      
      _onNav: function(evt) {
        domStyle.set(this.domNode,"display", evt.type==="processes"? "block": "none");
        if (evt.type==="processes") {
          this.load();
        }
      },
      
      load: function() {
        this.loadProcesses();
        this.loadTriggers();
      },
      
      // refresh (rerender) processes
      refreshProcesses: function() {
        this.clearProcesses();
        this.processProcesses(this.processes || []);
      },
      
      // load processes from server
      loadProcesses: function() {
        this.clearProcesses();
        
        ProcessesREST.list().then(
          lang.hitch(this,this.processProcesses),
          lang.hitch(this,function(error){
            topic.publish("msg", new Error(this.i18n.processes.errors.loading));
          })
        );
      },
      
      // loads triggers from server
      loadTriggers: function() {
        this.clearTriggers();

        TriggersREST.list().then(
          lang.hitch(this,this.processTriggers),
          lang.hitch(this,function(error){
            topic.publish("msg", new Error(this.i18n.triggers.errors.loading));
          })
        );
      },
      
      processTriggers: function(response) {
        this.triggers = response;
        
        array.forEach(response,lang.hitch(this,this.processSingleTrigger));
        
        topic.publish("triggers.update", this.triggers);
      },
      
      processSingleTrigger: function(info) {
        var widget = new Trigger(info).placeAt(this.triggersNode);
        this.triggerWidgets.push(widget);
        this.own(on(widget,"refresh",lang.hitch(this,this.loadTriggers)));
        widget.startup();
      },
      
      clearProcesses: function() {
        this.processWidgets.forEach(widget => widget.destroy());
        this.processWidgets = [];
        domConstruct.empty(this.processesNode);
      },
      
      clearTriggers: function() {
        this.triggerWidgets.forEach(widget => widget.destroy());
        this.triggerWidgets = [];
        domConstruct.empty(this.triggersNode);
      },
      
      _onPurge: function() {
        ProcessesREST.purge().then(lang.hitch(this,this.load));
      }
    });
});
