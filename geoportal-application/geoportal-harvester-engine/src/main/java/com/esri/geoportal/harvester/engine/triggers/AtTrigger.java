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
import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.TriggerInstance;
import com.esri.geoportal.harvester.api.defs.TriggerInstanceDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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
  public TriggerInstance createInstance(TriggerInstanceDefinition triggerDefinition) throws InvalidDefinitionException {
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
  private class AtTriggerInstance implements TriggerInstance {
    final TriggerInstanceDefinition triggerDefinition;
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
      try {
        Predicate<Date> predicate = parsePredicate(triggerDefinition.getProperties().get(T_AT_TIME));
        schedule(newRunnable(triggerContext,predicate));
      } catch (ParseException ex) {
        throw new InvalidDefinitionException(String.format("Invalid predicate definition: %s", triggerDefinition.getProperties().get(T_AT_TIME)), ex);
      }
    }

    @Override
    public synchronized void close() throws Exception {
      if (future!=null) {
        future.cancel(true);
        future = null;
      }
    }
    
    private Runnable newRunnable(Context triggerContext, Predicate<Date> predicate) {
      return ()->{
        if (predicate.test(new Date())) {
          try {
            ProcessInstance process = triggerContext.submit(triggerDefinition.getTaskDefinition());
            process.addListener(new ProcessInstance.Listener() {
              @Override
              public void onStatusChange(ProcessInstance.Status status) {
                if (status==ProcessInstance.Status.completed && !Thread.currentThread().isInterrupted()) {
                  schedule(newRunnable(triggerContext, predicate));
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
      if (split.length<2) {
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
    
    private Predicate<Date> parsePredicate(String strPredicate) throws ParseException  {
      if (strPredicate==null) {
        throw new ParseException(String.format("Invalid predicate: %s", strPredicate), 0);
      }
      String[] split = strPredicate.split(":");
      ArrayList<Predicate<Date>> predicates = new ArrayList<>();
      
      // day of the week
      if (split.length>=3) {
        String dayOfTheWeek = split[2];
        if (!"*".equals(dayOfTheWeek)) {
          ArrayList<Predicate<Date>> pred = new ArrayList<>();
          Arrays.asList(dayOfTheWeek.split(",")).forEach(str->{
            try {
              int n = Integer.parseInt(str);
              pred.add((d)->{
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                return cal.get(Calendar.DAY_OF_WEEK)==n;
              });
            } catch (NumberFormatException ex) {
              LOG.warn(String.format("Invalid day of the week definition", str), ex);
            }
          });
          predicates.add((d)->pred.stream().map(p->p.test(d)).anyMatch(b->b==true));
        }
      }
      
      // month of the year
      if (split.length>=4) {
        String monthOfTheYear = split[3];
        if (!"*".equals(monthOfTheYear)) {
          ArrayList<Predicate<Date>> pred = new ArrayList<>();
          Arrays.asList(monthOfTheYear.split(",")).forEach(str->{
            try {
              int n = Integer.parseInt(str);
              pred.add((d)->{
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                return cal.get(Calendar.MONTH)==n;
              });
            } catch (NumberFormatException ex) {
              LOG.warn(String.format("Invalid month definition", str), ex);
            }
          });
          predicates.add((d)->pred.stream().map(p->p.test(d)).anyMatch(b->b==true));
        }
      }
      
      return (d)->!predicates.stream().map(p->p.test(d)).anyMatch(b->b==false);
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
