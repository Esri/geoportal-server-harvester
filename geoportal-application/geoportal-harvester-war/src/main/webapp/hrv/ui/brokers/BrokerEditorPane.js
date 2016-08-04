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
        "dojo/text!./templates/BrokerEditorPane.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/dom-construct",
        "dojo/query",
        "dojo/topic",
        "dijit/form/Form",
        "hrv/rest/Connectors",
        "hrv/ui/main/UIRenderer"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,domConstruct,query,topic,
           Form,
           ConnectorsREST,
           Renderer
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      connectorTemplates: {},
      
      constructor: function(args) {
        this.data = args;
      },
    
      postCreate: function(){
        ConnectorsREST[this.category]().then(
          lang.hitch(this,this.processConnectors),
          lang.hitch(this,function(error){
            topic.publish("msg",this.i18n.brokers.editor.errors.connectorsLoadingErrors[this.category]);
          })
        );
        if (this.data) {
          this.typeSelector.setDisabled(true);
        }
      },
      
      processConnectors: function(response) {
        console.log("Connectors:",response);
        array.forEach(response,lang.hitch(this,this.processConnectorTemplate));
        if (this.data) {
          this.typeSelector.set('value',this.data.brokerDefinition.type);
          setTimeout(lang.hitch(this,function(){
            this.formWidget.setValues(this.data.brokerDefinition);
            this.formWidget.setValues(this.data.brokerDefinition.properties);
          }),100);
        }
        if (response.length>0) {
          this.updateArgumentsForm(response[0].arguments);
        }
      },
      
      processConnectorTemplate: function(connectorTemplate) {
        this.connectorTemplates[connectorTemplate.type] = connectorTemplate;
        this.typeSelector.addOption({label: connectorTemplate.label, value: connectorTemplate.type});
      },
      
      updateArgumentsForm: function(args) {
        this.resetForm();
        Renderer.render(this.formNode,args);
      },
      
      resetForm: function() {
        var formNodes = query("> div", this.formNode).splice(2);
        array.forEach(formNodes,function(node){
          domConstruct.destroy(node);
        });
      },
      
      _onTypeChanged: function(type) {
        this.updateArgumentsForm(this.connectorTemplates[type].arguments);
      },
      
      _onSubmit: function() {
        if (this.formWidget.validate()) {
          var values = this.formWidget.getValues();
          var brokerDefinition = {
            type: values.type,
            label: values.label,
            properties: values
          };
          if (this.data && this.data.uuid) {
            brokerDefinition.uuid = this.data.uuid;
          }
          if (this.data && this.data.brokerDefinition && this.data.brokerDefinition.type) {
            brokerDefinition.type = this.data.brokerDefinition.type;
          }
          delete brokerDefinition.properties.type;
          delete brokerDefinition.properties.label;
          this.emit("submit",{brokerDefinition: brokerDefinition});
        }
      }
    });
});
