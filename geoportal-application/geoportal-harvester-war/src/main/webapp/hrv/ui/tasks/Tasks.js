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
        "dojo/text!./templates/Tasks.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/dom-construct",
        "dojo/dom-attr",
        "dojo/query",
        "dojo/on",
        "dojo/json",
        "dojo/topic",
        "dojox/form/Uploader",
        "dijit/form/CheckBox",
        "dijit/Dialog",
        "dijit/form/Button",
        "hrv/rest/Tasks",
        "hrv/ui/tasks/Task",
        "hrv/ui/tasks/TaskGroup",
        "hrv/ui/tasks/TaskEditorPane",
        "hrv/utils/TaskUtils"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,domConstruct, domAttr, query, on,json,topic,
           Uploader,CheckBox,
           Dialog,Button,
           TasksREST,Task,TaskGroup,TaskEditorPane,
           TaskUtils
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      uploader: null,
      widgets: [],
    
      postCreate: function() {
        this.inherited(arguments);
        this.load(this.groupByCheckBox.get('checked'));
      },
      
      startup: function() {
        this.inherited(arguments);
        var inputNode = query("input", this.uploader.domNode);
        domAttr.set(inputNode[0], "accept", ".json");
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
        topic.publish("msg");
      },
      
      load: function(grouping) {
        this.clear();
        TasksREST.list().then(
          lang.hitch(this,function(response) {
            this.response = response;
            this.processTasks(response, grouping);
          }),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg", new Error(this.i18n.tasks.errors.accessInfo));
          })
        );
      },
      
      processTasks: function(tasks, grouping) {
        if (grouping) {
          var groups = this.groupTasks(tasks);
          array.forEach(groups, lang.hitch(this, function(group){
            group.tasks = this.sortTasks(group.tasks);
            this.processGroup(group);
          }));
        } else {
          tasks = this.sortTasks(tasks);
          array.forEach(tasks,lang.hitch(this,this.processTask));
        }
        topic.publish("triggers.list");
      },
      
      sortTasks: function(tasks) {
        return tasks.sort(function(a,b){
          var t1 = TaskUtils.makeLabel(a.taskDefinition).toLowerCase();
          var t2 = TaskUtils.makeLabel(b.taskDefinition).toLowerCase();
          if (t1<t2) return -1;
          if (t1>t2) return 1;
          return 0;
        });
      },
      
      groupTasks: function(tasks) {
        var groups = {};
        array.forEach(tasks, function(task){
          var source = task.taskDefinition.source;
          if (!groups[source.ref]) {
            groups[source.ref] = {commonSource: source, tasks: []};
          }
          groups[source.ref].tasks.push(task);
        });
        
        var groupsArray = [];
        
        for (group in groups) {
          groupsArray.push(groups[group]);
        }
        
        groupsArray = groupsArray.sort(function(a, b) {
          var la = a.commonSource.label.toLowerCase();
          var lb = b.commonSource.label.toLowerCase();
          if (la<lb) return -1;
          if (la>lb) return 1;
          return 0;
        });
        
        return groupsArray;
      },
      
      processGroup: function(group) {
        var widget = new TaskGroup(group);
        this.widgets.push(widget);
        
        widget.placeAt(this.contentNode);
        this.own(on(widget,"remove",lang.hitch(this,this._onRemove)));
        this.own(on(widget,"run",lang.hitch(this,this._onRun)));
        this.own(on(widget,"history",lang.hitch(this,this._onHistory)));
        this.own(on(widget,"renamed",lang.hitch(this,function(){
          this.clear();
          this.processTasks(this.response, this.groupByCheckBox.get('checked'));
        })));
        widget.startup();
      },
      
      processTask: function(task) {
        var widget = new Task(task);
        this.widgets.push(widget);
        
        widget.placeAt(this.contentNode);
        this.own(on(widget,"remove",lang.hitch(this,this._onRemove)));
        this.own(on(widget,"run",lang.hitch(this,this._onRun)));
        this.own(on(widget,"history",lang.hitch(this,this._onHistory)));
        this.own(on(widget,"renamed",lang.hitch(this,function(){
          domConstruct.empty(this.contentNode);
          this.processTasks(this.response, this.groupByCheckBox.get('checked'));
        })));
        widget.startup();
      },
      
      _onRemove: function(evt) {
        TasksREST.delete(evt.data.uuid).then(
          lang.hitch(this,function(){
            this.load(this.groupByCheckBox.get('checked'));
          }),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg", new Error(this.i18n.tasks.errors.remove));
          })
        );
      },
      
      _onRun: function(evt) {
        var data = evt.data;
        TasksREST.execute(data.uuid,data.taskDefinition.ignoreRobotsTxt, data.taskDefinition.incremental).then(
          lang.hitch(this,function(){
            this.load(this.groupByCheckBox.get('checked'));
          }),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg", new Error(this.i18n.tasks.errors.execute + ": " + data.taskDefinition.name));
          })
        );
      },
      
      _onAdd: function(evt) {
        var taskEditorPane = new TaskEditorPane({});

        // create editor dialog box
        var taskEditorDialog = new Dialog({
          title: this.i18n.tasks.editor.caption,
          content: taskEditorPane,
          onHide: function() {
            taskEditorDialog.destroy();
            taskEditorPane.destroy();
          }
        });
        this.own(taskEditorPane);
        this.own(taskEditorDialog);
        
        // listen to "submit" button click
        this.own(on(taskEditorPane,"submit",lang.hitch(this, function(evt){
          var taskDefinition = evt.taskDefinition;
          
          // use API create new task
          TasksREST.create(json.stringify(taskDefinition)).then(
            lang.hitch({taskEditorPane: taskEditorPane, taskEditorDialog: taskEditorDialog, self: this},function(){
              this.taskEditorDialog.destroy();
              this.taskEditorPane.destroy();
              this.self.load(this.self.groupByCheckBox.get('checked'));
            }),
            lang.hitch(this,function(error){
              console.error(error);
              topic.publish("msg", new Error(this.i18n.tasks.errors.create));
            })
          );
        })));
        
        taskEditorDialog.show();
      },
      
      _onHistory: function(evt) {
        topic.publish("nav",{type: "history", data: evt.data});
      },
      
      _onUpload: function(evt) {
        this.load(this.groupByCheckBox.get('checked'));
      },
      
      _onGroupByClicked: function(evt) {
        this.clear();
        this.processTasks(this.response, evt);
      }
    });
});
