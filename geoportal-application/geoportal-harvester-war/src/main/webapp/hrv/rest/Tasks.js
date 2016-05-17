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
        "dojo/request/xhr",
        "dojo/Deferred"
      ],
  function(declare,lang,xhr,Deferred){
  
    return declare([],{
      list: function() {
        return xhr("rest/harvester/tasks",{handleAs: "json"});
      },
      
      delete: function(id) {
        return xhr.del("rest/harvester/tasks/"+id,{handleAs: "json"});
      },
      
      create: function(taskDefinition) {
        return xhr.put("rest/harvester/tasks",{data: taskDefinition, handleAs: "json", headers: {"Content-Type": "application/json"}});
      },
      
      update: function(id, taskDefinition) {
        return xhr.post("rest/harvester/tasks/"+id,{data: taskDefinition, handleAs: "json", headers: {"Content-Type": "application/json"}});
      }
    });
});

