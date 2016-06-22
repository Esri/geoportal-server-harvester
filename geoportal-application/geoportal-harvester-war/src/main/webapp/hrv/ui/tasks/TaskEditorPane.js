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
        "dojo/query",
        "dojo/dom-construct",
        "dojo/topic",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/TaskEditorPane.html",
        "hrv/rest/Brokers",
        "dijit/form/CheckBox",
        "dijit/form/RadioButton",
        "dijit/form/Form"
      ],
  function(declare,lang,array,query,domConstruct,topic,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template,BrokersREST,CheckBox,RadioButton,Form){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      radios: [],
      checks: [],
      
      constructor: function(args) {
        this.data = args;
      },
    
      postCreate: function(){
        BrokersREST.input().then(
          lang.hitch(this,this.processInputs),
          lang.hitch(this,function(error){
            topic.publish("msg",this.i18n.brokers.editor.errors.connectorsLoadingErrors.inbound);
          })
        );
        BrokersREST.output().then(
          lang.hitch(this,this.processOutputs),
          lang.hitch(this,function(error){
            topic.publish("msg",this.i18n.brokers.editor.errors.connectorsLoadingErrors.outbound);
          })
        );
      },
      
      processInputs: function(response) {
        array.forEach(response,lang.hitch(this,this.addInput));
      },
      
      addInput: function(input) {
        var count = query("> div", this.inputsNode).length;
        var div = domConstruct.create("div",{class: "h-task-editor-input h-task-editor-broker"},this.inputsNode);
        var radioDiv = domConstruct.create("button",{class: "h-task-editor-input-radio"},div);
        var radio = new RadioButton({
          value: input.uuid,
          id: input.uuid,
          name: "input",
          checked: count==0,
          brokerDefinition: input.brokerDefinition
        }, radioDiv);
        radio.startup();
        this.radios.push(radio);
        var label = domConstruct.create("label",{"for": input.uuid, class: "h-task-editor-input-label", innerHTML: input.brokerDefinition.label},div);
      },
      
      processOutputs: function(response) {
        array.forEach(response,lang.hitch(this,this.addOutput));
      },
      
      addOutput: function(output) {
        var div = domConstruct.create("div",{class: "h-task-editor-output h-task-editor-broker"},this.outputsNode);
        var checkDiv = domConstruct.create("input",{class: "h-task-editor-output-check"},div);
        var check = new CheckBox({
          value: output.uuid,
          id: output.uuid,
          name: "output",
          brokerDefinition: output.brokerDefinition
        }, checkDiv);
        check.startup();
        this.checks.push(check);
        var label = domConstruct.create("label",{"for": output.uuid, class: "h-task-editor-output-label", innerHTML: output.brokerDefinition.label},div);
      },
      
      _onSubmit: function() {
        console.log("Submit");
        var taskDefinition = {
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
            taskDefinition.destinations.push(checks.brokerDefinition);
          }
        }));
        this.emit("submit",{taskDefinition: taskDefinition});
      }
    });
});
