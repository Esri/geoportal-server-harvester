/*
 * Copyright 2019 Esri.
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
package com.esri.geoportal.commons.dcat.client;

import org.apache.commons.lang3.StringUtils;

/**
 * DCAT version
 * @author Esri, Inc.
 */
public class DcatVersion implements Comparable<DcatVersion>{
  public static final DcatVersion DV10 = new DcatVersion(1, 0);
  public static final DcatVersion DV11 = new DcatVersion(1, 1);
  public static final DcatVersion DV201 = new DcatVersion(2, 0, 1);
  private final int major;
  private final int minor;
  private final int patch;
  
  public DcatVersion(int major, int minor) {
    this.major = major;
    this.minor = minor;
    this.patch = 0;
  }

  public DcatVersion(int major, int minor, int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }
  
  public int getPatch() {
    return patch;
  }

  public static DcatVersion parse(String version) {
    String [] parts = StringUtils.trimToEmpty(version).split(".");
    if (parts.length<1 || parts.length>3) {
      throw new IllegalArgumentException("Illegal DCAT version: "+StringUtils.trimToEmpty(version)+".");
    }
    try {
      int major = Integer.parseInt(parts[0]);
      int minor = Integer.parseInt(parts[1]);
      if (parts.length < 3) {
        return new DcatVersion(major, minor);
      }
      else {
        int patch = Integer.parseInt(parts[2]);
        return new DcatVersion(major, minor, patch);  
      }
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Illegal DCAT version: "+StringUtils.trimToEmpty(version)+".", ex);
    }
  }
  
  @Override
  public String toString() {
    return major+"."+minor;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DcatVersion) {
      DcatVersion objVer = (DcatVersion)obj;
      return this.major == objVer.major && this.minor == objVer.minor;
    }
    
    if (obj instanceof String) {
      try {
        DcatVersion objVer = DcatVersion.parse((String) obj);
        return this.major == objVer.major && this.minor == objVer.minor;
      } catch (NumberFormatException ex) {
        return false;
      }
    }
    
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 71 * hash + this.major;
    hash = 71 * hash + this.minor;
    return hash;
  }

  @Override
  public int compareTo(DcatVersion o) {
    if (this.major!=o.major) {
      return new Integer(this.major).compareTo(o.major);
    } else {
      return new Integer(this.minor).compareTo(o.minor);
    }
  }
  
  
}
