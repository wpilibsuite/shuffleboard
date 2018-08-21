package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;

import edu.wpi.first.networktables.NetworkTableInstance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecorderControllerTest extends ApplicationTest {

  private static final Logger log = Logger.getLogger(RecorderControllerTest.class.getName());

  private NetworkTableInstance ntInstance;
  private Recorder recorder;
  private RecorderController controller;

  @BeforeEach
  public void setup() {
    ntInstance = NetworkTableInstance.create();
    ntInstance.setUpdateRate(0.01);
    recorder = Recorder.createDummyInstance();
    controller = new RecorderController(
        ntInstance,
        RecorderController.DEFAULT_START_STOP_KEY,
        RecorderController.DEFAULT_FILE_NAME_FORMAT_KEY,
        recorder
    );
  }

  @AfterEach
  public void tearDownNetworktables() {
    NetworkTableUtils.shutdown(ntInstance);
    DashboardMode.setCurrentMode(DashboardMode.NORMAL);
  }

  @Test
  public void testSetOnStartup() {
    final String format = "already-set-when-connected-${time}";
    ntInstance.getEntry(RecorderController.DEFAULT_START_STOP_KEY).setBoolean(true);
    ntInstance.getEntry(RecorderController.DEFAULT_FILE_NAME_FORMAT_KEY).setString(format);
    controller.start();
    sleep(20, TimeUnit.MILLISECONDS); // wait for nt events
    assertAll(
        () -> assertTrue(recorder.isRunning(), "Recorder should be running"),
        () -> assertEquals(format, recorder.getFileNameFormat(), "File name format should have been set")
    );
  }

  @Test
  public void testStartWhenEntryUpdates() {
    controller.start();
    ntInstance.getEntry(RecorderController.DEFAULT_START_STOP_KEY).setBoolean(true);
    sleep(20, TimeUnit.MILLISECONDS);
    assertTrue(recorder.isRunning(), "Recorder should have been started");
  }

  @Test
  public void testNoUpdatesWhenNotRunning() {
    controller.start();
    controller.stop();
    sleep(20, TimeUnit.MILLISECONDS);
    ntInstance.getEntry(RecorderController.DEFAULT_START_STOP_KEY).setBoolean(true);
    assertFalse(recorder.isRunning(), "Recording should not be running");
  }

  @Test
  public void testStopsRecorder() {
    controller.start();
    ntInstance.getEntry(RecorderController.DEFAULT_START_STOP_KEY).setBoolean(true);
    sleep(20, TimeUnit.MILLISECONDS);
    assertTrue(recorder.isRunning());
    ntInstance.getEntry(RecorderController.DEFAULT_START_STOP_KEY).setBoolean(false);
    sleep(20, TimeUnit.MILLISECONDS);
    assertFalse(recorder.isRunning(), "Recorder should have been stopped");
  }

  @Test
  public void testDoNothingIfInPlayBack() {
    controller.start();
    DashboardMode.setCurrentMode(DashboardMode.PLAYBACK);
    ntInstance.getEntry(RecorderController.DEFAULT_START_STOP_KEY).setBoolean(true);
    sleep(20, TimeUnit.MILLISECONDS);
    assertFalse(recorder.isRunning(), "Recorder should not have been started");
  }
}