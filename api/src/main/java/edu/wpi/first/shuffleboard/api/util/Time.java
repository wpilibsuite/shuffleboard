package edu.wpi.first.shuffleboard.api.util;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 * Utility class for working with the current time. Useful for widgets that display historical data and need to know
 * when that data appeared.
 */
public final class Time {

  private static final LongProperty startTime = new SimpleLongProperty(Time.class, "startTime", 0);

  private Time() {
  }

  /**
   * Gets the current time in epoch milliseconds according to the system clock.
   */
  public static long now() {
    return System.currentTimeMillis();
  }

  /**
   * Gets the start time of the current application run in epoch milliseconds.
   */
  public static long getStartTime() {
    return startTime.get();
  }

  public static LongProperty startTimeProperty() {
    return startTime;
  }

  /**
   * Sets the start time of the current application run in epoch milliseconds.
   */
  public static void setStartTime(long startTime) {
    Time.startTime.set(startTime);
  }

}