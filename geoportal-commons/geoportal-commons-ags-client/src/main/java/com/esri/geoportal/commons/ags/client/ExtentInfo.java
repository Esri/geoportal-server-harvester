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
package com.esri.geoportal.commons.ags.client;

/**
 * Extent info.
 */
public final class ExtentInfo {
  public Double xmin;
  public Double ymin;
  public Double xmax;
  public Double ymax;
  public SpatialReferenceInfo spatialReference;
  
  /**
   * Checks if extent is a valid extent.
   * @return <code>true</code> if valid extent.
   */
  public boolean isValid() {
    return isValid(xmin) && isValid(ymin) && isValid(xmax) && isValid(ymax);
  }
  
  private boolean isValid(Double d) {
    return d!=null && !d.isNaN() && !d.isInfinite();
  }
  
  @Override
  public String toString() {
    return String.format("{ \"xmin\": %f, \"ymin\": %f, \"xmax\": %f, \"ymax\": %f, \"spatialReference\": %s}", xmin, ymin, xmax, ymax, spatialReference);
  }
}
