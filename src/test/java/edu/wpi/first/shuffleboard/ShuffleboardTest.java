package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.testfx.framework.junit.ApplicationTest;

public class ShuffleboardTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    Pane mainPane = FXMLLoader.load(MainWindowController.class.getResource("MainWindow.fxml"));
    Scene scene = new Scene(mainPane);
    stage.setScene(scene);
    stage.show();
  }

  @Before
  public void setUp() {
    NetworkTableUtils.shutdown();
  }

  @After
  public void tearDown() {
    NetworkTableUtils.shutdown();
  }

  // TODO

}
