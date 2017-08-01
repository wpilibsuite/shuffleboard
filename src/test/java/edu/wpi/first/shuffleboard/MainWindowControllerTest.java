package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.WidgetTile;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
public class MainWindowControllerTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    Widgets.discover();
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource("MainWindow.fxml"));
    Pane root = loader.load();
    stage.setScene(new Scene(root));
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

  @Test
  public void testDragSingleNetworkTableSourceToWidgetPane() {
    NetworkTablesJNI.putString("/a string source", "foo");
    NetworkTableUtils.waitForNtcoreEvents();
    WaitForAsyncUtils.waitForFxEvents();

    drag(NodeMatchers.hasText("a string source"), MouseButton.PRIMARY)
        .dropTo(".widget-pane");

    WidgetTile tile = lookup(".widget-pane .tile").query();
    assertNotNull(tile);
  }

  @Test
  @Tag("NonHeadlessTests")
  public void testNetworkTableSourceContextMenu() {
    NetworkTablesJNI.putString("/testSourceContextMenu", "value");
    NetworkTableUtils.waitForNtcoreEvents();
    WaitForAsyncUtils.waitForFxEvents();

    rightClickOn(NodeMatchers.hasText("testSourceContextMenu"));
    Node showAsText = lookup(NodeMatchers.hasText("Show as: Text View")).query();
    assertNotNull(showAsText);
    clickOn(showAsText);

    WidgetTile tile = lookup(".tile").query();
    assertNotNull(tile);
    Widget widget = tile.getWidget();
    assertTrue(widget.getSource().isActive());
    assertEquals("testSourceContextMenu", widget.getSource().getName());
    assertEquals("value", widget.getSource().getData());
  }

}
