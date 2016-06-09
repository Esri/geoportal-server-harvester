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
        "dojo/on",
        "dojo/html",
        "dojo/dom-construct",
        "dojo/json",
        "dojo/topic",
        "dijit/Dialog",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/Brokers.html",
        "hrv/rest/Brokers",
        "hrv/ui/brokers/Broker",
        "hrv/ui/brokers/BrokerEditorPane"
      ],
  function(declare,lang,array,on,html,domConstruct,json,topic,Dialog,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template,BrokersApi,Broker,BrokerEditorPane){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
    
      postCreate: function(){
        html.set(this.captionNode,this.i18n.brokers[this.category]);
        this.load();
      },
      
      load: function() {
        domConstruct.empty(this.contentNode);
        var rest = new BrokersApi();
        rest[this.category]().then(
          lang.hitch(this,this.processBrokers),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg",new Error("Unable to access brokers information"));
          })
        );
      },
      
      processBrokers: function(response) {
        console.log("Brokers ["+this.category+"]", response);
        array.forEach(response,lang.hitch(this,this.processBroker));
      },
      
      processBroker: function(broker) {
        var widget = new Broker(broker).placeAt(this.contentNode);
        on(widget,"remove",lang.hitch(this,this._onRemove));
        on(widget,"edit",lang.hitch(this,this._onEdit));
        widget.startup();
      },
      
      _onAdd: function(evt) {
        // create editor pane
        var brokerEditorPane = new BrokerEditorPane({
          category: this.category==="input"? "inbound": this.category==="output"? "outbound": null,
          data: null
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
        on(brokerEditorPane,"submit",lang.hitch(this, function(evt){
          var brokerDefinition = evt.brokerDefinition;
          var brokersApi = new BrokersApi();
          
          // use API create new broker
          brokersApi.create(json.stringify(brokerDefinition)).then(
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
        }));
        
        brokerEditorDialog.show();
      },
      
      _onEdit: function(evt) {
        console.log("TODO editing broker...", evt.data);
        
        // create editor pane
        var brokerEditorPane = new BrokerEditorPane({
          category: this.category==="input"? "inbound": this.category==="output"? "outbound": null,
          data: evt.data
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
        on(brokerEditorPane,"submit",lang.hitch(this, function(evt){
          var brokerDefinition = evt.brokerDefinition;
          var brokersApi = new BrokersApi();
          
          // use API to update broker
          console.log("TODO: updating broker...", evt.brokerDefinition);
          brokersApi.update(brokerDefinition.uuid,json.stringify(brokerDefinition)).then(
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
        }));
        
        brokerEditorDialog.show();
      },
      
      _onRemove: function(evt) {
        var uuid = evt.data.uuid;
        var brokersApi = new BrokersApi();
        
        // use API to remove broker
        brokersApi.delete(uuid).then(
          lang.hitch(this,function(){
            this.load();
          }),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg",new Error("Error removing broker"));
          })
        );
      }
    });
});
