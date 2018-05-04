/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.geoportal.harvester.sink;

/**
 * Sink context.
 */
/*package*/ class SinkContext {
  public final int attemptCount;
  public final long attemptDelay;

  /**
   * Creates instance of the sink context.
   * @param attemptCount number of IO attempts
   * @param attemptDelay delay between IO attempts
   */
  public SinkContext(int attemptCount, long attemptDelay) {
    this.attemptCount = attemptCount;
    this.attemptDelay = attemptDelay;
  }
}
