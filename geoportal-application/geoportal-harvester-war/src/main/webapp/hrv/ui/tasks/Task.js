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

define([
  "dojo/_base/declare",
  "dijit/_WidgetBase",
  "dijit/_TemplatedMixin",
  "dijit/_WidgetsInTemplateMixin",
  "dojo/i18n!../../nls/resources",
  "dojo/text!./templates/Task.html",
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
  "hrv/utils/TaskUtils",
  "dojo/dom-style"
], function (
  declare,
  _WidgetBase,
  _TemplatedMixin,
  _WidgetsInTemplateMixin,
  i18n,
  template,
  lang,
  array,
  string,
  domAttr,
  html,
  topic,
  on,
  json,
  all,
  registry,
  Dialog,
  ConfirmDialog,
  TasksREST,
  TriggersREST,
  SchedulerEditorPane,
  TaskRenamePane,
  TaskUtils,
  domStyle
) {
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
    i18n: i18n,
    templateString: template,

    constructor: function (args) {
      this.data = args;
      this.label = TaskUtils.makeLabel(this.data.taskDefinition);
    },

    postCreate: function () {
      domAttr.set(this.exportNode, "href", TasksREST.export(this.data.uuid));
      this.own(
        topic.subscribe(
          "triggers.update",
          lang.hitch(this, function (triggers) {
            var scheduled = !!triggers.find(
              lang.hitch(this, function (trigger) {
                return trigger.taskId === this.data.uuid;
              })
            );
            this.showTriggerMark(scheduled);
          })
        )
      );
    },

    showTriggerMark: function (show) {
      html.set(this.scheduledNode, show ? "Scheduled" : "");
      domStyle.set(this.scheduledNode, "display", show ? "inline" : "none");
    },

    _onRemove: function () {
      var dlg = new ConfirmDialog({
        title: this.i18n.tasks.removeDialog.title,
        content: string.substitute(this.i18n.tasks.removeDialog.content, {
          title: TaskUtils.makeLabel(this.data.taskDefinition)
        }),
        class: "h-tasks-remove-dialog",
        onExecute: lang.hitch(this, function () {
          this.emit("remove", { data: this.data });
        })
      });

      this.own(dlg);
      dlg.show();
    },

    _onRun: function () {
      var dlg = new ConfirmDialog({
        title: this.i18n.tasks.runDialog.title,
        content:
          string.substitute(this.i18n.tasks.runDialog.content, {
            title: TaskUtils.makeLabel(this.data.taskDefinition)
          }) +
          "<div class='h-tasks-run-dialog-options'>" +
          "<button data-dojo-type='dijit/form/CheckBox' id='ignoreRobots'></button>" +
          "<label for='ignoreRobots'>" +
          this.i18n.tasks.runDialog.ignoreRobots +
          "</label><br>" +
          "<button data-dojo-type='dijit/form/CheckBox' id='incremental'></button>" +
          "<label for='incremental'>" +
          this.i18n.tasks.runDialog.incremental +
          "</label>" +
          "</div>",
        class: "h-tasks-run-dialog",
        parseOnLoad: true,
        onExecute: lang.hitch(this, function () {
          var ignoreRobots = registry.byId("ignoreRobots");
          this.data.taskDefinition.ignoreRobotsTxt = ignoreRobots.checked;
          var incremental = registry.byId("incremental");
          this.data.taskDefinition.incremental = incremental.checked;
          this.emit("run", { data: this.data });
          ignoreRobots.destroyRecursive();
          incremental.destroyRecursive();
        }),
        onCancel: lang.hitch(this, function () {
          var ignoreRobots = registry.byId("ignoreRobots");
          var incremental = registry.byId("incremental");
          ignoreRobots.destroyRecursive();
          incremental.destroyRecursive();
        })
      });

      this.own(dlg);
      dlg.show();
    },

    _onHistory: function () {
      this.emit("history",{uuid: this.data.uuid});
    },

    _onSchedule: function (evt) {
      TasksREST.triggers(this.data.uuid).then(
        lang.hitch(this, function (triggers) {
          var close = function () {
            schedulerEditorDialog.destroy();
            schedulerEditorPane.destroy();
          };

          var data = {};
          if (triggers.length > 0) {
            data.type = triggers[0].triggerDefinition.type;
            lang.mixin(data, triggers[0].triggerDefinition.properties);
          }
          var schedulerEditorPane = new SchedulerEditorPane(data);

          // create editor dialog box
          var schedulerEditorDialog = new Dialog({
            title: this.i18n.tasks.editor.caption,
            content: schedulerEditorPane,
            onHide: close
          });
          this.own(schedulerEditorPane);
          this.own(schedulerEditorDialog);

          this.own(
            on(
              schedulerEditorPane,
              "submit",
              lang.hitch(this, function (evt) {
                TasksREST.triggers(this.data.uuid).then(
                  lang.hitch(this, function (triggers) {
                    var deferred = [];
                    array.forEach(
                      triggers,
                      lang.hitch(this, function (trigger) {
                        deferred.push(TriggersREST.delete(trigger.uuid));
                      })
                    );

                    all(deferred).then(
                      lang.hitch(this, function (response) {
                        if (evt.triggerDefinition.type !== "NULL") {
                          TasksREST.schedule(
                            this.data.uuid,
                            json.stringify(evt.triggerDefinition),
                            evt.ignoreRobots,
                            evt.incremental
                          ).then(
                            lang.hitch(this, function (response) {
                              close();
                              this.showTriggerMark(true);
                            }),
                            lang.hitch(this, function (error) {
                              console.error(error);
                              topic.publish("msg", new Error(this.i18n.tasks.errors.schedule));
                              close();
                            })
                          );
                        } else {
                          close();
                          this.showTriggerMark(false);
                        }
                      }),
                      lang.hitch(this, function (error) {
                        console.error(error);
                        topic.publish("msg", new Error(this.i18n.tasks.errors.triggersDelete));
                        close();
                      })
                    );
                  }),
                  lang.hitch(this, function (error) {
                    console.error(error);
                    topic.publish("msg", new Error(this.i18n.tasks.errors.accessInfo));
                    close();
                  })
                );
              })
            )
          );

          schedulerEditorDialog.show();
        }),
        lang.hitch(this, function (error) {
          console.error(error);
          topic.publish("msg", new Error(this.i18n.tasks.errors.readScheduling));
        })
      );
    },

    _onRename: function (evt) {
      var taskRenamePane = new TaskRenamePane(this.data);

      // create editor dialog box
      var taskRenameDialog = new Dialog({
        title: this.i18n.tasks.renamer.caption,
        content: taskRenamePane,
        onHide: close
      });

      this.own(taskRenamePane);
      this.own(taskRenameDialog);

      var close = function () {
        taskRenameDialog.destroy();
        taskRenamePane.destroy();
      };

      this.own(
        on(
          taskRenamePane,
          "rename-submit",
          lang.hitch(this, function (evt) {
            var taskDefinition = evt.taskDefinition;
            TasksREST.update(this.data.uuid, json.stringify(taskDefinition)).then(
              lang.hitch(this, function (response) {
                this.data.taskDefinition = taskDefinition;
                this.label = TaskUtils.makeLabel(this.data.taskDefinition);
                html.set(this.taskName, this.label);
                this.emit("renamed");
              }),
              lang.hitch(this, function (error) {
                console.error(error);
                topic.publish("msg", new Error(this.i18n.tasks.errors.rename));
              })
            );
            close();
          })
        )
      );

      this.own(
        on(
          taskRenamePane,
          "rename-close",
          lang.hitch(this, function () {
            close();
          })
        )
      );

      taskRenameDialog.show();
    }
  });
});
