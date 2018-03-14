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
package com.esri.geoportal.cli;

import com.esri.geoportal.cli.boot.Bootstrap;
import com.esri.geoportal.cli.boot.MemReportManager;
import com.esri.geoportal.harvester.api.base.SimpleIteratorContext;
import com.esri.geoportal.harvester.api.defs.TaskDefinition;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.api.specs.InputBroker.IteratorContext;
import com.esri.geoportal.harvester.engine.services.Engine;
import static com.esri.geoportal.harvester.engine.utils.JsonSerializer.deserialize;
import com.esri.geoportal.harvester.engine.utils.ProcessReference;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

/**
 * Command line application.
 */
public class Application {
  private static final String version = "2.0.0";
  private static final String geometryServiceUrl = "https://utility.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer";

  public static void main(String[] args) {
    Application app = new Application();
    app.execute(args);
  }

  private void execute(String[] args) {
    CommandLineParser parser = new DefaultParser();
    Options options = createOptions();
    
    try {
      CommandLine cli = parser.parse(options, args);
      if (cli.getOptions().length==0 || cli.hasOption('h') || cli.hasOption("v")) {
        if (cli.hasOption("v")) {
          printVersion();
        } else {
          printHeader();
          printHelp(options);
        }
      } else if (cli.hasOption('f')) {
        String fileName = cli.getOptionValue('f');
        File file = new File(fileName);
        try (InputStream inputStream = new FileInputStream(file)) {
          TaskDefinition taskDefinition = deserialize(inputStream,TaskDefinition.class);
          harvest(taskDefinition);
        }
      } else if (cli.hasOption('t')) {
        String sTaskDef = cli.getOptionValue('t');
        TaskDefinition taskDefinition = deserialize(sTaskDef,TaskDefinition.class);
        harvest(taskDefinition);
      } else {
        printHeader();
        printHelp(options);
      }
    } catch (IOException|DataProcessorException|InvalidDefinitionException ex) {
      ex.printStackTrace(System.err);
    } catch (ParseException ex) {
      printHeader();
      printHelp(options);
    }

  }
  
  protected void printHeader() {
    System.out.println(String.format("Harvest ver. %s, Copyright @ 2017 Esri, Inc.", version));
  }
  
  protected void printVersion() {
    System.out.println(String.format("Version: %s", version));
  }
  
  protected void printHelp(Options options) {
    HelpFormatter help = new HelpFormatter();
    help.printHelp("java -jar harvest.jar [options] [-file <file>] | [-task <task definition>]", options);
  }
  
  private Options createOptions() {
    Option help    = new Option("h", "help", false, "print this message");
    Option ver = new Option("v", "version", false, "print the version information and exit");
    Option verbose = new Option("V", "verbose", false, "be extra verbose");
    Option file = new Option("f", "file", true, "executes task defined in the file");
    Option task = new Option("t", "task", true, "executes task defined as JSON");
    
    Options options = new Options();
    options.addOption(help);
    options.addOption(ver);
    options.addOption(verbose);
    options.addOption(file);
    options.addOption(task);
    
    return options;
  }

  protected void harvest(TaskDefinition taskDefinition) throws DataProcessorException, InvalidDefinitionException {
    Bootstrap boot = new Bootstrap(geometryServiceUrl, new MemReportManager());
    Engine engine = boot.createEngine();
    IteratorContext iterCtx = new SimpleIteratorContext();

    ProcessReference processRef = engine.getExecutionService().execute(taskDefinition, iterCtx);
    processRef.getProcess().begin();
  }
}
