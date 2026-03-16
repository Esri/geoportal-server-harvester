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
        "dojo/text!./templates/App.html",
        "dojo/_base/lang",
        "dojo/topic",
        "dojo/router",
        "dijit/form/CheckBox",
        "dijit/form/RadioButton",
        "dijit/layout/ContentPane", 
        "dijit/layout/LayoutContainer",
        "hrv/ui/main/Header",
        "hrv/ui/main/Status",
        "hrv/ui/main/Nav",
        "hrv/ui/main/Stage",
        "hrv/ui/brokers/BrokersPane",
        "hrv/ui/tasks/TasksPane",
        "hrv/ui/tasks/HistoryPane",
        "hrv/ui/processes/ProcessesPane"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,topic,router,
           CheckBox,RadioButton,ContentPane,LayoutContainer,
           Header,Status,Nav,Stage,BrokersPane,TasksPane,HistoryPane,ProcessesPane
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
    
      postCreate: function(){
        // home
        router.register("/", function() {
          router.go("/home");
        });
        router.register("/home", function() {
          topic.publish("nav",{type: "processes"});
        });
        
        // brokers
        router.register("/brokers", function() {
          topic.publish("nav",{type: "brokers"});
        });
        
        // tasks
        router.register("/tasks", function() {
          topic.publish("nav",{type: "tasks"});
        });
        
         // api
        router.register("/api", function() {
          topic.publish("nav",{type: "api"});
        });
        
        // task history
        router.register("/tasks/:uuid", function(evt) {
          router.go("/tasks/" + evt.params.uuid + "/history");
        });
        router.register("/tasks/:uuid/history", function(evt) {
          topic.publish("nav",{type: "history", uuid: evt.params.uuid});
        });
        
        // task details
        router.register("/tasks/:uuid/history/:eventid", function(evt) {
          router.go("/tasks/" + evt.params.uuid + "/history/" + evt.params.eventid + "/details");
        });
        router.register("/tasks/:uuid/history/:eventid/details", function(evt) {
          topic.publish("nav",{type: "details", uuid: evt.params.uuid, eventid: evt.params.eventid});
        });
        
        // task failed documents
        router.register("/tasks/:uuid/history/:eventid/failed", function(evt) {
          topic.publish("nav",{type: "failed", uuid: evt.params.uuid, eventid: evt.params.eventid});
        });
        
        // initialize router
        router.startup();
        if (!location.hash || location.hash.length==0) {
          router.go("/home");
        }
      },
	  _onLogout: function() {
	  		console.log('Logout initiated');
	  		const ORIGIN = window.location.origin || (window.location.protocol + '//' + window.location.host);		
	  		const parts = window.location.pathname.split('/').filter(Boolean);
	  		  // If this page is at /<context>/file.html, the first segment is the WAR context.
	  		const CONTEXT = parts.length > 0 ? '/' + parts[0] : ''; 
	          const BASE = ORIGIN + CONTEXT; 
	          const redirectTo = BASE+'/login.html'; // Spring Security default logout redirect
	  		try {
	  		    // 1) Clear SPA-side tokens/state
	  		    sessionStorage.removeItem('access_token');
	  		    sessionStorage.removeItem('refresh_token');
	  		    sessionStorage.removeItem('id_token');
	  		    sessionStorage.removeItem('token_type');
	  		    sessionStorage.removeItem('expires_in');		  
	  		    sessionStorage.removeItem('pkce_code_verifier');
	  		    sessionStorage.removeItem('pkce_state');

	  		    // 2) Server-side logout (Spring Security default /logout endpoint)	
	  			var def = new Deferred();
	  			def.resolve(fetch(`${BASE}/logout`, {
	                method: 'POST',
	                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	                credentials: 'include'
	              }));
	              def.then(() => {
	                  console.log('Logout successful');
	              }).catch(e => {
	                  console.warn('Logout error', e);
	              });	    
	  		  fetch(`${BASE}/logout`, {
	  		      method: 'POST',
	  		      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	  		      credentials: 'include'
	  		    });
	  		  } catch (e) {
	  		    // Log-and-continue; redirect anyway
	  		    console.warn('Logout error', e);
	  		  } finally {
	  		    // 3) Navigate user to login page 
	  		    window.location.replace(redirectTo);
	  		  }
	       }
	  		
    });
});
