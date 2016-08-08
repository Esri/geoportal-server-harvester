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
        "dojo/text!./templates/SchedulerEditorPane.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/dom-construct",
        "dojo/query",
        "dojo/topic",
        "dijit/form/Select",
        "dijit/form/Button",
        "dijit/form/Form",
        "hrv/rest/Triggers",
        "hrv/ui/main/UIRenderer"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,domConstruct,query,topic,
           Select,Button,Form,
           TriggersREST,
           Renderer
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      allTypes: {},
      dstr: null,
      
      constructor: function(args) {
        this.data = args;
      },
    
      postCreate: function(){
        WIDGET = this.formWidget;
        TriggersREST.types().then(
          lang.hitch(this,this.processTypes),
          lang.hitch(this,function(error){
            topic.publish("msg",this.i18n.tasks.errors.typesLoadingError);
          })
        );
      },
      
      processTypes: function(result) {
        var type = {label: "None", type: "NULL"};
        this.allTypes[type.type] = type;
        this.typeSelector.addOption({label: type.label, value: type.type});
        
        array.forEach(result,lang.hitch(this,this.processType));
        this.formWidget.setValues(this.data);
        this.dstr = Renderer.render(this.formNode, this.data);
        
        setTimeout(lang.hitch(this,function(){
          this.formWidget.setValues(this.data);
        }),100);
        
      },
      
      processType: function(type) {
        if (type.type!=="NOW") {
          this.allTypes[type.type] = type;
          this.typeSelector.addOption({label: type.label, value: type.type});
        }
      },
      
      resetForm: function() {
        if (this.dstr) {
          this.dstr();
          this.dstr = null;
        }
        var formNodes = query("> div", this.formNode).splice(1);
        array.forEach(formNodes,function(node){
          domConstruct.destroy(node);
        });
      },
      
      _onTypeChanged: function(evt) {
        var type = this.allTypes[evt];
        this.resetForm();
        this.dstr = Renderer.render(this.formNode, type.arguments);
      },
      
      _onSubmit: function(evt) {
        if (this.formWidget.validate()) {
          var values = this.formWidget.getValues();
          console.log("Values", values);
          var triggerDefinition = {
            type: null,
            properties: null
          };
          triggerDefinition.type = values.type;
          delete values.type;
          triggerDefinition.properties = values;
          this.emit("submit",{triggerDefinition: triggerDefinition});
        }
      }
    });
});
