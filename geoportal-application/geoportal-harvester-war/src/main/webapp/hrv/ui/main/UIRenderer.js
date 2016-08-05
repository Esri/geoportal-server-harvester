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
        "dojo/i18n!../../nls/resources",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/dom-construct",
        "dojo/number",
        "dijit/form/Select",
        "dijit/form/ValidationTextBox",
        "dijit/form/CheckBox",
        "dijit/form/TimeTextBox",
        "dijit/form/RadioButton",
        "dijit/form/Form"
      ],
  function(declare,i18n,
           lang,array,domConstruct,number,
           Select,ValidationTextBox,CheckBox,TimeTextBox,RadioButton,Form
          ){
  
    return {
      render: function(rootNode,args) {
        var dstr = [];
        array.forEach(args,lang.hitch({self: this, rootNode: rootNode},function(arg){
          dstr.push(this.self.renderArgument(this.rootNode,arg));
        }));
        return function(){ 
            array.forEach(dstr,function(dstr) {dstr();});
        };
      },
      
      renderArgument: function(rootNode,arg) {
        var argNode = domConstruct.create("div",{class: "h-editor-line"},rootNode);
        var titleNode = domConstruct.create("span",{innerHTML: arg.label, class: "h-editor-argname"},argNode);
        var placeholderWrapper = domConstruct.create("span",{class: "h-editor-argctrl"},argNode);
        var placeholderNode = domConstruct.create("span",null,placeholderWrapper);
        
        switch(arg.type) {
          case "string": return this.renderString(placeholderNode,arg);
          case "choice": return this.renderChoice(placeholderNode,arg);
          case "bool": return this.renderBool(placeholderNode,arg);
          case "temporal": return this.renderTime(placeholderNode,arg);
          case "periodical": return this.renderPeriod(placeholderNode,arg);
          default: 
            console.error("Unsupported argument type:", arg.type);
            return function(){};
        }
      },
      
      renderString: function(placeholderNode,arg) {
        var input = new ValidationTextBox({name: arg.name, required: arg.required}).placeAt(placeholderNode);
        input.name = arg.name;
        input.startup();
        return function(){ input.destroy(); };
      },
      
      renderChoice: function(placeholderNode,arg) {
        var input = new Select({name: arg.name, required: arg.required}).placeAt(placeholderNode);
        array.forEach(arg.choices,function(choice){
          input.addOption({label: choice.value, value: choice.name});
        });
        input.startup();
        return function(){ input.destroy(); };
      },
      
      renderBool: function(placeholderNode,arg) {
        var input = new CheckBox({name: arg.name, required: arg.required}).placeAt(placeholderNode);
        input.name = arg.name;
        input.startup();
        return function(){ input.destroy(); };
      },
      
      renderTime: function(placeholderNode,arg) {

        var CustomTimeTextBox = declare([TimeTextBox],{
            lastTime: null,
            
            get: function(name){
              if (name==='value') {
                var time = this.inherited(arguments);
                if (time) {
                  this.lastTime = number.format(time.getHours(),{pattern: "00"})+":"+number.format(time.getMinutes(),{pattern: "00"});
                  return this.lastTime;
                } else {
                  return this.lastTime;
                }
              } else {
                return this.inherited(arguments);
              }
            },
            set: function(name,value) {
              if (name==='value') {
                this.lastTime = value;
                return this.inherited(arguments);
              } else {
                return this.inherited(arguments);
              }
            }
        });
        
        var input = new CustomTimeTextBox({
          name: arg.name,
          constraints: {
            timePattern: 'HH:mm',
            clickableIncrement: 'T00:15:00',
            visibleIncrement: 'T00:15:00',
            visibleRange: 'T01:00:00'
          }
        }).placeAt(placeholderNode);
        
        return function(){ input.destroy(); };
      },
      
      renderPeriod: function(placeholderNode,arg) {
        var rootNode = domConstruct.create("div",null,placeholderNode);
        var dstr = [];
        dstr.push(this.renderRadio(rootNode,arg.name,"P1D",i18n.periodical.daily));
        dstr.push(this.renderRadio(rootNode,arg.name,"P1W",i18n.periodical.weekly));
        dstr.push(this.renderRadio(rootNode,arg.name,"P2W",i18n.periodical.biweekly));
        dstr.push(this.renderRadio(rootNode,arg.name,"P1M",i18n.periodical.monthly));
        return function(){ 
            array.forEach(dstr,function(dstr) {dstr();} );
        };
      },
      
      renderRadio: function(rootNode,name,value,label) {
        var div = domConstruct.create("div",null,rootNode);
        var radio = new RadioButton({
          id: "_"+value,
          value: value,
          name: name,
          "class": "h-period-radio"
        }).placeAt(div);
        radio.startup();
        var div = domConstruct.create("label",{"for": "_"+value, innerHTML: label},div);
        return function(){radio.destroy();};
      }
    };
});

