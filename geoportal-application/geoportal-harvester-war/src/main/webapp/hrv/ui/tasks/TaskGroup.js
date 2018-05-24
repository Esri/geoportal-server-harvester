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
        "dojo/text!./templates/TaskGroup.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/string",
        "dojo/dom-attr",
        "dojo/html",
        "dojo/topic",
        "dojo/on",
        "dojo/json",
        "dojo/promise/all",
        "dijit/registry",
        "dijit/Dialog",
        "dijit/ConfirmDialog",
        "hrv/rest/Tasks",
        "hrv/rest/Triggers",
        "hrv/ui/tasks/SchedulerEditorPane",
        "hrv/ui/tasks/TaskRenamePane",
        "hrv/ui/tasks/Task",
        "hrv/utils/TaskUtils"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,string,domAttr,html,topic,on,json,all,
           registry, Dialog,ConfirmDialog,
           TasksREST, TriggersREST,
           SchedulerEditorPane, TaskRenamePane, Task,
           TaskUtils
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      widgets: [],
      
      constructor: function(args) {
        this.inherited(arguments);
        this.data = args;
      },
    
      postCreate: function(){
        this.inherited(arguments);
        array.forEach(this.data.tasks, lang.hitch(this, this.processTask));
      },
      
      destroy: function() {
        this.inherited(arguments);
        this.clear();
      },
      
      clear: function() {
        array.forEach(this.widgets, function(widget){
          widget.destroy();
        });
        this.widgets = [];
      },
      
      processTask: function(task) {
        var widget = new Task(task);
        this.widgets.push(widget);
        widget.placeAt(this.contentNode);
        widget.startup();
      }
    });
});
