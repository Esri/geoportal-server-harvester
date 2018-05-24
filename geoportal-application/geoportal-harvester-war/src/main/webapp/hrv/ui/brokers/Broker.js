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
        "dojo/text!./templates/Broker.html",
        "dojo/_base/lang",
        "dojo/string",
        "dojo/topic",
        "dojo/on",
        "dojo/json",
        "dijit/Dialog",
        "dijit/ConfirmDialog",
        "hrv/rest/Brokers",
        "hrv/ui/brokers/BrokerEditorPane"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,string,topic,on,json,
           Dialog,ConfirmDialog,
           BrokersREST,BrokerEditorPane
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      typeLC: null,
      
      constructor: function(args) {
        this.data = args;
        this.typeLC = this.data.brokerDefinition.type.toLowerCase();
      },
    
      postCreate: function(){
      },
      
      _onEdit: function() {
        var brokerEditorPane = new BrokerEditorPane({
          category: this.category==="INBOUND"? "inbound": this.category==="OUTBOUND"? "outbound": null,
          data: this.data
        });
        
        // create editor dialog box
        var brokerEditorDialog = new Dialog({
          title: this.i18n.brokers.editor.caption,
          content: brokerEditorPane,
          onHide: function() {
            brokerEditorDialog.destroy();
            brokerEditorPane.destroy();
          }
        });
        
        // listen to "submit" button click
        this.own(on(brokerEditorPane,"submit",lang.hitch(this, function(evt){
          var brokerDefinition = evt.brokerDefinition;
          
          // use API to update broker
          BrokersREST.update(brokerDefinition.uuid,json.stringify(brokerDefinition)).then(
            lang.hitch({brokerEditorPane: brokerEditorPane, brokerEditorDialog: brokerEditorDialog, self: this},function(){
              this.brokerEditorDialog.destroy();
              this.brokerEditorPane.destroy();
              this.self.load();
            }),
            lang.hitch(this,function(error){
              console.error(error);
              topic.publish("msg",new Error("Error creating broker"));
            })
          );
        })));
        
        brokerEditorDialog.show();
      },
      
      _onRemove: function() {
        var dlg = new ConfirmDialog({
          title: this.i18n.brokers.removeDialog.title,
          content: string.substitute(this.i18n.brokers.removeDialog.content,{title: this.data.brokerDefinition.label}),
          "class": "h-brokers-remove-dialog",
          onExecute: lang.hitch(this,function(){
            this.emit("remove",{data: this.data});
          })
        });
        dlg.show();
      }
    });
});
