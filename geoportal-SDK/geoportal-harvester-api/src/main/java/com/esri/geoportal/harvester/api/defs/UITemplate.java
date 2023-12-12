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
package com.esri.geoportal.harvester.api.defs;

import java.util.List;

/**
 * UI template.
 * <p>
 * Connector template is used to generate information sufficient to build form UI.
 * @see com.esri.geoportal.harvester.api.Connector
 */
public final class UITemplate {
  private final String type;
  private final String label;
  private final List<Argument> arguments;

  /**
   * Creates instance of the template.
   * @param type type
   * @param label label
   * @param arguments arguments
   */
  public UITemplate(String type, String label, List<Argument> arguments) {
    this.type = type;
    this.label = label;
    this.arguments = arguments;
  }

  /**
   * Gets type.
   * @return type
   */
  public String getType() {
    return type;
  }

  /**
   * Gets label.
   * @return label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Gets arguments.
   * @return list of arguments
   */
  public List<Argument> getArguments() {
    return arguments;
  }
  
  /**
   * Argument type.
   */
  public static enum ArgumentType {
    /** string type */
    string,
    /** text type */
    text,
    /** integer type */
    integer,
    /** boolean type */
    bool,
    /** choice type */
    choice,
    /** temporal type */
    temporal,
    /** periodical */
    periodical,
    button,
    hidden
    
  }
  
  /**
   * Argument definition.
   * @param <T> data type
   */
  public static interface Argument<T> {
    ArgumentType getType();
    String getName();
    String getLabel();
    String getHint();
    boolean getRequired();
    T getDefaultValue();
  }
  
  /**
   * Argument wrapper.
   * @param <T> data type
   */
  public static class ArgumentWrapper<T> implements Argument<T> {
    private Argument<T> arg;

    public ArgumentWrapper(Argument<T> arg) {
      this.arg = arg;
    }

    @Override
    public ArgumentType getType() {
      return arg.getType();
    }

    @Override
    public String getName() {
      return arg.getName();
    }

    @Override
    public String getLabel() {
      return arg.getLabel();
    }

    @Override
    public String getHint() {
      return arg.getHint();
    }

    @Override
    public boolean getRequired() {
      return arg.getRequired();
    }

    @Override
    public T getDefaultValue() {
      return arg.getDefaultValue();
    }
    
  }
  
  /**
   * Argument base implementation.
   * @param <T> data type
   */
  public static abstract class ArgumentBase<T> implements Argument<T> {
    private final String name;
    private final String label;
    private boolean required;
    private T defaultValue;

    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     */
    public ArgumentBase(String name, String label) {
      this.name = name;
      this.label = label;
      this.required = false;
      this.defaultValue = null;
    }

    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required <code>true</code> if argument is required
     */
    public ArgumentBase(String name, String label, boolean required) {
      this.name = name;
      this.label = label;
      this.required = required;
      this.defaultValue = null;
    }

    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required <code>true</code> if argument is required
     * @param defaultValue default value
     */
    public ArgumentBase(String name, String label, boolean required, T defaultValue) {
      this.name = name;
      this.label = label;
      this.required = required;
      this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getLabel() {
      return label;
    }

    /**
     * Checks if is required.
     * @return <code>true</code> if required
     */
    @Override
    public boolean getRequired() {
      return required;
    }

    @Override
    public String getHint() {
      return null;
    }

    @Override
    public T getDefaultValue() {
      return defaultValue;
    }

    @Override
    public String toString() {
      return String.format("%s(%s,%s,%b)", getType(), getName(), getLabel(),getRequired());
    }
  }
  
