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
package com.esri.geoportal.harvester.api.ex;

import com.esri.geoportal.harvester.api.specs.OutputBroker;

/**
 * Data output exception.
 * <p>
 * Exception associated with output broker.
 */
public class DataOutputException extends DataException {
  private final OutputBroker outputBroker;
  private final String dataId;

  /**
   * Gets data output.
   * @return data output
   */
  public OutputBroker getOutputBroker() {
    return outputBroker;
  }

  /**
   * Gets data id.
   * @return data id
   */
  public String getDataId() {
    return dataId;
  }

  /**
   * Constructs an instance of <code>DataOutputException</code> with the
   * specified detail message.
   *
   * @param outputBroker output
   * @param dataId data id
   * @param msg the detail message.
   */
  public DataOutputException(OutputBroker outputBroker, String dataId, String msg) {
    super(msg);
    this.dataId = dataId;
    this.outputBroker = outputBroker;
  }

  /**
   * Constructs an instance of <code>DataOutputException</code> with the
   * specified detail message.
   *
   * @param outputBroker output
   * @param dataId data id
   * @param msg the detail message.
   * @param t cause
   */
  public DataOutputException(OutputBroker outputBroker, String dataId, String msg, Throwable t) {
    super(msg,t);
    this.dataId = dataId;
    this.outputBroker = outputBroker;
  }
}
