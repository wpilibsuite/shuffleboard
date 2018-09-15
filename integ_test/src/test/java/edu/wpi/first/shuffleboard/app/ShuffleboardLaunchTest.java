package edu.wpi.first.shuffleboard.app;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.stage.Stage;

@Tag("UI")
public class ShuffleboardLaunchTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    final Shuffleboard shuffleboard = new Shuffleboard();
    shuffleboard.init();
    shuffleboard.start(stage);
  }

  @Test
  public void launchIsSuccessful() {
    // NO-OP
  }
}
