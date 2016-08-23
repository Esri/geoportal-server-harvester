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
        "hrv/rest/Processes"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,domClass,domStyle,html,topic,
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
              domStyle.set(this.progressNode,"display",result.status==="working"? "inline": "none");
              if (result.status==="working" && result.statistics) {
                html.set(this.progressNode, ""+result.statistics.succeeded);
              }
              if (result.status==="working" || result.status==="aborting") {
                setTimeout(update,2000);
              }
            }),
            lang.hitch(this,function(error){
              topic.publish("msg",this.i18n.processes.errors.canceling);
            })
        );
        });
        if (this.data.status==="working") {
          domStyle.set(this.cancelNode,"display","inline");
        }
        if (this.data.status!=="completed") {
          update();
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
