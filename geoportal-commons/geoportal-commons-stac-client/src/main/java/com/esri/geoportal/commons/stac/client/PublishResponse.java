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
package com.esri.geoportal.commons.stac.client;

/**
 * GPT 2.0 PublishResponse.
 */
public class PublishResponse {
  private String id;
  private String status;
  private Error error;

  /**
   * Gets document id.
   * @return document id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets document id.
   * @param id document id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets status information.
   * @return status information
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets status information.
   * @param status status information
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Gets error if any.
   * @return error or <code>null</code> if no error
   */
  public Error getError() {
    return error;
  }

  /**
   * Sets error.
   * @param error error or <code>null</code> if no error
   */
  public void setError(Error error) {
    this.error = error;
  }
  
  @Override
  public String toString() {
    return String.format("RESPONSE :: status: %s, error: %s", status, error);
  }
  
  /**
   * Error information.
   */
  public static class Error {
    private String message;

    /**
     * Gets error message.
     * @return error message
     */
    public String getMessage() {
      return message;
    }

    /**
     * Sets error message.
     * @param message error message
     */
    public void setMessage(String message) {
      this.message = message;
    }
    
    @Override
    public String toString() {
      return String.format("message: %s", message);
    }
  }
}
