/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
define({
  root: {
    
    general: {
      cancel: "Cancel",
      close: "Close",
      del: "Delete",
      error: "Error",
      ok: "OK",
      open: "Open"
    },
    
    periodical: {
      daily: "Daily",
      weekly: "Weekly",
      biweekly: "Bi-weekly",
      monthly: "Monthly"
    },
    
    header: {
      caption: "Geoportal Server Harvester ver. 2.0"
    },
    
    navigation: {
      home: "Home",
      connectors: "Connectors",
      brokers: "Brokers",
      tasks: "Tasks"
    },
    
    brokers: {
      input: "Input brokers",
      output: "Output brokers",
      edit: "edit",
      remove: "remove",
      add: "Add",
      editor: {
        caption: "Broker",
        type: "Type:",
        title: "Title:",
        submit: "Submit",
        errors: {
          connectorsLoadingErrors: {
            inbound: "Error loading inbound connectors",
            outbound: "Error loading outbound connectors"
          }
        }
      },
      removeDialog: {
        title: "Delete adaptor?",
        content: "Please, confirm deletion of the broker: ${title}"
      }
    },
    
    tasks: {
      caption: "Tasks",
      add: "Add",
      run: "run",
      export: "export",
      remove: "remove",
      history: "history",
      schedule: "schedule",
      imp: "Import",
      editor: {
        caption: "Task",
        submit: "Submit",
        inputs: "Inputs:",
        outputs: "Outputs:"
      },
      events: {
        caption: "History",
        startTime: "Start Time",
        endTime: "End Time",
        acquired: "Acquired",
        created: "Created",
        updated: "Updated",
        failed: "Failed"
      },
      scheduler: {
        type: "Type",
        submit: "Submit"
      },
      removeDialog: {
        title: "Delete task?",
        content: "Please, confirm deletion of the task:<br> ${title}"
      },
      errors: {
        typesLoadingError: "Unable to load trigger types."
      }
    },
    
    processes: {
      caption: "Processes",
      cancel: "cancel",
      min: "min",
      purge: "Purge",
      errors: {
        loading: "Error loading processes",
        canceling: "Error canceling process"
      }
    },
    
    triggers: {
      caption: "Scheduled processes",
      cancel: "cancel",
      errors: {
        loading: "Error loading triggers",
        canceling: "Error canceling trigger"
      }
    }

  }
});
