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

import com.esri.geoportal.harvester.api.specs.InputBroker;

/**
 * Input broker exception.
 * <p>
 * Exception associated with the input broker.
 */
public class DataInputException extends DataException {
  private final InputBroker inputBroker;

  /**
   * Gets input broker.
   * @return input broker
   */
  public InputBroker getInputBroker() {
    return inputBroker;
  }

  /**
   * Creates a new instance of <code>DataInputException</code> without detail
   * message.
   * @param inputBroker input broker
   */
  public DataInputException(InputBroker inputBroker) {
    this.inputBroker = inputBroker;
  }

  /**
   * Constructs an instance of <code>DataInputException</code> with the
   * specified detail message.
   *
   * @param inputBroker input broker
   * @param msg the detail message.
   */
  public DataInputException(InputBroker inputBroker, String msg) {
    super(msg);
    this.inputBroker = inputBroker;
  }

  /**
   * Constructs an instance of <code>DataInputException</code> with the
   * specified detail message.
   *
   * @param inputBroker input broker
   * @param msg the detail message.
   * @param t cause
   */
  public DataInputException(InputBroker inputBroker, String msg, Throwable t) {
    super(msg,t);
    this.inputBroker = inputBroker;
  }
}
