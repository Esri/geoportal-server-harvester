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
        "dojo/text!./templates/TaskEditorPane.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/query",
        "dojo/dom-construct",
        "dojo/dom-attr",
        "dojo/topic",
        "dijit/form/CheckBox",
        "dijit/form/RadioButton",
        "dijit/form/Form",
        "hrv/rest/Brokers"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,query,domConstruct,domAttr,topic,
           CheckBox,RadioButton,Form,
           BrokersREST){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      radios: [],
      checks: [],
      
      constructor: function(args) {
        this.data = args;
      },
    
      postCreate: function(){
        this.radios = [];
        this.checks = [];
        BrokersREST.input().then(
          lang.hitch(this,this.processInputs),
          lang.hitch(this,function(error){
            topic.publish("msg", new Error(this.i18n.brokers.errors.inbound));
          })
        );
        BrokersREST.output().then(
          lang.hitch(this,this.processOutputs),
          lang.hitch(this,function(error){
            topic.publish("msg", new Error(this.i18n.brokers.errors.outbound));
          })
        );
      },
      
      processInputs: function(response) {
        response = response.sort(function(a,b){
          var t1 = a.brokerDefinition.label;
          t1 = t1? t1.toLowerCase(): "";
          var t2 = b.brokerDefinition.label;
          t2 = t2? t2.toLowerCase(): "";
          if (t1<t2) return -1;
          if (t1>t2) return 1;
          return 0;
        });
        array.forEach(response,lang.hitch(this,this.addInput));
      },
      
      addInput: function(input) {
        var count = query("> div", this.inputsNode).length;
        var div = domConstruct.create("div",{class: "h-task-editor-input h-task-editor-broker"},this.inputsNode);
        var radioDiv = domConstruct.create("button",{class: "h-task-editor-input-radio"},div);
        var radio = new RadioButton({
          id: input.uuid,
          value: input.uuid,
          name: "input",
          checked: count===0,
          brokerDefinition: input.brokerDefinition
        }, radioDiv);
        this.own(radio);
        radio.startup();
        this.radios.push(radio);
        var label = domConstruct.create("label",{"for": input.uuid, class: "h-task-editor-input-label", innerHTML: input.brokerDefinition.label},div);
      },
      
      processOutputs: function(response) {
        response = response.sort(function(a,b){
          var t1 = a.brokerDefinition.label;
          t1 = t1? t1.toLowerCase(): "";
          var t2 = b.brokerDefinition.label;
          t2 = t2? t2.toLowerCase(): "";
          if (t1<t2) return -1;
          if (t1>t2) return 1;
          return 0;
        });
        array.forEach(response,lang.hitch(this,this.addOutput));
      },
      
      addOutput: function(output) {
        var div = domConstruct.create("div",{class: "h-task-editor-output h-task-editor-broker"},this.outputsNode);
        var checkDiv = domConstruct.create("input",{class: "h-task-editor-output-check"},div);
        var check = new CheckBox({
          id: output.uuid,
          value: output.uuid,
          name: "output",
          brokerDefinition: output.brokerDefinition
        }, checkDiv);
        this.own(check);
        check.startup();
        this.checks.push(check);
        var label = domConstruct.create("label",{"for": output.uuid, class: "h-task-editor-output-label", innerHTML: output.brokerDefinition.label},div);
      },
      
      _onSubmit: function() {
        var taskDefinition = {
          name: domAttr.get(this.taskName, "value"),
          source: null,
          destinations: []
        };
        array.forEach(this.radios,lang.hitch(this,function(radio){
          if (radio.checked) {
            taskDefinition.source = radio.brokerDefinition;
          }
        }));
        array.forEach(this.checks,lang.hitch(this,function(checks){
          if (checks.checked) {
            taskDefinition.destinations.push({action: checks.brokerDefinition});
          }
        }));
        this.emit("submit",{taskDefinition: taskDefinition});
      }
    });
});