    /**
   * String argument.
   */
  public static class StringArgument extends ArgumentBase<String> {
    private String regExp;
    
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     */
    public StringArgument(String name, String label) {
      super(name, label);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required <code>true</code> if argument is required
     */
    public StringArgument(String name, String label, boolean required) {
      super(name, label, required);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required <code>true</code> if argument is required
     * @param defaultValue default value
     */
    public StringArgument(String name, String label, boolean required, String defaultValue) {
      super(name, label, required, defaultValue);
    }

    /**
     * Gets optional regular expression to validate value.
     * @return regular expression (JavaScript style) or <code>null</code>
     */
    public String getRegExp() {
      return regExp;
    }

    @Override
    public ArgumentType getType() {
      return ArgumentType.string;
    }
  }
  
  /**
   * Button argument.
   */
  public static class ButtonArgument extends ArgumentBase<String> { 
      
    /**
  * Creates instance of the argument.
  * @param name type
  * @param label label
  * @param required <code>true</code> if argument is required
  */
    public ButtonArgument(String name, String label, boolean required) {
      super(name, label, required);
    }
    @Override
    public ArgumentType getType() {
      return ArgumentType.button;
    }
  }
  
  /**
   * Button argument.
   */
  public static class HiddenArgument extends ArgumentBase<String> {       
    /**
  * Creates instance of the argument.
  * @param name type
  * @param label label
 */
    public HiddenArgument(String name, String label) {
      super(name, label);
    }
    @Override
    public ArgumentType getType() {
      return ArgumentType.hidden;
    }
  }
  
  /**
   * String argument.
   */
  public static class TextArgument extends ArgumentBase<String> {
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     */
    public TextArgument(String name, String label) {
      super(name, label);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required <code>true</code> if argument is required
     */
    public TextArgument(String name, String label, boolean required) {
      super(name, label, required);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required <code>true</code> if argument is required
     * @param defaultValue defaultValue
     */
    public TextArgument(String name, String label, boolean required, String defaultValue) {
      super(name, label, required, defaultValue);
    }

    @Override
    public ArgumentType getType() {
      return ArgumentType.text;
    }
  }
  
  /**
   * Integer argument.
   */
  public static class IntegerArgument extends ArgumentBase<Integer> {
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     */
    public IntegerArgument(String name, String label) {
      super(name, label);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required required
     */
    public IntegerArgument(String name, String label, boolean required) {
      super(name, label, required);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required required
     * @param defaultValue default value
     */
    public IntegerArgument(String name, String label, boolean required, Integer defaultValue) {
      super(name, label, required, defaultValue);
    }

    @Override
    public ArgumentType getType() {
      return ArgumentType.integer;
    }
  }

  /**
   * Choice.
   * @param <CT> data type of the choice
   */
  public static class Choice<CT> {
    private final String name;
    private final CT value;

    /**
     * Creates instance of the choice.
     * @param name type
     * @param value value
     */
    public Choice(String name, CT value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Gets type.
     * @return type
     */
    public String getName() {
      return name;
    }

    /**
     * Gets value.
     * @return value
     */
    public CT getValue() {
      return value;
    }
    
    @Override
    public String toString() {
      return String.format("%s/%s", getName(), getValue());
    }
  }
  
  /**
   * Choice argument.
   * @param <CT> type of choice data
   */
  public static class ChoiceArgument<CT> extends ArgumentBase<List<CT>> {
    private final List<Choice<CT>> choices;

    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param choices choices
     */
    public ChoiceArgument(String name, String label, List<Choice<CT>> choices) {
      super(name, label);
      this.choices = choices;
    }

    @Override
    public ArgumentType getType() {
      return ArgumentType.choice;
    }
  
    /**
     * Gets choices.
     * @return list of choices
     */
    public List<Choice<CT>> getChoices() {
      return choices;
    }
  }
  
  /**
   * Boolean argument.
   */
  public static class BooleanArgument extends ArgumentBase<Boolean> {
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     */
    public BooleanArgument(String name, String label) {
      super(name, label);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required required
     */
    public BooleanArgument(String name, String label, boolean required) {
      super(name, label, required);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required required
     * @param defaultValue default value
     */
    public BooleanArgument(String name, String label, boolean required, Boolean defaultValue) {
      super(name, label, required, defaultValue);
    }

    @Override
    public ArgumentType getType() {
      return ArgumentType.bool;
    }
  }
  
  /**
   * Temporal argument.
   */
  public static class TemporalArgument extends ArgumentBase<Integer> {
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     */
    public TemporalArgument(String name, String label) {
      super(name, label);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required required
     */
    public TemporalArgument(String name, String label, boolean required) {
      super(name, label, required);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required required
     * @param defaultValue default value
     */
    public TemporalArgument(String name, String label, boolean required, Integer defaultValue) {
      super(name, label, required, defaultValue);
    }

    @Override
    public ArgumentType getType() {
      return ArgumentType.temporal;
    }
  }
  
  /**
   * Periodical argument.
   */
  public static class PeriodicalArgument extends ArgumentBase<Integer> {
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     */
    public PeriodicalArgument(String name, String label) {
      super(name, label);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required required
     */
    public PeriodicalArgument(String name, String label, boolean required) {
      super(name, label, required);
    }
    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required required
     * @param defaultValue default value
     */
    public PeriodicalArgument(String name, String label, boolean required, Integer defaultValue) {
      super(name, label, required, defaultValue);
    }

    @Override
    public ArgumentType getType() {
      return ArgumentType.periodical;
    }
  }
}
