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
        "dojo/_base/lang",
        "dojo/_base/array",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/ProcessesPane.html",
        "dojo/topic",
        "dojo/dom-style",
        "dojo/dom-construct",
        "hrv/rest/Processes",
        "hrv/ui/processes/Process"
      ],
  function(declare,lang,array,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template,topic,domStyle,domConstruct,ProcessesREST,Process){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
    
      postCreate: function(){
        topic.subscribe("nav",lang.hitch(this,this._onNav));
      },
      
      processProcesses: function(response) {
        console.log("TODO handle response:", response);
        array.forEach(response,lang.hitch(this,this.processSingleProcess));
      },
      
      processSingleProcess: function(info) {
        var widget = new Process(info).placeAt(this.processesNode);
        widget.startup();
      },
      
      _onNav: function(evt) {
        domStyle.set(this.domNode,"display", evt==="processes"? "block": "none");
        if (evt==="processes") {
          domConstruct.empty(this.processesNode);
        
          ProcessesREST.list().then(
            lang.hitch(this,this.processProcesses),
            lang.hitch(this,function(error){
              topic.publish("msg",this.i18n.processes.errors.loading);
            })
          );
        }
      }
    });
});
