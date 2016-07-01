/*
 * Copyright 2016 Esri, Inc..
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

/**
 * Harvester API. 
 * Provides basic definitions of artifact used during data harvest.
 * <p>
 * {@link com.esri.geoportal.harvester.api.Connector} - artifact which knows how 
 * to conduct communication (either harvesting or publishing). It also provides
 * a way to create instance of the {@link com.esri.geoportal.harvester.api.Broker}.
 * There are two types of connectors: {@link com.esri.geoportal.harvester.api.specs.InputConnector}
 * and {@link com.esri.geoportal.harvester.api.specs.OutputConnector}.
 * <p>
 * {@link com.esri.geoportal.harvester.api.Broker} - artifact which knows how to
 * conduct communication with the particular end point. It could be though as a 
 * concrete instance of the {@link com.esri.geoportal.harvester.api.Connector}.
 * Most typically, broker is bound to the particular URL or URI.
 * There are two types of connectors: {@link com.esri.geoportal.harvester.api.specs.InputBroker}
 * and {@link com.esri.geoportal.harvester.api.specs.OutputBroker}.
 * <p>
 * {@link com.esri.geoportal.harvester.api.Processor} - artifact which knows how 
 * to execute harvesting process. In the most basic implementation it will iterate
 * through input data using {@link com.esri.geoportal.harvester.api.specs.InputConnector}
 * iteration pattern methods and push each data to the output using 
 * {@link com.esri.geoportal.harvester.api.specs.OutputConnector} interface.
 * <p>
 * {@link com.esri.geoportal.harvester.api.ProcessInstance} - instance of the
 * harvesting process. In the most basic implementation it allows to start harvesting
 * and abort harvesting if needed. It is associated with a single 
 * {@link com.esri.geoportal.harvester.api.specs.InputBroker} and one or more
 * {@link com.esri.geoportal.harvester.api.specs.OutputBroker}'s.
 * <p>
 * {@link com.esri.geoportal.harvester.api.Trigger} - artifact providing a general 
 * way to launch harvesting process.
 * <p>
 * {@link com.esri.geoportal.harvester.api.TriggerInstance} - an instance of the 
 * trigger associated with the single process definition.
 * <p>
 * {@link com.esri.geoportal.harvester.api.defs} - package containing serialzable
 * definitions.
 * <p>
 * {@link com.esri.geoportal.harvester.api.ex} - package containing exceptions
 * structure.
 */
package com.esri.geoportal.harvester.api;
