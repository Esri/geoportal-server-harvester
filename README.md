# geoportal-server-harvester
As part of the evolution of Geoportal Server, the harvesting capability has been separated into its own module. This is because there are use cases where the harvesting can be used as a stand-alone broker between catalogs of content. 

This repository thus contains the harvesting capability, while it's sibling [geoportal-server-catalog](https://github.com/Esri/geoportal-server-catalog) is the new catalog of Geoportal Server.

For details about geoportal server harvester, please visit the [wiki](https://github.com/Esri/geoportal-server-harvester/wiki).

To report an issue, please go to [issues](https://github.com/Esri/geoportal-server-harvester/issues).

## Releases and Downloads
- 2.6.1 - Released November 13, 2018, click [here](https://github.com/Esri/geoportal-server-harvester/releases) for release notes and downloads. 

## Features

* Please visit [Features](https://github.com/Esri/geoportal-server-harvester/wiki/Features).

## Instructions

Building the source code:

* Run 'mvn clean install'

Deploying war file:

* Deploy 'geoportal-application\geoportal-harvester-war\target\geoportal-harvester-war-&lt;version&gt;.war' into the web server of your choice.
* No configuration required.

## Requirements

* Java JDK 11 (preferred: [AdoptOpenJDK 11](https://adoptopenjdk.net/))
* Apache Tomcat 9.x

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

A copy of the license is available in the repository's [LICENSE](LICENSE.txt) file.
