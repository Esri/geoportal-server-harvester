# geoportal-server-harvester
As part of the evolution of Geoportal Server, the harvesting capability has been separated into its own module. This because there are use cases where the harvesting can be used in with catalogs of content (for example ArcGIS Online) without the Geoportal Server's catalog. This repository thus contains the harvesting capability, while it's sibling [geoportal-server-catalog](https://github.com/ArcGIS/geoportal-server-catalog) is the new catalog of Geoportal Server.

## Features

* Support for harvesting WAF and CSW repositories
* Publication into Geoportal Server catalog or a local folder
* Extensibility

## Instructions

Building the source code:

* Run 'mvn clean install'

Deploying war file:

* Deploy 'geoportal-application\geoportal-harvester-war\target\geoportal-harvester-war-&lt;version&gt;.war' into the web server of your choice.

## Requirements

* Java JDK 1.8 or higher
* Apache Tomcat 8 or higher

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2016 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [LICENSE](LICENSE?raw=true) file.
