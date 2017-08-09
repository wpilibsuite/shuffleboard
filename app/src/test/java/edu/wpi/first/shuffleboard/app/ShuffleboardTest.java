package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class ShuffleboardTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    Pane mainPane = FXMLLoader.load(MainWindowController.class.getResource("MainWindow.fxml"));
    Scene scene = new Scene(mainPane);
    stage.setScene(scene);
    stage.show();
  }

  @BeforeEach
  public void setUp() {
    NetworkTableUtils.shutdown();
  }

  @AfterEach
  public void tearDown() {
    NetworkTableUtils.shutdown();
  }

  // TODO

}
