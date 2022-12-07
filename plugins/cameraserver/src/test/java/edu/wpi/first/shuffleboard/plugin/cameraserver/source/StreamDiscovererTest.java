package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArrayPublisher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class StreamDiscovererTest {

  private final NetworkTableInstance ntInstance = NetworkTableInstance.create();
  private final NetworkTable rootTable = ntInstance.getTable("CameraPublisher");

  @Test
  public void testArrayChanges() {
    String[] urls = {"foo", "bar"};
    try (final StreamDiscoverer discoverer = new StreamDiscoverer(rootTable, "Camera");
         final StringArrayPublisher publisher = rootTable.getStringArrayTopic("Camera/streams").publish()) {
      publisher.set(urls);
      waitForNtEvents();
      assertArrayEquals(urls, discoverer.getUrls());
    }
  }

  @Test
  public void testInitiallyEmpty() {
    try (final StreamDiscoverer discoverer = new StreamDiscoverer(rootTable, "Camera")) {
      assertArrayEquals(new String[0], discoverer.getUrls(), "Initial URL array should be empty");
    }
  }

  @Test
  public void testEmptyWhenIncorrectType() {
    try (final StreamDiscoverer discoverer = new StreamDiscoverer(rootTable, "Camera");
         final DoublePublisher publisher = rootTable.getDoubleTopic("Camera/streams").publish()) {
      publisher.set(12.34);
      waitForNtEvents();
      assertArrayEquals(new String[0], discoverer.getUrls());
    }
  }

  @Test
  public void testClose() {
    final StreamDiscoverer discoverer = new StreamDiscoverer(rootTable, "Camera");

    String[] urls = {"foo", "bar"};
    try (final StringArrayPublisher publisher = rootTable.getStringArrayTopic("Camera/streams").publish()) {
      publisher.set(urls);
      waitForNtEvents();

      discoverer.close();

      publisher.set(new String[]{"bar", "foo"});
      waitForNtEvents();

      assertArrayEquals(new String[0], discoverer.getUrls());
    }
  }

  private void waitForNtEvents() {
    if (!ntInstance.waitForListenerQueue(0.5)) {
      fail("Timed out while waiting for entry listeners to fire");
    }
  }

}
