package edu.wpi.first.shuffleboard.app;

import javafx.stage.Stage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

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
