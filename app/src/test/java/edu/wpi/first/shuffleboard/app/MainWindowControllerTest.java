package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.components.WidgetTile;

import edu.wpi.first.networktables.NetworkTableInstance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.TextMatchers;
import org.testfx.util.WaitForAsyncUtils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Move to network table plugin tests")
@Tag("UI")
public class MainWindowControllerTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
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
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    inst.getEntry("/a string source").setString("foo");
    inst.waitForEntryListenerQueue(-1.0);
    WaitForAsyncUtils.waitForFxEvents();

    drag(TextMatchers.hasText("a string source"), MouseButton.PRIMARY)
        .dropTo(".widget-pane");

    WidgetTile tile = lookup(".widget-pane .tile").query();
    assertNotNull(tile);
  }

  @Test
  @Tag("NonHeadlessTests")
  public void testNetworkTableSourceContextMenu() {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    inst.getEntry("/testSourceContextMenu").setString("value");
    inst.waitForEntryListenerQueue(-1.0);
    WaitForAsyncUtils.waitForFxEvents();

    rightClickOn(TextMatchers.hasText("testSourceContextMenu"));
    Node showAsText = lookup(TextMatchers.hasText("Show as: Text View")).query();
    assertNotNull(showAsText);
    clickOn(showAsText);

    WidgetTile tile = lookup(".tile").query();
    assertNotNull(tile);
    Widget widget = tile.getContent();
    DataSource source = widget.getSources().get(0);
    assertTrue(source.isActive());
    assertEquals("testSourceContextMenu", source.getName());
    assertEquals("value",source.getData());
  }

}
