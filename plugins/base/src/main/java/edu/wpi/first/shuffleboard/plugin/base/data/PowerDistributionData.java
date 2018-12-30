package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * A complex data object that contains the current draw of every PDP channel, as well as the voltage and total current
 * draw being sourced by the PDP to all connected components.
 */
public class PowerDistributionData extends ComplexData<PowerDistributionData> {

  private static final int NUM_CHANNELS = 16;

  private final double[] currents;
  private final double voltage;
  private final double totalCurrent;

  /**
   * Creates a new data object for PDP data.
   *
   * @param currents     an array of the currents. This <i>must</i> have 16 entries
   * @param voltage      the current voltage of the PDP
   * @param totalCurrent the total current sourced by the PDP
   */
  public PowerDistributionData(double[] currents, double voltage, double totalCurrent) {
    if (currents.length != NUM_CHANNELS) {
      throw new IllegalArgumentException("Require " + NUM_CHANNELS + " channels, but was " + currents.length);
    }
    this.currents = currents.clone();
    this.voltage = voltage;
    this.totalCurrent = totalCurrent;
  }

  /**
   * Creates a new data object for PDP data.
   */
  public PowerDistributionData(Map<String, Object> map) {
    this(
        extractChannels(map),
        (Double) map.getOrDefault("Voltage", 0.0),
        (Double) map.getOrDefault("TotalCurrent", 0.0)
    );
  }

  private static double[] extractChannels(Map<String, Object> map) {
    return map.entrySet().stream()
        .filter(e -> e.getKey().matches("^Chan[0-9]+$"))
        .filter(e -> e.getValue() instanceof Number)
        .sorted(Comparator.comparing(Map.Entry::getKey, AlphanumComparator.INSTANCE))
        .mapToDouble(e -> ((Number) e.getValue()).doubleValue())
        .toArray();
  }

  /**
   * Gets an array of all the currents of the PDP. The array index of a specific channels current is the same as that
   * channels number; channel 0 current is at array index 0, channel 1 current is at array index 1, and so on.
   */
  public double[] getCurrents() {
    return currents.clone(); // defensive copy
  }

  /**
   * Gets the current voltage being sourced by the PDP.
   */
  public double getVoltage() {
    return voltage;
  }

  /**
   * Gets the total current draw of the PDP.
   */
  public double getTotalCurrent() {
    return totalCurrent;
  }

  @Override
  public Map<String, Object> asMap() {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    for (int i = 0; i < NUM_CHANNELS; i++) {
      builder.put("Chan" + i, currents[i]);
    }
    builder.put("Voltage", voltage);
    builder.put("TotalCurrent", totalCurrent);
    return builder.build();
  }

  @Override
  public String toHumanReadableString() {
    return String.format("voltage=%.3f Volts, currents=%s, totalCurrent=%.3f Amps",
        voltage,
        Arrays.toString(currents),
        totalCurrent
    );
  }
}
