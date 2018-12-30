package edu.wpi.first.shuffleboard.plugin.base.data;

import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.util.Map;
import java.util.Objects;

public final class PIDControllerData extends ComplexData<PIDControllerData> {

  private final double p;
  private final double i;
  private final double d;
  private final double f;
  private final double setpoint;
  private final boolean enabled;

  /**
   * Creates a new PIDController data object.
   *
   * @param p        the proportional constant
   * @param i        the integral constant
   * @param d        the derivative constant
   * @param f        the feedforward constant
   * @param setpoint the controller setpoint
   * @param enabled  whether or not the controller is enabled
   */
  public PIDControllerData(double p, double i, double d, double f, double setpoint, boolean enabled) {
    this.p = p;
    this.i = i;
    this.d = d;
    this.f = f;
    this.setpoint = setpoint;
    this.enabled = enabled;
  }

  /**
   * Creates a new data object from a map. The map should contain values for all the properties of the data object. If
   * a value is missing, the default value of {@code 0} (for numbers) or {@code false} (for booleans) is used.
   */
  public PIDControllerData(Map<String, Object> map) {
    this((double) map.getOrDefault("p", 0.0),
        (double) map.getOrDefault("i", 0.0),
        (double) map.getOrDefault("d", 0.0),
        (double) map.getOrDefault("f", 0.0),
        (double) map.getOrDefault("setpoint", 0.0),
        (boolean) map.getOrDefault("enabled", false));
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

  public double getF() {
    return f;
  }

  public double getSetpoint() {
    return setpoint;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public PIDControllerData withP(double p) {
    return new PIDControllerData(p, i, d, f, setpoint, enabled);
  }

  public PIDControllerData withI(double i) {
    return new PIDControllerData(p, i, d, f, setpoint, enabled);
  }

  public PIDControllerData withD(double d) {
    return new PIDControllerData(p, i, d, f, setpoint, enabled);
  }

  public PIDControllerData withF(double f) {
    return new PIDControllerData(p, i, d, f, setpoint, enabled);
  }

  public PIDControllerData withSetpoint(double setpoint) {
    return new PIDControllerData(p, i, d, f, setpoint, enabled);
  }

  public PIDControllerData withEnabled(boolean enabled) {
    return new PIDControllerData(p, i, d, f, setpoint, enabled);
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.<String, Object>builder()
        .put("p", p)
        .put("i", i)
        .put("d", d)
        .put("f", f)
        .put("setpoint", setpoint)
        .put("enabled", enabled)
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
        && this.f == that.f
        && this.setpoint == that.setpoint
        && this.enabled == that.enabled;
  }

  @Override
  public int hashCode() {
    return Objects.hash(p, i, d, f, setpoint, enabled);
  }

  @Override
  public String toString() {
    return String.format("PIDControllerData(p=%s, i=%s, d=%s, f=%s, setpoint=%s, enabled=%s)",
        p, i, d, f, setpoint, enabled);
  }

  @Override
  public String toHumanReadableString() {
    return String.format("p=%.3f, i=%.3f, d-%.3f, f=%.3f, setpoint=%.3f, enabled=%s", p, i, d, f, setpoint, enabled);
  }
}
