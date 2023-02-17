package edu.wpi.first.shuffleboard.plugin.base.data;

import com.google.common.collect.ImmutableMap;
import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.util.Map;
import java.util.Objects;

/**
 * Data for a profiled PID controller from WPILib. WPILib currently sends P, I, D, and the goal position; this makes
 * it effectively identical to {@link PIDControllerData}.  Future updates to WPILib to fully support profiled PID
 * controllers will result in extra fields in the data; for example, the goal velocity or trapezoidal constraints.
 */
public final class ProfiledPIDControllerData extends ComplexData<ProfiledPIDControllerData> {

  private final double p;
  private final double i;
  private final double d;
  private final double goal;

  /**
   * Creates a new PIDController data object.
   *
   * @param p        the proportional constant
   * @param i        the integral constant
   * @param d        the derivative constant
   * @param goal the controller goal
   */
  public ProfiledPIDControllerData(double p, double i, double d, double goal) {
    this.p = p;
    this.i = i;
    this.d = d;
    this.goal = goal;
  }

  /**
   * Creates a new data object from a map. The map should contain values for all the properties of the data object. If
   * a value is missing, the default value of {@code 0} (for numbers) is used.
   */
  public ProfiledPIDControllerData(Map<String, Object> map) {
    this((double) map.getOrDefault("p", 0.0),
        (double) map.getOrDefault("i", 0.0),
        (double) map.getOrDefault("d", 0.0),
        (double) map.getOrDefault("goal", 0.0));
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

  public double getGoal() {
    return goal;
  }


  public ProfiledPIDControllerData withP(double p) {
    return new ProfiledPIDControllerData(p, i, d, goal);
  }

  public ProfiledPIDControllerData withI(double i) {
    return new ProfiledPIDControllerData(p, i, d, goal);
  }

  public ProfiledPIDControllerData withD(double d) {
    return new ProfiledPIDControllerData(p, i, d, goal);
  }

  public ProfiledPIDControllerData withGoal(double goal) {
    return new ProfiledPIDControllerData(p, i, d, goal);
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.<String, Object>builder()
        .put("p", p)
        .put("i", i)
        .put("d", d)
        .put("goal", goal)
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
    ProfiledPIDControllerData that = (ProfiledPIDControllerData) obj;
    return this.p == that.p
        && this.i == that.i
        && this.d == that.d
        && this.goal == that.goal;
  }

  @Override
  public int hashCode() {
    return Objects.hash(p, i, d, goal);
  }

  @Override
  public String toString() {
    return String.format("ProfiledPIDControllerData(p=%s, i=%s, d=%s, goal=%s)",
        p, i, d, goal);
  }

  @Override
  public String toHumanReadableString() {
    return String.format("p=%.3f, i=%.3f, d=%.3f, goal=%.3f", p, i, d, goal);
  }
}
