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
        "dojo/text!./templates/Brokers.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/on",
        "dojo/html",
        "dojo/dom-construct",
        "dojo/json",
        "dojo/topic",
        "dijit/Dialog",
        "dijit/form/Button",
        "dijit/form/CheckBox",
        "dijit/form/Select",
        "hrv/rest/Brokers",
        "hrv/ui/brokers/Broker",
        "hrv/ui/brokers/BrokerEditorPane"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,on,html,domConstruct,json,topic,
           Dialog,Button,CheckBox,Select,
           BrokersREST,Broker,BrokerEditorPane
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
      widgets: [],
      response: null,
    
      postCreate: function(){
        this.inherited(arguments);
        html.set(this.captionNode,this.i18n.brokers[this.category]);
//        this.load(this.groupByCheckBox.get('checked'));
      },
      
      destroy: function() {
        this.inherited(arguments);
        this.clear();
      },
      
      clear: function() {
        array.forEach(this.widgets, function(widget){
          widget.destroy();
        });
        this.widgets = [];
        domConstruct.empty(this.contentNode);
        topic.publish("msg");
      },
      
      load: function(grouping) {
        if (grouping===undefined) {
          grouping = this.groupByCheckBox.get('checked');
        }
        BrokersREST[this.category]().then(
          lang.hitch(this,function(response){ 
            this.response = response;
            
            this.filterSelect.getOptions().forEach(lang.hitch(this, function(opt) {
              this.filterSelect.removeOption(opt);
            }));
            this.filterSelect.addOption({value: " ", label: ""});
            new Set(response.map(b => b.brokerDefinition.type)).forEach(lang.hitch(this, function(type) {
              this.filterSelect.addOption({value: type, label: type});
            }));

            this.processBrokers(response, grouping); 
          }),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg", new Error(this.i18n.brokers.errors.access));
          })
        );
      },
      
      compareBrokerDefinitons: function(bd1, bd2) {
        var t1 = bd1.label;
        t1 = t1? t1.toLowerCase(): "";
        var t2 = bd2.label;
        t2 = t2? t2.toLowerCase(): "";
        if (t1<t2) return -1;
        if (t1>t2) return 1;
        return 0;
      },
      
      processGroup: function(group) {
        if (group.brokers.length > 0) {
          var brokers = group.brokers.sort(lang.hitch(this, function(a,b){
            return this.compareBrokerDefinitons(a.brokerDefinition, b.brokerDefinition);
          }));
          
          var placeholder = domConstruct.create("div", {class: "h-broker-group"}, this.contentNode);
          array.forEach(brokers, lang.hitch(this, function(broker){
            this.processBroker(placeholder, broker); 
          }));
        }
      },
      
      processBrokers: function(response, grouping) {
        
        var filter = this.filterSelect.getValue().trim();
        if (filter.length > 0) {
          response = response.filter(function(r) {
            return r.brokerDefinition.type === filter;
          });
        }
        
        if (grouping) {
          
          var groups = {};
          array.forEach(response, function(broker){ 
            var group = [];
            if (groups[broker.brokerDefinition.type]) {
              group = groups[broker.brokerDefinition.type];
            } else {
              groups[broker.brokerDefinition.type] = group;
            }
            group.push(broker);
          });
          
          var groupsArray = [];
          for (group in groups) {
            groupsArray.push({
              group: group,
              brokers: groups[group]
            });
          }
          groupsArray = groupsArray.sort(function(g1, g2){
            return g1.group.localeCompare(g2.group);
          });
          
          this.clear();
          array.forEach(groupsArray, lang.hitch(this, this.processGroup));
          
        } else {
          
          response = response.sort(lang.hitch(this, function(a,b){
            return this.compareBrokerDefinitons(a.brokerDefinition, b.brokerDefinition);
          }));
          this.clear();
          array.forEach(response,lang.hitch(this,function(broker){ 
            this.processBroker(this.contentNode, broker); 
          }));
          
        }
        
      },
      
      processBroker: function(placeholder, broker) {
        var widget = new Broker(broker).placeAt(placeholder);
        this.widgets.push(widget);
        
        widget.load = lang.hitch(this,this.load);
        this.own(on(widget,"remove",lang.hitch(this,this._onRemove)));
        this.own(on(widget,"edit",lang.hitch(this,this._onEdit)));
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
          class: "h-broker-editor",
          onHide: function() {
            brokerEditorDialog.destroy();
            brokerEditorPane.destroy();
          }
        });
        this.own(brokerEditorPane);
        this.own(brokerEditorDialog);
        
        // listen to "submit" button click
        this.own(on(brokerEditorPane,"submit",lang.hitch(this, function(evt){
          var brokerDefinition = evt.brokerDefinition;
          
          // use API create new broker
          BrokersREST.create(json.stringify(brokerDefinition)).then(
            lang.hitch({brokerEditorPane: brokerEditorPane, brokerEditorDialog: brokerEditorDialog, self: this},function(){
              this.brokerEditorDialog.destroy();
              this.brokerEditorPane.destroy();
              this.self.load(this.self.groupByCheckBox.get('checked'));
            }),
            lang.hitch(this,function(error){
              console.error(error);
              topic.publish("msg", new Error(this.i18n.brokers.errors.creating));
            })
          );
        })));
        
        brokerEditorDialog.show();
      },
      
      _onRemove: function(evt) {
        var uuid = evt.data.uuid;
        
        // use API to remove broker
        BrokersREST.delete(uuid).then(
          lang.hitch(this,function(){
            this.load();
          }),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg", new Error(this.i18n.brokers.errors.creating));
          })
        );
      },
      
      _onGroupByClicked: function(evt) {
        this.clear();
        this.processBrokers(this.response, evt);
      },
      
      _onChangeFilter: function(evt) {
        console.log(evt);
        this.clear();
        this.processBrokers(this.response, this.groupByCheckBox.get('checked'));
      }
    });
});
