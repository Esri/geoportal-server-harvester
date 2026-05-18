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

import static com.esri.geoportal.commons.utils.CrlfUtils.formatForLog;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.owasp.esapi.ESAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.geoportal.harvester.api.ProcessInstance;
import com.esri.geoportal.harvester.api.Trigger;
import com.esri.geoportal.harvester.api.TriggerInstance;
import com.esri.geoportal.harvester.api.base.BaseProcessInstanceListener;
import com.esri.geoportal.harvester.api.defs.TriggerDefinition;
import com.esri.geoportal.harvester.api.defs.UITemplate;
import com.esri.geoportal.harvester.api.defs.UITemplate.Choice;
import com.esri.geoportal.harvester.api.ex.DataProcessorException;
import com.esri.geoportal.harvester.api.ex.InvalidDefinitionException;


/**
 * 'At' trigger. Triggers harvesting at the specific time.
 * <p>
 * Trigger allow to schedule to run a task not only at a predetermined time, but
 * allows to add some temporal constraints, for example: it could be trigger at
 * a particular hour of the day, but only Mondays and Thursdays, every second
 * week of the month, but only in January and June. The syntax of the request:
   <pre><code>
   {
     "type": "AT",
     "properties": {
       "t-at-time": &lt;time definition syntax&gt;
     },
     taskDefinition: &lt;task definition&gt;
   }
   </code></pre>
 * Time definition syntax loosely follows "crontab" syntax:<br>
 * &lt;hour&gt;:&lt;minute&gt;[&lt;day of the week&gt;[:&lt;week of the month&gt;[:&lt;month of the year&gt;]]]
 * <p>
 * hour - numerical value of the hour of the day (24H clock)
 * minute - numerical value of the minute of the hour
 * day of the week - numerical index of the day of the week; Sunday=1, Monday=2, and so on
 * week of the month - numerical index of the week of the month; first week of the month has value 1
 * month of the year - numerical index of the month of the year; January=0, February=2, and so on
 * <p>
 * day of the week, week of the month, month of the year are optional. Any of
 * this parts could be substituted with asterisk (*) to skip condition.
 * <p>
 * Examples:<br>
 * "04:00" - scheduled at 4:00 AM every day<br>
 * "16:00" - scheduled at 4:00 PM every day<br>
 * "02:30:2" - scheduled at 2:30 AM every Monday<br>
 * "03:30:2,4:2,4" - scheduled at 3:30 AM every Monday and Wednesday, but only on second and fourth week of the month<br>
 * "05:00:1:*:0" - scheduled at 5:00 AM every Sunday in January regardless of the week<br>
 * "12:00:1:1:1" - scheduled at noon first day of the year<br>
 */
public class AtTrigger implements Trigger {

