package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.app.components.WidgetTile;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.widget.Widgets;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.Assert.*;

public class MainWindowControllerTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    Widgets.discover();
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource("MainWindow.fxml"));
    Pane root = loader.load();
    stage.setScene(new Scene(root));
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
  @Category(NonHeadlessTests.class)
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
