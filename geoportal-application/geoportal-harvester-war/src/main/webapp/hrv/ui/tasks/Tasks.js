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
        "dojo/dom-construct",
        "dojo/topic",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/Tasks.html",
        "hrv/rest/Tasks",
        "hrv/ui/tasks/Task"
      ],
  function(declare,lang,array,domConstruct,topic,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template,Tasks,Task){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
    
      postCreate: function(){
        this.load();
      },
      
      load: function() {
        domConstruct.empty(this.contentNode);
        var rest = new Tasks();
        rest.list().then(
          lang.hitch(this,this.processTasks),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg",new Error("Unable to access tasks information"));
          })
        );
      },
      
      processTasks: function(response) {
        array.forEach(response,lang.hitch(this,this.processTask));
      },
      
      processTask: function(task) {
        var widget = new Task(task).placeAt(this.contentNode);
        widget.startup();
      },
      
      _onAdd: function(evt) {
      }
    });
});
