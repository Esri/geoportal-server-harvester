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
package com.esri.geoportal.harvester.unc;

import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UNC folder.
 */
/*package*/ class UncFolder {
  private static final Logger LOG = LoggerFactory.getLogger(UncFolder.class);
  private static final FileSystem fileSystem = FileSystems.getDefault();
  private static final String DEFAULT_MATCH_PATTERN = "";
  
  private final UncBroker broker;
  private final Path folder;
  private final String matchPattern;
  private final Date since;

  /**
   * Creates instance of the UNC folder.
   * @param broker broker
   * @param folder folder
   * @param matchPattern match pattern
   * @param since since date
   */
  public UncFolder(UncBroker broker, Path folder, String matchPattern, Date since) {
    this.broker = broker;
    this.folder = folder;
    this.matchPattern = StringUtils.defaultIfBlank(matchPattern, DEFAULT_MATCH_PATTERN);
    this.since = since;
  }

  /**
   * Reads content of the folder.
   * @return content
   * @throws IOException if error reading content
   * @throws URISyntaxException if invalid URL
   */
  public UncFolderContent readContent() throws IOException, URISyntaxException {
    Map<String, UncFile> files = new TreeMap<>(String.CASE_INSENSITIVE_ORDER) {
      @Override
      public UncFile put(String key, UncFile value) {
        // The code below is intended to identify descriptors for binary files,
        // for example if the binary file is some_imgae.jpg then some_image.jpg.xml would be
        // a descriptor for the binary files. If the binary file is found it supersedes binary
        // file and will be published instead
        if (key.toLowerCase().endsWith(".xml")) {
          String orgPath = key.replaceAll("\\.[xX][mM][lL]$", "");
          this.remove(orgPath);
          return super.put(key, value);
        } else {
          String xmlPath = key + ".xml";
          if (!this.containsKey(xmlPath)) {
            return super.put(key, value);
          } else {
            return null;
          }
        }
      }
    };
    List<UncFolder> subFolders = new ArrayList<>();

    Files.list(folder).forEach(f->{
      try {
        if (Files.isSymbolicLink(f)) {
          f = f.toRealPath();
        }
        if (Files.isDirectory(f)) {
          subFolders.add(new UncFolder(broker, f, matchPattern, since));
        } else if (Files.isRegularFile(f) && (StringUtils.isBlank(matchPattern) || multiMatchFileName(f, matchPattern))) {
          files.put(f.toString(), new UncFile(broker, f));
        }
      } catch (IOException ex) {
        LOG.warn(formatForLog("Error processing path element: %s", f), ex);
      }
    });
    
    LOG.debug(formatForLog("UNC FILES in %s: %s",folder,files.values().toString()));
    LOG.debug(formatForLog("UNC SUBFOLDERS in %s: %s",folder,subFolders.toString()));
    
    return new UncFolderContent(this, subFolders, files.values().stream().filter(uncFile -> {
      if (since == null) return true;
      
      Path fPath = uncFile.file;
      ArrayList<Long> allTimes = new ArrayList<>();
      collectTime(allTimes, fPath, "lastModifiedTime");
      collectTime(allTimes, fPath, "lastAccessTime");
      collectTime(allTimes, fPath, "creationTime");
      
      long latestTime = max(allTimes);
      
      return latestTime >= since.getTime();
    }).collect(Collectors.toList()));
  }
  
  private void collectTime(ArrayList<Long> allTimes, Path fPath, String timeName) {
    try {
      FileTime fileTime = (FileTime) Files.getAttribute(fPath, timeName);
      if (fileTime != null) {
        allTimes.add(fileTime.toMillis());
      }
    } catch (Exception ex) {
      // It's OK to ignore it.
    }
  }
  private long max(List<Long> attrs) {
    Long max = null;
    for (long attr : attrs) {
      if (max == null)
        max = attr;
      else
        max = Math.max(max, attr);
    }
    return max;
  }

  @Override
  public String toString() {
    return folder.toString();
  }
  
  /**
   * Matches file
   * @param file file
   * @param patterns comma separated match patterns (glob)
   * @return <code>true</code> if URL matches the pattern
   */
  private boolean multiMatchFileName(Path path, String patterns) {
    return Arrays.stream(patterns.split(","))
            .map(pattern -> StringUtils.trimToEmpty(pattern))
            .anyMatch(pattern -> this.matchFileName(path, pattern));
  }
  
  /**
   * Matches file
   * @param file file
   * @param pattern match pattern (glob)
   * @return <code>true</code> if URL matches the pattern
   */
  private boolean matchFileName(Path path, String pattern) {
    PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:"+pattern);
    // noXmlPath is a path to the file with potiential '.xm' suffix being removed
    // this is to assure that binary file descriptors are still being matched, for example:
    // if the pattern is **.jpg, then both 'some_image.jpg' and 'some_image.jpg.xml' will match.
    Path noXmlPath = Path.of(path.toString().replaceAll("\\.[xX][mM][lL]$", ""));
    return pathMatcher.matches(path) || pathMatcher.matches(noXmlPath);
  }
  
}
