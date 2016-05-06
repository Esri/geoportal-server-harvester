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
        "dojo/query",
        "dojo/html",
        "dojo/topic",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/BrokerEditorPane.html",
        "hrv/rest/Connectors",
        "hrv/rest/Brokers",
        "dijit/form/Select",
        "dijit/form/TextBox"
      ],
  function(declare,lang,array,domConstruct,query,html,topic,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template,ConnectorsApi,BrokersApi,Select,TextBox){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      connectorTemplates: {},
      
      constructor: function(args) {
        this.data = args;
      },
    
      postCreate: function(){
        var connectorsApi = new ConnectorsApi();
        connectorsApi[this.category]().then(
          lang.hitch(this,this.processConnectors),
          lang.hitch(this,function(error){
            topic.publish("msg",this.i18n.brokers.editor.errors.connectorsLoadingErrors[this.category]);
          })
        );
      },
      
      processConnectors: function(response) {
        console.log("Connectors:",response);
        array.forEach(response,lang.hitch(this,this.processConnectorTemplate));
        if (response.length>0) {
          this.updateArgumentsForm(response[0].arguments);
        }
      },
      
      processConnectorTemplate: function(connectorTemplate) {
        this.connectorTemplates[connectorTemplate.type] = connectorTemplate;
        this.typeSelector.addOption({label: connectorTemplate.label, value: connectorTemplate.type});
      },
      
      updateArgumentsForm: function(arguments) {
        this.resetForm();
        array.forEach(arguments,lang.hitch(this,this.renderArgument));
      },
      
      resetForm: function() {
        var formNodes = query("> div", this.formNode).splice(2);
        array.forEach(formNodes,function(node){
          domConstruct.destroy(node);
        });
      },
      
      renderArgument: function(arg) {
        console.log("Argument:", arg);
        
        var argNode = domConstruct.create("div",{class: "h-broker-editor-line"},this.formNode);
        var titleNode = domConstruct.create("span",{innerHTML: arg.label, class: "h-broker-editor-argname"},argNode);
        var placeholderWrapper = domConstruct.create("span",{class: "h-broker-editor-argctrl"},argNode);
        var placeholderNode = domConstruct.create("span",null,placeholderWrapper);
        
        switch(arg.type) {
          case "string": this.renderString(placeholderNode,arg); break;
          case "choice": this.renderChoice(placeholderNode,arg); break;
          default: console.error("Unsupported argument type:", arg.type);
        }
      },
      
      renderString: function(placeholderNode,arg) {
        var input = new TextBox().placeAt(placeholderNode);
      },
      
      renderChoice: function(placeholderNode,arg) {
        var select = new Select().placeAt(placeholderNode);
        array.forEach(arg.choices,function(choice){
          select.addOption({label: choice.value, value: choice.name});
        });
      },
      
      _onTypeChanged: function(type) {
        this.updateArgumentsForm(this.connectorTemplates[type].arguments);
      },
      
      _onSubmit: function() {
        this.emit("submit");
      }
    });
});
