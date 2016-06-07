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
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.ex.DataException;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;
import java.text.ParseException;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Period trigger.
 */
public class PeriodTrigger implements Trigger {
  private static final Logger LOG = LoggerFactory.getLogger(PeriodTrigger.class);
  public static final String T_PERIOD = "t-period";
  public static final String TYPE = "PERIOD";
  public static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1000);

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
  public Instance createInstance(TriggerDefinition triggerDefinition) throws InvalidDefinitionException {
    if (!getType().equals(triggerDefinition.getType())) {
      throw new InvalidDefinitionException(String.format("Invalid trigger definition: %s", triggerDefinition));
    }
    return new PeriodTriggerInstance(triggerDefinition);
  }

  @Override
  public void close() throws Exception {
    service.shutdownNow();
  }

  /**
   * Period trigger instance.
   */
  private class PeriodTriggerInstance implements Trigger.Instance {
    private final TriggerDefinition triggerDefinition;
    private ScheduledFuture<?> future;

    /**
     * Creates instance of the trigger instance
     * @param triggerDefinition trigger definition
     */
    public PeriodTriggerInstance(TriggerDefinition triggerDefinition) {
      this.triggerDefinition = triggerDefinition;
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
        long rate = calcRate();
        future = service.scheduleAtFixedRate(runnable, 0, rate, TimeUnit.MINUTES);
      } catch (ParseException ex) {
        LOG.error(String.format("Error activating trigger: %s", getType()), ex);
      }
    }
    
    /**
     * Calculates rate (in minutes)
     * @return delay
     * @throws ParseException if extracting minute of the day failed
     */
    private long calcRate() throws ParseException {
      return parsePeriod(triggerDefinition.getArguments().get(T_PERIOD));
    }
    
    /**
     * Parses minute of the day. Format: HH:mm.
     * @param strPeriod period
     * @return minute of the day.
     * @throws ParseException if invalid minute of the day definition
     */
    private int parsePeriod(String strPeriod) throws ParseException {
      try {
        Period period = Period.parse(strPeriod);
        int days = period.getDays();
        return days*24*60;
      } catch (DateTimeParseException ex) {
        throw new ParseException(String.format("Invalid period: %s", strPeriod), 0);
      }
    }
  }
}
