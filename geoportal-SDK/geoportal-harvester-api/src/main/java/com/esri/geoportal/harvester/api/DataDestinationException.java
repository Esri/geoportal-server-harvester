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
 * Data destination exception.
 * <p>
 * Exception associated with destination.
 */
public class DataDestinationException extends DataException {
  private final DataDestination destination;

  /**
   * Gets data destination.
   * @return data destination
   */
  public DataDestination getDestination() {
    return destination;
  }

  /**
   * Creates a new instance of <code>DataDestinationException</code> without
   * detail message.
   * @param destination destination
   */
  public DataDestinationException(DataDestination destination) {
    this.destination = destination;
  }

  /**
   * Constructs an instance of <code>DataDestinationException</code> with the
   * specified detail message.
   *
   * @param destination destination
   * @param msg the detail message.
   */
  public DataDestinationException(DataDestination destination, String msg) {
    super(msg);
    this.destination = destination;
  }

  /**
   * Constructs an instance of <code>DataDestinationException</code> with the
   * specified detail message.
   *
   * @param destination destination
   * @param msg the detail message.
   * @param t cause
   */
  public DataDestinationException(DataDestination destination, String msg, Throwable t) {
    super(msg,t);
    this.destination = destination;
  }
}