    private static final Logger LOG = LoggerFactory.getLogger(AtTrigger.class);
    public static final String T_AT_TIME = "t-at-time";
    public static final String T_AT_DAY = "t-at-day";
    public static final String TYPE = "AT";
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1000);
    private static final WeakHashMap<AtTriggerInstance, WeakReference<AtTriggerInstance>> weakMap = new WeakHashMap<>();

    @Override
    public String getType() {
        return TYPE;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public UITemplate getTemplate(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("EngineResource", locale);
        List<UITemplate.Argument> arguments = new ArrayList<>();
        arguments.add(new UITemplate.TemporalArgument(T_AT_TIME, bundle.getString("engine.triggers.at.time"), true));
        Map<String,String> dayOfWeek = new LinkedHashMap<>();
        dayOfWeek.put("1", bundle.getString("engine.triggers.at.day1"));
        dayOfWeek.put("2", bundle.getString("engine.triggers.at.day2"));
        dayOfWeek.put("3", bundle.getString("engine.triggers.at.day3"));
        dayOfWeek.put("4", bundle.getString("engine.triggers.at.day4"));
        dayOfWeek.put("5", bundle.getString("engine.triggers.at.day5"));
        dayOfWeek.put("6", bundle.getString("engine.triggers.at.day6"));
        dayOfWeek.put("7", bundle.getString("engine.triggers.at.day7")); 
        dayOfWeek.put("1,2,3,4,5,6,7", bundle.getString("engine.triggers.at.dayAll"));       
       
        
		Choice<String>[] choices = dayOfWeek.entrySet()
        .stream()
        .map(e -> new Choice<>(e.getKey(), e.getValue()))
        .toArray(Choice[]::new);
        arguments.add(new UITemplate.ChoiceArgument(T_AT_DAY, bundle.getString("engine.triggers.at.day"), Arrays.asList(choices)){
            @SuppressWarnings("unused")
			public String getDefault() {
              return "1";
            }
        });
       
        UITemplate uiTemplate = new UITemplate(getType(), bundle.getString("engine.triggers.at"), arguments);
        return uiTemplate;
    }

    @Override
    public TriggerInstance createInstance(TriggerDefinition triggerDefinition) throws InvalidDefinitionException {
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
            i.deactivate();
        });
        service.shutdownNow();
    }

    /**
     * 'At' trigger instance.
     */
    private class AtTriggerInstance implements TriggerInstance {

        final TriggerDefinition triggerDefinition;
        private ScheduledFuture<?> future;

        /**
         * Creates instance of the trigger instance
         *
         * @param triggerDefinition trigger definition
         */
        public AtTriggerInstance(TriggerDefinition triggerDefinition) {
            this.triggerDefinition = triggerDefinition;
        }

        @Override
        public TriggerDefinition getTriggerDefinition() {
            return triggerDefinition;
        }

        @Override
        public void activate(Context triggerContext) throws DataProcessorException, InvalidDefinitionException {
            try {
                String time =  triggerDefinition.getProperties().get(T_AT_TIME);
                String day = triggerDefinition.getProperties().get(T_AT_DAY);
               
                Predicate<Date> predicate = parsePredicate(time+":"+day);
                //   schedule(newRunnable(triggerContext,predicate));

                long delay = calcDelayToNextMatch(predicate);
                LOG.info(ESAPI.encoder().encodeForHTML(String.format("Task is --new code now scheduled to be run in %d minues: %s", delay, triggerDefinition.getTaskDefinition())));
                future = service.schedule(
                        newRunnable(triggerContext, predicate),
                        delay,
                        TimeUnit.MINUTES
                );

            } catch (ParseException ex) {
                throw new InvalidDefinitionException(String.format("Invalid predicate definition: %s", triggerDefinition.getProperties().get(T_AT_TIME)), ex);
            }
        }

        @Override
        public synchronized void deactivate() {
            if (future != null) {
                future.cancel(true);
                future = null;
            }
        }

        private Runnable newRunnable(Context triggerContext, Predicate<Date> predicate) {
            return () -> {
                if (predicate.test(new Date())) {
                    try {
                        ProcessInstance process = triggerContext.execute(triggerDefinition.getTaskDefinition());
                        process.addListener(new BaseProcessInstanceListener() {
                            @Override
                            public void onStatusChange(ProcessInstance.Status status) {
                                if (status == ProcessInstance.Status.completed && !Thread.currentThread().isInterrupted()) {
                                    //schedule(newRunnable(triggerContext, predicate));

                                    long delay = calcDelayToNextMatch(predicate);
                                    LOG.info(ESAPI.encoder().encodeForHTML(String.format("Task is now scheduled to be run in %d minues: %s", delay, triggerDefinition.getTaskDefinition())));

                                    future = service.schedule(
                                            newRunnable(triggerContext, predicate),
                                            delay,
                                            TimeUnit.MINUTES
                                    );

                                }
                            }
                        });
                        process.begin();
                    } catch (DataProcessorException | InvalidDefinitionException ex) {
                        LOG.error(String.format("Error submitting task"), ex);
                    } catch (TimeoutException ex) {
                        LOG.error(String.format("Error submitting task"), ex);
                    } catch (ExecutionException ex) {
                        LOG.error(String.format("Error submitting task"), ex);
                    } catch (InterruptedException ex) {
                        LOG.error(String.format("Error submitting task"), ex);
                    }
                } else {
                    //schedule(newRunnable(triggerContext, predicate));

                    long delay = calcDelayToNextMatch(predicate);
                    LOG.info(ESAPI.encoder().encodeForHTML(String.format("Task is --new code now scheduled to be run in %d minues: %s", delay, triggerDefinition.getTaskDefinition())));
                    future = service.schedule(
                            newRunnable(triggerContext, predicate),
                            delay,
                            TimeUnit.MINUTES
                    );

                }
            };
        }

        private int extractRequiredMinOfDay() throws ParseException {
            String str = triggerDefinition.getProperties().get(T_AT_TIME);
            return parseMinOfDay(str);
        }

        private long calcDelayToNextMatch(Predicate<Date> predicate) {
            Calendar now = Calendar.getInstance();
            Calendar cal = (Calendar) now.clone();

            int requiredMin;
            try {
                requiredMin = extractRequiredMinOfDay();
            } catch (ParseException e) {
                LOG.error("Invalid time definition", e);
                return 24 * 60;
            }

            cal.add(Calendar.MINUTE, 1);

            for (int i = 0; i < 14 * 24 * 60; i++) {
                Date candidate = cal.getTime();

                if (predicate.test(candidate)) {
                    int candidateMin
                            = cal.get(Calendar.HOUR_OF_DAY) * 60
                            + cal.get(Calendar.MINUTE);

                    // ✅ CRITICAL FIX: match time as well
                    if (candidateMin == requiredMin) {
                        long diffMillis
                                = cal.getTimeInMillis() - now.getTimeInMillis();
                        return TimeUnit.MILLISECONDS.toMinutes(diffMillis);
                    }
                }
                cal.add(Calendar.MINUTE, 1);
            }

            return 7 * 24 * 60; // fallback
        }

//    private synchronized void schedule(Runnable runnable) {
//      try {
//        long delay = calcDelay();
//        LOG.info(ESAPI.encoder().encodeForHTML(String.format("Task is scheduled to be run in %d minues: %s", delay, triggerDefinition.getTaskDefinition())));
//        if(delay > 1439) //Delay should be less than 24*60
//        {
//             LOG.error("Error activating trigger. Delay is greater than 1 day");
//        }
//        else
//        {
//            future = service.schedule(runnable, delay, TimeUnit.MINUTES);
//        }        
//      } catch (ParseException ex) {
//        LOG.error(String.format("Error activating trigger: %s", getType()), ex);
//      }
//    }
        /**
         * Calculates delay (in minutes)
         *
         * @return delay
         * @throws ParseException if extracting minute of the day failed
         */
//    private long calcDelay() throws ParseException {
//      int reqMinOfDay = getMinOfDay();
//      Calendar cal = Calendar.getInstance();
//      int curMinOfDay = extractMinOfDay(cal);
//
//      int dif = reqMinOfDay - curMinOfDay;
//      long delay = dif>0? dif: (24*60)-curMinOfDay+reqMinOfDay; 
//
//      return delay;
//    }
        /**
         * Gets minute of the day from trigger definition.
         *
         * @return minute of the day.
         * @throws ParseException if extracting minute of the day failed
         */
        private int getMinOfDay() throws ParseException {
            return parseMinOfDay(triggerDefinition.getProperties().get(T_AT_TIME));
        }

        /**
         * Parses minute of the day. Format: HH:mm.
         *
         * @param strMinOfDay minute of the day definition
         * @return minute of the day.
         * @throws ParseException if invalid minute of the day definition
         */
        private int parseMinOfDay(String strMinOfDay) throws ParseException {
            if (strMinOfDay == null) {
                throw new ParseException(String.format("Invalid minute of the day: %s", strMinOfDay), 0);
            }
            String[] split = strMinOfDay.split(":");
            if (split.length < 2) {
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
            return hour * 60 + min;
        }

        private Predicate<Date> parsePredicate(String strPredicate) throws ParseException {
            if (strPredicate == null) {
                throw new ParseException(String.format("Invalid predicate: %s", strPredicate), 0);
            }
            String[] split = strPredicate.split(":");
            ArrayList<Predicate<Date>> predicates = new ArrayList<>();

            // day of the week
            if (split.length >= 3) {
                String dayOfTheWeek = split[2];
                if (!"*".equals(dayOfTheWeek)) {
                    ArrayList<Predicate<Date>> pred = new ArrayList<>();
                    Arrays.asList(dayOfTheWeek.split(",")).forEach(str -> {
                        try {
                            int n = Integer.parseInt(str);
                            pred.add((d) -> {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(d);
                                return cal.get(Calendar.DAY_OF_WEEK) == n;
                            });
                        } catch (NumberFormatException ex) {
                            LOG.warn(formatForLog("Invalid day of the week definition: %s", str), ex);
                        }
                    });
                    predicates.add((d) -> pred.stream().map(p -> p.test(d)).anyMatch(b -> b == true));
                }
            }

            // week of the month
            if (split.length >= 4) {
                String weekOfTheMonth = split[3];
                if (!"*".equals(weekOfTheMonth)) {
                    ArrayList<Predicate<Date>> pred = new ArrayList<>();
                    Arrays.asList(weekOfTheMonth.split(",")).forEach(str -> {
                        try {
                            int n = Integer.parseInt(str);
                            pred.add((d) -> {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(d);
                                return cal.get(Calendar.WEEK_OF_MONTH) == n;
                            });
                        } catch (NumberFormatException ex) {
                            LOG.warn(formatForLog("Invalid week of the month definition: %s", str), ex);
                        }
                    });
                    predicates.add((d) -> pred.stream().map(p -> p.test(d)).anyMatch(b -> b == true));
                }
            }

            // month of the year
            if (split.length >= 5) {
                String monthOfTheYear = split[4];
                if (!"*".equals(monthOfTheYear)) {
                    ArrayList<Predicate<Date>> pred = new ArrayList<>();
                    Arrays.asList(monthOfTheYear.split(",")).forEach(str -> {
                        try {
                            int n = Integer.parseInt(str);
                            pred.add((d) -> {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(d);
                                return cal.get(Calendar.MONTH) == n;
                            });
                        } catch (NumberFormatException ex) {
                            LOG.warn(formatForLog("Invalid month definition: %s", str), ex);
                        }
                    });
                    predicates.add((d) -> pred.stream().map(p -> p.test(d)).anyMatch(b -> b == true));
                }
            }

            return (d) -> !predicates.stream().map(p -> p.test(d)).anyMatch(b -> b == false);
        }

        /**
         * Gets minute of the day.
         *
         * @param cal calendar
         * @return minute of the day.
         */
//        private int extractMinOfDay(Calendar cal) {
//            int minOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
//            return minOfDay;
//        }
    }
}
