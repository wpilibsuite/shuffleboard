package edu.wpi.first.shuffleboard.plugin.base.data;

import com.google.common.collect.ImmutableMap;
import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class FieldData extends ComplexData<FieldData> {
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

  private final SimplePose2d robot;
  private final Map<String, SimplePose2d[]> objects;

  public FieldData(SimplePose2d robot, Map<String, SimplePose2d[]> objects) {
    this.robot = robot;
    this.objects = objects;
  }

  @SuppressWarnings("JavadocMethod")
  public FieldData(Map<String, Object> map) {
    this.robot = new SimplePose2d((double[]) map.get("Robot"));
    this.objects = new HashMap<>();

    for (String key : map.keySet()) {
      if (key.equals("Robot") || key.startsWith(".")) {
        continue;
      }

      double[] doubles;
      if (map.get(key) instanceof byte[]) {
        byte[] data = (byte[]) map.get(key);
        doubles = new double[data.length / Double.BYTES];
        for (int i = 0; i < doubles.length; i++) {
          ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
          // this implementation assumes Double.BYTES is 8
          buffer.put(new byte[]{
                  data[8 * i], data[8 * i + 1], data[8 * i + 2], data[8 * i + 3],
                  data[8 * i + 4], data[8 * i + 5], data[8 * i + 6], data[8 * i + 7]
          });
          buffer.flip();
          doubles[i] = buffer.getDouble();
        }
      } else {
        doubles = (double[]) map.get(key);
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
