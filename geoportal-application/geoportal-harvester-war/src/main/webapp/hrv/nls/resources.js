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
      open: "Open",
      all: "All"
    },

    periodical: {
      hourly: "Hourly",
      daily: "Daily",
      weekly: "Weekly",
      biweekly: "Bi-weekly",
      monthly: "Monthly",
    },

    header: {
      caption: "Harvester",
      logout: "Log out",
    },

    navigation: {
      home: "Home",
      connectors: "Connectors",
      brokers: "Brokers",
      tasks: "Tasks",
    },

    brokers: {
      groupBy: "Group by type",
      input: "Input brokers",
      output: "Output brokers",
      edit: "Edit",
      remove: "Remove",
      add: "Add",
      hint: "Hint",
      editor: {
        caption: "Broker",
        type: "Type:",
        title: "Title:",
        submit: "Submit",
      },
      removeDialog: {
        title: "Delete adaptor?",
        content: "Please, confirm deletion of the broker:<br>${title}",
      },
      errors: {
        inbound: "Error loading inbound connectors",
        outbound: "Error loading outbound connectors",
        access: "Unable to access brokers information",
        creating: "Error creating broker",
        removing: "Error removing broker",
        schedule: "Unable to schedule task",
      },
    },

    tasks: {
      caption: "Tasks",
      groupBy: "Group by input",
      add: "Add",
      run: "Run",
      rename: "Rename",
      export: "Export",
      remove: "Delete",
      history: "History",
      schedule: "Schedule",
      imp: "Import",
      running: "Running",
      inputFilterSelect: "Input:",
      outputFilterSelect: "Output:",
      editor: {
        caption: "Task",
        submit: "Submit",
        inputs: "Inputs:",
        outputs: "Outputs:",
        name: "Name:",
      },
      renamer: {
        caption: "Rename",
        submit: "Submit",
        cancel: "Cancel",
      },
      events: {
        caption: "History",
        startTime: "Start Time",
        endTime: "End Time",
        acquired: "Acquired",
        created: "Created",
        updated: "Updated",
        failed: "Failed (in/out)",
      },
      scheduler: {
        type: "Type",
        submit: "Submit",
      },
      removeDialog: {
        title: "Delete task?",
        content: "Please, confirm deletion of the task:<br>${title}",
      },
      runDialog: {
        title: "Run task?",
        content: "Please, confirm executing task:<br>${title}",
        ignoreRobots: "Ignore robots.txt",
        incremental: "Incremental",
      },
      errors: {
        typesLoadingError: "Unable to load trigger types.",
        accessFialed: "Unable to access failed documents information",
        accessHistory: "Unable to access history information",
        accessInfo: "Unable to access tasks information",
        triggersDelete: "Unable to delete current task triggers",
        readScheduling: "Unable to read scheduling",
        rename: "Unable to rename task",
        remove: "Error removing task",
        execute: "Error executing task",
        create: "Error creating task",
      },
    },

    processes: {
      caption: "Processes",
      cancel: "Cancel",
      min: "min",
      purge: "Clear Completed",
      history: "History",
      started: "Started",
      finished: "Finished",
      errors: {
        loading: "Error loading processes",
        canceling: "Error canceling process",
      },
    },

    triggers: {
      caption: "Scheduled processes",
      cancel: "Cancel",
      removeDialog: {
        title: "Cancel scheduled process?",
        content: "Please, confirm cancelation of the scheduled process:<br>${title}",
      },
      runsAt: "Runs at: ${time}",
      runsEvery: "Runs: ${period}",
      errors: {
        loading: "Error loading triggers",
        canceling: "Error canceling trigger",
      },
    },
  },
});
