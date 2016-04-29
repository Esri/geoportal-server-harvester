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
package com.esri.geoportal.harvester.api;

/**
 * Data input exception.
 * <p>
 * Exception associated with the input.
 */
public class DataInputException extends DataException {
  private final DataInput dataInput;

  /**
   * Gets data input.
   * @return data input
   */
  public DataInput getDataSource() {
    return dataInput;
  }

  /**
   * Creates a new instance of <code>DataInputException</code> without detail
   * message.
   * @param dataInput data input
   */
  public DataInputException(DataInput dataInput) {
    this.dataInput = dataInput;
  }

  /**
   * Constructs an instance of <code>DataInputException</code> with the
   * specified detail message.
   *
   * @param dataInput data input
   * @param msg the detail message.
   */
  public DataInputException(DataInput dataInput, String msg) {
    super(msg);
    this.dataInput = dataInput;
  }

  /**
   * Constructs an instance of <code>DataInputException</code> with the
   * specified detail message.
   *
   * @param dataInput data input
   * @param msg the detail message.
   * @param t cause
   */
  public DataInputException(DataInput dataInput, String msg, Throwable t) {
    super(msg,t);
    this.dataInput = dataInput;
  }
}
