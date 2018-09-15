package edu.wpi.first.shuffleboard.app.components;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@Tag("UI")
public class DashboardTabPaneTest extends ApplicationTest {

  private DashboardTabPane tabPane;

  @Override
  public void start(Stage stage) {
    tabPane = new DashboardTabPane();
    stage.setScene(new Scene(tabPane));
    stage.show();
  }

  @Test
  public void testCloseTabKeepsSelectedIndex() {
    // Add three tabs, select the second one, then close it
    // The selected index should remain 1
    Platform.runLater(() -> {
      tabPane.addNewTab();
      tabPane.addNewTab();
      tabPane.addNewTab();
      tabPane.getSelectionModel().select(1);
      tabPane.closeCurrentTab();
    });
    waitForFxEvents();
    assertEquals(1, tabPane.getSelectionModel().getSelectedIndex(), "Wrong selected tab index");
  }

  @Test
  public void testCloseRightmostTabDecrementsIndex() {
    // Add four tabs, select the fourth one, then close it
    // The selected index should decrement from 3 to 2
    Platform.runLater(() -> {
      tabPane.addNewTab();
      tabPane.addNewTab();
      tabPane.addNewTab();
      tabPane.addNewTab();
      tabPane.getSelectionModel().select(3);
      tabPane.closeCurrentTab();
    });
    waitForFxEvents();
    assertEquals(2, tabPane.getSelectionModel().getSelectedIndex(), "Wrong selected tab index");
  }

}
