package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.LayoutClass;
import edu.wpi.first.shuffleboard.app.components.LayoutTile;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;
import edu.wpi.first.shuffleboard.app.components.WidgetTile;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@Tag("UI")
public class WidgetPaneControllerTest extends ApplicationTest {

  private WidgetPane pane;
  private WidgetTile firstTile;
  private WidgetTile secondTile;
  private WidgetTile thirdTile;

  @BeforeAll
  public static void setUp() {
    Components.setDefault(new Components());
    Components.getDefault().register(new LayoutClass<>("Mock Layout", MockLayout.class));
  }

  @AfterAll // you're my wonder wall
  public static void tearDown() {
    Components.setDefault(new Components());
  }

  @Override
  public void start(Stage stage) {
    pane = new WidgetPane();
    pane.setNumColumns(3);
    pane.setNumRows(2);
    firstTile = pane.addWidget(new MockWidget());
    secondTile = pane.addWidget(new MockWidget());
    thirdTile = pane.addWidget(new MockWidget());
    stage.setScene(new Scene(pane));
    stage.getScene().getStylesheets().setAll("/edu/wpi/first/shuffleboard/api/base.css");
    stage.show();
  }

  @Test
  @Tag("NonHeadlessTests")
  public void testAddMultipleTilesToLayout() {
    drag(pane.localToScreen(150, 150))
        .drag()
        .dropBy(-30, -30);
    rightClickOn(firstTile);
    moveTo(hasText("Add to new layout..."));
    clickOn("Mock Layout");
    waitForFxEvents();
    assertFalse(pane.getChildren().contains(firstTile), "First tile should have been removed");
    assertFalse(pane.getChildren().contains(secondTile), "Second tile should have been removed");
    assertTrue(pane.getChildren().contains(thirdTile), "Third tile should not have been removed");
    Tile<?> layoutTile = pane.getTiles().get(pane.getTiles().size() - 1);
    assertEquals(LayoutTile.class, layoutTile.getClass(), "Not  layout tile");
    MockLayout layout = (MockLayout) layoutTile.getContent();
    assertEquals(2, layout.getChildren().size(), "Should have been two children in the layout");
    assertAll("All children should be mock widgets",
        layout.getChildren()
            .stream()
            .map(c -> () -> assertEquals(MockWidget.class, c.getClass()))
    );
  }

}
