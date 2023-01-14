package edu.wpi.first.shuffleboard.plugin.base.data;

import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.util.Map;
import java.util.Objects;

public final class PIDControllerData extends ComplexData<PIDControllerData> {

  private final double p;
  private final double i;
  private final double d;
  private final double setpoint;

  /**
   * Creates a new PIDController data object.
   *
   * @param p        the proportional constant
   * @param i        the integral constant
   * @param d        the derivative constant
   * @param setpoint the controller setpoint
   */
  public PIDControllerData(double p, double i, double d, double setpoint) {
    this.p = p;
    this.i = i;
    this.d = d;
    this.setpoint = setpoint;
  }

  /**
   * Creates a new data object from a map. The map should contain values for all the properties of the data object. If
   * a value is missing, the default value of {@code 0} (for numbers) is used.
   */
  public PIDControllerData(Map<String, Object> map) {
    this((double) map.getOrDefault("p", 0.0),
        (double) map.getOrDefault("i", 0.0),
        (double) map.getOrDefault("d", 0.0),
        (double) map.getOrDefault("setpoint", 0.0));
  }

  public double getP() {
    return p;
  }

  public double getI() {
    return i;
  }

  public double getD() {
    return d;
  }

  public double getSetpoint() {
    return setpoint;
  }


  public PIDControllerData withP(double p) {
    return new PIDControllerData(p, i, d, setpoint);
  }

  public PIDControllerData withI(double i) {
    return new PIDControllerData(p, i, d, setpoint);
  }

  public PIDControllerData withD(double d) {
    return new PIDControllerData(p, i, d, setpoint);
  }

  public PIDControllerData withSetpoint(double setpoint) {
    return new PIDControllerData(p, i, d, setpoint);
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.<String, Object>builder()
        .put("p", p)
        .put("i", i)
        .put("d", d)
        .put("setpoint", setpoint)
        .build();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    PIDControllerData that = (PIDControllerData) obj;
    return this.p == that.p
        && this.i == that.i
        && this.d == that.d
        && this.setpoint == that.setpoint;
  }

  @Override
  public int hashCode() {
    return Objects.hash(p, i, d, setpoint);
  }

  @Override
  public String toString() {
    return String.format("PIDControllerData(p=%s, i=%s, d=%s, setpoint=%s)",
        p, i, d, setpoint);
  }

  @Override
  public String toHumanReadableString() {
    return String.format("p=%.3f, i=%.3f, d=%.3f, setpoint=%.3f", p, i, d, setpoint);
  }
}
