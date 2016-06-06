/*
 * Copyright 2016 Esri, Inc.
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
package com.esri.geoportal.harvester.engine.support;

/**
 * CRUDS exception.
 */
public class CrudsException extends Exception {

  /**
   * Creates a new instance of <code>CrudsException</code> without detail
   * message.
   */
  public CrudsException() {
  }

  /**
   * Constructs an instance of <code>CrudsException</code> with the specified
   * detail message.
   *
   * @param msg the detail message.
   */
  public CrudsException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of <code>CrudsException</code> with the specified
   * detail message.
   *
   * @param msg the detail message.
   * @param c cause
   */
  public CrudsException(String msg, Throwable c) {
    super(msg, c);
  }
}
