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
package com.esri.geoportal.harvester.api;

import java.util.List;

/**
 * Data broker UI template.
 * <p>
 * Connector template is used to generate information sufficient to build form UI.
 * @see Connector
 */
public final class ConnectorTemplate {
  private final String type;
  private final String label;
  private final List<Argument> arguments;

  /**
   * Creates instance of the template.
   * @param type type
   * @param label label
   * @param arguments arguments
   */
  public ConnectorTemplate(String type, String label, List<Argument> arguments) {
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
    /** integer type */
    integer,
    /** choice type */
    choice
  }
  
  /**
   * Argument definition.
   * @param <T> data type
   */
  public static interface Argument<T> {
    ArgumentType getType();
    String getName();
    String getLabel();
  }
  
  /**
   * Argument base implementation.
   * @param <T> data type
   */
  public static abstract class ArgumentBase<T> implements Argument<T> {
    private final String name;
    private final String label;
    private final boolean required;

    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     */
    public ArgumentBase(String name, String label) {
      this.name = name;
      this.label = label;
      this.required = false;
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
    public boolean getRequired() {
      return required;
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

    @Override
    public ArgumentType getType() {
      return ArgumentType.string;
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

    /**
     * Creates instance of the argument.
     * @param name type
     * @param label label
     * @param required required
     * @param choices choices
     */
    public ChoiceArgument(String name, String label, boolean required, List<Choice<CT>> choices) {
      super(name, label, required);
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
}
