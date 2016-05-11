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
package com.esri.geoportal.harvester.api.base;

import com.esri.geoportal.harvester.api.DataReference;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

/**
 * Data reference serializer.
 */
public class DataReferenceSerializer {

  private final static Base64.Encoder ENCODER = Base64.getEncoder();
  private final static Base64.Decoder DECODER = Base64.getDecoder();
  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  /**
   * Serializes data reference
   *
   * @param out output print stream
   * @param ref data reference
   * @throws IOException if serialization fails
   */
  public void serialize(PrintStream out, DataReference ref) throws IOException {

    byte[] bSourceUri = ENCODER.encode(ref.getSourceUri().toASCIIString().getBytes("UTF-8"));
    byte[] bLastModifiedDate = ENCODER.encode((ref.getLastModifiedDate() != null ? formatIsoDate(ref.getLastModifiedDate()) : "").getBytes("UTF-8"));
    byte[] bContent = ENCODER.encode(ref.getContent());

    out.write(bSourceUri);
    out.write(',');
    out.write(bLastModifiedDate);
    out.write(',');
    out.write(bContent);
    out.write('\r');
    out.write('\n');
    out.flush();
  }

  /**
   * De-serializes data reference
   *
   * @param input input stream
   * @return data reference
   * @throws IOException if de-serialization fails
   * @throws URISyntaxException if de-serialization fails
   */
  public DataReference deserialize(InputStream input) throws IOException, URISyntaxException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
    String line = reader.readLine();
    if (line != null) {
      String[] split = line.split(",");
      if (split.length == 3) {
        byte[] bSourceUri = DECODER.decode(split[0].getBytes("UTF-8"));
        byte[] bLastModifiedDate = DECODER.decode(split[1].getBytes("UTF-8"));
        byte[] bContent = DECODER.decode(split[2].getBytes("UTF-8"));

        URI sSourceUri = URI.create(new String(bSourceUri, "UTF-8"));
        String sLastModifiedDate = new String(bLastModifiedDate, "UTF-8");

        Date lastModifiedDate = !sLastModifiedDate.isEmpty() ? Date.from(OffsetDateTime.from(FORMATTER.parse(sLastModifiedDate)).toInstant()) : null;

        return new SimpleDataReference(sSourceUri, lastModifiedDate, bContent);
      }
    }
    return null;
  }

  private String formatIsoDate(Date date) {
    Instant instant = date.toInstant();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    ZoneOffset zoneOffset = ZoneOffset.ofHours(cal.getTimeZone().getRawOffset() / (1000 * 60 * 60));
    OffsetDateTime ofInstant = OffsetDateTime.ofInstant(instant, zoneOffset);
    return FORMATTER.format(ofInstant);
  }
}
