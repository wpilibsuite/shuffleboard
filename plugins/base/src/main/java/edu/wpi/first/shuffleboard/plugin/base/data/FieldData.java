package edu.wpi.first.shuffleboard.plugin.base.data;

import com.google.common.collect.ImmutableMap;
import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FieldData extends ComplexData<FieldData> {
  private final SimplePose2d robot;
  private final Map<String, SimplePose2d[]> objects;

  public static class SimplePose2d {
    private final double x;
    private final double y;
    private final double degrees;

    @SuppressWarnings("JavadocMethod")
    public SimplePose2d(double x, double y, double degrees) {
      this.x = x;
      this.y = y;
      this.degrees = degrees;
    }

    @SuppressWarnings("JavadocMethod")
    public SimplePose2d(double[] data) {
      this.x = data[0];
      this.y = data[1];
      this.degrees = data[2];
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    public double getDegrees() {
      return degrees;
    }
  }

  public FieldData(SimplePose2d robot, Map<String, SimplePose2d[]> objects) {
    this.robot = robot;
    this.objects = objects;
  }

  @SuppressWarnings("JavadocMethod")
  public FieldData(Map<String, Object> map) {
    this.robot = new SimplePose2d((double[]) map.getOrDefault("Robot", new double[3]));
    this.objects = new HashMap<>();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      if (key.equals("Robot") || key.startsWith(".")) {
        continue;
      }

      double[] doubles;
      Object value = entry.getValue();
      if (value instanceof double[]) {
        // Intended flow
        doubles = (double[]) value;
      } else if (entry.getValue() instanceof byte[]) {
        // double[] encoded as byte[] is supported too (for some reason)
        byte[] data = (byte[]) value;
        doubles = new double[data.length / Double.BYTES];
        for (int i = 0; i < doubles.length; i++) {
          doubles[i] = ByteBuffer.allocate(Double.BYTES)
                  .put(data, Double.BYTES * i, Double.BYTES)
                  .flip()
                  .getDouble();
        }
      } else {
        // Some other type (might be an externally-published topic under the same table)
        // Warn and drop, but don't raise an exception
        doubles = new double[0];
        Logger.getLogger(FieldData.class.getName())
                .warning(() -> String.format(
                        "Failed to parse entry %s of type %s: %s",
                        key, value.getClass(), value));
      }

      SimplePose2d[] poses = new SimplePose2d[doubles.length / 3];
      for (int i = 0; i < poses.length; i++) {
        poses[i] = new SimplePose2d(doubles[3 * i], doubles[3 * i + 1], doubles[3 * i + 2]);
      }

      this.objects.put(key, poses);
    }
  }

  public SimplePose2d getRobot() {
    return robot;
  }

  public Map<String, SimplePose2d[]> getObjects() {
    return objects;
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.of("Robot", robot);
  }
}
