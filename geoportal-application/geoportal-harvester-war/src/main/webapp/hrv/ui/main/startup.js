 require(["hrv"],function() {
      require(["dojo/ready","hrv/ui/main/App"],
      function(ready,App) {
        ready(function() {
          var app = new App({},"hrv");
          app.startup();
        });
      });
    });