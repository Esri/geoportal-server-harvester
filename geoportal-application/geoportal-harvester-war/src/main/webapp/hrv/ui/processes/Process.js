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
        "dojo/dom-construct",
        "dojo/on",
        "dojo/topic",
        "hrv/rest/Processes"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,domConstruct,on,topic,
           ProcessesREST
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      data: null,
      
      constructor: function(arg) {
        this.data = arg;
      },
    
      postCreate: function(){
        if (this.data.status==="working") {
          var a = domConstruct.create("a",{class: "h-processes-process-cancel", innerHTML: this.i18n.processes.cancel},this.domNode);
          on(a,"click",lang.hitch(this,this._onCancel));
        }
      },
      
      _onCancel: function(evt) {
        ProcessesREST.abort(this.data.uuid).then(
            lang.hitch(this,this._onCanceled),
            lang.hitch(this,function(error){
              topic.publish("msg",this.i18n.processes.errors.canceling);
            })
        );
      },
      
      _onCanceled: function(evt) {
        this.emit("reload");
      }
    });
});
