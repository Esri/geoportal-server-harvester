 window.dojoConfig = {
        locale: "en",
        parseOnLoad: true,
        packages: [
         {
              name: "esri4",
              location: "https://js.arcgis.com/4.30/esri"
          },
          {
            name: 'hrv',
            location: location.pathname.replace(/\/[^/]*$/, '') + '/hrv'
          }
        ]
      };