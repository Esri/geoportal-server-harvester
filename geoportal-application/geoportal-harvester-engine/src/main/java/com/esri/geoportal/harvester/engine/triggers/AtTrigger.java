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
package com.esri.geoportal.harvester.engine.triggers;

import com.esri.geoportal.harvester.api.DataReference;
import com.esri.geoportal.harvester.api.Processor;
import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.defs.TriggerInstanceDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 'At' trigger. Triggers harvesting at the specific time.
 */
public class AtTrigger implements Trigger {
  private static final Logger LOG = LoggerFactory.getLogger(AtTrigger.class);
  public static final String T_AT_TIME = "t-at-time";
  public static final String TYPE = "AT";
  private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1000);
  private static final WeakHashMap<AtTriggerInstance,WeakReference<AtTriggerInstance>> weakMap = new WeakHashMap<>();

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate() {
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.TemporalArgument(T_AT_TIME, "Time", true));
    UITemplate uiTemplate = new UITemplate(getType(), "Harvest at", arguments);
    return uiTemplate;
  }

  @Override
  public Instance createInstance(TriggerInstanceDefinition triggerDefinition) throws InvalidDefinitionException {
    if (!getType().equals(triggerDefinition.getType())) {
      throw new InvalidDefinitionException(String.format("Invalid trigger definition: %s", triggerDefinition));
    }
    AtTriggerInstance instance = new AtTriggerInstance(triggerDefinition);
    weakMap.put(instance, new WeakReference<>(instance));
    return instance;
  }

  @Override
  public void close() throws Exception {
    weakMap.values().stream().map(v->v.get()).forEach(i->{
      try {
        i.close();
      } catch (Exception ex) {
        LOG.warn(String.format("Error closing instance"), ex);
      }
    });
    service.shutdownNow();
  }
  
  /**
   * 'At' trigger instance.
   */
  private class AtTriggerInstance implements Trigger.Instance {
    private final TriggerInstanceDefinition triggerDefinition;
    private ScheduledFuture<?> future;

    /**
     * Creates instance of the trigger instance
     * @param triggerDefinition trigger definition
     */
    public AtTriggerInstance(TriggerInstanceDefinition triggerDefinition) {
      this.triggerDefinition = triggerDefinition;
    }

    @Override
    public TriggerInstanceDefinition getTriggerDefinition() {
      return triggerDefinition;
    }

    @Override
    public void activate(Context triggerContext) throws DataProcessorException, InvalidDefinitionException {
      schedule(newRunnable(triggerContext));
    }

    @Override
    public synchronized void close() throws Exception {
      if (future!=null) {
        future.cancel(true);
        future = null;
      }
    }
    
    private Runnable newRunnable(Context triggerContext) {
      return ()->{
        try {
          Processor.Process process = triggerContext.submit(triggerDefinition.getTaskDefinition());
          process.addListener(new Processor.Listener() {
            @Override
            public void onStatusChange(Processor.Status status) {
              if (status==Processor.Status.completed && !Thread.currentThread().isInterrupted()) {
                schedule(newRunnable(triggerContext));
              }
            }

            @Override
            public void onDataProcessed(DataReference dataReference) {
              // Ignore
            }

            @Override
            public void onError(DataException ex) {
              // Ignore
            }
          });
          process.begin();
        } catch (DataProcessorException|InvalidDefinitionException ex) {
          LOG.error(String.format("Error submitting task"), ex);
        }
      };
    }
    
    private synchronized void schedule(Runnable runnable) {
      try {
        long delay = calcDelay();
        LOG.info(String.format("Task is scheduled to be run in %d minues: %s", delay, triggerDefinition.getTaskDefinition()));
        future = service.schedule(runnable, delay, TimeUnit.MINUTES);
      } catch (ParseException ex) {
        LOG.error(String.format("Error activating trigger: %s", getType()), ex);
      }
    }
    
    /**
     * Calculates delay (in minutes)
     * @return delay
     * @throws ParseException if extracting minute of the day failed
     */
    private long calcDelay() throws ParseException {
      int reqMinOfDay = getMinOfDay();
      Calendar cal = Calendar.getInstance();
      int curMinOfDay = extractMinOfDay(cal);

      int dif = reqMinOfDay - curMinOfDay;
      long delay = dif>0? dif: (24*60)-curMinOfDay+reqMinOfDay; 

      return delay;
    }
    
    /**
     * Gets minute of the day from trigger definition.
     * @return minute of the day.
     * @throws ParseException if extracting minute of the day failed
     */
    private int getMinOfDay() throws ParseException {
      return parseMinOfDay(triggerDefinition.getProperties().get(T_AT_TIME));
    }
    
    /**
     * Parses minute of the day. Format: HH:mm.
     * @param strMinOfDay minute of the day definition
     * @return minute of the day.
     * @throws ParseException if invalid minute of the day definition
     */
    private int parseMinOfDay(String strMinOfDay) throws ParseException {
      if (strMinOfDay==null) {
        throw new ParseException(String.format("Invalid minute of the day: %s", strMinOfDay), 0);
      }
      String[] split = strMinOfDay.split(":");
      if (split.length!=2) {
        throw new ParseException(String.format("Invalid minute of the day: %s", strMinOfDay), 0);
      }
      int hour;
      try {
        hour = Integer.parseInt(split[0]);
      } catch (NumberFormatException ex) {
        throw new ParseException(String.format("Invalid minute of the day: %s", strMinOfDay), 0);
      }
      int min;
      try {
        min = Integer.parseInt(split[1]);
      } catch (NumberFormatException ex) {
        throw new ParseException(String.format("Invalid minute of the day: %s", strMinOfDay), 3);
      }
      return hour*60+min;
    }
    
    /**
     * Gets minute of the day.
     * @param cal calendar
     * @return minute of the day.
     */
    private int extractMinOfDay(Calendar cal) {
      int minOfDay = cal.get(Calendar.HOUR_OF_DAY)*60 + cal.get(Calendar.MINUTE);
      return minOfDay;
    }
  }
}
