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
 * Data process exception.
 */
public class DataProcessException extends DataException {

  /**
   * Creates a new instance of <code>DataProcessException</code> without detail
   * message.
   */
  public DataProcessException() {
  }

  /**
   * Constructs an instance of <code>DataProcessException</code> with the
   * specified detail message.
   *
   * @param msg the detail message.
   */
  public DataProcessException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of <code>DataProcessException</code> with the
   * specified detail message.
   *
   * @param msg the detail message.
   * @param t cause
   */
  public DataProcessException(String msg, Throwable t) {
    super(msg,t);
  }
}
