/*
 * Copyright 2018 Esri, Inc.
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
package com.esri.geoportal.harvester.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Script processor.
 */
/*package*/ class ScriptProcessor {
  private final ObjectMapper mapper = new ObjectMapper();
  private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
  private final String script;
  private Map globals = new HashMap<>();

  /**
   * Creates instance of the script processor.
   * @param script script
   */
  public ScriptProcessor(String script) {
    this.script = script;
  }
  
  /**
   * Process data through the script.
   * @param data data to process
   * @return processed data as JSON string
   * @throws ScriptException if error executing script
   * @throws JsonProcessingException if error transforming JSON data
   */
  public Data process(Data data) throws ScriptException, JsonProcessingException {
    engine.put("globals", globals);
    engine.put("data", data);
    
    engine.eval(script);
    
    globals = (Map)engine.get("globals");
    data = (Data)engine.get("data");
    
    return data;
  }
  
  /**
   * Data
   */
  public static class Data extends HashMap<String, String> {
    public Map json;
    public Map<String,Object> attr;
  }
}
