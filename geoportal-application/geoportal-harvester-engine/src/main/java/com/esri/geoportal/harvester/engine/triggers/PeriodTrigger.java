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

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.TriggerInstance;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import com.esri.geoportal.harvester.engine.support.BaseProcessInstanceListener;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.time.Instant;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Period trigger. Allows to schedule task periodically, for example: daily or 
 * monthly. . The syntax of the request: 
   <pre><code>
   {
     "type": "PERIOD",
     "properties": {
       "t-period": &lt;period syntax&gt;
     },
     taskDefinition: &lt;task definition&gt;
   }
   </code></pre>
 * Period definition syntax is exactly as defined by {@link java.time.Period} or <a href="https://en.wikipedia.org/wiki/ISO_8601#Durations">https://en.wikipedia.org/wiki/ISO_8601#Durations</a>.
 * <p>
 * Examples:<br>
 * "P1M" - monthly<br>
 * "P1D" - daily<br>
 * "P0.5M" - biweekly<br>
 */
public class PeriodTrigger implements Trigger {
  private static final Logger LOG = LoggerFactory.getLogger(PeriodTrigger.class);
  public static final String T_PERIOD = "t-period";
  public static final String TYPE = "PERIOD";
  private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1000);
  private static final WeakHashMap<PeriodTriggerInstance,WeakReference<PeriodTriggerInstance>> weakMap = new WeakHashMap<>();

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public UITemplate getTemplate() {
    List<UITemplate.Argument> arguments = new ArrayList<>();
    arguments.add(new UITemplate.TemporalArgument(T_PERIOD, "Period", true));
    UITemplate uiTemplate = new UITemplate(getType(), "Harvest periodically", arguments);
    return uiTemplate;
  }

  @Override
  public TriggerInstance createInstance(TriggerDefinition triggerDefinition) throws InvalidDefinitionException {
    if (!getType().equals(triggerDefinition.getType())) {
      throw new InvalidDefinitionException(String.format("Invalid trigger definition: %s", triggerDefinition));
    }
    PeriodTriggerInstance instance = new PeriodTriggerInstance(triggerDefinition);
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
   * Period trigger instance.
   */
  private class PeriodTriggerInstance implements TriggerInstance {
    final TriggerDefinition triggerDefinition;
    private Future<?> future;

    /**
     * Creates instance of the trigger instance
     * @param triggerDefinition trigger definition
     */
    public PeriodTriggerInstance(TriggerDefinition triggerDefinition) {
      this.triggerDefinition = triggerDefinition;
    }

    @Override
    public TriggerDefinition getTriggerDefinition() {
      return triggerDefinition;
    }

    @Override
    public void activate(Context triggerContext) throws DataProcessorException, InvalidDefinitionException {
      schedule(triggerContext.lastHarvest(), newRunnable(triggerContext));
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
          ProcessInstance process = triggerContext.submit(triggerDefinition.getTaskDefinition());
          process.addListener(new BaseProcessInstanceListener() {
            @Override
            public void onStatusChange(ProcessInstance.Status status) {
              if (status==ProcessInstance.Status.completed && !Thread.currentThread().isInterrupted()) {
                schedule(new Date(),newRunnable(triggerContext));
              }
            }
          });
          process.begin();
        } catch (DataProcessorException|InvalidDefinitionException ex) {
          LOG.error(String.format("Error submitting task"), ex);
        }
      };
    }
    
    private synchronized void schedule(Date lastHarvest, Runnable runnable) {
      try {
        if (lastHarvest==null) {
          LOG.info(String.format("Task is being submitted now: %s", triggerDefinition.getTaskDefinition()));
          future = service.submit(runnable);
        } else {
          Period period = parsePeriod(triggerDefinition.getProperties().get(T_PERIOD));
          Calendar cal = Calendar.getInstance();
          cal.setTime(lastHarvest);
          Instant instant = cal.toInstant();
          period.addTo(instant);
          long delay = (cal.getTimeInMillis()-instant.toEpochMilli())/1000/60;
          if (delay>0) {
            LOG.info(String.format("Task is scheduled to be run in %d minues: %s", delay, triggerDefinition.getTaskDefinition()));
            future = service.schedule(runnable, delay, TimeUnit.MINUTES);
          } else {
            LOG.info(String.format("Task is being submitted now: %s", triggerDefinition.getTaskDefinition()));
            future = service.submit(runnable);
          }
        }
      } catch (ParseException ex) {
        LOG.error(String.format("Error activating trigger: %s", getType()), ex);
      }
    }
    
    /**
     * Parses minute of the day. Format: HH:mm.
     * @param strPeriod period
     * @return period
     * @throws ParseException if invalid minute of the day definition
     */
    private Period parsePeriod(String strPeriod) throws ParseException {
      try {
        return Period.parse(strPeriod);
      } catch (DateTimeParseException ex) {
        throw new ParseException(String.format("Invalid period: %s", strPeriod), 0);
      }
    }
  }
}
