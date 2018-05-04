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
package com.esri.geoportal.harvester.sink;

import com.esri.geoportal.harvester.api.base.SimpleDataReference;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.constants.MimeTypeUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import org.apache.commons.io.IOUtils;

/**
 * Sink file.
 */
/*package*/ class SinkFile {
  private final SinkBroker broker;
  private final Path file;

  /**
   * Creates instance of Sink file.
   * @param broker broker
   * @param file file
   */
  public SinkFile(SinkBroker broker, Path file) {
    this.broker = broker;
    this.file = file;
  }

  /**
   * Reads content.
   * @return content reference
   * @throws IOException if reading content fails
   * @throws URISyntaxException if file url is an invalid URI
   */
  public SimpleDataReference readContent() throws IOException, URISyntaxException {
    Date lastModifiedDate = readLastModifiedDate();
    MimeType contentType = readContentType();
    try (InputStream input = attemptToOpenStream(5, 1000);) {
      SimpleDataReference ref = new SimpleDataReference(broker.getBrokerUri(), broker.getEntityDefinition().getLabel(), file.toAbsolutePath().toString(), lastModifiedDate, file.toUri(), broker.td.getSource().getRef(), broker.td.getRef());
      ref.addContext(contentType, IOUtils.toByteArray(input));
      return ref;
    } finally {
      // once file is read, delete it
      attemptToDeleteFile(5, 1000);
    }
  }
  
  /**
   * Attempts to open a file
   * @param attempts number of attempts
   * @param mills delay between consecutive attempts
   * @return input stream
   * @throws IOException if all attempts fail
   */
  private InputStream attemptToOpenStream(int attempts, long mills) throws IOException {
    while (true) {
      attempts--;
      try {
        // it should be open at the first attempt
        return new FileInputStream(file.toFile());
      } catch (FileNotFoundException ex) {
        // if not check if maximum number of attempts has been exhausted...
        if (attempts <= 0) {
          // ...then throw exceptiion
          throw ex;
        }
        // otherwise wait and try again latter
        try {
          Thread.sleep(mills);
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }
  }
  
  /**
   * Attempts to delete file
   * @param attempts number of attempts
   * @param mills delay between consecutive attempts
   * @throws IOException if all attempts fail
   */
  private void attemptToDeleteFile(int attempts, long mills) throws IOException {
    while (true) {
      attempts--;
      try {
        Files.delete(file);
        return;
      } catch (FileSystemException ex) {
        // if not check if maximum number of attempts has been exhausted...
        if (attempts <= 0) {
          // ...then throw exceptiion
          throw ex;
        }
        // otherwise wait and try again latter
        try {
          Thread.sleep(mills);
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }
  }

  /**
   * Reads last modified date.
   * @return last modified date
   */
  private Date readLastModifiedDate() throws IOException {
    return new Date(Files.getLastModifiedTime(file).toMillis());
  }
  
  /**
   * Reads content type.
   * @return content type or <code>null</code> if unable to read content type
   */
  private MimeType readContentType() {
    try {
      String strFileUrl = file.toAbsolutePath().toString();
      int lastDotIndex = strFileUrl.lastIndexOf(".");
      String ext = lastDotIndex>=0? strFileUrl.substring(lastDotIndex+1): "";
      return MimeTypeUtils.mapExtension(ext);
    } catch (Exception ex) {
      return null;
    }
  }
  
  @Override
  public String toString() {
    return file.toString();
  }
}
