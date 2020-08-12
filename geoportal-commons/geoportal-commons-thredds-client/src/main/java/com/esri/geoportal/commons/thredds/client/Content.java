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
package com.esri.geoportal.commons.thredds.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Content of the catalog.
 */
public class Content {
    public final URL url;
    public final List<Record> records;
    public final List<URL> folders;
    

    /**
     * Creates instance of the content.
     * @param url starting point
     * @param records list of record URL's
     * @param folders list of sub-folders URL's
     */
    public Content(URL url, List<Record> records, List<URL> folders) {
        if (url==null) {
          throw new IllegalArgumentException(String.format("Missing url"));
        }
        this.url = url;
        this.records = records!=null? records: new ArrayList<>();
        this.folders = folders!=null? folders: new ArrayList<>();
    }
    
}
