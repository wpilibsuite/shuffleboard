package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class StreamDiscovererTest {

  private final NetworkTableInstance ntInstance = NetworkTableInstance.create();
  private final NetworkTable rootTable = ntInstance.getTable("CameraPublisher");

  @Test
  public void testArrayChanges() {
    final StreamDiscoverer discoverer = new StreamDiscoverer(rootTable, "Camera");
    String[] urls = {"foo", "bar"};
    rootTable.getEntry("Camera/streams").setStringArray(urls);
    waitForNtEvents();

    assertArrayEquals(urls, discoverer.getUrls());
  }

  @Test
  public void testInitiallyEmpty() {
    final StreamDiscoverer discoverer = new StreamDiscoverer(rootTable, "Camera");

    assertArrayEquals(new String[0], discoverer.getUrls(), "Initial URL array should be empty");
  }

  @Test
  public void testEmptyWhenIncorrectType() {
    StreamDiscoverer discoverer = new StreamDiscoverer(rootTable, "Camera");

    rootTable.getEntry("Camera/streams").setDouble(12.34);
    waitForNtEvents();

    assertArrayEquals(new String[0], discoverer.getUrls());
  }

  @Test
  public void testEmptyWhenTypeChanges() {
    final StreamDiscoverer discoverer = new StreamDiscoverer(rootTable, "Camera");
    String[] urls = {"foo", "bar"};
    rootTable.getEntry("Camera/streams").setStringArray(urls);
    waitForNtEvents();

    rootTable.getEntry("Camera/streams").forceSetBoolean(false);
    waitForNtEvents();

    assertArrayEquals(new String[0], discoverer.getUrls());
  }

  @Test
  public void testClose() {
    final StreamDiscoverer discoverer = new StreamDiscoverer(rootTable, "Camera");
    String[] urls = {"foo", "bar"};
    rootTable.getEntry("Camera/streams").setStringArray(urls);
    waitForNtEvents();

    discoverer.close();

    rootTable.getEntry("Camera/streams").setStringArray(new String[]{"bar", "foo"});
    waitForNtEvents();

    assertArrayEquals(new String[0], discoverer.getUrls());
  }

  private void waitForNtEvents() {
    if (!ntInstance.waitForEntryListenerQueue(0.5)) {
      fail("Timed out while waiting for entry listeners to fire");
    }
  }

}
