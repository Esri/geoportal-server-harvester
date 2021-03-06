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
        "dojo/text!./templates/Status.html",
        "dojo/_base/lang",
        "dojo/html",
        "dojo/dom-class",
        "dojo/topic"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,html,domClass,topic
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
    
      postCreate: function(){
        topic.subscribe("msg",lang.hitch(this,this._onMsg));
      },
      
      _onMsg: function(msg) {
        domClass.remove(this.messageNode,"h-status-message-info");
        domClass.remove(this.messageNode,"h-status-message-error");
        if (!msg) {
          html.set(this.messageNode,"");
        } else if (msg instanceof Error) {
          domClass.add(this.messageNode,"h-status-message-error");
          html.set(this.messageNode,msg.message);
        } else {
          domClass.add(this.messageNode,"h-status-message-info");
          html.set(this.messageNode,msg);
        }
      }
    });
});
