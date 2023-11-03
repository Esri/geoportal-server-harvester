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
        "dojo/dom-attr",
        "dojo/html",
        "dojo/number",
        "dijit/form/Select",
        "dijit/form/ValidationTextBox",
        "dijit/form/CheckBox",
        "dijit/form/TextBox",
        "dijit/form/TimeTextBox",
        "dijit/form/RadioButton",
        "dijit/form/NumberTextBox",
        "dijit/form/Textarea",
        "dijit/form/Form",
        "dijit/form/Button",
        "dojox/html/entities",
        "hrv/utils/TextScrambler"
      ],
  function(declare,i18n,
           lang,array,domConstruct,domAttr,html,number,
           Select,ValidationTextBox,CheckBox,TextBox,TimeTextBox,RadioButton,NumberTextBox,Textarea,Form,Button,
           entities,TextScrambler
          ){
  
    return {
      render: function(rootNode,args) {
        var rendHandlers = [];
        array.forEach(args,lang.hitch({self: this, rootNode: rootNode},function(arg){
          rendHandlers.push(this.self.renderArgument(this.rootNode,arg));
        }));
        return { 
          init: function(values) {
            array.forEach(rendHandlers,function(rendHandler) {rendHandler.init(values);});
          },
          read: function(values) {
            array.forEach(rendHandlers,function(rendHandler) {rendHandler.read(values);});
          },
          destroy: function() {
            array.forEach(rendHandlers,function(rendHandler) {rendHandler.destroy();});
          }
        };
      },
      
      renderArgument: function(rootNode,arg) {
        var argNode = domConstruct.create("div",{class: "h-editor-line"},rootNode);
        if(arg.label!=="hidden")
            var titleNode = domConstruct.create("span",{innerHTML: arg.label+":", class: "h-editor-argname"},argNode);
        var placeholderWrapper = domConstruct.create("span",{class: "h-editor-argctrl"},argNode);
        var placeholderNode = domConstruct.create("span",null,placeholderWrapper);
        
        if (arg.hint) {
          domConstruct.create("br",null,placeholderWrapper,"last");
          var hintNode = domConstruct.create("span",{class: "h-editor-argctrl-hint"},placeholderWrapper,"last");
          html.set(hintNode,i18n.brokers.hint+": "+entities.encode(arg.hint));
        }
        
        switch(arg.type) {
          case "string": return this.renderString(placeholderNode,arg);
          case "text": return this.renderText(placeholderNode,arg);
          case "choice": return this.renderChoice(placeholderNode,arg);
          case "bool": return this.renderBool(placeholderNode,arg);
          case "temporal": return this.renderTime(placeholderNode,arg);
          case "periodical": return this.renderPeriod(placeholderNode,arg);
          case "integer": return this.renderInteger(placeholderNode, arg);
          case "button": return this.renderButton(placeholderNode, arg);    
          case "hidden": return this.renderHidden(placeholderNode, arg);            
          default: 
            console.error("Unsupported argument type:", arg.type);
            return {
              init: function(){},
              read: function(){},
              destroy: function() {}
            };
        }
      },
      
      renderString: function(placeholderNode,arg) {
        var input = !arg.regExp? 
        new ValidationTextBox({
          name: arg.name, 
          required: arg.required, 
          type: arg.password? "password": "input"
        }).placeAt(placeholderNode):
        new ValidationTextBox({
          name: arg.name, 
          required: arg.required, 
          type: arg.password? "password": "input",
          regExp: arg.regExp
        }).placeAt(placeholderNode);
        input.name = arg.name;
        if (arg.password) {
          domAttr.set(input.focusNode,"autocomplete","new-password");
        } 
        if (arg.defaultValue!=null) {
          input.set("value", arg.defaultValue);
        }
        input.startup();
        return { 
          init: function(values) {
            input.set("value", !arg.password? (values[arg.name]!=null? values[arg.name]: arg.defaultValue): (values[arg.name]? TextScrambler.decode(values[arg.name]): ""));
          },
          read: function(values) {
            values[arg.name] = !arg.password? input.get("value"): TextScrambler.encode(input.get("value"));
          },
          destroy: function() {
            input.destroy();
          } 
        };
      },
      
      renderText: function(placeholderNode,arg) {
        var input = new Textarea({
          name: arg.name, 
          required: arg.required          
        }).placeAt(placeholderNode);
        input.name = arg.name;
        if (arg.defaultValue!==null) {
          input.set("value", arg.defaultValue);
        }
        input.startup();
        return { 
          init: function(values) {
            input.set("value", values[arg.name]!==null? values[arg.name]: arg.defaultValue);
          },
          read: function(values) {
            values[arg.name] = input.get("value");
          },
          destroy: function() {
            input.destroy();
          } 
        };
      },
      
       renderButton: function (placeholderNode, arg) {
        var input = new Button({
          name: arg.name,
          required: arg.required         
        }).placeAt(placeholderNode);

        input.set("label", arg.label);
        input.startup();

        return {
          init: function (values) {           
          },
          read: function (values) {            
          },
          destroy: function () {
            input.destroy();
          }
        }
      },
      renderHidden: function (placeholderNode, arg) {
        var input = new TextBox({
          name: arg.name,
          type: "hidden"
        });

        input.set("value", "");
        input.startup();

        return {
          init: function (values) {
            input.set("value", Number(values[arg.name]));
          },
          read: function (values) {
            values[arg.name] = input.get("value");
          },
          destroy: function () {
            input.destroy();
          }
        };
      },

      renderInteger: function (placeholderNode, arg) {
        var input = new NumberTextBox({
          name: arg.name,
          required: arg.required
        }).placeAt(placeholderNode);

        input.set("value", arg.defaultValue ? Number(arg.defaultValue) : 0);

        input.startup();

        return {
          init: function (values) {
            input.set("value", Number(values[arg.name]));
          },
          read: function (values) {
            values[arg.name] = input.get("value");
          },
          destroy: function () {
            input.destroy();
          }
        }
      },
      
      renderChoice: function(placeholderNode,arg) {
        var input = new Select({name: arg.name, required: arg.required}).placeAt(placeholderNode);
        array.forEach(arg.choices,function(choice){
          input.addOption({label: choice.value, value: choice.name});
        });
        input.startup();
        return { 
          init: function(values) {
            input.set("value", values[arg.name]);
          },
          read: function(values) {
            values[arg.name] = input.get("value");
          },
          destroy: function() {
            input.destroy();
          } 
        };
      },
      
      renderBool: function(placeholderNode,arg) {
        var input = new CheckBox({name: arg.name, required: arg.required}).placeAt(placeholderNode);
        input.name = arg.name;
        if (arg.defaultValue!=null) {
          input.set("checked", arg.defaultValue);
        }
        input.startup();
        return { 
          init: function(values) {
            input.set("checked", values[arg.name]!=null? values[arg.name]=="true": arg.defaultValue==true);
          },
          read: function(values) {
            values[arg.name] = input.get("checked")? "true": "false";
          },
          destroy: function() {
            input.destroy();
          } 
        };
      },
      
      renderTime: function(placeholderNode,arg) {
        
        var input = new TimeTextBox({
          name: arg.name,
          constraints: {
            timePattern: 'HH:mm',
            clickableIncrement: 'T00:15:00',
            visibleIncrement: 'T00:15:00',
            visibleRange: 'T01:00:00'
          }
        }).placeAt(placeholderNode);
        
        if (arg.defaultValue!=null) {
          input.set("vallue", "T"+arg.defaultValue);
        }
        
        input.startup();
        
        return { 
          init: function(values) {
            input.set("value", "T"+(values[arg.name]!=null? values[arg.name]: arg.defaultValue));
          },
          read: function(values) {
            var result = input.get("value");
            if (result) {
              values[arg.name] = number.format(result.getHours(),{pattern:"00"})+":"+number.format(result.getMinutes(),{pattern:"00"});
            }
          },
          destroy: function() {
            input.destroy();
          } 
        };
      },
      
      renderPeriod: function(placeholderNode,arg) {
        var rootNode = domConstruct.create("div",null,placeholderNode);
        var rendHandlers = [];
        rendHandlers.push(this.renderRadio(rootNode,arg.name,"PT1H",i18n.periodical.hourly));
        rendHandlers.push(this.renderRadio(rootNode,arg.name,"P1D",i18n.periodical.daily));
        rendHandlers.push(this.renderRadio(rootNode,arg.name,"P1W",i18n.periodical.weekly));
        rendHandlers.push(this.renderRadio(rootNode,arg.name,"P2W",i18n.periodical.biweekly));
        rendHandlers.push(this.renderRadio(rootNode,arg.name,"P1M",i18n.periodical.monthly));
        return { 
          init: function(values) {
            array.forEach(rendHandlers,function(rendHandler) {rendHandler.init(values);} );
          },
          read: function(values) {
            array.forEach(rendHandlers,function(rendHandler) {rendHandler.read(values);} );
          },
          destroy: function() {
            array.forEach(rendHandlers,function(rendHandler) {rendHandler.destroy();} );
          }
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
        return { 
          init: function(values) {
            radio.set("checked", values[name]===value);
          },
          read: function(values) {
            if (radio.get("checked")) {
              values[name] = radio.get("value");
            }
          },
          destroy: function() {
            radio.destroy();
          } 
        };
      }
    };
});

