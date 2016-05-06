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
  function(declare,lang,array,on,html,topic,Dialog,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,i18n,template,Brokers,Broker,BrokerEditorPane){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
    
      postCreate: function(){
        html.set(this.captionNode,this.i18n.brokers[this.category]);
        var rest = new Brokers();
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
        widget.startup();
      },
      
      _onAdd: function(evt) {
        var brokerEditorPane = new BrokerEditorPane({
          category: this.category==="input"? "inbound": this.category==="output"? "outbound": null
        });
        var brokerEditorDialog = new Dialog({
          title: this.i18n.brokers.editor.caption,
          content: brokerEditorPane,
          onHide: function() {
            brokerEditorDialog.destroy();
          }
        });
        on(brokerEditorPane,"submit",function(){
          brokerEditorDialog.destroy();
        });
        brokerEditorDialog.show();
      }
    });
});
