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
        "dojo/topic",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/TaskEditorPane.html",
        "hrv/rest/Brokers",
        "dijit/form/CheckBox",
        "dijit/form/RadioButton"
      ],
  function(declare,lang,array,topic,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template,BrokersREST,CheckBox,RadioButton){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      
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
        console.log("Inputs", response);
        array.forEach(response,lang.hitch(this,this.addInput));
      },
      
      addInput: function(input) {
        /*
        var radio = new RadioButton({
          value: "aa",
          name: "input"
        });
        radio.placeAt(this.inputsNode);
        radio.startup();
        */
      },
      
      processOutputs: function(response) {
        console.log("Outputs", response);
      }
    });
});
