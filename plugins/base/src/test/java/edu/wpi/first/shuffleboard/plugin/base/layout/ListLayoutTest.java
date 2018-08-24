package edu.wpi.first.shuffleboard.plugin.base.layout;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@Tag("UI")
public class ListLayoutTest extends ApplicationTest {

  private ListLayout layout;

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader loader = new FXMLLoader(ListLayout.class.getResource("ListLayout.fxml"));
    loader.load();
    layout = loader.getController();
    Scene scene = new Scene(layout.getView());
    stage.setScene(scene);
    stage.show();
  }

  @Test
  public void testChildrenAdded() {
    Platform.runLater(() -> {
      layout.addChild(new MockWidget());
      layout.addChild(new MockWidget());
    });
    waitForFxEvents();
    assertEquals(2, ((VBox) layout.getView().lookup(".layout-container")).getChildren().size());
  }

}
