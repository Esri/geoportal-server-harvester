/*
 * Copyright 2020 Esri, Inc.
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
package com.esri.geoportal.commons.csw.client.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Stream opener.
 */
public interface StreamOpener {
  InputStream open(String path) throws IOException;
  
  StreamOpener RESOURCE_OPENER = path -> {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
  };
  
  StreamOpener FILE_OPENER = path -> {
    File file = path.startsWith("~")?
      new File(System.getProperty("user.home"), path.substring(1)):
      new File(path);
    return new FileInputStream(file);
  };
}
